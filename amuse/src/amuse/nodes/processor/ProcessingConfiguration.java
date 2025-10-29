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
package amuse.nodes.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.FeatureTable;
import amuse.data.FileTable;
import amuse.data.datasets.ProcessorConfigSet;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Describes the parameters for a feature processing task 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ProcessingConfiguration extends TaskConfiguration {

	/** For Serializable interface */
	private static final long serialVersionUID = -6557916168549473663L; 
	
	/** Input types */
	public enum InputSourceType{RAW_FEATURE_LIST, PROCESSING_CONFIG};

	/** Music file list for feature processing */
	private final FileTable musicFileList;

	/** Input type for this configuration */
	private final InputSourceType inputSourceType;
	
	/** Input features (either raw feature list or processing configuration) */
	private final String inputFeatures;
	
	/** List with features to process */
	private final FeatureTable inputFeatureList;
	
	/** Processing steps (feature or time dimension processing) */
	private final String reductionSteps;
	
	/** Unit */
	public enum Unit {MILLISECONDS, SAMPLES};
	
	/** Unit of window and step size */
	private final Unit unit;
	
	/** Aggregation window size (each window -> input for classification) */
	private final Integer aggregationWindowSize;
	
	/** Aggregation window step size */
	private final Integer aggregationWindowStepSize;
	
	/** Method for conversion of matrix (features over time) to vector */
	private final String conversionStep;
	
	/** Feature description, if required */	
	private final String featureDescription;
	
	/** Folder to load the raw features; can be currently configured only with the set method, not with a constructor (can be used in a tool node) */
	private String featureDatabase;
	
	/** Folder to store the processed features (default: Amuse processed feature database) */
	private String processedFeatureDatabase;
	
	/**
	 * Standard constructor
	 * @param inputMusicFile Music file list for feature processing
	 * @param inputSourceType Input source type, either raw or processed features
	 * @param inputFeatures Description of input features (feature table or processing configuration)
	 * @param reductionSteps Processing steps (feature or time dimension processing)
	 * @param aggregationWindowSize Aggregation window size (each window -> input for classification)
	 * @param aggregationWindowStepSize Classification window step size
	 * @param conversionStep Method for conversion of matrix (features over time) to vector
	 * @param featureDescription Feature description, if required
	 */
	public ProcessingConfiguration(FileTable musicFileList, InputSourceType inputSourceType, String inputFeatures, String reductionSteps, Unit unit,
			Integer aggregationWindowSize, Integer aggregationWindowStepSize, String conversionStep, String featureDescription) {
		this.musicFileList = musicFileList;
		this.inputSourceType = inputSourceType;
		if(inputSourceType == InputSourceType.RAW_FEATURE_LIST) {
			this.inputFeatureList = new FeatureTable(new File(inputFeatures));
		} else {
			this.inputFeatureList = null;
		}
		this.inputFeatures = inputFeatures;
		this.reductionSteps = reductionSteps;
		this.unit = unit;
		this.aggregationWindowSize = aggregationWindowSize;
		this.aggregationWindowStepSize = aggregationWindowStepSize;
		this.conversionStep = conversionStep;
		this.featureDescription = featureDescription;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
	}
	
	/**
	 * Standard constructor
	 * @param inputMusicFile Music file list for feature processing
	 * @param inputFeatureList List with features to process
	 * @param reductionSteps Processing steps (feature or time dimension processing)
	 * @param classificationWindowSize Classification window size (each classification window -> input for classification)
	 * @param classificationWindowStepSize Classification window step size
	 * @param conversionStep Method for conversion of matrix (features over time) to vector
	 * @param featureDescription Feature description, if required
	 * 
	 * @deprecated Old constructor when only raw features are used as input
	 */
	public ProcessingConfiguration(FileTable musicFileList, String inputFeatureList, String reductionSteps, Unit unit,
			Integer classificationWindowSize, Integer classificationWindowStepSize, String conversionStep, String featureDescription) {
		this.musicFileList = musicFileList;
		this.inputSourceType = InputSourceType.RAW_FEATURE_LIST;
		this.inputFeatures = null;
		this.inputFeatureList = new FeatureTable(new File(inputFeatureList));
		this.reductionSteps = reductionSteps;
		this.unit = unit;
		this.aggregationWindowSize = classificationWindowSize;
		this.aggregationWindowStepSize = classificationWindowStepSize;
		this.conversionStep = conversionStep;
		this.featureDescription = featureDescription;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
	}
	
	/**
	 * Constructor with use of FeatureTableSet
	 * @param inputMusicFile Music file list for feature processing
	 * @param inputFeatureList List with features to process
	 * @param reductionSteps Processing steps (feature or time dimension processing)
	 * @param classificationWindowSize Classification window size (each classification window -> input for classification)
	 * @param classificationWindowStepSize Classification window step size
	 * @param conversionStep Method for conversion of matrix (features over time) to vector
	 * @param featureDescription Feature description, if required
	 * 
	 * @deprecated Old constructor when only raw features are used as input
	 */
	public ProcessingConfiguration(FileTable musicFileList, FeatureTable inputFeatureList, String reductionSteps, Unit unit,
			Integer classificationWindowSize, Integer classificationWindowStepSize, String conversionStep, String featureDescription) {
		this.musicFileList = musicFileList;
		this.inputSourceType = InputSourceType.RAW_FEATURE_LIST;
		this.inputFeatures = null;
		this.inputFeatureList = inputFeatureList;
		this.reductionSteps = reductionSteps;
		this.unit = unit;
		this.aggregationWindowSize = classificationWindowSize;
		this.aggregationWindowStepSize = classificationWindowStepSize;
		this.conversionStep = conversionStep;
		this.featureDescription = featureDescription;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
	}
	
	/**
	 * Returns an array of ProcessingConfigurations from the given data set
	 * @param processingConfig Data set with configurations for one or more processing tasks
	 * @return ProcessorConfigurations
	 */
	public static ProcessingConfiguration[] loadConfigurationsFromDataSet(ProcessorConfigSet processingConfig) throws IOException {
		ArrayList<ProcessingConfiguration> taskConfigurations = new ArrayList<ProcessingConfiguration>();
		
   		// Proceed music file lists one by one
	    for(int i=0;i<processingConfig.getValueCount();i++) {
			String currentMusicFileList = processingConfig.getMusicFileListAttribute().getValueAt(i).toString();
			String currentInputSourceType = processingConfig.getInputSourceTypeAttribute().getValueAt(i).toString();
			String currentInput = processingConfig.getInputAttribute().getValueAt(i).toString();
			String currentReductionSteps = processingConfig.getReductionStepsAttribute().getValueAt(i).toString();
			Unit currentUnit = Unit.valueOf(processingConfig.getUnitAttribute().getValueAt(i).toString());
			Integer currentAggregationWindowSize = (new Double(processingConfig.getAggregationWindowSizeAttribute().getValueAt(i).toString())).intValue();
			Integer currentAggregationWindowStepSize = (new Double(processingConfig.getAggregationWindowStepSizeAttribute().getValueAt(i).toString())).intValue();
			String currentMatrixToVectorMethod = processingConfig.getMatrixToVectorMethodAttribute().getValueAt(i).toString();
			String currentFeatureDescription = processingConfig.getFeatureDescriptionAttribute().getValueAt(i).toString();
	
			// Proceed music files from the current file list
			FileTable currentFileTable = new FileTable(new File(currentMusicFileList));
		    taskConfigurations.add(new ProcessingConfiguration(currentFileTable, InputSourceType.valueOf(currentInputSourceType), currentInput, currentReductionSteps, currentUnit,
	    		currentAggregationWindowSize, currentAggregationWindowStepSize, currentMatrixToVectorMethod, currentFeatureDescription));
			AmuseLogger.write(ProcessingConfiguration.class.getName(), Level.DEBUG, taskConfigurations.size() + " processing task(s) for " + currentMusicFileList + " loaded");
		}
		
		// Create an array
	    ProcessingConfiguration[] tasks = new ProcessingConfiguration[taskConfigurations.size()];
		for(int i=0;i<taskConfigurations.size();i++) {
	    	tasks[i] = taskConfigurations.get(i);
	    }
		return tasks;
	}
	
	/**
	 * Returns an array of ProcessingConfigurations from the given ARFF file
	 * @param configurationFile ARFF file with configurations for one or more processing tasks
	 * @return ProcessorConfigurations
	 */
	public static ProcessingConfiguration[] loadConfigurationsFromFile(File configurationFile) throws IOException {
		ProcessorConfigSet processingConfig = new ProcessorConfigSet(configurationFile);
		return loadConfigurationsFromDataSet(processingConfig);
	}

	/**
	 * @return the musicFileList
	 */
	public FileTable getMusicFileList() {
		return musicFileList;
	}

	/**
	 * @return the inputSourceType
	 */
	public InputSourceType getInputSourceType() {
		return inputSourceType;
	}
	
	/**
	 * @return the inputFeatures
	 */
	public String getInputFeatures() {
		return inputFeatures;
	}
	
	/**
	 * @return the inputFeatureList
	 */
	public FeatureTable getInputFeatureList() {
		return inputFeatureList;
	}

	/**
	 * @return the reductionSteps
	 */
	public String getReductionSteps() {
		return reductionSteps;
	}

	/**
	 * @return the aggregationWindowSize
	 */
	public Integer getAggregationWindowSize() {
		return aggregationWindowSize;
	}

	/**
	 * @return the AggregationWindowStepSize
	 */
	public Integer getAggregationWindowStepSize() {
		return aggregationWindowStepSize;
	}

	/**
	 * @return the conversionStep
	 */
	public String getConversionStep() {
		return conversionStep;
	}

	/**
	 * @return the featureDescription
	 */
	public String getFeatureDescription() {
		return featureDescription;
	}

	/**
	 * Sets the path to folder to store the processed features (default: Amuse processed feature database)
	 * @param processedFeatureDatabase Path to folder
	 */
	public void setProcessedFeatureDatabase(String processedFeatureDatabase) {
		this.processedFeatureDatabase = processedFeatureDatabase;
	}

	/**
	 * @return Folder to store the processed features (default: Amuse processed feature database)
	 */
	public String getProcessedFeatureDatabase() {
		return processedFeatureDatabase;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getType()
	 */
	public String getType() {
		return "Feature Processing";
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getDescription()
	 */
	public String getDescription() {
		return new String("Number of files: " + musicFileList.getFiles().size() + " Number of features: " + inputFeatureList.getSelectedIds().size());
	}
	
	/**
	 * @return Feature database
	 */
	public String getFeatureDatabase() {
		return featureDatabase;
	}

	/**
	 * @param featureDatabase Feature database
	 */
	public void setFeatureDatabase(String featureDatabase) {
		this.featureDatabase = featureDatabase;
	}

	public Unit getUnit() {
		return unit;
	}
	
}
