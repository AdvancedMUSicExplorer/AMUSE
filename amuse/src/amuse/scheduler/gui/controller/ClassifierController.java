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
 * Creation date: 30.07.2009
 */
package amuse.scheduler.gui.controller;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import amuse.data.io.DataSetAbstract;
import amuse.data.io.FileListInput;
import amuse.data.datasets.ClassifierConfigSet;
import amuse.data.datasets.FileTableSet;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.classifier.ClassifierView;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.scheduler.gui.filesandfeatures.FileTreeController;
import amuse.scheduler.gui.filesandfeatures.FileTreeModel;
import amuse.scheduler.gui.filesandfeatures.FileTreeView;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;

/**
 * @author Clemens Waeltken
 *
 */
public class ClassifierController extends AbstractController {

    private WizardController wizardController;
    private String[] endings = {"mp3", "wav"};
    private File musicDatabaseFolder = new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE));
    private String musicDatabaseLabel = "Music Database";
    private FileTreeView fileView = new FileTreeView();
    private FileTreePanel ftPanel = new FileTreePanel(fileView);
    private FileTreeModel ftModel = new FileTreeModel(musicDatabaseFolder, musicDatabaseLabel, endings);
    private FileTreeController ftController = new FileTreeController(ftModel, fileView);
    private ClassifierView classifierView;
    private static final File clFolder = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "experiments" + File.separator + "CL");

    public ClassifierController(WizardController wc) {
        this.wizardController = wc;
        classifierView = new ClassifierView(this);
    }

    @Override
    public JComponent getView() {
        return ftPanel;
    }

    private void goToClassifierSetup() {
        wizardController.setWizardPanel(classifierView);
    }

    @Override
    public void saveTask(File file) {
        file = addArff(file);
        if (!askOverwrite(file)) {
            return;
        }
        /* Gather all neccessary information and create variables */
        File musicFilesFile = new File(file.getParent() + File.separator + "filelists" + File.separator + file.getName());
        String processedFeatureDescription = classifierView.getProcessingModelStr();
        String algorithmId = classifierView.getSelectedTrainingAlgorithmStr();
        int categoryId = classifierView.getCategoryID();
        int mergeSongResults = 1;
        if (!classifierView.isAverageCalculationSelected()) {
            mergeSongResults = 0;
        }
        String outputResultPath = classifierView.getTargetFilePath();
        ClassifierConfigSet dataSet = new ClassifierConfigSet(musicFilesFile, "FILE_LIST", processedFeatureDescription, algorithmId, categoryId, mergeSongResults, outputResultPath);
        // Create folders...
        musicFilesFile.getParentFile().mkdirs();
        FileTableSet fileTableSet = new FileTableSet(ftModel.getFiles());
        // Save Files and Features:
        try {
            dataSet.saveToArffFile(file);
            fileTableSet.saveToArffFile(musicFilesFile);
        } catch (IOException ex) {
            showErr(ex.getLocalizedMessage());
        }
        showMsg("Successfully saved: " + file);
    }

    public void saveButtonClicked() {
        clFolder.mkdirs();
        JFileChooser fc = new SelectArffFileChooser("Classification Task", clFolder);
        if (fc.showSaveDialog(classifierView) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fc.getSelectedFile();
        saveTask(selectedFile);
    }

    @Override
    public void loadTask(DataSetAbstract dataSet) {
        try {
            ClassificationConfiguration set = ClassificationConfiguration.loadConfigurationsFromDataSet(new ClassifierConfigSet(dataSet))[0];
            setConfiguration(set);
        } catch (IOException e) {
            showErr(e.getLocalizedMessage());
        }
    }

    private void setConfiguration(ClassificationConfiguration conf) {
        classifierView.setProcessingModelStr(conf.getProcessedFeaturesModelName());
        classifierView.setSelectedTrainingAlgorithm(conf.getAlgorithmDescription());
        classifierView.setSelectedCategoryID(conf.getCategoryId());
        classifierView.setAverageCalculationSelected(conf.getMergeSongResults());
        classifierView.setTargetFilePath(conf.getClassificationOutput());
        if (conf.getInputToClassify() instanceof FileListInput) {
            ftController.loadFiles(((FileListInput) conf.getInputToClassify()).getInputFiles());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ClassificationConfiguration getExperimentConfiguration() {
        /* Gather all neccessary information and create variables */
        ClassificationConfiguration conf = null;
        try {
            File musicFilesFile = File.createTempFile("FileTable", "arff");
            musicFilesFile.deleteOnExit();
            String processedFeatureDescription = classifierView.getProcessingModelStr();
            String algorithmStr = classifierView.getSelectedTrainingAlgorithmStr();
            int categoryId = classifierView.getCategoryID();
            int mergeSongResults = 1;
            if (!classifierView.isAverageCalculationSelected()) {
                mergeSongResults = 0;
            }
            String outputResultPath = classifierView.getTargetFilePath();
            // Create folders...
            musicFilesFile.getParentFile().mkdirs();
            FileTableSet fileTableSet = new FileTableSet(ftModel.getFiles());
            // Save Files and Features:
            fileTableSet.saveToArffFile(musicFilesFile);
            conf = new ClassificationConfiguration(ClassificationConfiguration.InputSourceType.FILE_LIST, musicFilesFile.getAbsolutePath(), processedFeatureDescription, algorithmStr, categoryId, mergeSongResults, outputResultPath);
        } catch (IOException ex) {
            showErr(ex.getLocalizedMessage());
        }
        return conf;
    }

    @Override
    public void loadTask(TaskConfiguration conf) {
        if (conf instanceof ClassificationConfiguration) {
            setConfiguration((ClassificationConfiguration) conf);
        }
    }

    private class FileTreePanel extends JPanel implements HasCaption, NextButtonUsable, HasLoadButton {

        private static final long serialVersionUID = 6775206923190367970L;

        /**
         * @param fileView
         */
        public FileTreePanel(FileTreeView fileView) {
            this.setLayout(new BorderLayout());
            this.add(fileView.getView(), BorderLayout.CENTER);
        }

        /* (non-Javadoc)
         * @see amuse.scheduler.gui.navigation.HasCaption#getCaption()
         */
        @Override
        public String getCaption() {
            return "Select Music Files to Classify";
        }

        /* (non-Javadoc)
         * @see amuse.scheduler.gui.navigation.NextButtonUsable#getNextButtonText()
         */
        @Override
        public String getNextButtonText() {
            return "Setup Classifier";
        }

        /* (non-Javadoc)
         * @see amuse.scheduler.gui.navigation.NextButtonUsable#nextButtonClicked()
         */
        @Override
        public boolean nextButtonClicked() {
            goToClassifierSetup();
            return false;
        }

        @Override
        public String getLoadButtonText() {
            return "Load";
        }

        @Override
        public void loadButtonClicked() {
            clFolder.mkdirs();
            JFileChooser fc = new SelectArffFileChooser(
                    "Classification Task", clFolder);
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

    public void addClassification() {
        taskManager.addExperiment(getExperimentConfiguration());
    }
}
