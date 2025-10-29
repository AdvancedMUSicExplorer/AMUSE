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
 * Creation date: 21.12.2006
 */
package amuse.nodes.extractor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.Attribute;
import amuse.interfaces.nodes.NodeEvent;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.NodeScheduler;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.scheduler.SchedulerException;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.pluginmanagement.PluginLoader;
import amuse.util.AmuseLogger;
import amuse.util.FileOperations;
import amuse.util.audio.AudioFileConversion;

/**
 * ExtractorNodeScheduler is responsible for the extractor node. The given music files
 * are converted to wave form and the features are extracted by several extractors. 
 * ExtractorNodeScheduler registers himself as an event listener by the extractor adapters 
 * and is notified about the extraction status.
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ExtractorNodeScheduler extends NodeScheduler {

	/** Extractors are kept in this map (ID -> extractor interface) during feature extraction */
	private HashMap<Integer,ExtractorInterface> extractors;
	
	/** Input music file */
	private String inputFileName = null;
	
	
	/** If the music file is split into several parts.. */
	private int numberOfParts = 0;
	
	/** currentPartForThisExtractor[i] contains the number of a track part
	 * which is currently extracted by extractor i */
	private HashMap<Integer,Integer> currentPartForThisExtractor;
	
	/** Optional settings which can be used in tool node */
	public boolean copyDirectlyToDatabase = false;
	
	/**
	 * Constructor
	 */
	public ExtractorNodeScheduler(String folderForResults) throws NodeException {
		super(folderForResults);
		extractors = new HashMap<Integer,ExtractorInterface>();
		currentPartForThisExtractor = new HashMap<Integer,Integer>();
		inputFileName = new String();
	}
	
	/**
	 * Main method for feature extraction
	 * @param args Feature extraction configuration
	 */
	public static void main(String[] args) {
		
		// Create the node scheduler
		ExtractorNodeScheduler thisScheduler = null;
		try {
			thisScheduler = new ExtractorNodeScheduler(args[0] + File.separator + "input" + File.separator + "task_" + args[1]);
		} catch(NodeException e) {
			AmuseLogger.write(ExtractorNodeScheduler.class.getName(), Level.ERROR,
					"Could not create folder for extractor node intermediate results: " + e.getMessage());
			return;
		}
		
		// Proceed the task
		thisScheduler.proceedTask(args);
		
		// Remove the folder for input and intermediate results
		try {
			thisScheduler.removeInputFolder();
		} catch(NodeException e) {
				AmuseLogger.write(ExtractorNodeScheduler.class.getClass().getName(), Level.WARN,
					"Could not remove properly the folder with intermediate results '" + 
					thisScheduler.nodeHome + File.separator + "input" + File.separator + "task_" + thisScheduler.jobId + 
					"; please delete it manually! (Exception: "+ e.getMessage() + ")");
		}
	}
	
	/**
	 * Performs feature extraction
	 * @param extractorConfiguration Feature extraction configuration
	 */
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration extractorConfiguration) {

		// --------------------------------------
		// (I) Configure extractor node scheduler
		// --------------------------------------
		this.nodeHome = nodeHome;
		if(this.nodeHome.startsWith(AmusePreferences.get(KeysStringValue.AMUSE_PATH))) {
			this.directStart = true;
		}
		this.jobId = new Long(jobId);
		this.taskConfiguration = extractorConfiguration;
		
		// If this node is started directly, the properties are loaded from AMUSEHOME folder;
		// if this node is started via command line (e.g. in a grid, the properties are loaded from
		// %trainer home folder%/input
		if(!this.directStart) {
			File preferencesFile = new File(this.nodeHome + File.separator + "config" + File.separator + "amuse.properties");
			AmusePreferences.restoreFromFile(preferencesFile);
			try {
				PluginLoader.loadPlugins(new File(this.nodeHome + File.separator + "lib" + File.separator + "plugins"));
			} catch(SchedulerException e) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Could not load the plugins from folder '" + 
						this.nodeHome + File.separator + "lib" + File.separator + "plugins': " + e.getMessage() + "; trying to proceed the task anyway..");
			}
		}
		
		
		// Set the music file name without music database directory path 
		String relativeName = new String();
		String musicDatabasePath = AmusePreferences.get(KeysStringValue.MUSIC_DATABASE);
		// Make sure music database path ends with file separator to catch tracks that have the data base path as suffix but are not in the database
		musicDatabasePath += musicDatabasePath.endsWith(File.separator) ? "" : File.separator;
		if(((ExtractionConfiguration)extractorConfiguration).getMusicFileList().getFileAt(0).startsWith(musicDatabasePath)) {
			relativeName = ((ExtractionConfiguration)extractorConfiguration).getMusicFileList().getFileAt(0).substring(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)).getPath().length());
		} else {
			relativeName = ((ExtractionConfiguration)extractorConfiguration).getMusicFileList().getFileAt(0);
		}
		if(relativeName.charAt(0) == File.separatorChar) {
			relativeName = relativeName.substring(1);
		}
		if(relativeName.endsWith(".mp3")) {
			// Cut extension
			relativeName = relativeName.substring(0,relativeName.length()-4);
			relativeName = new String(relativeName + ".wav");
		}
		
		this.inputFileName = relativeName;
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Extractor node scheduler for " + 
				this.inputFileName + " configured; starting decoding..");
	
		// --------------------------------
		// (II) Convert mp3 file to wave(s)
		// --------------------------------
		try {
			AudioFileConversion.processFile(new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId), 
					new File(((ExtractionConfiguration)this.taskConfiguration).getMusicFileList().getFileAt(0)));
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
				"Audio decoding error: " + e.getMessage());
			errorDescriptionBuilder.append(this.inputFileName);
			this.fireEvent(new NodeEvent(NodeEvent.EXTRACTION_FAILED, this));
			return;
		}

		AmuseLogger.write(this.getClass().getName(), Level.INFO, "..decoding completed!");
		
		// Find out the number of parts if the music file was splitted
		File file = new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId);
		if(!file.exists()) {
			System.out.println("No music files found: " + file.getAbsolutePath());
			errorDescriptionBuilder.append(this.inputFileName);
			this.fireEvent(new NodeEvent(NodeEvent.EXTRACTION_FAILED, this));
			return;
		}
		
		File[] files = file.listFiles();
		for(int i=0;i<files.length;i++) {
			if(files[i].isDirectory()) {
				this.numberOfParts++;
			}
		}
		
		// --------------------------------------
		// (III) Configure the extractor adapters 
		// --------------------------------------
		try {
			this.configureFeatureExtractors();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
				"Could not configure feature extractor(s): " + e.getMessage());
			errorDescriptionBuilder.append(this.inputFileName);
			this.fireEvent(new NodeEvent(NodeEvent.EXTRACTION_FAILED, this));
			return;
		}
		
		// -------------------------------------------------------------
		// (IV) Check if at least one extractor has been properly loaded
		// -------------------------------------------------------------		
	    if(this.extractors.size() == 0) {
    		AmuseLogger.write(this.getClass().getName(), Level.FATAL, 
    				"No extractor has been properly loaded, exiting the extractor node...");
			errorDescriptionBuilder.append(this.inputFileName);
			this.fireEvent(new NodeEvent(NodeEvent.EXTRACTION_FAILED, this));
			return;
	    }
		
		// --------------------------------
		// (V) Start the extractor adapters
		// --------------------------------		
		this.startFeatureExtractors();
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "All extractors finished their work");
		
		// ----------------------------------------------------------------------------------
		// (VI) If started directly, remove generated data and fire event for Amuse scheduler
		// ----------------------------------------------------------------------------------
		if(this.directStart) {
			try {
				this.cleanInputFolder();
			} catch(NodeException e) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Could not remove properly the intermediate results '" + 
					this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
			}
			this.fireEvent(new NodeEvent(NodeEvent.EXTRACTION_COMPLETED, this));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String[])
	 */
	public void proceedTask(String[] args) {
		if(args.length < 2) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL, 2 - args.length + 
					" arguments are missing; The usage is 'ExtractorNodeScheduler %1 %2', where: \n" +
					"%1 - Home folder of this node\n" +
					"%2 - Unique (for currently running Amuse instance) task Id\n"); 
			System.exit(1);
		}
		
		// Load the task configuration from %EXTRACTORHOME%/task.ser
		ExtractionConfiguration extractorConfig = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(args[0] + File.separator + "task_" + args[1] + ".ser");
			in = new ObjectInputStream(fis);
			extractorConfig = (ExtractionConfiguration)in.readObject();
		    in.close();
		} catch(IOException ex) {
		    ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Proceed the extractor task
		proceedTask(args[0],new Long(args[1]),extractorConfig);
	}
	
	/**
	 * Configures the extractor adapters
	 * @throws NodeException
	 */
	public void configureFeatureExtractors() throws NodeException {
		
		// HashMap with the custom scripts for each extractor tool
		HashMap<Integer, List<String>> tool2CustomScripts = new HashMap<Integer, List<String>>();
		// HashMap with the features that each custom script extracts
		HashMap<String, List<Feature>> customScript2Features = new HashMap<String, List<Feature>>();
		ArrayList<Integer> requiredExtractorIDs = new ArrayList<Integer>();
		// extractorIDs of extractors that extract regular features
		ArrayList<Integer> requiredRegularExtractorIDs = new ArrayList<Integer>();
		for(Feature feature : ((ExtractionConfiguration)this.taskConfiguration).getFeatureTable().getSelectedFeatures()) {
			if(!requiredExtractorIDs.contains(feature.getExtractorId()) ) {
				requiredExtractorIDs.add(feature.getExtractorId());
			}
			// if the feature has a custom configuration
			// it has to be added to tool2customScripts
			if(feature.getConfigurationId() != null) {
				int extractorId = feature.getExtractorId();
				String customScript = feature.getCustomScript();
				if(!tool2CustomScripts.containsKey(extractorId)){
					tool2CustomScripts.put(extractorId, new ArrayList<String>());
				}
				if(!tool2CustomScripts.get(extractorId).contains(customScript)) {
					tool2CustomScripts.get(extractorId).add(customScript);
				}
				if(!customScript2Features.containsKey(customScript)) {
					customScript2Features.put(customScript, new ArrayList<Feature>());
				}
				customScript2Features.get(customScript).add(feature);
			} else {
				if(!requiredRegularExtractorIDs.contains(feature.getExtractorId())) {
					requiredRegularExtractorIDs.add(feature.getExtractorId());
				}
			}
		}
		
		// Load the extractors table
		DataSetAbstract extractorTableSet;
	    try {
	    	if(this.directStart) {
	    		extractorTableSet = new ArffDataSet(new File(AmusePreferences.getFeatureExtractorToolTablePath()));
	    	} else {
	    		extractorTableSet = new ArffDataSet(new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "featureExtractorToolTable.arff"));
	    	}
			Attribute idAttribute = extractorTableSet.getAttribute("Id");
			Attribute extractorNameAttribute = extractorTableSet.getAttribute("Name");
			Attribute adapterClassAttribute = extractorTableSet.getAttribute("AdapterClass");
			Attribute homeFolderAttribute = extractorTableSet.getAttribute("HomeFolder");
			Attribute extractorStartScriptAttribute = extractorTableSet.getAttribute("StartScript");
			Attribute inputExtractorBatchAttribute = extractorTableSet.getAttribute("InputBatch");
			
			int highestId = 0;
			for(int i = 0; i < extractorTableSet.getValueCount(); i++) {
				int id = ((Double)idAttribute.getValueAt(i)).intValue();
				if(id > highestId) {
					highestId = id;
				}
			}
			int currentCustomId = highestId + 1;
			
			for(int i=0;i<extractorTableSet.getValueCount();i++) {

				// Load the adapters classes and configure them
				if(requiredExtractorIDs.contains(new Double(idAttribute.getValueAt(i).toString()).intValue())) {
					try {
						// prepare extractor
						
						Class<?> adapter = Class.forName(adapterClassAttribute.getValueAt(i).toString());
						
						ExtractorInterface ead = (ExtractorInterface)adapter.newInstance();
						Properties extractorProperties = new Properties();
						Integer idOfCurrentExtractor = new Double(idAttribute.getValueAt(i).toString()).intValue();
						
						// prepare the extractor for regular features if that is required
						if(requiredRegularExtractorIDs.contains(idOfCurrentExtractor)) {
							extractorProperties.setProperty("id",idOfCurrentExtractor.toString());
							extractorProperties.setProperty("extractorName",extractorNameAttribute.getValueAt(i).toString());
							extractorProperties.setProperty("extractorFolderName",homeFolderAttribute.getValueAt(i).toString());
							if(directStart) {
								extractorProperties.setProperty("extractorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + homeFolderAttribute.getValueAt(i));
							} else {
								extractorProperties.setProperty("extractorFolder",nodeHome + File.separator + "tools" + File.separator + homeFolderAttribute.getValueAt(i));
							}
							extractorProperties.setProperty("extractorStartScript",extractorStartScriptAttribute.getValueAt(i).toString());
							extractorProperties.setProperty("inputExtractorBatch",inputExtractorBatchAttribute.getValueAt(i).toString());
							((AmuseTask)ead).configure(extractorProperties,this,null);
							this.extractors.put(idOfCurrentExtractor,ead);
							
							AmuseLogger.write(this.getClass().getName(), Level.DEBUG, 
									"Extractor is configured: " + adapterClassAttribute.getValueAt(i));
							}
							
							// prepare extractor for features with custom configurations
							if(!tool2CustomScripts.containsKey(idOfCurrentExtractor)) {
								continue;
							}
							for(String customScript : tool2CustomScripts.get(idOfCurrentExtractor)) {
								adapter = Class.forName(adapterClassAttribute.getValueAt(i).toString());
								
								ead = (ExtractorInterface)adapter.newInstance();
								extractorProperties = new Properties();
								extractorProperties.setProperty("id",idOfCurrentExtractor.toString());
								extractorProperties.setProperty("extractorName",extractorNameAttribute.getValueAt(i).toString());
								extractorProperties.setProperty("extractorFolderName",homeFolderAttribute.getValueAt(i).toString());
								if(directStart) {
									extractorProperties.setProperty("extractorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + homeFolderAttribute.getValueAt(i));
								} else {
									extractorProperties.setProperty("extractorFolder",nodeHome + File.separator + "tools" + File.separator + homeFolderAttribute.getValueAt(i));
								}
								extractorProperties.setProperty("extractorStartScript",extractorStartScriptAttribute.getValueAt(i).toString());
								extractorProperties.setProperty("inputExtractorBatch",customScript);
								((AmuseTask)ead).configure(extractorProperties,this,null);
								this.extractors.put(currentCustomId,ead);
								currentCustomId++;
								
								AmuseLogger.write(this.getClass().getName(), Level.DEBUG, 
										"Extractor with custom script " + customScript + " is configured: " + adapterClassAttribute.getValueAt(i));
						}
						
					} catch(ClassNotFoundException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Extractor class cannot be located: " + adapterClassAttribute.getValueAt(i));
					} catch(IllegalAccessException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Extractor class or its nullary constructor is not accessible: " + adapterClassAttribute.getValueAt(i));
					} catch(InstantiationException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Instantiation failed for extractor class: " + adapterClassAttribute.getValueAt(i));
					}
				}
			}
	    } catch(IOException e) {
    		throw new NodeException("Extractor table cannot be parsed properly: " + e.getMessage());
	    }
	}
	
	/**
	 * Starts the extractor adapters
	 */
	public void startFeatureExtractors() {
		
		// For a music file name without path
	    String inputFileName = new String();
		inputFileName = this.inputFileName;
		if(inputFileName.lastIndexOf(File.separator) != -1) {
			inputFileName = inputFileName.substring(inputFileName.lastIndexOf(File.separator)+1);
		}
	    
	    // Start the extractor adapters
		Set<?> usedExtractorIDs = this.extractors.keySet();
		Iterator<?> it = usedExtractorIDs.iterator();
		while(it.hasNext()) {
			int i = (Integer)it.next();
			this.currentPartForThisExtractor.put(new Integer(((AmuseTask)this.extractors.
					get(i)).getProperties().getProperty("id")), 1);
		    
			// Start the feature extractors for all parts
			for(int currentPart = 1; currentPart <= this.numberOfParts; currentPart++) {
                String sep = File.separator;
				String musicInput = new String(this.nodeHome + sep + "input" + sep + "task_" + this.jobId + sep + currentPart + sep + inputFileName);
				String featureOutput = new String(this.nodeHome + sep + "input" + sep + "task_" + this.jobId + sep + currentPart + sep +
						inputFileName.substring(0,inputFileName.lastIndexOf(".")) + "_" +  
						((AmuseTask)this.extractors.get(i)).getProperties().getProperty("extractorName") + 
						"_features.arff"); 
				try {
					this.extractors.get(i).setFilenames(musicInput, featureOutput,currentPart);
					this.extractors.get(i).extractFeatures();
				} catch (NodeException e) {
					AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
							"Error occured during feature extraction with extractor '" + 
							((AmuseTask)this.extractors.get(i)).getProperties().getProperty("extractorName") + 
							"': " + e.getMessage());
				}
			}
			
			// Consolidate the part results and copy them to feature database
			consolidateResults(this.extractors.get(i));
		}
	}
	
	/**
	 * Copies the extracted features to feature database
	 * @param adapter Extractor 
	 */
	private void consolidateResults(ExtractorInterface adapter) {
		
		// -------------------------------------------------------------------------
		// (I) If the track was split, create a new arff feature file from several
		// -------------------------------------------------------------------------
		if(numberOfParts > 1) {
			File file = new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "1" + File.separator + 
					((AmuseTask)adapter).getProperties().getProperty("extractorFolderName"));
			File[] files = file.listFiles();
			
			// Create a folder for consolidated features
			File folder = new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "features" + File.separator);
			if (!folder.exists() && !folder.mkdirs()) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR,
						"Error creating temp folder; could not consolidate the features extracted by '" + 
						((AmuseTask)adapter).getProperties().getProperty("extractorName") + "'");
				extractors.remove(adapter);
				return;
			}
			
			// Go through all features (equal to the number of files for each part)
			for(int i=0;i<files.length;i++) {
				
				// TODO Problem for features from split files.
				// Currently hard-coded, better solution required!!!
				double duration_id_400=0.0d;
				boolean dataPartStarted=false;
				
				try {
					
					// Create the ARFF feature file for all features
					String featureFileName = files[i].toString().substring(files[i].toString().lastIndexOf(File.separator));
					FileOutputStream values_to = new FileOutputStream(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + 
							File.separator + "features" + File.separator + featureFileName);
					DataOutputStream values_writer = new DataOutputStream(values_to);
					String sep = System.getProperty("line.separator");
					
					values_writer.writeBytes("%This feature was calculated from splitted wave file\n");
					values_writer.writeBytes("%and the values of attribute WindowNumber were approximated\n");
					values_writer.writeBytes("%for windows after the first split.\n");
					values_writer.writeBytes(sep);
					
					// Find out the overall number of windows
					int columnNumber = 0;
					if(files[i].getName().toString().endsWith("_400.arff")) {
						columnNumber = 1;
					} else if(files[i].getName().toString().endsWith("_601.arff")) {
						File currentPartFile = new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "1" + File.separator + 
								((AmuseTask)adapter).getProperties().getProperty("extractorFolderName") + File.separator + files[i].getName());
						FileReader featuresInput = null;
						featuresInput = new FileReader(currentPartFile);
						BufferedReader featuresReader = new BufferedReader(featuresInput);
						String line =  new String();
			            // Save the feature values from the part file
						while ((line = featuresReader.readLine()) != null) {
							if(line.startsWith("%columns=")) {
								columnNumber = new Integer(line.substring(9));
								break;
							}
						}
					} else {
						for(int j=1;j<=numberOfParts;j++) {
							File currentPartFile = new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + j + File.separator + 
									((AmuseTask)adapter).getProperties().getProperty("extractorFolderName") + File.separator + files[i].getName());
							FileReader featuresInput = null;
							featuresInput = new FileReader(currentPartFile);
							BufferedReader featuresReader = new BufferedReader(featuresInput);
							String line =  new String();
				            // Save the feature values from the part file
							while ((line = featuresReader.readLine()) != null) {
								if(line.startsWith("%columns=")) {
									columnNumber += new Integer(line.substring(9));
									break;
								}
							}
						}
					}
					
					int lineCounter = 1;
						
					// Go through the splitted parts
					for(int j=1;j<=numberOfParts;j++) {
						
						// TODO v0.2 Problem for features from splitted files.
						// Currently hard-coded, better solution required!!!
						if(files[i].getName().toString().endsWith("_601.arff") && j>1) break;
						
						File currentPartFile = new File((this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + j + File.separator + 
								((AmuseTask)adapter).getProperties().getProperty("extractorFolderName") + File.separator + files[i].getName()));
						
						FileReader featuresInput = null;
						featuresInput = new FileReader(currentPartFile);
						BufferedReader featuresReader = new BufferedReader(featuresInput);

						String line =  new String();
						
						// If we are by second or higher part, do not write the arff header again,
						// wait till data part
						if(j>1) {
							line = featuresReader.readLine();
							while(!(line.startsWith("@DATA"))) {
								line = featuresReader.readLine();
								if(line == null) break;
							}
						}
						
			            // Save the feature values from the part file
						while ((line = featuresReader.readLine()) != null) {
							
							if(line.startsWith("%columns")) {
								line = new String("%columns=" + columnNumber);
							}
							
							// TODO v0.2 Problem for features from splitted files.
							// Currently hard-coded, better solution required!!!
							// Only the lines (a) and (b) should remain in the future!
							if(files[i].getName().toString().endsWith("_400.arff")) {
								if(dataPartStarted) { 
									duration_id_400 += new Double(line);
								} else {
									values_writer.writeBytes(line); 
									values_writer.writeBytes(sep);  
								}
							} else {
								if(files[i].getName().toString().endsWith("_408.arff")) {
									if(dataPartStarted) {
										Double beatTimesValue = new Double(line);
										// TODO 480 = maxSize from system.sh !
										beatTimesValue += (j-1)*480;
										line = beatTimesValue.toString();
									}
								} else if(files[i].getName().toString().endsWith("_416.arff")) {
									if(dataPartStarted) {	
										Double tatumTimesValue = new Double(line);
										tatumTimesValue += (j-1)*480;
										line = tatumTimesValue.toString();
									}
								} else if(files[i].getName().toString().endsWith("_419.arff")) {
									if(dataPartStarted) {	
										Double onsetTimesValue = new Double(line);
										onsetTimesValue += (j-1)*480;
										line = onsetTimesValue.toString();
									}
								} else if(dataPartStarted) {
									if(line.indexOf(",") != -1) {
										line = new String(line.substring(0,line.lastIndexOf(",")) + "," + lineCounter);
									}
									lineCounter++;
								}
								
								values_writer.writeBytes(line); // (a)
								values_writer.writeBytes(sep);  // (b)
							}
							
							// TODO v0.2Problem for features from splitted files.
							// Currently hard-coded, better solution required!!!
							if(line.startsWith("@DATA")) {
								dataPartStarted = true;
							}
			            }
						featuresReader.close();
					}
					
					if(files[i].getName().toString().endsWith("_400.arff")) {
						values_writer.writeBytes(new Double(duration_id_400).toString());
					}
					
					values_writer.close();
				} catch(Exception e) {
					AmuseLogger.write(this.getClass().getName(), Level.ERROR,
							"Could not consolidate the features extracted by '" + 
							((AmuseTask)adapter).getProperties().getProperty("extractorName") + "'");
					extractors.remove(adapter);
					return;
				}
 			}
		}
		
		// Create folders for features equal to folder structure of music database
		StringTokenizer paths2Add = new StringTokenizer(this.inputFileName,File.separator);
		ArrayList<String> pathsArray = new ArrayList<String>();
		while(paths2Add.hasMoreElements()) {
			pathsArray.add(new String(paths2Add.nextToken()));
		}
		
		// Go through the paths till the file name
		StringBuffer path2Create = new StringBuffer();
		
		// The first option can be used in a tool node for a more flexible storage of features
		if(copyDirectlyToDatabase) {
			path2Create.append(((ExtractionConfiguration)this.taskConfiguration).getFeatureDatabase());
		} else {
			for(int i=0;i<pathsArray.size();i++) {
				path2Create = new StringBuffer();
				path2Create.append(((ExtractionConfiguration)this.taskConfiguration).getFeatureDatabase());
				for(int j=0;j<i+1;j++) {
					path2Create.append(File.separator);
					
					// Cut the extension for the folder with music file name
					if(i == pathsArray.size()-1 && j == i) {
						int l = pathsArray.get(j).lastIndexOf(".");
						path2Create.append(pathsArray.get(j).substring(0,l));
					} else {
						path2Create.append(pathsArray.get(j));
					}
				}
			}
		}
	
		// Move the extracted features
		try {
			if(numberOfParts > 1) {
				FileOperations.move(new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "features" + File.separator), new File(path2Create.toString()) );
			} else {
				FileOperations.move(new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "1" + File.separator +  
						((AmuseTask)adapter).getProperties().getProperty("extractorFolderName")), 
						new File(path2Create.toString()));
			}
		} catch(IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Could not move the features extracted by '" + 
					((AmuseTask)adapter).getProperties().getProperty("extractorName") + "'");
			return;
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, 
				"Features extracted with '" + ((AmuseTask)adapter).getProperties().getProperty("extractorName") + "' are copied to feature database");
	}

}
