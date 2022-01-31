/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2021 by code authors
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
 * Creation date: 15.02.2019
 */
package amuse.scheduler.taskstarters;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Level;

import amuse.data.FileTable;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.scheduler.AmuseTaskStarter;
import amuse.interfaces.scheduler.SchedulerException;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysIntValue;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.tools.ToolConfiguration;
import amuse.scheduler.tools.ToolScheduler;
import amuse.util.AmuseLogger;

/**
 * This scheduler class starts a tool
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class ToolStarter extends AmuseTaskStarter {

	/**
	 * Constructor
	 */
	public ToolStarter(String nodeFolder, long jobCounter, boolean startNodeDirectly) throws SchedulerException {
		super(nodeFolder,jobCounter,startNodeDirectly);
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.scheduler.AmuseTaskStarterInterface#startTask(amuse.interfaces.nodes.TaskConfiguration[], java.util.Properties)
	 */
	public long startTask(TaskConfiguration[] taskConfiguration, Properties props) throws SchedulerException {

		// TODO v0.3 Currently, it is expected that attribute ToolObject in toolConfig.arff contains the file list with data objects to be processed with the tool.
		// Later, it should be extended with another attribute which describes this object (e.g., FILE_LIST, SINGLE_FILE) and distinguished here, whether several
		// tool jobs should be generated or only one.
		
		// Create tasks for only one music file 
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Creating jobs for " + taskConfiguration.length + " tasks..");
		ArrayList<ToolConfiguration> oneTaskConfigs = new ArrayList<ToolConfiguration>();
		for (int i = 0; i < taskConfiguration.length; i++) {
			FileTable musicFileList = ((ToolConfiguration)taskConfiguration[i]).getToolObject();
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "Creating jobs for " + musicFileList.getFiles().size() + " files..");
			for(int k=0;k<musicFileList.getFiles().size();k++) {	
				ArrayList<Integer> fileId = new ArrayList<Integer>(1);
				fileId.add(musicFileList.getIds().get(k));
				ArrayList<String> filePath = new ArrayList<String>(1);
				filePath.add(musicFileList.getFileAt(k));
				oneTaskConfigs.add(new ToolConfiguration(((ToolConfiguration)taskConfiguration[i]).getToolClass(),
						((ToolConfiguration)taskConfiguration[i]).getToolFolder(),
						(new FileTable(fileId,filePath)),
						((ToolConfiguration)taskConfiguration[i]).getToolConfiguration(),
						((ToolConfiguration)taskConfiguration[i]).getDestinationFolder()));
			}
		}
		
		// Generate and proceed Amuse jobs
		for (int i = 0; i < oneTaskConfigs.size(); i++) {
			
			// If the optimizer node scheduler will be started via grid or batch script...
			if (!this.startNodeDirectly) {
				
				// How many Amuse jobs should be proceeded to one grid machine?
				int numberOfJobsToMerge = AmusePreferences.getInt(KeysIntValue.NUMBER_OF_JOBS_PER_GRID_MACHINE);
				if(oneTaskConfigs.size() - i < numberOfJobsToMerge) {
					numberOfJobsToMerge = oneTaskConfigs.size() - i;
				}
				ToolConfiguration[] toolConfig = new ToolConfiguration[numberOfJobsToMerge];
				for(int k=0;k<numberOfJobsToMerge;k++) {
					toolConfig[k] = oneTaskConfigs.get(i);
					AmuseLogger.write(this.getClass().getName(), Level.INFO, "Tool task script for tool "
							+ toolConfig[k].getToolClass() + " is prepared");
					i++;
				}
				i--; // Since the for-loop increments i  
				
	   	   		FileOutputStream fos = null;
	   	   		ObjectOutputStream out = null;
	   	   		try {
	   	   			fos = new FileOutputStream(new String(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "taskoutput" + File.separator + "task_" + 
	   	   					this.jobCounter + ".ser"));
	   	   		    out = new ObjectOutputStream(fos);
	   	   		    out.writeObject(toolConfig);
	   	   		    out.close();
	   	   	    } catch(IOException ex) {
	   	   		    ex.printStackTrace();
	   	   	    }
	   	    	    
	   	    	// Create parameter line
				String parameterString = new String();
				parameterString = new Long(this.jobCounter).toString();
				//parameterString += "\"";
				//parameterString += " &";
				//parameterString = "nohup " + parameterString;
				// FIXME As different tools may use very different grid scripts, set up the path to them in toolConfig.arff individually and remove KeysStringValue.GRID_SCRIPT_TOOL from properties!!
				System.out.println(AmusePreferences.get(KeysStringValue.GRID_SCRIPT_TOOL) + " " + parameterString);
				try {
					FileOutputStream values_toTest = new FileOutputStream(new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + "/config/jobcounter.txt"));
					DataOutputStream values_writerTest = new DataOutputStream(values_toTest);
					values_writerTest.writeBytes(new Long(jobCounter+1).toString());
					values_toTest.close();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// Proceed script to grid
				Process process;
				try {
					process = Runtime.getRuntime().exec(AmusePreferences.get(KeysStringValue.GRID_SCRIPT_TOOL) + " " + parameterString);
				} catch (IOException e) {
				    throw new SchedulerException("Error on proceeding a script to the grid: " + e.getMessage());
				}
		
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
				try {
					System.out.println("Waiting 30 seconds...");
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	   	    } 
			
			// ... or if the tool scheduler will be started directly
			else {
	   	    	ToolConfiguration toolConfig = (ToolConfiguration)oneTaskConfigs.get(i);
				ToolScheduler toolThread = null;
				try {
					toolThread = new ToolScheduler(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + 
							File.separator + "tools" + File.separator + toolConfig.getToolFolder() + File.separator + "task_" + this.jobCounter);
				} catch (NodeException e) {
					throw new SchedulerException("Tool thread could not be started: " + e.getMessage());
				}
				
			    // Prepare tool scheduler arguments and start it as thread
				toolThread.setThreadParameters(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator +  
						toolConfig.getToolFolder(), this.jobCounter, toolConfig);
			    Thread newToolThread = new Thread(toolThread);
			    // TODO Timeout 
			    while (this.nodeSchedulers.size() >= AmusePreferences.getInt(KeysIntValue.MAX_NUMBER_OF_TASK_THREADS)) {
					try {
					    Thread.sleep(1000);
					} catch (InterruptedException e) {
					    throw new SchedulerException(this.getClass().getName() + " was interrupted: " + e.getMessage());
					}
			    }
			    this.connectSchedulerToErrorDescriptionList(toolThread);
			    nodeSchedulers.add(toolThread);
			    toolThread.addListener(this);
			    newToolThread.start();
			}
			this.jobCounter++;
		}
			
		// If the node schedulers are started directly (and not e.g. as grid scripts), wait until all jobs are ready
		if(this.startNodeDirectly) {
			
			// TODO Timeout
			while(this.nodeSchedulers.size() > 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new SchedulerException(this.getClass().getName() + " was interrupted: " + e.getMessage());
				}
			}
		} 
		
		return this.jobCounter;
	}

}
