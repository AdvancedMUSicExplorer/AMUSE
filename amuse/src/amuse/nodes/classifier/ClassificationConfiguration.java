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

import amuse.data.ClassificationType;
import amuse.data.GroundTruthSourceType;
import amuse.data.datasets.ClassifierConfigSet;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataInputInterface;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.FileListInput;
import amuse.interfaces.nodes.TaskConfiguration;
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
	public enum InputSourceType {FILE_LIST, READY_INPUT};
	
	/** Description of the processed features model */
	private final String processedFeaturesModelName;
	
	/** Id of classification algorithm from classifierTable.arff 
	 * (optionally with parameters listed in brackets) */
	private final String algorithmDescription;
	
	/** Id of the groundtrhuth category. Is not used if the concrete model path is known.*/
	private final int groundTruthCategoryId;
	
	/** categories of the groundtruth that were used for training. Is not used if the concrete model path is known.*/
	private final List<Integer> attributesToClassify;
	
	/** the type of classifcation that is performed */
	private final ClassificationType classificationType;
	
	/** is the classification fuzzy?*/
	private final boolean fuzzy;
	
	/** Flag if song relationship grade should be averaged over all partitions (="1") */
	private final Integer mergeSongResults;
	
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
	

	/**
	 * Standard constructor
	 * @param inputToClassify Input to classify
	 * @param inputSourceType Defines the input source type
	 * @param attributesToIgnore features of the processed feature files or the ready input that should not be used for the classification
	 * @param processedFeaturesModelName Description of the processed features model
	 * @param algorithmDescription Id and parameters of the classification algorithm from classifierTable.arff
	 * @param attributesToClassify the categories of the category file of the annotation database
	 * @param classificationType is the classification unsupervised, binary, multilabel or multiclass?
	 * @param fuzzy should the classification be fuzzy?
	 * @param mergeSongResults Flag if song relationship grade should be averaged over all partitions (="1")
	 * @param classificationOutput Destination for classification output
	 */
	public ClassificationConfiguration(
			DataInputInterface inputToClassify,
			InputSourceType inputSourceType,
			List <Integer> attributesToIgnore,
			String processedFeaturesModelName,
			String algorithmDescription,
			List<Integer> attributesToClassify,
			ClassificationType classificationType,
			boolean fuzzy,
			Integer mergeSongResults,
			String classificationOutput) {
		
		this.inputToClassify = inputToClassify;
		this.inputSourceType = inputSourceType;
		this.processedFeaturesModelName = processedFeaturesModelName;
		this.algorithmDescription = algorithmDescription;
		this.attributesToClassify = attributesToClassify;
		this.attributesToIgnore = attributesToIgnore;
		this.classificationType = classificationType;
		this.fuzzy = fuzzy;
		this.mergeSongResults = mergeSongResults;
		this.classificationOutput = classificationOutput;
		this.groundTruthCategoryId = -1;
	}
	
	/**
	 * Alternative constructor if the song list to classify is loaded by the category id
	 * @param inputSourceType Defines the input source type
	 * @param attributesToIgnore features of the processed feature files or the ready input that should not be used for the classification
	 * @param pathToInputSource Input for classification
	 * @param processedFeaturesModelName Description of the processed features model
	 * @param algorithmDescription Id and parameters of the classification algorithm from classifierTable.arff
	 * @param groundTruthSource Id of the music category
	 * @param attributesToClassify the categories of the category file of the annotation database
	 * @param classificationType is the classification unsupervised, binary, multilabel or multiclass?
	 * @param fuzzy should the classification be fuzzy?
	 * @param mergeSongResults Flag if song relationship grade should be averaged over all partitions (="1")
	 * @param classificationOutput Destination for classification output
	 */
	public ClassificationConfiguration(
			InputSourceType inputSourceType,
			String pathToInputSource,
			List <Integer> attributesToIgnore,
			String processedFeaturesModelName,
			String algorithmDescription,
			int groundTruthSource,
			List<Integer> attributesToClassify,
			ClassificationType classificationType,
			boolean fuzzy,
			Integer mergeSongResults,
			String classificationOutput,
			String pathToInputModel,
			String trainingDescription) {
		List<File> input;
		List<Integer> ids = null;
		if(inputSourceType.equals(InputSourceType.FILE_LIST)) {
			DataSetAbstract inputFileSet; 
			try {
				inputFileSet = new ArffDataSet(new File(pathToInputSource));
			} catch(IOException e) {
				throw new RuntimeException("Could not create ClassificationConfiguration: " + e.getMessage());
			}
			ids = new ArrayList<Integer>(inputFileSet.getValueCount());
			input = new ArrayList<File>(inputFileSet.getValueCount());
			for(int j=0;j<inputFileSet.getValueCount();j++) {
				ids.add(new Double(inputFileSet.getAttribute("Id").getValueAt(j).toString()).intValue());
				input.add(new File(inputFileSet.getAttribute("Path").getValueAt(j).toString()));
			}
			
		} else {
			input = new ArrayList<File>(1);
			input.add(new File(pathToInputSource));
		}
		this.inputSourceType = inputSourceType;
		this.inputToClassify = new FileListInput(input,ids);
		this.processedFeaturesModelName = processedFeaturesModelName;
		this.algorithmDescription = algorithmDescription;
		this.groundTruthCategoryId = groundTruthSource;
		this.attributesToClassify = attributesToClassify;
		this.attributesToIgnore = attributesToIgnore;
		this.classificationType = classificationType;
		this.fuzzy = fuzzy;
		this.mergeSongResults = mergeSongResults;
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
			String currentInputFileList = classifierConfig.getInputFileListAttribute().getValueAt(i).toString();
			String currentProcessedFeaturesDescription = classifierConfig.getProcessedFeatureDescriptionAttribute().getValueAt(i).toString();
			String currentAlgorithmDescription = classifierConfig.getClassificationAlgorithmIdAttribute().getValueAt(i).toString();
			int currentGroundTruthSource = classifierConfig.getGroundTruthSourceAttribute().getValueAt(i).intValue();

			String attributesToClassifyString = classifierConfig.getAttributesToClassifyAttribute().getValueAt(i).toString();
			attributesToClassifyString = attributesToClassifyString.replaceAll("\\[", "").replaceAll("\\]", "");
			String[] attributesToClassifyStringArray = attributesToClassifyString.split("\\s*,\\s*");
			List<Integer> currentAttributesToClassify = new ArrayList<Integer>();
			try {
				for(String str : attributesToClassifyStringArray) {
					if(!str.equals("")) {
						currentAttributesToClassify.add(Integer.parseInt(str));
					}
				}
			} catch(NumberFormatException e) {
				throw new IOException("The categories for classification were not properly specified.");
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
						"The attributes to ignore were not properly specified. All features will be used for training.");
				currentAttributesToIgnore = new ArrayList<Integer>();
			}
			
			ClassificationType currentClassificationType;
			if(classifierConfig.getClassificationTypeAttribute().getValueAt(i).toString().equals("UNSUPERVISED")) {
				currentClassificationType = ClassificationType.UNSUPERVISED;
			} else if(classifierConfig.getClassificationTypeAttribute().getValueAt(i).toString().equals("BINARY")) {
				currentClassificationType = ClassificationType.BINARY;
			} else if(classifierConfig.getClassificationTypeAttribute().getValueAt(i).equals("MULTILABEL")) {
				currentClassificationType = ClassificationType.MULTILABEL;
			} else {
				currentClassificationType = ClassificationType.MULTICLASS;
			}
			
			boolean currentFuzzy = classifierConfig.getFuzzyAttribute().getValueAt(i) >= 0.5;
			
			String currentPathToInputModel = classifierConfig.getPathToInputModelAttribute().getValueAt(i);
			
			
			Integer currentMergeSongResults = (new Double(classifierConfig.getMergeSongResultsAttribute().getValueAt(i).toString())).intValue();
			String currentOutputResult = classifierConfig.getOutputResultAttribute().getValueAt(i).toString();
			InputSourceType ist;
			if(classifierConfig.getInputSourceTypeAttribute().getValueAt(i).toString().equals(new String("FILE_LIST"))) {
				ist = InputSourceType.FILE_LIST;
			} else {
				ist = InputSourceType.READY_INPUT;
			}	
			
			String currentTrainingDescription = classifierConfig.getTrainingDescriptionAttribute().getValueAt(i);
			
			// Create a classification task
		    taskConfigurations.add(new ClassificationConfiguration(ist, currentInputFileList, currentAttributesToIgnore, currentProcessedFeaturesDescription, 
		    		currentAlgorithmDescription, currentGroundTruthSource, currentAttributesToClassify, currentClassificationType, currentFuzzy, currentMergeSongResults, currentOutputResult, currentPathToInputModel, currentTrainingDescription));
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
	public String getProcessedFeaturesModelName() {
		return processedFeaturesModelName;
	}

	/**
	 * @return the algorithmDescription
	 */
	public String getAlgorithmDescription() {
		return algorithmDescription;
	}
	
	public int getGroundTruthCategoryId() {
		return groundTruthCategoryId;
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
	 * @return the mergeSongResults
	 */
	public Integer getMergeSongResults() {
		return mergeSongResults;
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

}
