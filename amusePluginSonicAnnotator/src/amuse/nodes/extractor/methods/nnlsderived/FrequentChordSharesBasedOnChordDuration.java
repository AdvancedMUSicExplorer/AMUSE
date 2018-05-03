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
 *  Creation date: 24.05.2012
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import amuse.interfaces.nodes.NodeException;


/**
 * This feature estimates the shares of 20%, 40% and 60% of the most frequent chords in 10 second intervals.
 * The frequency is calculated with regard to chord duration. 
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class FrequentChordSharesBasedOnChordDuration implements NNLSDerivedFeature {

	class ChordFrequency{
		String chord;
		Double sumOfDurations;
		public ChordFrequency(String chord, Double sumOfDurations) {
			this.chord = chord; 
			this.sumOfDurations = sumOfDurations;
		}
	}
	class ChordComparator implements Comparator<ChordFrequency> {  
        public int compare(ChordFrequency o1,ChordFrequency o2)  {
        	
        	// Sort due to the number of corresponding durations
        	if(o1.sumOfDurations != o2.sumOfDurations) {
        		return o2.sumOfDurations.compareTo(o1.sumOfDurations);
        	} else {
        		return o1.chord.compareTo(o2.chord);
        	}
        }  
    }; 
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.methods.nnlsderived.NNLSDerivedFeature#extractFeature(java.lang.String, java.lang.String)
	 */
	public void extractFeature(String sourceFile, String targetFile) throws NodeException {
		
		ArrayList<ChordFrequency> allChordsFreq = new ArrayList<ChordFrequency>();
		HashMap<String,Double> chord2Duration = new HashMap<String,Double>();
				
		FileReader featuresInput = null;
		BufferedReader featuresReader = null;
		try {
			featuresInput = new FileReader(new File(sourceFile));
			featuresReader = new BufferedReader(featuresInput);
		
			String currentLine = featuresReader.readLine();
			StringTokenizer str = new StringTokenizer(currentLine, ",");
			Double time = new Double(str.nextToken());
			String chord = str.nextToken();
			Double nextTime;
			String nextChord;
			while((currentLine = featuresReader.readLine()) != null) {
				str = new StringTokenizer(currentLine, ",");
				nextTime = new Double(str.nextToken());
				nextChord = str.nextToken();
				
				if(!chord2Duration.containsKey(chord)) {
					chord2Duration.put(chord, nextTime - time);
				} else {
					double d = chord2Duration.get(chord);
					chord2Duration.remove(chord);
					chord2Duration.put(chord, d + (nextTime - time));
				}
				time = nextTime; chord = nextChord;
			}
			featuresReader.close();
		} catch(IOException e) {
			throw new NodeException("Error reading the feature file '" + sourceFile + "': " + e.getMessage());
		}
		finally{
			if(featuresReader != null){
				try {
					featuresReader.close();
				} catch (IOException e) {
					// The program can still continue, unclosed Streams may only effect deleting operations
				}
			}
			if(featuresInput != null){
				try {
					featuresInput.close();
				} catch (IOException e) {
					// The program can still continue, unclosed Streams may only effect deleting operations
				}
			}
		}
		
		Iterator<String> iterator = chord2Duration.keySet().iterator();
	    while(iterator.hasNext()){
	    	String w = iterator.next();
	    	allChordsFreq.add(new ChordFrequency(w,chord2Duration.get(w)));
        }
	    ChordComparator comp = new ChordComparator();
        Collections.sort(allChordsFreq,comp);
        
        // Calculate the sum of all chord durations and boundaries
        double allDurations = 0d;
        for(int i=0;i<allChordsFreq.size();i++) {
        	allDurations += allChordsFreq.get(i).sumOfDurations;
        }
        double boundary20Percent = allDurations / 5;
        double boundary40Percent = boundary20Percent * 2;
        double boundary60Percent = boundary20Percent * 3;
        
        // Estimate the chords which are existing in 20, 40 and 60 percent of most frequent durations
        ArrayList<String> chords20 = new ArrayList<String>();
        ArrayList<String> chords40 = new ArrayList<String>();
        ArrayList<String> chords60 = new ArrayList<String>();
        
        double currSum = 0;
        for(int i=0;i<allChordsFreq.size();i++) {
        	if(currSum <= boundary20Percent) {
        		chords20.add(allChordsFreq.get(i).chord);
        		chords40.add(allChordsFreq.get(i).chord);
        		chords60.add(allChordsFreq.get(i).chord);
        	} else if(currSum <= boundary40Percent) {
        		chords40.add(allChordsFreq.get(i).chord);
        		chords60.add(allChordsFreq.get(i).chord);
        	} else if(currSum <= boundary60Percent) {
        		chords60.add(allChordsFreq.get(i).chord);
        	} else {
        		break;
        	}
        	currSum += allChordsFreq.get(i).sumOfDurations;
        }
		
		// Calculate the shares of the most frequent chords
        ArrayList<Double> shareOf20PercentFrequentChords = new ArrayList<Double>();
        ArrayList<Double> shareOf40PercentFrequentChords = new ArrayList<Double>();
        ArrayList<Double> shareOf60PercentFrequentChords = new ArrayList<Double>();
		HashMap<String,Double> chord2DurationIn10Sec = new HashMap<String,Double>();
				
		ArrayList<String> chords = new ArrayList<String>();
		ArrayList<Double> startTimes = new ArrayList<Double>();
		try {
			featuresInput = new FileReader(new File(sourceFile));
			featuresReader = new BufferedReader(featuresInput);
			String currentLine = null;
			while((currentLine = featuresReader.readLine()) != null) {
				StringTokenizer str = new StringTokenizer(currentLine, ",");
				startTimes.add(new Double(str.nextToken()));
				chords.add(str.nextToken());
			}
			featuresReader.close();
		} catch(IOException e) {
			throw new NodeException("Error reading the feature file '" + sourceFile + "': " + e.getMessage());
		}
		finally{
			if(featuresReader != null){
				try {
					featuresReader.close();
				} catch (IOException e) {
					// The program can still continue, unclosed Streams may only effect deleting operations
				}
			}
			if(featuresInput != null){
				try {
					featuresInput.close();
				} catch (IOException e) {
					// The program can still continue, unclosed Streams may only effect deleting operations
				}
			}
		}
		
		double lowerBound = 0d;
		double upperBound = 10d;
		int currentChord = 0;
		while(upperBound < startTimes.get(startTimes.size()-1)) {
			
			// For the position of the first chord in the current interval (from the overall chord sequence)
			int c;
			
			// Go through chords which may be in the current 10s interval
			for(c=currentChord;c<chords.size();c++) {
				
				// Chord starts before the lower interval boundary?
				if(startTimes.get(currentChord) <= lowerBound) {
					// Chord finishes before the upper boundary?
					if(startTimes.get(currentChord+1) < upperBound) {
						if(!chord2DurationIn10Sec.containsKey(chords.get(currentChord))) {
							chord2DurationIn10Sec.put(chords.get(currentChord), startTimes.get(currentChord+1) - lowerBound);
						} else {
							double d = chord2DurationIn10Sec.get(chords.get(currentChord));
							chord2DurationIn10Sec.remove(chords.get(currentChord));
							chord2DurationIn10Sec.put(chords.get(currentChord), d + (startTimes.get(currentChord+1) - lowerBound));
						}
					} else {
						if(!chord2DurationIn10Sec.containsKey(chords.get(currentChord))) {
							chord2DurationIn10Sec.put(chords.get(currentChord), upperBound - lowerBound);
						} else {
							double d = chord2DurationIn10Sec.get(chords.get(currentChord));
							chord2DurationIn10Sec.remove(chords.get(currentChord));
							chord2DurationIn10Sec.put(chords.get(currentChord), d + (upperBound - lowerBound));
						}
					}
				} else if(startTimes.get(currentChord) > lowerBound && startTimes.get(currentChord) <= upperBound) {
					if(startTimes.get(currentChord+1) <= upperBound) {
						if(!chord2DurationIn10Sec.containsKey(chords.get(currentChord))) {
							chord2DurationIn10Sec.put(chords.get(currentChord), startTimes.get(currentChord+1) - startTimes.get(currentChord));
						} else {
							double d = chord2DurationIn10Sec.get(chords.get(currentChord));
							chord2DurationIn10Sec.remove(chords.get(currentChord));
							chord2DurationIn10Sec.put(chords.get(currentChord), d + (startTimes.get(currentChord+1) - startTimes.get(currentChord)));
						}
					} else {
						if(!chord2DurationIn10Sec.containsKey(chords.get(currentChord))) {
							chord2DurationIn10Sec.put(chords.get(currentChord), upperBound - startTimes.get(currentChord));
						} else {
							double d = chord2DurationIn10Sec.get(chords.get(currentChord));
							chord2DurationIn10Sec.remove(chords.get(currentChord));
							chord2DurationIn10Sec.put(chords.get(currentChord), d + (upperBound - startTimes.get(currentChord)));
						}
					}
				} else {
					break;
				}
				
				currentChord++;
			}
			
			// Save the values
			double share20 = 0;
			double share40 = 0;
			double share60 = 0;
			iterator = chord2DurationIn10Sec.keySet().iterator();
		    while(iterator.hasNext()){
		    	String s = iterator.next();
		    	if(chords20.contains(s)) {
		    		share20 += chord2DurationIn10Sec.get(s);
		    	}
		    	if(chords40.contains(s)) {
		    		share40 += chord2DurationIn10Sec.get(s);
		    	}
		    	if(chords60.contains(s)) {
		    		share60 += chord2DurationIn10Sec.get(s);
		    	}
		    }
		    shareOf20PercentFrequentChords.add(share20 / 10d);
		    shareOf40PercentFrequentChords.add(share40 / 10d);
		    shareOf60PercentFrequentChords.add(share60 / 10d);
			
			// Search for the next currentChord which starts before the new lower bound
			lowerBound += 10;
			upperBound += 10;
			int k=c;
			for(k = 0;k<chords.size();k++) {
				if(startTimes.get(k) > lowerBound) {
					break;
				}
			}
			currentChord = k-1;
			chord2DurationIn10Sec = new HashMap<String,Double>();
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
			values_writer.writeBytes("%rows=" + shareOf20PercentFrequentChords.size());
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%columns=4");
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%sample_rate=" + "22050"); // TODO set this value correctly
			values_writer.writeBytes(sep);
			values_writer.writeBytes("%window_size=" + "220500");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			
			// Create an attribute vector with Amuse feature names
			values_writer.writeBytes("@ATTRIBUTE 'Shares of the most frequent 20, 40 and 60 percents of chords with regard to their duration' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'Shares of the most frequent 20, 40 and 60 percents of chords with regard to their duration' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE 'Shares of the most frequent 20, 40 and 60 percents of chords with regard to their duration' NUMERIC" + sep);
			values_writer.writeBytes("@ATTRIBUTE WindowNumber NUMERIC");
			values_writer.writeBytes(sep);
			values_writer.writeBytes(sep);
			
			// Write to the ARFF feature file (data)
			values_writer.writeBytes("@DATA");
			values_writer.writeBytes(sep);
			for(int i=0;i<shareOf20PercentFrequentChords.size();i++) {
				values_writer.writeBytes(shareOf20PercentFrequentChords.get(i) + "," + 
						shareOf40PercentFrequentChords.get(i) + "," + shareOf60PercentFrequentChords.get(i) + "," + (i+1) + sep);
			}
			values_writer.close();
			values_to.close();
		} catch(IOException e) {
			throw new NodeException("Error writing to the AMUSE feature file '" + targetFile + "':" + e.getMessage());
		}
		
		
	}

}
