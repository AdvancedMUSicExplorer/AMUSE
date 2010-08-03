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
 * Creation date: 16.01.2008
 */
package amuse.nodes.validator.methods;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Level;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;
import amuse.data.MetricTable;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.ClassifierNodeScheduler;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitionsDescription;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.nodes.validator.ValidatorNodeScheduler;
import amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface;
import amuse.nodes.validator.interfaces.DataReductionMetricCalculatorInterface;
import amuse.nodes.validator.interfaces.MetricCalculatorInterface;
import amuse.nodes.validator.interfaces.ValidationMetric;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;
import amuse.nodes.validator.interfaces.ValidatorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Performs n-fold cross-validation
 * 
 * @author Igor Vatolkin
 * @version $Id: SingleEvaluator.java 1253 2010-08-03 14:05:51Z vatolkin $
 */
public class SingleEvaluator extends AmuseTask implements ValidatorInterface {
	
	/** Metric calculator used by this validator */
	private ArrayList<MetricCalculatorInterface> metricCalculators = new ArrayList<MetricCalculatorInterface>();
	
	/** Ids of metrics to calculate */
	private ArrayList<Integer> metricIds = new ArrayList<Integer>();
	
	/** Folder for models and metrics use the classification algorithm description */
	private String classifierDescription = null;
	
	/** Path to a single model file or folder with several models which should be evaluated */
	private String pathToModelFile = null;
	
	private File folderForMetrics = null;
	
	/**
	 * Performs validation of the given model(s)
	 */
	public void validate() throws NodeException {
		
		// --------------------------------------
		// (I) Set the name for the metric folder
		// --------------------------------------
		try {
			setFolderForMetrics();
		} catch(NodeException e) {
			throw e;
		}
		
		// ---------------------------------
		// (II) Configure metric calculators
		// ---------------------------------
		try {
			configureMetricCalculators();
		} catch(NodeException e) {
			throw e;
		}
		
		// ------------------------
		// (III) Perform evaluation
		// ------------------------
		try {
			performEvaluation();
		} catch(NodeException e) {
			throw e;
		}
		
	}
	
