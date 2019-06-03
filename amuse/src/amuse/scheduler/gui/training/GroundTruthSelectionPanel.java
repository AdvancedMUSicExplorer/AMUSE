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
		readyInputSelectionPanel = new ReadyInputSelectionPanel("Select the Ready Input File", false);
		
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
