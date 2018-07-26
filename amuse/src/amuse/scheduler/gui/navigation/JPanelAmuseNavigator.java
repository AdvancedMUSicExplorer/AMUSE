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
 * Creation date: 10.08.2008
 */
package amuse.scheduler.gui.navigation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author Clemens Waeltken
 * 
 */
public class JPanelAmuseNavigator extends JPanel implements NavigatorInterface {

    private static final long serialVersionUID = -7503455700344123774L;
    private final Stack<JComponent> previousPanels;
    private final Stack<JComponent> nextPanels;
    private final JComponent navigationPanel;
    private JButton backButton;
    private JButton nextButton;
    private final TitleUpdater titleUpdater;
    private JButton loadButton;
    private JButton saveButton;

    public JPanelAmuseNavigator(JComponent tabbedPane, TitleUpdater titleUpdater) {
        this.previousPanels = new Stack<JComponent>();
        this.nextPanels = new Stack<JComponent>();
        this.navigationPanel = tabbedPane;
        BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
        this.setLayout(layout);
        this.add(getBackButton());
        this.add(Box.createHorizontalGlue());
        this.add(getLoadButton());
        this.add(getSaveButton());
        this.add(Box.createHorizontalGlue());
        this.add(getForwardButton());
        this.titleUpdater = titleUpdater;
    }

