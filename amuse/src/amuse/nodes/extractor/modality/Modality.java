package amuse.nodes.extractor.modality;

import java.io.File;
import java.util.List;

/**
 * This interface defines the operations which should be supported by all modalities.
 */
public interface Modality {
	
	public List getFormats();
	
	public boolean matchesRequirements(File file);
}
