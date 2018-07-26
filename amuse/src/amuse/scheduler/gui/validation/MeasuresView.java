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
 * Creation date: 26.08.2008
 */

package amuse.scheduler.gui.validation;

import amuse.data.MeasureTable;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Clemens Wältken
 */
public class MeasuresView extends JPanel {

    private JScrollPane measureTableScrollPane;
    private JTable measureTable;
    MeasuresTableModel model = new MeasuresTableModel();
    private JPanel buttons = new JPanel();
    private JButton btnUncheck = new JButton("Uncheck Selected");
    private JButton btnCheck = new JButton("Check Selected");

    public MeasuresView() throws IOException {
        super(new BorderLayout());
        measureTable = new JTable(model);
        measureTable.setFillsViewportHeight(true);
        measureTable.setRowSorter(new TableRowSorter(measureTable.getModel()));
        measureTable.getColumnModel().getColumn(0).setMinWidth(30);
        measureTable.getColumnModel().getColumn(0).setMaxWidth(25);
        measureTable.getColumnModel().getColumn(2).setMinWidth(300);
        measureTable.getColumnModel().getColumn(2).setMaxWidth(300);
        measureTable.getColumnModel().getColumn(3).setMinWidth(100);
        measureTable.getColumnModel().getColumn(3).setMaxWidth(100);
        measureTable.getColumnModel().getColumn(4).setMinWidth(100);
        measureTable.getColumnModel().getColumn(4).setMaxWidth(100);
//        measureTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        measureTableScrollPane = new JScrollPane(measureTable);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(btnCheck);
        buttons.add(btnUncheck);
        btnCheck.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                checkSelected();
            }
        });
        btnUncheck.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                uncheckSelected();
            }
        });
        this.add(measureTableScrollPane, BorderLayout.CENTER);
        this.add(buttons, BorderLayout.SOUTH);
    }

    private void checkSelected() {
        setSelectedTo(true);
    }

    private void setSelectedTo(boolean value) {
        int[] selectedRows = measureTable.getSelectedRows();
        for (int selection: selectedRows) {
            selection = measureTable.convertRowIndexToModel(selection);
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (model.getColumnClass(i) == Boolean.class) {
                    model.setValueAt(value, selection, i);
                }
            }
        }
    }

    private void uncheckSelected() {
        setSelectedTo(false);
    }

    public MeasureTable getMeasureTable() {
        return ((MeasuresTableModel)measureTable.getModel()).getMeasureTable();
    }

    public void loadSelection(File file) throws IOException {
        model.loadSelection(new MeasureTable(file));
    }

    public void loadSelection(MeasureTable measures) {
        model.loadSelection(measures);
    }
}
