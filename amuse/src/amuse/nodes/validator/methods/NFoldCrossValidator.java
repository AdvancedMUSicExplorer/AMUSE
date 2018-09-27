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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;
import amuse.data.GroundTruthSourceType;
import amuse.data.MeasureTable;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.ClassifierNodeScheduler;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.trainer.TrainerNodeScheduler;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.nodes.validator.ValidatorNodeScheduler;
import amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface;
import amuse.nodes.validator.interfaces.DataReductionMeasureCalculatorInterface;
import amuse.nodes.validator.interfaces.MeasureCalculatorInterface;
import amuse.nodes.validator.interfaces.ValidationMeasure;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;
import amuse.nodes.validator.interfaces.ValidatorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;

/**
 * Performs n-fold cross-validation
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class NFoldCrossValidator extends AmuseTask implements ValidatorInterface {
	
	/** Measure calculator used by this validator */
	private ArrayList<MeasureCalculatorInterface> measureCalculators = new ArrayList<MeasureCalculatorInterface>();
	
	/** Ids of measures to calculate */
	private ArrayList<Integer> measureIds = new ArrayList<Integer>();
	
	/** Number of partitions (standard value = 10 for 10-fold cross-validation) */
	private int n = 10;
	
	/** Random seed for partition allocation */
	private long randomSeed = -1;
	
	/** Random number generator */
	private Random random = null;
	
	private File folderForModels = null;
	
	public void validate() throws NodeException {
		
		// ---------------------------------------------
		// (I) Set up the folders for models and measures
		// ---------------------------------------------
		try {
			setFolders();
		} catch(NodeException e) {
			throw e;
		}
		
		// ---------------------------------
		// (II) Configure measure calculators
		// ---------------------------------
		try {
			configureMeasureCalculators();
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
	 * Sets up the folders for models and measures
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
				classificationAlgorithmLoader.setFile(new File(AmusePreferences.getClassifierAlgorithmTablePath()));
	    	} else {
	    		classificationAlgorithmLoader.setFile(new File(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + "classifierAlgorithmTable.arff"));
	    	}
			Attribute idAttribute = classificationAlgorithmLoader.getStructure().attribute("Id");
			Attribute nameAttribute = classificationAlgorithmLoader.getStructure().attribute("Name");
			classificationAlgorithmInstance = classificationAlgorithmLoader.getNextInstance(classificationAlgorithmLoader.getStructure());
			while(classificationAlgorithmInstance != null) {
				
				// If the given classification algorithm is found..
				if(classificationAlgorithmInstance.value(idAttribute) == algorithmToSearch) {
					classificationMethodFound = true;
					
					// Set the name of folder for models and measures (combined from
					// classifier ID, parameters and name)
					String classifierDescription = ((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription() + 
					"-" + classificationAlgorithmInstance.stringValue(nameAttribute);
					
					
					// Check if the folder for model file(s) exists; if not create it
					this.folderForModels = new File( 
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getModelDatabase()
							+ File.separator + ((ValidatorNodeScheduler)this.correspondingScheduler).getCategoryDescription() + 
							File.separator + classifierDescription + File.separator + 
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName() + File.separator + 
							((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getValidationAlgorithmDescription() +
							"-" + properties.getProperty("name"));
					if(!this.folderForModels.exists()) {
						if(!this.folderForModels.mkdirs()) {
							throw new NodeException("Could not create the folder for classification models: " + this.folderForModels);
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
	 * Configures measure calculators
	 * @throws NodeException
	 */
	private void configureMeasureCalculators() throws NodeException {
		
		// TODO Support measure calculators which use some parameters (like F-Measure) -> similar to algorithms 
		try {
			MeasureTable mt = ((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getMeasures();
			for(int i=0;i<mt.size();i++) {
				
				// Set measure method properties
				Class<?> measureMethod = Class.forName(mt.get(i).getMeasureClass());
				MeasureCalculatorInterface vmc = (MeasureCalculatorInterface)measureMethod.newInstance();
				this.measureCalculators.add(vmc);
				this.measureIds.add(mt.get(i).getID());
				if(vmc instanceof ClassificationQualityMeasureCalculatorInterface) {
					if(mt.get(i).isPartitionLevelSelected()) {
						((ClassificationQualityMeasureCalculatorInterface)vmc).setPartitionLevel(true);
					} 
					if(mt.get(i).isSongLevelSelected()) {
						((ClassificationQualityMeasureCalculatorInterface)vmc).setSongLevel(true);
					}
				}
			}
		} catch(Exception e) {
			throw new NodeException("Configuration of measure method for validation failed: " + e.getMessage());
		}
		
		// Check if any measure calculators are loaded
		if(this.measureCalculators.size() == 0) {
			throw new NodeException("No measure method could be loaded for validation");
		}
	}
	
	/**
	 * Performs cross-validation
	 * @throws NodeException
	 */
	private void performCrossValidation() throws NodeException {
		
		// Set with all data instances
		DataSet allPartitions = ((DataSetInput)((ValidationConfiguration)this.getCorrespondingScheduler().getConfiguration()).
				getInputToValidate()).getDataSet();
		
		// Create n song partitions with the equal partition size
		int partitionSize = ((ValidatorNodeScheduler)this.correspondingScheduler).getLabeledSongRelationships().size() / this.n;
		HashMap<Integer,Integer> songIdToSongNumber = new HashMap<Integer,Integer>();
		
		// Shuffle the song sequence for partition building
		ArrayList<Integer> shuffledSongIdsForCrossValidation = new ArrayList<Integer>(((ValidatorNodeScheduler)this.correspondingScheduler).
				getLabeledSongRelationships().size());
		for(int i=0;i<((ValidatorNodeScheduler)this.correspondingScheduler).getLabeledSongRelationships().size();i++) {
			shuffledSongIdsForCrossValidation.add(((ValidatorNodeScheduler)this.correspondingScheduler).getLabeledSongRelationships().get(i).getSongId());
			songIdToSongNumber.put(((ValidatorNodeScheduler)this.correspondingScheduler).getLabeledSongRelationships().get(i).getSongId(), i);
		}
		Collections.shuffle(shuffledSongIdsForCrossValidation, random);
		
		// Maps song id to the validation partition after the random shuffle
		HashMap<Integer,Integer> songIdToValidationPartition = new HashMap<Integer,Integer>();
		int nextBoundary = partitionSize;
		int partitionNumber = 0;
		for(int i=0;i<partitionSize*this.n;i++) {
			if(i >= nextBoundary) {
				nextBoundary += partitionSize;
				partitionNumber++;
			}
			songIdToValidationPartition.put(shuffledSongIdsForCrossValidation.get(i), partitionNumber);
		}
		
		// Validation measures are saved in a list (for each run)
		ArrayList<ArrayList<ValidationMeasure>> measuresOfEveryValidationRun = new ArrayList<ArrayList<ValidationMeasure>>();
		
		// Go through all validation runs (equal to partition number), using the current partition as test partition each time
		for(int i=0;i<this.n;i++) { 
			
			// Create the sets with the data of the current partition for training and validation
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
			
			// Ground truth for the validation set
			ArrayList<Double> songRelationshipsValidationSet = new ArrayList<Double>(); // If binary classification is applied
			ArrayList<ClassifiedSongPartitions> songRelationshipsMValidationSet = new ArrayList<ClassifiedSongPartitions>(); // If multiclass classification is applied
			int currentSongId = -1;
			
			for(int j=0;j<allPartitions.getValueCount();j++) {
				
				// To which validation partition should the current song partition be assigned?
				int songIdToSearchFor = new Double(allPartitions.getAttribute("Id").getValueAt(j).toString()).intValue();
				
				// Training or validation set? Go through all song partitions assigned to cv
				if(songIdToValidationPartition.containsKey(songIdToSearchFor)) {
					if(songIdToValidationPartition.get(songIdToSearchFor) == i) {
						
						// Add the partition to validation set
						for(int a = 0; a < allPartitions.getAttributeCount(); a++) {
							validationSet.getAttribute(a).addValue(allPartitions.getAttribute(a).getValueAt(j));
						}
						
						// Save the ground truth for the validation set
						// TODO It is assumed that partitions of the same song are coming all together one after each other in the DataSet
						// - if the ID is changed to the next song, the ground truth of all partitions is then loaded
						if(currentSongId != songIdToSearchFor) {
							currentSongId = songIdToSearchFor;
							if(!((ValidatorNodeScheduler)this.getCorrespondingScheduler()).isMulticlass()) {
								songRelationshipsValidationSet.add(((ValidatorNodeScheduler)this.correspondingScheduler).
									getLabeledAverageSongRelationships().get(songIdToSongNumber.get(songIdToSearchFor)));
							} else {
								songRelationshipsMValidationSet.add(((ValidatorNodeScheduler)this.correspondingScheduler).
									getLabeledSongRelationships().get(songIdToSongNumber.get(songIdToSearchFor)));
							}
						}
					
					} else {
						
						// Add the partition to training set
						for(int a = 0; a < allPartitions.getAttributeCount(); a++) {
							trainingSet.getAttribute(a).addValue(allPartitions.getAttribute(a).getValueAt(j));
						}
					}
				}
			}
			
			// Train the model
			// TODO Classification preprocessing is not currently supported!
			TrainingConfiguration tConf = new TrainingConfiguration(
				((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName(), 
				((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription(),
				"-1",
				new DataSetInput(trainingSet),
				GroundTruthSourceType.READY_INPUT,
				this.folderForModels + File.separator + "model_" + i + ".mod");
			TrainerNodeScheduler ts = new TrainerNodeScheduler(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId());
			ts.setCleanInputFolder(false);
			ts.proceedTask(this.correspondingScheduler.getHomeFolder(), this.correspondingScheduler.getTaskId(), tConf);
			
			// Classify the validation set
			ClassificationConfiguration cConf = new ClassificationConfiguration(
				new DataSetInput(validationSet),
				ClassificationConfiguration.InputSourceType.READY_INPUT,
				((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getProcessedFeaturesModelName(), 
				((ValidationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationAlgorithmDescription(),
				new Integer(((ValidatorNodeScheduler)this.correspondingScheduler).getCategoryDescription().substring(0,
						((ValidatorNodeScheduler)this.correspondingScheduler).getCategoryDescription().indexOf("-"))),this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + "result.arff");
			cConf.setPathToInputModel(this.folderForModels + File.separator + "model_" + i + ".mod");
			ClassifierNodeScheduler cs = new ClassifierNodeScheduler(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId());
			cs.setCleanInputFolder(false);
			ArrayList<ClassifiedSongPartitions> predictedSongs = cs.proceedTask(this.correspondingScheduler.getHomeFolder(), this.correspondingScheduler.getTaskId(), cConf, false);
			
			// Calculate the classifier evaluation measures for result
			try {
				ArrayList<ValidationMeasure> measuresOfThisRun = new ArrayList<ValidationMeasure>();
				for(int currentMeasure = 0; currentMeasure < this.measureCalculators.size(); currentMeasure++) {
					ValidationMeasure[] currMeas = null;
					if(this.measureCalculators.get(currentMeasure) instanceof ClassificationQualityMeasureCalculatorInterface) {
						if(!((ValidatorNodeScheduler)this.getCorrespondingScheduler()).isMulticlass()) {
							currMeas = ((ClassificationQualityMeasureCalculatorInterface)this.measureCalculators.get(currentMeasure)).calculateOneClassMeasure(
								songRelationshipsValidationSet, predictedSongs);
						} else {
							currMeas = ((ClassificationQualityMeasureCalculatorInterface)this.measureCalculators.get(currentMeasure)).calculateMultiClassMeasure(
								songRelationshipsMValidationSet, predictedSongs);
						}
					} else if(this.measureCalculators.get(currentMeasure) instanceof DataReductionMeasureCalculatorInterface) {
						currMeas = ((DataReductionMeasureCalculatorInterface)this.measureCalculators.get(currentMeasure)).calculateMeasure(
								((ValidatorNodeScheduler)this.correspondingScheduler).getListOfAllProcessedFiles());
					} else {
						throw new NodeException("Unknown measure: " + this.measureCalculators.get(currentMeasure));
					}
					
					for(int k=0;k<currMeas.length;k++) {
						measuresOfThisRun.add(currMeas[k]);
					}
				}
				measuresOfEveryValidationRun.add(measuresOfThisRun);
			} catch (NodeException e) {
				throw e;
			}
		}
		
		// Calculate the number of double measures
		int numberOfDoubleMeasures = 0;
		for(int i=0;i<measuresOfEveryValidationRun.get(0).size();i++) {
			if(measuresOfEveryValidationRun.get(0).get(i) instanceof ValidationMeasureDouble) {
				numberOfDoubleMeasures++;
			}
		}
		
		// Calculate the mean measure values only for double measures over all validation runs
		Double[] meanMeasures = new Double[numberOfDoubleMeasures];
		for(int i=0;i<meanMeasures.length;i++) 
			meanMeasures[i] = 0.0d;
		int currentIndexOfMeanMeasure = 0;
		for(int i=0;i<measuresOfEveryValidationRun.get(0).size();i++) {
			if(measuresOfEveryValidationRun.get(0).get(i) instanceof ValidationMeasureDouble) {
			
				// Go through all runs
				for(int j=0;j<measuresOfEveryValidationRun.size();j++) {
					meanMeasures[currentIndexOfMeanMeasure] += ((ValidationMeasureDouble)
							measuresOfEveryValidationRun.get(j).get(i)).getValue();
				}
				meanMeasures[currentIndexOfMeanMeasure] /= measuresOfEveryValidationRun.size();
				currentIndexOfMeanMeasure++;
			}
		}
		
		// Save the measure values to the list, going through all measures
		try {
			ArrayList<ValidationMeasure> measureList = new ArrayList<ValidationMeasure>();
			currentIndexOfMeanMeasure = 0;
			for(int i=0;i<measuresOfEveryValidationRun.get(0).size();i++) {
			
				for(int j=0;j<measuresOfEveryValidationRun.size();j++) {
					Class<?> measureClass = Class.forName(measuresOfEveryValidationRun.get(0).get(i).getClass().getCanonicalName());
					ValidationMeasure m = (ValidationMeasure)measureClass.newInstance();
					m.setValue(measuresOfEveryValidationRun.get(j).get(i).getValue());
					m.setName("run_" + j + "_(" + measuresOfEveryValidationRun.get(0).get(i).getName() + ")");
					m.setId(measuresOfEveryValidationRun.get(0).get(i).getId());
					if(m instanceof ValidationMeasureDouble) {
						((ValidationMeasureDouble)m).setForMinimizing(((ValidationMeasureDouble)
								measuresOfEveryValidationRun.get(0).get(i)).isForMinimizing());
					}
					measureList.add(m);
				}
					
				// Add the mean measure value over all validation runs for double measures
				Class<?> measureClass = Class.forName(measuresOfEveryValidationRun.get(0).get(i).getClass().getCanonicalName());
				ValidationMeasure m = (ValidationMeasure)measureClass.newInstance();
				if(m instanceof ValidationMeasureDouble) {
					m.setValue(meanMeasures[currentIndexOfMeanMeasure]);
					m.setName("mean(" + measuresOfEveryValidationRun.get(0).get(i).getName() + ")");
					m.setId(measuresOfEveryValidationRun.get(0).get(i).getId());
					((ValidationMeasureDouble)m).setForMinimizing(((ValidationMeasureDouble)measuresOfEveryValidationRun.
							get(0).get(i)).isForMinimizing());
					measureList.add(m);
					currentIndexOfMeanMeasure++;
				}
			}
			((ValidationConfiguration)this.getCorrespondingScheduler().getConfiguration()).setCalculatedMeasures(measureList);
		} catch(ClassNotFoundException e) {
			throw new NodeException("Could not find the appropriate measure class: " + e.getMessage());
		} catch(IllegalAccessException e) {
			throw new NodeException("Could not access the appropriate measure class: " + e.getMessage());
		} catch(InstantiationException e) {
			throw new NodeException("Could not instantiate the appropriate measure class: " + e.getMessage());
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


