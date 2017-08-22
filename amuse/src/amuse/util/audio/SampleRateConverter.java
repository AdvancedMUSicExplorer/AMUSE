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
 * Creation date: 18.12.2009
 */
package amuse.util.audio;

import javax.sound.sampled.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SampleRateConverter {
    /**
     * Flag for debugging messages.
     * If true, some messages are dumped to the console
     * during operation.
     */
    private static boolean DEBUG = false;


    private static void changeSampling(File sourceFile, File targetFile, float targetSampleRate, boolean toMono)
            throws UnsupportedAudioFileException, IOException {
        if (DEBUG) {
            out("target sample rate: " + targetSampleRate);
            if (toMono) out(", Mono");
            else out(", Stereo");
        }
        if (sourceFile.equals(targetFile)) {
            out("WARNING: Source-File and Target-File are the same.");
            return;
        }
        if (targetFile.exists()) {
            boolean success = targetFile.delete();
            if (!success) {
                out("WARNING: Target-File can not be deleted!");
                return;
            }
        }
        /* We try to use the same audio file type for the target
             file as the source file. So we first have to find
             out about the source file's properties.
          */
        AudioFileFormat sourceFileFormat = AudioSystem.getAudioFileFormat(sourceFile);
        AudioFileFormat.Type targetFileType = sourceFileFormat.getType();
        /* Here, we are reading the source file.
         * The sourceFile is only properly released when creating the AudioInputstream with another Inputstream.
           */
        
    
        AudioInputStream sourceStream = null;
        FileInputStream tmpFis = new FileInputStream(sourceFile);
        BufferedInputStream tmpBis = new BufferedInputStream(tmpFis);
        sourceStream = AudioSystem.getAudioInputStream(tmpBis);
        
        if (sourceStream == null) {
            out("cannot open source audio file: " + sourceFile);
            System.exit(1);
        }
        AudioFormat sourceFormat = sourceStream.getFormat();
        if (DEBUG) {
            out("source format: " + sourceFormat);
        }
        /* Currently, the only known and working sample rate
             converter for Java Sound requires that the encoding
             of the source stream is PCM (signed or unsigned).
             So as a measure of convenience, we check if this
             holds here.
          */
        AudioFormat.Encoding encoding = sourceFormat.getEncoding();
        if (!isPcm(encoding)) {
            out("encoding of source audio data is not PCM; conversion not possible");
            System.exit(1);
        }

        /* Since we now know that we are dealing with PCM, we know
             that the frame rate is the same as the sample rate.
          */
        float fTargetFrameRate = targetSampleRate;

        /* Here, we are constructing the desired format of the
             audio data (as the result of the conversion should be).
             We take over all values besides the sample/frame rate.
          */

        int channels = sourceFormat.getChannels();

        if (toMono) {
            channels = 1;
        }
        AudioFormat targetFormat = new AudioFormat(
                sourceFormat.getEncoding(),
                targetSampleRate,
                sourceFormat.getSampleSizeInBits(),
                channels,
                sourceFormat.getFrameSize(),
                fTargetFrameRate,
                sourceFormat.isBigEndian());

        if (DEBUG) {
            out("desired target format: " + targetFormat);
        }

        /* Now, the conversion takes place.
           */
        AudioInputStream targetStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
        if (DEBUG) {
            out("targetStream: " + targetStream);
        }

        /* And finally, we are trying to write the converted audio
             data to a new file.
          */
        int nWrittenBytes = 0;
        nWrittenBytes = AudioSystem.write(targetStream, targetFileType, targetFile);
        targetStream.close();
        sourceStream.close();
        tmpBis.close();
        tmpFis.close();

        if (DEBUG) {
            out("Written bytes: " + nWrittenBytes);
        }
    }

    /**
     * Checks if the encoding is PCM.
     */
    public static boolean isPcm(AudioFormat.Encoding encoding) {
        return encoding.equals(AudioFormat.Encoding.PCM_SIGNED)
                || encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED);
    }


    private static void out(String strMessage) {
        System.out.println(strMessage);
    }

    public static void changeFormat(File src, File target, float targetSampleRate, boolean toMono) throws UnsupportedAudioFileException, IOException {
        if (toMono) {
            File tmpFile = new File(target.getParent()+ File.separator + "monotmp_" + target.getName());
            changeSampling(src, tmpFile, targetSampleRate, false);
            changeSampling(tmpFile, target, targetSampleRate, true);
            tmpFile.delete();
        } else {
            changeSampling(src, target, targetSampleRate, false);
        }
    }
}
