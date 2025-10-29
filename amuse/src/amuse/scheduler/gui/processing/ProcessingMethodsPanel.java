/*
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
 * Creation date: 11.03.2009
 */

/*
 * ProcessingMethodsPanel.java
 *
 * Created on 11.03.2009, 19:17:41
 */

package amuse.scheduler.gui.processing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import amuse.scheduler.gui.processing.ProcessingMethodsListModel.ProcessingAlgorithm;
import amuse.scheduler.gui.views.WizardView;

/**
 * 
 * @author Clemens Waeltken
 */
public class ProcessingMethodsPanel extends javax.swing.JPanel implements
		NextButtonUsable, HasCaption, HasSaveButton {

	private static final long serialVersionUID = -1903561725561883507L;
	private ProcessingMethodsListModel pcmListModel;
	private MatrixToVectorMethodModel mtvModel;
	private ProcessingMethodsController controller;
	private Color validColor = WizardView.VALID_COLOR;
	private Color invalidColor = WizardView.INVALID_COLOR;

	private JSplitPane splitPane;
	private JList pcmJList;
	private JPopupMenu popupMenu = new JPopupMenu();
	private JComboBox mtvComboBox;
	private JPanel leftSide;
	private JPanel leftUpper;
	private JPanel leftLower;
	private JPanel matrixToVectorPanel;
	private JPanel globalSettingsPanel;
	private JTextField classificationWindowSize;
	private JTextField classificationWindowStepSize;
	private JButton addButton = new JButton("Add");
	private JButton removeButton = new JButton("Remove");
	private JButton upButton = new JButton("Up");
	private JButton downButton = new JButton("Down");
	private JTextField modelDescriptionTextField = new JTextField(30);
    private JComboBox<Unit> boxUnit;

	/** Creates new form ProcessingMethodsPanel */
	public ProcessingMethodsPanel(ProcessingMethodsListModel pcmListModel,
			MatrixToVectorMethodModel mtvModel,
			ProcessingMethodsController controller) {
		super(new BorderLayout());
		this.pcmListModel = pcmListModel;
		this.mtvModel = mtvModel;
		this.controller = controller;
		initComponents();
		initPopupMenu();
	}

	private void initPopupMenu() {
		popupMenu = new JPopupMenu();
		popupMenu.setInvoker(addButton);
		JMenu submenu;
		JMenuItem item;
		for (String category : pcmListModel.getCategories()) {
			submenu = new JMenu(category);
			for (Object al : pcmListModel.getAvailableAlgorithms()) {
				ProcessingAlgorithm algo = (ProcessingAlgorithm) al;
				if (algo.getCategory().equalsIgnoreCase(category)) {
					item = new JMenuItem(algo.getName());
					item.addActionListener(new MenuAlgorithmAction(algo));
					submenu.add(item);
				}
			}
			if (submenu.getItemCount() > 0) {
				popupMenu.add(submenu);
			}
		}
	}

	/**
	 * Initialize GUI Components
	 */
	private void initComponents() {
		splitPane = new JSplitPane();
		this.add(splitPane, BorderLayout.CENTER);
		setRightSide(new JPanel());
		leftSide = new JPanel(new BorderLayout());
		splitPane.add(leftSide, JSplitPane.LEFT);
		leftUpper = new JPanel(new MigLayout("nogrid, fillx, ins 5"));
		leftLower = new JPanel(new BorderLayout());
		leftUpper.setBorder(new TitledBorder("Selected Processing Algorithms"));
		leftSide.add(leftUpper, BorderLayout.CENTER);
		leftSide.add(leftLower, BorderLayout.SOUTH);
		// Setup Left Upper
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showPopupMenu();
			}
		});
		leftUpper.add(addButton, "");
		removeButton.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent e) {
				removeSelectedAlgorithms();
			}
		});
		leftUpper.add(removeButton, "gap unrel:push, wrap");
		pcmJList = new JList(pcmListModel);
		pcmJList.addKeyListener(new KeyListener() {
		
			@Override
			public void keyTyped(KeyEvent e) {
		
			}
		
			@Override
			public void keyReleased(KeyEvent e) {
			}
		
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeSelectedAlgorithms();
				}
			}
		});
		pcmJList.addListSelectionListener(controller.getSelectionListener());
		leftUpper.add(new JScrollPane(pcmJList), "w max, h max, wrap");
		upButton.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.moveUp(pcmJList.getSelectedIndex());
			}
		});
		downButton.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.moveDown(pcmJList.getSelectedIndex());
			}
		});
		leftUpper.add(upButton, "align right");
		leftUpper.add(downButton, "gap rel");
		// Setup Left Lower
		matrixToVectorPanel = new JPanel(new BorderLayout());
		globalSettingsPanel = new JPanel(new MigLayout("ins 5"));
		matrixToVectorPanel.setBorder(new TitledBorder(
				"Select Matrix to Vector Method"));
		globalSettingsPanel.setBorder(new TitledBorder("Global Settings"));
		mtvComboBox = new JComboBox(mtvModel);
		mtvComboBox.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setupMatrixToVector();
			}
		});
		matrixToVectorPanel.add(mtvComboBox, BorderLayout.CENTER);
		leftLower.add(matrixToVectorPanel, BorderLayout.CENTER);
		globalSettingsPanel.add(new JLabel("Window Size:"));
		classificationWindowSize = new IntegerTextField("5000");
		globalSettingsPanel.add(classificationWindowSize,
				"gap rel, w 1.5cm:max, growx");
		globalSettingsPanel.add(new JLabel("Step Size:"), "gap unrel");
		classificationWindowStepSize = new IntegerTextField("2500");
		globalSettingsPanel.add(classificationWindowStepSize,
				"gap rel, w 1.5cm:max, growx, wrap");
		globalSettingsPanel.add(new JLabel("Unit:"), "");
        boxUnit = new JComboBox(Unit.values());
        boxUnit.setSelectedItem(Unit.MILLISECONDS);
		globalSettingsPanel.add(boxUnit, "gap rel, spanx 3, wrap");
		globalSettingsPanel.add(new JLabel("Optional Processing Description:"), "spanx 2");
		globalSettingsPanel.add(modelDescriptionTextField, "gap rel, spanx 2, wrap");
		leftLower.add(globalSettingsPanel, BorderLayout.SOUTH);
	}

	private void removeSelectedAlgorithms() {
		controller.removeMethods(pcmJList.getSelectedValues());
	}

	@Override
	public boolean nextButtonClicked() {
		controller.addProcessing();
		return false;
	}

	@Override
	public String getNextButtonText() {
		return "Finish Configuration";
	}

	@Override
	public String getCaption() {
		return "Feature Processing";
	}

	/**
	 *
	 * @param i
	 */
	public void setListSelectedIndex(int i) {
		pcmJList.setSelectedIndex(i);
	}

	/**
	 * @param methodView
	 */
	public void setRightSide(JComponent methodView) {
		splitPane.add(methodView, JSplitPane.RIGHT);
	}

	/**
	 * @return
	 */
	public int getListSelectedIndex() {
		return pcmJList.getSelectedIndex();
	}

	private void showPopupMenu() {
		popupMenu.show(addButton, addButton.getX(), addButton.getY());
	}

    @Override
    public String getSaveButtonText() {
        return "Save";
    }

    @Override
    public void saveButtonClicked() {
        controller.saveButtonClicked();
    }

    Unit getUnit() {
        return (Unit)boxUnit.getSelectedItem();
    }

    void setUnit(String value) {
        boxUnit.setSelectedItem(Unit.valueOf(value));
    }

    void setClassificationWindowSizeStr(int value) {
        classificationWindowSize.setText(value + "");
    }

    void setClassificationWindowStepSizeStr(int value) {
        classificationWindowStepSize.setText(value + "");
    }

    String getOptionalModelStr() {
	return modelDescriptionTextField.getText();
    }

    void setOptionalModelStr(String str) {
	modelDescriptionTextField.setText(str);
    }

	/**
	 * @author Clemens Waeltken
	 * 
	 */
	private final class MenuAlgorithmAction implements ActionListener {
		private ProcessingAlgorithm algorithm;

		/**
		 * @param algo
		 */
		public MenuAlgorithmAction(ProcessingAlgorithm algo) {
			algorithm = algo;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			controller.addMethod(algorithm, pcmJList.getSelectedIndex());
		}
	}

	private class IntegerTextField extends JTextField {

		private static final long serialVersionUID = 2859951336392274420L;

		/**
		 * @param string
		 */
		public IntegerTextField(String string) {
			super(string);
			setColor();
			this.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					setColor();
				}

				@Override
				public void keyReleased(KeyEvent e) {
					setColor();
				}

				@Override
				public void keyPressed(KeyEvent e) {
					setColor();
				}
			});
		}

		/**
		 * 
		 */
		private void setColor() {
			boolean error = false;
			try {
				new Integer(getText());
			} catch (NumberFormatException e) {
				error = true;
			}
			if (error) {
				setForeground(invalidColor);
			} else {
				setForeground(validColor);
			}
		}
	}

	/**
	 * @return
	 */
	public String getClassificationWindowSizeStr() {
		return classificationWindowSize.getText();
	}

	/**
	 * @return
	 */
	public String getStepSizeStr() {
		return classificationWindowStepSize.getText();
	}
	
	public String getModelDescription() {
		return modelDescriptionTextField.getText();
	}
}
