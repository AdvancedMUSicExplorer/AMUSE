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
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface;
import amuse.util.AmuseLogger;

/**
 * Performs GMM conversion of the given feature matrix
 * 
 * @author Igor Vatolkin
 * @version $Id: GMMConverter.java 1210 2010-08-02 09:08:13Z vatolkin $
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
	
	public ArrayList<Feature> runConversion(ArrayList<Feature> features, Integer ms, Integer overlap, String nameOfProcessorModel, long taskId) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the GMM conversion...");
		
		// TODO Currently only 22050 sampling rate is supported!
		int sampleRate = 22050;
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalFrameSize();
		
		// Single features used as classifier input vector
		ArrayList<Feature> endFeatures = new ArrayList<Feature>();
		
		try {

			// Go through music features
			for(int i=0;i<features.size();i++) {
				int numberOfAllSingleFeatures = features.get(i).getValues().get(0).length;
				
				ArrayList<Feature> newFeatures = new ArrayList<Feature>(numberOfAllSingleFeatures * 
						((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0)));
				for(int j=0;j<numberOfAllSingleFeatures;j++) {
					if(saveMeanValues) {
						Feature meanOfCurrentSingleFeature = new Feature(-1);
						meanOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
						meanOfCurrentSingleFeature.getHistory().add("Mean_" + (j+1));
						newFeatures.add(meanOfCurrentSingleFeature);
					}
					if(saveStddevValues) {
						Feature stdDevOfCurrentSingleFeature = new Feature(-1);
						stdDevOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
						stdDevOfCurrentSingleFeature.getHistory().add("Std_dev_" + (j+1));
						newFeatures.add(stdDevOfCurrentSingleFeature);
					}
				}
				
				double partitionSizeInWindows;
				double overlapSizeInWindows;
				int numberOfAllPartitions;
				
				// Aggregate the data over the complete song or build partitions?
				if(ms == -1) {
					
					// In 1st case we have only one "partition" which covers the complete song
					// ("+ 1" is used because of the exclusive calculation of the partition end window)
					partitionSizeInWindows = features.get(i).getWindows().get(features.get(i).getWindows().size()-1) + 1;
					overlapSizeInWindows = partitionSizeInWindows;
					numberOfAllPartitions = 1;
				} else {
					
					// In 2nd case we can calculate the number of windows which belong to each partition
					partitionSizeInWindows = (Double)(sampleRate*(ms/1000d)/windowSize);
					overlapSizeInWindows = (Double)(sampleRate*((ms-overlap)/1000d)/windowSize);
					
					// Calculates the last used time window and the number of maximum available partitions from it
					double numberOfAllPartitionsD = ((features.get(i).getWindows().get(features.get(i).getWindows().size()-1)) - partitionSizeInWindows)/(partitionSizeInWindows - overlapSizeInWindows)+1;
					numberOfAllPartitions = new Double(Math.ceil(numberOfAllPartitionsD)).intValue();
				}
				
				// If the partition size is greater than music song length..
				if(numberOfAllPartitions == 0) {
					throw new NodeException("Partition size too large");
				}
				
			    // TODO Consider only the first 6 minutes of a music track; should be a parameter?
				// FUNKTIONIERT NICHT MIT 30'' PRUNER!!! 
				/*if(numberOfAllPartitions > 360000/overlap) {
					//numberOfAllPartitions = 360000/overlap;
				}*/
				
				int currentWindow = 0;
				
				// Go through all partitions
				for(int numberOfCurrentPartition=0;numberOfCurrentPartition<numberOfAllPartitions;numberOfCurrentPartition++) {
					
					// Calculate the start (inclusive) and end (exclusive) windows for the current partition
					Double partitionStart = Math.floor(new Double(partitionSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentPartition));
					Double partitionEnd = Math.ceil((new Double(partitionSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentPartition)+partitionSizeInWindows));
					
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
					
					// Calculate mean and standard deviation for feature dimensions
					// Go through all feature dimensions
					for(int k=0;k<numberOfAllSingleFeatures;k++) {
						
						// Number of values (NaN values are omitted!)
						int valueNumber = 0;
						
						// Calculate mean and variance
						Double mean = 0d;
						Double variance = 0d;
						for(Double l:windowsOfCurrentPartition) {
							if(!Double.isNaN(features.get(i).getValuesFromWindow(l)[k])) {
								mean += features.get(i).getValuesFromWindow(l)[k];
								valueNumber++;
							}
						}
						mean /= valueNumber;
						for(Double l:windowsOfCurrentPartition) {
							if(!Double.isNaN(features.get(i).getValuesFromWindow(l)[k])) {
								variance += Math.pow((Double)features.get(i).getValuesFromWindow(l)[k]-mean,2);
							}
						}
						variance /= valueNumber;
								
						// Add mean and deviation to the new generated features
						if(numberOfCurrentPartition < numberOfAllPartitions) {
							Double[] meanD = new Double[1]; meanD[0] = mean;
							Double[] stddevD = new Double[1]; stddevD[0] = variance;
							if(saveMeanValues) {
								newFeatures.get(((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0))*k).getValues().add(meanD);
								newFeatures.get(((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0))*k).getWindows().add(new Double(partitionStart));
							}
							if(saveStddevValues) {
								newFeatures.get(((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0))*k+(saveMeanValues ? 1 : 0)).getValues().add(stddevD);
								newFeatures.get(((saveMeanValues ? 1 : 0) + (saveStddevValues ? 1 : 0))*k+(saveMeanValues ? 1 : 0)).getWindows().add(new Double(partitionStart));
							}
						}
						
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
