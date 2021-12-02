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
package amuse.nodes.processor.methods.converters;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface;
import amuse.util.AmuseLogger;

/**
 * Performs GMM conversion of the given feature matrix
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class GMMConverter extends AmuseTask implements MatrixToVectorConverterInterface {

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
				
				double classificationWindowSizeInWindows;
				double overlapSizeInWindows;
				int numberOfAllClassificationWindows;
				
				// Aggregate the data over the complete track or build classfication windows?
				if(aggregationWindowSize == -1) {
					
					// In 1st case we have only one "classification window" which covers the complete track
					// ("+ 1" is used because of the exclusive calculation of the classification window end window)
					classificationWindowSizeInWindows = features.get(i).getWindows().get(features.get(i).getWindows().size()-1) + 1;
					overlapSizeInWindows = classificationWindowSizeInWindows;
					numberOfAllClassificationWindows = 1;
				} else {
					
					// In 2nd case we can calculate the number of windows which belong to each classification window
					if(unit == Unit.SAMPLES) {
						classificationWindowSizeInWindows = aggregationWindowSize;
						overlapSizeInWindows = aggregationWindowSize - stepSize;
					} else {
						classificationWindowSizeInWindows = (Double)(sampleRate*(aggregationWindowSize/1000d)/windowSize);
						overlapSizeInWindows = (Double)(sampleRate*((aggregationWindowSize-stepSize)/1000d)/windowSize);
					}
					
					// Calculates the last used time window and the number of maximum available classification windows from it
					double numberOfAllClassificationWindowsD = ((features.get(i).getWindows().get(features.get(i).getWindows().size()-1)) - classificationWindowSizeInWindows)/(classificationWindowSizeInWindows - overlapSizeInWindows)+1;
					numberOfAllClassificationWindows = new Double(Math.ceil(numberOfAllClassificationWindowsD)).intValue();
				}
				
				// If the classification window size is greater than music track length..
				if(numberOfAllClassificationWindows == 0) {
					throw new NodeException("Classification window size too large");
				}
				
			    // TODO Consider only the first 6 minutes of a music track; should be a parameter?
				// FUNKTIONIERT NICHT MIT 30'' PRUNER!!! 
				/*if(numberOfAllClassificationWindows > 360000/overlap) {
					//numberOfAllClassificationWindows = 360000/overlap;
				}*/
				
				int currentWindow = 0;
				
				// Go through all classification windows
				for(int numberOfCurrentClassificationWindow=0;numberOfCurrentClassificationWindow<numberOfAllClassificationWindows;numberOfCurrentClassificationWindow++) {
					
					// Calculate the start (inclusive) and end (exclusive) windows for the current classification window
					Double classificationWindowStart = Math.floor(new Double(classificationWindowSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentClassificationWindow));
					Double classificationWindowEnd = Math.ceil((new Double(classificationWindowSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentClassificationWindow)+classificationWindowSizeInWindows));
					
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
						if(numberOfCurrentClassificationWindow < numberOfAllClassificationWindows) {
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
						
						// Go with the current window back because of overlap (some time windows used in the
						// current classification window may be also used in the next classification window)
						while(features.get(i).getWindows().get(currentWindow) >= classificationWindowStart + (classificationWindowSizeInWindows - overlapSizeInWindows) && currentWindow > 0) {
							currentWindow--;
						}
					}
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



}
