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
 * Creation date: 09.04.2009
 */ 
package amuse.nodes.trainer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import amuse.data.GroundTruthSourceType;
import amuse.data.ClassificationType;
import amuse.data.datasets.TrainingConfigSet;
import amuse.data.io.DataInputInterface;
import amuse.data.io.FileInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.classifier.methods.supervised.FKNNAdapter;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Describes the parameters for a classification training task 
 * 
 * @author Igor Vatolkin
 * @version $Id: TrainingConfiguration.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class TrainingConfiguration extends TaskConfiguration {

	/** For Serializable interface */
	private static final long serialVersionUID = 4335790625923084158L;
	
	/** Description of the processed features model */
	private final String processedFeaturesModelName;
	
	/** Id of classification algorithm from classificatorTable.arff 
	 * (optionally with parameters listed in brackets) */
	private final String algorithmDescription;
	
	/** Id and parameters of data preprocessing algorithm (e.g. outlier removal) */
	private final String preprocessingAlgorithmDescription;
	
	/** Ground truth source */
	private DataInputInterface groundTruthSource;
	
	/** Ground truth type for this configuration */
	private final GroundTruthSourceType groundTruthSourceType;
	
	private final List<Integer> categoriesToClassify;
	private final List<Integer> featuresToIgnore;
	private final ClassificationType classificationType;
	private final boolean fuzzy;
	
	private final String trainingDescription;
	
	
	/** Alternative path for saving of training model(s) (e.g. an optimization task may train
	 * different models and compare their performance; here it is not required to save them to
	 * the central Amuse model database!) */
	private String pathToOutputModel;
	
	/** Folder to load the processed features from (default: Amuse processed feature database) */
	private String processedFeatureDatabase;
	
	/** Folder to store the classification model(s) (default: Amuse model database) */
	private String modelDatabase;
	
	/**
	 * Standard constructor
	 * @param processedFeaturesModelName Description of the processed features model
 	 * @param algorithmDescription ID of classification algorithm from classificationTrainerTable.arff
	 * @param groundTruthSource Source with ground truth for model training. Can be either
	 * - Id of the music category from $AMUSEHOME$/config/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready training input (prepared e.g. by a validator method)
	 * - Ready input (as EditableDataSet)
	 * @param groundTruthSourceType Describes the source type of ground truth
	 * @param categoriesToClassify the categories of the category file of the annotationdatabase or the attributes of the ready input that should be predicted
	 * @param featuresToIgnore features of the processed feature files or the ready input that should not be used for the classification
	 * @param classificationType is the classification unsupervised, binary, multilabel or multiclass?
	 * @param fuzzy should the classification be fuzzy?
	 * @param trainingDescription optional description of this experiment, that will be added to the name of the model
	 * @param pathToOutputModel optional path to where the model should be saved 
	 * (three possibilities are given above) 
	 */
	public TrainingConfiguration(String processedFeaturesModelName, String algorithmDescription, String preprocessingAlgorithmDescription,
			DataInputInterface groundTruthSource, GroundTruthSourceType groundTruthSourceType, List<Integer> categoriesToClassify, List<Integer> featuresToIgnore, ClassificationType classificationType, boolean fuzzy, String trainingDescription, String pathToOutputModel) {
		this.processedFeaturesModelName = processedFeaturesModelName;
		this.algorithmDescription = algorithmDescription;
		this.preprocessingAlgorithmDescription = preprocessingAlgorithmDescription;
		this.groundTruthSource = groundTruthSource;
		this.groundTruthSourceType = groundTruthSourceType;
		this.categoriesToClassify = categoriesToClassify;
		this.featuresToIgnore = featuresToIgnore;
		this.classificationType = classificationType;
		this.fuzzy = fuzzy;
		this.trainingDescription = trainingDescription;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.modelDatabase = AmusePreferences.get(KeysStringValue.MODEL_DATABASE);
		this.pathToOutputModel = pathToOutputModel;
	}

	/**
	 * Returns an array of TrainingConfigurations from the given data set
	 * @param trainingConfig Data set with configurations for one or more training tasks
	 * @return TrainingConfigurations
	 */
	public static TrainingConfiguration[] loadConfigurationsFromDataSet(TrainingConfigSet trainingConfig) throws IOException {
		ArrayList<TrainingConfiguration> taskConfigurations = new ArrayList<TrainingConfiguration>();
		
   		// Proceed music file lists one by one
	    for(int i=0;i<trainingConfig.getValueCount();i++) {
			String currentProcessedFeaturesModelName = trainingConfig.getProcessedFeatureDescriptionAttribute().getValueAt(i).toString();
			String currentAlgorithmDescription = trainingConfig.getAlgorithmIdAttribute().getValueAt(i).toString();
			String currentPreprocessingAlgorithmDescription = trainingConfig.getPreprocessingAlgorithmIdAttribute().getValueAt(i).toString();
			String currentGroundTruthSource = trainingConfig.getGroundTruthSourceAttribute().getValueAt(i).toString();
			String currentPathToOutputModel = trainingConfig.getPathToOutputModelAttribute().getValueAt(i).toString();
			GroundTruthSourceType gtst;
			if(trainingConfig.getGroundTruthSourceTypeAttribute().getValueAt(i).toString().equals(new String("CATEGORY_ID"))) {
				gtst = GroundTruthSourceType.CATEGORY_ID;
			} else if(trainingConfig.getGroundTruthSourceTypeAttribute().getValueAt(i).toString().equals(new String("FILE_LIST"))) {
				gtst = GroundTruthSourceType.FILE_LIST;
			} else {
				gtst = GroundTruthSourceType.READY_INPUT;
			}
			
			
			String categoriesToClassifyString = trainingConfig.getCategoriesToClassifyAttribute().getValueAt(i).toString();
			categoriesToClassifyString = categoriesToClassifyString.replaceAll("\\[", "").replaceAll("\\]", "");
			String[] categoriesToClassifyStringArray = categoriesToClassifyString.split("\\s*,\\s*");
			List<Integer> currentCategoriesToClassify = new ArrayList<Integer>();
			try {
				for(String str : categoriesToClassifyStringArray) {
					if(!str.equals("")) {
						currentCategoriesToClassify.add(Integer.parseInt(str));
					} else {
						throw new IOException("No categories for training were specified.");
					}
				}
			} catch(NumberFormatException e) {
				throw new IOException("The categories for training were not properly specified.");
			}
			
			String featuresToIgnoreString = trainingConfig.getFeaturesToIgnoreAttribute().getValueAt(i).toString();
			featuresToIgnoreString = featuresToIgnoreString.replaceAll("\\[", "").replaceAll("\\]", "");
			String[] featuresToIgnoreStringArray = featuresToIgnoreString.split("\\s*,\\s*");
			List<Integer> currentFeaturesToIgnore = new ArrayList<Integer>();
			try {
				for(String str : featuresToIgnoreStringArray) {
					if(!str.equals("")) {
						currentFeaturesToIgnore.add(Integer.parseInt(str));
					}
				}
			} catch(NumberFormatException e) {
				AmuseLogger.write(TrainingConfiguration.class.getName(), Level.WARN,
						"The features to ignore were not properly specified. All features will be used for training.");
				currentFeaturesToIgnore = new ArrayList<Integer>();
			}
			
			ClassificationType currentClassificationType;
			if(trainingConfig.getClassificationTypeAttribute().getValueAt(i).toString().equals("UNSUPERVISED")) {
				currentClassificationType = ClassificationType.UNSUPERVISED;
			} else if(trainingConfig.getClassificationTypeAttribute().getValueAt(i).toString().equals("BINARY")) {
				currentClassificationType = ClassificationType.BINARY;
			} else if(trainingConfig.getClassificationTypeAttribute().getValueAt(i).equals("MULTILABEL")) {
				currentClassificationType = ClassificationType.MULTILABEL;
			} else { //Ist es gut Sachen einfach standardmaessig als multiclass einzustellen, wenn sich jemand vertippt oder so?
				currentClassificationType = ClassificationType.MULTICLASS;
			}
			
			boolean currentFuzzy = trainingConfig.getFuzzyAttribute().getValueAt(i) >= 0.5;
			
			String currentTrainingDescription = trainingConfig.getTrainingDescriptionAttribute().getValueAt(i).toString();
			
			
				
			// Create a training task
			TrainingConfiguration trConfig = new TrainingConfiguration(currentProcessedFeaturesModelName, currentAlgorithmDescription,
		    		currentPreprocessingAlgorithmDescription, new FileInput(currentGroundTruthSource),gtst, currentCategoriesToClassify, currentFeaturesToIgnore, currentClassificationType, currentFuzzy, currentTrainingDescription, currentPathToOutputModel);
			taskConfigurations.add(trConfig);

			AmuseLogger.write(TrainingConfiguration.class.getName(), Level.DEBUG,  
					"Training task for ground truth source " + 
					currentGroundTruthSource + " loaded");
		}
		
		// Create an array
	    TrainingConfiguration[] tasks = new TrainingConfiguration[taskConfigurations.size()];
		for(int i=0;i<taskConfigurations.size();i++) {
	    	tasks[i] = taskConfigurations.get(i);
	    }
		return tasks;
	}
	
	/**
	 * Returns an array of TrainingConfigurations from the given ARFF file
	 * @param configurationFile ARFF file with configurations for one or more training tasks
	 * @return TrainingConfigurations
	 * @throws IOException 
	 */
	public static TrainingConfiguration[] loadConfigurationsFromFile(File configurationFile) throws IOException {
		TrainingConfigSet trainingConfig = new TrainingConfigSet(configurationFile);
		return loadConfigurationsFromDataSet(trainingConfig);
	}
	
	/**
	 * @return the processedFeaturesModelName
	 */
	public String getProcessedFeaturesModelName() {
		return processedFeaturesModelName;
	}

	/**
	 * @return the algorithmDescription
	 */
	public String getAlgorithmDescription() {
		return algorithmDescription;
	}

	/**
	 * @return the preprocessingAlgorithmDescription
	 */
	public String getPreprocessingAlgorithmDescription() {
		return preprocessingAlgorithmDescription;
	}
	
	/**
	 * @return the groundTruthSource
	 */
	public DataInputInterface getGroundTruthSource() {
		return groundTruthSource;
	}

	/**
	 * @return the groundTruthSourceType
	 */
	public GroundTruthSourceType getGroundTruthSourceType() {
		return groundTruthSourceType;
	}
	
	public List<Integer> getCategoriesToClassify(){
		return categoriesToClassify;
	}
	
	public List<Integer> getFeaturesToIgnore(){
		return featuresToIgnore;
	}
	
	public ClassificationType getClassificationType() {
		return classificationType;
	}
	
	public boolean isFuzzy() {
		return fuzzy;
	}
	
	public String getTrainingDescription() {
		return trainingDescription;
	}

	/**
	 * @return the pathToOutputModel
	 */
	public String getPathToOutputModel() {
		return pathToOutputModel;
	}
	
	/**
	 * Sets the path to folder to load the processed features from (default: Amuse processed feature database)
	 * @param processedFeatureDatabase Path to folder
	 */
	public void setProcessedFeatureDatabase(String processedFeatureDatabase) {
		this.processedFeatureDatabase = processedFeatureDatabase;
	}

	/**
	 * @return Folder to load the processed features from (default: Amuse processed feature database)
	 */
	public String getProcessedFeatureDatabase() {
		return processedFeatureDatabase;
	}
	
	/**
	 * Sets the path to folder to store the classification model(s) (default: Amuse model database)
	 * @param processedFeatureDatabase Path to folder
	 */
	public void setModelDatabase(String modelDatabase) {
		this.modelDatabase = modelDatabase;
	}

	/**
	 * @return Folder to store the classification model(s) (default: Amuse model database)
	 */
	public String getModelDatabase() {
		return modelDatabase;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getType()
	 */
	public String getType() {
		return "Classification training";
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getDescription()
	 */
	public String getDescription() {
        return new String("Ground truth source: " + groundTruthSource);
	}

	/**
	 * @param groundTruthSource the groundTruthSource to set
	 */
	public void setGroundTruthSource(DataInputInterface groundTruthSource) {
		this.groundTruthSource = groundTruthSource;
	}

	/**
	 * @param pathToOutputModel the pathToOutputModel to set
	 */
	public void setPathToOutputModel(String pathToOutputModel) {
		this.pathToOutputModel = pathToOutputModel;
	}
	
	/**
	 * Creates a copy of this configuration
	 */
	public TrainingConfiguration clone(){
		TrainingConfiguration conf = new TrainingConfiguration(processedFeaturesModelName, algorithmDescription, preprocessingAlgorithmDescription, groundTruthSource, groundTruthSourceType, categoriesToClassify, featuresToIgnore, classificationType, fuzzy, trainingDescription, pathToOutputModel); 
		return conf;
	}
}
