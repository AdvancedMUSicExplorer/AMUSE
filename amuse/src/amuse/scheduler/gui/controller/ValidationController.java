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
 * Creation date: 07.08.2009
 */
package amuse.scheduler.gui.controller;

import amuse.data.MetricTable;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetException;
import amuse.data.io.FileInput;
import amuse.data.datasets.ValidatorConfigSet;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;

import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.GroundTruthSourceType;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.algorithm.Algorithm;
import amuse.scheduler.gui.algorithm.AlgorithmConfigurationFacade;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import amuse.scheduler.gui.validation.MetricsView;
import amuse.scheduler.gui.validation.ValidationView;
import java.util.Arrays;
import javax.swing.JFileChooser;

/**
 * @author Clemens Waeltken
 * 
 */
public class ValidationController extends AbstractController {

    private ValidationView validationView;
    private final WizardController wizardController;
    private AlgorithmConfigurationFacade validationAlgorithmFacade;
    private String amusePath = AmusePreferences.get(KeysStringValue.AMUSE_PATH);
    private File validationAlgorithmTableFile = new File(amusePath
            + File.separator + "config" + File.separator + "validationAlgorithmTable.arff");
    private MetricsView metricsView;
    private static final File vtFolder = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH)
            + File.separator + "experiments" + File.separator + "VT");

    /**
     * @param wizardController
     *            The WizardController initiating Validation.
     */
    public ValidationController(WizardController wizardController) {
        this.wizardController = wizardController;
        try {
            validationAlgorithmFacade = new AlgorithmConfigurationFacade(
                    "Validation", validationAlgorithmTableFile);
            validationView = new ValidationView(validationAlgorithmFacade);
            metricsView = new MetricsView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToMetricView() {
        wizardController.setWizardPanel(new MetricPanel(metricsView));
    }

    /**
     * @return
     */
    public JComponent getView() {
        return new ValidationPanel(validationView.getView());
    }

    @Override
    public void saveTask(File file) {
        file = addArff(file);
        if (!askOverwrite(file)) {
            return;
        }
        /* Gather all necessary information and create variables */
        File metricTableFile = new File(file.getParent() + File.separator + "metricTables" + File.separator
                + file.getName());
        String parameterStr = "";
        if (validationAlgorithmFacade.getSelectedAlgorithm().getCurrentParameterValues().length > 0) {
        	Algorithm selectedAlgorithm = validationAlgorithmFacade.getSelectedAlgorithm();
        	String[] allowedParameterStrings = selectedAlgorithm.getAllowedParamerterStrings();
        	String[] currentParameterValues = selectedAlgorithm.getCurrentParameterValues();
        	for(int i = 0; i < currentParameterValues.length; i++){
        		if(allowedParameterStrings[i].equals("fof")){
        			currentParameterValues[i] = "|" + currentParameterValues[i] + "|";
        		}
        	}
            parameterStr = Arrays.toString(validationAlgorithmFacade.getSelectedAlgorithm().getCurrentParameterValues());
        }
        String validationMethodId = validationAlgorithmFacade.getSelectedAlgorithm().getIdAndParameterStr();
        MetricTable metricTable = metricsView.getMetricTable();
        String processedFeatureDescription = validationView.getProcessingModelString();
        String groundTruthSource = validationView.getGroundTruthSource();
        String groundTruthSourceType = validationView.getGroundTruthSourceType().toString();
        String classificationAlgorithmId = validationView.getClassifierAlgorithmStr();
        ValidatorConfigSet dataSet = new ValidatorConfigSet(
        		validationMethodId,
                metricTableFile, 
                processedFeatureDescription, 
                groundTruthSource, 
                groundTruthSourceType,
                classificationAlgorithmId);
        // Create folders...
        metricTableFile.getParentFile().mkdirs();
        // Save Files and Features:
        try {
            dataSet.saveToArffFile(file);
            metricTable.saveToArffFile(metricTableFile);
        } catch (IOException ex) {
            showErr(ex.getLocalizedMessage());
        }
        showMsg("Successfully saved: " + file);
    }

    @Override
    public void loadTask(DataSetAbstract dataSet) {
        try {
            ValidatorConfigSet set = new ValidatorConfigSet(dataSet);
            setConfiguration(set);
        } catch (DataSetException e) {
            showErr(e.getLocalizedMessage());
        }
    }

    private void setConfiguration(ValidatorConfigSet set) {
        try {
        	/*
        	String validationMethodIdAttribute = set.getValidationMethodIdAttribute().getValueAt(0);
        	String id = validationMethodIdAttribute.substring(0, validationMethodIdAttribute.indexOf('['));
        	if(id.equals("0")){
        		String[] parameters = new String[]{validationMethodIdAttribute.substring(validationMethodIdAttribute.indexOf('['),validationMethodIdAttribute.lastIndexOf(']') + 1)};
        		validationAlgorithmFacade.setSelectedAlgorithm(id + "[]");
        		validationAlgorithmFacade.getSelectedAlgorithm().setCurrentParameters(parameters);
        	}
        	else{
        		validationAlgorithmFacade.setSelectedAlgorithm(validationMethodIdAttribute);
        	}
        	*/
        	validationAlgorithmFacade.setSelectedAlgorithm(set.getValidationMethodIdAttribute().getValueAt(0));
            // FIXME was tun wenn kein Int?
        	String groundTruthSourceType = set.getGroundTruthSourceAttribute().getValueAt(0);
        	validationView.setGroundTruthSourceType(GroundTruthSourceType.valueOf(groundTruthSourceType));
        	validationView.setGroundTruthSource(set.getInputToValidateAttribute().getValueAt(0));
            validationView.setClassifierAlgorithm(set.getClassificationAlgorithmIdAttribute().getValueAt(0));
            metricsView.loadSelection(new File(set.getMetricListAttribute().getValueAt(0)));
            validationView.setProcessingModelString(set.getProcessedFeatureDescriptionAttribute().getValueAt(0));
        } catch (IOException ex) {
            showErr(ex.getLocalizedMessage());
        }
    }

    @Override
    public ValidationConfiguration getExperimentConfiguration() {
        /* Gather all neccessary information and create variables */
        ValidationConfiguration conf = null;
        String validationMethodStr = validationAlgorithmFacade.getSelectedAlgorithm().getIdAndParameterStr();
        MetricTable metricTable = metricsView.getMetricTable();
        String processedFeatureDescription = validationView.getProcessingModelString();
        FileInput groundTruthSource = new FileInput(validationView.getGroundTruthSource());
        GroundTruthSourceType groundTruthSourceType = validationView.getGroundTruthSourceType();
        String classificationAlgorithmStr = validationView.getClassifierAlgorithmStr();
        conf = new ValidationConfiguration(
        		validationMethodStr, 
        		metricTable,
                processedFeatureDescription, 
                classificationAlgorithmStr, 
                groundTruthSource,
                groundTruthSourceType);
        return conf;
    }

    private class ValidationPanel extends JPanel implements HasCaption,
            NextButtonUsable, HasLoadButton {

        public ValidationPanel(JComponent comp) {
            super(new BorderLayout());
            this.add(comp, BorderLayout.CENTER);
        }
        private static final long serialVersionUID = 7876865964338180871L;

        /*
         * (non-Javadoc)
         *
         * @see amuse.scheduler.gui.navigation.HasCaption#getCaption()
         */
        @Override
        public String getCaption() {
            return "Setup Validation Algorithm";
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * amuse.scheduler.gui.navigation.NextButtonUsable#getNextButtonText()
         */
        @Override
        public String getNextButtonText() {
            return "Setup Metrics";
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * amuse.scheduler.gui.navigation.NextButtonUsable#nextButtonClicked()
         */
        @Override
        public boolean nextButtonClicked() {
            goToMetricView();
            return false;
        }

        @Override
        public String getLoadButtonText() {
            return "Load";
        }

        @Override
        public void loadButtonClicked() {
            vtFolder.mkdirs();
            JFileChooser fc = new SelectArffFileChooser(
                    "Validation Task", vtFolder);
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

    private class MetricPanel extends JPanel implements HasCaption,
            NextButtonUsable, HasSaveButton {

        public MetricPanel(JComponent comp) {
            super(new BorderLayout());
            this.add(comp, BorderLayout.CENTER);
        }

        @Override
        public String getCaption() {
            return "Select Metrics";
        }

        @Override
        public boolean nextButtonClicked() {
            addValidation();
            return false;
        }

        @Override
        public String getNextButtonText() {
            return "Finish Configuration";
        }

        @Override
        public String getSaveButtonText() {
            return "Save";
        }

        @Override
        public void saveButtonClicked() {
            vtFolder.mkdirs();
            JFileChooser fc = new SelectArffFileChooser("Validation Task",
                    vtFolder);
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selectedFile = fc.getSelectedFile();
            saveTask(selectedFile);
        }
    }

    /**
     *
     */
    public void addValidation() {
        taskManager.addExperiment(getExperimentConfiguration());
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
        if (conf instanceof ValidationConfiguration) {
            ValidationConfiguration valConf = (ValidationConfiguration) conf;
            validationAlgorithmFacade.setSelectedAlgorithm(valConf.getValidationAlgorithmDescription());
            validationView.setGroundTruthSourceType(valConf.getGroundTruthSourceType());
            validationView.setGroundTruthSource(((FileInput)valConf.getInputToValidate()).toString());
            validationView.setClassifierAlgorithm(valConf.getClassificationAlgorithmDescription());
            metricsView.loadSelection(valConf.getMetrics());
        }
    }
}
