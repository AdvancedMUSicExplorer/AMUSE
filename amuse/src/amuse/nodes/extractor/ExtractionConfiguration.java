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
 * Creation date: xx
 */ 
package amuse.nodes.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import amuse.data.FeatureTable;
import amuse.data.FileTable;
import amuse.data.datasets.ExtractorConfigSet;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Describes the parameters for a feature extraction task 
 *  
 * @author Clemens Waeltken
 * @version $Id$
 */
public class ExtractionConfiguration extends TaskConfiguration {

	/** For Serializable interface */
	private static final long serialVersionUID = 4534520214095700389L;

	/** Music file list to extract features from */
	private final FileTable musicFileList;

	/** Feature list */
	private final FeatureTable features;
	
	/** Folder to store the extracted features (default: Amuse extracted feature database) */
	private String featureDatabase;

	/**
	 * Standard constructor
	 * @param musicFile Music file list for feature extraction
	 * @param features Features to extract
	 */
	public ExtractionConfiguration(FileTable musicFileList, FeatureTable features) {
		this.musicFileList = musicFileList;
		this.features = features;
		this.setFeatureDatabase(AmusePreferences.get(KeysStringValue.FEATURE_DATABASE));
	}

	/**
	 * Returns an array of ExtractorConfigurations from the given data set
	 * @param extractorConfig Data set with configurations for one or more extraction tasks
	 * @return ExtractorConfigurations
	 */
	public static ExtractionConfiguration[] loadConfigurationsFromDataSet(ExtractorConfigSet extractorConfig) throws IOException {
	    ArrayList<ExtractionConfiguration> taskConfigurations = new ArrayList<ExtractionConfiguration>();
   		
	    // Proceed music file lists one by one
	    for(int i=0;i<extractorConfig.getValueCount();i++) {
			String currentMusicFileList = extractorConfig.getMusicFileListAttribute().getValueAt(i).toString();
			String currentFeatureTableString = extractorConfig.getFeatureTableAttribute().getValueAt(i).toString();

			// Load the file and feature tables
			FileTable currentFileTable = new FileTable(new File(currentMusicFileList));
			FeatureTable currentFeatureTable = new FeatureTable(new File(currentFeatureTableString));
			
			taskConfigurations.add(new ExtractionConfiguration(currentFileTable,currentFeatureTable));
			AmuseLogger.write(ExtractionConfiguration.class.getName(), Level.DEBUG, taskConfigurations.size() + " extraction task(s) for " + currentMusicFileList + " loaded");
		}

		// Create an array
	    ExtractionConfiguration[] tasks = new ExtractionConfiguration[taskConfigurations.size()];
		for(int i=0;i<taskConfigurations.size();i++) {
	    	tasks[i] = taskConfigurations.get(i);
	    }
		return tasks;
	}
	
	/**
	 * Returns an array of ExtractorConfigurations from the given ARFF file
	 * @param configurationFile ARFF file with configurations for one or more extraction tasks
	 * @return ExtractorConfigurations
	 */
	public static ExtractionConfiguration[] loadConfigurationsFromFile(File configurationFile) throws IOException {
		ExtractorConfigSet extractionConfig = new ExtractorConfigSet(configurationFile);
		return loadConfigurationsFromDataSet(extractionConfig);	
	}

	/**
	 * @return Music file list to extract features from
	 */
	public FileTable getMusicFileList() {
		return this.musicFileList;
	}

	/**
	 * @return Array of feature IDs.
	 */
	public List<Integer> getFeatureIds() {
		return this.features.getSelectedIds();
	}

	/**
	 * @return Feature table
	 */
	public FeatureTable getFeatureTable() {
		return this.features;
	}

	/**
	 * Sets the path to folder to store the extracted features (default: Amuse extracted feature database)
	 * @param featureDatabase Path to folder
	 */
	public void setFeatureDatabase(String featureDatabase) {
		this.featureDatabase = featureDatabase;
	}

	/**
	 * @return Folder to store the extracted features (default: Amuse extracted feature database)
	 */
	public String getFeatureDatabase() {
		return featureDatabase;
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getType()
	 */
	public String getType() {
		return "Feature Extraction";
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.TaskConfiguration#getDescription()
	 */
	public String getDescription() {
		return new String("Number of files: " + musicFileList.getFiles().size() + " Number of features: " + features.getSelectedIds().size());
	}

	
}
