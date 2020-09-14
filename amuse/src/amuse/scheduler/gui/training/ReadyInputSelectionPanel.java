package amuse.scheduler.gui.training;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Level;

import amuse.scheduler.gui.dialogs.AttributeSelector;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.util.AmuseLogger;
import net.miginfocom.swing.MigLayout;

public class ReadyInputSelectionPanel extends JPanel{
	
	private JTextField pathField;
	private JLabel attributesToPredictLabel = new JLabel("Attributes to classify:");
    private JTextField attributesToPredictTextField = new JTextField(10);
    private JLabel attributesToIgnoreLabel = new JLabel("Attributes to ignore:");
    private JTextField attributesToIgnoreTextField = new JTextField(10);
	
	public ReadyInputSelectionPanel(String title, boolean classify) {
		super(new MigLayout("fillx"));
		pathField = new JTextField(10);
		
		JButton selectPathButton = new JButton("...");
		selectPathButton.addActionListener(e -> {
			JFileChooser fc = new SelectArffFileChooser("", new File(""));
	        if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
	            return;
	        }
	        pathField.setText(fc.getSelectedFile().toString());

		});
		
		JButton selectAttributesToPredictButton = new JButton("...");
		selectAttributesToPredictButton.addActionListener(e -> {
			AttributeSelector attributeSelector = new AttributeSelector(pathField.getText(), attributesToPredictTextField.getText());
			attributesToPredictTextField.setText(attributeSelector.getSelectedAttributes().toString().replaceAll("\\[", "").replaceAll("\\]", ""));
		});
		
		JButton selectAttributesToIgnoreButton = new JButton("...");
		selectAttributesToIgnoreButton.addActionListener(e -> {
			AttributeSelector attributeSelector = new AttributeSelector(pathField.getText(), attributesToIgnoreTextField.getText());
			attributesToIgnoreTextField.setText(attributeSelector.getSelectedAttributes().toString().replaceAll("\\[", "").replaceAll("\\]", ""));
		});
		
		selectAttributesToPredictButton.setEnabled(false);
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
				boolean enable = f.exists() && !f.isDirectory() && pathField.getText().endsWith(".arff");
				selectAttributesToPredictButton.setEnabled(enable);
				selectAttributesToIgnoreButton.setEnabled(enable);
			}
		});
		
		this.setBorder(new TitledBorder(title));
		this.add(pathField, "split 2, growx");
		this.add(selectPathButton, "wrap");
		if(!classify) {
			this.add(attributesToPredictLabel, "pushx, wrap");
        	this.add(attributesToPredictTextField, "split2, growx");
        	this.add(selectAttributesToPredictButton, "wrap");
        }
        this.add(attributesToIgnoreLabel, "pushx, wrap");
        this.add(attributesToIgnoreTextField, "split 2, growx");
        this.add(selectAttributesToIgnoreButton, "wrap");
		
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
	
	public List<Integer> getAttributesToPredict(){
    	String attributesToPredictString = attributesToPredictTextField.getText();
		attributesToPredictString = attributesToPredictString.replaceAll("\\[", "").replaceAll("\\]", "");
		String[] attributesToPredictStringArray = attributesToPredictString.split("\\s*,\\s*");
		List<Integer> attributesToPredict = new ArrayList<Integer>();
		try {
			for(String str : attributesToPredictStringArray) {
				if(!str.equals("")) {
					attributesToPredict.add(Integer.parseInt(str));
				}
			}
		} catch(NumberFormatException e) {
			AmuseLogger.write(this.getClass().getName(), Level.WARN,
					"The attributes to classify were not properly specified.");
			attributesToPredict = new ArrayList<Integer>();
		}
		return attributesToPredict;
    }
	
	public void setAttributesToPredict(List<Integer> attributesToPredict) {
		attributesToPredictTextField.setText(attributesToPredict.toString());
	}
}