package amuse.scheduler.gui.training;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetException;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;

import net.miginfocom.swing.MigLayout;

public class FileListSelectionPanel extends JPanel{
	
	JTextField pathField = new JTextField(10);
	private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	
	public FileListSelectionPanel() {
		super(new MigLayout("fillx"));
		
		JButton selectPathButton = new JButton("...");
		selectPathButton.addActionListener(e -> {
			JFileChooser fc = new SelectArffFileChooser("", new File(""));
			if(fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			pathField.setText(fc.getSelectedFile().toString());
		});
		
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
				if(f.exists() && !f.isDirectory() && pathField.getText().endsWith(".arff")) {
					updateCheckBoxes(f);
				} else {
					removeCheckBoxes();
					revalidate();
				}
			}
		});
		
		this.setBorder(new TitledBorder("Select Category File"));
		this.add(pathField, "split 2, growx");
		this.add(selectPathButton, "wrap");
	}

	private void updateCheckBoxes(File fileList) {
		removeCheckBoxes();
		try {
			DataSet dataSet = new DataSet(fileList);
			dataSet.checkNumericAttribute("Id");
			dataSet.checkStringAttribute("Path");
			dataSet.checkNominalAttribute("Unit");
			dataSet.checkNumericAttribute("Start");
			dataSet.checkNumericAttribute("End");
			for(int i=5;i<dataSet.getAttributeCount();i++) {
				JCheckBox categoryCheckBox = new JCheckBox(dataSet.getAttribute(i).getName());
				checkBoxes.add(categoryCheckBox);
//				this.add(new JLabel(dataSet.getAttribute(i).getName()));
				this.add(categoryCheckBox, "pushx, gap rel, wrap");
			}
		} catch(IOException exception){
			
		} catch(DataSetException exception) {
			JOptionPane.showMessageDialog(this,
				    "This is not a correct category file!\n" + exception.getMessage(),
				    "Category File Incorrect",
				    JOptionPane.ERROR_MESSAGE);
		}
		this.revalidate();
	}
	
	private void removeCheckBoxes() {
		for(JCheckBox checkBox : checkBoxes) {
			this.remove(checkBox);
		}
		checkBoxes = new ArrayList<JCheckBox>();
	}

	public String getPath() {
		return pathField.getText();
	}

	public List<Integer> getAttributesToPredict() {
		List<Integer> attributesToPredict = new ArrayList<Integer>();
		for(int i=0;i<checkBoxes.size();i++) {
			if(checkBoxes.get(i).isSelected()) {
				attributesToPredict.add(i);
			}
		}
		return attributesToPredict;
	}

	public void setAttributesToPredict(List<Integer> attributesToPredict) {
		for(int i=0;i<checkBoxes.size();i++) {
			checkBoxes.get(i).setSelected(attributesToPredict.contains(i));
		}
	}

	public void setSelectedPath(String groundTruthSource) {
		pathField.setText(groundTruthSource);
	}

}
