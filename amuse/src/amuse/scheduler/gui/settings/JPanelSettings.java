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
 * Creation date: 19.08.2008
 */
package amuse.scheduler.gui.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import amuse.scheduler.gui.controller.WizardController;
import amuse.scheduler.gui.navigation.BackButtonUsable;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.NextButtonUsable;

/**
 * @author Clemens Waeltken
 * 
 */
public class JPanelSettings extends JPanel implements NextButtonUsable,
        BackButtonUsable, HasCaption {

    private static final long serialVersionUID = -42299063219685180L;
    private JTree settingsTree;
    private final JScrollPane treeScrollPane;
    private final JPanel rightSidePanel = new JPanel();
    private JPanel currentView = new JPanel();
    private List<EditableAmuseSettingInterface> availableSettingPagesList = EditableAmuseSettingsFactory.getInstance().getEditableAmuseSettings();
    
    private final JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT);
    private final JButton applyButton = new JButton("Apply");
    private volatile static JPanelSettings instance = null;
    private volatile boolean currentPanelHasChanged = false;
    private EditableAmuseSettingInterface currentSetting = null;
    private final SettingsChangedListener panelChangeListener = new SettingsChangedListener() {

        public void settingsStateChanged(EditableAmuseSettingInterface source,
                boolean changed) {
            if (changed) {
                currentPanelHasChanged = true;
                applyButton.setEnabled(true);
            } else {
                currentPanelHasChanged = false;
                applyButton.setEnabled(false);
            }
        }
    };

    private JPanelSettings() {
        super();
        /*
         * Setup the Layout of this Component. Left side will show available
         * categories, while the right side will show all settings of the
         * currently selected category together with an "Apply" Button.
         */
        BorderLayout layout = new BorderLayout();
        this.setLayout(layout);
        this.add(splitPane, BorderLayout.CENTER);
        layout = new BorderLayout();
        rightSidePanel.setLayout(layout);
        JPanel applyPanel = new JPanel();
        BoxLayout applyPanelLayout = new BoxLayout(applyPanel, BoxLayout.X_AXIS);
        applyPanel.setLayout(applyPanelLayout);
        applyPanel.add(Box.createHorizontalGlue());
        applyPanel.add(applyButton);
        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applyButtonBehavior();
            }
        });
        applyButton.setEnabled(false);
        rightSidePanel.add(currentView, BorderLayout.CENTER);
        rightSidePanel.add(applyPanel, BorderLayout.SOUTH);
        settingsTree = new SettingsTree();
        treeScrollPane = new JScrollPane(settingsTree);
        treeScrollPane.setMinimumSize(new Dimension(150, (int) treeScrollPane.getMinimumSize().getHeight()));
        splitPane.add(treeScrollPane);
        splitPane.add(rightSidePanel);
        splitPane.setEnabled(false);
    }

    public static JPanelSettings getInstance() {
        if (instance == null) {
            synchronized (JPanelSettings.class) {
                if (instance == null) {
                    instance = new JPanelSettings();
                }
            }
        }
        return instance;
    }

    @Override
    public String getCaption() {
        return "Settings";
    }

    private class SettingsTree extends JTree {

        private static final long serialVersionUID = 5161187736094474176L;
        
        private TreePath currentPath;
        
        public SettingsTree() {

            super();
            this.setRootVisible(false);
            // Set up the selection model.
            TreeSelectionModel treeSelectionModel = new DefaultTreeSelectionModel();
            treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            this.setSelectionModel(treeSelectionModel);
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.getModel().getRoot();
            // Remove default entries.
            rootNode.removeAllChildren();
            // Add all available Setting Pages:
            for (EditableAmuseSettingInterface setting : availableSettingPagesList) {
                rootNode.add(new DefaultMutableTreeNode(setting));
            }
            // Update the model.
            ((DefaultTreeModel) this.getModel()).reload();
            // Setup selection behavior.
            this.addTreeSelectionListener(new TreeSelectionListener() {

                public void valueChanged(TreeSelectionEvent e) {
                    newNodeKlicked(e);
                }
            });
            // Display first Settings Page on entry:
            if (!availableSettingPagesList.isEmpty()) {
                changeSettingsPage(availableSettingPagesList.get(0));
            }

        }

        private void newNodeKlicked(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                    EditableAmuseSettingInterface newSetting = (EditableAmuseSettingInterface) node.getUserObject();
                    if (newSetting != currentSetting) {
                        boolean yesNo = askToDiscard();
                        if (!yesNo) {
                            if (currentPath != null) {
                                this.setSelectionPath(currentPath);
                            }
                            return;
                        }
                        currentPath = e.getPath();
                        changeSettingsPage(newSetting);
                    }
        }
        private void changeSettingsPage(EditableAmuseSettingInterface newSetting) {
            // stop watching for changes on old one:
            if (currentSetting != null) {
                currentSetting.removeChangeListener(panelChangeListener);
            }
            currentSetting = newSetting;
            rightSidePanel.remove(currentView);
            currentView = newSetting.getPanel();
            rightSidePanel.add(currentView, BorderLayout.CENTER);
            // watch for changes:
            newSetting.addChangeListener(panelChangeListener);
            splitPane.revalidate();
            splitPane.repaint();
        }
    }

    private boolean askToDiscard() {
        if (currentPanelHasChanged) {
            // JOptionPane.showOptionDialog(this,
            // "Save current changes?", "Settings have changed",
            // PROPERTIES, PROPERTIES, true);
            int yesOrNo = JOptionPane.showConfirmDialog(instance,
                    "Discard current changes?", "Settings have changed!",
                    JOptionPane.YES_NO_OPTION);
            if (yesOrNo == JOptionPane.YES_OPTION) {
                currentSetting.discardChanges();
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private void applyButtonBehavior() {
        saveChanges();
        applyButton.setEnabled(false);
    }

    private void saveChanges() {
        for (EditableAmuseSettingInterface setting : availableSettingPagesList) {
            setting.saveChanges();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see amuse.scheduler.gui.NextButtonUsable#getNextButtonText()
     */
    @Override
    public String getNextButtonText() {
        return "OK";
    }

    /*
     * (non-Javadoc)
     * 
     * @see amuse.scheduler.gui.NextButtonUsable#nextButtonClicked()
     */
    @Override
    public boolean nextButtonClicked() {
        currentSetting.saveChanges();
        WizardController.getInstance().goBack();
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see amuse.scheduler.gui.navigation.BackButtonUsable#backButtonClicked()
     */
    @Override
    public boolean backButtonClicked() {
        // TODO leichte verhaltensaenderung? Ein klick verwirft, zweiter klick geht zurueck.
        if (!askToDiscard()) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see amuse.scheduler.gui.navigation.BackButtonUsable#getBackButtonText()
     */
    @Override
    public String getBackButtonText() {
        return "Cancel";
    }
}
