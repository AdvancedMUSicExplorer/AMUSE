package amuse.scheduler.gui.training;

import java.awt.CardLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Level;

import amuse.data.GroundTruthSourceType;
import amuse.data.io.DataSet;
import amuse.scheduler.gui.dialogs.AttributeSelector;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.util.AmuseLogger;
import net.miginfocom.swing.MigLayout;

public class GroundTruthSelectionPanel extends JPanel {

	private JComboBox<GroundTruthSourceType> groundTruthSourceTypeComboBox;
	private ReadyInputSelectionPanel readyInputSelectionPanel;
	private CategorySelectionPanel categorySelectionPanel;

	public GroundTruthSelectionPanel() {
		super(new MigLayout("fillx, wrap"));
		this.setBorder(new TitledBorder("Select Ground Truth"));
		
		categorySelectionPanel = new CategorySelectionPanel();
		readyInputSelectionPanel = new ReadyInputSelectionPanel("Select the Ready Input File");
		
		CardLayout cardLayout = new CardLayout();
		JPanel cardLayoutPanel = new JPanel(cardLayout);

		cardLayoutPanel.add(categorySelectionPanel, GroundTruthSourceType.CATEGORY_ID.toString());
		cardLayoutPanel.add(readyInputSelectionPanel, GroundTruthSourceType.READY_INPUT.toString());
		
		groundTruthSourceTypeComboBox = new JComboBox<GroundTruthSourceType>(new GroundTruthSourceType[]{GroundTruthSourceType.CATEGORY_ID, GroundTruthSourceType.READY_INPUT});
		groundTruthSourceTypeComboBox.addActionListener(e -> {
			cardLayout.show(cardLayoutPanel, groundTruthSourceTypeComboBox.getSelectedItem().toString());
		});
		
		//groundTruthSourceTypeComboBox.setSelectedIndex(2);
		
		this.add(new JLabel("Ground Truth Source Type:"), "split 2");
		this.add(groundTruthSourceTypeComboBox, "pushx, growx");
		this.add(cardLayoutPanel, "pushx, growx");
	}
	
	private class ReadyInputSelectionPanel extends JPanel{
		
		private JTextField pathField;
		private JLabel attributesToClassifyLabel = new JLabel("Attributes to classify:");
	    private JTextField attributesToClassifyTextField = new JTextField(10);
	    private JLabel attributesToIgnoreLabel = new JLabel("Attributes to ignore:");
	    private JTextField attributesToIgnoreTextField = new JTextField(10);
		
		public ReadyInputSelectionPanel(String title) {
			super(new MigLayout("fillx"));
			pathField = new JTextField();
			
			JButton selectPathButton = new JButton("...");
			selectPathButton.addActionListener(e ->{
				JFileChooser fc = new SelectArffFileChooser("", new File(""));
		        if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
		            return;
		        }
		        pathField.setText(fc.getSelectedFile().toString());

			});
			
			JButton selectAttributesToClassifyButton = new JButton("...");
			selectAttributesToClassifyButton.addActionListener(e -> {
				AttributeSelector attributeSelector = new AttributeSelector(pathField.getText(), attributesToClassifyTextField.getText());
				attributesToClassifyTextField.setText(attributeSelector.getSelectedAttributes().toString().replaceAll("\\[", "").replaceAll("\\]", ""));
			});
			
			JButton selectAttributesToIgnoreButton = new JButton("...");
			selectAttributesToIgnoreButton.addActionListener(e -> {
				AttributeSelector attributeSelector = new AttributeSelector(pathField.getText(), attributesToIgnoreTextField.getText());
				attributesToIgnoreTextField.setText(attributeSelector.getSelectedAttributes().toString().replaceAll("\\[", "").replaceAll("\\]", ""));
			});
			
			selectAttributesToClassifyButton.setEnabled(false);
			selectAttributesToIgnoreButton.setEnabled(false);
			
			pathField.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					update();
				}
				
				public void removeUpdate(DocumentEvent e) {
					update();
				}
				
				public void insertUpdate(DocumentEvent e) {
					update();
				}
				