    private Component getForwardButton() {
        if (nextButton == null) {
            nextButton = new JButton();
            nextButton.setText("Forward");
            nextButton.setEnabled(false);
            nextButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    nextButtonBehavior();
                }
            });
        }
        return nextButton;
    }

    private Component getBackButton() {
        if (backButton == null) {
            backButton = new JButton();
            backButton.setText("Back");
            backButton.setEnabled(false);
            backButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    backButtonBehavior();
                }
            });
        }
        return backButton;
    }

    private Component getLoadButton() {
        if (loadButton == null) {
            loadButton = new JButton();
            loadButton.setText("Load");
            loadButton.setEnabled(false);
            loadButton.setVisible(false);
            loadButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    loadButtonBehavior();
                }
            });
        }
        return loadButton;
    }

    private Component getSaveButton() {
        if (saveButton == null) {
            saveButton = new JButton();
            saveButton.setText("Save");
            saveButton.setEnabled(false);
            saveButton.setVisible(false);
            saveButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    saveButtonBehavior();
                }
            });
        }
        return saveButton;
    }

    @Override
    public void setNewContent(JComponent panel) {
        previousPanels.push(getPanel());
        setPanel(panel);
        getBackButton().setEnabled(true);
        // If new path is chosen delete next stack.
        if (!nextPanels.isEmpty()) {
            if (nextPanels.peek() != panel) {
                nextPanels.clear();
            } else if (nextPanels.peek() == panel) {
                nextPanels.pop();
            }
        }
        // If same panel was on previous stack retrack to that...
        while (previousPanels.contains(panel))
            previousPanels.pop();
        updateDisplayedTexts();
    }

    private void updateDisplayedTexts() {
        updateNextButton();
        updateBackButton();
        updateLoadButton();
        updateSaveButton();
        updateTitle();
    }

    private JComponent getPanel() {
        return (JComponent) navigationPanel.getComponent(0);
    }

    private void setPanel(JComponent component) {
        navigationPanel.remove(0);
        navigationPanel.add(component, BorderLayout.CENTER, 0);
        navigationPanel.repaint();
    }

    private void updateNextButton() {
        if (getPanel() instanceof NextButtonUsable) {
            // The current panels behavior.
            NextButtonUsable nextButtonUsingPanel = (NextButtonUsable) getPanel();
            nextButton.setText(nextButtonUsingPanel.getNextButtonText());
            nextButton.setEnabled(true);
        } else {
            // Standard behavior.
            nextButton.setText("Forward");
            if (nextPanels.isEmpty()) {
                nextButton.setEnabled(false);
            }
        }
    }

    private void updateBackButton() {
        if (getPanel() instanceof BackButtonUsable) {
            // The current panels behavior.
            BackButtonUsable backButtonUsingPanel = (BackButtonUsable) getPanel();
            backButton.setText(backButtonUsingPanel.getBackButtonText());
            backButton.setEnabled(true);
        } else {
            // Standard behavior.
            backButton.setText("Back");
            if (previousPanels.isEmpty()) {
                backButton.setEnabled(false);
            }
        }
    }

    private void updateLoadButton() {
        if (getPanel() instanceof HasLoadButton) {
            HasLoadButton loadButtonUsingPanel = (HasLoadButton) getPanel();
            loadButton.setVisible(true);
            loadButton.setEnabled(true);
            loadButton.setText(loadButtonUsingPanel.getLoadButtonText());

        } else {
            loadButton.setVisible(false);
            loadButton.setEnabled(false);
            loadButton.setText("Load");
        }
    }

    private void updateSaveButton() {
        if (getPanel() instanceof HasSaveButton) {
            HasSaveButton saveButtonUsingPanel = (HasSaveButton) getPanel();
            saveButton.setVisible(true);
            saveButton.setEnabled(true);
            saveButton.setText(saveButtonUsingPanel.getSaveButtonText());

        } else {
            saveButton.setVisible(false);
            saveButton.setEnabled(false);
            saveButton.setText("Save");
        }
    }

    private void nextButtonBehavior() {
        // If the current panel has special behavior for this button use it.
        if (getPanel() instanceof NextButtonUsable) {
            NextButtonUsable nextButtonUsingPanel = (NextButtonUsable) getPanel();
            if (nextButtonUsingPanel.nextButtonClicked()) {
                goForward();
            }
        } else {
            // If not go forward to a visited panel.
            goForward();
        }
    }

    @Override
    public void goForward() {
        // Check preconditions:
        if (nextPanels.isEmpty()) {
            throw new IllegalStateException("No next Panel available!");
        }
        // Remember where we have been.
        previousPanels.push(getPanel());
        // Enable the back Button.
        backButton.setEnabled(true);
        // SetPanel with top of next panels.
        setPanel(nextPanels.pop());
        // If last forward panel is reached disable this button.
        if (nextPanels.isEmpty()) {
            nextButton.setEnabled(false);
        }
        updateDisplayedTexts();
    }

    private void backButtonBehavior() {
        // If the current panel has special behavior for this button use it.
        if (getPanel() instanceof BackButtonUsable) {
            BackButtonUsable backButtonUsingPanel = (BackButtonUsable) getPanel();
            if (backButtonUsingPanel.backButtonClicked()) {
                goBack();
            }

        } else {
            // If not go backward to a visited panel.
            goBack();
        }
    }

    private void loadButtonBehavior() {
        if (getPanel() instanceof HasLoadButton) {
            HasLoadButton loadButtonUsingPanel = (HasLoadButton) getPanel();
            loadButtonUsingPanel.loadButtonClicked();
        }
    }

    private void saveButtonBehavior() {
        if (getPanel() instanceof HasSaveButton) {
            HasSaveButton loadButtonUsingPanel = (HasSaveButton) getPanel();
            loadButtonUsingPanel.saveButtonClicked();
        }
    }

    @Override
    public void goBack() {
        // Check preconditions:
        if (previousPanels.isEmpty()) {
            throw new IllegalStateException("No previous Panel available!");
        }
        // Remember where we have been.
        nextPanels.push(getPanel());
        // Enable the forward Button
        nextButton.setEnabled(true);
        // Set Panel with top of previous panels.
        setPanel(previousPanels.pop());
        // If last back panel is reached disable this button.
        if (previousPanels.isEmpty()) {
            backButton.setEnabled(false);
        }
        updateDisplayedTexts();
    }

    private void updateTitle() {
        if (getPanel() instanceof HasCaption) {
            // The current panels caption.
            HasCaption panel = (HasCaption) getPanel();
            titleUpdater.setTitleSuffix(panel.getCaption());
        } else {
            titleUpdater.setTitleSuffix("");
        }
    }

    @Override
    public JPanel getNavBar() {
        return this;
    }
}
