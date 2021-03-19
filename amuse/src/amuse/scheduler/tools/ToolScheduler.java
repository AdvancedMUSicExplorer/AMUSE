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
package amuse.scheduler.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Properties;

import org.apache.log4j.Level;

import amuse.interfaces.nodes.NodeEvent;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.NodeScheduler;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * ToolScheduler is responsible for tools. 
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class ToolScheduler extends NodeScheduler { 
	
	/** Tool adapter */
	ToolInterface tad = null;

	/**
	 * Constructor
	 */
	public ToolScheduler(String folderForResults) throws NodeException {
		super(folderForResults);
	}
	
	/**
	 * Main method for tool
	 * @param args Tool configuration
	 */
	public static void main(String[] args) {
		
		// Create the node scheduler
		ToolScheduler thisScheduler = null;
		try {
			thisScheduler = new ToolScheduler(args[0] + File.separator + "input" + File.separator + "task_" + args[1]);
		} catch(NodeException e) {
			AmuseLogger.write(ToolScheduler.class.getName(), Level.ERROR,
					"Could not create folder for tool intermediate results: " + e.getMessage());
			return;
		}
		
		// Proceed the task
		thisScheduler.proceedTask(args);
		
		// Remove the folder for input and intermediate results
		try {
			thisScheduler.removeInputFolder();
		} catch(NodeException e) {
				AmuseLogger.write(ToolScheduler.class.getClass().getName(), Level.WARN,
					"Could not remove properly the folder with intermediate results '" + 
					thisScheduler.nodeHome + File.separator + "input" + File.separator + "task_'" + thisScheduler.jobId + 
					"; please delete it manually! (Exception: "+ e.getMessage() + ")");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String, long, amuse.interfaces.nodes.TaskConfiguration)
	 */
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration toolConfiguration) {
		
		// ----------------------------
		// (I) Configure tool scheduler
		// ----------------------------
		this.nodeHome = nodeHome;
		if(this.nodeHome.startsWith(AmusePreferences.get(KeysStringValue.AMUSE_PATH))) {
			this.directStart = true;
		}
		this.jobId = new Long(jobId);
		this.taskConfiguration = toolConfiguration;
		
		// If this node is started directly, the properties are loaded from AMUSEHOME folder;
		// if this node is started via command line (e.g. in a grid, the properties are loaded from
		// %optimizer home folder%/input
		if(!this.directStart) {
			File preferencesFile = new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "amuse.properties");
			AmusePreferences.restoreFromFile(preferencesFile);
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Tool scheduler for tool " + 
				((ToolConfiguration)this.taskConfiguration).getToolClass() + " started");
		
		// -----------------------
		// (II) Configure the tool
		// -----------------------
		try {
			this.configureTool();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Configuration of tool failed: " + e.getMessage()); 
			errorDescriptionBuilder.append(taskConfiguration.getDescription());
			this.fireEvent(new NodeEvent(NodeEvent.TOOL_FAILED, this));
			return;
		}
		
		// --------------------
		// (III) Start the tool
		// --------------------
		try {
			this.toolStart();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Tool start failed: " + e.getMessage()); 
			errorDescriptionBuilder.append(taskConfiguration.getDescription());
			this.fireEvent(new NodeEvent(NodeEvent.TOOL_FAILED, this));
			return;
		}
		
		// ----------------------------------------------------------------------------------
		// (IV) If started directly, remove generated data and fire event for Amuse scheduler
		// ----------------------------------------------------------------------------------
		if(this.directStart) {
			try {
				this.cleanInputFolder();
			} catch(NodeException e) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Could not remove properly the intermediate results '" + 
					this.nodeHome + File.separator + "input" + File.separator + "task_'" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
			}
			this.fireEvent(new NodeEvent(NodeEvent.TOOL_COMPLETED, this));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String[])
	 */
	public void proceedTask(String[] args) {
		if(args.length < 2) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL, 2 - args.length + 
					" arguments are missing; The usage is 'ToolScheduler %1 %2', where: \n" +
					"%1 - Home folder of this node\n" +
					"%2 - Unique (for currently running Amuse instance) task Id\n"); 
			System.exit(1);
		}
		
		// Load the task configuration from %TOOLHOME%/task.ser
		ToolConfiguration toolConfig[] = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(args[0] + File.separator + "task_" + args[1] + ".ser");
			in = new ObjectInputStream(fis);
			Object o = in.readObject();
			toolConfig = (ToolConfiguration[])o;
			in.close();
		} catch(IOException ex) {
		    ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Proceed the tool task(s)
		for(int i=0;i<toolConfig.length;i++) {
			proceedTask(args[0],new Long(args[1]),toolConfig[i]);
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "Tool is going to start job " + 
					(i+1) + File.separator + toolConfig.length);
		}
	}
	
	/**
	 * Configures the tool
	 * @throws NodeException
	 */
	private void configureTool() throws NodeException {
		try {
			try {
				Class <?> toolClass = Class.forName(((ToolConfiguration)this.taskConfiguration).getToolClass());
				this.tad = (ToolInterface)toolClass.newInstance();
			} catch (ClassNotFoundException e1) {
				throw new NodeException("Tool class cannot be located: " + ((ToolConfiguration)this.taskConfiguration).getToolClass());
			} catch (InstantiationException e) {
				throw new NodeException("Instantiation failed for tool class: " + ((ToolConfiguration)this.taskConfiguration).getToolClass());
			} catch (IllegalAccessException e) {
				throw new NodeException("Optimizer class or its nullary constructor is not accessible:  " + ((ToolConfiguration)this.taskConfiguration).getToolClass());
			}
		
			Properties toolProperties = new Properties();
			toolProperties.setProperty("toolConfigurationXML",((ToolConfiguration)this.taskConfiguration).getToolConfiguration());
			toolProperties.setProperty("pathToMusicFile",((ToolConfiguration)this.taskConfiguration).getToolObject().getFileAt(0).toString());
			
			((AmuseTask)this.tad).configure(toolProperties, this, null);
	    } catch(NodeException e) {
	    	e.printStackTrace(); System.exit(1);
	    	throw new NodeException(e.getMessage());
	    }
	}
	
	/**
	 * Starts the tool
	 * @throws NodeException
	 */
	private void toolStart() throws NodeException {
		try {
	    	this.tad.startTool();
	    } catch(NodeException e) {
			throw e;
	    }
	}
	
	/**
	 * Starts the tool - for tools with a main method
	 * @throws NodeException
	 */
	/*private void toolStart() throws NodeException {
		try {
			Class<?> toolClass = Class.forName(((ToolConfiguration)this.taskConfiguration).getToolClass());
			Method mainMethod = toolClass.getMethod("main", String[].class);
		    String[] params = new String[1];
		    params[0] = ((ToolConfiguration)this.taskConfiguration).getToolConfiguration();
		    mainMethod.invoke(null, (Object) params);
	    } catch (ClassNotFoundException e) {
			throw new NodeException("Could not find the appropriate class to start the tool: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new NodeException("Illegal access during tool start: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new NodeException("No main method in the tool class: " + e.getMessage());
		} catch (SecurityException e) {
			throw new NodeException("Security exception: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new NodeException("Illegal argument exception: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new NodeException("Invocation target exception: " + e.getMessage());
		}
	}*/

	
}

