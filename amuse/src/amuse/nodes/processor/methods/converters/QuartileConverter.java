/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2022 by code authors
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
 * Calculates quartile boundaries for the given feature matrix
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class QuartileConverter extends AmuseTask implements MatrixToVectorConverterInterface {

	/** Save the min values? */
	private boolean saveMinValues = false;
	
	/** Save the 1st quartile values? */
	private boolean save1stQuartileValues = false;

	/** Save the median values? */
	private boolean saveMedianValues = false;

	/** Save the 3rd quartile values? */
	private boolean save3rdQuartileValues = false;

	/** Save the max values? */
	private boolean saveMaxValues = false;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString == null || parameterString == "") {
			saveMinValues = true;
			save1stQuartileValues = true;
			saveMedianValues = true;
			save3rdQuartileValues = true;
			saveMaxValues = true;
		} else {
			StringTokenizer tok = new StringTokenizer(parameterString,"_");
			if(tok.nextToken().equals(new String("true"))) {
				saveMinValues = true;
			}
			if(tok.nextToken().equals(new String("true"))) {
				save1stQuartileValues = true;
			}
			if(tok.nextToken().equals(new String("true"))) {
				saveMedianValues = true;
			}
			if(tok.nextToken().equals(new String("true"))) {
				save3rdQuartileValues = true;
			}
			if(tok.nextToken().equals(new String("true"))) {
				saveMaxValues = true;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface#runConversion(java.util.ArrayList, java.lang.Integer, java.lang.Integer, java.lang.String, long)
	 */
	public ArrayList<Feature> runConversion(ArrayList<Feature> features, Integer aggregationWindowSize, Integer stepSize, String nameOfProcessorModel, Unit unit) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the quartile conversion...");
		
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalStepSize();
		
		// How many quartiles will be stored?
		int numberOfQuartileDimensions = ((saveMinValues ? 1 : 0) + (save1stQuartileValues ? 1 : 0) + (saveMedianValues ? 1 : 0) + 
				(save3rdQuartileValues ? 1 : 0) + (saveMaxValues ? 1 : 0));
				
		// Single features used as classifier input vector
		ArrayList<Feature> endFeatures = new ArrayList<Feature>();
		
		try {

			// Go through music features
			for(int i=0;i<features.size();i++) {
				int sampleRate = features.get(i).getSampleRate();
				int numberOfAllSingleFeatures = features.get(i).getValues().get(0).length;
				
				ArrayList<Feature> newFeatures = new ArrayList<Feature>(numberOfAllSingleFeatures * numberOfQuartileDimensions);
				for(int j=0;j<numberOfAllSingleFeatures;j++) {
					if(saveMinValues) {
						Feature minOfCurrentSingleFeature = new Feature(-1);
						minOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
						minOfCurrentSingleFeature.getHistory().add("Min_" + (j+1));
						minOfCurrentSingleFeature.setSampleRate(sampleRate);
						newFeatures.add(minOfCurrentSingleFeature);
					} 
					if(save1stQuartileValues) {
						Feature firstQBoundOfCurrentSingleFeature = new Feature(-1);
						firstQBoundOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
						firstQBoundOfCurrentSingleFeature.getHistory().add("1st_quartile_bound" + (j+1));
						firstQBoundOfCurrentSingleFeature.setSampleRate(sampleRate);
						newFeatures.add(firstQBoundOfCurrentSingleFeature);
					}
					if(saveMedianValues) {
						Feature secondQBoundOfCurrentSingleFeature = new Feature(-1);
						secondQBoundOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
						secondQBoundOfCurrentSingleFeature.getHistory().add("2nd_quartile_bound" + (j+1));
						secondQBoundOfCurrentSingleFeature.setSampleRate(sampleRate);
						newFeatures.add(secondQBoundOfCurrentSingleFeature);
					}
					if(save3rdQuartileValues) {
						Feature thirdQBoundOfCurrentSingleFeature = new Feature(-1);
						thirdQBoundOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
						thirdQBoundOfCurrentSingleFeature.getHistory().add("3rd_quartile_bound" + (j+1));
						thirdQBoundOfCurrentSingleFeature.setSampleRate(sampleRate);
						newFeatures.add(thirdQBoundOfCurrentSingleFeature);
					}
					if(saveMaxValues) {
						Feature maxOfCurrentSingleFeature = new Feature(-1);
						maxOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
						maxOfCurrentSingleFeature.getHistory().add("Max_" + (j+1));
						maxOfCurrentSingleFeature.setSampleRate(sampleRate);
						newFeatures.add(maxOfCurrentSingleFeature);
					}
				}
				
				double classificationWindowSizeInWindows;
				double overlapSizeInWindows;
				int numberOfAllClassificationWindows;
				
				// Aggregate the data over the complete track or build classification windows?
				if(aggregationWindowSize == -1) {
					
					// In 1st case we have only one "classificatoin window" which covers the complete track
					classificationWindowSizeInWindows = features.get(i).getWindows().get(features.get(i).getWindows().size()-1);
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
					
					// FIXME evtl. check! Calculates the last used time window and the number of maximum available classification windows from it
					double numberOfAllClassificationWindowsD = ((features.get(i).getWindows().get(features.get(i).getWindows().size()-1)) - classificationWindowSizeInWindows)/(classificationWindowSizeInWindows - overlapSizeInWindows)+1;
					numberOfAllClassificationWindows = new Double(Math.floor(numberOfAllClassificationWindowsD)).intValue();
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
						
						// Save the feature values for this classification window here for sorting
						ArrayList<Double> featureValuesForThisClassificationWindow = new ArrayList<Double>();
						for(Double l:windowsOfCurrentClassificationWindow) {
							featureValuesForThisClassificationWindow.add(features.get(i).getValuesFromWindow(l)[k]);
						}
						
						// Remove NaN-values for quartile calculation (it is also possible that for feature with
						// large source frames the last smaller frames are filled with NaN-values!)
						for(int z=0;z<featureValuesForThisClassificationWindow.size();z++) {
							if(featureValuesForThisClassificationWindow.get(z).isNaN()) {
								featureValuesForThisClassificationWindow.remove(z); z--;
							}
						}
						java.util.Collections.sort(featureValuesForThisClassificationWindow);
						
						Double[] minD = new Double[1]; 
						Double[] firstQD = new Double[1]; 
						Double[] secondQD = new Double[1]; 
						Double[] thirdQD = new Double[1]; 
						Double[] maxD = new Double[1];
						
						if(numberOfCurrentClassificationWindow < numberOfAllClassificationWindows) {
							if(featureValuesForThisClassificationWindow.size() > 0) {
								minD[0] = (Double)featureValuesForThisClassificationWindow.get(0);
								int indexOfFirstBoundary = new Double(featureValuesForThisClassificationWindow.size()*0.25).intValue();
								firstQD[0] = (Double)featureValuesForThisClassificationWindow.get(indexOfFirstBoundary);
								int indexOfSecondBoundary = new Double(featureValuesForThisClassificationWindow.size()*0.5).intValue();
								secondQD[0] = (Double)featureValuesForThisClassificationWindow.get(indexOfSecondBoundary);
								int indexOfThirdBoundary = new Double(featureValuesForThisClassificationWindow.size()*0.75).intValue();
								thirdQD[0] = (Double)featureValuesForThisClassificationWindow.get(indexOfThirdBoundary);
								maxD[0] = (Double)featureValuesForThisClassificationWindow.get(featureValuesForThisClassificationWindow.size()-1);
							} else { // If all feature values consist of NaN values
								minD[0] = Double.NaN;
								firstQD[0] = Double.NaN;
								secondQD[0] = Double.NaN;
								thirdQD[0] = Double.NaN;
								maxD[0] = Double.NaN;
							}
							
							// OLD: Always store all quartiles
							/*newFeatures.get(5*k).getValues().add(minD);
							newFeatures.get(5*k).getWindows().add(new Double(classificationWindowStart));
							newFeatures.get(5*k+1).getValues().add(firstQD);
							newFeatures.get(5*k+1).getWindows().add(new Double(classificationWindowStart));
							newFeatures.get(5*k+2).getValues().add(secondQD);
							newFeatures.get(5*k+2).getWindows().add(new Double(classificationWindowStart));
							newFeatures.get(5*k+3).getValues().add(thirdQD);
							newFeatures.get(5*k+3).getWindows().add(new Double(classificationWindowStart));
							newFeatures.get(5*k+4).getValues().add(maxD);
							newFeatures.get(5*k+4).getWindows().add(new Double(classificationWindowStart));*/
							if(saveMinValues) {
								newFeatures.get(numberOfQuartileDimensions*k).getValues().add(minD);
								newFeatures.get(numberOfQuartileDimensions*k).getWindows().add(new Double(classificationWindowStart));
							}
							if(save1stQuartileValues) {
								newFeatures.get(numberOfQuartileDimensions*k+(saveMinValues ? 1 : 0)).getValues().add(firstQD);
								newFeatures.get(numberOfQuartileDimensions*k+(saveMinValues ? 1 : 0)).getWindows().add(new Double(classificationWindowStart));
							}
							if(saveMedianValues) {
								newFeatures.get(numberOfQuartileDimensions*k+(saveMinValues ? 1 : 0)+(save1stQuartileValues ? 1 : 0)).getValues().add(secondQD);
								newFeatures.get(numberOfQuartileDimensions*k+(saveMinValues ? 1 : 0)+(save1stQuartileValues ? 1 : 0)).getWindows().add(new Double(classificationWindowStart));
							}
							if(save3rdQuartileValues) {
								newFeatures.get(numberOfQuartileDimensions*k+(saveMinValues ? 1 : 0)+(save1stQuartileValues ? 1 : 0)+(saveMedianValues ? 1 : 0)).getValues().add(thirdQD);
								newFeatures.get(numberOfQuartileDimensions*k+(saveMinValues ? 1 : 0)+(save1stQuartileValues ? 1 : 0)+(saveMedianValues ? 1 : 0)).getWindows().add(new Double(classificationWindowStart));
							}
							if(saveMaxValues) {
								newFeatures.get(numberOfQuartileDimensions*k+(saveMinValues ? 1 : 0)+(save1stQuartileValues ? 1 : 0)+(saveMedianValues ? 1 : 0)+(save3rdQuartileValues ? 1 : 0)).getValues().add(maxD);
								newFeatures.get(numberOfQuartileDimensions*k+(saveMinValues ? 1 : 0)+(save1stQuartileValues ? 1 : 0)+(saveMedianValues ? 1 : 0)+(save3rdQuartileValues ? 1 : 0)).getWindows().add(new Double(classificationWindowStart));
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

}
