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
 * Creation date: 18.02.2008
 */
package amuse.scheduler.gui.controller;

import amuse.data.FileTable;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.TaskConfiguration;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;


import amuse.data.io.DataSetException;
import amuse.data.datasets.ExtractorConfigSet;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.scheduler.gui.filesandfeatures.FilesAndFeaturesFacade;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import java.awt.BorderLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * 
 * @author Clemens Waeltken
 */
public class ExtractionController extends AbstractController {

    WizardController wizardController;
    FilesAndFeaturesFacade filesAndFeatures;
    File feFolder = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH)
            + File.separator + "experiments" + File.separator + "FE");

    public ExtractionController(WizardController wc) {
        wizardController = wc;
        getView();
    }

    public void addExtraction() {
        // System.out.println("File count: " +
        // jPanelAmuseFileTree.getFiles().size());
        if (!filesAndFeatures.filesAndFeaturesSelected()) {
            JOptionPane.showMessageDialog(filesAndFeatures.getView(),
                    "Please select at least one music file and feature!",
                    "Unable to start Feature Extraction",
                    JOptionPane.WARNING_MESSAGE);
            // System.out.println("No Files!");
            return;
        }
        taskManager.addExperiment(getExperimentConfiguration());
    }

    @Override
    public JComponent getView() {
        return getFilesAndFeatures();
    }

    private JComponent getFilesAndFeatures() {
        JPanel p = new ExtracionPanel();
        if (filesAndFeatures == null) {
            filesAndFeatures = new FilesAndFeaturesFacade();
        }
        p.add(filesAndFeatures.getView(), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void saveTask(File selectedFile) {
        selectedFile = addArff(selectedFile);
        askOverwrite(selectedFile);
        File fileTableFile = new File(selectedFile.getParent() + File.separator + "filelists" + File.separator
                + selectedFile.getName());
        File featureTableFile = new File(selectedFile.getParent()
                + File.separator + "featurelists" + File.separator + selectedFile.getName());
        try {
            fileTableFile.getParentFile().mkdirs();
            featureTableFile.getParentFile().mkdirs();
            selectedFile.getParentFile().mkdirs();
        } catch (SecurityException ex) {
            showErr(ex.getLocalizedMessage());
            return;
        }
        ExtractorConfigSet extractorConfigSet = new ExtractorConfigSet(
                fileTableFile, featureTableFile);
        try {
            extractorConfigSet.saveToArffFile(selectedFile);
            filesAndFeatures.saveFilesAndFeatures(fileTableFile,
                    featureTableFile);
        } catch (IOException ex) {
            showErr(ex.getLocalizedMessage());
            return;
        }
        showMsg("Successfully saved: " + selectedFile.getPath() + ",\n "
                + fileTableFile.getPath() + " and\n"
                + featureTableFile.getPath() + ".");
    }

    @Override
    public void loadTask(DataSetAbstract dataSet) {
        ExtractorConfigSet extract = null;
        try {
            extract = new ExtractorConfigSet(dataSet);
        } catch (DataSetException ex) {
            showErr(ex.getLocalizedMessage());
            return;
        }
        if (extract.getMusicFileLists().isEmpty()) {
            showErr("Empty Configuration.");
            return;
        }
        File musicFileList = extract.getMusicFileLists().get(0);
        File featureTableFile = extract.getFeatureTables().get(0);
        filesAndFeatures.loadFilesAndFeatures(musicFileList, featureTableFile);
    }

    @Override
    public ExtractionConfiguration getExperimentConfiguration() {
        ExtractionConfiguration config = new ExtractionConfiguration(new FileTable(filesAndFeatures.getFiles()),
                filesAndFeatures.getFeatureTable());
        return config;
    }

    @Override
    public void loadTask(TaskConfiguration conf) {
        if (conf instanceof ExtractionConfiguration) {
            ExtractionConfiguration ex = (ExtractionConfiguration) conf;
            filesAndFeatures.loadFileList(ex.getMusicFileList());
            filesAndFeatures.loadFeatureTable(ex.getFeatureTable());
        }
    }

    private class ExtracionPanel extends JPanel implements HasSaveButton,
            HasLoadButton, NextButtonUsable, HasCaption {

        public ExtracionPanel() {
            super(new BorderLayout());
        }

        @Override
        public String getSaveButtonText() {
            return "Save";
        }

        @Override
        public void saveButtonClicked() {
            feFolder.mkdirs();
            JFileChooser fc = new SelectArffFileChooser(
                    "Feature Extraction Task", feFolder);
            if (fc.showSaveDialog(filesAndFeatures.getView()) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selectedFile = fc.getSelectedFile();
            saveTask(selectedFile);
        }

        @Override
        public String getLoadButtonText() {
            return "Load";
        }

        @Override
        public void loadButtonClicked() {
            feFolder.mkdirs();
            JFileChooser fc = new SelectArffFileChooser(
                    "Feature Extraction Task", feFolder);
            if (fc.showOpenDialog(filesAndFeatures.getView()) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selectedFile = fc.getSelectedFile();
            try {
                loadTask(selectedFile);
            } catch (IOException ex) {
                showErr(ex.getLocalizedMessage());
            }
        }

        @Override
        public boolean nextButtonClicked() {
            addExtraction();
            return false;
        }

        @Override
        public String getNextButtonText() {
            return "Finish Configuration";
        }

        @Override
        public String getCaption() {
            return "Feature Extraction Configurator";
        }
    }
}
