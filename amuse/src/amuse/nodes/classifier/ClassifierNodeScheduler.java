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
 * Creation date: 29.02.2008
 */ 
package amuse.nodes.classifier;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import amuse.data.io.FileListInput;
import amuse.data.io.attributes.NumericAttribute;
import amuse.interfaces.nodes.NodeEvent;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.NodeScheduler;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.interfaces.BinaryClassifiedSongPartitions;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.classifier.interfaces.ClassifierInterface;
import amuse.nodes.classifier.interfaces.MulticlassClassifiedSongPartitions;
import amuse.nodes.classifier.interfaces.SongPartitionsDescription;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * ClassifierNodeScheduler is responsible for the classifier node. The given music files
 * are classified with the previously learned classification model. 
 * 
 * @author Igor Vatolkin
 * @version $Id: ClassifierNodeScheduler.java 1233 2010-08-02 14:51:50Z vatolkin $
 */
public class ClassifierNodeScheduler extends NodeScheduler { 

	/** Classifier adapter */
	ClassifierInterface cad = null;
	
	/** Parameters for classification algorithm if required */
	private String requiredParameters = null;
	
	/** Music category id and name separated by '-', e.g. "15-Music_for_Inspiration" */
	private String categoryDescription = null;
	
	/** Here the description of data instances (from what music files and intervals) is saved */
	private ArrayList<SongPartitionsDescription> descriptionOfClassifierInput = null;
	
	/**
	 * Constructor
	 */
	public ClassifierNodeScheduler(String folderForResults) throws NodeException {
		super(folderForResults);
		requiredParameters = new String();
		categoryDescription = new String();
	}
	
	/**
	 * Main method for classification
	 * @param args Classification configuration
	 */
	public static void main(String[] args) {
		
		// Create the node scheduler
		ClassifierNodeScheduler thisScheduler = null;
		try {
			thisScheduler = new ClassifierNodeScheduler(args[0] + "/input/task_" + args[1]);
		} catch(NodeException e) {
			AmuseLogger.write(ClassifierNodeScheduler.class.getName(), Level.ERROR,
					"Could not create folder for classifier node intermediate results: " + e.getMessage());
			return;
		}
		
		// Proceed the task
		thisScheduler.proceedTask(args);
		
		// Remove the folder for input and intermediate results
		try {
			thisScheduler.removeInputFolder();
		} catch(NodeException e) {
				AmuseLogger.write(ClassifierNodeScheduler.class.getClass().getName(), Level.WARN,
					"Could not remove properly the folder with intermediate results '" + 
					thisScheduler.nodeHome + "/input/task_'" + thisScheduler.jobId + 
					"; please delete it manually! (Exception: "+ e.getMessage() + ")");
		}
	}
	
