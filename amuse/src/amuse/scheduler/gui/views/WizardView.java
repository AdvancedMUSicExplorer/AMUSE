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
 **/

package amuse.scheduler.gui.views;

import amuse.scheduler.gui.controller.*;
import amuse.scheduler.gui.logger.JPanelAmuseLogger;
import amuse.scheduler.gui.navigation.ButtonPanelInterface;
import amuse.scheduler.gui.navigation.JButtonPanelWithHeading;
import amuse.scheduler.gui.navigation.JPanelAmuseNavigator;
import amuse.scheduler.gui.navigation.NavigatorInterface;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;


/**
 *
 * @author Clemens Waeltken
 */
public class WizardView {

    public static final Color INVALID_COLOR = new Color(150, 0, 0);

    public static final Color VALID_COLOR = new Color(0, 110, 0);

    WizardControllerInterface wizardController;

    JPanel overallLayoutPanel;

    JPanel wizardPanel;

    NavigatorInterface navigator;

    JTabbedPane tabbedPane;

    JButton buttonCreateExperiment = new JButton("Create Amuse Experiments");

    JButton buttonLoadExperiment = new JButton("Load Amuse Experiment");

    JButton buttonAmuseSettings = new JButton("Edit Amuse Settings");

    JButton buttonFeatureExtraction = new JButton("Feature Extraction");

    JButton buttonFeatureProcessing = new JButton("Feature Processing");
    
    JButton buttonTraining = new JButton("Classification Training");

    JButton buttonClassifier = new JButton("Classification");
    
    JButton buttonValidation = new JButton("Classification Validation");
    
    JButton buttonAnnotation = new JButton("Create Annotation"); //TODO better name?

    JPanelAmuseLogger scrollableLogger;

    ButtonPanelInterface newLoadPreferencesButtons = new JButtonPanelWithHeading("What are you going to do?");

    ButtonPanelInterface newExperimentButtons = new JButtonPanelWithHeading("What kind of experiment?");

    JPanel contentArea;

    public WizardView(WizardControllerInterface wizardController) {
        this.wizardController = wizardController;

        // Create Main WizardView Panel:
        overallLayoutPanel = new JPanel();
        BorderLayout layout = new BorderLayout();
        overallLayoutPanel.setLayout(layout);

        // CreatscrollableLoggere Scrollable Logger Pane:
	scrollableLogger = new JPanelAmuseLogger();
	scrollableLogger.startListening();

        // Create New/Load/Settings ButtonPanel:
        buttonCreateExperiment.addActionListener(new CreateExprimentListener());
        newLoadPreferencesButtons.addButton(buttonCreateExperiment);
        buttonLoadExperiment.addActionListener(new LoadExperimentListener());
        buttonLoadExperiment.setEnabled(false);
        newLoadPreferencesButtons.addButton(buttonLoadExperiment);
        buttonAmuseSettings.addActionListener(new AmuseSettingsListener());
        newLoadPreferencesButtons.addButton(buttonAmuseSettings);
        
        buttonAnnotation.addActionListener(new AnnotationListener());
        newLoadPreferencesButtons.addButton(buttonAnnotation);

        // Create New Experiment ButtonPanel:
        buttonFeatureExtraction.addActionListener(new FeatureExtractionListener());
        newExperimentButtons.addButton(buttonFeatureExtraction);
        buttonFeatureProcessing.addActionListener(new FeatureProcessingListener());
        newExperimentButtons.addButton(buttonFeatureProcessing);
        buttonTraining.addActionListener(new TrainingExperimentListener());
        newExperimentButtons.addButton(buttonTraining);
        newExperimentButtons.addButton(buttonClassifier);
        buttonClassifier.addActionListener(new ClassifierButtonListener());
        newExperimentButtons.addButton(buttonValidation);
        buttonValidation.addActionListener(new ValidationButtonListener());

        // Initialise WizardView Panel with New/Load/Preferences:
        contentArea = new JPanel(new BorderLayout());
        contentArea.add(newLoadPreferencesButtons.getPanel(), BorderLayout.CENTER);
        wizardPanel = new JPanel(new BorderLayout());
        wizardPanel.add(contentArea, BorderLayout.CENTER);
        // Create TabbedPane for Log and WizardView:
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane.addTab("Wizard", null, wizardPanel, null);
        tabbedPane.addTab("Log", null, scrollableLogger, null);

        // Create Navigator:
        navigator = new JPanelAmuseNavigator(contentArea, wizardController.getTitleUpdater());

        // Add initialised conted to Overall Layout:
        overallLayoutPanel.add(tabbedPane, BorderLayout.CENTER);
        wizardPanel.add(navigator.getNavBar(), BorderLayout.NORTH);
        navigator.getNavBar().setBorder(new TitledBorder(""));
        //overallLayoutPanel.add(navigator.getNavBar(), BorderLayout.NORTH);
    }

    private void showCreateExperimentPane() {
        navigator.setNewContent(TaskManagerView.getInstance());
    }

    public void showInWizardPane(JComponent component) {
        navigator.setNewContent(component);
    }
    
    /**
     *  Switch AmuseWizard to Logger.
     */
    public void switchToConsole() {
        tabbedPane.setSelectedComponent(scrollableLogger);
    }

    public JPanel getContentPane() {
        return overallLayoutPanel;
    }

    
	private final class ValidationButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			wizardController.goToValidation();
		}
	}

	private final class ClassifierButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			wizardController.goToClassification();
		}
	}

	/**
     * Listeners for Buttons:
     */
    private class CreateExprimentListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            showCreateExperimentPane();
        }
    }

    private class LoadExperimentListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not yet implemented1");
        }
    }

    private class AmuseSettingsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            wizardController.goToSettings();
        }
    }
    
    private class AnnotationListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            wizardController.goToAnnotationEditor();
        }
    }

    private class FeatureExtractionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            wizardController.goToFeatureExtraction();
        }
    }

    private class FeatureProcessingListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            wizardController.goToFeatureProcessing();
        }
    }
    

	private class TrainingExperimentListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			wizardController.goToTrainingExperiment();
		}
	}


	/**
	 * 
	 */
	public void goBack() {
		navigator.goBack();
	}
}
