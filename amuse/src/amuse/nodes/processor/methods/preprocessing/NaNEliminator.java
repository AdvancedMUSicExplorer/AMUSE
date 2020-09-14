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
 * Creation date: 11.12.2007
 */
package amuse.nodes.processor.methods.preprocessing;

import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.util.AmuseLogger;

/**
 * Replaces NaN feature values with the median values 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class NaNEliminator extends AmuseTask implements DimensionProcessorInterface {

	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Currently do nothing (possible are different methods for NaN elimination)
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

	/**
	 * Replace NaN feature values with the median values
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting NaN elimination... ");
		
		try {
			
			// Go through features
			for(int i=0;i<features.size();i++) {
				features.get(i).getHistory().add(new String("NaN_eliminated"));
				
				int numberOfAllSingleFeatures = features.get(i).getValues().get(0).length;

				// Go through all feature dimensions
				for(int j=0;j<numberOfAllSingleFeatures;j++) {
					
					// Calculate the median
					ArrayList<Double> featureValues = new ArrayList<Double>(features.get(i).getValues().size());
					for(int k=0;k<features.get(i).getValues().size();k++) {
						if(!features.get(i).getValues().get(k)[j].isNaN()) {
							featureValues.add(new Double(features.get(i).getValues().get(k)[j]));
						}
					}
					java.util.Collections.sort(featureValues);
					
					// If only NaN values are there, replace by zero
					double median = 0d;
					if(featureValues.size()>0) {
						median = featureValues.get(featureValues.size()/2);
					}
					
					// Replace NaN-values to medians
					for(int k=0;k<features.get(i).getValues().size();k++) {
						if(features.get(i).getValues().get(k)[j].isNaN()) {
							features.get(i).getValues().get(k)[j] = median;
						}
					}
			    }
			}
		} catch(Exception e) {
			throw new NodeException("Problem occured during NaN elimination: " + e.getMessage());
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...elimination succeeded");
	}
	
}
