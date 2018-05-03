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
 *  Creation date: 03.06.2012
 */ 
package amuse.nodes.extractor.methods.nnlsderived;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

import amuse.interfaces.nodes.NodeException;

/**
 * This feature estimates the strengths of 12 music intervals from minor second to octave. Only the highest semitone
 * amplitudes above 3/4 amplitude of the strongest peak are taken into account from each frame and the strength of the 
 * corresponding interval is measured by the minimum of the candidate amplitudes (distances between all of candidate 
 * semitones are compared).
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class IntervalStrengthsFromHighestSemitonePeaksRelatedToMaxPeak implements NNLSDerivedFeature {

	class SemitoneAmplitude{
		Integer semitone;
		Double amplitude;
		public SemitoneAmplitude(Integer semitone, Double amplitude) {
			this.semitone = semitone; 
			this.amplitude = amplitude;
		}
	}
	class SemitoneComparator implements Comparator<SemitoneAmplitude> {  
        public int compare(SemitoneAmplitude o1,SemitoneAmplitude o2)  {
        	
        	// Sort due to the number of corresponding amplitudes
        	if(o1.amplitude != o2.amplitude) {
        		return o2.amplitude.compareTo(o1.amplitude);
        	} else {
        		return o1.amplitude.compareTo(o2.amplitude);
        	}
        }  
    }; 
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.methods.nnlsderived.NNLSDerivedFeature#extractFeature(java.lang.String, java.lang.String)
	 */
	public void extractFeature(String sourceFile, String targetFile) throws NodeException {
		ArrayList<Double[]> intervalStrengthsFromHighestPeaks=new ArrayList<Double[]>();
		
		FileReader featuresInput = null;
		BufferedReader featuresReader = null;
		try {
			featuresInput = new FileReader(new File(sourceFile));
			featuresReader = new BufferedReader(featuresInput);
		
			String currentLine = null;
			while((currentLine = featuresReader.readLine()) != null) {
				StringTokenizer str = new StringTokenizer(currentLine, ",");
				ArrayList<SemitoneAmplitude> allSemitoneAmps = new ArrayList<SemitoneAmplitude>();
				
				// Omit the time
				str.nextToken();
				
				// Get all semitone amplitudes for the current frame 
				for(int i=0;i<84;i++) {
					allSemitoneAmps.add(new SemitoneAmplitude(i, new Double(str.nextToken())));
					
				}
				SemitoneComparator comp = new SemitoneComparator();
		        Collections.sort(allSemitoneAmps,comp);
		        
		        // Save the strengths of the intervals between semitone amplitudes which are higher than 3/4 of the maximum
		        // amplitude
		        double boundary = allSemitoneAmps.get(0).amplitude * 3d / 4d;
		        int numberOfPeaksToAnalyze = 0;
		        for(int i=0;i<allSemitoneAmps.size();i++) {
		        	if(allSemitoneAmps.get(i).amplitude < boundary) {
		        		break;
		        	} else {
		        		numberOfPeaksToAnalyze++;
		        	}
		        }
		        
		        Double[] strengthOfIntervalsInCurrentFrame = new Double[12];
		        for(int i=0;i<strengthOfIntervalsInCurrentFrame.length;i++) {
		        	strengthOfIntervalsInCurrentFrame[i] = 0d;
		        }
		        for(int sa1=0;sa1<numberOfPeaksToAnalyze;sa1++) {
		        	for(int sa2=sa1+1;sa2<numberOfPeaksToAnalyze;sa2++) {
		        		
		        		int difference = Math.abs(allSemitoneAmps.get(sa1).semitone - allSemitoneAmps.get(sa2).semitone);
		        		
		        		// Save the strengths of all intervals between minor second and perfect octave
		        		if(difference > 0 && difference <13) {
		        			strengthOfIntervalsInCurrentFrame[difference-1] += Math.min(allSemitoneAmps.get(sa1).amplitude, 
		        					allSemitoneAmps.get(sa2).amplitude);
		        		}
		        		
		        	}
		        }
		        intervalStrengthsFromHighestPeaks.add(strengthOfIntervalsInCurrentFrame);
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
			values_writer.writeBytes("%rows=" + intervalStrengthsFromHighestPeaks.size());
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%columns=2");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%sample_rate=" + "22050"); // TODO set this value correctly
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%window_size=" + "2048");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			
			// Create an attribute vector with Amuse feature names
			for(int j=0;j<intervalStrengthsFromHighestPeaks.get(0).length;j++) {
				values_writer.writeBytes("@ATTRIBUTE 'Interval strengths estimated from the semitone peaks above 3/4 of the maximum peak' NUMERIC" + sep);
			}
			values_writer.writeBytes("@ATTRIBUTE WindowNumber NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			
			// Write to the ARFF feature file (data)
			values_writer.writeBytes("@DATA");
			values_writer.writeBytes(sep);
			for(int i=0;i<intervalStrengthsFromHighestPeaks.size();i++) {
				for(int j=0;j<intervalStrengthsFromHighestPeaks.get(i).length;j++) {
					values_writer.writeBytes(intervalStrengthsFromHighestPeaks.get(i)[j] + ",");
				}
				values_writer.writeBytes((i+1) + sep);
			}
			values_writer.close();
		} catch(IOException e) {
			throw new NodeException("Error writing to the AMUSE feature file '" + targetFile + "':" + e.getMessage());
		}
		
		
	}

}
