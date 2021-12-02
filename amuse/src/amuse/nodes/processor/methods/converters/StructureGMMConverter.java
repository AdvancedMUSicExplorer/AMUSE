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
 * Creation date: 11.01.2009
 */
package amuse.nodes.processor.methods.converters;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Level;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;

import amuse.data.Feature;
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
 * Performs GMM conversion of the given feature matrix
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class StructureGMMConverter extends AmuseTask implements MatrixToVectorConverterInterface {

	/** The number of classification windows which are saved from each segment */
	private int numberOfClassificationWindowsToSelect;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		this.numberOfClassificationWindowsToSelect = new Integer(parameterString);
	}
	
	public ArrayList<Feature> runConversion(ArrayList<Feature> features, Integer aggregationWindowSize, Integer stepSize, String nameOfProcessorModel, Unit unit) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the GMM conversion based on track structure information...");
		
		int sampleRate = features.get(0).getSampleRate();
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalStepSize();
		
		// TODO Fuer Metrik-Berechnung, auslagern!!
		ArrayList<Double> usedTimeWindows = new ArrayList<Double>();
		
		// Single features used as classifier input vector
		ArrayList<Feature> endFeatures = new ArrayList<Feature>();
		
		try {

			// Load the structure information for this file, using the file name of the first feature
			// for finding the path to onset times file (ID = 601)
			String currentStructureFile = ((ProcessingConfiguration)this.correspondingScheduler.getConfiguration()).getMusicFileList().getFileAt(0);
				
			// Calculate the path to track structure file
			String relativeName = new String();
			if(currentStructureFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
				relativeName = currentStructureFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length());
			} else {
				relativeName = currentStructureFile;
			}
			if(relativeName.charAt(0) == File.separatorChar) {
				relativeName = relativeName.substring(1);
			}
			relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
			if(relativeName.lastIndexOf(File.separator) != -1) {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
					relativeName.substring(relativeName.lastIndexOf(File.separator)) + "_601.arff";
			} else {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
						File.separator + relativeName + "_601.arff";
			}	
			
			ArffLoader structureArffLoader = new ArffLoader();
			structureArffLoader.setFile(new File(relativeName));
			Attribute segmentStartAttribute = structureArffLoader.getStructure().attribute("SegmentStart");
			Attribute segmentEndAttribute = structureArffLoader.getStructure().attribute("SegmentEnd");
			Instance segmentInstance = structureArffLoader.getNextInstance(structureArffLoader.getStructure());
			ArrayList<Double[]> segments = new ArrayList<Double[]>();
			while(segmentInstance != null) {
				Double[] currentSegment = new Double[2];
				currentSegment[0] = segmentInstance.value(segmentStartAttribute);
				currentSegment[1] = segmentInstance.value(segmentEndAttribute);
				segments.add(currentSegment);
				segmentInstance = structureArffLoader.getNextInstance(structureArffLoader.getStructure());
			}
			structureArffLoader.reset();
			int numberOfSegments = segments.size();
			
			// Transform the segment boundaries from seconds to time windows
			for(int i=0;i<segments.size();i++) {
				segments.get(i)[0] = segments.get(i)[0]*sampleRate / windowSize;
				segments.get(i)[1] = segments.get(i)[1]*sampleRate / windowSize;
			}
			
			// Go through music features
			for(int i=0;i<features.size();i++) {
				int numberOfAllSingleFeatures = features.get(i).getValues().get(0).length;
				
				ArrayList<Feature> newFeatures = new ArrayList<Feature>(numberOfAllSingleFeatures*2);
				for(int j=0;j<numberOfAllSingleFeatures;j++) {
					Feature meanOfCurrentSingleFeature = new Feature(-1);
					meanOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
					meanOfCurrentSingleFeature.getHistory().add("Mean_" + (j+1));
					Feature stdDevOfCurrentSingleFeature = new Feature(-1);
					stdDevOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
					stdDevOfCurrentSingleFeature.getHistory().add("Std_dev_" + (j+1));
					newFeatures.add(meanOfCurrentSingleFeature);
					newFeatures.add(stdDevOfCurrentSingleFeature);
				}
				
				double classificationWindowSizeInWindows;
				double overlapSizeInWindows;
				if(unit == Unit.SAMPLES) {
					classificationWindowSizeInWindows = aggregationWindowSize;
					overlapSizeInWindows = aggregationWindowSize - stepSize;
				} else {
					classificationWindowSizeInWindows = (Double)(sampleRate*(aggregationWindowSize/1000d)/windowSize);
					overlapSizeInWindows = (Double)(sampleRate*((aggregationWindowSize-stepSize)/1000d)/windowSize);
				}
				
				// FIXME evtl. check! Calculates the last used time window and the number of maximum available classificatoin windows from it
				double numberOfAllClassificationWindowsD = ((features.get(i).getWindows().get(features.get(i).getWindows().size()-1)) - classificationWindowSizeInWindows)/(classificationWindowSizeInWindows - overlapSizeInWindows)+1;
				int numberOfAllClassificationWindows = new Double(Math.floor(numberOfAllClassificationWindowsD)).intValue();
				
				// If the classification window size is greater than music track length..
				if(numberOfAllClassificationWindows == 0) {
					throw new NodeException("Classification window size too large");
				}
				
				int currentWindow = 0;
				
				// Go through all required classification windows
				for(int numberOfCurrentClassificationWindow=0;numberOfCurrentClassificationWindow<numberOfSegments * numberOfClassificationWindowsToSelect;
				  numberOfCurrentClassificationWindow++) {
					
					// Calculate the start (inclusive) and end (exclusive) windows for the current classification window
					int currentSegment = numberOfCurrentClassificationWindow / numberOfClassificationWindowsToSelect;
					int currentClassificationWindowInSegment = numberOfCurrentClassificationWindow % numberOfClassificationWindowsToSelect;
					int maxNumberOfClassificationWindowsInSegment = new Double(((segments.get(currentSegment)[1] - segments.get(currentSegment)[0]) - 
						classificationWindowSizeInWindows) / (classificationWindowSizeInWindows - overlapSizeInWindows)).intValue();
					
					// If the current segment is small and does not have enough classification windows...
					if(currentClassificationWindowInSegment >= maxNumberOfClassificationWindowsInSegment) continue;
					
					int numberOfPossibleClassificationWindowsToSelect;
					if(numberOfClassificationWindowsToSelect > maxNumberOfClassificationWindowsInSegment) {
						numberOfPossibleClassificationWindowsToSelect = maxNumberOfClassificationWindowsInSegment;
					} else {
						numberOfPossibleClassificationWindowsToSelect = numberOfClassificationWindowsToSelect;
					}
					
					int midWindowOfCurrentSegment = new Double( (segments.get(currentSegment)[1] + segments.get(currentSegment)[0])/2 ).intValue(); 
					Double classificationWindowStart = (2d*new Double(midWindowOfCurrentSegment) - classificationWindowSizeInWindows -
							(numberOfPossibleClassificationWindowsToSelect - 1)*(classificationWindowSizeInWindows - overlapSizeInWindows)) / 2 + 
							currentClassificationWindowInSegment*(classificationWindowSizeInWindows - overlapSizeInWindows); 
					Double classificaitonWindowEnd = classificationWindowStart + classificationWindowSizeInWindows;
					
					// Increment the number of current time window if the lower classification window boundary is not achieved
					for(int k=currentWindow;k<features.get(i).getWindows().size();k++) {
						if(features.get(i).getWindows().get(k) >= classificationWindowStart) {
							currentWindow = k;
							break;
						}
					}
					
					// If no features are available for the current classification window, go to the next classification window
					if(features.get(i).getWindows().get(currentWindow) > classificaitonWindowEnd) {
						continue;
					}
					
					// Create a list with time windows which are in the current classification window
					ArrayList<Double> windowsOfCurrentClassificationWindow = new ArrayList<Double>();
					while(features.get(i).getWindows().get(currentWindow) >= classificationWindowStart && 
							features.get(i).getWindows().get(currentWindow) < classificaitonWindowEnd) {
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
					
					// Update the list with all used time windows for a measure
					// TODO Evtl. auslagern
					if(i == 0) {
						for(int k=0;k<windowsOfCurrentClassificationWindow.size();k++) {
							if(!usedTimeWindows.contains(windowsOfCurrentClassificationWindow.get(k))) {
								usedTimeWindows.add(windowsOfCurrentClassificationWindow.get(k));
							}
						}
					}
					
					// Calculate mean and standard deviation for feature dimensions
					// Go through all feature dimensions
					for(int k=0;k<numberOfAllSingleFeatures;k++) {
						
						// Calculate mean and variance
						Double mean = 0d;
						Double variance = 0d;
						for(Double l:windowsOfCurrentClassificationWindow) {
							mean += features.get(i).getValuesFromWindow(l)[k];
						}
						mean /= windowsOfCurrentClassificationWindow.size();
						for(Double l:windowsOfCurrentClassificationWindow) {
							variance += Math.pow((Double)features.get(i).getValuesFromWindow(l)[k]-mean,2);
						}
						variance /= windowsOfCurrentClassificationWindow.size();
								
						// Add mean and deviation to the new generated features
						Double[] meanD = new Double[1]; meanD[0] = mean;
						Double[] stddevD = new Double[1]; stddevD[0] = variance;
						newFeatures.get(2*k).getValues().add(meanD);
						newFeatures.get(2*k).getWindows().add(classificationWindowStart);
						newFeatures.get(2*k+1).getValues().add(stddevD);
						newFeatures.get(2*k+1).getWindows().add(classificationWindowStart);
						
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
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "error: ");
			e.printStackTrace();
			throw new NodeException("Problem occured during feature conversion: " + e.getMessage());
		}
		
		// For the number of used time windows
		// FIXME 1 Auslagern!
		((ProcessorNodeScheduler)this.correspondingScheduler).setFinalWindows(usedTimeWindows,features);
				
		
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
