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
 * Creation date: 24.08.2008
 */
package amuse.scheduler.gui.settings;


import amuse.preferences.KeysBooleanValue;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import amuse.preferences.KeysIntValue;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.settings.panels.BooleanSelectionPanel;
import amuse.scheduler.gui.settings.panels.ListSelectionPanel;
import amuse.scheduler.gui.settings.panels.PathSelectionPanel;
import amuse.scheduler.gui.settings.panels.SliderIntSelectionPanel;

/**
 * @author Clemens Waeltken
 * 
 */
public class GeneralAmuseSettings extends AmuseSettingsPageBody {

	public GeneralAmuseSettings() {
		panel.setLayout(new MigLayout("fillx"));
		panel.setBorder(new TitledBorder(this.toString()));
		// Path Section:
		JPanel internalPanel = new JPanel();
		BoxLayout layout = new BoxLayout(internalPanel, BoxLayout.Y_AXIS);
		internalPanel.setLayout(layout);
		internalPanel.setBorder(new TitledBorder("Path Settings"));
		Vector<EditableAmuseSettingInterface> settings = new Vector<EditableAmuseSettingInterface>();
		// Add Grid Components:
		settings.add(new PathSelectionPanel("Amuse Folder", KeysStringValue.AMUSE_PATH));
		settings.add(new PathSelectionPanel("Music Database", KeysStringValue.MUSIC_DATABASE));
		settings.add(new PathSelectionPanel("Feature Database", KeysStringValue.FEATURE_DATABASE));
		settings.add(new PathSelectionPanel("Annotation Database", KeysStringValue.ANNOTATION_DATABASE));
		settings.add(new PathSelectionPanel("Processed Feature Database", KeysStringValue.PROCESSED_FEATURE_DATABASE));
		settings.add(new PathSelectionPanel("Category Database", KeysStringValue.CATEGORY_DATABASE));
		settings.add(new PathSelectionPanel("Model Database", KeysStringValue.MODEL_DATABASE));
		settings.add(new PathSelectionPanel("Metric Database", KeysStringValue.METRIC_DATABASE));
		settings.add(new PathSelectionPanel("Optimization Database", KeysStringValue.OPTIMIZATION_DATABASE));
		for (EditableAmuseSettingInterface singlePref : settings) {
			internalPanel.add(singlePref.getPanel());
			watchForChanges(singlePref);
		}
		internalPanel.setMaximumSize(new Dimension(internalPanel.getMaximumSize().height, internalPanel.getPreferredSize().width));
		panel.add(internalPanel, "grow x, wrap");
                // New Section:
		settings.clear();
		internalPanel = new JPanel(new MigLayout("fillx"));
		internalPanel.setBorder(new TitledBorder("External Tools"));
		internalPanel.setLayout(new BoxLayout(internalPanel, BoxLayout.Y_AXIS));
		settings.add(new PathSelectionPanel("Java Executable:", KeysStringValue.JAVA_PATH));
		settings.add(new PathSelectionPanel("MatLab Executable:", KeysStringValue.MATLAB_PATH));
                for (EditableAmuseSettingInterface singlePref : settings) {
			internalPanel.add(singlePref.getPanel(), "wrap");
			watchForChanges(singlePref);
		}
		panel.add(internalPanel, "grow x, wrap");
		// New Section:
		settings.clear();
		internalPanel = new JPanel(new MigLayout("fillx"));
		internalPanel.setBorder(new TitledBorder("Wave-Sampling Settings"));
		layout = new BoxLayout(internalPanel, BoxLayout.Y_AXIS);
                settings.add(new BooleanSelectionPanel("Use Downsamling", KeysBooleanValue.USE_DOWNSAMPLING));
                String [] valuesKHZ = {"44100Hz", "22050Hz", "11025Hz"};
                settings.add(new ListSelectionPanel("Select Target Sampling Rate", valuesKHZ, KeysIntValue.DOWNSAMPLING_TARGET_SIZE_IN_HZ));
		settings.add(new BooleanSelectionPanel("Reduce Stereo to Mono", KeysBooleanValue.REDUCE_TO_MONO));
                for (EditableAmuseSettingInterface singlePref : settings) {
			internalPanel.add(singlePref.getPanel(), "wrap");
			watchForChanges(singlePref);
		}
                panel.add(internalPanel, "grow x, wrap");
                // New Section:
		settings.clear();
		internalPanel = new JPanel(new MigLayout("fillx"));
		internalPanel.setBorder(new TitledBorder("Miscellaneous Settings"));
		layout = new BoxLayout(internalPanel, BoxLayout.Y_AXIS);
		// Add Miscellanious Settings Components
		String[] values = {"Debug", "Info", "Quiet"};
		settings.add(new ListSelectionPanel("Log Level", values, KeysIntValue.GUI_LOG_LEVEL));
		settings.add(new SliderIntSelectionPanel("Max number of Task Threads", 1, 10, KeysIntValue.MAX_NUMBER_OF_TASK_THREADS));
               for (EditableAmuseSettingInterface singlePref : settings) {
			internalPanel.add(singlePref.getPanel(), "wrap");
			watchForChanges(singlePref);
		}
		panel.add(internalPanel, "grow x, wrap");
	}

	public String toString() {
		return "General Settings";
	}
}
