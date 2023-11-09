package amuse.nodes.extractor.modality;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;

/**
 * WaveModality objects can be used by Extractor-Tools to specify that they accept WAVE-input and define the conditions.
 */
public class WaveModality implements AudioModality {
	
	public enum WaveFormat{WAVE}
	
	public List<WaveFormat> formats = List.of(WaveFormat.WAVE);
	
	public WaveModality() {}

	@Override
	public List<WaveFormat> getFormats() {
		return formats;
	}

	@Override
	public boolean matchesRequirements(File file) {
		try {
			/*Check, if file is appropriate wave data*/
			AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(file);
			if(audioFileFormat.getType() != AudioFileFormat.Type.WAVE) {
				return false;
			}
			
		} catch (UnsupportedAudioFileException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "The given file could not be used as WAVE-input: " + file.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
