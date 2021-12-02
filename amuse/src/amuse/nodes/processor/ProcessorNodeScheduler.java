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
 * Creation date: 18.09.2007
 */ 
package amuse.nodes.processor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Level;

import amuse.data.ArffFeatureLoader;
import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.Attribute;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.nodes.NodeEvent;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.NodeScheduler;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * ProcessorNodeScheduler is responsible for the processor node. The gInstiven features over time
 * are converted to vectors for classification algorithms 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ProcessorNodeScheduler extends NodeScheduler { 

	/** The counter of all processing steps (used for intermediate results folder) */
	int currentProcessingStep = 0;
	
	/** Minimal source frame size from all features to process */
	private int minimalStepSize = Integer.MAX_VALUE;
	
	/** Optional settings which can be used in tool node */
	public boolean loadFeaturesFromGivenFolder = false;
	public boolean saveDirectlyToDatabase = false;
	
	/** Saves the number of initially used time windows for features from larger frame sources as from minimal sources */
	private HashMap<Integer,Long> featureIdToWindowNumber;
	
	private HashMap<Integer,Integer> featureIdToSourceStepSize;
	
	/** Number of time windows before processing, used for the calculation of used raw time windows ratio measure.
	 * Here the number of MINIMAL time windows is calculated. E.g. if the feature with the smallest source frame
	 * size equal to 512 exists, and there is the second feature which is calculated from frames of 1024 samples,
	 * the number of values from the second feature is multiplied by (1024/512) */
	private long initialNumberOfUsedRawTimeWindows = 0;
	
	private long finalNumberOfUsedRawTimeWindows = 0;
	private long initialNumberOfFeatureMatrixEntries = 0;
	private long finalNumberOfFeatureMatrixEntries = 0;
	
	/** Saves the processed features if they are not saved in the database */
	private List<Feature> processedFeatures;
	
	/**
	 * Constructor
	 */
	public ProcessorNodeScheduler(String folderForResults) throws NodeException {
		super(folderForResults);
	}
	
	/**
	 * Main method for feature processing
	 * @param args Processing configuration 
	 */
	public static void main(String[] args) {
		
		// Create the node scheduler
		ProcessorNodeScheduler thisScheduler = null;
		try {
			thisScheduler = new ProcessorNodeScheduler(args[0] + File.separator + "input" + File.separator + "task_" + args[1]);
		} catch(NodeException e) {
			AmuseLogger.write(ProcessorNodeScheduler.class.getName(), Level.ERROR,
					"Could not create folder for processor node intermediate results: " + e.getMessage());
			return;
		}
		
		// Proceed the task
		thisScheduler.proceedTask(args);
		
		// Remove the folder for input and intermediate results
		try {
			thisScheduler.removeInputFolder();
		} catch(NodeException e) {
				AmuseLogger.write(ProcessorNodeScheduler.class.getClass().getName(), Level.WARN,
					"Could not remove properly the folder with intermediate results '" + 
					thisScheduler.nodeHome + File.separator + "input" + File.separator + "task_'" + thisScheduler.jobId + 
					"; please delete it manually! (Exception: "+ e.getMessage() + ")");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String, long, amuse.interfaces.nodes.TaskConfiguration)
	 */
	@Override
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration taskConfiguration) {
		proceedTask(nodeHome, jobId, taskConfiguration, true);
	}
	
	/**
	 * Proceeds the processing task
	 * @param saveToFile If true, the processing results are saved to the processed features database 
	 */
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration processingConfiguration, boolean saveToFile) {
		
		// ---------------------------------------
		// (I): Configure processor node scheduler
		// ---------------------------------------
		this.nodeHome = nodeHome;
		if(this.nodeHome.startsWith(AmusePreferences.get(KeysStringValue.AMUSE_PATH))) {
			this.directStart = true;
		}
		this.jobId = new Long(jobId);
		this.featureIdToWindowNumber = new HashMap<Integer,Long>();
		this.featureIdToSourceStepSize = new HashMap<Integer,Integer>();
		this.taskConfiguration = processingConfiguration;
		
		// If this node is started directly, the properties are loaded from AMUSEHOME folder;
		// if this node is started via command line (e.g. in a grid, the properties are loaded from
		// %processor home folder%/input
		if(!this.directStart) {
			File preferencesFile = new File(this.nodeHome + File.separator + "config" + File.separator + "amuse.properties");
			AmusePreferences.restoreFromFile(preferencesFile);
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Processor node scheduler for " + 
				((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0) + " started");
		
		// -----------------------------------------------------------
		// (II) Prepare the first list of all features to be processed 
		// -----------------------------------------------------------
		ArrayList<Feature> rawFeatures = null;
		try {
			rawFeatures = this.loadFeatures();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
				"Problem(s) occured during feature list generation: " + e.getMessage());
			errorDescriptionBuilder.append(((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0));
			this.fireEvent(new NodeEvent(NodeEvent.PROCESSING_FAILED, this));
			return;
		}
		
		// --------------------------------------------------------------------
		// (III) Start the methods for feature and/or time dimension processing
		// --------------------------------------------------------------------
		try {
			this.proceedProcessingSteps(rawFeatures);
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
				"Problem(s) occured during feature processing steps: " + e.getMessage());
			errorDescriptionBuilder.append(((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0));
			this.fireEvent(new NodeEvent(NodeEvent.PROCESSING_FAILED, this));
			return;
		}
		
		// -------------------------------------------------------------------------
		// (IV) Start the method for partitioning and conversion of matrix to vector
		// -------------------------------------------------------------------------
		try {
			rawFeatures = this.proceedMatrix2VectorConversion(rawFeatures);
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
				"Problem(s) occured during conversion from matrix to vector: " + e.getMessage());
			errorDescriptionBuilder.append(((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0));
			this.fireEvent(new NodeEvent(NodeEvent.PROCESSING_FAILED, this));
			return;
		}
		
		// ---------------------------------------------------
		// (V) Save the processed features to feature database
		// ---------------------------------------------------
		try {
			if(saveToFile) {
				this.saveProcessedFeaturesToDatabase(rawFeatures);
			} else {
				this.processedFeatures = rawFeatures;
			}
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Problem(s) occured during saving of processed features to database: " + e.getMessage());
			errorDescriptionBuilder.append(((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0));
			this.fireEvent(new NodeEvent(NodeEvent.PROCESSING_FAILED, this));
			return;
		}
		
		// ----------------------------------------------------------------------------------
		// (VI) If started directly, remove generated data and fire event for Amuse scheduler
		// ----------------------------------------------------------------------------------
		if(this.directStart) {
			try {
				this.cleanInputFolder();
			} catch(NodeException e) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Could not remove properly the intermediate results '" + 
					this.nodeHome + File.separator + "input" + File.separator + "task_'" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
			}
			this.fireEvent(new NodeEvent(NodeEvent.PROCESSING_COMPLETED, this));
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String[])
	 */
	public void proceedTask(String[] args) {
		if(args.length < 2) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL, 2 - args.length + 
					" arguments are missing; The usage is 'ProcessorNodeScheduler %1 %2', where: \n" +
					"%1 - Home folder of this node\n" +
					"%2 - Unique (for currently running Amuse instance) task Id\n"); 
			System.exit(1);
		}
		
		// Load the task configuration from %PROCESSORHOME%/task.ser
		ProcessingConfiguration[] processorConfig = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(args[0] + File.separator + "task_" + args[1] + ".ser");
			in = new ObjectInputStream(fis);
			Object o = in.readObject();
			processorConfig = (ProcessingConfiguration[])o;
			in.close();
		} catch(IOException ex) {
		    ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Proceed the processing task(s)
		for(int i=0;i<processorConfig.length;i++) {
			proceedTask(args[0],new Long(args[1]),processorConfig[i]);
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "Processor node is going to start job " + 
					(i+1) + File.separator + processorConfig.length);
		}
	}
	
	/**
	 * Prepares the first list of all features to be processed
	 * @return List of feature files
	 * @throws NodeException
	 */
	private ArrayList<Feature> loadFeatures() throws NodeException {
		
		// Load either raw unprocessed or previously processed features?
		if (((ProcessingConfiguration)this.taskConfiguration).getInputSourceType() == ProcessingConfiguration.InputSourceType.RAW_FEATURE_LIST) {
			return loadFeaturesRaw();
		} else {
			return loadFeaturesProcessed();
		}
	}
	
	/**
	 * Prepares the first list of all features to be processed from the raw feature files
	 * @return List of feature files
	 * @throws NodeException
	 */
	private ArrayList<Feature> loadFeaturesRaw() throws NodeException {
		
		ArrayList<Feature> features = new ArrayList<Feature>();
		
		// Calculate the first list of feature files to be processed.
		// These files must be raw features from the Amuse feature database
		// At first create a list with all feature IDs which should be proceeded during the first 
		// reduction step..
		FeatureTable featureSet = ((ProcessingConfiguration)this.taskConfiguration).getInputFeatureList();
		List<Integer> featureIDs = featureSet.getSelectedIds();
		List<Integer> configurationIDs = featureSet.getSelectedConfigurationIds();
			
		// Feature files for the current music file
		for(int i=0;i<featureIDs.size();i++) {
					
			// Calculate the path to feature files
			String relativeName = new String();
			String musicDatabasePath = AmusePreferences.get(KeysStringValue.MUSIC_DATABASE);
			// Make sure music database path ends with file separator to catch tracks that have the data base path as suffix but are not in the database
			musicDatabasePath += musicDatabasePath.endsWith(File.separator) ? "" : File.separator;
			if(((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0).startsWith(musicDatabasePath)) {
				relativeName = ((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0).substring(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)).getPath().length());
			} else {
				relativeName = ((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0);
			}
			if(relativeName.charAt(0) == File.separatorChar) {
				relativeName = relativeName.substring(1);
			}
			relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
			
			if(relativeName.lastIndexOf(File.separator) != -1) {
				// TODO-17C1 evomix
				if(loadFeaturesFromGivenFolder) {
					features.add(ArffFeatureLoader.loadFeature(((ProcessingConfiguration)this.taskConfiguration).getFeatureDatabase() + File.separator + 
							relativeName.substring(relativeName.lastIndexOf(File.separator)+1,relativeName.length()) + "_" + featureIDs.get(i) + 
							(configurationIDs.get(i) == null ? "" : "_" + configurationIDs.get(i))
							+ ".arff", featureIDs.get(i)));
				} else {
					features.add(ArffFeatureLoader.loadFeature(AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +
							relativeName.substring(relativeName.lastIndexOf(File.separator)) + "_" + featureIDs.get(i)
							+ (configurationIDs.get(i) == null ? "" : "_" + configurationIDs.get(i))
							+ ".arff", featureIDs.get(i)));
				}
			} else {
				features.add(ArffFeatureLoader.loadFeature(AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +
						File.separator + relativeName + "_" + featureIDs.get(i)
						+ (configurationIDs.get(i) == null ? "" : "_" + configurationIDs.get(i))
						+ ".arff", featureIDs.get(i)));
			}
		}
		
		// Check if the all features have been extracted using the same sample rate
		// TODO v0.x This limitation may disappear in one of the future AMUSE versions
		int firstSampleRate = features.get(0).getSampleRate();
		for(Feature currentFeature : features) {
			if(currentFeature.getSampleRate() != firstSampleRate) {
				throw new NodeException("Feature used for processing have different sampling rates (" + 
						firstSampleRate + " and " + currentFeature.getSampleRate() + "; it is not currently supported!");
			}
		}
		
		// If the features to process have different source time windows (e.g. mfccs for each 512 samples
		// and fluctuation patterns for each 32768 samples), the feature vectors must be adjusted according to the
		// smallest step size. The reason is that the processor methods should operate on the vectors of equal length
		// and some methods will produce errors in extremal cases: e.g. onset pruning makes no sense for a feature
		// "Duration of a music piece" since it has only one value. Therefore the smallest step size in samples
		// is here calculated and the features from larger time windows are converted, e.g. "Duration of a music pieces"
		// becomes a vector with a larger number of equal values
		this.minimalStepSize = Integer.MAX_VALUE;
		int exampleOfFeatureWithMinimalStep = -1; // Used later
		for(int i=0;i<features.size();i++) {
			if(features.get(i).getSourceFrameSize() != -1 && features.get(i).getSourceFrameSize() < minimalStepSize) {
				minimalStepSize = features.get(i).getSourceStepSize();
				exampleOfFeatureWithMinimalStep = i;
			}
		}
		this.initialNumberOfUsedRawTimeWindows = 0;
		
		// Change the feature vectors if the corresponding feature is created from larger frames as the minimal frame
		for(int i=0;i<features.size();i++) {
			int actualStepSize = features.get(i).getSourceStepSize();
			featureIdToSourceStepSize.put(features.get(i).getId(), features.get(i).getSourceStepSize());
			

			// If the complete song is used as source..
			if(actualStepSize == -1) {
				actualStepSize = features.get(exampleOfFeatureWithMinimalStep).getWindows().size() * minimalStepSize;
				features.get(i).getWindows().set(0,1d);
			}
			
			if(actualStepSize > minimalStepSize) {
				featureIdToWindowNumber.put(features.get(i).getId(), new Long(features.get(i).getValues().size()));
				initialNumberOfUsedRawTimeWindows += (features.get(i).getValues().size() * (new Double(actualStepSize) / minimalStepSize));
				ArrayList<Double[]> newValues = new ArrayList<Double[]>(features.get(exampleOfFeatureWithMinimalStep).getWindows().size());
				ArrayList<Double> newWindows = new ArrayList<Double>(features.get(exampleOfFeatureWithMinimalStep).getWindows().size());
				int numberOfCurrentSmallWindow = 0;
				
				// Proceed the larger time frames and map them to the smallest time frame
				for(int indexOfLargeWindow = 0; indexOfLargeWindow < features.get(i).getWindows().size(); indexOfLargeWindow++) {

					// Time window numbers can be doubles e.g. for CENS features
					double numberOfLargeTimeWindow = features.get(i).getWindows().get(indexOfLargeWindow);
					
					// The last small time window which correspond to the large time window
					int numberOfLastSmallTWForThisLargeTW = new Double(Math.ceil(numberOfLargeTimeWindow * (double)actualStepSize / 
							(double)minimalStepSize)).intValue();
					
					for(int smallTWCounter = numberOfCurrentSmallWindow; smallTWCounter < numberOfLastSmallTWForThisLargeTW;
						smallTWCounter++) {
						
						Double[] currentVals = new Double[features.get(i).getValues().get(indexOfLargeWindow).length];
						for(int b=0;b<currentVals.length;b++) {
							currentVals[b] = new Double(features.get(i).getValues().get(indexOfLargeWindow)[b]);
						}
						
						newValues.add(currentVals);
						newWindows.add(new Double(smallTWCounter + 1)); // Time windows are counted up from 1, not 0!
					}
					numberOfCurrentSmallWindow = numberOfLastSmallTWForThisLargeTW;
				}
				
				// The number of values in the updated feature should be equal to the number of values for all
				// features with minimal frame length. However the features from longer source frames will not 
				// achieve the end of music file so precise as the features from smaller source frames and we
				// must fill some windows with NaN values
				for(;numberOfCurrentSmallWindow < features.get(exampleOfFeatureWithMinimalStep).getWindows().size();numberOfCurrentSmallWindow++) {
					Double[] vals = new Double[features.get(i).getValues().get(0).length];
					for(int k=0;k<vals.length;k++) vals[k] = Double.NaN;
					newValues.add(vals);
					newWindows.add(new Double(numberOfCurrentSmallWindow + 1)); // Time windows are counted up from 1, not 0!
				}
				
				// Replace the old feature with adapted feature
				Feature adaptedFeature = new Feature(features.get(i).getIds(), features.get(i).getDescription(), 
						newValues, newWindows);
				adaptedFeature.setHistory(features.get(i).getHistory());
				adaptedFeature.setSampleRate(features.get(i).getSampleRate());
				adaptedFeature.setSourceFrameSize(features.get(i).getSourceFrameSize());
				adaptedFeature.setSourceStepSize(features.get(i).getSourceStepSize());
				features.set(i, adaptedFeature);
			} else {
				initialNumberOfUsedRawTimeWindows += features.get(i).getValues().size();
			}
		}
		
		int numberOfAllFeatureDimensions = 0;
		for(Feature f: features) {
			numberOfAllFeatureDimensions += f.getDimension();
		}
		initialNumberOfFeatureMatrixEntries = numberOfAllFeatureDimensions * features.get(0).getWindows().size();
		
		return features;
	}
	
	/**
	 * Prepares the first list of all features to be processed from the already processed feature files
	 * @return List of feature files
	 * @throws NodeException
	 */
	private ArrayList<Feature> loadFeaturesProcessed() throws NodeException {
		
		ArrayList<Feature> features = new ArrayList<Feature>();
		
		// Calculate the path to processed feature files
		String relativeName = new String();
		String musicDatabasePath = AmusePreferences.get(KeysStringValue.MUSIC_DATABASE);
		// Make sure music database path ends with file separator to catch tracks that have the data base path as suffix but are not in the database
		musicDatabasePath += musicDatabasePath.endsWith(File.separator) ? "" : File.separator;
		if(((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0).startsWith(musicDatabasePath)) {
			relativeName = ((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0).substring(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)).getPath().length());
		} else {
			relativeName = ((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0);
		}
		if(relativeName.charAt(0) == File.separatorChar) {
			relativeName = relativeName.substring(1);
		}
		if(relativeName.contains(".")) {
			relativeName = relativeName.substring(0,relativeName.lastIndexOf('.'));
		}
		
		String processedFeatureFile = new String();
		if(relativeName.lastIndexOf(File.separator) != -1) {
			processedFeatureFile = (AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE) + File.separator + relativeName +
				relativeName.substring(relativeName.lastIndexOf(File.separator)) + "_" + ((ProcessingConfiguration)this.taskConfiguration).getInputFeatures()
				+ ".arff");
		} else {
			processedFeatureFile = (AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE) + File.separator + relativeName +
					File.separator + relativeName + "_" + ((ProcessingConfiguration)this.taskConfiguration).getInputFeatures()
					+ ".arff");
		}
		
		// Load the processed feature files omitting the last three attributes (Unit, Start, End)
		DataSet inputFeatureSet = null;
		try {
			inputFeatureSet = new DataSet(new File(processedFeatureFile));
		} catch (IOException e) {
			throw new NodeException("Could not load processed features as input for processing task: " + e.getMessage());
		}

		// Estimate window size and step size based on the first two windows
		int sourceFrameSize = new Integer(inputFeatureSet.getAttribute("End").getValueStrAt(0)) - new Integer(inputFeatureSet.getAttribute("Start").getValueStrAt(0));
		int sourceStepSize = new Integer(inputFeatureSet.getAttribute("Start").getValueStrAt(1));
		
		for(int currAttribute=0;currAttribute<inputFeatureSet.getAttributeCount()-3;currAttribute++) {
			ArrayList<Integer> id = new ArrayList<Integer>(1);
			id.add(new Integer(-1)); // No id available for already processed features
			String featureName = inputFeatureSet.getAttribute(currAttribute).getName();
			
			ArrayList<Double[]> values = new ArrayList<Double[]>(inputFeatureSet.getValueCount());
			ArrayList<Double> windows = new ArrayList<Double>(inputFeatureSet.getValueCount());
			
			for(int currWindow=0;currWindow<inputFeatureSet.getValueCount();currWindow++) {
				Double val = new Double(inputFeatureSet.getAttribute(currAttribute).getValueStrAt(currWindow));
				values.add(new Double[] {val});
				
				// The number of the current window
				int startMsOfCurrentWindow = new Integer(inputFeatureSet.getAttribute("Start").getValueStrAt(currWindow));
				Double windowN = startMsOfCurrentWindow / sourceStepSize + 1d;
				windows.add(windowN);
			}
			
			Feature loadedFeature = new Feature(id, featureName, values, windows);
			loadedFeature.setSourceFrameSize(sourceFrameSize);
			loadedFeature.setSourceStepSize(sourceStepSize);
			loadedFeature.setSampleRate(22050);
			features.add(loadedFeature);
		}
		
		// Set the minimal frame size using a notional frame rate of 22050 Hz 
		this.minimalStepSize = new Double(Math.floor(sourceStepSize * 22050d / 1000d)).intValue();
		
		// Current hack, as the original frame size is not known for already processed features
		featureIdToSourceStepSize.put(-1, this.minimalStepSize);
		return features;
	}
	
	/**
	 * Starts the methods for feature and/or time dimension processing
	 * @param rawFeatures Raw music features
	 * @throws NodeException
	 */
	private void proceedProcessingSteps(ArrayList<Feature> rawFeatures) throws NodeException {
		
		// Go through all feature and time dimension reduction steps
		// Example for reduction step chain:
		// "0-1[5,15]-3"
		// Here methods with IDs 0,1,3 are applied one after another; method with ID 1 has a
		// parameter string "5,15"
		StringTokenizer t = new StringTokenizer(((ProcessingConfiguration)this.taskConfiguration).getReductionSteps(),"-");
		while(t.hasMoreElements()) {
			
			String currentStepWithParams = t.nextToken();
			int currentStepID;
			String currentStepParams = new String();
			// If parameter string of this step exists
			if(currentStepWithParams.contains("[") && currentStepWithParams.contains("]")) {
				currentStepID = new Integer(currentStepWithParams.substring(0,currentStepWithParams.indexOf("[")));
				currentStepParams = currentStepWithParams.substring(currentStepWithParams.indexOf("[")+1,
						currentStepWithParams.lastIndexOf("]")); 
			} else {
				currentStepID = new Integer(currentStepWithParams);
				currentStepParams = null;
			}
			
			// Adapter for a current reduction step
			Class<?> adapter = null;
			DimensionProcessorInterface dri = null;
			
			// Load the tool table
			DataSetAbstract processingToolsSet;
		    try {
		    	if(this.directStart) {
		    		processingToolsSet = new ArffDataSet(new File(AmusePreferences.getProcessorAlgorithmTablePath()));
		    	} else {
		    		processingToolsSet = new ArffDataSet(new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "processorAlgorithmTable.arff"));
		    	}
				Attribute idAttribute = processingToolsSet.getAttribute("Id");
				for(int i=0;i<processingToolsSet.getValueCount();i++) {

					// Load the required adapter class and configure it
					if(currentStepID == new Double(idAttribute.getValueAt(i).toString()).intValue()) {
						
						// Attributes with properties of dimension reducer
						Attribute processorNameAttribute = processingToolsSet.getAttribute("Name");
						Attribute adapterClassAttribute = processingToolsSet.getAttribute("AdapterClass");
						Attribute homeFolderAttribute = processingToolsSet.getAttribute("HomeFolder");
						Attribute processorStartScriptAttribute = processingToolsSet.getAttribute("StartScript");
						Attribute inputProcessorBatchAttribute = processingToolsSet.getAttribute("InputBatch");
						
						try {
							adapter = Class.forName(adapterClassAttribute.getValueAt(i).toString());
							
							dri = (DimensionProcessorInterface)adapter.newInstance();
							Properties processorProperties = new Properties();
							Integer idOfCurrentProcessor = new Double(idAttribute.getValueAt(i).toString()).intValue();
							processorProperties.setProperty("id",idOfCurrentProcessor.toString());
							processorProperties.setProperty("processorName",processorNameAttribute.getValueAt(i).toString());
							processorProperties.setProperty("processorFolderName",homeFolderAttribute.getValueAt(i).toString());
							if(directStart) {
								processorProperties.setProperty("processorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + homeFolderAttribute.getValueAt(i).toString());
							} else {
								processorProperties.setProperty("processorFolder",nodeHome + File.separator + "tools" + File.separator + homeFolderAttribute.getValueAt(i).toString());
							}
							
							processorProperties.setProperty("processorStartScript",processorStartScriptAttribute.getValueAt(i).toString());
							processorProperties.setProperty("inputProcessorBatch",inputProcessorBatchAttribute.getValueAt(i).toString());
							processorProperties.setProperty("minimalFrameSize",new Integer(this.minimalStepSize).toString());

							((AmuseTask)dri).configure(processorProperties,this,currentStepParams);
							((AmuseTask)dri).initialize();
							
							AmuseLogger.write(this.getClass().getName(), Level.INFO, 
									"Processor step is configured: " + adapterClassAttribute.getValueAt(i).toString());
							break;
						} catch(ClassNotFoundException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Processor class cannot be located: " + adapterClassAttribute.getValueAt(i).toString());
						} catch(IllegalAccessException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Processor class or its nullary constructor is not accessible: " + adapterClassAttribute.getValueAt(i).toString());
						} catch(InstantiationException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Instantiation failed for processor class: " + adapterClassAttribute.getValueAt(i).toString());
						}
					}
				}
		    } catch(IOException e) {
	    		AmuseLogger.write(this.getClass().getName(), Level.FATAL, 
		    				"Processor table could not be parsed properly: " + e.getMessage());
	    		System.exit(1);
		    }
		
			// Start the adapter
			dri.runDimensionProcessing(rawFeatures);
		}
		
		// Calculate the data for pruning rates of raw features usage and matrix processing
		calculateFinalUsedWindowNumbers(rawFeatures);
	}

	/**
	 * Starts the method for partitioning and conversion of matrix to vector
	 * @param currentListOfFeatureFiles List of feature files to process
	 * @throws NodeException
	 */
	private ArrayList<Feature> proceedMatrix2VectorConversion(ArrayList<Feature> features) throws NodeException {
		int stepID;
		String stepParams = new String();
		
		// If parameter string of this step exists
		if(((ProcessingConfiguration)this.taskConfiguration).getConversionStep().contains("[") && 
				((ProcessingConfiguration)this.taskConfiguration).getConversionStep().contains("]")) {
			stepID = new Integer(((ProcessingConfiguration)this.taskConfiguration).getConversionStep().
					substring(0,((ProcessingConfiguration)this.taskConfiguration).getConversionStep().indexOf("[")));
			stepParams = ((ProcessingConfiguration)this.taskConfiguration).getConversionStep().
					substring(((ProcessingConfiguration)this.taskConfiguration).getConversionStep().indexOf("[")+1,
							((ProcessingConfiguration)this.taskConfiguration).getConversionStep().lastIndexOf("]")); 
		} else {
			stepID = new Integer(((ProcessingConfiguration)this.taskConfiguration).getConversionStep());
			stepParams = null;
		}
		
			
		// Adapter for a current reduction step
		Class<?> adapter = null;
		MatrixToVectorConverterInterface mtvci = null;
			
		// Load the tool table
		DataSetAbstract processingToolsSet;
		try {
		   	if(this.directStart) {
		   		processingToolsSet = new ArffDataSet(new File(AmusePreferences.getProcessorConversionAlgorithmTablePath()));
		   	} else {
		   		processingToolsSet = new ArffDataSet(new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "processorConversionAlgorithmTable.arff"));
		   	}
			Attribute idAttribute = processingToolsSet.getAttribute("Id");
			for(int i=0;i<processingToolsSet.getValueCount();i++) {

				// Load the required adapter class and configure it
				if(stepID == new Double(idAttribute.getValueAt(i).toString()).intValue()) {
						
					// Attributes with properties of dimension reducer
					Attribute processorNameAttribute = processingToolsSet.getAttribute("Name");
					Attribute adapterClassAttribute = processingToolsSet.getAttribute("AdapterClass");
					Attribute homeFolderAttribute = processingToolsSet.getAttribute("HomeFolder");
					Attribute processorStartScriptAttribute = processingToolsSet.getAttribute("StartScript");
					Attribute inputProcessorBatchAttribute = processingToolsSet.getAttribute("InputBatch");
						
					try {
						adapter = Class.forName(adapterClassAttribute.getValueAt(i).toString());
							
						mtvci = (MatrixToVectorConverterInterface)adapter.newInstance();
						Properties processorProperties = new Properties();
						Integer idOfCurrentProcessor = new Double(idAttribute.getValueAt(i).toString()).intValue();
						processorProperties.setProperty("id",idOfCurrentProcessor.toString());
						processorProperties.setProperty("processorName",processorNameAttribute.getValueAt(i).toString());
						processorProperties.setProperty("processorFolderName",homeFolderAttribute.getValueAt(i).toString());
						if(directStart) {
							processorProperties.setProperty("processorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + homeFolderAttribute.getValueAt(i).toString());
						} else {
							processorProperties.setProperty("processorFolder",nodeHome + File.separator + "tools" + File.separator + homeFolderAttribute.getValueAt(i).toString());
						}

						processorProperties.setProperty("processorStartScript",processorStartScriptAttribute.getValueAt(i).toString());
						processorProperties.setProperty("inputProcessorBatch",inputProcessorBatchAttribute.getValueAt(i).toString());
						processorProperties.setProperty("minimalFrameSize",new Integer(this.minimalStepSize).toString());

						((AmuseTask)mtvci).configure(processorProperties,this,stepParams);
							
						AmuseLogger.write(this.getClass().getName(), Level.INFO, 
								"Processor step is configured: " + adapterClassAttribute.getValueAt(i).toString());
						break;
					} catch(ClassNotFoundException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Processor class cannot be located: " + adapterClassAttribute.getValueAt(i).toString());
					} catch(IllegalAccessException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Processor class or its nullary constructor is not accessible: " + adapterClassAttribute.getValueAt(i).toString());
					} catch(InstantiationException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Instantiation failed for processor class: " + adapterClassAttribute.getValueAt(i).toString());
					}
				}
			}
	    } catch(IOException e) {
		   e.printStackTrace();
	    	AmuseLogger.write(this.getClass().getName(), Level.FATAL, 
		   				"Processor table could not be parsed properly: " + e.getMessage());
    		System.exit(1);
	    }
		
		// Start the adapter
		String unitString;
		if(((ProcessingConfiguration)this.taskConfiguration).getUnit().toString().equals("SAMPLES")) {
			unitString = "samples";
		} else {
			unitString = "ms";
		}
		return mtvci.runConversion(features, ((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowSize(), 
				((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowStepSize(), 
				((ProcessingConfiguration)this.taskConfiguration).getReductionSteps() + "_" + 
				((ProcessingConfiguration)this.taskConfiguration).getConversionStep() + "_" + 
				((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowSize() + unitString + "_" + 
				((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowStepSize() + unitString,
				((ProcessingConfiguration)this.taskConfiguration).getUnit());
	}
	
	/**
	 * Saves the processed features to feature database
	 */
	private void saveProcessedFeaturesToDatabase(ArrayList<Feature> features) throws NodeException {

		// Create file and folder for processed features
		String destinationFile = ((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0);
		String relativeName = new String();
		String musicDatabasePath = AmusePreferences.get(KeysStringValue.MUSIC_DATABASE);
		// Make sure music database path ends with file separator to catch tracks that have the data base path as suffix but are not in the database
		musicDatabasePath += musicDatabasePath.endsWith(File.separator) ? "" : File.separator;
		if(destinationFile.startsWith(musicDatabasePath)) {
			relativeName = destinationFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length());
		} else {
			relativeName = destinationFile;
		}
		if(relativeName.charAt(0) == File.separatorChar) {
			relativeName = relativeName.substring(1);
		}
		relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
		String featureDesc = new String("");
		if(!((ProcessingConfiguration)this.taskConfiguration).getFeatureDescription().equals(new String(""))) {
			featureDesc = "_" + ((ProcessingConfiguration)this.taskConfiguration).getFeatureDescription();
		}
		
		// Can be used in tool node
		String unitString;
		if(((ProcessingConfiguration)this.taskConfiguration).getUnit().toString().equals("SAMPLES")) {
			unitString = "samples";
		} else {
			unitString = "ms";
		}
		if(saveDirectlyToDatabase) {
			relativeName = relativeName.substring(relativeName.lastIndexOf(File.separator)+1,relativeName.length());
			relativeName = ((ProcessingConfiguration)this.getConfiguration()).getProcessedFeatureDatabase() + File.separator + relativeName + "_" + 
					((ProcessingConfiguration)this.taskConfiguration).getReductionSteps() + "__" +
					((ProcessingConfiguration)this.taskConfiguration).getConversionStep() + "__" + 
					((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowSize() + unitString + "_" + 
					((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowStepSize() + unitString + featureDesc + ".arff";
		} else {
			if(relativeName.lastIndexOf(File.separator) != -1) {
				relativeName = ((ProcessingConfiguration)this.getConfiguration()).getProcessedFeatureDatabase() + File.separator + relativeName +
					relativeName.substring(relativeName.lastIndexOf(File.separator)) + "_" +
					((ProcessingConfiguration)this.taskConfiguration).getReductionSteps() + "__" + 
					((ProcessingConfiguration)this.taskConfiguration).getConversionStep() + "__" + 
					((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowSize() + unitString + "_" + 
					((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowStepSize() + unitString + featureDesc + ".arff";
			} else {
				relativeName = ((ProcessingConfiguration)this.getConfiguration()).getProcessedFeatureDatabase() + File.separator + relativeName +
						File.separator + relativeName + "_" + ((ProcessingConfiguration)this.taskConfiguration).getReductionSteps() + "__" +
						((ProcessingConfiguration)this.taskConfiguration).getConversionStep() + "__" + 
						((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowSize() + unitString + "_" + 
						((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowStepSize() + unitString + featureDesc + ".arff";
			}
		}
		
		File destinationFileFolder = new File(relativeName.substring(0,relativeName.lastIndexOf(File.separator)));
		destinationFileFolder.mkdirs();
		
		try {
			// Save the header
			// Create the output file
			File feature_values_save_file = new File(relativeName);
			if (feature_values_save_file.exists())
				if (!feature_values_save_file.canWrite()) {
					throw new NodeException("Cannot write to processor output file!");
				}
			if (!feature_values_save_file.exists()) {
				feature_values_save_file.createNewFile();
			} else {
				AmuseLogger.write(this.getClass().getName(), Level.WARN, "File with processed features '" + 
						feature_values_save_file.getAbsolutePath() + "' will be overwritten");
			}
			
			FileOutputStream values_to = new FileOutputStream(feature_values_save_file);
			DataOutputStream values_writer = new DataOutputStream(values_to);
			String sep = System.getProperty("line.separator");
			values_writer.writeBytes("@RELATION 'Classifier input'");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%initialNumberOfUsedRawTimeWindows=" + this.initialNumberOfUsedRawTimeWindows);
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%finalNumberOfUsedRawTimeWindows=" + this.finalNumberOfUsedRawTimeWindows);
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%initalNumberOfFeatureMatrixEntries=" + this.initialNumberOfFeatureMatrixEntries);
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%finalNumberOfFeatureMatrixEntries=" + this.finalNumberOfFeatureMatrixEntries);
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%finalNumberOfFeatureVectorEntries=" + features.size());
			values_writer.writeBytes(sep);
			
			// @deprecated
			//values_writer.writeBytes("%finalNumberOfUsedTimeWindows=" + this.finalNumberOfUsedTimeWindows);
			values_writer.writeBytes(sep+sep);
			
			// Save the attributes
			for(int i=0;i<features.size();i++) {
				values_writer.writeBytes("@ATTRIBUTE '");
				values_writer.writeBytes(features.get(i).getHistoryAsString());
				values_writer.writeBytes("' NUMERIC" + sep);
			}
			values_writer.writeBytes("@ATTRIBUTE Unit {MILLISECONDS, SAMPLES}");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE Start NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE End NUMERIC");
			values_writer.writeBytes(sep+sep);
			values_writer.writeBytes("@DATA");
			values_writer.writeBytes(sep);
			
			// TODO Consider only the classification windows up to 6 minutes of a music track; should be a parameter?
			int numberOfMaxClassificatoinWindows = features.get(0).getValues().size();
			for(int j=1;j<features.size();j++) {
				if(features.get(j).getValues().size() < numberOfMaxClassificatoinWindows) {
					numberOfMaxClassificatoinWindows = features.get(j).getValues().size();
				}
			}
			if((numberOfMaxClassificatoinWindows * (((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowSize() - 
					((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowStepSize())) > 360000) {
				numberOfMaxClassificatoinWindows = 360000 / (((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowSize() - 
						((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowStepSize());
				AmuseLogger.write(this.getClass().getName(), Level.WARN, 
		   				"Number of classification windows after processing reduced from " + features.get(0).getValues().size() + 
		   				" to " + numberOfMaxClassificatoinWindows);
			}
			
			// TODO [1/2] For adaptive onset classification windows the boundaries are calculated here. A more generic solution
			// is to change Feature class and allow frames of different sizes (e.g. with a child class)
			// Load the attack start events and release end events
			Double[] attackStarts = null;
			Double[] releaseEnds = null;
			//only load eventtimes when they are really needed
			if (((ProcessingConfiguration)this.taskConfiguration).getConversionStep().startsWith(new String("1"))){
				attackStarts = loadEventTimes("attack");
				releaseEnds = loadEventTimes("release");		
			}
			
			double partSize = ((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowSize();
			double stepSize = ((ProcessingConfiguration)this.taskConfiguration).getAggregationWindowStepSize();
			
			String unit = ((ProcessingConfiguration)this.taskConfiguration).getUnit().toString();
			
			// Save the data
			for(int i=0;i<numberOfMaxClassificatoinWindows;i++) {
				for(int j=0;j<features.size();j++) {
				
					// [0] since the converted features must be single-dimensional!
					values_writer.writeBytes(features.get(j).getValues().get(i)[0].toString() + ",");
				}
				double sampleRate = new Integer(features.get(0).getSampleRate()).doubleValue();
				if(!((ProcessingConfiguration)this.taskConfiguration).getConversionStep().startsWith(new String("1"))) {
					//values_writer.writeBytes("milliseconds," + features.get(0).getWindows().get(i)*((double)minimalFrameSize/sampleRate*1000d) + "," + 
						//	(features.get(0).getWindows().get(i)*((double)minimalFrameSize/sampleRate*1000d)+((ProcessingConfiguration)this.taskConfiguration).getClassificationWindowSize()) + sep);
					values_writer.writeBytes(unit + "," + (i*stepSize) + "," + (i*stepSize + partSize) + sep);
				} else {
					
					// TODO [2/2] For adaptive onset classification windows the boundaries are calculated here. A more generic solution
					// is to change Feature class and allow frames of different sizes (e.g. with a child class)
					if(unit.equals(Unit.SAMPLES.toString())) {
						Double classificationWindowStart = Math.ceil((attackStarts[i] * sampleRate) / this.minimalStepSize); 
						Double classificationWindowEnd = Math.ceil((releaseEnds[i] * sampleRate) / this.minimalStepSize) + 1;
						values_writer.writeBytes(unit + "," + classificationWindowStart + "," + classificationWindowEnd + sep);
					} else {
						values_writer.writeBytes(unit + "," + attackStarts[i] * 1000 + "," + releaseEnds[i] * 1000 + sep);
					}
					
				}
			} 
			values_writer.close();
		} catch(IOException e) {
			throw new NodeException("Could not save the processed feature file!");
		}
	}

	
	/**
	 * Calculates the data for pruning ratios
	 * @param features
	 */
	private void calculateFinalUsedWindowNumbers(ArrayList<Feature> features) {
		// Calculate the pruning rates for raw features and for matrix processing
		ArrayList<Integer> usedRawFeatures = new ArrayList<Integer>();
		for(Feature currentFeature: features) {
			for(Integer id: currentFeature.getIds()) {
				if(!usedRawFeatures.contains(id)) {
					usedRawFeatures.add(id);
				}
			}
		}
		finalNumberOfUsedRawTimeWindows = 0;
		// Go through all required raw features
		for(Integer currentRawFeature : usedRawFeatures) {
			
			// If the feature has the minimal frame size, the number of used minimal raw time windows can be calculated directly
			if(featureIdToSourceStepSize.get(currentRawFeature) == minimalStepSize) {
				
				// Here we suggest that the windows are the same for all features!! (Reduction methods work for all features
				// similarly, since the matrix form remains!)
				finalNumberOfUsedRawTimeWindows += features.get(0).getWindows().size();
			}
			
			else if(featureIdToSourceStepSize.get(currentRawFeature) == -1) {
				finalNumberOfUsedRawTimeWindows += features.get(0).getWindows().size();
			}
			
			// In the frame size is larger than minimal frame size, the number of required minimal frames must be calculated..
			else {
				int numberOfUsedLargeTimeWindows = 1;
				int previousLargeWindow = 0;
				int actualLargeWindow = 0;
				int sourceStepSize = featureIdToSourceStepSize.get(currentRawFeature); 
				for(int j=0;j<features.get(0).getWindows().size();j++) {
					// Get the number of the corresponding large time window
					double actualSmallWindow = features.get(0).getWindows().get(j);
					actualLargeWindow = new Double(actualSmallWindow / 
							((new Double(sourceStepSize) / minimalStepSize))).intValue();
					
					// If we get large windows which were not initially used (at conversion of feature vectors from smaller number of
					// values to the larger number some values at the end of the vector are filled with NaNs and they actually do
					// not correspond to a real large window), they should not be counted!
					if(actualLargeWindow > featureIdToWindowNumber.get(currentRawFeature)) {
						numberOfUsedLargeTimeWindows--;
						break;
					}

					if(actualLargeWindow != previousLargeWindow) {
						numberOfUsedLargeTimeWindows++;
						previousLargeWindow = actualLargeWindow;
					}
				}
				finalNumberOfUsedRawTimeWindows += (numberOfUsedLargeTimeWindows * (new Double(sourceStepSize) / minimalStepSize));
			}
		}
		
		int numberOfAllFeatureDimensions = 0;
		for(Feature f: features) {
			numberOfAllFeatureDimensions += f.getDimension();
		}
		finalNumberOfFeatureMatrixEntries = numberOfAllFeatureDimensions * features.get(0).getWindows().size();
	}
	
	// TODO Used only for measures for StructurePruner
	@Deprecated
	public void setFinalWindows(ArrayList<Double> usedTimeWindows, ArrayList<Feature> features) {
		// Calculate the pruning rates for raw features and for matrix processing
		ArrayList<Integer> usedRawFeatures = new ArrayList<Integer>();
		for(Feature currentFeature: features) {
			for(Integer id: currentFeature.getIds()) {
				if(!usedRawFeatures.contains(id)) {
					usedRawFeatures.add(id);
				}
			}
		}
		finalNumberOfUsedRawTimeWindows = 0;
		// Go through all required raw features
		for(Integer currentRawFeature : usedRawFeatures) {
			
			// If the feature has the minimal frame size, the number of used minimal raw time windows can be calculated directly
			if(featureIdToSourceStepSize.get(currentRawFeature) == minimalStepSize) {
				
				// Here we suggest that the windows are the same for all features!! (Reduction methods work for all features
				// similarly, since the matrix form remains!)
				finalNumberOfUsedRawTimeWindows += usedTimeWindows.size();
			} 
			// In the frame size is larger than minimal frame size, the number of required minimal frames must be calculated..
			else {
				int numberOfUsedLargeTimeWindows = 1;
				int previousLargeWindow = 0;
				int actualLargeWindow = 0;
				int sourceStepSize = featureIdToSourceStepSize.get(currentRawFeature); 
				for(int j=0;j<usedTimeWindows.size();j++) {
					// Get the number of the corresponding large time window
					double actualSmallWindow = usedTimeWindows.get(j);
					actualLargeWindow = new Double(actualSmallWindow / 
							((new Double(sourceStepSize) / minimalStepSize))).intValue();
					
					// If we get large windows which were not initially used (at conversion of feature vectors from smaller number of
					// values to the larger number some values at the end of the vector are filled with NaNs and they actually do
					// not correspond to a real large window), they should not be counted!
					if(actualLargeWindow > featureIdToWindowNumber.get(currentRawFeature)) {
						numberOfUsedLargeTimeWindows--;
						break;
					}

					if(actualLargeWindow != previousLargeWindow) {
						numberOfUsedLargeTimeWindows++;
						previousLargeWindow = actualLargeWindow;
					}
				}
				finalNumberOfUsedRawTimeWindows += (numberOfUsedLargeTimeWindows * (new Double(sourceStepSize) / minimalStepSize));
			}
		}
		
		int numberOfAllFeatureDimensions = 0;
		for(Feature f: features) {
			numberOfAllFeatureDimensions += f.getDimension();
		}
		finalNumberOfFeatureMatrixEntries = numberOfAllFeatureDimensions * features.get(0).getWindows().size();
	}

	/**
	 * @return the minimalStepSize
	 */
	public int getMinimalStepSize() {
		return minimalStepSize;
	}
	
	/**
	 * Loads the event times TODO this function exists also in AORSplitter!
	 * @param string Event description (onset, attack or release)
	 * @return Double array with time values in s
	 */
	private Double[] loadEventTimes(String string) throws NodeException {
		Double[] eventTimes = null;
		
		String idPostfix = null;
		if(string.equals(new String("onset"))) {
			idPostfix = new String("_419.arff");
		} else if(string.equals(new String("attack"))) {
			idPostfix = new String("_423.arff");
		} if(string.equals(new String("release"))) {
			idPostfix = new String("_424.arff");
		} 
		
		try {
			
			// Load the attack, onset or release times, using the file name of the first feature
			// for finding the path to feature files (ID = 419, 423 and 424)
			String currentTimeEventFile = ((ProcessingConfiguration)this.getConfiguration()).getMusicFileList().getFileAt(0);
				
			// Calculate the path to file with time events
			String relativeName = new String();
			if(currentTimeEventFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
				relativeName = currentTimeEventFile.substring(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)).getPath().length());
			} else {
				relativeName = currentTimeEventFile;
			}
			if(relativeName.charAt(0) == File.separatorChar) {
				relativeName = relativeName.substring(1);
			}
			relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
			if(relativeName.lastIndexOf(File.separator) != -1) {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
					relativeName.substring(relativeName.lastIndexOf(File.separator)) + idPostfix;
			} else {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
					File.separator + relativeName + idPostfix;
			}	
			
			DataSetAbstract eventTimesSet = new ArffDataSet(new File(relativeName));
			
			// TODO vINTERNAL Current implementation for the cases if MIR Toolbox extracts none or more than one onset for instrument tones
			/*if(eventTimesSet.getValueCount() != 1) {
				return loadEventTimesBasedOnRMS(string);
			}*/
			
			eventTimes = new Double[eventTimesSet.getValueCount()];
			for(int i=0;i<eventTimes.length;i++) {
				if(string.equals(new String("onset"))) {
					eventTimes[i] = new Double(eventTimesSet.getAttribute("Onset times").getValueAt(i).toString());
				} else if(string.equals(new String("attack"))) {
					eventTimes[i] = new Double(eventTimesSet.getAttribute("Start points of attack intervals").getValueAt(i).toString());
				} if(string.equals(new String("release"))) {
					eventTimes[i] = new Double(eventTimesSet.getAttribute("End points of release intervals").getValueAt(i).toString());
				} 
			}
		} catch(Exception e) {
			throw new NodeException("Could not load the time events: " + e.getMessage());
		}
		return eventTimes;
	}
	
	public List<Feature> getProcessedFeatures(){
		return this.processedFeatures;
	}
	
	
}
