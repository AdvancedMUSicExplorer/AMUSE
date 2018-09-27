package amuse.scheduler.gui.annotation.multiplefiles;

import java.awt.Component;
import java.util.LinkedList;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import amuse.scheduler.gui.controller.MultipleFilesAnnotationController;
import net.miginfocom.swing.MigLayout;

public class FilterView extends JPanel{
	
	private enum ConditionType {
		STARTS_WITH, ENDS_WITH, CONTAINS, IS;
		
		@Override
		public String toString(){
			switch(this){
			case CONTAINS: return "Contains";
			case STARTS_WITH: return "Starts With";
			case ENDS_WITH: return "Ends With";
			case IS: return "Is";
			default: throw new IllegalArgumentException("Invalid ConditionType used");
			}
		}
	}
	
	private enum Operator {
		AND, OR;
		
		@Override
		public String toString(){
			switch(this){
			case AND: return "And";
			case OR: return "Or";
			default: throw new IllegalArgumentException("Invalid Operator used");
			}
		}
	}
	
	final int NUM_ELEMENTS_PER_ROW = 6;
	
	JPanel conditionsPanel;
	MultipleFilesAnnotationController annotationController;
	DefaultTableModel tableModel;
	
	public FilterView(MultipleFilesAnnotationController annotationController, DefaultTableModel tableModel, TableColumnModel columnModel){
		super(new MigLayout("ins 0, wrap 1, fillx"));
		this.setBorder(new TitledBorder("Filter"));
		
		this.tableModel = tableModel;
		this.annotationController = annotationController; 

		conditionsPanel = new JPanel(new MigLayout("fillx, wrap " + NUM_ELEMENTS_PER_ROW));
		/*
		 * Header
		 */
		conditionsPanel.add(new JLabel("Operator"));
		conditionsPanel.add(new JLabel("Column"));
		conditionsPanel.add(new JLabel("Type"));
		conditionsPanel.add(new JLabel("Condition"));
		conditionsPanel.add(new JLabel("<html><body>Case-<br>Sensitive?</body></html>"));
		conditionsPanel.add(new JLabel(""));
		
		/*
		 * First Row that cannot be deleted
		 */
		JComboBox<ConditionType> firstConditionType = new JComboBox<ConditionType>(ConditionType.values());
		JTextField firstCondition = new JTextField();
		JCheckBox firstCaseSensitive = new JCheckBox();
		JComboBox<String> firstColumn = new JComboBox<String>(this.getColumnNames());
		
		conditionsPanel.add(new JLabel(""));
		conditionsPanel.add(firstColumn, "growx");
		conditionsPanel.add(firstConditionType, "growx");
		conditionsPanel.add(firstCondition, "growx, pushx");
		conditionsPanel.add(firstCaseSensitive);
		conditionsPanel.add(new JLabel(""));
		
		/*
		 * When a column is added/ deleted/ moved, the values in the comboBoxes for the columns must be updated
		 */
		columnModel.addColumnModelListener(new TableColumnModelListener() {
			
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {}
			
			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				refreshColumnComboBoxes();
			}
			
			@Override
			public void columnMoved(TableColumnModelEvent e) {
				refreshColumnComboBoxes();
			}
			
			@Override
			public void columnMarginChanged(ChangeEvent e) {}
			
			@Override
			public void columnAdded(TableColumnModelEvent e) {
				refreshColumnComboBoxes();
			}
			
			public void refreshColumnComboBoxes(){
				String[] columnNames = getColumnNames();
				for(int i = 1 + NUM_ELEMENTS_PER_ROW; i < conditionsPanel.getComponentCount(); i+=NUM_ELEMENTS_PER_ROW){
					JComboBox<String> comboBox = (JComboBox<String>) conditionsPanel.getComponent(i);
					Object selectedItem = comboBox.getSelectedItem();
					comboBox.removeAllItems();
					for(String s: columnNames){
						comboBox.addItem(s);
					}
					comboBox.setSelectedItem(selectedItem);
				}
			}
		});
		
		
					
		
		
		
		/*
		 * Adds a new row/ condition
		 */
		JButton addConditionButton = new JButton("Add Condition");
		addConditionButton.addActionListener(e ->{
			
			JComboBox<Operator> operator = new JComboBox<Operator>(Operator.values());
			JComboBox<String> column = new JComboBox<String>(this.getColumnNames());
			JComboBox<ConditionType> conditionType = new JComboBox<ConditionType>(ConditionType.values());
			JTextField condition = new JTextField();
			JCheckBox caseSensitive = new JCheckBox();
			JButton deleteCondition = new JButton("x");
			deleteCondition.addActionListener(e2 -> {
				conditionsPanel.remove(operator);
				conditionsPanel.remove(column);
				conditionsPanel.remove(conditionType);
				conditionsPanel.remove(condition);
				conditionsPanel.remove(caseSensitive);
				conditionsPanel.remove(deleteCondition);
				conditionsPanel.revalidate();
				conditionsPanel.repaint();
			});
			
			conditionsPanel.add(operator);
			conditionsPanel.add(column);
			conditionsPanel.add(conditionType);
			conditionsPanel.add(condition, "growx, pushx");
			conditionsPanel.add(caseSensitive);
			conditionsPanel.add(deleteCondition);
			conditionsPanel.revalidate();
			conditionsPanel.repaint();
		});

		JButton applyButton = new JButton("Apply");
		JCheckBox invertSelectionCheckBox = new JCheckBox("Invert");
		invertSelectionCheckBox.addActionListener(e -> {
			if(applyButton.isEnabled()){
				applyButton.doClick();
			}
		});
		
