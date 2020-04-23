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
package amuse.scheduler.gui.algorithm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import javax.swing.event.CaretEvent;
import net.miginfocom.swing.MigLayout;
import amuse.scheduler.gui.processing.JTextFieldWithValidation;
import amuse.scheduler.gui.views.WizardView;
import java.security.InvalidParameterException;
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;

/**
 * 
 * @author Clemens Waeltken
 */
public class AlgorithmView {

	private JPanel panel = new JPanel();
	private final AlgorithmInterface algorithm;
	private final JButton defaultButton = new JButton("Default");
	private final JLabel noSettingsLabel = new JLabel("Nothing to Setup");

	public AlgorithmView(AlgorithmInterface algorithm) {
		this.algorithm = algorithm;
		panel.setLayout(new MigLayout("fillx"));
		initView();
	}

	public JPanel getPanel() {
		return panel;
	}

	private void initView() {
		if (algorithm == null) {
			panel.add(noSettingsLabel);
			return;
		}
		ParameterComponent[] components = generateParameterComponents();
		panel.setBorder(new TitledBorder(algorithm.getName()));
		for (ParameterComponent c : components) {
			panel.add(new JLabel(c.getLabelText()), "wrap");
			panel.add(c.getJComponent(), "grow x, wrap");
		}
		if (components.length > 0) {
			panel.add(defaultButton);
			defaultButton
					.addActionListener(new ResetToDefaultsButtonListener());
		} else {
			panel.add(noSettingsLabel);
		}
	}

	/**
	 * @return
	 */
	private ParameterComponent[] generateParameterComponents() {
		List<ParameterComponent> components = new ArrayList<ParameterComponent>();
		for (int i = 0; i < algorithm.getAllowedParamerterStrings().length; i++) {
			components.add(new ParameterComponent(i));
		}
		return components.toArray(new ParameterComponent[components.size()]);
	}

	private class JTextFieldForInt extends JTextField {

		private static final long serialVersionUID = -3353029706401172369L;

                private int begin;
                private int end;
                private boolean useRange = false;

		private JTextFieldForInt(String currentValue) {
			super(currentValue);
			this.addKeyListener(new KeyListenerCheckInt(this));
			setColor();
		}

        private JTextFieldForInt(String currentValue, int begin, int end) {
            this(currentValue);
            if (!(begin < end)) {
                throw new InvalidParameterException();
            }
            this.begin = begin;
            this.end = end;
            useRange = true;
            setColor();
        }

		@Override
		public void setText(String text) {
			super.setText(text);
			setColor();
		}

		private void setColor() {
			String txt = getText();
			boolean okay = true;
			try {
				int value = Integer.valueOf(txt);
                                if (useRange && !(value >= begin && value <= end))
                                    okay = false;
			} catch (NumberFormatException ex) {
				okay = false;
			}
			if (okay) {
				setForeground(WizardView.VALID_COLOR);
			} else {
				setForeground(WizardView.INVALID_COLOR);
			}
		}

		private class KeyListenerCheckInt implements KeyListener {

