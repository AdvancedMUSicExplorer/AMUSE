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
 * Creation date: 19.01.2010
 */
package amuse.data.datasets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetException;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.extractor.ExtractionConfiguration;

/**
 * This class represents a list of extraction tasks as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id$
 */
public class ExtractorConfigSet extends AbstractArffExperimentSet {

	// Strings which describe ARFF attributes
	private static final String strMusicFileList = "MusicFileList";
    private static final String strFeatureTable = "FeatureTable";
    
    // ARFF attributes
    private final StringAttribute musicFileListAttribute;
    private final StringAttribute featureTableAttribute;

    private String description = "";

    public ExtractorConfigSet(DataSetAbstract dataSet) throws DataSetException {
        super(dataSet.getName());
        // Check preconditions:
        dataSet.checkStringAttribute(strMusicFileList);
        dataSet.checkStringAttribute(strFeatureTable);
        musicFileListAttribute = (StringAttribute) dataSet.getAttribute(strMusicFileList);
        featureTableAttribute = (StringAttribute) dataSet.getAttribute(strFeatureTable);
        addAttribute(musicFileListAttribute);
        addAttribute(featureTableAttribute);
    }

    public List<File> getFeatureTables() {
        List<File> featureTables = new ArrayList<File>();
        for (int i = 0; i < featureTableAttribute.getValueCount(); i++) {
	    featureTables.add(new File(featureTableAttribute.getValueAt(i)));
	}
        return featureTables;
    }

    public List<File> getMusicFileLists() {
        List<File> musicFileLists = new ArrayList<File>();
        for (int i = 0; i < musicFileListAttribute.getValueCount(); i++) {
	    musicFileLists.add(new File(musicFileListAttribute.getValueAt(i)));
	}
        return musicFileLists;
    }

    /**
     * Creates a new ExtractorConfigSet from a file. Validates if the given file contains a ExtractorConfigSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid ExtractorConfigSet.
     */
    public ExtractorConfigSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        checkStringAttribute(strMusicFileList);
        checkStringAttribute(strFeatureTable);
        musicFileListAttribute = (StringAttribute) this.getAttribute(strMusicFileList);
        featureTableAttribute = (StringAttribute) this.getAttribute(strFeatureTable);
    }

    public ExtractorConfigSet(List<File> musicFileLists, List<File> featureTableLists) {
        super("ExtractorConfig");
        List<String> musicFileL = new ArrayList<String>();
        List<String> featureTablesL = new ArrayList<String>();
        for (File f : musicFileLists) {
            musicFileL.add(f.getAbsolutePath());
        }
        for (File f : featureTableLists) {
            featureTablesL.add(f.getAbsolutePath());
        }
        musicFileListAttribute = new StringAttribute(strMusicFileList, musicFileL);
        featureTableAttribute = new StringAttribute(strFeatureTable, featureTablesL);
        this.addAttribute(musicFileListAttribute);
        this.addAttribute(featureTableAttribute);
    }

    public ExtractorConfigSet(File musicFileListFile, File featureTableListFile) {
        super("ExtractorConfig");
        List<String> musicFileL = new ArrayList<String>();
        List<String> featureTablesL = new ArrayList<String>();
        musicFileL.add(musicFileListFile.getAbsolutePath());
        featureTablesL.add(featureTableListFile.getAbsolutePath());
        musicFileListAttribute = new StringAttribute(strMusicFileList, musicFileL);
        featureTableAttribute = new StringAttribute(strFeatureTable, featureTablesL);
        this.addAttribute(musicFileListAttribute);
        this.addAttribute(featureTableAttribute);
    }

    public String getType() {
        return "Feature Extraction";
    }

    public String getDescription() {
        int musicFiles = 0;
        int featureCount = 0;
        if (description.equals("")) {
            try {
                musicFiles = new FileTableSet(new File(musicFileListAttribute.getValueAt(0))).getValueCount();
            } catch (IOException ex) {
                description = "WARNING: Music file table seems to be broken.";
                return description;
            }
            try {
                featureCount = new FeatureTableSet(new File(featureTableAttribute.getValueAt(0))).getValueCount();
            } catch (IOException ex) {
                description = "WARINING: Feature table seems to be broken.";
                return description;
            }
            description = musicFiles+ " music file(s) and " + featureCount + " feature(s) seleced";
        }
        return description;
    }

    @Override
    public TaskConfiguration[] getTaskConfiguration() {
    	try {
    		return ExtractionConfiguration.loadConfigurationsFromDataSet(this);
    	} catch (IOException ex) {
    		throw new RuntimeException(ex);
    	}
    }

	/**
	 * @return the musicFileListAttribute
	 */
	public StringAttribute getMusicFileListAttribute() {
		return musicFileListAttribute;
	}

	/**
	 * @return the featureTableAttribute
	 */
	public StringAttribute getFeatureTableAttribute() {
		return featureTableAttribute;
	}
}
