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
 * Creation date: 14.05.2010
 */
package amuse.nodes.processor.methods.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Generates from each feature f_i up to five new features {f_i(a), f_i(ao), f_i(o), f_i(or), f_i(r)} responding to 
 * Attack/Onset/Release information:
 * f_i(a) saves only f_i values from the beginnings of the attack intervals
 * f_i(ao) from the middle of the attack interval
 * f_i(o) from the onset window
 * f_i(or) from the middle of the release interval
 * f_i(r) from the end of the release interval
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class AORSplitter extends AmuseTask implements DimensionProcessorInterface {

	/** For chord mixes only one onset must exist; in case several are found, select the frame with the highest RMS */
	private boolean saveOnlyOneEvent = false;
	
	/** If true, the features from the corresponding windows are processed */
	private boolean saveAWindows = false;
	private boolean saveAOWindows = false;
	private boolean saveOWindows = false;
	private boolean saveORWindows = false;
	private boolean saveRWindows = false;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		StringTokenizer tok = new StringTokenizer(parameterString,"_");
		if(tok.nextToken().equals(new String("one"))) {
			saveOnlyOneEvent = true;
		}
		if(tok.nextToken().equals(new String("true"))) {
			saveAWindows = true;
		}
		if(tok.nextToken().equals(new String("true"))) {
			saveAOWindows = true;
		}
		if(tok.nextToken().equals(new String("true"))) {
			saveOWindows = true;
		}
		if(tok.nextToken().equals(new String("true"))) {
			saveORWindows = true;
		}
		if(tok.nextToken().equals(new String("true"))) {
			saveRWindows = true;
		}
	}
	
	/**
	 * Perform onset reduction of the given feature files
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting AOR splitting...");
		
		Double[] onsetTimes = loadEventTimes("onset");
		Double[] attackBeginTimes = loadEventTimes("attack");
		Double[] releaseEndTimes = loadEventTimes("release");
		
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalStepSize();
		ArrayList<Feature> newFeatures = new ArrayList<Feature>();
			
	    // Go through features
		for(int j=0;j<features.size();j++) {
			int sampleRate = features.get(j).getSampleRate();

			Feature AWindowsFeature = null;
			Feature AOWindowsFeature = null;
			Feature OWindowsFeature = null;
			Feature ORWindowsFeature = null;
			Feature RWindowsFeature = null;
			   	
			if(this.saveAWindows) {
				AWindowsFeature = new Feature(features.get(j).getIds(),features.get(j).getDescription(),
						features.get(j).getDimension());
				AWindowsFeature.setHistory(features.get(j).getHistory());
				AWindowsFeature.getHistory().add(new String("Only_attack_start_windows"));
				AWindowsFeature.setSampleRate(sampleRate);
			}
			if(this.saveAOWindows) {
				AOWindowsFeature = new Feature(features.get(j).getIds(),features.get(j).getDescription(),
						features.get(j).getDimension());
				AOWindowsFeature.setHistory(features.get(j).getHistory());
				AOWindowsFeature.getHistory().add(new String("Only_attack_interval_middle_windows"));
				AOWindowsFeature.setSampleRate(sampleRate);
			}
			if(this.saveOWindows) {
				OWindowsFeature = new Feature(features.get(j).getIds(),features.get(j).getDescription(),
						features.get(j).getDimension());
				OWindowsFeature.setHistory(features.get(j).getHistory());
				OWindowsFeature.getHistory().add(new String("Only_onset_windows"));
				OWindowsFeature.setSampleRate(sampleRate);
			}
			if(this.saveORWindows) {
				ORWindowsFeature = new Feature(features.get(j).getIds(),features.get(j).getDescription(),
						features.get(j).getDimension());
				ORWindowsFeature.setHistory(features.get(j).getHistory());
				ORWindowsFeature.getHistory().add(new String("Only_release_interval_middle_windows"));
				ORWindowsFeature.setSampleRate(sampleRate);
			}
			if(this.saveRWindows) {
				RWindowsFeature = new Feature(features.get(j).getIds(),features.get(j).getDescription(),
						features.get(j).getDimension());
				RWindowsFeature.setHistory(features.get(j).getHistory());
				RWindowsFeature.getHistory().add(new String("Only_release_end_windows"));
				RWindowsFeature.setSampleRate(sampleRate);
			}

			// Go through all features values and save only the features from windows containing onset times
			int currentAttackStartTimeNumber = 0;
			int currentAttackMiddleTimeNumber = 0;
			int currentOnsetTimeNumber = 0;
			int currentReleaseMiddleTimeNumber = 0;
			int currentReleaseEndTimeNumber = 0;
			int windowOfCurrentAttackStart = new Double(Math.floor(attackBeginTimes[currentAttackStartTimeNumber]*sampleRate/windowSize)).intValue();
			int windowOfCurrentAttackMiddle = new Double(Math.floor( (onsetTimes[currentAttackMiddleTimeNumber]*sampleRate/windowSize + 
			   		attackBeginTimes[currentAttackMiddleTimeNumber]*sampleRate/windowSize))/2 ).intValue();
			int windowOfCurrentOnset = new Double(Math.floor(onsetTimes[currentOnsetTimeNumber]*sampleRate/windowSize)).intValue();
			int windowOfCurrentReleaseMiddle = new Double(Math.floor( (releaseEndTimes[currentReleaseMiddleTimeNumber]*sampleRate/windowSize + 
			   		onsetTimes[currentReleaseMiddleTimeNumber]*sampleRate/windowSize))/2 ).intValue();
			int windowOfCurrentReleaseEnd = new Double(Math.floor(releaseEndTimes[currentReleaseEndTimeNumber]*sampleRate/windowSize)).intValue();
			
			// Go through all time windows
			for(int k=0;k<features.get(j).getWindows().size();k++) {
				
				int currentWindow = features.get(j).getWindows().get(k).intValue()-1;
				
				// If any of the current time events belongs to the end frames of a track from which the current feature has not
				// been extracted (holds e.g. for delta MFCCs and occurs rather seldom! - the time event must be at the very end
				// of the music piece), an approximation is used: window of the current time event is set to the last window for 
				// which this feature has been extracted
				if(windowOfCurrentAttackStart > features.get(j).getWindows().size()-1) {
					windowOfCurrentAttackStart = features.get(j).getWindows().size()-1;
				}
				if(windowOfCurrentAttackMiddle > features.get(j).getWindows().size()-1) {
					windowOfCurrentAttackMiddle = features.get(j).getWindows().size()-1;
				}
				if(windowOfCurrentOnset > features.get(j).getWindows().size()-1) {
					windowOfCurrentOnset = features.get(j).getWindows().size()-1;
				}
				if(windowOfCurrentReleaseMiddle > features.get(j).getWindows().size()-1) {
					windowOfCurrentReleaseMiddle = features.get(j).getWindows().size()-1;
				}
				if(windowOfCurrentReleaseEnd > features.get(j).getWindows().size()-1) {
					windowOfCurrentReleaseEnd = features.get(j).getWindows().size()-1;
				}
						
				// Update attack start feature?
				if(windowOfCurrentAttackStart == currentWindow && this.saveAWindows) {
							
					// Go to the next attack start time. If the next attack start time corresponds to the
					// same time window, proceed further!
					while(currentAttackStartTimeNumber < attackBeginTimes.length-1) {
						currentAttackStartTimeNumber++;
						int windowOfNextAttackStart = new Double(Math.floor(attackBeginTimes[currentAttackStartTimeNumber]*sampleRate/windowSize)).intValue();
						if(windowOfCurrentAttackStart != windowOfNextAttackStart) {
							windowOfCurrentAttackStart = windowOfNextAttackStart;
							break;
						}
						windowOfCurrentAttackStart = new Double(Math.floor(attackBeginTimes[currentAttackStartTimeNumber]*sampleRate/windowSize)).intValue();
					} 
					AWindowsFeature.getWindows().add(features.get(j).getWindows().get(k));
					AWindowsFeature.getValues().add(features.get(j).getValues().get(k));
				} 
					
				// Update attack middle features?
				if(windowOfCurrentAttackMiddle == currentWindow && this.saveAOWindows) {
							
					// Go to the next attack middle time. If the next attack middle time corresponds to the
					// same time window, proceed further!
					while(currentAttackMiddleTimeNumber < attackBeginTimes.length-1) {
						currentAttackMiddleTimeNumber++;
						int windowOfNextAttackMiddle = new Double(Math.floor( (onsetTimes[currentAttackMiddleTimeNumber]*sampleRate/windowSize + 
					    		attackBeginTimes[currentAttackMiddleTimeNumber]*sampleRate/windowSize))/2 ).intValue();
						if(windowOfCurrentAttackMiddle != windowOfNextAttackMiddle) {
							windowOfCurrentAttackMiddle = windowOfNextAttackMiddle;
							break;
						}
						windowOfCurrentAttackMiddle = new Double(Math.floor( (onsetTimes[currentAttackMiddleTimeNumber]*sampleRate/windowSize + 
					    		attackBeginTimes[currentAttackMiddleTimeNumber]*sampleRate/windowSize))/2 ).intValue();
					} 
					
					AOWindowsFeature.getWindows().add(features.get(j).getWindows().get(k));
					AOWindowsFeature.getValues().add(features.get(j).getValues().get(k));
				} 
					
				// Update onset features?
				if(windowOfCurrentOnset == currentWindow && this.saveOWindows) {
							
					// Go to the next onset time. If the next onset time corresponds to the
					// same time window, proceed further!
					while(currentOnsetTimeNumber < onsetTimes.length-1) {
						currentOnsetTimeNumber++;
						int windowOfNextOnset = new Double(Math.floor(onsetTimes[currentOnsetTimeNumber]*sampleRate/windowSize)).intValue();
						if(windowOfCurrentOnset != windowOfNextOnset) {
							windowOfCurrentOnset = windowOfNextOnset;
							break;
						}
						windowOfCurrentOnset = new Double(Math.floor(onsetTimes[currentOnsetTimeNumber]*sampleRate/windowSize)).intValue();
					} 
					
					OWindowsFeature.getWindows().add(features.get(j).getWindows().get(k));
					OWindowsFeature.getValues().add(features.get(j).getValues().get(k));
				} 
					
				// Update release middle features?
				if(windowOfCurrentReleaseMiddle == currentWindow && this.saveORWindows) {
						
					// Go to the next release middle time. If the next release middle time corresponds to the
					// same time window, proceed further!
					while(currentReleaseMiddleTimeNumber < releaseEndTimes.length-1) {
						currentReleaseMiddleTimeNumber++;
						int windowOfNextReleaseMiddle = new Double(Math.floor( (releaseEndTimes[currentReleaseMiddleTimeNumber]*sampleRate/windowSize + 
					    		onsetTimes[currentReleaseMiddleTimeNumber]*sampleRate/windowSize))/2 ).intValue();
						if(windowOfCurrentReleaseMiddle != windowOfNextReleaseMiddle) {
							windowOfCurrentReleaseMiddle = windowOfNextReleaseMiddle;
							break;
						}
						windowOfCurrentReleaseMiddle = new Double(Math.floor( (releaseEndTimes[currentReleaseMiddleTimeNumber]*sampleRate/windowSize + 
					    		onsetTimes[currentReleaseMiddleTimeNumber]*sampleRate/windowSize))/2 ).intValue();
					} 
					
					ORWindowsFeature.getWindows().add(features.get(j).getWindows().get(k));
					ORWindowsFeature.getValues().add(features.get(j).getValues().get(k));
				} 
					
				// Update release end features?
				if(windowOfCurrentReleaseEnd == currentWindow && this.saveRWindows) {
							
					// Go to the next release end time. If the next release end time corresponds to the
					// same time window, proceed further!
					while(currentReleaseEndTimeNumber < releaseEndTimes.length-1) {
						currentReleaseEndTimeNumber++;
						int windowOfNextReleaseEnd = new Double(Math.floor(releaseEndTimes[currentReleaseEndTimeNumber]*sampleRate/windowSize)).intValue();
						if(windowOfCurrentReleaseEnd != windowOfNextReleaseEnd) {
							windowOfCurrentReleaseEnd = windowOfNextReleaseEnd;
							break;
						}
						windowOfCurrentReleaseEnd = new Double(Math.floor(releaseEndTimes[currentReleaseEndTimeNumber]*sampleRate/windowSize)).intValue();
					} 
						
					RWindowsFeature.getWindows().add(features.get(j).getWindows().get(k));
					RWindowsFeature.getValues().add(features.get(j).getValues().get(k));
				} 
			}
				
			// Add new features
			if(saveAWindows) {
				newFeatures.add(AWindowsFeature);
			}
			if(saveAOWindows) {
				newFeatures.add(AOWindowsFeature);
			}
			if(saveOWindows) {
				newFeatures.add(OWindowsFeature);
			}
			if(saveORWindows) {
				newFeatures.add(ORWindowsFeature);
			}
			if(saveRWindows) {
				newFeatures.add(RWindowsFeature);
			}
		}
			
		// Replace old features by new
		features.clear();
		for(Feature f : newFeatures) {
			features.add(f);
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...splitting succeeded");
	}
	
	/**
	 * Loads the event times
	 * @param string Event description (onset, attack or release)
	 * @return Double array with time values in ms
	 */
	private Double[] loadEventTimes(String string) throws NodeException {
		Double[] eventTimes = null;
		
		String idPostfix = null;
		if(string.equals(new String("onset"))) {
			idPostfix = new String("_419.arff");
		} else if(string.equals(new String("attack"))) {
			idPostfix = new String("_423.arff");
		} if(string.equals(new String("release"))) {
			idPostfix = new String("_424.arff");
		} 
		
		try {
			
			// Load the attack, onset or release times, using the file name of the first feature
			// for finding the path to feature files (ID = 419, 423 and 424)
			String currentTimeEventFile = ((ProcessingConfiguration)this.correspondingScheduler.getConfiguration()).getMusicFileList().getFileAt(0);
				
			// Calculate the path to file with time events
			String relativeName = new String();
			if(currentTimeEventFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
				relativeName = currentTimeEventFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length());
			} else {
				relativeName = currentTimeEventFile;
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
			
			// Should only one event be used? (e.g., for analysis of chords with only one onset)
			if(saveOnlyOneEvent) {
				if(eventTimesSet.getValueCount() != 1) {
					return loadEventTimesBasedOnRMS(string);
				}
			}
			
			
			eventTimes = new Double[eventTimesSet.getValueCount()];
			for(int i=0;i<eventTimes.length;i++) {
				if(string.equals(new String("onset"))) {
					eventTimes[i] = new Double(eventTimesSet.getAttribute("Onset times").getValueAt(i).toString());
				} else if(string.equals(new String("attack"))) {
					eventTimes[i] = new Double(eventTimesSet.getAttribute("Start points of attack intervals").getValueAt(i).toString());
				} if(string.equals(new String("release"))) {
					eventTimes[i] = new Double(eventTimesSet.getAttribute("End points of release intervals").getValueAt(i).toString());
				} 
			}
		} catch(Exception e) {
			throw new NodeException("Could not load the time events: " + e.getMessage());
		}
		return eventTimes;
	}

	private Double[] loadEventTimesBasedOnRMS(String string) throws NodeException {
		Double[] eventTimes = null;
		
		String idPostfix = new String("_4.arff");

		try {
			
			// Load the RMS using the file name of the first feature
			String currentRMSFile = ((ProcessingConfiguration)this.correspondingScheduler.getConfiguration()).getMusicFileList().getFileAt(0);
				
			// Calculate the path to onset file
			String relativeName = new String();
			if(currentRMSFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
				relativeName = currentRMSFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length());
			} else {
				relativeName = currentRMSFile;
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
			
			DataSetAbstract rmsSet = new ArffDataSet(new File(relativeName));
			
			// TODO vINTERNAL Hack for instrument detection
			int windowOfRmsMax = 0;
			double currentMax = 0d;
			for(int i=0;i<rmsSet.getValueCount();i++) {
				double r = new Double(rmsSet.getAttribute("Root mean square").getValueAt(i).toString());
				if(r > currentMax) {
					windowOfRmsMax = i+1;
					currentMax = r;
				}
			}
			double approxOnsetMs = (new Double(windowOfRmsMax)-1d)*23.2199546485 + 11.6099773243;
			
			int windowOfReleaseEnd = rmsSet.getValueCount();
			for(int i=windowOfRmsMax;i<rmsSet.getValueCount();i++) {
				double r = new Double(rmsSet.getAttribute("Root mean square").getValueAt(i).toString());
				if(r == 0) {
					windowOfReleaseEnd = i+1;
					break;
				}
			}
			double approxReleaseEndMs = (new Double(windowOfReleaseEnd)-1d)*23.2199546485 + 11.6099773243;
						
			// TODO vINTERNAL Hack for instrument detection
			eventTimes = new Double[1];
			if(string.equals(new String("onset"))) {
				eventTimes[0] = new Double(approxOnsetMs/1000d); // Convert to seconds
			} else if(string.equals(new String("attack"))) {
				eventTimes[0] = new Double(0);
			} if(string.equals(new String("release"))) {
				eventTimes[0] = new Double(approxReleaseEndMs/1000d); // Convert to seconds
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
