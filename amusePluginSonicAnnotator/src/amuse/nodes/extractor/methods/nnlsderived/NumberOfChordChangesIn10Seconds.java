/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2012 by code authors
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
 *  Creation date: 31.05.2012
 */ 
package amuse.nodes.extractor.methods.nnlsderived;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import amuse.interfaces.nodes.NodeException;

/**
 * This feature estimates the number of chord changes in 10 second intervals 
 * 
 * @author Igor Vatolkin / Daniel Stoller
 * @version $Id: $
 */
public class NumberOfChordChangesIn10Seconds implements NNLSDerivedFeature {

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.methods.nnlsderived.NNLSDerivedFeature#extractFeature(java.lang.String, java.lang.String)
	 */
	public void extractFeature(String sourceFile, String targetFile) throws NodeException {
		
		ArrayList<Integer> diffChordNumber = new ArrayList<Integer>();
		
		FileReader featuresInput = null;
		BufferedReader featuresReader = null;
		try {
			featuresInput = new FileReader(new File(sourceFile));
			featuresReader = new BufferedReader(featuresInput);
		
		
			int nextBoundary = 10;
			int currentChordChangeNumber = 0;
			
			String currentLine = null;
			while((currentLine = featuresReader.readLine()) != null) {
				StringTokenizer str = new StringTokenizer(currentLine, ",");
				Double time = new Double(str.nextToken());
				String chord = str.nextToken();
				
				// Check if the next boundary is achieved
				if(time > nextBoundary) {
					
					// If no new chords were estimated in the current 10 seconds, set the number of the different chords to 1
					diffChordNumber.add(currentChordChangeNumber);
					nextBoundary += 10;
					
					// If no chord changes are existing for a longer time..
					while(nextBoundary < time) {
						diffChordNumber.add(0);
						nextBoundary += 10;
					}
					currentChordChangeNumber = 0;
					if(!chord.equals("\"N\"")) {
						currentChordChangeNumber++;
					}
				} else {
					
					// Add the chord?
					if(!chord.equals("\"N\"")) {
						currentChordChangeNumber++;
					}
				}
			}
			featuresReader.close();
		} catch(IOException e) {
			throw new NodeException("Error reading the feature file '" + sourceFile + "': " + e.getMessage());
		}
		
		// Save the feature file
		try {
			File feature_values_save_file = new File(targetFile);
			if (feature_values_save_file.exists())
				if (!feature_values_save_file.canWrite()) {
					throw new NodeException("Error creating the AMUSE feature file '" + targetFile + "'");
				}
			if (!feature_values_save_file.exists())
				feature_values_save_file.createNewFile();
			FileOutputStream values_to = new FileOutputStream(feature_values_save_file);
			DataOutputStream values_writer = new DataOutputStream(values_to);
			String sep = System.getProperty("line.separator");
			
			// Write to the ARFF feature file (header)
			values_writer.writeBytes("@RELATION 'Music feature'");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%rows=" + diffChordNumber.size());
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%columns=2");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%sample_rate=" + "22050"); // TODO set this value correctly
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%window_size=" + "220500");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			
			// Create an attribute vector with Amuse feature names
			values_writer.writeBytes("@ATTRIBUTE 'Number of chord changes in 10s' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE WindowNumber NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			
			// Write to the ARFF feature file (data)
			values_writer.writeBytes("@DATA");
			values_writer.writeBytes(sep);
			for(int i=0;i<diffChordNumber.size();i++) {
				values_writer.writeBytes(diffChordNumber.get(i) + "," + (i+1) + sep);
			}
			values_writer.close();
		} catch(IOException e) {
			throw new NodeException("Error writing to the AMUSE feature file '" + targetFile + "':" + e.getMessage());
		}
		
		
	}

}
