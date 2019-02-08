package amuse.scheduler.gui.training;

import java.awt.CardLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import amuse.data.GroundTruthSourceType;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import net.miginfocom.swing.MigLayout;

public class GroundTruthSelectionPanel extends JPanel {

	private JComboBox<GroundTruthSourceType> groundTruthSourceTypeComboBox;
	private PathSelectionPanel readyInputSelectionPanel;
	private PathSelectionPanel fileListSelectionPanel;
	private CategorySelectionPanel categorySelectionPanel;

	public GroundTruthSelectionPanel() {
		super(new MigLayout("fillx, wrap"));
		this.setBorder(new TitledBorder("Select Ground Truth"));
		
		categorySelectionPanel = new CategorySelectionPanel();
		readyInputSelectionPanel = new PathSelectionPanel("Select the Ready Input File");
		fileListSelectionPanel = new PathSelectionPanel("Select the File List");
		
		CardLayout cardLayout = new CardLayout();
		JPanel cardLayoutPanel = new JPanel(cardLayout);

		cardLayoutPanel.add(categorySelectionPanel, GroundTruthSourceType.CATEGORY_ID.toString());
		cardLayoutPanel.add(readyInputSelectionPanel, GroundTruthSourceType.READY_INPUT.toString());
		cardLayoutPanel.add(fileListSelectionPanel, GroundTruthSourceType.FILE_LIST.toString());
		
		// TODO for future updates: Make every ground truth source type configurable (GroundTruthSourceType.values())
		groundTruthSourceTypeComboBox = new JComboBox<GroundTruthSourceType>(new GroundTruthSourceType[]{GroundTruthSourceType.CATEGORY_ID});
		groundTruthSourceTypeComboBox.addActionListener(e -> {
			cardLayout.show(cardLayoutPanel, groundTruthSourceTypeComboBox.getSelectedItem().toString());
		});
		
		//groundTruthSourceTypeComboBox.setSelectedIndex(2);
		
		this.add(new JLabel("Ground Truth Source Type:"), "split 2");
		this.add(groundTruthSourceTypeComboBox, "pushx, growx");
		this.add(cardLayoutPanel, "pushx, growx");
	}
	
	class PathSelectionPanel extends JPanel{
		
		private JTextField pathField;
		
		public PathSelectionPanel(String title) {
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
			this.setBorder(new TitledBorder(title));
			
			this.add(pathField, "split 2, growx, pushx");
			this.add(selectPathButton, "");
			
		}
		
		public String getPath(){
			return pathField.getText();
		}

		public void setSelectedPath(String path) {
			pathField.setText(path);
		}
	}
	
	public String getSelectedGroundTruthSource(){
		switch((GroundTruthSourceType) groundTruthSourceTypeComboBox.getSelectedItem()){
		case CATEGORY_ID:
			return categorySelectionPanel.getSelectedCategoryID() + "";
		case FILE_LIST:
			return fileListSelectionPanel.getPath();
		case READY_INPUT:
			return readyInputSelectionPanel.getPath();
		}
		return null;
	}
	
	public List<Integer> getCategoriesToClassify(){
		//TODO make this method work also with other groundtruth source types
		switch((GroundTruthSourceType) groundTruthSourceTypeComboBox.getSelectedItem()){
		case CATEGORY_ID:
			return categorySelectionPanel.getCategoriesToClassify();
		case FILE_LIST:
			return new ArrayList<Integer>();
		case READY_INPUT:
			return new ArrayList<Integer>();
		}
		return null;
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
		case FILE_LIST:
			fileListSelectionPanel.setSelectedPath(groundTruthSource);
			break;
		case READY_INPUT:
			readyInputSelectionPanel.setSelectedPath(groundTruthSource);
			break;
		}
	}
}
