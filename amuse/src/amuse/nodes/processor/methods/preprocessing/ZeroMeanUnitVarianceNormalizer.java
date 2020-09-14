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
 * Creation date: 29.01.2008
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
 * Performs zero mean - unit variance normalization
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ZeroMeanUnitVarianceNormalizer extends AmuseTask implements DimensionProcessorInterface {

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
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#runDimensionProcessing(java.util.ArrayList)
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting normalization...");
		
		// Go through features
		for(int j=0;j<features.size();j++) {
			   	
			features.get(j).getHistory().add(new String("Zmuv_normalized"));
				
			// Go through feature dimensions
			for(int l=0;l<features.get(j).getValues().get(0).length;l++) {
				double featureDimMean = 0d;
				double featureDimVariance = 0d;
			
				// For all values except NaN!
				double numberCounter = 0; 
				
				// Go through all time windows searching for mean and variance
				for(int m=0;m<features.get(j).getValues().size();m++) {
					if(!Double.isNaN(features.get(j).getValues().get(m)[l])) {
						featureDimMean += features.get(j).getValues().get(m)[l];
						numberCounter++;
					} 
				}
				
				featureDimMean /= numberCounter;
						
				// Go through all time windows searching for mean and variance
				for(int m=0;m<features.get(j).getValues().size();m++) {
					if(!Double.isNaN(features.get(j).getValues().get(m)[l])) {
						featureDimVariance += Math.pow(features.get(j).getValues().get(m)[l]-featureDimMean,2);
					}
				}
				
				featureDimVariance /= numberCounter;
				featureDimVariance = Math.sqrt(featureDimVariance);
				if(featureDimVariance == 0) {
					featureDimVariance = Double.MIN_NORMAL;
				}
				
				// Go through all time windows
				for(int k=0;k<features.get(j).getWindows().size();k++) {
					
					// Normalize
					if(!Double.isNaN(features.get(j).getValues().get(k)[l])) {
						features.get(j).getValues().get(k)[l] = 
							((features.get(j).getValues().get(k)[l]-featureDimMean) /
							 featureDimVariance);
					}
				}
				
			}
		}

		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...normalization succeeded");
	}
	
}
