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
 * Creation date: 21.05.2008
 */
package amuse.nodes.processor.methods.reducers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Performs sampling of the given feature files
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class DataSampler extends AmuseTask implements DimensionProcessorInterface {

	/** Difference between windows which remain */
	private int stepSize = 1;
	
	/** Sampling method */
	private String method = null; 
	
	/** Sampling coefficient */
	private double coeff = 1;
	
	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString.startsWith(new String("t")) || parameterString.startsWith(new String("b")) 
				|| parameterString.startsWith(new String("o"))) {
			method = parameterString.substring(0,1);
			if(parameterString.length() > 1) {
				coeff = new Double(parameterString.substring(parameterString.indexOf("_")+1,parameterString.length()));
			}
		} else {
			stepSize = new Integer(parameterString);
		}
	}
	
	/**
	 * Perform sampling of the given feature files
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting data sampling...");
		
		// If sampling corresponding to tatum, beat or onset events number should be done...
		if(method != null) {
			String currentInfoFile = ((ProcessingConfiguration)this.correspondingScheduler.getConfiguration()).getMusicFileList().getFileAt(0);
			String relativeName = new String();
			if(currentInfoFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
				relativeName = currentInfoFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length());
			} else {
				relativeName = currentInfoFile;
			}
			if(relativeName.charAt(0) == File.separatorChar) {
				relativeName = relativeName.substring(1);
			}
			relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
			if(relativeName.lastIndexOf(File.separator) != -1) {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
					relativeName.substring(relativeName.lastIndexOf(File.separator));
			} else {
				relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE) + File.separator + relativeName +  
						File.separator + relativeName;
			}	
			
			// Load the event number
			try {
				if(method.equals(new String("t"))) {
					relativeName += "_416.arff"; // Data sampling according to tatum events number
				} else if(method.equals(new String("b"))) {
					relativeName += "_408.arff"; // Data sampling according to beat events number
				} else if(method.equals(new String("o"))) {
					relativeName += "_419.arff"; // Data sampling according to onset events number
				} else {
					throw new NodeException("Unknown sampling method");
				}
			
				// Try to get the number of events from Amuse attribute "columns" 
				int eventTimesNumber = 0;
				boolean attrFound = false;
				File featureFile = new File(relativeName);
				FileReader featureInput = null;
				featureInput = new FileReader(featureFile);
				BufferedReader resultReader = new BufferedReader(featureInput);
				String line = new String();
				while ((line = resultReader.readLine()) != null) {

					if(line.toLowerCase().startsWith("%columns=")) {
						eventTimesNumber = new Integer(line.substring(9));
						attrFound = true;
						break;
					}
				}
				resultReader.close();
				
				// If no columns attribute was available, count the event number manually
				if(!attrFound) {
					DataSetAbstract eventTimesSet = new ArffDataSet(new File(relativeName));
					eventTimesNumber = eventTimesSet.getValueCount();
				}
				
				// Set the step size
				eventTimesNumber *= coeff; 
				stepSize = features.get(0).getValues().size() / eventTimesNumber;
			} catch(IOException e) {
				throw new NodeException("Problem occured during data sampling: " + e.getMessage());
			}
		}
		
		// Go through features
		for(int j=0;j<features.size();j++) {
			features.get(j).getHistory().add(new String(this.stepSize + "_sampled"));
			int windowToLookFor = 0;
					
			// Go through all time windows
			for(int k=0;k<features.get(j).getWindows().size();k++) {
				
				int currentWindow = features.get(j).getWindows().get(k).intValue()-1;
						
				// The value remains
				if(windowToLookFor == currentWindow) {
					windowToLookFor += this.stepSize;	
				} 
					
				// Remove features
				else {
					features.get(j).getValues().remove(k);
					features.get(j).getWindows().remove(k);
					k--;
				}
			}
		}
				
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...sampling succeeded");
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

}
