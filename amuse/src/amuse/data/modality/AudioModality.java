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
import amuse.util.AmuseLogger;

/** 
 * AudioModality objects can be used by extraction tools to specify, 
 * that all of their features can be extracted from audio data and from which formats.
 * 
 * @author Clara Pingel
 */
public class AudioModality implements Modality {
	
	public enum AudioFormat implements Format {
		
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
		
		public static AudioFormat getFormat(File file) {
			for(AudioFormat format : AudioFormat.values()) {
				for(String ending : format.endings) {
					if(file.getPath().endsWith("." + ending)) {
						return format;
					}
				}
			}
			return null;
		}

		@Override
		public boolean matchesEndings(File file) {
			for(String ending: this.endings) {
				if(file.getPath().endsWith("." + ending)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean confirmFormat(File file) {
			switch(this) {
				case MP3: {
					return true;
				}
				case WAVE: {
					try {
						return AudioSystem.getAudioFileFormat(file).getType() == AudioFileFormat.Type.WAVE;
					} catch (UnsupportedAudioFileException e) {
						return false;
					} catch (IOException e) { e.printStackTrace(); }
				}
				case AIFF: {
					try {
						return AudioSystem.getAudioFileFormat(file).getType() == AudioFileFormat.Type.AIFF;
					} catch (UnsupportedAudioFileException e) {
						return false;
					} catch (IOException e) { e.printStackTrace(); }
				}
				case AIFC: {
					try {
						return AudioSystem.getAudioFileFormat(file).getType() == AudioFileFormat.Type.AIFC;
					} catch (UnsupportedAudioFileException e) {
						return false;
					} catch (IOException e) { e.printStackTrace(); }
				}
				case SND: {
					try {
						return AudioSystem.getAudioFileFormat(file).getType() == AudioFileFormat.Type.SND;
					} catch (UnsupportedAudioFileException e) {
						return false;
					} catch (IOException e) { e.printStackTrace(); }
				}
				case AU: {
					try {
						return AudioSystem.getAudioFileFormat(file).getType() == AudioFileFormat.Type.AU;
					} catch (UnsupportedAudioFileException e) {
						return false;
					} catch (IOException e) { e.printStackTrace(); }
				}
				default: return false;
			}
		}

		@Override
		public List<String> getEndings() {
			return endings;
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
	public List<AudioFormat> getFormats() {
		return formats;
	}
	
	@Override
	public boolean matchesRequirements(File file) {
		
		for(AudioFormat audioFormat : this.formats) {
			boolean fileEndingMatchesRequirements = audioFormat.matchesEndings(file);
			boolean fileFormatConfirmed = audioFormat.confirmFormat(file);
			
			if(fileEndingMatchesRequirements && fileFormatConfirmed) {
				return true;
			} else if (fileEndingMatchesRequirements && !fileFormatConfirmed) {
				AmuseLogger.write(this.getClass().getName(), Level.WARN,"The audio file format could not be confirmed and might be broken: " + file.getName());
				return true;
			}
		}
		return false;
	}

	@Override
	public ModalityEnum getModalityEnum() {
		return modalityEnum;
	}
}

