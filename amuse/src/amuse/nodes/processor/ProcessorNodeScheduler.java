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
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.nodes.NodeEvent;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.NodeScheduler;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;

/**
 * ProcessorNodeScheduler is responsible for the processor node. The given features over time
 * are converted to vectors for classification algorithms 
 * 
 * @author Igor Vatolkin
 * @version $Id: ProcessorNodeScheduler.java 1238 2010-08-02 15:27:12Z waeltken $
 */
public class ProcessorNodeScheduler extends NodeScheduler { 

	/** The counter of all processing steps (used for intermediate results folder) */
	int currentProcessingStep = 0;
	
	/** Minimal source frame size from all features to process */
	private int minimalFrameSize = Integer.MAX_VALUE;
	
	/** Saves the number of initially used time windows for features from larger frame sources as from minimal sources */
	private HashMap<Integer,Long> featureIdToWindowNumber;
	
	private HashMap<Integer,Integer> featureIdToSourceFrameSize;
	
	/** Number of time windows before processing, used for the calculation of used raw time windows ratio metric.
	 * Here the number of MINIMAL time windows is calculated. E.g. if the feature with the smallest source frame
	 * size equal to 512 exists, and there is the second feature which is calculated from frames of 1024 samples,
	 * the number of values from the second feature is multiplied by (1024/512) */
	private long initialNumberOfUsedRawTimeWindows = 0;
	
