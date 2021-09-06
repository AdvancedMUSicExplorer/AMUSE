/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2020 by code authors
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
 * Creation date: 29.08.2020
 */
package amuse.nodes.processor.methods.preprocessing.discretization;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.util.AmuseLogger;

/**
 * Performs discretization of original values to intervals defined by percentiles
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class PercentileDiscretization extends AmuseTask implements DimensionProcessorInterface {
	
	/** Number of percentile boundaries (default: 4 for quartiles */
	private int numOfPercentileBoundaries = 4;

	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		this.numOfPercentileBoundaries = new Integer(parameterString);
	}
	
	/**
	 * Perform normalization
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting percentile discretization...");
		
		// Go through features
		for(int f=0;f<features.size();f++) {
			
			// After each indexBoundaryForPercentile indices begins the new interval for discretization
			int indexBoundaryForPercentile = features.get(f).getWindows().size() / numOfPercentileBoundaries;
			   	
			features.get(f).getHistory().add(new String("PercentileDiscretized"));
				
			// Go through feature dimensions
			for(int d=0;d<features.get(f).getValues().get(0).length;d++) {
				
				ArrayList<Double> sortedValuesOfCurrentDimension = new ArrayList<Double>(features.get(f).getWindows().size());
				
				// Sort the values for all time windows
				for(int tw=0;tw<features.get(f).getWindows().size();tw++) {
					sortedValuesOfCurrentDimension.add(features.get(f).getValues().get(tw)[d]);
				}
				Collections.sort(sortedValuesOfCurrentDimension);
				
				// Go through all time windows and discretize
				for(int tw=0;tw<features.get(f).getWindows().size();tw++) {
					int positionInSortedList = sortedValuesOfCurrentDimension.indexOf(features.get(f).getValues().get(tw)[d]);
					features.get(f).getValues().get(tw)[d] = new Double(positionInSortedList / indexBoundaryForPercentile);
				}
			}
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...discretization succeeded");
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

}
