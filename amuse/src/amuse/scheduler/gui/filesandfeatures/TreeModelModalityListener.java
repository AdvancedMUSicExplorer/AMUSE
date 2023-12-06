package amuse.scheduler.gui.filesandfeatures;

import amuse.data.modality.Modality.ModalityEnum;

public interface TreeModelModalityListener {
	
	public void fileAdded(ModalityEnum modality);
	
	public void allFilesRemoved();
}
