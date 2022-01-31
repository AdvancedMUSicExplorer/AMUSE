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
 * Creation date: 01.09.2008
 */
package amuse.preferences;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;

/**
 * This enumeration lists all available keys to get presistent Strings from <code>AmusePreferences</code>.
 * @author Clemens Waeltken
 * @version $Id$
 */
public enum KeysStringValue {
    AMUSE_PATH,
    AMUSE_VERSION,
    WORKSPACE,
    MUSIC_DATABASE,
    SINGLE_TRACK_ANNOTATION_DATABASE,
    MULTIPLE_TRACKS_ANNOTATION_DATABASE,
    FEATURE_DATABASE,
    PROCESSED_FEATURE_DATABASE,
    MODEL_DATABASE, 
    MEASURE_DATABASE, 
    OPTIMIZATION_DATABASE,
    GRID_SCRIPT_EXTRACTOR, 
    GRID_SCRIPT_PROCESSOR, 
    GRID_SCRIPT_TRAINER, 
    GRID_SCRIPT_CLASSIFIER, 
    GRID_SCRIPT_VALIDATOR, 
    GRID_SCRIPT_OPTIMIZER,
    GRID_SCRIPT_TOOL,
    JAVA_PATH, 
    MATLAB_PATH,
    PYTHON_PATH,
	LD_LIBRARY_PATH;

    /**
     * This method is used to determine and get default values for any key.
     * @return the default value for this key.
     */
    protected String getDefaultValue() {
    	
    	// Currently no default values for string keys are available!
    	switch (this) {
	    case JAVA_PATH:
	    	return "java";
	    case MATLAB_PATH:
	    	return "matlab";
	    case PYTHON_PATH:
	    	return "python3";
        default:
            //AmuseLogger.write(this.getClass().getName(), Level.DEBUG, this.toString() + ": no default value set!");
        return getNoValue();
        }
    }

    /**
     * This method checks if the given value would be valid for this key.
     * @param value the to be checked.
     * @return true if the value is valid for this key.
     */
    public boolean isValid(String value) {
        switch (this) {
            case GRID_SCRIPT_CLASSIFIER:
            case GRID_SCRIPT_EXTRACTOR:
            case GRID_SCRIPT_OPTIMIZER:
            case GRID_SCRIPT_TOOL:
            case GRID_SCRIPT_PROCESSOR:
            case GRID_SCRIPT_TRAINER:
            case GRID_SCRIPT_VALIDATOR:
                return !value.equalsIgnoreCase("NO_VALUE");
            case WORKSPACE:
            case MEASURE_DATABASE:
            case MUSIC_DATABASE:
            case MULTIPLE_TRACKS_ANNOTATION_DATABASE:
            case SINGLE_TRACK_ANNOTATION_DATABASE:
            case FEATURE_DATABASE:
            case PROCESSED_FEATURE_DATABASE:
            case MODEL_DATABASE:
            case OPTIMIZATION_DATABASE:
                return new File(value).isDirectory();
            case AMUSE_PATH:
                // Check if it is actually a Directory.
                File amuseFolder = new File(value);
                if (!amuseFolder.isDirectory()) {
                    return false;
                }
                // Maybe no need for this in future release.
                File amusePreferences = new File(amuseFolder.getAbsolutePath() + File.separator + "config" + File.separator + "amuse.properties");
                if (!amusePreferences.isFile()) {
                    AmuseLogger.write(this.getClass().toString(), Level.DEBUG, "File amuse.properties not found in config-folder of Amuse!");
                    return false;
                }
                return true;

	    case JAVA_PATH:
		return true;
	    case MATLAB_PATH:
	    	return true;
	    case PYTHON_PATH:
		return true;
	    case LD_LIBRARY_PATH:
		return true;
            default:
                AmuseLogger.write(this.getClass().getName(), Level.DEBUG, this.toString() + ": no validator available!");
                // If NoValue String is passed return false otherwise return true.
                boolean b = true;
                if (value.equals(getNoValue())) {
                    b = false;
                }
                return b;
        }
    }

    @Override
    public String toString() {
        return "STRING_" + super.toString();
    }

    private String getNoValue() {
        return "NO_VALUE";
    }

	/**
	 * @param key String representation of a KeysStringValue element.
	 * @return The comment for this key.
	 */
	public static String getCommentFor(String key) {
		return commentsMap.get(key);
	}
	
	private static Map<String, String> commentsMap = getCommentsMap();
	
	/**
	 * This map holds all comments for each Key.
	 * @return
	 */
	private static Map<String, String> getCommentsMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(AMUSE_PATH.toString(), "Absolute path to AMUSE folder:");
		map.put(AMUSE_VERSION.toString(), "Amuse version:");
		map.put(MUSIC_DATABASE.toString(), "Absolute path to database folder with music files:");
		map.put(SINGLE_TRACK_ANNOTATION_DATABASE.toString(), "Absolute path to database folder with annotations for a single tack:");
		map.put(MULTIPLE_TRACKS_ANNOTATION_DATABASE.toString(), "Absolute path to database folder with annotations for multiple tacks:");
		map.put(FEATURE_DATABASE.toString(), "Absolute path to database folder with features:");
		map.put(OPTIMIZATION_DATABASE.toString(), "Absolute path to database folder with optimization results:");
		map.put(PROCESSED_FEATURE_DATABASE.toString(), "Absolute path to database folder with processed features:");
		map.put(MEASURE_DATABASE.toString(), "Absolute path to database folder with validation results:");
		map.put(MODEL_DATABASE.toString(), "Absolute path to database folder with classification models:");
		map.put(GRID_SCRIPT_CLASSIFIER.toString(), "Batch command to proceed classifier task in grid:");
		map.put(GRID_SCRIPT_EXTRACTOR.toString(), "Batch command to proceed feature extractor task in grid:");
		map.put(GRID_SCRIPT_OPTIMIZER.toString(), "Batch command to proceed optimizer task in grid:");
		map.put(GRID_SCRIPT_TOOL.toString(), "Batch command to proceed tool task in grid:");
		map.put(GRID_SCRIPT_PROCESSOR.toString(), "Batch command to proceed feature processor task in grid:");
		map.put(GRID_SCRIPT_TRAINER.toString(), "Batch command to proceed classification trainer task in grid:");
		map.put(GRID_SCRIPT_VALIDATOR.toString(), "Batch command to proceed validator task in grid:");
		map.put(JAVA_PATH.toString(), "Path to your local java executable:");
		map.put(MATLAB_PATH.toString(), "Path to your local Matlab executable:");
		map.put(PYTHON_PATH.toString(), "Path to your local Python3 executable:");
		map.put(LD_LIBRARY_PATH.toString(), "Python library path:");
		map.put(WORKSPACE.toString(), "Absolute path to amuse workspace");
		return map;
	}
}