	private long finalNumberOfUsedRawTimeWindows = 0;
	private long initialNumberOfFeatureMatrixEntries = 0;
	private long finalNumberOfFeatureMatrixEntries = 0;
	
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
			thisScheduler = new ProcessorNodeScheduler(args[0] + "/input/task_" + args[1]);
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
					thisScheduler.nodeHome + "/input/task_'" + thisScheduler.jobId + 
					"; please delete it manually! (Exception: "+ e.getMessage() + ")");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String, long, amuse.interfaces.nodes.TaskConfiguration)
	 */
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration processingConfiguration) {
		
		// ---------------------------------------
		// (I): Configure processor node scheduler
		// ---------------------------------------
		this.nodeHome = nodeHome;
		if(this.nodeHome.startsWith(System.getenv("AMUSEHOME"))) {
			this.directStart = true;
		}
		this.jobId = new Long(jobId);
		this.featureIdToWindowNumber = new HashMap<Integer,Long>();
		this.featureIdToSourceFrameSize = new HashMap<Integer,Integer>();
		this.taskConfiguration = processingConfiguration;
		
		// If this node is started directly, the properties are loaded from AMUSEHOME folder;
		// if this node is started via command line (e.g. in a grid, the properties are loaded from
		// %processor home folder%/input
		if(!this.directStart) {
			File preferencesFile = new File(this.nodeHome + "/config/amuse.properties");
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
			System.exit(1);
		}
		
		// --------------------------------------------------------------------
		// (III) Start the methods for feature and/or time dimension processing
		// --------------------------------------------------------------------
		try {
			this.proceedProcessingSteps(rawFeatures);
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
				"Problem(s) occured during feature processing steps: " + e.getMessage());
			System.exit(1);
		}
		
		// -------------------------------------------------------------------------
		// (IV) Start the method for partitioning and conversion of matrix to vector
		// -------------------------------------------------------------------------
		try {
			rawFeatures = this.proceedMatrix2VectorConversion(rawFeatures);
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
				"Problem(s) occured during conversion from matrix to vector: " + e.getMessage());
			System.exit(1);
		}
		
		// ---------------------------------------------------
		// (V) Save the processed features to feature database
		// ---------------------------------------------------
		try {
			this.saveProcessedFeaturesToDatabase(rawFeatures);
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Problem(s) occured during saving of processed features to database: " + e.getMessage());
				e.printStackTrace();
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
					this.nodeHome + "/input/task_'" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
				System.exit(1);
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
			fis = new FileInputStream(args[0] + "/task_" + args[1] + ".ser");
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
					(i+1) + "/" + processorConfig.length);
		}
	}
	
	/**
	 * Prepares the first list of all features to be processed
	 * @return List of feature files
	 * @throws NodeException
	 */
	private ArrayList<Feature> loadFeatures() throws NodeException {
		
		ArrayList<Feature> features = new ArrayList<Feature>();
		
		// Calculate the first list of feature files to be processed.
		// These files must be raw features from the Amuse feature database
		// At first create a list with all feature IDs which should be proceeded during the first 
		// reduction step..
		FeatureTable featureSet = ((ProcessingConfiguration)this.taskConfiguration).getInputFeatureList();
		List<Integer> featureIDs = featureSet.getSelectedIds();
			
		// Feature files for the current music file
		for(int i=0;i<featureIDs.size();i++) {
					
			// Calculate the path to feature files
			String relativeName = new String();
			if(((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0).startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
				relativeName = ((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0).substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length()+1);
			} else {
				relativeName = ((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0);
			}
			relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
			if(relativeName.lastIndexOf(File.separator) != -1) {
				features.add(ArffFeatureLoader.loadFeature(AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +
					relativeName.substring(relativeName.lastIndexOf(File.separator)) + "_" + featureIDs.get(i) + ".arff"));
			} else {
				features.add(ArffFeatureLoader.loadFeature(AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +
						File.separator + relativeName + "_" + featureIDs.get(i) + ".arff"));
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
		// smallest window size. The reason is that the processor methods should operate on the vectors of equal length
		// and some methods will produce errors in extremal cases: e.g. onset pruning makes no sense for a feature
		// "Duration of a music piece" since it has only one value. Therefore the smallest window size in samples
		// is here calculated and the features from larger time windows are converted, e.g. "Duration of a music pieces"
		// becomes a vector with a larger number of equal values
		this.minimalFrameSize = Integer.MAX_VALUE;
		int exampleOfFeatureWithMinimalFrame = -1; // Used later
		for(int i=0;i<features.size();i++) {
			if(features.get(i).getSourceFrameSize() != -1 && features.get(i).getSourceFrameSize() < minimalFrameSize) {
				minimalFrameSize = features.get(i).getSourceFrameSize();
				exampleOfFeatureWithMinimalFrame = i;
			}
		}
		this.initialNumberOfUsedRawTimeWindows = 0;
		
		// Change the feature vectors if the corresponding feature is created from larger frames as the minimal frame
		for(int i=0;i<features.size();i++) {
			int actualFrameSize = features.get(i).getSourceFrameSize();
			featureIdToSourceFrameSize.put(features.get(i).getId(), features.get(i).getSourceFrameSize());
			
			// If the complete song is used as source..
			if(actualFrameSize == -1) {
				actualFrameSize = features.get(exampleOfFeatureWithMinimalFrame).getWindows().size() * minimalFrameSize;
				features.get(i).getWindows().set(0,1d);
			}
			
			if(actualFrameSize > minimalFrameSize) {
				featureIdToWindowNumber.put(features.get(i).getId(), new Long(features.get(i).getValues().size()));
				initialNumberOfUsedRawTimeWindows += (features.get(i).getValues().size() * (new Double(actualFrameSize) / minimalFrameSize));
				ArrayList<Double[]> newValues = new ArrayList<Double[]>(features.get(exampleOfFeatureWithMinimalFrame).getWindows().size());
				ArrayList<Double> newWindows = new ArrayList<Double>(features.get(exampleOfFeatureWithMinimalFrame).getWindows().size());
				int numberOfCurrentSmallWindow = 0;
				
				// Proceed the larger time frames and map them to the smallest time frame
				for(int indexOfLargeWindow = 0; indexOfLargeWindow < features.get(i).getWindows().size(); indexOfLargeWindow++) {

					// Time window numbers can be doubles e.g. for CENS features
					double numberOfLargeTimeWindow = features.get(i).getWindows().get(indexOfLargeWindow);
					
					// The last small time window which correspond to the large time window
					int numberOfLastSmallTWForThisLargeTW = new Double(Math.ceil(numberOfLargeTimeWindow * (double)actualFrameSize / 
							(double)minimalFrameSize)).intValue();
					
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
				for(;numberOfCurrentSmallWindow < features.get(exampleOfFeatureWithMinimalFrame).getWindows().size();numberOfCurrentSmallWindow++) {
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
			ArffLoader processingToolsLoader = new ArffLoader();
		    try {
		    	if(this.directStart) {
		    		processingToolsLoader.setFile(new File(System.getenv("AMUSEHOME") + "/config/processorAlgorithmTable.arff"));
		    	} else {
		    		processingToolsLoader.setFile(new File(this.nodeHome + "/input/task_" + this.jobId + "/processorAlgorithmTable.arff"));
		    	}
				Instance currentInstance = processingToolsLoader.getNextInstance(processingToolsLoader.getStructure());
				Attribute idAttribute = processingToolsLoader.getStructure().attribute("Id");
				while(currentInstance != null) {

					// Load the required adapter class and configure it
					if(!currentInstance.isMissing(idAttribute) && 
							currentStepID == new Double(currentInstance.value(idAttribute)).intValue()) {
						
						// Attributes with properties of dimension reducer
						Attribute processorNameAttribute = processingToolsLoader.getStructure().attribute("Name");
						Attribute adapterClassAttribute = processingToolsLoader.getStructure().attribute("AdapterClass");
						Attribute homeFolderAttribute = processingToolsLoader.getStructure().attribute("HomeFolder");
						Attribute processorStartScriptAttribute = processingToolsLoader.getStructure().attribute("StartScript");
						Attribute inputProcessorBatchAttribute = processingToolsLoader.getStructure().attribute("InputBatch");
						
						try {
							adapter = Class.forName(currentInstance.stringValue(adapterClassAttribute));
							
							dri = (DimensionProcessorInterface)adapter.newInstance();
							Properties processorProperties = new Properties();
							Integer idOfCurrentProcessor = new Double(currentInstance.value(idAttribute)).intValue();
							processorProperties.setProperty("id",idOfCurrentProcessor.toString());
							processorProperties.setProperty("processorName",currentInstance.stringValue(processorNameAttribute));
							processorProperties.setProperty("processorFolderName",currentInstance.stringValue(homeFolderAttribute));
							if(directStart) {
								processorProperties.setProperty("processorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + "/tools/" + currentInstance.stringValue(homeFolderAttribute));
							} else {
								processorProperties.setProperty("processorFolder",nodeHome + "/tools/" + currentInstance.stringValue(homeFolderAttribute));
							}
							processorProperties.setProperty("processorStartScript",currentInstance.stringValue(processorStartScriptAttribute));
							processorProperties.setProperty("inputProcessorBatch",currentInstance.stringValue(inputProcessorBatchAttribute));
							processorProperties.setProperty("minimalFrameSize",new Integer(this.minimalFrameSize).toString());
							((AmuseTask)dri).configure(processorProperties,this,currentStepParams);
							((AmuseTask)dri).initialize();
							
							AmuseLogger.write(this.getClass().getName(), Level.INFO, 
									"Processor step is configured: " + currentInstance.stringValue(adapterClassAttribute));
							break;
						} catch(ClassNotFoundException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Processor class cannot be located: " + currentInstance.stringValue(adapterClassAttribute));
						} catch(IllegalAccessException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Processor class or its nullary constructor is not accessible: " + currentInstance.stringValue(adapterClassAttribute));
						} catch(InstantiationException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Instantiation failed for processor class: " + currentInstance.stringValue(adapterClassAttribute));
						}
					}
					currentInstance = processingToolsLoader.getNextInstance(processingToolsLoader.getStructure());
				}
				processingToolsLoader.reset();
		    } catch(IOException e) {
		    	e.printStackTrace();
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
		ArffLoader processingToolsLoader = new ArffLoader();
		try {
		   	if(this.directStart) {
		   		processingToolsLoader.setFile(new File(System.getenv("AMUSEHOME") + "/config/processorConversionAlgorithmTable.arff"));
		   	} else {
		   		processingToolsLoader.setFile(new File(this.nodeHome + "/input/task_" + this.jobId + "/processorConversionAlgorithmTable.arff"));
		   	}
			Instance currentInstance = processingToolsLoader.getNextInstance(processingToolsLoader.getStructure());
			Attribute idAttribute = processingToolsLoader.getStructure().attribute("Id");
			while(currentInstance != null) {

				// Load the required adapter class and configure it
				if(!currentInstance.isMissing(idAttribute) && 
						stepID == new Double(currentInstance.value(idAttribute)).intValue()) {
						
					// Attributes with properties of dimension reducer
					Attribute processorNameAttribute = processingToolsLoader.getStructure().attribute("Name");
					Attribute adapterClassAttribute = processingToolsLoader.getStructure().attribute("AdapterClass");
					Attribute homeFolderAttribute = processingToolsLoader.getStructure().attribute("HomeFolder");
					Attribute processorStartScriptAttribute = processingToolsLoader.getStructure().attribute("StartScript");
					Attribute inputProcessorBatchAttribute = processingToolsLoader.getStructure().attribute("InputBatch");
						
					try {
						adapter = Class.forName(currentInstance.stringValue(adapterClassAttribute));
							
						mtvci = (MatrixToVectorConverterInterface)adapter.newInstance();
						Properties processorProperties = new Properties();
						Integer idOfCurrentProcessor = new Double(currentInstance.value(idAttribute)).intValue();
						processorProperties.setProperty("id",idOfCurrentProcessor.toString());
						processorProperties.setProperty("processorName",currentInstance.stringValue(processorNameAttribute));
						processorProperties.setProperty("processorFolderName",currentInstance.stringValue(homeFolderAttribute));
						if(directStart) {
							processorProperties.setProperty("processorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + "/tools/" + currentInstance.stringValue(homeFolderAttribute));
						} else {
							processorProperties.setProperty("processorFolder",nodeHome + "/tools/" + currentInstance.stringValue(homeFolderAttribute));
						}
						processorProperties.setProperty("processorStartScript",currentInstance.stringValue(processorStartScriptAttribute));
						processorProperties.setProperty("inputProcessorBatch",currentInstance.stringValue(inputProcessorBatchAttribute));
						processorProperties.setProperty("minimalFrameSize",new Integer(this.minimalFrameSize).toString());
						((AmuseTask)mtvci).configure(processorProperties,this,stepParams);
							
						AmuseLogger.write(this.getClass().getName(), Level.INFO, 
								"Processor step is configured: " + currentInstance.stringValue(adapterClassAttribute));
						break;
					} catch(ClassNotFoundException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Processor class cannot be located: " + currentInstance.stringValue(adapterClassAttribute));
					} catch(IllegalAccessException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Processor class or its nullary constructor is not accessible: " + currentInstance.stringValue(adapterClassAttribute));
					} catch(InstantiationException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Instantiation failed for processor class: " + currentInstance.stringValue(adapterClassAttribute));
					}
				}
				currentInstance = processingToolsLoader.getNextInstance(processingToolsLoader.getStructure());
			}
			processingToolsLoader.reset();
	   } catch(IOException e) {
		   e.printStackTrace();
	    	AmuseLogger.write(this.getClass().getName(), Level.FATAL, 
		   				"Processor table could not be parsed properly: " + e.getMessage());
    		System.exit(1);
	    }
		
		// Start the adapter
		return mtvci.runConversion(features, ((ProcessingConfiguration)this.taskConfiguration).getPartitionSize(), 
				((ProcessingConfiguration)this.taskConfiguration).getPartitionOverlap(), 
				((ProcessingConfiguration)this.taskConfiguration).getReductionSteps() + "_" + 
				((ProcessingConfiguration)this.taskConfiguration).getConversionStep() + "_" + 
				((ProcessingConfiguration)this.taskConfiguration).getPartitionSize() + "ms_" + 
				((ProcessingConfiguration)this.taskConfiguration).getPartitionOverlap() + "ms");
	}
	
	/**
	 * Saves the processed features to feature database
	 */
	private void saveProcessedFeaturesToDatabase(ArrayList<Feature> features) throws NodeException {

		// Create file and folder for processed features
		String destinationFile = ((ProcessingConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0);
		String relativeName = new String();
		if(destinationFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
			relativeName = destinationFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length()+1);
		} else {
			relativeName = destinationFile;
		}
		relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
		String featureDesc = new String("");
		if(!((ProcessingConfiguration)this.taskConfiguration).getFeatureDescription().equals(new String(""))) {
			featureDesc = "_" + ((ProcessingConfiguration)this.taskConfiguration).getFeatureDescription();
		}
		if(relativeName.lastIndexOf(File.separator) != -1) {
			relativeName = ((ProcessingConfiguration)this.getConfiguration()).getProcessedFeatureDatabase() + File.separator + relativeName +
				relativeName.substring(relativeName.lastIndexOf(File.separator)) + "_" +
				((ProcessingConfiguration)this.taskConfiguration).getReductionSteps() + "__" + 
				((ProcessingConfiguration)this.taskConfiguration).getConversionStep() + "__" + 
				((ProcessingConfiguration)this.taskConfiguration).getPartitionSize() + "ms_" + 
				((ProcessingConfiguration)this.taskConfiguration).getPartitionOverlap() + "ms" + featureDesc + ".arff";
		} else {
			relativeName = ((ProcessingConfiguration)this.getConfiguration()).getProcessedFeatureDatabase() + File.separator + relativeName +
					File.separator + relativeName + "_" + ((ProcessingConfiguration)this.taskConfiguration).getReductionSteps() + "__" +
					((ProcessingConfiguration)this.taskConfiguration).getConversionStep() + "__" + 
					((ProcessingConfiguration)this.taskConfiguration).getPartitionSize() + "ms_" + 
					((ProcessingConfiguration)this.taskConfiguration).getPartitionOverlap() + "ms" + featureDesc + ".arff";
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
			values_writer.writeBytes("@ATTRIBUTE Unit {milliseconds,samples}");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE Start NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("@ATTRIBUTE End NUMERIC");
			values_writer.writeBytes(sep+sep);
			values_writer.writeBytes("@DATA");
			values_writer.writeBytes(sep);
			
			// TODO Consider only the partitions up to 6 minutes of a music track; should be a parameter?
			int numberOfMaxPartitions = features.get(0).getValues().size();
			for(int j=1;j<features.size();j++) {
				if(features.get(j).getValues().size() < numberOfMaxPartitions) {
					numberOfMaxPartitions = features.get(j).getValues().size();
				}
			}
			System.out.println("N: " + numberOfMaxPartitions);
			if((numberOfMaxPartitions * (((ProcessingConfiguration)this.taskConfiguration).getPartitionSize() - 
					((ProcessingConfiguration)this.taskConfiguration).getPartitionOverlap())) > 360000) {
				numberOfMaxPartitions = 360000 / (((ProcessingConfiguration)this.taskConfiguration).getPartitionSize() - 
						((ProcessingConfiguration)this.taskConfiguration).getPartitionOverlap());
				AmuseLogger.write(this.getClass().getName(), Level.WARN, 
		   				"Number of partitions after processing reduced from " + features.get(0).getValues().size() + 
		   				" to " + numberOfMaxPartitions);
			}
			
			// TODO [1/2] For adaptive onset partitions the boundaries are calculated here. A more generic solution
			// is to change Feature class and allow frames of different sizes (e.g. with a child class)
			// Load the attack start events and release end events
			Double[] attackStarts = null;
			Double[] releaseEnds = null;
			//only load eventtimes when they are really needed
			if (((ProcessingConfiguration)this.taskConfiguration).getConversionStep().startsWith(new String("1"))){
				attackStarts = loadEventTimes("attack");
				releaseEnds = loadEventTimes("release");		
			}
			
			double partSize = ((ProcessingConfiguration)this.taskConfiguration).getPartitionSize();
			double stepSize = ((ProcessingConfiguration)this.taskConfiguration).getPartitionOverlap();
			
			// Save the data
			for(int i=0;i<numberOfMaxPartitions;i++) {
				for(int j=0;j<features.size();j++) {
				
					// [0] since the converted features must be single-dimensional!
					values_writer.writeBytes(features.get(j).getValues().get(i)[0].toString() + ",");
				}
				double sampleRate = new Integer(features.get(0).getSampleRate()).doubleValue();
				if(!((ProcessingConfiguration)this.taskConfiguration).getConversionStep().startsWith(new String("1"))) {
					//values_writer.writeBytes("milliseconds," + features.get(0).getWindows().get(i)*((double)minimalFrameSize/sampleRate*1000d) + "," + 
						//	(features.get(0).getWindows().get(i)*((double)minimalFrameSize/sampleRate*1000d)+((ProcessingConfiguration)this.taskConfiguration).getPartitionSize()) + sep);
					values_writer.writeBytes("milliseconds," + (i*stepSize) + "," + (i*stepSize + partSize) + sep);
				} else {
					
					// TODO [2/2] For adaptive onset partitions the boundaries are calculated here. A more generic solution
					// is to change Feature class and allow frames of different sizes (e.g. with a child class)
					values_writer.writeBytes("milliseconds," + attackStarts[i] * 1000 + "," + releaseEnds[i] * 1000 + sep);
					
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
			if(featureIdToSourceFrameSize.get(currentRawFeature) == minimalFrameSize) {
				
				// Here we suggest that the windows are the same for all features!! (Reduction methods work for all features
				// similarly, since the matrix form remains!)
				finalNumberOfUsedRawTimeWindows += features.get(0).getWindows().size();
			}
			
			else if(featureIdToSourceFrameSize.get(currentRawFeature) == -1) {
				finalNumberOfUsedRawTimeWindows += features.get(0).getWindows().size();
			}
			
			// In the frame size is larger than minimal frame size, the number of required minimal frames must be calculated..
			else {
				int numberOfUsedLargeTimeWindows = 1;
				int previousLargeWindow = 0;
				int actualLargeWindow = 0;
				int sourceFrameSize = featureIdToSourceFrameSize.get(currentRawFeature); 
				for(int j=0;j<features.get(0).getWindows().size();j++) {
					// Get the number of the corresponding large time window
					double actualSmallWindow = features.get(0).getWindows().get(j);
					actualLargeWindow = new Double(actualSmallWindow / 
							((new Double(sourceFrameSize) / minimalFrameSize))).intValue();
					
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
				finalNumberOfUsedRawTimeWindows += (numberOfUsedLargeTimeWindows * (new Double(sourceFrameSize) / minimalFrameSize));
			}
		}
		
		int numberOfAllFeatureDimensions = 0;
		for(Feature f: features) {
			numberOfAllFeatureDimensions += f.getDimension();
		}
		finalNumberOfFeatureMatrixEntries = numberOfAllFeatureDimensions * features.get(0).getWindows().size();
	}
	
	// TODO Used only for metrics for StructurePruner
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
			if(featureIdToSourceFrameSize.get(currentRawFeature) == minimalFrameSize) {
				
				// Here we suggest that the windows are the same for all features!! (Reduction methods work for all features
				// similarly, since the matrix form remains!)
				finalNumberOfUsedRawTimeWindows += usedTimeWindows.size();
			} 
			// In the frame size is larger than minimal frame size, the number of required minimal frames must be calculated..
			else {
				int numberOfUsedLargeTimeWindows = 1;
				int previousLargeWindow = 0;
				int actualLargeWindow = 0;
				int sourceFrameSize = featureIdToSourceFrameSize.get(currentRawFeature); 
				for(int j=0;j<usedTimeWindows.size();j++) {
					// Get the number of the corresponding large time window
					double actualSmallWindow = usedTimeWindows.get(j);
					actualLargeWindow = new Double(actualSmallWindow / 
							((new Double(sourceFrameSize) / minimalFrameSize))).intValue();
					
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
				finalNumberOfUsedRawTimeWindows += (numberOfUsedLargeTimeWindows * (new Double(sourceFrameSize) / minimalFrameSize));
			}
		}
		
		int numberOfAllFeatureDimensions = 0;
		for(Feature f: features) {
			numberOfAllFeatureDimensions += f.getDimension();
		}
		finalNumberOfFeatureMatrixEntries = numberOfAllFeatureDimensions * features.get(0).getWindows().size();
	}

	/**
	 * @return the minimalFrameSize
	 */
	public int getMinimalFrameSize() {
		return minimalFrameSize;
	}
	
	/**
	 * Loads the event times TODO this function exists also in AORSplitter!
	 * @param string Event description (onset, attack or release)
	 * @return Double array with time values in ms
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
				relativeName = currentTimeEventFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length()+1);
			} else {
				relativeName = currentTimeEventFile;
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


}
