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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import amuse.data.ClassificationType;
import amuse.data.GroundTruthSourceType;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.controller.ClassifierController;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import amuse.scheduler.gui.training.TrainingView;
import javax.swing.border.TitledBorder;

/**
 * @author Clemens Waeltken
 *
 */
public class ClassifierView extends JPanel implements HasCaption, NextButtonUsable, HasSaveButton {

    private JPanel targetPathSelectionPanel = new JPanel(new MigLayout("fillx"));
    private final ClassifierController classifierController;
    private JTextField txtTargetFilePath = new JTextField(30);
    private TrainingView trainingView;
    private JButton btnSelectFolder = new JButton("Select File");
    private JCheckBox selectAverageCalculation = new JCheckBox("Calculate average");
    private TitledBorder pathSelectionTitle = new TitledBorder("Additional Settings");
    private static final String toolTipCheckboxAverage = "Select to calculate song average category relationship over all song partitions";

    /**
     * @param classifierController
     * @param trainingAlgorithmModel
     */
    public ClassifierView(ClassifierController classifierController) {
        this.classifierController = classifierController;
        targetPathSelectionPanel.add(selectAverageCalculation, "growx, wrap");
        targetPathSelectionPanel.add(new JLabel("Enter Filename for Result:"), "wrap");
        targetPathSelectionPanel.add(txtTargetFilePath, "growx");
        txtTargetFilePath.setText(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "experiments" + 
        		File.separator);
        targetPathSelectionPanel.add(btnSelectFolder, "gap rel");
        targetPathSelectionPanel.setBorder(pathSelectionTitle);
        btnSelectFolder.addActionListener(new SelectFolderListener());
        // Add all elements:
        this.setLayout(new BorderLayout());
        trainingView = new TrainingView("Setup Classification Model", false);
        selectAverageCalculation.setToolTipText(toolTipCheckboxAverage);
        selectAverageCalculation.setSelected(true);
        trainingView.addLineInView(targetPathSelectionPanel);
        this.add(trainingView.getView(), BorderLayout.CENTER);
    }

    public String getSaveButtonText() {
        return "Save";
    }

    public void saveButtonClicked() {
        classifierController.saveButtonClicked();
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
     * @return
     */
    public String getProcessingModelStr() {
        return trainingView.getProcessingModelString();
    }

    public void setProcessingModelStr(String value) {
        trainingView.setProcessingModelString(value);
    }

    /**
     * @return
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
        trainingView.setSelectedTrainingAlgorithm(value);
    }

    public String getSelectedTrainingAlgorithmStr() {
        return trainingView.getSelectedTrainingAlgorithmStr();
    }

	public String getGroundTruthSource() {
		return trainingView.getGroundTruthSource();
	}

	public GroundTruthSourceType getGroundTruthSourceType() {
		return trainingView.getGroundTruthSourceType();
	}

	public void setGroundTruthSource(String groundTruthSource) {
		trainingView.setGroundTruthSource(groundTruthSource);
	}
	
	public void setGroundTruthSourceType(GroundTruthSourceType type){
		trainingView.setGroundTruthSourceType(type);
	}
	
	public List<Integer> getAttributesToIgnore(){
		return trainingView.getAttributesToIgnore();
	}
	
	public List<Integer> getAttributesToClassify(){
		return trainingView.getAttributesToClassify();
	}
	
	public ClassificationType getClassificationType() {
		return trainingView.getClassificationType();
	}
	
	public boolean isFuzzy() {
		return trainingView.isFuzzy();
	}

	public void setAttributesToClassify(List<Integer> attributesToClassify) {
		trainingView.setAttributesToClassify(attributesToClassify);
	}

	public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
		trainingView.setAttributesToIgnore(attributesToIgnore);
	}

	public void setClassificationType(ClassificationType classificationType) {
		trainingView.setClassificationType(classificationType);
	}

	public void setFuzzy(boolean fuzzy) {
		trainingView.setFuzzy(fuzzy);
	}
}
