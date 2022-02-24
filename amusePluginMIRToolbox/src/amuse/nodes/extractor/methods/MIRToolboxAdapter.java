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
 * Creation date: 03.09.2008
 */
package amuse.nodes.extractor.methods;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.data.io.DataSet;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;
import amuse.util.FileOperations;

/**
 * Adapter to MIR Toolbox as feature extractor
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class MIRToolboxAdapter extends AmuseTask implements ExtractorInterface {

	/** Input music file */
	private String musicFile;
	
	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;
	
	private boolean convertAttackSlopes;
	
	private boolean convertRiseTimes;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#setFilenames(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
		// musicFile need not to be saved to input script since Matlab reads the music files from a given directory and 
		// does not use music file names
		this.musicFile = musicFile;
		this.currentPart = currentPart;
	}
	
	/**
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertBaseScript(HashMap)
	 */
	public void convertBaseScript(HashMap<Integer,Integer> feature2Tool, FeatureTable featureTable) throws NodeException {
		
		// the features 429 and 430 are not extracted directly but converted from base features
		// one of the base features (duration of music piece) needs to be extracted with the matlab adapter
		boolean extractDurationWithMatlab = false;
		if(feature2Tool.containsKey(429)) {
			feature2Tool.put(400, 2);
			feature2Tool.put(423, 4);
			feature2Tool.put(426, 4);
			extractDurationWithMatlab = true;
		}
		if(feature2Tool.containsKey(430)) {
			feature2Tool.put(400, 2);
			feature2Tool.put(423, 4);
			feature2Tool.put(428, 4);
			extractDurationWithMatlab = true;
		}
		
		if(extractDurationWithMatlab) {
			// create matlab adapter
			MatlabAdapter matlabAdapter = new MatlabAdapter();
			Properties matlabProperties = new Properties();
			matlabProperties.setProperty("id", "2");
			matlabProperties.setProperty("extractorName", "Matlab");
			matlabProperties.setProperty("extractorFolder", AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator +"tools" + File.separator + "MatlabFeatures");
			matlabProperties.setProperty("inputExtractorBaseBatch", "matlabBase.xml");
			matlabProperties.setProperty("inputExtractorBatch", "matlabBaseModified_for_MIRToolbox.m");
			matlabAdapter.configure(matlabProperties, correspondingScheduler, "");
			
			// convert matlab base script
			matlabAdapter.convertBaseScript(feature2Tool, featureTable);
		}
		
		// Load MIRToolbox base script
		Document currentBaseScript = null;
		try {
			String inputBaseBatchPath = properties.getProperty("inputExtractorBaseBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBaseBatchPath.startsWith(File.separator)) {
		    	inputBaseBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBaseBatchPath;
		    }
			currentBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBaseBatchPath);
			
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot open Matlab base script: " + e.getMessage());
		} catch(javax.xml.parsers.ParserConfigurationException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: " + e.getMessage());
		} catch(org.xml.sax.SAXException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: " + e.getMessage());
		}

		// Search for all AmuseEnabler Nodes
		NodeList nList = currentBaseScript.getElementsByTagName("amuseEnableFeature");
		for(int i=0;i<nList.getLength();i++) {
			Node node = nList.item(i);
			NamedNodeMap attr = node.getAttributes();
			StringTokenizer idsTokenizer = new StringTokenizer(attr.getNamedItem("id").getNodeValue(),",");	
			ArrayList<Integer> idsOfCurrentEnabler = new ArrayList<Integer>();
			while(idsTokenizer.hasMoreElements()) {
				idsOfCurrentEnabler.add(new Integer(idsTokenizer.nextToken()));
			}
			
			// The subtree will be kept only if this extractor should extract the feature
			boolean enableSubTree = false;
			for(int j=0;j<idsOfCurrentEnabler.size();j++) {
				if(!feature2Tool.containsKey(idsOfCurrentEnabler.get(j))) {
					continue;
				}
				if(feature2Tool.get(idsOfCurrentEnabler.get(j)).toString().equals(properties.getProperty("id"))) {
					enableSubTree = true; break;
				}
			}
			
			if(!enableSubTree) {
				// Cut the XML subtree which extracts current feature(s) since it should not be supported by this extractor
				node.getParentNode().removeChild(node);
				
				// Important, since the list is updated after replacement!
				i--;
			} else {
				// Get the number of dimensions for each feature which are extracted in the subtree of this enabler
				// (e.g. RMS has 1 dimension, chroma vector has 24 dimensions..)
				StringTokenizer dimensionsTokenizer = new StringTokenizer(attr.getNamedItem("dimensions").getNodeValue(),",");	
				ArrayList<Integer> dimensionsOfFeaturesOfCurrentEnabler = new ArrayList<Integer>();
				while(dimensionsTokenizer.hasMoreElements()) {
					dimensionsOfFeaturesOfCurrentEnabler.add(new Integer(dimensionsTokenizer.nextToken()));
				}
				
				// Go through the list of features which are extracted in the subtree of this enabler
				for(int j=0;j<idsOfCurrentEnabler.size();j++) {
					if(feature2Tool.get(idsOfCurrentEnabler.get(j)).toString().equals(properties.getProperty("id"))) {
						
						AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Feature with ID '" + idsOfCurrentEnabler.get(j) + 
								"' will be extracted with " + properties.getProperty("extractorName"));
						
					}
				}
				
				Node parent = node.getParentNode(); 
				NodeList children = node.getChildNodes();
				for(int j=0;j<children.getLength();j++) {
					if(children.item(j).getNodeType() == Node.ELEMENT_NODE) {
						
						// Replace "amuseEnableNode" with child subtree which extracts the current feature
						parent.replaceChild(children.item(j),node);
					}
				}

				// Important, since the list is updated after replacement!
				i--;
				
				Node start2Search = parent;
				
				// Go upside in XML tree and look for amuseEnableTransform nodes. If they are found, replace them
				// with their subtrees since these transforms are needed for the extraction of features
				while(start2Search.getNodeType() != Node.DOCUMENT_NODE) {
					start2Search = start2Search.getParentNode();
					if(start2Search == null) break;
					
					// amuseEnableTransform found?
					if(start2Search.getNodeName().equals("amuseEnableTransform")) {
						Node parentOfEnableTransform = start2Search.getParentNode();
						NodeList childrenOfTransform = start2Search.getChildNodes();
						for(int j=0;j<childrenOfTransform.getLength();j++) {
							if(childrenOfTransform.item(j).getNodeType() == Node.ELEMENT_NODE) {
								
								// Replace "amuseEnableTransform" with child subtree which extracts somewhere the current feature(s)
								parentOfEnableTransform.replaceChild(childrenOfTransform.item(j),start2Search);
							}
						}
						start2Search = parentOfEnableTransform;
					}
				}
			} 
		}
		
		// Search for the rest of amuseEnableTransform nodes and delete them, since they do not include features to extract
		// with this Tool
		nList = currentBaseScript.getElementsByTagName("amuseEnableTransform");
		for(int i=0;i<nList.getLength();i++) {
			Node node = nList.item(i);
			node.getParentNode().removeChild(node);
			
			// Important, since the list is updated after replacement!
			i--;
		}
		
		// Save the modified script as matlab file (content found in "text" nodes will be written)
		try {
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			BufferedWriter out = new BufferedWriter(new FileWriter(inputBatchPath));
		    nList = currentBaseScript.getElementsByTagName("text");
			for(int i=0;i<nList.getLength();i++) {
				Node node = nList.item(i);
				out.write(node.getTextContent());
			}
		    out.close();
		} catch (IOException e) {
		    	e.printStackTrace();
		}
		
	}
	
	/**
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#extractFeatures()
	 */
	public void extractFeatures() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature extraction...");
		
		// Create a folder for Amuse feature files
		File folder = new File(
				this.correspondingScheduler.getHomeFolder() + "/input/task_" 
				+ this.correspondingScheduler.getTaskId() + 
				"/" + this.currentPart + "/" + properties.getProperty("extractorFolderName"));
		if(!folder.exists() && !folder.mkdirs()) {
			throw new NodeException("Extraction with MIRToolbox failed: could not create temp folder " + 
					folder.toString());
		}
				
		// Start Matlab
		try {
			List<String> commands = new ArrayList<String>();
			commands.add(AmusePreferences.get(KeysStringValue.MATLAB_PATH));
			commands.add("-nodisplay");
			commands.add("-nosplash");
			commands.add("-nojvm");
			commands.add("-r");
			File inputBatchFile = new File(properties.getProperty("inputExtractorBatch"));
			String inputBatchName = inputBatchFile.getName();
			inputBatchName = inputBatchName.substring(0, inputBatchName.lastIndexOf("."));
			String inputBatchFolder = properties.getProperty("extractorFolder");
			if(properties.getProperty("inputExtractorBatch").startsWith(File.separator)) {
				inputBatchFolder = inputBatchFile.getParent();
			}
			commands.add(inputBatchName + "('" + this.musicFile + "','" + folder + "')");
			commands.add("-logfile");
			commands.add("\"" + properties.getProperty("extractorFolder") + File.separator + "MIRToolbox.log\"");
			ExternalProcessBuilder matlab = new ExternalProcessBuilder(commands);
			matlab.setWorkingDirectory(new File(inputBatchFolder));
			matlab.setEnv("MATLABPATH", properties.getProperty("extractorFolder"));
			
			
			// Monitor the path that contains the log file
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path pathToWatch = FileSystems.getDefault().getPath(properties.getProperty("extractorFolder"));
			pathToWatch.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
						
			// Start the matlab process
			Process matlabProcess = matlab.start();
			
			// Monitor the log file as long as the process did not finish on its own.
			whileMatlabProcessAlive:
			while (matlabProcess.isAlive()) {
			    WatchKey key;
			    try {
			        key = watcher.poll(1, TimeUnit.SECONDS); // Waits until something has changed in the directory
			    } catch (InterruptedException x) {
			        continue;
			    }
			    if(key == null){
			    	continue;
			    }
	
			    for (WatchEvent<?> event: key.pollEvents()) {
			        WatchEvent.Kind<?> kind = event.kind();
			        
			        // OVERFLOW signals that an event may be lost, in which case we do not need to consider it.
			        if (kind == StandardWatchEventKinds.OVERFLOW) {
			            continue;
			        }
	
			        Path filename = ((WatchEvent<Path>)event).context(); // Get the filename of the modified/ new file.
	
			        // Only open the log if it was modified
			        if(filename.toString().equals("MIRToolbox.log")){
			        	File logFile = new File(pathToWatch.resolve(filename).toString());
			        	Scanner scanner = null;
			        	
			        	// Iterate through the file and search for errors.
			        	try {
			        	    scanner = new Scanner(logFile);
			        	    String errortext = "";
			        	    Boolean errorOccurred = false;
			        	    Boolean errorComplete = false;
			        	    
			        	    // When an error occurred, concatenate the whole error message
			        	    while (scanner.hasNextLine()) {
			        	        String line = scanner.nextLine();
			        	        errorOccurred = errorOccurred | line.contains("Error");
			        	        if(errorOccurred) { 
			        	            errortext += line + "\n";
			        	            if(line.contains("}")){ // The error message ends with "}"
			        	            	errorComplete = true;
			        	            	break;
			        	            }
			        	        }
			        	    }
			        	    
			        	    // If the complete error was written to the log file, the matlabProcess does not do anything anymore.
			        	    if(errorComplete){
			        	    	AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Output from the Matlab-log:\n" + errortext);
			        	    	try{
				    				watcher.close();
				    			}
				    			catch(IOException e){}
				    			matlabProcess.destroy();
			        	    	throw new NodeException("Extraction with MIR Toolbox failed");
			        	    }
			        	} catch(FileNotFoundException e) { 		        	    	
			        		AmuseLogger.write(this.getClass().getName(), Level.WARN, "Unable to monitor the log-File from MIR Toolbox. " + e.getMessage());
			        		break whileMatlabProcessAlive;
			        	} finally {
			        		if(scanner != null){
				        		scanner.close();
				        	}
						}
			        }
			    }
	
			    // Reset the key
			    boolean valid = key.reset();
			    if (!valid) {
			        break;
			    }
			}
			        	    
			try{
				watcher.close();
			}
			catch(IOException e){}
			

			try {
				matlabProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// see if attack slopes or rise times need to be converted
			// convert attack, slopes and rise times if needed
			String musicFileName = this.musicFile.substring(musicFile.lastIndexOf(File.separator) + 1, musicFile.lastIndexOf("."));
			String windowedAttackSlopesPath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_429.arff";
			String windowedRiseTimesPath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_430.arff";
			convertAttackSlopes = new File(windowedAttackSlopesPath).exists();
			convertRiseTimes = new File(windowedRiseTimesPath).exists();
			
			// for conversion the matlab feature duration of music piece is needed
			if(convertAttackSlopes || convertRiseTimes) {
				MatlabAdapter matlabAdapter = new MatlabAdapter();
				Properties matlabProperties = new Properties();
				matlabProperties.setProperty("id", "2");
				matlabProperties.setProperty("extractorName", "Matlab");
				matlabProperties.setProperty("extractorFolder", AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator +"tools" + File.separator + "MatlabFeatures");
				matlabProperties.setProperty("inputExtractorBaseBatch", "matlabBase.xml");
				matlabProperties.setProperty("inputExtractorBatch", "matlabBaseModified_for_MIRToolbox.m");
				matlabProperties.setProperty("extractorFolderName", this.properties.getProperty("extractorFolderName"));
				matlabAdapter.configure(matlabProperties, correspondingScheduler, "");
				matlabAdapter.setFilenames(musicFile, null, currentPart);
				matlabAdapter.extractFeatures();
			}
			
			convertOutput();
			
		} catch (IOException e) {
        	throw new NodeException("Extraction with MIR Toolbox failed: " + e.getMessage());
        }
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertOutput()
	 */
	public void convertOutput() throws NodeException {
		// convert attack, slopes and rise times if needed
		if(convertAttackSlopes) {
			convertAttackSlopes();
		}
		if(convertRiseTimes) {
			convertRiseTimes();
		}
		
		// convert custom features
		// list of ids of custom features
		List<Integer> ids = new ArrayList<Integer>();
		// maps feature id to configuration id
		HashMap<Integer,String> idToConfiguration = new HashMap<Integer,String>();
		
		FeatureTable featureTable = ((ExtractionConfiguration)this.correspondingScheduler.getConfiguration()).getFeatureTable();
		for(Feature feature : featureTable.getFeatures()) {
			if(feature.getCustomScript() != null && feature.getCustomScript().equals(properties.getProperty("inputExtractorBatch"))) {
				ids.add(feature.getId());
				idToConfiguration.put(feature.getId(), feature.getConfigurationId().toString());
			}
		}
		
		// rename files of custom features
		if(!ids.isEmpty()) {
			File folder = new File(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + 
					File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName"));
			File[] listOfFiles = folder.listFiles();
			if(listOfFiles == null) {
				listOfFiles = new File[0];
			}
			for(int id : ids) {
				// search for the feature file
				for(int i = 0; i < listOfFiles.length; i++) {
					File file = listOfFiles[i];
					if(file != null && file.getName().endsWith("_" + id + ".arff")) {
						String oldPath = file.getAbsolutePath();
						String newPath = oldPath.substring(0, oldPath.lastIndexOf(".")) + "_" + idToConfiguration.get(id) + ".arff";
						
						// rename the file
						file.renameTo(new File(newPath));
						
						// the file cannot be renamed twice
						// this could otherwise occur
						// if a configurationId ends
						// with the Id of another feature
						listOfFiles[i] = null;
						break;
					}
				}
			}
		}
	}
	
	private void convertAttackSlopes() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature conversion...");
		// convert the base features
		String musicFileName = this.musicFile.substring(musicFile.lastIndexOf(File.separator) + 1, musicFile.lastIndexOf("."));
		
		String durationOfMusicPiecePath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + 
				File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_400.arff";
		String attackTimesPath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + 
				File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_423.arff";
		String attackSlopesPath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() +
				File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_426.arff";
		
		DataSet durationOfMusicPieceDataSet = null;
		DataSet attackTimesDataSet = null;
		DataSet attackSlopesDataSet = null;
		try {
			durationOfMusicPieceDataSet = new DataSet(new File(durationOfMusicPiecePath));
			attackTimesDataSet = new DataSet(new File(attackTimesPath));
			attackSlopesDataSet = new DataSet(new File(attackSlopesPath));
		} catch (IOException e) {
			throw new NodeException(e.getLocalizedMessage());
		}
		
		double durationOfMusicPiece = (double)durationOfMusicPieceDataSet.getAttribute(0).getValueAt(0);
		int windowSize = 512;
		int stepSize = 512;
		int sampleRate = 22050;
		int numberOfWindows = (int)Math.ceil((durationOfMusicPiece*sampleRate - windowSize) / stepSize + 1);
		System.out.println(numberOfWindows);
		
		double[] attackSlopeWindows = new double[numberOfWindows];
		int currentAttack = -1;
		double currentSlope = Double.NaN;
		for(int i = 0; i < numberOfWindows; i++) {
			double middleOfWindow = i * ((double)stepSize/sampleRate) + ((double)windowSize/sampleRate)/2;
			while(currentAttack + 1 < attackTimesDataSet.getValueCount() && middleOfWindow >= (double)attackTimesDataSet.getAttribute(0).getValueAt(currentAttack + 1)) {
				currentAttack++;
				currentSlope = (double)attackSlopesDataSet.getAttribute(0).getValueAt(currentAttack);
			}
			attackSlopeWindows[i] = currentSlope;
		}
		
		String featureFilePath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_429.arff";
		File featureFile = new File(featureFilePath);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(featureFile);
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
			
			bufferedWriter.write("@RELATION 'Music feature'\n");
			bufferedWriter.write("%rows=1\n");
			bufferedWriter.write("%columns=" + numberOfWindows + "\n");
			bufferedWriter.write("%sample_rate=" + sampleRate +"\n");
			bufferedWriter.write("%window_size=" + windowSize + "\n");
			bufferedWriter.write("%step_size=" + stepSize + "\n");
			bufferedWriter.write("\n");
			bufferedWriter.write("@ATTRIBUTE 'Attack slopes as windowed numeric' NUMERIC\n");
			bufferedWriter.write("@ATTRIBUTE WindowNumber NUMERIC\n");
			bufferedWriter.write("\n");
			bufferedWriter.write("@DATA\n");
			
			for(int i = 0; i < attackSlopeWindows.length; i++) {
				bufferedWriter.write(attackSlopeWindows[i] + "," + (i+1));
				if(i < attackSlopeWindows.length - 1) {
					bufferedWriter.write("\n");
				}
			}
			bufferedWriter.close();
			
		} catch (IOException e) {
			throw new NodeException(e.getLocalizedMessage());
		}
	}
	
	private void convertRiseTimes() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature conversion...");
		// convert the base features
		String musicFileName = this.musicFile.substring(musicFile.lastIndexOf(File.separator) + 1, musicFile.lastIndexOf("."));
		
		String durationOfMusicPiecePath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + 
				File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_400.arff";
		String attackTimesPath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + 
				File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_423.arff";
		String riseTimesPath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() +
				File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_428.arff";
		
		DataSet durationOfMusicPieceDataSet = null;
		DataSet attackTimesDataSet = null;
		DataSet riseTimesDataSet = null;
		try {
			durationOfMusicPieceDataSet = new DataSet(new File(durationOfMusicPiecePath));
			attackTimesDataSet = new DataSet(new File(attackTimesPath));
			riseTimesDataSet = new DataSet(new File(riseTimesPath));
		} catch (IOException e) {
			throw new NodeException(e.getLocalizedMessage());
		}
		
		double durationOfMusicPiece = (double)durationOfMusicPieceDataSet.getAttribute(0).getValueAt(0);
		int windowSize = 512;
		int stepSize = 512;
		int sampleRate = 22050;
		int numberOfWindows = (int)Math.ceil((durationOfMusicPiece*sampleRate - windowSize) / stepSize + 1);
		
		double[] riseTimeWindows = new double[numberOfWindows];
		int currentAttack = -1;
		double currentRiseTime = Double.NaN;
		for(int i = 0; i < numberOfWindows; i++) {
			double middleOfWindow = i * ((double)stepSize/sampleRate) + ((double)windowSize/sampleRate)/2;
			while(currentAttack + 1 < attackTimesDataSet.getValueCount() && middleOfWindow >= (double)attackTimesDataSet.getAttribute(0).getValueAt(currentAttack + 1)) {
				currentAttack++;
				currentRiseTime = (double)riseTimesDataSet.getAttribute(0).getValueAt(currentAttack);
			}
			riseTimeWindows[i] = currentRiseTime;
		}
		
		String featureFilePath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName") + File.separator + musicFileName + "_430.arff";
		File featureFile = new File(featureFilePath);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(featureFile);
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
			
			bufferedWriter.write("@RELATION 'Music feature'\n");
			bufferedWriter.write("%rows=1\n");
			bufferedWriter.write("%columns=" + numberOfWindows + "\n");
			bufferedWriter.write("%sample_rate=" + sampleRate +"\n");
			bufferedWriter.write("%window_size=" + windowSize + "\n");
			bufferedWriter.write("%step_size=" + stepSize + "\n");
			bufferedWriter.write("\n");
			bufferedWriter.write("@ATTRIBUTE 'Rise times as windowed numeric' NUMERIC\n");
			bufferedWriter.write("@ATTRIBUTE WindowNumber NUMERIC\n");
			bufferedWriter.write("\n");
			bufferedWriter.write("@DATA\n");
			
			for(int i = 0; i < riseTimeWindows.length; i++) {
				bufferedWriter.write(riseTimeWindows[i] + "," + (i+1));
				if(i < riseTimeWindows.length - 1) {
					bufferedWriter.write("\n");
				}
			}
			bufferedWriter.close();
			
		} catch (IOException e) {
			throw new NodeException(e.getLocalizedMessage());
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
		// Do nothing, since initialization is not required	
	}
}