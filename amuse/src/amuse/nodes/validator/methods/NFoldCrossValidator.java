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
import java.util.HashMap;
import java.util.Random;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;
import amuse.data.MetricTable;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.ClassifierNodeScheduler;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitionsDescription;
import amuse.nodes.trainer.TrainerNodeScheduler;
import amuse.nodes.trainer.TrainingConfiguration;
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

/**
 * Performs n-fold cross-validation
 * 
 * @author Igor Vatolkin
 * @version $Id: NFoldCrossValidator.java 1253 2010-08-03 14:05:51Z vatolkin $
 */
public class NFoldCrossValidator extends AmuseTask implements ValidatorInterface {
	
	/** Metric calculator used by this validator */
	private ArrayList<MetricCalculatorInterface> metricCalculators = new ArrayList<MetricCalculatorInterface>();
	
	/** Ids of metrics to calculate */
	private ArrayList<Integer> metricIds = new ArrayList<Integer>();
	
	/** Number of partitions (standard value = 10 for 10-fold cross-validation) */
	private int n = 10;
	
	/** Random seed for partition allocation */
	private long randomSeed = -1;
	
	/** Random number generator */
	private Random random = null;
	
	private File folderForModels = null;
	private File folderForMetrics = null;
	
	public void validate() throws NodeException {
		
		// ---------------------------------------------
		// (I) Set up the folders for models and metrics
		// ---------------------------------------------
		try {
			setFolders();
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
		
		
		// ------------------------------
		// (III) Perform cross-validation
		// ------------------------------
		try {
			performCrossValidation();
		} catch(NodeException e) {
			throw e;
		}
		
	}
	
	/**
	 * Sets up the folders for models and metrics
	 * @throws NodeException
	 */
	private void setFolders() throws NodeException {
		ArffLoader classificationAlgorithmLoader = new ArffLoader();
		Instance classificationAlgorithmInstance;
		int algorithmToSearch;
		boolean classificationMethodFound = false;
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
					String classifierDescription = ((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription() + 
					"-" + classificationAlgorithmInstance.stringValue(nameAttribute);
					
					
					// Check if the folder for model file(s) exists; if not create it
					this.folderForModels = new File( 
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getModelDatabase()
							+ "/" + ((ValidatorNodeScheduler)this.correspondingScheduler).getCategoryDescription() + 
							"/" + classifierDescription + "/" + 
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName() + "/" + 
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getValidationAlgorithmDescription() +
							"-" + properties.getProperty("name"));
					if(!this.folderForModels.exists()) {
						if(!this.folderForModels.mkdirs()) {
							throw new NodeException("Could not create the folder for classification models: " + this.folderForModels);
						}
					}
					
					// Check if the folder for metric file exists; if not create it
					this.folderForMetrics = new File( 
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getMetricDatabase()
							+ "/" + ((ValidatorNodeScheduler)this.correspondingScheduler).getCategoryDescription() + 
							"/" + classifierDescription + "/" + 
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName() + "/" + 
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getValidationAlgorithmDescription() + 
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
			
		} catch(IOException e) {
			throw new NodeException("Could not create the folder for classification models: " + e.getMessage());
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
	 * Performs cross-validation
	 * @throws NodeException
	 */
	private void performCrossValidation() throws NodeException {
		// Create ten partitions
		// For partitions
		ArrayList<Integer[]> partitions= new ArrayList<Integer[]>();
		// Songs are distributed randomly to partitions
		ArrayList<Integer> partitionsToDistribute = new ArrayList<Integer>();
		for(int i=0;i<((ValidatorNodeScheduler)this.correspondingScheduler).getLabeledSongRelationships().size();i++) {
			partitionsToDistribute.add(i);
		}

		// Create a HashMap which maps song ids to the corresponding partition for faster search later
		HashMap<Integer,Integer> songIdToPartition = new HashMap<Integer,Integer>();
		HashMap<Integer,Integer> songIdToSongNumber = new HashMap<Integer,Integer>();
		
		// Create n partitions with the equal partition size 
		int partitionSize = ((ValidatorNodeScheduler)this.correspondingScheduler).getLabeledSongRelationships().size() / this.n;
		for(int i=0;i<this.n;i++) {
			Integer[] currentPartition = new Integer[partitionSize];
			for(int j=0;j<partitionSize;j++) {
				
				// If the random seed is set to -1, the first n songs build the first partition, the next n songs the second etc.
				int songPosition = 0; 
				if(randomSeed >= 0) {
					songPosition = random.nextInt(partitionsToDistribute.size());
				}
				int songNumber = partitionsToDistribute.get(songPosition);
				partitionsToDistribute.remove(songPosition);
				Integer idOfCurrentSong = ((ValidatorNodeScheduler)this.correspondingScheduler).getLabeledSongRelationships().get(songNumber).getSongId();
				currentPartition[j] = idOfCurrentSong;
				songIdToPartition.put(idOfCurrentSong, i);
				songIdToSongNumber.put(idOfCurrentSong,songNumber);
			}
			partitions.add(currentPartition);
		}
		
		// Validation metrics are saved in a list (for each run)
		ArrayList<ArrayList<ValidationMetricDouble>> metricsOfEveryValidationRun = new ArrayList<ArrayList<ValidationMetricDouble>>();
		
		// Go through all validation runs (equal to partition number), using the current partition as test partition each time
		for(int i=0;i<this.n;i++) { 
			
			// Create the sets with the data of the current partition for training and validation
			DataSet allPartitions = ((DataSetInput)((ValidationConfiguration)this.getCorrespondingScheduler().getConfiguration()).
					getInputToValidate()).getDataSet();
			DataSet trainingSet = new DataSet("TrainingSet");
			DataSet validationSet = new DataSet("ValidationSet");
			
			// Create the attributes for these sets
			for(int a = 0; a < allPartitions.getAttributeCount(); a++) {
				if(allPartitions.getAttribute(a) instanceof NumericAttribute) {
					trainingSet.addAttribute(new NumericAttribute(allPartitions.getAttribute(a).getName(),new ArrayList<Double>()));
					validationSet.addAttribute(new NumericAttribute(allPartitions.getAttribute(a).getName(),new ArrayList<Double>()));
				} else if(allPartitions.getAttribute(a) instanceof StringAttribute) {
					trainingSet.addAttribute(new StringAttribute(allPartitions.getAttribute(a).getName(),new ArrayList<String>()));
					validationSet.addAttribute(new StringAttribute(allPartitions.getAttribute(a).getName(),new ArrayList<String>()));
				} else {
					trainingSet.addAttribute(new NominalAttribute(allPartitions.getAttribute(a).getName(),new ArrayList<String>()));
					validationSet.addAttribute(new NominalAttribute(allPartitions.getAttribute(a).getName(),new ArrayList<String>()));
				}
			}
			
			// Save the ground truth for the validation set
			ArrayList<Double> songRelationshipsValidationSet = new ArrayList<Double>();
			for(int v=0;v<partitionSize;v++) {
				int currentSong = partitions.get(i)[v];
				int numberOfThisSong = songIdToSongNumber.get(currentSong);
				songRelationshipsValidationSet.add(((ValidatorNodeScheduler)this.correspondingScheduler).getLabeledAverageSongRelationships().get(numberOfThisSong));
			}
			
			// Fill the validation set (with songs which belong to current partition)
			for(int j=0;j<partitions.get(i).length;j++) {
				int currentSongIdToSearch = partitions.get(i)[j];
				
				// Search for partitions which belong to this song going through all given partitions
				for(int k=0;k<allPartitions.getValueCount();k++) {
					int currentSongId = new Double(allPartitions.getAttribute("Id").getValueAt(k).toString()).intValue();
					
					// Is it found?
					if(currentSongId == currentSongIdToSearch) {
						
						// Go through all attributes and add the values of this partition
						for(int a = 0; a < allPartitions.getAttributeCount(); a++) {
							validationSet.getAttribute(a).addValue(allPartitions.getAttribute(a).getValueAt(k));
						}
					}
				}
			}
			
			// Fill the training set (with songs which does not belong to current partition)
			for(int p=0;p<this.n;p++) {
				
				if(p==i) continue;
				
				for(int j=0;j<partitions.get(p).length;j++) {
					int currentSongIdToSearch = partitions.get(p)[j];
					
					// Search for partitions which belong to this song going through all given partitions
					for(int k=0;k<allPartitions.getValueCount();k++) {
						int currentSongId = new Double(allPartitions.getAttribute("Id").getValueAt(k).toString()).intValue();
						
						// Is it found?
						if(currentSongId == currentSongIdToSearch) {
							
							// Go through all attributes and add the values of this partition
							for(int a = 0; a < allPartitions.getAttributeCount(); a++) {
								trainingSet.getAttribute(a).addValue(allPartitions.getAttribute(a).getValueAt(k));
							}
						}
					}
				}
			}
			
			// Train the model
			// FIXME Classification preprocessing is not currently supported!
			TrainingConfiguration tConf = new TrainingConfiguration(
					((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName(), 
					((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription(),
					"-1",
					new DataSetInput(trainingSet),
					TrainingConfiguration.GroundTruthSourceType.READY_INPUT, 
					this.folderForModels + "/model_" + i + ".mod");
			TrainerNodeScheduler ts = new TrainerNodeScheduler(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId());
			ts.setCleanInputFolder(false);
			ts.proceedTask(this.correspondingScheduler.getHomeFolder(), this.correspondingScheduler.getTaskId(), tConf);
			
			// Classify the validation set
			ClassificationConfiguration cConf = new ClassificationConfiguration(
					new DataSetInput(validationSet),
					ClassificationConfiguration.InputSourceType.READY_INPUT,
					((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName(), 
					((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription(),
					new Integer(((ValidatorNodeScheduler)this.correspondingScheduler).getCategoryDescription().substring(0,
							((ValidatorNodeScheduler)this.correspondingScheduler).getCategoryDescription().indexOf("-"))),this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + "/result.arff",
					this.folderForModels + "/model_" + i + ".mod");
			ClassifierNodeScheduler cs = new ClassifierNodeScheduler(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId());
			cs.setCleanInputFolder(false);
			ArrayList<ClassifiedSongPartitionsDescription> predictedSongs = cs.proceedTask(this.correspondingScheduler.getHomeFolder(), this.correspondingScheduler.getTaskId(), cConf, false);
			
			// Calculate the classifier evaluation metrics for result
			try {
				ArrayList<ValidationMetricDouble> metricsOfThisRun = new ArrayList<ValidationMetricDouble>();
				for(int currentMetric = 0; currentMetric < this.metricCalculators.size(); currentMetric++) {
					ValidationMetric[] currMetr = null;
					if(this.metricCalculators.get(currentMetric) instanceof ClassificationQualityMetricCalculatorInterface) {
						currMetr = ((ClassificationQualityMetricCalculatorInterface)this.metricCalculators.get(currentMetric)).calculateMetric(
								songRelationshipsValidationSet, predictedSongs);
					} else if(this.metricCalculators.get(currentMetric) instanceof DataReductionMetricCalculatorInterface) {
						currMetr = ((DataReductionMetricCalculatorInterface)this.metricCalculators.get(currentMetric)).calculateMetric(
								((ValidatorNodeScheduler)this.correspondingScheduler).getListOfAllProcessedFiles());
					} else {
						throw new NodeException("Unknown metric: " + this.metricCalculators.get(currentMetric));
					}
					
					// Mean statistics can be done only for ValidationMetricDouble!
					if(currMetr instanceof ValidationMetricDouble[]) {
						for(int k=0;k<currMetr.length;k++) {
							metricsOfThisRun.add((ValidationMetricDouble)currMetr[k]);
						}
					}
				}
				metricsOfEveryValidationRun.add(metricsOfThisRun);
			} catch (NodeException e) {
				throw e;
			}
		}
		
		// For the mean metric values over all validation runs
		Double[] meanMetrics = new Double[metricsOfEveryValidationRun.get(0).size()];
		for(int i=0;i<meanMetrics.length;i++) 
			meanMetrics[i] = 0.0d;
		
		// Go through all metrics
		for(int i=0;i<metricsOfEveryValidationRun.get(0).size();i++) {
			
			// Go through all runs
			for(int j=0;j<metricsOfEveryValidationRun.size();j++) {
				meanMetrics[i] += metricsOfEveryValidationRun.get(j).get(i).getValue();
			}
			meanMetrics[i] /= metricsOfEveryValidationRun.size();
		}
		
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
				values_writer.writeBytes("@ATTRIBUTE MetricValue NUMERIC");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("@DATA");
			}
			
			values_writer.writeBytes(sep);
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			
			// Save the metric values, going through all metrics
			for(int i=0;i<meanMetrics.length;i++) {
				for(int j=0;j<metricsOfEveryValidationRun.size();j++) {
					values_writer.writeBytes("\"" + sdf.format(new Date()) + "\", " + 
							metricsOfEveryValidationRun.get(0).get(i).getId() + ", " + 
							"\"run_" + j + "_(" + metricsOfEveryValidationRun.get(0).get(i).getName() + ")\", " + 
							metricsOfEveryValidationRun.get(j).get(i).getValue());
					values_writer.writeBytes(sep);
				}
				
				values_writer.writeBytes("\"" + sdf.format(new Date()) + "\", " + 
						metricsOfEveryValidationRun.get(0).get(i).getId() + ", " + 
						"\"mean(" + metricsOfEveryValidationRun.get(0).get(i).getName() + ")\", " + meanMetrics[i]);
				values_writer.writeBytes(sep);
			}
			values_writer.close();
		} catch(IOException e) {
			throw new NodeException("Could not save metrics: " + e.getMessage());
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
			// Load the data about initially and finally used time windows from the processed feature files of training and test tracks
			ArffLoader inputDescriptionLoader = new ArffLoader();
			inputDescriptionLoader.setFile(((ValidatorNodeScheduler)this.correspondingScheduler).getGroundTruthFile());
			Attribute musicFileNameAttribute = inputDescriptionLoader.getStructure().attribute("Path");
			Instance currentInstance = inputDescriptionLoader.getNextInstance(inputDescriptionLoader.getStructure());
			while(currentInstance != null) {
				
				String musicFile = currentInstance.stringValue(musicFileNameAttribute);
				if(musicFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
					musicFile = musicFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length(),musicFile.length());
				}
				String absoluteName = musicFile.substring(musicFile.lastIndexOf("/")+1,musicFile.lastIndexOf("."));
				String pathToFile = musicFile.substring(0,musicFile.lastIndexOf("/"));
				musicFile = 
					((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeatureDatabase() + pathToFile + "/" + 
						absoluteName + "/" + absoluteName + "_" + ((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName() + 
						".arff";
				listOfUsedProcessedFeatureFiles.add(musicFile);
				currentInstance = inputDescriptionLoader.getNextInstance(inputDescriptionLoader.getStructure());
			}
		} catch(IOException e) {
			throw new NodeException(e.getMessage());
		}
		
		return listOfUsedProcessedFeatureFiles;
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
		if(parameterString != null && parameterString != "") {
			this.n = new Integer(parameterString.substring(0,parameterString.indexOf("_")));
			this.randomSeed = new Long(parameterString.substring(parameterString.indexOf("_")+1,parameterString.length()));
		}
		
		// Setup the random operator: if the given seed is equal to zero, it is set randomly; if greater, the given seed is used
		if(this.randomSeed == 0) {
			random = new Random();
		} else if(this.randomSeed > 0) {
			random = new Random(randomSeed);
		}
		
		if(this.n <= 2) {
			throw new NodeException("For n-fold cross-validation n must be greater than 2!");
		}
	}
}


