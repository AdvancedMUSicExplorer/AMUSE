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

import amuse.scheduler.gui.algorithm.Algorithm;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import amuse.scheduler.gui.algorithm.AlgorithmConfigurationFacade;
import java.io.File;
import javax.swing.JScrollPane;

/**
 * @author Clemens Waeltken
 *
 */
public class TrainingView {

    private JPanel viewLeft;
    private JPanel rightSide = new JPanel(new MigLayout("ins 0, fillx"));
    private JSplitPane splitPane = new JSplitPane();
    private CategorySelectionPanel categorySelectionPanel;
    private ProcessingHistoryPanel processingHistoryPanel;
    private AlgorithmConfigurationFacade trainingAlgorithmFacade;
    private AlgorithmConfigurationFacade preprocessingAlgorithmFacade = null;
    private static final String trainingViewName = "Setup Training";
    private static final String ToolTipSelectTrainingAlgorithm = "Select Algorithm to train with.";

    public TrainingView() {
	this(trainingViewName);
    }

    public TrainingView(String leftTitle) {
	this.trainingAlgorithmFacade = new AlgorithmConfigurationFacade("Training", new File("config/classifierAlgorithmTable.arff"));
	trainingAlgorithmFacade.setToolTip(ToolTipSelectTrainingAlgorithm);
	this.categorySelectionPanel = new CategorySelectionPanel();
	viewLeft = new JPanel(new MigLayout("fillx"));
	viewLeft.setBorder(new TitledBorder(leftTitle));
	splitPane.add(new JScrollPane(viewLeft), JSplitPane.LEFT);
	splitPane.add(new JScrollPane(rightSide), JSplitPane.RIGHT);
	this.processingHistoryPanel = new ProcessingHistoryPanel();
	viewLeft.add(categorySelectionPanel, "growx, span, wrap");
	if (leftTitle.equals(trainingViewName)) {
	    preprocessingAlgorithmFacade = new AlgorithmConfigurationFacade("Preprocessing", new File("config/classifierPreprocessingAlgorithmTable.arff"));
	    preprocessingAlgorithmFacade.setUseEnableButton(true);
            preprocessingAlgorithmFacade.setSelectedAlgorithm("-1");
	    viewLeft.add(preprocessingAlgorithmFacade.getAlgorithmSelectionComboBox(), "growx, span, wrap");
	    addRightSide(preprocessingAlgorithmFacade.getPrameterPanel());
	}
	viewLeft.add(trainingAlgorithmFacade.getAlgorithmSelectionComboBox(), "growx, span, wrap");
	viewLeft.add(processingHistoryPanel, "growx, span, wrap");
	addRightSide(trainingAlgorithmFacade.getPrameterPanel());
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

    public int getSelectedCategoryID() {
	return categorySelectionPanel.getSelectedCategoryID();
    }

    public void setProcessingModelString(String value) {
	processingHistoryPanel.setProcessingModelString(value);
    }

    public void setPreprocessingAlgorithm(String value) {
	preprocessingAlgorithmFacade.setSelectedAlgorithm(value);
    }

    public void setSelectedCategoryID(int value) {
	categorySelectionPanel.setSelectedCategory(value);
    }

    public void setSelectedTrainingAlgorithm(String value) {
        trainingAlgorithmFacade.setSelectedAlgorithm(value);
    }

    public String getSelectedTrainingAlgorithmStr() {
        return trainingAlgorithmFacade.getSelectedAlgorithm().getIdAndParameterStr();
    }
}
