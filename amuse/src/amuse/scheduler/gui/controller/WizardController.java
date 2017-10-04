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
import amuse.interfaces.scheduler.SchedulerException;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.preferences.*;
import amuse.scheduler.Scheduler;
import amuse.scheduler.gui.navigation.TitleUpdater;
import amuse.scheduler.gui.settings.JPanelSettings;
import amuse.scheduler.gui.views.TaskManagerView;
import amuse.scheduler.gui.views.WizardView;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 *
 * @author Clemens Waeltken
 */
public final class WizardController implements WizardControllerInterface {

	private static WizardController instance;
	private WizardView wizard;
	private JFrame wizardFrame;
	private final String applicationTitle = "Amuse Wizard";
	private ExtractionController exController;
	private ProcessingController proController;
	private TrainingController trainController;
	private ClassifierController classifierController;
	private ValidationController validationController;
	private Scheduler scheduler;
	private OptimizationController optimizationController;
	//private AnnotationController annotationController;

	private WizardController() {
		instance = this;
	}

	/**
	 * Singleton Pattern.
	 * Create or get the current WizardController.
	 * @return the current WizardController instance.
	 */
	public static synchronized WizardController getInstance() {
		if (instance == null) {
			instance = new WizardController();
		}
		return instance;
	}
	@Override
	public void startWizard() {
		wizard = new WizardView(this);
		wizardFrame = new JFrame(applicationTitle);
		wizardFrame.add(wizard.getContentPane());
		wizardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setFrameSizeAndPosition();
		wizardFrame.setVisible(true);
		scheduler = Scheduler.getInstance();
	}

	@Override
	public void closeWizard() {
	}

	private void setFrameSizeAndPosition() {
		// Get the screen size
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		// Set the window size
		double proportions = 16. / 9.;
		wizardFrame.setSize((int) (Math.min((int) screenSize.width * 0.75, screenSize.height * 0.75 * proportions)),
				(int) Math.min((int) screenSize.height * 0.75, (int) screenSize.width * 0.75 / proportions));
		int x = (screenSize.width - wizardFrame.getWidth()) / 2;
		int y = (screenSize.height - wizardFrame.getHeight()) / 2;
		wizardFrame.setLocation(x, y);
	}

	/**
	 *
	 */
	@Override
	public void goToSettings() {
		wizard.showInWizardPane(JPanelSettings.getInstance());
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		new WizardController().startWizard();
	}

	private void setTitleSuffix(String suffix) {
		if (suffix.equals("")) {
			wizardFrame.setTitle(applicationTitle);
		} else {
			wizardFrame.setTitle(applicationTitle + " - " + suffix);
		}
	}

	/**
	 *
	 * @return
	 */
	@Override
	public TitleUpdater getTitleUpdater() {
		return new TitleUpdater() {

			@Override
			public void setTitleSuffix(String suffix) {
				instance.setTitleSuffix(suffix);
			}
		};
	}

	/**
	 *
	 */
	@Override
	public void goToFeatureProcessing() {
		goToFeatureProcessing(null);
	}

	/**
	 *
	 * @param conf
	 */
	@Override
	public void goToFeatureProcessing(ProcessingConfiguration conf) {
		proController = new ProcessingController(instance);
		if (conf != null) {
			proController.loadTask(conf);
		}
		wizard.showInWizardPane(proController.getView());
	}

	/**
	 *
	 */
	@Override
	public void goToFeatureExtraction() {
		goToFeatureExtraction(null);
	}

	/**
	 *
	 * @param set
	 */
	@Override
	public void goToFeatureExtraction(ExtractionConfiguration set) {
		exController = new ExtractionController(instance);
		if (set != null) {
			exController.loadTask(set);
		}
		wizard.showInWizardPane(exController.getView());
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.controller.WizardControllerInterface#goToTrainingExperiment()
	 */
	/**
	 *
	 */
	@Override
	public void goToTrainingExperiment() {
		goToTrainingExperiment(null);
	}

	/**
	 *
	 * @param set
	 */
	@Override
	public void goToTrainingExperiment(TrainingConfiguration set) {
		trainController = new TrainingController(instance);
		if (set != null) {
			trainController.loadTask(set);
		}
		wizard.showInWizardPane(trainController.getView());
	}

	/**
	 *
	 * @param panel
	 */
	@Override
	public void setWizardPanel(JPanel panel) {
		wizard.showInWizardPane(panel);
	}

	/**
	 *
	 * @return
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 *
	 */
	public void goToLogger() {
		wizard.switchToConsole();
	}

	/**
	 *
	 */
	public void goBack() {
		wizard.goBack();
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.controller.WizardControllerInterface#gotoClassification()
	 */
	/**
	 *
	 */
	@Override
	public void goToClassification() {
		goToClassification(null);
	}

	@Override
	public void goToClassification(ClassificationConfiguration set) {
		classifierController = new ClassifierController(instance);
		if (set != null) {
			classifierController.loadTask(set);
		}
		wizard.showInWizardPane(classifierController.getView());
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.controller.WizardControllerInterface#goToValidation()
	 */
	@Override
	public void goToValidation() {
		goToValidation(null);
	}

	@Override
	public void goToValidation(ValidationConfiguration set) {
		validationController = new ValidationController(instance);
		if (set != null) {
			validationController.loadTask(set);
		}
		wizard.showInWizardPane(validationController.getView());
	}

	@Override
	public void goToExperimentManager() {
		wizard.showInWizardPane(TaskManagerView.getInstance());
	}

	@Override
	public void startTasks(final List<TaskConfiguration> experiments) {
		goToLogger();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				for (TaskConfiguration task : experiments) {
					try {
						scheduler.proceedTask(task);
					} catch (SchedulerException ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		});
		thread.start();
	}

	@Override
	public void goToOptimization() {
		goToOptimization(null);
	}

	@Override
	public void goToOptimization(OptimizationConfiguration set) {
		optimizationController = new OptimizationController(instance);
		if (set != null) {
			optimizationController.loadTask(set);
		}
		wizard.showInWizardPane(optimizationController.getView());
	}

	@Override
	public void goToAnnotationEditor(){
		//annotationController = new AnnotationController(instance);
		//wizard.showInWizardPane(annotationController.getView());

	}
}
