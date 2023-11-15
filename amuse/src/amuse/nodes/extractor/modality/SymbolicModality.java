package amuse.nodes.extractor.modality;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;

import amuse.nodes.extractor.modality.AudioModality.AudioFormat;

public class SymbolicModality implements Modality {

	public enum SymbolicFormat{MIDI, MUSICXML}
	
	private List<SymbolicFormat> formats;
	
	public SymbolicModality(List<SymbolicFormat> formats) {
		this.formats = formats;
	}
	
	@Override
	public List<SymbolicFormat> getFormats() {
		return formats;
	}

	@Override
	public boolean matchesRequirements(File file) {
		/*Check, if file is appropriate midi data*/
		try {
			MidiFileFormat midifileformat = MidiSystem.getMidiFileFormat(file);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
