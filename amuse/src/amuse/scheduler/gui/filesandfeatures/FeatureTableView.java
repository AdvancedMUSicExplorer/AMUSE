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
 * Creation date: 08.03.2009
 */

package amuse.scheduler.gui.filesandfeatures;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Clemens Waeltken
 */
public class FeatureTableView {
    private JPanel view = new JPanel();
    private JTable table = new JTable();
    private JPanel jPanelFeatureExtractionButtons;
    private JButton saveSelectionButton;
    private JButton loadSelectionButton;
    private JButton jButtonSelectAll;
    private JButton jButtonDeselectAll;
    private JCheckBox customFeatureCheckBox;

    /**
     * Creates a new Feature Table View.
     */
    public FeatureTableView() {
        initGUI();
    }

    /**
     * Returns this view as JPanel to be placed in other components.
     * @return This view.
     */
    public JPanel getView() {
        return view;
    }

    void setController(ActionListener controller) {
        getJButtonDeselectAll().addActionListener(controller);
        getJButtonSelectAll().addActionListener(controller);
        getLoadButton().addActionListener(controller);
        getSaveButton().addActionListener(controller);
    }

    void setModel(FeatureTableModel model) {
        this.table.setModel(model);
        table.getColumnModel().getColumn(0).setMinWidth(25);
        table.getColumnModel().getColumn(0).setMaxWidth(25);
        table.getColumnModel().getColumn(1).setMinWidth(35);
        table.getColumnModel().getColumn(1).setMaxWidth(35);
        table.getColumnModel().getColumn(2).setMinWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setMinWidth(95);
        table.getColumnModel().getColumn(4).setMaxWidth(95);
        table.setDragEnabled(false);
        table.setRowSorter(new TableRowSorter<FeatureTableModel>(model));
    }

    int[] getSelectedRows() {
        int[] rows = this.table.getSelectedRows();
        int[] rowsInModel = new int[rows.length];
        int i = 0;
        for (int row : rows) {
            rowsInModel[i] = table.getRowSorter().convertRowIndexToModel(row);
            i++;
        }
        return rowsInModel;
    }

    private Component getJPanelFeatureExtractionButtons() {
        if (jPanelFeatureExtractionButtons == null) {
            jPanelFeatureExtractionButtons = new JPanel();
            BoxLayout layout = new BoxLayout(jPanelFeatureExtractionButtons,
                    BoxLayout.LINE_AXIS);
            jPanelFeatureExtractionButtons.setLayout(layout);
            jPanelFeatureExtractionButtons.add(getJButtonSelectAll());
            jPanelFeatureExtractionButtons.add(getJButtonDeselectAll());
            jPanelFeatureExtractionButtons.add(getCustomFeatureCheckBox());
            jPanelFeatureExtractionButtons.add(Box.createHorizontalGlue());
            jPanelFeatureExtractionButtons.add(getLoadButton());
            jPanelFeatureExtractionButtons.add(getSaveButton());
        }
        return jPanelFeatureExtractionButtons;
    }

    private JButton getSaveButton() {
        if (saveSelectionButton == null) {
            saveSelectionButton = new JButton();
            saveSelectionButton.setText("->|");
            saveSelectionButton.setToolTipText("Save current selection");
            saveSelectionButton.setActionCommand("save");
        }
        return saveSelectionButton;
    }

    private JButton getLoadButton() {
        if (loadSelectionButton == null) {
            loadSelectionButton = new JButton();
            loadSelectionButton.setText("<-|");
            loadSelectionButton.setToolTipText("Load selection");
            loadSelectionButton.setActionCommand("load");
        }
        return loadSelectionButton;
    }

    private JButton getJButtonSelectAll() {
        if (jButtonSelectAll == null) {
            jButtonSelectAll = new JButton();
            jButtonSelectAll.setText("Check Selected");
            jButtonSelectAll.setActionCommand("check");
        }
        return jButtonSelectAll;
    }

    private JButton getJButtonDeselectAll() {
        if (jButtonDeselectAll == null) {
            jButtonDeselectAll = new JButton();
            jButtonDeselectAll.setText("Uncheck Selected");
            jButtonDeselectAll.setActionCommand("uncheck");
        }
        return jButtonDeselectAll;
    }
    
    private JCheckBox getCustomFeatureCheckBox() {
    	if(customFeatureCheckBox == null) {
    		customFeatureCheckBox = new JCheckBox();
    		customFeatureCheckBox.setText("Show custom features");
    		customFeatureCheckBox.addActionListener(l -> {
    			FeatureTableModel model = (FeatureTableModel)table.getModel();
    			if(customFeatureCheckBox.isSelected()) {
    				model.addCustomFeatures();
    			} else {
    				model.removeCustomFeatures();
    			}
    		});
    	}
    	return customFeatureCheckBox;
    }

    private void initGUI() {
        BorderLayout layout = new BorderLayout();
        view.setLayout(layout);
        table.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 9) {
                    table.transferFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        // Create the scroll pane and add the table to it.
        JScrollPane tableView = new JScrollPane(table);
        // Add the scroll pane to this panel.
        view.add(tableView, BorderLayout.CENTER);
        // Add the Control Buttons to this panel.
        view.add(getJPanelFeatureExtractionButtons(), BorderLayout.SOUTH);
        // Add Titled Border
        view.setBorder(new TitledBorder("Select Features"));
    }
    
    public void showCustomFeatures(boolean show) {
    	getCustomFeatureCheckBox().setSelected(show);
    }
}
