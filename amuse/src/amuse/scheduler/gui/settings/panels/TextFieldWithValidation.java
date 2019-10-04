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
 * Creation date: 09.07.2009
 */
package amuse.scheduler.gui.settings.panels;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysIntValue;
import amuse.scheduler.gui.views.WizardView;

/**
 * @author Clemens Waeltken
 *
 */
public class TextFieldWithValidation extends EditableAmuseSettingBody {

	private JTextField textField = new JTextField(10);
	private JLabel label;
    private KeysIntValue intKey;


    /**
	 * @param label
     * @param key
	 */
	public TextFieldWithValidation(String label, KeysIntValue key) {
		this.label = new JLabel(label + ": ");
		this.intKey = key;
		panel.setLayout(new BorderLayout());
		panel.add(this.label, BorderLayout.WEST);
		panel.add(this.textField, BorderLayout.CENTER);
		textField.setText(AmusePreferences.getInt(intKey) + "");
		setColorAndUpdate();
		textField.addCaretListener(new CaretListener() {
			
			@Override
			public void caretUpdate(CaretEvent e) {
				setColorAndUpdate();
			}
		});
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.panels.EditableAmuseSettingBody#saveChanges()
	 */
	@Override
	public void saveChanges() {
		AmusePreferences.putInt(intKey, new Integer(textField.getText()));
		setColorAndUpdate();
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.EditableAmuseSettingInterface#discardChanges()
	 */
	@Override
	public void discardChanges() {
		textField.setText(AmusePreferences.getInt(intKey)+"");
		setColorAndUpdate();
	}
	
	private void setColorAndUpdate() {
		boolean thrown = false;
		try {
			int value = new Integer(textField.getText());
			if (intKey.isValid(value)) {
				textField.setForeground(WizardView.VALID_COLOR);
			} else {
				textField.setForeground(WizardView.INVALID_COLOR);
			}
		} catch (NumberFormatException ex) {
			thrown = true;
		}
		if (thrown) {
			textField.setForeground(WizardView.INVALID_COLOR);
		}
		notifyListeners(hasChanges());
	}

	/**
	 * @return
	 */
	private boolean hasChanges() {
		String storedValue = "" + AmusePreferences.getInt(intKey);
		return (!storedValue.equals(textField.getText()));
	}

}
