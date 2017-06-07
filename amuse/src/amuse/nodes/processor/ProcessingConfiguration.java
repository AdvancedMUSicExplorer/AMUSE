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
 * @version $Id: $
 */
public class ProcessingConfiguration extends TaskConfiguration {

	/** For Serializable interface */
	private static final long serialVersionUID = -6557916168549473663L; 

	/** Music file list for feature processing */
	private final FileTable musicFileList;

	/** List with features to process */
	private final FeatureTable inputFeatureList;
	
	/** Processing steps (feature or time dimension processing) */
	private final String reductionSteps;
	
	/** Partition size (each partition -> input for classification) */
	private final Integer partitionSize;
	
	/** Partition overlap */
	private final Integer partitionOverlap;
	
	/** Method for conversion of matrix (features over time) to vector */
	private final String conversionStep;
	
	/** Feature description, if required */	
	private final String featureDescription;
	
	/** Folder to store the processed features (default: Amuse processed feature database) */
	private String processedFeatureDatabase;
	
	/**
	 * Standard constructor
	 * @param inputMusicFile Music file list for feature processing
	 * @param inputFeatureList List with features to process
	 * @param reductionSteps Processing steps (feature or time dimension processing)
	 * @param partitionSize Partition size (each partition -> input for classification)
	 * @param partitionOverlap Partition overlap
	 * @param conversionStep Method for conversion of matrix (features over time) to vector
	 * @param featureDescription Feature description, if required
	 */
	public ProcessingConfiguration(FileTable musicFileList, String inputFeatureList, String reductionSteps,
			Integer partitionSize, Integer partitionOverlap, String conversionStep, String featureDescription) {
		this.musicFileList = musicFileList;
		this.inputFeatureList = new FeatureTable(new File(inputFeatureList));
		this.inputFeatureList.removeUnsuitableForFeatureMatrixProcessing();
		this.reductionSteps = reductionSteps;
		this.partitionSize = partitionSize;
		this.partitionOverlap = partitionOverlap;
		this.conversionStep = conversionStep;
		this.featureDescription = featureDescription;
		this.processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
	}
	
	/**
	 * Constructor with use of FeatureTableSet
	 * @param inputMusicFile Music file list for feature processing
	 * @param inputFeatureList List with features to process
	 * @param reductionSteps Processing steps (feature or time dimension processing)
	 * @param partitionSize Partition size (each partition -> input for classification)
	 * @param partitionOverlap Partition overlap
	 * @param conversionStep Method for conversion of matrix (features over time) to vector
	 * @param featureDescription Feature description, if required
	 */
	public ProcessingConfiguration(FileTable musicFileList, FeatureTable inputFeatureList, String reductionSteps,
			Integer partitionSize, Integer partitionOverlap, String conversionStep, String featureDescription) {
		this.musicFileList = musicFileList;
		this.inputFeatureList = inputFeatureList;
		this.reductionSteps = reductionSteps;
		this.partitionSize = partitionSize;
		this.partitionOverlap = partitionOverlap;
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
			String currentFeatureList = processingConfig.getFeatureListAttribute().getValueAt(i).toString();
			String currentReductionSteps = processingConfig.getReductionStepsAttribute().getValueAt(i).toString();
			Integer currentPartitionSize = (new Double(processingConfig.getPartitionSizeAttribute().getValueAt(i).toString())).intValue();
			Integer currentPartitionOverlap = (new Double(processingConfig.getPartitionOverlapAttribute().getValueAt(i).toString())).intValue();
			String currentMatrixToVectorMethod = processingConfig.getMatrixToVectorMethodAttribute().getValueAt(i).toString();
			String currentFeatureDescription = processingConfig.getFeatureDescriptionAttribute().getValueAt(i).toString();
	
			// Proceed music files from the current file list
			FileTable currentFileTable = new FileTable(new File(currentMusicFileList));
		    taskConfigurations.add(new ProcessingConfiguration(currentFileTable, currentFeatureList, currentReductionSteps,
	    		currentPartitionSize, currentPartitionOverlap, currentMatrixToVectorMethod, currentFeatureDescription));
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
	 * @return the partitionSize
	 */
	public Integer getPartitionSize() {
		return partitionSize;
	}

	/**
	 * @return the partitionOverlap
	 */
	public Integer getPartitionOverlap() {
		return partitionOverlap;
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
		return new String("File number: " + musicFileList.getFiles().size() + " Feature number: " + inputFeatureList.size());
	}
	
}
