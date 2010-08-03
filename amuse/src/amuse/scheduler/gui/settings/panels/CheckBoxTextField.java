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
 * Creation date: 08.07.2009
 */
package amuse.scheduler.gui.settings.panels;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.miginfocom.swing.MigLayout;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysBooleanValue;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.views.WizardView;

/**
 * @author Clemens Waeltken
 *
 */
public class CheckBoxTextField extends EditableAmuseSettingBody {

	private KeysStringValue key;
	private KeysBooleanValue bKey;
	private JCheckBox checkBox = new JCheckBox();
	private JTextField textField = new JTextField();
	public CheckBoxTextField(String label, KeysStringValue key, KeysBooleanValue bKey) {
		this.key = key;
		this.bKey = bKey;
		textField.addCaretListener(new CaretListener() {
			
			@Override
			public void caretUpdate(CaretEvent e) {
				setColorAndUpdate();
			}
		});
		loadSettings();
		setColorAndUpdate();
		checkBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxChanged();
			}
		});
		panel = new JPanel(new MigLayout("ins 0"));
		panel.add(new Label("Enable:"), "");
		panel.add(checkBox ,"gap rel");
		panel.add(new JLabel(label+":"), "gap unrel");
		panel.add(textField, "gap rel, w :500: ,growx");
	}

	private void setColorAndUpdate() {
		if (textField.getText().equalsIgnoreCase("NO_VALUE")) {
			textField.setForeground(WizardView.INVALID_COLOR);
		} else {
			textField.setForeground(WizardView.VALID_COLOR);
		}
		notifyListeners(!textField.getText().equals(AmusePreferences.get(key))
                || checkBox.isSelected() != AmusePreferences.getBoolean(bKey));
	}
	
	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.panels.EditableAmuseSettingBody#saveChanges()
	 */
	@Override
	public void saveChanges() {
		AmusePreferences.putBoolean(bKey, checkBox.isSelected());
		AmusePreferences.put(key, textField.getText());
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.EditableAmuseSettingInterface#discardChanges()
	 */
	@Override
	public void discardChanges() {
		loadSettings();
	}
	
	private void loadSettings() {
		checkBox.setSelected(AmusePreferences.getBoolean(bKey));
		checkBoxChanged();
		textField.setText(AmusePreferences.get(key));
	}
	
	private void checkBoxChanged() {
		if (checkBox.isSelected()) {
			textField.setEditable(true);
		} else {
			textField.setEditable(false);
		}
		setColorAndUpdate();
	}

}
