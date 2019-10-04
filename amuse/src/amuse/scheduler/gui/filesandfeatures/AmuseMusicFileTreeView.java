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
 * Creation date: 23.07.2008
 */

package amuse.scheduler.gui.filesandfeatures;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetException;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;

/**
 * @author Clemens Waeltken
 * A View showing Music files in a JTree according to the folder they are located in.
 */
public class AmuseMusicFileTreeView {

    private static final long serialVersionUID = 3603262664571036628L;
    private JPanel view = new JPanel();
    private JTree fileTree;
    private File relativeToPath;
    private DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode("Music Database", true);
    private DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(File.separator, true);
    private String[] fileEndings;
    private JPanel buttonPanel = null;
    private JButton addButton = null;
    private JButton removeBotton = null;
    private JButton saveFileListButton = null;
    private JButton loadFileListButton = null;

    /**
     * Create a new FileTreeView
     * @param acceptedFileEndings Array of accepted file types.
     * @param relativeToPath The relative Path is used as a root node for this tree.
     */
    public AmuseMusicFileTreeView(String[] acceptedFileEndings, File relativeToPath) {
        super();
        this.relativeToPath = relativeToPath;
        this.fileEndings = acceptedFileEndings;
        initialize();
    }

    /**
     * Get all Files in this FileTree
     * @return List of all files in the tree.
     */
    public List<File> getFiles() {
        Vector<File> files = new Vector<File>();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) fileTree.getModel().getRoot();
        Enumeration nodes = rootNode.breadthFirstEnumeration();
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

    /**
     * Get the JPanel of this view.
     * Place this in your own view to show this FileTree.
     * @return a JPanel containing a FileTree.
     */
    public JPanel getView() {
        return view;
    }

