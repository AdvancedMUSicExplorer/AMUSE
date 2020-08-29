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
 * Creation date: 15.06.2009
 */
package amuse.nodes.processor.methods.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.io.ArffDataSet;
import amuse.data.io.attributes.Attribute;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Performs standard normalization of given features using the given minimal and maximum values
 * for each feature. These values are loaded from $AMUSEHOME$/tools/Normalizer and may be created 
 * from theoretical analysis or empirical study on a large amount of music files
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class NormalizerWithGivenMinMax extends AmuseTask implements DimensionProcessorInterface {

	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Do nothing
		// TODO Set path (optionally) to file with information about normalized features
	}
	
	/**
	 * Perform normalization
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting normalization...");
		
		// Load the minimal and maximum values for each feature
		HashMap<Integer,Double[]> mins = new HashMap<Integer,Double[]>();
		HashMap<Integer,Double[]> maxs = new HashMap<Integer,Double[]>();
		loadMinMaxVals(mins,maxs);
		
		// Go through features
		for(int j=0;j<features.size();j++) {
			   	
			features.get(j).getHistory().add(new String("Normalized"));
				
			// Go through all time windows
			for(int k=0;k<features.get(j).getWindows().size();k++) {
				
				// Go through feature dimensions
				for(int l=0;l<features.get(j).getValues().get(k).length;l++) {
					int featureId = features.get(j).getId();
					
					// Normalize
					if(!maxs.get(featureId)[l].equals(mins.get(featureId)[l])) {
						features.get(j).getValues().get(k)[l] = new Double(
							((features.get(j).getValues().get(k)[l]-mins.get(featureId)[l]) /
							 (maxs.get(featureId)[l]-mins.get(featureId)[l])));
					}
				}
				
			}
		}
				
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...normalization succeeded");
	}
	
	/**
	 * Load the minimal and maximum values for each feature dimension
	 * @param mins For minimal values
	 * @param maxs For maximum values
	 * @throws NodeException
	 */
	private void loadMinMaxVals(HashMap<Integer, Double[]> mins, HashMap<Integer, Double[]> maxs) throws NodeException {
		DataSetAbstract featuresMinMaxSet;
		try {
			if(this.correspondingScheduler.getDirectStart()) {
				featuresMinMaxSet = new ArffDataSet(new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + 
					File.separator + "tools" + File.separator + "Normalizer" + File.separator + "featureTableMaxMin.arff"));
			} else {
				featuresMinMaxSet = new ArffDataSet(new File(this.correspondingScheduler.getHomeFolder() + 
					File.separator + "tools" + File.separator + "Normalizer" + File.separator + "featureTableMaxMin.arff"));
			}
		} catch (IOException e) {
			throw new NodeException("Could not load featureTableMaxMin.arff: " + e.getMessage());
		}
		Attribute idAttribute = featuresMinMaxSet.getAttribute("Id");
		Attribute minAttribute = featuresMinMaxSet.getAttribute("Min");
		Attribute maxAttribute = featuresMinMaxSet.getAttribute("Max");
		for(int i=0;i<featuresMinMaxSet.getValueCount();i++) {
	
			String minValuesString = minAttribute.getValueAt(i).toString();
			String maxValuesString = maxAttribute.getValueAt(i).toString();
			StringTokenizer minTok = new StringTokenizer(minValuesString," ");
			StringTokenizer maxTok = new StringTokenizer(maxValuesString," ");
	
			// Min / max values for different dimensions of one feature
			Double[] minValues = new Double[minTok.countTokens()];
			Double[] maxValues = new Double[maxTok.countTokens()];
			if(minValues.length != maxValues.length) {
				throw new NodeException("Number of feature dimensions is set wrongly in featureTableMaxMin.arff");
			}
	
			// Set the min / max values
			int c=0;
			while(minTok.hasMoreTokens()) {
				minValues[c] = new Double(minTok.nextToken());
				maxValues[c] = new Double(maxTok.nextToken());
				c++;
			}
	
			mins.put(new Double(idAttribute.getValueAt(i).toString()).intValue(), minValues);
			maxs.put(new Double(idAttribute.getValueAt(i).toString()).intValue(), maxValues);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

}
