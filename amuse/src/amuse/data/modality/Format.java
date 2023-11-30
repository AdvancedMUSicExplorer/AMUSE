package amuse.data.modality;

import java.io.File;
import java.util.List;

/**
 * This interface defines the operations which should be supported by all format enum classes.
 */
public interface Format {
	
	/** Returns true, if file matches this formats file endings. */
	public boolean matchesEndings(File file);
	
	/** Provides methods to confirm the files format.
	 * Returns true, if the file is appropriate data of this format. */
	public boolean confirmFormat(File file);
	
	/** Returns the formats endings. */
	public List<String> getEndings();
}
