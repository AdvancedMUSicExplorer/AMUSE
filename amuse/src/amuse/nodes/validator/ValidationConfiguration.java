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

import amuse.data.Metric;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.MetricTable;
import amuse.data.datasets.ValidatorConfigSet;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataInputInterface;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.FileInput;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.validator.interfaces.ValidationMetric;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Describes the parameters for a classification validation task 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ValidationConfiguration extends TaskConfiguration {

	/** For Serializable interface */
	private static final long serialVersionUID = 8680312567847735075L;
	
	/** ID of validation method (optionally with parameters listed in brackets) */
	private final String validationAlgorithmDescription;
	
	/** Metric list, e.g. containing accuracy and precision */
	private final MetricTable metrics;
	
	/** Description of methods used for feature processing for the search of 
	 * according features in processed features DB */
	private final String processedFeaturesModelName;
	
	/** Input to validate */
	private DataInputInterface inputToValidate;
	
	/** Defines the ground truth type. Can be either
	 * - Id of the music category from $AMUSECATEGORYDATABASE$/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready training input (prepared e.g. by a validator method) */
	public enum GroundTruthSourceType {CATEGORY_ID, FILE_LIST, READY_INPUT};
	
	/** Ground truth type for this configuration */
	private final GroundTruthSourceType groundTruthSourceType;
	
	/** ID of classification algorithm from classifierTable.arff 
	 * (optionally with parameters listed in brackets) */
	private final String classificationAlgorithmDescription;
	
	/** Folder to load the processed features from (default: Amuse processed feature database) */
	private String processedFeatureDatabase;
	
	/** Folder to load the classification model(s) from (default: Amuse model database) */
	private String modelDatabase;
	
	/** Folder to store the classification validation results (default: Amuse metric database) */
	private String metricDatabase;
	
	/** Calculated metrics are stored here after the corresponding validation task has been successfully applied */
	private ArrayList<ValidationMetric> calculatedMetrics;
	
	/**
	 * Standard constructor
	 * @param validationAlgorithmDescription ID and parameters of validation method
	 * @param metrics Metric list, e.g. containing accuracy and precision
	 * @param processedFeaturesModelName Description of methods used for feature processing
	 * @param classificationAlgorithmDescription ID of classification algorithm from classifierTable.arff
 	 * @param groundTruthSource Source with input to validate. Can be either
	 * - Id of the music category from $AMUSECATEGORYDATABASE$/categoryTable.arff or
	 * - Path to the labeled file list or
	 * - Path to the ready labeled input 
	 * - Ready input (as EditableDataSet)
	 * @param groundTruthSourceType Describes the source type of ground truth 
	 */
	public ValidationConfiguration(String validationAlgorithmDescription, MetricTable metrics, 
			String processedFeaturesModelName, String classificationAlgorithmDescription,
			DataInputInterface inputToValidate, GroundTruthSourceType groundTruthSourceType) {
		this.validationAlgorithmDescription = validationAlgorithmDescription;
		this.metrics = metrics;
		this.processedFeaturesModelName = processedFeaturesModelName;
		this.classificationAlgorithmDescription = classificationAlgorithmDescription;
		this.inputToValidate = inputToValidate;
		this.groundTruthSourceType = groundTruthSourceType;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
		this.modelDatabase = AmusePreferences.get(KeysStringValue.MODEL_DATABASE);
		this.metricDatabase = AmusePreferences.get(KeysStringValue.METRIC_DATABASE);
		this.calculatedMetrics = new ArrayList<ValidationMetric>();
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
			String currentMetricList = validatorConfig.getMetricListAttribute().getValueAt(i).toString();
			String currentProcessedFeaturesModelName = validatorConfig.getProcessedFeatureDescriptionAttribute().getValueAt(i).toString();
			String currentClassificationAlgorithmDescription = validatorConfig.getClassificationAlgorithmIdAttribute().getValueAt(i).toString();
			String currentInputToValidate = validatorConfig.getInputToValidateAttribute().getValueAt(i).toString();
			GroundTruthSourceType gtst;
			if(validatorConfig.getGroundTruthSourceAttribute().getValueAt(i).toString().equals(new String("CATEGORY_ID"))) {
				gtst = GroundTruthSourceType.CATEGORY_ID;
				
				// Search for the category file
				DataSetAbstract categoryList = new ArffDataSet(new File(AmusePreferences.get(KeysStringValue.CATEGORY_DATABASE)));
				for(int j=0;j<categoryList.getValueCount();j++) {
					Integer id = new Double(categoryList.getAttribute("Id").getValueAt(j).toString()).intValue();
					if(id.toString().equals(currentInputToValidate)) {
						currentInputToValidate = id.toString();
						break;
					}
				}
			} else if(validatorConfig.getGroundTruthSourceAttribute().getValueAt(i).toString().equals(new String("FILE_LIST"))) {
				gtst = GroundTruthSourceType.FILE_LIST;
			} else {
				gtst = GroundTruthSourceType.READY_INPUT;
			}
			
			// Load the metric table
			MetricTable currentMetricTable = new MetricTable(new File(currentMetricList));
			
			// Create a classification task
		    taskConfigurations.add(new ValidationConfiguration(currentValidationMethodId, currentMetricTable, 
		    		currentProcessedFeaturesModelName, currentClassificationAlgorithmDescription, new FileInput(currentInputToValidate),
		    		gtst));
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
	 * @return the metrics
	 */
	public MetricTable getMetrics() {
		return metrics;
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
	 * Sets the path to folder to store the classification validation results (default: Amuse metric database)
	 * @param metricDatabase Path to folder
	 */
	public void setMetricDatabase(String metricDatabase) {
		this.metricDatabase = metricDatabase;
	}

	/**
	 * @return Folder to store the classification validation results (default: Amuse metric database)
	 */
	public String getMetricDatabase() {
		return metricDatabase;
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
            for (Metric m: metrics) {
                if (m.isPartitionLevelSelected())
                    partitionLevel++;
                if (m.isSongLevelSelected())
                    songLevel++;
            }
        return new String("Input: " + inputToValidate.toString() + " Metric number: " + songLevel +"(Songlevel) " + partitionLevel+"(Partitionlevel)");
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
	 * @return the calculatedMetrics
	 */
	public ArrayList<ValidationMetric> getCalculatedMetrics() {
		return calculatedMetrics;
	}

	/**
	 * @param calculatedMetrics the calculatedMetrics to set
	 */
	public void setCalculatedMetrics(ArrayList<ValidationMetric> calculatedMetrics) {
		this.calculatedMetrics = calculatedMetrics;
	}

}
