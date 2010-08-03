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
 * Creation date: 10.09.2008
 */
package amuse.scheduler.gui.settings.panels;

import java.util.Vector;

import javax.swing.JPanel;

import amuse.scheduler.gui.settings.EditableAmuseSettingInterface;
import amuse.scheduler.gui.settings.SettingsChangedListener;

/**
 * @author Clemens Waeltken
 *
 */
public abstract class EditableAmuseSettingBody implements EditableAmuseSettingInterface {

	private final Vector<SettingsChangedListener> listeners = new Vector<SettingsChangedListener>();
	
	public JPanel panel = new JPanel();

	public EditableAmuseSettingBody() {
		super();
	}

	public JPanel getPanel() {
		return panel;
	}

	/**
	 * @param bool
	 */
	protected void notifyListeners(boolean bool) {
		for (SettingsChangedListener listener: listeners) {
			listener.settingsStateChanged(this, bool);
		}
	}

	public void addChangeListener(SettingsChangedListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(SettingsChangedListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.EditableAmuseSetting#saveChanges()
	 */
	public abstract void saveChanges();
}