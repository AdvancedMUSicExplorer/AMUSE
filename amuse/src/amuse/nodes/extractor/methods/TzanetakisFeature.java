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
 * Creation date: 08.12.2010
 */
package amuse.nodes.extractor.methods;

import amuse.data.FeatureTable;
import amuse.data.io.ArffDataSet;
import amuse.data.io.attributes.NumericAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.divergences.MahalanobisDistance;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Level;


/**
 *
 * @author Clemens Wältken
 */
public class TzanetakisFeature extends AmuseTask implements ExtractorInterface {
    private File processedFeatureFile = null;
    private String musicDatabase = AmusePreferences.get(KeysStringValue.MUSIC_DATABASE);
    private String processedFeatureDatabase = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE);
    private String currentFile = null;
    private File outputFile = null;


    public void setParameters(String parameterString) throws NodeException {
	// Nothing to do.
    }

    public void initialize() throws NodeException {
	// Nothing to do.
    }

    public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
	File file = new File(musicFile);
	currentFile = file.getName().substring(0, file.getName().lastIndexOf('.'));
	outputFile = new File(outputFeatureFile);
    }

    public void convertBaseScript(HashMap<Integer, Integer> feature2Tool, FeatureTable featureTable) throws NodeException {
	// Nothing to do.
    }

    public void extractFeatures() throws NodeException {
	try {
	    // Locate According File in Processed Feature Database:
	    ExtractionConfiguration conf = (ExtractionConfiguration)this.getCorrespondingScheduler().getConfiguration();
	    List<String> fileList = conf.getMusicFileList().getFiles();
	    for (String s : fileList) {
		if (s.contains(currentFile)) {
		    s = s.replace(musicDatabase, processedFeatureDatabase);
		    s = s.substring(0, s.lastIndexOf('.'));
		    File f = new File(s);
		    processedFeatureFile = new File(f.getAbsolutePath() + '/' + f.getName()+ "___0[true_true]__1000ms_1000ms.arff");
		}
	    }
	    if (processedFeatureFile == null || !processedFeatureFile.exists()) {
		AmuseLogger.write(TzanetakisFeature.class.getName(), Level.WARN, "Did not find Processed Feature File for: " + currentFile);
	    }
	    // Read Data.
	    ArffDataSet features = new ArffDataSet(processedFeatureFile, 1000);
	    int rows = features.getValueCount();
	    int columns = features.getAttributeCount()-3;
	    double[][] data = new double[rows][columns];
	    for (int x = 0; x < rows; x++) {
		for (int y = 0; y < columns; y++) {
		    NumericAttribute a = (NumericAttribute) features.getAttribute(y);
		    data[x][y] = a.getValueAt(x);
		}
	    }
	    ExampleSet exampleSet = ExampleSetFactory.createExampleSet(data);

	    // Calculate distances.
	    MahalanobisDistance ditanceMesurer = new MahalanobisDistance();
	    ditanceMesurer.init(exampleSet);
	    double[] distances = new double[rows - 1];
	    for (int i = 0; i < rows-1; i++) {
		distances[i] = ditanceMesurer.calculateDistance(data[i], data[i+1]);
//		System.out.println(distances[i]);
	    }
	    double[] derivedDistances = new double[rows - 2];
	    for (int i = 0; i < rows -2 ; i++) {
		derivedDistances[i] = distances[i+1] - distances[i]; // Vorwärtsgradient
//		System.out.println(derivedDistances[i]);
	    }

	    // Select peaks:
	    int peaks = 5;
	    int peaksFound = 0;
	    int envSize = derivedDistances.length / peaks / 5;
	    int peakPositions[] = new int[peaks];
	    while (peaksFound < peaks) {
		int pos = 0;
		double cMax = Math.abs(derivedDistances[pos]);
		for (int i = 0; i < derivedDistances.length - 1; i++) {
		    if (Math.abs(derivedDistances[i]) > Math.abs(cMax)) {
			pos = i;
			cMax = Math.abs(derivedDistances[i]);
		    }
		}
		peakPositions[peaksFound] = pos;
		System.out.println(peaksFound + ": " + cMax + ", " + pos);
		// Zero out peaks environment to prevent multiple selection of same peak:
		for (int x = pos - envSize / 2; x < pos + envSize / 2; x++) {
		    if (x >= 0 && x < derivedDistances.length)
			derivedDistances[x] = 0f;
		}
		peaksFound++;
	    }
	    // Write results:
	    ArffDataSet set = new ArffDataSet("Tzanetakis Feature");
	    ArrayList<Integer> peakList = new ArrayList<Integer>();
	    for (int i : peakPositions) {
		peakList.add(i);
	    }
	    Collections.sort(peakList);
	    NumericAttribute peakAttribute = NumericAttribute.createFromIntList("Peaks:", peakList);
	    set.addAttribute(peakAttribute);
	    outputFile.getParentFile().mkdirs();
	    set.saveToArffFile(outputFile);
	    System.out.println("TAAADAAA");
	    set.saveToArffFile(new File("/Users/waeltken/Desktop/TzanetakisResult.arff"));
	} catch (IOException ex) {
	    AmuseLogger.write(TzanetakisFeature.class.getName(), Level.ERROR, ex.getMessage());
	} catch (OperatorException ex) {
	    AmuseLogger.write(TzanetakisFeature.class.getName(), Level.ERROR, ex.getMessage());
	} finally {
	    processedFeatureFile = null;
	}
    }

    public void convertOutput() throws NodeException {
	// Nothing to do.
    }

}
