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

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.data.GroundTruthSourceType;
import amuse.data.InputFeatureType;
import amuse.data.ModelType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.datasets.TrainingConfigSet;
import amuse.data.io.DataInputInterface;
import amuse.data.io.DataSetInput;
import amuse.data.io.FileInput;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
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
	
	/** Description of input features (feature table or processing description, depending on InputFeaturesType) */
	private final String inputFeaturesDescription;
	
	/** List with raw input features (if the InputFeatureType RAW_FEATURES is used) */
	private final FeatureTable inputFeatureList;
	
	/** Type of input features for classfication */
	private final InputFeatureType inputFeatureType;
	
	/** Unit of window and step size */
	private final Unit unit;
	
	/** Size of classification window */
	private final Integer classificationWindowSize;
	
	/** Size of classification window steps */
	private final Integer classificationWindowStepSize;
	
	/** Id of classification algorithm from classificatorTable.arff 
	 * (optionally with parameters listed in brackets) */
	private final String algorithmDescription;
	
	/** Id and parameters of data preprocessing algorithm (e.g. outlier removal) */
	private final String preprocessingAlgorithmDescription;
	
	/** Ground truth source */
	private DataInputInterface groundTruthSource;
	
	/** Ground truth type for this configuration */
	private final GroundTruthSourceType groundTruthSourceType;
	
	private final List<Integer> attributesToPredict;
	private final List<Integer> attributesToIgnore;
	private final ModelType modelType;
	
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
	 * Number of values per extraction window.
	 * Used for raw features so that classification training methods can assemble 
	 * the feature vectors back to matrices.
	 */
	private int numberOfValuesPerWindow = -1;
	
	/**
	 * Standard constructor
	 * @param inputFeatures Description of the input features
	 * @param inputFeatureType type of the input features
	 * @param classificationWindowSize size of the classification windows
	 * @param classificationWindowStepSize step size of the classification windows
 	 * @param algorithmDescription ID of classification algorithm from classificationTrainerTable.arff
	 * @param groundTruthSource Source with ground truth for model training. Can be either
	 * - Id of the music category from $AMUSEHOME$/config/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready training input (prepared e.g. by a validator method)
	 * - Ready input (as EditableDataSet)
	 * @param groundTruthSourceType Describes the source type of ground truth
	 * @param attributesToPredict the categories of the category file of the annotationdatabase or the attributes of the ready input that should be predicted
	 * @param attributesToIgnore features of the processed feature files or the ready input that should not be used for the classification
	 * @param modelType the type of the classification model
	 * @param trainingDescription optional description of this experiment, that will be added to the name of the model
	 * @param pathToOutputModel optional path to where the model should be saved 
	 * (three possibilities are given above) 
	 */
	public TrainingConfiguration(String inputFeatures, InputFeatureType inputFeatureType, Unit unit, Integer classificationWindowSize, Integer classificationWindowStepSize, String algorithmDescription, String preprocessingAlgorithmDescription,
			DataInputInterface groundTruthSource, GroundTruthSourceType groundTruthSourceType, List<Integer> attributesToPredict, List<Integer> attributesToIgnore, ModelType modelType, String trainingDescription, String pathToOutputModel) {
		this.inputFeatureType = inputFeatureType;
		if(inputFeatureType == InputFeatureType.RAW_FEATURES) {
			this.inputFeatureList = new FeatureTable(new File(inputFeatures));
			List<Feature> features = inputFeatureList.getFeatures();
			String description = "";
			if(!features.isEmpty()) {
				description += features.get(0).getId();
			}
			for(int i = 1; i < features.size(); i++) {
				description += "_" + features.get(i).getId();
			}
			this.inputFeaturesDescription = description;
		} else {
			this.inputFeatureList = null;
			this.inputFeaturesDescription = inputFeatures;
		}
		this.unit = unit;
		this.classificationWindowSize = classificationWindowSize;
		this.classificationWindowStepSize = classificationWindowStepSize;
		this.algorithmDescription = algorithmDescription;
		this.preprocessingAlgorithmDescription = preprocessingAlgorithmDescription;
		this.groundTruthSource = groundTruthSource;
		this.groundTruthSourceType = groundTruthSourceType;
		this.attributesToPredict = attributesToPredict;
		this.attributesToIgnore = attributesToIgnore;
		this.modelType = modelType;
		this.trainingDescription = trainingDescription;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.modelDatabase = AmusePreferences.get(KeysStringValue.MODEL_DATABASE);
		this.pathToOutputModel = pathToOutputModel;
	}
	
	/**
	 * Constructor where the inputFeatures are given as a FeatureTable (featureType is RAW_FEATURES)
	 * 
	 * @param inputFeatures Description of the input features
	 * @param classificationWindowSize size of the classification windows
	 * @param classificationWindowStepSize step size of the classification windows
 	 * @param algorithmDescription ID of classification algorithm from classificationTrainerTable.arff
	 * @param groundTruthSource Source with ground truth for model training. Can be either
	 * - Id of the music category from $AMUSEHOME$/config/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready training input (prepared e.g. by a validator method)
	 * - Ready input (as EditableDataSet)
	 * @param groundTruthSourceType Describes the source type of ground truth
	 * @param attributesToPredict the categories of the category file of the annotationdatabase or the attributes of the ready input that should be predicted
	 * @param attributesToIgnore features of the processed feature files or the ready input that should not be used for the classification
	 * @param modelType the type of the classification model
	 * @param trainingDescription optional description of this experiment, that will be added to the name of the model
	 * @param pathToOutputModel optional path to where the model should be saved 
	 * (three possibilities are given above) 
	 */
	public TrainingConfiguration(FeatureTable inputFeatures, Unit unit, Integer classificationWindowSize, Integer classificationWindowStepSize, String algorithmDescription, String preprocessingAlgorithmDescription,
			DataInputInterface groundTruthSource, GroundTruthSourceType groundTruthSourceType, List<Integer> attributesToPredict, List<Integer> attributesToIgnore, ModelType modelType, String trainingDescription, String pathToOutputModel) {
		List<Feature> features = inputFeatures.getFeatures();
		String description = "";
		if(!features.isEmpty()) {
			description += features.get(0).getId();
		}
		for(int i = 1; i < features.size(); i++) {
			description += "_" + features.get(i).getId();
		}
		this.inputFeaturesDescription = description;
		this.inputFeatureList = inputFeatures;
		this.inputFeatureType = InputFeatureType.RAW_FEATURES;
		this.unit = unit;
		this.classificationWindowSize = classificationWindowSize;
		this.classificationWindowStepSize = classificationWindowStepSize;
		this.algorithmDescription = algorithmDescription;
		this.preprocessingAlgorithmDescription = preprocessingAlgorithmDescription;
		this.groundTruthSource = groundTruthSource;
		this.groundTruthSourceType = groundTruthSourceType;
		this.attributesToPredict = attributesToPredict;
		this.attributesToIgnore = attributesToIgnore;
		this.modelType = modelType;
		this.trainingDescription = trainingDescription;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.modelDatabase = AmusePreferences.get(KeysStringValue.MODEL_DATABASE);
		this.pathToOutputModel = pathToOutputModel;
	}

	/**
	 * Constructor that sets attributesToPredict, attributesToIgnore, modelType and outputPath to their default values.
	 * Currently only used by FitnessEvaluator
	 * 
	 * @param inputFeatures Description of the input features
	 * @param inputFeatureType type of the input features
	 * @param classificationWindowSize size of the classification windows
	 * @param classificationWindowStepSize step size of the classification windows
 	 * @param algorithmDescription ID of classification algorithm from classificationTrainerTable.arff
	 * @param groundTruthSource Source with ground truth for model training. Can be either
	 * - Id of the music category from $AMUSEHOME$/config/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready training input (prepared e.g. by a validator method)
	 * - Ready input (as EditableDataSet)
	 * @param groundTruthSourceType Describes the source type of ground truth
	 * @param pathToOutputModel optional path to where the model should be saved 
	 * (three possibilities are given above) 
	 */
	public TrainingConfiguration(String inputFeatures, InputFeatureType inputFeatureType, Unit unit, Integer classificationWindowSize, Integer classificationWindowStepSize, String algorithmDescription, String preprocessingAlgorithmDescription, DataSetInput groundTruthSource,
			GroundTruthSourceType groundTruthSourceType, String pathToOutputModel) {
		this.inputFeatureType = inputFeatureType;
		if(inputFeatureType == InputFeatureType.RAW_FEATURES) {
			this.inputFeatureList = new FeatureTable(new File(inputFeatures));
			List<Feature> features = inputFeatureList.getFeatures();
			String description = "";
			if(!features.isEmpty()) {
				description += features.get(0).getId();
			}
			for(int i = 1; i < features.size(); i++) {
				description += "_" + features.get(i).getId();
			}
			this.inputFeaturesDescription = description;
		} else {
			this.inputFeatureList = null;
			this.inputFeaturesDescription = inputFeatures;
		}
		this.unit = unit;
		this.classificationWindowSize = classificationWindowSize;
		this.classificationWindowStepSize = classificationWindowStepSize;
		this.algorithmDescription = algorithmDescription;
		this.preprocessingAlgorithmDescription = preprocessingAlgorithmDescription;
		this.groundTruthSource = groundTruthSource;
		this.groundTruthSourceType = groundTruthSourceType;
		this.attributesToPredict = new ArrayList<Integer>();
		this.attributesToIgnore = new ArrayList<Integer>();
		RelationshipType relationshipType = RelationshipType.BINARY;
		LabelType labelType = LabelType.SINGLELABEL;
		MethodType methodType = MethodType.SUPERVISED;
		ModelType tmpModelType = null;
		try {
			tmpModelType = new ModelType(relationshipType, labelType, methodType);
		} catch(Exception e) {}
		this.modelType = tmpModelType;
		this.trainingDescription = "";
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
			String currentInputFeatures = trainingConfig.getInputFeatureAttribute().getValueAt(i).toString();
			InputFeatureType currentInputFeatureType;
			if(trainingConfig.getInputFeatureTypeAttribute().getValueAt(i).toString().equals(new String("RAW_FEATURES"))) {
				currentInputFeatureType = InputFeatureType.RAW_FEATURES;
			} else {
				currentInputFeatureType = InputFeatureType.PROCESSED_FEATURES;
			}
			Unit currentUnit;
			if(trainingConfig.getUnitAttribute().getValueAt(i).toString().equals(new String("SAMPLES"))) {
				currentUnit = Unit.SAMPLES;
			} else {
				currentUnit = Unit.MILLISECONDS;
			}
			Integer currentClassificationWindowSize = trainingConfig.getClassificationWindowSizeAttribute().getValueAt(i).intValue();
			Integer currentClassificationWindowStepSize = trainingConfig.getClassificationWindowStepSizeAttribute().getValueAt(i).intValue();
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
			
			
			String attributesToPredictString = trainingConfig.getAttributesToPredictAttribute().getValueAt(i).toString();
			attributesToPredictString = attributesToPredictString.replaceAll("\\[", "").replaceAll("\\]", "");
			String[] attributesToPredictStringArray = attributesToPredictString.split("\\s*,\\s*");
			List<Integer> currentAttributesToPredict = new ArrayList<Integer>();
			try {
				for(String str : attributesToPredictStringArray) {
					if(!str.equals("")) {
						currentAttributesToPredict.add(Integer.parseInt(str));
					}
				}
			} catch(NumberFormatException e) {
				throw new IOException("The categories for training were not properly specified.");
			}
			
			String attributesToIgnoreString = trainingConfig.getAttributesToIgnoreAttribute().getValueAt(i).toString();
			attributesToIgnoreString = attributesToIgnoreString.replaceAll("\\[", "").replaceAll("\\]", "");
			String[] attributesToIgnoreStringArray = attributesToIgnoreString.split("\\s*,\\s*");
			List<Integer> currentAttributesToIgnore = new ArrayList<Integer>();
			try {
				for(String str : attributesToIgnoreStringArray) {
					if(!str.equals("")) {
						currentAttributesToIgnore.add(Integer.parseInt(str));
					}
				}
			} catch(NumberFormatException e) {
				AmuseLogger.write(TrainingConfiguration.class.getName(), Level.WARN,
						"The attributes to ignore were not properly specified. All features will be used for training.");
				currentAttributesToIgnore = new ArrayList<Integer>();
			}
			
			RelationshipType currentRelationshipType;
			if(trainingConfig.getRelationshipTypeAttribute().getValueAt(i).toString().equals("BINARY")) {
				currentRelationshipType = RelationshipType.BINARY;
			} else if(trainingConfig.getRelationshipTypeAttribute().getValueAt(i).toString().equals("CONTINUOUS")) {
				currentRelationshipType = RelationshipType.CONTINUOUS;
			} else {
				throw new IOException("The relationship type was not properly specified.");
			}
			
			LabelType currentLabelType;
			if(trainingConfig.getLabelTypeAttribute().getValueAt(i).toString().equals("MULTICLASS")) {
				currentLabelType = LabelType.MULTICLASS;
			} else if(trainingConfig.getLabelTypeAttribute().getValueAt(i).toString().equals("MULTILABEL")) {
				currentLabelType = LabelType.MULTILABEL;
			} else if(trainingConfig.getLabelTypeAttribute().getValueAt(i).toString().equals("SINGLELABEL")) {
				currentLabelType = LabelType.SINGLELABEL;
			} else {
				throw new IOException("The label type was not properly specified.");
			}
			
			MethodType currentMethodType;
			if(trainingConfig.getMethodTypeAttribute().getValueAt(i).toString().equals("SUPERVISED")) {
				currentMethodType = MethodType.SUPERVISED;
			} else if(trainingConfig.getMethodTypeAttribute().getValueAt(i).toString().equals("UNSUPERVISED")) {
				currentMethodType = MethodType.UNSUPERVISED;
			} else if(trainingConfig.getMethodTypeAttribute().getValueAt(i).toString().equals("REGRESSION")) {
				currentMethodType = MethodType.REGRESSION;
			} else {
				throw new IOException("The method type was not properly specified.");
			}
			
			ModelType currentModelType = new ModelType(currentRelationshipType, currentLabelType, currentMethodType);
			
			String currentTrainingDescription = trainingConfig.getTrainingDescriptionAttribute().getValueAt(i).toString();
			
			
				
			// Create a training task
			TrainingConfiguration trConfig = new TrainingConfiguration(currentInputFeatures, currentInputFeatureType, currentUnit, currentClassificationWindowSize, currentClassificationWindowStepSize, currentAlgorithmDescription,
		    		currentPreprocessingAlgorithmDescription, new FileInput(currentGroundTruthSource),gtst, currentAttributesToPredict, currentAttributesToIgnore, currentModelType, currentTrainingDescription, currentPathToOutputModel);
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
	public String getInputFeaturesDescription() {
		return inputFeaturesDescription;
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
	 * @return the attributesToPredict
	 */
	public List<Integer> getAttributesToPredict(){
		return attributesToPredict;
	}
	
	/**
	 * @return the attributesToIgnore
	 */
	public List<Integer> getAttributesToIgnore(){
		return attributesToIgnore;
	}
	
	/**
	 * @return the trainingDescription
	 */
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
		TrainingConfiguration conf;
		if(inputFeatureType == InputFeatureType.PROCESSED_FEATURES) {
				conf = new TrainingConfiguration(inputFeaturesDescription, inputFeatureType, unit, classificationWindowSize, classificationWindowStepSize, algorithmDescription, preprocessingAlgorithmDescription, groundTruthSource, groundTruthSourceType, attributesToPredict, attributesToIgnore, modelType, trainingDescription, pathToOutputModel);
		} else {
			conf = new TrainingConfiguration(inputFeatureList, unit, classificationWindowSize, classificationWindowStepSize, algorithmDescription, preprocessingAlgorithmDescription, groundTruthSource, groundTruthSourceType, attributesToPredict, attributesToIgnore, modelType, trainingDescription, pathToOutputModel);
		}
		return conf;
	}

	/**
	 * @return the relatioshipType
	 */
	public RelationshipType getRelationshipType() {
		return modelType.getRelationshipType();
	}

	/**
	 * @return the labelType
	 */
	public LabelType getLabelType() {
		return modelType.getLabelType();
	}

	/**
	 * @return the methodType
	 */
	public MethodType getMethodType() {
		return modelType.getMethodType();
	}
	
	/**
	 * @return the modelType
	 */
	public ModelType getModelType() {
		return modelType;
	}

	/**
	 * @return the inputFeatureType
	 */
	public InputFeatureType getInputFeatureType() {
		return inputFeatureType;
	}

	/**
	 * @return the classificationWindowSize
	 */
	public Integer getClassificationWindowSize() {
		return classificationWindowSize;
	}

	/**
	 * @return the classificationWindowStepSize
	 */
	public Integer getClassificationWindowStepSize() {
		return classificationWindowStepSize;
	}
	
	/**
	 * @return the inputFeatureList
	 */
	public FeatureTable getInputFeatureList() {
		return inputFeatureList;
	}
	
	/**
	 * @param windowSize the numberOfValuesPerWindow
	 */
	public void setNumberOfValuesPerWindow(int windowSize) {
		this.numberOfValuesPerWindow = windowSize;
	}
	
	 /**
	  * @return the numberOfValuesPerWindow
	  */
	public int getNumberOfValuesPerWindow() {
		return this.numberOfValuesPerWindow;
	}

	/**
	 * @return unit of classification window and step size
	 */
	public Unit getUnit() {
		return unit;
	}
}
