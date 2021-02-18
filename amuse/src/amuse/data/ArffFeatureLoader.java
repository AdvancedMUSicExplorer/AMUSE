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
 * Creation date: 18.04.2008
 */
package amuse.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import amuse.interfaces.nodes.NodeException;

/**
 * Loads feature from an arff file
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ArffFeatureLoader {
	
	public static Feature loadFeature(String featureFile, int id) throws NodeException {
		ArrayList<Double[]> values;
		ArrayList<Double> windows;
		int numberOfValues = 0;
		int sourceFrameSize = -1;
		int sourceStepSize = -1;
		int sampleRate = -1;
		String featureName = new String();
		
		FileReader featureInput = null;
		try {
			featureInput = new FileReader(featureFile);
		} catch(FileNotFoundException e) {
			throw new NodeException("Could not open the feature file: " + e.getMessage());
		}
		BufferedReader featureReader = new BufferedReader(featureInput);
		
		boolean nameFound = false;
		
		boolean windowNumberAttributeExisting = false;
		
		int dimensionNumber = 0;
		try {
			String line = featureReader.readLine();
			while(!line.toLowerCase().startsWith(new String("@data"))) {
				if(line.toLowerCase().replaceAll("\\s+","").startsWith(new String("%columns="))) {
					numberOfValues = new Integer(line.replaceAll("\\s+","").substring(9));
				} else if (line.toLowerCase().replaceAll("\\s+","").startsWith(new String("%window_size="))) {
					sourceFrameSize = new Integer(line.replaceAll("\\s+","").substring(13)); 
				} else if(line.toLowerCase().replaceAll("\\s+","").startsWith(new String("%step_size="))) {
					sourceStepSize = new Integer(line.replaceAll("\\s+","").substring(11));
				} else if (line.toLowerCase().replaceAll("\\s+","").startsWith(new String("%sample_rate="))) {
					sampleRate = new Integer(line.replaceAll("\\s+","").substring(13));
				} else if(line.toLowerCase().startsWith(new String("@attribute"))) {
					String lineTrimmed = line.replace("'", "");
					lineTrimmed = lineTrimmed.replace("\"", "");
					if(lineTrimmed.toLowerCase().startsWith(new String("@attribute windownumber"))) {
						windowNumberAttributeExisting = true;
					}
					if(!nameFound) {
						featureName = line.substring(line.indexOf(" ")+1,line.lastIndexOf(" "));
						if(featureName.startsWith("'")) {
							featureName = featureName.substring(1,featureName.length());
						}
						if(featureName.endsWith("'")) {
							featureName = featureName.substring(0,featureName.length()-1);
						}
						nameFound = true;
					}
					dimensionNumber++;
				}
				
				line = featureReader.readLine();
			}
			
			// Initialize the array lists
			values = new ArrayList<Double[]>(numberOfValues);
			windows = new ArrayList<Double>(numberOfValues);
			
			// Window number (the last attribute) does not count as feature dimension!
			if(windowNumberAttributeExisting) {
				dimensionNumber--;
			}
			
			line = featureReader.readLine();
			while(line != null) {
				if(!line.equals("") && !line.startsWith("%")) {
					
					// Proceed the attributes
					StringTokenizer t = new StringTokenizer(line,",");
					Double[] valuesOfCurrentWindow = new Double[dimensionNumber];
					for(int i=0;i<dimensionNumber;i++) {
						String val = t.nextToken();
						if(val.toUpperCase().equals("NAN")) {
							val = "NaN";
						}
						valuesOfCurrentWindow[i] = new Double(val);
					}
					
					// Add time window of this feature (last attribute)
					if(windowNumberAttributeExisting) {
						windows.add(new Double(t.nextToken()));
					} else {
						windows.add(-1d);
					}
					values.add(valuesOfCurrentWindow);
				}
				line = featureReader.readLine();
			}
			
			featureReader.close();
			
		} catch(IOException e) {
			throw new NodeException("Could not read from the feature file: " + e.getMessage());
		}
		
		// Create the Feature object and set the frame size
		ArrayList<Integer> ids = new ArrayList<Integer>(1);
		ids.add(id);
		Feature loadedFeature = new Feature(ids, featureName, values, windows);
		loadedFeature.setSourceFrameSize(sourceFrameSize);
		if(sourceStepSize == -1) {
			sourceStepSize = sourceFrameSize;
		}
		loadedFeature.setSourceStepSize(sourceStepSize);
		loadedFeature.setSampleRate(sampleRate);
		return loadedFeature;
	}

}
