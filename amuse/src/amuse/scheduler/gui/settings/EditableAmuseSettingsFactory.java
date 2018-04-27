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
 * Creation date: 23.08.2008
 */
package amuse.scheduler.gui.settings;

import java.util.Vector;

/**
 * @author Clemens Waeltken
 *
 */
public class EditableAmuseSettingsFactory {
	
	private static volatile EditableAmuseSettingsFactory instance = null;
	
	private static volatile Vector<EditableAmuseSettingInterface> allEditableAmuseSettings = null;
	
	public static EditableAmuseSettingsFactory getInstance() {
		if(instance == null) {
			synchronized (EditableAmuseSettingsFactory.class) {
				if(instance == null) {
					instance = new EditableAmuseSettingsFactory();
				}
			}
		}
		return instance;
	}
	
	public java.util.List<EditableAmuseSettingInterface> getEditableAmuseSettings() {
		if(allEditableAmuseSettings == null) {
			synchronized (EditableAmuseSettingsFactory.class) {
				if(allEditableAmuseSettings == null) {
					allEditableAmuseSettings = new Vector<EditableAmuseSettingInterface>();
					createSettingsDialogs();
				}
			}
		}
		return allEditableAmuseSettings;
	}

	/**
	 * This method will create all <code>EditableAmuseSettings</code> displayed in <code>JPanelSettings</code>.
	 */
	private void createSettingsDialogs() {
		allEditableAmuseSettings.add(new GeneralAmuseSettings());
		allEditableAmuseSettings.add(new GridSettings());
        allEditableAmuseSettings.add(new PluginSettings());
        allEditableAmuseSettings.add(new AnnotationSettings());
	}
}
