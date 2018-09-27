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
 * Creation date: 24.08.2008
 */
package amuse.preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Vector;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;

/**
 * This class handles all persistent preferences of Amuse.
 * 
 * @author Clemens Waeltken
 * @version $Id$
 */
public class AmusePreferences {

    private static PropertyFileAdapter prefs;
    private static final Vector<PreferenceChangeListener> listeners = new Vector<PreferenceChangeListener>();

    private static void readyFileAdapter() {
        if (prefs != null)
            return;
        try {
        	String path = System.getenv("AMUSEHOME");
        	if(path == null){
        		try {
        			File file = new File(Level.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        			boolean contains = false;
        			do{
        				file = file.getParentFile();
        				for(String subPath: file.list()){
        					if(subPath.equals("src")){
        						contains = true;
        						break;
        					}
        				}
        			}while(!contains);
        			if(KeysStringValue.AMUSE_PATH.isValid(file.getAbsolutePath())){
        				path = file.getAbsolutePath();
        			}
        		} catch (URISyntaxException e) {
        			e.printStackTrace();
        		}
        	}
            prefs = new PropertyFileAdapter(new File(path + File.separator + "config" + File.separator + "amuse.properties"));
           	put(KeysStringValue.AMUSE_PATH, path);
        } catch (Exception e) {
            throw new RuntimeException("Could not load the preferences: " + e.getMessage());
        }
        preloadAllValues();
    }

    private static void preloadAllValues() {
        for (KeysStringValue key : KeysStringValue.values()) {
            put(key, get(key));
        }
        for (KeysBooleanValue key : KeysBooleanValue.values()) {
            putBoolean(key, getBoolean(key));
        }
        for (KeysIntValue key : KeysIntValue.values()) {
            putInt(key, getInt(key));
        }
    }

    /**
     * This method is used to get a <code>String</code> value according to a
     * given <b>key</b>.
     *
     * @param key
     *            a key as given by <code>KeysStringValue</code> enumeration.
     * @return the stored <code>String</code> or (if not available) a defalut
     *         value given by <code>getDefaultValue()</code> method of
     *         <code>KeysStringValue</code> enumeration.
     */
    public static String get(KeysStringValue key) {
        readyFileAdapter();
        return prefs.get(key.toString(), key.getDefaultValue());
    }

    /**
     * This method is used to store a <code>String</code> value under a given
     * <b>key</b>.
     *
     * @param key
     *            a key as given by <code>KeysStringValue</code> enumeration.
     * @param value
     *            a value to store under the given key.
     */
    public static void put(KeysStringValue key, String value) {
        readyFileAdapter();
        prefs.put(key.toString(), value);
        notifyListeners();
    }

    /**
     * This method is used to get a <code>int</code> value according to a given
     * <b>key</b>.
     *
     * @param key
     *            a key as given by <code>KeysIntValue</code> enumeration.
     * @return the stored <code>int</code> or (if not available) a defalut value
     *         given by <code>getDefaultValue()</code> method of
     *         <code>KeysIntValue</code> enumeration.
     */
    public static int getInt(KeysIntValue key) {
        readyFileAdapter();
        return prefs.getInt(key.toString(), key.getDefaultValue());
    }

    /**
     * This method is used to store a <code>int</code> value under a given
     * <b>key</b>.
     *
     * @param key
     *            a key as given by <code>KeysIntValue</code> enumeration.
     * @param value
     *            a value to store under the given key.
     */
    public static void putInt(KeysIntValue key, int value) {
        readyFileAdapter();
        prefs.putInt(key.toString(), value);
        notifyListeners();
    }

    /**
     * This method is used to get a <code>boolean</code> value according to a
     * given <b>key</b>.
     *
     * @param key
     *            a key as given by <code>KeysBooleanValue</code> enumeration.
     * @return the stored <code>boolean</code> or (if not available) a defalut
     *         value given by <code>getDefaultValue()</code> method of
     *         <code>KeysBooleanValue</code> enumeration.
     */
    public static boolean getBoolean(KeysBooleanValue key) {
        readyFileAdapter();
        return prefs.getBoolean(key.toString(), key.getDefaultValue());
    }

    /**
     * This method is used to store a <code>boolean</code> value under a given
     * <b>key</b>.
     *
     * @param key
     *            a key as given by <code>KeysBooleanValue</code> enumeration.
     * @param value
     *            a value to store under the given key.
     */
    public static void putBoolean(KeysBooleanValue key, boolean value) {
        readyFileAdapter();
        prefs.putBoolean(key.toString(), value);
        notifyListeners();
    }

    /**
     * This method allows to add a <code>PreferenceChangeListener</code> to
     * <code>AmusePreferences</code>.
     *
     * @param pcl
     *            the <code>PreferenceChangeListener</code> to be notified of
     *            any changes made to the stored data in
     *            <code>AmusePreferences</code>.
     */
    public static void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        listeners.add(pcl);
    }

