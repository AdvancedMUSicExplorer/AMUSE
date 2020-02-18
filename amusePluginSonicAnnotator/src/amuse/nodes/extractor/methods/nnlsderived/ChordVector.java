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
public class ChordVector implements NNLSDerivedFeature {

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.methods.nnlsderived.NNLSDerivedFeature#extractFeature(java.lang.String, java.lang.String)
	 */
	public void extractFeature(String sourceFile, String targetFile) throws NodeException {
		
		ArrayList<double[]> chordList = new ArrayList<double[]>();
		
		double windowSizeInSec = (1.0/22050.0)*512;
		
		
		FileReader featuresInput = null;
		BufferedReader featuresReader = null;
		try {
			featuresInput = new FileReader(new File(sourceFile));
			featuresReader = new BufferedReader(featuresInput);
		
			
			
			double nextBoundary = windowSizeInSec;
			
			double c = 0;
			double db = 0;
			double d = 0;
			double eb = 0;
			double e = 0;
			double f = 0;
			double gb = 0;
			double g = 0;
			double ab = 0;
			double a = 0;
			double bb = 0;
			double b = 0;
			
			double major = 0;
			double minor = 0;
			double dim = 0;
			double aug = 0;
			
			double seven = 0;
			double maj7 = 0;
			double dim7 = 0;
			
			double six = 0;
			
			String currentLine = null;
			while((currentLine = featuresReader.readLine()) != null) {
				StringTokenizer str = new StringTokenizer(currentLine, ",");
				Double time = new Double(str.nextToken());
				String chord = str.nextToken();
				
				// Check if the next boundary is achieved
				if(time > nextBoundary) {
					
					// If no new chords were estimated in the current window, set the chord to the last found chord
					chordList.add(new double[]{c,db,d,eb,e,f,gb,g,ab,a,bb,b,major,minor,dim,aug,seven,maj7,dim7,six});
					nextBoundary += windowSizeInSec;
					
					// If no chord changes are existing for a longer time..
					while(nextBoundary < time) {
						chordList.add(new double[]{c,db,d,eb,e,f,gb,g,ab,a,bb,b,major,minor,dim,aug,seven,maj7,dim7,six});
						nextBoundary += windowSizeInSec;
					}
					//add the chord
					c = 0;
					db = 0;
					d = 0;
					eb = 0;
					e = 0;
					f = 0;
					gb = 0;
					g = 0;
					ab = 0;
					a = 0;
					bb = 0;
					b = 0;
					
					major = 0;
					minor = 0;
					dim = 0;
					aug = 0;
					
					seven = 0;
					maj7 = 0;
					dim7 = 0;
					
					six = 0;
					
					if(chord.startsWith("\"C")) {
						c = 1;
					}
					else if(chord.startsWith("\"C#")||chord.startsWith("\"Db")) {
						db = 1;
					}
					else if(chord.startsWith("\"D")) {
						d = 1;
					}
					else if(chord.startsWith("\"D#")||chord.startsWith("\"Eb")) {
						eb = 1;
					}
					else if(chord.startsWith("\"E")) {
						e = 1;
					}
					else if(chord.startsWith("\"F")) {
						f = 1;
					}
					else if(chord.startsWith("\"F#")||chord.startsWith("\"Gb")) {
						gb = 1;
					}
					else if(chord.startsWith("\"G")) {
						g = 1;
					}
					else if(chord.startsWith("\"G#")||chord.startsWith("\"Ab")) {
						ab = 1;
					}
					else if(chord.startsWith("\"A")) {
						a = 1;
					}
					else if(chord.startsWith("\"A#")||chord.startsWith("\"Bb")) {
						bb = 1;
					}
					else if(chord.startsWith("\"B")) {
						b = 1;
					}
					
					if(chord.contains("aug")) {
						aug = 1;
					}
					else if(chord.contains("dim")) {
						dim = 1;
					}
					else if(chord.contains("m")&&!chord.contains("maj7")) {
						minor = 1;
					}
					else if (!chord.equals("\"N\"")){
						major = 1;
					}
					
					if(chord.contains("dim7")) {
						dim7 = 1;
					}
					else if(chord.contains("maj7")) {
						maj7 = 1;
					}
					else if(chord.contains("7")) {
						seven = 1;
					}
					
					if(chord.contains("6")) {
						six = 1;
					}
					
				} else {
					//add the chord
					c = 0;
					db = 0;
					d = 0;
					eb = 0;
					e = 0;
					f = 0;
					gb = 0;
					g = 0;
					ab = 0;
					a = 0;
					bb = 0;
					b = 0;
					
					major = 0;
					minor = 0;
					dim = 0;
					aug = 0;
					
					seven = 0;
					maj7 = 0;
					dim7 = 0;
					
					six = 0;
					
					if(chord.startsWith("C")) {
						c = 1;
					}
					else if(chord.startsWith("C#")||chord.startsWith("Db")) {
						db = 1;
					}
					else if(chord.startsWith("D")) {
						d = 1;
					}
					else if(chord.startsWith("D#")||chord.startsWith("Eb")) {
						eb = 1;
					}
					else if(chord.startsWith("E")) {
						e = 1;
					}
					else if(chord.startsWith("F")) {
						f = 1;
					}
					else if(chord.startsWith("F#")||chord.startsWith("Gb")) {
						gb = 1;
					}
					else if(chord.startsWith("G")) {
						g = 1;
					}
					else if(chord.startsWith("g#")||chord.startsWith("ab")) {
						ab = 1;
					}
					else if(chord.startsWith("A")) {
						a = 1;
					}
					else if(chord.startsWith("A#")||chord.startsWith("Bb")) {
						bb = 1;
					}
					else if(chord.startsWith("B")) {
						b = 1;
					}
					
					if(chord.contains("aug")) {
						aug = 1;
					}
					else if(chord.contains("dim")) {
						dim = 1;
					}
					else if(chord.contains("m")&&!chord.contains("maj7")) {
						minor = 1;
					}
					else if(!chord.equals("\"N\"")) {
						major = 1;
					}
					
					if(chord.contains("dim7")) {
						dim7 = 1;
					}
					else if(chord.contains("maj7")) {
						maj7 = 1;
					}
					else if(chord.contains("7")) {
						seven = 1;
					}
					
					if(chord.contains("6")) {
						six = 1;
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
			values_writer.writeBytes("%rows=" + chordList.size());
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%columns=21");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%sample_rate=" + "22050"); // TODO set this value correctly
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%window_size=" + "512");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			
			// Create an attribute vector with Amuse feature names
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector C' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector Db' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector D' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector Eb' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector E' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector F' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector Gb' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector G' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector Ab' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector A' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector Bb' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector B' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector Major' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector Minor' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector Diminished' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector Augmented' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector minor seventh' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector major seventh' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector diminished seventh' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'ChordVector sixth' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE WindowNumber NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			
			// Write to the ARFF feature file (data)
			values_writer.writeBytes("@DATA");
			values_writer.writeBytes(sep);
			for(int i=0;i<chordList.size();i++) {
				double[] currentChordArray = chordList.get(i);
				
				for(int j = 0; j < currentChordArray.length; j++) {
					values_writer.writeBytes(currentChordArray[j] + ",");
				}
				
				values_writer.writeBytes("" + (i+1) + sep);
			}
			values_writer.close();
		} catch(IOException e) {
			throw new NodeException("Error writing to the AMUSE feature file '" + targetFile + "':" + e.getMessage());
		}
		
		
	}

}