	/**
	 * Sets the name of the folder for metrics
	 * @throws NodeException
	 */
	private void setFolderForMetrics() throws NodeException {
		ArffLoader classificationAlgorithmLoader = new ArffLoader();
		Instance classificationAlgorithmInstance;
		boolean classificationMethodFound = false; 
		int algorithmToSearch;
		if(((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription().indexOf("[") == -1) {
			algorithmToSearch = new Double(((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription()).intValue();
		} else {
			algorithmToSearch = new Double(((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription().substring(0,
					((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription().indexOf("["))).intValue();
		}
		try {
			if(this.correspondingScheduler.getDirectStart()) {
				classificationAlgorithmLoader.setFile(new File(System.getenv("AMUSEHOME") + "/config/classifierAlgorithmTable.arff"));
	    	} else {
	    		classificationAlgorithmLoader.setFile(new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + "/classifierAlgorithmTable.arff"));
	    	}
			Attribute idAttribute = classificationAlgorithmLoader.getStructure().attribute("Id");
			Attribute nameAttribute = classificationAlgorithmLoader.getStructure().attribute("Name");
			classificationAlgorithmInstance = classificationAlgorithmLoader.getNextInstance(classificationAlgorithmLoader.getStructure());
			while(classificationAlgorithmInstance != null) {
				
				// If the given classification algorithm is found..
				if(classificationAlgorithmInstance.value(idAttribute) == algorithmToSearch) {
					classificationMethodFound = true;
					
					// Set the name of folder for models and metrics (combined from
					// classifier ID, parameters and name)
					classifierDescription = ((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription() + "-" + classificationAlgorithmInstance.stringValue(nameAttribute);

					String validatorMethodId = ((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getValidationAlgorithmDescription();
					if(validatorMethodId.contains("[")) {
						validatorMethodId = validatorMethodId.substring(0,validatorMethodId.indexOf("["));
					}
					
					// Check if the folder for metric file exists; if not create it
					this.folderForMetrics = new File(
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getMetricDatabase() + "/" + 
							((ValidatorNodeScheduler)this.correspondingScheduler).getCategoryDescription() + 
							File.separator + classifierDescription + File.separator +
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName() + File.separator +
							validatorMethodId + 
							"-" + properties.getProperty("name"));
					if(!this.folderForMetrics.exists()) {
						if(!this.folderForMetrics.mkdirs()) {
							throw new NodeException("Could not create the folder for classifier evaluation metrics: " + this.folderForMetrics);
						}
					}
					break;
				}
				classificationAlgorithmInstance = classificationAlgorithmLoader.getNextInstance(classificationAlgorithmLoader.getStructure());
			}
		
			// Check the classification method id
			if(!classificationMethodFound) {
				throw new NodeException("Could not find the appropriate classification method for algorithm with ID: " + 
						properties.getProperty("classificationAlgorithmId"));
			}
		
		} catch(Exception e) {
			throw new NodeException("Configuration of classifier for validation failed: " + e.getMessage());
		}
	}
	
	/**
	 * Configures metric calculators
	 * @throws NodeException
	 */
	private void configureMetricCalculators() throws NodeException {
		
		// TODO Support metric calculators which use some parameters (like F-Measure) -> similar to algorithms 
		try {
			MetricTable mt = ((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getMetrics();
			for(int i=0;i<mt.size();i++) {
				
				// Set metric method properties
				Class<?> metricMethod = Class.forName(mt.get(i).getMetricClass());
				MetricCalculatorInterface vmc = (MetricCalculatorInterface)metricMethod.newInstance();
				this.metricCalculators.add(vmc);
				this.metricIds.add(mt.get(i).getID());
				if(vmc instanceof ClassificationQualityMetricCalculatorInterface) {
					if(mt.get(i).isPartitionLevelSelected()) {
						((ClassificationQualityMetricCalculatorInterface)vmc).setPartitionLevel(true);
					} 
					if(mt.get(i).isSongLevelSelected()) {
						((ClassificationQualityMetricCalculatorInterface)vmc).setSongLevel(true);
					}
				}
			}
		} catch(Exception e) {
			throw new NodeException("Configuration of metric method for validation failed: " + e.getMessage());
		}
		
		// Check if any metric calculators are loaded
		if(this.metricCalculators.size() == 0) {
			throw new NodeException("No metric method could be loaded for validation");
		}
	}
	
	/**
	 * Performs model evaluation
	 * @throws NodeException
	 */
	private void performEvaluation() throws NodeException {
		
		// List of all models to evaluate
		ArrayList<File> modelsToEvaluate = new ArrayList<File>();
		File modelFile = new File(pathToModelFile);
		
		// If a folder is given, search for all models
		if(modelFile.isDirectory()) {
			File[] files = modelFile.listFiles();
			
			// Go through all files in the given directory
			for(int i=0;i<files.length;i++) {
				if(files[i].isFile() && files[i].toString().endsWith(".mod")) {
					modelsToEvaluate.add(files[i]);
				}
			}
		}
		// If a single model file is given, load it to the list
		else {
			modelsToEvaluate.add(modelFile);
		}
		AmuseLogger.write(this.getClass().getName(), Level.INFO, modelsToEvaluate.size() + " model(s) will be evaluated");
		
		// Validation metrics are saved in a list (for each run)
		ArrayList<ArrayList<ValidationMetric>> metricsForEveryModel = new ArrayList<ArrayList<ValidationMetric>>();
		
		// Go through all models which should be evaluated
		for(int i=0;i<modelsToEvaluate.size();i++) { 
			
			// Classify the music input with the current model
			ArrayList<ClassifiedSongPartitionsDescription> predictedSongs = new ArrayList<ClassifiedSongPartitionsDescription>();
			ClassificationConfiguration cConf = null;
			cConf = new ClassificationConfiguration(
				((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getInputToValidate(),
				ClassificationConfiguration.InputSourceType.READY_INPUT,
				((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName(), 
				((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription(),
				0,
				this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + "/result.arff",
				modelsToEvaluate.get(i).getAbsolutePath());
				
			ClassifierNodeScheduler cs = new ClassifierNodeScheduler(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId());
			cs.setCleanInputFolder(false);
			cConf.setProcessedFeatureDatabase(((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeatureDatabase());
			predictedSongs = cs.proceedTask(this.correspondingScheduler.getHomeFolder(), this.correspondingScheduler.getTaskId(), cConf, false);
			
			// Calculate the classifier evaluation metrics for result
			try {
				ArrayList<ValidationMetric> metricsOfThisRun = new ArrayList<ValidationMetric>();
				for(int currentMetric = 0; currentMetric < this.metricCalculators.size(); currentMetric++) {
					ValidationMetric[] currMetr = null; 
					if(this.metricCalculators.get(currentMetric) instanceof ClassificationQualityMetricCalculatorInterface) {
						currMetr = ((ClassificationQualityMetricCalculatorInterface)this.metricCalculators.get(currentMetric)).calculateMetric(
								((ValidatorNodeScheduler)this.getCorrespondingScheduler()).getLabeledAverageSongRelationships(), predictedSongs);
					} else if(this.metricCalculators.get(currentMetric) instanceof DataReductionMetricCalculatorInterface) {
						currMetr = ((DataReductionMetricCalculatorInterface)this.metricCalculators.get(currentMetric)).calculateMetric(
								((ValidatorNodeScheduler)this.correspondingScheduler).getListOfAllProcessedFiles());
					} else {
						throw new NodeException("Unknown metric: " + this.metricCalculators.get(currentMetric));
					}
					for(int k=0;k<currMetr.length;k++) {
						metricsOfThisRun.add(currMetr[k]);
					}
				}
				metricsForEveryModel.add(metricsOfThisRun);
			} catch (NodeException e) {
				throw e;
			}
		}
		
		int numberOfDoubleMetrics = 0;
		for(int i=0;i<metricsForEveryModel.get(0).size();i++) {
			if(metricsForEveryModel.get(0).get(i) instanceof ValidationMetricDouble) {
				numberOfDoubleMetrics++;
			}
		}
		
		// For the mean metric values over all evaluated models TODO see below, currently not supported
		/*Double[] meanMetrics = new Double[numberOfDoubleMetrics];
		for(int i=0;i<meanMetrics.length;i++) 
			meanMetrics[i] = 0.0d;
		
		// Go through all metrics
		for(int i=0;i<metricsForEveryModel.get(0).size();i++) {
			
			// Mean metrics can be built only for double metrics
			if(metricsForEveryModel.get(0).get(i) instanceof ValidationMetricDouble) {
			
				// Go through all runs
				for(int j=0;j<metricsForEveryModel.size();j++) {
					meanMetrics[i] += ((ValidationMetricDouble)metricsForEveryModel.get(j).get(i)).getValue();
				}
				meanMetrics[i] /= metricsForEveryModel.size();
			}
		}*/
		
		// Save the metrics
		try {
			boolean saveHeader = false;
			
			// If no metric file is there, save header
			if(!new File(this.folderForMetrics + "/" + "metrics.arff").exists()) {
				saveHeader = true;
			}
			FileOutputStream values_to = new FileOutputStream(this.folderForMetrics + "/" + "metrics.arff",true);
			DataOutputStream values_writer = new DataOutputStream(values_to);
			String sep = System.getProperty("line.separator");
			
			// Saves the header
			if(saveHeader) {
				values_writer.writeBytes("@RELATION 'Classifier metrics'");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE Time STRING");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE MetricId NUMERIC");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE MetricName STRING");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@ATTRIBUTE MetricValue STRING");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@DATA");
			}
			values_writer.writeBytes(sep);
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			
			// Save the metric values, going through all metrics
			for(int i=0;i<metricsForEveryModel.get(0).size()/* TODO see below meanMetrics.length*/;i++) {
				
				// If only one model was evaluated, write it; if more models were evaluated,
				// save also the mean metric values across all models
				
				// TODO Currently only data of the 1st model is given (restriction for metric "correctly predicted song list" 
				// where no mean value can be calculated
				
				//  if(modelsToEvaluate.size() == 1) {
					values_writer.writeBytes("\"" + sdf.format(new Date()) + "\", " + 
							metricsForEveryModel.get(0).get(i).getId() + ", " + 
							"\"" + metricsForEveryModel.get(0).get(i).getName() + " for " + modelsToEvaluate.get(0).toString() + "\", " + 
							metricsForEveryModel.get(0).get(i).getValue());
					values_writer.writeBytes(sep);
				/*} else {
					for(int j=0;j<metricsForEveryModel.size();j++) {
						values_writer.writeBytes("\"" + sdf.format(new Date()) + "\", " + 
								metricsForEveryModel.get(0).get(i).getId() + ", " + 
								"\"" + metricsForEveryModel.get(0).get(i).getName() + " for " + modelsToEvaluate.get(j).toString() + "\", " + 
								metricsForEveryModel.get(j).get(i).getValue());
						values_writer.writeBytes(sep);
					}
					
					values_writer.writeBytes("\"" + sdf.format(new Date()) + "\", " + 
							metricsForEveryModel.get(0).get(i).getId() + ", " + 
							"\"mean(" + metricsForEveryModel.get(0).get(i).getName() + ")\", " + meanMetrics[i]);
					values_writer.writeBytes(sep);
				}*/
			}
			
			values_writer.close();
		} catch(IOException e) {
			e.printStackTrace();
			throw new NodeException("Could not save metrics: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required		
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString.startsWith("\"") || parameterString.startsWith("'")) {
			this.pathToModelFile = parameterString.substring(1,parameterString.length()-1);
		} else {
			this.pathToModelFile = parameterString;
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidatorInterface#calculateListOfUsedProcessedFeatureFiles()
	 */
	public ArrayList<String> calculateListOfUsedProcessedFeatureFiles() throws NodeException {
		
		// If the ground truth file is not available, return null
		if(((ValidatorNodeScheduler)this.correspondingScheduler).getGroundTruthFile() == null) {
			return null;
		}

		ArrayList<String> listOfUsedProcessedFeatureFiles = new ArrayList<String>();
		try {
		
			int categoryIdForTrainingSet = 0;
			
			// Get the id for training category
			String pathToModel = new String(this.pathToModelFile);
			pathToModel = pathToModel.substring(
					((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getModelDatabase().length()+1,pathToModel.length());
			pathToModel = pathToModel.substring(0,pathToModel.indexOf("-"));
			categoryIdForTrainingSet = new Integer(pathToModel);
			
			// Load the file list with training tracks
			String categoryForTraining = new String();
			ArffLoader categoryDescriptionLoader = new ArffLoader();
			Instance currentInstance;
			categoryDescriptionLoader.setFile(new File(AmusePreferences.get(KeysStringValue.CATEGORY_DATABASE)));
			Attribute idAttribute = categoryDescriptionLoader.getStructure().attribute("Id");
			Attribute fileNameAttribute = categoryDescriptionLoader.getStructure().attribute("Path");
			currentInstance = categoryDescriptionLoader.getNextInstance(categoryDescriptionLoader.getStructure());
			while(currentInstance != null) {
				if(categoryIdForTrainingSet == currentInstance.value(idAttribute)) {
					categoryForTraining = currentInstance.stringValue(fileNameAttribute);
					break;
				}
				currentInstance = categoryDescriptionLoader.getNextInstance(categoryDescriptionLoader.getStructure());
			}
			
			// Load the data about initially and finally used time windows from the processed feature files of training tracks
			ArffLoader inputDescriptionLoader = new ArffLoader();
			inputDescriptionLoader.setFile(new File(categoryForTraining));
			Attribute musicFileNameAttribute = inputDescriptionLoader.getStructure().attribute("Path");
			currentInstance = inputDescriptionLoader.getNextInstance(inputDescriptionLoader.getStructure());
			while(currentInstance != null) {
				
				String musicFile = currentInstance.stringValue(musicFileNameAttribute);
				if(musicFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
					musicFile = musicFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length(),musicFile.length());
				}
				String absoluteName = musicFile.substring(musicFile.lastIndexOf(File.separator)+1,musicFile.lastIndexOf("."));
				String pathToFile = musicFile.substring(0,musicFile.lastIndexOf(File.separator));
				musicFile = 
						((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeatureDatabase() + pathToFile + File.separator +
						absoluteName + File.separator + absoluteName + "_" + ((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName() +
						".arff";
				listOfUsedProcessedFeatureFiles.add(musicFile);
				currentInstance = inputDescriptionLoader.getNextInstance(inputDescriptionLoader.getStructure());
			}
			
			// Load the data about initially and finally used time windows from the processed feature files of test tracks
			inputDescriptionLoader = new ArffLoader();
			inputDescriptionLoader.setFile(((ValidatorNodeScheduler)this.correspondingScheduler).getGroundTruthFile());
			musicFileNameAttribute = inputDescriptionLoader.getStructure().attribute("Path");
			currentInstance = inputDescriptionLoader.getNextInstance(inputDescriptionLoader.getStructure());
			while(currentInstance != null) {
				
				String musicFile = currentInstance.stringValue(musicFileNameAttribute);
				if(musicFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
					musicFile = musicFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length(),musicFile.length());
				}
				String absoluteName = musicFile.substring(musicFile.lastIndexOf(File.separator)+1,musicFile.lastIndexOf("."));
				String pathToFile = musicFile.substring(0,musicFile.lastIndexOf(File.separator));
				musicFile = 
					((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeatureDatabase() + pathToFile + File.separator +
						absoluteName + File.separator + absoluteName + "_" + ((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName() +
						".arff";
				listOfUsedProcessedFeatureFiles.add(musicFile);
				currentInstance = inputDescriptionLoader.getNextInstance(inputDescriptionLoader.getStructure());
			}
		} catch(IOException e) {
			throw new NodeException(e.getMessage());
		}
		
		return listOfUsedProcessedFeatureFiles;
	}
	
	/*private String listCorrectSongs(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		amuse.nodes.validator.metrics.confusionmatrix.base.ListOfCorrectlyPredictedInstances mc = new amuse.nodes.validator.metrics.confusionmatrix.base.ListOfCorrectlyPredictedInstances();
		mc.setSongLevel(true);
		ValidationMetricDouble[] list = mc.calculateMetric(groundTruthRelationships, predictedRelationships);
		StringBuffer b = new StringBuffer();
		for(int i=0;i<list.length;i++) {
			b.append(list[i].getValue().intValue() + " ");
		}
		return b.toString().substring(0,b.length()-1);
	}*/
	
}