			private KeyListenerCheckInt(JTextFieldForInt aThis) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				setColor();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				setColor();
			}

		}
	}

	private class JTextFieldForDouble extends JTextField {

		private static final long serialVersionUID = -3353029706401172369L;

                private double begin;
                private double end;
                private boolean useRange = false;

		private JTextFieldForDouble(String currentValue) {
			super(currentValue);
			this.addKeyListener(new KeyListenerCheckDouble(this));
			setColor();
		}

        private JTextFieldForDouble(String currentValue, double begin, double end) {
            this(currentValue);
            this.begin =  begin;
            this.end = end;
            useRange = true;
            setColor();
        }

		@Override
		public void setText(String text) {
			super.setText(text);
			setColor();
		}

		private void setColor() {
			String txt = getText();
			boolean okay = true;
			try {
				double value = Double.valueOf(txt);
                                if (useRange && !(value >= begin && value <= end))
                                    okay = false;
			} catch (NumberFormatException ex) {
				okay = false;
			}
			if (okay) {
				setForeground(WizardView.VALID_COLOR);
			} else {
				setForeground(WizardView.INVALID_COLOR);
			}
		}

		private class KeyListenerCheckDouble implements KeyListener {

			private KeyListenerCheckDouble(JTextFieldForDouble aThis) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				setColor();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				setColor();
			}

		}
	}

	private class ParameterComponent {

		final int indexForAlgorithm;
		final String definition;
		String currentValue;
		JComponent comp;

		/**
		 * @param i
		 */
		public ParameterComponent(int i) {
			indexForAlgorithm = i;
			if (indexForAlgorithm >= algorithm.getAllowedParamerterStrings().length) {
			    System.out.println("Error" + Arrays.toString(algorithm.getAllowedParamerterStrings()));
			}
			if (indexForAlgorithm >= algorithm.getCurrentParameterValues().length) {
			    System.out.println("Error" + Arrays.toString(algorithm.getCurrentParameterValues()));
			}
			definition = algorithm.getAllowedParamerterStrings()[indexForAlgorithm]
					.trim();
			currentValue = algorithm.getCurrentParameterValues()[indexForAlgorithm];
			comp = new JLabel("Configuration not supported!");
			// Parse definition:
			// n for nominal values.
			if (definition.startsWith("n{") && definition.lastIndexOf("}") > 0) {
				Scanner scanner = new Scanner(definition.substring(2,
						definition.lastIndexOf("}")));
				scanner.useDelimiter(",");
				List<String> nominals = new Vector<String>();
				while (scanner.hasNext()) {
                                    nominals.add(scanner.next());
				}
				JComboBox box = new JComboBox(nominals
						.toArray(new String[nominals.size()]));
				box.setSelectedItem(currentValue);
				algorithm.addAlgorithmChangeListener(new ListenerComboBox(box,
						indexForAlgorithm));
				comp = box;
			}
			// w for word.
			else if (definition.equalsIgnoreCase("w")) {
				JTextFieldWithValidation textField = new JTextFieldWithValidation(
						currentValue, ".*");
				algorithm
						.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(
								textField, indexForAlgorithm));
				comp = textField;
			}
			// word with regexp.
			else if (definition.startsWith("w{") && definition.lastIndexOf("}") > 0) {
				JTextFieldWithValidation textField = new JTextFieldWithValidation(
						currentValue, definition.substring(2, definition.lastIndexOf("}") - 1));
				algorithm
						.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(
								textField, indexForAlgorithm));
				comp = textField;
			}
			// i{begin,end} for integer with value range
			else if (definition.startsWith("i{") && definition.endsWith("}") && definition.contains(",")) {
                                // Parse value range:
                                int begin = new Integer(definition.substring(2,definition.indexOf(",")));
                                int end = new Integer(definition.substring(definition.indexOf(",")+1, definition.length()-1));
				JTextFieldForInt textField = new JTextFieldForInt(currentValue, begin, end);
				algorithm
						.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(
								textField, indexForAlgorithm));
				comp = textField;
			}
			// i for integer
			else if (definition.startsWith("i")) {
				JTextFieldForInt textField = new JTextFieldForInt(currentValue);
				algorithm
						.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(
								textField, indexForAlgorithm));
				comp = textField;
			}
			// d{begin,end} for double value with value range
			else if (definition.startsWith("d{") && definition.endsWith("}") && definition.contains(",")) {
                                // Parse value range:
                                double begin = new Double(definition.substring(2,definition.indexOf(",")));
                                double end = new Double(definition.substring(definition.indexOf(",")+1, definition.length()-1));
				JTextFieldForDouble textField = new JTextFieldForDouble(currentValue, begin, end);
				algorithm
						.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(
								textField, indexForAlgorithm));
				comp = textField;
			}
			// d for double value
			else if (definition.startsWith("d")) {
			    JTextFieldForDouble textField = new JTextFieldForDouble(currentValue);
			    algorithm.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(textField, indexForAlgorithm));
			    comp = textField;
			}
			// b for boolean
			else if (definition.equalsIgnoreCase("b")) {
				JCheckBox box = new JCheckBox();
				box.setSelected(Boolean.valueOf(algorithm.getCurrentParameterValues()[i]));
				algorithm.addAlgorithmChangeListener(new AlgorithmChangeListenerCheckBox(box, indexForAlgorithm));
				comp = box;
			}
			// fof for file or folder
			else if (definition.equalsIgnoreCase("fof")) {
                            JPanel panel = new JPanel();
                            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
                            JTextFieldWithValidation textField = new JTextFieldWithValidation(
						currentValue, ".*");
				algorithm
						.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(
								textField, indexForAlgorithm));
                                panel.add(textField);
                                JButton fileOrFolderButton = new JButton("...");
                                fileOrFolderButton.addActionListener(new ActionListenerForFileAndFolder(textField));
                                panel.add(fileOrFolderButton);
				comp = panel;
                        }
			// f for file
			else if (definition.equalsIgnoreCase("f")) {
                            JPanel panel = new JPanel();
                            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
                            JTextFieldWithValidation textField = new JTextFieldWithValidation(
						currentValue, ".*");
				algorithm
						.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(
								textField, indexForAlgorithm));
                                panel.add(textField);
                                JButton fileButton = new JButton("...");
                                fileButton.addActionListener(new ActionListenerForFileSelection(textField));
                                panel.add(fileButton);
				comp = panel;
                        }
			// xml for xml-file
			else if (definition.equalsIgnoreCase("xml")) {
                            JPanel panel = new JPanel();
                            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
                            JTextFieldWithValidation textField = new JTextFieldWithValidation(
						currentValue, ".*");
				algorithm
						.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(
								textField, indexForAlgorithm));
                                panel.add(textField);
                                JButton fileButton = new JButton("...");
                                fileButton.addActionListener(new ActionListenerForXMLFileSelection(textField));
                                panel.add(fileButton);
				comp = panel;
			// s for string
            } else if(definition.equals("s")) {
            	JTextFieldWithValidation textField = new JTextFieldWithValidation(currentValue, ".*");
			    algorithm.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(textField, indexForAlgorithm));
			    comp = textField;
            // c for code
            } else if(definition.equals("c")) {
            	JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
                JTextFieldWithValidation textField = new JTextFieldWithValidation(currentValue, ".*");
                algorithm.addAlgorithmChangeListener(new AlgorithmChangeListenerTextField(textField, indexForAlgorithm));
                panel.add(textField);
                JButton fileButton = new JButton("...");
                fileButton.addActionListener(new ActionListenerForFileSelection(textField));
                panel.add(fileButton);
                
                JButton editorButton = new JButton("Edit");
                editorButton.addActionListener(new ActionListenerForTextEditing(textField));
                panel.add(editorButton);
                
                comp = panel;
            }
            comp.setToolTipText(algorithm.getParameterDescriptions()[i]);
		}

		JComponent getJComponent() {
			return comp;
		}

		String getLabelText() {
			return algorithm.getParameterNames()[indexForAlgorithm];
		}

        private class ActionListenerForFileAndFolder implements ActionListener {

            private JTextField field;
            public ActionListenerForFileAndFolder(JTextField field) {
                this.field = field;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int result = fc.showOpenDialog(panel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    field.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        }
        
        private class ActionListenerForTextEditing implements ActionListener{
        	private JTextField field;
        	
        	public ActionListenerForTextEditing(JTextField field) {
				this.field = field;
			}
        	
			@Override
			public void actionPerformed(ActionEvent e) {
				TextEditor textEditor = new TextEditor(new File(field.getText()));
				File file = textEditor.getFile();
				if(file != null) {
					field.setText(file.getAbsolutePath());
				}
			}
        	
        }

        private class ActionListenerForFileSelection implements ActionListener {

            private JTextField field;
            public ActionListenerForFileSelection(JTextField field) {
                this.field = field;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = fc.showOpenDialog(panel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    field.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        }
        private class ActionListenerForXMLFileSelection implements ActionListener {

            private JTextField field;
            public ActionListenerForXMLFileSelection(JTextField field) {
                this.field = field;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(true);
                fc.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().endsWith(".xml");
                    }

                    @Override
                    public String getDescription() {
                        return "Select XML file";
                    }
                });
                int result = fc.showOpenDialog(panel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    field.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        }
	}

	private abstract class AlgorithmChangeListenerComponent implements
			AlgorithmChangeListener {
		final int index;

		public AlgorithmChangeListenerComponent(int i) {
			this.index = i;
		}
	}
	
	private class AlgorithmChangeListenerCheckBox extends AlgorithmChangeListenerComponent implements ActionListener {
		private JCheckBox box;
		/**
		 * @param box
		 * @param indexForAlgorithm
		 */
		public AlgorithmChangeListenerCheckBox(JCheckBox box,
				int indexForAlgorithm) {
			super(indexForAlgorithm);
			this.box = box;
			this.box.addActionListener(this);
		}

		/* (non-Javadoc)
		 * @see amuse.scheduler.gui.processing.AlgorithmChangeListener#parametersChanged()
		 */
		@Override
		public void parametersChanged() {
		}

		/* (non-Javadoc)
		 * @see amuse.scheduler.gui.processing.AlgorithmChangeListener#parametersReset()
		 */
		@Override
		public void parametersReset() {
			box.setSelected(Boolean.valueOf(algorithm.getDefaultParameters()[index]));
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			algorithm.setCurrentParameterAt(index, Boolean.toString((box.isSelected())));
		}
		
	}

	private class AlgorithmChangeListenerTextField extends
			AlgorithmChangeListenerComponent implements CaretListener {

		final JTextField textField;

		/**
		 * @param i
		 */
		public AlgorithmChangeListenerTextField(JTextField textField, int i) {
			super(i);
			this.textField = textField;
			this.textField.addCaretListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * amuse.scheduler.gui.processing.AlgorithmChangeListener#parametersChanged
		 * ()
		 */
		@Override
		public void parametersChanged() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * amuse.scheduler.gui.processing.AlgorithmChangeListener#parametersReset
		 * ()
		 */
		@Override
		public void parametersReset() {
			textField.setText(algorithm.getDefaultParameters()[index]);
		}

        @Override
        public void caretUpdate(CaretEvent e) {
            algorithm.setCurrentParameterAt(index, textField.getText());
        }

	}

	private class ListenerComboBox extends AlgorithmChangeListenerComponent
			implements ActionListener {

		JComboBox box;

		public ListenerComboBox(JComboBox box, int i) {
			super(i);
			this.box = box;
			this.box.addActionListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * amuse.scheduler.gui.processing.AlgorithmChangeListener#parametersChanged
		 * ()
		 */
		@Override
		public void parametersChanged() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			algorithm.setCurrentParameterAt(index, box.getSelectedItem()
					.toString());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * amuse.scheduler.gui.processing.AlgorithmChangeListener#parametersReset
		 * ()
		 */
		@Override
		public void parametersReset() {
			String str = algorithm.getDefaultParameters()[index];
			ComboBoxModel model = box.getModel();
			model.setSelectedItem(str);
		}

	}

	private class ResetToDefaultsButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			algorithm.resetDefaults();
		}
	}
}
