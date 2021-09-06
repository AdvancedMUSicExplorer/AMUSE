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
 * Creation date: 23.12.2008
 */
package amuse.nodes.processor.methods.reducers;

import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.util.AmuseLogger;

/**
 * Selects only the features from a given interval
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class IntervalSelector extends AmuseTask implements DimensionProcessorInterface {

	private boolean selectFromTheBeginning;
	private int msNumber;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		
		// Examples:
		// 30000_m -> select 30000 milliseconds from the middle of a track
		// 30000_b -> select 30000 milliseconds from the beginning of a track
		
		// Set the number of milliseconds to select
		this.msNumber = new Integer(parameterString.substring(0,parameterString.indexOf("_")));
		
		if(parameterString.endsWith("m")) {
			this.selectFromTheBeginning = false;
		} else {
			this.selectFromTheBeginning = true;
		}
	}
	
	/**
	 * Perform sampling of the given feature files
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting interval selection...");
		
		int startWindow, endWindow;
		
		int sampleRate = features.get(0).getSampleRate();
		Double d = (new Double(sampleRate) / 
				(double)((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalStepSize()) * this.msNumber/1000;
		if(this.selectFromTheBeginning) {
			startWindow = 0;
			endWindow = d.intValue();
		} else {
			// TODO better: read the last window from the file!!!
			int lastWindow = features.get(0).getWindows().get(features.get(0).getWindows().size()-1).intValue();
			
			// If the track is shorter than the given ms interval, no reduction is required
			if(lastWindow < d) {
				AmuseLogger.write(this.getClass().getName(), Level.WARN, "...interval selection not applied since " + 
						"the current track is not long enough");
				return;
			} else {
				int middleWindow = lastWindow / 2;
				startWindow = middleWindow - (d.intValue()/2);
				endWindow = middleWindow + (d.intValue()/2);
			}
		}
		
		// Go through features
		for(int j=0;j<features.size();j++) {
			   	
			if(this.selectFromTheBeginning) {
				features.get(j).getHistory().add(new String(this.msNumber + "_ms_selected_from_beginning"));
			} else {
				features.get(j).getHistory().add(new String(this.msNumber + "_ms_selected_from_middle"));
			}
				
			// Go through all time windows
			for(int k=0;k<features.get(j).getWindows().size();k++) {
				
				int currentWindow = features.get(j).getWindows().get(k).intValue()-1;
						
				// Remove features if they are outside of a given interval
				if(currentWindow < startWindow || currentWindow >= endWindow) {
					features.get(j).getValues().remove(k);
					features.get(j).getWindows().remove(k);
					k--;
				} 
			}
		}
				
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...interval selection succeeded");
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

}
