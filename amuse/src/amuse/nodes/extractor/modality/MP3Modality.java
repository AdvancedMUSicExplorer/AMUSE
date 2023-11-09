package amuse.nodes.extractor.modality;

import java.io.File;
import java.util.List;

/**
 * MP3Modality objects can be used by Extractor-Tools to specify that they accept MP3-input and define the conditions.
 */
public class MP3Modality implements AudioModality {
	
	public enum MP3Format {MP3};
	
	public List<MP3Format> formats = List.of(MP3Format.MP3);
	public List<Integer> sampleRates;
	
	public MP3Modality(List<Integer> sampleRates) {
		this.sampleRates = sampleRates;
	}
	
	@Override
	public List<MP3Format> getFormats() {
		return null;
	}

	@Override
	public boolean matchesRequirements(File file) {
		// TODO Auto-generated method stub
		return false;
	}
}