    private void initialize() {
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("root");
        // Create a tree that allows one selection at a time.
        fileTree = new JTree(treeRoot);
        BorderLayout layout = new BorderLayout();
        view.setLayout(layout);
        fileTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        fileTree.setRootVisible(false);
        // Create the scroll pane and add the fileTree to it.
        JScrollPane treeView = new JScrollPane(fileTree);
        // Add the scroll pane to this panel.
        view.add(treeView, BorderLayout.CENTER);
        view.add(getButtonPanel(), BorderLayout.SOUTH);
        fileTree.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    removeSelectedNodes();
                }
            }
            // Nothing to do here.

            @Override
            public void keyReleased(KeyEvent e) {
            }
            // Nothing to do here.

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
        // Add TiteldBorder
        view.setBorder(new TitledBorder("Select Music Files"));
    }

    /**
     * @return
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
            buttonPanel.setLayout(layout);
            buttonPanel.add(getAddButton());
            buttonPanel.add(getRemoveButton());
            buttonPanel.add(Box.createHorizontalGlue());
            buttonPanel.add(getLoadButton());
            buttonPanel.add(getSaveButton());
        }
        return buttonPanel;
    }

    /**
     * @return
     */
    private JButton getAddButton() {
        if (addButton == null) {
            addButton = new JButton();
            addButton.setText("Add Files");
            addButton.addActionListener(new AddButtonListener());
        }
        return addButton;
    }

    private JButton getSaveButton() {
        if (saveFileListButton == null) {
            saveFileListButton = new JButton();
            saveFileListButton.setText("->|");
            saveFileListButton.setToolTipText("Save this FileList");
            saveFileListButton.addActionListener(new ActionListenerSaveButton());
        }
        return saveFileListButton;
    }

    private JButton getLoadButton() {
        if (loadFileListButton == null) {
            loadFileListButton = new JButton();
            loadFileListButton.setText("<-|");
            loadFileListButton.setToolTipText("Load FileFist");
            loadFileListButton.addActionListener(new ActionListenerLoadButton());
        }
        return loadFileListButton;
    }

    /**
     * @return returns the correct description for the FileFilter used in FileChooser.
     */
    private String getFileFilterDescription() {
        String description = "";
        int index = 0;
        if (fileEndings.length == 1) {
            description = fileEndings[0] + " is supported";
            return description;
        }
        while (index < fileEndings.length - 2) {
            description = description + fileEndings[index] + ", ";
            index++;
        }
        if (fileEndings.length > 1) {
            description = description + fileEndings[index] + " and ";
        }
        description = description + fileEndings[index + 1];
        return description + " are supported";
    }

    /**
     * @return
     */
    private JButton getRemoveButton() {
        if (removeBotton == null) {
            removeBotton = new JButton();
            removeBotton.setText("Remove Selected");
            removeBotton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    removeSelectedNodes();
                }
            });
        }
        return removeBotton;
    }

    /**
     * @param file
     */
    protected void addFile(File file) {
//        System.out.println("Path to Music DB: " + relativeToPath.getAbsolutePath());
//        System.out.println("File to Add: "+ file.getAbsolutePath());
        if (file.isFile() && hasCorrectEnding(file)) {
            insertInTree(file);
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
        DefaultTreeModel model = (DefaultTreeModel) fileTree.getModel();
        model.reload();
    }

    private void insertInTree(File file) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getModel().getRoot();
        StringBuilder path = new StringBuilder(file.getAbsolutePath());
        if (isInRelPath(file)) {
            path.setLength(0);
            path.append(file.getAbsolutePath().substring(relativeToPath.getAbsolutePath().length() + 1));
            node.add(databaseNode);
            databaseNode.setParent(node);
            node = databaseNode;
        } else {
            node.add(mainNode);
            mainNode.setParent(node);
            node = mainNode;
        }
        // Insert nodes:
        StringBuilder donePath = new StringBuilder(File.separator);
        // Drop first pathseperator:
        if (path.charAt(0) == '/') {
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
//                System.out.println("Creating node: "+ expectedFile.getName());
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
//                System.out.println("Creating leaf: "+ expectedFile.getAbsolutePath());
            existingChild = new FileMutableTreeNode(expectedFile);
            node.add(existingChild);
        }
    }

    private boolean isInRelPath(File file) {
        return file.getAbsolutePath().startsWith(relativeToPath.getAbsolutePath());
    }

    private void removeSelectedNodes() {
        TreePath[] selectedNodes = fileTree.getSelectionPaths();
        if (selectedNodes != null) {
            for (TreePath path : selectedNodes) {
                removeNode((DefaultMutableTreeNode) path.getLastPathComponent());
            }
        } else {
            JOptionPane.showMessageDialog(view, "Select atleast one file to remove.", "No file selcted!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeNode(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        int[] childIndex = new int[1];
        childIndex[0] = parent.getIndex(node);
        DefaultMutableTreeNode[] nodeAsArray = new DefaultMutableTreeNode[1];
        nodeAsArray[0] = node;
        node.removeAllChildren();
        node.removeFromParent();
        DefaultTreeModel treeModel = (DefaultTreeModel) fileTree.getModel();
        treeModel.nodesWereRemoved(parent, childIndex, nodeAsArray);
    }


    private void removeAllFiles() {
        mainNode.removeAllChildren();
        mainNode.removeFromParent();
        databaseNode.removeAllChildren();
        mainNode.removeFromParent();
        DefaultTreeModel treeModel = (DefaultTreeModel) fileTree.getModel();
        treeModel.reload();
    }

    private boolean hasNonHiddenContent(File f) {
        for (File content : f.listFiles()) {
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

    private boolean hasCorrectEnding(File f) {
        for (String ending : fileEndings) {
            if (f.getName().endsWith(ending)) {
                return true;
            }
        }
        return false;
    }

    private class SelectArffDialog extends JFileChooser {

        public SelectArffDialog() {
            super("experiments" + File.separator + "filelists");
            this.setAcceptAllFileFilterUsed(false);
            this.setFileSelectionMode(JFileChooser.FILES_ONLY);
            this.setDialogTitle("Select ARFF File List");
            this.setMultiSelectionEnabled(false);
            this.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".arff");
                }

                @Override
                public String getDescription() {
                    return "ARFF FileLists";
                }
            });
        }
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

    private class AddButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            JFileChooser jFileChooserSelect = new JFileChooser();
            jFileChooserSelect.setDialogTitle("Add Music File/Folder");
            jFileChooserSelect.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
            jFileChooserSelect.setAcceptAllFileFilterUsed(false);
            jFileChooserSelect.setMultiSelectionEnabled(true);
            jFileChooserSelect.setCurrentDirectory(relativeToPath);
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
            int returnValue = jFileChooserSelect.showDialog(view, "Select File/Folder");
            if (returnValue == javax.swing.JFileChooser.APPROVE_OPTION) {
                for (File f : jFileChooserSelect.getSelectedFiles()) {
                    addFile(f);
                }
            }
        }
    }

    private class ActionListenerSaveButton implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getFiles().size() == 0) {
                JOptionPane.showMessageDialog(fileTree, "Please add files first!", "Nothing to Save!", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 1. FileName:
            SelectArffDialog saveDialog = new SelectArffDialog();
            int option = saveDialog.showDialog(fileTree, "Save to ARFF File");
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
                int selected = JOptionPane.showConfirmDialog(view, "Do you want to override this file? " + file.getName(), "File already exists!", JOptionPane.YES_NO_OPTION);
                if (selected == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            // 3. Create DataSet:
            ArffDataSet fileSet = new ArffDataSet("Music Files");
            List<Double> idList = new Vector<Double>();
            List<String> fileList = new Vector<String>();
            double id = 1;
            for (File musicFile : getFiles()) {
                idList.add(id);
                fileList.add(musicFile.getAbsolutePath());
                id++;
            }
            fileSet.addAttribute(new NumericAttribute("Id", idList));
            fileSet.addAttribute(new StringAttribute("Path", fileList));
//            System.out.println(fileSet.getValueCount());
            try {
                // 4. Write file
                fileSet.saveToArffFile(file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, ex, "Error writing FileList!", JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showMessageDialog(view, "FileList successfully saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class ActionListenerLoadButton implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // 1. FileName:
            SelectArffDialog loadDialog = new SelectArffDialog();
            File file = null;
            while (true) {
                int option = loadDialog.showDialog(fileTree, "Load from ARFF File");
                if (option == JFileChooser.CANCEL_OPTION) {
                    return;
                }
                file = loadDialog.getSelectedFile();
                if (file.exists()) {
                    break;
                } else {
                    JOptionPane.showMessageDialog(fileTree, "Selected file does not exist!", "Missing File", JOptionPane.ERROR_MESSAGE);
                }
            }
            // 3. Load DataSet:
            DataSetAbstract fileSet = null;
            try {
                fileSet = new ArffDataSet(file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(fileTree, "Unable to Load FileList : " + ex.getMessage(), "Error Loading FileList", JOptionPane.ERROR_MESSAGE);
            }
            List<File> fileList = new ArrayList<File>();
            try {
                fileSet.checkStringAttribute("PATH");
                StringAttribute fileNames = (StringAttribute) fileSet.getAttribute("Path");
                for (String fileName : fileNames.getValues()) {
                    File f = new File(fileName);
                    if (f.exists())
                        fileList.add(f);
                }
                removeAllFiles();
                for (File currentFile : fileList) {
                    addFile(currentFile);
                }
            } catch (DataSetException ex) {
                JOptionPane.showMessageDialog(fileTree, "This file does not contain a FileList!", "Error Loading FileList", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
