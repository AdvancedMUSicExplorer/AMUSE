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
 * Creation date: 14.03.2008
 */
package amuse.interfaces.scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;

import amuse.interfaces.nodes.NodeEvent;
import amuse.interfaces.nodes.NodeEventListener;
import amuse.interfaces.nodes.NodeEventSource;
import amuse.interfaces.nodes.NodeScheduler;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.taskstarters.ClassificationStarter;
import amuse.scheduler.taskstarters.ClassificationTrainingStarter;
import amuse.scheduler.taskstarters.ClassificationValidationStarter;
import amuse.scheduler.taskstarters.FeatureExtractionStarter;
import amuse.scheduler.taskstarters.FeatureProcessingStarter;
import amuse.scheduler.taskstarters.OptimizationStarter;
import amuse.util.AmuseLogger;
import amuse.util.FileOperations;

/**
 * All task starters should extend this class
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public abstract class AmuseTaskStarter implements AmuseTaskStarterInterface, NodeEventListener {

    /** The corresponding node location is:
     *  AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "node" + File.separator + this.nodeFolder */
    private String nodeFolder = null;
    
    /** Each Amuse job has its own Id (unique during the lifecycle of Amuse scheduler);
     * task starter receives the Id of the last job and increments it for the jobs of a new task */
    protected long jobCounter;
    
    /** A list used to capture descriptions from NodeSchedulers that failed to complete their computation.
     * To monitor a NodeScheduler, its asasd must be added to this list before execution. 
     */
    protected List<StringBuilder> errorDescriptionsList;
    
    /** If the tasks should be started locally as threads, currently working node schedulers are
     * kept in this list */
    protected ArrayList<NodeEventSource> nodeSchedulers = null;
    
    /** If true, node scheduler will be started directly as thread; if false, it will be started
     * via command line script (e.g. processing the task to grid) */
    protected boolean startNodeDirectly = true;

    /**
     * Constructor
     */
    public AmuseTaskStarter(String nodeFolder, long jobCounter, boolean startNodeDirectly) throws SchedulerException {
    	this.errorDescriptionsList = Collections.synchronizedList(new ArrayList<StringBuilder>());
        this.nodeSchedulers = new ArrayList<NodeEventSource>();
        this.nodeFolder = new String(nodeFolder);
        this.jobCounter = jobCounter;
        this.startNodeDirectly = startNodeDirectly;

        // Clear input folder (it may exist from previous tasks)
        if (this.startNodeDirectly) {
            try {
                removeInputFolder();
            } catch (Exception e) {
                AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Could not clean the node input folder: " + this.nodeFolder);
            }
        }
    }
    
    /**
     * Connects a NodeScheduler to the errorDescriptionsList to enable it to return a description if it fails.
     * @param scheduler
     */
    public void connectSchedulerToErrorDescriptionList(NodeScheduler scheduler){
	    errorDescriptionsList.add(scheduler.getErrorDescriptionBuilder());
    }
    
    public void logResults(){
		String errorText = "";
		int numErrors = 0;
		for(StringBuilder s:errorDescriptionsList){
			if(!s.toString().equals("")){
				errorText += "\n" + s;
				numErrors++;
			}
		}
		
		String jobType = "";
		if(this instanceof FeatureExtractionStarter){
			jobType = "feature extraction";
		}
		else if(this instanceof FeatureProcessingStarter){
			jobType = "feature processing";
		}
		else if(this instanceof ClassificationTrainingStarter){
			jobType = "classification training";
		}
		else if(this instanceof ClassificationStarter){
			jobType = "classification";
		}
		else if(this instanceof ClassificationValidationStarter){
			jobType = "validation";
		}
		else if(this instanceof OptimizationStarter){
			jobType = "optimization";
		}
		if(numErrors == 0){
			AmuseLogger.write(this.getClass().getName(),Level.INFO, errorDescriptionsList.size() + "/" + errorDescriptionsList.size() + " " + jobType + " jobs finished successfully!");
		}
		else{
			AmuseLogger.write(this.getClass().getName(),Level.ERROR, (errorDescriptionsList.size() - numErrors) + "/" + errorDescriptionsList.size() + " " + jobType + " jobs finished successfully!");
			AmuseLogger.write(this.getClass().getName(),Level.ERROR, "Error occured while processing the following files:" + errorText);

		}
	}

    /*
     * (non-Javadoc)
     * @see amuse.interfaces.nodes.NodeEventListener#processEvent(amuse.interfaces.nodes.NodeEvent)
     */
    public void processEvent(NodeEvent event) {
        AmuseLogger.write(this.getClass().getName(), Level.INFO, "Job "
                + ((NodeScheduler) event.getEventSource()).getTaskId() + " ready");
        this.nodeSchedulers.remove(event.getEventSource());

        // DEBUG Show the current number of threads
        //System.out.println("Current Number of Threads: " + this.processorNodeSchedulers.size());
    }

    /**
     * Removes input folder of the corresponding node (which contains intermediate results)
     * @throws Exception
     */
    protected void removeInputFolder() throws Exception {
        File f = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "node" + File.separator + this.nodeFolder + File.separator + "input");
        if(f.exists()) {
	        boolean delete = FileOperations.delete(f, true);
	        if (!delete) {
	            throw new SchedulerException("Can't remove node input folder: " + f);
	        }
        }
    }
}
