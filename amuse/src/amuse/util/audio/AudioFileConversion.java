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
 * Creation date: 13.11.2008
 */
package amuse.util.audio;

import amuse.interfaces.nodes.NodeException;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysBooleanValue;
import amuse.preferences.KeysIntValue;
import amuse.util.AmuseLogger;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import org.apache.log4j.Level;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.Type;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

/**
 * This class provides static methods to do various operations on audio files.
 *
 * @author Clemens Waeltken
 * @version $Id$
 */
public class AudioFileConversion {

   public enum KHz {
	   KHz11, KHz22, KHz44
   }
    public static void convertFile(File musicFile, File targetFile, KHz khz, boolean isReduceToMono, boolean isDownSamplingActive) throws IOException {
	int targetKHZ;
        if (khz == KHz.KHz44) {
            targetKHZ = 44100;
        } else if (khz == KHz.KHz22) {
            targetKHZ = 22050;
        } else if (khz == KHz.KHz11) {
            targetKHZ = 11025;
        } else {
            targetKHZ = 44100;
        }
        boolean isOriginal = true;
        AudioFileFormat audioFileFormat = null;
        File wavFile = new File(targetFile.getParent() + File.separator + "tmp_" + musicFile.getName());
        // ---------------------------------------------------------------------
        // I.: Try to get AudioFileFormat:
        // ---------------------------------------------------------------------
        try {
            audioFileFormat = AudioSystem.getAudioFileFormat(musicFile);
        } catch (UnsupportedAudioFileException ex) {
            // If no wave file is given, this exception is generated. However the conversion mp3->wave can
            // be done!
            // throw new NodeException("Could not convert audio file: " + ex.getMessage());
        } catch (IOException ex) {
            throw new IOException("Error accessing file to process: " + ex.getMessage());
        }

        // ---------------------------------------------------------------------
        // II.: Try to convert mp3 to wave if needed:
        // ---------------------------------------------------------------------
        if (audioFileFormat == null || audioFileFormat.getType() != Type.WAVE) { // If not wave already, convert to wave
            try {
                AmuseLogger.write(AudioFileConversion.class.getName(), Level.INFO, "Converting " + musicFile.getName() + " to wave.");
                convertMp3ToWave(musicFile, wavFile);
                isOriginal = false;
            } catch (IOException ex) {
                throw new IOException("Error converting audio file " + musicFile.getName() + ": " + ex.getMessage());
            }
        } else {

            // wavFile is now the file to process.
            wavFile = musicFile;
        }

        // ---------------------------------------------------------------------
        // III.: Try to reduce audio quality:
        // ---------------------------------------------------------------------
        try {
            AudioFormat format = AudioSystem.getAudioFileFormat(wavFile).getFormat();
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.DEBUG, "Starting: "+wavFile.getName() + " "+ (int)format.getFrameRate()+"kHz, "+format.getChannels());
            fileCopy(wavFile, targetFile);
            if (!isDownSamplingActive) {
		targetKHZ = (int) format.getFrameRate();
	    }
            if (!isReduceToMono && format.getChannels() == 1) {
		AmuseLogger.write(AudioFileConversion.class.getName(), Level.WARN, "Target is stereo, but this file is mono already: " + wavFile.getName());
	    }
            if (isDownSamplingActive && targetKHZ > format.getFrameRate()) {
		AmuseLogger.write(AudioFileConversion.class.getName(), Level.WARN, "Target is " + targetKHZ + "kHz, but this file is at " + (int) format.getFrameRate() + "kHz already: " + wavFile.getName());
	    }
            sampleToTargetSize(targetFile, targetKHZ, isReduceToMono);
            format = AudioSystem.getAudioFileFormat(targetFile).getFormat();
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.DEBUG, "Result: "+targetFile.getName() + " "+ (int)format.getFrameRate()+"kHz, "+format.getChannels());
            if (isDownSamplingActive && format.getFrameRate() != 44100f && format.getFrameRate() != 22050f && format.getFrameRate() != 11025f) {
                AmuseLogger.write(AudioFileConversion.class.getName(), Level.WARN, "This file has no standard frame rate: \"" + wavFile + "\"");
            }
            deleteConvertedFile(wavFile, isOriginal);
        } catch (IOException ex) {
            ex.printStackTrace();
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.ERROR, "Unable to perform down-sampling: " + ex.getMessage());
            fileCopy(wavFile, targetFile);
            deleteConvertedFile(wavFile, isOriginal);
            throw ex;
        } catch (UnsupportedAudioFileException ex) {
            fileCopy(wavFile, targetFile);
            deleteConvertedFile(wavFile, isOriginal);
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.ERROR, "Unsupported Audio-File: \"" + ex.getLocalizedMessage() + "\"");
            throw new IOException(ex.getMessage());
        }

    }
    public static void convertWithSettings(File musicFile, File targetFile) throws IOException {
        boolean isReduceToMono = AmusePreferences.getBoolean(KeysBooleanValue.REDUCE_TO_MONO);
        boolean isDownSamplingActive = AmusePreferences.getBoolean(KeysBooleanValue.USE_DOWNSAMPLING);
        int targetKHZ = AmusePreferences.getInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ);
	KHz khz;
        if (targetKHZ == 0) {
            khz = KHz.KHz44;
        } else if (targetKHZ == 1) {
            khz = KHz.KHz22;
        } else if (targetKHZ == 2) {
            khz = KHz.KHz11;
        } else {
            khz = KHz.KHz44;
        }
	convertFile(musicFile, targetFile, khz, isReduceToMono, isDownSamplingActive);
    }

    /**
     * This method is used primarily by <class>ExtractorNodeScheduler</class> to prepare the feature extraction of the current music file.
     * The given file will be converted to wave, downsampled and split according to the settings stored in <class>AmusePreferences</class>.
     *
     * @param targetDir The parent folder to place the processed music file into.
     * @param musicFile The music file to process.
     * @throws NodeException
     */
    public static void processFile(File targetDir, File musicFile) throws NodeException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        if (!targetDir.isDirectory() || !musicFile.isFile()) {
            throw new NodeException("Path to music file or target directory is not properly set!");
        }
        boolean isSplittingEnabled = AmusePreferences.getBoolean(KeysBooleanValue.SPLIT_WAVE);
        int splitSize = AmusePreferences.getInt(KeysIntValue.SPLIT_SIZE_IN_KB);
        File wavFile = new File(targetDir.getAbsolutePath() + File.separator +
                musicFile.getName().substring(0, musicFile.getName().lastIndexOf('.')) + ".wav");
        File targetFile = new File(targetDir.getAbsolutePath() + File.separator + "1" + File.separator + wavFile.getName());

        // Create targetDir if necessary
        if (!new File(targetDir.getAbsolutePath() + File.separator + "1").exists()) {
            new File(targetDir.getAbsolutePath() + File.separator + "1").mkdirs();
        }
        try {
            convertWithSettings(musicFile, targetFile);
        } catch (IOException e) {
            throw new NodeException(e.getMessage());
        }
        // Again wavFile is now the current file to process.
        wavFile = targetFile;

        // ---------------------------------------------------------------------
        // IV.II: Try splitting wave file and copy into according directories.
        // ---------------------------------------------------------------------
        if (isSplittingEnabled) {
            int index = 1;
            int splitFileCount = 1;

            try {
                splitFileCount = splitWaveFile(wavFile, splitSize);
            } catch (IOException ex) {
                AmuseLogger.write(AudioFileConversion.class.getName(), Level.ERROR, "Unable to split " + wavFile.getName() + ": " + ex.getMessage());
            }

            while (index <= splitFileCount) {
                // Create folders if necessary:
                if (!new File(targetDir.getAbsolutePath() + File.separator + index).exists()) { // Create targetDir if necessary.
                    new File(targetDir.getAbsolutePath() + File.separator + index).mkdirs();
                }
                File src = new File(wavFile.getAbsolutePath() + "." + index);
                File dest = new File(targetDir.getAbsolutePath() + File.separator + index + File.separator + wavFile.getName());
                fileCopy(src, dest);
                src.delete();
                index++;
            }
        }
    }

    /**
     * This method splits up a single .wav File into multiple smaller files.
     * The actual limit of each files size is given as a parameter.
     *
     * @param waveFile  The .wav to split.
     * @param splitSize The maximum size of each split part in KiloByte.
     * @return The count of files created.
     * @throws java.io.IOException
     */
    public static int splitWaveFile(File waveFile, int splitSize) throws IOException {
        int part = 1;
        AudioInputStream ais = null;
        FileInputStream tmpFis = null;
        BufferedInputStream tmpBis = null;
        File tempOutputFile = null;
        FileOutputStream tmpOutputStream = null;
        
        if (waveFile.length() < 1024 * splitSize) {
        	
        	// Also when the file is not splitted, processFile() expects that file ends with ".1"
        	// (see the line with "while (index <= splitFileCount) {") 
        	fileCopy(waveFile, getNextSplitFile(waveFile, part));
        	return 1;
        }
        try {
            tempOutputFile = new File(waveFile.getParentFile().getAbsolutePath() + File.separator + ".tmp_" + waveFile.getName());
            tmpOutputStream = new FileOutputStream(tempOutputFile);
            tmpFis = new FileInputStream(waveFile);
            tmpBis = new BufferedInputStream(tmpFis);
            ais = AudioSystem.getAudioInputStream(tmpBis);
            // Size to split at in Byte.
            int maxSize = 1024 * splitSize;
            File currentOutputFile = getNextSplitFile(waveFile, part);
            currentOutputFile.createNewFile();
            AudioFormat format = ais.getFormat();
            byte[] buffer = new byte[1024];
            int bytesWritten = 0;
            while (ais.read(buffer) > -1) {
                if (bytesWritten > maxSize) {
                    AudioInputStream tais = new AudioInputStream(new FileInputStream(tempOutputFile), format, tempOutputFile.length() / format.getFrameSize());
                    AudioSystem.write(tais, Type.WAVE, currentOutputFile);
                    tmpOutputStream.close();
                    tempOutputFile.delete();
                    tempOutputFile.createNewFile();
                    tmpOutputStream = new FileOutputStream(tempOutputFile);
                    part++;
                    currentOutputFile = getNextSplitFile(waveFile, part);
                    currentOutputFile.createNewFile();
                    bytesWritten = 0;
                }
                tmpOutputStream.write(buffer);
                bytesWritten += 1024;
            }
            tmpOutputStream.close();
            AudioInputStream tais = new AudioInputStream(new FileInputStream(tempOutputFile), format, tempOutputFile.length() / format.getFrameSize());
            AudioSystem.write(tais, Type.WAVE, currentOutputFile);
            tais.close();
        } catch (UnsupportedAudioFileException ex) {
            throw new IOException(ex);
        } finally {
            if (ais != null) {
                ais.close();
            }
            if (tmpBis != null) {
                tmpBis.close();
            }
            if (tmpFis != null) {
                tmpFis.close();
            }
            if (tmpOutputStream != null) {
                tmpOutputStream.close();
            }
            if (tempOutputFile.exists()) {
                tempOutputFile.delete();
            }
        }
        return part;
    }

    /**
     * This method is used to convert a given .mp3 file to .wav. This method currently uses the javazoom library from www.javazoom.net.
     *
     * @param mp3File    The file to convert.
     * @param outputFile The file to write the converted .wav to.
     * @throws IOException Thrown each time the given file is invalid.
     */
    public static void convertMp3ToWave(File mp3File, File outputFile) throws IOException {
        try {
            Converter con = new Converter();
            con.convert(mp3File.getAbsolutePath(), outputFile.getAbsolutePath());
        } catch (JavaLayerException ex) {
            Logger.getLogger(AudioFileConversion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    private static void deleteConvertedFile(File wavFile, boolean original) {
        if (!original) {
            boolean success = wavFile.delete();
            if (!success) {
		wavFile.deleteOnExit();
	    }
        }
    }

    private static void fileCopy(File srcFile, File destFile) {
    	FileInputStream srcChannelFIS = null;
    	FileOutputStream dstChannelFOS = null;
        try {
        	// System.out.println("Trying to copy " + srcFile.getName());
            // Create channel on the source
        	srcChannelFIS = new FileInputStream(srcFile);
            FileChannel srcChannel = srcChannelFIS.getChannel();

            // Create channel on the destination
            dstChannelFOS = new FileOutputStream(destFile);
            FileChannel dstChannel = dstChannelFOS.getChannel();

            // Copy file contents from source to destination
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

            // Close the channels
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.ERROR, "Unable to copy " + srcFile.getAbsolutePath() + " to " + destFile.getAbsolutePath() + ".");
        }
        finally{
        	if(srcChannelFIS != null){
        		try {
					srcChannelFIS.close();
				} catch (IOException e) {}
        	}
        	if(dstChannelFOS != null){
        		try {
        			dstChannelFOS.close();
				} catch (IOException e) {}
        	}
        }
    }

    /**
     * This method is manly used to calculate the file to write each split part of a .wav to. It is currently used by <class>AudioFileConversion.splitWaveFile</class>.
     *
     * @param inputFile The file being processed by the split method.
     * @param part      The current part in the splitting process.
     * @return The file denoting the target of the next split part.
     */
    private static File getNextSplitFile(File inputFile, int part) {
        String fileName = inputFile.getAbsolutePath();
        File file = new File(fileName.replace(inputFile.getName(), inputFile.getName() + "." + part));
        return file;
    }

    /**
     * This method is used for testing purposes only!
     */
    public static void main(String[] args) throws IOException, NodeException {
        AmusePreferences.putBoolean(KeysBooleanValue.USE_DOWNSAMPLING, true);
        AmusePreferences.putBoolean(KeysBooleanValue.SPLIT_WAVE, false);
        AmusePreferences.putBoolean(KeysBooleanValue.REDUCE_TO_MONO, false);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 0);
        File musicFile = new File("test" + File.separator + "test.mp3");
        File destFolder = new File("test" + File.separator + "stereo44" + File.separator);
        processFile(destFolder, musicFile);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 1);
        destFolder = new File("test" + File.separator + "stereo22" + File.separator);
        processFile(destFolder, musicFile);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 2);
        destFolder = new File("test" + File.separator + "stereo11" + File.separator);
        processFile(destFolder, musicFile);
        AmusePreferences.putBoolean(KeysBooleanValue.REDUCE_TO_MONO, true);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 0);
        destFolder = new File("test" + File.separator + "mono44" + File.separator);
        processFile(destFolder, musicFile);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 1);
        destFolder = new File("test" + File.separator + "mono22" + File.separator);
        processFile(destFolder, musicFile);
        AmusePreferences.putInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ, 2);
        destFolder = new File("test" + File.separator + "mono11" + File.separator);
        processFile(destFolder, musicFile);
    }

    private static void sampleToTargetSize(File file, int targetKHZ, boolean isReduceToMono) throws UnsupportedAudioFileException, IOException {
        File tmpFile = new File(file.getParent() + File.separator + "tmp_" + file.getName());
        fileCopy(file, tmpFile);
        SampleRateConverter.changeFormat(tmpFile, file, targetKHZ, isReduceToMono);
	    tmpFile.delete();
	}
}
