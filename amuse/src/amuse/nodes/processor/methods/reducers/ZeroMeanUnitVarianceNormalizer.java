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
package amuse.nodes.processor.methods.reducers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Performs zero mean - unit variance normalization
 * 
 * @author Igor Vatolkin
 * @version $Id: ZeroMeanUnitVarianceNormalizer.java 1078 2010-07-01 14:06:57Z vatolkin $
 */
public class ZeroMeanUnitVarianceNormalizer extends AmuseTask implements DimensionProcessorInterface {

	/**
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Do nothing
	}
	
	/**
	 * Perform zero mean - unit variance normalization
	 * @deprecated
	 */
	public void runReduction(
			ArrayList<ArrayList<String>> currentListOfFeatureFiles,
			String outputFolder) throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting normalization... " + currentListOfFeatureFiles.size());
		
		// Relative path for the music files
		String relativePath = new String();
		
		try {
		
			// (a) If normalization was started as the first processing step, 
			// the relative path must be calculated using the path to Amuse feature database
			if(currentListOfFeatureFiles.get(0).get(0).startsWith(AmusePreferences.get(KeysStringValue.FEATURE_DATABASE))) {
				relativePath = currentListOfFeatureFiles.get(0).get(0).substring(AmusePreferences.
						get(KeysStringValue.FEATURE_DATABASE).length()+1, currentListOfFeatureFiles.get(0).get(0).lastIndexOf("/"));
			}
			// (b) If normalization was started as the second or later step,
			// the relative path must be calculated using the path to intermediate results
			else if(currentListOfFeatureFiles.get(0).get(0).startsWith(properties.getProperty("homeFolder"))) {
				relativePath = currentListOfFeatureFiles.get(0).get(0).substring(properties.getProperty("homeFolder").length()+6);
				relativePath = relativePath.substring(relativePath.indexOf("/")+1);
				relativePath = relativePath.substring(0,relativePath.lastIndexOf("/"));
				relativePath = relativePath.substring(relativePath.indexOf("/")+1);
			} 
			// (c) In other case something went wrong..
			else {
				throw new NodeException("Could not find the relative path for music file");
			}
			
			// Go through music files
			for(int i=0;i<currentListOfFeatureFiles.size();i++) {
				
				// Create a folder for intermediate results of this file
				//relativePath = currentListOfFeatureFiles.get(i).get(0).substring(properties.getProperty(
					//		"featureDatabase").length()+1, currentListOfFeatureFiles.get(i).get(0).lastIndexOf("/"));
				File musicFileFolder = new File(outputFolder + "/" + relativePath);
				musicFileFolder.mkdirs();
				
				// Go through features
			    for(int j=0;j<currentListOfFeatureFiles.get(i).size();j++) {
			    	String output = musicFileFolder + "/" + 
			    		currentListOfFeatureFiles.get(i).get(j).substring(currentListOfFeatureFiles.get(i).get(j).lastIndexOf("/"));
					File feature_values_save_file = new File(output);
					if (feature_values_save_file.exists())
						if (!feature_values_save_file.canWrite()) {
							throw new NodeException("Cannot write to file with reduced features!");
						}
					if (!feature_values_save_file.exists())
						feature_values_save_file.createNewFile();
					
					FileOutputStream values_to = new FileOutputStream(feature_values_save_file);
					DataOutputStream values_writer = new DataOutputStream(values_to);
					String sep = System.getProperty("line.separator");
					
					// Read the feature values
					FileReader featuresInput = null;
					try {
						featuresInput = new FileReader(currentListOfFeatureFiles.get(i).get(j));
					} catch(FileNotFoundException e) {
						throw new NodeException("Could not find the file with extracted features: " + e.getMessage());
					}
					BufferedReader featuresReader = new BufferedReader(featuresInput);
					
					// Number of feature dimensions
					int featureDimensions = 0;
					
					// Write header and attributes
					String line = featuresReader.readLine();
					while(!line.equals(new String("@DATA")) && line != null) {
						
						// WindowNumber attribute name remains
						if(line.startsWith("@ATTRIBUTE WindowNumber")) {
							values_writer.writeBytes(line);
							values_writer.writeBytes(sep);
						} 
						// other attributes become zmuv_normalized()-prefix 
						else if(line.startsWith("@ATTRIBUTE")) {
							StringTokenizer t = new StringTokenizer(line," ");
							String attributeType = new String();
							while(t.hasMoreElements()) {
								attributeType = t.nextToken();
							}
							String oldAttributeName = line.substring(11,line.lastIndexOf(attributeType)-1);
							if(oldAttributeName.startsWith("'") && oldAttributeName.endsWith("'")) {
								oldAttributeName = oldAttributeName.substring(1,oldAttributeName.length()-1);
							}
							values_writer.writeBytes("@ATTRIBUTE 'Zmuv_normalized(" + oldAttributeName + ")' " + attributeType);
							values_writer.writeBytes(sep);
							featureDimensions++;
						} 
						// other lines remain
						else {
							values_writer.writeBytes(line);
							values_writer.writeBytes(sep);
						}
						line = featuresReader.readLine();
					}

					values_writer.writeBytes("@DATA");
					values_writer.writeBytes(sep);
			    	
					// Calculate mean and variance of the current feature
					ArrayList[] featureValues = new ArrayList[featureDimensions];
					for(int k=0;k<featureDimensions;k++) {
						featureValues[k] = new ArrayList<Double>();
					}
					Double[] means = new Double[featureDimensions];
					Double[] variances = new Double[featureDimensions];
					
					line = featuresReader.readLine();
					while(line != null) {
						StringTokenizer t = new StringTokenizer(line,",");
						for(int currentDimension = 0; currentDimension < featureDimensions; currentDimension++) {
							String nextValue = t.nextToken();
							if(!nextValue.equals(new String("NaN")) && !nextValue.equals(new String("?"))) {
								featureValues[currentDimension].add(new Double(nextValue));
							}
						}
						
						line = featuresReader.readLine();
					}
					
					for(int k=0;k<featureDimensions;k++) {
						double mean = 0.0d;
						for(int m=0;m<featureValues[k].size();m++) {
							mean += (Double)featureValues[k].get(m);
						}
						mean /= featureValues[k].size();
						
						double variance = 0.0d;
						for(int m=0;m<featureValues[k].size();m++) {
							variance += Math.pow((Double)featureValues[k].get(m)-mean,2);
						}
						variance /= featureValues[k].size();
						
						means[k] = mean;
						variances[k] = Math.sqrt(variance);
						if(variances[k] == 0) {
							variances[k] = Double.POSITIVE_INFINITY;
						}
					}
					
					// Replace values with normalized values
					featuresReader.close();
					featuresInput.close();
					featuresInput = new FileReader(currentListOfFeatureFiles.get(i).get(j));
					featuresReader = new BufferedReader(featuresInput);
					line = featuresReader.readLine();
					while(!line.equals(new String("@DATA")) && line != null) {
						line = featuresReader.readLine();
					}
					
					line = featuresReader.readLine();
					while(line != null) {
						StringTokenizer t = new StringTokenizer(line,",");
						int currentDimension = 0;
						while(t.hasMoreElements()) {
							String currentFeature = t.nextToken();
							if(currentDimension < featureDimensions) {
								Double featVal = new Double(currentFeature);
								featVal = (featVal - means[currentDimension]) / variances[currentDimension];
								currentFeature = featVal.toString();
							}
							currentDimension++;
							values_writer.writeBytes(currentFeature);
							if(t.hasMoreElements()) values_writer.writeBytes(",");
						}
						
						values_writer.writeBytes(sep);
						line = featuresReader.readLine();
					}
					
					values_writer.close();
					featuresReader.close();
					featuresInput.close();
			    }
			}
		} catch(IOException e) {
			throw new NodeException("Problem occured during normalization: " + e.getMessage());
		}
		
		// Update the list of features after tatum reduction
		// Go through music files
		for(int i=0;i<currentListOfFeatureFiles.size();i++) {
			for(int j=0;j<currentListOfFeatureFiles.get(i).size();j++) {
				String newPath = currentListOfFeatureFiles.get(i).get(j);
				newPath = outputFolder + "/" + relativePath +  
					newPath.substring(newPath.lastIndexOf("/"));
				currentListOfFeatureFiles.get(i).set(j, newPath);
			}
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...normalization succeeded");
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Do nothing, since initialization is not required
	}

	// FIXME Provide the code! 
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
	}
	
}
