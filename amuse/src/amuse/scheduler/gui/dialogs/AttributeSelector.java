package amuse.scheduler.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.accessibility.Accessible;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import amuse.data.io.DataSet;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.util.AmuseLogger;
import net.miginfocom.swing.MigLayout;

import javax.swing.table.TableModel;

import org.apache.log4j.Level;

public class AttributeSelector extends JComponent{
	//TODO finish this class
	private List<String> attributes;
//	private List<Integer> selectedAttributes;
	private DataSet readyInput;
	private JPanel attributeView;
	private JPanel view;
	private JTable table;
    private JPanel jPanelAttributeSelectionButtons;
    private JButton jButtonSelectAll;
    private JButton jButtonDeselectAll;
    private JButton jButtonFinish;
    private AttributeTableModel model;
	
	public AttributeSelector(String inputPath, String selectedAttributesString) {

		selectedAttributesString = selectedAttributesString.replaceAll("\\[", "").replaceAll("\\]", "");
		String[] selectedAttributesStringArray = selectedAttributesString.split("\\s*,\\s*");
		List<Integer> currentSelectedAttributes = new ArrayList<Integer>();
		try {
			for(String str : selectedAttributesStringArray) {
				if(!str.equals("")) {
					currentSelectedAttributes.add(Integer.parseInt(str));
				}
			}
		} catch(NumberFormatException e) {
			currentSelectedAttributes = new ArrayList<Integer>();
		}
		
		try {
			readyInput = new DataSet(new File(inputPath));
//			selectedAttributes = new ArrayList<Integer>();
			attributes = readyInput.getAttributeNames();
		} catch(IOException e) {
			JOptionPane.showMessageDialog(this, "Unable to load Ready Input: \"" + e.getLocalizedMessage() + "\"", "Unable to load Ready Input!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		JDialog dialog = new JDialog((Frame)null, "Attribute Selector", true);

		view =  new JPanel(new MigLayout("fill", "", ""));
		
        attributeView = new JPanel();
        BorderLayout layout = new BorderLayout();
        attributeView.setLayout(layout);
        table = new JTable();
        model = new AttributeTableModel(attributes, currentSelectedAttributes);
        
        jButtonFinish = new JButton("OK");
		jButtonFinish.addActionListener(e -> {
        	dialog.dispose();
        });
        
        table.setModel(model);
        table.getColumnModel().getColumn(0).setMinWidth(30);
        table.getColumnModel().getColumn(0).setMaxWidth(25);
        table.setDragEnabled(false);
        attributeView.add(new JScrollPane(table), BorderLayout.CENTER);
        attributeView.add(getJPanelAttributeSelectionButtons(), BorderLayout.SOUTH);
        attributeView.setBorder(new TitledBorder("Select Attributes"));
        
        view.add(attributeView, "wrap");
        view.add(jButtonFinish, "push, al right");
        
        dialog.setContentPane(view);
        dialog.pack();   
        dialog.setLocationByPlatform(true);
        dialog.setVisible(true);
	}
	
	public List<Integer> getSelectedAttributes(){
		return ((AttributeTableModel)table.getModel()).getSelectedAttributes();
	}
	
	private Component getJPanelAttributeSelectionButtons() {
        if (jPanelAttributeSelectionButtons == null) {
            jPanelAttributeSelectionButtons = new JPanel();
            BoxLayout layout = new BoxLayout(jPanelAttributeSelectionButtons,
                    BoxLayout.LINE_AXIS);
            jPanelAttributeSelectionButtons.setLayout(layout);
            jPanelAttributeSelectionButtons.add(getJButtonSelectAll());
            jPanelAttributeSelectionButtons.add(getJButtonDeselectAll());
            jPanelAttributeSelectionButtons.add(Box.createHorizontalGlue());
        }
        return jPanelAttributeSelectionButtons;
    }

	private JButton getJButtonSelectAll() {
        if (jButtonSelectAll == null) {
            jButtonSelectAll = new JButton();
            jButtonSelectAll.setText("Check Selected");
            jButtonSelectAll.setActionCommand("check");
            jButtonSelectAll.addActionListener(e -> {
            	checkSelected();
            });
        }
        return jButtonSelectAll;
    }

    private JButton getJButtonDeselectAll() {
        if (jButtonDeselectAll == null) {
            jButtonDeselectAll = new JButton();
            jButtonDeselectAll.setText("Uncheck Selected");
            jButtonDeselectAll.setActionCommand("uncheck");
            jButtonDeselectAll.addActionListener(e -> {
            	uncheckSelected();
            });
        }
        return jButtonDeselectAll;
    }
    
    private void checkSelected() {
        int[] selectedRows = table.getSelectedRows();
        for (int row : selectedRows) {
            model.setValueAt(true, row, 0);
        }
    }

    private void uncheckSelected() {
        int[] selectedRows = table.getSelectedRows();
        for (int row : selectedRows) {
            model.setValueAt(false, row, 0);
        }
    }
	
	private class AttributeTableModel implements TableModel{

		private Object[][] table;
		private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
		
		public AttributeTableModel(List<String> attributes, List<Integer> currentSelectedAttributes) {
			table = new Object[attributes.size()][2];
			int tableIndex = 0;
			for(String attribute : attributes) {
				table[tableIndex][0] = currentSelectedAttributes.contains(tableIndex);
				table[tableIndex][1] = attribute;
				tableIndex++;
			}
		}
		
		public List<Integer> getSelectedAttributes(){
			List<Integer> selectedAttributes = new ArrayList<Integer>();
			for(int tableIndex = 0; tableIndex < table.length; tableIndex++) {
				if((Boolean)table[tableIndex][0]) {
					selectedAttributes.add(tableIndex);
				}
			}
			return selectedAttributes;
		}
		
		@Override
		public int getRowCount() {
			return table.length;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return "";
			case 1:
				return "Name";
			default:
				throw new UnsupportedOperationException("Not supported.");
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return java.lang.Boolean.class;
			case 1:
				return java.lang.String.class;
			default:
				throw new UnsupportedOperationException("Not supported.");
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return true;
			case 1:
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
		
		@Override
	    public void addTableModelListener(TableModelListener l) {
	        listeners.add(l);
	    }
	    @Override
	    public void removeTableModelListener(TableModelListener l) {
	        listeners.remove(l);
	    }
	    
	    private void notifyListeners(int row) {
	        for (TableModelListener l :listeners) {
	            l.tableChanged(new TableModelEvent(this, row));
	        }
	    }
		
	}
}
