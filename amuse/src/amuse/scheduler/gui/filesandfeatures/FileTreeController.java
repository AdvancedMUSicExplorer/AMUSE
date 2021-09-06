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

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import amuse.data.datasets.FileTableSet;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;

/**
 * Controller Class for MusicFileTrees.
 * @author Clemens Waeltken
 */
public class FileTreeController implements ActionListener, KeyListener {

    FileTreeModel model;
    FileTreeView view;
    private String fileFilterDescription = "wav and mp3 supported";

    private File filelistFolder = new File("experiments" + File.separator + "filelists");

    /**
     * Creates a new FileTreeController.
     * @param model The <class>FileTreeModel</class> to display.
     * @param view The <class>FileTreeView</class> to display in.
     */
    public FileTreeController(FileTreeModel model, FileTreeView view) {
        this.model = model;
        this.view = view;
        view.setModel(this.model);
        view.setController(this);
    }

    /**
     * Handles all actions performed by the user in FileTreeView.
     * @param e the Action event create by FileTreeView.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("save")) {
            if (model.getFiles().size() == 0) {
                JOptionPane.showMessageDialog(view.getView(), "Please add files first!", "Nothing to Save!", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 1. FileName:
            JFileChooser saveDialog = new SelectArffFileChooser("File List", filelistFolder);
            int option = saveDialog.showDialog(view.getView(), "Save to ARFF File");
            if (option == JFileChooser.CANCEL_OPTION) {
                return;
            }
            File file = saveDialog.getSelectedFile();
            if (!file.getName().endsWith(".arff")) {
                file = new File(file.getAbsolutePath() + ".arff");
            //JOptionPane.showMessageDialog(fileTree, "Added \".arff\" filename extension.", "Added filename extension!", JOptionPane.INFORMATION_MESSAGE);
            }
            // 2. Existing file list?
            if (file.exists()) {
                int selected = JOptionPane.showConfirmDialog(view.getView(), "Do you want to override this file? " + file.getName(), "File already exists!", JOptionPane.YES_NO_OPTION);
                if (selected == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            // 3. Create DataSet:
            List<File> fileList = new Vector<File>();
            for (File musicFile : model.getFiles()) {
                fileList.add(musicFile);
            }
            FileTableSet fileSet = new FileTableSet(fileList);
//            System.out.println(fileSet.getValueCount());
            try {
                // 4. Write file
                fileSet.saveToArffFile(file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view.getView(), ex, "Error writing FileList!", JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showMessageDialog(view.getView(), "FileList successfully saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getActionCommand().equals("load")) {
            // 1. FileName:
            JFileChooser loadDialog = new SelectArffFileChooser("File List(s)", filelistFolder);
            loadDialog.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
            loadDialog.setAcceptAllFileFilterUsed(false);
            loadDialog.setMultiSelectionEnabled(true);
            loadDialog.setCurrentDirectory(new File(AmusePreferences.get(KeysStringValue.MULTIPLE_TRACKS_ANNOTATION_DATABASE)));
            loadDialog.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File arg0) {
                    if (arg0.isDirectory() || arg0.getName().endsWith(".arff")) {
                        return true;
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return getFileFilterDescription();
                }
            });
            int returnValue = loadDialog.showDialog(view.getView(), "Load from ARFF File(s)");
            if (returnValue == javax.swing.JFileChooser.APPROVE_OPTION) {
                for (File f : loadDialog.getSelectedFiles()) {
                	if(f.isDirectory()) {
                		for(File file : f.listFiles()) {
                			if(file.getName().endsWith(".arff")) {
                				loadFileList(file);
                			}
                		}
                	} else {
                		loadFileList(f);
                	}
                }
            }
        } else if (e.getActionCommand().equals("add")) {
            JFileChooser jFileChooserSelect = new JFileChooser();
            jFileChooserSelect.setDialogTitle("Add Music File/Folder");
            jFileChooserSelect.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
            jFileChooserSelect.setAcceptAllFileFilterUsed(false);
            jFileChooserSelect.setMultiSelectionEnabled(true);
            jFileChooserSelect.setCurrentDirectory(model.getRelativFolder());
            jFileChooserSelect.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File arg0) {
                    if (arg0.isDirectory() || arg0.getName().endsWith(".mp3") || arg0.getName().endsWith(".wav")) {
                        return true;
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return getFileFilterDescription();
                }
            });
            int returnValue = jFileChooserSelect.showDialog(view.getView(), "Select File/Folder");
            if (returnValue == javax.swing.JFileChooser.APPROVE_OPTION) {
                for (File f : jFileChooserSelect.getSelectedFiles()) {
                    model.addFile(f);
                }
            }
        } else if (e.getActionCommand().equals("remove")) {
            removeSelectedNodes();
        } else if(e.getActionCommand().equals("remove all")) {
        	model.removeAllFiles();
        }
    }

    /**
     * Load a List of MusicFiles by given arff music file list.
     * @param file arff File containing absolute paths to music files.
     * @throws HeadlessException
     */
    public void loadFileList(File file) throws HeadlessException {
        // 3. Load DataSet:
        FileTableSet fileSet = null;
        try {
            fileSet = new FileTableSet(file);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(view.getView(), "Unable to Load FileList : " + ex.getMessage(), "Error Loading FileList", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<File> fileList = fileSet.getFiles();
        loadFiles(fileList);
    }

    /**
     * Load a List of files into the current FileTree.
     * @param files
     */
    public void loadFiles(List<File> files) {
        // model.removeAllFiles();
        for (File currentFile : files) {
            model.addFile(currentFile);
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * If delete key is pressed remove current selection from file tree.
     * @param e
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            removeSelectedNodes();
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        // Nothing ToDo
    }

    private String getFileFilterDescription() {
        return fileFilterDescription;
    }

    private void removeSelectedNodes() {
        TreePath[] selectedNodes = view.getSelectionPaths();
        if (selectedNodes != null) {
            for (TreePath path : selectedNodes) {
                model.removeNode((DefaultMutableTreeNode) path.getLastPathComponent());
            }
        } else {
            JOptionPane.showMessageDialog(view.getView(), "Select atleast one file to remove.", "No file selcted!", JOptionPane.WARNING_MESSAGE);
        }
    }
    
}
