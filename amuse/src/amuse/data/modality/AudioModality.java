package amuse.data.modality;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Level;

import amuse.data.modality.Modality;
import amuse.data.modality.Modality.ModalityEnum;
import amuse.nodes.extractor.ExtractorNodeScheduler;
import amuse.util.AmuseLogger;

public class AudioModality implements Modality {
	
	public enum AudioFormat {
		
		MP3		(List.of("mp3")),
		WAVE	(List.of("wav")), 
		AIFF 	(List.of("aiff", "aif")), 
		AIFC 	(List.of("aifc")), 
		SND 	(List.of("snd")), 
		AU 		(List.of("au"));
		
		private final List<String> endings;
		
		private AudioFormat(List<String> endings) {
			this.endings = endings;
		}
	}
	
	private final ModalityEnum modalityEnum = ModalityEnum.AUDIO;
	
	private List<AudioFormat> formats;
	
	public AudioModality(List<AudioFormat> formats) {
		this.formats = formats;
	}
	
	/** Returns an array with all possible audio file endings */
	public static List<String> getEndings() {
		List<String> endings = new ArrayList<String>();
		for (AudioFormat format : AudioFormat.values()) {
			endings.addAll(format.endings);
		}
		return endings;
		
	}
	
	@Override
	public List getFormats() {
		return formats;
	}

	@Override
	public boolean matchesRequirements(File file) {
		
		/* Check, if file is mp3-data */
		if(file.getPath().endsWith(".mp3") && formats.contains(AudioFormat.MP3)) {
			return true;
		}
		/* Check other file formats */
		AudioFileFormat audioFileFormat;
		try {
			audioFileFormat = AudioSystem.getAudioFileFormat(file);
			if(audioFileFormat.getType() == AudioFileFormat.Type.WAVE && formats.contains(AudioFormat.WAVE)) {
				return true;
			}
			else if(audioFileFormat.getType() == AudioFileFormat.Type.SND && formats.contains(AudioFormat.SND)) {
				return true;
			}
			else if(audioFileFormat.getType() == AudioFileFormat.Type.AIFF && formats.contains(AudioFormat.AIFF)) {
				return true;
			}
			else if(audioFileFormat.getType() == AudioFileFormat.Type.AIFC && formats.contains(AudioFormat.AIFC)) {
				return true;
			}
			else if(audioFileFormat.getType() == AudioFileFormat.Type.AU && formats.contains(AudioFormat.AU)) {
				return true;
			}
		} catch (UnsupportedAudioFileException e) {
			AmuseLogger.write(this.getClass().getName(), Level.WARN,"The audio file format could not be confirmed: " + file.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ModalityEnum getModalityEnum() {
		return modalityEnum;
	}

}
