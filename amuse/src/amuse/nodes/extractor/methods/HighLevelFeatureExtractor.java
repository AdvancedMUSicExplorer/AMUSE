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
 * Creation date: 18.11.2011
 */
package amuse.nodes.extractor.methods;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

import amuse.data.ModelType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.FeatureTable;
import amuse.data.InputFeatureType;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.data.io.attributes.NumericAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.ClassifierNodeScheduler;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Applies binary classification model (e.g. recognition of piano) and saves the relative rate of positive frames in 
 * a defined larger time window: e.g. if piano is detected in 70% of 512 sample frames around onsets events for a 10s
 * window, the feature value for this window is 0.7.
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class HighLevelFeatureExtractor extends AmuseTask implements ExtractorInterface {

	/** If the input music file was splitted, here is the number of current part */
	// TODO v0.2: may be removed if no mp3->wave conversion is done 
	// (not required here but is currently done by ExtractorNodeScheduler automaticly!)
	private Integer currentPart;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#setFilenames(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
		this.currentPart = currentPart;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertBaseScript(java.util.HashMap)
	 */
	public void convertBaseScript(HashMap<Integer,Integer> feature2Tool, FeatureTable featureTable) throws NodeException {
		
		// First of all, a mapping from Amuse feature ID to its description is loaded
		HashMap<Integer,String> featureId2Description = new HashMap<Integer,String>();

		// Load feature table
		for(int i=0;i<featureTable.size();i++) {
			featureId2Description.put(featureTable.getFeatureAt(i).getId(),
					featureTable.getFeatureAt(i).getDescription());
		}
		
		// Load the base script
		Document currentBaseScript = null;
		try {
			currentBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					properties.getProperty("extractorFolder") + File.separator + 
					properties.getProperty("inputExtractorBaseBatch"));
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot open HighLevelFeatureExtractor base script: " + e.getMessage());
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
					AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Feature with ID '" + idsOfCurrentEnabler.get(j) + 
							"' will be extracted with " + properties.getProperty("extractorName"));
					enableSubTree = true; break;
				}
			}
			
			if(!enableSubTree) {
				// Cut the XML subtree which extracts current feature(s) since it should not be supported by this extractor
				node.getParentNode().removeChild(node);
				
				// Important, since the list is updated after replacement!
				i--;
			} 
		}
		
		// Save the modified script
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource domsource = new DOMSource(currentBaseScript);
			File modifiedScript = new File(properties.getProperty("extractorFolder")+ File.separator +
										   properties.getProperty("inputExtractorBatch"));
			if (modifiedScript.exists())
				if (!modifiedScript.canWrite()) {
					throw new NodeException("Cannot write to modified HighLevelFeatureAdapter base script");
				}
			
			if (!modifiedScript.exists())
				modifiedScript.createNewFile();
			StreamResult result = new StreamResult(modifiedScript);
			transformer.transform(domsource,result);
		} catch(javax.xml.transform.TransformerConfigurationException e) {
			throw new NodeException("Cannot transform HighLevelFeatureAdapter base script: " + e.getMessage());
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot save transformed HighLevelFeatureAdapter base script: " + e.getMessage());
		} catch(javax.xml.transform.TransformerException e) {
			throw new NodeException("Cannot transform HighLevelFeatureAdapter base script: " + e.getMessage());
		}
	}
	
	/**
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#extractFeatures()
	 */
	public void extractFeatures() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature extraction...");
		
		// Load the modified base script with configuration for features to extract
		Document modifiedBaseScript = null;
		try {
			modifiedBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					properties.getProperty("extractorFolder") + File.separator + 
					properties.getProperty("inputExtractorBatch"));
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot open HighLevelFeatureExtractor script: " + e.getMessage());
		} catch(javax.xml.parsers.ParserConfigurationException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: " + e.getMessage());
		} catch(org.xml.sax.SAXException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: " + e.getMessage());
		}

		String relativePath = ((ExtractionConfiguration)this.correspondingScheduler.getConfiguration()).getMusicFileList().getFileAt(0);
		if(relativePath.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
			relativePath = relativePath.substring(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)).getPath().length());
		}
		relativePath = relativePath.substring(0,relativePath.lastIndexOf("."));
		String musicFile = relativePath.substring(relativePath.lastIndexOf(File.separator)+1);
				
		// Search for all AmuseEnabler Nodes
		NodeList nList = modifiedBaseScript.getElementsByTagName("amuseEnableFeature");
		for(int currentFeature=0;currentFeature<nList.getLength();currentFeature++) {
			Node node = nList.item(currentFeature);
			NodeList parameters = node.getChildNodes();
			int currentFeatureId = new Integer(node.getAttributes().getNamedItem("id").getNodeValue());
			String currentFeatureName = node.getAttributes().getNamedItem("name").getNodeValue();
			
			String processedFeaturesDescription = null;
			String selectedFeaturesModel = null;
			String classifierAlgorithmDescription = null;
			String classificationModel = null;
			int windowSize = 0;
			int offsetSize = 0;
			for(int i=0;i<parameters.getLength();i++) {
				if(parameters.item(i).getNodeType() == Node.ELEMENT_NODE && parameters.item(i).getNodeName().equals("extractionParameter")) {
					if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Processing description")) {
						processedFeaturesDescription = parameters.item(i).getAttributes().getNamedItem("stringValue").getNodeValue();
					} else if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Selected features model")) {
						selectedFeaturesModel = parameters.item(i).getAttributes().getNamedItem("fileValue").getNodeValue();
					} else if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Classifier algorithm description")) {
						classifierAlgorithmDescription = parameters.item(i).getAttributes().getNamedItem("stringValue").getNodeValue();
					} else if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Classification model")) {
						classificationModel = parameters.item(i).getAttributes().getNamedItem("fileValue").getNodeValue();
					} else if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Window size")) {
						windowSize = new Integer(parameters.item(i).getAttributes().getNamedItem("stringValue").getNodeValue());
					} else if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Offset size")) {
						offsetSize = new Integer(parameters.item(i).getAttributes().getNamedItem("stringValue").getNodeValue());
					}
				}
			}
			
			// How many different models will be used in this feature?
			StringTokenizer tok = new StringTokenizer(classifierAlgorithmDescription,";");
			int featureDimensions = tok.countTokens();
			ArrayList<ArrayList<Double>> featureValues = new ArrayList<ArrayList<Double>>(featureDimensions);
			
			try {
			
				// Load the data set with processed features for this file
				DataSet processedFeatures = new DataSet(new File(AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE) +
						relativePath + File.separator + musicFile + "_" + 
						processedFeaturesDescription + ".arff"));
				StringTokenizer currentClassifierTok = new StringTokenizer(classifierAlgorithmDescription,";");
				StringTokenizer currentSelectedFeaturesModelTok = new StringTokenizer(selectedFeaturesModel,";");
				StringTokenizer currentClassificationModelTok = new StringTokenizer(classificationModel,";");
				
				// Go through all dimensions
				for(int currentDim=0;currentDim<featureDimensions;currentDim++) {
					featureValues.add(currentDim, new ArrayList<Double>());
					DataSet featuresToClassify = new DataSet("ProcessedFeatures");
				
					// TODO v0.2: Currently selected feature model is a simple bit string which should be replaced by more detailed description
					// so that the overall feature file and bit string length must not be of the same length
					String currentFile = currentSelectedFeaturesModelTok.nextToken();
					BufferedReader selectedFeaturesReader = new BufferedReader(new FileReader(new File(currentFile)));
					String selectedFeatures = selectedFeaturesReader.readLine();
					selectedFeaturesReader.close();
						
					// Create the data set for classification
					for(int i=0;i<selectedFeatures.length();i++) {
						if(selectedFeatures.charAt(i) == '1') {
							featuresToClassify.addAttribute(processedFeatures.getAttribute(i));
						}
					}
					
					// Create the fake id - currently ClassifierNodeScheduler requires it for ready input data sets
					NumericAttribute idAttribute = new NumericAttribute("Id", new ArrayList<Double>());
					for(int i=0;i<processedFeatures.getValueCount();i++) {
						idAttribute.addValue(0d);
					}
					featuresToClassify.addAttribute(idAttribute);
					
					
					// Classify the music input with the current model
					ArrayList<ClassifiedClassificationWindow> predictedFeatures = new ArrayList<ClassifiedClassificationWindow>();
					ClassificationConfiguration cConf = null;
					cConf = new ClassificationConfiguration(
						new DataSetInput(featuresToClassify),
						ClassificationConfiguration.InputSourceType.READY_INPUT,
						new ArrayList<Integer>(),
						processedFeaturesDescription, 
						InputFeatureType.PROCESSED_FEATURES,
						null,
						-1,
						-1,
						currentClassifierTok.nextToken(),
						new ArrayList<Integer>(),
						new ModelType(RelationshipType.BINARY, LabelType.SINGLELABEL, MethodType.SUPERVISED),
						0,
						this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + "result.arff");
							
					cConf.setPathToInputModel(currentClassificationModelTok.nextToken());
					ClassifierNodeScheduler cs = new ClassifierNodeScheduler(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId());
					cs.setCleanInputFolder(false);
					predictedFeatures = cs.proceedTask(this.correspondingScheduler.getHomeFolder(), this.correspondingScheduler.getTaskId(), cConf, false);
				
					// The next boundary for feature estimation
					int currentFrameStart = 0;
					int currentFrameEnd = windowSize;
					int numberOfValuesInCurrentFrame = 0;
					int sumOfPositivesInCurrentFrame = 0;
					
					// Go through all feature classification windows
					for(int i=0;i<processedFeatures.getValueCount();i++) {
						int currentClassificationWindowStart = new Double(processedFeatures.getAttribute("Start").getValueAt(i).toString()).intValue();
						int currentClassificationWindowEnd = new Double(processedFeatures.getAttribute("End").getValueAt(i).toString()).intValue();
						
						if(currentClassificationWindowEnd > currentFrameEnd) {
							// Save the results
							featureValues.get(currentDim).add((double)sumOfPositivesInCurrentFrame/(double)numberOfValuesInCurrentFrame);
							
							numberOfValuesInCurrentFrame = 0;
							sumOfPositivesInCurrentFrame = 0;
							currentFrameStart = currentFrameStart + offsetSize;
							currentFrameEnd = currentFrameStart + windowSize;
							
							// Reset i going back due to possible overlap
							while(i>0 && new Double(processedFeatures.getAttribute("Start").getValueAt(i).toString()).intValue() > currentFrameStart) {
								i--;
							}
						} else if(currentClassificationWindowStart >= currentFrameStart){
							numberOfValuesInCurrentFrame++;
							// Since all of the feature vectors have become the id 0..
							sumOfPositivesInCurrentFrame += predictedFeatures.get(0).getRelationships()[i][0];
						}
					}
				} 
				
				// Create a folder for Amuse feature files
				File folder = new File(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + 
					this.correspondingScheduler.getTaskId() + 
					//properties.getProperty("taskId") + 
					File.separator + this.currentPart + File.separator + properties.getProperty("extractorFolderName"));
				if(!folder.exists() && !folder.mkdirs()) {
					throw new NodeException("Extraction with jAudio failed: could not create temp folder " + 
						folder.toString());
				}
				
				// Create a name for Amuse feature file 
				String currentFeatureFile = new String(folder.toString());
				currentFeatureFile += File.separator +	musicFile + "_" + currentFeatureId + ".arff";
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
					
				// Write to the ARFF feature file (header)
				values_writer.writeBytes("@RELATION 'Music feature'");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%rows=" + (featureDimensions+1));
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%columns=" + featureValues.get(0).size());
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%sample_rate=22050"); // TODO set sampling size properly
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%window_size=" + ((ExtractionConfiguration)this.correspondingScheduler.
						getConfiguration()).getFeatureTable().getFeatureByID(currentFeatureId).getSourceFrameSize());
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				for(int currentDim=0;currentDim<featureDimensions;currentDim++) {
					values_writer.writeBytes("@ATTRIBUTE '" + currentFeatureName + "' NUMERIC");
					values_writer.writeBytes(sep);
				}
				values_writer.writeBytes("@ATTRIBUTE WindowNumber NUMERIC");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
					
				// Write to the ARFF feature file (data)
				values_writer.writeBytes("@DATA");
				values_writer.writeBytes(sep);
				for(int k=0;k<featureValues.get(0).size();k++) {
					double windowNumber = (double)offsetSize / (double)windowSize * (double)k;
					for(int currentDim=0;currentDim<featureDimensions;currentDim++) {
						values_writer.writeBytes(featureValues.get(currentDim).get(k) + ",");
					}
					values_writer.writeBytes((windowNumber+1) + sep);
				}
				values_writer.close();
			} catch (IOException e) {
				throw new NodeException("Extraction with HighLevelFeatureExtractor failed: " + e.getMessage());
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertOutput()
	 */
	public void convertOutput() throws NodeException {
		// Do nothing
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
