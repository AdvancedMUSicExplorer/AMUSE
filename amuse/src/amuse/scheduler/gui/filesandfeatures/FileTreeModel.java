/**
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 *
 * Copyright 2006-2010 by code authors
 *
 * Created at TU Dortmund, Chair of Algorithm Engineering
 * (Contact: <http://ls11-www.cs.tu-dortmund.de>)
 *
 * AMUSE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMUSE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
 *
 * Creation date: 09.03.2009
 */

package amuse.scheduler.gui.filesandfeatures;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import amuse.data.modality.Modality.ModalityEnum;

/**
 *
 * @author Clemens Waeltken
 */
public class FileTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = -8847125059405665407L;
	private File relativeToFolder;
    private List<String> fileEndings = ModalityEnum.getAllEndings();
    private DefaultMutableTreeNode relativeToNode = new DefaultMutableTreeNode("Music Database", true);
    private DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(File.separator, true);
    
    private Vector<TreeModelModalityListener> listeners = new Vector<TreeModelModalityListener>();

    /**
     * Creates a new <class>FileTreeModel</class>.
     * @param relativeFolder The folder to display files relative to (e.g. MusicDatabase).
     * @param label The label used for the relative folder (e.g. "Music Database").
     */
    public FileTreeModel(File relativeFolder, String label) {
        super(new DefaultMutableTreeNode("root"));
        this.relativeToFolder = relativeFolder;
        this.relativeToNode = new DefaultMutableTreeNode(label, true);
        
        if (!this.relativeToFolder.isDirectory()) {
        	JOptionPane.showMessageDialog(null,
                    this.relativeToFolder.getPath() + " is not a folder!",
                    "Music database is not a folder",
                    JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException(this.relativeToFolder.getName() + " is not a folder!");
        }
    }
    
    public void addTreeModelModalityListener(TreeModelModalityListener listener) {
	    listeners.add(listener);
    }
    
    public void removeTreeModelModalityListener(TreeModelModalityListener listener) {
        listeners.remove(listener);
    }
    
    /** 
     * Notify all listeners, that a single file was added. 
     * @param modality of the added file
     */
    private void notifyModalityListenersAdd(List<ModalityEnum> modalities) {
    	this.updateEndings(modalities);
        for (TreeModelModalityListener listener : listeners) {
        	listener.fileAdded(modalities);
        }
    }
    
    /** Notify all listeners, that all files were removed. */
    private void notifyModalityListenersRemovedAll() {
    	this.updateEndings(null);
        for (TreeModelModalityListener listener : listeners) {
        	listener.allFilesRemoved();
        }
    }
    
    /** Notify listeners, if files were removed and the remaining files fit one modality. */
    public void notifyModalityListenersRemovedSelected() {
    	List<ModalityEnum> modalities = new ArrayList<ModalityEnum>();
    	List<File> files = getFiles();
    	// If there are no files remaining
    	if(files.size() == 0) {
    		notifyModalityListenersRemovedAll();
    	} else {
    		for(File file: files) {
        		ModalityEnum currentModality = ModalityEnum.getModalityEnum(file);
        		if(!modalities.contains(currentModality)) {
        			modalities.add(currentModality);
        		}
        	}
            listeners.forEach(listener -> listener.selectedFilesRemoved(modalities));
    		this.updateEndings(modalities);
    	}
	}
    

    /** 
     * Updates the accepted file endings according to the given modalities.
     * @param modalities
     */
	public void updateEndings(List<ModalityEnum> modalities) {
        if(modalities == null || modalities.isEmpty()) {
        	this.fileEndings = ModalityEnum.getAllEndings();
        } else {
        	for(ModalityEnum modality: modalities) {
        		this.fileEndings.clear();
        		this.fileEndings.addAll(modality.getEndings());
        	}
        }
    }

    /**
     * Adds a single file or files in a folder to this model.
     * @param file or folder to add to the model.
     */
    protected void addFile(File file) {
        if (!file.isDirectory() && ModalityEnum.fitsAnyModality(file)) {
            insertInTree(file);
            List<ModalityEnum> modalities = new ArrayList<ModalityEnum>();
            ModalityEnum currentModality = ModalityEnum.getModalityEnum(file);
        	if(!modalities.contains(currentModality)) {
        		modalities.add(currentModality);
        	}
        	this.nodeStructureChanged(root);
            this.notifyModalityListenersAdd(modalities);
        }
        if (file.isDirectory() && hasNonHiddenContent(file)) {
    		Vector<File> files = new Vector<File>();
            for (File child : file.listFiles()) {
                files.add(child);
            }
            Collections.sort(files);
            for (File child : files) {
                	addFile(child);
            }
        }
    }

    /**
     * Returns all files in the current FileTreeModel
     * @return complete list of files in this model.
     */
    public List<File> getFiles() {
        Vector<File> files = new Vector<File>();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) root;
        Enumeration<?> nodes = rootNode.breadthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
            if (node.isLeaf()) {
                if (node.getUserObject() instanceof File) {
                    files.add((File) node.getUserObject());
                }
            }
        }
        return files;
    }

    File getRelativFolder() {
        return relativeToFolder;
    }

    /** Removes all files from the fileTree. */
    public void removeAllFiles() {
        mainNode.removeAllChildren();
        mainNode.removeFromParent();
        relativeToNode.removeAllChildren();
        relativeToNode.removeFromParent();
        mainNode.removeFromParent();
        this.reload();
        this.notifyModalityListenersRemovedAll();
    }

    public void removeNode(DefaultMutableTreeNode node) {
        node.removeAllChildren();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        node.removeFromParent();
        if (parent != null && parent.isLeaf()) {
            removeNode(parent);
        }
        this.nodeStructureChanged(node);
    }

    private void insertInTree(File file) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) root;
        StringBuilder path = new StringBuilder(file.getAbsolutePath());
        if (isInRelPath(file)) {
            path.setLength(0);
            path.append(file.getAbsolutePath().substring(relativeToFolder.getAbsolutePath().length() + 1));
            node.add(relativeToNode);
            relativeToNode.setParent(node);
            node = relativeToNode;
        } else {
            node.add(mainNode);
            mainNode.setParent(node);
            node = mainNode;
        }
        // Insert nodes:
        StringBuilder donePath = new StringBuilder(File.separator);
        // Drop first pathseperator:
        if (path.charAt(0) == File.separatorChar) {
            path = path.deleteCharAt(0);
        }
        while (path.indexOf(File.separator) != -1) {
            donePath.append(path.substring(0, path.indexOf(File.separator) + 1));
            Enumeration<FileMutableTreeNode> children = (Enumeration)node.children();
            File expectedFile = new File(donePath.toString());
            FileMutableTreeNode existingChild = null;
            while (children.hasMoreElements()) {
                FileMutableTreeNode child = children.nextElement();
                if (child.getFile().equals(expectedFile)) {
                    existingChild = child;
                }
            }
            if (existingChild == null) {
            	// System.out.println("Creating node: "+ expectedFile.getName());
                existingChild = new FileMutableTreeNode(expectedFile);
                node.add(existingChild);
            }
            node = existingChild;
            path.delete(0, path.indexOf(File.separator) + 1);
        }
        Enumeration<FileMutableTreeNode> children = (Enumeration)node.children();
        FileMutableTreeNode existingChild = null;
        File expectedFile = file;
        while (children.hasMoreElements()) {
            FileMutableTreeNode child = children.nextElement();
            if (child.getFile().equals(expectedFile)) {
                existingChild = child;
            }
        }
        if (existingChild == null) {
            existingChild = new FileMutableTreeNode(expectedFile);
            node.add(existingChild);
        }
    }

    private boolean isInRelPath(File file) {
        return file.getAbsolutePath().startsWith(relativeToFolder.getAbsolutePath());
    }

    private boolean hasNonHiddenContent(File file) {
        for (File content : file.listFiles()) {
            if (!content.isHidden()) {
                if (content.isFile() && hasCorrectEnding(content)) {
                    return true;
                }
                if (content.isDirectory() && hasNonHiddenContent(content)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns true, if the given file fits one of the currently accepted endings. */
    private boolean hasCorrectEnding(File file) {
        for (String ending : this.fileEndings) {
            if (file.getName().endsWith('.'+ending)) {
                return true;
            }
        }
        return false;
    }

    private class FileMutableTreeNode extends DefaultMutableTreeNode {

        private static final long serialVersionUID = -5250973108098843435L;

        /**
         * @param file
         */
        public FileMutableTreeNode(File file) {
            super(file);
        }

        @Override
        public String toString() {
            File f = (File) this.userObject;
            return f.getName();
        }

        public File getFile() {
            return (File) this.getUserObject();
        }
    }

}
