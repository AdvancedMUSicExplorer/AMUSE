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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Level;

import amuse.data.io.ArffDataSet;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.scheduler.SchedulerException;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysBooleanValue;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.Scheduler;
import amuse.scheduler.gui.annotation.singlefile.AnnotationView;
import amuse.scheduler.gui.navigation.TitleUpdater;
import amuse.scheduler.gui.settings.JPanelSettings;
import amuse.scheduler.gui.views.TaskListener;
import amuse.scheduler.gui.views.TaskManagerView;
import amuse.scheduler.gui.views.WizardView;
import amuse.util.AmuseLogger;


/**
 *
 * @author Clemens Waeltken
 */
public final class WizardController implements WizardControllerInterface {

	private static WizardController instance;
	private WizardView wizard;
	private JFrame wizardFrame;
	private final String applicationTitle = "AMUSE Wizard";
	private ExtractionController exController;
	private ProcessingController proController;
	private TrainingController trainController;
	private ClassifierController classifierController;
	private ValidationController validationController;
	private Scheduler scheduler;
	private OptimizationController optimizationController;
	private SingleFileAnnotationController singleFileAnnotationController;
	private MultipleFilesAnnotationController multipleFilesAnnotationController;
	private List<TaskListener> taskListeners = new ArrayList<TaskListener>();
	
	private enum ControllerType {
		CLASSIFICATION, FEATURE_EXTRACTION, OPTIMIZATION, FEATURE_PROCESSING, CLASSIFICATION_TRAINING, VALIDATION;
		
		@Override
		public String toString(){
			switch(this){
			case CLASSIFICATION: return "c";
			case FEATURE_EXTRACTION: return "fe";
			case OPTIMIZATION: return "o";
			case FEATURE_PROCESSING: return "fp";
			case CLASSIFICATION_TRAINING: return "ct";
			case VALIDATION: return "v";
			default: throw new IllegalArgumentException("Unsupported Controller Type");
			}
		}
		
		public AbstractController getController(){
			switch(this){
			case CLASSIFICATION: return new ClassifierController(instance);
			case FEATURE_EXTRACTION: return new ExtractionController(instance);
			case OPTIMIZATION: return new OptimizationController(instance);
			case FEATURE_PROCESSING: return new ProcessingController(instance);
			case CLASSIFICATION_TRAINING: return new TrainingController(instance);
			case VALIDATION: return new ValidationController(instance);
			default: throw new IllegalArgumentException("Unsupported Controller Type");
			}
		}
		
		public static ControllerType valueOf(TaskConfiguration config){
			if(config instanceof ClassificationConfiguration){
				return CLASSIFICATION;
			}
			else if(config instanceof ExtractionConfiguration){
				return FEATURE_EXTRACTION;
			}
			else if(config instanceof OptimizationConfiguration){
				return OPTIMIZATION;
			}
			else if(config instanceof ProcessingConfiguration){
				return FEATURE_PROCESSING;
			}
			else if(config instanceof TrainingConfiguration){
				return CLASSIFICATION_TRAINING;
			}
			else if(config instanceof ValidationConfiguration){
				return VALIDATION;
			}
			else{
				return null;
			}
		}

	};

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
		
		// check amuse workspace
		if(!AmusePreferences.getBoolean(KeysBooleanValue.ADVANCED_PATHS) && !new File(AmusePreferences.get(KeysStringValue.WORKSPACE)).exists()) {
			int result = JOptionPane.showConfirmDialog(wizardFrame, "The path to AMUSE workspace is not set properly.\nDo you want to go to the settings to chose an AMUSE\nworkspace and set all paths and folders accordingly?", "Workspace is not set", JOptionPane.YES_NO_OPTION);
    		if(result == JOptionPane.YES_OPTION){
    			goToSettings();
    		}
		}
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
			wizardFrame.setTitle(suffix);
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
						notifyListenersOfStart(task);
						scheduler.proceedTask(task);
						notifyListenersOfFinish(task);
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
	public void goToSingleFileAnnotationEditor(){
		if(singleFileAnnotationController == null){
			singleFileAnnotationController = new SingleFileAnnotationController(instance);
		}
		wizard.showInWizardPane(singleFileAnnotationController.getView());
		if(singleFileAnnotationController.getMusicFilePath().equals("")){
			((AnnotationView) singleFileAnnotationController.getView()).loadButtonClicked();
		}

	}
	
