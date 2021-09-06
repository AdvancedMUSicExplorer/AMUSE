package amuse.scheduler.gui.annotation.singlefile;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.Attribute;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.annotation.singlefile.action.AnnotationAddAttributeAction;
import amuse.scheduler.gui.annotation.singlefile.action.AnnotationAddAttributeEntryAction;
import amuse.scheduler.gui.annotation.singlefile.action.AnnotationEditAttributeEntryAction;
import amuse.scheduler.gui.annotation.singlefile.action.AnnotationRemoveAttributeAction;
import amuse.scheduler.gui.annotation.singlefile.action.AnnotationRemoveAttributeEntryAction;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttribute;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttributeEntry;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttributeType;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationEventAttribute;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationNominalAttribute;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationNumericAttribute;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationStringAttribute;
import amuse.scheduler.gui.controller.SingleFileAnnotationController;
import net.miginfocom.swing.MigLayout;

/**
 * Displays the different attributes and attribute entries of the currently open annotation.
 * Offers a user interface for editing.
 * @author Frederik Heerde
 * @version $Id$
 */
@SuppressWarnings("serial")
public class AnnotationSelectionPanel extends JSplitPane {

	SingleFileAnnotationController annotationController;
	JPanel attributeListPanel, attributeEntryPanel, attributeEntryListPanel;
	JList<AnnotationAttribute<?>> attributeList;
	JList<AnnotationAttributeEntry<?>> attributeEntryList;

