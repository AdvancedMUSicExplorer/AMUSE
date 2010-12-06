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

import org.apache.log4j.Level;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataInputInterface;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.FileInput;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Describes the parameters for a classification training task 
 * 
 * @author Igor Vatolkin
 * @version $Id: $
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
	
	/** Defines the ground truth type. Can be either
	 * - Id of the music category from $AMUSECATEGORYDATABASE$/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready training input (prepared e.g. by a validator method) */
	public enum GroundTruthSourceType {CATEGORY_ID, FILE_LIST, READY_INPUT};
	
	/** Ground truth type for this configuration */
	private final GroundTruthSourceType groundTruthSourceType;
	
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
	 * - Id of the music category from $AMUSECATEGORYDATABASE$/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready training input (prepared e.g. by a validator method)
	 * - Ready input (as EditableDataSet)
	 * @param groundTruthSourceType Describes the source type of ground truth 
	 * (three possibilities are given above) 
	 */
	public TrainingConfiguration(String processedFeaturesModelName, String algorithmDescription, String preprocessingAlgorithmDescription,
			DataInputInterface groundTruthSource, GroundTruthSourceType groundTruthSourceType/*, String pathToOutputModel*/) {
		this.processedFeaturesModelName = processedFeaturesModelName;
		this.algorithmDescription = algorithmDescription;
		this.preprocessingAlgorithmDescription = preprocessingAlgorithmDescription;
		this.groundTruthSource = groundTruthSource;
		this.groundTruthSourceType = groundTruthSourceType;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.modelDatabase = AmusePreferences.get(KeysStringValue.MODEL_DATABASE);
	}

	/**
	 * Returns an array of TrainingConfigurations from the given data set
	 * @param trainingConfig Data set with configurations for one or more training tasks
	 * @return TrainingConfigurations
	 */
	public static TrainingConfiguration[] loadConfigurationsFromDataSet(DataSetAbstract trainingConfig) throws IOException {
		ArrayList<TrainingConfiguration> taskConfigurations = new ArrayList<TrainingConfiguration>();
		
   		// Proceed music file lists one by one
	    for(int i=0;i<trainingConfig.getValueCount();i++) {
			String currentProcessedFeaturesModelName = trainingConfig.getAttribute("ProcessedFeaturesDescription").getValueAt(i).toString();
			String currentAlgorithmDescription = trainingConfig.getAttribute("AlgorithmId").getValueAt(i).toString();
			String currentPreprocessingAlgorithmDescription = trainingConfig.getAttribute("PreprocessingAlgorithmId").getValueAt(i).toString();
			String currentGroundTruthSource = trainingConfig.getAttribute("GroundTruthSource").getValueAt(i).toString();
			String currentPathToOutputModel = trainingConfig.getAttribute("PathToOutputModel").getValueAt(i).toString();
			GroundTruthSourceType gtst;
			if(trainingConfig.getAttribute("GroundTruthSourceType").getValueAt(i).toString().equals(new String("CATEGORY_ID"))) {
				gtst = GroundTruthSourceType.CATEGORY_ID;
			} else if(trainingConfig.getAttribute("GroundTruthSourceType").getValueAt(i).toString().equals(new String("FILE_LIST"))) {
				gtst = GroundTruthSourceType.FILE_LIST;
			} else {
				gtst = GroundTruthSourceType.READY_INPUT;
			}
				
			// Create a training task
			TrainingConfiguration trConfig = new TrainingConfiguration(currentProcessedFeaturesModelName, currentAlgorithmDescription,
		    		currentPreprocessingAlgorithmDescription, new FileInput(currentGroundTruthSource),gtst);
			trConfig.setPathToOutputModel(currentPathToOutputModel);
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
		DataSetAbstract trainingConfig = new ArffDataSet(configurationFile);
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
}
