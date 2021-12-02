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
 * Creation date: 28.06.2009
 */
package amuse.scheduler.gui.training;

import amuse.data.FeatureTable;
import amuse.data.GroundTruthSourceType;
import amuse.data.InputFeatureType;
import amuse.data.ModelType;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.algorithm.Algorithm;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import amuse.scheduler.gui.algorithm.AlgorithmConfigurationFacade;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JScrollPane;

/**
 * @author Clemens Waeltken
 *
 */
public class TrainingView extends JPanel {

	private static final long serialVersionUID = 6467742990267146553L;
	private JPanel viewLeft;
	private JPanel rightSide = new JPanel(new MigLayout("ins 0, fillx"));
	private JSplitPane splitPane = new JSplitPane();
	private GroundTruthSelectionPanel groundTruthSelectionPanel;
	private InputFeaturePanel inputFeaturePanel;
	private AlgorithmConfigurationFacade trainingAlgorithmFacade;
	private AlgorithmConfigurationFacade preprocessingAlgorithmFacade = null;
	private ModelTypePanel modelTypePanel = new ModelTypePanel();
	private TrainingDescriptionPanel trainingDescriptionPanel = null;
	private static final String trainingViewName = "Setup Training";
	private static final String ToolTipSelectTrainingAlgorithm = "Select Algorithm to train with.";
	private JPanel targetPathSelectionPanel = new JPanel(new MigLayout("fillx"));
    private JButton btnSelectFolder = new JButton("Select File");
    private JTextField txtTargetFilePath = new JTextField(30);
    private TitledBorder pathSelectionTitle = new TitledBorder("Optional Output Path");

	public TrainingView(boolean training) {
		this(trainingViewName, training);
	}

	public TrainingView(String leftTitle, boolean training) {
		this.trainingAlgorithmFacade = new AlgorithmConfigurationFacade("Training", new File("config" + File.separator + "classifierAlgorithmTable.arff"));
		trainingAlgorithmFacade.setToolTip(ToolTipSelectTrainingAlgorithm);
		this.groundTruthSelectionPanel = new GroundTruthSelectionPanel();
		viewLeft = new JPanel(new MigLayout("fillx"));
		viewLeft.setBorder(new TitledBorder(leftTitle));
		splitPane.add(new JScrollPane(viewLeft), JSplitPane.LEFT);
		splitPane.add(new JScrollPane(rightSide), JSplitPane.RIGHT);
		this.inputFeaturePanel = new InputFeaturePanel();
		viewLeft.add(groundTruthSelectionPanel, "growx, span, wrap");
		groundTruthSelectionPanel.getGroundTruthSourceTypeComboBox().addActionListener(e -> {
			setChildsEnabled(inputFeaturePanel, !groundTruthSelectionPanel.getSelectedGroundTruthSourceType().equals(GroundTruthSourceType.READY_INPUT));
		});
		if (leftTitle.equals(trainingViewName)) {
			preprocessingAlgorithmFacade = new AlgorithmConfigurationFacade("Preprocessing", new File("config" + File.separator + "classifierPreprocessingAlgorithmTable.arff"));
			preprocessingAlgorithmFacade.setUseEnableButton(true);
			preprocessingAlgorithmFacade.setSelectedAlgorithm("-1");
			viewLeft.add(preprocessingAlgorithmFacade.getAlgorithmSelectionComboBox(), "growx, span, wrap");
			addRightSide(preprocessingAlgorithmFacade.getParameterPanel());
		}
		viewLeft.add(inputFeaturePanel, "growx, span, wrap");
		if(training) {
			trainingDescriptionPanel = new TrainingDescriptionPanel();
			viewLeft.add(trainingDescriptionPanel, "growx, span, wrap");
		}
		addRightSide(modelTypePanel);
		modelTypePanel.addModelTypeListener(trainingAlgorithmFacade);
		addRightSide(trainingAlgorithmFacade.getAlgorithmSelectionComboBox());
		addRightSide(trainingAlgorithmFacade.getParameterPanel());
		
		if(training) {
			targetPathSelectionPanel.add(new JLabel("Enter Filename for Output Model:"), "wrap");
	        targetPathSelectionPanel.add(txtTargetFilePath, "growx");
	        txtTargetFilePath.setText("");
	        targetPathSelectionPanel.add(btnSelectFolder, "gap rel");
	        targetPathSelectionPanel.setBorder(pathSelectionTitle);
	        btnSelectFolder.addActionListener(new SelectFolderListener());
	        addRightSide(targetPathSelectionPanel);
        }
		
		splitPane.setDividerLocation(0.5);
	}

	public JComponent getView() {
		return splitPane;
	}

	public void addLineInView(JComponent line) {
		viewLeft.add(line, "growx, spanx, wrap");
		splitPane.setDividerLocation(0.5);
	}

	/**
	 * @return processingModelString
	 */
	public String getProcessingModelString() {
		return inputFeaturePanel.getProcessingHistoryString();
	}

	/**
	 * @param comp
	 */
	public void addRightSide(JComponent comp) {
		rightSide.add(comp, "grow, wrap");
		splitPane.setDividerLocation(0.5);
	}

