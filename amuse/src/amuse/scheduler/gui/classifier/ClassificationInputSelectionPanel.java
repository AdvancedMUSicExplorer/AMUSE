package amuse.scheduler.gui.classifier;

import java.awt.CardLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import amuse.data.GroundTruthSourceType;
import amuse.data.datasets.FileTableSet;
import amuse.data.io.DataInputInterface;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.FileInput;
import amuse.data.io.FileListInput;
import amuse.nodes.classifier.ClassificationConfiguration.InputSourceType;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.filesandfeatures.FileTreeController;
import amuse.scheduler.gui.filesandfeatures.FileTreeModel;
import amuse.scheduler.gui.filesandfeatures.FileTreeView;
import amuse.scheduler.gui.training.CategorySelectionPanel;
import amuse.scheduler.gui.training.ReadyInputSelectionPanel;
import net.miginfocom.swing.MigLayout;

public class ClassificationInputSelectionPanel extends JPanel{
	
  private String[] endings = {"mp3", "wav"};
  private File musicDatabaseFolder = new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE));
  private String musicDatabaseLabel = "Music Database";
  private FileTreeView fileView = new FileTreeView();
  private FileTreeModel ftModel = new FileTreeModel(musicDatabaseFolder, musicDatabaseLabel, endings);
  private FileTreeController ftController = new FileTreeController(ftModel, fileView);
  private ReadyInputSelectionPanel readyInputSelectionPanel;
  private CategorySelectionPanel categorySelectionPanel;
  private JComboBox<InputSourceType> inputSourceTypeComboBox;
	
	public ClassificationInputSelectionPanel() {
		super(new MigLayout("fillx, wrap"));
		this.setBorder(new TitledBorder("Select Classification Input"));
		
		readyInputSelectionPanel = new ReadyInputSelectionPanel("Select the Ready Input File", true);
		categorySelectionPanel = new CategorySelectionPanel(true);
		
		CardLayout cardLayout = new CardLayout();
		JPanel cardLayoutPanel = new JPanel(cardLayout);
		
		cardLayoutPanel.add(readyInputSelectionPanel, InputSourceType.READY_INPUT.toString());
		cardLayoutPanel.add(fileView.getView(), InputSourceType.FILE_LIST.toString());
		cardLayoutPanel.add(categorySelectionPanel, InputSourceType.CATEGORY_ID.toString());
		
		inputSourceTypeComboBox = new JComboBox<InputSourceType>(new InputSourceType[]{InputSourceType.FILE_LIST, InputSourceType.CATEGORY_ID, InputSourceType.READY_INPUT});
		inputSourceTypeComboBox.addActionListener(e -> {
			cardLayout.show(cardLayoutPanel, inputSourceTypeComboBox.getSelectedItem().toString());
		});
		cardLayout.show(cardLayoutPanel, inputSourceTypeComboBox.getSelectedItem().toString());
		
		this.add(new JLabel("Input Source Type:"), "split 2");
		this.add(inputSourceTypeComboBox, "pushx, growx");
		this.add(cardLayoutPanel, "pushx, growx");
	}

	public InputSourceType getSelectedInputSourceType() {
		return (InputSourceType)inputSourceTypeComboBox.getSelectedItem();
	}

	public JComboBox<InputSourceType> getInputSourceTypeComboBox() {
		return inputSourceTypeComboBox;
	}

	public List<Integer> getAttributesToIgnore() {
		return readyInputSelectionPanel.getAttributesToIgnore();
	}

	public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
		readyInputSelectionPanel.setAttributesToIgnore(attributesToIgnore);
	}

	public void setInputToClassify(DataInputInterface inputToClassify) {
		if(getSelectedInputSourceType().equals(InputSourceType.READY_INPUT)) {
			readyInputSelectionPanel.setSelectedPath(((FileListInput) inputToClassify).getInputFiles().get(0).getPath());
		} else if (getSelectedInputSourceType().equals(InputSourceType.FILE_LIST)){
			ftController.loadFiles(((FileListInput) inputToClassify).getInputFiles());
		} else {
			categorySelectionPanel.setCategory(new Integer(inputToClassify.toString()));
		}
	}

	public void setInputSourceType(InputSourceType type) {
		inputSourceTypeComboBox.setSelectedItem(type);
	}

	public DataSetAbstract getInputToClassify() throws IOException {
		if(getSelectedInputSourceType().equals(InputSourceType.READY_INPUT)) {
			return new DataSet(new File(readyInputSelectionPanel.getPath()), "ClassificationSet");
		} else if(getSelectedInputSourceType().equals(InputSourceType.FILE_LIST)){
			return new FileTableSet(ftModel.getFiles());
		} else {
			return new DataSet(new File(categorySelectionPanel.getSelectedCategoryPath()));
		}
	}

	public String getReadyInputPath() {
		return readyInputSelectionPanel.getPath();
	}

	public int getCategoryId() {
		return categorySelectionPanel.getSelectedCategoryID();
	}

}
