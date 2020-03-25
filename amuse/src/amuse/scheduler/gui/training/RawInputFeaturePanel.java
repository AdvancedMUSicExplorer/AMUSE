package amuse.scheduler.gui.training;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.preferences.AmusePreferences;
import amuse.scheduler.gui.dialogs.AttributeSelector;
import amuse.scheduler.gui.filesandfeatures.FeatureTableController;
import amuse.scheduler.gui.filesandfeatures.FeatureTableModel;
import amuse.scheduler.gui.filesandfeatures.FeatureTableView;
import amuse.util.AmuseLogger;
import net.miginfocom.swing.MigLayout;

public class RawInputFeaturePanel extends JPanel {
	
	private JLabel attributesToIgnoreLabel = new JLabel("Attributes to ignore:");
    private JTextField attributesToIgnoreTextField = new JTextField(10);
    private TitledBorder title = new TitledBorder("Select Raw Input Features");
    private FeatureTable featureTable = new FeatureTable();
    private JButton featureButton;
    private JLabel windowSizeLabel = new JLabel("Window Size (ms):");
    private JLabel windowOverlapLabel = new JLabel("Window Overlap (ms):");
    private JTextField windowSizeTextField = new JTextField(10);
    private JTextField windowOverlapTextField = new JTextField(10);
    
    public RawInputFeaturePanel() {
    	super(new MigLayout("fillx"));
    	this.setBorder(title);
    	
    	featureButton = new JButton("Select features from list");
    	featureButton.addActionListener(e -> {
			RawFeatureSelector featureSelector = new RawFeatureSelector(featureTable);
			featureTable = featureSelector.getFeatureTable();
		});
    	windowSizeTextField.setText("5000");
    	windowOverlapTextField.setText("2500");
    	this.add(featureButton, "wrap");
    	this.add(windowSizeLabel);
    	this.add(windowSizeTextField, "pushx, wrap");
    	this.add(windowOverlapLabel);
    	this.add(windowOverlapTextField, "pushx, wrap");
    	this.add(attributesToIgnoreLabel, "pushx, wrap");
        this.add(attributesToIgnoreTextField, "growx, wrap");
    }
    
    private class RawFeatureSelector extends JComponent{
    	
    	private FeatureTableModel featureTableModel;
        private final FeatureTableView featureTableView = new FeatureTableView();
        private final FeatureTableController featureTableController;
        private final File featureTableFile = new File(AmusePreferences.getFeatureTablePath());
    	
    	private RawFeatureSelector(FeatureTable selectedFeatures) {
            featureTableModel = new FeatureTableModel(new FeatureTable(featureTableFile));
            
            JDialog dialog = new JDialog((Frame)null, "Raw Feature Selector", true);
            featureTableController = new FeatureTableController(getFeatureTableModel(), featureTableView); 
            featureTableController.setSelectedFeatures(selectedFeatures);
            
            dialog.setContentPane(featureTableView.getView());
            dialog.pack();   
            dialog.setLocationByPlatform(true);
            dialog.setVisible(true);
    	}
    	
    	private FeatureTableModel getFeatureTableModel() {
            if (featureTableModel == null) {
                featureTableModel = new FeatureTableModel(new FeatureTable(featureTableFile));
            }
            return featureTableModel;
        }

		private FeatureTable getFeatureTable() {
			return featureTableModel.getCurrentFeatureTable();
		}
    	
    }
    
    public FeatureTable getInputFeatures() {
    	List<Feature> features = this.featureTable.getFeatures();
    	List<Feature> selectedFeatures = new ArrayList<Feature>();
    	for(Feature feature : features) {
    		if(feature.isSelectedForExtraction()) {
    			selectedFeatures.add(feature);
    		}
    	}
    	return new FeatureTable(selectedFeatures);
    }
    
    public int getClassificaitonWindowSize() {
    	int size = -1;
    	try {
    		size = Integer.parseInt(windowSizeTextField.getText());
    	} catch(NumberFormatException e) {
    		AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Please specify the Classification Window Size correctly.");
    	}
    	return size;
    }
    
    public int getClassificaitonWindowOverlap() {
    	int overlap = -1;
    	try {
    		overlap = Integer.parseInt(windowOverlapTextField.getText());
    	} catch(NumberFormatException e) {
    		AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Please specify the Classification Window Overlap correctly.");
    	}
    	return overlap;
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
    
    public JTextField getAttributesToIgnoreTextField() {
    	return attributesToIgnoreTextField;
    }

	public void setInputFeatures(FeatureTable inputFeatures) {
		this.featureTable = inputFeatures;
	}

	public void setClassificationWindowSize(Integer size) {
		this.windowSizeTextField.setText(size.toString());
	}

	public void setClassificationWindowOverlap(Integer overlap) {
		this.windowOverlapTextField.setText(overlap.toString());
	}

}
