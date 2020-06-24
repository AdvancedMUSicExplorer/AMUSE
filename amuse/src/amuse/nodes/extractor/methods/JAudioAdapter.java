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
 * Creation date: 11.12.2006
 */
package amuse.nodes.extractor.methods;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.converters.ArffLoader;
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
import java.util.List;

/**
 * Adapter to jAudio feature extractor. For further details of jAudio see <a href="http://jaudio.sourceforge.net/">http://jaudio.sourceforge.net/</a>
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class JAudioAdapter extends AmuseTask implements ExtractorInterface {

	/** Input music file */
	private String musicFile;
	
	/** File with the extracted jAudio features */
	private String outputFeatureFile;
	
	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#setFilenames(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
		this.currentPart = currentPart;
		
		// Load the jAudio batch script
		Document jAudioScript;
		try {
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
		    // if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			jAudioScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBatchPath);
		} catch (SAXException e) {
			throw new NodeException("Setting of input music file with jAudio failed: " + e.getMessage());		
		} catch (IOException e) {
			throw new NodeException("Setting of input music file with jAudio failed: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new NodeException("Setting of input music file with jAudio failed: " + e.getMessage());
		}
		
		// Set the input music file in the batch script
		NodeList nList = jAudioScript.getElementsByTagName("fileSet");
		Node node = nList.item(0);
		NodeList children = node.getChildNodes();
		for(int j=0;j<children.getLength();j++) {
			if(children.item(j).getNodeType() == Node.ELEMENT_NODE && children.item(j).getNodeName().equals("file")) {
				Node feature2Change = children.item(j);
				feature2Change.setTextContent(musicFile);
			}
		}
		this.musicFile = musicFile;
		
		// Set the output feature file in the batch script
		nList = jAudioScript.getElementsByTagName("destination");
		if(nList.getLength() < 2) {
			throw new NodeException("Cannot set the name of output ARFF file. Please check " + 
					properties.getProperty("inputExtractorBatch") + ".");
		}
		node = nList.item(0);
		node.setTextContent(outputFeatureFile.substring(0,outputFeatureFile.lastIndexOf(File.separator)+1) +
			"feature_def_rms512.arff");
		node = nList.item(1);
		node.setTextContent(outputFeatureFile);
		this.outputFeatureFile = outputFeatureFile;
		
		// Save the modified jAudio batch script
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "jAudio.dtd");
			DOMSource domsource = new DOMSource(jAudioScript);
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
		    // if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			File modifiedScript = new File(inputBatchPath);
			
			if (modifiedScript.exists())
				if (!modifiedScript.canWrite())
					throw new NodeException("Cannot write to "
							+ modifiedScript + ".");
			
			if (!modifiedScript.exists())
				modifiedScript.createNewFile();
			StreamResult result = new StreamResult(modifiedScript);
			transformer.transform(domsource,result);
		} catch(javax.xml.transform.TransformerConfigurationException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Cannot transform jAudio input script: " + e.getMessage());
		} catch(java.io.IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Cannot save transformed jAudio input script: " + e.getMessage());
		} catch(javax.xml.transform.TransformerException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Cannot transform jAudio input script: " + e.getMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertBaseScript(java.util.HashMap)
	 */
	public void convertBaseScript(HashMap<Integer,Integer> feature2Tool, FeatureTable featureTable) throws NodeException {
		// Load jAudio base script
		Document currentBaseScript = null;
		try {
			String inputBaseBatchPath = properties.getProperty("inputExtractorBaseBatch");
		    // if it is a relative path the input batch is in the extractor folder
		    if(!inputBaseBatchPath.startsWith(File.separator)) {
		    	inputBaseBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBaseBatchPath;
		    }
			currentBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBaseBatchPath);
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot open jAudio base script: " + e.getMessage());
		} catch(javax.xml.parsers.ParserConfigurationException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: " + e.getMessage());
		} catch(org.xml.sax.SAXException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: " + e.getMessage());
		}

		int currentFeatureID = 0;
		
		// Search for all AmuseEnabler Nodes
		NodeList nList = currentBaseScript.getElementsByTagName("amuseEnableFeature");
		for(int i=0;i<nList.getLength();i++) {
			Node node = nList.item(i);
			NamedNodeMap attr = node.getAttributes();
			Integer idOfCurrentEnabler = new Integer(attr.getNamedItem("id").getNodeValue());
			
			// If the feature with ID mentioned in current enabler should not be extracted with jAudio,
			// just delete the enabler node
			if(!feature2Tool.containsKey(idOfCurrentEnabler)) {
				Node parent = node.getParentNode(); 
				NodeList children = node.getChildNodes();
				for(int j=0;j<children.getLength();j++) {
					if(children.item(j).getNodeType() == Node.ELEMENT_NODE) {
						
						// Replace "amuseEnableNode" with child subtree 
						parent.replaceChild(children.item(j),node);
					}
				}
				
				// Important, since the list is updated after replacement!
				i--;
				continue;
			}
			if(feature2Tool.get(idOfCurrentEnabler) == currentFeatureID) {
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Feature with ID '" + idOfCurrentEnabler + 
						"' will be extracted with " + properties.getProperty("extractorName"));
				NodeList children = node.getChildNodes();
				for(int j=0;j<children.getLength();j++) {
					if(children.item(j).getNodeType() == Node.ELEMENT_NODE && children.item(j).getNodeName().equals("feature")) {
						Node feature2Change = children.item(j);
						NodeList featureDescription = feature2Change.getChildNodes();
						for(int k=0;k<featureDescription.getLength();k++) {
							if(featureDescription.item(k).getNodeType() == Node.ELEMENT_NODE && featureDescription.item(k).getNodeName().equals("active")) {
								Node activeFlag = featureDescription.item(k);
								activeFlag.setTextContent("true");
							}
						}
						
						Node parent = node.getParentNode(); 
						parent.replaceChild(feature2Change,node);
						
						// Important, since the list is updated after replacement!
						i--;
					}
				}
			} 

			// If the feature should not be extracted by jAudio, just delete amuseEnableFeature node
			else {
				NodeList children = node.getChildNodes();
				for(int j=0;j<children.getLength();j++) {
					if(children.item(j).getNodeType() == Node.ELEMENT_NODE && children.item(j).getNodeName().equals("feature")) {
						Node feature2Change = children.item(j);
						Node parent = node.getParentNode(); 
						parent.replaceChild(feature2Change,node);
						
						// Important, since the list is updated after replacement!
						i--;
					}
				}
			}
		}
		
		// Save the modified script
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "jAudio.dtd");
			DOMSource domsource = new DOMSource(currentBaseScript);
			
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
		    // if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			File modifiedScript = new File(inputBatchPath);
			if (modifiedScript.exists())
				if (!modifiedScript.canWrite()) {
					throw new NodeException("Cannot write to modified jAudio base script");
				}
			
			if (!modifiedScript.exists())
				modifiedScript.createNewFile();
			StreamResult result = new StreamResult(modifiedScript);
			transformer.transform(domsource,result);
		} catch(javax.xml.transform.TransformerConfigurationException e) {
			throw new NodeException("Cannot transform jAudio base script: " + e.getMessage());
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot save transformed jAudio base script: " + e.getMessage());
		} catch(javax.xml.transform.TransformerException e) {
			throw new NodeException("Cannot transform jAudio base script: " + e.getMessage());
		}
	}
	
	/**
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#extractFeatures()
	 */
	public void extractFeatures() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature extraction...");
		
		// Start jAudio
		try {
		String amuse = AmusePreferences.get(KeysStringValue.AMUSE_PATH);
		    List<String> libs = new ArrayList<String>();
		    libs.add(amuse + File.separator + "tools" + File.separator + "jAudio" + File.separator + "jhall.jar");
		    libs.add(amuse + File.separator + "tools" + File.separator + "jAudio" + File.separator + "mp3plugin.jar");
		    libs.add(amuse + File.separator + "tools" + File.separator + "jAudio" + File.separator + "tritonus_remaining-0.3.6.jar");
		    libs.add(amuse + File.separator + "tools" + File.separator + "jAudio" + File.separator + "tritonus_share-0.3.6.jar");
		    libs.add(amuse + File.separator + "tools" + File.separator + "jAudio" + File.separator + "jAudio.jar");
		    libs.add(amuse + File.separator + "tools" + File.separator + "jAudio" + File.separator + "xerces.jar");
		    List<String> javaParameters = new ArrayList<String>();
		    javaParameters.add("-Xmx1024m");
		    List<String> commands = new ArrayList<String>();
		    commands.add("jAudioFE");
		    commands.add("-b");
		    String inputBatchPath = properties.getProperty("inputExtractorBatch");
		    // if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
		    commands.add(inputBatchPath);
		    ExternalProcessBuilder jAudio = ExternalProcessBuilder.buildJavaProcess(javaParameters, libs, commands);
		    jAudio.setWorkingDirectory(new File(amuse + File.separator +"tools"+ File.separator + "jAudio"));
		    Process pc = jAudio.start();
		    
		    // Ausgabe für JAudio 
		    InputStream inputStream = pc.getInputStream();
		    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		    BufferedReader inputReader = new BufferedReader(inputStreamReader);

		    InputStream errorStream = pc.getErrorStream();
		    InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
		    BufferedReader errorReader = new BufferedReader(errorStreamReader);
		    String line = "";
		    while(pc.isAlive()){
		    	System.out.println("Output von JAudios InputStream");
		    	while ((line = inputReader.readLine()) != null){
		    		System.out.println(line);
		    	}

		    	System.out.println("Output von JAudios ErrorStream");
		    	while ((line = errorReader.readLine()) != null){
		    		System.out.println(line);
		    	}
		    }
		    
		    
            pc.waitFor();
		    convertOutput();
		} catch (InterruptedException e) {
                        throw new NodeException("Extraction with jAudio interrupted! " + e.getMessage());
                }
                catch (NodeException e) {
			throw new NodeException("Extraction with jAudio failed: " + e.getMessage());		
		} catch (IOException e) {
			throw new NodeException("Extraction with jAudio failed: " + e.getMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertOutput()
	 */
	public void convertOutput() throws NodeException {
		
		// Maps the jAudio feature name to Amuse feature ID
		HashMap<String,Integer> jAudioFeatureToAmuseId = new HashMap<String,Integer>();
		
		// Maps the Amuse feature ID to the Amuse feature name
		HashMap<Integer,String> amuseIdToAmuseName = new HashMap<Integer,String>();
		
		// Maps the Amuse feature ID to feature that wa extracted with a custom configuration
		// if custom configurations are used
		HashMap<Integer,Feature> amuseIdToCustomFeature = new HashMap<Integer,Feature>();
		
		// Maps the Amuse feature ID to the corresponding list of jAudio features
		HashMap<Integer,ArrayList<String>> featureIdToFeatureList = new HashMap<Integer,ArrayList<String>>();
		
		ArffLoader featureIDsMappingLoader = new ArffLoader();
		try {
			// Load the ARFF file which maps jAudio feature descriptions to Amuse feature descriptions
			featureIDsMappingLoader.setFile(new File(properties.getProperty("extractorFolder") + File.separator + "extractorFeatureTable.arff"));
			
			// Set up the first two hash maps
			Attribute extractorDescriptionAttribute = featureIDsMappingLoader.getStructure().attribute("ExtractorDescription");
			Attribute idAttribute = featureIDsMappingLoader.getStructure().attribute("Id");
			Instance currentInstance = featureIDsMappingLoader.getNextInstance(featureIDsMappingLoader.getStructure()); 
			while(currentInstance != null) {
				jAudioFeatureToAmuseId.put(currentInstance.stringValue(extractorDescriptionAttribute),
						                            new Double(currentInstance.value(idAttribute)).intValue());
				currentInstance = featureIDsMappingLoader.getNextInstance(featureIDsMappingLoader.getStructure());
			}
			
			// Set up the third hash map 
			featureIDsMappingLoader.reset();
			currentInstance = featureIDsMappingLoader.getNextInstance(featureIDsMappingLoader.getStructure()); 
			while(currentInstance != null) {
				if(featureIdToFeatureList.containsKey(new Double(currentInstance.value(idAttribute)).intValue())) {
					featureIdToFeatureList.get(new Double(currentInstance.value(idAttribute)).intValue()).add(
							currentInstance.stringValue(extractorDescriptionAttribute));
				} else {
					ArrayList<String> newFeatureList = new ArrayList<String>();
					newFeatureList.add(currentInstance.stringValue(extractorDescriptionAttribute));
					featureIdToFeatureList.put(new Double(currentInstance.value(idAttribute)).intValue(),newFeatureList);
				}
				jAudioFeatureToAmuseId.put(currentInstance.stringValue(extractorDescriptionAttribute),
						                            new Double(currentInstance.value(idAttribute)).intValue());
				currentInstance = featureIDsMappingLoader.getNextInstance(featureIDsMappingLoader.getStructure());
			}
			
			// Set the Amuse descriptions of features
			FeatureTable featureTable = ((ExtractionConfiguration)this.correspondingScheduler.getConfiguration()).getFeatureTable();
			for(int i=0;i<featureTable.size();i++) {
				Feature feature = featureTable.getFeatureAt(i);
				amuseIdToAmuseName.put(feature.getId(),
						feature.getDescription());
				// if the feature was extracted by this extractor using a custom configuration
				// put in the hash map of custom features
				if(feature.getCustomScript() != null && feature.getCustomScript().equals(properties.getProperty("inputExtractorBatch"))) {
					amuseIdToCustomFeature.put(feature.getId(), feature);
				}
			}
			
			// Go through Amuse metafeatures one by one and save them to ARFF feature files
			Set<?> ids = featureIdToFeatureList.keySet();
			Iterator<?> i = ids.iterator();
			while(i.hasNext()) {
				
				// Load jAudio output feature file
				ArffLoader featureLoader = new ArffLoader();
				FileInputStream outputFeatureFileStream = new FileInputStream(new File(this.outputFeatureFile));
				featureLoader.setSource(outputFeatureFileStream);
				
				Object o = i.next();
				
				// If the feature from featureIdToFeatureList has not been extracted by jAudio, go on
				if(featureLoader.getStructure().attribute(new String(featureIdToFeatureList.get(o).get(0))) == null) {
					outputFeatureFileStream.close();
					continue;
				}
				// Calculate the number of instances
				int window_number = 0;
				currentInstance = featureLoader.getNextInstance(featureLoader.getStructure());
				while(currentInstance != null) {
					currentInstance = featureLoader.getNextInstance(featureLoader.getStructure());
					window_number++;
				}
				featureLoader = new ArffLoader();
				outputFeatureFileStream.close();
				outputFeatureFileStream = new FileInputStream(new File(this.outputFeatureFile));
				featureLoader.setSource(outputFeatureFileStream);
				
				
				
				// Get the jAudio ARFF attributes for current Amuse feature ID
				FastVector atts = new FastVector();
				for(int j=0;j<featureIdToFeatureList.get(o).size();j++) {
					atts.addElement(featureLoader.getStructure().attribute(new String(featureIdToFeatureList.get(o).get(j))));
				}
				
				currentInstance = featureLoader.getNextInstance(featureLoader.getStructure());
				
				// Create a folder for Amuse feature files
				File folder = new File(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + 
						this.correspondingScheduler.getTaskId() + 
						//properties.getProperty("taskId") + 
						File.separator + this.currentPart + File.separator + properties.getProperty("extractorName"));
				if(!folder.exists() && !folder.mkdirs()) {
					throw new NodeException("Extraction with jAudio failed: could not create temp folder " + 
							folder.toString());
				}
				
				// Create a name for Amuse feature file 
				String currentFeatureFile = new String(folder.toString());
				String configurationId = "";
				if(amuseIdToCustomFeature.containsKey(new Integer(o.toString()))) {
					configurationId = "_" + amuseIdToCustomFeature.get(new Integer(o.toString())).getConfigurationId();
				}
				if(musicFile.lastIndexOf(File.separator) != -1) {
					currentFeatureFile += File.separator +
						musicFile.substring(musicFile.lastIndexOf(File.separator),musicFile.lastIndexOf(".")) + "_" + o + configurationId + ".arff";
				} else {
					currentFeatureFile += File.separator +
						musicFile.substring(0,musicFile.lastIndexOf(".")) + "_" + o + configurationId + ".arff";
				}
				File feature_values_save_file = new File(currentFeatureFile);
				if (feature_values_save_file.exists())
					if (!feature_values_save_file.canWrite()) {
						
						return;
					}
				if (!feature_values_save_file.exists())
					feature_values_save_file.createNewFile();

				FileOutputStream values_to = new FileOutputStream(feature_values_save_file);
				DataOutputStream values_writer = new DataOutputStream(values_to);
				String sep = System.getProperty("line.separator");
				
				int windowSize = 512;
				int stepSize = 512;
				if(amuseIdToCustomFeature.containsKey(new Integer(o.toString()))) {
					windowSize = amuseIdToCustomFeature.get(new Integer(o.toString())).getSourceFrameSize();
					stepSize = amuseIdToCustomFeature.get(new Integer(o.toString())).getSourceStepSize();
				}
				
				// Write to the ARFF feature file (header)
				values_writer.writeBytes("@RELATION 'Music feature'");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%rows=" + atts.size());
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%columns=" + window_number);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%sample_rate=" + "22050");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%window_size=" + windowSize);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%step_size=" + stepSize);
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				
				// Create an attribute vector with Amuse feature names
				for(int j=0;j<atts.size();j++) {
					values_writer.writeBytes("@ATTRIBUTE '" + amuseIdToAmuseName.get(jAudioFeatureToAmuseId.get( ((Attribute)atts.elementAt(j)).name() )) + "' NUMERIC");
					values_writer.writeBytes(sep);
				}
				values_writer.writeBytes("@ATTRIBUTE WindowNumber NUMERIC");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				
				// Write to the ARFF feature file (data)
				values_writer.writeBytes("@DATA");
				values_writer.writeBytes(sep);
				
				// Current window
				int window_counter = 0;
				while(currentInstance != null) {
					for(int j=0;j<atts.size();j++) {

						// If the feature is multidimensional...
						if(j>0) {
							values_writer.writeBytes(",");
						}
						int z = ((Attribute)atts.elementAt(j)).index();
						Attribute zc = featureLoader.getStructure().attribute(z);
						values_writer.writeBytes(new Double(currentInstance.value(zc)).toString());
					}
					
					// Go to the next line
					values_writer.writeBytes("," + new Integer(++window_counter).toString());
					values_writer.writeBytes(sep);
					
					// Get the next feature
					currentInstance = featureLoader.getNextInstance(featureLoader.getStructure());
				}
				
				// Close the feature file
				values_writer.close();
				values_to.close();
				outputFeatureFileStream.close();
				
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new NodeException("Conversion of jAudio features failed: " + e.getMessage());
		}
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Extracted features has been converted to Amuse ARFF");

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