	public AnnotationSelectionPanel(SingleFileAnnotationController pAnnotationController) {
		super(HORIZONTAL_SPLIT);
		this.annotationController = pAnnotationController;
		this.initAttributeEntryPanel();
		this.initAttributeEntryListPanel();
		this.initAttributeListPanel();

		/*
		 * Assembling
		 */
		JSplitPane entrySplitPane = new JSplitPane(HORIZONTAL_SPLIT, attributeEntryListPanel, attributeEntryPanel);
		entrySplitPane.setResizeWeight(0.3);
		
		
		// To set the initial divider location, the JSplitPane must be visible.
		entrySplitPane.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				setDividerLocation(0.3);
				removeComponentListener(this); //The divider location must only set once
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {}
			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});

		this.setLeftComponent(attributeListPanel);
		this.setRightComponent(entrySplitPane);
		
		this.setResizeWeight(0.3);
		
		
		// To set the initial divider location, the JSplitPane must be visible.
		this.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				setDividerLocation(0.3);
				removeComponentListener(this); //The divider location must only set once
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {}
			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
	}


	private void initAttributeListPanel() {
		attributeListPanel = new JPanel();
		attributeListPanel.setLayout(new BorderLayout());
		attributeListPanel.setBorder(new TitledBorder("Attributes"));

		/*
		 * Init Buttons
		 */

		JButton addAttributeButton = new JButton("Add");
		addAttributeButton.setToolTipText("Create or add an attribute");
		addAttributeButton.addActionListener(new AddAttributeActionListener());
		

		JButton removeAttributeButton = new JButton("Remove");
		removeAttributeButton.setToolTipText("Remove an attribute from the list");
		removeAttributeButton.addActionListener(e -> {
			AnnotationAttribute<?> att = (AnnotationAttribute<?>) attributeList.getSelectedValue();
			if (att != null) {
				String[] options = new String[]{"Delete", "Only Hide", "Cancel"};
				int result = JOptionPane.showOptionDialog(null,
						"Do you really want to delete the attribute '" + att.getName() + "'?",
						"Delete Attribute?",
						JOptionPane.PLAIN_MESSAGE,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[1]);
				if (result != 3){
					annotationController.addUndoableAction(new AnnotationRemoveAttributeAction(annotationController, 
							att, 
							annotationController.getAttributeListModel().indexOf(att)));
					if (result == 0){
						annotationController.deleteAttributeFile(att);
					}
					else if (result == 1){
						annotationController.removeAttribute(att);
					}
					attributeList.repaint();
					attributeList.clearSelection();
					((AnnotationView) annotationController.getView()).resizePanels();
				}
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
				attributeEntryList.setModel((DefaultListModel) selectedAttribute.getEntryList());
				selectAttributeEntryAtTime(annotationController.getCurrentMs());
			}
			else{
				attributeEntryList.setModel(new DefaultListModel());
			}

		});
		
		attributeList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value,
					int index,
					boolean isSelected,
					boolean cellHasFocus) {
				Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				this.setForeground(new Color(Color.HSBtoRGB(index * 0.16667f, 0.8f, 0.7f)));
				return component;
			}
		});
		
		// Set the focus that is needed for key events
		attributeList.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {
				attributeList.requestFocusInWindow();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				attributeEntryList.requestFocusInWindow();
			}
			
		});
		
		// Associates the delete key with the removeAttributeButton
		attributeList.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE){
					removeAttributeButton.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
		});

		/*
		 * Assembling of Buttons and JList
		 */
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addAttributeButton);
		buttonPanel.add(removeAttributeButton);

		JScrollPane attributeListScrollPane = new JScrollPane(attributeList);

		attributeListPanel.add(buttonPanel, BorderLayout.SOUTH);
		attributeListPanel.add(attributeListScrollPane, BorderLayout.CENTER);

	}

	private void initAttributeEntryListPanel() {
		attributeEntryListPanel = new JPanel();
		attributeEntryListPanel.setLayout(new BorderLayout());
		attributeEntryListPanel.setBorder(new TitledBorder("Attribute Entries"));

		/*
		 * Init Buttons
		 */

		JButton addAttributeEntryButton = new JButton("Add");
		addAttributeEntryButton.setToolTipText("Add a new entry starting at the current playing time for the selected attribute");
		addAttributeEntryButton.addActionListener(e -> {
			AnnotationAttribute<?> selectedAttribute = (AnnotationAttribute<?>) attributeList.getSelectedValue();
			if(selectedAttribute != null){
				AnnotationAttributeEntry<?> newEntry = annotationController.addNewEntryToAttribute(selectedAttribute);
				annotationController.addUndoableAction(new AnnotationAddAttributeEntryAction(annotationController, newEntry));
				selectAttributeEntry(newEntry);
			}
		});
		JButton removeAttributeEntryButton = new JButton("Remove");
		removeAttributeEntryButton.setToolTipText("Remove the selected entry for the selected attribute");
		removeAttributeEntryButton.addActionListener(e -> {
			AnnotationAttribute<?> selectedAttribute = (AnnotationAttribute<?>) attributeList.getSelectedValue();
			AnnotationAttributeEntry<?> selectedAttributeValue = (AnnotationAttributeEntry<?>) attributeEntryList.getSelectedValue();
			int selectedIndex = attributeEntryList.getSelectedIndex();
			if(selectedAttribute != null && selectedAttributeValue != null){
				annotationController.addUndoableAction(new AnnotationRemoveAttributeEntryAction(annotationController, attributeEntryList.getSelectedValue()));
				annotationController.removeEntry(attributeEntryList.getSelectedValue());
				attributeEntryList.setSelectedIndex(Math.min(selectedIndex, attributeEntryList.getModel().getSize() - 1));
			}
		});

		/*
		 * Init JList in which attribute entries are displayed
		 */
		attributeEntryList = new JList<AnnotationAttributeEntry<?>>(new DefaultListModel<AnnotationAttributeEntry<?>>());
		attributeEntryList.setFont(new Font("monospaced", Font.BOLD, 12));
		attributeEntryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		attributeEntryList.addListSelectionListener(e -> {
			if(e.getValueIsAdjusting()){ // Prevent multiple executions
				return;
			}
			AnnotationAttributeEntry<?> selectedAttributeEntry = (AnnotationAttributeEntry<?>) attributeEntryList.getSelectedValue();
			attributeEntryPanel.removeAll();
			JPanel panel = new JPanel();
			if (selectedAttributeEntry != null) {
				switch(selectedAttributeEntry.getAnnotationAttribute().getType()){
				case NUMERIC: panel = new NumericAttributeEntryPanel((AnnotationAttributeEntry<Double>) selectedAttributeEntry); break;
				case NOMINAL: panel = new NominalAttributeEntryPanel((AnnotationAttributeEntry<String>) selectedAttributeEntry); break;
				case STRING: panel = new StringAttributeEntryPanel((AnnotationAttributeEntry<String>) selectedAttributeEntry); break;
				case EVENT: panel = new EventAttributeEntryPanel((AnnotationAttributeEntry<Integer>) selectedAttributeEntry); break;
				}
			}
			attributeEntryPanel.add(panel);
			((CardLayout)attributeEntryPanel.getLayout()).first(attributeEntryPanel);
		});

		// Associates the delete key with the removeAttributeValueButton
		attributeEntryList.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE){
					removeAttributeEntryButton.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
		});

		/*
		 * Assembling of Buttons and JList
		 */
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addAttributeEntryButton);
		buttonPanel.add(removeAttributeEntryButton);

		JScrollPane attributeEntryListScrollPane = new JScrollPane(attributeEntryList);

		attributeEntryListPanel.add(buttonPanel, BorderLayout.SOUTH);
		attributeEntryListPanel.add(attributeEntryListScrollPane, BorderLayout.CENTER);

	}


	private void initAttributeEntryPanel() {
		attributeEntryPanel = new JPanel(new CardLayout());
		attributeEntryPanel.setBorder(new TitledBorder("Attribute Values"));
		attributeEntryPanel.add(new JPanel());
		((CardLayout) attributeEntryPanel.getLayout()).first(attributeEntryPanel);
	}

	public void selectAttribute(int index){
		attributeList.setSelectedIndex(index);
	}

	public void selectAttributeEntryAtTime(double secs){
		AnnotationAttribute<?> selectedAttribute = (AnnotationAttribute<?>) attributeList.getSelectedValue();
		if (selectedAttribute != null && !selectedAttribute.getEntryList().isEmpty()) {
			AnnotationAttributeEntry<?> entry = (AnnotationAttributeEntry<?>) selectedAttribute.getEntryList().get(0); 
			double minDistance = Double.POSITIVE_INFINITY;
			for(int i = 0; i < selectedAttribute.getEntryList().size(); i++){
				AnnotationAttributeEntry<?> tmpEntry = selectedAttribute.getEntryList().getElementAt(i);
				if(tmpEntry.getStart() <= secs && tmpEntry.getEnd() >= secs){ // Perfect Match
					entry = tmpEntry;
					minDistance = 0;
					break;
				}
				double distance = Math.min(Math.abs(tmpEntry.getStart() - secs), Math.abs(tmpEntry.getEnd() - secs));
				if(distance < minDistance){
					entry = tmpEntry;
					minDistance = distance;
				}
			}

			attributeEntryList.setSelectedValue(entry, false);
		}
	}

	public void selectAttributeWithEntry(int attributeNumber, double secs) {
		this.selectAttribute(attributeNumber);
		this.selectAttributeEntryAtTime(secs);
	}

	public void selectAttributeEntry(AnnotationAttributeEntry<?> entry) {
		attributeList.setSelectedValue(entry.getAnnotationAttribute(), true);
		attributeEntryList.setSelectedValue(entry, true);
	}

	public void refreshAttributeEntryPanel(){
		if(attributeEntryPanel != null){
			for(int i = 0; i < attributeEntryPanel.getComponentCount(); i++){
				Component c = attributeEntryPanel.getComponent(i);
				if(c instanceof AttributeEntryPanel<?>){
					((AttributeEntryPanel<?>) c).refreshValues();
				}
			}
		}
	}

	@Override 
	public void repaint(){
		super.repaint();
		this.refreshAttributeEntryPanel();
	}

	public void clearAnnotation() {
		((DefaultListModel<AnnotationAttribute<?>>) attributeList.getModel()).removeAllElements();
		((DefaultListModel<AnnotationAttributeEntry<?>>) attributeEntryList.getModel()).removeAllElements();
		attributeEntryPanel.removeAll();
		attributeEntryPanel.add(new JPanel());
		((CardLayout) attributeEntryPanel.getLayout()).first(attributeEntryPanel);
	}
	
	public JList<AnnotationAttributeEntry<?>> getAttributeEntryList() {
		return attributeEntryList;
	}














	/*
	 * Panels for AttributeEntries
	 */


	private abstract class AttributeEntryPanel<T> extends JPanel{
		AnnotationAttributeEntry<T> entry;
		JButton saveButton;

		public AttributeEntryPanel(AnnotationAttributeEntry<T> annotationAttributeEntry){
			super();
			this.entry = annotationAttributeEntry;

			saveButton = new JButton("Save");
			saveButton.setToolTipText("Save the changes");
			saveButton.addActionListener(e -> {
				annotationController.addUndoableAction(new AnnotationEditAttributeEntryAction(annotationController, entry, entry.getStart(), entry.getEnd(), entry.getValue()));
				saveChanges();
				attributeEntryList.repaint();
				annotationController.getView().repaint();
			});

			this.setLayout(new MigLayout("fill, wrap 2"));
		}

		public abstract void saveChanges();

		public abstract void refreshValues();
	}

	private abstract class TimeAttributeEntryPanel<T> extends AttributeEntryPanel<T>{
		JFormattedTextField fieldStart, fieldEnd;

		public TimeAttributeEntryPanel(AnnotationAttributeEntry<T> annotationAttributeEntry){
			super(annotationAttributeEntry);

			NumberFormatter doubleFormatterStart = new NumberFormatter(DecimalFormat.getInstance(Locale.ROOT));
			((DecimalFormat) doubleFormatterStart.getFormat()).setGroupingUsed(false);
			((DecimalFormat) doubleFormatterStart.getFormat()).applyPattern("#0.0000");
			doubleFormatterStart.setValueClass(Double.class);
			doubleFormatterStart.setMinimum(0.);
			doubleFormatterStart.setMaximum(Double.MAX_VALUE);
			doubleFormatterStart.setAllowsInvalid(false);
			
			NumberFormatter doubleFormatterEnd = new NumberFormatter(DecimalFormat.getInstance(Locale.ROOT));
			((DecimalFormat) doubleFormatterEnd.getFormat()).setGroupingUsed(false);
			((DecimalFormat) doubleFormatterEnd.getFormat()).applyPattern("#0.0000");
			doubleFormatterEnd.setValueClass(Double.class);
			doubleFormatterEnd.setMinimum(0.);
			doubleFormatterEnd.setMaximum(Double.MAX_VALUE);
			doubleFormatterEnd.setAllowsInvalid(false);

			fieldStart = new JFormattedTextField(doubleFormatterStart);
			fieldStart.setMaximumSize(new Dimension(100,19));
			fieldEnd = new JFormattedTextField(doubleFormatterEnd);
			fieldEnd.setMaximumSize(new Dimension(100,19));

			this.add(new JLabel("Start:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldStart, "w 50%, pushx");
			this.add(new JLabel("End:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldEnd, "w 50%");

			fieldStart.setText(annotationAttributeEntry.getStart() + "");
			fieldEnd.setText(annotationAttributeEntry.getEnd() + "");
		}
	}

	private class StringAttributeEntryPanel extends TimeAttributeEntryPanel<String>{
		JFormattedTextField fieldValue;

		public StringAttributeEntryPanel(AnnotationAttributeEntry<String> annotationAttributeEntry) {
			super(annotationAttributeEntry);
			fieldValue = new JFormattedTextField("");
			fieldValue.setMaximumSize(new Dimension(100,19));

			this.add(new JLabel("Value:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldValue, "w 50%");
			this.add(saveButton, "span, center, bottom, pushy");

			fieldValue.setText(annotationAttributeEntry.getValue());
		}

		@Override
		public void saveChanges() {
			if(!fieldStart.getText().isEmpty() && !fieldEnd.getText().isEmpty() && !fieldValue.getText().isEmpty()){
				entry.setStart(Double.parseDouble(fieldStart.getText()));
				entry.setEnd(Double.parseDouble(fieldEnd.getText()));
				entry.setValue(fieldValue.getText());
			}
		}

		@Override
		public void refreshValues() {
			fieldStart.setText(entry.getStart() + "");
			fieldEnd.setText(entry.getEnd() + "");
			fieldValue.setText(entry.getValue() + "");
		}


	}

	private class NumericAttributeEntryPanel extends TimeAttributeEntryPanel<Double>{
		JFormattedTextField fieldValue;

		public NumericAttributeEntryPanel(AnnotationAttributeEntry<Double> annotationAttributeEntry) {
			super(annotationAttributeEntry);
			NumberFormat decimalFormat = NumberFormat.getNumberInstance(Locale.ROOT);
			decimalFormat.setGroupingUsed(false);

			fieldValue = new JFormattedTextField(decimalFormat);
			fieldValue.setMaximumSize(new Dimension(100,19));

			this.add(new JLabel("Value:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldValue, "w 50%");
			this.add(saveButton, "span, center, bottom, pushy");

			fieldValue.setText(annotationAttributeEntry.getValue() + "");
		}

		@Override
		public void saveChanges() {
			if(!fieldStart.getText().isEmpty() && !fieldEnd.getText().isEmpty() && !fieldValue.getText().isEmpty()){
				entry.setStart(Double.parseDouble(fieldStart.getText()));
				entry.setEnd(Double.parseDouble(fieldEnd.getText()));
				entry.setValue(Double.parseDouble(fieldValue.getText()));
			}
		}

		@Override
		public void refreshValues() {
			fieldStart.setText(entry.getStart() + "");
			fieldEnd.setText(entry.getEnd() + "");
			fieldValue.setText(entry.getValue() + "");
		}
	}

	private class NominalAttributeEntryPanel extends TimeAttributeEntryPanel<String>{
		JList<String> attributeValueList;

		public NominalAttributeEntryPanel(AnnotationAttributeEntry<String> annotationAttributeEntry) {
			super(annotationAttributeEntry);

			
			attributeValueList = new JList<String>( ((AnnotationNominalAttribute) annotationAttributeEntry.getAnnotationAttribute()).getAllowedValues());
			JScrollPane attributeValueListPane = new JScrollPane(attributeValueList);
			attributeValueListPane.setMaximumSize(new Dimension(1000, attributeValueList.getModel().getSize() * 20));
			this.add(attributeValueListPane, "span, pushy, w 100%");
			this.add(saveButton, "span, center, bottom, pushy");

			attributeValueList.setSelectedValue(annotationAttributeEntry.getValue(), true);
		}

		@Override
		public void saveChanges() {
			if(!fieldStart.getText().isEmpty() && !fieldEnd.getText().isEmpty() && attributeValueList.getSelectedValue() != null){
				entry.setStart(Double.parseDouble(fieldStart.getText()));
				entry.setEnd(Double.parseDouble(fieldEnd.getText()));
				entry.setValue(attributeValueList.getSelectedValue().toString());
			}
		}

		@Override
		public void refreshValues() {
			fieldStart.setText(entry.getStart() + "");
			fieldEnd.setText(entry.getEnd() + "");
			attributeValueList.setSelectedValue(entry.getValue(), false);
		}
	}

	private class EventAttributeEntryPanel extends AttributeEntryPanel<Integer>{
		JFormattedTextField fieldValue;

		public EventAttributeEntryPanel(AnnotationAttributeEntry<Integer> annotationAttributeValue) {
			super(annotationAttributeValue);
			NumberFormatter doubleFormatter = new NumberFormatter(DecimalFormat.getInstance(Locale.ROOT));
			((DecimalFormat) doubleFormatter.getFormat()).setGroupingUsed(false);
			((DecimalFormat) doubleFormatter.getFormat()).applyPattern("#0.0000");
			doubleFormatter.setValueClass(Double.class);
			doubleFormatter.setMinimum(0.);
			doubleFormatter.setMaximum(Double.MAX_VALUE);
			doubleFormatter.setAllowsInvalid(false);

			fieldValue = new JFormattedTextField(doubleFormatter);
			fieldValue.setMaximumSize(new Dimension(100,19));

			this.add(new JLabel("Value:", SwingConstants.RIGHT), "w 50%");
			this.add(fieldValue, "w 50%, pushx");
			this.add(saveButton, "span, center, bottom, pushy");

			fieldValue.setText(annotationAttributeValue.getStart() + "");
		}

		@Override
		public void saveChanges() {
			entry.setStart(Double.parseDouble(fieldValue.getText()));
		}

		@Override
		public void refreshValues() {
			fieldValue.setText(entry.getStart() + "");			
		}

	}
	
	
	
	private class AddAttributeActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			

			LinkedHashMap<Integer, AnnotationAttribute<?>> idToAttributeMap = (LinkedHashMap<Integer, AnnotationAttribute<?>>) annotationController.getAnnotationAttributeTable().clone(); 
			
			// Remove Attributes that were already added
			for(int i = 0; i < attributeList.getModel().getSize(); i++){
				idToAttributeMap.remove(attributeList.getModel().getElementAt(i).getId());
			}
			
			// Fill an array with the data of the attributes that were not yet added
			Object[][] data = new Object[idToAttributeMap.size()][3];
			int dataIndex = 0;
			for(AnnotationAttribute<?> att: idToAttributeMap.values()){
				data[dataIndex][0] = att.getId();
				data[dataIndex][1] = att.getName();
				data[dataIndex][2] = att.getType();
				dataIndex++;
			}
			JTable annotationAttributeTable = new JTable(new DefaultTableModel(data, new String[]{"ID", "Name", "Type"}) {
				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex){
					return false;
				}
			});
			
			
			JButton loadFeaturesButton = new JButton ("Fill With Values From Amuse Event Feature");
			loadFeaturesButton.addActionListener(event -> {
				int selectedAttributeId = (Integer) annotationAttributeTable.getModel().getValueAt(annotationAttributeTable.getSelectedRow(), 0);
				
				// Close the frame that contains the list of the attributes
				SwingUtilities.getWindowAncestor(loadFeaturesButton).dispose();
				
				// Get available AMUSE Features
				File featureTableFile = new File(AmusePreferences.getFeatureTablePath());
	            List<Feature> features = new FeatureTable(featureTableFile).getFeatures();
	            for(int i = 0; i < features.size();){
	            	if(!features.get(i).getFeatureType().equals(Feature.FeatureType.Event.toString())){
	            		features.remove(i);
	            	}
	            	else{
	            		i++; // Only increment when no element is deleted
	            	}
	            }
	            
				// Fill an array with the data of the attributes that were not yet added
				Object[][] featureData = new Object[features.size()][2];
				int featureDataindex = 0;
				for(Feature f: features){
					featureData[featureDataindex][0] = f.getId();
					featureData[featureDataindex][1] = f.getDescription();
					featureDataindex++;
				}
				JTable amuseFeatureTable = new JTable(new DefaultTableModel(featureData, new String[]{"ID", "Description"}) {
					@Override
					public boolean isCellEditable(int rowIndex, int columnIndex){
						return false;
					}
				});
				
				amuseFeatureTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				amuseFeatureTable.setRowSelectionAllowed(true);

				
				
				final JComponent[] inputs = new JComponent[] {new JScrollPane(amuseFeatureTable)};
				int result = JOptionPane.showConfirmDialog(null, inputs, "Loading Amuse Features",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); 
				if (result == JOptionPane.OK_OPTION) {
					if(amuseFeatureTable.getSelectedRowCount() > 0){
						int selectedRow = amuseFeatureTable.getSelectedRow();
						int id = (int) featureData[selectedRow][0];
						
						// Calculate the path to the feature
						String trackPath = annotationController.getMusicFilePath();
						String relativeName = new String();
						if(trackPath.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
							relativeName = trackPath.substring(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)).getPath().length());
						} else {
							relativeName = trackPath;
						}
						if(relativeName.charAt(0) == File.separatorChar) {
							relativeName = relativeName.substring(1);
						}
						relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
						if(relativeName.lastIndexOf(File.separator) != -1) {
							relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE)
									+ File.separator
									+ relativeName
									+ relativeName.substring(relativeName.lastIndexOf(File.separator));
						} else {
							relativeName = AmusePreferences.get(KeysStringValue.FEATURE_DATABASE)
									+ File.separator
									+ relativeName
									+ File.separator
									+ relativeName;
						}
						relativeName += "_"
								+ id
								+ ".arff";
						try {
							DataSetAbstract eventTimesSet = new ArffDataSet(new File(relativeName));
							AnnotationAttribute<?> att = annotationController.addAttribute(selectedAttributeId);
							Attribute arffAtt = eventTimesSet.getAttribute(0);
							for(int i = 0; i < arffAtt.getValueCount(); i++){
								AnnotationAttributeEntry<Integer> entry = (AnnotationAttributeEntry<Integer>) annotationController.addNewEntryToAttribute(att);
								entry.setStart((Double) arffAtt.getValueAt(i));
							}
							
							
							
							annotationController.addUndoableAction(new AnnotationAddAttributeAction(annotationController, att));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
					}
				}
				else{ // Action was cancelled 
					// Show the list of the attributes again
					((JButton) e.getSource()).doClick();
				}
		
				attributeList.repaint();
				((AnnotationView) annotationController.getView()).resizePanels();
			});
			loadFeaturesButton.setEnabled(false);
			
			
			
			
			
			annotationAttributeTable.setRowSelectionAllowed(true);
			annotationAttributeTable.getSelectionModel().addListSelectionListener(event -> {
				loadFeaturesButton.setEnabled(
						annotationAttributeTable.getSelectedRowCount() == 1
						&& data[annotationAttributeTable.getSelectedRow()][2] == (Object) AnnotationAttributeType.EVENT);
			});
			
			
			
			
			JButton createAttributeButton = new JButton("Create new Attribute");
			createAttributeButton.addActionListener(event -> {
				// Close the frame that contains the list of the attributes
				SwingUtilities.getWindowAncestor(createAttributeButton).dispose();
				
				JList<String> nominalValueList = new JList<String>(new DefaultListModel<String>());
				nominalValueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


				JButton addAllowedValueForNominalButton = new JButton("Add");
				addAllowedValueForNominalButton.addActionListener(evt -> {
					String input = null;
					while ((input = JOptionPane.showInputDialog("Enter the name for the new value.")) != null) {
						if (input.equals("")) {
							JOptionPane.showMessageDialog(null, "The name of the new value cannot be empty");
						} 
						else if (((DefaultListModel<String>) nominalValueList.getModel()).contains(input)) {
							JOptionPane.showMessageDialog(null, "The name of the new value already exists.");
						}
						else if (input.contains("'")){
							JOptionPane.showMessageDialog(null, "The name of the new value cannot contain the character '");
						}
						else {
							((DefaultListModel<String>) nominalValueList.getModel()).addElement(input);
							break;
						}
					}
					nominalValueList.repaint();
				});
				
				JButton editAllowedValueForNominalButton = new JButton("Edit");
				editAllowedValueForNominalButton.addActionListener(evt -> {
					String selectedValue = (String) nominalValueList.getSelectedValue();
					if (selectedValue != null) {
						String input = null;
						while ((input = JOptionPane.showInputDialog("Enter the new name for the value.")) != null) {
							if (input.equals("")) {
								JOptionPane.showMessageDialog(null, "The name of the value cannot be empty");
							} 
							else if (!input.equals(selectedValue)
									&& ((DefaultListModel<?>) nominalValueList.getModel()).contains(input)) {
								JOptionPane.showMessageDialog(null, "The name of the value already exists.");
							}
							else if (input.contains("'")){
								JOptionPane.showMessageDialog(null, "The name of the value cannot contain the character '");
							}
							else {
								((DefaultListModel<String>) nominalValueList.getModel()).set(nominalValueList.getSelectedIndex(), input);
								break;
							}
						}
						nominalValueList.repaint();
					}
				});

				JButton removeAllowedValueForNominalButton = new JButton("Remove");
				removeAllowedValueForNominalButton.addActionListener(evt -> {
					int selectedIndex = nominalValueList.getSelectedIndex();
					if (selectedIndex != -1) {
						((DefaultListModel<String>) nominalValueList.getModel()).remove(selectedIndex);
						nominalValueList.repaint();
					}
				});
				
				
				
				
				JPanel nominalCardPanel = new JPanel(new MigLayout("fill, wrap"));
				JScrollPane attributeValueListPane = new JScrollPane(nominalValueList);
				nominalCardPanel.add(attributeValueListPane, "span, pushy, w 100%");
				nominalCardPanel.add(addAllowedValueForNominalButton, "span, center, split 3");
				nominalCardPanel.add(editAllowedValueForNominalButton);
				nominalCardPanel.add(removeAllowedValueForNominalButton);
				
				JPanel nominalAttributePanel = new JPanel(new CardLayout());
				nominalAttributePanel.add(nominalCardPanel, AnnotationAttributeType.NOMINAL.toString());
				nominalAttributePanel.add(new JPanel(), "");
				
				
				JPanel panel = new JPanel(new MigLayout("fill, wrap 2"));
				JTextField nameTextField = new JTextField();
				
				JComboBox<AnnotationAttributeType> typeComboBox = new JComboBox<AnnotationAttributeType>(AnnotationAttributeType.values());
				typeComboBox.addActionListener(evt ->{
					if(typeComboBox.getSelectedItem().equals(AnnotationAttributeType.NOMINAL)){
						((CardLayout) nominalAttributePanel.getLayout()).show(nominalAttributePanel, AnnotationAttributeType.NOMINAL.toString());
					}
					else{
						((CardLayout) nominalAttributePanel.getLayout()).show(nominalAttributePanel, "");
					}
					
				});

				panel.add(new JLabel("Name", JLabel.RIGHT), "w 20%");
				panel.add(nameTextField, "w 70%");
				panel.add(new JLabel("Type", JLabel.RIGHT), "w 20%");
				panel.add(typeComboBox, "w 70%");
				
				typeComboBox.setSelectedIndex(0);
				
				while(JOptionPane.showConfirmDialog(
						null,
						new JComponent[] {panel, nominalAttributePanel},
						"Adding a new Attribute",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION){
					String name = nameTextField.getText();
					// Check name
					if(name.equals("")){
						JOptionPane.showMessageDialog(null, "The name cannot be empty.");
					}
					else if(name.contains("'")){
						JOptionPane.showMessageDialog(null, "The name cannot contain the character \" ' \".");
					}
					else if(!annotationController.isAttributeNameAvailable(name)){
						JOptionPane.showMessageDialog(null, "The name is already taken."); 
					}
					// Check nominal values, if neccessary
					else if(typeComboBox.getSelectedItem().equals(AnnotationAttributeType.NOMINAL)
							&& ((DefaultListModel<String>) nominalValueList.getModel()).isEmpty()){
						JOptionPane.showMessageDialog(null, "At least one allowed value needs to be specified.");
					}
					else{
						AnnotationAttribute<?> att = null;
						switch((AnnotationAttributeType) typeComboBox.getSelectedItem()){
						case NOMINAL: att = new AnnotationNominalAttribute(name, annotationController.getNextAvailableId()); 
							for(int index = 0; index < nominalValueList.getModel().getSize(); index++){
								((AnnotationNominalAttribute) att).addAllowedValue((String) nominalValueList.getModel().getElementAt(index));
							}
						break;
						case STRING: att = new AnnotationStringAttribute(name, annotationController.getNextAvailableId()); break;
						case NUMERIC: att = new AnnotationNumericAttribute(name, annotationController.getNextAvailableId()); break;
						case EVENT: att = new AnnotationEventAttribute(name, annotationController.getNextAvailableId()); break;
						}
						annotationController.addNewAttribute(att);
						break;
					}
				}
				
				// Show the list of the attributes again
				((JButton) e.getSource()).doClick();
			});
			
			final JComponent[] inputs = new JComponent[] {new JScrollPane(annotationAttributeTable), createAttributeButton, loadFeaturesButton};
			int result = JOptionPane.showConfirmDialog(null, inputs, "Adding a new Attribute",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); 
			if (result == JOptionPane.OK_OPTION) {
				if(annotationAttributeTable.getSelectedRowCount() > 0){
					for(int selectedRow: annotationAttributeTable.getSelectedRows()){
						AnnotationAttribute<?> att = annotationController.addAttribute((Integer) annotationAttributeTable.getModel().getValueAt(selectedRow, 0));
						annotationController.addUndoableAction(new AnnotationAddAttributeAction(annotationController, att));
					} 
				}
			}
	
			attributeList.repaint();
			((AnnotationView) annotationController.getView()).resizePanels();
		}
	}

}
