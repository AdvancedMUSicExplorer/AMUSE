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
 * Creation date: 28.11.2011
 */
package amuse.nodes.processor.methods.converters;

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
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Performs GMM conversion of the given feature matrix for classification windows spanned between attack interval start events and 
 * release interval end events.
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class AdaptiveOnsetGMM1Converter extends AmuseTask implements MatrixToVectorConverterInterface {

	/** Save the mean values of GMMS? */
	private boolean saveMeanValues = false;
	
	/** Save the standard deviations of GMMs? */
	private boolean saveStddevValues = false;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString == null || parameterString == "") {
			saveMeanValues = true;
			saveStddevValues = true;
		} else {
			StringTokenizer tok = new StringTokenizer(parameterString,"_");
			if(tok.nextToken().equals(new String("true"))) {
				saveMeanValues = true;
			}
			if(tok.nextToken().equals(new String("true"))) {
				saveStddevValues = true;
			}
		}
	}
	
	public ArrayList<Feature> runConversion(ArrayList<Feature> features, Integer aggregationWindowSize, Integer stepSize, String nameOfProcessorModel, Unit unit) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the GMM conversion...");
		
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalStepSize();
		
		// Single features used as classifier input vector
		ArrayList<Feature> endFeatures = new ArrayList<Feature>();
		
		try {
			// Load the attack start events and release end events
			Double[] attackStarts = loadEventTimes("attack");
			Double[] releaseEnds = loadEventTimes("release");
			
			// If no events were found..
			if(attackStarts.length == 0 || releaseEnds.length == 0) {
				throw new NodeException("Attack and/or release interval number is equal to zero!");
			} 
			// .. or the number of both events is not equal
			else if(attackStarts.length != releaseEnds.length) {
				throw new NodeException("Attack and release interval number must be the same!");
			}

			// Go through music features
			for(int i=0;i<features.size();i++) {
				int sampleRate = features.get(i).getSampleRate();
				int numberOfAllSingleFeatures = features.get(i).getValues().get(0).length;
				
				ArrayList<Feature> newFeatures = new ArrayList<Feature>(numberOfAllSingleFeatures * 
						((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0)));
				for(int j=0;j<numberOfAllSingleFeatures;j++) {
					if(saveMeanValues) {
						Feature meanOfCurrentSingleFeature = new Feature(-1);
						meanOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
						meanOfCurrentSingleFeature.getHistory().add("Mean_" + (j+1));
						meanOfCurrentSingleFeature.setSampleRate(sampleRate);
						newFeatures.add(meanOfCurrentSingleFeature);
					}
					if(saveStddevValues) {
						Feature stdDevOfCurrentSingleFeature = new Feature(-1);
						stdDevOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
						stdDevOfCurrentSingleFeature.getHistory().add("Std_dev_" + (j+1));
						stdDevOfCurrentSingleFeature.setSampleRate(sampleRate);
						newFeatures.add(stdDevOfCurrentSingleFeature);
					}
				}
				
				
			    int currentWindow = 0;
				
				// Go through all onset classificatoin windows
				for(int numberOfCurrentClassificationWindow=0;numberOfCurrentClassificationWindow<attackStarts.length;numberOfCurrentClassificationWindow++) {
					
					// Calculate the start and end windows for the current onset classification window
					Double classificationWindowStart = Math.ceil((attackStarts[numberOfCurrentClassificationWindow] * (double)features.get(i).getSampleRate()) / windowSize); 
					Double classificationWindowEnd = Math.ceil((releaseEnds[numberOfCurrentClassificationWindow] * (double)features.get(i).getSampleRate()) / windowSize) + 1;
					
					// Increment the number of current time window if the lower classification window boundary is not achieved
					for(int k=currentWindow;k<features.get(i).getWindows().size();k++) {
						if(features.get(i).getWindows().get(k) >= classificationWindowStart) {
							currentWindow = k;
							break;
						}
					}
					
					// If no features are available for the current classification window, go to the next classification window
					if(features.get(i).getWindows().get(currentWindow) > classificationWindowEnd) {
						continue;
					}
					
					// Create a list with time windows which are in the current classification window
					ArrayList<Double> windowsOfCurrentClassificationWindow = new ArrayList<Double>();
					while(features.get(i).getWindows().get(currentWindow) >= classificationWindowStart && 
							features.get(i).getWindows().get(currentWindow) < classificationWindowEnd) {
						windowsOfCurrentClassificationWindow.add(features.get(i).getWindows().get(currentWindow));
						
						// The last existing window is achieved
						if(currentWindow == features.get(i).getWindows().size() - 1) {
							break;
						}
						currentWindow++;
					}
					
					// Check if the current classification window has any windows
					if(windowsOfCurrentClassificationWindow.size() == 0) {
						continue;
					}
					
					// Calculate mean and standard deviation for feature dimensions
					// Go through all feature dimensions
					for(int k=0;k<numberOfAllSingleFeatures;k++) {
						
						// Number of values (NaN values are omitted!)
						int valueNumber = 0;
						
						// Calculate mean and variance
						Double mean = 0d;
						Double variance = 0d;
						for(Double l:windowsOfCurrentClassificationWindow) {
							if(!Double.isNaN(features.get(i).getValuesFromWindow(l)[k])) {
								mean += features.get(i).getValuesFromWindow(l)[k];
								valueNumber++;
							}
						}
						mean /= valueNumber;
						for(Double l:windowsOfCurrentClassificationWindow) {
							if(!Double.isNaN(features.get(i).getValuesFromWindow(l)[k])) {
								variance += Math.pow((Double)features.get(i).getValuesFromWindow(l)[k]-mean,2);
							}
						}
						variance /= valueNumber;
								
						// Add mean and deviation to the new generated features
						Double[] meanD = new Double[1]; meanD[0] = mean;
						Double[] stddevD = new Double[1]; stddevD[0] = variance;
						if(saveMeanValues) {
							newFeatures.get(((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0))*k).getValues().add(meanD);
							newFeatures.get(((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0))*k).getWindows().add(new Double(classificationWindowStart));
						}
						if(saveStddevValues) {
							newFeatures.get(((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0))*k+(saveMeanValues ? 1 : 0)).getValues().add(stddevD);
							newFeatures.get(((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0))*k+(saveMeanValues ? 1 : 0)).getWindows().add(new Double(classificationWindowStart));
						}
					}
					
					// Go one window back (for the case that release end frame is the same as the next attack start frame
					currentWindow--;
				}

				for(int m=0;m<newFeatures.size();m++) {
					endFeatures.add(newFeatures.get(m));
				}
			}
		} catch(Exception e) {
			throw new NodeException("Problem occured during feature conversion: " + e.getMessage());
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...conversion succeeded");
		return endFeatures;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

	/**
	 * Loads the event times TODO this function exists also in AORSplitter and ProcessorNodeScheduler and should be removed sometime!
	 * @param string Event description (onset, attack or release)
	 * @return Double array with time values in s
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
			
			// TODO vINTERNAL Current implementation for the cases if MIR Toolbox extracts none or more than one onset for instrument tones
			/*if(eventTimesSet.getValueCount() != 1) {
				return loadEventTimesBasedOnRMS(string);
			}*/
			
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
			throw new NodeException("Could not load the time events (can be extracted with MIRToolbox-Plugin): " + e.getMessage());
		}
		return eventTimes;
	}


}
