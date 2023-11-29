package amuse.data.modality;

import java.io.File;

public interface FormatInterface {
	
	/** Returns true, if file matches this formats file endings. */
	public boolean matchesEndings(File file);
	
	/** Provides methods to confirm the files format.
	 * Returns true, if the file is appropriate data of this format. */
	public boolean confirmFormat(File file);
}
