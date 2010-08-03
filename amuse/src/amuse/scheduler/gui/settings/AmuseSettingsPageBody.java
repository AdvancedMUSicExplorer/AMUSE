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
package amuse.scheduler.gui.settings;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * @author Clemens Waeltken
 *
 */
public abstract class AmuseSettingsPageBody implements EditableAmuseSettingInterface {

	protected JPanel panel = new JPanel();
	
	private final List<EditableAmuseSettingInterface> changedSettings = new Vector<EditableAmuseSettingInterface>();
	
	private final List<EditableAmuseSettingInterface> watchedSettings = new Vector<EditableAmuseSettingInterface>();
	
	private final List<SettingsChangedListener> listeners = new Vector<SettingsChangedListener>();
	
	private final JPanel extPanel = new JPanel();

	public AmuseSettingsPageBody() {
		super();
		JScrollPane sp = new JScrollPane(panel);
		BorderLayout layout = new BorderLayout();
		extPanel.setLayout(layout);
		extPanel.add(sp, BorderLayout.CENTER);
	}

	public JPanel getPanel() {
		return extPanel;
	}

	public void saveChanges() {
		for (EditableAmuseSettingInterface setting : watchedSettings) {
			setting.saveChanges();
		}
		changedSettings.clear();
		notifyListeners(false);
	}
	
	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.settings.EditableAmuseSettingInterface#discardChanges()
	 */
	public void discardChanges() {
		for (EditableAmuseSettingInterface setting : watchedSettings) {
			setting.discardChanges();
		}
		changedSettings.clear();
		notifyListeners(false);
	}

	public void addChangeListener(SettingsChangedListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(SettingsChangedListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners(boolean bool) {
		for (SettingsChangedListener listener: listeners) {
			listener.settingsStateChanged(this, bool);
		}
	}
	
	public void watchForChanges(EditableAmuseSettingInterface setting) {
		watchedSettings.add(setting);
		setting.addChangeListener(new SettingsChangedListener() {
			public void settingsStateChanged(EditableAmuseSettingInterface source,
					boolean changed) {
				if (changed && !changedSettings.contains(source)) {
					changedSettings.add(source);
				} else if (!changed && changedSettings.contains(source)) {
					changedSettings.remove(source);
				}
				notifyListeners(!changedSettings.isEmpty());
			}
		});
	}

}