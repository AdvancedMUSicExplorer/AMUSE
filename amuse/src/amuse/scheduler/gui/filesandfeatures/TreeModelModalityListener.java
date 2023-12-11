package amuse.scheduler.gui.filesandfeatures;

import amuse.data.modality.Modality.ModalityEnum;

/**
 * This interface is used to listen to changes in FileTreeModel,
 * that are related to the modality of an extraction experiment.
 * 
 * @author Clara Pingel
 */
public interface TreeModelModalityListener {
	
	/** 
	 * This method is called each time a single file or folder is added to the fileTree. 
	 * @param modality of the added file
	 */
	public void fileAdded(ModalityEnum modality);
	
	/** 
	 * This method is called, if all files were removed from the fileTree. 
	 */
	public void allFilesRemoved();
	
	/** 
	 * This method is called, if selected files were removed from the fileTree
	 * and only files of one modality remain.  
	 * @param remaining modality
	 */
	public void selectedFilesRemoved(ModalityEnum modality);
}
