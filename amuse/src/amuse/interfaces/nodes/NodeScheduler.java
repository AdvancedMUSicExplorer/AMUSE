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
 * Creation date: 13.03.2008
 */
package amuse.interfaces.nodes;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;
import static amuse.util.FileOperations.*;

/**
 * All node schedulers should extend this class
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public abstract class NodeScheduler implements NodeSchedulerInterface, Runnable, NodeEventSource {

	/** Home folder of this node */
	protected String nodeHome = null; 
	
	/** If this node scheduler is started directly as a thread, Amuse scheduler listens for events */
	private NodeEventListener amuseScheduler = null;
	
	/** Id of amuse task performed by this scheduler. Used for: 
	 * (1) naming of folder for intermediate data (this.nodeHome/input/task_xx)
	 * (2) after the task is ready, Amuse scheduler is informed about it with task id */
	protected long jobId;

	/** If this node is started directly, will be set to true; if this node
	 * is started via command line (e.g. in grid), will remain as false */
	protected boolean directStart = false;
	
	/** Should the node input folder be cleaned? (default: true)
	 * Set to false if this node is started from another node */
	protected boolean cleanInputFolder = true;
	
	/** Properties for this node */
	protected Properties properties = null;
	
	/** Parameter configuration of the Amuse task which is currently executed by this node */
	protected TaskConfiguration taskConfiguration = null;
	
	/**
	 * Constructor, here the folder for input and intermediate results of this node is created 
	 */
	public NodeScheduler(String folderForResults) throws NodeException {
		nodeHome = new String();
		properties = new Properties();
		if(!new File(folderForResults).exists()) {
			if (!new File(folderForResults).mkdirs()) {
				throw new NodeException("Could not create folder for processor node intermediate results!");
			}
	    }
	}
	
	/**
	 * @see amuse.interfaces.nodes.NodeEventSource#addListener(amuse.interfaces.nodes.NodeEventListener)
	 */
    @Override
	public void addListener(NodeEventListener listener) {
		this.amuseScheduler = listener;
		
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeEventSource#fireEvent(amuse.interfaces.nodes.NodeEvent)
	 */
    @Override
	public void fireEvent(NodeEvent event) {
		
		// If this NodeScheduler is started from another node (e.g. TrainingNodeScheduler from 
		// ValidatorNodeScheduler, Amuse scheduler does not listen!
		if(this.amuseScheduler != null) {
			this.amuseScheduler.processEvent(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeEventSource#removeListener(amuse.interfaces.nodes.NodeEventListener)
	 */
    @Override
	public void removeListener(NodeEventListener listener) {
		this.amuseScheduler = null;
	}
	
	/**
	 * Returns the task id
	 * @return Task id
	 */
	public long getTaskId() {
		return this.jobId;
	}
	
	/**
	 * Returns the home folder
	 * @return Home folder
	 */
	public String getHomeFolder() {
		return this.nodeHome;
	}
	
	/**
	 * Returns the flag if this node is started directly
	 * @return Flag if this node is started directly
	 */
	public boolean getDirectStart() {
		return this.directStart;
	}
	
	/**
	 * @return the cleanInputFolder
	 */
	public boolean isCleanInputFolder() {
		return cleanInputFolder;
	}

	/**
	 * @param cleanInputFolder the cleanInputFolder to set
	 */
	public void setCleanInputFolder(boolean cleanInputFolder) {
		this.cleanInputFolder = cleanInputFolder;
	}
	
	/**
	 * Returns the task configuration of this node
	 * @return Task configuration of this node
	 */
	public TaskConfiguration getConfiguration() {
		return this.taskConfiguration;
	}
	
	/**
	 * Sets the parameters for the node task which is started in run() method
	 * @param args Task parameters
	 */
	public void setThreadParameters(String homeFolder, long jobId, TaskConfiguration taskConfiguration) {
		this.nodeHome = homeFolder;
		this.jobId = jobId;
		this.taskConfiguration = taskConfiguration;
	}
	
	/**
	 * Runs this scheduler as a thread
	 */
    @Override
	public void run() {
		AmuseLogger.write(this.getClass().getName(),Level.DEBUG,this.getClass().getName() + " thread started");
		proceedTask(this.nodeHome, this.jobId, this.taskConfiguration);
		
		// Remove the input folder after the task is ready
		try { 
			this.removeInputFolder();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(),Level.ERROR, "Could not remove properly the folder with intermediate results '" + 
					this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
		}
	}
	
	/**
	 * Removes node input folder with intermediate results
	 */
	protected void removeInputFolder() throws NodeException {
            File f = new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId);
            if (!delete(f)) {
            	AmuseLogger.write(this.getClass().getName(),Level.WARN, "Could not remove properly the folder with intermediate results '" + 
            			this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + "; please delete it manually!");
                //throw new NodeException("Could not delete file: " + f.getAbsolutePath());
            }
	}
	
	/**
	 * Cleans the node input folder
	 */
	protected void cleanInputFolder() throws NodeException {
		if(!cleanInputFolder)
                    return;
        File list = new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId);
        File[] a = null;
        if(list.exists() && list != null) {
        	a = list.listFiles();
        }
		for(int i=0;i<a.length;i++) {
			File f = a[i];
			
			if(!delete(f, true))
                    	AmuseLogger.write(this.getClass().getName(),Level.WARN, "Could not clean the folder with intermediate results '" + 
                    			this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + "; please delete it manually!");
                        // TODO v0.2 because of Weka ArffLoader bug (streams remain opened)
                    	// throw new NodeException("Could not delete file: " + f.getAbsolutePath());
                }
	}
	
}
