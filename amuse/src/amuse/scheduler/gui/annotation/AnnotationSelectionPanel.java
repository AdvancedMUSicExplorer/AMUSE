package amuse.scheduler.gui.annotation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

import amuse.nodes.annotation.AnnotationAttribute;
import amuse.nodes.annotation.AnnotationAttributeValue;
import amuse.nodes.annotation.AnnotationNominalAttribute;
import amuse.scheduler.gui.controller.AnnotationController;
import net.miginfocom.swing.MigLayout;

/**
 * Displays the different attributes and attribute values of the currently open annotation.
 * Offers a user interface for editing.
 * @author Frederik Heerde
 * @version $Id$
 */
@SuppressWarnings("serial")
public class AnnotationSelectionPanel extends JSplitPane {

	AnnotationController annotationController;
	JPanel attributeListPanel, attributeValuePanel, attributeValueListPanel;
	JList<AnnotationAttribute<?>> attributeList;
	JList<AnnotationAttributeValue<?>> attributeValueList;

	public AnnotationSelectionPanel(AnnotationController pAnnotationController) {
		super(HORIZONTAL_SPLIT);
		this.annotationController = pAnnotationController;
		this.initAttributeValuePanel();
		this.initAttributeValueListPanel();
		this.initAttributeListPanel();

		/*
		 * Assembling
		 */
		JSplitPane valueSplitPane = new JSplitPane(HORIZONTAL_SPLIT, attributeValueListPanel, attributeValuePanel);
		valueSplitPane.setDividerLocation(0.5);
		valueSplitPane.setResizeWeight(0.5);

		this.setLeftComponent(attributeListPanel);
		this.setRightComponent(valueSplitPane);
		this.setDividerLocation(0.33);
		this.setResizeWeight(0.33);
	}


	private void initAttributeListPanel() {
		attributeListPanel = new JPanel();
		attributeListPanel.setLayout(new BorderLayout());
		attributeListPanel.setBorder(new TitledBorder("Attributes"));

		/*
		 * Init Buttons
		 */
		JButton editAttributeButton = new JButton("Edit");
		editAttributeButton.addActionListener(e -> {
			AnnotationAttribute<?> att = (AnnotationAttribute<?>) attributeList.getSelectedValue();
			if (att != null) {
				String input = null;
				while ((input = JOptionPane.showInputDialog("Enter the new name for the Attribute " + att.getName() + ".")) != null) {
					if (input.equals("")) {
						JOptionPane.showMessageDialog(null, "The name of the attribute cannot be empty");
					} 
					else if (!annotationController.isAttributeNameValid(input)) {
						JOptionPane.showMessageDialog(null, "The name of the attribute already exists.");
					}
					else if (input.contains("'")){
						JOptionPane.showMessageDialog(null, "The name of the attribute cannot contain the character '");
					}
					else {
						break;
					}
				}
				if (input != null) {
					att.setName(input);
				}
				attributeList.repaint();
			}
		});

		
		
		
		
		JButton addAttributeButton = new JButton("Add");
		addAttributeButton.addActionListener(e -> {
			LinkedHashMap<Integer, AnnotationAttribute<?>> idToAttributeMap = (LinkedHashMap<Integer, AnnotationAttribute<?>>) annotationController.getAnnotationAttributeTable().clone(); 
			String[] columnNames = {"ID", "Name", "Type"};
			int numberOfAvailableAttributes = idToAttributeMap.size() - attributeList.getModel().getSize();
			System.out.println(idToAttributeMap.size() + " " + attributeList.getModel().getSize() + " " + numberOfAvailableAttributes);
			if(numberOfAvailableAttributes <= 0){
				JOptionPane.showMessageDialog(null, "No more attributes in the available in the currently loaded annotation attribute table!");
			}
			else{
				
				for(int i = 0; i < attributeList.getModel().getSize(); i++){
					idToAttributeMap.remove(attributeList.getModel().getElementAt(i).getId());
				}
				Object[][] data = new Object[idToAttributeMap.size()][3];
				int i = 0;
				for(AnnotationAttribute<?> att: idToAttributeMap.values()){
					data[i][0] = att.getId();
					data[i][1] = att.getName();
					data[i][2] = att.getType();
					i++;
				}
				JTable annotationAttributeTable = new JTable(new DefaultTableModel(data, columnNames) {
					@Override
					public boolean isCellEditable(int rowIndex, int columnIndex){
						return false;
					}
				});
				annotationAttributeTable.setRowSelectionAllowed(true);
				final JComponent[] inputs = new JComponent[] {new JScrollPane(annotationAttributeTable)};
				int result = JOptionPane.showConfirmDialog(null, inputs, "Adding a new Attribute",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); 
				if (result == JOptionPane.OK_OPTION) {
					if(annotationAttributeTable.getSelectedRowCount() > 0){
						for(int selectedRow: annotationAttributeTable.getSelectedRows()){
							annotationController.addAttribute((Integer) annotationAttributeTable.getModel().getValueAt(selectedRow, 0));
						}
					}
				}
		
				attributeList.repaint();
				((AnnotationView) annotationController.getView()).resizePanels();
			}
		});

		JButton removeAttributeButton = new JButton("Remove");
		removeAttributeButton.addActionListener(e -> {
			AnnotationAttribute<?> att = (AnnotationAttribute<?>) attributeList.getSelectedValue();
			if (att != null) {
				int result = JOptionPane.showConfirmDialog(null,
						"Do you really want to delete the attribute '" + att.getName() + "'?",
						"Delete Attribute?", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					annotationController.removeAttribute(att);
				}
				attributeList.repaint();
				((AnnotationView) annotationController.getView()).resizePanels();
			}

		});

		/*
		 * Init JList in which attributes are displayed
		 */
		attributeList = new JList<AnnotationAttribute<?>>(annotationController.getAttributeListModel());
		attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		attributeList.addListSelectionListener(e -> {
			if(e.getValueIsAdjusting()){ // Prevent multiple executions
				return;
			}
			AnnotationAttribute<?> selectedAttribute = (AnnotationAttribute<?>) attributeList.getSelectedValue();
			if(selectedAttribute != null){
				attributeValueList.setModel((DefaultListModel) selectedAttribute.getValueList());
				selectAttributeValueAtTime(annotationController.getCurrentMs());
			}

		});

		/*
		 * Assembling of Buttons and JList
		 */
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addAttributeButton);
		buttonPanel.add(editAttributeButton);
		buttonPanel.add(removeAttributeButton);

