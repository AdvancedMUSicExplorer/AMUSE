package amuse.nodes.extractor.modality;

import java.util.List;

public class MP3Modality implements AudioModality {
	
	public enum MP3Format {MP3};
	public int sampleRate;
	
	public List<MP3Format> getFormat() {
		return null;
	}
}
