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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import amuse.nodes.classifier.interfaces.BinaryClassifiedSongPartitions;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.classifier.interfaces.MulticlassClassifiedSongPartitions;
import amuse.nodes.validator.ValidationConfiguration.GroundTruthSourceType;
import amuse.nodes.validator.interfaces.ValidationMetric;
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
	private ArrayList<ClassifiedSongPartitions> labeledSongRelationships = null;
	
	private boolean isMulticlass = false;
	
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
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String, long, amuse.interfaces.nodes.TaskConfiguration)
	 */
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration classificationConfiguration) {
		
		// Start the task with the file output
		try {
			proceedTask(nodeHome, jobId, classificationConfiguration, true);
		} catch (NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
					"Could not proceed validation task: " + e.getMessage());
		}
	}
	
	/**
	 * Proceeds the validation task
	 * @param saveToFile If true, the metric results are outputted to a file 
	 */
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration taskConfiguration, 
			boolean saveToFile) throws NodeException {
		
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
			throw new NodeException("Validation data could not be loaded: " + e.getMessage()); 
		}
		
		// --------------------------------------
		// (III): Configure the validation method
		// --------------------------------------
		try {
			this.configureValidationMethod();
		} catch(NodeException e) {
			throw new NodeException("Configuration of validation method failed: " + e.getMessage()); 
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
			// TODO v0.2: if the file input is validated, the place for metric file must be also given!
			if(saveToFile) {
				saveMetricsToFile();
			}
		} catch(NodeException e) {
			throw new NodeException("Validation failed: " + e.getMessage());
		}
		
		// ----------------------------------------------------------------------------------
		// (VI) If started directly, remove generated data and fire event for Amuse scheduler
		// ----------------------------------------------------------------------------------
		if(this.directStart) {
			try {
				this.cleanInputFolder();
			} catch(NodeException e) {
				throw new NodeException("Could not remove properly the intermediate results '" + 
					this.nodeHome + "/input/task_'" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
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
		labeledSongRelationships = new ArrayList<ClassifiedSongPartitions>();
		
		// If the validation set is not given as ready data set..
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
					
					// Is the current classification result binary or multiclass?
					String category = validatorGroundTruthSet.getAttribute("Category").getValueAt(0).toString();
					if(category.startsWith("NOT")) {
						category = category.substring(4,category.length());
					}
					for(int i=1;i<validatorGroundTruthSet.getAttribute("Category").getValueCount();i++) {
						String currentCategory = validatorGroundTruthSet.getAttribute("Category").getValueAt(i).toString();
						if(currentCategory.startsWith("NOT")) {
							currentCategory = currentCategory.substring(4,currentCategory.length());
						}
						if(!currentCategory.equals(category)) {
							isMulticlass = true;
							break;
						}
					}
					
					// Load the first classifier input for attributes information
					String currentInputFile = validatorGroundTruthSet.getAttribute("Path").getValueAt(0).toString();
					if(currentInputFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
						currentInputFile = 
							((ValidationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
							+ File.separator 
							+ currentInputFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length() + 1,
									currentInputFile.lastIndexOf("."))
							+ File.separator
							+ currentInputFile.substring(currentInputFile.lastIndexOf(File.separator) + 1,
									currentInputFile.lastIndexOf("."))
							+ "_" 
							+ ((ValidationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
					}
					else{
						currentInputFile = 
							((ValidationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
							+ File.separator 
							+ currentInputFile.substring(0,
									currentInputFile.lastIndexOf(".")) 
							+ File.separator 
							+ currentInputFile.substring(currentInputFile.lastIndexOf(File.separator) + 1,
									currentInputFile.lastIndexOf(".")) 
							+ "_" 
							+ ((ValidationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
					}
					currentInputFile = currentInputFile.replaceAll("/+", "/");
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
						String[] labels = null; // If multiclass classification is done
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
								if(!isMulticlass) {
									if(confidence >= 0.5) {
										labeledInputForValidation.getAttribute("Category").addValue(label);
									} else {
										labeledInputForValidation.getAttribute("Category").addValue("NOT_" + label);
									}
								} else {
									labels = new String[partitionStarts.size()];
									for(int j=0;j<partitionStarts.size();j++) {
										labels[j] = validatorGroundTruthSet.getAttribute("Category").getValueAt(j).toString();
									}
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
							
							ClassifiedSongPartitions newSongDesc = null;
							if(!isMulticlass) {
								newSongDesc = new BinaryClassifiedSongPartitions(path, 
									songId, partitionStartsAsArray, partitionEndsAsArray, label, relationships);
							} else {
								newSongDesc = new MulticlassClassifiedSongPartitions(path, 
										songId, partitionStartsAsArray, partitionEndsAsArray, labels, relationships);
							}
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
								((ValidationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
								+ File.separator 
								+ newInputFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length() + 1,
										newInputFile.lastIndexOf("."))
								+ File.separator
								+ newInputFile.substring(newInputFile.lastIndexOf(File.separator) + 1,
										newInputFile.lastIndexOf("."))
								+ "_" 
								+ ((ValidationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
						}
						else{
							newInputFile = 
								((ValidationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
								+ File.separator 
								+ newInputFile.substring(0,
										newInputFile.lastIndexOf(".")) 
								+ File.separator 
								+ newInputFile.substring(newInputFile.lastIndexOf(File.separator) + 1,
										newInputFile.lastIndexOf(".")) 
								+ "_" 
								+ ((ValidationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
						}
						newInputFile = newInputFile.replaceAll("/+", "/");
						
						
						
						
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
		
		// Set the ground truth if the validation set is given as ready data set
		if(((ValidationConfiguration)this.getConfiguration()).getGroundTruthSourceType().equals(GroundTruthSourceType.READY_INPUT)) {
			amuse.data.io.attributes.Attribute idAttribute = ((DataSetInput)((ValidationConfiguration)this.getConfiguration()).
					getInputToValidate()).getDataSet().getAttribute("Id");
			amuse.data.io.attributes.Attribute labelAttribute = ((DataSetInput)((ValidationConfiguration)this.getConfiguration()).
					getInputToValidate()).getDataSet().getAttribute("Category");
				
			// Is the current classification result binary or multiclass?
			String category = labelAttribute.getValueAt(0).toString();
			if(category.startsWith("NOT")) {
				category = category.substring(4,category.length());
			}
			for(int i=1;i<labelAttribute.getValueCount();i++) {
				String currentCategory = labelAttribute.getValueAt(i).toString();
				if(currentCategory.startsWith("NOT")) {
					currentCategory = currentCategory.substring(4,currentCategory.length());
				}
				if(!currentCategory.equals(category)) {
					isMulticlass = true;
					break;
				}
			}
			
			Integer currentSongId = new Double(idAttribute.getValueAt(0).toString()).intValue();
			ArrayList<Double> relationships = new ArrayList<Double>();
			ArrayList<String> labels = new ArrayList<String>();
			for(int i=0;i<labelAttribute.getValueCount();i++) {
				Integer newSongId = new Double(idAttribute.getValueAt(i).toString()).intValue();
				
				// New song is reached
				if(!newSongId.equals(currentSongId)) {
					Double[] relationshipsAsArray = new Double[relationships.size()];
					for(int k=0;k<relationships.size();k++) {
						relationshipsAsArray[k] = relationships.get(k);
					} 
						
					ClassifiedSongPartitions newSongDesc = null; 
					if(!isMulticlass) {
						newSongDesc = new BinaryClassifiedSongPartitions("", 
							currentSongId, new Double[relationshipsAsArray.length], new Double[relationshipsAsArray.length], "", relationshipsAsArray);
					} else {
						String[] labelsAsArray = new String[labels.size()];
						for(int k=0;k<labels.size();k++) {
							labelsAsArray[k] = labels.get(k);
						} 
						newSongDesc = new MulticlassClassifiedSongPartitions("", 
								currentSongId, new Double[relationshipsAsArray.length], new Double[relationshipsAsArray.length], labelsAsArray, relationshipsAsArray);
					}
					labeledSongRelationships.add(newSongDesc);
					currentSongId = newSongId;
					relationships = new ArrayList<Double>();
					labels = new ArrayList<String>();
				} 
						
				if(!labelAttribute.getValueAt(i).toString().startsWith("NOT")) {
					relationships.add(1d);
				} else {
					relationships.add(0d);
				}
				if(isMulticlass) {
					labels.add(labelAttribute.getValueAt(i).toString());
				}
			}
				
			// For the last song
			Double[] relationshipsAsArray = new Double[relationships.size()];
			for(int k=0;k<relationships.size();k++) {
				relationshipsAsArray[k] = relationships.get(k);
			} 
			
			ClassifiedSongPartitions newSongDesc = null; 
			if(!isMulticlass) {
				newSongDesc = new BinaryClassifiedSongPartitions("", 
					currentSongId, new Double[relationshipsAsArray.length], new Double[relationshipsAsArray.length], "", relationshipsAsArray);
			} else {
				String[] labelsAsArray = new String[labels.size()];
				for(int k=0;k<labels.size();k++) {
					labelsAsArray[k] = labels.get(k);
				} 
				newSongDesc = new MulticlassClassifiedSongPartitions("", 
						currentSongId, new Double[relationshipsAsArray.length], new Double[relationshipsAsArray.length], labelsAsArray, relationshipsAsArray);
			}
			labeledSongRelationships.add(newSongDesc);
		} 
	}

	/**
	 * Saves the calculated metrics to the output file
	 */
	private void saveMetricsToFile() throws NodeException {
		try {
			
			// Check if the folder for metric file exists; if not create it
			File folderForMetrics = createMetricFolder();
			
			// If no metric file is there, save header
			boolean saveHeader = false;
			// FIXME Currently the file is overwritten each time for Windows compatibility during optimization task
			//if(!new File(this.folderForMetrics + "/" + "metrics.arff").exists()) {
				saveHeader = true;
			//}
			//FileOutputStream values_to = new FileOutputStream(this.folderForMetrics + "/" + "metrics.arff",true);
			FileOutputStream values_to = new FileOutputStream(folderForMetrics + "/" + "metrics.arff");
			DataOutputStream values_writer = new DataOutputStream(values_to);
			String sep = System.getProperty("line.separator");
			
			// Saves the header
			if(saveHeader) {
				values_writer.writeBytes("@RELATION 'Classifier metrics'");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE Time STRING");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE MetricId NUMERIC");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE MetricName STRING");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE MetricValue STRING");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@DATA");
			}
			values_writer.writeBytes(sep);
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			
			// Save the metric values, going through all metrics
			for(ValidationMetric m : ((ValidationConfiguration)taskConfiguration).getCalculatedMetrics()) {
				values_writer.writeBytes("\"" + sdf.format(new Date()) + "\", " + 
							m.getId() + ", " + "\"" + m.getName() + "\", " + 
							m.getValue());
				values_writer.writeBytes(sep);
			}
			values_writer.close();
		} catch(IOException e) {
			throw new NodeException("Could not save metrics: " + e.getMessage());
		}
	}
	
	/**
	 * Sets the name of the folder for metrics and creates this folder
	 * @throws NodeException
	 */
	private File createMetricFolder() throws NodeException {
		File folderForMetrics = null;
		String classifierDescription = new String("");
		ArffLoader classificationAlgorithmLoader = new ArffLoader();
		Instance classificationAlgorithmInstance;
		boolean classificationMethodFound = false; 
		int algorithmToSearch;
		if(((ValidationConfiguration)taskConfiguration).getClassificationAlgorithmDescription().indexOf("[") == -1) {
			algorithmToSearch = new Double(((ValidationConfiguration)taskConfiguration).getClassificationAlgorithmDescription()).intValue();
		} else {
			algorithmToSearch = new Double(((ValidationConfiguration)taskConfiguration).getClassificationAlgorithmDescription().substring(0,
					((ValidationConfiguration)taskConfiguration).getClassificationAlgorithmDescription().indexOf("["))).intValue();
		}
		try {
			if(this.getDirectStart()) {
				classificationAlgorithmLoader.setFile(new File(System.getenv("AMUSEHOME") + "/config/classifierAlgorithmTable.arff"));
	    	} else {
	    		classificationAlgorithmLoader.setFile(new File(this.getHomeFolder() + "/input/task_" + this.getTaskId() + "/classifierAlgorithmTable.arff"));
	    	}
			Attribute idAttribute = classificationAlgorithmLoader.getStructure().attribute("Id");
			Attribute nameAttribute = classificationAlgorithmLoader.getStructure().attribute("Name");
			classificationAlgorithmInstance = classificationAlgorithmLoader.getNextInstance(classificationAlgorithmLoader.getStructure());
			while(classificationAlgorithmInstance != null) {
				
				// If the given classification algorithm is found..
				if(classificationAlgorithmInstance.value(idAttribute) == algorithmToSearch) {
					classificationMethodFound = true;
					
					// Set the name of folder for models and metrics (combined from
					// classifier ID, parameters and name)
					classifierDescription = ((ValidationConfiguration)taskConfiguration).getClassificationAlgorithmDescription() + "-" + classificationAlgorithmInstance.stringValue(nameAttribute);

					String validatorMethodId = ((ValidationConfiguration)taskConfiguration).getValidationAlgorithmDescription();
					if(validatorMethodId.contains("[")) {
						validatorMethodId = validatorMethodId.substring(0,validatorMethodId.indexOf("["));
					}
					
					// Check if the folder for metric file exists; if not create it
					folderForMetrics = new File(
							((ValidationConfiguration)taskConfiguration).getMetricDatabase() + "/" + 
							categoryDescription + 
							File.separator + classifierDescription + File.separator +
							((ValidationConfiguration)taskConfiguration).getProcessedFeaturesModelName() + File.separator +
							validatorMethodId + 
							"-" + ((AmuseTask)vmi).getProperties().getProperty("name"));
					if(!folderForMetrics.exists()) {
						if(!folderForMetrics.mkdirs()) {
							throw new NodeException("Could not create the folder for classifier evaluation metrics: " + folderForMetrics);
						}
					}
					break;
				}
				classificationAlgorithmInstance = classificationAlgorithmLoader.getNextInstance(classificationAlgorithmLoader.getStructure());
			}
		
			// Check the classification method id
			if(!classificationMethodFound) {
				throw new NodeException("Could not find the appropriate classification method for algorithm with ID: " + 
						properties.getProperty("classificationAlgorithmId"));
			}
		} catch(Exception e) {
			throw new NodeException("Configuration of classifier for validation failed: " + e.getMessage());
		}
		return folderForMetrics;
	}
	
	/**
	 * @return the labeledSongRelationships
	 */
	public ArrayList<ClassifiedSongPartitions> getLabeledSongRelationships() {
		return labeledSongRelationships;
	}
	
	/**
	 * @return the labeledSongRelationships
	 */
	public ArrayList<Double> getLabeledAverageSongRelationships() {
		ArrayList<Double> a = new ArrayList<Double>(labeledSongRelationships.size());
		for(ClassifiedSongPartitions d : labeledSongRelationships) {
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

	/**
	 * @return the isMulticlass
	 */
	public boolean isMulticlass() {
		return isMulticlass;
	}
	
}