		applyButton.addActionListener(e -> {
			
			/*
			 * Read the data
			 */
			Component[] components = conditionsPanel.getComponents();
			
			LinkedList<Operator> opList = new LinkedList<Operator>();
			for(int i = 2 * NUM_ELEMENTS_PER_ROW; i < components.length; i+=NUM_ELEMENTS_PER_ROW){
				opList.add((Operator) ((JComboBox<?>) conditionsPanel.getComponent(i)).getSelectedItem());
			}
			
			LinkedList<Integer> columnIndiceList = new LinkedList<Integer>();
			for(int i = 1 + NUM_ELEMENTS_PER_ROW; i < components.length; i+=NUM_ELEMENTS_PER_ROW){
				columnIndiceList.add(((JComboBox<?>) conditionsPanel.getComponent(i)).getSelectedIndex());
			}
			
			LinkedList<ConditionType> conditionTypeList = new LinkedList<ConditionType>();
			for(int i = 2 + NUM_ELEMENTS_PER_ROW; i < components.length; i+=NUM_ELEMENTS_PER_ROW){
				conditionTypeList.add((ConditionType) ((JComboBox<?>) conditionsPanel.getComponent(i)).getSelectedItem());
			}
			
			LinkedList<String> conditionList = new LinkedList<String>();
			for(int i = 3 + NUM_ELEMENTS_PER_ROW; i < components.length; i+=NUM_ELEMENTS_PER_ROW){
				conditionList.add(((JTextField) conditionsPanel.getComponent(i)).getText());
			}
			
			LinkedList<Boolean> caseSensitiveList = new LinkedList<Boolean>();
			for(int i = 4 + NUM_ELEMENTS_PER_ROW; i < components.length; i+=NUM_ELEMENTS_PER_ROW){
				caseSensitiveList.add(((JCheckBox) conditionsPanel.getComponent(i)).isSelected());
			}

			
			LinkedList<RowFilter<DefaultTableModel, Integer>> filterList = new LinkedList<RowFilter<DefaultTableModel, Integer>>();
			for(int i = 0; i < conditionTypeList.size(); i++){
				String condition = conditionList.get(i);
				ConditionType conditionType = conditionTypeList.get(i);
				boolean caseSensitive = caseSensitiveList.get(i);
				int columnIndice = columnIndiceList.get(i);
				switch(conditionType){
				case CONTAINS:	condition = ".*" + condition + ".*"; break;
				case ENDS_WITH: condition = ".*" + condition + "$"; break;
				case STARTS_WITH: condition = "^" + condition + ".*"; break;
				case IS: condition = "^" + condition + "$"; break; // for the conditionType COND_IS, the condition does not need to be altered.
				}
				
				if(!caseSensitive){
					condition = "(?i)" + condition;
				}
				try{
					if(columnIndice == 0){
						filterList.add(RowFilter.regexFilter(condition));
					}
					else{
						filterList.add(RowFilter.regexFilter(condition, columnIndice));
					}
				}
				catch(PatternSyntaxException exc){
					JOptionPane.showConfirmDialog(null, "Problem with the condition\"" + conditionList.get(i) +"\": " + exc.getMessage(), "Error in Condition", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			/*
			 * Connect filters with ANDs
			 */
			for(int i = 0; i < opList.size();){ //Don't increment because elements might be deleted
				if(opList.get(i) == Operator.AND){
					LinkedList<RowFilter<DefaultTableModel, Integer>> andList = new LinkedList<RowFilter<DefaultTableModel, Integer>>();
					while(i < opList.size() && opList.get(i) == Operator.AND){
						andList.add(filterList.get(i));
						opList.remove(i);
						filterList.remove(i);
					}
					andList.add(filterList.get(i));
					RowFilter<DefaultTableModel, Integer> filter = RowFilter.andFilter(andList);
					filterList.set(i, filter);
				}
				else{
					i++;
				}
			}
			/*
			 * Connect filters with ORs
			 */
			RowFilter<DefaultTableModel, Integer> filter = RowFilter.orFilter(filterList);
			
			/*
			 * Invert selection if necessary
			 */
			if(invertSelectionCheckBox.isSelected()){
				filter = RowFilter.notFilter(filter);
			}
			annotationController.setRowFilter(filter);
		});
		
		
		JCheckBox enableFilteringCheckBox = new JCheckBox("Enable Filtering", true);
		enableFilteringCheckBox.addActionListener(e -> {
			if(enableFilteringCheckBox.isSelected()){
				applyButton.setEnabled(true);
				applyButton.doClick();
			}
			else{
				applyButton.setEnabled(false);
				annotationController.setRowFilter(null);
			}
		});
		
		JPanel controlPanel = new JPanel(new MigLayout("fillx, insets 0"));
		controlPanel.add(invertSelectionCheckBox, "w 25%, growx");
		controlPanel.add(addConditionButton, "w 25%, growx");
		controlPanel.add(applyButton, "w 25%, growx");
		controlPanel.add(enableFilteringCheckBox, "w 25%, growx");
		
		this.add(new JScrollPane(conditionsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "push, grow");
		this.add(controlPanel, "pushx, growx");
		
	}
	
	public String[] getColumnNames(){
		String[] columnNames = null;
		TableColumnModel columnModel = null;
		try{
			columnModel = annotationController.getColumnModel();
			columnNames = new String[columnModel.getColumnCount()];
		}
		catch(NullPointerException e){
			columnNames = new String[1];
		}
		for(int i = 1; i < columnNames.length; i++){
			columnNames[i] = columnModel.getColumn(i).getHeaderValue().toString();
		}
		columnNames[0] = "All";
		return columnNames;
	}
		
}
	
