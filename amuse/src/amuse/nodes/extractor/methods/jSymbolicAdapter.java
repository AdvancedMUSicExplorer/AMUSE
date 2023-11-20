package amuse.nodes.extractor.methods;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import amuse.data.FeatureTable;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.nodes.extractor.modality.Modality;
import amuse.nodes.extractor.modality.SymbolicModality;
import amuse.nodes.extractor.modality.SymbolicModality.SymbolicFormat;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;

public class jSymbolicAdapter  extends AmuseTask implements ExtractorInterface {

	/** Input music file */
	private String musicFile;

	/** File with the extracted jAudio features */
	private String outputFeatureFile;

	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;

	/** List of supported modalities */
	private final List<Modality> modalities = List.of(new SymbolicModality(List.of(SymbolicFormat.MIDI)));

	@Override
	public void setParameters(String parameterString) throws NodeException {}

	@Override
	public void initialize() throws NodeException {}

	@Override
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
		this.currentPart = currentPart;
		this.musicFile = musicFile;
		
		File configFile = new File(properties.getProperty("extractorFolder") + File.separator + "jSymbolicConfig.txt");
		try {
			configFile.createNewFile();
			FileWriter writer = new FileWriter(configFile, true);
			
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
			writer.write("window_size=1.0");
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
			Integer idOfCurrentEnabler = new Integer(attr.getNamedItem("id").getNodeValue());
		
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
		// TODO Auto-generated method stub

	}

	@Override
	public List<Modality> getModalities() {
		return modalities;
	}

}
