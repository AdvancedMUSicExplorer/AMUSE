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
 * Creation date: 17.09.2007
 */
package amuse.scheduler.taskstarters;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.data.FileTable;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.Attribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.scheduler.AmuseTaskStarter;
import amuse.interfaces.scheduler.SchedulerException;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.extractor.ExtractorNodeScheduler;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysIntValue;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * This scheduler class starts feature extraction
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class FeatureExtractionStarter extends AmuseTaskStarter {

    /**
     * Constructor
     */
    public FeatureExtractionStarter(String nodeFolder, long jobCounter, boolean startNodeDirectly) throws SchedulerException {
    	super(nodeFolder, jobCounter, startNodeDirectly);
    }

    /*
     * (non-Javadoc)
     * @see amuse.interfaces.scheduler.AmuseTaskStarterInterface#startTask(amuse.interfaces.nodes.TaskConfiguration[], java.util.Properties)
     */
    public long startTask(TaskConfiguration[] taskConfiguration, Properties props) throws SchedulerException {
    	
    	// For the comparison of feature tables: each new feature table means that the base extraction scripts
    	// should be updated. 
    	// TODO: if the extraction is done via grid, the following error may occur:
    	// (1) feature 1 should be extracted from 1st music file -> base script is converted, so that only feature 1
    	// will be extracted
    	// (2) job is added to grid, however all pcs are busy
    	// (3) features 2 and 3 should be extracted from 2nd music file -> base script is converted, only features 2
    	// and 3 will be extracted
    	// (4) job added to grid
    	// (5) pcs are now free, both jobs use the scripts which extract features 2 and 3!!
    	FeatureTable previousFeatureTable = null;
    	
    	// Generate and proceed Amuse jobs
		for (int i = 0; i < taskConfiguration.length; i++) {
	
			// Configuration of the current task 
			ExtractionConfiguration extractorConfig = (ExtractionConfiguration)taskConfiguration[i];
			FeatureTable featureTable = extractorConfig.getFeatureTable();
			
			// Is the current feature table other than the previous? Then update the extraction scripts!
			if(!featureTable.equals(previousFeatureTable)) {
				modifyBaseScripts(featureTable);

				// Update previousFeatureTable
				previousFeatureTable = featureTable;
			}
    		
			
			
			// If the extractor node scheduler will be started via grid or batch script...
			// Here the Amuse parameter "numberOfJobsPerGridMachine" is ignored since the conversion of base
			// scripts is done here and not on the grid machine! 
			// TODO Provide conversion in ExtractorNodeScheduler? (consider also previous TODO) 
			// Then it will be done very often!
			if (!this.startNodeDirectly) {
				
		   	   	// Create a separate job for each music file
				for(int k=0;k<extractorConfig.getMusicFileList().getFiles().size();k++) {	
					
					ArrayList<Integer> fileId = new ArrayList<Integer>(1);
					fileId.add(extractorConfig.getMusicFileList().getIds().get(k));
					ArrayList<String> filePath = new ArrayList<String>(1);
					filePath.add(extractorConfig.getMusicFileList().getFileAt(k));
					ExtractionConfiguration extractorConfigWithOneFile = new ExtractionConfiguration(new FileTable(fileId,filePath), featureTable);
					
					FileOutputStream fos = null;
		   	   		ObjectOutputStream out = null;
		   	   		try {
		   	   			fos = new FileOutputStream(new String(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "taskoutput" + File.separator + "task_" + 
		   	   					this.jobCounter + ".ser"));
		   	   		    out = new ObjectOutputStream(fos);
		   	   		    out.writeObject(extractorConfigWithOneFile);
		   	   		    out.close();
		   	   	    } catch(IOException ex) {
		   	   		    ex.printStackTrace();
		   	   	    }
		   	    	    
		   	    	// Create parameter line
					String parameterString = new String();
					parameterString = new Long(this.jobCounter).toString();
					
					// Update the counter of batch jobs
					try {
						FileOutputStream values_toTest = new FileOutputStream(new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + "/config/jobcounter.txt"));
						DataOutputStream values_writerTest = new DataOutputStream(values_toTest);
						values_writerTest.writeBytes(new Long(jobCounter+1).toString());
						values_toTest.close();
					} catch (Exception e) {
						throw new SchedulerException("Could not update job counter during proceeding a script to the grid: " + e.getMessage());
					}
			
					// Proceed script to grid
					Process process;
					try {
					    process = Runtime.getRuntime().exec(AmusePreferences.get(KeysStringValue.GRID_SCRIPT_EXTRACTOR) + " " + parameterString);
					} catch (IOException e) {
					    throw new SchedulerException("Error on proceeding a script to the grid: " + e.getMessage());
					}
			
					AmuseLogger.write(this.getClass().getName(), Level.INFO, "Extraction task script for "
						+ extractorConfigWithOneFile.getMusicFileList().getFileAt(0) + " is prepared");
			
					// Wait till the job is proceeded to grid (otherwise "too many open files" exception may occur)
					try {
					    process.waitFor();
			
						// DEBUG Show the runtime outputs
						/*String s = null; 
						java.io.BufferedReader stdInput = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
	 			        java.io.BufferedReader stdError = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()));
						System.out.println("Here is the standard output of the command:\n"); 
						while ((s = stdInput.readLine()) != null) { System.out.println(s); } 
						System.out.println("Here is the standard error of the command (if any):\n"); 
						while ((s = stdError.readLine()) != null) { System.out.println(s); }*/
					} catch (Exception e) {
					    throw new SchedulerException("Problems at proceeding of jobs to grid: " + e.getMessage());
					}
					this.jobCounter++;
		   	   	}
	   	    } 
			
			// ... or if the extractor node scheduler will be started directly
			else {
                String sep = File.separator;
                
				// Create a separate job for each music file
				// TODO if the extractor node scheduler will be started locally, ExtractorNodeScheduler can be changed
				// to be able to run extraction of several files? See ProcessorStarter also...
				for(int k=0;k<extractorConfig.getMusicFileList().getFiles().size();k++) {	
					
					ArrayList<Integer> fileId = new ArrayList<Integer>(1);
					fileId.add(extractorConfig.getMusicFileList().getIds().get(k));
					ArrayList<String> filePath = new ArrayList<String>(1);
					filePath.add(extractorConfig.getMusicFileList().getFileAt(k));
					ExtractionConfiguration extractorConfigWithOneFile = new ExtractionConfiguration(new FileTable(fileId,filePath), featureTable);
				
					
					ExtractorNodeScheduler extractorThread = null;
					try {
						extractorThread = new ExtractorNodeScheduler(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + 
								sep + "config" + sep + "node" + sep +"extractor"+ sep + "input" + sep + "task_" + this.jobCounter);
					} catch (NodeException e) {
						throw new SchedulerException("Extractor node thread could not be started: " + e.getMessage());
					}
		
				    // Prepare extractor node scheduler arguments and start it as thread
				    extractorThread.setThreadParameters(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + sep + "config" + sep + "node" + sep + "extractor", this.jobCounter, extractorConfigWithOneFile);
				    Thread newExtractorThread = new Thread(extractorThread);
				    // TODO Timeout einbauen
				    while (this.nodeSchedulers.size() >= AmusePreferences.getInt(KeysIntValue.MAX_NUMBER_OF_TASK_THREADS)) {
						try {
						    Thread.sleep(1000);
						} catch (InterruptedException e) {
						    throw new SchedulerException(this.getClass().getName() + " was interrupted: " + e.getMessage());
						}
				    }
				    this.connectSchedulerToErrorDescriptionList(extractorThread);

				    nodeSchedulers.add(extractorThread);
				    extractorThread.addListener(this);
				    newExtractorThread.start();
				    this.jobCounter++;
				}
			}
		}
	
		// If the node schedulers are started directly (and not e.g. as grid scripts), wait until all jobs are ready
		if (this.startNodeDirectly) {
			// Wait until all jobs are ready
			// TODO Timeout einbauen!
			while (this.nodeSchedulers.size() > 0) {
			    try {
			    	Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new SchedulerException(this.getClass().getName() + " was interrupted: " + e.getMessage());
				}
			}
		}
		
		return this.jobCounter;
	} 
    
    /**
     * Converts the extractor tools base scripts (which provide the extraction of ALL available features), so 
     * that only the features from the given feature table are extracted
     * @param featureTable The given feature table
     */
    private void modifyBaseScripts(FeatureTable featureTable) {
		
    	// ---------------------------------------------
		// (I) Load the mapping of features to extractor
		// ---------------------------------------------
    	
    	// list of all features that are extracted using their regular settings
    	// (no custom settings defined)
    	List<Feature> regularFeatures = new ArrayList<Feature>();
    	// hashmap with the tool for each feature that is extracted using regular settings
		HashMap<Integer, Integer> regularFeature2Tool = new HashMap<Integer, Integer>();
		// hash map with the custom scripts for each extractor tool
		HashMap<Integer, List<String>> tool2CustomScripts = new HashMap<Integer, List<String>>();
		// hash map with the features that are extracted with each custom base script
		HashMap<String, List<Feature>> script2Features = new HashMap<String, List<Feature>>();
		List<Feature> features = featureTable.getSelectedFeatures();
		for (int j = 0; j < features.size(); j++) {
			Feature feature = features.get(j);
			if(feature.getConfigurationId() == null) { // is it a regular feature?
				regularFeature2Tool.put(feature.getId(), feature.getExtractorId());
				regularFeatures.add(feature);
		    } else { // or does it have a custom configuration?
		    	int extractorId = feature.getExtractorId();
				String customScript = feature.getCustomScript();
				if(!tool2CustomScripts.containsKey(extractorId)){
					tool2CustomScripts.put(extractorId, new ArrayList<String>());
				}
				if(!tool2CustomScripts.get(extractorId).contains(customScript)) {
					tool2CustomScripts.get(extractorId).add(customScript);
				}
				if(!script2Features.containsKey(customScript)) {
					script2Features.put(customScript, new ArrayList<Feature>());
				}
				script2Features.get(customScript).add(feature);
		    }
		}
		FeatureTable regularFeatureTable = new FeatureTable(regularFeatures);
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Feature table loaded");
    	
		// --------------------------------------
		// (II) Modify the base extractor scripts
		// --------------------------------------
    	// Load ARFF extractor table
		try {
			DataSetAbstract toolTableSet = new ArffDataSet(new File(AmusePreferences.getFeatureExtractorToolTablePath()));
			Attribute idAttribute = toolTableSet.getAttribute("Id");
			Attribute extractorNameAttribute = toolTableSet.getAttribute("Name");
			Attribute adapterClassAttribute = toolTableSet.getAttribute("AdapterClass");
			Attribute homeFolderAttribute = toolTableSet.getAttribute("HomeFolder");
			Attribute inputExtractorBaseBatchAttribute = toolTableSet.getAttribute("InputBaseBatch");
		    Attribute inputExtractorBatchAttribute = toolTableSet.getAttribute("InputBatch");
			for(int i=0;i<toolTableSet.getValueCount();i++) {
	    		try {
	    			if (regularFeature2Tool.containsValue(new Double(idAttribute.getValueAt(i).toString()).intValue())) {
		    			// convert the base script for the regular features
		    			
		    			// Create extractor adapter for the modification of the
		    			// base script
		    			Class<?> adapter = Class.forName(adapterClassAttribute.getValueAt(i).toString());
		    			ExtractorInterface ead = (ExtractorInterface) adapter.newInstance();
		
						// Set the extractor properties
						Properties extractorProperties = new Properties();
						extractorProperties.setProperty("id", new Integer(new Double(idAttribute.getValueAt(i).toString()).intValue()).toString());
						extractorProperties.setProperty("extractorName", extractorNameAttribute.getValueAt(i).toString());
						extractorProperties.setProperty("extractorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator +"tools" + File.separator + homeFolderAttribute.getValueAt(i));
						extractorProperties.setProperty("inputExtractorBaseBatch", inputExtractorBaseBatchAttribute.getValueAt(i).toString());
						extractorProperties.setProperty("inputExtractorBatch", inputExtractorBatchAttribute.getValueAt(i).toString());
						((AmuseTask) ead).configure(extractorProperties, null, null);
		
						// Convert the base script
						ead.convertBaseScript(regularFeature2Tool, featureTable);
						AmuseLogger.write(this.getClass().getName(), Level.DEBUG, extractorNameAttribute.getValueAt(i)
							+ " base script converted");
	    			}
						
					// convert the base scripts of the custom features
					int extractorId = new Double(idAttribute.getValueAt(i).toString()).intValue();
					if(tool2CustomScripts.containsKey(extractorId)) {
						for(String customScript : tool2CustomScripts.get(extractorId)) {
							FeatureTable customFeatureTable = new FeatureTable(script2Features.get(customScript));
							HashMap<Integer, Integer> customFeature2Tool = new HashMap<Integer, Integer>();
							String customBaseScript = "";
							for(Feature feature : script2Features.get(customScript)) {
								// prepare the feature to tool hash map
								// if one feature is extracted multiple times with the same script
								// (by different tools) we put the current tool in the map
								if(!customFeature2Tool.containsKey(feature.getId()) || feature.getExtractorId() == extractorId) {
									customFeature2Tool.put(feature.getId(), feature.getExtractorId());
								}
								
								// find the path where the modified script should be saved
								if(feature.getExtractorId() == extractorId) {
									DataSet configurationDataSet = new DataSet(new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "features" + File.separator + feature.getId() + ".arff"));
									for(int j = 0; j < configurationDataSet.getValueCount(); j++) {
										if(configurationDataSet.getAttribute("Id").getValueAt(j).equals(feature.getConfigurationId())) {
											String currentBaseScript = configurationDataSet.getAttribute("InputBaseBatch").getValueAt(j).toString();
											if(customBaseScript.equals("")) {
												customBaseScript = currentBaseScript;
											} else if(!customBaseScript.equals(currentBaseScript)) {
												AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Cannot convert custom base script " + currentBaseScript + " for feature " + feature.getId() + " because the input script " + customScript + " is already used for another base script.");
											}
											break;
										}
									}
								}
							}
							
							// Create extractor adapter for the modification of the
			    			// base script
			    			Class<?> adapter = Class.forName(adapterClassAttribute.getValueAt(i).toString());
			    			ExtractorInterface ead = (ExtractorInterface) adapter.newInstance();
			    			
			    			// Set the extractor properties
							Properties extractorProperties = new Properties();
							extractorProperties.setProperty("id", new Integer(new Double(idAttribute.getValueAt(i).toString()).intValue()).toString());
							extractorProperties.setProperty("extractorName", extractorNameAttribute.getValueAt(i).toString());
							extractorProperties.setProperty("extractorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator +"tools" + File.separator + homeFolderAttribute.getValueAt(i));
							extractorProperties.setProperty("inputExtractorBaseBatch", customBaseScript);
							extractorProperties.setProperty("inputExtractorBatch", customScript);
							((AmuseTask) ead).configure(extractorProperties, null, null);
							
							// Convert the base script
							ead.convertBaseScript(customFeature2Tool, featureTable);
							AmuseLogger.write(this.getClass().getName(), Level.DEBUG, extractorNameAttribute.getValueAt(i)
								+ " custom base script " + customBaseScript + " converted");
						}
					}
						
			    } catch (ClassNotFoundException e) {
			    	AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Extractor class cannot be located: "
			    		+ adapterClassAttribute.getValueAt(i));
			    } catch (IllegalAccessException e) {
			    	AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Extractor class or its nullary constructor is not accessible: "
			    		+ adapterClassAttribute.getValueAt(i));
			    } catch (InstantiationException e) {
			    	AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Instantiation failed for extractor class: "
			    		+ adapterClassAttribute.getValueAt(i));
			    } catch (NodeException e) {
			    	AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Conversion of base script for "
			    		+ adapterClassAttribute.getValueAt(i) + " failed: " + e.getMessage());
			    }
	    	}
		} catch(IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Could not load the extractor tool table: " 
					+ e.getMessage());
		}
    }
    
}