	/**
	 * Proceeds classification task and returns the results as ArrayList<ClassifiedSongPartitionsDescription>
	 * OR saves them to file
	 * TODO sollte einheitlich sein: Ergebnisse sollen in ClassificationConfiguration gespeichert werden
	 */
	public ArrayList<ClassifiedSongPartitions> proceedTask(String nodeHome, long jobId, TaskConfiguration classificationConfiguration,
			boolean saveToFile) throws NodeException {
		
		// --------------------------------------------
		// (I): Configure classification node scheduler
		// --------------------------------------------
		this.nodeHome = nodeHome;
		if(this.nodeHome.startsWith(System.getenv("AMUSEHOME"))) {
			this.directStart = true;
		}
		this.jobId = new Long(jobId);
		this.taskConfiguration = classificationConfiguration;
		
		// If this node is started directly, the properties are loaded from AMUSEHOME folder;
		// if this node is started via command line (e.g. in a grid, the properties are loaded from
		// %classifier home folder%/input
		if(!this.directStart) {
			File preferencesFile = new File(this.nodeHome + "/input/task_" + this.jobId + "/amuse.properties");
			AmusePreferences.restoreFromFile(preferencesFile);
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Classifier node scheduler started");
		
		// ----------------------------------------------------------------
		// (II): Convert feature vectors + descriptions to classifier input
		// ----------------------------------------------------------------
		try {
			this.prepareClassifierInput();
		} catch(NodeException e) {
			throw new NodeException("Could not prepare classifier input: " + e.getMessage()); 
		}
		
		// ------------------------------------------
		// (III): Configure the classification method
		// ------------------------------------------
		try {
			this.configureClassificationMethod();
		} catch(NodeException e) {
			throw new NodeException("Configuration of classifier failed: " + e.getMessage()); 
		}
		
		// -------------------------------------
		// (IV): Start the classification method
		// -------------------------------------
		ArrayList<ClassifiedSongPartitions> classifierResult = null;
		try {
			this.classify();
			classifierResult = createClassifiedSongPartitionDescriptions();
			if(saveToFile) {
				saveClassifierResultToFile(classifierResult);
			}
		} catch(NodeException e) {
			throw new NodeException("Classification failed: " + e.getMessage()); 
		}
		
		// ---------------------------------------------------------------------------------
		// (V) If started directly, remove generated data and fire event for Amuse scheduler
		// ---------------------------------------------------------------------------------
		if(this.directStart) {
			try {
				this.cleanInputFolder();
			} catch(NodeException e) {
				throw new NodeException("Could not remove properly the intermediate results '" + 
					this.nodeHome + "/input/task_'" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
			}
			this.fireEvent(new NodeEvent(NodeEvent.CLASSIFICATION_COMPLETED, this));
		}
		
		// If the classification result is saved to file, returns null here
		return classifierResult;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String, long, amuse.interfaces.nodes.TaskConfiguration)
	 */
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration classificationConfiguration) {
		
		// Since ClassifierNodeScheduler may output the result as ArrayList<ClassifiedSongPartitionsDescription>
		// and not only as file output, here the file output is set
		try {
			proceedTask(nodeHome, jobId, classificationConfiguration, true);
		} catch (NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
					"Could not proceed classification task: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String[])
	 */
	public void proceedTask(String[] args) {
		if(args.length < 2) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL, 2 - args.length + 
					" arguments are missing; The usage is 'ClassifierNodeScheduler %1 %2', where: \n" +
					"%1 - Home folder of this node\n" +
					"%2 - Unique (for currently running Amuse instance) task Id\n"); 
			System.exit(1);
		}
		
		// Load the task configuration from %CLASSIFIERHOME%/task.ser
		ClassificationConfiguration[] classifierConfig = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(args[0] + File.separator + "task_" + args[1] + ".ser");
			in = new ObjectInputStream(fis);
			Object o = in.readObject();
			classifierConfig = (ClassificationConfiguration[])o;
		    in.close();
		} catch(IOException ex) {
		    ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Proceed the classification task(s)
		for(int i=0;i<classifierConfig.length;i++) {
			proceedTask(args[0],new Long(args[1]),classifierConfig[i]);
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "Classifier node is going to start job " + 
					(i+1) + File.separator + classifierConfig.length);
		}
	}
	