    /**
     * This method removes a previously added
     * <code>PreferenceChangeListener</code>.
     *
     * @param pcl
     *            the <code>PreferenceChangeListener</code> to be removed form
     *            <code>AmusePreferences</code>.
     */
    public static void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        listeners.remove(pcl);
    }

    private static void notifyListeners() {
        for (PreferenceChangeListener pcl : listeners) {
            pcl.preferenceChange();
        }
    }

    /**
     * @param file
     */
    public static void storeToFile(File file) {
        try {
            new PropertyFileAdapter().saveToFile(file);
        } catch (IOException ex) {
            AmuseLogger.write(AmusePreferences.class.getName(), Level.ERROR, "Unable to store settings to: "  + file.getAbsolutePath());
        }
    }

    /**
     *
     */
    public static void clearSettings() {
        readyFileAdapter();
        prefs.deleteSettings();
    }

    /**
     * @param file
     */
    public static void restoreFromFile(File file) {
        try {
        	new PropertyFileAdapter().restoreFromFile(file);
        	AmuseLogger.write(AmusePreferences.class.getName(), Level.INFO, "Preferences restored from " + file.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            AmuseLogger.write(AmusePreferences.class.getName(), Level.ERROR, ex.getLocalizedMessage());
        } catch (IOException ex) {
            AmuseLogger.write(AmusePreferences.class.getName(), Level.ERROR, ex.getLocalizedMessage());
        }
    }

    static String getCommentFor(String key) {
        String commentStr = null;
        commentStr = KeysIntValue.getCommentFor(key);
        if (commentStr != null) {
            return commentStr;
        }
        commentStr = KeysBooleanValue.getCommentFor(key);
        if (commentStr != null) {
            return commentStr;
        }
        commentStr = KeysStringValue.getCommentFor(key);
        if (commentStr != null) {
            return commentStr;
        }
        return "No Comment for this Key!";
    }
    
    /**
     * Convenience method to get the path to the multipleTacksAnnotationTable File
     * @return the path to multipleTacksAnnotationTable.arff
     */
    public static String getMultipleTracksAnnotationTablePath(){
    	return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "multipleTracksAnnotationTable.arff";
    }
    
    /**
     * Convenience method to get the path to the singleTrackAnnotationAttributeTable File
     * @return the path to singleTrackAnnotationAttributeTable.arff
     */
    public static String getSingleTrackAnnotationAttributeTablePath(){
    	return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "singleTrackAnnotationAttributeTable.arff";
    }
    
    /**
     * Convenience method to get the path to the classifierAlgorithmTable File
     * @return the path to classifierAlgorithmTable.arff
     */
	public static String getClassifierAlgorithmTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "classifierAlgorithmTable.arff";
	}

    /**
     * Convenience method to get the path to the featureExtractorToolTable File
     * @return the path to featureExtractorToolTable.arff
     */
	public static String getFeatureExtractorToolTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "featureExtractorToolTable.arff";
	}

    /**
     * Convenience method to get the path to the optimizerAlgorithmTable File
     * @return the path to optimizerAlgorithmTable.arff
     */
	public static String getOptimizerAlgorithmTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "optimizerAlgorithmTable.arff";
	}

    /**
     * Convenience method to get the path to the processorAlgorithmTable File
     * @return the path to processorAlgorithmTable.arff
     */
	public static String getProcessorAlgorithmTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "processorAlgorithmTable.arff";
	}

    /**
     * Convenience method to get the path to the processorConversionAlgorithmTable File
     * @return the path to processorConversionAlgorithmTable.arff
     */
	public static String getProcessorConversionAlgorithmTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "processorConversionAlgorithmTable.arff";
	}

    /**
     * Convenience method to get the path to the classifierPreprocessingAlgorithmTable File
     * @return the path to classifierPreprocessingAlgorithmTable.arff
     */
	public static String getClassifierPreprocessingAlgorithmTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "classifierPreprocessingAlgorithmTable.arff";
	}

    /**
     * Convenience method to get the path to the validationAlgorithmTable File
     * @return the path to validationAlgorithmTable.arff
     */
	public static String getValidationAlgorithmTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "validationAlgorithmTable.arff";
	}

    /**
     * Convenience method to get the path to the featureTable File
     * @return the path to featureTable.arff
     */
	public static String getFeatureTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "featureTable.arff";
	}

    /**
     * Convenience method to get the path to the pluginTable File
     * @return the path to pluginTable.arff
     */
	public static String getPluginTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "pluginTable.arff";
	}

    /**
     * Convenience method to get the path to the multipleTacksAnnotationTable File
     * @return the path to multipleTacksAnnotationTable.arff
     */
	public static String getMeasureTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "measureTable.arff";
	}

    /**
     * Convenience method to get the path to the multipleTacksAnnotationTable File
     * @return the path to multipleTacksAnnotationTable.arff
     */
	public static String getToolTablePath() {
		return get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "toolTable.arff";
	}
}
