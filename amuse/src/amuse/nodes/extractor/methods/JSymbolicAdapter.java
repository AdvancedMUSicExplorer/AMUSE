package amuse.nodes.extractor.methods;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
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

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.data.modality.Modality;
import amuse.data.modality.SymbolicModality;
import amuse.data.modality.SymbolicModality.SymbolicFormat;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.converters.ArffLoader;

/**
 * Adapter to jSymbolic feature extractor.
 */
public class JSymbolicAdapter  extends AmuseTask implements ExtractorInterface {

	/** Input music file */
	private String musicFile;

	/** File with the extracted jSymbolic features */
	private String outputFeatureFile;

	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;

	/** List of supported modalities */
	private static final List<Modality> modalities = List.of(new SymbolicModality(List.of(SymbolicFormat.MIDI)));
	
	private double windowDurationInSeconds = 0.1;
	int sampleRate = 22050;

	@Override
	public void setParameters(String parameterString) throws NodeException {}

	@Override
	public void initialize() throws NodeException {}

	@Override
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
		this.currentPart = currentPart;
		this.musicFile = musicFile;
		this.outputFeatureFile = properties.getProperty("extractorFolder") + File.separator + "feature_values.arff";
		
		File configFile = new File(properties.getProperty("extractorFolder") + File.separator + "jSymbolicConfig.txt");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			List<String> features = new ArrayList<String>();
			String line;
			while((line = reader.readLine()) != null && !line.equals("<input_files>")) {
				features.add(line);
			}
			reader.close();
			
			FileWriter writer = new FileWriter(configFile, false);
			
			for(String feature: features) {
				writer.write(feature);
				writer.write("\n");
			}
			
			// Set input file in config file
			writer.write("<input_files>");
			writer.write(System.getProperty("line.separator"));
			writer.write(musicFile);
			writer.write(System.getProperty("line.separator"));
			
			// Set output files in config file
			writer.write("<output_files>");
			writer.write(System.getProperty("line.separator"));
			writer.write("feature_values_save_path=feature_values.xml");
			writer.write(System.getProperty("line.separator"));
			writer.write("feature_definitions_save_path=feature_definitions.xml");
			writer.write(System.getProperty("line.separator"));
			
			// Set options in config file
			writer.write("<jSymbolic_options>");
			writer.write(System.getProperty("line.separator"));
			writer.write("window_size="+windowDurationInSeconds);
			writer.write(System.getProperty("line.separator"));
			writer.write("window_overlap=0.0");
			writer.write(System.getProperty("line.separator"));
			writer.write("save_features_for_each_window=true");
			writer.write(System.getProperty("line.separator"));
			writer.write("save_overall_recording_features=false");
			writer.write(System.getProperty("line.separator"));
			writer.write("convert_to_arff=true");
			writer.write(System.getProperty("line.separator"));
			writer.write("convert_to_csv=false");
			writer.write(System.getProperty("line.separator"));
			
