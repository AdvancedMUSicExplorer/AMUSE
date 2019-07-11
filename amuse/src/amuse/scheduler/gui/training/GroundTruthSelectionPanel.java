package amuse.scheduler.gui.training;

import java.awt.CardLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import amuse.data.GroundTruthSourceType;
import net.miginfocom.swing.MigLayout;

public class GroundTruthSelectionPanel extends JPanel {

	private JComboBox<GroundTruthSourceType> groundTruthSourceTypeComboBox;
	private ReadyInputSelectionPanel readyInputSelectionPanel;
	private CategorySelectionPanel categorySelectionPanel;
	private FileListSelectionPanel fileListSelectionPanel;

	public GroundTruthSelectionPanel() {
		super(new MigLayout("fillx, wrap"));
		this.setBorder(new TitledBorder("Select Ground Truth"));
		
		categorySelectionPanel = new CategorySelectionPanel(false);
		fileListSelectionPanel = new FileListSelectionPanel();
		readyInputSelectionPanel = new ReadyInputSelectionPanel("Select the Ready Input File", false);
		
		CardLayout cardLayout = new CardLayout();
		JPanel cardLayoutPanel = new JPanel(cardLayout);

		cardLayoutPanel.add(categorySelectionPanel, GroundTruthSourceType.CATEGORY_ID.toString());
		cardLayoutPanel.add(fileListSelectionPanel, GroundTruthSourceType.FILE_LIST.toString());
		cardLayoutPanel.add(readyInputSelectionPanel, GroundTruthSourceType.READY_INPUT.toString());
		
		groundTruthSourceTypeComboBox = new JComboBox<GroundTruthSourceType>(new GroundTruthSourceType[]{GroundTruthSourceType.CATEGORY_ID, GroundTruthSourceType.FILE_LIST, GroundTruthSourceType.READY_INPUT});
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
		case FILE_LIST:
			return fileListSelectionPanel.getPath();
		}
		return null;
	}
	
	public List<Integer> getAttributesToPredict(){
		switch((GroundTruthSourceType) groundTruthSourceTypeComboBox.getSelectedItem()){
		case CATEGORY_ID:
			return categorySelectionPanel.getAttributesToPredict();
		case READY_INPUT:
			return readyInputSelectionPanel.getAttributesToPredict();
		case FILE_LIST:
			return fileListSelectionPanel.getAttributesToPredict();
		}
		return null;
	}
	
	public void setAttributesToPredict(List<Integer> attributesToPredict) {
		switch((GroundTruthSourceType)groundTruthSourceTypeComboBox.getSelectedItem()) {
		case CATEGORY_ID:
			categorySelectionPanel.setAttributesToPredict(attributesToPredict); break;
		case READY_INPUT:
			readyInputSelectionPanel.setAttributesToPredict(attributesToPredict); break;
		case FILE_LIST:
			fileListSelectionPanel.setAttributesToPredict(attributesToPredict); break;
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
		case FILE_LIST:
			fileListSelectionPanel.setSelectedPath(groundTruthSource);
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
