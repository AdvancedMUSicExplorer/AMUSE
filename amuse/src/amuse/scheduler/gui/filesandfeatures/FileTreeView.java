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

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Clemens Waeltken
 */
public class FileTreeView {

    JPanel view = new JPanel();

    JTree tree = new JTree();
    JPanel buttonPanel;
    JButton addButton;
    JButton removeButton;
    JButton saveFileListButton;
    JButton loadFileListButton;

    public FileTreeView() {
        BorderLayout layout = new BorderLayout();
        view.setLayout(layout);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setRootVisible(false);
        // Create the scroll pane and add the fileTree to it.
        JScrollPane treeView = new JScrollPane(tree);
        // Add the scroll pane to this panel.
        view.add(treeView, BorderLayout.CENTER);
        view.add(getButtonPanel(), BorderLayout.SOUTH);
        // Add TiteldBorder
        view.setBorder(new TitledBorder("Select Music Files"));
    }

    TreePath[] getSelectionPaths() {
        return tree.getSelectionPaths();
    }

    public JComponent getView() {
        return view;
    }

    void setController(FileTreeController controller) {
        tree.addKeyListener(controller);
        addButton.addActionListener(controller);
        removeButton.addActionListener(controller);
        saveFileListButton.addActionListener(controller);
        loadFileListButton.addActionListener(controller);
    }

    void setModel(FileTreeModel model) {
        tree.setModel(model);
    }

    private Component getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
            buttonPanel.setLayout(layout);
            addButton = new JButton();
            addButton.setText("Add Files");
            addButton.setActionCommand("add");
            removeButton = new JButton();
            removeButton.setText("Remove Selected");
            removeButton.setActionCommand("remove");
            saveFileListButton = new JButton();
            saveFileListButton.setText("->|");
            saveFileListButton.setToolTipText("Save this FileList");
            saveFileListButton.setActionCommand("save");
            loadFileListButton = new JButton();
            loadFileListButton.setText("<-|");
            loadFileListButton.setToolTipText("Load FileFist");
            loadFileListButton.setActionCommand("load");
            buttonPanel.add(addButton);
            buttonPanel.add(removeButton);
            buttonPanel.add(Box.createHorizontalGlue());
            buttonPanel.add(loadFileListButton);
            buttonPanel.add(saveFileListButton);
        }
        return buttonPanel;
    }

}