	public String getPreprocessingAlgorithmStr() {
		if (preprocessingAlgorithmFacade == null || !preprocessingAlgorithmFacade.isEnabled()) {
			return "-1";
		} else {
			Algorithm algo = preprocessingAlgorithmFacade.getSelectedAlgorithm();
			String algorithmStr = algo.getID() + "";
			if (algo.getCurrentParameterValues().length > 0) {
				algorithmStr = algorithmStr + "[";
				for (String parameter : algo.getCurrentParameterValues()) {
					algorithmStr = algorithmStr + parameter + "|";
				}
				algorithmStr = algorithmStr.substring(0, algorithmStr.lastIndexOf('|')) + "]";
			}
			return algorithmStr;
		}
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
                if (!absolutePath.endsWith(".mod")) {
                    absolutePath = absolutePath + ".mod";
                }
                txtTargetFilePath.setText(absolutePath);
            }
        }
    }
	
	public void setGroundTruthSourceType(GroundTruthSourceType type){
		groundTruthSelectionPanel.setGroundTruthSourceType(type);
	}
	
	public GroundTruthSourceType getGroundTruthSourceType(){
		return groundTruthSelectionPanel.getSelectedGroundTruthSourceType();
	}
	
	public List<Integer> getAttributesToPredict(){
		return groundTruthSelectionPanel.getAttributesToPredict();
	}
	
	public void setAttributesToPredict(List<Integer> attributesToPredict) {
		groundTruthSelectionPanel.setAttributesToPredict(attributesToPredict);
	}
	
	public List<Integer> getAttributesToIgnore(){
		if(groundTruthSelectionPanel.getSelectedGroundTruthSourceType().equals(GroundTruthSourceType.CATEGORY_ID)) {
			return inputFeaturePanel.getAttributesToIgnore();
		} else {
			return groundTruthSelectionPanel.getAttributesToIgnore();
		}
	}
	
	public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
		if(groundTruthSelectionPanel.getSelectedGroundTruthSourceType().equals(GroundTruthSourceType.CATEGORY_ID)) {
			inputFeaturePanel.setAttributesToIgnore(attributesToIgnore);
		} else {
			groundTruthSelectionPanel.setAttributesToIgnore(attributesToIgnore);
		}
	}
	
	public String getTrainingDescription() {
		return trainingDescriptionPanel != null ? trainingDescriptionPanel.getTrainingDescription() : "";
	}
	
	public String getGroundTruthSource(){
		return groundTruthSelectionPanel.getSelectedGroundTruthSource();
	}
		
	public void setProcessingModelString(String value) {
		inputFeaturePanel.setProcessingModelString(value);
	}

	public void setPreprocessingAlgorithm(String value) {
		preprocessingAlgorithmFacade.setSelectedAlgorithm(value);
	}

	public void setSelectedTrainingAlgorithm(String value) {
		trainingAlgorithmFacade.setSelectedAlgorithm(value);
	}

	public String getSelectedTrainingAlgorithmStr() {
		return trainingAlgorithmFacade.getSelectedAlgorithm().getIdAndParameterStr();
	}

	public void setGroundTruthSource(String groundTruthSource) {
		groundTruthSelectionPanel.setGroundTruthSource(groundTruthSource);
	}

	public void setTrainingDescription(String trainingDescription) {
		trainingDescriptionPanel.setTrainingDescription(trainingDescription);
	}

	public void setModelType(ModelType modelType) {
		modelTypePanel.setModelType(modelType);
	}

	public ModelType getModelType() {
		return modelTypePanel.getModelType();
	}

	public String getPathToOutputModel() {
		return txtTargetFilePath.getText();
	}

	public void setPathToOutputModel(String pathToOutputModel) {
		txtTargetFilePath.setText(pathToOutputModel);
	}

	public FeatureTable getInputFeatures() {
		return inputFeaturePanel.getInputFeatures();
	}

	public Integer getClassificaitonWindowSize() {
		return inputFeaturePanel.getClassificationWindowSize();
	}
	
	public Integer getClassificationWindowStepSize() {
		return inputFeaturePanel.getClassificationWindowStepSize();
	}

	public InputFeatureType getInputFeatureType() {
		return inputFeaturePanel.getInputFeatureType();
	}

	public void setInputFeatureType(InputFeatureType inputFeatureType) {
		inputFeaturePanel.setInputFeatureType(inputFeatureType);
	}

	public void setInputFeatures(FeatureTable inputFeatures) {
		inputFeaturePanel.setInputFeatures(inputFeatures);
	}

	public void setClassificationWindowSize(Integer size) {
		inputFeaturePanel.setClassificationWindowSize(size);
	}

	public void setClassificationWindowStepSize(Integer overlap) {
		inputFeaturePanel.setClassificationWindowStepSize(overlap);
	}

	public Unit getUnit() {
		return inputFeaturePanel.getUnit();
	}
	
	public void setUnit(Unit unit) {
		inputFeaturePanel.setUnit(unit);
	}

}
