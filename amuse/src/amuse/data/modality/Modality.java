package amuse.data.modality;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This interface defines the operations which should be supported by all modalities.
 */
public interface Modality {
	
	public enum ModalityEnum{
		SYMBOLIC("Symbolic"),
		AUDIO("Audio");
		
		private String genericName;
		
		private ModalityEnum(String genericName) {
			this.genericName = genericName;
		}
		
		/** Returns a list of all endings that are associated with 
		 * this modality in general and are listed in their modality class. */
		public List<String> getEndings() {
			switch(this) {
				case SYMBOLIC: 	return SymbolicModality.getEndings();
				case AUDIO: 	return AudioModality.getEndings();
				default: 		return ModalityEnum.getAllEndings();
			}
		}
		
		/** Returns a list of all file endings */
		public static List<String> getAllEndings() {
			List<String> allEndings = new ArrayList<String>();
			for(ModalityEnum modality: ModalityEnum.values()) {
				allEndings.addAll(modality.getEndings());
			}
			return allEndings;
		}
		
		public static ModalityEnum getByGenericName(String modalityString) {
			for (ModalityEnum modality : ModalityEnum.values()) {
				if(modality.getGenericName() == modalityString) {
					return modality;
				}
			}
			throw new IllegalArgumentException("ModalityEnum could not be found.");
		}
		
		public String getGenericName() {
			return genericName;
		}
		
		/** Returns true, if the file ending of the given fits this modality. */
		public boolean fitsModality(File file) {
			List<String> endings = this.getEndings();
			String fileName = file.getName();
			for (String ending: endings) {
				if(fileName.endsWith(ending)) {
					return true;
				}
			}
			return false;
		}

		/** Returns true, if the file ending of this file fits any modality. */
		public static boolean fitsAnyModality(File file) {
			List<String> endings = ModalityEnum.getAllEndings();
			String fileName = file.getName();
			for (String ending: endings) {
				if(fileName.endsWith(ending)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/** Returns a list of all supported formats of the modality object. */
	public List<?> getFormats();
	
	/** Returns true, if the given file matches 
	 * the supported formats of the modality object. */
	public boolean matchesRequirements(File file);
	
	public ModalityEnum getModalityEnum();
	
}

