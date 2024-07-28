package amuse.scheduler.gui.validation;

import amuse.data.Measure;
import amuse.data.Measure.Parameter;
import amuse.data.MeasureTable;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MeasuresView extends JPanel {

    private static final long serialVersionUID = 1L;
    private JScrollPane measureTableScrollPane;
    private JTable measureTable;
    MeasuresTableModel model = new MeasuresTableModel();
    private JPanel buttons = new JPanel();
    private JButton btnUncheck = new JButton("Uncheck Selected");
    private JButton btnCheck = new JButton("Check Selected");
	protected Object parentView;

    public MeasuresView() throws IOException {
        super(new BorderLayout());
        measureTable = new JTable(model);
        measureTable.setFillsViewportHeight(true);
        measureTable.setRowSorter(new TableRowSorter<TableModel>(measureTable.getModel()));
        measureTable.getColumnModel().getColumn(0).setMinWidth(30);
        measureTable.getColumnModel().getColumn(0).setMaxWidth(25);
        measureTable.getColumnModel().getColumn(2).setMinWidth(300);
        measureTable.getColumnModel().getColumn(2).setMaxWidth(300);
        measureTable.getColumnModel().getColumn(3).setMinWidth(100);
        measureTable.getColumnModel().getColumn(3).setMaxWidth(100);
        measureTable.getColumnModel().getColumn(4).setMinWidth(100);
        measureTable.getColumnModel().getColumn(4).setMaxWidth(100);
        measureTable.getColumnModel().getColumn(5).setMinWidth(100);
        measureTable.getColumnModel().getColumn(5).setMaxWidth(100);

        // Renderer and editor for the "Adjust Parameter" column
        measureTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonCellRenderer());
        measureTable.getColumnModel().getColumn(5).setCellEditor(new ParameterButtonEditor(this));

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
        for (int selection : selectedRows) {
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
        return ((MeasuresTableModel) measureTable.getModel()).getMeasureTable();
    }

    public void loadSelection(File file) throws IOException {
        model.loadSelection(new MeasureTable(file));
    }

    public void loadSelection(MeasureTable measures) {
        model.loadSelection(measures);
    }

    // For "Parameters" column
    public class ParameterButtonEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        private JButton btnAdjust;
        private MeasuresView parentView;
        private boolean isPushed;

        public ParameterButtonEditor(MeasuresView parentView) {
            super(new JCheckBox()); 
            this.parentView = parentView;
            btnAdjust = new JButton("Adjust");
            btnAdjust.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int row = parentView.measureTable.getSelectedRow();
                    if (row != -1) {
                        row = parentView.measureTable.convertRowIndexToModel(row);
                        Measure measure = parentView.model.getMeasureTable().get(row);
                        if (measure.isChangeParameterSelected()) { 
                            showParameterDialog(measure);
                        } else {
                            JOptionPane.showMessageDialog(null, "Parameter change is disabled for this measure.");
                        }
                    }
                    isPushed = false;
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            isPushed = true;

            // Get the model value for the "Change Parameter" 
            boolean changeParameter = (boolean) table.getModel().getValueAt(row, column);
            btnAdjust.setEnabled(changeParameter);

            return btnAdjust;
        }

        @Override
        public Object getCellEditorValue() {
            return "Adjust";
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }


    private void showParameterDialog(Measure measure) {
        String[] columnHeaders = {"Name", "Value", "Default", "Range", "Definition"};
        DefaultTableModel tableModel = new DefaultTableModel(columnHeaders, 0);
        for (Parameter param : measure.getParameters().values()) {
            Object[] row = {
            		param.getName(), 
            		param.getValue(), 
            		param.getDefaultValue(), 
            		param.getRange(), 
            		param.getDefinition()};
            tableModel.addRow(row);
        }
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        int result = JOptionPane.showConfirmDialog(null, scrollPane, "Parameters", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String paramName = (String) tableModel.getValueAt(i, 0);
                double paramValue = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
                measure.getParameters().get(paramName).setValue(paramValue);
            }
        }
    }

    // ButtonCellRenderer to adjust buttons in the table
    private static class ButtonCellRenderer extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        public ButtonCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Adjust");

            // Get the value for the "Change Parameter" attribute in the arff
            boolean changeParameter = (boolean) table.getModel().getValueAt(row, column);
            setEnabled(changeParameter);

            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            return this;
        }
    }

}
