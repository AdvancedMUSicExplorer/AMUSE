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

package amuse.scheduler.gui.controller;

import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetException;
import amuse.data.datasets.OptimizerConfigSet;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import amuse.scheduler.gui.views.OptimizerView;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author Clemens Wältken
 */
public class OptimizationController extends AbstractController {

    private OptimizerView oView;
    private WizardController wc;

    OptimizationController(WizardController instance) {
        oView = new OptimizerView(this);
        view = new OptimizerPanel(oView.getView());
        wc = instance;
    }

    @Override
    public void saveTask(File file) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void loadTask(DataSetAbstract dataSet) {
        try {
            OptimizerConfigSet ttSet = new OptimizerConfigSet(dataSet);
            setOptimizerConfiguration(ttSet);
        } catch (DataSetException e) {
            showErr(e.getLocalizedMessage());
        }
    }

    @Override
    public void loadTask(TaskConfiguration conf) {
        if (conf instanceof OptimizationConfiguration) {
            OptimizationConfiguration optiConf = (OptimizationConfiguration) conf;
            oView.setTrainingAlgorithmDescriptor(optiConf.getAlgorithmDescription());
            oView.setLearningCategory(new Integer(optiConf.getTrainingInput()));
            oView.setOptimizingCategory(new Integer(optiConf.getOptimizationInput()));
            oView.setTestCategory(new Integer(optiConf.getTestInput()));
            oView.setOutputDescriptor(optiConf.getDestinationFolder());
        }
    }

    @Override
    public TaskConfiguration getExperimentConfiguration() {
        String catLearningID = oView.getLearningCategory() + "";
        String catOptimizationID = oView.getOptimizingCategory() + "";
        String catTestID = oView.getTestCategory() + "";
        String algorithmDescriptor = oView.getTrainingAlgorithmDescriptor();
        String targetDescriptor = oView.getOutputDescriptor();
        OptimizationConfiguration oConfig = new OptimizationConfiguration(catLearningID, catOptimizationID, catTestID, algorithmDescriptor, "-1", targetDescriptor);
        return oConfig;
    }

    @Override
    public JComponent getView() {
	return view;
    }

    private void addOptimization() {
        taskManager.addExperiment(getExperimentConfiguration());
    }

    private void setOptimizerConfiguration(OptimizerConfigSet ttSet) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class OptimizerPanel extends JPanel implements HasCaption, NextButtonUsable {

        public OptimizerPanel(JComponent panel) {
            super(new BorderLayout());
            super.add(panel, BorderLayout.CENTER);
        }
        
        public String getCaption() {
            return "Optimization Configurator";
        }

        public boolean nextButtonClicked() {
            addOptimization();
            return false;
        }

        public String getNextButtonText() {
            return "Finish";
        }

    }

}
