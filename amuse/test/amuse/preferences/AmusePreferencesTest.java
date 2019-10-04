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
 * Creation date: 15.01.2009
 */

package amuse.preferences;

import amuse.util.FileOperations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Clemens Waeltken
 */
public class AmusePreferencesTest {

    private static File storeSettingsWhileTestingFile = new File("test/SavedSettingsWhileTesting.properties");

    private static File settingsFile = new File("config/amuse.properties");

    private static File saveLoadTestFile = new File("test/testStore");

    public AmusePreferencesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
	FileOperations.copy(settingsFile, storeSettingsWhileTestingFile);
        AmusePreferences.clearSettings();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        AmusePreferences.clearSettings();
	FileOperations.move(storeSettingsWhileTestingFile, settingsFile);
	saveLoadTestFile.delete();
    }

    @Test
    public void testGet() {
        System.out.println("get");
        KeysStringValue key = KeysStringValue.MEASURE_DATABASE;
        String expResult = "TestString";
        AmusePreferences.put(key, expResult);
        String result = AmusePreferences.get(key);
        assertEquals(expResult, result);
    }

    @Test
    public void testPut() {
        System.out.println("put");
        KeysStringValue key = KeysStringValue.MEASURE_DATABASE;
        String value = "TestString2";
        AmusePreferences.put(key, value);
    }

    @Test
    public void testGetInt() {
        System.out.println("getInt");
        KeysIntValue key = KeysIntValue.MAX_NUMBER_OF_TASK_THREADS;
        int expResult = 5;
        AmusePreferences.putInt(key, expResult);
        int result = AmusePreferences.getInt(key);
        assertEquals(expResult, result);
    }

    @Test
    public void testPutInt() {
        System.out.println("putInt");
        KeysIntValue key = KeysIntValue.MAX_NUMBER_OF_TASK_THREADS;
        int value = 5;
        AmusePreferences.putInt(key, value);
    }

    @Test
    public void testGetBoolean() {
        System.out.println("getBoolean");
        KeysBooleanValue key = KeysBooleanValue.REDUCE_TO_MONO;
        boolean expResult = key.getDefaultValue();
        boolean result = AmusePreferences.getBoolean(key);
        assertEquals(expResult, result);
        expResult = !key.getDefaultValue();
        AmusePreferences.putBoolean(key, expResult);
        result = AmusePreferences.getBoolean(key);
        assertEquals(expResult, result);
    }

    @Test
    public void testPutBoolean() {
        System.out.println("putBoolean");
        KeysBooleanValue key = KeysBooleanValue.REDUCE_TO_MONO;
        boolean value = false;
        AmusePreferences.putBoolean(key, value);
    }

    @Test
    public void testStoreToFile() throws Exception {
        System.out.println("storeToFile");
        if (saveLoadTestFile.exists()) {
            assertTrue("Could not delete test-file!", saveLoadTestFile.delete());
        }
        AmusePreferences.storeToFile(saveLoadTestFile);
        assertTrue("Expected file not created!",saveLoadTestFile.exists());
    }

    @Test
    public void testRestoreFromFile() throws Exception {
        System.out.println("restoreFromFile");
        KeysStringValue key = KeysStringValue.MODEL_DATABASE;
        String expResult = "Crazy";
        AmusePreferences.put(key, expResult);
        AmusePreferences.storeToFile(saveLoadTestFile);
        AmusePreferences.restoreFromFile(saveLoadTestFile);
        String result = AmusePreferences.get(key);
        assertEquals(expResult, result);
    }

    @Test
    public void testAddPreferenceChangeListener() {
        System.out.println("addPreferenceChangeListener");
        TestListener pcl = new TestListener();
        AmusePreferences.addPreferenceChangeListener(pcl);
        assertFalse("Error in private class TestListener!", pcl.getChanged());
        KeysBooleanValue key = KeysBooleanValue.REDUCE_TO_MONO;
        AmusePreferences.putBoolean(key, !AmusePreferences.getBoolean(key));
        assertTrue("Listener not notified!",pcl.getChanged());
    }

    @Test
    public void testRemovePreferenceChangeListener() {
        System.out.println("removePreferenceChangeListener");
        PreferenceChangeListener pcl = new PreferenceChangeListener() {

            @Override
            public void preferenceChange() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        AmusePreferences.addPreferenceChangeListener(pcl);
        AmusePreferences.removePreferenceChangeListener(pcl);
    }

    @Test
    public void testClearSettings() {
        System.out.println("clearSettings");
        KeysStringValue key = KeysStringValue.AMUSE_PATH;
        String defaultValue = key.getDefaultValue();
        AmusePreferences.put(key, defaultValue + "Make it non default!");
        AmusePreferences.clearSettings();
        String result = AmusePreferences.get(key);
        assertEquals(defaultValue, result);
    }
    
    private class TestListener implements PreferenceChangeListener {
        private boolean changed = false;
        @Override
        public void preferenceChange() {
            changed = true;
        }

        public boolean getChanged() {
            return changed;
        }
    }
}