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

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
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

	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Do nothing
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
	public ArrayList<Feature> runConversion(ArrayList<Feature> features, Integer ms, Integer stepSize, String nameOfProcessorModel) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the quartile conversion...");
		
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalStepSize();
				
		// Single features used as classifier input vector
		ArrayList<Feature> endFeatures = new ArrayList<Feature>();
		
		try {

			// Go through music features
			for(int i=0;i<features.size();i++) {
				int sampleRate = features.get(i).getSampleRate();
				int numberOfAllSingleFeatures = features.get(i).getValues().get(0).length;
				
				ArrayList<Feature> newFeatures = new ArrayList<Feature>(numberOfAllSingleFeatures*5);
				for(int j=0;j<numberOfAllSingleFeatures;j++) {
					Feature minOfCurrentSingleFeature = new Feature(-1);
					minOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
					minOfCurrentSingleFeature.getHistory().add("Min_" + (j+1));
					minOfCurrentSingleFeature.setSampleRate(sampleRate);
					Feature firstQBoundOfCurrentSingleFeature = new Feature(-1);
					firstQBoundOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
					firstQBoundOfCurrentSingleFeature.getHistory().add("1st_quartile_bound" + (j+1));
					firstQBoundOfCurrentSingleFeature.setSampleRate(sampleRate);
					Feature secondQBoundOfCurrentSingleFeature = new Feature(-1);
					secondQBoundOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
					secondQBoundOfCurrentSingleFeature.getHistory().add("2nd_quartile_bound" + (j+1));
					secondQBoundOfCurrentSingleFeature.setSampleRate(sampleRate);
					Feature thirdQBoundOfCurrentSingleFeature = new Feature(-1);
					thirdQBoundOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
					thirdQBoundOfCurrentSingleFeature.getHistory().add("3rd_quartile_bound" + (j+1));
					thirdQBoundOfCurrentSingleFeature.setSampleRate(sampleRate);
					Feature maxOfCurrentSingleFeature = new Feature(-1);
					maxOfCurrentSingleFeature.setHistory(features.get(i).getHistory());
					maxOfCurrentSingleFeature.getHistory().add("Max_" + (j+1));
					maxOfCurrentSingleFeature.setSampleRate(sampleRate);
					newFeatures.add(minOfCurrentSingleFeature);
					newFeatures.add(firstQBoundOfCurrentSingleFeature);
					newFeatures.add(secondQBoundOfCurrentSingleFeature);
					newFeatures.add(thirdQBoundOfCurrentSingleFeature);
					newFeatures.add(maxOfCurrentSingleFeature);
				}
				
				double partitionSizeInWindows;
				double overlapSizeInWindows;
				int numberOfAllPartitions;
				
				// Aggregate the data over the complete song or build partitions?
				if(ms == -1) {
					
					// In 1st case we have only one "partition" which covers the complete song
					partitionSizeInWindows = features.get(i).getWindows().get(features.get(i).getWindows().size()-1);
					overlapSizeInWindows = partitionSizeInWindows;
					numberOfAllPartitions = 1;
				} else {
					
					// In 2nd case we can calculate the number of windows which belong to each partition
					partitionSizeInWindows = (Double)(sampleRate*(ms/1000d)/windowSize);
					overlapSizeInWindows = (Double)(sampleRate*((ms-stepSize)/1000d)/windowSize);
					
					// FIXME evtl. check! Calculates the last used time window and the number of maximum available partitions from it
					double numberOfAllPartitionsD = ((features.get(i).getWindows().get(features.get(i).getWindows().size()-1)) - partitionSizeInWindows)/(partitionSizeInWindows - overlapSizeInWindows)+1;
					numberOfAllPartitions = new Double(Math.floor(numberOfAllPartitionsD)).intValue();
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
						
						// Save the feature values for this partition here for sorting
						ArrayList<Double> featureValuesForThisPartition = new ArrayList<Double>();
						for(Double l:windowsOfCurrentPartition) {
							featureValuesForThisPartition.add(features.get(i).getValuesFromWindow(l)[k]);
						}
						
						// Remove NaN-values for quartile calculation (it is also possible that for feature with
						// large source frames the last smaller frames are filled with NaN-values!)
						for(int z=0;z<featureValuesForThisPartition.size();z++) {
							if(featureValuesForThisPartition.get(z).isNaN()) {
								featureValuesForThisPartition.remove(z); z--;
							}
						}
						java.util.Collections.sort(featureValuesForThisPartition);
						
						Double[] minD = new Double[1]; 
						Double[] firstQD = new Double[1]; 
						Double[] secondQD = new Double[1]; 
						Double[] thirdQD = new Double[1]; 
						Double[] maxD = new Double[1];
						
						if(numberOfCurrentPartition < numberOfAllPartitions) {
							if(featureValuesForThisPartition.size() > 0) {
								minD[0] = (Double)featureValuesForThisPartition.get(0);
								int indexOfFirstBoundary = new Double(featureValuesForThisPartition.size()*0.25).intValue();
								firstQD[0] = (Double)featureValuesForThisPartition.get(indexOfFirstBoundary);
								int indexOfSecondBoundary = new Double(featureValuesForThisPartition.size()*0.5).intValue();
								secondQD[0] = (Double)featureValuesForThisPartition.get(indexOfSecondBoundary);
								int indexOfThirdBoundary = new Double(featureValuesForThisPartition.size()*0.75).intValue();
								thirdQD[0] = (Double)featureValuesForThisPartition.get(indexOfThirdBoundary);
								maxD[0] = (Double)featureValuesForThisPartition.get(featureValuesForThisPartition.size()-1);
							} else { // If all feature values consist of NaN values
								minD[0] = Double.NaN;
								firstQD[0] = Double.NaN;
								secondQD[0] = Double.NaN;
								thirdQD[0] = Double.NaN;
								maxD[0] = Double.NaN;
							}
							
							newFeatures.get(5*k).getValues().add(minD);
							newFeatures.get(5*k).getWindows().add(new Double(partitionStart));
							newFeatures.get(5*k+1).getValues().add(firstQD);
							newFeatures.get(5*k+1).getWindows().add(new Double(partitionStart));
							newFeatures.get(5*k+2).getValues().add(secondQD);
							newFeatures.get(5*k+2).getWindows().add(new Double(partitionStart));
							newFeatures.get(5*k+3).getValues().add(thirdQD);
							newFeatures.get(5*k+3).getWindows().add(new Double(partitionStart));
							newFeatures.get(5*k+4).getValues().add(maxD);
							newFeatures.get(5*k+4).getWindows().add(new Double(partitionStart));
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

}
