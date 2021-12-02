/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2010 by code authors
 * 
 * Created at TU Dortmund, Chair of Algorithm Engineering
 * (Contact: <http://ls11-www.cs.tu-dortmund.de>) 
 *
 * AMUSE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMUSE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Creation date: 29.07.2009
 */
package amuse.scheduler.gui.classifier;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import amuse.data.ModelType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.FeatureTable;
import amuse.data.GroundTruthSourceType;
import amuse.data.io.DataInputInterface;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.nodes.classifier.ClassificationConfiguration.InputSourceType;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.algorithm.AlgorithmConfigurationFacade;
import amuse.scheduler.gui.controller.ClassifierController;
import amuse.data.InputFeatureType;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import amuse.scheduler.gui.training.InputFeaturePanel;
import amuse.scheduler.gui.training.ModelTypePanel;
import amuse.scheduler.gui.training.ProcessingHistoryPanel;
import amuse.scheduler.gui.training.TrainingDescriptionPanel;
import amuse.scheduler.gui.training.TrainingView;
import javax.swing.border.TitledBorder;

/**
 * @author Clemens Waeltken
 *
 */
public class ClassifierView extends JPanel implements HasCaption, NextButtonUsable, HasSaveButton {

	private JPanel viewLeft;
	private JPanel rightSide = new JPanel(new MigLayout("ins 0, fillx"));
	private JSplitPane splitPane = new JSplitPane();
	private ClassificationInputSelectionPanel inputSelectionPanel;
	private ClassificationGroundTruthSelectionPanel groundTruthSelectionPanel;
	private InputFeaturePanel inputFeaturePanel;
	private AlgorithmConfigurationFacade trainingAlgorithmFacade;
	private ModelTypePanel modelTypePanel = new ModelTypePanel();
	private TrainingDescriptionPanel trainingDescriptionPanel = null;
    private JPanel targetPathSelectionPanel = new JPanel(new MigLayout("fillx"));
    private final ClassifierController classifierController;
    private JTextField txtTargetFilePath = new JTextField(30);
//    private TrainingView trainingView;
    private JButton btnSelectFolder = new JButton("Select File");
    private JCheckBox selectAverageCalculation = new JCheckBox("Calculate average");
    private TitledBorder pathSelectionTitle = new TitledBorder("Additional Settings");
    private static final String toolTipCheckboxAverage = "Select to calculate track average category relationship over all classification windows";
    
    
    /**
     * @param classifierController
     * @param trainingAlgorithmModel
     */
    public ClassifierView(ClassifierController classifierController) {
        this.classifierController = classifierController;
        this.trainingAlgorithmFacade = new AlgorithmConfigurationFacade("Classification", new File("config" + File.separator + "classifierAlgorithmTable.arff"));
        this.trainingAlgorithmFacade.setToolTip("Select Algorithm to classify with.");
        this.inputSelectionPanel = new ClassificationInputSelectionPanel();
        this.groundTruthSelectionPanel = new ClassificationGroundTruthSelectionPanel();
        viewLeft = new JPanel(new MigLayout("fillx"));
        viewLeft.setBorder(new TitledBorder("Setup Classification"));
        splitPane.add(new JScrollPane(viewLeft), JSplitPane.LEFT);
        splitPane.add(new JScrollPane(rightSide), JSplitPane.RIGHT);
        this.inputFeaturePanel = new InputFeaturePanel();
        this.inputSelectionPanel = new ClassificationInputSelectionPanel();
        this.groundTruthSelectionPanel = new ClassificationGroundTruthSelectionPanel();
        viewLeft.add(inputSelectionPanel, "growx, span, wrap");
        viewLeft.add(groundTruthSelectionPanel, "growx, span, wrap");
        
        inputSelectionPanel.getInputSourceTypeComboBox().addActionListener(e ->{
        	setChildsEnabled(inputFeaturePanel,
        			groundTruthSelectionPanel.getSelectedGroundTruthSourceType().equals("CATEGORY_ID") || 
					inputSelectionPanel.getSelectedInputSourceType().equals(InputSourceType.FILE_LIST) ||
					inputSelectionPanel.getSelectedInputSourceType().equals(InputSourceType.CATEGORY_ID));
        	inputFeaturePanel.getAttributesToIgnoreTextField().setEnabled(inputSelectionPanel.getSelectedInputSourceType().equals(InputSourceType.FILE_LIST) || inputSelectionPanel.getSelectedInputSourceType().equals(InputSourceType.CATEGORY_ID));
        });
        groundTruthSelectionPanel.getGroundTruthSourceTypeComboBox().addActionListener(e -> {
        	setChildsEnabled(inputFeaturePanel,
        			groundTruthSelectionPanel.getSelectedGroundTruthSourceType().equals("CATEGORY_ID") || 
					inputSelectionPanel.getSelectedInputSourceType().equals(InputSourceType.FILE_LIST) ||
					inputSelectionPanel.getSelectedInputSourceType().equals(InputSourceType.CATEGORY_ID));
		});
        
        viewLeft.add(inputFeaturePanel, "growx, span, wrap");
        addRightSide(modelTypePanel);
        modelTypePanel.addModelTypeListener(trainingAlgorithmFacade);
        addRightSide(trainingAlgorithmFacade.getAlgorithmSelectionComboBox());
        addRightSide(trainingAlgorithmFacade.getParameterPanel());

        targetPathSelectionPanel.add(selectAverageCalculation, "growx, wrap");
        targetPathSelectionPanel.add(new JLabel("Enter Filename for Result:"), "wrap");
        targetPathSelectionPanel.add(txtTargetFilePath, "growx");
        txtTargetFilePath.setText(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + (AmusePreferences.get(KeysStringValue.AMUSE_PATH).endsWith(File.separator) ? "" : File.separator) + "experiments" + 
        		File.separator);
        targetPathSelectionPanel.add(btnSelectFolder, "gap rel");
        targetPathSelectionPanel.setBorder(pathSelectionTitle);
        btnSelectFolder.addActionListener(new SelectFolderListener());
        // Add all elements:
        this.setLayout(new BorderLayout());
        selectAverageCalculation.setToolTipText(toolTipCheckboxAverage);
        selectAverageCalculation.setSelected(true);
        
		trainingDescriptionPanel = new TrainingDescriptionPanel();
		addRightSide(trainingAlgorithmFacade.getParameterPanel());
		addRightSide(targetPathSelectionPanel);
		addRightSide(trainingDescriptionPanel);
		splitPane.setDividerLocation(0.5);
    }

    public void addRightSide(JComponent comp) {
		rightSide.add(comp, "grow, wrap");
		splitPane.setDividerLocation(0.5);
	}

	public String getSaveButtonText() {
        return "Save";
    }

    public void saveButtonClicked() {
        classifierController.saveButtonClicked();
    }
    
    private void setChildsEnabled(Component comp, boolean b) {
		if (comp instanceof JComponent) {
			for (Component c : ((JComponent) comp).getComponents()) {
				c.setEnabled(b);
				setChildsEnabled(c, b);
			}
		}
	}

    private final class SelectFolderListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(txtTargetFilePath.getText());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            if (fileChooser.showSaveDialog(targetPathSelectionPanel) == JFileChooser.APPROVE_OPTION) {
                String absolutePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!absolutePath.endsWith(".arff")) {
                    absolutePath = absolutePath + ".arff";
                }
                txtTargetFilePath.setText(absolutePath);
            }
        }
    }
    private static final long serialVersionUID = -5950319404162079845L;

    /* (non-Javadoc)
     * @see amuse.scheduler.gui.navigation.HasCaption#getCaption()
     */
    @Override
    public String getCaption() {
        return "Classification Configurator";
    }

    /* (non-Javadoc)
     * @see amuse.scheduler.gui.navigation.NextButtonUsable#getNextButtonText()
     */
    @Override
    public String getNextButtonText() {
        return "Finish Configuration";
    }

    /* (non-Javadoc)
     * @see amuse.scheduler.gui.navigation.NextButtonUsable#nextButtonClicked()
     */
    @Override
    public boolean nextButtonClicked() {
        classifierController.addClassification();
        return false;
    }

    /**
     * @return processingModelString
     */
    public String getProcessingModelString() {
    	return inputFeaturePanel.getProcessingHistoryString();
    }

    public void setProcessingModelString(String value) {
    	inputFeaturePanel.setProcessingModelString(value);
    }

    /**
     * @return targetFilePath
     */
    public String getTargetFilePath() {
        return txtTargetFilePath.getText();
    }

    public void setTargetFilePath(String value) {
        txtTargetFilePath.setText(value);
    }

    public boolean isAverageCalculationSelected() {
        return selectAverageCalculation.isSelected();
    }

    public void setAverageCalculationSelected(int intValue) {
        if (intValue == 0) {
            selectAverageCalculation.setSelected(false);
        } else if (intValue == 1) {
            selectAverageCalculation.setSelected(true);
        }
    }

    public void setSelectedTrainingAlgorithm(String value) {
    	trainingAlgorithmFacade.setSelectedAlgorithm(value);
    }

    public String getSelectedTrainingAlgorithmStr() {
    	return trainingAlgorithmFacade.getSelectedAlgorithm().getIdAndParameterStr();
    }

	public int getGroundTruthCategoryId() {
		return groundTruthSelectionPanel.getGroundTruthCategoryId();
	}

	public void setGroundTruthCategoryId(int id) {
		groundTruthSelectionPanel.setCategoryId(id);
	}
	
	public void setGroundTruthSourceType(String type){
		groundTruthSelectionPanel.setGroundTruthSourceType(type);
	}
	
	public List<Integer> getAttributesToIgnore(){
		if(inputSelectionPanel.getSelectedInputSourceType().equals(InputSourceType.READY_INPUT)) {
			return inputSelectionPanel.getAttributesToIgnore();
		} else {
			return inputFeaturePanel.getAttributesToIgnore();
		}
	}
	
	public List<Integer> getAttributesToPredict(){
		return groundTruthSelectionPanel.getAttributesToPredict();
	}
	
	public ModelType getModelType() {
		return modelTypePanel.getModelType();
	}

	public void setAttributesToPredict(List<Integer> attributesToPredict) {
		groundTruthSelectionPanel.setAttributesToPredict(attributesToPredict);
	}

	public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
		if(inputSelectionPanel.getSelectedInputSourceType().equals(InputSourceType.READY_INPUT)) {
			inputSelectionPanel.setAttributesToIgnore(attributesToIgnore);
		} else {
			inputFeaturePanel.setAttributesToIgnore(attributesToIgnore);
		}
	}

	public void setModelType(ModelType modelType) {
		modelTypePanel.setModelType(modelType);
	}

	public InputSourceType getInputSourceType() {
		return inputSelectionPanel.getSelectedInputSourceType();
	}

	public String getTrainingDescription() {
		return trainingDescriptionPanel.getTrainingDescription();
	}

	public void setInputToClassify(DataInputInterface inputToClassify) {
		inputSelectionPanel.setInputToClassify(inputToClassify);
	}

	public void setInputSourceType(InputSourceType inputSourceType) {
		inputSelectionPanel.setInputSourceType(inputSourceType);
	}

	public void setMergeTrackResults(Integer mergeTrackResults) {
		selectAverageCalculation.setSelected(mergeTrackResults == 1);
	}

	public void setOutputResult(String classificationOutput) {
		txtTargetFilePath.setText(classificationOutput);
	}

	public void setTrainingDescription(String trainingDescription) {
		trainingDescriptionPanel.setTrainingDescription(trainingDescription);
	}

	public JComponent getView() {
		return splitPane;
	}

	public DataSetAbstract getInputToClassify() throws IOException {
		return inputSelectionPanel.getInputToClassify();
	}

	public String getPathToInputModel() {
		return groundTruthSelectionPanel.getPath();
	}

	public String getGroundTruthSourceType() {
		return groundTruthSelectionPanel.getSelectedGroundTruthSourceType();
	}

	public void setPathToInputModel(String pathToInputModel) {
		groundTruthSelectionPanel.setPath(pathToInputModel);
	}

	public String getReadyInputPath() {
		return inputSelectionPanel.getReadyInputPath();
	}
	
	public int getCategoryId() {
		return inputSelectionPanel.getCategoryId();
	}

	public InputFeatureType getInputFeatureType() {
		return inputFeaturePanel.getInputFeatureType();
	}

	public Integer getClassificationWindowSize() {
		return inputFeaturePanel.getClassificationWindowSize();
	}

	public Integer getClassificationWindowStepSize() {
		return inputFeaturePanel.getClassificationWindowStepSize();
	}

	public FeatureTable getInputFeatures() {
		return inputFeaturePanel.getInputFeatures();
	}

	public void setInputFeatureType(InputFeatureType inputFeatureType) {
		inputFeaturePanel.setInputFeatureType(inputFeatureType);
	}

	public void setInputFeatures(FeatureTable inputFeatureList) {
		inputFeaturePanel.setInputFeatures(inputFeatureList);
	}

	public void setClassificationWindowSize(Integer classificationWindowSize) {
		inputFeaturePanel.setClassificationWindowSize(classificationWindowSize);
	}

	public void setClassificationWindowStepSize(Integer classificationWindowStepSize) {
		inputFeaturePanel.setClassificationWindowStepSize(classificationWindowStepSize);
	}

	public Unit getUnit() {
		return inputFeaturePanel.getUnit();
	}
	
	public void setUnit(Unit unit) {
		inputFeaturePanel.setUnit(unit);
	}

}
