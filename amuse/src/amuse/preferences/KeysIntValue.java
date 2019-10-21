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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;

/**
 * This enumeration lists all available keys to get presistent Integers from <code>AmusePreferences</code>.
 * @author Clemens Waeltken
 * @version $Id$
 */
public enum KeysIntValue {

    MAX_NUMBER_OF_TASK_THREADS,
    NUMBER_OF_JOBS_PER_GRID_MACHINE,
    GUI_LOG_LEVEL,
    SPLIT_SIZE_IN_KB,
    DOWNSAMPLING_TARGET_SIZE_IN_HZ,
    AUDIOSPECTRUM_WINDOWSIZE,
    AUDIOSPECTRUM_HOPSIZE,
    YALE_HEAP_SIZE;

    /**
     * This method is used to determin and get default values for any key.
     * @return the default value for this key.
     */
    protected int getDefaultValue() {
        switch (this) {
        case AUDIOSPECTRUM_WINDOWSIZE:
        	return 1;
        case AUDIOSPECTRUM_HOPSIZE:
        	return 1;
        case MAX_NUMBER_OF_TASK_THREADS:
            return 1;
        case NUMBER_OF_JOBS_PER_GRID_MACHINE:
        	return 1;
        case GUI_LOG_LEVEL:
            return 0;
        case SPLIT_SIZE_IN_KB:
            return 1024 * 20;
        case DOWNSAMPLING_TARGET_SIZE_IN_HZ:
            return 1;
        case YALE_HEAP_SIZE:
        	return 2000;
        default:
            AmuseLogger.write(this.getClass().getName(), Level.DEBUG, this.toString() + ": no default value set!");
            return 0;
        }
    }

    /**
     * This method checks if the given value would be valid for this key.
     * @param value the to be checked.
     * @return true if the value is valid for this key.
     */
    public boolean isValid(int value) {
        switch (this) {
        case AUDIOSPECTRUM_WINDOWSIZE:
        	return value >= 0 && value < 3;
        case AUDIOSPECTRUM_HOPSIZE:
        	return value >= 0 && value < 3;
        case MAX_NUMBER_OF_TASK_THREADS:
            return value >= 1;
        case NUMBER_OF_JOBS_PER_GRID_MACHINE:
        	return value >= 1;
        case GUI_LOG_LEVEL:
            return value >= 0 && value < 3;
        case SPLIT_SIZE_IN_KB:
            return value >= 1;
        case DOWNSAMPLING_TARGET_SIZE_IN_HZ:
            return (value == 0 || value == 1 || value == 2);
        case YALE_HEAP_SIZE:
        	return value >= 1;
        default:
            AmuseLogger.write(this.getClass().getName(), Level.DEBUG, this.toString() + ": no validator available!");
            return true;
        }
    }

    @Override
    public String toString() {
        return "INT_" + super.toString();
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
		map.put(MAX_NUMBER_OF_TASK_THREADS.toString(), "Maximum number of parallel task threads:");
		map.put(NUMBER_OF_JOBS_PER_GRID_MACHINE.toString(), "Number of Amuse jobs to proceed on one grid machine (one grid job):");
		map.put(GUI_LOG_LEVEL.toString(), "Log level of GUI (0 == Debug, 1 == Info, 2 == Quiet)");
		map.put(SPLIT_SIZE_IN_KB.toString(), "Size in KB to split music files at.");
        map.put(DOWNSAMPLING_TARGET_SIZE_IN_HZ.toString(), "Target sampling rate of wave file. (0 = 44000Hz, 1 = 22050Hz, 2 = 11025HZ)");
		map.put(AUDIOSPECTRUM_WINDOWSIZE.toString(), "Window size used for the calculation of the audiospectrum in the annotation editor. (0 = 256, 1 = 512, 2 = 1024)");
		map.put(AUDIOSPECTRUM_HOPSIZE.toString(), "Hop size used for the calculation of the audiospectrum in the annotation editor. (0 = 256, 1 = 512, 2 = 1024)");
		map.put(YALE_HEAP_SIZE.toString(), "eap size in megabytes for Yale feature extractor (should be increased for long music files)");
		return map;
	}

}