	/**
	 * Converts feature vectors + descriptions to classifier input
	 * @throws NodeException
	 */
	private void prepareClassifierInput() throws NodeException {
		descriptionOfClassifierInput = new ArrayList<SongPartitionsDescription>();
		
		if(! (((ClassificationConfiguration)this.getConfiguration()).getInputToClassify() instanceof DataSetInput)) {
			
			DataSet inputForClassification = null;
		
			try {
			
				// If the input for classification has been previously prepared (e.g. from validator), it is ready! 
				if(((ClassificationConfiguration)this.getConfiguration()).getInputSourceType().
						equals(ClassificationConfiguration.InputSourceType.READY_INPUT)) {
					inputForClassification = new DataSet(new File(((ClassificationConfiguration)this.getConfiguration()).
						getInputToClassify().toString()),"ClassificationSet");
				} 
				
				else {
					
					// Set the category description only if FILE_LIST as source is used!!!
					DataSetAbstract categoryList = null;
					try {
						categoryList = new ArffDataSet(new File(AmusePreferences.get(KeysStringValue.CATEGORY_DATABASE)));
					} catch (IOException e) {
						throw new NodeException("Could not load the category table: " + e.getMessage()); 
					}
					int i=0;
					while(i < categoryList.getValueCount()) {
						Integer id = new Double(categoryList.getAttribute("Id").getValueAt(i).toString()).intValue();
						if(id.toString().equals(
								((ClassificationConfiguration)this.taskConfiguration).getCategoryId().toString())) {
							this.categoryDescription = ((ClassificationConfiguration)this.taskConfiguration).getCategoryId().toString() + 
								"-" + categoryList.getAttribute("CategoryName").getValueAt(i).toString();
							break;
						}
						i++;
					}

					inputForClassification = new DataSet("ClassificationSet");
					
					// Load the processed feature files for a given music file list
					// Load the first classifier input for attributes information
					String currentInputFile = ((FileListInput)((ClassificationConfiguration)this.taskConfiguration).getInputToClassify()).
						getInputFiles().get(0).toString();
					if(currentInputFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
						currentInputFile = 
							((ClassificationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
							+ File.separator 
							+ currentInputFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length() + 1,
									currentInputFile.lastIndexOf("."))
							+ File.separator
							+ currentInputFile.substring(currentInputFile.lastIndexOf(File.separator) + 1,
									currentInputFile.lastIndexOf("."))
							+ "_" 
							+ ((ClassificationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
					}
					else{
						currentInputFile = 
							((ClassificationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
							+ File.separator 
							+ currentInputFile.substring(0,
									currentInputFile.lastIndexOf(".")) 
							+ File.separator 
							+ currentInputFile.substring(currentInputFile.lastIndexOf(File.separator) + 1,
									currentInputFile.lastIndexOf(".")) 
							+ "_" 
							+ ((ClassificationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
					}
					
					ArffLoader classifierInputLoader = new ArffLoader();
					Instance inputInstance;
					classifierInputLoader.setFile(new File(currentInputFile));
					inputInstance = classifierInputLoader.getNextInstance(classifierInputLoader.getStructure());
						
					// Save the attributes omitting UNIT, START and END attributes (they describe the partition for modeled features)
					for(i=0;i<classifierInputLoader.getStructure().numAttributes()-3;i++) {
						inputForClassification.addAttribute(new NumericAttribute(inputInstance.attribute(i).name(),
								new ArrayList<Double>()));
					}
						
					// Save the processed features for classifier
					for(int k=0;k<((FileListInput)((ClassificationConfiguration)this.taskConfiguration).getInputToClassify()).getInputFiles().size();k++) {
						currentInputFile = ((FileListInput)((ClassificationConfiguration)this.taskConfiguration).getInputToClassify()).getInputFiles().get(k).toString();
						ArrayList<Double> partitionStarts = new ArrayList<Double>();
						ArrayList<Double> partitionEnds = new ArrayList<Double>();
						
						// Save the name of music file for later conversion of classification output
						String currentInputSong = new String(currentInputFile);
						if(currentInputFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
							currentInputFile = 
								((ClassificationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
								+ File.separator 
								+ currentInputFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length() + 1,
										currentInputFile.lastIndexOf("."))
								+ File.separator
								+ currentInputFile.substring(currentInputFile.lastIndexOf(File.separator) + 1,
										currentInputFile.lastIndexOf("."))
								+ "_" 
								+ ((ClassificationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
						}
						else{
							currentInputFile = 
								((ClassificationConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
								+ File.separator 
								+ currentInputFile.substring(0,
										currentInputFile.lastIndexOf(".")) 
								+ File.separator 
								+ currentInputFile.substring(currentInputFile.lastIndexOf(File.separator) + 1,
										currentInputFile.lastIndexOf(".")) 
								+ "_" 
								+ ((ClassificationConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
						}
						
						
						
						AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading:  " + currentInputFile);
							
						// Load processed features of the current file and save them to classifier input file
						ArffLoader processedFeaturesLoader = new ArffLoader();
						Instance processedFeaturesInstance;
						processedFeaturesLoader.setFile(new File(currentInputFile));
						processedFeaturesInstance = processedFeaturesLoader.getNextInstance(processedFeaturesLoader.getStructure());
						while(processedFeaturesInstance != null) {
							double startPosition = processedFeaturesInstance.value(processedFeaturesLoader.getStructure().attribute("Start"));
							double endPosition = processedFeaturesInstance.value(processedFeaturesLoader.getStructure().attribute("End"));
							partitionStarts.add(startPosition);
							partitionEnds.add(endPosition);
							
							// Save the processed features (attributes) omitting UNIT, START and END attributes 
							// (they describe the partition for modeled features)
							for(i=0;i<processedFeaturesInstance.numAttributes()-3;i++) {
								Double val = processedFeaturesInstance.value(i);
								inputForClassification.getAttribute(i).addValue(val);
							}
							
							processedFeaturesInstance = processedFeaturesLoader.getNextInstance(processedFeaturesLoader.getStructure());
						}
						
						// Add descriptions of the partitions of the current song
						Double[] partitionStartsAsArray = new Double[partitionStarts.size()];
						Double[] partitionEndsAsArray = new Double[partitionEnds.size()];
						for(int l=0;l<partitionStarts.size();l++) {
							partitionStartsAsArray[l] = partitionStarts.get(l);
							partitionEndsAsArray[l] = partitionEnds.get(l);
						}
						int currentInputSongId = ((FileListInput)((ClassificationConfiguration)this.taskConfiguration).getInputToClassify()).getInputFileIds().get(k);
						descriptionOfClassifierInput.add(new SongPartitionsDescription(currentInputSong,currentInputSongId,
								partitionStartsAsArray,partitionEndsAsArray));
					}
				} 
			} catch(IOException e) {
				throw new NodeException(e.getMessage());
			}
			
			// Replace the input to classify by the data set input loaded into memory
			((ClassificationConfiguration)this.taskConfiguration).setInputToClassify(new DataSetInput(inputForClassification));
		}
		
		// Load only the song information if the data is already prepared
		else {
			if(((ClassificationConfiguration)this.getConfiguration()).getInputToClassify() instanceof DataSetInput) {
				amuse.data.io.attributes.Attribute idAttribute = ((DataSetInput)((ClassificationConfiguration)this.getConfiguration()).
						getInputToClassify()).getDataSet().getAttribute("Id");
				
				Integer currentSongId = new Double(idAttribute.getValueAt(0).toString()).intValue();
				Integer numberOfPartitionsInCurrentSong = 0;
				for(int i=0;i<idAttribute.getValueCount();i++) {
					Integer newSongId = new Double(idAttribute.getValueAt(i).toString()).intValue();
					
					// New song is reached
					if(!newSongId.equals(currentSongId)) {
						SongPartitionsDescription newSongDesc = new SongPartitionsDescription("", 
								currentSongId, new Double[numberOfPartitionsInCurrentSong], new Double[numberOfPartitionsInCurrentSong]);
						descriptionOfClassifierInput.add(newSongDesc);
						currentSongId = newSongId;
						numberOfPartitionsInCurrentSong = 0;
					}
					numberOfPartitionsInCurrentSong++;
				}
				
				// For the last song
				SongPartitionsDescription newSongDesc = new SongPartitionsDescription("", 
						currentSongId, new Double[numberOfPartitionsInCurrentSong], new Double[numberOfPartitionsInCurrentSong]);
				descriptionOfClassifierInput.add(newSongDesc);
			} 
		}
	}
	
	/**
	 * Configures the classification method
	 * @throws NodeException
	 */
	private void configureClassificationMethod() throws NodeException {
		Integer requiredAlgorithm; 

		// If parameter string for this algorithm exists..
		if(((ClassificationConfiguration)taskConfiguration).getAlgorithmDescription().contains("[") && 
				((ClassificationConfiguration)taskConfiguration).getAlgorithmDescription().contains("]")) {
			requiredAlgorithm = new Integer(((ClassificationConfiguration)taskConfiguration).
					getAlgorithmDescription().substring(0,((ClassificationConfiguration)taskConfiguration).
							getAlgorithmDescription().indexOf("[")));
			this.requiredParameters = ((ClassificationConfiguration)taskConfiguration).getAlgorithmDescription().
				substring(((ClassificationConfiguration)taskConfiguration).getAlgorithmDescription().indexOf("[")+1,
						((ClassificationConfiguration)taskConfiguration).getAlgorithmDescription().lastIndexOf("]")); 
		} else {
			requiredAlgorithm = new Integer(((ClassificationConfiguration)taskConfiguration).getAlgorithmDescription());
			this.requiredParameters = null;
		}
		boolean algorithmFound = false;
		try {
	    	ArffLoader classifierTableLoader = new ArffLoader();
	    	if(this.directStart) {
	    		classifierTableLoader.setFile(new File(System.getenv("AMUSEHOME") + "/config/classifierAlgorithmTable.arff"));
	    	} else {
	    		classifierTableLoader.setFile(new File(this.nodeHome + "/input/task_" + this.jobId + "/classifierAlgorithmTable.arff"));
	    	}
			Instance currentInstance = classifierTableLoader.getNextInstance(classifierTableLoader.getStructure());
			Attribute idAttribute = classifierTableLoader.getStructure().attribute("Id");
			Attribute nameAttribute = classifierTableLoader.getStructure().attribute("Name");
			Attribute classifierAdapterClassAttribute = classifierTableLoader.getStructure().attribute("ClassifierAdapterClass");
			Attribute homeFolderAttribute = classifierTableLoader.getStructure().attribute("HomeFolder");
			Attribute startScriptAttribute = classifierTableLoader.getStructure().attribute("StartScript");
			Attribute inputBaseClassificationBatchAttribute = classifierTableLoader.getStructure().attribute("InputBaseClassificationBatch");
			Attribute inputClassificationBatchAttribute = classifierTableLoader.getStructure().attribute("InputClassificationBatch");
			while(currentInstance != null) {
				Integer idOfCurrentAlgorithm = new Double(currentInstance.value(idAttribute)).intValue();
				if(idOfCurrentAlgorithm.equals(requiredAlgorithm)) {
					
					// Configure the adapter class
					try {
						Class<?> adapter = Class.forName(currentInstance.stringValue(classifierAdapterClassAttribute));
						this.cad = (ClassifierInterface)adapter.newInstance();
						Properties classifierProperties = new Properties();
						Integer id = new Double(currentInstance.value(idAttribute)).intValue();
						classifierProperties.setProperty("id",id.toString());
						classifierProperties.setProperty("name",currentInstance.stringValue(nameAttribute));
						classifierProperties.setProperty("classifierFolderName",currentInstance.stringValue(homeFolderAttribute));
						if(directStart) {
							classifierProperties.setProperty("classifierFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + currentInstance.stringValue(homeFolderAttribute));
						} else {
							classifierProperties.setProperty("classifierFolder",nodeHome + File.separator + "tools" + File.separator + currentInstance.stringValue(homeFolderAttribute));
						}
						classifierProperties.setProperty("startScript",currentInstance.stringValue(startScriptAttribute));
						classifierProperties.setProperty("inputBaseBatch",currentInstance.stringValue(inputBaseClassificationBatchAttribute));
						classifierProperties.setProperty("inputBatch",currentInstance.stringValue(inputClassificationBatchAttribute));
						classifierProperties.setProperty("categoryDescription", this.categoryDescription);
						((AmuseTask)this.cad).configure(classifierProperties,this,this.requiredParameters);
						
						AmuseLogger.write(this.getClass().getName(), Level.INFO, 
								"Classifier is configured: " + currentInstance.stringValue(classifierAdapterClassAttribute));
					} catch(ClassNotFoundException e) {
						e.printStackTrace();
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Classifier class cannot be located: " + currentInstance.stringValue(classifierAdapterClassAttribute));
						System.exit(1);
					} catch(IllegalAccessException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Classifier class or its nullary constructor is not accessible: " + currentInstance.stringValue(classifierAdapterClassAttribute));
						System.exit(1);
					} catch(InstantiationException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Instantiation failed for classifier class: " + currentInstance.stringValue(classifierAdapterClassAttribute));
						System.exit(1);
					} catch(NodeException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Setting of parameters failed for classifier class: " + e.getMessage());
						System.exit(1);
					}
					
					algorithmFound = true;
					break;
				} 
				currentInstance = classifierTableLoader.getNextInstance(classifierTableLoader.getStructure());
			}
			
			if(!algorithmFound) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
						"Algorithm with id " + ((ClassificationConfiguration)taskConfiguration).getAlgorithmDescription() + 
						" was not found, task aborted");
				System.exit(1);
			}

	    } catch(IOException e) {
	    	throw new NodeException(e.getMessage());
	    }
	}
	
	/**
	 * Starts the classification method
	 * @throws NodeException
	 */
	private void classify() throws NodeException {
		try {
	    	// Check the folder for model file if it exists
			if(this.requiredParameters != null) {
				this.requiredParameters = "[" + this.requiredParameters + "]";
			} else {
				this.requiredParameters = "";
			}
	    	
			// Find the classification model in the Amuse model database or set the path to a concrete
			// model given in ClassificationConfiguration.pathToInputModel (for validator or optimizer)
			String pathToModel = new String();
			if(((ClassificationConfiguration)this.taskConfiguration).getPathToInputModel() == null
					|| ((ClassificationConfiguration)this.taskConfiguration).getPathToInputModel().equals(new String("-1"))) {
				File folderForModels = new File(AmusePreferences.get(KeysStringValue.MODEL_DATABASE)
						+ "/"
						+ this.categoryDescription + "/" + ((AmuseTask)this.cad).getProperties().getProperty("id") 
						+ "-" 
						+ ((AmuseTask)this.cad).getProperties().getProperty("name") 
						+ this.requiredParameters 
						+ "/" 
						+ ((ClassificationConfiguration)taskConfiguration).getProcessedFeaturesModelName());
				pathToModel = folderForModels + "/model.mod";
			} else {
				pathToModel = ((ClassificationConfiguration)this.taskConfiguration).getPathToInputModel();
			}
			
	    	// Classify
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the classification with " + 
					((AmuseTask)this.cad).getProperties().getProperty("name") + "...");
			this.cad.classify(pathToModel);
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "..classification finished!");
	    } catch(NodeException e) {
			throw new NodeException("Problem during classification: " + e.getMessage());
	    }
	}
	
	private ArrayList<ClassifiedSongPartitions> createClassifiedSongPartitionDescriptions() {
		ArrayList<ClassifiedSongPartitions> classificationResults = new ArrayList<ClassifiedSongPartitions>();
		
		DataSet d = ((DataSetInput)((ClassificationConfiguration)taskConfiguration).getInputToClassify()).getDataSet();
		
		// Is the current classification result binary or multiclass?
		boolean isMulticlass = false;
		String predictedLabel = d.getAttribute("PredictedCategory").getValueAt(0).toString();
		if(predictedLabel.startsWith("NOT")) {
			predictedLabel = predictedLabel.substring(4,predictedLabel.length());
		}
		for(int i=1;i<d.getAttribute("PredictedCategory").getValueCount();i++) {
			String currentPredictedLabel = d.getAttribute("PredictedCategory").getValueAt(i).toString();
			if(currentPredictedLabel.startsWith("NOT")) {
				currentPredictedLabel = currentPredictedLabel.substring(4,currentPredictedLabel.length());
			}
			if(!currentPredictedLabel.equals(predictedLabel)) {
				isMulticlass = true;
				break;
			}
		}
		
		// Go through all songs
		int currentPartition = 0;
		for(int i=0;i<descriptionOfClassifierInput.size();i++) {
			int numberOfCorrespondingPartitions = descriptionOfClassifierInput.get(i).getStartMs().length;
			
			// Gather the partition data for this song
			Double[] relationships = null;
			String[] categories = null;
			
			if(!isMulticlass) {
				relationships = new Double[numberOfCorrespondingPartitions];
				for(int j=0;j<numberOfCorrespondingPartitions;j++) {
					relationships[j] = d.getAttribute("PredictedCategory").getValueAt(currentPartition).toString().startsWith("NOT") ? 
							0.0 : 1.0;
					currentPartition++;
				}
			} else {
				categories = new String[numberOfCorrespondingPartitions];
				relationships = new Double[numberOfCorrespondingPartitions];
				for(int j=0;j<numberOfCorrespondingPartitions;j++) {
					categories[j] = d.getAttribute("PredictedCategory").getValueAt(currentPartition).toString();
					relationships[j] = 1.0; // TODO Currently the single partition cannot be fuzzy classified
					currentPartition++;
				}
			}
			
			// Save the partition data for this song
			if(!isMulticlass) {
				classificationResults.add(new BinaryClassifiedSongPartitions(descriptionOfClassifierInput.get(i).getPathToMusicSong(), 
					descriptionOfClassifierInput.get(i).getSongId(),
					descriptionOfClassifierInput.get(i).getStartMs(), 
					descriptionOfClassifierInput.get(i).getEndMs(), predictedLabel, relationships));
			} else {
				classificationResults.add(new MulticlassClassifiedSongPartitions(descriptionOfClassifierInput.get(i).getPathToMusicSong(), 
					descriptionOfClassifierInput.get(i).getSongId(),
					descriptionOfClassifierInput.get(i).getStartMs(), 
					descriptionOfClassifierInput.get(i).getEndMs(), categories, relationships));
			}
		}
		
		return classificationResults;
	}

	/**
	 * Saves the results of classification to the given output file
	 */
	private void saveClassifierResultToFile(ArrayList<ClassifiedSongPartitions> classifierResult) throws NodeException {
		try {
			File classifierResultFile = new File(((ClassificationConfiguration)taskConfiguration).getClassificationOutput());
			if (classifierResultFile.exists())
				if (!classifierResultFile.canWrite()) {
					throw new NodeException("Cannot save classification results");
				}
			if (!classifierResultFile.exists())
				classifierResultFile.createNewFile();
			
			FileOutputStream values_to = new FileOutputStream(classifierResultFile);
			DataOutputStream values_writer = new DataOutputStream(values_to);
			String sep = System.getProperty("line.separator");
			values_writer.writeBytes("% Classifier result");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@RELATION 'Classification result'");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE Id NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE Filename STRING");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE StartMs NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE EndMs NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE Category STRING");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE Relationship NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@DATA");
			values_writer.writeBytes(sep);
			String categoryNameString = categoryDescription.substring(categoryDescription.indexOf("-") + 1);
	        
			// If the partition classifications should be combined
			if(((ClassificationConfiguration)taskConfiguration).getMergeSongResults().equals(new Integer("1"))) {
				
				// Go through all songs
				for(int i=0;i<classifierResult.size();i++) {
					double meanRelationship = 0d;
					String currentSongName = classifierResult.get(i).getPathToMusicSong();
					
					// Go through all partitions of the current song
					for(int j=0;j<classifierResult.get(i).getRelationships().length;j++) {
						meanRelationship += classifierResult.get(i).getRelationships()[j];
					}
					meanRelationship /= classifierResult.get(i).getRelationships().length;
					
					// Save the results
					values_writer.writeBytes(descriptionOfClassifierInput.get(i).getSongId() + ",'" + currentSongName + "',-1,-1," +
							categoryNameString + "," + meanRelationship);
					values_writer.writeBytes(sep);
				}
			}
			// If the classification results for each partition should be saved
			else {
				// Go through all songs
				for(int i=0;i<classifierResult.size();i++) {
					String currentSongName = classifierResult.get(i).getPathToMusicSong();
					
					// Go through all partitions of the current song
					for(int j=0;j<classifierResult.get(i).getRelationships().length;j++) {
						
						// Save the results
						values_writer.writeBytes(descriptionOfClassifierInput.get(i).getSongId() + "," + currentSongName + "," + 
								classifierResult.get(i).getStartMs()[j] + "," + 
								classifierResult.get(i).getEndMs()[j] + "," + 
								categoryNameString + "," + classifierResult.get(i).getRelationships()[j]);
						values_writer.writeBytes(sep);
					}
				}
			}
			values_writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	
}

