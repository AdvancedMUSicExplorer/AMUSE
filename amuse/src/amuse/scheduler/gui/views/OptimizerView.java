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
 * Creation date: 15.03.2010
 */

package amuse.scheduler.gui.views;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.algorithm.AlgorithmConfigurationFacade;
import amuse.scheduler.gui.training.CategorySelectionPanel;
import amuse.scheduler.gui.controller.OptimizationController;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Clemens Wältken
 */
public class OptimizerView {
    private CategorySelectionPanel categoryPanelLearning;
    private CategorySelectionPanel categoryPanelOptimizing;
    private CategorySelectionPanel categoryPanelTest;
    private AlgorithmConfigurationFacade optimizierAlgorithm;
    private OptimizationController controller;
    private JTextField txtOutputDescriptor = new JTextField();
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel outputPanel;
    private JSplitPane panel = new JSplitPane();

    public OptimizerView(OptimizationController ctrl) {
	controller = ctrl;
	leftPanel = new JPanel(new MigLayout("fillx"));
        rightPanel = new JPanel(new MigLayout("fillx"));
        panel.add(leftPanel, JSplitPane.LEFT);
        panel.add(rightPanel, JSplitPane.RIGHT);
	categoryPanelLearning = new CategorySelectionPanel(false);
	categoryPanelOptimizing = new CategorySelectionPanel(false);
	categoryPanelTest = new CategorySelectionPanel(false);
        categoryPanelTest.setOptional(true);
        optimizierAlgorithm = new AlgorithmConfigurationFacade("Optimizer Algorithm", new File(AmusePreferences.getOptimizerAlgorithmTablePath()));
	outputPanel = new JPanel(new MigLayout("fillx"));
	outputPanel.setBorder(new TitledBorder("Set Output Descriptor"));
        outputPanel.add(txtOutputDescriptor, "growx, wrap");
        categoryPanelLearning.setBorder(new TitledBorder("Learning Category"));
        categoryPanelOptimizing.setBorder(new TitledBorder("Optimizing Category"));
        categoryPanelTest.setBorder(new TitledBorder("Test Category"));
	leftPanel.add(categoryPanelLearning, "growx, wrap");
	leftPanel.add(categoryPanelOptimizing, "growx, wrap");
	leftPanel.add(categoryPanelTest, "growx, wrap");
        leftPanel.add(optimizierAlgorithm.getAlgorithmSelectionComboBox(), "growx, wrap");
	leftPanel.add(outputPanel, "growx, wrap");
        rightPanel.add(optimizierAlgorithm.getParameterPanel(), "growx, wrap");
    }

    public JComponent getView() {
	return this.panel;
    }

    public int getLearningCategory() {
        return categoryPanelLearning.getSelectedCategoryID();
    }


    public int getOptimizingCategory() {
        return categoryPanelOptimizing.getSelectedCategoryID();
    }

    public int getTestCategory() {
        return categoryPanelTest.getSelectedCategoryID();
    }

    public String getTrainingAlgorithmDescriptor() {
        return optimizierAlgorithm.getSelectedAlgorithm().getIdAndParameterStr();
    }

    public String getOutputDescriptor() {
        return txtOutputDescriptor.getText();
    }

    public void setTrainingAlgorithmDescriptor(String algorithmDescription) {
        optimizierAlgorithm.setSelectedAlgorithm(algorithmDescription);
    }

    public void setLearningCategory(int id) {
        categoryPanelLearning.setCategory(id);
    }

    public void setOptimizingCategory(int id) {
        categoryPanelOptimizing.setCategory(id);
    }

    public void setTestCategory(int id) {
        categoryPanelTest.setCategory(id);
    }

    public void setOutputDescriptor(String folder) {
        txtOutputDescriptor.setText(folder);
    }
}
