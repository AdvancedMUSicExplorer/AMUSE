/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2012 by code authors
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
 * Creation date: 11.06.2012
 */
package amuse.nodes.processor.methods.converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface;
import amuse.util.AmuseLogger;

/**
 * Estimates mean and deviation for structural complexity method as introduced in: Mauch, M. and Levy, M. Structural
 * Change on Multiple Time Scales As a Correlate of Musical Complexity. Proceedings of ISMIR 2011.
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class StructuralComplexityConverter extends AmuseTask implements MatrixToVectorConverterInterface {

	/** The time scales from the paper (seconds before and after the current window to analyze)*/
	private ArrayList<Integer> timeScales;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		timeScales = new ArrayList<Integer>();
		if(parameterString == null || parameterString == "") {
			timeScales.add(1);
			timeScales.add(2);
			timeScales.add(4);
		} else {
			StringTokenizer tok = new StringTokenizer(parameterString,"_");
			while(tok.hasMoreTokens()) {
				timeScales.add(new Integer(tok.nextToken()));
			}
		}
	}
	
	public ArrayList<Feature> runConversion(ArrayList<Feature> features, Integer aggregationWindowSize, Integer stepSize, String nameOfProcessorModel, Unit unit) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the structural complexity conversion...");
		
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalStepSize();
		
		// Single features used as classifier input vector
		ArrayList<Feature> endFeatures = new ArrayList<Feature>();
		
		ArrayList<String> history = new ArrayList<String>(1);
		history.add(((ProcessingConfiguration)this.correspondingScheduler.getConfiguration()).getFeatureDescription());
		
		try {

			int sampleRate = features.get(0).getSampleRate();
				
			// Estimated complexity statistics are the quartile boundaries, mean and standard deviation
			ArrayList<Feature> newFeatures = new ArrayList<Feature>(timeScales.size() * 7);
			for(int j=0;j<timeScales.size();j++) {
				Feature minOfCurrentScale = new Feature(-1);
				minOfCurrentScale.setHistory(history);
				minOfCurrentScale.getHistory().add("Structural_Complexity_Min_" + timeScales.get(j));
				minOfCurrentScale.setSampleRate(sampleRate);
				newFeatures.add(minOfCurrentScale);
				
				Feature firstQBoundOfCurrentScale = new Feature(-1);
				firstQBoundOfCurrentScale.setHistory(history);
				firstQBoundOfCurrentScale.getHistory().add("Structural_Complexity_1st_Quartile_Bound_" + timeScales.get(j));
				firstQBoundOfCurrentScale.setSampleRate(sampleRate);
				newFeatures.add(firstQBoundOfCurrentScale);

				Feature medianOfCurrentScale = new Feature(-1);
				medianOfCurrentScale.setHistory(history);
				medianOfCurrentScale.getHistory().add("Structural_Complexity_Median_" + timeScales.get(j));
				medianOfCurrentScale.setSampleRate(sampleRate);
				newFeatures.add(medianOfCurrentScale);
				
				Feature thirdQBoundOfCurrentScale = new Feature(-1);
				thirdQBoundOfCurrentScale.setHistory(history);
				thirdQBoundOfCurrentScale.getHistory().add("Structural_Complexity_3rd_Quartile_Bound_" + timeScales.get(j));
				thirdQBoundOfCurrentScale.setSampleRate(sampleRate);
				newFeatures.add(thirdQBoundOfCurrentScale);
				
				Feature maxOfCurrentScale = new Feature(-1);
				maxOfCurrentScale.setHistory(history);
				maxOfCurrentScale.getHistory().add("Structural_Complexity_Max_" + timeScales.get(j));
				maxOfCurrentScale.setSampleRate(sampleRate);
				newFeatures.add(maxOfCurrentScale);
								
				Feature meanOfCurrentScale = new Feature(-1);
				meanOfCurrentScale.setHistory(history);
				meanOfCurrentScale.getHistory().add("Structural_Complexity_Mean_" + timeScales.get(j));
				meanOfCurrentScale.setSampleRate(sampleRate);
				newFeatures.add(meanOfCurrentScale);
				
				Feature stdDevOfCurrentSingleFeature = new Feature(-1);
				stdDevOfCurrentSingleFeature.setHistory(history);
				stdDevOfCurrentSingleFeature.getHistory().add("Structural_Complexity_Stddev_" + timeScales.get(j));
				stdDevOfCurrentSingleFeature.setSampleRate(sampleRate);
				newFeatures.add(stdDevOfCurrentSingleFeature);
			}
			
			double classificationWindowSizeInWindows;
			double overlapSizeInWindows;
			int numberOfAllClassificationWindows;
				
			// Aggregate the data over the complete track or build classification windows?
			if(aggregationWindowSize == -1) {
					
				// In 1st case we have only one "classification window" which covers the complete track
				// ("+ 1" is used because of the exclusive calculation of the classification window end window)
				classificationWindowSizeInWindows = features.get(0).getWindows().get(features.get(0).getWindows().size()-1) + 1;
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
				double numberOfAllClassificationWindowsD = ((features.get(0).getWindows().get(features.get(0).getWindows().size()-1)) - classificationWindowSizeInWindows)/(classificationWindowSizeInWindows - overlapSizeInWindows)+1;
				// Round down since the complete classification windows are required!
				numberOfAllClassificationWindows = new Double(Math.floor(numberOfAllClassificationWindowsD)).intValue();
			}
				
			// If the classification window size is greater than music track length..
			if(numberOfAllClassificationWindows == 0) {
				throw new NodeException("Classification window size too large");
			}
				
		    // Go through all classification windows
			for(int numberOfCurrentClassificationWindow=0;numberOfCurrentClassificationWindow<numberOfAllClassificationWindows;numberOfCurrentClassificationWindow++) {
				int currentWindow = 0;
				
				// Calculate the start (inclusive) and end (exclusive) windows for the current classification window
				Double classificationWindowStartWindow = Math.floor(new Double(classificationWindowSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentClassificationWindow));
				Double classificaotinWindowEndWindow = Math.ceil((new Double(classificationWindowSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentClassificationWindow)+classificationWindowSizeInWindows));
				
				// Increment the number of current time window if the lower classification window boundary is not achieved
				for(int k=currentWindow;k<features.get(0).getWindows().size();k++) {
					if(features.get(0).getWindows().get(k) >= classificationWindowStartWindow) {
						currentWindow = k;
						break;
					}
				}
					
				// If no features are available for the current classification window, go to the next classification window
				if(features.get(0).getWindows().get(currentWindow) > classificaotinWindowEndWindow) {
					continue;
				}
					
				// Create a list with time windows which are in the current classification window
				ArrayList<Double> windowsOfCurrentClassificationWindow = new ArrayList<Double>();
				while(features.get(0).getWindows().get(currentWindow) >= classificationWindowStartWindow && 
						features.get(0).getWindows().get(currentWindow) < classificaotinWindowEndWindow) {
					windowsOfCurrentClassificationWindow.add(features.get(0).getWindows().get(currentWindow));
					
					// The last existing window is achieved
					if(currentWindow == features.get(0).getWindows().size() - 1) {
						break;
					}
					currentWindow++;
				}
					
				// Check if the current classification window has any windows
				if(windowsOfCurrentClassificationWindow.size() == 0) {
					continue;
				}
					
				// Go through different time scales
				for(int s=0;s<timeScales.size();s++) {
					ArrayList<Double> distances = new ArrayList<Double>();
					
					// Estimate the number of windows for the number of seconds from the current time scale
					double seconds = timeScales.get(s);
					int windowNumber = new Double(sampleRate * seconds / (double)windowSize).intValue();
						
					// Go through all windows of the current classification window
					for(Double l:windowsOfCurrentClassificationWindow) {
							
						// Complexity can be only calculated if w_j windows before and after the current window
						// belong to this classification window (see the paper)
						if(l - windowNumber < classificationWindowStartWindow+1 || l + windowNumber >= classificaotinWindowEndWindow) {
							continue;
						} else {
							ArrayList<Double> s1 = new ArrayList<Double>();
							ArrayList<Double> s2 = new ArrayList<Double>();
							for(int i=0;i<features.size();i++) {
								for(int j=0;j<features.get(i).getDimension();j++) {
										
									// Summary of the current dimension of the current feature for the windows before window l
									double sum1 = 0d;
									int numberOfValues1 = 0;
									double sum2 = 0d;
									int numberOfValues2 = 0;
									for(int cWin=0;cWin<windowsOfCurrentClassificationWindow.size();cWin++) {
										if(!features.get(i).getValuesFromWindow(windowsOfCurrentClassificationWindow.get(cWin))[j].isNaN()) {
											if(windowsOfCurrentClassificationWindow.get(cWin) > l - windowNumber && windowsOfCurrentClassificationWindow.get(cWin) <= l) {
												sum1 += features.get(i).getValuesFromWindow(windowsOfCurrentClassificationWindow.get(cWin))[j];
												numberOfValues1++;
											} else if(windowsOfCurrentClassificationWindow.get(cWin) > l && windowsOfCurrentClassificationWindow.get(cWin) <= l + windowNumber) {
												sum2 += features.get(i).getValuesFromWindow(windowsOfCurrentClassificationWindow.get(cWin))[j];
												numberOfValues2++;
											} else if(windowsOfCurrentClassificationWindow.get(cWin) > l + windowNumber) {
												break;
											}
										}
									}
										
									// Estimate the means (summaries)
									s1.add(sum1 / (double)numberOfValues1);
									s2.add(sum2 / (double)numberOfValues2);
								}
							}
								
							double complexity = jensonShannonDivergence(s1,s2);
							distances.add(complexity);
						}
					}
						 
					// Add complexity mean and deviation to the new generated features
					if(numberOfCurrentClassificationWindow < numberOfAllClassificationWindows) {
							
						// Calculate different statistics for the complexity vector
						Collections.sort(distances);
						
						Double[] minD = new Double[1]; 
						Double[] firstQD = new Double[1]; 
						Double[] secondQD = new Double[1]; 
						Double[] thirdQD = new Double[1]; 
						Double[] maxD = new Double[1];
						Double[] meanD = new Double[1];
						Double[] stddevD = new Double[1];
							
						meanD[0] = 0d;
						stddevD[0] = 0d;
						int valuesNumber = 0;
						for(int d=0;d<distances.size();d++) {
							if(!distances.get(d).isNaN()) {
								meanD[0] += distances.get(d);
								valuesNumber++;
							}
						}
						meanD[0] /= valuesNumber;
							
						for(int d=0;d<distances.size();d++) {
							if(!distances.get(d).isNaN()) {
								stddevD[0] += Math.pow(distances.get(d)-meanD[0],2);
							}
						}
						stddevD[0] /= valuesNumber;
							
						if(numberOfCurrentClassificationWindow < numberOfAllClassificationWindows) {
							if(distances.size() > 0) {
								minD[0] = (Double)distances.get(0);
								int indexOfFirstBoundary = new Double(distances.size()*0.25).intValue();
								firstQD[0] = (Double)distances.get(indexOfFirstBoundary);
								int indexOfSecondBoundary = new Double(distances.size()*0.5).intValue();
								secondQD[0] = (Double)distances.get(indexOfSecondBoundary);
								int indexOfThirdBoundary = new Double(distances.size()*0.75).intValue();
								thirdQD[0] = (Double)distances.get(indexOfThirdBoundary);
								maxD[0] = (Double)distances.get(distances.size()-1);
							} else { // If all feature values consist of NaN values
								minD[0] = Double.NaN;
								firstQD[0] = Double.NaN;
								secondQD[0] = Double.NaN;
								thirdQD[0] = Double.NaN;
								maxD[0] = Double.NaN;
								meanD[0] = Double.NaN;
								stddevD[0] = Double.NaN;
							}
						}
							
						newFeatures.get(7*s).getValues().add(minD);
						newFeatures.get(7*s).getWindows().add(new Double(classificationWindowStartWindow));
						newFeatures.get(7*s+1).getValues().add(firstQD);
						newFeatures.get(7*s+1).getWindows().add(new Double(classificationWindowStartWindow));
						newFeatures.get(7*s+2).getValues().add(secondQD);
						newFeatures.get(7*s+2).getWindows().add(new Double(classificationWindowStartWindow));
						newFeatures.get(7*s+3).getValues().add(thirdQD);
						newFeatures.get(7*s+3).getWindows().add(new Double(classificationWindowStartWindow));
						newFeatures.get(7*s+4).getValues().add(maxD);
						newFeatures.get(7*s+4).getWindows().add(new Double(classificationWindowStartWindow));
						newFeatures.get(7*s+5).getValues().add(meanD);
						newFeatures.get(7*s+5).getWindows().add(new Double(classificationWindowStartWindow));
						newFeatures.get(7*s+6).getValues().add(stddevD);
						newFeatures.get(7*s+6).getWindows().add(new Double(classificationWindowStartWindow));
					}
						
				}
			}

			for(int m=0;m<newFeatures.size();m++) {
				endFeatures.add(newFeatures.get(m));
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new NodeException("Problem occured during feature conversion: " + e.getMessage());
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...conversion succeeded");
		return endFeatures;
	}
	
	private double jensonShannonDivergence(ArrayList<Double> s1, ArrayList<Double> s2) {
		ArrayList<Double> m = new ArrayList<Double>(s1.size());
		for(int i=0;i<s1.size();i++) {
			m.add((s1.get(i) + s2.get(i)) / 2);
		}
		return (kullbackLeiblerDivergence(s1,m) + kullbackLeiblerDivergence(s2,m)) / 2;
	}
	
	private double kullbackLeiblerDivergence(ArrayList<Double> s1, ArrayList<Double> s2) {
		double sum = 0d;
		for(int i=0;i<s1.size();i++) {
			if(s1.get(i) != 0 && s2.get(i) != 0) {
				sum += s1.get(i) * Math.log(s1.get(i) / s2.get(i));
			}
		}
		return sum;
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}



}