				private void update() {
					File f = new File(pathField.getText());
					selectAttributesToClassifyButton.setEnabled(f.exists() && !f.isDirectory() && pathField.getText().endsWith(".arff"));
					selectAttributesToIgnoreButton.setEnabled(f.exists() && !f.isDirectory() && pathField.getText().endsWith(".arff"));
				}
			});
			
			this.setBorder(new TitledBorder(title));
			
			this.add(pathField, "split 2, growx, pushx");
			this.add(selectPathButton, "pushx, wrap");
			this.add(attributesToClassifyLabel, "pushx, wrap");
	        this.add(attributesToClassifyTextField, "split 2, growx, pushx");
	        this.add(selectAttributesToClassifyButton, "pushx, wrap");
	        this.add(attributesToIgnoreLabel, "pushx, wrap");
	        this.add(attributesToIgnoreTextField, "split 2, growx, pushx");
	        this.add(selectAttributesToIgnoreButton, "pushx, wrap");
			
		}
		
		public String getPath(){
			return pathField.getText();
		}

		public void setSelectedPath(String path) {
			pathField.setText(path);
		}
		
		public List<Integer> getAttributesToIgnore(){
	    	String attributesToIgnoreString = attributesToIgnoreTextField.getText();
			attributesToIgnoreString = attributesToIgnoreString.replaceAll("\\[", "").replaceAll("\\]", "");
			String[] attributesToIgnoreStringArray = attributesToIgnoreString.split("\\s*,\\s*");
			List<Integer> attributesToIgnore = new ArrayList<Integer>();
			try {
				for(String str : attributesToIgnoreStringArray) {
					if(!str.equals("")) {
						attributesToIgnore.add(Integer.parseInt(str));
					}
				}
			} catch(NumberFormatException e) {
				AmuseLogger.write(this.getClass().getName(), Level.WARN,
						"The attributes to ignore were not properly specified. All features will be used for training.");
				attributesToIgnore = new ArrayList<Integer>();
			}
			return attributesToIgnore;
	    }
		
		public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
	    	attributesToIgnoreTextField.setText(attributesToIgnore.toString());
	    }
		
		public List<Integer> getAttributesToClassify(){
	    	String attributesToClassifyString = attributesToClassifyTextField.getText();
			attributesToClassifyString = attributesToClassifyString.replaceAll("\\[", "").replaceAll("\\]", "");
			String[] attributesToClassifyStringArray = attributesToClassifyString.split("\\s*,\\s*");
			List<Integer> attributesToClassify = new ArrayList<Integer>();
			try {
				for(String str : attributesToClassifyStringArray) {
					if(!str.equals("")) {
						attributesToClassify.add(Integer.parseInt(str));
					}
				}
			} catch(NumberFormatException e) {
				AmuseLogger.write(this.getClass().getName(), Level.WARN,
						"The attributes to classify were not properly specified.");
				attributesToClassify = new ArrayList<Integer>();
			}
			return attributesToClassify;
	    }
		
		public void setAttributesToClassify(List<Integer> attributesToClassify) {
			attributesToClassifyTextField.setText(attributesToClassify.toString());
		}
	}
	
	public String getSelectedGroundTruthSource(){
		switch((GroundTruthSourceType) groundTruthSourceTypeComboBox.getSelectedItem()){
		case CATEGORY_ID:
			return categorySelectionPanel.getSelectedCategoryID() + "";
		case READY_INPUT:
			return readyInputSelectionPanel.getPath();
		}
		return null;
	}
	
	public List<Integer> getAttributesToClassify(){
		switch((GroundTruthSourceType) groundTruthSourceTypeComboBox.getSelectedItem()){
		case CATEGORY_ID:
			return categorySelectionPanel.getAttributesToClassify();
		case READY_INPUT:
			return readyInputSelectionPanel.getAttributesToClassify();
		}
		return null;
	}
	
	public void setAttributesToClassify(List<Integer> attributesToClassify) {
		switch((GroundTruthSourceType)groundTruthSourceTypeComboBox.getSelectedItem()) {
		case CATEGORY_ID:
			categorySelectionPanel.setAttributesToClassify(attributesToClassify); break;
		case READY_INPUT:
			readyInputSelectionPanel.setAttributesToClassify(attributesToClassify); break;
		}
	}

	public GroundTruthSourceType getSelectedGroundTruthSourceType() {
		return (GroundTruthSourceType) groundTruthSourceTypeComboBox.getSelectedItem();
	}

	public void setGroundTruthSourceType(GroundTruthSourceType type) {
		groundTruthSourceTypeComboBox.setSelectedItem(type);
	}

	public void setGroundTruthSource(String groundTruthSource) {
		switch((GroundTruthSourceType) groundTruthSourceTypeComboBox.getSelectedItem()){
		case CATEGORY_ID:
			categorySelectionPanel.setCategory(new Integer(groundTruthSource));
			break;
		case READY_INPUT:
			readyInputSelectionPanel.setSelectedPath(groundTruthSource);
			break;
		}
	}
	
	public JComboBox<GroundTruthSourceType> getGroundTruthSourceTypeComboBox(){
		return groundTruthSourceTypeComboBox;
	}
	
	public List<Integer> getAttributesToIgnore() {
		return readyInputSelectionPanel.getAttributesToIgnore();
	}
	
	public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
		readyInputSelectionPanel.setAttributesToIgnore(attributesToIgnore);
	}
}
