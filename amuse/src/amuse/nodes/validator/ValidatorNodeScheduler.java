/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2010 by code authors
 * 
 * Created at TU Dortmund, Chair of Algorithm Engineering
 * (Contact: <http://ls11-www.cs.tu-dortmund.de>) 
 *
 * AMUSE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMUSE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Creation date: 14.01.2008
 */ 
package amuse.nodes.validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Level;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetInput;
import amuse.data.io.FileInput;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.NodeEvent;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.NodeScheduler;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitionsDescription;
import amuse.nodes.validator.interfaces.ValidatorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * ValidationNodeScheduler configures and runs the appropriate classifier validation method.
 * 
 * @author Igor Vatolkin
 * @version $Id: ValidatorNodeScheduler.java 1226 2010-08-02 14:13:57Z waeltken $
 */
public class ValidatorNodeScheduler extends NodeScheduler { 

	/** Interface for validation method */
	ValidatorInterface vmi = null;
	
	/** Parameters for validation algorithm if required */
	private String requiredParameters = null;
	
	/** Personal user category id and name separated by '-', e.g. "15-Music_for_Inspiration" */
	private String categoryDescription = null;
	
	/** Ground truth relationships for given songs*/
	private ArrayList<ClassifiedSongPartitionsDescription> labeledSongRelationships = null;
	
	/** Used for calculation of data reduction metrics */ 
	private ArrayList<String> listOfAllProcessedFiles = null;
	File groundTruthFile = null;
	
	/**
	 * Constructor
	 */
	public ValidatorNodeScheduler(String folderForResults) throws NodeException {
		super(folderForResults);
		requiredParameters = new String();
		categoryDescription = new String();
		listOfAllProcessedFiles = new ArrayList<String>();
	}
	
	/**
	 * Main method for validation
	 * @param args Validation configuration
	 */
	public static void main(String[] args) {
		
		// Create the node scheduler
		ValidatorNodeScheduler thisScheduler = null;
		try {
			thisScheduler = new ValidatorNodeScheduler(args[0] + "/input/task_" + args[1]);
		} catch(NodeException e) {
			AmuseLogger.write(ValidatorNodeScheduler.class.getName(), Level.ERROR,
					"Could not create folder for validator node intermediate results: " + e.getMessage());
			return;
		}
		
		// Proceed the task
		thisScheduler.proceedTask(args);
		
		// Remove the folder for input and intermediate results
		try {
			thisScheduler.removeInputFolder();
		} catch(NodeException e) {
				AmuseLogger.write(ValidatorNodeScheduler.class.getClass().getName(), Level.WARN,
					"Could not remove properly the folder with intermediate results '" + 
					thisScheduler.nodeHome + "/input/task_'" + thisScheduler.jobId + 
					"; please delete it manually! (Exception: "+ e.getMessage() + ")");
		}
	}
	
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration taskConfiguration) {
		
		// ----------------------------------------
		// (I): Configure validation node scheduler
		// ----------------------------------------
		this.nodeHome = nodeHome;
		if(this.nodeHome.startsWith(System.getenv("AMUSEHOME"))) {
			this.directStart = true;
		}
		this.jobId = new Long(jobId);
		this.taskConfiguration = taskConfiguration;
		
		// If this node is started directly, the properties are loaded from AMUSEHOME folder;
		// if this node is started via command line (e.g. in a grid, the properties are loaded from
		// %trainer home folder%/input
		if(!this.directStart) {
			File preferencesFile = new File(this.nodeHome + "/config/amuse.properties");
			AmusePreferences.restoreFromFile(preferencesFile);
		}
		
		// Set the category description - used for the metric destination folder
		DataSetAbstract categoryList = null;
		try {
			categoryList = new ArffDataSet(new File(AmusePreferences.get(KeysStringValue.CATEGORY_DATABASE)));
		} catch (IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL,  
					"Could not load the category table: " + e.getMessage()); 
			return;
		}
		int i=0;
		// TODO currently works only with category id
		while(i < categoryList.getValueCount()) {
			Integer id = new Double(categoryList.getAttribute("Id").getValueAt(i).toString()).intValue();
			if(id.toString().equals(
					((ValidationConfiguration)this.taskConfiguration).getInputToValidate().toString())) {
				this.categoryDescription = ((ValidationConfiguration)this.taskConfiguration).getInputToValidate().toString() + 
					"-" + categoryList.getAttribute("CategoryName").getValueAt(i).toString();
				break;
			}
			i++;
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Validator node scheduler for category " + 
				this.categoryDescription + " and processing configuration " + 
				((ValidationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + " started");
				
		// -------------------------------------
		// (II): Prepare the data for validation
		// -------------------------------------
		try {
			this.prepareValidatorInput();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL,  
					"Validation data could not loaded: " + e.getMessage()); 
			System.exit(1);
		}
		
		// --------------------------------------
		// (III): Configure the validation method
		// --------------------------------------
		try {
			this.configureValidationMethod();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL,  
					"Configuration of validation method failed: " + e.getMessage()); 
			System.exit(1);
		}
		
		// -------------------------------------------------------------------------
		// (IV): Load the list of processed feature files for data reduction metrics
		// -------------------------------------------------------------------------
		try {
			listOfAllProcessedFiles = this.vmi.calculateListOfUsedProcessedFeatureFiles();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.WARN,  
					"Data reduction data from processed feature files is not available: " + e.getMessage()); 
		}
		
		
		// -------------------------------		
		// (V) Start the validation method
		// -------------------------------
		try {
			this.vmi.validate();
		} catch(NodeException e) {
			e.printStackTrace();
			AmuseLogger.write(this.getClass().getName(), Level.FATAL, 
					"Validation failed: " + e.getMessage());
			System.exit(1);
		}
		
		// ---------------------------------------------------------------------------------
		// (V) If started directly, remove generated data and fire event for Amuse scheduler
		// ---------------------------------------------------------------------------------
		if(this.directStart) {
			try {
				this.cleanInputFolder();
			} catch(NodeException e) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Could not remove properly the intermediate results '" + 
					this.nodeHome + "/input/task_'" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
				System.exit(1);
			}
			this.fireEvent(new NodeEvent(NodeEvent.VALIDATION_COMPLETED, this));
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String[])
	 */
	public void proceedTask(String[] args) {
		if(args.length < 2) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL, 2 - args.length + 
					" arguments are missing; The usage is 'ValidatorNodeScheduler %1 %2', where: \n" +
					"%1 - Home folder of this node\n" +
					"%2 - Unique (for currently running Amuse instance) task Id\n"); 
			System.exit(1);
		}
		
		// Load the task configuration from %VALIDATORHOME%/task.ser
		ValidationConfiguration[] validatorConfig = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(args[0] + "/task_" + args[1] + ".ser");
			in = new ObjectInputStream(fis);
			Object o = in.readObject();
			validatorConfig = (ValidationConfiguration[])o;
			in.close();
		} catch(IOException ex) {
		    ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Proceed the validation task(s)
		for(int i=0;i<validatorConfig.length;i++) {
			proceedTask(args[0],new Long(args[1]),validatorConfig[i]);
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "Validator node is going to start job " + 
					(i+1) + "/" + validatorConfig.length);
		}
	}
	
	/**
	 * Configures the validation method
	 * @throws NodeException
	 */
	private void configureValidationMethod() throws NodeException{

		Integer requiredAlgorithm; 

		// If parameter string for this algorithm exists..
		if(((ValidationConfiguration)taskConfiguration).getValidationAlgorithmDescription().contains("[") && 
				((ValidationConfiguration)taskConfiguration).getValidationAlgorithmDescription().contains("]")) {
			requiredAlgorithm = new Integer(((ValidationConfiguration)taskConfiguration).
					getValidationAlgorithmDescription().substring(0,((ValidationConfiguration)taskConfiguration).
							getValidationAlgorithmDescription().indexOf("[")));
			this.requiredParameters = ((ValidationConfiguration)taskConfiguration).getValidationAlgorithmDescription().
				substring(((ValidationConfiguration)taskConfiguration).getValidationAlgorithmDescription().indexOf("[")+1,
						((ValidationConfiguration)taskConfiguration).getValidationAlgorithmDescription().lastIndexOf("]")); 
		} else {
			requiredAlgorithm = new Integer(((ValidationConfiguration)taskConfiguration).getValidationAlgorithmDescription());
			this.requiredParameters = null;
		}
		boolean algorithmFound = false;
		
		// Load the validation methods table
		ArffLoader validationMethodLoader = new ArffLoader();
		Instance currentInstance;
		try {
			if(this.directStart) {
				validationMethodLoader.setFile(new File(System.getenv("AMUSEHOME") + "/config/validationAlgorithmTable.arff"));
	    	} else {
	    		validationMethodLoader.setFile(new File(this.nodeHome + "/input/task_" + this.jobId + "/validationAlgorithmTable.arff"));
	    	}
			Attribute idAttribute = validationMethodLoader.getStructure().attribute("Id");
			Attribute nameAttribute = validationMethodLoader.getStructure().attribute("Name");
			Attribute methodClassAttribute = validationMethodLoader.getStructure().attribute("MethodClass");
			currentInstance = validationMethodLoader.getNextInstance(validationMethodLoader.getStructure());
			
			while(currentInstance != null) {
				Integer idOfCurrentAlgorithm = new Double(currentInstance.value(idAttribute)).intValue();
				if(!currentInstance.isMissing(idAttribute) && (idOfCurrentAlgorithm.equals(requiredAlgorithm))) {
					try {
						Class<?> validationMethod = Class.forName(currentInstance.stringValue(methodClassAttribute));
						
						vmi = (ValidatorInterface)validationMethod.newInstance();
						Properties validatorProperties = new Properties();
						validatorProperties.setProperty("name",currentInstance.stringValue(nameAttribute));
						((AmuseTask)vmi).configure(validatorProperties,this,this.requiredParameters);

						AmuseLogger.write(this.getClass().getName(), Level.INFO, 
								"Validation method is configured: " + currentInstance.stringValue(methodClassAttribute));
					} catch(ClassNotFoundException e) {
						AmuseLogger.write(this.getClass().getName(), Level.FATAL, 
								"Validation method class cannot be located: " + currentInstance.stringValue(methodClassAttribute));
						System.exit(1);
					} catch(IllegalAccessException e) {
						AmuseLogger.write(this.getClass().getName(), Level.FATAL, 
								"Validation method class or its nullary constructor is not accessible: " + currentInstance.stringValue(methodClassAttribute));
						System.exit(1);
					} catch(InstantiationException e) {
						AmuseLogger.write(this.getClass().getName(), Level.FATAL, 
								"Instantiation failed for validation method class: " + currentInstance.stringValue(methodClassAttribute));
						System.exit(1);
					}
					algorithmFound = true;
					break;
				}
				
				currentInstance = validationMethodLoader.getNextInstance(validationMethodLoader.getStructure());
			}
			
			if(!algorithmFound) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
						"Validation algorithm with id " + ((ValidationConfiguration)taskConfiguration).getValidationAlgorithmDescription() + 
						" was not found, task aborted");
				System.exit(1);
			}
			
		} catch(IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL, 
    				"Validation methods table cannot be parsed properly: " + e.getMessage());
    		System.exit(1);
		}
	}
	
	/**
	 * Converts feature vectors + descriptions to labeled classifier input for validation and loads 
	 * the information about song structure
	 * @throws NodeException
	 */
	private void prepareValidatorInput() throws NodeException {
		labeledSongRelationships = new ArrayList<ClassifiedSongPartitionsDescription>();
		
		if(! (((ValidationConfiguration)this.getConfiguration()).getInputToValidate() instanceof DataSetInput)) {
			
			DataSet labeledInputForValidation = null;
			try {
			
				// If the ground truth has been previously prepared, the input is ready! 
				if(((ValidationConfiguration)this.getConfiguration()).getGroundTruthSourceType().
						equals(ValidationConfiguration.GroundTruthSourceType.READY_INPUT)) {
					labeledInputForValidation = new DataSet(new File(((ValidationConfiguration)this.getConfiguration()).getInputToValidate().toString()),
						"ValidationSet");
				} else {
					labeledInputForValidation = new DataSet("ValidationSet");
					
					// Load the ground truth
					String pathToCategoryFile = ((FileInput)((ValidationConfiguration)this.getConfiguration()).getInputToValidate()).toString();
					if(((ValidationConfiguration)this.getConfiguration()).getGroundTruthSourceType().equals(
							ValidationConfiguration.GroundTruthSourceType.CATEGORY_ID)) {
						
						// Search for the category file
						Integer categoryId = new Integer(((FileInput)((ValidationConfiguration)this.getConfiguration()).
								getInputToValidate()).toString());
						DataSetAbstract categoryList = new ArffDataSet(new File(AmusePreferences.get(KeysStringValue.CATEGORY_DATABASE)));
						for(int i=0;i<categoryList.getValueCount();i++) {
							Double currentCategoryId = new Double(categoryList.getAttribute("Id").getValueAt(i).toString());
							if(new Integer(currentCategoryId.intValue()).equals(categoryId)) {
								pathToCategoryFile = new String(categoryList.getAttribute("Path").getValueAt(i).toString());
								break;
							}
						}
					}
					groundTruthFile = new File(pathToCategoryFile);
					DataSetAbstract validatorGroundTruthSet = new ArffDataSet(groundTruthFile);
					
					// Load the first classifier input for attributes information
					String currentInputFile = validatorGroundTruthSet.getAttribute("Path").getValueAt(0).toString();
					if(currentInputFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
						currentInputFile = 
							((ValidationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
							+ File.separator +
							currentInputFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length()+1,
									currentInputFile.lastIndexOf(".")) + File.separator +
							currentInputFile.substring(currentInputFile.lastIndexOf(File.separator)+1,currentInputFile.lastIndexOf(".")) + "_" +
							((ValidationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
					}
					
					ArffLoader validatorInputLoader = new ArffLoader();
					Instance inputInstance;
					AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading: " + currentInputFile);
					validatorInputLoader.setFile(new File(currentInputFile));
					inputInstance = validatorInputLoader.getNextInstance(validatorInputLoader.getStructure());
					
					// Create the attributes omitting UNIT, START and END attributes (they describe the partition for modeled features)
					for(int i=0;i<validatorInputLoader.getStructure().numAttributes()-3;i++) {
						labeledInputForValidation.addAttribute(new NumericAttribute(inputInstance.attribute(i).name(),
							new ArrayList<Double>()));
					}
					labeledInputForValidation.addAttribute(new NumericAttribute("Id",new ArrayList<Double>()));
					labeledInputForValidation.addAttribute(new StringAttribute("Category",new ArrayList<String>()));
					
					// Create the labeled data
					for(int i=0;i<validatorGroundTruthSet.getValueCount();i++) {
						Integer songId = new Double(validatorGroundTruthSet.getAttribute("Id").getValueAt(i).toString()).intValue();
						String label = validatorGroundTruthSet.getAttribute("Category").getValueAt(i).toString();
						Double confidence = new Double(validatorGroundTruthSet.getAttribute("Relationship").getValueAt(i).toString());
						Integer end = new Double(validatorGroundTruthSet.getAttribute("End").getValueAt(i).toString()).intValue();
						String path = validatorGroundTruthSet.getAttribute("Path").getValueAt(i).toString();
						
						ArrayList<Double> partitionStarts = new ArrayList<Double>();
						ArrayList<Double> partitionEnds = new ArrayList<Double>();
						
						// If the complete song should be read
						if(end == -1) {
							while(inputInstance != null) {
								for(int j=0;j<validatorInputLoader.getStructure().numAttributes()-3;j++) {
									Double val = inputInstance.value(j);
									labeledInputForValidation.getAttribute(j).addValue(val);
								}
								
								// Write the ID attribute (from what song the features are saved)
								// IMPORTANT: --------------------------------------------------- 
								// This attribute must not be used for classification model training! 
								// If any new classification algorithms are integrated into AMUSE, they must
								// handle this properly!!!
								Double id = new Double(validatorGroundTruthSet.getAttribute("Id").getValueAt(i).toString());
								labeledInputForValidation.getAttribute("Id").addValue(id);
								
								// FIXME Must be saved directly!
								if(confidence >= 0.5) {
									labeledInputForValidation.getAttribute("Category").addValue(label);
								} else {
									labeledInputForValidation.getAttribute("Category").addValue("NOT_" + label);
								}
								double startPosition = inputInstance.value(validatorInputLoader.getStructure().attribute("Start"));
								double endPosition = inputInstance.value(validatorInputLoader.getStructure().attribute("End"));
								partitionStarts.add(startPosition);
								partitionEnds.add(endPosition);
								
								inputInstance = validatorInputLoader.getNextInstance(validatorInputLoader.getStructure());
							}
							
							// Add descriptions of the partitions of the current song
							Double[] partitionStartsAsArray = new Double[partitionStarts.size()];
							Double[] partitionEndsAsArray = new Double[partitionEnds.size()];
							Double[] relationships = new Double[partitionStarts.size()];
							for(int l=0;l<partitionStarts.size();l++) {
								partitionStartsAsArray[l] = partitionStarts.get(l);
								partitionEndsAsArray[l] = partitionEnds.get(l);
								if(confidence == 0.99) {
									relationships[l] = 1d;
								} else if(confidence == 0.01) {
									relationships[l] = 0d;
								} else {
									relationships[l] = confidence;
								}
							}
							ClassifiedSongPartitionsDescription newSongDesc = new ClassifiedSongPartitionsDescription(path, 
									songId, partitionStartsAsArray, partitionEndsAsArray, label, relationships);
							labeledSongRelationships.add(newSongDesc);
						} else {
							// TODO Consider Vocals/Piano-Recognition-Scenario!
							/*for (Enumeration attrs = classifierInputLoader.getStructure().enumerateAttributes() ; attrs.hasMoreElements() ;) {
								if(inputInstance == null) System.out.println("HARR!");
								Double val = inputInstance.value((Attribute)attrs.nextElement());
								values_writer.writeBytes(val.toString());
								values_writer.writeBytes(",");
				   	        }
							values_writer.writeBytes(label);
							values_writer.writeBytes(sep);*/
		
						}
						
						// Go to the next description
						String newInputFile = validatorGroundTruthSet.getAttribute("Path").getValueAt(i+1).toString();
						
						if(newInputFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
							newInputFile = 
								((ValidationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase() + File.separator +
								newInputFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length()+1,
								newInputFile.lastIndexOf(".")) + File.separator +
								newInputFile.substring(newInputFile.lastIndexOf(File.separator)+1,newInputFile.lastIndexOf(".")) + "_" +
								((ValidationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
						}
						
						// Go to the next music file?
						if(!newInputFile.equals(currentInputFile)) {
							currentInputFile = newInputFile;
							AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading: " + currentInputFile);
							validatorInputLoader = new ArffLoader();
							validatorInputLoader.setFile(new File(currentInputFile));
						} 
						
						// Load the next input vector
						inputInstance = validatorInputLoader.getNextInstance(validatorInputLoader.getStructure());
						
						// If the input vector and its description do not match 
						if(inputInstance == null) {
							AmuseLogger.write(this.getClass().getName(), Level.WARN,  
									"Descriptions from " + groundTruthFile +
									"do not correspond to extractorNode.jar input vectors, the classifier will not be trained with" +
									"the complete data!");
							break;
						}
					}
				} 
			} catch(IOException e) {
				throw new NodeException(e.getMessage());
			}
			
			// Replace the ground truth source by the data set input loaded into memory
			((ValidationConfiguration)this.taskConfiguration).setInputToValidate(new DataSetInput(labeledInputForValidation));
		} 
		
		// Load only the song information if the data is already prepared
		else {
			if(((ValidationConfiguration)this.getConfiguration()).getInputToValidate() instanceof DataSetInput) {
				amuse.data.io.attributes.Attribute idAttribute = ((DataSetInput)((ValidationConfiguration)this.getConfiguration()).
						getInputToValidate()).getDataSet().getAttribute("Id");
				amuse.data.io.attributes.Attribute labelAttribute = ((DataSetInput)((ValidationConfiguration)this.getConfiguration()).
						getInputToValidate()).getDataSet().getAttribute("Category");
				
				Integer currentSongId = new Double(idAttribute.getValueAt(0).toString()).intValue();
				ArrayList<Double> relationships = new ArrayList<Double>();
				for(int i=0;i<labelAttribute.getValueCount();i++) {
					Integer newSongId = new Double(idAttribute.getValueAt(i).toString()).intValue();
					
					// New song is reached
					if(!newSongId.equals(currentSongId)) {
						Double[] relationshipsAsArray = new Double[relationships.size()];
						for(int k=0;k<relationships.size();k++) {
							relationshipsAsArray[k] = relationships.get(k);
						}
						ClassifiedSongPartitionsDescription newSongDesc = new ClassifiedSongPartitionsDescription("", 
								currentSongId, new Double[relationshipsAsArray.length], new Double[relationshipsAsArray.length], "", relationshipsAsArray);
						labeledSongRelationships.add(newSongDesc);
						currentSongId = newSongId;
						relationships = new ArrayList<Double>();
					} 
						
					if(!labelAttribute.getValueAt(i).toString().startsWith("NOT")) {
						relationships.add(1d);
					} else {
						relationships.add(0d);
					}
				}
				
				// For the last song
				Double[] relationshipsAsArray = new Double[relationships.size()];
				for(int k=0;k<relationships.size();k++) {
					relationshipsAsArray[k] = relationships.get(k);
				}
				ClassifiedSongPartitionsDescription newSongDesc = new ClassifiedSongPartitionsDescription("", 
						currentSongId, new Double[relationshipsAsArray.length], new Double[relationshipsAsArray.length], "", relationshipsAsArray);
				labeledSongRelationships.add(newSongDesc);
			} 
		}
	}

	/**
	 * @return the labeledSongRelationships
	 */
	public ArrayList<ClassifiedSongPartitionsDescription> getLabeledSongRelationships() {
		return labeledSongRelationships;
	}
	
	/**
	 * @return the labeledSongRelationships
	 */
	public ArrayList<Double> getLabeledAverageSongRelationships() {
		ArrayList<Double> a = new ArrayList<Double>(labeledSongRelationships.size());
		for(ClassifiedSongPartitionsDescription d : labeledSongRelationships) {
			a.add(d.getMeanRelationship());
		}
		return a;
	}

	/**
	 * @return the categoryDescription
	 */
	public String getCategoryDescription() {
		return categoryDescription;
	}

	/**
	 * @param categoryDescription the categoryDescription to set
	 */
	public void setCategoryDescription(String categoryDescription) {
		this.categoryDescription = categoryDescription;
	}

	/**
	 * @return the listOfAllProcessedFiles
	 */
	public ArrayList<String> getListOfAllProcessedFiles() {
		return listOfAllProcessedFiles;
	}

	/**
	 * @return the groundTruthFile
	 */
	public File getGroundTruthFile() {
		return groundTruthFile;
	}
	
}