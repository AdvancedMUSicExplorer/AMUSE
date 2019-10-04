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
 * Creation date: 09.08.2009
 */
package amuse.scheduler.gui.algorithm;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Level;

import amuse.data.datasets.AlgorithmTableSet;
import amuse.data.io.DataSetException;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.util.AmuseLogger;
import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.scheduler.gui.training.ModelTypeListener;

/**
 * @author Clemens Waeltken
 *
 */
public class AlgorithmConfigurationFacade implements ModelTypeListener {

	List<Algorithm> availableAlgorithms = new ArrayList<Algorithm>();
	Algorithm selectedAlgorithm;
	private final JComboBox comboBox = new JComboBox();
	private final JPanel pnlParameterDisplay = new JPanel(new BorderLayout());
	private final JPanel pnlComboBox = new JPanel(new MigLayout("fillx"));
	private String algorithmsName;
	private JCheckBox useAlgorithmBox = new JCheckBox();
	private JPopupMenu algorithmMenu = new JPopupMenu();
	private boolean usesCategories = true;
	private JButton algorithmButton = new JButton("Select Algorithm");

	public AlgorithmConfigurationFacade() {
		setAlgorithmName("");
	}

	/**
	 *
	 * @param name Name of the Category of algorithm to setup.
	 * @param algorithmTableFile arff file to get AlgorithmTable from.
	 */
	public AlgorithmConfigurationFacade(String name, File algorithmTableFile) {
		setFile(algorithmTableFile);
		setAlgorithmName(name);
		createViews();
		useAlgorithmBox.setText("Use " + name);
		useAlgorithmBox.setSelected(true);
		useAlgorithmBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				comboBoxClicked();
			}
		});
		pnlComboBox.add(new JLabel("Algorithm:"), "");
		pnlComboBox.add(comboBox, "gap rel, pushx,  wrap");
		if (usesCategories) {
			pnlComboBox.remove(comboBox);
			pnlComboBox.add(algorithmButton, "gap rel, pushx,  wrap");
			updateAlgrithmButtonText();
		}
	}

	private void comboBoxClicked() {
		if (useAlgorithmBox.isSelected() == true) {
			setChildsEnabled(pnlComboBox, true);
			setChildsEnabled(pnlParameterDisplay, true);
		} else {
			setChildsEnabled(pnlComboBox, false);
			setChildsEnabled(pnlParameterDisplay, false);
			useAlgorithmBox.setEnabled(true);
		}
	}

	private void updateAlgrithmButtonText() {
		algorithmButton.setText(comboBox.getSelectedItem().toString());
	}

	public void setToolTip(String text) {
		pnlComboBox.setToolTipText(text);
		comboBox.setToolTipText(text);
	}

	private void setChildsEnabled(Component comp, boolean b) {
		if (comp instanceof JComponent) {
			for (Component c : ((JComponent) comp).getComponents()) {
				c.setEnabled(b);
				setChildsEnabled(c, b);
			}
		}
	}

	/**
	 *
	 */
	private void addListenerToComboBox() {
		comboBox.getModel().addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent e) {
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				if (e.getIndex0() == -1 && e.getIndex1() == -1) {
					updateAlgorithmParameterView();
				}
			}
		});
	}

	private void setFile(File file) {
		loadTable(file);
		createViews();
	}

	/**
	 *
	 */
	private void createViews() {
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(new Vector<AlgorithmInterface>(availableAlgorithms));
		comboBox.setModel(comboBoxModel);
		addListenerToComboBox();
		updateAlgorithmParameterView();
		createMenu(RelationshipType.BINARY, LabelType.SINGLELABEL, MethodType.SUPERVISED);
		algorithmButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showPopupMenu();
			}
		});
	}

	private void showPopupMenu() {
		algorithmMenu.show(pnlComboBox, algorithmButton.getX(), algorithmButton.getY() + algorithmButton.getHeight());
	}

	public void setUseEnableButton(boolean use) {
		if (use) {
			if (!pnlComboBox.isAncestorOf(useAlgorithmBox)) {
				pnlComboBox.add(useAlgorithmBox, "spanx 2, wrap", 0);
			}
		} else if (pnlComboBox.isAncestorOf(useAlgorithmBox)) {
			pnlComboBox.remove(useAlgorithmBox);
		}
	}

	private boolean isEnableButtonInUse() {
		return pnlComboBox.isAncestorOf(useAlgorithmBox);
	}

	public void setAlgorithmName(String title) {
		algorithmsName = title;
		if (algorithmsName.equalsIgnoreCase("")) {
			pnlParameterDisplay.setBorder(new TitledBorder("Setup Parameters"));
			pnlComboBox.setBorder(new TitledBorder("Select Algorithm"));
		} else {
			pnlParameterDisplay.setBorder(new TitledBorder("Setup " + algorithmsName + " Algorithm Parameters"));
			pnlComboBox.setBorder(new TitledBorder("Select " + algorithmsName + " Algorithm"));
		}
	}

	/**
	 *
	 */
	protected void updateAlgorithmParameterView() {
		pnlParameterDisplay.removeAll();
		AlgorithmInterface al = (AlgorithmInterface) comboBox.getModel().getSelectedItem();
		pnlParameterDisplay.add(new AlgorithmView(al).getPanel(), BorderLayout.CENTER);
		pnlParameterDisplay.revalidate();
		if (isEnableButtonInUse()) {
			comboBoxClicked();
		}
	}

	/**
	 * Place the Algorithm Selection ComboBox in your panel.
	 * @return the <class>ComboBox</class> to select algorithms.
	 */
	public JComponent getAlgorithmSelectionComboBox() {
		return pnlComboBox;
	}

	/**
	 * This methods loads an Algorithm Table from arff file.
	 * @param file The file to load from.
	 */
	private void loadTable(File file) {
		// Load DataSet:
		AlgorithmTableSet algorithmDataSet;
		try {
			algorithmDataSet = new AlgorithmTableSet(file);
		} catch (IOException e) {
			AmuseLogger.write(this.getClass().toString(), Level.ERROR, "Unable to load Algorithm Table from: \"" + file.getAbsolutePath() + "\"\n" + e.getLocalizedMessage());
			return;
		} catch (DataSetException e) {
			AmuseLogger.write(this.getClass().toString(), Level.ERROR, "Unable to load Algorithm Table from: \"" + file.getAbsolutePath() + "\"\n" + e.getLocalizedMessage());
			return;
		}
		// Get Attributes of AlgorithmDataSet:
		NumericAttribute idAttr = algorithmDataSet.getIdAttribute();
		StringAttribute nameAttr = algorithmDataSet.getNameAttribute();
		NominalAttribute categoryAttr = null;
		try {
			categoryAttr = algorithmDataSet.getCategoryAttribute();
			if (categoryAttr.getNominalValues().length <= 1) {
				usesCategories = false;
			}
		} catch (DataSetException ex) {
			usesCategories = false;
		}
		StringAttribute descAttr = algorithmDataSet.getAlgorithmDescriptionAttribute();
		StringAttribute exParamNamesAttr = algorithmDataSet.getParameterNamesAttribute();
		StringAttribute exParamAttr = algorithmDataSet.getParameterDefinitionsAttribute();
		StringAttribute defaultValsAttr = algorithmDataSet.getDefaultParameterValuesAttribute();
		StringAttribute paramDescAttr = algorithmDataSet.getParameterDescriptionsAttribute();
		NumericAttribute supportsBinaryAttr = algorithmDataSet.getSupportsBinaryAttribute();
		NumericAttribute supportsContinuousAttr = algorithmDataSet.getSupportsContinuousAttribute();
		NumericAttribute supportsMulticlassAttr = algorithmDataSet.getSupportsMulticlassAttribute();
		NumericAttribute supportsMultilabelAttr = algorithmDataSet.getSupportsMultilabelAttribute();
		NumericAttribute supportsSinglelabelAttr = algorithmDataSet.getSupportsSinglelabelAttribute();
		NumericAttribute supportsSupervisedAttr = algorithmDataSet.getSupportsSupervisedAttribute();
		NumericAttribute supportsUnsupervisedAttr = algorithmDataSet.getSupportsUnsupervisedAttribute();
		NumericAttribute supportsRegressionAttr = algorithmDataSet.getSupportsRegressionAttribute();
		// Create Model:
		this.availableAlgorithms = new ArrayList<Algorithm>();
		for (int i = 0; i < algorithmDataSet.getValueCount(); i++) {
			// Create ProcessingAlgorithm Object:
			String category;
			if (usesCategories) {
				category = categoryAttr.getValueAt(i);
			} else {
				category = "";
			}
			boolean supportsBinary = supportsBinaryAttr.getValueAt(i) != 0;
			boolean supportsContinuous = supportsContinuousAttr.getValueAt(i) != 0;
			boolean supportsMulticlass = supportsMulticlassAttr.getValueAt(i) != 0;
			boolean supportsMultilabel = supportsMultilabelAttr.getValueAt(i) != 0;
			boolean supportsSinglelabel = supportsSinglelabelAttr.getValueAt(i) != 0;
			boolean supportsSupervised = supportsSupervisedAttr.getValueAt(i) != 0;
			boolean supportsUnsupervised = supportsUnsupervisedAttr.getValueAt(i) != 0;
			boolean supportsRegression = supportsRegressionAttr.getValueAt(i) != 0;
			Algorithm al = new Algorithm(idAttr.getValueAt(
					i).intValue(), nameAttr.getValueAt(i), descAttr.getValueAt(i), category,
					exParamNamesAttr.getValueAt(i), exParamAttr.getValueAt(i),
					defaultValsAttr.getValueAt(i), paramDescAttr.getValueAt(i),
					supportsBinary,
					supportsContinuous,
					supportsMulticlass,
					supportsMultilabel,
					supportsSinglelabel,
					supportsSupervised,
					supportsUnsupervised,
					supportsRegression);
			this.availableAlgorithms.add(al);
		}

	}

	/**
	 * Place this Panel in your GUI to allow algorithm configuration.
	 * @return the panel to setup parameters for the currently selected algorithm.
	 */
	public JComponent getParameterPanel() {
		return pnlParameterDisplay;
	}

	/**
	 * Get the selected Algorithm with its current configuration.
	 * @return the selected <class>Algorithm</class> containing selected parameters.
	 */
	public Algorithm getSelectedAlgorithm() {
		return (Algorithm) comboBox.getSelectedItem();
	}

	private void createMenu(RelationshipType relationshipType, LabelType labelType, MethodType methodType) {
		algorithmMenu = new JPopupMenu();
		algorithmMenu.setInvoker(algorithmButton);
		JMenuItem item;
		List<String> categories = new ArrayList<String>();
		for (AlgorithmInterface al : availableAlgorithms) {
			if (!categories.contains(al.getCategory())) {
				categories.add(al.getCategory());
			}
		}
		for (String cat : categories) {
			JMenu submenu = null;
			JMenu currentMenu = null;
			String rest = cat;
			if (!cat.contains(">")) {
				Component[] components = algorithmMenu.getComponents();
				for (Component c: components) {
					if (c instanceof JMenu) {
						JMenu men = (JMenu) c;
						if (men.getText().equals(cat)) {
							submenu = men;
						}
					}
				}
			}
			while(rest.contains(">")) {
				String path = rest.substring(0, rest.indexOf(">"));
				rest = rest.substring(rest.indexOf(">") +1);
				Component[] components = algorithmMenu.getComponents();
				if (currentMenu != null) {
					components = currentMenu.getComponents();
				}
				for (Component c: components) {
					if (c instanceof JMenu) {
						JMenu men = (JMenu) c;
						if (men.getText().equals(path)) {
							currentMenu = men;
						} 
					}
				}
				JMenu newMenu = new JMenu(path);
				if (currentMenu == null) {
					algorithmMenu.add(newMenu);
				} else {
					currentMenu.add(newMenu);
				}
				currentMenu = newMenu;
			}
			if (submenu == null) {
				submenu = new JMenu(rest);
			}
			for (AlgorithmInterface al : availableAlgorithms) {
				if (al.getCategory().equalsIgnoreCase(cat)) {
					item = new JMenuItem(al.getName());
					item.addActionListener(new MenuAlgorithmAction(al));
					item.setToolTipText(al.getDescription());
					boolean supportsSettings = true;
					
					if(relationshipType == RelationshipType.BINARY && !al.supportsBinary()){
						supportsSettings = false;
					} else if(relationshipType == RelationshipType.CONTINUOUS && !al.supportsContinuous()) {
						supportsSettings = false;
					} else if(labelType == LabelType.SINGLELABEL && !al.supportsSinglelabel()) {
						supportsSettings = false;
					} else if(labelType == LabelType.MULTICLASS && !al.supportsMulticlass()) {
						supportsSettings = false;
					} else if(labelType == LabelType.MULTILABEL && !al.supportsMultilabel()) {
						supportsSettings = false;
					} else if(methodType == MethodType.SUPERVISED && !al.supportsSupervised()) {
						supportsSettings = false;
					} else if(methodType == MethodType.UNSUPERVISED && !al.supportsUnsupervised()) {
						supportsSettings = false;
					} else if(methodType == MethodType.REGRESSION && !al.supportsRegression()) {
						supportsSettings = false;
					}
					
					item.setEnabled(supportsSettings);
					
					if (categories.size() == 1) {
						algorithmMenu.add(item);
					} else {
						submenu.add(item);
					}
				}
				if (submenu.getItemCount() > 0) {
					if (currentMenu == null)
						algorithmMenu.add(submenu);
					else
						currentMenu.add(submenu);
				}
			}
		}
	}

	public boolean isEnabled() {
		return useAlgorithmBox.isSelected();
	}

	public void setSelectedAlgorithm(String str) {
		String idStr = str;
		int paramBegin = str.indexOf("[");
		int paramEnd = str.lastIndexOf("]");
		String parameters = "";
		if (idStr.equalsIgnoreCase("-1") && isEnableButtonInUse()) {
			useAlgorithmBox.setSelected(false);
			comboBoxClicked();
		} else {
			useAlgorithmBox.setSelected(true);
			comboBoxClicked();
		}
		if (paramBegin != -1 && paramEnd != -1) {
			idStr = str.substring(0, paramBegin);
			parameters = str.substring(paramBegin, paramEnd + 1);
		}
		int id = new Integer(idStr);
		for (Algorithm a : availableAlgorithms) {
			if (a.getID() == id) {
				comboBox.setSelectedItem(a);
				if (!parameters.equals("")) {
					a.setCurrentParameters(Algorithm.scanParameters(parameters));
				}
			}
		}
		updateAlgrithmButtonText();
		updateAlgorithmParameterView();
	}

	private final class MenuAlgorithmAction implements ActionListener {

		private AlgorithmInterface algorithm;

		/**
		 * @param algo
		 */
		public MenuAlgorithmAction(AlgorithmInterface algo) {
			algorithm = algo;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			comboBox.setSelectedItem(algorithm);
			algorithmButton.setText(algorithm.getName());
			algorithmMenu.setVisible(false);
		}
	}

	@Override
	public void updateModelType(RelationshipType relationshipType, LabelType labelType, MethodType methodType) {
		createMenu(relationshipType, labelType, methodType);
		Algorithm currentAlgorithm = (Algorithm)comboBox.getSelectedItem();
		
		boolean supportsSettings = true;
		if(relationshipType == RelationshipType.BINARY && !currentAlgorithm.supportsBinary()){
			supportsSettings = false;
		} else if(relationshipType == RelationshipType.CONTINUOUS && !currentAlgorithm.supportsContinuous()) {
			supportsSettings = false;
		} else if(labelType == LabelType.SINGLELABEL && !currentAlgorithm.supportsSinglelabel()) {
			supportsSettings = false;
		} else if(labelType == LabelType.MULTICLASS && !currentAlgorithm.supportsMulticlass()) {
			supportsSettings = false;
		} else if(labelType == LabelType.MULTILABEL && !currentAlgorithm.supportsMultilabel()) {
			supportsSettings = false;
		} else if(methodType == MethodType.SUPERVISED && !currentAlgorithm.supportsSupervised()) {
			supportsSettings = false;
		} else if(methodType == MethodType.UNSUPERVISED && !currentAlgorithm.supportsUnsupervised()) {
			supportsSettings = false;
		} else if(methodType == MethodType.REGRESSION && !currentAlgorithm.supportsRegression()) {
			supportsSettings = false;
		}
		
		// If the current algorithm does not support the settings, find an algorithm that does.
		if(!supportsSettings) {
			for(Algorithm al : availableAlgorithms) {
				if(relationshipType == RelationshipType.BINARY && !al.supportsBinary()){
					continue;
				} else if(relationshipType == RelationshipType.CONTINUOUS && !al.supportsContinuous()) {
					continue;
				} else if(labelType == LabelType.SINGLELABEL && !al.supportsSinglelabel()) {
					continue;
				} else if(labelType == LabelType.MULTICLASS && !al.supportsMulticlass()) {
					continue;
				} else if(labelType == LabelType.MULTILABEL && !al.supportsMultilabel()) {
					continue;
				} else if(methodType == MethodType.SUPERVISED && !al.supportsSupervised()) {
					continue;
				} else if(methodType == MethodType.UNSUPERVISED && !al.supportsUnsupervised()) {
					continue;
				} else if(methodType == MethodType.REGRESSION && !al.supportsRegression()) {
					continue;
				} else {
					comboBox.setSelectedItem(al);
					updateAlgrithmButtonText();
					updateAlgorithmParameterView();
				}
			}
		}
	}
}
