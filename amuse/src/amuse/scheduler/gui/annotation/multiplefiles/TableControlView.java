package amuse.scheduler.gui.annotation.multiplefiles;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListSelectionModel;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttributeType;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationNominalAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationNumericAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationStringAttribute;
import amuse.scheduler.gui.controller.MultipleFilesAnnotationController;
import net.miginfocom.swing.MigLayout;

public class TableControlView extends JPanel{
	
	MultipleFilesAnnotationController annotationController;
	
	public TableControlView(MultipleFilesAnnotationController annotationController, ListSelectionModel selectionModel){
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.annotationController = annotationController;
		
		JButton addTrackButton = new JButton("Add Track(s)");
		addTrackButton.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setCurrentDirectory(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)));
			fileChooser.setFileFilter(new FileNameExtensionFilter("", "mp3", "wav"));
			JCheckBox recursiveCheckBox = new JCheckBox("Recursive? ");
			recursiveCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(fileChooser.getPreferredSize().width, recursiveCheckBox.getPreferredSize().height + 10));
			panel.setLayout(new BorderLayout());
			panel.add(recursiveCheckBox, BorderLayout.WEST);
			fileChooser.setLayout(new BoxLayout(fileChooser, BoxLayout.Y_AXIS));
			fileChooser.add(panel, 3);
			if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				for(File file: fileChooser.getSelectedFiles()){
					if(file.isDirectory()){
						this.addFolder(file, recursiveCheckBox.isSelected());
					}
					else{
						this.annotationController.addTrack(file.getAbsolutePath());
					}
				}
				
			}
		});
		
		
		
		JButton removeTracksButton = new JButton ("Remove Selected Track(s)");
		removeTracksButton.addActionListener(e -> {
			if(JOptionPane.YES_OPTION == 
					JOptionPane.showConfirmDialog(null,
					"Do you really want to delete all selected rows and its content?",
					"Remove Rows", 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.QUESTION_MESSAGE)){
				this.annotationController.removeSelectedTracks();
				
			}
		});
		removeTracksButton.setEnabled(false);
		
		selectionModel.addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting()){
				if(((DefaultListSelectionModel) e.getSource()).getMaxSelectionIndex() >= 0){
					removeTracksButton.setEnabled(true);
				}
				else{
					removeTracksButton.setEnabled(false);
				}
			}
		});
		
		JButton addAttributeButton = new JButton("Add Annotation");
		addAttributeButton.addActionListener(e -> showAddAttributeDialog("Adding a new Attribute"));
		
		JCheckBox showAbsolutePathCheckBox = new JCheckBox("Show Absolute Path", false);
		showAbsolutePathCheckBox.addActionListener(e -> {
			annotationController.showAbsolutePath(showAbsolutePathCheckBox.isSelected());
		});
		
		
		this.add(showAbsolutePathCheckBox);
		this.add(Box.createRigidArea(new Dimension(30, 10)));
		this.add(addTrackButton);
		this.add(removeTracksButton);
		this.add(Box.createRigidArea(new Dimension(30, 10)));
		this.add(addAttributeButton);
	}
	
	private void addFolder(File file, boolean recursive){
		if(file.isDirectory()){
			int depth = recursive? Integer.MAX_VALUE: 1;
			try {
				Files.find(Paths.get(file.getAbsolutePath()),
						depth,
						(filePath, fileAttr) -> fileAttr.isRegularFile() 
											&& (filePath.toString().endsWith(".mp3") 
											|| filePath.toString().endsWith(".wav"))).forEach((Path path) -> annotationController.addTrack(path.toString()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			annotationController.addTrack(file.getAbsolutePath());
		}
	}

	public void showAddAttributeDialog(String title, AnnotationAttributeType... validTypesToShow){
		final AnnotationAttributeType[] validTypes;
		if(validTypesToShow.length == 0){
			validTypes = AnnotationAttributeType.values();
		}
		else{
			validTypes = validTypesToShow;
		}
		LinkedHashMap<Integer, AnnotationAttribute<?>> idToAttributeMap = (LinkedHashMap<Integer, AnnotationAttribute<?>>) annotationController.getAnnotationAttributeTable().clone(); 

		// Remove Attributes that were already added
		for(int i = 2; i < annotationController.getColumnCount(); i++){
			idToAttributeMap.remove(annotationController.getAttributeFromColumn(i).getId());
		}
		
		// Remove Attributes that are not in validTypes
		if(validTypes.length != AnnotationAttributeType.values().length){
			LinkedList<Integer> attributesToRemove = new LinkedList<Integer>();
			for(AnnotationAttribute<?> att: idToAttributeMap.values()){
				boolean contained = false;
				for(AnnotationAttributeType type: validTypes){
					contained = contained || type == att.getType();
				}
				if(!contained){
					attributesToRemove.add(att.getId());
				}
			}
			for(int id: attributesToRemove){
				idToAttributeMap.remove(id);
			}
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
		
		
		annotationAttributeTable.setRowSelectionAllowed(true);
		
		JButton removeAttributeButton = new JButton("Remove Attribute");
		removeAttributeButton.addActionListener(event ->{
			if(JOptionPane.showConfirmDialog(null, 
					"Do you really want to remove this attribute from the attribute table?",
					"Confirm Removal",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
				for(int row: annotationAttributeTable.getSelectedRows()){
					annotationController.removeAttributeFromAnnotationAttributeTable((int) annotationAttributeTable.getValueAt(row, 0));
				}
				SwingUtilities.getWindowAncestor(removeAttributeButton).dispose();
				showAddAttributeDialog(title, validTypesToShow);
			}
			
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
			
			JComboBox<AnnotationAttributeType> typeComboBox = new JComboBox<AnnotationAttributeType>(validTypes);
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
					}
					annotationController.addNewAttribute(att);
					break;
				}
			}
			
			// Show the list of the attributes again
			showAddAttributeDialog(title, validTypesToShow);
		});
		
		JPanel panel = new JPanel();
		panel.add(createAttributeButton);
		panel.add(removeAttributeButton);
		
		final JComponent[] inputs = new JComponent[] {new JScrollPane(annotationAttributeTable), panel};
		int result = JOptionPane.showConfirmDialog(null, inputs, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); 
		if (result == JOptionPane.OK_OPTION) {
			if(annotationAttributeTable.getSelectedRowCount() > 0){
				for(int selectedRow: annotationAttributeTable.getSelectedRows()){
					AnnotationAttribute<?> att = annotationController.getAnnotationAttributeTable().get((Integer) annotationAttributeTable.getModel().getValueAt(selectedRow, 0));
					annotationController.addAttribute(att);
				} 
			}
		}

		annotationController.repaintTable();
	}
}