		JScrollPane attributeListScrollPane = new JScrollPane(attributeList);

		attributeListPanel.add(buttonPanel, BorderLayout.SOUTH);
		attributeListPanel.add(attributeListScrollPane, BorderLayout.CENTER);

	}

	private void initAttributeValueListPanel() {
		attributeValueListPanel = new JPanel();
		attributeValueListPanel.setLayout(new BorderLayout());
		attributeValueListPanel.setBorder(new TitledBorder("Attribute Values"));

		/*
		 * Init Buttons
		 */

		JButton addAttributeValueButton = new JButton("Add");
		addAttributeValueButton.addActionListener(e -> {
			AnnotationAttribute<?> selectedAttribute = (AnnotationAttribute<?>) attributeList.getSelectedValue();
			if(selectedAttribute != null){
				AnnotationAttributeValue<?> newValue = annotationController.addNewValueToAttribute(selectedAttribute);
				selectAttributeValue(newValue);
			}
		});
		JButton removeAttributeValueButton = new JButton("Remove");
		removeAttributeValueButton.addActionListener(e -> {
			AnnotationAttribute<?> selectedAttribute = (AnnotationAttribute<?>) attributeList.getSelectedValue();
			AnnotationAttributeValue<?> selectedAttributeValue = (AnnotationAttributeValue<?>) attributeValueList.getSelectedValue();
			if(selectedAttribute != null && selectedAttributeValue != null){
				annotationController.removeValue(attributeValueList.getSelectedValue());
			}
		});

		/*
		 * Init JList in which attribute values are displayed
		 */
		attributeValueList = new JList<AnnotationAttributeValue<?>>(new DefaultListModel<AnnotationAttributeValue<?>>());
		attributeValueList.setFont(new Font("monospaced", Font.BOLD, 12));
		attributeValueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		attributeValueList.addListSelectionListener(e -> {
			if(e.getValueIsAdjusting()){ // Prevent multiple executions
				return;
			}
			AnnotationAttributeValue<?> selectedAttributeValue = (AnnotationAttributeValue<?>) attributeValueList.getSelectedValue();
			attributeValuePanel.removeAll();
			if (selectedAttributeValue != null) {

				switch(selectedAttributeValue.getAnnotationAttribute().getType()){
				case NUMERIC: attributeValuePanel.add(new NumericAttributeValuePanel((AnnotationAttributeValue<Double>) selectedAttributeValue)); break;
				case NOMINAL: attributeValuePanel.add(new NominalAttributeValuePanel((AnnotationAttributeValue<String>) selectedAttributeValue)); break;
				case STRING: attributeValuePanel.add(new StringAttributeValuePanel((AnnotationAttributeValue<String>) selectedAttributeValue)); break;
				case EVENT: attributeValuePanel.add(new EventAttributeValuePanel((AnnotationAttributeValue<Integer>) selectedAttributeValue)); break;
				default: 
				}
			} 
			else{
				attributeValuePanel.add(new JPanel());
			}
			((CardLayout)attributeValuePanel.getLayout()).first(attributeValuePanel);
		});

		/*
		 * Assembling of Buttons and JList
		 */
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addAttributeValueButton);
		buttonPanel.add(removeAttributeValueButton);

		JScrollPane attributeValueListScrollPane = new JScrollPane(attributeValueList);

		attributeValueListPanel.add(buttonPanel, BorderLayout.SOUTH);
		attributeValueListPanel.add(attributeValueListScrollPane, BorderLayout.CENTER);

	}


	private void initAttributeValuePanel() {
		attributeValuePanel = new JPanel(new CardLayout());
		attributeValuePanel.setBorder(new TitledBorder("Attribute Values"));
		attributeValuePanel.add(new JPanel());
		((CardLayout) attributeValuePanel.getLayout()).first(attributeValuePanel);
	}

	public void selectAttribute(int index){
		attributeList.setSelectedIndex(index);
	}

	public void selectAttributeValueAtTime(double millis){
		AnnotationAttribute<?> selectedAttribute = (AnnotationAttribute<?>) attributeList.getSelectedValue();
		if (selectedAttribute != null && !selectedAttribute.getValueList().isEmpty()) {
			AnnotationAttributeValue<?> value = (AnnotationAttributeValue<?>) selectedAttribute.getValueList().get(0); 
			double minDistance = Double.POSITIVE_INFINITY;
			for(int i = 0; i < selectedAttribute.getValueList().size(); i++){
				AnnotationAttributeValue<?> tmpValue = selectedAttribute.getValueList().getElementAt(i);
				if(tmpValue.getStart() <= millis && tmpValue.getEnd() >= millis){ // Perfect Match
					value = tmpValue;
					minDistance = 0;
					break;
				}
				double distance = Math.min(Math.abs(tmpValue.getStart() - millis), Math.abs(tmpValue.getEnd() - millis));
				if(distance < minDistance){
					value = tmpValue;
					minDistance = distance;
				}
			}

			attributeValueList.setSelectedValue(value, false);
		}
	}

	public void selectAttributeWithValue(int attributeNumber, double millis) {
		this.selectAttribute(attributeNumber);
		this.selectAttributeValueAtTime(millis);
	}

	public void selectAttributeValue(AnnotationAttributeValue<?> value) {
		attributeList.setSelectedValue(value.getAnnotationAttribute(), false);
		attributeValueList.setSelectedValue(value, false);
	}

	public void refreshAttributeValuePanel(){
		if(attributeValuePanel != null){
			for(int i = 0; i < attributeValuePanel.getComponentCount(); i++){
				Component c = attributeValuePanel.getComponent(i);
				if(c instanceof AttributeValuePanel<?>){
					((AttributeValuePanel<?>) c).refreshValues();
				}
			}
		}
	}

	@Override 
	public void repaint(){
		super.repaint();
		this.refreshAttributeValuePanel();
	}

	public void clearAnnotation() {
		((DefaultListModel) attributeValueList.getModel()).removeAllElements();
		attributeValuePanel.removeAll();
		attributeValuePanel.add(new JPanel());
		((CardLayout) attributeValuePanel.getLayout()).first(attributeValuePanel);
	}















	/*
	 * Panels for Attributevalues
	 */


	private abstract class AttributeValuePanel<T> extends JPanel{
		AnnotationAttributeValue<T> annotationAttributeValue;
		JButton saveButton;

		public AttributeValuePanel(AnnotationAttributeValue<T> annotationAttributeValue){
			super();
			this.annotationAttributeValue = annotationAttributeValue;

			saveButton = new JButton("Save");
			saveButton.addActionListener(e -> {
				saveChanges();
				attributeValueList.repaint();
				annotationController.getView().repaint();
			});

			this.setLayout(new MigLayout("fill, wrap 2"));
		}

		public abstract void saveChanges();

		public abstract void refreshValues();
	}

	private abstract class TimeAttributeValuePanel<T> extends AttributeValuePanel<T>{
		JFormattedTextField fieldStart, fieldEnd;

		public TimeAttributeValuePanel(AnnotationAttributeValue<T> annotationAttributeValue){
			super(annotationAttributeValue);

			NumberFormatter integerFormatter = new NumberFormatter(NumberFormat.getInstance());
			((NumberFormat) integerFormatter.getFormat()).setGroupingUsed(false);
			integerFormatter.setValueClass(Integer.class);
			integerFormatter.setMinimum(0);
			integerFormatter.setMaximum(Integer.MAX_VALUE);
			integerFormatter.setAllowsInvalid(false);

			fieldStart = new JFormattedTextField(integerFormatter);
			fieldStart.setMaximumSize(new Dimension(100,19));
			fieldEnd = new JFormattedTextField(integerFormatter);
			fieldEnd.setMaximumSize(new Dimension(100,19));

			this.add(new JLabel("Start:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldStart, "w 50%, pushx");
			this.add(new JLabel("End:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldEnd, "w 50%");

			fieldStart.setText(annotationAttributeValue.getStart() + "");
			fieldEnd.setText(annotationAttributeValue.getEnd() + "");
		}
	}

	private class StringAttributeValuePanel extends TimeAttributeValuePanel<String>{
		JFormattedTextField fieldValue;

		public StringAttributeValuePanel(AnnotationAttributeValue<String> annotationAttributeValue) {
			super(annotationAttributeValue);
			fieldValue = new JFormattedTextField("");
			fieldValue.setMaximumSize(new Dimension(100,19));

			this.add(new JLabel("Value:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldValue, "w 50%");
			this.add(saveButton, "span, center, bottom, pushy");

			fieldValue.setText(annotationAttributeValue.getValue());
		}

		@Override
		public void saveChanges() {
			if(!fieldStart.getText().isEmpty() && !fieldEnd.getText().isEmpty() && !fieldValue.getText().isEmpty()){
				annotationAttributeValue.setStart(new Integer(fieldStart.getText()));
				annotationAttributeValue.setEnd(new Integer(fieldEnd.getText()));
				annotationAttributeValue.setValue(fieldValue.getText());
			}
		}

		@Override
		public void refreshValues() {
			fieldStart.setText(annotationAttributeValue.getStart() + "");
			fieldEnd.setText(annotationAttributeValue.getEnd() + "");
			fieldValue.setText(annotationAttributeValue.getValue() + "");
		}


	}

	private class NumericAttributeValuePanel extends TimeAttributeValuePanel<Double>{
		JFormattedTextField fieldValue;

		public NumericAttributeValuePanel(AnnotationAttributeValue<Double> annotationAttributeValue) {
			super(annotationAttributeValue);
			DecimalFormat decimalFormat = new DecimalFormat();
			decimalFormat.setGroupingUsed(false);

			fieldValue = new JFormattedTextField(decimalFormat);
			fieldValue.setMaximumSize(new Dimension(100,19));

			this.add(new JLabel("Value:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldValue, "w 50%");
			this.add(saveButton, "span, center, bottom, pushy");

			fieldValue.setText(annotationAttributeValue.getValue() + "");
		}

		@Override
		public void saveChanges() {
			if(!fieldStart.getText().isEmpty() && !fieldEnd.getText().isEmpty() && !fieldValue.getText().isEmpty()){
				annotationAttributeValue.setStart(new Integer(fieldStart.getText()));
				annotationAttributeValue.setEnd(new Integer(fieldEnd.getText()));
				annotationAttributeValue.setValue(new Double(fieldValue.getText()));
			}
		}

		@Override
		public void refreshValues() {
			fieldStart.setText(annotationAttributeValue.getStart() + "");
			fieldEnd.setText(annotationAttributeValue.getEnd() + "");
			fieldValue.setText(annotationAttributeValue.getValue() + "");
		}
	}

	private class NominalAttributeValuePanel extends TimeAttributeValuePanel<String>{
		JList<?> attributeValueList;

		public NominalAttributeValuePanel(AnnotationAttributeValue<String> annotationAttributeValue) {
			super(annotationAttributeValue);

			attributeValueList = new JList<String>(((AnnotationNominalAttribute)this.annotationAttributeValue.getAnnotationAttribute()).getAllowedValues());
			attributeValueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


			JButton addValueButton = new JButton("Add");
			addValueButton.addActionListener(e -> {
				String input = null;
				while ((input = JOptionPane.showInputDialog("Enter the name for the new value.")) != null) {
					if (input.equals("")) {
						JOptionPane.showMessageDialog(null, "The name of the new value cannot be empty");
					} 
					else if (((DefaultListModel<?>) attributeValueList.getModel()).contains(input)) {
						JOptionPane.showMessageDialog(null, "The name of the new value already exists.");
					}
					else if (input.contains("'")){
						JOptionPane.showMessageDialog(null, "The name of the new value cannot contain the character '");
					}
					else {
						break;
					}
				}
				if (input != null) {
					((AnnotationNominalAttribute) attributeList.getSelectedValue()).addAllowedValue(input);
				}
				attributeValueList.repaint();
			});

			JButton editValueButton = new JButton("Edit");
			editValueButton.addActionListener(e -> {
				String selectedValue = (String) attributeValueList.getSelectedValue();
				if (attributeValueList.getSelectedValue() != null) {
					String input = null;
					while ((input = JOptionPane.showInputDialog("Enter the new name for the value.")) != null) {
						if (input.equals("")) {
							JOptionPane.showMessageDialog(null, "The name of the value cannot be empty");
						} 
						else if (!input.equals(selectedValue)
								&& ((DefaultListModel<?>) attributeValueList.getModel()).contains(input)) {
							JOptionPane.showMessageDialog(null, "The name of the value already exists.");
						}
						else if (input.contains("'")){
							JOptionPane.showMessageDialog(null, "The name of the value cannot contain the character '");
						}
						else {
							break;
						}
					}
					if (input != null && !input.equals(selectedValue)) {
						((AnnotationNominalAttribute) attributeList.getSelectedValue())
						.editAllowedValue(selectedValue, input);
					}
					saveButton.doClick();
					attributeValueList.repaint();
				}
			});

			JButton removeValueButton = new JButton("Remove");
			removeValueButton.addActionListener(e -> {
				String selectedValue = (String) attributeValueList.getSelectedValue();
				if (selectedValue != null) {
					int result = JOptionPane.showConfirmDialog(null,
							"Do you really want to delete the attribute value '" + selectedValue
							+ "' and all its corresponding annotation values?",
							"Delete Attribute Value?", JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						((AnnotationNominalAttribute) attributeList.getSelectedValue())
						.removeAllowedValue(selectedValue);
					}
					attributeValueList.repaint();
				}
			});

			JScrollPane attributeValueListPane = new JScrollPane(attributeValueList);
			this.add(attributeValueListPane, "span 2, w 100%, pushy");
			this.add(addValueButton, "span, center, split 3");
			this.add(editValueButton);
			this.add(removeValueButton);
			this.add(saveButton, "span, center, bottom, pushy");

			attributeValueList.setSelectedValue(annotationAttributeValue.getValue(), true);
		}

		@Override
		public void saveChanges() {
			if(!fieldStart.getText().isEmpty() && !fieldEnd.getText().isEmpty() && attributeValueList.getSelectedValue() != null){
				annotationAttributeValue.setStart(new Integer(fieldStart.getText()));
				annotationAttributeValue.setEnd(new Integer(fieldEnd.getText()));
				annotationAttributeValue.setValue(attributeValueList.getSelectedValue().toString());
			}
		}

		@Override
		public void refreshValues() {
			fieldStart.setText(annotationAttributeValue.getStart() + "");
			fieldEnd.setText(annotationAttributeValue.getEnd() + "");
			attributeValueList.setSelectedValue(annotationAttributeValue.getValue(), false);
		}
	}

	private class EventAttributeValuePanel extends AttributeValuePanel<Integer>{
		JFormattedTextField fieldValue;

		public EventAttributeValuePanel(AnnotationAttributeValue<Integer> annotationAttributeValue) {
			super(annotationAttributeValue);
			NumberFormatter integerFormatter = new NumberFormatter(NumberFormat.getInstance());
			((NumberFormat) integerFormatter.getFormat()).setGroupingUsed(false);
			integerFormatter.setValueClass(Integer.class);
			integerFormatter.setMinimum(0);
			integerFormatter.setMaximum(Integer.MAX_VALUE);
			integerFormatter.setAllowsInvalid(false);

			fieldValue = new JFormattedTextField(integerFormatter);
			fieldValue.setMaximumSize(new Dimension(100,19));

			this.add(new JLabel("Value:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldValue, "w 50%, pushx");
			this.add(saveButton, "span, center, bottom, pushy");

			fieldValue.setText(annotationAttributeValue.getStart() + "");
		}

		@Override
		public void saveChanges() {
			annotationAttributeValue.setStart(new Integer(fieldValue.getText()));
		}

		@Override
		public void refreshValues() {
			fieldValue.setText(annotationAttributeValue.getStart() + "");			
		}

	}

}
