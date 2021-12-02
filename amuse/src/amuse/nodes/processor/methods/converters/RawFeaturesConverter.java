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
 * Creation date: 23.03.2020
 */
package amuse.nodes.processor.methods.converters;

import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface;
import amuse.util.AmuseLogger;

/**
 * Harmonizes raw features without calculating any statisitcs on them
 * 
 * @author Philipp Ginsel
 * @version $Id$
 */
public class RawFeaturesConverter extends AmuseTask implements MatrixToVectorConverterInterface{

	@Override
	public void initialize() throws NodeException {
		// Do nothing
		
	}

	@Override
	public void setParameters(String parameterString) throws NodeException {
		// Do nothing
	}

	@Override
	public ArrayList<Feature> runConversion(ArrayList<Feature> features, Integer aggregationWindowSize, Integer stepSize,
			String nameOfProcessorModel, Unit unit) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the raw feature conversion...");
		
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalStepSize();
				
		// Single features used as classifier input vector
		ArrayList<Feature> endFeatures = new ArrayList<Feature>();
		
		try {
			double classificationWindowSizeInWindows;
			double overlapSizeInWindows;
			int numberOfAllClassificationWindows;
			
			int sampleRate = features.get(0).getSampleRate();
			
			// Aggregate the data over the complete track or build classification windows?
			if(aggregationWindowSize == -1) {
				
				// In 1st case we have only one "classification window" which covers the complete track
				classificationWindowSizeInWindows = features.get(0).getWindows().get(features.get(0).getWindows().size()-1);
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
				
				// FIXME evtl. check! Calculates the last used time window and the number of maximum available classification windows from it
				double numberOfAllClassificationWindowsD = ((features.get(0).getWindows().get(features.get(0).getWindows().size()-1)) - classificationWindowSizeInWindows)/(classificationWindowSizeInWindows - overlapSizeInWindows)+1;
				numberOfAllClassificationWindows = new Double(Math.floor(numberOfAllClassificationWindowsD)).intValue();
			}
			
			// If the classificatoin window size is greater than music track length..
			if(numberOfAllClassificationWindows == 0) {
				throw new NodeException("Classification window size too large");
			}
			
			for(int i=0; i<classificationWindowSizeInWindows;i++) {
				for(int j=0; j<features.size(); j++) {
					int dimensions = features.get(j).getDimension();
					for(int k=0; k<dimensions;k++) {
						Feature newFeature = new Feature(-1);
						newFeature.setHistory(features.get(j).getHistory());
						newFeature.getHistory().add("window"+(i+1)+"_dim"+(k+1));
						endFeatures.add(newFeature);
					}
				}
			}
			
			int currentWindow = 0;
			// Go through all classification windows
			for(int numberOfCurrentClassificationWindow=0;numberOfCurrentClassificationWindow<numberOfAllClassificationWindows;numberOfCurrentClassificationWindow++) {
				
				// Calculate the start (inclusive) and end (exclusive) windows for the current classificaiton window
				Double classificationWindowStart = Math.floor(new Double(classificationWindowSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentClassificationWindow));
				Double classificationWindowEnd = Math.floor((new Double(classificationWindowSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentClassificationWindow)+classificationWindowSizeInWindows));
				
				// Increment the number of current time window if the lower classification window boundary is not achieved
				for(int k=currentWindow;k<features.get(0).getWindows().size();k++) {
					if(features.get(0).getWindows().get(k) >= classificationWindowStart) {
						currentWindow = k;
						break;
					}
				}
				
				// If no features are available for the current classification window, go to the next classification window
				if(features.get(0).getWindows().get(currentWindow) > classificationWindowEnd || features.get(0).getWindows().get(currentWindow) < classificationWindowStart) {
					continue;
				}
				
				int currentEndFeature = 0;
				for(int window= classificationWindowStart.intValue(); window<classificationWindowEnd;window++) {
					// Increment the number of current time window if the current window is not reached
					for(int k=currentWindow;k<features.get(0).getWindows().size();k++) {
						if(features.get(0).getWindows().get(k) >= window) {
							currentWindow = k;
							break;
						}
					}
					
					// if the window could not be reached we have to fill the remaining values with NaN values
					if(features.get(0).getWindows().get(currentWindow) < window) {
						for(Feature feature : features) {
							int dimensions = feature.getDimension();
							for(int dim=0; dim<dimensions; dim++) {
								Double[] val = {Double.NaN};
								endFeatures.get(currentEndFeature).getWindows().add(new Double(window));
								endFeatures.get(currentEndFeature).getValues().add(val);
								currentEndFeature++;
							}
						}
					} else {
						// fill the end features with the correct values
						for(Feature feature : features) {
							int dimensions = feature.getDimension();
							Double[] vals = feature.getValues().get(currentWindow);
							for(int dim=0; dim<dimensions; dim++) {
								Double[] val = {vals[dim]};
								endFeatures.get(currentEndFeature).getWindows().add(new Double(window));
								endFeatures.get(currentEndFeature).getValues().add(val);
								currentEndFeature++;
							}
						}
					}
				}
			}
			
		} catch(Exception e) {
			throw new NodeException("Problem occured during feature conversion: " + e.getMessage());
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...conversion succeeded");
		return endFeatures;
	}

}
