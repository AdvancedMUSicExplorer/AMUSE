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
 * Creation date: 19.05.2009
 */
package amuse.nodes.processor.methods.generators;

import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.util.AmuseLogger;

/**
 * Performs the calculation of the running mean of the given features
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class RunningMeanCalculator extends AmuseTask implements DimensionProcessorInterface {

	/** Subset size */
	private int subsetSize = 10;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString != null) {
			subsetSize = new Integer(parameterString);
		} else {
			throw new NodeException("Parameters for running means calculation are not set!");
		}
	}
	
	/**
	 * Calculate the running mean of the given features
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting running mean calculation...");
		
		ArrayList<Feature> newFeaturesRunningMeans = new ArrayList<Feature>(features.size());
		
		// Go through features
		for(int j=0;j<features.size();j++) {
			int sampleRate = features.get(j).getSampleRate();
			
			// Calculate the running mean values
			ArrayList<Double[]> valuesOfRunningMeans = new ArrayList<Double[]>(features.get(j).getValues().size());
			
			if(subsetSize > features.get(j).getValues().size()) {
				AmuseLogger.write(this.getClass().getName(), Level.WARN, "Subset size is larger than feature values number for feature " + 
						j + "; omitting this feature..");
				continue;
			}
			
			for(int k=0;k<features.get(j).getWindows().size()-subsetSize+1;k++) {
				Double[] runningMeansForCurrentWindow = new Double[features.get(j).getDimension()];
				
				// Go through all feature dimensions
				for(int d=0;d<features.get(j).getDimension();d++) {
					double sum = 0d;
					for(int m=0;m<subsetSize;m++) {
						sum += features.get(j).getValues().get(k+m)[d];
					}
					sum /= (double)subsetSize;
					runningMeansForCurrentWindow[d] = sum;
				}
				valuesOfRunningMeans.add(runningMeansForCurrentWindow);
			}
			
			// For last x-1 time windows (x == subsetSize) no running means can be calculated!
			Double[] derivationsForLastWindow = new Double[features.get(j).getDimension()];
			for(int d=0;d<features.get(j).getDimension();d++) {
				derivationsForLastWindow[d] = Double.NaN;
			}
			for(int d=0;d<subsetSize-1;d++) {
				valuesOfRunningMeans.add(derivationsForLastWindow);
			}
			
			// Save the features
			Feature currentRunningMean = new Feature(features.get(j).getIds(),features.get(j).getDescription(),
					valuesOfRunningMeans,features.get(j).getWindows());
			currentRunningMean.setHistory(features.get(j).getHistory());
			currentRunningMean.getHistory().add(new String("Running_mean_with_subset_size_" + subsetSize));
			currentRunningMean.setSampleRate(sampleRate);
			newFeaturesRunningMeans.add(currentRunningMean);
		}
				
		for(Feature f : newFeaturesRunningMeans) {
			features.add(f);
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...running mean calculation succeeded");
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

}
