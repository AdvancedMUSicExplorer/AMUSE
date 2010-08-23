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
 * Creation date: 04.01.2009
 */
package amuse.nodes.processor.methods.reducers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;

import amuse.data.Feature;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Performs onset reduction of the given feature files
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class OnsetPruner extends AmuseTask implements DimensionProcessorInterface {

	/** If true, the features from windows with onset times are processed;
	 * If false, the features from the middle window between two onset windows are processed */
	private boolean useOnsetWindows = false;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString.equals(new String("t"))) {
			useOnsetWindows = true;
		} else {
			useOnsetWindows = false;
		}
	}
	
	/**
	 * Perform onset reduction of the given feature files
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting onset reduction...");
		
		try {
		
			// (1) Load the onset times for this file, using the file name of the first feature
			// for finding the path to onset times file (ID = 419)
			String currentOnsetFile = ((ProcessingConfiguration)this.correspondingScheduler.getConfiguration()).getMusicFileList().getFileAt(0);
				
			// Calculate the path to onset file
			String relativeName = new String();
			if(currentOnsetFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
				relativeName = currentOnsetFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length()+1);
			} else {
				relativeName = currentOnsetFile;
			}
			relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
			if(relativeName.lastIndexOf(File.separator) != -1) {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
					relativeName.substring(relativeName.lastIndexOf(File.separator)) + "_419.arff";
			} else {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
					File.separator + relativeName + "_419.arff";
			}	
			
			ArffLoader onsetArffLoader = new ArffLoader();
			onsetArffLoader.setFile(new File(relativeName));
			Attribute onsetTimesAttribute = onsetArffLoader.getStructure().attribute("Onset times");
			Instance onsetInstance = onsetArffLoader.getNextInstance(onsetArffLoader.getStructure());
			ArrayList<Double> onsetTimes = new ArrayList<Double>();
			while(onsetInstance != null) {
				onsetTimes.add(onsetInstance.value(onsetTimesAttribute));
				onsetInstance = onsetArffLoader.getNextInstance(onsetArffLoader.getStructure());
			}
			onsetArffLoader.reset();
				
			// TODO Currently only 22050 sampling rate is supported!
			int sampleRate = 22050;
			int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalFrameSize();
			
			// Go through features
			for(int j=0;j<features.size();j++) {
			   	
				if(this.useOnsetWindows) {
					features.get(j).getHistory().add(new String("Onset_reduced"));
				} else {
					features.get(j).getHistory().add(new String("Between_onset_reduced"));
				}

				// Go through all features values and save only the features from windows containing onset times
				int currentOnsetTimeNumber = 0;
				int windowOfCurrentOnset = 0;
				if(this.useOnsetWindows) {
					windowOfCurrentOnset = new Double(Math.floor(onsetTimes.get(currentOnsetTimeNumber)*sampleRate/windowSize)).intValue();
				} else {
					windowOfCurrentOnset = new Double(Math.floor(onsetTimes.get(currentOnsetTimeNumber)*sampleRate/windowSize)).intValue()/2;
				}
					
				// Go through all time windows
				for(int k=0;k<features.get(j).getWindows().size();k++) {
				
					int currentWindow = features.get(j).getWindows().get(k).intValue()-1;
						
					// The value remains
					if(windowOfCurrentOnset == currentWindow) {
							
						// Go to the next onset time. If the next onset times corresponds to the
						// same time window, proceed further!
						while(currentOnsetTimeNumber < onsetTimes.size()-1) {
							currentOnsetTimeNumber++;
							int windowOfNextOnset;
							if(this.useOnsetWindows) {
								windowOfNextOnset = new Double(Math.floor(onsetTimes.get(currentOnsetTimeNumber)*sampleRate/windowSize)).intValue();
							} else {
								windowOfNextOnset = (new Double(Math.floor(onsetTimes.get(currentOnsetTimeNumber-1)*sampleRate/windowSize)).intValue() +
													new Double(Math.floor(onsetTimes.get(currentOnsetTimeNumber)*sampleRate/windowSize)).intValue())/2;
							}
							if(windowOfCurrentOnset != windowOfNextOnset) {
								windowOfCurrentOnset = windowOfNextOnset;
								break;
							}
							if(this.useOnsetWindows) {
								windowOfCurrentOnset = new Double(Math.floor(onsetTimes.get(currentOnsetTimeNumber)*sampleRate/windowSize)).intValue();
							} else {
								windowOfCurrentOnset = (new Double(Math.floor(onsetTimes.get(currentOnsetTimeNumber-1)*sampleRate/windowSize)).intValue() +
										new Double(Math.floor(onsetTimes.get(currentOnsetTimeNumber)*sampleRate/windowSize)).intValue())/2;
							}
						} 
						
					} 
					
					// Remove features from onset or between onset windows
					else {
						features.get(j).getValues().remove(k);
						features.get(j).getWindows().remove(k);
						k--;
					}
			    }
			}
		} catch(IOException e) {
			throw new NodeException("Problem occured during feature reduction: " + e.getMessage());
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...reduction succeeded");
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

}
