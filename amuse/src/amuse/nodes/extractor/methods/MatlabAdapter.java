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
 * Creation date: 22.03.2007
 */
package amuse.nodes.extractor.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;

/**
 * Adapter to Matlab as feature extractor
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class MatlabAdapter extends AmuseTask implements ExtractorInterface {

	/** Input music file */
	private String musicFile;
	
	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;
	
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
		// First of all, a mapping from Amuse feature ID to its description is loaded
		HashMap<Integer,String> featureId2Description = new HashMap<Integer,String>();

		// Load feature table
		for(int i=0;i<featureTable.size();i++) {
			featureId2Description.put(featureTable.getFeatureAt(i).getId(),
					featureTable.getFeatureAt(i).getDescription());
		}
		
		// Load Matlab base script
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
					//start2Search = start2Search.getParentNode();
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
		BufferedWriter out = null;
		FileWriter fileWriter = null;
		try {
				String inputBatchPath = properties.getProperty("inputExtractorBatch");
				// if it is a relative path the input batch is in the extractor folder
			    if(!inputBatchPath.startsWith(File.separator)) {
			    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
			    }
				fileWriter = new FileWriter(inputBatchPath);
		        out = new BufferedWriter(fileWriter);
		        
		        nList = currentBaseScript.getElementsByTagName("text");
				for(int i=0;i<nList.getLength();i++) {
					Node node = nList.item(i);
					String content = node.getTextContent();
					if(content.contains("%AMUSEHOME%")) {
						content = content.replace("%AMUSEHOME%", AmusePreferences.get(KeysStringValue.AMUSE_PATH));
					}
					out.write(content);
				}
		} catch (IOException e) {
		    	e.printStackTrace();
		}
		finally{
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fileWriter != null){
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#extractFeatures()
	 */
	public void extractFeatures() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature extraction...");
		
		// Create a folder for Amuse feature files
		File folder = new File(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + 
				File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName"));
		if(!folder.exists() && !folder.mkdirs()) {
			throw new NodeException("Extraction with Matlab failed: could not create temp folder " + 
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
			commands.add("\"" + properties.getProperty("extractorFolder") + File.separator + "MatlabFeatures.log\"");
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
			//whileMatlabProcessAlive:
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
			        if(filename.toString().equals("MatlabFeatures.log")){
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
				    			throw new NodeException("Extraction with Matlab failed");
			        	    }
			        	} catch(FileNotFoundException e) {
		        	    	AmuseLogger.write(this.getClass().getName(), Level.WARN, "Unable to monitor the log-File from Matlab. " + e.getMessage());
		        	    	break;
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
			
			convertOutput();
			
		} catch (IOException e) {
        	throw new NodeException("Extraction with Matlab failed: " + e.getMessage());
        } 
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertOutput()
	 */
	public void convertOutput() throws NodeException {
		// Conversion is not needed, since Matlab script writes output as Amuse ARFF
		// but files might need to be renamed, Matlab does not know which custom configurations were used
		
		// list of ids of custom features
		List<Integer> ids = new ArrayList<Integer>();
		// maps feature id to configuration id
		HashMap<Integer,Integer> idToConfiguration = new HashMap<Integer,Integer>();
		
		FeatureTable featureTable = ((ExtractionConfiguration)this.correspondingScheduler.getConfiguration()).getFeatureTable();
		for(Feature feature : featureTable.getFeatures()) {
			if(feature.getCustomScript() != null && feature.getCustomScript().equals(properties.getProperty("inputExtractorBatch"))) {
				ids.add(feature.getId());
				idToConfiguration.put(feature.getId(), feature.getConfigurationId());
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
