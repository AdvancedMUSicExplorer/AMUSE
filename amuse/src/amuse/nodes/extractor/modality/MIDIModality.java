package amuse.nodes.extractor.modality;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;

/**
 * MIDIModality objects can be used by Extractor-Tools to specify that they accept MIDI-input and define the conditions.
 */
public class MIDIModality implements SymbolicModality {
	
	public enum MIDIFormat{SMF0, SMF1, SMF2}
	
	/** List of supported formats */
	private final List<MIDIFormat> formats;
	
	public MIDIModality(List<MIDIFormat> formats) throws IOException {
		this.formats = formats;
		
		if(formats.isEmpty()) {
			throw new IOException("MIDIModality has to support at least one format.");
		}
	}
	
	@Override
	public List<MIDIFormat> getFormats() {
		return formats;
	}
	
	@Override
	public boolean matchesRequirements(File file) {
		try {
			/*Check, if file is appropriate midi data*/
			MidiFileFormat midifileformat = MidiSystem.getMidiFileFormat(file);
			
			/*Check, if midi-file matches the supported formats*/
			int type = midifileformat.getType();
			boolean modalitySupportsFormat = false;
			for (MIDIFormat format: formats) {
				if(format.ordinal() == type) {
					modalitySupportsFormat = true;
				}
			}
			if (!modalitySupportsFormat) {
				return false;
			}
			
		} catch (InvalidMidiDataException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "The given file could not be used as MIDI-input: " + file.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}
