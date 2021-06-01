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
 * Creation date: 23.12.2009
 */
package amuse.util.audio;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides methods to mix various audio files to one.
 *
 * @author Clemens Waeltken
 * @version $Id$
 */
public class AudioFileMixing {

    public static void mixFiles(List<File> files, File targetFile) throws UnsupportedAudioFileException, IOException {
        List<File> musicFiles = new ArrayList<File>(files);
        List<File> preparedFiles = new ArrayList<File>();
        String targetFilesName = targetFile.getName();
        targetFilesName = removeExtension(targetFilesName);

        // Check preconditions:
        if (musicFiles.isEmpty())
            return;
        if (targetFile.exists() && !targetFile.delete())
            return;

        // Prepare all files for mixing.
        for (File f : musicFiles) {
            if (!f.exists()) {
                AmuseLogger.write(AudioFileMixing.class.getName(), Level.ERROR, "Missing file: " + f.getName());
            } else {
                String sourceFilesName = f.getName();
                sourceFilesName = removeExtension(sourceFilesName);
                File tmpFile = new File(f.getParent() + File.separator + targetFilesName + "_" + sourceFilesName + ".wav");
                AudioFileConversion.convertWithSettings(f, tmpFile);
                tmpFile.deleteOnExit();
                preparedFiles.add(tmpFile);
            }
        }
        if (preparedFiles.size() < 2) {
            AmuseLogger.write(AudioFileMixing.class.getName(), Level.ERROR, "No files to mix!");
            return;
        }
        // Prepare AudioStreams

        AudioFormat format = AudioSystem.getAudioFileFormat(preparedFiles.get(0)).getFormat();

        List<AudioInputStream> audioStreams = new ArrayList<AudioInputStream>();
        for (File f : preparedFiles) {
            audioStreams.add(AudioSystem.getAudioInputStream(f));
        }

        MixingFloatAudioInputStream mixingStream = new MixingFloatAudioInputStream(format, audioStreams);
        AmuseLogger.write(AudioFileMixing.class.getName(), Level.DEBUG, "Start writing stream... \t" + format);
        AudioSystem.write(mixingStream, AudioFileFormat.Type.WAVE, targetFile);
        AmuseLogger.write(AudioFileMixing.class.getName(), Level.DEBUG, "... finished!");
    }

    private static String removeExtension(String fileName) {
        if (fileName.lastIndexOf('.') > 0) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        } else
            return fileName;
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
        
    	// At least three files must be given (two or more audio inputs and one output)
    	if(args.length < 3) {
        	throw new RuntimeException("Not enough input arguments!");
        }
        
        // Create the list with input files
    	List<File> filesToMix = new ArrayList<File>();
        for(int i=0;i<args.length-1;i++) {
        	filesToMix.add(new File(args[i]));
        }
        
        // Output file
        File mixedFile = new File(args[args.length-1]);
        
        // Do the job
        mixFiles(filesToMix, mixedFile);
    }
    
    public static void mixSamples(List<File> files, File targetFile) throws UnsupportedAudioFileException, IOException {
    	List<File> musicFiles = new ArrayList<File>(files);

    	// Check preconditions:
    	if (musicFiles.isEmpty()) {
    		throw new IOException("No Input Files!");
    	}
    	if (targetFile.exists() && !targetFile.delete()) {
    		throw new IOException("Unable to write output!");
    	}

    	AudioFormat format = AudioSystem.getAudioFileFormat(files.get(0)).getFormat();
    	List<AudioInputStream> audioStreams = new ArrayList<AudioInputStream>();
    	for (File f : files) {
    		audioStreams.add(AudioSystem.getAudioInputStream(f));
    	}

    	MixingFloatAudioInputStream mixingStream = new MixingFloatAudioInputStream(format, audioStreams);
    	AmuseLogger.write(AudioFileMixing.class.getName(), Level.DEBUG, "Start writing stream... \t" + format);
    	AudioSystem.write(mixingStream, AudioFileFormat.Type.WAVE, targetFile);
    	AmuseLogger.write(AudioFileMixing.class.getName(), Level.DEBUG, "..finished");
    }

    

}