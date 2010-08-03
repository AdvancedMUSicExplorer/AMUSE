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
 * Creation date: 24.09.2008
 */
package amuse.scheduler.gui.settings;

import java.util.Vector;

import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import amuse.preferences.KeysBooleanValue;
import amuse.preferences.KeysIntValue;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.settings.panels.CheckBoxTextField;
import amuse.scheduler.gui.settings.panels.TextFieldWithValidation;

/**
 * @author Clemens Waeltken
 *
 */
public class GridSettings extends AmuseSettingsPageBody {
	
	public GridSettings() {
		panel.setLayout(new MigLayout("fillx"));
		panel.setBorder(new TitledBorder(this.toString()));
		// Path Section:
		Vector<EditableAmuseSettingInterface> settings = new Vector<EditableAmuseSettingInterface>();
		// Add Grid Components:
		settings.add(new CheckBoxTextField("Extractor Script", KeysStringValue.GRID_SCRIPT_EXTRACTOR, KeysBooleanValue.USE_GRID_EXTRACTOR));
		settings.add(new CheckBoxTextField("Processor Script", KeysStringValue.GRID_SCRIPT_PROCESSOR, KeysBooleanValue.USE_GRID_PROCESSOR));
		settings.add(new CheckBoxTextField("Trainer Script", KeysStringValue.GRID_SCRIPT_TRAINER, KeysBooleanValue.USE_GRID_TRAINER));
		settings.add(new CheckBoxTextField("Classifier Script", KeysStringValue.GRID_SCRIPT_CLASSIFIER, KeysBooleanValue.USE_GRID_CLASSIFIER));
		settings.add(new CheckBoxTextField("Validator Script", KeysStringValue.GRID_SCRIPT_VALIDATOR, KeysBooleanValue.USE_GRID_VALIDATOR));
		settings.add(new CheckBoxTextField("Optimizer Script", KeysStringValue.GRID_SCRIPT_OPTIMIZER, KeysBooleanValue.USE_GRID_OPTIMIZER));
		settings.add(new TextFieldWithValidation("Number of jobs per grid machine", KeysIntValue.NUMBER_OF_JOBS_PER_GRID_MACHINE));
		for (EditableAmuseSettingInterface singlePref : settings) {
			panel.add(singlePref.getPanel(), "wrap");
			watchForChanges(singlePref);
		}
	}

	public String toString() {
		return "Grid Settings";
	}
}
