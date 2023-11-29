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
		
		public List<String> getEndings() {
			switch(this) {
				case SYMBOLIC: 	return SymbolicModality.getEndings();
				case AUDIO: 	return AudioModality.getEndings();
				default: 		return ModalityEnum.getAllEndings();
			}
		}
		
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
	
	public List<?> getFormats();
	
	public boolean matchesRequirements(File file);
	
	public ModalityEnum getModalityEnum();
}
