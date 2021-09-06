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
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;


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

    JPanelAmuseLogger scrollableLogger;

    JPanel newLoadPreferencesButtons;

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
		
		JPanel startPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("What are you going to do?");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		startPanel.add(label, BorderLayout.NORTH);
		
		JPanel startButtonsPanel = new JPanel();
		startButtonsPanel.setLayout(new BoxLayout(startButtonsPanel, BoxLayout.LINE_AXIS));
		startPanel.add(startButtonsPanel, BorderLayout.CENTER);
		
		// Panel for experiment buttons
		JPanel experimentsPanel = new JPanel();
		experimentsPanel.setLayout(new BoxLayout(experimentsPanel, BoxLayout.PAGE_AXIS));
		experimentsPanel.setBorder(new TitledBorder("Experiments"));
		
		// Adding buttons to the main screen
		JButton buttonCreateExperiment = new JButton("Create AMUSE Experiment");
		buttonCreateExperiment.addActionListener(e -> showCreateExperimentPane());

	    JButton buttonLoadExperiment = new JButton("Load AMUSE Experiment");
	    buttonLoadExperiment.addActionListener(e -> {
	    	TaskManagerView.getInstance().loadButtonClicked();
	    	buttonCreateExperiment.doClick();
	    });
	    
	    JButton buttonAmuseSettings = new JButton("Edit AMUSE Settings");
	    buttonAmuseSettings.addActionListener(e -> wizardController.goToSettings());

	    // Panel for annotation buttons
		JPanel annotationPanel = new JPanel();
		annotationPanel.setLayout(new BoxLayout(annotationPanel, BoxLayout.PAGE_AXIS));
		annotationPanel.setBorder(new TitledBorder("Annotation"));
		
	    JButton buttonSingleAnnotation = new JButton("Create Single Track Annotation"); 
	    buttonSingleAnnotation.addActionListener(e -> wizardController.goToSingleFileAnnotationEditor());
	    
	    JButton buttonMultipleAnnotation = new JButton("Create Multiple Tracks Annotation");
	    buttonMultipleAnnotation.addActionListener(e -> wizardController.goToMultipleFilesAnnotationEditor());

	    // Adding all together
	    final int panelWidth = 250;

	    buttonCreateExperiment.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonLoadExperiment.setAlignmentX(Component.CENTER_ALIGNMENT);
	    experimentsPanel.add(Box.createRigidArea(new Dimension(5, 5)));
	    experimentsPanel.add(buttonCreateExperiment);
	    experimentsPanel.add(Box.createRigidArea(new Dimension(5, 5)));
	    experimentsPanel.add(buttonLoadExperiment);
	    experimentsPanel.add(Box.createRigidArea(new Dimension(panelWidth, 5)));

	    buttonSingleAnnotation.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonMultipleAnnotation.setAlignmentX(Component.CENTER_ALIGNMENT);
	    annotationPanel.add(Box.createRigidArea(new Dimension(5, 5)));
	    annotationPanel.add(buttonSingleAnnotation);
	    annotationPanel.add(Box.createRigidArea(new Dimension(5, 5)));
	    annotationPanel.add(buttonMultipleAnnotation);
	    annotationPanel.add(Box.createRigidArea(new Dimension(panelWidth, 5)));
	    
	    startButtonsPanel.add(Box.createHorizontalGlue());
	    startButtonsPanel.add(buttonAmuseSettings);
	    startButtonsPanel.add(Box.createHorizontalGlue());
	    startButtonsPanel.add(annotationPanel);
	    startButtonsPanel.add(Box.createHorizontalGlue());
	    startButtonsPanel.add(experimentsPanel);
	    startButtonsPanel.add(Box.createHorizontalGlue());
	    
	    
	    // Create New Experiment ButtonPanel:
	    JButton buttonFeatureExtraction = new JButton("Feature Extraction");
	    buttonFeatureExtraction.addActionListener(e -> wizardController.goToFeatureExtraction());
	    newExperimentButtons.addButton(buttonFeatureExtraction);

	    JButton buttonFeatureProcessing = new JButton("Feature Processing");
	    buttonFeatureProcessing.addActionListener(e -> wizardController.goToFeatureProcessing());
	    newExperimentButtons.addButton(buttonFeatureProcessing);
	    
	    JButton buttonTraining = new JButton("Classification Training");
	    buttonTraining.addActionListener(e -> wizardController.goToTrainingExperiment());
	    newExperimentButtons.addButton(buttonTraining);

	    JButton buttonClassifier = new JButton("Classification");
	    buttonClassifier.addActionListener(e -> wizardController.goToClassification());
	    newExperimentButtons.addButton(buttonClassifier);
	    
	    JButton buttonValidation = new JButton("Classification Validation");
	    newExperimentButtons.addButton(buttonValidation);
	    buttonValidation.addActionListener(e -> wizardController.goToValidation());
		

        // Initialise WizardView Panel with New/Load/Preferences:
        contentArea = new JPanel(new BorderLayout());
        contentArea.add(startPanel, BorderLayout.CENTER);
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

    

	/**
	 * 
	 */
	public void goBack() {
		navigator.goBack();
	}
}