	@Override
	public void goToMultipleFilesAnnotationEditor(){
		if(multipleFilesAnnotationController == null){
			multipleFilesAnnotationController = new MultipleFilesAnnotationController(instance);
		}
		wizard.showInWizardPane(multipleFilesAnnotationController.getView());
	}

	@Override
	public void saveTasks(List<TaskConfiguration> experiments, File selectedFile) {
		selectedFile.getParentFile().mkdirs();
		String tasksFolderPath = selectedFile.getParentFile().getAbsolutePath()
				+ File.separator
				+ "tasks"
				+ File.separator
				+ selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.'))
				+ File.separator;
		
		// Remove old files?
		//TODO
		
		new File(tasksFolderPath).mkdirs();
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(selectedFile.getAbsolutePath());
			
			writer.write("@RELATION 'AMUSE Tasks'\n"
						+ "%rows=" + experiments.size() + "\n"
						+ "%columns=2\n"
						+ "@ATTRIBUTE Type {" + Arrays.stream(ControllerType.values()).map(String::valueOf).collect(Collectors.joining(",")) + "}\n"
						+ "@ATTRIBUTE Path STRING\n\n"
						+ "@DATA\n");
			
			int taskNumber = 0;
			for(TaskConfiguration exp: experiments){
				
				ControllerType type = ControllerType.valueOf(exp);
				if(type == null){
					AmuseLogger.write(this.getClass().toString(), Level.ERROR, "");
					return;
				}
				String pathToConfigArff = tasksFolderPath
						+ "task_"
						+ taskNumber
						+ "_"
						+ type
						+ ".arff";
				AbstractController controller = type.getController();
				controller.loadTask(exp); 
				controller.saveTask(new File(pathToConfigArff)); // TODO Extraction Controller shows a dialog after saving.
				writer.write(type + ",'" + pathToConfigArff + "'\n");
				taskNumber++;
			}
			
		}
		catch(FileNotFoundException e){
			AmuseLogger.write(this.getClass().getName(),
					Level.ERROR,
					"Failed to save the experiments.");
		}
		finally{
			if(writer != null){
				writer.close();
			}
		}
		
	}

	@Override
	public void loadTasks(File selectedFile) {
		TaskManagerView taskManager = TaskManagerView.getInstance();
		try {
			ArffDataSet arffDataSet = new ArffDataSet(selectedFile);
			NominalAttribute typeAtt = (NominalAttribute) arffDataSet.getAttribute("Type");
			StringAttribute pathAtt = (StringAttribute) arffDataSet.getAttribute("Path");
			for(int i = 0; i < pathAtt.getValueCount(); i++){
				ControllerType type = null;
				String typeStr = typeAtt.getValueAt(i);
				switch(typeStr) {
				case "c":
					type = ControllerType.CLASSIFICATION;
					break;
				case "fe":
					type = ControllerType.FEATURE_EXTRACTION;
					break;
				case "o":
					type = ControllerType.OPTIMIZATION;
					break;
				case "fp":
					type = ControllerType.FEATURE_PROCESSING;
					break;
				case "ct":
					type = ControllerType.CLASSIFICATION_TRAINING;
					break;
				case "v":
					type = ControllerType.VALIDATION;
					break;
				default:
					type = ControllerType.valueOf(typeAtt.getValueAt(i));
				}
				AbstractController controller = type.getController();
				controller.loadTask(new File(pathAtt.getValueAt(i)));
				TaskConfiguration configuration = controller.getExperimentConfiguration();
				taskManager.addExperiment(configuration);
			}
		} catch (IOException e) {
			AmuseLogger.write(this.getClass().getName(),
					Level.ERROR,
					"Failed to load the experiments.");
		}
	}
	
	public void addTaskListener(TaskListener taskListener) {
		taskListeners.add(taskListener);
	}
	
	public void notifyListenersOfStart(TaskConfiguration experiment) {
		for(TaskListener listener : taskListeners) {
			listener.experimentStarted(experiment);
		}
	}
	
	public void notifyListenersOfFinish(TaskConfiguration experiment) {
		for(TaskListener listener : taskListeners) {
			listener.experimentFinished(experiment);
		}
	}
}
