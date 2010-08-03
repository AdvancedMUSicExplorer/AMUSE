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

import amuse.data.Feature;
import amuse.data.FeatureTable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Clemens Waeltken
 */
public class FeatureTableModel implements TableModel {

    private Object[][] table;
    private FeatureTable featureTable;
    private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();

    public FeatureTableModel(FeatureTable featureTable) {
        this.featureTable = featureTable;
        List<Feature> features = this.featureTable.getFeatures();
        this.table = new Object[features.size()][4];
        int tableIndex = 0;
        for (Feature f : features) {
            this.table[tableIndex][0] = f.isSelectedForExtraction();
            this.table[tableIndex][1] = f.getId();
            this.table[tableIndex][2] = f.getDescription();
            this.table[tableIndex][3] = f.getDimension();
            tableIndex++;
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    void selectFeaturesByID(List<Integer> ids) {
        for (int i = 0; i < featureTable.size(); i++) {
            if (ids.contains(featureTable.getFeatureAt(i).getId())) {
                table[i][0] = true;
                notifyListeners(i);
            } else {
                table[i][0] = false;
                notifyListeners(i);
            }
        }
    }

    private void notifyListeners(int row) {
        for (TableModelListener l :listeners) {
            l.tableChanged(new TableModelEvent(this, row));
        }
    }

    @Override
    public int getRowCount() {
        return table.length;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Use";
            case 1:
                return "ID";
            case 2:
                return "Feature Description";
            case 3:
                return "Value Count";
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return java.lang.Boolean.class;
            case 1:
                return java.lang.Integer.class;
            case 2:
                return java.lang.String.class;
            case 3:
                return java.lang.Integer.class;
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return true;
            case 1:
                return false;
            case 2:
                return false;
            case 3:
                return false;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return table[rowIndex][columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        table[rowIndex][columnIndex] = aValue;
        notifyListeners(rowIndex);
    }

    int getSelectForExtractionColumnIndex() {
        return 0;
    }

    public FeatureTable getCurrentFeatureTable() {
        // Update FeatureTable with current selection:
        for (int i = 0; i < featureTable.size(); i++) {
            featureTable.getFeatureAt(i).setSelectedForExtraction(
                    table[i][0].equals(true));
        }
        // Return the FretureTable:
        return this.featureTable;
    }
}
