package amuse.scheduler.gui.classifier;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import net.miginfocom.swing.MigLayout;

public class ModelSelectionPanel extends JPanel {
	private JLabel pathLabel = new JLabel("Path to Input Model:");
	private JTextField pathField = new JTextField(10);
	
	public ModelSelectionPanel(String title) {
		super(new MigLayout("fillx"));
		
		JButton selectPathButton = new JButton("...");
		selectPathButton.addActionListener(e ->{
			JFileChooser fc = new JFileChooser(new File(""));
	        if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
	            return;
	        }
	        pathField.setText(fc.getSelectedFile().toString());
		});
		this.add(pathLabel, "pushx, wrap");
		this.add(pathField, "split 2, growx");
		this.add(selectPathButton, "wrap");
	}

	public String getPath() {
		return pathField.getText();
	}

	public void setPath(String path) {
		pathField.setText(path);
	}
}
