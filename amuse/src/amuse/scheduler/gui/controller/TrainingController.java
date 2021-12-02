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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Level;

import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetException;
import amuse.data.io.FileInput;
import amuse.data.ModelType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.FeatureTable;
import amuse.data.GroundTruthSourceType;
import amuse.data.InputFeatureType;
import amuse.data.datasets.TrainingConfigSet;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import amuse.scheduler.gui.training.TrainingView;
import amuse.util.AmuseLogger;

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
        this.trainingView = new TrainingView(true);
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
        	String inputFeatureDescription;
            File featureListFile = null;
            if(trainingView.getInputFeatureType() == InputFeatureType.RAW_FEATURES) {
            	featureListFile = new File(file.getParent() + File.separator + "featurelists" + File.separator + file.getName());
            	inputFeatureDescription = featureListFile.getAbsolutePath();
            } else {
            	inputFeatureDescription = trainingView.getProcessingModelString();
            }
            TrainingConfigSet dataSet = new TrainingConfigSet(
            		inputFeatureDescription,
            		trainingView.getInputFeatureType().toString(),
            		trainingView.getUnit().toString(),
            		trainingView.getClassificaitonWindowSize(), 
            		trainingView.getClassificationWindowStepSize(),
            		trainingView.getSelectedTrainingAlgorithmStr(),
            		trainingView.getPreprocessingAlgorithmStr(),
            		trainingView.getGroundTruthSource(),
            		trainingView.getGroundTruthSourceType().toString(),
            		trainingView.getAttributesToPredict().toString(),
            		trainingView.getAttributesToIgnore().toString(),
            		trainingView.getModelType().getRelationshipType().toString(),
            		trainingView.getModelType().getLabelType().toString(),
            		trainingView.getModelType().getMethodType().toString(),
            		trainingView.getTrainingDescription(),
            		trainingView.getPathToOutputModel());
            
            // if the input features are given as raw features a feature list must be saved
            if(trainingView.getInputFeatureType() == InputFeatureType.RAW_FEATURES) {
            	// Create folders...
            	featureListFile.getParentFile().mkdirs();
            	FeatureTable inputFeatures = trainingView.getInputFeatures();
            	inputFeatures.getAccordingDataSet().saveToArffFile(featureListFile);
            }
            dataSet.saveToArffFile(file);
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
        
        String inputFeatureType = ttSet.getInputFeatureTypeAttribute().getValueAt(0);
        if(inputFeatureType.equals(InputFeatureType.RAW_FEATURES.toString())) {
        	trainingView.setInputFeatureType(InputFeatureType.RAW_FEATURES);
        	FeatureTable inputFeatures = new FeatureTable(new File(ttSet.getInputFeatureAttribute().getValueAt(0)));
        	trainingView.setInputFeatures(inputFeatures);
        	trainingView.setUnit(Unit.valueOf(ttSet.getUnitAttribute().getValueAt(0).toString()));
        	trainingView.setClassificationWindowSize(ttSet.getClassificationWindowSizeAttribute().getValueAt(0).intValue());
        	trainingView.setClassificationWindowStepSize(ttSet.getClassificationWindowStepSizeAttribute().getValueAt(0).intValue());
        } else {
        	trainingView.setInputFeatureType(InputFeatureType.PROCESSED_FEATURES);
        	trainingView.setProcessingModelString(ttSet.getInputFeatureAttribute().getValueAt(0));
        }
        
        trainingView.setProcessingModelString(ttSet.getInputFeatureAttribute().getValueAt(0));
        trainingView.setPreprocessingAlgorithm(ttSet.getPreprocessingAlgorithmIdAttribute().getValueAt(0));

        String groundTruthSourceType = ttSet.getGroundTruthSourceTypeAttribute().getValueAt(0);
        if(groundTruthSourceType.equals(GroundTruthSourceType.CATEGORY_ID.toString())){
        	trainingView.setGroundTruthSourceType(GroundTruthSourceType.CATEGORY_ID);
        }
        else if(groundTruthSourceType.equals(GroundTruthSourceType.READY_INPUT.toString())){
        	trainingView.setGroundTruthSourceType(GroundTruthSourceType.READY_INPUT);
        } else {
        	trainingView.setGroundTruthSourceType(GroundTruthSourceType.FILE_LIST);
        }
        trainingView.setGroundTruthSource(ttSet.getGroundTruthSourceAttribute().getValueAt(0));
        
        String attributesToPredictString = ttSet.getAttributesToPredictAttribute().getValueAt(0).toString();
		attributesToPredictString = attributesToPredictString.replaceAll("\\[", "").replaceAll("\\]", "");
		String[] attributesToPredictStringArray = attributesToPredictString.split("\\s*,\\s*");
		List<Integer> attributesToPredict = new ArrayList<Integer>();
		try {
			for(String str : attributesToPredictStringArray) {
				if(!str.equals("")) {
					attributesToPredict.add(Integer.parseInt(str));
				}
			}
		} catch(NumberFormatException e) {
			AmuseLogger.write(this.getClass().getName(), Level.WARN,
					"The attributes to classify were not properly specified.");
			attributesToPredict = new ArrayList<Integer>();
		}
		trainingView.setAttributesToPredict(attributesToPredict);
		
		String attributesToIgnoreString = ttSet.getAttributesToIgnoreAttribute().getValueAt(0).toString();
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
					"The attributes to ignore were not properly specified.");
			attributesToIgnore = new ArrayList<Integer>();
		}
		trainingView.setAttributesToIgnore(attributesToIgnore);
		
		String test = ttSet.getTrainingDescriptionAttribute().getValueAt(0).toString();
		trainingView.setTrainingDescription(ttSet.getTrainingDescriptionAttribute().getValueAt(0).toString());
		trainingView.setPathToOutputModel(ttSet.getPathToOutputModelAttribute().getValueAt(0).toString());
		
		try {
			ModelType modelType = new ModelType(RelationshipType.valueOf(ttSet.getRelationshipTypeAttribute().getValueAt(0)), LabelType.valueOf(ttSet.getLabelTypeAttribute().getValueAt(0)), MethodType.valueOf(ttSet.getMethodTypeAttribute().getValueAt(0)));
			trainingView.setModelType(modelType);
		} catch(IOException e) {
			showErr(e.getLocalizedMessage());
		}
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
            return "Classification Training Configurator";
        }

        public void addTraining() {
        	TrainingConfiguration tConf = getExperimentConfiguration();
        	if(tConf.getInputFeatureType() == InputFeatureType.PROCESSED_FEATURES || tConf.getInputFeatureList().size() > 0) {
        		taskManager.addExperiment(tConf);
            } else {
            	JOptionPane.showMessageDialog(
                        getView(),
                        "Please select at least one input feature for training!",
                        "Unable to add training task",
                        JOptionPane.WARNING_MESSAGE);
            }
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
                    "Classificator Training Task", ctFolder);
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
//    	String folder = trainingView.getGroundTruthSource();
//    	if(trainingView.getGroundTruthSourceType() != GroundTruthSourceType.CATEGORY_ID){
//    		folder = folder.substring(folder.lastIndexOf(File.separatorChar) + 1, folder.lastIndexOf('.'));
//    	}
//    	String pathToOutputModel = AmusePreferences.get(KeysStringValue.MODEL_DATABASE)
//    			+ File.separator
//    			+ folder
//    			+ File.separator
//    			+ trainingView.getSelectedTrainingAlgorithmStr()
//    			+ File.separator
//    			+ trainingView.getProcessingModelString()
//    			+ File.separator
//    			+ "model.mod";
//    	pathToOutputModel = pathToOutputModel.replaceAll(File.separator + "+", File.separator);
    	TrainingConfiguration conf;
    	if(trainingView.getInputFeatureType() == InputFeatureType.RAW_FEATURES) {
	    	conf = new TrainingConfiguration(
	    			trainingView.getInputFeatures(),
	    			trainingView.getUnit(),
	    			trainingView.getClassificaitonWindowSize(),
	    			trainingView.getClassificationWindowStepSize(),
	    			trainingView.getSelectedTrainingAlgorithmStr(),
	    			trainingView.getPreprocessingAlgorithmStr(), 
	    			new FileInput(trainingView.getGroundTruthSource()),
	    			trainingView.getGroundTruthSourceType(),
	    			trainingView.getAttributesToPredict(),
	    			trainingView.getAttributesToIgnore(),
	    			trainingView.getModelType(),
	    			trainingView.getTrainingDescription(),
	    			trainingView.getPathToOutputModel());
    	} else {
    		conf = new TrainingConfiguration(
	    			trainingView.getProcessingModelString(),
	    			trainingView.getInputFeatureType(),
	    			trainingView.getUnit(),
	    			trainingView.getClassificaitonWindowSize(),
	    			trainingView.getClassificationWindowStepSize(),
	    			trainingView.getSelectedTrainingAlgorithmStr(),
	    			trainingView.getPreprocessingAlgorithmStr(), 
	    			new FileInput(trainingView.getGroundTruthSource()),
	    			trainingView.getGroundTruthSourceType(),
	    			trainingView.getAttributesToPredict(),
	    			trainingView.getAttributesToIgnore(),
	    			trainingView.getModelType(),
	    			trainingView.getTrainingDescription(),
	    			trainingView.getPathToOutputModel());
    	}
        return conf;
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
            trainingView.setProcessingModelString(trainConf.getInputFeaturesDescription());
            trainingView.setPreprocessingAlgorithm(trainConf.getPreprocessingAlgorithmDescription());
            trainingView.setGroundTruthSourceType(trainConf.getGroundTruthSourceType());
            trainingView.setGroundTruthSource(trainConf.getGroundTruthSource().toString());
            trainingView.setAttributesToPredict(trainConf.getAttributesToPredict());
            trainingView.setAttributesToIgnore(trainConf.getAttributesToIgnore());
            trainingView.setModelType(((TrainingConfiguration) conf).getModelType());
            trainingView.setTrainingDescription(trainConf.getTrainingDescription());
            trainingView.setPathToOutputModel(trainConf.getPathToOutputModel());
        }
    }
}
