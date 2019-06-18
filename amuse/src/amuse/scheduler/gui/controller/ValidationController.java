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

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.log4j.Level;

import amuse.data.GroundTruthSourceType;
import amuse.data.MeasureTable;
import amuse.data.ModelType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.datasets.ValidatorConfigSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetException;
import amuse.data.io.FileInput;
import amuse.interfaces.nodes.TaskConfiguration;
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
import amuse.scheduler.gui.validation.MeasuresView;
import amuse.scheduler.gui.validation.ValidationView;
import amuse.util.AmuseLogger;

/**
 * @author Clemens Waeltken
 * 
 */
public class ValidationController extends AbstractController {

    private ValidationView validationView;
    private final WizardController wizardController;
    private AlgorithmConfigurationFacade validationAlgorithmFacade;
    private File validationAlgorithmTableFile = new File(AmusePreferences.getValidationAlgorithmTablePath());
    private MeasuresView measuresView;
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
            measuresView = new MeasuresView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToMeasureView() {
        wizardController.setWizardPanel(new MeasurePanel(measuresView));
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
        File measureTableFile = new File(file.getParent() + File.separator + "measureTables" + File.separator
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
        MeasureTable measureTable = measuresView.getMeasureTable();
        String processedFeatureDescription = validationView.getProcessingModelString();
        String groundTruthSource = validationView.getGroundTruthSource();
        String groundTruthSourceType = validationView.getGroundTruthSourceType().toString();
        String classificationAlgorithmId = validationView.getClassifierAlgorithmStr();
        String attributesToClassify = validationView.getAttributesToClassify().toString();
        String attributesToIgnore = validationView.getAttributesToIgnore().toString();
        String relationshipType = validationView.getModelType().getRelationshipType().toString();
        String labelType = validationView.getModelType().getLabelType().toString();
        String methodType = validationView.getModelType().getMethodType().toString();
        ValidatorConfigSet dataSet = new ValidatorConfigSet(
        		validationMethodId,
                measureTableFile, 
                processedFeatureDescription, 
                groundTruthSource, 
                groundTruthSourceType,
                attributesToClassify,
                attributesToIgnore,
                relationshipType,
                labelType,
                methodType,
                classificationAlgorithmId);
        // Create folders...
        measureTableFile.getParentFile().mkdirs();
        // Save Files and Features:
        try {
            dataSet.saveToArffFile(file);
            measureTable.saveToArffFile(measureTableFile);
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
            measuresView.loadSelection(new File(set.getMeasureListAttribute().getValueAt(0)));
            validationView.setProcessingModelString(set.getProcessedFeatureDescriptionAttribute().getValueAt(0));
            
            String attributesToClassifyString = set.getAttributesToClassifyAttribute().getValueAt(0).toString();
    		attributesToClassifyString = attributesToClassifyString.replaceAll("\\[", "").replaceAll("\\]", "");
    		String[] attributesToClassifyStringArray = attributesToClassifyString.split("\\s*,\\s*");
    		List<Integer> attributesToClassify = new ArrayList<Integer>();
    		try {
    			for(String str : attributesToClassifyStringArray) {
    				if(!str.equals("")) {
    					attributesToClassify.add(Integer.parseInt(str));
    				}
    			}
    		} catch(NumberFormatException e) {
    		}
    		validationView.setAttributesToClassify(attributesToClassify);
    		
    		String attributesToIgnoreString = set.getAttributesToIgnoreAttribute().getValueAt(0).toString();
    		attributesToIgnoreString = attributesToIgnoreString.replaceAll("\\[", "").replaceAll("\\]", "");
    		String[] attributesToIgnoreStringArray = attributesToIgnoreString.split("\\s*,\\s*");
    		List<Integer> attributesToIgnore = new ArrayList<Integer>();
    		try {
    			for(String str : attributesToIgnoreStringArray) {
    				if(!str.equals("")) {
    					attributesToIgnore.add(Integer.parseInt(str));
    				}
    			}
    		} catch(NumberFormatException e) {
    			AmuseLogger.write(this.getClass().getName(), Level.WARN,
    					"The attributes to ignore were not properly specified. All features will be used for training.");
    			attributesToIgnore = new ArrayList<Integer>();
    		}
    		validationView.setAttributesToIgnore(attributesToIgnore);
    		
    		ModelType modelType = new ModelType(RelationshipType.valueOf(set.getRelationshipTypeAttribute().getValueAt(0)), LabelType.valueOf(set.getLabelTypeAttribute().getValueAt(0)), MethodType.valueOf(set.getMethodTypeAttribute().getValueAt(0)));
    		validationView.setModelType(modelType);
            
        } catch (IOException ex) {
            showErr(ex.getLocalizedMessage());
        }
    }

    @Override
    public ValidationConfiguration getExperimentConfiguration() {
        /* Gather all neccessary information and create variables */
        ValidationConfiguration conf = null;
        String validationMethodStr = validationAlgorithmFacade.getSelectedAlgorithm().getIdAndParameterStr();
        MeasureTable measureTable = measuresView.getMeasureTable();
        String processedFeatureDescription = validationView.getProcessingModelString();
        FileInput groundTruthSource = new FileInput(validationView.getGroundTruthSource());
        GroundTruthSourceType groundTruthSourceType = validationView.getGroundTruthSourceType();
        String classificationAlgorithmStr = validationView.getClassifierAlgorithmStr();
        List<Integer> attributesToClassify = validationView.getAttributesToClassify();
        List<Integer> attributesToIgnore = validationView.getAttributesToIgnore();
        ModelType modelType = validationView.getModelType();
        conf = new ValidationConfiguration(
        		validationMethodStr, 
        		measureTable,
                processedFeatureDescription, 
                classificationAlgorithmStr, 
                groundTruthSource,
                groundTruthSourceType,
                attributesToClassify,
                attributesToIgnore,
                modelType);
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
            return "Validation Configurator";
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * amuse.scheduler.gui.navigation.NextButtonUsable#getNextButtonText()
         */
        @Override
        public String getNextButtonText() {
            return "Setup Measures";
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * amuse.scheduler.gui.navigation.NextButtonUsable#nextButtonClicked()
         */
        @Override
        public boolean nextButtonClicked() {
            goToMeasureView();
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

    private class MeasurePanel extends JPanel implements HasCaption,
            NextButtonUsable, HasSaveButton {

        public MeasurePanel(JComponent comp) {
            super(new BorderLayout());
            this.add(comp, BorderLayout.CENTER);
        }

        @Override
        public String getCaption() {
            return "Select Measures";
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
            measuresView.loadSelection(valConf.getMeasures());
            validationView.setAttributesToClassify(valConf.getAttributesToClassify());
            validationView.setAttributesToIgnore(valConf.getAttributesToIgnore());
            validationView.setModelType(valConf.getModelType());
        }
    }
}
