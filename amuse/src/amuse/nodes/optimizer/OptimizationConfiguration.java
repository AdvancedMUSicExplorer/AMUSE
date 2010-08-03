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
package amuse.nodes.optimizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.util.AmuseLogger;

/**
 * Describes the parameters for an optimization task 
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class OptimizationConfiguration extends TaskConfiguration {

	/** For Serializable interface */
	private static final long serialVersionUID = -6880712656836146853L;
	
	/** Category Id for training set (only from these files the training models will be built) */
	private final Integer categoryLearningId;
	
	/** Category Id for optimization set. Set to -1 if optimization should be done as 10-fold 
	 * cross-validation using only the training set */
	private final Integer categoryOptimizationId;
	
	/** Category Id for independent test set (Here the generalization performance of optimizer 
	 * can be evaluated). Set to -1 if independent test set should not be used */
	private final Integer categoryTestId;
	
	/** ID of optimization algorithm from optimizerAlgorithmTable.arff 
	 * (optionally with parameters listed in brackets) */
	private final String algorithmDescription;
	
	/** For a new experiment set to -1. If the previous optimization should be continued, 
	 * set here the path to the log file with previous experiment */
	private final String continueOldExperimentFrom;

	/** Folder for optimization results: they are written to the following folder:
	 * '[optimizationDatabase from amuse.properties]/[evaluation category description]/
	 * DESTINATION_FOLDER/optimization_x.arff'
	 * where x is equal to the number of files in the folder plus one
	 */
	private final String destinationFolder;
	
	/**
	 * Standard constructor
	 * @param categoryLearningId Id of the music category which is used for training
	 * @param categoryOptimizationId Id of the music category which is used for independent optimization
	 * @param categoryTestId Id of the music category which is used for independent evaluation
	 * @param algorithmDescription Id of optimization algorithm from optimizerAlgorithmTable.arff
	 * @param continueOldExperimentFrom Path to file with previous unfinished experiment to continue 
	 * from (otherwise "-1", which is default value)
	 * @param destinationFolder Folder for optimization results
	 */
	public OptimizationConfiguration(Integer categoryLearningId, Integer categoryOptimizationId, 
			Integer categoryTestId, String algorithmDescription, String continueOldExperimentFrom,
			String destinationFolder) {
		this.categoryLearningId = categoryLearningId;
		this.categoryOptimizationId = categoryOptimizationId;
		this.categoryTestId = categoryTestId;
		this.algorithmDescription = algorithmDescription;
		if(continueOldExperimentFrom.equals("")) continueOldExperimentFrom = "-1";
		this.continueOldExperimentFrom = continueOldExperimentFrom;
		this.destinationFolder = destinationFolder;
	}
	
	/**
	 * Returns an array of OptimizationConfigurations from the given data set
	 * @param optimizerConfig ARFF file with configurations for one or more processing tasks
	 * @return OptimizationConfigurations
	 * @throws IOException 
	 */
	public static OptimizationConfiguration[] loadConfigurationsFromDataSet(DataSetAbstract optimizerConfig) throws IOException {
		ArrayList<OptimizationConfiguration> taskConfigurations = new ArrayList<OptimizationConfiguration>();
			
   		// Proceed music file lists one by one
	    for(int i=0;i<optimizerConfig.getValueCount();i++) {
			Integer currentCategoryForLearningId = (new Double(optimizerConfig.getAttribute("CategoryId").getValueAt(i).toString())).intValue();
			Integer currentCategoryForOptimizationId = (new Double(optimizerConfig.getAttribute("CategoryOptimizationId").getValueAt(i).toString())).intValue();
			Integer currentCategoryForTestId = (new Double(optimizerConfig.getAttribute("CategoryTestId").getValueAt(i).toString())).intValue();
			String currentAlgorithmDescription = optimizerConfig.getAttribute("AlgorithmId").getValueAt(i).toString();
			String currentContinueOldExperimentFrom = optimizerConfig.getAttribute("ContinueOldExperimentFrom").getValueAt(i).toString();
			if(currentContinueOldExperimentFrom.equals("")) currentContinueOldExperimentFrom = "-1";
			String currentDestinationFolder = optimizerConfig.getAttribute("DestinationFolder").getValueAt(i).toString();
				
			// Create an optimization task
		    taskConfigurations.add(new OptimizationConfiguration(currentCategoryForLearningId, currentCategoryForOptimizationId, 
		    		currentCategoryForTestId, currentAlgorithmDescription, currentContinueOldExperimentFrom, currentDestinationFolder));
			AmuseLogger.write(OptimizationConfiguration.class.getName(), Level.DEBUG, 
					taskConfigurations.size() + " optimization task(s) for category " + currentCategoryForLearningId + " loaded");
		}
		
		// Create an array
	    OptimizationConfiguration[] tasks = new OptimizationConfiguration[taskConfigurations.size()];
		for(int i=0;i<taskConfigurations.size();i++) {
	    	tasks[i] = taskConfigurations.get(i);
	    }
		return tasks;
	}
	
	/**
	 * Returns an array of OptimizationConfigurations from the given ARFF file
	 * @param configurationFile ARFF file with configurations for one or more processing tasks
	 * @return OptimizationConfigurations
	 */
	public static OptimizationConfiguration[] loadConfigurationsFromFile(File configurationFile) throws IOException {
		DataSetAbstract optimizerConfig = new ArffDataSet(configurationFile);
		return loadConfigurationsFromDataSet(optimizerConfig);
	}

	/**
	 * @return the categoryLearningId
	 */
	public Integer getCategoryLearningId() {
		return categoryLearningId;
	}

	/**
	 * @return the categoryOptimizationId
	 */
	public Integer getCategoryOptimizationId() {
		return categoryOptimizationId;
	}
	
	/**
	 * @return the categoryTestId
	 */
	public Integer getCategoryTestId() {
		return categoryTestId;
	}

	/**
	 * @return the continueOldExperimentFrom
	 */
	public String getContinueOldExperimentFrom() {
		return continueOldExperimentFrom;
	}
	
	/**
	 * @return Algorithm description
	 */
	public String getAlgorithmDescription() {
		return algorithmDescription;
	}
	
	/**
	 * @return Destination folder
	 */
	public String getDestinationFolder() {
		return destinationFolder;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getType()
	 */
	public String getType() {
		return "Optimization";
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getDescription()
	 */
	public String getDescription() {
		return new String("Training category: " + categoryLearningId + " Optimization category: " + categoryOptimizationId + 
				" Test category: " + categoryTestId);
	}
}
