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
package amuse.nodes.validator;

import amuse.data.ModelType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.data.GroundTruthSourceType;
import amuse.data.InputFeatureType;
import amuse.data.Measure;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import amuse.data.MeasureTable;
import amuse.data.datasets.ValidatorConfigSet;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataInputInterface;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetInput;
import amuse.data.io.FileInput;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.validator.interfaces.ValidationMeasure;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Describes the parameters for a classification validation task 
 * 
 * @author Igor Vatolkin
 * @version $Id: ValidationConfiguration.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class ValidationConfiguration extends TaskConfiguration {

	/** For Serializable interface */
	private static final long serialVersionUID = 8680312567847735075L;
	
	/** ID of validation method (optionally with parameters listed in brackets) */
	private final String validationAlgorithmDescription;
	
	/** Measure list, e.g. containing accuracy and precision */
	private final MeasureTable measures;
	
	/** Description of input features (feature table or processing description, depending on InputFeaturesType) */
	private final String inputFeaturesDescription;
	
	/** Type of input features for classfication */
	private final InputFeatureType inputFeatureType;
	
	/** Size of classification window in milliseconds */
	private final Integer classificationWindowSize;
	
	/** Size of classification window overlap in milliseconds */
	private final Integer classificationWindowOverlap;
	
	/** Input to validate */
	private DataInputInterface inputToValidate;
	
	/** Ground truth type for this configuration */
	private final GroundTruthSourceType groundTruthSourceType;
	
	/** The attributes of the ready input or the categories of the category file that are to be classified */
	private final List<Integer> attributesToPredict;
	
	/** The attributes of the ready input or the processed features that are to be ignored*/
	private final List<Integer> attributesToIgnore;
	
	/** The type of the model that is used for classification and training*/
	private final ModelType modelType;
	
	
	/** ID of classification algorithm from classifierTable.arff 
	 * (optionally with parameters listed in brackets) */
	private final String classificationAlgorithmDescription;
	
	/** Folder to load the processed features from (default: Amuse processed feature database) */
	private String processedFeatureDatabase;
	
	/** Folder to load the classification model(s) from (default: Amuse model database) */
	private String modelDatabase;
	
	/** Folder to store the classification validation results (default: Amuse measure database) */
	private String measureDatabase;
	
	/** Path to were the validation result should be saved. If it is empty or -1, the path is calculated automatically*/
	private String outputPath;
	
	/** Calculated measures are stored here after the corresponding validation task has been successfully applied */
	private ArrayList<ValidationMeasure> calculatedMeasures;
	
	/** List with raw input features (if the InputFeatureType RAW_FEATURES is used) */
	private final FeatureTable inputFeatureList;
	
	/** Number of feature dimensions per time window. Is only used with raw features */
	private Integer numberOfFeatureDimensions;
	
	/**
	 * Standard constructor
	 * @param validationAlgorithmDescription ID and parameters of validation method
	 * @param measures Measure list, e.g. containing accuracy and precision
	 * @param inputFeatures Description of methods used for feature processing
	 * @param inputFeatureType type of the input features
	 * @param classificationWindowSize size of the classification windows
	 * @param classificaitonWindowOverlap overlap of the classificaiton windows
	 * @param classificationAlgorithmDescription ID of classification algorithm from classifierTable.arff
 	 * @param groundTruthSource Source with input to validate. Can be either
	 * - Id of the music category from $AMUSEHOME$/config/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready labeled input 
	 * - Ready input (as EditableDataSet)
	 * @param groundTruthSourceType Describes the source type of ground truth 
	 * @param attributesToPredict attributes of the readyInput or categories of the category file that are to be predicted
	 * @param attributesToIgnore attribute of the readyInput or the processed features that are to be ignored
	 * @param modelType the type of the classification model
	 * @param outPutPath path where the results are saved, set to -1 for the path to be automatically calculated
	 */
	public ValidationConfiguration(String validationAlgorithmDescription,
			MeasureTable measures, 
			String inputFeatures,
			InputFeatureType inputFeatureType,
			Integer classificationWindowSize,
			Integer classificationWindowOverlap,
			String classificationAlgorithmDescription,
			DataInputInterface inputToValidate,
			GroundTruthSourceType groundTruthSourceType,
			List<Integer> attributesToPredict,
			List<Integer> attributesToIgnore,
			ModelType modelType,
			String outputPath) {
		this.validationAlgorithmDescription = validationAlgorithmDescription;
		this.measures = measures;
		this.inputFeaturesDescription = inputFeatures;
		this.inputFeatureType = inputFeatureType;
		if(inputFeatureType == InputFeatureType.RAW_FEATURES) {
			this.inputFeatureList = new FeatureTable(new File(inputFeatures));
		} else {
			this.inputFeatureList = null;
		}
		this.classificationWindowSize = classificationWindowSize;
		this.classificationWindowOverlap = classificationWindowOverlap;
		this.classificationAlgorithmDescription = classificationAlgorithmDescription;
		this.inputToValidate = inputToValidate;
		this.groundTruthSourceType = groundTruthSourceType;
		this.attributesToPredict = attributesToPredict;
		this.attributesToIgnore = attributesToIgnore;
		this.modelType = modelType;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.modelDatabase = AmusePreferences.get(KeysStringValue.MODEL_DATABASE);
		this.measureDatabase = AmusePreferences.get(KeysStringValue.MEASURE_DATABASE);
		this.calculatedMeasures = new ArrayList<ValidationMeasure>();
		this.outputPath = outputPath;
	}
	
	/**
	 * Constructor where the input features are given as a FeatureTable (only used for raw input features)
	 * @param validationAlgorithmDescription ID and parameters of validation method
	 * @param measures Measure list, e.g. containing accuracy and precision
	 * @param inputFeatures Description of methods used for feature processing
	 * @param inputFeatureType type of the input features
	 * @param classificationWindowSize size of the classification windows
	 * @param classificaitonWindowOverlap overlap of the classificaiton windows
	 * @param classificationAlgorithmDescription ID of classification algorithm from classifierTable.arff
 	 * @param groundTruthSource Source with input to validate. Can be either
	 * - Id of the music category from $AMUSEHOME$/config/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready labeled input 
	 * - Ready input (as EditableDataSet)
	 * @param groundTruthSourceType Describes the source type of ground truth 
	 * @param attributesToPredict attributes of the readyInput or categories of the category file that are to be predicted
	 * @param attributesToIgnore attribute of the readyInput or the processed features that are to be ignored
	 * @param modelType the type of the classification model
	 * @param outPutPath path where the results are saved, set to -1 for the path to be automatically calculated
	 */
	public ValidationConfiguration(String validationAlgorithmDescription,
			MeasureTable measures, 
			FeatureTable inputFeatures,
			Integer classificationWindowSize,
			Integer classificationWindowOverlap,
			String classificationAlgorithmDescription,
			DataInputInterface inputToValidate,
			GroundTruthSourceType groundTruthSourceType,
			List<Integer> attributesToPredict,
			List<Integer> attributesToIgnore,
			ModelType modelType,
			String outputPath) {
		this.validationAlgorithmDescription = validationAlgorithmDescription;
		this.measures = measures;
		this.inputFeatureList = inputFeatures;
		this.inputFeatureType = InputFeatureType.RAW_FEATURES;
		List<Feature> features = inputFeatures.getFeatures();
		String description = "";
		if(!features.isEmpty()) {
			description += features.get(0).getId();
		}
		for(int i = 1; i < features.size(); i++) {
			description += "_" + features.get(i).getId();
		}
		this.inputFeaturesDescription = description;
		this.classificationWindowSize = classificationWindowSize;
		this.classificationWindowOverlap = classificationWindowOverlap;
		this.classificationAlgorithmDescription = classificationAlgorithmDescription;
		this.inputToValidate = inputToValidate;
		this.groundTruthSourceType = groundTruthSourceType;
		this.attributesToPredict = attributesToPredict;
		this.attributesToIgnore = attributesToIgnore;
		this.modelType = modelType;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.modelDatabase = AmusePreferences.get(KeysStringValue.MODEL_DATABASE);
		this.measureDatabase = AmusePreferences.get(KeysStringValue.MEASURE_DATABASE);
		this.calculatedMeasures = new ArrayList<ValidationMeasure>();
		this.outputPath = outputPath;
	}
	
	/**
	 * Constructor that sets attributesToPredict, attributesToIgnore, modelType and outputPath to their default values.
	 * Currently only used by FitnessEvaluator
	 * 
	 * @param validationAlgorithmDescription ID and parameters of validation method
	 * @param measures Measure list, e.g. containing accuracy and precision
	 * @param inputFeatures Description of methods used for feature processing
	 * @param inputFeatureType type of the input features
	 * @param classificationWindowSize size of the classification windows
	 * @param classificaitonWindowOverlap overlap of the classificaiton windows
	 * @param classificationAlgorithmDescription ID of classification algorithm from classifierTable.arff
 	 * @param groundTruthSource Source with input to validate. Can be either
	 * - Id of the music category from $AMUSEHOME$/config/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready labeled input 
	 * - Ready input (as EditableDataSet)
	 */
	public ValidationConfiguration(String validationAlgorithmDescription,
			MeasureTable measures,
			String inputFeatures,
			InputFeatureType inputFeatureType,
			Integer classificationWindowSize,
			Integer classificationWindowOverlap,
			String classificationAlgorithmDescription,
			DataSetInput inputToValidate,
			GroundTruthSourceType groundTruthSourceType) {
		this.validationAlgorithmDescription = validationAlgorithmDescription;
		this.measures = measures;
		this.inputFeaturesDescription = inputFeatures;
		this.inputFeatureType = inputFeatureType;
		if(inputFeatureType == InputFeatureType.RAW_FEATURES) {
			this.inputFeatureList = new FeatureTable(new File(inputFeatures));
		} else {
			this.inputFeatureList = null;
		}
		this.classificationWindowSize = classificationWindowSize;
		this.classificationWindowOverlap = classificationWindowOverlap;
		this.classificationAlgorithmDescription = classificationAlgorithmDescription;
		this.inputToValidate = inputToValidate;
		this.groundTruthSourceType = groundTruthSourceType;
		List<Integer> attributesToPredict = new ArrayList<Integer>();
		this.attributesToPredict = attributesToPredict;
		List<Integer> attributesToIgnore = new ArrayList<Integer>();
		this.attributesToIgnore = attributesToIgnore;
		RelationshipType relationshipType = RelationshipType.BINARY;
		LabelType labelType = LabelType.SINGLELABEL;
		MethodType methodType = MethodType.SUPERVISED;
		ModelType tmpModelType = null;
		try {
			tmpModelType = new ModelType(relationshipType, labelType, methodType);
		} catch(Exception e) {}
		this.modelType = tmpModelType;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.modelDatabase = AmusePreferences.get(KeysStringValue.MODEL_DATABASE);
		this.measureDatabase = AmusePreferences.get(KeysStringValue.MEASURE_DATABASE);
		this.calculatedMeasures = new ArrayList<ValidationMeasure>();
		this.outputPath = "-1";
	}

	/**
	 * Returns an array of ValidationConfigurations from the given data set
	 * @param validatorConfig Data set with configurations for one or more processing tasks
	 * @return ValidationConfigurations
	 */
	public static ValidationConfiguration[] loadConfigurationsFromDataSet(ValidatorConfigSet validatorConfig) throws IOException {
		ArrayList<ValidationConfiguration> taskConfigurations = new ArrayList<ValidationConfiguration>();
		
   		// Proceed music file lists one by one
	    for(int i=0;i<validatorConfig.getValueCount();i++) {
			String currentValidationMethodId = validatorConfig.getValidationMethodIdAttribute().getValueAt(i).toString();
			String currentMeasureList = validatorConfig.getMeasureListAttribute().getValueAt(i).toString();
			String currentInputFeatureDescription = validatorConfig.getInputFeaturesAttribute().getValueAt(i).toString();
			InputFeatureType currentInputFeatureType;
			if(validatorConfig.getInputFeatureTypeAttribute().getValueAt(i).toString().equals(new String("RAW_FEATURES"))) {
				currentInputFeatureType = InputFeatureType.RAW_FEATURES;
			} else {
				currentInputFeatureType = InputFeatureType.PROCESSED_FEATURES;
			}
			Integer currentClassificationWindowSize = validatorConfig.getClassificationWindowSizeAttribute().getValueAt(i).intValue();
			Integer currentClassificationWindowOverlap = validatorConfig.getClassificationWindowOverlapAttribute().getValueAt(i).intValue();
			String currentClassificationAlgorithmDescription = validatorConfig.getClassificationAlgorithmIdAttribute().getValueAt(i).toString();
			String currentInputToValidate = validatorConfig.getInputToValidateAttribute().getValueAt(i).toString();
			GroundTruthSourceType gtst;
			if(validatorConfig.getGroundTruthSourceAttribute().getValueAt(i).toString().equals(new String("CATEGORY_ID"))) {
				gtst = GroundTruthSourceType.CATEGORY_ID;
			} else if (validatorConfig.getGroundTruthSourceAttribute().getValueAt(i).toString().equals(new String("FILE_LIST"))){
				gtst = GroundTruthSourceType.FILE_LIST;
			} else {
				gtst = GroundTruthSourceType.READY_INPUT;
			}
			String attributesToPredictString = validatorConfig.getAttributesToPredictAttribute().getValueAt(i).toString();
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
			String attributesToIgnoreString = validatorConfig.getAttributesToIgnoreAttribute().getValueAt(i).toString();
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
				AmuseLogger.write(ValidationConfiguration.class.getName(), Level.WARN,
						"The attributes to ignore were not properly specified. All features will be used for training.");
				currentAttributesToIgnore = new ArrayList<Integer>();
			}
			
			RelationshipType currentRelationshipType;
			if(validatorConfig.getRelationshipTypeAttribute().getValueAt(i).toString().equals("BINARY")) {
				currentRelationshipType = RelationshipType.BINARY;
			} else if(validatorConfig.getRelationshipTypeAttribute().getValueAt(i).toString().equals("CONTINUOUS")) {
				currentRelationshipType = RelationshipType.CONTINUOUS;
			} else {
				throw new IOException("The relationship type was not properly specified.");
			}
			
			LabelType currentLabelType;
			if(validatorConfig.getLabelTypeAttribute().getValueAt(i).toString().equals("MULTICLASS")) {
				currentLabelType = LabelType.MULTICLASS;
			} else if(validatorConfig.getLabelTypeAttribute().getValueAt(i).toString().equals("MULTILABEL")) {
				currentLabelType = LabelType.MULTILABEL;
			} else if(validatorConfig.getLabelTypeAttribute().getValueAt(i).toString().equals("SINGLELABEL")) {
				currentLabelType = LabelType.SINGLELABEL;
			} else {
				throw new IOException("The label type was not properly specified.");
			}
			
			MethodType currentMethodType;
			if(validatorConfig.getMethodTypeAttribute().getValueAt(i).toString().equals("SUPERVISED")) {
				currentMethodType = MethodType.SUPERVISED;
			} else if(validatorConfig.getMethodTypeAttribute().getValueAt(i).toString().equals("UNSUPERVISED")) {
				currentMethodType = MethodType.UNSUPERVISED;
			} else if(validatorConfig.getMethodTypeAttribute().getValueAt(i).toString().equals("REGRESSION")) {
				currentMethodType = MethodType.REGRESSION;
			} else {
				throw new IOException("The method type was not properly specified.");
			}
			
			ModelType currentModelType = new ModelType(currentRelationshipType, currentLabelType, currentMethodType);
			
			// Load the measure table
			MeasureTable currentMeasureTable = new MeasureTable(new File(currentMeasureList));
			
			String currentOutputPath = validatorConfig.getOutputPathAttribute().getValueAt(i);
			
			// Create a classification task
		    taskConfigurations.add(new ValidationConfiguration(currentValidationMethodId, currentMeasureTable, 
		    		currentInputFeatureDescription, currentInputFeatureType, currentClassificationWindowSize, currentClassificationWindowOverlap,
		    		currentClassificationAlgorithmDescription, new FileInput(currentInputToValidate),
		    		gtst, currentAttributesToPredict, currentAttributesToIgnore, currentModelType, currentOutputPath));
			AmuseLogger.write(ValidationConfiguration.class.getName(), Level.DEBUG, "Validation task(s) for validation input " + 
					currentInputToValidate.toString() + " loaded");
		}
		
		// Create an array
	    ValidationConfiguration[] tasks = new ValidationConfiguration[taskConfigurations.size()];
		for(int i=0;i<taskConfigurations.size();i++) {
	    	tasks[i] = taskConfigurations.get(i);
	    }
		return tasks;
	}
	
	/**
	 * Returns an array of ValidationConfigurations from the given ARFF file
	 * @param configurationFile ARFF file with configurations for one or more processing tasks
	 * @return ValidationConfigurations
	 */
	public static ValidationConfiguration[] loadConfigurationsFromFile(File configurationFile) throws IOException {
		ValidatorConfigSet validatorConfig = new ValidatorConfigSet(configurationFile);
		return loadConfigurationsFromDataSet(validatorConfig);
	}
	
	/**
	 * @return the validationAlgorithmDescription
	 */
	public String getValidationAlgorithmDescription() {
		return validationAlgorithmDescription;
	}

	/**
	 * @return the measures
	 */
	public MeasureTable getMeasures() {
		return measures;
	}

	/**
	 * @return the processedFeaturesModelName
	 */
	public String getInputFeaturesDescription() {
		return inputFeaturesDescription;
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
	 * @return the classificationWindowOverlap
	 */
	public Integer getClassificationWindowOverlap() {
		return classificationWindowOverlap;
	}

	/**
	 * @return the classificationAlgorithmDescription
	 */
	public String getClassificationAlgorithmDescription() {
		return classificationAlgorithmDescription;
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
	 * Sets the path to folder to load the classification model(s) from (default: Amuse model database)
	 * @param modelDatabase Path to folder
	 */
	public void setModelDatabase(String modelDatabase) {
		this.modelDatabase = modelDatabase;
	}

	/**
	 * @return Folder to load the classification model(s) from (default: Amuse model database)
	 */
	public String getModelDatabase() {
		return modelDatabase;
	}

	/**
	 * Sets the path to folder to store the classification validation results (default: Amuse measure database)
	 * @param measureDatabase Path to folder
	 */
	public void setMeasureDatabase(String measureDatabase) {
		this.measureDatabase = measureDatabase;
	}

	/**
	 * @return Folder to store the classification validation results (default: Amuse measure database)
	 */
	public String getMeasureDatabase() {
		return measureDatabase;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getType()
	 */
	public String getType() {
		return "Validation";
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getDescription()
	 */
	public String getDescription() {
            int songLevel = 0;
            int partitionLevel = 0;
            for (Measure m: measures) {
                if (m.isPartitionLevelSelected())
                    partitionLevel++;
                if (m.isSongLevelSelected())
                    songLevel++;
            }
        return new String("Input: " + inputToValidate.toString() + " Measure number: " + songLevel +"(Songlevel) " + partitionLevel+"(Partitionlevel)");
	}

	/**
	 * @return the inputToValidate
	 */
	public DataInputInterface getInputToValidate() {
		return inputToValidate;
	}

	/**
	 * @return the groundTruthSourceType
	 */
	public GroundTruthSourceType getGroundTruthSourceType() {
		return groundTruthSourceType;
	}

	/**
	 * @param inputToValidate the inputToValidate to set
	 */
	public void setInputToValidate(DataInputInterface inputToValidate) {
		this.inputToValidate = inputToValidate;
	}

	/**
	 * @return the calculatedMeasures
	 */
	public ArrayList<ValidationMeasure> getCalculatedMeasures() {
		return calculatedMeasures;
	}

	/**
	 * @param calculatedMeasures the calculatedMeasures to set
	 */
	public void setCalculatedMeasures(ArrayList<ValidationMeasure> calculatedMeasures) {
		this.calculatedMeasures = calculatedMeasures;
	}
	
	/**
	 * @return the outputPath
	 */
	public String getOutputPath() {
		return outputPath;
	}
	
	/**
	 * @param numberOfFeatureDimensions
	 */
	public void setNumberOfFeatureDimensions(int numberOfFeatureDimensions) {
		this.numberOfFeatureDimensions = numberOfFeatureDimensions;
	}
	
	/**
	 * @return the numberOfFeatureDimensions
	 */
	public Integer getNumberOfFeatureDimensions() {
		return numberOfFeatureDimensions;
	}

	public FeatureTable getInputFeatureList() {
		return inputFeatureList;
	}
}
