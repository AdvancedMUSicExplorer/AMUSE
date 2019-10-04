package amuse.scheduler.gui.classifier;

import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import amuse.data.GroundTruthSourceType;
import amuse.scheduler.gui.training.CategorySelectionPanel;
import net.miginfocom.swing.MigLayout;

public class ClassificationGroundTruthSelectionPanel extends JPanel {
	
	private CategorySelectionPanel categorySelectionPanel = new CategorySelectionPanel(false);
	private ModelSelectionPanel modelSelectionPanel = new ModelSelectionPanel("Select Input Model");
	private JComboBox<String> groundTruthSourceTypeComboBox;
	
	public ClassificationGroundTruthSelectionPanel() {
		super(new MigLayout("fillx, wrap"));
		this.setBorder(new TitledBorder("Select Ground Truth"));
		
		CardLayout cardLayout = new CardLayout();
		JPanel cardLayoutPanel = new JPanel(cardLayout);
		cardLayoutPanel.add(categorySelectionPanel, "CATEGORY_ID");
		cardLayoutPanel.add(modelSelectionPanel, "MODEL_PATH");
		
		groundTruthSourceTypeComboBox = new JComboBox<String>(new String[] {"CATEGORY_ID","MODEL_PATH"});
		groundTruthSourceTypeComboBox.addActionListener(e -> {
			cardLayout.show(cardLayoutPanel, groundTruthSourceTypeComboBox.getSelectedItem().toString());
		});
		cardLayout.show(cardLayoutPanel, groundTruthSourceTypeComboBox.getSelectedItem().toString());
		
		this.add(new JLabel("Ground Truth Source Type:"), "split 2");
		this.add(groundTruthSourceTypeComboBox, "pushx, growx");
		this.add(cardLayoutPanel, "pushx, growx");
	}
	
	public JComboBox<String> getGroundTruthSourceTypeComboBox() {
		return groundTruthSourceTypeComboBox;
	}

	public String getSelectedGroundTruthSourceType() {
		return (String)groundTruthSourceTypeComboBox.getSelectedItem();
	}

	public int getGroundTruthCategoryId() {
		if(groundTruthSourceTypeComboBox.getSelectedItem().equals("CATEGORY_ID")) {
			return categorySelectionPanel.getSelectedCategoryID();
		}
		else {
			return -1;
		}
	}

	public void setCategoryId(int id) {
		categorySelectionPanel.setCategory(id);
		
	}

	public void setGroundTruthSourceType(String type) {
		groundTruthSourceTypeComboBox.setSelectedItem(type);
	}

	public List<Integer> getAttributesToPredict() {
		if(getSelectedGroundTruthSourceType().equals("CATEGORY_ID")) {
			return categorySelectionPanel.getAttributesToPredict();
		} else {
			return new ArrayList<Integer>();
		}
	}

	public void setAttributesToPredict(List<Integer> attributesToPredict) {
		categorySelectionPanel.setAttributesToPredict(attributesToPredict);
	}

	public String getPath() {
		if(getSelectedGroundTruthSourceType().equals("CATEGORY_ID")) {
			return "-1";
		} else {
			return modelSelectionPanel.getPath();
		}
	}

	public void setPath(String path) {
		modelSelectionPanel.setPath(path);
	}

}
