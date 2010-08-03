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
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author Clemens Waeltken
 */
public interface WizardControllerInterface {

    void setWizardPanel(JPanel panel);

    void goToFeatureProcessing();
    void goToFeatureProcessing(ProcessingConfiguration processorConfigSet);

    void goToFeatureExtraction();
    void goToFeatureExtraction(ExtractionConfiguration set);

    void goToSettings();

    void goToExperimentManager();

    TitleUpdater getTitleUpdater();

    void startWizard();

    void closeWizard();

    void goToTrainingExperiment();
    void goToTrainingExperiment(TrainingConfiguration set);

    void goToClassification();
    void goToClassification(ClassificationConfiguration set);

    void goToValidation();
    void goToValidation(ValidationConfiguration set);

    public void startTasks(List<TaskConfiguration> experiments);

    public void goToOptimization();
    public void goToOptimization(OptimizationConfiguration set);

}
