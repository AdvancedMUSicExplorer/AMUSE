package amuse.scheduler.gui.training;

import java.awt.CardLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import amuse.data.FeatureTable;
import amuse.data.InputFeatureType;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import net.miginfocom.swing.MigLayout;

public class InputFeaturePanel extends JPanel {
	
	private JComboBox<InputFeatureType> inputFeatureTypeComboBox;
	private ProcessingHistoryPanel processingHistoryPanel;
	private RawInputFeaturePanel rawInputFeaturePanel;
	
	public InputFeaturePanel() {
		super(new MigLayout("fillx, wrap"));
		this.setBorder(new TitledBorder("Select Input Features"));
		
		processingHistoryPanel = new ProcessingHistoryPanel();
		rawInputFeaturePanel = new RawInputFeaturePanel();
		
		CardLayout cardLayout = new CardLayout();
		JPanel cardLayoutPanel = new JPanel(cardLayout);
		
		cardLayoutPanel.add(processingHistoryPanel, InputFeatureType.PROCESSED_FEATURES.toString());
		cardLayoutPanel.add(rawInputFeaturePanel, InputFeatureType.RAW_FEATURES.toString());
		
		inputFeatureTypeComboBox = new JComboBox<InputFeatureType>(new InputFeatureType[] {InputFeatureType.PROCESSED_FEATURES, InputFeatureType.RAW_FEATURES});
		inputFeatureTypeComboBox.addActionListener(e -> {
			cardLayout.show(cardLayoutPanel, inputFeatureTypeComboBox.getSelectedItem().toString());
		});
		
		this.add(new JLabel("Input Feature Type:"), "split 2");
		this.add(inputFeatureTypeComboBox, "pushx, growx");
		this.add(cardLayoutPanel, "pushx, growx");
	}

	public String getProcessingHistoryString() {
		return processingHistoryPanel.getProcessingHistoryString();
	}

	public List<Integer> getAttributesToIgnore() {
		switch((InputFeatureType) inputFeatureTypeComboBox.getSelectedItem()) {
		case PROCESSED_FEATURES:
			return processingHistoryPanel.getAttributesToIgnore();
		case RAW_FEATURES:
			return rawInputFeaturePanel.getAttributesToIgnore();
		}
		return null;
	}

	public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
		switch((InputFeatureType) inputFeatureTypeComboBox.getSelectedItem()) {
		case PROCESSED_FEATURES:
			processingHistoryPanel.setAttributesToIgnore(attributesToIgnore);
			break;
		case RAW_FEATURES:
			rawInputFeaturePanel.setAttributesToIgnore(attributesToIgnore);
			break;
		}
	}

	public void setProcessingModelString(String value) {
		processingHistoryPanel.setProcessingModelString(value);
	}

	public FeatureTable getInputFeatures() {
		return rawInputFeaturePanel.getInputFeatures();
	}

	public Integer getClassificationWindowSize() {
		if(inputFeatureTypeComboBox.getSelectedItem() == InputFeatureType.RAW_FEATURES) {
			return rawInputFeaturePanel.getClassificaitonWindowSize();
		} else {
			return -1;
		}
	}
	
	public Integer getClassificationWindowStepSize() {
		if(inputFeatureTypeComboBox.getSelectedItem() == InputFeatureType.RAW_FEATURES) {
			return rawInputFeaturePanel.getClassificaitonWindowStepSize();
		} else {
			return -1;
		}
	}

	public InputFeatureType getInputFeatureType() {
		return (InputFeatureType)inputFeatureTypeComboBox.getSelectedItem();
	}

	public JTextField getAttributesToIgnoreTextField() {
		return processingHistoryPanel.getAttributesToIgnoreTextField();
	}

	public void setInputFeatureType(InputFeatureType inputFeatureType) {
		inputFeatureTypeComboBox.setSelectedItem(inputFeatureType);
	}

	public void setInputFeatures(FeatureTable inputFeatures) {
		rawInputFeaturePanel.setInputFeatures(inputFeatures);
	}

	public void setClassificationWindowSize(Integer size) {
		rawInputFeaturePanel.setClassificationWindowSize(size);
	}

	public void setClassificationWindowStepSize(Integer stepSize) {
		rawInputFeaturePanel.setClassificationWindowStepSize(stepSize);
	}
	
	public Unit getUnit() {
		return rawInputFeaturePanel.getUnit();
	}
	
	public void setUnit(Unit unit) {
		rawInputFeaturePanel.setUnit(unit);
	}
}
