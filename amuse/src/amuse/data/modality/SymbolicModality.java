package amuse.data.modality;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;

/** 
 * SymbolicModality objects can be used by extraction tools to specify, 
 * that all of their features can be extracted from symbolic data and from which formats.
 * 
 * @author Clara Pingel
 */
public class SymbolicModality implements Modality {

	public enum SymbolicFormat implements Format {
		
		MIDI 		(List.of("mid", "midi")),
		MUSICXML 	(List.of("mxl")),
		ABC			(List.of("abc"));
		
		private final List<String> endings;
		
		private SymbolicFormat(List<String> endings) {
			this.endings = endings;
		}
		
		public static SymbolicFormat getFormat(File file) {
			for(SymbolicFormat format : SymbolicFormat.values()) {
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
				case MIDI: {
					try {
						MidiFileFormat midifileformat = MidiSystem.getMidiFileFormat(file);
						return true;
					} catch (InvalidMidiDataException e) {
						return false;
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;
				}
				case MUSICXML: {
					return true;
				}
				default: return false;
			}
		}

		@Override
		public List<String> getEndings() {
			return endings;
		}
	}
	
	private final ModalityEnum modalityEnum = ModalityEnum.SYMBOLIC;
	
	private List<SymbolicFormat> formats;
	
	public SymbolicModality(List<SymbolicFormat> formats) {
		this.formats = formats;
	}
	
	/** Returns an array with all possible symbolic file endings */
	public static List<String> getEndings() {
		List<String> endings = new ArrayList<String>();
		for (SymbolicFormat format : SymbolicFormat.values()) {
			endings.addAll(format.endings);
		}
		return endings;
	}
	
	@Override
	public List<SymbolicFormat> getFormats() {
		return formats;
	}

	@Override
	public boolean matchesRequirements(File file) {

		for(SymbolicFormat symbolicFormat : this.formats) {
			boolean fileEndingMatchesRequirements = symbolicFormat.matchesEndings(file);
			boolean fileFormatConfirmed = symbolicFormat.confirmFormat(file);
			
			if(fileEndingMatchesRequirements && fileFormatConfirmed) {
				return true;
			} else if (fileEndingMatchesRequirements && !fileFormatConfirmed) {
				AmuseLogger.write(this.getClass().getName(), Level.WARN,"The symbolic file format could not be confirmed and might be broken: " + file.getName());
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
