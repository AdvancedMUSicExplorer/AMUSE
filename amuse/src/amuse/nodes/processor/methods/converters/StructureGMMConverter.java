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
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Performs GMM conversion of the given feature matrix
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class StructureGMMConverter extends AmuseTask implements MatrixToVectorConverterInterface {

	/** The number of partitions which are saved from each segment */
	private int numberOfPartitionsToSelect;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		this.numberOfPartitionsToSelect = new Integer(parameterString);
	}
	
	public ArrayList<Feature> runConversion(ArrayList<Feature> features, Integer ms, Integer overlap, String nameOfProcessorModel, long taskId) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the GMM conversion based on song structure information...");
		
		int sampleRate = features.get(0).getSampleRate();
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalFrameSize();
		
		// TODO Fuer Metrik-Berechnung, auslagern!!
		ArrayList<Double> usedTimeWindows = new ArrayList<Double>();
		
		// Single features used as classifier input vector
		ArrayList<Feature> endFeatures = new ArrayList<Feature>();
		
		try {

			// Load the structure information for this file, using the file name of the first feature
			// for finding the path to onset times file (ID = 601)
			String currentStructureFile = ((ProcessingConfiguration)this.correspondingScheduler.getConfiguration()).getMusicFileList().getFileAt(0);
				
			// Calculate the path to song structure file
			String relativeName = new String();
			if(currentStructureFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
				relativeName = currentStructureFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length()+1);
			} else {
				relativeName = currentStructureFile;
			}
			relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
			if(relativeName.lastIndexOf("/") != -1) {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + "/" + relativeName +  
					relativeName.substring(relativeName.lastIndexOf("/")) + "_601.arff";
			} else {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + "/" + relativeName +  
						"/" + relativeName + "_601.arff";
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
				
				double partitionSizeInWindows = (Double)(sampleRate*(ms/1000d)/windowSize);
				double overlapSizeInWindows = (Double)(sampleRate*((ms-overlap)/1000d)/windowSize);
				
				// FIXME evtl. check! Calculates the last used time window and the number of maximum available partitions from it
				double numberOfAllPartitionsD = ((features.get(i).getWindows().get(features.get(i).getWindows().size()-1)) - partitionSizeInWindows)/(partitionSizeInWindows - overlapSizeInWindows)+1;
				int numberOfAllPartitions = new Double(Math.floor(numberOfAllPartitionsD)).intValue();
				
				// If the partition size is greater than music song length..
				if(numberOfAllPartitions == 0) {
					throw new NodeException("Partition size too large");
				}
				
				int currentWindow = 0;
				
				// Go through all required partitions
				for(int numberOfCurrentPartition=0;numberOfCurrentPartition<numberOfSegments * numberOfPartitionsToSelect;
				  numberOfCurrentPartition++) {
					
					// Calculate the start (inclusive) and end (exclusive) windows for the current partition
					int currentSegment = numberOfCurrentPartition / numberOfPartitionsToSelect;
					int currentPartitionInSegment = numberOfCurrentPartition % numberOfPartitionsToSelect;
					int maxNumberOfPartitionsInSegment = new Double(((segments.get(currentSegment)[1] - segments.get(currentSegment)[0]) - 
						partitionSizeInWindows) / (partitionSizeInWindows - overlapSizeInWindows)).intValue();
					
					// If the current segment is small and does not have enough partitions...
					if(currentPartitionInSegment >= maxNumberOfPartitionsInSegment) continue;
					
					int numberOfPossiblePartitionsToSelect;
					if(numberOfPartitionsToSelect > maxNumberOfPartitionsInSegment) {
						numberOfPossiblePartitionsToSelect = maxNumberOfPartitionsInSegment;
					} else {
						numberOfPossiblePartitionsToSelect = numberOfPartitionsToSelect;
					}
					
					int midWindowOfCurrentSegment = new Double( (segments.get(currentSegment)[1] + segments.get(currentSegment)[0])/2 ).intValue(); 
					Double partitionStart = (2d*new Double(midWindowOfCurrentSegment) - partitionSizeInWindows -
							(numberOfPossiblePartitionsToSelect - 1)*(partitionSizeInWindows - overlapSizeInWindows)) / 2 + 
							currentPartitionInSegment*(partitionSizeInWindows - overlapSizeInWindows); 
					Double partitionEnd = partitionStart + partitionSizeInWindows;
					
					// Increment the number of current time window if the lower partition boundary is not achieved
					for(int k=currentWindow;k<features.get(i).getWindows().size();k++) {
						if(features.get(i).getWindows().get(k) >= partitionStart) {
							currentWindow = k;
							break;
						}
					}
					
					// If no features are available for the current partition, go to the next partition
					if(features.get(i).getWindows().get(currentWindow) > partitionEnd) {
						continue;
					}
					
					// Create a list with time windows which are in the current partition
					ArrayList<Double> windowsOfCurrentPartition = new ArrayList<Double>();
					while(features.get(i).getWindows().get(currentWindow) >= partitionStart && 
							features.get(i).getWindows().get(currentWindow) < partitionEnd) {
						windowsOfCurrentPartition.add(features.get(i).getWindows().get(currentWindow));
						
						// The last existing window is achieved
						if(currentWindow == features.get(i).getWindows().size() - 1) {
							break;
						}
						currentWindow++;
					}
					
					// Check if the current partition has any windows
					if(windowsOfCurrentPartition.size() == 0) {
						continue;
					}
					
					// Update the list with all used time windows for a metric
					// TODO Evtl. auslagern
					if(i == 0) {
						for(int k=0;k<windowsOfCurrentPartition.size();k++) {
							if(!usedTimeWindows.contains(windowsOfCurrentPartition.get(k))) {
								usedTimeWindows.add(windowsOfCurrentPartition.get(k));
							}
						}
					}
					
					// Calculate mean and standard deviation for feature dimensions
					// Go through all feature dimensions
					for(int k=0;k<numberOfAllSingleFeatures;k++) {
						
						// Calculate mean and variance
						Double mean = 0d;
						Double variance = 0d;
						for(Double l:windowsOfCurrentPartition) {
							mean += features.get(i).getValuesFromWindow(l)[k];
						}
						mean /= windowsOfCurrentPartition.size();
						for(Double l:windowsOfCurrentPartition) {
							variance += Math.pow((Double)features.get(i).getValuesFromWindow(l)[k]-mean,2);
						}
						variance /= windowsOfCurrentPartition.size();
								
						// Add mean and deviation to the new generated features
						Double[] meanD = new Double[1]; meanD[0] = mean;
						Double[] stddevD = new Double[1]; stddevD[0] = variance;
						newFeatures.get(2*k).getValues().add(meanD);
						newFeatures.get(2*k).getWindows().add(partitionStart);
						newFeatures.get(2*k+1).getValues().add(stddevD);
						newFeatures.get(2*k+1).getWindows().add(partitionStart);
						
						// Go with the current window back because of overlap (some time windows used in the
						// current partition may be also used in the next partition)
						while(features.get(i).getWindows().get(currentWindow) >= partitionStart + (partitionSizeInWindows - overlapSizeInWindows) && currentWindow > 0) {
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
