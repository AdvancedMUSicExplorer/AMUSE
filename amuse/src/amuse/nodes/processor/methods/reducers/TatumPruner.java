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
 * Creation date: 16.11.2007
 */
package amuse.nodes.processor.methods.reducers;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Performs tatum reduction of the given feature files
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class TatumPruner extends AmuseTask implements DimensionProcessorInterface {

	/** If true, the features from windows with tatum times are processed;
	 * If false, the features from the middle window between two tatum windows are processed */
	private boolean useTatumWindows = false;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString.equals(new String("t"))) {
			useTatumWindows = true;
		} else {
			useTatumWindows = false;
		}
	}
	
	/**
	 * Perform tatum reduction of the given feature files
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting tatum reduction...");
		
		// Load the tatum times for this file, using the file name of the first feature
		// for finding the path to tatum times file (ID = 416)
		Double[] tatumTimes = loadTatumTimes();
		
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalStepSize();
			
		// Go through features
		for(int j=0;j<features.size();j++) {
			int sampleRate = features.get(j).getSampleRate();
			   	
			if(this.useTatumWindows) {
				features.get(j).getHistory().add(new String("Tatum_reduced"));
			} else {
				features.get(j).getHistory().add(new String("Between_tatum_reduced"));
			}

			// Go through all features values and save only the features from windows containing tatum times
			int currentTatumTimeNumber = 0;
			int windowOfCurrentTatum = 0;
			if(this.useTatumWindows) {
				windowOfCurrentTatum = new Double(Math.floor(tatumTimes[currentTatumTimeNumber]*sampleRate/windowSize)).intValue();
			} else {
				windowOfCurrentTatum = new Double(Math.floor(tatumTimes[currentTatumTimeNumber]*sampleRate/windowSize)).intValue()/2;
			}
					
			// Go through all time windows
			for(int k=0;k<features.get(j).getWindows().size();k++) {
			
				int currentWindow = features.get(j).getWindows().get(k).intValue()-1;
						
				// The value remains
				if(windowOfCurrentTatum == currentWindow) {
							
					// Go to the next tatum time. If the next tatum times corresponds to the
					// same time window, proceed further!
					while(currentTatumTimeNumber < tatumTimes.length-1) {
						currentTatumTimeNumber++;
						int windowOfNextTatum;
						if(this.useTatumWindows) {
							windowOfNextTatum = new Double(Math.floor(tatumTimes[currentTatumTimeNumber]*sampleRate/windowSize)).intValue();
						} else {
							windowOfNextTatum = (new Double(Math.floor(tatumTimes[currentTatumTimeNumber-1]*sampleRate/windowSize)).intValue() +
												new Double(Math.floor(tatumTimes[currentTatumTimeNumber]*sampleRate/windowSize)).intValue())/2;
						}
						if(windowOfCurrentTatum != windowOfNextTatum) {
							windowOfCurrentTatum = windowOfNextTatum;
							break;
						}
						if(this.useTatumWindows) {
							windowOfCurrentTatum = new Double(Math.floor(tatumTimes[currentTatumTimeNumber]*sampleRate/windowSize)).intValue();
						} else {
							windowOfCurrentTatum = (new Double(Math.floor(tatumTimes[currentTatumTimeNumber-1]*sampleRate/windowSize)).intValue() +
									new Double(Math.floor(tatumTimes[currentTatumTimeNumber]*sampleRate/windowSize)).intValue())/2;
						}
					} 
				} 
					
				// Remove features from tatum or between tatum windows
				else {
					features.get(j).getValues().remove(k);
					features.get(j).getWindows().remove(k);
					k--;
				}
		    }
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...reduction succeeded");
	}
	
	/**
	 * Loads the tatum times
	 * @return Double array with time values in ms
	 */
	private Double[] loadTatumTimes() throws NodeException {
		Double[] eventTimes = null;
		
		String idPostfix = new String("_416.arff");

		try {
			
			// Load the tatum times, using the file name of the first feature
			// for finding the path to feature files (ID = 416)
			String currentTatumFile = ((ProcessingConfiguration)this.correspondingScheduler.getConfiguration()).getMusicFileList().getFileAt(0);
				
			// Calculate the path to tatum file
			String relativeName = new String();
			if(currentTatumFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
				relativeName = currentTatumFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length());
			} else {
				relativeName = currentTatumFile;
			}
			if(relativeName.charAt(0) == File.separatorChar) {
				relativeName = relativeName.substring(1);
			}
			relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
			if(relativeName.lastIndexOf(File.separator) != -1) {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
					relativeName.substring(relativeName.lastIndexOf(File.separator)) + idPostfix;
			} else {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
						File.separator + relativeName + idPostfix;
			}	
			
			DataSetAbstract eventTimesSet = new ArffDataSet(new File(relativeName));
			eventTimes = new Double[eventTimesSet.getValueCount()];
			for(int i=0;i<eventTimes.length;i++) {
				eventTimes[i] = new Double(eventTimesSet.getAttribute("Tatum times").getValueAt(i).toString());
			}
		} catch(Exception e) {
			throw new NodeException("Could not load the time events: " + e.getMessage());
		}
		return eventTimes;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

}
