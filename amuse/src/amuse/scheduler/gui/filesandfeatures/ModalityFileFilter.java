package amuse.scheduler.gui.filesandfeatures;

import amuse.data.modality.Modality.ModalityEnum;
import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * This class is used to filter files by a given modality.
 * 
 * @author Clara Pingel
 *
 */
public class ModalityFileFilter extends FileFilter{
	
	private final ModalityEnum modality;

	/**
	 * @param modality (can be null, if all modalities are accepted)
	 */
	public ModalityFileFilter(ModalityEnum modality) {
		this.modality = modality;
	}
	
	@Override
	public boolean accept(File file) {
		boolean fileFitsModality;
		if(this.modality == null) {
			fileFitsModality = ModalityEnum.fitsAnyModality(file);
		} else {
			fileFitsModality = this.modality.fitsModality(file);
		}
		return (file.isDirectory() || fileFitsModality);
	}

	@Override
	public String getDescription() {
		String genericDescription = "music files";
		if(this.modality == null) {
			return genericDescription;
		} else {
			return this.modality.getGenericName() + " " + genericDescription;
		}
	}
	
	public ModalityEnum getModalityEnum() {
		return modality;
	}

}
