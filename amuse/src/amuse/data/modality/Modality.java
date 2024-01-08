package amuse.data.modality;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import amuse.data.modality.SymbolicModality.SymbolicFormat;
import amuse.data.modality.AudioModality.AudioFormat;

/**
 * This interface defines the operations which should be supported by all modalities.
 */
public interface Modality {
	
	public enum ModalityEnum{
		SYMBOLIC("Symbolic"),
		AUDIO("Audio");
		
		private String genericName;
		
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
		
		/** Returns the ModalityEnum, that fits the file ending. */
		public static ModalityEnum getModalityEnum(File file) {
			String fileName = file.getName();
			for(ModalityEnum modality: ModalityEnum.values()) {
				List<String> endings = modality.getEndings();
				for (String ending: endings) {
					if(fileName.endsWith(ending)) {
						return modality;
					}
				}
			}
			return null;
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
	
	/** Returns a list of all formats listed in modality classes */
	public static List<Format> getAllFormats() {
		List<Format> formats = new ArrayList<Format>();
		formats.addAll(Arrays.asList(SymbolicFormat.values()));
		formats.addAll(Arrays.asList(AudioFormat.values()));
		return formats;
	}
	
	/** Returns format enum by checking the fileending. */
	public static Format getFormat(File file) {
		for(Format format : getAllFormats()) {
			for(String ending : format.getEndings()) {
				if(file.getPath().endsWith(ending)) {
					return format;
				}
			}
		}
		return null;
	}
	
	/** Returns a list of all supported formats of the modality object. */
	public List<?> getFormats();
	
	/** Returns true, if the given file matches 
	 * the supported formats of the modality object. */
	public boolean matchesRequirements(File file);
	
	/** Returns corresponding ModalityEnum object. */
	public ModalityEnum getModalityEnum();
}

