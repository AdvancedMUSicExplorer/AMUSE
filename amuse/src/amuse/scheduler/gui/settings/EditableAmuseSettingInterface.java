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

import javax.swing.JPanel;

/**
 * This Interface is used for settings to be displayed and changeable within the <code>AmuseWizard</code>. 
 * @author Clemens Waeltken
 */
public interface EditableAmuseSettingInterface {
	
	/**
	 * @return - <code>JPanel</code>, displaying all settings. 
	 */
	public JPanel getPanel();
	
	/**
	 * This method requests that this Object saves it's changes.
	 */
	public void saveChanges();
	
	public void discardChanges();
	
	public void addChangeListener(SettingsChangedListener listener);
	
	public void removeChangeListener(SettingsChangedListener listener);
}
