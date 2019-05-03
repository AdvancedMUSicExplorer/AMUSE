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

import amuse.data.ClassificationType;
import amuse.data.GroundTruthSourceType;
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
	
	/** Description of methods used for feature processing for the search of 
	 * according features in processed features DB */
	private final String processedFeaturesModelName;
	
	/** Input to validate */
	private DataInputInterface inputToValidate;
	
	/** Ground truth type for this configuration */
	private final GroundTruthSourceType groundTruthSourceType;
	
	
	private final List<Integer> attributesToClassify;
	private final List<Integer> attributesToIgnore;
	private final ClassificationType classificationType;
	private final boolean fuzzy;
	
	
	/** ID of classification algorithm from classifierTable.arff 
	 * (optionally with parameters listed in brackets) */
	private final String classificationAlgorithmDescription;
	
	/** Folder to load the processed features from (default: Amuse processed feature database) */
	private String processedFeatureDatabase;
	
	/** Folder to load the classification model(s) from (default: Amuse model database) */
	private String modelDatabase;
	
	/** Folder to store the classification validation results (default: Amuse measure database) */
	private String measureDatabase;
	
	/** Calculated measures are stored here after the corresponding validation task has been successfully applied */
	private ArrayList<ValidationMeasure> calculatedMeasures;
	
	/**
	 * Standard constructor
	 * @param validationAlgorithmDescription ID and parameters of validation method
	 * @param measures Measure list, e.g. containing accuracy and precision
	 * @param processedFeaturesModelName Description of methods used for feature processing
	 * @param classificationAlgorithmDescription ID of classification algorithm from classifierTable.arff
 	 * @param groundTruthSource Source with input to validate. Can be either
	 * - Id of the music category from $AMUSEHOME$/config/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready labeled input 
	 * - Ready input (as EditableDataSet)
	 * @param groundTruthSourceType Describes the source type of ground truth 
	 */
	public ValidationConfiguration(String validationAlgorithmDescription, MeasureTable measures, 
			String processedFeaturesModelName, String classificationAlgorithmDescription,
			DataInputInterface inputToValidate, GroundTruthSourceType groundTruthSourceType, List<Integer> attributesToClassify, List<Integer> attributesToIgnore, ClassificationType classificationType, boolean fuzzy) {
		this.validationAlgorithmDescription = validationAlgorithmDescription;
		this.measures = measures;
		this.processedFeaturesModelName = processedFeaturesModelName;
		this.classificationAlgorithmDescription = classificationAlgorithmDescription;
		this.inputToValidate = inputToValidate;
		this.groundTruthSourceType = groundTruthSourceType;
		this.attributesToClassify = attributesToClassify;
		this.attributesToIgnore = attributesToIgnore;
		this.classificationType = classificationType;
		this.fuzzy = fuzzy;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.modelDatabase = AmusePreferences.get(KeysStringValue.MODEL_DATABASE);
		this.measureDatabase = AmusePreferences.get(KeysStringValue.MEASURE_DATABASE);
		this.calculatedMeasures = new ArrayList<ValidationMeasure>();
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
			String currentProcessedFeaturesModelName = validatorConfig.getProcessedFeatureDescriptionAttribute().getValueAt(i).toString();
			String currentClassificationAlgorithmDescription = validatorConfig.getClassificationAlgorithmIdAttribute().getValueAt(i).toString();
			String currentInputToValidate = validatorConfig.getInputToValidateAttribute().getValueAt(i).toString();
			GroundTruthSourceType gtst;
			if(validatorConfig.getGroundTruthSourceAttribute().getValueAt(i).toString().equals(new String("CATEGORY_ID"))) {
				gtst = GroundTruthSourceType.CATEGORY_ID;
				
				// Search for the category file
				DataSetAbstract categoryList = new ArffDataSet(new File(AmusePreferences.getMultipleTracksAnnotationTablePath()));
				for(int j=0;j<categoryList.getValueCount();j++) {
					Integer id = new Double(categoryList.getAttribute("Id").getValueAt(j).toString()).intValue();
					if(id.toString().equals(currentInputToValidate)) {
						currentInputToValidate = id.toString();
						break;
					}
				}
			} else {
				gtst = GroundTruthSourceType.READY_INPUT;
			}
			
			
			String attributesToClassifyString = validatorConfig.getAttributesToClassifyAttribute().getValueAt(i).toString();
			attributesToClassifyString = attributesToClassifyString.replaceAll("\\[", "").replaceAll("\\]", "");
			String[] attributesToClassifyStringArray = attributesToClassifyString.split("\\s*,\\s*");
			List<Integer> currentAttributesToClassify = new ArrayList<Integer>();
			try {
				for(String str : attributesToClassifyStringArray) {
					if(!str.equals("")) {
						currentAttributesToClassify.add(Integer.parseInt(str));
					} else {
						throw new IOException("No categories for validation were specified.");
					}
				}
			} catch(NumberFormatException e) {
				throw new IOException("The categories for validation were not properly specified.");
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
			
			ClassificationType currentClassificationType;
			if(validatorConfig.getClassificationTypeAttribute().getValueAt(i).toString().equals("UNSUPERVISED")) {
				currentClassificationType = ClassificationType.UNSUPERVISED;
			} else if(validatorConfig.getClassificationTypeAttribute().getValueAt(i).toString().equals("BINARY")) {
				currentClassificationType = ClassificationType.BINARY;
			} else if(validatorConfig.getClassificationTypeAttribute().getValueAt(i).equals("MULTILABEL")) {
				currentClassificationType = ClassificationType.MULTILABEL;
			} else { //Ist es gut Sachen einfach standardmaessig als multiclass einzustellen, wenn sich jemand vertippt oder so?
				currentClassificationType = ClassificationType.MULTICLASS;
			}
			
			boolean currentFuzzy = validatorConfig.getFuzzyAttribute().getValueAt(i) >= 0.5;
			//***
			
			// Load the measure table
			MeasureTable currentMeasureTable = new MeasureTable(new File(currentMeasureList));
			
			// Create a classification task
		    taskConfigurations.add(new ValidationConfiguration(currentValidationMethodId, currentMeasureTable, 
		    		currentProcessedFeaturesModelName, currentClassificationAlgorithmDescription, new FileInput(currentInputToValidate),
		    		gtst, currentAttributesToClassify, currentAttributesToIgnore, currentClassificationType, currentFuzzy));
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
	public String getProcessedFeaturesModelName() {
		return processedFeaturesModelName;
	}

	/**
	 * @return the classificationAlgorithmDescription
	 */
	public String getClassificationAlgorithmDescription() {
		return classificationAlgorithmDescription;
	}

	public List<Integer> getAttributesToClassify(){
		return attributesToClassify;
	}
	
	public List<Integer> getAttributesToIgnore(){
		return attributesToIgnore;
	}
	
	public ClassificationType getClassificationType() {
		return classificationType;
	}
	
	public boolean isFuzzy() {
		return fuzzy;
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

}
