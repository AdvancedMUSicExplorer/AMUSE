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

import amuse.data.FeatureTable;
import amuse.data.FileTable;
import amuse.data.io.DataSetAbstract;

import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import amuse.data.io.DataSetException;
import amuse.data.datasets.ProcessorConfigSet;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.scheduler.gui.filesandfeatures.FilesAndFeaturesFacade;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import amuse.scheduler.gui.processing.MatrixToVectorMethodModel;
import amuse.scheduler.gui.processing.ProcessingMethodsController;
import amuse.scheduler.gui.processing.ProcessingMethodsListModel;
import amuse.scheduler.gui.processing.ProcessingMethodsPanel;
import amuse.util.AmuseLogger;
import java.awt.BorderLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * 
 * @author Clemens Waeltken
 */
public class ProcessingController extends AbstractController {

    WizardController wizardController;
    ProcessingMethodsController pcmController;
    ProcessingMethodsPanel pcmPanel;
    FilesAndFeaturesFacade filesAndFeatures;
    private File fpFolder = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH)
            + File.separator + "experiments" + File.separator + "FP");

    public ProcessingController(WizardController wizardController) {
        getFilesAndFeatures();
        this.wizardController = wizardController;
        ProcessingMethodsListModel pcmListModel = null;
        MatrixToVectorMethodModel mtvComboBoxModel = null;
        try {
            pcmListModel = new ProcessingMethodsListModel();
        } catch (IOException ex) {
            AmuseLogger.write(this.getClass().getName(),
                    org.apache.log4j.Level.ERROR,
                    "Can not Load Processing Algorithm Table: "
                    + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        try {
            mtvComboBoxModel = new MatrixToVectorMethodModel();
        } catch (IOException ex) {
            AmuseLogger.write(this.getClass().getName(),
                    org.apache.log4j.Level.ERROR,
                    "Can not Load Matrix To Vector Algorithm Table: "
                    + ex.getLocalizedMessage());
        }
        if (pcmController == null) {
            pcmController = new ProcessingMethodsController(this, pcmListModel,
                    mtvComboBoxModel);
        }
    }

    public void goToProcessingMethods() {
        if (!filesAndFeatures.filesAndFeaturesSelected()) {
            JOptionPane.showMessageDialog(
                    filesAndFeatures.getView(),
                    "Please select at least one feature and music file to process!",
                    "Unable to continue Feature Proccessing",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        wizardController.setWizardPanel(pcmController.getView());
    }

    public JComponent getView() {
        return getFilesAndFeatures();
    }

    private JComponent getFilesAndFeatures() {
        JPanel p = new ExtractionPanel();
        if (filesAndFeatures == null) {
            File featureTableFile = new File(AmusePreferences.getFeatureTablePath());
            FeatureTable featureTable = new FeatureTable(featureTableFile);
            featureTable.removeUnsuitableForFeatureMatrixProcessing();
            filesAndFeatures = new FilesAndFeaturesFacade(featureTable);
            
        }
        p.add(filesAndFeatures.getView(), BorderLayout.CENTER);
        return p;
    }
    

    /**
     *
     */
    public void addProcessing() {
        taskManager.addExperiment(getExperimentConfiguration());
    }

    public void saveButtonClicked() {
        fpFolder.mkdirs();
        JFileChooser fc = new SelectArffFileChooser("Feature Processing Task",
                fpFolder);
        if (fc.showSaveDialog(filesAndFeatures.getView()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fc.getSelectedFile();
        saveTask(selectedFile);
    }

    @Override
    public void saveTask(File selectedFile) {
        selectedFile = addArff(selectedFile);
        if (!askOverwrite(selectedFile)) {
            return;
        }
        /* Gather all neccessary information and create variables */
        File featureTableFile = new File(selectedFile.getParent()
                + File.separator + "featurelists" + File.separator + selectedFile.getName());
        File musicFilesFile = new File(selectedFile.getParent() + File.separator + "filelists" + File.separator
                + selectedFile.getName());
        String reductionSteps = pcmController.getReductionSteps();
        String unit = pcmController.getUnit().toString();
        int classificationWindowSize = pcmController.getClassificationWindowSize();
        int classificationWindowStepSize = pcmController.getClassificationWindowStepSize();
        String matrixToVector = pcmController.getConversionStep();
        String optionalModelStr = pcmController.getOptionalModelStr();
        /*ProcessorConfigSet dataSet = new ProcessorConfigSet(musicFilesFile,
        		featureTableFile, reductionSteps, unit, classificaitonWindowSize,
                partitiopnStepSize, matrixToVector, optionalModelStr);*/
        // FIXME
        ProcessorConfigSet dataSet = new ProcessorConfigSet(musicFilesFile,
        		ProcessingConfiguration.InputSourceType.RAW_FEATURE_LIST.toString(),featureTableFile.toString(), reductionSteps, unit, classificationWindowSize,
                classificationWindowStepSize, matrixToVector, optionalModelStr);
        
        // Create folders...
        featureTableFile.getParentFile().mkdirs();
        musicFilesFile.getParentFile().mkdirs();

        // Save Files and Features:
        try {
            dataSet.saveToArffFile(selectedFile);
            filesAndFeatures.saveFilesAndFeatures(musicFilesFile,
                    featureTableFile);
        } catch (IOException ex) {
            showErr(ex.getLocalizedMessage());
        }
        showMsg("Successfully saved: " + selectedFile);
    }

    @Override
    public void loadTask(DataSetAbstract dataSet) {
        ProcessorConfigSet config = null;
        try {
            config = new ProcessorConfigSet(dataSet);
        } catch (DataSetException ex) {
            showErr(ex.getLocalizedMessage());
            return;
        }
        if (config.getMusicFileLists().isEmpty()) {
            showErr("Empty Configuration.");
            return;
        }
        File musicFileList = config.getMusicFileLists().get(0);
        File featureTableFile = config.getFeatureTables().get(0);
        filesAndFeatures.loadFilesAndFeatures(musicFileList, featureTableFile);
        pcmController.setUnit(config.getUnitAttribute().getValueAt(0));
        pcmController.setClassificationWindowSize(config.getAggregationWindowSizeAttribute().getValueAt(0).intValue());
        pcmController.setStepSize(config.getAggregationWindowStepSizeAttribute().getValueAt(0).intValue());
        pcmController.setMatrixToVector(config.getMatrixToVectorAttribute().getValueAt(0));
        pcmController.setReductionSteps(config.getReductionStepsAttribute().getValueAt(0));
        pcmController.setOptionalModelStr(config.getFeatureDescriptionAttribute().getValueAt(0));

    }

    @Override
    public ProcessingConfiguration getExperimentConfiguration() {
        /* Gather all neccessary information and create variables */
        String reductionSteps = pcmController.getReductionSteps();
        Unit unit = pcmController.getUnit();
        int classificationWindowSize = pcmController.getClassificationWindowSize();
        int classificationWindowStepSize = pcmController.getClassificationWindowStepSize();
        String matrixToVector = pcmController.getConversionStep();
        String optionalModelStr = pcmController.getOptionalModelStr();
        FileTable files = filesAndFeatures.getFileTable();
        ProcessingConfiguration conf = new ProcessingConfiguration(files, filesAndFeatures.getFeatureTable(),
                reductionSteps, unit, classificationWindowSize, classificationWindowStepSize,
                matrixToVector, optionalModelStr);
        return conf;
    }

    private class ExtractionPanel extends JPanel implements HasCaption,
            NextButtonUsable, HasLoadButton {

        public ExtractionPanel() {
            super(new BorderLayout());
        }

        @Override
        public String getCaption() {
            return "Feature Processing Configurator";
        }

        @Override
        public boolean nextButtonClicked() {
            goToProcessingMethods();
            return false;
        }

        @Override
        public String getNextButtonText() {
            return "Setup Processing Methods";
        }

        @Override
        public String getLoadButtonText() {
            return "Load";
        }

        @Override
        public void loadButtonClicked() {
            fpFolder.mkdirs();
            JFileChooser fc = new SelectArffFileChooser(
                    "Feature Processing Task", fpFolder);
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
        if (conf instanceof ProcessingConfiguration) {
            ProcessingConfiguration ex = (ProcessingConfiguration) conf;
            filesAndFeatures.loadFileList(ex.getMusicFileList());
            filesAndFeatures.loadFeatureTable(ex.getInputFeatureList());

            // pcmController.setUnit();
        pcmController.setClassificationWindowSize(ex.getAggregationWindowSize());
        pcmController.setStepSize(ex.getAggregationWindowStepSize());
        pcmController.setMatrixToVector(ex.getConversionStep());
        pcmController.setReductionSteps(ex.getReductionSteps());
        pcmController.setOptionalModelStr(ex.getFeatureDescription());
        }
    }
}
