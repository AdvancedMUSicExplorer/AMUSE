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
 * Creation date: 17.02.2008
 */
package amuse.scheduler.gui.controller;

import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.scheduler.gui.navigation.TitleUpdater;
import amuse.scheduler.gui.views.TaskListener;

import java.io.File;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author Clemens Waeltken
 */
public interface WizardControllerInterface {

    /**
     * Set a new Panel inside the wizard.
     * @param panel
     */
    void setWizardPanel(JPanel panel);

    /**
     * Go to the Freature Processing View.
     */
    void goToFeatureProcessing();
    
    /**
     * Go to the Freature Processing View and restore configuration.
     * @param processorConfigSet The ProcessingConfiguration to restore from.
     */
    void goToFeatureProcessing(ProcessingConfiguration processorConfigSet);

    /**
     * Go to the Freature Extraction View.
     */
    void goToFeatureExtraction();
    /**
     * Go to the Freature Extraction View and restore configuration.
     * @param set The ExtractionConfiguration to restore from.
     */
    void goToFeatureExtraction(ExtractionConfiguration set);

    /**
     * Go to the settings page for the amuse wizard.
     */
    void goToSettings();

    /**
     * Go to the annotation editor (single file) page for the amuse wizard.
     */
    void goToSingleFileAnnotationEditor();
    
    /**
     * Go to the annotation editor (multiple files) page for the amuse wizard.
     */
    void goToMultipleFilesAnnotationEditor();
    
    /**
     * Got to the Experiment Manager.
     * The Experiment Manager allows to configure multiple Experiments.
     */
    void goToExperimentManager();

    /**
     * This method is used by the navbar to update the window title.
     * @return a TitleUpdate object to update the title of the wizard.
     */
    TitleUpdater getTitleUpdater();

    /**
     * Start the amuse wizard.
     */
    void startWizard();

    /**
     * Stop the amuse wizard.
     */
    void closeWizard();

    /**
     * Go to the Training Experiment View.
     */
    void goToTrainingExperiment();
    
    /**
     * Go to the Training Experiment View and restore configuration.
     * @param set The TrainingConfiguration to restore from.
     */
    void goToTrainingExperiment(TrainingConfiguration set);

    /**
     * Go to the Classification View.
     */
    void goToClassification();
    /**
     * Go to the Classification View and restore configuration.
     * @param set The ClassificationConfiguration to restore from.
     */
    void goToClassification(ClassificationConfiguration set);

    /**
     * Go to the Validation View.
     */
    void goToValidation();
    /**
     * Go to the Validation View and restore configuration.
     * @param set The ValidationConfiguration to restore from.
     */
    void goToValidation(ValidationConfiguration set);

    /**
     * Start a List of preconfigured amuse Tasks.
     * @param experiments
     */
    public void startTasks(List<TaskConfiguration> experiments);
    
    /**
     * Saves a List of preconfigured amuse Tasks.
     */
    public void saveTasks(List<TaskConfiguration> experiments, File selectedFile);
    
    /**
     * Go to the Optimization View.
     */
    public void goToOptimization();
    
    /**
     * Go to the Optimization View and restore configuration.
     * @param set The OptimizationConfiguration to restore from.
     */
    public void goToOptimization(OptimizationConfiguration set);
    
    /**
     * Loads the experiments from a file.
     */
	public void loadTasks(File selectedFile);
	
	/*
	 * Adds a task listener that listens to tasks being started and finished
	 */
	public void addTaskListener(TaskListener taskListener);
	
	/*
	 * Notifies the task listeners of the start of a task
	 */
	public void notifyListenersOfStart(TaskConfiguration experiment);
	
	/*
	 * Notifies the task listeners of the finishing of a task
	 */
	public void notifyListenersOfFinish(TaskConfiguration experiment);

}
