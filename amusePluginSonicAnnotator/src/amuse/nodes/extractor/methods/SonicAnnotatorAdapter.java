/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2012 by code authors
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
 * Creation date: 14.04.2012
 */
package amuse.nodes.extractor.methods;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import amuse.nodes.extractor.methods.nnlsderived.NNLSDerivedFeature;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;

/**
 * Adapter to NNLS Chroma SonicAnnotator feature extractor. 
 * For further details of SonicAnnotator see <a href="http://www.omras2.org/SonicAnnotator">http://www.omras2.org/SonicAnnotator</a>
 * For further details of NNLS see <a href="http://www.isophonics.net/nnls-chroma">http://www.isophonics.net/nnls-chroma</a>
 *  
 * @author Daniel Stoller
 * @version $Id: $
 */
public class SonicAnnotatorAdapter extends AmuseTask implements ExtractorInterface {

	/** Input music file */
	private String musicFile;
	
	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;
	
	/** Path to the desired arff output from Sonic Annotator */
	private String outputFeatureFile;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#setFilenames(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
		this.musicFile = musicFile;
		this.currentPart = currentPart;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertBaseScript(java.util.HashMap, amuse.data.FeatureTable)
	 */
	public void convertBaseScript(HashMap<Integer, Integer> feature2Tool,
			FeatureTable featureTable) throws NodeException {
		
		// Load Sonic Annotator base script
		Document currentBaseScript = null;
		try {
			String inputBaseBatchPath = properties.getProperty("inputExtractorBaseBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBaseBatchPath.startsWith(File.separator)) {
		    	inputBaseBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBaseBatchPath;
		    }
			currentBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBaseBatchPath);
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot open Sonic Annotator base script: " + e.getMessage());
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
						// Insert the child node before "amuseEnableNode" node
						parent.insertBefore(children.item(j), node);
					}
				}
				parent.removeChild(node);
				
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
		
		// Save the modified script
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource domsource = new DOMSource(currentBaseScript);
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			File modifiedScript = new File(inputBatchPath);
			if (modifiedScript.exists())
				if (!modifiedScript.canWrite()) {
					throw new NodeException(
							"Cannot write to modified Sonic Annotator base script");
				}
			if (!modifiedScript.exists())
				modifiedScript.createNewFile();
			StreamResult result = new StreamResult(modifiedScript);
			transformer.transform(domsource, result);
		} catch (javax.xml.transform.TransformerConfigurationException e) {
			throw new NodeException("Cannot transform Sonic Annotator base script: " + e.getMessage());
		} catch (java.io.IOException e) {
			throw new NodeException("Cannot save transformed Sonic Annotator base script: " + e.getMessage());
		} catch (javax.xml.transform.TransformerException e) {
			throw new NodeException("Cannot transform Sonic Annotator base script: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertOutput()
	 */
	public void convertOutput() throws NodeException {
		// maps feature id to custom feature
		HashMap<Integer,Feature> idToCustomFeature = new HashMap<Integer,Feature>();
		FeatureTable featureTable = ((ExtractionConfiguration)this.correspondingScheduler.getConfiguration()).getFeatureTable();
		for(Feature feature : featureTable.getFeatures()) {
			if(feature.getCustomScript() != null && feature.getCustomScript().equals(properties.getProperty("inputExtractorBatch"))) {
				idToCustomFeature.put(feature.getId(), feature);
			}
		}
		
		String musicFileName = this.musicFile.substring(musicFile.lastIndexOf(File.separator) + 1, musicFile.lastIndexOf("."));
		
		// Create a folder for Amuse feature files
		File folder = new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + 
				"/" + this.currentPart + "/" + properties.getProperty("extractorFolderName"));
		if(!folder.exists() && !folder.mkdirs()) {
			throw new NodeException("Extraction with Sonic Annotator failed: could not create temp folder " + 
					folder.toString());
		}
		
		// Load Sonic Annotator modified script
		Document modifiedBaseScript = null;
		try {
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			modifiedBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBatchPath);
		} catch(Exception e) {
			throw new NodeException("Cannot open Sonic Annotator modified script: " + e.getMessage());
		} 
		
		// Go through all extracted feature files which must be converted to AMUSE ARFF
		NodeList nList = modifiedBaseScript.getElementsByTagName("convertExtractedFeature");
		for(int k=0;k<nList.getLength();k++) {
			// Retrieve the path of the *.csv feature output file of Sonic Annotator
			Node node = nList.item(k);
			NamedNodeMap attr = node.getAttributes();
			Integer id = new Integer(attr.getNamedItem("id").getNodeValue());
			String filename = attr.getNamedItem("filename").getNodeValue();
			String outputPath = outputFeatureFile + File.separator + musicFileName + "_" + filename;
			
			// Retrieve current Feature
			Feature currentFeature = null;
			// if the current feature was extracted using a custom configuration
			if(idToCustomFeature.containsKey(id)) {
				currentFeature = idToCustomFeature.get(id);
			} else {
				// if the feature was extracted using the standard configuration
				for(Feature feature : featureTable.getFeatures()) {
					if(feature.getCustomScript() == null && feature.getId() == id) {
						currentFeature = feature;
					}
				}
			}
			
			// For AMUSE feature file (TMP in the folder name is omitted)
			String outputAmuseFeature = outputFeatureFile.substring(0,outputFeatureFile.length()-3) + 
				File.separator + musicFileName + "_" + id + (currentFeature.getConfigurationId() == null ? "" : "_" + currentFeature.getConfigurationId()) + ".arff";
			FileReader featuresInput = null;
			BufferedReader featuresReader = null;
			int rows;
			String[] attributeTypes;
			try {
				// Retrieve the number of columns (representing points of time) and the attribute types of the *.csv feature output file
				rows = count(outputPath);
				attributeTypes = getAttributeTypes(outputPath);
				
				featuresInput = new FileReader(new File(outputPath));
				featuresReader = new BufferedReader(featuresInput);
			} catch (FileNotFoundException e) {
				throw new NodeException("Could not find the file with extracted features: " + e.getMessage());
			} catch (IOException e) {
				throw new NodeException("Could not read the file with extracted features: " + e.getMessage());
			}
			
			// Check if the current feature has a correct window size (not -1) and thus needs an additional attribute
			boolean hasWindows = (currentFeature.getSourceFrameSize() > 0);
			int columns = currentFeature.getDimension();
			
			// Start writing the arff file
			FileOutputStream values_to = null;
			DataOutputStream writer = null;
			try {
				values_to = new FileOutputStream(new File(outputAmuseFeature));
				writer = new DataOutputStream(values_to);
				String sep = System.getProperty("line.separator");
								
				// Start off by describing the feature (name, rows, columns, sample_rate, window_size)
				writer.writeBytes("@RELATION 'Music feature'" + sep + sep
						+ "%rows=" + rows + sep
						+ "%columns=" + columns + sep 
						+ "%sample_rate=" + "22050" + sep /*Currently hardcoded to 22050Hz, variable implementation: currentFeature.getSampleRate()*/ 
						+ "%window_size=" + currentFeature.getSourceFrameSize() + sep
						+ "%step_size=" + currentFeature.getSourceStepSize() + sep + sep);
				
				// Write attribute declarations
				// Time attribute needed if there are no windows 
				if(!hasWindows) {
					writer.writeBytes("@ATTRIBUTE Time NUMERIC" + sep);
				}
				
				for(int i=0;i<attributeTypes.length;i++) {
					writer.writeBytes("@ATTRIBUTE '" + currentFeature.getDescription() + "' " + attributeTypes[i] + sep); 
				}
				// Window attribute only necessary if the feature has windows
				if(hasWindows) {
					writer.writeBytes("@ATTRIBUTE WindowNumber NUMERIC" + sep);
				}
				writer.writeBytes(sep);
				
				// Write the data
				writer.writeBytes("@DATA\n");
				String input = null;
				int i = 1;
				while((input = featuresReader.readLine()) != null) {
					// Go through every line
					StringTokenizer str = new StringTokenizer(input, ",");
					if(hasWindows) {
						str.nextToken(); //Skip the first attribute (time) if the feature has windows
						while(str.hasMoreElements()) {
							writer.writeBytes(str.nextToken()+",");
						}
						writer.writeBytes(new Integer(i).toString()); // Append the window attribute
					}
					else {
						writer.writeBytes(input); // If there are no windows, no changes are needed
					}
					writer.writeBytes(sep);
					i++;
				}
				writer.close();
				values_to.close();
				
			}
			catch(IOException e) {
				throw new NodeException("Could not read the csv output from Sonic Annotator: " + e.getMessage());
			}
			finally{
				if(writer != null){
					try {
						writer.close();
					} catch (IOException e) {
		                AmuseLogger.write(SonicAnnotatorAdapter.class.getName(), Level.DEBUG, "Could not close a DataOutputStream.");
					}
				}
				if(values_to != null){
					try {
						values_to.close();
					} catch (IOException e) {
		                AmuseLogger.write(SonicAnnotatorAdapter.class.getName(), Level.DEBUG, "Could not close a FileOutputStream.");
					}
				}
			}

			try {
				featuresReader.close();
			} catch (IOException e) {
				throw new NodeException("Could not close the input stream: " + e.getMessage());
			}
			try {
				featuresInput.close();
			} catch (IOException e) {
				throw new NodeException("Could not close the input stream: " + e.getMessage());
			}
		}
	}

	/**
	 * Takes the path to a comma-seperated value file generated by Sonic Annotator
	 * and determines for each comma-seperated value (column) whether the value is
	 * a string or a number.
	 * Returns the results in a string array, in which each field corresponds
	 * to one value and contains "STRING" or "NUMERIC" depending on its type.
	 * The first column containing the timestamp is ignored and its type not saved into the array.
	 * @param csvFile Path to a comma-seperated value file
	 * @return String array containing "STRING"/"NUMERIC" for each column depending on its type.
	 * @throws NodeException
	 */
	private String[] getAttributeTypes(String csvFile) throws NodeException {
		FileReader featuresInput = null;
		BufferedReader featuresReader = null;
		try {
			featuresInput = new FileReader(new File(csvFile));
			featuresReader = new BufferedReader(featuresInput);
			String line = featuresReader.readLine();
			StringTokenizer strTok = new StringTokenizer(line, ",");
			int columns = strTok.countTokens() - 1; // Decrement, because the time column is being handled in convertOutput
			strTok.nextToken(); // Skip the corresponding attribute type of the time column
			String[] attributeTypes = new String[columns];
			for(int i=0;i<columns;i++)
			{
				attributeTypes[i] = strTok.nextToken().contains("\"") ? "STRING" : "NUMERIC"; // String values appear with "
			}
			featuresReader.close();
			featuresInput.close();
			return attributeTypes;
		} catch (FileNotFoundException e) {
			throw new NodeException("Could not find the file with extracted features: " + e.getMessage());
		} catch (IOException e) {
			throw new NodeException("Could not read the file with extracted features: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#extractFeatures()
	 */
	public void extractFeatures() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature extraction...");
		
		// Create a folder for Sonic Annotator temporary feature files
		File folder = new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + 
				"/" + this.currentPart + "/" + properties.getProperty("extractorFolderName") + "TMP");
		if(!folder.exists() && !folder.mkdirs()) {
			throw new NodeException("Extraction with Sonic Annotator failed: could not create temp folder " + 
					folder.toString());
		}
				
		// Go through the modified base script and start Sonic Annotator for each feature
		try {
			
			// Load Sonic Annotator modified script
			Document modifiedBaseScript = null;
			try {
				String inputBatchPath = properties.getProperty("inputExtractorBatch");
				// if it is a relative path the input batch is in the extractor folder
			    if(!inputBatchPath.startsWith(File.separator)) {
			    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
			    }
				modifiedBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBatchPath);
			} catch(Exception e) {
				throw new NodeException("Cannot open Sonic Annotator modified script: " + e.getMessage());
			} 
			
			// Search for all features which must be extracted by Sonic Annotator directly
			NodeList nList = modifiedBaseScript.getElementsByTagName("sonicAnnotatorCommand");
			for(int i=0;i<nList.getLength();i++) {
				Node node = nList.item(i);
				String input = node.getTextContent();
				
				// Build the command needed to run the specific task with Sonic Annotator
				List<String> commands = new ArrayList<String>();
				commands.add(properties.getProperty("extractorFolder") + File.separator + properties.getProperty("extractorStartScript")); // Path to Sonic Annotator executable
				commands.add("-d");
				commands.add(input);
				commands.add(this.musicFile); // Path to the music file which Sonic Annotator should process
				commands.add("-w");
				commands.add("csv"); // Set the output format
				commands.add("--csv-basedir");
				commands.add(folder.getAbsolutePath()); // Set the output folder
				ExternalProcessBuilder sonic = new ExternalProcessBuilder(commands);
				sonic.setWorkingDirectory(new File(properties.getProperty("extractorFolder")));
				// Set VAMP_PATH environment variable to the "Plugins" folder so Sonic Annotator finds the NNLS-Chroma Plugin
				sonic.setEnv("VAMP_PATH", properties.getProperty("extractorFolder")+File.separator+"Plugins");
				Process pc = sonic.start();
				
			    pc.waitFor(); // Wait for Sonic Annotator to finish
			}
		} catch (IOException e) {
        	throw new NodeException("Extraction with Sonic Annotator failed: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new NodeException("Extraction with Sonnic Annotator interrupted! " + e.getMessage());
        }
        outputFeatureFile = folder.getAbsolutePath(); // Remember the output path for later use as input file in convertOutput
        convertOutput(); // Convert output comma-seperated value file to ARFF
        extractAMUSEFeatures(); // Convert features from Sonic Annotator into (possibly higher-level) AMUSE features
	}

	/**
	 * Extracts the AMUSE features based on previously extracted Sonic Annotator features
	 */
	private void extractAMUSEFeatures() throws NodeException {
		// maps feature id to custom feature
		HashMap<Integer,Feature> idToCustomFeature = new HashMap<Integer,Feature>();
		FeatureTable featureTable = ((ExtractionConfiguration)this.correspondingScheduler.getConfiguration()).getFeatureTable();
		for(Feature feature : featureTable.getFeatures()) {
			if(feature.getCustomScript() != null && feature.getCustomScript().equals(properties.getProperty("inputExtractorBatch"))) {
				idToCustomFeature.put(feature.getId(), feature);
			}
		}
		
		String musicFileName = this.musicFile.substring(musicFile.lastIndexOf(File.separator) + 1, musicFile.lastIndexOf("."));
		
		// Create a folder for Amuse feature files
		File folder = new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + 
				"/" + this.currentPart + "/" + properties.getProperty("extractorFolderName"));
		if(!folder.exists() && !folder.mkdirs()) {
			throw new NodeException("Extraction with Sonic Annotator failed: could not create temp folder " + 
					folder.toString());
		}
		
		// Load Sonic Annotator modified script
		Document modifiedBaseScript = null;
		try {
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			modifiedBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBatchPath);
		} catch(Exception e) {
			throw new NodeException("Cannot open Sonic Annotator modified script: " + e.getMessage());
		} 
		
		// Search for all features which must be extracted by Sonic Annotator directly
		NodeList nList = modifiedBaseScript.getElementsByTagName("extractFeature");
		for(int i=0;i<nList.getLength();i++) {
			Node node = nList.item(i);
			String featureClassString = node.getTextContent();
			NamedNodeMap attr = node.getAttributes();
			Integer id = new Integer(attr.getNamedItem("id").getNodeValue());
			String source = new String(attr.getNamedItem("source").getNodeValue());
			String sourceFeatureFile = folder + "TMP" + File.separator + musicFileName + "_" + source;
			
			// Retrieve current Feature
			Feature currentFeature = null;
			// if the current feature was extracted using a custom configuration
			if(idToCustomFeature.containsKey(id)) {
				currentFeature = idToCustomFeature.get(id);
			} else {
				// if the feature was extracted using the standard configuration
				for(Feature feature : featureTable.getFeatures()) {
					if(feature.getCustomScript() == null && feature.getId() == id) {
						currentFeature = feature;
					}
				}
			}
			
			try {
				Class<?> featureClass = Class.forName(featureClassString);
				NNLSDerivedFeature featureToEstimate = (NNLSDerivedFeature)featureClass.newInstance();
				featureToEstimate.extractFeature(sourceFeatureFile, folder + File.separator + musicFileName + "_" + id + (currentFeature.getConfigurationId() == null ? "" : "_" + currentFeature.getConfigurationId()) + ".arff");
			} catch (Exception e) {
				throw new NodeException("Cannot convert Sonic Annotator feature file to AMUSE feature file: " + e.getMessage());
			}
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.methods.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Nothing to initialize
		
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.methods.AmuseTaskInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Nothing to set
		
	}
	
	/**
	 * Takes the path to a file and efficiently counts the number of lines seperated by '\n'
	 * @param filename Path to a file
	 * @return Number of lines the file contains
	 * @throws IOException
	 */
	private int count(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        return count;
	    } finally {
	        is.close();
	    }
	}

}