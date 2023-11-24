package amuse.nodes.extractor.modality;

import java.io.File;
import java.util.List;

/**
 * This interface defines the operations which should be supported by all modalities.
 */
public interface Modality {
	
	public enum ModalityEnum{
		SYMBOLIC,
		AUDIO;
		
		public List<String> getEndings() {
			switch(this) {
			case SYMBOLIC: 	return SymbolicModality.getEndings();
			case AUDIO: 	return AudioModality.getEndings();
			default: 		return null;
			}
		}
	}
	
	public List<?> getFormats();
	
	public boolean matchesRequirements(File file);
}
