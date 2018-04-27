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
 * Creation date: 16.06.2010
 */
package amuse.nodes.classifier.methods.supervised;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.interfaces.ClassifierInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;

/**
 * Adapter for Matlab Linear Discriminant Analysis. 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class LinearDiscriminantAnalysisAdapter extends AmuseTask implements ClassifierInterface {

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) {
		// Does nothing
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Does nothing
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.classifier.interfaces.ClassifierInterface#classify(java.lang.String, java.util.ArrayList, java.lang.String)
	 */
	public void classify(String pathToModelFile) throws NodeException {
		DataSet dataSetToClassify = ((DataSetInput)((ClassificationConfiguration)this.correspondingScheduler.
				getConfiguration()).getInputToClassify()).getDataSet();
		
		// (1) Save the dataSet as ARFF
		try {
			dataSetToClassify.saveToArffFile(new File(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + "input.arff"));
		} catch(IOException e) {
			throw new NodeException("Could not save the data: " + e.getMessage());
		}
		
		// (2) Run Matlab LDA
		/* OLD try {
			ExternalToolAdapter.runBatch(properties.getProperty("classifierFolder") + "/" + properties.getProperty("startScript"), 
				// ARFF input for LDA classification
				this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + "/input.arff" + " " + 
				// Model (here the training set)
				pathToModelFile + " " + 
			    // Temporal output
				this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + "/output.arff");
		} catch (NodeException e) {
			throw new NodeException("Classification with Matlab failed: " + e.getMessage());
		}*/
		try {
			List<String> commands = new ArrayList<String>();
			commands.add(AmusePreferences.get(KeysStringValue.MATLAB_PATH));
			commands.add("-nodisplay");
			commands.add("-nosplash");
			commands.add("-nojvm");
			commands.add("-r");
			commands.add("matlabLDA('" + 
				// ARFF input for LDA classification
				this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + "input.arff" + "','" + 
				// Model (here the training set)
				pathToModelFile + "','" + 
				// Temporal output
				this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + File.separator + "output.arff" + "')");
			commands.add("-logfile");
			commands.add("\"" + properties.getProperty("classifierFolder") + File.separator + "MatlabClassification.log\"");
			ExternalProcessBuilder matlab = new ExternalProcessBuilder(commands);
			matlab.setWorkingDirectory(new File(properties.getProperty("classifierFolder")));
			matlab.setEnv("MATLABPATH", properties.getProperty("classifierFolder"));
			
			
			
			// Monitor the path that contains the log file
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path pathToWatch = FileSystems.getDefault().getPath(properties.getProperty("classifierFolder"));
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
			        if(filename.toString().equals("MatlabClassification.log")){
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
			        	    	AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Output from the Matlab-log:\n" +errortext);
			        			try{
			        				watcher.close();
			        			}
			        			catch(IOException e){}
			        			matlabProcess.destroy();
			    				throw new NodeException("Classification with Matlab failed");
			        	    
			        	    }
			        	} catch(FileNotFoundException e) {
		        	    	AmuseLogger.write(this.getClass().getName(), Level.WARN, "Unable to monitor the log-File from Matlab. " + e.getMessage());
		        	    	break whileMatlabProcessAlive;
			        	} finally {
			        		if(scanner != null){
				        		scanner.close();
				        	}
						}
			        }
			    }
				try{
					watcher.close();
				}
				catch(IOException e){}
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
			
		} catch (IOException e) {
        	throw new NodeException("Classification with Matlab failed: " + e.getMessage());
        } 
		
		
		// (3) Convert the results to AMUSE format
		try {
			dataSetToClassify.addAttribute(new StringAttribute("PredictedCategory", new ArrayList<String>()));
			FileReader inputReader = new FileReader(new File(this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + 
					this.correspondingScheduler.getTaskId() + File.separator + "output.arff"));
			BufferedReader bufferedInputReader = new BufferedReader(inputReader);
			String line =  new String();
			line = bufferedInputReader.readLine();
			while(line != null) {
				dataSetToClassify.getAttribute("PredictedCategory").addValue(line);
				line = bufferedInputReader.readLine();
			}
			inputReader.close();
		} catch (Exception e) {
			throw new NodeException("Could not parse the classification results: " + e.getMessage());
		}
	}

}
