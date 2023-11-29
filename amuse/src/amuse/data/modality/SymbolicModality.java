package amuse.data.modality;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;

public class SymbolicModality implements Modality {

	public enum SymbolicFormat {
		
		MIDI 		(List.of("mid", "midi")),
		MUSICXML 	(List.of("mxl"));
		
		private final List<String> endings;
		
		private SymbolicFormat(List<String> endings) {
			this.endings = endings;
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
		/*Check, if file is appropriate midi data*/
		if(file.getName().endsWith(".mid")) {
			try {
				MidiFileFormat midifileformat = MidiSystem.getMidiFileFormat(file);
				return true;
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public ModalityEnum getModalityEnum() {
		return modalityEnum;
	}
	
}
