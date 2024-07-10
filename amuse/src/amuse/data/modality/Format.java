package amuse.data.modality;

import java.io.File;
import java.util.List;
import amuse.data.modality.AudioModality.AudioFormat;
import amuse.data.modality.SymbolicModality.SymbolicFormat;

/**
 * This interface defines the operations which should be supported by all format enum classes.
 * 
 * @author Clara Pingel
 */
public interface Format {
	
	/** Returns true, if file matches this formats file endings. */
	public boolean matchesEndings(File file);
	
	/** Provides methods to confirm the files format.
	 * Returns true, if the file is appropriate data of this format. */
	public boolean confirmFormat(File file);
	
	/** Returns the formats endings. */
	public List<String> getEndings();
	
	public static Format getFormatByString(String format) {
		Format[] audioFormats = AudioFormat.values();
		for(Format audioFormat: audioFormats) {
			if(audioFormat.toString().equals(format)) {
				return audioFormat;
			}
		}
		Format[] symbolicFormats = SymbolicFormat.values();
		for(Format symbolicFormat: symbolicFormats) {
			if(symbolicFormat.toString().equals(format)) {
				return symbolicFormat;
			}
		}
		return null;
	}
}
