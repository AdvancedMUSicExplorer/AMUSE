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
 * Creation date: 15.09.2008
 */
package amuse.scheduler.gui.settings.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysIntValue;

/**
 * @author Clemens Waeltken
 *
 */
public class ListSelectionPanel extends EditableAmuseSettingBody {
	
	private int savedValue;
	
	private final JComboBox comboBox;
	
	private final String label;
	
	private final String[] values;
	
	private final KeysIntValue key;
	
	public ListSelectionPanel(String label, String[] values, KeysIntValue intKey) {
		this.label = label + ": ";
		this.values = values;
		this.key = intKey;
		this.savedValue = AmusePreferences.getInt(this.key);
		if (savedValue < 0 || savedValue >= this.values.length) {
			System.out.println("ListSelectionPanel: Illegal saved state!");
		}
		JLabel jLabel = new JLabel(this.label);
		comboBox = new JComboBox(this.values);
		comboBox.setSelectedIndex(this.savedValue);
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(jLabel);
		panel.add(comboBox);
		comboBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
				notifyListeners(hasChanges());
			}
		});
	}
	
	private boolean hasChanges() {
		return (savedValue != comboBox.getSelectedIndex());
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.EditableAmuseSettingBody#saveChanges()
	 */
	@Override
	public void saveChanges() {
		if (hasChanges()) {
			savedValue = comboBox.getSelectedIndex();
			AmusePreferences.putInt(key, savedValue);
		}
		notifyListeners(false);
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.EditableAmuseSettingInterface#discardChanges()
	 */
    @Override
	public void discardChanges() {
		if (hasChanges()) {
			savedValue = AmusePreferences.getInt(key);
			comboBox.setSelectedIndex(savedValue);
		}
		notifyListeners(false);
	}
}
