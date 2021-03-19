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
 * Creation date: 02.03.2007
 */
package amuse.nodes.extractor.methods;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysIntValue;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Adapter to Yale feature extractor. For further details of Yale see <a
 * href="http://rapid-i.com/">http://rapid-i.com/</a>
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class YaleAdapter extends AmuseTask implements ExtractorInterface {

	/** Input music file */
	private String musicFile;

	/** File with extracted Yale features */
	private String outputFeatureFile;

	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * amuse.nodes.extractor.interfaces.ExtractorInterface#setFilenames(java
	 * .lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setFilenames(String musicFile, String outputFeatureFile,
			Integer currentPart) throws NodeException {
		// Load the Yale batch script
		Document yaleScript;
		try {
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			yaleScript = DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.parse(inputBatchPath);
		} catch (SAXException e) {
			throw new NodeException(
					"Setting of input music file with Yale failed: "
							+ e.getMessage());
		} catch (IOException e) {
			throw new NodeException(
					"Setting of input music file with Yale failed: "
							+ e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new NodeException(
					"Setting of input music file with Yale failed: "
							+ e.getMessage());
		}

		// Set the input music file in the batch script
		NodeList operatorList = yaleScript.getElementsByTagName("operator");
		for (int j = 0; j < operatorList.getLength(); j++) {
			NamedNodeMap attr = operatorList.item(j).getAttributes();
			if (attr.getNamedItem("class").getNodeValue()
					.equals("MusicPreprocessing")) {
				NodeList parametersList = operatorList.item(j).getChildNodes();
				for (int k = 0; k < parametersList.getLength(); k++) {
					if (parametersList.item(k).getNodeName()
							.equals("parameter")) {
						NamedNodeMap attr2 = parametersList.item(k)
								.getAttributes();
						if (attr2.getNamedItem("key").getNodeValue()
								.equals("source_dir")) {
							attr2.getNamedItem("value").setTextContent(
									musicFile.substring(0, musicFile
											.lastIndexOf(File.separator)));
							j = operatorList.getLength();
							break;
						}
					}
				}
			}
		}

		// Set the output feature file in the batch script
		operatorList = yaleScript.getElementsByTagName("operator");
		for (int j = 0; j < operatorList.getLength(); j++) {
			NamedNodeMap attr = operatorList.item(j).getAttributes();
			if (attr.getNamedItem("class").getNodeValue()
					.equals("GnuPlotWriter")) {
				NodeList parametersList = operatorList.item(j).getChildNodes();
				for (int k = 0; k < parametersList.getLength(); k++) {
					if (parametersList.item(k).getNodeName()
							.equals("parameter")) {
						NamedNodeMap attr2 = parametersList.item(k)
								.getAttributes();
						if (attr2.getNamedItem("key").getNodeValue()
								.equals("output_file")) {
							attr2.getNamedItem("value").setTextContent(
									outputFeatureFile);
							j = operatorList.getLength();
							break;
						}
					}
				}
			}
		}
		this.musicFile = musicFile;
		this.currentPart = currentPart;
		this.outputFeatureFile = outputFeatureFile;

		// Save the modified Yale batch script
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			DOMSource domsource = new DOMSource(yaleScript);
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			File modifiedScript = new File(inputBatchPath);
			if (modifiedScript.exists())
				if (!modifiedScript.canWrite())
					throw new NodeException("Cannot write to " + modifiedScript
							+ ".");

			if (!modifiedScript.exists())
				modifiedScript.createNewFile();
			StreamResult result = new StreamResult(modifiedScript);
			transformer.transform(domsource, result);
		} catch (javax.xml.transform.TransformerConfigurationException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Cannot transform Yale input script: " + e.getMessage());
		} catch (java.io.IOException e) {
			AmuseLogger.write(
					this.getClass().getName(),
					Level.ERROR,
					"Cannot save transformed Yale input script: "
							+ e.getMessage());
		} catch (javax.xml.transform.TransformerException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Cannot transform Yale input script: " + e.getMessage());
		}
	}

	/**
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertBaseScript(HashMap)
	 */
	public void convertBaseScript(HashMap<Integer, Integer> feature2Tool,
			FeatureTable featureTable) throws NodeException {
		// First of all, a mapping from Amuse feature ID to the corresponding feature is loaded
		HashMap<Integer, Feature> featureId2Feature = new HashMap<Integer, Feature>();
		/*
		 * ArffLoader featureTableLoader = new ArffLoader(); Instance
		 * currentInstance;
		 */

		// Load feature table
		for (int i = 0; i < featureTable.size(); i++) {
			featureId2Feature.put(featureTable.getFeatureAt(i).getId(),
					featureTable.getFeatureAt(i));
		}

		// Create an attribute vector with Amuse feature names. Yale outputs all
		// features in a single text file.
		// For the conversion to Amuse ARFF, we need the information about the
		// IDs of the features which have
		// been extracted. data saves this information
		FastVector amuseAttributes = new FastVector();
		amuseAttributes.addElement(new Attribute("Id"));
		amuseAttributes.addElement(new Attribute("Description",
				(FastVector) null));
		amuseAttributes.addElement(new Attribute("ConfigurationId",
				(FastVector) null));
		amuseAttributes.addElement(new Attribute("WindowSize"));
		amuseAttributes.addElement(new Attribute("StepSize"));
		amuseAttributes.addElement(new Attribute("Dimensions"));
		amuseAttributes.addElement(new Attribute("Consider"));
		Instances data = new Instances("Music feature", amuseAttributes, 0);

		// Load Yale base script
		Document currentBaseScript = null;
		try {
			String inputBaseBatchPath = properties.getProperty("inputExtractorBaseBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBaseBatchPath.startsWith(File.separator)) {
		    	inputBaseBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBaseBatchPath;
		    }
			currentBaseScript = DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.parse(inputBaseBatchPath);
		} catch (java.io.IOException e) {
			throw new NodeException("Cannot open Yale base script: "
					+ e.getMessage());
		} catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new NodeException(
					"Cannot create DocumentBuilder which satisfies the configuration: "
							+ e.getMessage());
		} catch (org.xml.sax.SAXException e) {
			throw new NodeException(
					"Cannot create DocumentBuilder which satisfies the configuration: "
							+ e.getMessage());
		}

		// Search for all AmuseEnabler Nodes
		NodeList nList = currentBaseScript
				.getElementsByTagName("amuseEnableFeature");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			NamedNodeMap attr = node.getAttributes();
			StringTokenizer idsTokenizer = new StringTokenizer(attr
					.getNamedItem("id").getNodeValue(), ",");
			ArrayList<Integer> idsOfCurrentEnabler = new ArrayList<Integer>();
			while (idsTokenizer.hasMoreElements()) {
				idsOfCurrentEnabler.add(new Integer(idsTokenizer.nextToken()));
			}

			// The subtree will be kept only if this extractor should extract
			// the feature
			boolean enableSubTree = false;
			for (int j = 0; j < idsOfCurrentEnabler.size(); j++) {
				if (!feature2Tool.containsKey(idsOfCurrentEnabler.get(j))) {
					continue;
				}
				if (feature2Tool.get(idsOfCurrentEnabler.get(j)).toString()
						.equals(properties.getProperty("id"))) {
					enableSubTree = true;
					break;
				}
			}

			if (!enableSubTree) {
				// Cut the XML subtree which extracts current feature(s) since
				// it should not be supported by this extractor
				node.getParentNode().removeChild(node);

				// Important, since the list is updated after replacement!
				i--;
			} else {
				// Get the number of dimensions for each feature which are
				// extracted in the subtree of this enabler
				// (e.g. RMS has 1 dimension, chroma vector has 24 dimensions..)
				StringTokenizer dimensionsTokenizer = new StringTokenizer(attr
						.getNamedItem("dimensions").getNodeValue(), ",");
				ArrayList<Integer> dimensionsOfFeaturesOfCurrentEnabler = new ArrayList<Integer>();
				while (dimensionsTokenizer.hasMoreElements()) {
					dimensionsOfFeaturesOfCurrentEnabler.add(new Integer(
							dimensionsTokenizer.nextToken()));
				}

				// Go through the list of features which are extracted in the
				// subtree of this enabler
				for (int j = 0; j < idsOfCurrentEnabler.size(); j++) {
					// Should this feature be extracted?
					if (feature2Tool.containsKey(idsOfCurrentEnabler.get(j))) {
						if (feature2Tool.get(idsOfCurrentEnabler.get(j))
								.toString()
								.equals(properties.getProperty("id"))) {

							AmuseLogger
									.write(this.getClass().getName(),
											Level.DEBUG,
											"Feature with ID '"
													+ idsOfCurrentEnabler
															.get(j)
													+ "' will be extracted with "
													+ properties
															.getProperty("extractorName"));

							double vals[] = new double[7];
							Feature feature = featureId2Feature.get(idsOfCurrentEnabler.get(j));
							Integer configurationId = feature.getConfigurationId();
							vals[0] = idsOfCurrentEnabler.get(j);
							vals[1] = data.attribute(1).addStringValue(feature.getDescription());
							vals[2] = data.attribute(2).addStringValue(configurationId == null ? "" : configurationId.toString());
							vals[3] = feature.getSourceFrameSize();
							vals[4] = feature.getSourceStepSize();
							vals[5] = dimensionsOfFeaturesOfCurrentEnabler
									.get(j);
							vals[6] = 1;
							data.add(new Instance(1.0, vals));
							continue;
						}
					}

					double vals[] = new double[7];
					Feature feature = featureId2Feature.get(idsOfCurrentEnabler.get(j));
					Integer configurationId = feature.getConfigurationId();
					vals[1] = data.attribute(1).addStringValue(
							"feature result is to be ignored");
					vals[2] = data.attribute(2).addStringValue(configurationId == null ? "" : configurationId.toString());
					vals[3] = feature.getSourceFrameSize();
					vals[4] = feature.getSourceStepSize();
					vals[5] = dimensionsOfFeaturesOfCurrentEnabler
							.get(j);
					vals[6] = 0;
					data.add(new Instance(1.0, vals));
				}

				Node parent = node.getParentNode();
				NodeList children = node.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					if (children.item(j).getNodeType() == Node.ELEMENT_NODE) {

						// Replace "amuseEnableNode" with child subtree which
						// extracts the current feature
						parent.replaceChild(children.item(j), node);
					}
				}

				// Important, since the list is updated after replacement!
				i--;

				Node start2Search = parent;

				// Go upside in XML tree and look for amuseEnableTransform
				// nodes. If they are found, replace them
				// with their subtrees since these transforms are needed for the
				// extraction of features
				while (start2Search.getNodeType() != Node.DOCUMENT_NODE) {
					start2Search = start2Search.getParentNode();
					if (start2Search == null)
						break;

					// amuseEnableTransform found?
					if (start2Search.getNodeName().equals(
							"amuseEnableTransform")) {
						Node parentOfEnableTransform = start2Search
								.getParentNode();
						NodeList childrenOfTransform = start2Search
								.getChildNodes();
						for (int j = 0; j < childrenOfTransform.getLength(); j++) {
							if (childrenOfTransform.item(j).getNodeType() == Node.ELEMENT_NODE) {

								// Replace "amuseEnableTransform" with child
								// subtree which extracts somewhere the current
								// feature(s)
								parentOfEnableTransform.replaceChild(
										childrenOfTransform.item(j),
										start2Search);
							}
						}
						start2Search = parentOfEnableTransform;
					}
				}

			}
		}

		// Search for the rest of amuseEnableTransform nodes and delete them,
		// since they do not include features to extract
		// with this Tool
		nList = currentBaseScript.getElementsByTagName("amuseEnableTransform");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			node.getParentNode().removeChild(node);

			// Important, since the list is updated after replacement!
			i--;
		}

		// Save the modified script
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
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
							"Cannot write to modified Yale base script");
				}

			if (!modifiedScript.exists())
				modifiedScript.createNewFile();
			StreamResult result = new StreamResult(modifiedScript);
			transformer.transform(domsource, result);
		} catch (javax.xml.transform.TransformerConfigurationException e) {
			throw new NodeException("Cannot transform Yale base script: "
					+ e.getMessage());
		} catch (java.io.IOException e) {
			throw new NodeException(
					"Cannot save transformed Yale base script: "
							+ e.getMessage());
		} catch (javax.xml.transform.TransformerException e) {
			throw new NodeException("Cannot transform Yale base script: "
					+ e.getMessage());
		}

		// Save the information from featureIDs
		ArffSaver saver = new ArffSaver();

		// Create a name for Amuse feature file
		String inputBatchPath = properties.getProperty("inputExtractorBatch");
	    if(!inputBatchPath.startsWith(File.separator)) {
	    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
	    }
		String currentFeatureFile = inputBatchPath.substring(0, inputBatchPath.lastIndexOf(File.separator) + 1)
				+ inputBatchPath.substring(inputBatchPath.lastIndexOf(File.separator) + 1, inputBatchPath.lastIndexOf(".")) + "_"
				+ "extractorFeatureTable.arff";
		saver.setInstances(data);
		new File(currentFeatureFile).delete();
		try {
			saver.setFile(new File(currentFeatureFile));
			saver.writeBatch();
		} catch (Exception e) {
			throw new NodeException(
					"Cannot save the information about features to extract: "
							+ e.getMessage());
		}

	}

	/**
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#extractFeatures()
	 */
	public void extractFeatures() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG,
				"Starting feature extraction...");

		// Start Yale
		try {
			List<String> javaProperties = new ArrayList<String>();
			int heapSize = AmusePreferences.getInt(KeysIntValue.YALE_HEAP_SIZE);
			javaProperties.add("-Xmx" + heapSize + "m");
			List<String> libs = new ArrayList<String>();
			String yaleHome = new File(properties.getProperty("extractorFolder")).getAbsolutePath();
			String lib = yaleHome + File.separator + "lib" + File.separator;
			libs.add(lib + "yale.jar");
			List<String> commands = new ArrayList<String>();
			commands.add("edu.udo.cs.yale.YaleCommandLine");
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
			// if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			commands.add(inputBatchPath);
			ExternalProcessBuilder javaPCB = ExternalProcessBuilder
					.buildJavaProcess(javaProperties, libs, commands);
			javaPCB.setWorkingDirectory(new File(properties
					.getProperty("extractorFolder")));
			Process pc = javaPCB.start();
			boolean debug = false;
			String s = null;
			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					pc.getErrorStream()));
			if (debug)
				AmuseLogger
						.write(this.getClass().getName(), Level.INFO,
								"\n+Here is the standard error of the command (if any):\n");
			// Yale blocks if u dont read error output!
			while ((s = stdError.readLine()) != null) {
				if (debug)
					AmuseLogger.write(ExternalProcessBuilder.class.getName(),
							Level.INFO, s);
			}

			pc.waitFor();
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG,
					"...Extraction succeeded");
			// Convert the results to Amuse ARFF
			convertOutput();
		} catch (InterruptedException e) {
			throw new NodeException("Extraction with Yale failed: "
					+ e.getMessage());
		} catch (NodeException e) {
			throw new NodeException("Extraction with Yale failed: "
					+ e.getMessage());
		} catch (IOException e) {
			throw new NodeException("Extraction with Yale failed: "
					+ e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertOutput()
	 */
	public void convertOutput() throws NodeException {

		// Maps the Amuse feature ID to its name
		HashMap<Integer, String> amuseIdToAmuseName = new HashMap<Integer, String>();

		FileReader featuresInput = null;
		try {
			featuresInput = new FileReader(new File(this.outputFeatureFile));
		} catch (FileNotFoundException e) {
			throw new NodeException(
					"Could not find the file with extracted features: "
							+ e.getMessage());
		}
		BufferedReader featuresReader = new BufferedReader(featuresInput);

		// Saves features for consecutive time windows
		ArrayList<ArrayList<Float>> timeWindowFeatures = new ArrayList<ArrayList<Float>>();
		try {

			// The first two lines of Yale output are comments
			featuresReader.readLine();
			featuresReader.readLine();

			String currentWindow = new String();

			// Read lines till EOF
			while ((currentWindow = featuresReader.readLine()) != null) {

				// All features of this time window
				StringTokenizer featuresForCurrentWindow = new StringTokenizer(
						currentWindow);

				// Time window number is not needed
				featuresForCurrentWindow.nextToken();

				// Save the features of this time window to an ArrayList
				ArrayList<Float> features = new ArrayList<Float>();
				while (featuresForCurrentWindow.hasMoreElements()) {
					features.add(new Float(featuresForCurrentWindow.nextToken()));
				}

				timeWindowFeatures.add(features);
			}
		} catch (IOException e) {
			throw new NodeException("Problems loading the Yale output: "
					+ e.getMessage());
		}
		finally{
			try {
				featuresReader.close();
			} catch (IOException e) {
				throw new NodeException("Problems loading the Yale output: "
						+ e.getMessage());
			}
		}
		
		
		// Load the IDs of features
		ArffLoader featureDescriptionsloader = new ArffLoader();
		try {
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			String currentFeatureTableFile = inputBatchPath.substring(0, inputBatchPath.lastIndexOf(File.separator) + 1)
					+ inputBatchPath.substring(inputBatchPath.lastIndexOf(File.separator) + 1, inputBatchPath.lastIndexOf(".")) + "_"
					+ "extractorFeatureTable.arff";
			featureDescriptionsloader.setFile(new File(currentFeatureTableFile));

			// Set up the attributes
			Attribute idAttribute = featureDescriptionsloader.getStructure().attribute("Id");
			Attribute configurationIdAttribute = featureDescriptionsloader.getStructure().attribute("ConfigurationId");
			Attribute windowSizeAttribute = featureDescriptionsloader.getStructure().attribute("WindowSize");
			Attribute stepSizeAttribute = featureDescriptionsloader.getStructure().attribute("StepSize");
			Attribute dimensionsAttribute = featureDescriptionsloader.getStructure().attribute("Dimensions");
			Attribute considerAttribute = featureDescriptionsloader.getStructure().attribute("Consider");
			Instance currentInstance = featureDescriptionsloader.getNextInstance(featureDescriptionsloader.getStructure());

			// Set the Amuse descriptions of features
			FeatureTable featureTable = ((ExtractionConfiguration) this.correspondingScheduler
					.getConfiguration()).getFeatureTable();
			for (int i = 0; i < featureTable.size(); i++) {
				amuseIdToAmuseName.put(featureTable.getFeatureAt(i).getId(),
						featureTable.getFeatureAt(i).getDescription());
			}

			// Here the position of current feature in
			// timeWindowFeatures.get(numberOfCurrentWindow) is set
			int positionOfCurrentFeatureInArray = 0;

			// Proceed all (multidimensional) features
			while (currentInstance != null) {
				int dimensions = new Double(
						currentInstance.value(dimensionsAttribute)).intValue();

				// If this feature values should not be saved (considerAttribute
				// == 0), go on
				if (new Double(currentInstance.value(considerAttribute))
						.intValue() == 0) {
					positionOfCurrentFeatureInArray += dimensions;
					currentInstance = featureDescriptionsloader
							.getNextInstance(featureDescriptionsloader
									.getStructure());
					continue;
				}

				int id = new Double(currentInstance.value(idAttribute))
						.intValue();
				String configurationId = currentInstance.stringValue(configurationIdAttribute);
				int windowSize = new Double(currentInstance.value(windowSizeAttribute)).intValue();
				int stepSize = new Double(currentInstance.value(stepSizeAttribute)).intValue();
				
				// Create a folder for Amuse feature files
				File folder = new File(this.getCorrespondingScheduler()
						.getHomeFolder()
						+ File.separator
						+ "input"
						+ File.separator
						+ "task_"
						+ this.correspondingScheduler.getTaskId()
						+ File.separator
						+ this.currentPart
						+ File.separator
						+ properties.getProperty("extractorName"));
				if (!folder.exists() && !folder.mkdirs()) {
					throw new NodeException(
							"Extraction with Yale failed: could not create temp folder "
									+ folder.toString());
				}

				// Create a name for Amuse feature file
				String currentFeatureFile = new String(folder.toString());
				if (musicFile.lastIndexOf(File.separator) != -1) {
					currentFeatureFile += File.separator
							+ musicFile.substring(
									musicFile.lastIndexOf(File.separator),
									musicFile.lastIndexOf(".")) + "_" + id
							+ (configurationId.equals("") ? "" : "_" + configurationId)
							+ ".arff";
				} else {
					currentFeatureFile += File.separator
							+ musicFile
									.substring(0, musicFile.lastIndexOf("."))
							+ "_" + id + ".arff";
				}
				File feature_values_save_file = new File(currentFeatureFile);
				if (feature_values_save_file.exists())
					if (!feature_values_save_file.canWrite()) {

						return;
					}
				if (!feature_values_save_file.exists())
					feature_values_save_file.createNewFile();
				FileOutputStream values_to = new FileOutputStream(
						feature_values_save_file);
				DataOutputStream values_writer = new DataOutputStream(values_to);
				String sep = System.getProperty("line.separator");

				// Write to the ARFF feature file (header)
				// TODO Set correctly window size and sample frequency!!!
				values_writer.writeBytes("@RELATION 'Music feature'");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%rows=" + dimensions);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%columns="
						+ timeWindowFeatures.size());
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%sample_rate=" + "22050");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%window_size=" + windowSize);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%step_size=" + stepSize);
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);

				// Create an attribute vector with Amuse feature names
				for (int j = 0; j < dimensions; j++) {
					values_writer.writeBytes("@ATTRIBUTE '"
							+ amuseIdToAmuseName.get(id) + "' NUMERIC");
					values_writer.writeBytes(sep);
				}
				values_writer.writeBytes("@ATTRIBUTE WindowNumber NUMERIC");
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);

				// Write to the ARFF feature file (data)
				values_writer.writeBytes("@DATA");
				values_writer.writeBytes(sep);

				// Write the feature values to the ARFF feature file
				for (int j = 0; j < timeWindowFeatures.size(); j++) {
					double vals[] = new double[dimensions];
					for (int k = positionOfCurrentFeatureInArray; k < positionOfCurrentFeatureInArray
							+ dimensions; k++) {
						vals[k - positionOfCurrentFeatureInArray] = timeWindowFeatures
								.get(j).get(k);

						// If the feature is multidimensional...
						if (k > positionOfCurrentFeatureInArray) {
							values_writer.writeBytes(",");
						}
						values_writer.writeBytes(timeWindowFeatures.get(j)
								.get(k).toString());
					}

					// Go to the next line
					values_writer.writeBytes(","
							+ new Integer(j + 1).toString());
					values_writer.writeBytes(sep);
				}

				// Close the feature file
				values_writer.close();
				values_to.close();
				
				// Update the position of current feature in array
				positionOfCurrentFeatureInArray += dimensions;

				// Go to the next feature
				currentInstance = featureDescriptionsloader
						.getNextInstance(featureDescriptionsloader
								.getStructure());
			}
		} catch (Exception e) {
			throw new NodeException("Conversion of Yale features failed: "
					+ e.getMessage());
		}

		AmuseLogger.write(this.getClass().getName(), Level.INFO,
				"Extracted features has been converted to Amuse ARFF");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amuse.interfaces.AmuseTaskInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Do nothing, since initialization is not required
	}

}
