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
 * Creation date: 11.09.2008
 */
package amuse.scheduler.gui.settings.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysBooleanValue;
import amuse.preferences.PreferenceChangeListener;

/**
 * @author Clemens Waeltken
 *
 */
public class BooleanSelectionPanel extends EditableAmuseSettingBody {
	
	private Boolean savedBoolean;
	
	private final String label;
	
	private final KeysBooleanValue key;

	
	private final JCheckBox checkBox = new JCheckBox();
	
	public BooleanSelectionPanel(String label, KeysBooleanValue booleanKey) {
		this.key = booleanKey;
		this.label = label + ":";
		this.savedBoolean = AmusePreferences.getBoolean(key);
		this.checkBox.setText(this.label);
		this.checkBox.setSelected(savedBoolean);
		this.checkBox.setHorizontalTextPosition(SwingConstants.LEFT);
		// Listen to CheckBox klicks:
		this.checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				notifyListeners(hasChanges());
			}
		});
		// Listen to changes in AmusePreferences:
		AmusePreferences.addPreferenceChangeListener(new PreferenceChangeListener() {
			public void preferenceChange() {
				if(AmusePreferences.getBoolean(key) != savedBoolean) {
					savedBoolean = AmusePreferences.getBoolean(key);
					checkBox.setSelected(savedBoolean);
				}
			}
		});
		// Add CheckBox to panel:
		panel.setLayout(new BorderLayout());
		panel.add(checkBox, BorderLayout.CENTER);
		panel.add(Box.createHorizontalGlue(), BorderLayout.EAST);
//		panel.add(Box.createHorizontalGlue(), BorderLayout.WEST);
	}
	
	private boolean hasChanges() {
		return (savedBoolean != this.checkBox.isSelected());
	}
	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.EditableAmuseSettingBody#saveChanges()
	 */
	@Override
	public void saveChanges() {
		if (hasChanges()) {
			savedBoolean = checkBox.isSelected();
			AmusePreferences.putBoolean(key, savedBoolean);
		}
		notifyListeners(hasChanges());
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.EditableAmuseSettingInterface#discardChanges()
	 */
	public void discardChanges() {
		if(hasChanges()) {
			checkBox.setSelected(savedBoolean);
		}
		notifyListeners(hasChanges());
	}
	
	public boolean isSelected() {
		return this.checkBox.isSelected();
	}

}
