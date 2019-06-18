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
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import amuse.data.ModelType;
import amuse.data.datasets.ClassifierConfigSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.ClassificationConfiguration.InputSourceType;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.classifier.ClassifierView;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;

/**
 * @author Clemens Waeltken
 *
 */
public class ClassifierController extends AbstractController {

    private WizardController wizardController;
//    private String[] endings = {"mp3", "wav"};
//    private File musicDatabaseFolder = new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE));
//    private String musicDatabaseLabel = "Music Database";
//    private FileTreeView fileView = new FileTreeView();
//    private FileTreePanel ftPanel = new FileTreePanel(fileView);
//    private FileTreeModel ftModel = new FileTreeModel(musicDatabaseFolder, musicDatabaseLabel, endings);
//    private FileTreeController ftController = new FileTreeController(ftModel, fileView);
    private ClassifierView classifierView;
    private static final File clFolder = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "experiments" + File.separator + "CL");

    public ClassifierController(WizardController wc) {
        this.wizardController = wc;
        this.classifierView = new ClassifierView(this);
        view = new ClassificationPanel(classifierView.getView());
    }

    @Override
    public JComponent getView() {
        return view;
    }

//    private void goToClassifierSetup() {
//        wizardController.setWizardPanel(classifierView);
//    }

    @Override
    public void saveTask(File file) {
        file = addArff(file);
        if (!askOverwrite(file)) {
            return;
        }
        /* Gather all neccessary information and create variables */
        File musicFilesFile = null;
        if(classifierView.getInputSourceType() == InputSourceType.FILE_LIST) {
        	musicFilesFile = new File(file.getParent() + File.separator + "filelists" + File.separator + file.getName());
        } else {
        	musicFilesFile = new File(classifierView.getReadyInputPath());
        }
        String inputSourceType = classifierView.getInputSourceType().toString();
        String processedFeatureDescription = classifierView.getProcessingModelString();
        String algorithmId = classifierView.getSelectedTrainingAlgorithmStr();
        int groundTruthCategoryId = classifierView.getGroundTruthCategoryId();
        int mergeSongResults = 1;
        if (!classifierView.isAverageCalculationSelected()) {
            mergeSongResults = 0;
        }
        String outputResultPath = classifierView.getTargetFilePath();
        String attributesToClassify = classifierView.getAttributesToClassify().toString();
        String attributesToIgnore = classifierView.getAttributesToIgnore().toString();
        String relationshipType = classifierView.getModelType().getRelationshipType().toString();
        String labelType = classifierView.getModelType().getLabelType().toString();
        String methodType = classifierView.getModelType().getMethodType().toString();
        String trainingDescription = classifierView.getTrainingDescription();
        String pathToInputModel = classifierView.getPathToInputModel();
        
        ClassifierConfigSet dataSet = new ClassifierConfigSet(
        		musicFilesFile, 
        		inputSourceType,
        		attributesToIgnore,
        		processedFeatureDescription, 
        		algorithmId, 
        		groundTruthCategoryId,
        		attributesToClassify,
        		relationshipType,
        		labelType,
        		methodType,
        		mergeSongResults,
        		outputResultPath,
        		pathToInputModel,
        		trainingDescription);
        
        //if the input is given as files a file list must be saved
        if(classifierView.getInputSourceType() == InputSourceType.FILE_LIST) {
	        // Create folders...
	        musicFilesFile.getParentFile().mkdirs();
	        DataSetAbstract inputToClassify = null;
	        try {
	        	inputToClassify = classifierView.getInputToClassify();
	        } catch (IOException ex) {
	        	showErr(ex.getLocalizedMessage());
	        }
	        try {
	        	inputToClassify.saveToArffFile(musicFilesFile);
	        } catch(IOException ex) {
	        	showErr(ex.getLocalizedMessage());
	        }
        }
	        
        // Save Files and Features:
        try {
            dataSet.saveToArffFile(file);
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
    	classifierView.setInputSourceType(conf.getInputSourceType());
        classifierView.setProcessingModelString(conf.getProcessedFeaturesModelName());
        classifierView.setSelectedTrainingAlgorithm(conf.getAlgorithmDescription());
        classifierView.setGroundTruthCategoryId(conf.getGroundTruthCategoryId());
        classifierView.setAverageCalculationSelected(conf.getMergeSongResults());
        classifierView.setTargetFilePath(conf.getClassificationOutput());
        classifierView.setAttributesToClassify(conf.getAttributesToClassify());
        classifierView.setAttributesToIgnore(conf.getAttributesToIgnore());
        classifierView.setModelType(conf.getModelType());
        classifierView.setMergeSongResults(conf.getMergeSongResults());
        classifierView.setOutputResult(conf.getClassificationOutput());
        classifierView.setTrainingDescription(conf.getTrainingDescription());
        classifierView.setInputSourceType(conf.getInputSourceType());
    	classifierView.setInputToClassify(conf.getInputToClassify());
        
        if(conf.getPathToInputModel() == null
				|| conf.getPathToInputModel().equals(new String("-1"))) {
        	classifierView.setGroundTruthSourceType("CATEGORY_ID");
        } else {
        	classifierView.setGroundTruthSourceType("MODEL_PATH");
        	classifierView.setPathToInputModel(conf.getPathToInputModel());
        }
        
//        if (conf.getInputToClassify() instanceof FileListInput) {
//            ftController.loadFiles(((FileListInput) conf.getInputToClassify()).getInputFiles());
//        } else {
//            throw new UnsupportedOperationException();
//        }
        
    }

    @Override
    public ClassificationConfiguration getExperimentConfiguration() {
        /* Gather all neccessary information and create variables */
        ClassificationConfiguration conf = null;
        try {
        	File musicFilesFile = null;
        	if(classifierView.getInputSourceType() == InputSourceType.FILE_LIST) {
	            musicFilesFile = File.createTempFile("FileTable", "arff");
	            musicFilesFile.deleteOnExit();
            } else {
            	musicFilesFile = new File(classifierView.getReadyInputPath());
            }
            String inputSourceType = classifierView.getInputSourceType().toString();
            String processedFeatureDescription = classifierView.getProcessingModelString();
            String algorithmStr = classifierView.getSelectedTrainingAlgorithmStr();
            int groundTruthCategoryId = classifierView.getGroundTruthCategoryId();
            List<Integer> attributesToClassify = classifierView.getAttributesToClassify();
            List<Integer> attributesToIgnore = classifierView.getAttributesToIgnore();
            ModelType modelType = classifierView.getModelType();
            int mergeSongResults = 1;
            if (!classifierView.isAverageCalculationSelected()) {
                mergeSongResults = 0;
            }
            String outputResultPath = classifierView.getTargetFilePath();
            String pathToInputModel = classifierView.getPathToInputModel();
            String trainingDescription = classifierView.getTrainingDescription();
            
            //if the input is given as files a file list needs to be saved
            if(classifierView.getInputSourceType() == InputSourceType.FILE_LIST) {
	            //Create folders...
	            musicFilesFile.getParentFile().mkdirs();
	            DataSetAbstract inputToClassify = classifierView.getInputToClassify();
	            inputToClassify.saveToArffFile(musicFilesFile);
            }
            
            // Save Files and Features:
            conf = new ClassificationConfiguration(
            		InputSourceType.valueOf(inputSourceType),
            		musicFilesFile.getAbsolutePath(),
            		attributesToIgnore,
            		processedFeatureDescription, 
            		algorithmStr, 
            		groundTruthCategoryId,
            		attributesToClassify,
            		modelType,
            		mergeSongResults, 
            		outputResultPath,
            		pathToInputModel,
            		trainingDescription);
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

//    private class FileTreePanel extends JPanel implements HasCaption, NextButtonUsable, HasLoadButton {
//
//        private static final long serialVersionUID = 6775206923190367970L;
//
//        /**
//         * @param fileView
//         */
//        public FileTreePanel(FileTreeView fileView) {
//            this.setLayout(new BorderLayout());
//            this.add(fileView.getView(), BorderLayout.CENTER);
//        }
//
//        /* (non-Javadoc)
//         * @see amuse.scheduler.gui.navigation.HasCaption#getCaption()
//         */
//        @Override
//        public String getCaption() {
//            return "Classification Configurator - Select Music Files to Classify";
//        }
//
//        /* (non-Javadoc)
//         * @see amuse.scheduler.gui.navigation.NextButtonUsable#getNextButtonText()
//         */
//        @Override
//        public String getNextButtonText() {
//            return "Setup Classifier";
//        }
//
//        /* (non-Javadoc)
//         * @see amuse.scheduler.gui.navigation.NextButtonUsable#nextButtonClicked()
//         */
//        @Override
//        public boolean nextButtonClicked() {
//            goToClassifierSetup();
//            return false;
//        }
//
//        @Override
//        public String getLoadButtonText() {
//            return "Load";
//        }
//
//        @Override
//        public void loadButtonClicked() {
//            clFolder.mkdirs();
//            JFileChooser fc = new SelectArffFileChooser(
//                    "Classification Task", clFolder);
//            if (fc.showOpenDialog(view) != JFileChooser.APPROVE_OPTION) {
//                return;
//            }
//            File selectedFile = fc.getSelectedFile();
//            try {
//                loadTask(selectedFile);
//            } catch (IOException ex) {
//                showErr(ex.getLocalizedMessage());
//            }
//        }
//    }

    private class ClassificationPanel extends JPanel implements NextButtonUsable,
            HasCaption, HasSaveButton, HasLoadButton {

        /**
         *
         */
        private static final long serialVersionUID = -8208226717664963513L;

        /**
         * @param jComponent
         */
        public ClassificationPanel(JComponent jComponent) {
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
            clFolder.mkdirs();
            JFileChooser fc = new SelectArffFileChooser(
                    "Classificator Training Task", clFolder);
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
        clFolder.mkdirs();
        JFileChooser fc = new SelectArffFileChooser(
                "Classification Training Task", clFolder);
        if (fc.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fc.getSelectedFile();
        saveTask(selectedFile);
    }

    public void addClassification() {
        taskManager.addExperiment(getExperimentConfiguration());
    }
}
