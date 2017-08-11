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
 * Creation date: 01.07.2009
 */
package amuse.scheduler.gui.controller;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetException;
import amuse.data.io.FileInput;
import amuse.data.datasets.TrainingConfigSet;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import amuse.scheduler.gui.training.TrainingView;

/**
 * @author Clemens Waeltken
 * 
 */
public class TrainingController extends AbstractController {

    WizardController wizardController;
    TrainingView trainingView;
    File ctFolder = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH)
                + File.separator + "experiments" + File.separator + "CT");

    /**
     * @param wizardController
     */
    public TrainingController(WizardController wizardController) {
        this.wizardController = wizardController;
        this.trainingView = new TrainingView();
        view = new TrainingPanel(trainingView.getView());

    }

    /**
     * @return
     */
    public JComponent getView() {
        return view;
    }

    @Override
    public void saveTask(File file) {
        file = addArff(file);
        if (file.exists()) {
            if (!askOverwrite(file)) {
                return;
            }
        }
        try {
            new TrainingConfigSet(getTrainingConfiguration()).saveToArffFile(file);
        } catch (IOException ex) {
            showErr(ex.getLocalizedMessage());
        }
        showMsg("Task successfully saved!");
    }

    @Override
    public void loadTask(DataSetAbstract dataSet) {
        try {
            TrainingConfigSet ttSet = new TrainingConfigSet(dataSet);
            setTrainingConfiguration(ttSet);
        } catch (DataSetException e) {
            showErr(e.getLocalizedMessage());
        }
    }

    @Override
    public TrainingConfiguration getExperimentConfiguration() {
        return getTrainingConfiguration();
    }

    private void setTrainingConfiguration(TrainingConfigSet ttSet) {
        trainingView.setSelectedTrainingAlgorithm(ttSet.getAlgorithmIdAttribute().getValueAt(0));
        trainingView.setProcessingModelString(ttSet.getProcessedFeatureDescriptionAttribute().getValueAt(0));
        trainingView.setPreprocessingAlgorithm(ttSet.getPreprocessingAlgorithmIdAttribute().getValueAt(0));
        trainingView.setSelectedCategoryID(new Integer(ttSet.getGroundTruthSourceAttribute().getValueAt(0)));
    }

    private class TrainingPanel extends JPanel implements NextButtonUsable,
            HasCaption, HasSaveButton, HasLoadButton {

        /**
         *
         */
        private static final long serialVersionUID = -8208226717664963513L;

        /**
         * @param jComponent
         */
        public TrainingPanel(JComponent jComponent) {
            super(new BorderLayout());
            this.add(jComponent, BorderLayout.CENTER);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * amuse.scheduler.gui.navigation.NextButtonUsable#getNextButtonText()
         */
        @Override
        public String getNextButtonText() {
            return "Finish Configuration";
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * amuse.scheduler.gui.navigation.NextButtonUsable#nextButtonClicked()
         */
        @Override
        public boolean nextButtonClicked() {
            addTraining();
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see amuse.scheduler.gui.navigation.HasCaption#getCaption()
         */
        @Override
        public String getCaption() {
            return "Create Training Experiment";
        }

        public void addTraining() {
            taskManager.addExperiment(getExperimentConfiguration());
        }

        @Override
        public String getSaveButtonText() {
            return "Save";
        }

        @Override
        public void saveButtonClicked() {
            saveButtonClick();
        }

        @Override
        public String getLoadButtonText() {
            return "Load";
        }

        @Override
        public void loadButtonClicked() {
            ctFolder.mkdirs();
            JFileChooser fc = new SelectArffFileChooser(
                    "Feature Extraction Task", ctFolder);
            if (fc.showOpenDialog(view) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selectedFile = fc.getSelectedFile();
            try {
                loadTask(selectedFile);
            } catch (IOException ex) {
                showErr(ex.getLocalizedMessage());
            }
        }
    }

    private void saveButtonClick() {
        ctFolder.mkdirs();
        JFileChooser fc = new SelectArffFileChooser(
                "Classification Training Task", ctFolder);
        if (fc.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fc.getSelectedFile();
        saveTask(selectedFile);
    }

    private TrainingConfiguration getTrainingConfiguration() {
        return new TrainingConfiguration(trainingView.getProcessingModelString(), trainingView.getSelectedTrainingAlgorithmStr(), trainingView.getPreprocessingAlgorithmStr(), 
        		new FileInput(trainingView.getSelectedCategoryID() + ""), TrainingConfiguration.GroundTruthSourceType.CATEGORY_ID);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * amuse.scheduler.gui.controller.AbstractController#loadTask(amuse.interfaces
     * .nodes.TaskConfiguration)
     */
    @Override
    public void loadTask(TaskConfiguration conf) {
        if (conf instanceof TrainingConfiguration) {
            TrainingConfiguration trainConf = (TrainingConfiguration) conf;
            trainingView.setSelectedTrainingAlgorithm(trainConf.getAlgorithmDescription());
            trainingView.setProcessingModelString(trainConf.getProcessedFeaturesModelName());
            trainingView.setPreprocessingAlgorithm(trainConf.getPreprocessingAlgorithmDescription());
            trainingView.setSelectedCategoryID(new Integer(trainConf.getGroundTruthSource().toString()));
        }
    }
}
