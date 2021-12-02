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
 * Creation date: 07.08.2009
 */
package amuse.scheduler.gui.validation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import amuse.data.FeatureTable;
import amuse.data.GroundTruthSourceType;
import amuse.data.InputFeatureType;
import amuse.data.ModelType;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.scheduler.gui.algorithm.AlgorithmConfigurationFacade;
import amuse.scheduler.gui.training.TrainingView;
import net.miginfocom.swing.MigLayout;

/**
 * @author Clemens Waeltken
 *
 */
public class ValidationView {
	
	private TrainingView trainingView;
    private static final String ToolTipValidationAlgorithms = "Select Validation Method.";
    private JPanel targetPathSelectionPanel = new JPanel(new MigLayout("fillx"));
    private JButton btnSelectFolder = new JButton("Select File");
    private JTextField txtTargetFilePath = new JTextField(30);
    private TitledBorder pathSelectionTitle = new TitledBorder("Optional Output Path");

	public ValidationView(AlgorithmConfigurationFacade validationAlgorithms) {
		this.trainingView = new TrainingView("Setup Validation", false);
        validationAlgorithms.setToolTip(ToolTipValidationAlgorithms);
		trainingView.addRightSide(validationAlgorithms.getParameterPanel());
		trainingView.addLineInView(validationAlgorithms.getAlgorithmSelectionComboBox());
		
		targetPathSelectionPanel.add(new JLabel("Enter Filename for Results:"), "wrap");
        targetPathSelectionPanel.add(txtTargetFilePath, "growx");
        txtTargetFilePath.setText("");
        targetPathSelectionPanel.add(btnSelectFolder, "gap rel");
        targetPathSelectionPanel.setBorder(pathSelectionTitle);
        btnSelectFolder.addActionListener(new SelectFolderListener());
        trainingView.addRightSide(targetPathSelectionPanel);
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
	
	public JComponent getView() {
		return trainingView.getView();
	}
	
	/**
	 * @return
	 */
	public String getProcessingModelString() {
		return trainingView.getProcessingModelString();
	}
	
	/**
	 * Sets the text for the processing steps description
	 * @param processingSteps - The String that should be put in the textbox
	 */
	public void setProcessingModelString(String processingSteps){
		trainingView.setProcessingModelString(processingSteps);
	}
	
	/**
	 * @return
	 */
    public String getClassifierAlgorithmStr() {
        return trainingView.getSelectedTrainingAlgorithmStr();
    }

    public void re(int value) {
        //trainingView.setSelectedCategoryID(value);
    }

    public void setClassifierAlgorithm(String string) {
        trainingView.setSelectedTrainingAlgorithm(string);
    }
    
    public void setGroundTruthSourceType(GroundTruthSourceType type){
    	trainingView.setGroundTruthSourceType(type);
	}
	
	public GroundTruthSourceType getGroundTruthSourceType(){
		return trainingView.getGroundTruthSourceType();
	}

	public String getOuputPath() {
		return txtTargetFilePath.getText();
	}
	
	public String getGroundTruthSource(){
		return trainingView.getGroundTruthSource();
	}

	public void setGroundTruthSource(String groundTruthSource) {
		trainingView.setGroundTruthSource(groundTruthSource);
	}
	
	public List<Integer> getAttributesToPredict(){
		return trainingView.getAttributesToPredict();
	}
	
	public List<Integer> getAttributesToIgnore(){
		return trainingView.getAttributesToIgnore();
	}
	
	public void setAttributesToPredict(List<Integer> attributesToPredict) {
		trainingView.setAttributesToPredict(attributesToPredict);
	}
	
	public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
		trainingView.setAttributesToIgnore(attributesToIgnore);
	}
	
	public ModelType getModelType() {
		return trainingView.getModelType();
	}

	public void setModelType(ModelType modelType) {
		trainingView.setModelType(modelType);
	}

	public void setOutputPath(String outputPath) {
		txtTargetFilePath.setText(outputPath);
	}

	public InputFeatureType getInputFeatureType() {
		return trainingView.getInputFeatureType();
	}

	public Integer getClassificationWindowSize() {
		return trainingView.getClassificaitonWindowSize();
	}

	public Integer getClassificationWindowStepSize() {
		return trainingView.getClassificationWindowStepSize();
	}

	public FeatureTable getInputFeatures() {
		return trainingView.getInputFeatures();
	}

	public void setInputFeatureType(InputFeatureType inputFeatureType) {
		trainingView.setInputFeatureType(inputFeatureType);
	}

	public void setInputFeatures(FeatureTable inputFeatureList) {
		trainingView.setInputFeatures(inputFeatureList);
	}

	public void setClassificationWindowSize(Integer classificationWindowSize) {
		trainingView.setClassificationWindowSize(classificationWindowSize);
	}

	public void setClassificationWindowStepSize(Integer classificationWindowStepSize) {
		trainingView.setClassificationWindowStepSize(classificationWindowStepSize);
	}

	public Unit getUnit() {
		return trainingView.getUnit();
	}
	
	public void setUnit(Unit unit) {
		trainingView.setUnit(unit);
	}
}
