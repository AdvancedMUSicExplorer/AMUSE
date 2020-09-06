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
 * Creation date: 06.11.2009
 */
package amuse.nodes.processor.methods.generators;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.util.AmuseLogger;

/**
 * Performs the calculation of the 1st and 2nd derivations of the given features
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class DerivationCalculator extends AmuseTask implements DimensionProcessorInterface {

	/** Derivations to calculate */
	private boolean calculateFirstDerivation = false;
	private boolean calculateSecondDerivation = false;
	
	/** Replace the original feature series? */
	private boolean replaceOriginalFeatureSeries = false;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString != null) {
			StringTokenizer tok = new StringTokenizer(parameterString,"_");
			if(tok.nextToken().equals("true")) {
				calculateFirstDerivation = true;
			}
			if(tok.nextToken().equals("true")) {
				calculateSecondDerivation = true;
			}
			if(tok.hasMoreTokens()) {
				if(tok.nextToken().equals("true")) {
					replaceOriginalFeatureSeries = true;
				}
			}
		} else {
			throw new NodeException("Parameters for derivation calculation are not set!");
		}
	}
	
	/**
	 * Calculate the derivations of the given features
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting derivation calculation...");
		
		if(!calculateFirstDerivation && !calculateSecondDerivation) {
			AmuseLogger.write(this.getClass().getName(), Level.WARN, "No derivation selected for calculation...");
			return;
		}
		
		// Create new features
		ArrayList<Feature> newFeatures1stDerivation = null;
		ArrayList<Feature> newFeatures2ndDerivation = null;
		if(calculateFirstDerivation) {
			newFeatures1stDerivation = new ArrayList<Feature>(features.size());
		}
		if(calculateSecondDerivation) {
			newFeatures2ndDerivation = new ArrayList<Feature>(features.size());
		}
		
		// Go through features
		for(int j=0;j<features.size();j++) {
			int sampleRate = features.get(j).getSampleRate();
			
			// Calculate the 1st derivation, it must be done in each case (also if only 2nd derivation is saved as feature)
			ArrayList<Double[]> valuesOf1stDerivation = new ArrayList<Double[]>(features.get(j).getValues().size());
			for(int k=0;k<features.get(j).getWindows().size()-1;k++) {
				Double[] derivationsForCurrentWindow = new Double[features.get(j).getDimension()];
				
				// Go through all feature dimensions
				for(int d=0;d<features.get(j).getDimension();d++) {
					derivationsForCurrentWindow[d] = features.get(j).getValues().get(k+1)[d] - features.get(j).getValues().get(k)[d];
				}
				valuesOf1stDerivation.add(derivationsForCurrentWindow);
			}
			
			// For last time window no first derivation can be calculated!
			Double[] derivationsForLastWindow = new Double[features.get(j).getDimension()];
			for(int d=0;d<features.get(j).getDimension();d++) {
				derivationsForLastWindow[d] = Double.NaN;
			}
			valuesOf1stDerivation.add(derivationsForLastWindow);
			
			// Calculate the 2nd derivation if it is required
			ArrayList<Double[]> valuesOf2ndDerivation = null;
			if(calculateSecondDerivation) {
				valuesOf2ndDerivation = new ArrayList<Double[]>(features.get(j).getValues().size());
				for(int k=0;k<features.get(j).getWindows().size()-2;k++) {
					Double[] derivationsForCurrentWindow = new Double[features.get(j).getDimension()];
					
					// Go through all feature dimensions
					for(int d=0;d<features.get(j).getDimension();d++) {
						derivationsForCurrentWindow[d] = valuesOf1stDerivation.get(k+1)[d] - valuesOf1stDerivation.get(k)[d];  
					}
					valuesOf2ndDerivation.add(derivationsForCurrentWindow);
				}
				
				// For last two time windows no 2nd derivation can be calculated!
				valuesOf2ndDerivation.add(derivationsForLastWindow);
				valuesOf2ndDerivation.add(derivationsForLastWindow);
			}

			// Save the features
			if(calculateFirstDerivation) {
				Feature currentDer = new Feature(features.get(j).getIds(),features.get(j).getDescription(),
						valuesOf1stDerivation,features.get(j).getWindows());
				currentDer.setHistory(features.get(j).getHistory());
				currentDer.getHistory().add(new String("1st_derivation"));
				currentDer.setSampleRate(sampleRate);
				newFeatures1stDerivation.add(currentDer);
			}
			if(calculateSecondDerivation) {
				Feature currentDer = new Feature(features.get(j).getIds(),features.get(j).getDescription(),
						valuesOf2ndDerivation,features.get(j).getWindows());
				currentDer.setHistory(features.get(j).getHistory());
				currentDer.getHistory().add(new String("2nd_derivation"));
				currentDer.setSampleRate(sampleRate);
				newFeatures2ndDerivation.add(currentDer);
			}
		}
		
		// Replace the original feature series with derivation(s)?
		if(replaceOriginalFeatureSeries) {
			features.clear();
		}
				
		if(calculateFirstDerivation) {
			for(Feature f : newFeatures1stDerivation) {
				features.add(f);
			}
		}
		if(calculateSecondDerivation) {
			for(Feature f : newFeatures2ndDerivation) {
				features.add(f);
			}
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...derivation calculation succeeded");
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

}
