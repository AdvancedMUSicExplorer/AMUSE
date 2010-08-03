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
 * Creation date: 01.04.2010
 */

package amuse.util.audio;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysBooleanValue;
import amuse.preferences.KeysIntValue;
import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Clemens Waeltken
 */
public class AudioFileConversionTest {

    public AudioFileConversionTest() {
    }

    private static File storeSettings = new File("test/tmpSettings.prefs");
    private static File waveFile = new File("test/tmpWave.wav");
    private static File mp3File = new File("test/test.mp3");

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("AudioFileConversionTest: Storing Settings to: " + storeSettings.getAbsolutePath());
        storeSettings.delete();
        if (!storeSettings.createNewFile())
            fail("Can not store store settings.");
        AmusePreferences.storeToFile(storeSettings);
        assertTrue("Missing "+mp3File.getAbsolutePath()+"!",mp3File.isFile());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        AmusePreferences.restoreFromFile(storeSettings);
        storeSettings.deleteOnExit();
        waveFile.deleteOnExit();
    }

    @Test
    public void testConvertMp3ToWave() throws Exception {
	System.out.println("convertMp3ToWave");
	File mp3SourceFile = mp3File;
	File outputFile = waveFile;
	AudioFileConversion.convertMp3ToWave(mp3SourceFile, outputFile);
        assertTrue("No file was created!", outputFile.isFile());
        AudioFileFormat format = AudioSystem.getAudioFileFormat(outputFile);
        assertTrue("No Wave result!", format.getType() == AudioFileFormat.Type.WAVE);
        assertTrue("No 44kHz result!", format.getFormat().getFrameRate() == 44100f);
        assertTrue("No setreo result!", format.getFormat().getChannels() == 2);
    }
    
    @Test
    public void testConvertWithSettings() throws Exception {
	System.out.println("convertWithSettings");
	File musicFile = waveFile;
	File stereo44 = new File("test/stereo44.wav");
	File stereo22 = new File("test/stereo22.wav");
	File stereo11 = new File("test/stereo11.wav");
	File mono44 = new File("test/mono44.wav");
	File mono22 = new File("test/mono22.wav");
	File mono11 = new File("test/mono11.wav");
	File stereo22up = new File("test/stereo22up.wav");
	File stereo11up = new File("test/stereo11up.wav");
	File mono22up = new File("test/mono22up.wav");
	File mono11up = new File("test/mono11up.wav");
        stereo44.deleteOnExit();
        stereo22.deleteOnExit();
        stereo11.deleteOnExit();
        mono44.deleteOnExit();
        mono22.deleteOnExit();
        mono11.deleteOnExit();
        stereo22up.deleteOnExit();
        stereo11up.deleteOnExit();
        mono22up.deleteOnExit();
        mono11up.deleteOnExit();

        AudioFormat format;
        AmusePreferences.putBoolean(KeysBooleanValue.USE_DOWNSAMPLING, true);
        AmusePreferences.putBoolean(KeysBooleanValue.SPLIT_WAVE, false);
        
        // Test stereo results:
        AmusePreferences.putBoolean(KeysBooleanValue.REDUCE_TO_MONO, false);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 0);
	AudioFileConversion.convertWithSettings(musicFile, stereo44);
        format = AudioSystem.getAudioFileFormat(stereo44).getFormat();
        assertTrue(format.getFrameRate() == 44100f);
        assertTrue(format.getChannels() == 2);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 1);
	AudioFileConversion.convertWithSettings(musicFile, stereo22);
        format = AudioSystem.getAudioFileFormat(stereo22).getFormat();
        assertTrue(format.getFrameRate() == 22050f);
        assertTrue(format.getChannels() == 2);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 2);
	AudioFileConversion.convertWithSettings(musicFile, stereo11);
        format = AudioSystem.getAudioFileFormat(stereo11).getFormat();
        assertTrue(format.getFrameRate() == 11025f);
        assertTrue(format.getChannels() == 2);

        // Test mono results:
        AmusePreferences.putBoolean(KeysBooleanValue.REDUCE_TO_MONO, true);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 0);
	AudioFileConversion.convertWithSettings(musicFile, mono44);
        format = AudioSystem.getAudioFileFormat(mono44).getFormat();
        assertTrue(format.getFrameRate() == 44100f);
        assertTrue(format.getChannels() == 1);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 1);
	AudioFileConversion.convertWithSettings(musicFile, mono22);
        format = AudioSystem.getAudioFileFormat(mono22).getFormat();
        assertTrue(format.getFrameRate() == 22050f);
        assertTrue(format.getChannels() == 1);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 2);
	AudioFileConversion.convertWithSettings(musicFile, mono11);
        format = AudioSystem.getAudioFileFormat(mono11).getFormat();
        assertTrue(format.getFrameRate() == 11025f);
        assertTrue(format.getChannels() == 1);

        // Try sampling up:
        AmusePreferences.putBoolean(KeysBooleanValue.REDUCE_TO_MONO, false);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 0);
        AudioFileConversion.convertWithSettings(stereo22, stereo22up);
        format = AudioSystem.getAudioFileFormat(stereo22up).getFormat();
        assertTrue(format.getFrameRate() == 22050f);
        assertTrue(format.getChannels() == 2);
        AudioFileConversion.convertWithSettings(stereo11, stereo11up);
        format = AudioSystem.getAudioFileFormat(stereo11up).getFormat();
        assertTrue(format.getFrameRate() == 11025f);
        assertTrue(format.getChannels() == 2);
        AudioFileConversion.convertWithSettings(mono22, mono22up);
        format = AudioSystem.getAudioFileFormat(mono22up).getFormat();
        assertTrue(format.getFrameRate() == 22050f);
        assertTrue(format.getChannels() == 1);
        AudioFileConversion.convertWithSettings(mono11, mono11up);
        format = AudioSystem.getAudioFileFormat(mono11up).getFormat();
        assertTrue(format.getFrameRate() == 11025f);
        assertTrue(format.getChannels() == 1);
    }
}