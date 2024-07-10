package amuse.util.converters;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Level;

import amuse.interfaces.nodes.NodeException;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysBooleanValue;
import amuse.preferences.KeysIntValue;
import amuse.util.AmuseLogger;
import amuse.util.FileOperations;
import amuse.util.audio.AudioFileConversion;
import amuse.util.audio.SampleRateConverter;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

public class Mp3ToWaveConverter implements ConverterInterface {
	//TODO remove folder for splitted files

	@Override
	public File convert(File file, File outputFolder) throws IOException, NodeException {
		if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        if (!outputFolder.isDirectory() || !file.isFile()) {
            throw new NodeException("Path to music file or target directory is not properly set!");
        }
        File wavFile = new File(outputFolder.getAbsolutePath() + File.separator +
                file.getName().substring(0, file.getName().lastIndexOf('.')) + getEnding());
        File targetFile = new File(outputFolder.getAbsolutePath() + File.separator + "1" + File.separator + wavFile.getName());

        // Create targetDir if necessary
        if (!new File(outputFolder.getAbsolutePath() + File.separator + "1").exists()) {
            new File(outputFolder.getAbsolutePath() + File.separator + "1").mkdirs();
        }
        try {
            Converter con = new Converter();
            con.convert(file.getAbsolutePath(), targetFile.getAbsolutePath());
            
            reduceAudioQuality(targetFile);
        } catch (JavaLayerException ex) {
            Logger.getLogger(AudioFileConversion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
		return targetFile;
    }

	@Override
	public String getEnding() {
		return ".wav";
	}
	
	public void reduceAudioQuality(File file) throws IOException {
		boolean isReduceToMono = AmusePreferences.getBoolean(KeysBooleanValue.REDUCE_TO_MONO);
		boolean isDownSamplingActive = AmusePreferences.getBoolean(KeysBooleanValue.USE_DOWNSAMPLING);
		int targetKHZ = AmusePreferences.getInt(KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ);
		if (targetKHZ == 1) {
            targetKHZ = 22050;
        } else if (targetKHZ == 2) {
            targetKHZ = 11025;
        } else {
            targetKHZ = 44100;
        }
		
        File tempFile = new File(file.getParent() + File.separator + "tmp_" + file.getName());
		try {
            AudioFormat format = AudioSystem.getAudioFileFormat(file).getFormat();
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.DEBUG, "Starting: "+file.getName() + " "+ (int)format.getFrameRate()+"kHz, "+format.getChannels());
            
            FileOperations.fileCopy(file, tempFile);
            if (!isDownSamplingActive) {
				targetKHZ = (int) format.getFrameRate();
		    }
            if (!isReduceToMono && format.getChannels() == 1) {
            	AmuseLogger.write(AudioFileConversion.class.getName(), Level.WARN, "Target is stereo, but this file is mono already: " + file.getName());
            }
            if (isDownSamplingActive && targetKHZ > format.getFrameRate()) {
            	AmuseLogger.write(AudioFileConversion.class.getName(), Level.WARN, "Target is " + targetKHZ + "kHz, but this file is at " + (int) format.getFrameRate() + "kHz already: " + file.getName());
            }
            sampleToTargetSize(tempFile, targetKHZ, isReduceToMono);
            format = AudioSystem.getAudioFileFormat(tempFile).getFormat();
            if (isDownSamplingActive && format.getFrameRate() != 44100f && format.getFrameRate() != 22050f && format.getFrameRate() != 11025f) {
                AmuseLogger.write(AudioFileConversion.class.getName(), Level.WARN, "This file has no standard frame rate: \"" + file + "\"");
            }
            FileOperations.deleteFile(file);
            FileOperations.fileCopy(tempFile, file);
            FileOperations.deleteFile(tempFile);
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.DEBUG, "Result: "+file.getName() + " "+ (int)format.getFrameRate()+"kHz, "+format.getChannels());
        } catch (IOException ex) {
            ex.printStackTrace();
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.ERROR, "Unable to perform down-sampling: " + ex.getMessage());
            FileOperations.deleteFile(tempFile);
            throw ex;
        } catch (UnsupportedAudioFileException ex) {
            FileOperations.deleteFile(tempFile);
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.ERROR, "Unsupported Audio-File: \"" + ex.getLocalizedMessage() + "\"");
            throw new IOException(ex.getMessage());
        }
	}
	
	private void sampleToTargetSize(File file, int targetKHZ, boolean isReduceToMono) throws UnsupportedAudioFileException, IOException {
        File tmpFile = new File(file.getParent() + File.separator + "tmp_sample_" + file.getName());
        FileOperations.fileCopy(file, tmpFile);
        SampleRateConverter.changeFormat(tmpFile, file, targetKHZ, isReduceToMono);
	    tmpFile.delete();
	}
}