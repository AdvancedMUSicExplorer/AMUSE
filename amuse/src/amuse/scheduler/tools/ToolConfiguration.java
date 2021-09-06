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
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.FileTable;
import amuse.data.datasets.ToolConfigSet;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.util.AmuseLogger;

/**
 * Describes the parameters for a tool task 
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class ToolConfiguration extends TaskConfiguration {

	/** For Serializable interface */
	private static final long serialVersionUID = 383846247325262485L;

	/** Class with main method to start the tool */ 
	private final String toolClass;
	
	/** Relative path to tool folder, should be located in AMUSEHOME/tools */
	private final String toolFolder;
	
	/** Data object(s) for which the tool is applied, e.g., music file, ARFF file list, etc. */
	private final FileTable toolObject;
	
	/** Path to XML tool configuration file */
	private final String toolConfiguration;
	
	/** Folder for logs: they are written to the following folder:
	 * DESTINATION_FOLDER/log_x.arff'
	 * where x is equal to the number of files in the folder plus one
	 */
	private final String destinationFolder;
	
	/**
	 * Standard constructor
	 * @param toolClass Class with main method to start the tool
	 * @param toolFolder Relative path to tool folder, should be located in AMUSEHOME/tools
	 * @param toolObject Data object(s) for which the tool is applied, e.g., music file, ARFF file list, etc.
	 * @param toolConfiguration Path to XML tool configuration file
	 * @param destinationFolder Folder for logs
	 */
	public ToolConfiguration(String toolClass, String toolFolder, FileTable toolObject, String toolConfiguration, String destinationFolder) {
		this.toolClass = toolClass;
		this.toolFolder = toolFolder;
		this.toolObject = toolObject;
		this.toolConfiguration = toolConfiguration;
		this.destinationFolder = destinationFolder;
	}
	
	/**
	 * Returns an array of ToolConfigurations from the given data set
	 * @param optimizerConfig ARFF file with configurations for one or more tool tasks
	 * @return ToolConfigurations
	 * @throws IOException 
	 */
	public static ToolConfiguration[] loadConfigurationsFromDataSet(ToolConfigSet toolConfig) throws IOException {
		ArrayList<ToolConfiguration> taskConfigurations = new ArrayList<ToolConfiguration>();
			
   		// Proceed music file lists one by one
	    for(int i=0;i<toolConfig.getValueCount();i++) {
	    	String currentToolClass = toolConfig.getToolClassAttribute().getValueAt(i).toString();
	    	String currentToolFolder = toolConfig.getToolFolderAttribute().getValueAt(i).toString();
	    	String currentToolObject = toolConfig.getToolObjectAttribute().getValueAt(i).toString();
	    	String currentToolConfiguration = toolConfig.getToolConfigurationAttribute().getValueAt(i).toString();
			String currentDestinationFolder = toolConfig.getDestinationFolderAttribute().getValueAt(i).toString();
				
			// Proceed music files from the current file list
			FileTable currentFileTable = new FileTable(new File(currentToolObject));
						
			// Create an optimization task
		    taskConfigurations.add(new ToolConfiguration(currentToolClass, currentToolFolder, currentFileTable,
		    		currentToolConfiguration, currentDestinationFolder));
			AmuseLogger.write(ToolConfiguration.class.getName(), Level.DEBUG, 
					taskConfigurations.size() + " tool task(s) loaded");
		}
		
		// Create an array
	    ToolConfiguration[] tasks = new ToolConfiguration[taskConfigurations.size()];
		for(int i=0;i<taskConfigurations.size();i++) {
	    	tasks[i] = taskConfigurations.get(i);
	    }
		return tasks;
	}
	
	/**
	 * Returns an array of ToolConfigurations from the given ARFF file
	 * @param configurationFile ARFF file with configurations for one or more tool tasks
	 * @return ToolConfigurations
	 */
	public static ToolConfiguration[] loadConfigurationsFromFile(File configurationFile) throws IOException {
		ToolConfigSet toolConfig = new ToolConfigSet(configurationFile);
		return loadConfigurationsFromDataSet(toolConfig);
	}

	/**
	 * @return toolClass
	 */
	public String getToolClass() {
		return toolClass;
	}
	
	/**
	 * @return toolFolder
	 */
	public String getToolFolder() {
		return toolFolder;
	}
	
	/**
	 * @return toolObject
	 */
	public FileTable getToolObject() {
		return toolObject;
	}
	
	/**
	 * @return toolConfiguration
	 */
	public String getToolConfiguration() {
		return toolConfiguration;
	}

	/**
	 * @return Destination folder
	 */
	public String getDestinationFolder() {
		return destinationFolder;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getType()
	 */
	public String getType() {
		return "Tool";
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getDescription()
	 */
	public String getDescription() {
		return new String("Tool class: " + toolClass + " folder: " + toolFolder +  " object: " + toolObject + "configuration: " + toolConfiguration);
	}
}
