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
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.FileTable;
import amuse.data.GroundTruthSourceType;
import amuse.data.InputFeatureType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetInput;
import amuse.data.io.FileInput;
import amuse.data.io.attributes.NumericAttribute;
import amuse.interfaces.nodes.NodeEvent;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.NodeScheduler;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.ClassifierNodeScheduler;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.validator.interfaces.ValidationMeasure;
import amuse.nodes.validator.interfaces.ValidatorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.algorithm.Algorithm;
import amuse.util.AmuseLogger;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;

/**
 * ValidationNodeScheduler configures and runs the appropriate classifier validation method.
 * 
 * @author Igor Vatolkin
 * @version $Id: ValidatorNodeScheduler.java 245 2018-09-27 12:53:32Z frederik-h $
 */
public class ValidatorNodeScheduler extends NodeScheduler { 

	/** Interface for validation method */
	ValidatorInterface vmi = null;
	
	/** Parameters for validation algorithm if required */
	private String requiredParameters = null;
	
	/** Personal user category id and name separated by '-', e.g. "15-Music_for_Inspiration" */
	private String categoryDescription = null;
	
	/** Ground truth relationships for given tracks*/
	private ArrayList<ClassifiedClassificationWindow> labeledTrackRelationships = null;
	
	/** Used for calculation of data reduction measures */ 
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
			thisScheduler = new ValidatorNodeScheduler(args[0] + File.separator + "input" + File.separator + "task_" + args[1]);
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
					thisScheduler.nodeHome + File.separator + "input" + File.separator + "task_'" + thisScheduler.jobId + 
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
			errorDescriptionBuilder.append(taskConfiguration.getDescription());
			this.fireEvent(new NodeEvent(NodeEvent.VALIDATION_FAILED, this));
		}
	}
	
	/**
	 * Proceeds the validation task
	 * @param saveToFile If true, the measure results are outputted to a file 
	 */
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration taskConfiguration, 
			boolean saveToFile) throws NodeException {
		
		// ----------------------------------------
		// (I): Configure validation node scheduler
		// ----------------------------------------
		this.nodeHome = nodeHome;
		if(this.nodeHome.startsWith(AmusePreferences.get(KeysStringValue.AMUSE_PATH))) {
			this.directStart = true;
		}
		this.jobId = new Long(jobId);
		this.taskConfiguration = taskConfiguration;
		
		// If this node is started directly, the properties are loaded from AMUSEHOME folder;
		// if this node is started via command line (e.g. in a grid, the properties are loaded from
		// %trainer home folder%/input
		if(!this.directStart) {
			File preferencesFile = new File(this.nodeHome + File.separator + "config" + File.separator + "amuse.properties");
			AmusePreferences.restoreFromFile(preferencesFile);
		}
		
		// Set the category description - used for the measure destination folder
		DataSetAbstract categoryList = null;
		try {
			categoryList = new ArffDataSet(new File(AmusePreferences.getMultipleTracksAnnotationTablePath()));
		} catch (IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL,  
					"Could not load the category table: " + e.getMessage()); 
			return;
		}
		int i=0;
		
		if(((ValidationConfiguration)this.taskConfiguration).getGroundTruthSourceType().equals(GroundTruthSourceType.CATEGORY_ID)) {
			// Search for the category file
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
		} else {
			//set the category description according to the name of the input file
			this.categoryDescription = ((ValidationConfiguration)this.taskConfiguration).getInputToValidate().toString();
			this.categoryDescription = this.categoryDescription.substring(this.categoryDescription.lastIndexOf(File.separatorChar) + 1, this.categoryDescription.lastIndexOf('.'));
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Validator node scheduler for category " + 
				this.categoryDescription + " and processing configuration " + 
				((ValidationConfiguration)this.taskConfiguration).getInputFeaturesDescription() + " started");
				
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
		// (IV): Load the list of processed feature files for data reduction measures
		// -------------------------------------------------------------------------
		try {
			if(((ValidationConfiguration)this.getConfiguration()).getInputFeatureType() == InputFeatureType.PROCESSED_FEATURES) {
				listOfAllProcessedFiles = this.vmi.calculateListOfUsedProcessedFeatureFiles();
			}
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.WARN,  
					"Data reduction data from processed feature files is not available: " + e.getMessage()); 
		}
		
		
		// -------------------------------		
		// (V) Start the validation method
		// -------------------------------
		try {
			this.vmi.validate();
			// TODO v0.2: if the file input is validated, the place for measure file must be also given!
			if(saveToFile) {
				saveMeasuresToFile();
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
					this.nodeHome + File.separator + "input" + File.separator + "task_'" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
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
			fis = new FileInputStream(args[0] + File.separator + "task_" + args[1] + ".ser");
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
					(i+1) + File.separator + validatorConfig.length);
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
				validationMethodLoader.setFile(new File(AmusePreferences.getValidationAlgorithmTablePath()));
	    	} else {
	    		validationMethodLoader.setFile(new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "validationAlgorithmTable.arff"));
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
	 * the information about track structure
	 * @throws NodeException
	 */
	private void prepareValidatorInput() throws NodeException {
		labeledTrackRelationships = new ArrayList<ClassifiedClassificationWindow>();
		
		//check if the settings are supported
		if(((ValidationConfiguration)this.taskConfiguration).getMethodType() != MethodType.SUPERVISED){
			throw new NodeException("Currently only supervised classification is supported.");
		}
		
		List<Integer> attributesToPredict = ((ValidationConfiguration)this.taskConfiguration).getAttributesToPredict();
		List<Integer> attributesToIgnore = ((ValidationConfiguration)this.taskConfiguration).getAttributesToIgnore();
		int numberOfCategories = attributesToPredict.size();
		
		//check if the number of categories is correct
		if(((ValidationConfiguration)this.getConfiguration()).getLabelType() == LabelType.SINGLELABEL && numberOfCategories > 1) {
			throw new NodeException("Single label classification is not possible for more than one category.");
		}
		if(numberOfCategories <= 0 ) {
			throw new NodeException("No attributes to classify were given.");
		}
		
		// If the validation set is not given as ready data set..
		if(! (((ValidationConfiguration)this.getConfiguration()).getInputToValidate() instanceof DataSetInput)) {
			
			DataSet labeledInputForValidation = null;
			try {
			
				// If the ground truth has been previously prepared, the input is almost ready! 
				if(((ValidationConfiguration)this.getConfiguration()).getGroundTruthSourceType().
						equals(GroundTruthSourceType.READY_INPUT)) {
					DataSet completeInput = new DataSet(new File(((ValidationConfiguration)this.getConfiguration()).getInputToValidate().toString()));
					labeledInputForValidation = new DataSet("ValidationSet");
					
					//add the attributes (except for attributes that are to be ignored and attributes that should be classified and the Id
					for(int i = 0; i < completeInput.getAttributeCount(); i++) {
						if(!attributesToPredict.contains(i) && !attributesToIgnore.contains(i) && !completeInput.getAttribute(i).getName().equals("Id")) {
							if(completeInput.getAttribute(i).getName().equals("NumberOfCategories")) {
								AmuseLogger.write(ClassifierNodeScheduler.class.getName(), Level.WARN, "NumberOfCategories is not an allowed attribute name. The attribute will be ignored.");
							}
							else {
								labeledInputForValidation.addAttribute(completeInput.getAttribute(i));
							}
						}
					}
				
					//add the id attribute
					labeledInputForValidation.addAttribute(completeInput.getAttribute("Id"));
				
					//add the attribute "NumberOfCategories"
					//it marks the where the categories that are to be classified start and how many will follow
					labeledInputForValidation.addAttribute(new NumericAttribute("NumberOfCategories", new ArrayList<Double>()));
					for(int i = 0; i < completeInput.getValueCount(); i++) {
						labeledInputForValidation.getAttribute(labeledInputForValidation.getAttributeCount() - 1).addValue(new Double(numberOfCategories));
					}
				
					//add the category attributes
					for(int i : attributesToPredict) {
						labeledInputForValidation.addAttribute(completeInput.getAttribute(i));
						//if the classification is not continuous, the values have to be rounded
						if(((ValidationConfiguration)this.taskConfiguration).getRelationshipType() == RelationshipType.CONTINUOUS && ((ValidationConfiguration)this.taskConfiguration).getLabelType() != LabelType.MULTICLASS) {	
							for(int j = 0; j < completeInput.getAttribute(i).getValueCount(); j++) {
								labeledInputForValidation.getAttribute(labeledInputForValidation.getAttributeCount() - 1).setValueAt(j, (double)completeInput.getAttribute(i).getValueAt(j) >= 0.5 ? 1.0 : 0.0);
							}
						}
					}
					//if the classification is multiclass only the highest relationship of each classification window is 1
					if(((ValidationConfiguration)this.taskConfiguration).getLabelType() == LabelType.MULTICLASS) {
						int positionOfFirstCategory = labeledInputForValidation.getAttributeCount() - numberOfCategories;
						for(int classificationWindow = 0; classificationWindow < completeInput.getValueCount(); classificationWindow++) {
							double max = 0;
							int maxCategory = 0;
							for(int category = 0; category < numberOfCategories; category++) {
								double newValue = (double)labeledInputForValidation.getAttribute(positionOfFirstCategory + category).getValueAt(classificationWindow);
								if(newValue > max) {
									max = newValue;
									maxCategory = category;
								}
							}
							for(int category = 0; category < numberOfCategories; category++) {
								labeledInputForValidation.getAttribute(category).setValueAt(positionOfFirstCategory + category, category == maxCategory ? 1.0 : 0.0);
							}
						}
					}
				} else if(((ValidationConfiguration)this.getConfiguration()).getInputFeatureType() == InputFeatureType.PROCESSED_FEATURES){
					labeledInputForValidation = new DataSet("ValidationSet");
					
					// Load the ground truth
					String pathToCategoryFile = ((FileInput)((ValidationConfiguration)this.getConfiguration()).getInputToValidate()).toString();
					if(((ValidationConfiguration)this.getConfiguration()).getGroundTruthSourceType().equals(
							GroundTruthSourceType.CATEGORY_ID)) {
						
						// Search for the category file
						Integer categoryId = new Integer(((FileInput)((ValidationConfiguration)this.getConfiguration()).
								getInputToValidate()).toString());
						DataSetAbstract categoryList = new ArffDataSet(new File(AmusePreferences.getMultipleTracksAnnotationTablePath()));
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
					String musicDatabasePath = AmusePreferences.get(KeysStringValue.MUSIC_DATABASE);
					// Make sure music database path ends with file separator to catch tracks that have the data base path as suffix but are not in the database
					musicDatabasePath += musicDatabasePath.endsWith(File.separator) ? "" : File.separator;
					if(currentInputFile.startsWith(musicDatabasePath)) {
						currentInputFile = 
							((ValidationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
							+ File.separator 
							+ currentInputFile.substring(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)).getPath().length() + 1,
									currentInputFile.lastIndexOf("."))
							+ File.separator
							+ currentInputFile.substring(currentInputFile.lastIndexOf(File.separator) + 1,
									currentInputFile.lastIndexOf("."))
							+ "_" 
							+ ((ValidationConfiguration)this.taskConfiguration).getInputFeaturesDescription() + ".arff";
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
							+ ((ValidationConfiguration)this.taskConfiguration).getInputFeaturesDescription() + ".arff";
					}
					currentInputFile = currentInputFile.replaceAll(File.separator + "+", File.separator);
					ArffLoader validatorInputLoader = new ArffLoader();
					Instance inputInstance;
					AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading: " + currentInputFile);
					validatorInputLoader.setFile(new File(currentInputFile));
					inputInstance = validatorInputLoader.getNextInstance(validatorInputLoader.getStructure());
					
					// Create the attributes omitting UNIT, START and END attributes (they describe the classification window for modeled features)
					for(int i=0;i<validatorInputLoader.getStructure().numAttributes()-3;i++) {
						//also omit the attributes that are supposed to be ignored
						if(!attributesToIgnore.contains(i)) {
							labeledInputForValidation.addAttribute(new NumericAttribute(inputInstance.attribute(i).name(),
									new ArrayList<Double>()));
						}
					}
					//Add the id attribute
					labeledInputForValidation.addAttribute(new NumericAttribute("Id",new ArrayList<Double>()));
					//Add the "NumberOfCategories" attribute
					//It marks the where the categories that are to be classified start and how many will follow
					labeledInputForValidation.addAttribute(new NumericAttribute("NumberOfCategories",new ArrayList<Double>()));
					for(int category : attributesToPredict) {
						labeledInputForValidation.addAttribute(new NumericAttribute(validatorGroundTruthSet.getAttribute(5 + category).getName(),new ArrayList<Double>()));
					}
					
					// Create the labeled data
					for(int i=0;i<validatorGroundTruthSet.getValueCount();i++) {
						Integer trackId = new Double(validatorGroundTruthSet.getAttribute("Id").getValueAt(i).toString()).intValue();
						String[] labels = new String[numberOfCategories];
						Double[] confidences = new Double[numberOfCategories];
						Integer end = new Double(validatorGroundTruthSet.getAttribute("End").getValueAt(i).toString()).intValue();
						String path = validatorGroundTruthSet.getAttribute("Path").getValueAt(i).toString();
						
						ArrayList<Double> classificationWindowStarts = new ArrayList<Double>();
						ArrayList<Double> classificationWindowEnds = new ArrayList<Double>();
						
						int currentPosition = 0;
						for(int category : attributesToPredict) {
							labels[currentPosition] = validatorGroundTruthSet.getAttribute(5 + category).getName();
							currentPosition++;
						}
						
						// If the complete track should be read
						if(end == -1) {
							while(inputInstance != null) {
								int currentAttribute = 0;
								for(int j=0;j<validatorInputLoader.getStructure().numAttributes()-3;j++) {
									//omit the attributes that are supposed to be ignored
									if(!attributesToIgnore.contains(j)) {
										Double val = inputInstance.value(j);
										labeledInputForValidation.getAttribute(currentAttribute).addValue(val);
										currentAttribute++;
									}
								}
								//If the classification is mutlilabel or singlelabel the confidences are added and rounded if the relationships are binary
								if(((ValidationConfiguration)this.getConfiguration()).getLabelType() != LabelType.MULTICLASS) {
									currentPosition = 0;
									for(int category : attributesToPredict) {
										String label = validatorGroundTruthSet.getAttribute(5 + category).getName();
										Double confidence = new Double(validatorGroundTruthSet.getAttribute(5 + category).getValueAt(i).toString());
										if(((ValidationConfiguration)this.getConfiguration()).getRelationshipType() == RelationshipType.CONTINUOUS) {
											labeledInputForValidation.getAttribute(label).addValue(confidence);
											confidences[currentPosition] = confidence;
										} else {
											labeledInputForValidation.getAttribute(label).addValue(confidence >= 0.5 ? 1.0 : 0.0);
											confidences[currentPosition] = confidence >= 0.5 ? 1.0 : 0.0;
										}
										currentPosition++;
									}
									//If the classification is multiclass only the relationship of the class with the highest confidence is 1
								} else {
									double maxConfidence = 0;
									int positionOfMax = 0;
									currentPosition = 0;
									for(int category : attributesToPredict) {
										Double confidence = new Double(validatorGroundTruthSet.getAttribute(5 + category).getValueAt(i).toString());
										if(confidence > maxConfidence) {
											maxConfidence = confidence;
											positionOfMax = currentPosition;
										}
										currentPosition++;
									}
									int positionOfFirstCategory = labeledInputForValidation.getAttributeCount() - numberOfCategories;
									for(int category=0;category<numberOfCategories;category++) {
										labeledInputForValidation.getAttribute(positionOfFirstCategory + category).addValue(category == positionOfMax ? 1.0 : 0.0);
										confidences[category] = category == positionOfMax ? 1.0 : 0.0;
									}
								}
								// Write the ID attribute (from what track the features are saved)
								// IMPORTANT: --------------------------------------------------- 
								// This attribute must not be used for classification model training! 
								// If any new classification algorithms are integrated into AMUSE, they must
								// handle this properly!!!
								Double id = new Double(validatorGroundTruthSet.getAttribute("Id").getValueAt(i).toString());
								labeledInputForValidation.getAttribute("Id").addValue(id);
								
								labeledInputForValidation.getAttribute("NumberOfCategories").addValue(new Double(numberOfCategories));
								
								double startPosition = inputInstance.value(validatorInputLoader.getStructure().attribute("Start"));
								double endPosition = inputInstance.value(validatorInputLoader.getStructure().attribute("End"));
								classificationWindowStarts.add(startPosition);
								classificationWindowEnds.add(endPosition);
								
								inputInstance = validatorInputLoader.getNextInstance(validatorInputLoader.getStructure());
							}
							
							// Add descriptions of the classification windows of the current track
							Double[] classificationWindowStartsAsArray = new Double[classificationWindowStarts.size()];
							Double[] classificationWindowEndsAsArray = new Double[classificationWindowEnds.size()];
							Double[][] relationships = new Double[classificationWindowStarts.size()][numberOfCategories];
							for(int l=0;l<classificationWindowStarts.size();l++) {
								classificationWindowStartsAsArray[l] = classificationWindowStarts.get(l);
								classificationWindowEndsAsArray[l] = classificationWindowEnds.get(l);
								for(int category=0;category<numberOfCategories;category++) {
									relationships[l][category] = confidences[category];
								}
							} 
							
							ClassifiedClassificationWindow newTrackDesc = new ClassifiedClassificationWindow(path, 
										trackId, classificationWindowStartsAsArray, classificationWindowEndsAsArray, labels, relationships);
							labeledTrackRelationships.add(newTrackDesc);
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
						
						// Do not go to the next description if this was already the last description
						if(i == validatorGroundTruthSet.getValueCount() - 1) {
							break;
						}
						
						// Go to the next description
						String newInputFile = validatorGroundTruthSet.getAttribute("Path").getValueAt(i+1).toString();
						
						if(newInputFile.startsWith(musicDatabasePath)) {
							newInputFile = 
								((ValidationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
								+ File.separator 
								+ newInputFile.substring(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)).getPath().length() + 1,
										newInputFile.lastIndexOf("."))
								+ File.separator
								+ newInputFile.substring(newInputFile.lastIndexOf(File.separator) + 1,
										newInputFile.lastIndexOf("."))
								+ "_" 
								+ ((ValidationConfiguration)this.taskConfiguration).getInputFeaturesDescription() + ".arff";
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
								+ ((ValidationConfiguration)this.taskConfiguration).getInputFeaturesDescription() + ".arff";
						}
						newInputFile = newInputFile.replaceAll(File.separator + "+", File.separator);
						
						// Go to the next music file?
						if(!newInputFile.equals(currentInputFile) || (Double)validatorGroundTruthSet.getAttribute("Start").getValueAt(i+1) == 0) {
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
				} else {
					// load the raw features
					labeledInputForValidation = new DataSet("TrainingSet");
					// Load the ground truth
					String pathToCategoryFile = ((FileInput)((ValidationConfiguration)this.getConfiguration()).getInputToValidate()).toString();
					if(((ValidationConfiguration)this.getConfiguration()).getGroundTruthSourceType().equals(
							GroundTruthSourceType.CATEGORY_ID)) {
						
						// Search for the category file
						Integer categoryId = new Integer(((FileInput)((ValidationConfiguration)this.getConfiguration()).
								getInputToValidate()).toString());
						DataSetAbstract categoryList = new ArffDataSet(new File(AmusePreferences.getMultipleTracksAnnotationTablePath()));
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
					
					// load the first classifier input for attributes information
					String currentInputFile = validatorGroundTruthSet.getAttribute("Path").getValueAt(0).toString();
					
					List<Feature> features = getHarmonizedFeatures(currentInputFile);
					
					// find out how many values per window were used
					int numberOfValuesPerWindow = -1;
					int numberOfAttributesToIgnore = 0;
					for(int i = 0; i < features.size(); i++) {
						if(attributesToIgnore.contains(i)) {
							numberOfAttributesToIgnore++;
						}
						if(features.get(i).getHistoryAsString().charAt(6) == '2' || i == features.size()-1) {
							numberOfValuesPerWindow = i;
							break;
						}
					}
					// set the numberOfValuesPerWindow in the ValidationConfiguration for the classification algorithm
					// (the classification algorithm needs the size of the windows after the attributesToIgnore have been removed)
					((ValidationConfiguration)this.getConfiguration()).setNumberOfValuesPerWindow(numberOfValuesPerWindow - numberOfAttributesToIgnore);
					
					// create the attributes
					// and save the numberOfValuesPerWindow
					for(int i = 0; i < features.size(); i++) {
						//Omit the attributes that are supposed to be ignored
						if(!attributesToIgnore.contains(i%numberOfValuesPerWindow)) {
							labeledInputForValidation.addAttribute(new NumericAttribute(features.get(i).getHistoryAsString(), new ArrayList<Double>()));
						}
					}
					
					//Add the id attribute
					labeledInputForValidation.addAttribute(new NumericAttribute("Id", new ArrayList<Double>()));
					
					//Add the attribute "NumberOfCategories"
					//It marks where the categories that are to be classified start and how many will follow
					labeledInputForValidation.addAttribute(new NumericAttribute("NumberOfCategories",new ArrayList<Double>()));
					//add the category attributes
					for(int category : attributesToPredict) {
						labeledInputForValidation.addAttribute(new NumericAttribute(validatorGroundTruthSet.getAttribute(5 + category).getName(),new ArrayList<Double>()));
					}
					
					int partSize = ((ValidationConfiguration)this.getConfiguration()).getClassificationWindowSize();
					int partStep = partSize - ((ValidationConfiguration)this.getConfiguration()).getClassificationWindowStepSize();
					
					// Create the labeled data
					for(int i=0;i<validatorGroundTruthSet.getValueCount();i++) {
						Integer trackId = new Double(validatorGroundTruthSet.getAttribute("Id").getValueAt(i).toString()).intValue();
						String[] labels = new String[numberOfCategories];
						Double[] confidences = new Double[numberOfCategories];
						Integer end = new Double(validatorGroundTruthSet.getAttribute("End").getValueAt(i).toString()).intValue();
						String path = validatorGroundTruthSet.getAttribute("Path").getValueAt(i).toString();
						
						ArrayList<Double> classificationWindowStarts = new ArrayList<Double>();
						ArrayList<Double> classificationWindowEnds = new ArrayList<Double>();
						
						int currentPosition = 0;
						for(int category : attributesToPredict) {
							labels[currentPosition] = validatorGroundTruthSet.getAttribute(5 + category).getName();
							currentPosition++;
						}
						
						// If the complete track should be read
						if(end == -1) {
							
							// TODO Consider only the classification windows up to 6 minutes of a music track; should be a parameter?
							int numberOfMaxClassificationWindows = features.get(0).getValues().size();
							for(int j=1;j<features.size();j++) {
								if(features.get(j).getValues().size() < numberOfMaxClassificationWindows) {
									numberOfMaxClassificationWindows = features.get(j).getValues().size();
								}
							}
							if((numberOfMaxClassificationWindows * (((ValidationConfiguration)this.taskConfiguration).getClassificationWindowSize() - 
									((ValidationConfiguration)this.taskConfiguration).getClassificationWindowStepSize())) > 360000) {
								numberOfMaxClassificationWindows = 360000 / (((ValidationConfiguration)this.taskConfiguration).getClassificationWindowSize() - 
										((ValidationConfiguration)this.taskConfiguration).getClassificationWindowStepSize());
								AmuseLogger.write(this.getClass().getName(), Level.WARN, 
						   				"Number of classification windows after processing reduced from " + features.get(0).getValues().size() + 
						   				" to " + numberOfMaxClassificationWindows);
							}
							
							for(int j = 0; j < numberOfMaxClassificationWindows; j++) {
								double startPosition = j*partStep;
								double endPosition = j*partStep+partSize;
								classificationWindowStarts.add(startPosition);
								classificationWindowEnds.add(endPosition);
								int currentAttribute = 0;
								for(int k = 0; k < features.size(); k++) {
									// Omit the attributes that are supposed to be ignored
									if(!attributesToIgnore.contains(k%numberOfValuesPerWindow)) {
										Double val = features.get(k).getValues().get(j)[0];
										labeledInputForValidation.getAttribute(currentAttribute).addValue(val);
										currentAttribute++;
									}
								}
								//If the classification is mutlilabel or singlelabel the confidences are added and rounded if the relationships are binary
								if(((ValidationConfiguration)this.getConfiguration()).getLabelType() != LabelType.MULTICLASS) {
									currentPosition = 0;
									for(int category : attributesToPredict) {
										String label = validatorGroundTruthSet.getAttribute(5 + category).getName();
										Double confidence = new Double(validatorGroundTruthSet.getAttribute(5 + category).getValueAt(i).toString());
										if(((ValidationConfiguration)this.getConfiguration()).getRelationshipType() == RelationshipType.CONTINUOUS) {
											labeledInputForValidation.getAttribute(label).addValue(confidence);
											confidences[currentPosition] = confidence;
										} else {
											labeledInputForValidation.getAttribute(label).addValue(confidence >= 0.5 ? 1.0 : 0.0);
											confidences[currentPosition] = confidence >= 0.5 ? 1.0 : 0.0;
										}
										currentPosition++;
									}
									//If the classification is multiclass only the relationship of the class with the highest confidence is 1
								} else {
									double maxConfidence = 0;
									int positionOfMax = 0;
									currentPosition = 0;
									for(int category : attributesToPredict) {
										Double confidence = new Double(validatorGroundTruthSet.getAttribute(5 + category).getValueAt(i).toString());
										if(confidence > maxConfidence) {
											maxConfidence = confidence;
											positionOfMax = currentPosition;
										}
										currentPosition++;
									}
									int positionOfFirstCategory = labeledInputForValidation.getAttributeCount() - numberOfCategories;
									for(int category=0;category<numberOfCategories;category++) {
										labeledInputForValidation.getAttribute(positionOfFirstCategory + category).addValue(category == positionOfMax ? 1.0 : 0.0);
										confidences[category] = category == positionOfMax ? 1.0 : 0.0;
									}
								}
								
									
								// Write the ID attribute (from what track the features are saved)
								// IMPORTANT: --------------------------------------------------- 
								// This attribute must not be used for classification model training! 
								// If any new classification algorithms are integrated into AMUSE, they must
								// handle this properly!!!
								Double id = new Double(validatorGroundTruthSet.getAttribute("Id").getValueAt(i).toString());
								labeledInputForValidation.getAttribute("Id").addValue(id);
								
								
								labeledInputForValidation.getAttribute("NumberOfCategories").addValue(new Double(numberOfCategories));								
							}
							// Add descriptions of the classification windows of the current track
							Double[] classificationWindowStartsAsArray = new Double[classificationWindowStarts.size()];
							Double[] classificationWindowEndsAsArray = new Double[classificationWindowEnds.size()];
							Double[][] relationships = new Double[classificationWindowStarts.size()][numberOfCategories];
							for(int l=0;l<classificationWindowStarts.size();l++) {
								classificationWindowStartsAsArray[l] = classificationWindowStarts.get(l);
								classificationWindowEndsAsArray[l] = classificationWindowEnds.get(l);
								for(int category=0;category<numberOfCategories;category++) {
									relationships[l][category] = confidences[category];
								}
							} 
							
							ClassifiedClassificationWindow newTrackDesc = new ClassifiedClassificationWindow(path, 
										trackId, classificationWindowStartsAsArray, classificationWindowEndsAsArray, labels, relationships);
							labeledTrackRelationships.add(newTrackDesc);
						} else {
							// TODO Consider Vocals/Piano-Recognition-Scenario!
						}
						// Go to the next description
						String newInputFile = validatorGroundTruthSet.getAttribute("Path").getValueAt(i+1).toString();
						// Go to the next music file?
						if(!newInputFile.equals(currentInputFile)) {
							currentInputFile = newInputFile;
							features = getHarmonizedFeatures(currentInputFile);
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
			DataSet labeledInputForValidation = ((DataSetInput)((ValidationConfiguration)this.getConfiguration()).
					getInputToValidate()).getDataSet();
			amuse.data.io.attributes.Attribute idAttribute = labeledInputForValidation.getAttribute("Id");
//			amuse.data.io.attributes.Attribute labelAttribute = ((DataSetInput)((ValidationConfiguration)this.getConfiguration()).
//					getInputToValidate()).getDataSet().getAttribute("Category");
				
			
			
			Integer currentTrackId = new Double(idAttribute.getValueAt(0).toString()).intValue();
			ArrayList<Double[]> relationships = new ArrayList<Double[]>();
			String[] labels = new String[numberOfCategories];
			int positionOfFirstCategory = labeledInputForValidation.getAttributeCount() - numberOfCategories;
			for(int category=0;category<numberOfCategories;category++) {
				labels[category] = labeledInputForValidation.getAttribute(positionOfFirstCategory + category).getName();
			}
			for(int i=0;i<idAttribute.getValueCount();i++) {
				Integer newTrackId = new Double(idAttribute.getValueAt(i).toString()).intValue();
				
				// New track is reached
				if(!newTrackId.equals(currentTrackId)) {
					Double[][] relationshipsAsArray = new Double[relationships.size()][numberOfCategories];
					for(int k=0;k<relationships.size();k++) {
						relationshipsAsArray[k] = relationships.get(k);
					} 
						
					ClassifiedClassificationWindow newTrackDesc = new ClassifiedClassificationWindow("", 
								currentTrackId, new Double[relationshipsAsArray.length], new Double[relationshipsAsArray.length], labels, relationshipsAsArray);
					labeledTrackRelationships.add(newTrackDesc);
					currentTrackId = newTrackId;
					relationships = new ArrayList<Double[]>();
					labels = new String[numberOfCategories];
				} 
						
				if(((ValidationConfiguration)this.getConfiguration()).getLabelType() != LabelType.MULTICLASS) {
					Double[] currentRelationships = new Double[numberOfCategories];
					for(int category=0;category<numberOfCategories;category++) {
						double confidence = (double)labeledInputForValidation.getAttribute(positionOfFirstCategory + category).getValueAt(i);
						if(((ValidationConfiguration)this.getConfiguration()).getRelationshipType() == RelationshipType.CONTINUOUS) {
							currentRelationships[category] = confidence;
						} else {
							currentRelationships[category] = confidence >= 0.5 ? 1.0 : 0.0;
						}
					}
					relationships.add(currentRelationships);
				} else {
					Double[] currentRelationships = new Double[numberOfCategories];
					double maxConfidence = 0;
					int positionOfMax = 0;
					for(int category=0;category<numberOfCategories;category++) {
						double confidence = (double)labeledInputForValidation.getAttribute(positionOfFirstCategory + category).getValueAt(i);
						if(confidence > maxConfidence) {
							maxConfidence = confidence;
							positionOfMax = category;
						}
					}
					for(int category=0;category<numberOfCategories;category++) {
						currentRelationships[category] = category == positionOfMax ? 1.0 : 0.0;
					}
					relationships.add(currentRelationships);
				}
			}
				
			// For the last track
			Double[][] relationshipsAsArray = new Double[relationships.size()][numberOfCategories];
			for(int k=0;k<relationships.size();k++) {
				relationshipsAsArray[k] = relationships.get(k);
			} 
			
			ClassifiedClassificationWindow newTrackDesc = new ClassifiedClassificationWindow("", 
						currentTrackId, new Double[relationshipsAsArray.length], new Double[relationshipsAsArray.length], labels, relationshipsAsArray);
			labeledTrackRelationships.add(newTrackDesc);
		} 
	}
	
	/**
	 * Harmonizes the raw features using the ProcessorNodeScheduler
	 * @param currentInputFile
	 * @return the harmonized features
	 * @throws NodeException
	 */
	private List<Feature> getHarmonizedFeatures(String currentInputFile) throws NodeException {
		List<File> currentInputFileList = new ArrayList<File>();
		currentInputFileList.add(new File(currentInputFile));
		
		ProcessingConfiguration pConf = new ProcessingConfiguration(new FileTable(currentInputFileList),
				((ValidationConfiguration)this.getConfiguration()).getInputFeatureList(),
				"",
				((ValidationConfiguration)this.getConfiguration()).getUnit(),
				((ValidationConfiguration)this.getConfiguration()).getClassificationWindowSize(),
				((ValidationConfiguration)this.getConfiguration()).getClassificationWindowStepSize(),
				"6",
				"");
		
		ProcessorNodeScheduler processorNodeScheduler = new ProcessorNodeScheduler(this.nodeHome + File.separator + "input" + File.separator + "task_" + 
				this.jobId + File.separator + "processor");
		
		// proceed raw feature processing
		processorNodeScheduler.proceedTask(this.nodeHome, this.jobId, pConf, false);
		
		// return the processed features
		return processorNodeScheduler.getProcessedFeatures();
	}

	/**
	 * Saves the calculated measures to the output file
	 */
	private void saveMeasuresToFile() throws NodeException {
		try {
			
			String outputPath = ((ValidationConfiguration)this.taskConfiguration).getOutputPath();
			//If the outputPath should be calculated automatically
			if(outputPath.equals("-1") || outputPath.equals("")) {
				// Check if the folder for measure file exists; if not create it
				File folderForMeasures = createMeasureFolder();
				outputPath = folderForMeasures + File.separator + "measures.arff";
				if(((ValidationConfiguration)this.taskConfiguration).getGroundTruthSourceType() != GroundTruthSourceType.CATEGORY_ID) {
					AmuseLogger.write(ValidatorNodeScheduler.class.getClass().getName(), Level.WARN,"No output path given! The results will be saved at " + outputPath);
				}
			} else {
				// create the folders
				new File(outputPath).getParentFile().mkdirs();
			}
			
			// If no measure file is there, save header
			boolean saveHeader = false;
			// FIXME Currently the file is overwritten each time for Windows compatibility during optimization task
			//if(!new File(this.folderForMeasures + File.separator + "measures.arff").exists()) {
				saveHeader = true;
			//}
			//FileOutputStream values_to = new FileOutputStream(this.folderForMeasures + File.separator + "measures.arff",true);
			FileOutputStream values_to = new FileOutputStream(outputPath);
			DataOutputStream values_writer = new DataOutputStream(values_to);
			String sep = System.getProperty("line.separator");
			
			// Saves the header
			if(saveHeader) {
				values_writer.writeBytes("@RELATION 'Classifier measures'");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE Time STRING");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE MeasureId NUMERIC");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE MeasureName STRING");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE MeasureValue STRING");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@DATA");
			}
			values_writer.writeBytes(sep);
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			
			// Save the measure values, going through all measures
			for(ValidationMeasure m : ((ValidationConfiguration)taskConfiguration).getCalculatedMeasures()) {
				values_writer.writeBytes("\"" + sdf.format(new Date()) + "\", " + 
							m.getId() + ", " + "\"" + m.getName() + "\", " + 
							m.getValue());
				values_writer.writeBytes(sep);
			}
			values_writer.close();
		} catch(IOException e) {
			throw new NodeException("Could not save measures: " + e.getMessage());
		}
	}
	
	/**
	 * Sets the name of the folder for measures and creates this folder
	 * @throws NodeException
	 */
	private File createMeasureFolder() throws NodeException {
		File folderForMeasures = null;
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
				classificationAlgorithmLoader.setFile(new File(AmusePreferences.getClassifierAlgorithmTablePath()));
	    	} else {
	    		classificationAlgorithmLoader.setFile(new File(this.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.getTaskId() + File.separator + "classifierAlgorithmTable.arff"));
	    	}
			Attribute idAttribute = classificationAlgorithmLoader.getStructure().attribute("Id");
			Attribute nameAttribute = classificationAlgorithmLoader.getStructure().attribute("Name");
			classificationAlgorithmInstance = classificationAlgorithmLoader.getNextInstance(classificationAlgorithmLoader.getStructure());
			while(classificationAlgorithmInstance != null) {
				
				// If the given classification algorithm is found..
				if(classificationAlgorithmInstance.value(idAttribute) == algorithmToSearch) {
					classificationMethodFound = true;
					
					// Set the name of folder for models and measures (combined from
					// classifier ID, parameters and name)
					String classificationAlgorithmDescription = ((ValidationConfiguration)taskConfiguration).getClassificationAlgorithmDescription();
					/**
					 * if the parameter string contains paths with file separators
					 * we have to modify it so that the model can be saved correctly
					 */
					if(classificationAlgorithmDescription.contains(File.separator)) {
						String algorithmId = classificationAlgorithmDescription.substring(0, classificationAlgorithmDescription.indexOf("["));
						String parameterString = classificationAlgorithmDescription.substring(classificationAlgorithmDescription.indexOf("["), classificationAlgorithmDescription.indexOf("]") + 1);
						String[] parameters = Algorithm.scanParameters(parameterString);
						parameterString = "[";
						for(int i = 0; i < parameters.length; i++) {
							String parameter = parameters[i];
							if(parameter.contains(File.separator)) {
								parameter = parameter.substring(parameter.lastIndexOf(File.separator) + 1, parameter.lastIndexOf("."));
							}
							parameterString += parameter;
							if(i < parameters.length - 1) {
								parameterString += "_";
							}
						}
						parameterString += "]";
						classificationAlgorithmDescription = algorithmId + parameterString;
					}
					classifierDescription = classificationAlgorithmDescription + "-" + classificationAlgorithmInstance.stringValue(nameAttribute);

					String validatorMethodId = ((ValidationConfiguration)taskConfiguration).getValidationAlgorithmDescription();
					if(validatorMethodId.contains("[")) {
						validatorMethodId = validatorMethodId.substring(0,validatorMethodId.indexOf("["));
					}
					
					// Check if the folder for measure file exists; if not create it
					String folderForMeasuresString = 
							((ValidationConfiguration)taskConfiguration).getMeasureDatabase() + File.separator + 
							categoryDescription + 
							File.separator;
					
					String[] labels = labeledTrackRelationships.get(0).getLabels();
					
					for(int i=0;i<labels.length;i++) {
						if(i!=0) {
							folderForMeasuresString += "_";
						}
						folderForMeasuresString += labels[i];
					}
					
					String inputFeaturesDescription = ((ValidationConfiguration)taskConfiguration).getInputFeaturesDescription();
					if(((ValidationConfiguration)taskConfiguration).getInputFeatureType() == InputFeatureType.RAW_FEATURES) {
						if(inputFeaturesDescription.contains(File.separator) && inputFeaturesDescription.contains(".")) {
							inputFeaturesDescription = inputFeaturesDescription.substring(inputFeaturesDescription.lastIndexOf(File.separator) + 1, inputFeaturesDescription.lastIndexOf('.'));
						}
						inputFeaturesDescription = "RAW_FEATURES_" + inputFeaturesDescription;
					}
					folderForMeasuresString += File.separator + classifierDescription + "_" + ((ValidationConfiguration)this.taskConfiguration).getRelationshipType().toString() + "_" + ((ValidationConfiguration)this.taskConfiguration).getLabelType().toString() + "_" + ((ValidationConfiguration)this.taskConfiguration).getMethodType().toString() + File.separator +
							inputFeaturesDescription + File.separator +
							validatorMethodId + 
							"-" + ((AmuseTask)vmi).getProperties().getProperty("name");
					
					folderForMeasures = new File(folderForMeasuresString);
					if(!folderForMeasures.exists()) {
						if(!folderForMeasures.mkdirs()) {
							throw new NodeException("Could not create the folder for classifier evaluation measures: " + folderForMeasures);
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
		return folderForMeasures;
	}
	
	/**
	 * @return the labeledTrackRelationships
	 */
	public ArrayList<ClassifiedClassificationWindow> getLabeledTrackRelationships() {
		return labeledTrackRelationships;
	}
	
	/**
	 * @return the labeledTrackRelationships
	 */
	public ArrayList<Double> getLabeledAverageTrackRelationships() {
		ArrayList<Double> a = new ArrayList<Double>(labeledTrackRelationships.size());
		for(ClassifiedClassificationWindow d : labeledTrackRelationships) {
			a.add(d.getMeanRelationship(0));
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
