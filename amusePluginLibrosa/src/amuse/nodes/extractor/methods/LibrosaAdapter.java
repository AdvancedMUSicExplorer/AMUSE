/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2020 by code authors
 * 
 * Created at Dortmund University, Chair of Algorithm Engineering
 * (Contact: <http://ls11-www.cs.uni-dortmund.de>) 
 *
 * AMUSE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMUSE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Creation date: 12.07.2019
 */
package amuse.nodes.extractor.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

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
 * Adapter to song structure analyser.
 * 
 * @author Igor Vatolkin
 */
public class LibrosaAdapter extends AmuseTask implements ExtractorInterface {

	/** Input music file */
	private String musicFile;
	
	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#setFilenames(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
		this.musicFile = musicFile;
		this.currentPart = currentPart;
	}
	
	/**
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertBaseScript(HashMap)
	 */
	/**
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertBaseScript(HashMap)
	 */
	public void convertBaseScript(HashMap<Integer,Integer> feature2Tool, FeatureTable featureTable) throws NodeException {
		
		// First of all, a mapping from Amuse feature ID to its description is loaded
		HashMap<Integer,String> featureId2Description = new HashMap<Integer,String>();

		for(int i=0;i<featureTable.size();i++) {
			featureId2Description.put(featureTable.getFeatureAt(i).getId(),
					featureTable.getFeatureAt(i).getDescription());
		}
		
		// Load Librosa base XML script
		Document currentBaseScript = null;
		try {
			String inputBaseBatchPath = properties.getProperty("inputExtractorBaseBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBaseBatchPath.startsWith(File.separator)) {
		    	inputBaseBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBaseBatchPath;
		    }
			currentBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBaseBatchPath);
			
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot open Librosa base script: " + e.getMessage());
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
		
		// Save the modified script as python file (content found in "text" nodes will be written)
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
			throw new NodeException("Extraction with Librosa failed: could not create temp folder " + 
					folder.toString());
		}

		// Start Librosa
		try {
			List<String> commands = new ArrayList<String>();
			commands.add(AmusePreferences.get(KeysStringValue.PYTHON_PATH));
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			commands.add(inputBatchPath);
			commands.add(this.musicFile);
			commands.add(folder.getAbsolutePath());
			ExternalProcessBuilder librosa = new ExternalProcessBuilder(commands);
			librosa.setWorkingDirectory(new File(properties.getProperty("extractorFolder")));
			Process pc = librosa.start();

			pc.waitFor();
			// DEBUG Show the runtime outputs
			/*String s = null; 
			java.io.BufferedReader stdInput = new java.io.BufferedReader(new java.io.InputStreamReader(pc.getInputStream()));
			java.io.BufferedReader stdError = new java.io.BufferedReader(new java.io.InputStreamReader(pc.getErrorStream()));
			System.out.println("Here is the standard output of the command:\n"); 
			while ((s = stdInput.readLine()) != null) { System.out.println(s); } 
			System.out.println("Here is the standard error of the command (if any):\n"); 
			while ((s = stdError.readLine()) != null) { System.out.println(s); }*/
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "...Extraction succeeded");
		} catch (IOException e) {
			printStackTrace(e);
			//e.printStackTrace();
			throw new NodeException("Extraction with Librosa failed: " + e.getMessage());
		} catch (InterruptedException e) {
			throw new NodeException("Extraction with Librosa interrupted! " + e.getMessage());
		}

		// Convert the results to Amuse ARFF
		convertOutput();
	}
	
	public static void printStackTrace(Throwable t) {
		  System.out.println(t);
		  for (StackTraceElement ste : t.getStackTrace()) {
		    System.out.println("\tat " + ste);
		  }
		  Throwable cause = t.getCause();
		  if (cause != null) {
		    System.out.print("Caused by ");
		    printStackTrace(cause);
		  }
		}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertOutput()
	 */
	public void convertOutput() throws NodeException {
		// Conversion is not needed, since Librosa script writes output as Amuse ARFF
		// but files might need to be renamed, Librosa does not know which custom configurations were used
		
		// list of ids of custom features
		List<Integer> ids = new ArrayList<Integer>();
		// maps feature id to configuration id
		HashMap<Integer,String> idToConfiguration = new HashMap<Integer,String>();
		
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