			writer.close();
		} catch (IOException e) {
			throw new NodeException("Cannot write to jSymbolic config file: " + e.getMessage());
		}
	}

	@Override
	public void convertBaseScript(HashMap<Integer, Integer> feature2Tool, FeatureTable featureTable) throws NodeException {
		
		File configFile = new File(properties.getProperty("extractorFolder") + File.separator + "jSymbolicConfig.txt");
		if(configFile.exists()) {
			configFile.delete();
		}
		try {
			configFile.createNewFile();
			FileWriter writer = new FileWriter(configFile);
			writer.write("<features_to_extract>");
			writer.write(System.getProperty("line.separator"));
			writer.close();
		} catch (IOException e) {
			throw new NodeException("Cannot write to jSymbolic config file: " + e.getMessage());
		}
		
		// Load jSymbolic base script
		Document currentBaseScript = null;
		try {
			String inputBaseBatchPath = properties.getProperty("inputExtractorBaseBatch");
		    // if it is a relative path the input batch is in the extractor folder
		    if(!inputBaseBatchPath.startsWith(File.separator)) {
		    	inputBaseBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBaseBatchPath;
		    }
			currentBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBaseBatchPath);
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot open jSymbolic base script: " + e.getMessage());
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
			Integer idOfCurrentEnabler = Integer.valueOf(attr.getNamedItem("id").getNodeValue());
		
			/* Delete amuseEnablerNode */
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
			/* set active true, if feature should be extracted */
			if(feature2Tool.containsKey(idOfCurrentEnabler)) {
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
							if(featureDescription.item(k).getNodeType() == Node.ELEMENT_NODE && featureDescription.item(k).getNodeName().equals("name")) {
								Node featureName = featureDescription.item(k);
								String name = featureName.getTextContent();
								try {
									FileWriter writer = new FileWriter(configFile, true);
									writer.write(name);
									writer.write(System.getProperty("line.separator"));
									writer.close();
								} catch (IOException e) {
									throw new NodeException("Cannot write to jSymbolic config file: " + e.getMessage());
								}
							}
						}
						
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
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "jSymbolic.dtd");
			DOMSource domsource = new DOMSource(currentBaseScript);
			
			String inputBatchPath = properties.getProperty("inputExtractorBatch");
		    // if it is a relative path the input batch is in the extractor folder
		    if(!inputBatchPath.startsWith(File.separator)) {
		    	inputBatchPath = properties.getProperty("extractorFolder") + File.separator + inputBatchPath;
		    }
			File modifiedScript = new File(inputBatchPath);
			if (modifiedScript.exists())
				if (!modifiedScript.canWrite()) {
					throw new NodeException("Cannot write to modified jSymbolic base script");
				}
			
			if (!modifiedScript.exists())
				modifiedScript.createNewFile();
			StreamResult result = new StreamResult(modifiedScript);
			transformer.transform(domsource,result);
		} catch(javax.xml.transform.TransformerConfigurationException e) {
			throw new NodeException("Cannot transform jSymbolic base script: " + e.getMessage());
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot save transformed jSymbolic base script: " + e.getMessage());
		} catch(javax.xml.transform.TransformerException e) {
			throw new NodeException("Cannot transform jSymbolic base script: " + e.getMessage());
		}
	}

	@Override
	public void extractFeatures() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature extraction...");
		
		// Start jSymbolic
		try {
			String amuse = AmusePreferences.get(KeysStringValue.AMUSE_PATH);
			List<String> commands = new ArrayList<String>();
			String javapath = AmusePreferences.get(KeysStringValue.JAVA_PATH);
			commands.add(javapath);
		    commands.add("-Xmx6g");
		    commands.add("-jar");
		    commands.add("jSymbolic2.jar");
		    commands.add("-configrun");
		    commands.add("./jSymbolicConfig.txt");
		    
		    ExternalProcessBuilder jSymbolic = new ExternalProcessBuilder(commands);
		    jSymbolic.setWorkingDirectory(new File(amuse + File.separator +"tools"+ File.separator + "jSymbolic"));
		    Process pc = jSymbolic.start();
		    
		    // Ausgabe fuer JSymbolic
		    InputStream inputStream = pc.getInputStream();
		    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		    BufferedReader inputReader = new BufferedReader(inputStreamReader);

		    InputStream errorStream = pc.getErrorStream();
		    InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
		    BufferedReader errorReader = new BufferedReader(errorStreamReader);
		    String line = "";
		    while(pc.isAlive()){
		    	System.out.println("Output von jSymbolic InputStream");
		    	while ((line = inputReader.readLine()) != null){
		    		System.out.println(line);
		    	}

		    	System.out.println("Output von jSymbolic ErrorStream");
		    	while ((line = errorReader.readLine()) != null){
		    		System.out.println(line);
		    	}
		    }
		    
            pc.waitFor();
		    convertOutput();
		} catch (InterruptedException e) {
        	throw new NodeException("Extraction with jSymbolic interrupted! " + e.getMessage());
        } catch (NodeException e) {
			throw new NodeException("Extraction with jSymbolic failed: " + e.getMessage());		
		} catch (IOException e) {
			throw new NodeException("Extraction with jSymbolic failed: " + e.getMessage());
		}

	}

	@Override
	public void convertOutput() throws NodeException {
		// Maps the jSymbolic feature name to Amuse feature ID
		HashMap<String,Integer> jSymbolicFeatureToAmuseId = new HashMap<String,Integer>();
		
		// Maps the Amuse feature ID to the Amuse feature name
		HashMap<Integer,String> amuseIdToAmuseName = new HashMap<Integer,String>();
		
		// Maps the Amuse feature ID to feature that was extracted with a custom configuration
		// if custom configurations are used
		HashMap<Integer,Feature> amuseIdToCustomFeature = new HashMap<Integer,Feature>();
		
		// Maps the Amuse feature ID to the corresponding list of jSymbolic features
		HashMap<Integer,ArrayList<String>> featureIdToFeatureList = new HashMap<Integer,ArrayList<String>>();
		
		ArffLoader featureIDsMappingLoader = new ArffLoader();
		try {
			// Load the ARFF file which maps jSymbolic feature descriptions to Amuse feature descriptions
			featureIDsMappingLoader.setFile(new File(properties.getProperty("extractorFolder") + File.separator + "extractorFeatureTable.arff"));
			
			// Set up the first two hash maps
			Attribute extractorDescriptionAttribute = featureIDsMappingLoader.getStructure().attribute("ExtractorDescription");
			Attribute idAttribute = featureIDsMappingLoader.getStructure().attribute("Id");
			Instance currentInstance = featureIDsMappingLoader.getNextInstance(featureIDsMappingLoader.getStructure()); 
			while(currentInstance != null) {
				jSymbolicFeatureToAmuseId.put(currentInstance.stringValue(extractorDescriptionAttribute),
						                            Double.valueOf(currentInstance.value(idAttribute)).intValue());
				currentInstance = featureIDsMappingLoader.getNextInstance(featureIDsMappingLoader.getStructure());
			}
			
			// Set up the third hash map 
			featureIDsMappingLoader.reset();
			currentInstance = featureIDsMappingLoader.getNextInstance(featureIDsMappingLoader.getStructure()); 
			while(currentInstance != null) {
				if(featureIdToFeatureList.containsKey(Double.valueOf(currentInstance.value(idAttribute)).intValue())) {
					featureIdToFeatureList.get(Double.valueOf(currentInstance.value(idAttribute)).intValue()).add(
							currentInstance.stringValue(extractorDescriptionAttribute));
				} else {
					ArrayList<String> newFeatureList = new ArrayList<String>();
					newFeatureList.add(currentInstance.stringValue(extractorDescriptionAttribute));
					featureIdToFeatureList.put(Double.valueOf(currentInstance.value(idAttribute)).intValue(),newFeatureList);
				}
				jSymbolicFeatureToAmuseId.put(currentInstance.stringValue(extractorDescriptionAttribute),
						                            Double.valueOf(currentInstance.value(idAttribute)).intValue());
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
				
				// Load jSymbolic output feature file
				ArffLoader featureLoader = new ArffLoader();
				FileInputStream outputFeatureFileStream = new FileInputStream(new File(this.outputFeatureFile));
				featureLoader.setSource(outputFeatureFileStream);
				
				Object o = i.next();
				
				// If the feature from featureIdToFeatureList has not been extracted by jSymbolic, go on
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
				
				
				
				// Get the jSymbolic ARFF attributes for current Amuse feature ID
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
					throw new NodeException("Extraction with jSymbolic failed: could not create temp folder " + 
							folder.toString());
				}
				
				// Create a name for Amuse feature file 
				String currentFeatureFile = new String(folder.toString());
				String configurationId = "";
				if(amuseIdToCustomFeature.containsKey(Integer.valueOf(o.toString()))) {
					configurationId = "_" + amuseIdToCustomFeature.get(Integer.valueOf(o.toString())).getConfigurationId();
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
				
				int windowSize = (int) (sampleRate*windowDurationInSeconds);
				int stepSize = windowSize;
				if(amuseIdToCustomFeature.containsKey(Integer.valueOf(o.toString()))) {
					windowSize = amuseIdToCustomFeature.get(Integer.valueOf(o.toString())).getSourceFrameSize();
					stepSize = amuseIdToCustomFeature.get(Integer.valueOf(o.toString())).getSourceStepSize();
				}
				
				// Write to the ARFF feature file (header)
				values_writer.writeBytes("@RELATION 'Music feature'");
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%rows=" + atts.size());
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%columns=" + window_number);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%sample_rate="+sampleRate);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%window_size="+windowSize);
				values_writer.writeBytes(sep);
				values_writer.writeBytes("%step_size="+stepSize);
				values_writer.writeBytes(sep);
				values_writer.writeBytes(sep);
				
				// Create an attribute vector with Amuse feature names
				for(int j=0;j<atts.size();j++) {
					values_writer.writeBytes("@ATTRIBUTE '" + amuseIdToAmuseName.get(jSymbolicFeatureToAmuseId.get( ((Attribute)atts.elementAt(j)).name() )) + "' NUMERIC");
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
						values_writer.writeBytes(Double.valueOf(currentInstance.value(zc)).toString());
					}
					
					// Go to the next line
					values_writer.writeBytes("," + Integer.valueOf(++window_counter).toString());
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
			throw new NodeException("Conversion of jSymbolic features failed: " + e.getMessage());
		}
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Extracted features has been converted to Amuse ARFF");

	}

	@Override
	public List<Modality> getModalities() {
		return modalities;
	}

}
