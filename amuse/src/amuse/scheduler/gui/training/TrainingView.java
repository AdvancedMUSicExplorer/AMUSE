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

import amuse.data.ClassificationType;
import amuse.data.GroundTruthSourceType;
import amuse.scheduler.gui.algorithm.Algorithm;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import amuse.scheduler.gui.algorithm.AlgorithmConfigurationFacade;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JScrollPane;

/**
 * @author Clemens Waeltken
 *
 */
public class TrainingView {

	private JPanel viewLeft;
	private JPanel rightSide = new JPanel(new MigLayout("ins 0, fillx"));
	private JSplitPane splitPane = new JSplitPane();
	private GroundTruthSelectionPanel groundTruthSelectionPanel;
	private ProcessingHistoryPanel processingHistoryPanel;
	private AlgorithmConfigurationFacade trainingAlgorithmFacade;
	private AlgorithmConfigurationFacade preprocessingAlgorithmFacade = null;
	private ClassificationTypePanel classificationTypePanel = new ClassificationTypePanel();
	private TrainingDescriptionPanel trainingDescriptionPanel = null;
	private static final String trainingViewName = "Setup Training";
	private static final String ToolTipSelectTrainingAlgorithm = "Select Algorithm to train with.";

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
		this.processingHistoryPanel = new ProcessingHistoryPanel();
		viewLeft.add(groundTruthSelectionPanel, "growx, span, wrap");
		groundTruthSelectionPanel.getGroundTruthSourceTypeComboBox().addActionListener(e -> {
			setChildsEnabled(processingHistoryPanel, groundTruthSelectionPanel.getSelectedGroundTruthSourceType().equals(GroundTruthSourceType.CATEGORY_ID));
		});
		if (leftTitle.equals(trainingViewName)) {
			preprocessingAlgorithmFacade = new AlgorithmConfigurationFacade("Preprocessing", new File("config" + File.separator + "classifierPreprocessingAlgorithmTable.arff"));
			preprocessingAlgorithmFacade.setUseEnableButton(true);
			preprocessingAlgorithmFacade.setSelectedAlgorithm("-1");
			viewLeft.add(preprocessingAlgorithmFacade.getAlgorithmSelectionComboBox(), "growx, span, wrap");
			addRightSide(preprocessingAlgorithmFacade.getPrameterPanel());
		}
		viewLeft.add(trainingAlgorithmFacade.getAlgorithmSelectionComboBox(), "growx, span, wrap");
		viewLeft.add(processingHistoryPanel, "growx, span, wrap");
		if(training) {
			trainingDescriptionPanel = new TrainingDescriptionPanel();
			viewLeft.add(trainingDescriptionPanel, "growx, span, wrap");
		}
		addRightSide(trainingAlgorithmFacade.getPrameterPanel());
		addRightSide(classificationTypePanel);
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
	 * @return
	 */
	public String getProcessingModelString() {
		return processingHistoryPanel.getProcessingHistoryString();
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
	
	public void setGroundTruthSourceType(GroundTruthSourceType type){
		groundTruthSelectionPanel.setGroundTruthSourceType(type);
	}
	
	public GroundTruthSourceType getGroundTruthSourceType(){
		return groundTruthSelectionPanel.getSelectedGroundTruthSourceType();
	}
	
	public List<Integer> getAttributesToClassify(){
		return groundTruthSelectionPanel.getAttributesToClassify();
	}
	
	public void setAttributesToClassify(List<Integer> attributesToClassify) {
		groundTruthSelectionPanel.setAttributesToClassify(attributesToClassify);
	}
	
	public List<Integer> getAttributesToIgnore(){
		if(groundTruthSelectionPanel.getSelectedGroundTruthSourceType().equals(GroundTruthSourceType.CATEGORY_ID)) {
			return processingHistoryPanel.getAttributesToIgnore();
		} else {
			return groundTruthSelectionPanel.getAttributesToIgnore();
		}
	}
	
	public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
		if(groundTruthSelectionPanel.getSelectedGroundTruthSourceType().equals(GroundTruthSourceType.CATEGORY_ID)) {
			processingHistoryPanel.setAttributesToIgnore(attributesToIgnore);
		} else {
			groundTruthSelectionPanel.setAttributesToIgnore(attributesToIgnore);
		}
	}
	
	public ClassificationType getClassificationType() {
		return classificationTypePanel.getClassificationType();
	}
	
	public void setClassificationType(ClassificationType classificationType) {
		classificationTypePanel.setClassificationType(classificationType);
	}
	
	public boolean isFuzzy() {
		return classificationTypePanel.isFuzzy();
	}
	
	public void setFuzzy(boolean fuzzy) {
		classificationTypePanel.setFuzzy(fuzzy);
	}
	
	public String getTrainingDescription() {
		return trainingDescriptionPanel != null ? trainingDescriptionPanel.getTrainingDescription() : "";
	}
	
	public String getGroundTruthSource(){
		return groundTruthSelectionPanel.getSelectedGroundTruthSource();
	}
		
	public void setProcessingModelString(String value) {
		processingHistoryPanel.setProcessingModelString(value);
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

}
