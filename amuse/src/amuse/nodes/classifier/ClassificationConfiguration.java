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
package amuse.nodes.classifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.data.InputFeatureType;
import amuse.data.ModelType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.datasets.ClassifierConfigSet;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataInputInterface;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.FileInput;
import amuse.data.io.FileListInput;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Describes the parameters for a classification task 
 * 
 * @author Igor Vatolkin
 * @version $Id: ClassificationConfiguration.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class ClassificationConfiguration extends TaskConfiguration {

	/** For Serializable interface */
	private static final long serialVersionUID = -1278425528498549759L;
	
	/** Input source type for this configuration */
	private final InputSourceType inputSourceType;
	
	/** Input attributes that are ignored. Is only used if the input source type is READY_INPUT */
	private final List<Integer> attributesToIgnore;
	
	/** Input to classify */
	private DataInputInterface inputToClassify;
	
	/** Defines the input source type. Can be either
	 * - List with music files to classify
	 * - Path to the ready classification input (prepared e.g. by a validator method) */
	public enum InputSourceType {FILE_LIST, CATEGORY_ID, READY_INPUT};
	
	/** Description of the processed features model */
	private final String inputFeaturesDescription;
	
	/** Type of input features for classfication */
	private final InputFeatureType inputFeatureType;
	
	/** Unit of window and step size */
	private final Unit unit;
	
	/** Size of classification window */
	private final Integer classificationWindowSize;
	
	/** Size of classification window step size */
	private final Integer classificationWindowStepSize;
	
	/** Id of classification algorithm from classifierTable.arff 
	 * (optionally with parameters listed in brackets) */
	private final String algorithmDescription;
	
	/** Id of the groundtrhuth category. Is not used if the concrete model path is known.*/
	private final int groundTruthCategoryId;
	
	/** Categories of the groundtruth that were used for training. Is not used if the concrete model path is known.*/
	private final List<Integer> attributesToPredict;
	
	/** Type of the model that is used for classification*/
	private final ModelType modelType;
	
	/** Flag if track relationship grade should be averaged over all classificatoin windows (="1") */
	private final Integer mergeTrackResults;
	
	/** Destination for classification output */
	private final String classificationOutput;
	
	/** Alternative path for classification model(s) (e.g. an optimization task may train
	 * different models and compare their performance; here the models can't be loaded from
	 * Amuse model database!) */
	private String pathToInputModel;
	
	/** Training Description to differentiate between different models */
	private String trainingDescription;
	
	/** Folder to load the processed features from (default: Amuse processed feature database) */
	private String processedFeatureDatabase;
	
	/** List with raw input features (if the InputFeatureType RAW_FEATURES is used) */
	private final FeatureTable inputFeatureList;
	
	/**
	 * Number of values per extraction window.
	 * Used for raw features so that classification training methods can assemble 
	 * the feature vectors back to matrices.
	 */
	private int numberOfValuesPerWindow = -1;
	

	/**
	 * Standard constructor
	 * @param inputToClassify Input to classify
	 * @param inputSourceType Defines the input source type
	 * @param attributesToIgnore features of the processed feature files or the ready input that should not be used for the classification
	 * @param inputFeatures Description of the input features
	 * @param inputFeatureType type of the input features
	 * @param classificationWindowSize size of the classification windows
	 * @param classificationWindowStepSize step size of the classification windows
	 * @param algorithmDescription Id and parameters of the classification algorithm from classifierTable.arff
	 * @param attributesToPredict the categories of the category file of the annotation database
	 * @param classificationType is the classification unsupervised, binary, multilabel or multiclass?
	 * @param fuzzy should the classification be fuzzy?
	 * @param mergeTrackResults Flag if track relationship grade should be averaged over all classificatoin windows (="1")
	 * @param classificationOutput Destination for classification output
	 */
	public ClassificationConfiguration(
			DataInputInterface inputToClassify,
			InputSourceType inputSourceType,
			List <Integer> attributesToIgnore,
			String inputFeatures,
			InputFeatureType inputFeatureType,
			Unit unit,
			Integer classificationWindowSize,
			Integer classificationWindowStepSize,
			String algorithmDescription,
			List<Integer> attributesToPredict,
			ModelType modelType,
			Integer mergeTrackResults,
			String classificationOutput) {
		this.inputToClassify = inputToClassify;
		this.inputSourceType = inputSourceType;
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
		this.attributesToPredict = attributesToPredict;
		this.attributesToIgnore = attributesToIgnore;
		this.modelType = modelType;
		this.mergeTrackResults = mergeTrackResults;
		this.classificationOutput = classificationOutput;
		this.groundTruthCategoryId = -1;
	}
	
	/**
	 * Alternative constructor if the track list to classify is loaded by the category id or path to filelist
	 * @param inputSourceType Defines the input source type
	 * @param attributesToIgnore features of the processed feature files or the ready input that should not be used for the classification
	 * @param inputSource Input for classification
	 * @param inputFeatures Description of the input features
	 * @param inputFeatureType type of the input features
	 * @param classificationWindowSize size of the classification windows
	 * @param classificationWindowStepSize step size of the classification windows
	 * @param algorithmDescription Id and parameters of the classification algorithm from classifierTable.arff
	 * @param groundTruthSource Id of the music category
	 * @param attributesToPredict the categories of the category file of the annotation database
	 * @param classificationType is the classification unsupervised, binary, multilabel or multiclass?
	 * @param fuzzy should the classification be fuzzy?
	 * @param mergeTrackResults Flag if track relationship grade should be averaged over all classificatoin windows (="1")
	 * @param classificationOutput Destination for classification output
	 */
	public ClassificationConfiguration(
			InputSourceType inputSourceType,
			String inputSource,
			List <Integer> attributesToIgnore,
			String inputFeatures,
			InputFeatureType inputFeatureType,
			Unit unit,
			Integer classificationWindowSize,
			Integer classificationWindowStepSize,
			String algorithmDescription,
			int groundTruthSource,
			List<Integer> attributesToPredict,
			ModelType modelType,
			Integer mergeTrackResults,
			String classificationOutput,
			String pathToInputModel,
			String trainingDescription) throws IOException{
		List<File> input;
		List<Integer> ids = null;
		
		if(inputSourceType.equals(InputSourceType.CATEGORY_ID)) {
			this.inputToClassify = new FileInput(inputSource);
		} else if(inputSourceType.equals(InputSourceType.FILE_LIST)) {
			DataSetAbstract inputFileSet; 
			try {
				inputFileSet = new ArffDataSet(new File(inputSource));
			} catch(IOException e) {
				throw new RuntimeException("Could not create ClassificationConfiguration: " + e.getMessage());
			}
			ids = new ArrayList<Integer>(inputFileSet.getValueCount());
			input = new ArrayList<File>(inputFileSet.getValueCount());
			for(int j=0;j<inputFileSet.getValueCount();j++) {
				ids.add(new Double(inputFileSet.getAttribute("Id").getValueAt(j).toString()).intValue());
				input.add(new File(inputFileSet.getAttribute("Path").getValueAt(j).toString()));
			}
			this.inputToClassify = new FileListInput(input,ids);
			
		} else {
			input = new ArrayList<File>(1);
			input.add(new File(inputSource));
			this.inputToClassify = new FileListInput(input,ids);
		}
		this.inputSourceType = inputSourceType;
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
		this.groundTruthCategoryId = groundTruthSource;
		this.attributesToPredict = attributesToPredict;
		this.attributesToIgnore = attributesToIgnore;
		this.modelType = modelType;
		this.mergeTrackResults = mergeTrackResults;
		this.classificationOutput = classificationOutput;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.pathToInputModel = pathToInputModel;
		this.trainingDescription = trainingDescription;
	}

	/**
	 * Alternative constructor if the track list to classify is loaded by the category id or path to filelist
	 * input features are given as FeatureTalbe (only used for raw input features)
	 * @param inputSourceType Defines the input source type
	 * @param attributesToIgnore features of the processed feature files or the ready input that should not be used for the classification
	 * @param inputSource Input for classification
	 * @param inputFeatures Description of the input features
	 * @param inputFeatureType type of the input features
	 * @param classificationWindowSize size of the classification windows
	 * @param classificationWindowStepSize step size of the classification windows
	 * @param algorithmDescription Id and parameters of the classification algorithm from classifierTable.arff
	 * @param groundTruthSource Id of the music category
	 * @param attributesToPredict the categories of the category file of the annotation database
	 * @param classificationType is the classification unsupervised, binary, multilabel or multiclass?
	 * @param fuzzy should the classification be fuzzy?
	 * @param mergeTrackResults Flag if track relationship grade should be averaged over all classification windows (="1")
	 * @param classificationOutput Destination for classification output
	 */
	public ClassificationConfiguration(
			InputSourceType inputSourceType,
			String inputSource,
			List <Integer> attributesToIgnore,
			FeatureTable inputFeatures,
			Unit unit,
			Integer classificationWindowSize,
			Integer classificationWindowStepSize,
			String algorithmDescription,
			int groundTruthSource,
			List<Integer> attributesToPredict,
			ModelType modelType,
			Integer mergeTrackResults,
			String classificationOutput,
			String pathToInputModel,
			String trainingDescription) throws IOException{
		List<File> input;
		List<Integer> ids = null;
		
		if(inputSourceType.equals(InputSourceType.CATEGORY_ID)) {
			this.inputToClassify = new FileInput(inputSource);
		} else if(inputSourceType.equals(InputSourceType.FILE_LIST)) {
			DataSetAbstract inputFileSet; 
			try {
				inputFileSet = new ArffDataSet(new File(inputSource));
			} catch(IOException e) {
				throw new RuntimeException("Could not create ClassificationConfiguration: " + e.getMessage());
			}
			ids = new ArrayList<Integer>(inputFileSet.getValueCount());
			input = new ArrayList<File>(inputFileSet.getValueCount());
			for(int j=0;j<inputFileSet.getValueCount();j++) {
				ids.add(new Double(inputFileSet.getAttribute("Id").getValueAt(j).toString()).intValue());
				input.add(new File(inputFileSet.getAttribute("Path").getValueAt(j).toString()));
			}
			this.inputToClassify = new FileListInput(input,ids);
			
		} else {
			input = new ArrayList<File>(1);
			input.add(new File(inputSource));
			this.inputToClassify = new FileListInput(input,ids);
		}
		this.inputSourceType = inputSourceType;
		this.inputFeatureType = InputFeatureType.RAW_FEATURES;
		this.inputFeatureList = inputFeatures;
		List<Feature> features = inputFeatures.getFeatures();
		String description = "";
		if(!features.isEmpty()) {
			description += features.get(0).getId();
		}
		for(int i = 1; i < features.size(); i++) {
			description += "_" + features.get(i).getId();
		}
		this.inputFeaturesDescription = description;
		this.unit = unit;
		this.classificationWindowSize = classificationWindowSize;
		this.classificationWindowStepSize = classificationWindowStepSize;
		this.algorithmDescription = algorithmDescription;
		this.groundTruthCategoryId = groundTruthSource;
		this.attributesToPredict = attributesToPredict;
		this.attributesToIgnore = attributesToIgnore;
		this.modelType = modelType;
		this.mergeTrackResults = mergeTrackResults;
		this.classificationOutput = classificationOutput;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.pathToInputModel = pathToInputModel;
		this.trainingDescription = trainingDescription;
	}

	/**
	 * Returns an array of ClassificationConfigurations from the given data set
	 * @param classifierConfig Data set with configurations for one or more processing tasks
	 * @return ClassificationConfigurations
	 */
	public static ClassificationConfiguration[] loadConfigurationsFromDataSet(ClassifierConfigSet classifierConfig) throws IOException {
		ArrayList<ClassificationConfiguration> taskConfigurations = new ArrayList<ClassificationConfiguration>();
		
   		// Proceed music file lists one by one
	    for(int i=0;i<classifierConfig.getValueCount();i++) {
			String currentInputSource = classifierConfig.getInputSourceAttribute().getValueAt(i).toString();
			String currentInputFeatureDescription = classifierConfig.getInputFeaturesAttribute().getValueAt(i).toString();
			InputFeatureType currentInputFeatureType;
			if(classifierConfig.getInputFeatureTypeAttribute().getValueAt(i).toString().equals(new String("RAW_FEATURES"))){
				currentInputFeatureType = InputFeatureType.RAW_FEATURES;
			} else {
				currentInputFeatureType = InputFeatureType.PROCESSED_FEATURES;
			}
			Unit currentUnit;
			if(classifierConfig.getUnitAttribute().getValueAt(i).toString().equals(new String("SAMPLES"))) {
				currentUnit = Unit.SAMPLES;
			} else {
				currentUnit = Unit.MILLISECONDS;
			}
			Integer currentClassificationWindowSize = new Double(classifierConfig.getClassificationWindowSizeAttribute().getValueAt(i)).intValue();
			Integer currentClassificationWindowStepSize = new Double(classifierConfig.getClassificationWindowStepSizeAttribute().getValueAt(i)).intValue();
			String currentAlgorithmDescription = classifierConfig.getClassificationAlgorithmIdAttribute().getValueAt(i).toString();
			int currentGroundTruthSource = classifierConfig.getGroundTruthSourceAttribute().getValueAt(i).intValue();
			String attributesToPredictString = classifierConfig.getAttributesToPredictAttribute().getValueAt(i).toString();
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
				throw new IOException("The attributes to classify were not properly specified.");
			}
			
			String attributesToIgnoreString = classifierConfig.getAttributesToIgnoreAttribute().getValueAt(i).toString();
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
				AmuseLogger.write(ClassificationConfiguration.class.getName(), Level.WARN,
						"The attributes to ignore were not properly specified. All features will be used for classification.");
				currentAttributesToIgnore = new ArrayList<Integer>();
			}
			
			RelationshipType currentRelationshipType;
			if(classifierConfig.getRelationshipTypeAttribute().getValueAt(i).toString().equals("BINARY")) {
				currentRelationshipType = RelationshipType.BINARY;
			} else if(classifierConfig.getRelationshipTypeAttribute().getValueAt(i).toString().equals("CONTINUOUS")) {
				currentRelationshipType = RelationshipType.CONTINUOUS;
			} else {
				throw new IOException("The relationship type was not properly specified.");
			}
			
			LabelType currentLabelType;
			if(classifierConfig.getLabelTypeAttribute().getValueAt(i).toString().equals("MULTICLASS")) {
				currentLabelType = LabelType.MULTICLASS;
			} else if(classifierConfig.getLabelTypeAttribute().getValueAt(i).toString().equals("MULTILABEL")) {
				currentLabelType = LabelType.MULTILABEL;
			} else if(classifierConfig.getLabelTypeAttribute().getValueAt(i).toString().equals("SINGLELABEL")) {
				currentLabelType = LabelType.SINGLELABEL;
			} else {
				throw new IOException("The label type was not properly specified.");
			}
			
			MethodType currentMethodType;
			if(classifierConfig.getMethodTypeAttribute().getValueAt(i).toString().equals("SUPERVISED")) {
				currentMethodType = MethodType.SUPERVISED;
			} else if(classifierConfig.getMethodTypeAttribute().getValueAt(i).toString().equals("UNSUPERVISED")) {
				currentMethodType = MethodType.UNSUPERVISED;
			} else if(classifierConfig.getMethodTypeAttribute().getValueAt(i).toString().equals("REGRESSION")) {
				currentMethodType = MethodType.REGRESSION;
			} else {
				throw new IOException("The method type was not properly specified.");
			}
			
			ModelType currentModelType = new ModelType(currentRelationshipType, currentLabelType, currentMethodType);
			
			String currentPathToInputModel = classifierConfig.getPathToInputModelAttribute().getValueAt(i);
			
			
			Integer currentMergeTrackResults = (new Double(classifierConfig.getMergeTrackResultsAttribute().getValueAt(i).toString())).intValue();
			String currentOutputResult = classifierConfig.getOutputResultAttribute().getValueAt(i).toString();
			InputSourceType ist;
			if(classifierConfig.getInputSourceTypeAttribute().getValueAt(i).toString().equals(new String("FILE_LIST"))) {
				ist = InputSourceType.FILE_LIST;
			} else if(classifierConfig.getInputSourceTypeAttribute().getValueAt(i).toString().equals(new String("CATEGORY_ID"))){
				ist = InputSourceType.CATEGORY_ID;
			} else {
				ist = InputSourceType.READY_INPUT;
			}
			
			String currentTrainingDescription = classifierConfig.getTrainingDescriptionAttribute().getValueAt(i);
			
			// Create a classification task
		    taskConfigurations.add(new ClassificationConfiguration(ist, currentInputSource, currentAttributesToIgnore, currentInputFeatureDescription, currentInputFeatureType, currentUnit, currentClassificationWindowSize, currentClassificationWindowStepSize,
		    		currentAlgorithmDescription, currentGroundTruthSource, currentAttributesToPredict, currentModelType, currentMergeTrackResults, currentOutputResult, currentPathToInputModel, currentTrainingDescription));
			AmuseLogger.write(ClassificationConfiguration.class.getName(), Level.DEBUG, "Classification task loaded");
		}
		
		// Create an array
	    ClassificationConfiguration[] tasks = new ClassificationConfiguration[taskConfigurations.size()];
		for(int i=0;i<taskConfigurations.size();i++) {
	    	tasks[i] = taskConfigurations.get(i);
	    }
		return tasks;
	}
	
	/**
	 * Returns an array of ClassificationConfigurations from the given ARFF file
	 * @param configurationFile ARFF file with configurations for one or more processing tasks
	 * @return ClassificationConfigurations
	 * @throws IOException 
	 */
	public static ClassificationConfiguration[] loadConfigurationsFromFile(File configurationFile) throws IOException {
		ClassifierConfigSet classifierConfig = new ClassifierConfigSet(configurationFile);
    	return loadConfigurationsFromDataSet(classifierConfig);
	}
	
	/**
	 * @return the inputSourceType
	 */
	public InputSourceType getInputSourceType() {
		return inputSourceType;
	}
	
	/**
	 * @return the processedFeaturesModelName
	 */
	public String getInputFeatures() {
		return inputFeaturesDescription;
	}

	/**
	 * @return the algorithmDescription
	 */
	public String getAlgorithmDescription() {
		return algorithmDescription;
	}
	
	/**
	 * @return the groundTruthCategoryId
	 */
	public int getGroundTruthCategoryId() {
		return groundTruthCategoryId;
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
	 * @return the relationshipType
	 */
	public RelationshipType getRelationshipType() {
		return modelType.getRelationshipType();
	}
	
	/**
	 * @return return the labelType
	 */
	public LabelType getLabelType() {
		return modelType.getLabelType();
	}
	
	/**
	 * @return return the methodType
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
	 * @return the mergeTrackResults
	 */
	public Integer getMergeTrackResults() {
		return mergeTrackResults;
	}

	/**
	 * @return the classificationOutput
	 */
	public String getClassificationOutput() {
		return classificationOutput;
	}

	/**
	 * @return the pathToInputModel
	 */
	public String getPathToInputModel() {
		return pathToInputModel;
	}
	
	/**
	 * @return the trainingDescription
	 */
	public String getTrainingDescription() {
		return trainingDescription;
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

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getType()
	 */
	public String getType() {
		return "Classification";
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getDescription()
	 */
	public String getDescription() {
        return new String("Ground Truth Source: " + groundTruthCategoryId + " Output: " + classificationOutput);
	}

	/**
	 * @return the inputToClassify
	 */
	public DataInputInterface getInputToClassify() {
		return inputToClassify;
	}

	/**
	 * @param inputToClassify the inputToClassify to set
	 */
	public void setInputToClassify(DataInputInterface inputToClassify) {
		this.inputToClassify = inputToClassify;
	}

	/**
	 * @param pathToInputModel the pathToInputModel to set
	 */
	public void setPathToInputModel(String pathToInputModel) {
		this.pathToInputModel = pathToInputModel;
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
	public Integer getClassificationWindowStepSize()	{
		return classificationWindowStepSize;
	}

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
	 * @return the unit of classification window and step size
	 */
	public Unit getUnit() {
		return unit;
	}
}
