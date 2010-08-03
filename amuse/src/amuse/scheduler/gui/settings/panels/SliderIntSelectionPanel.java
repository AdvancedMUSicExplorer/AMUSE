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

import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysIntValue;

/**
 * @author Clemens Waeltken
 * 
 */
public class SliderIntSelectionPanel extends EditableAmuseSettingBody {

	private int savedValue;

	private final JSlider slider;

	private final String label;

	private final int max;

	private final int min;

	private final KeysIntValue key;

	public SliderIntSelectionPanel(String label, int minValue, int maxValue,
			KeysIntValue intKey) {
		this.label = label + ":";
		this.min = minValue;
		this.max = maxValue;
		this.key = intKey;
		this.savedValue = AmusePreferences.getInt(this.key);
		this.slider = new JSlider(min, max, savedValue);
		// slider.setInverted(true);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for (int i = min; i <= max; i++) {
			if (i%5 == 0 || i == min || i == max)
			labelTable.put(new Integer(i), new JLabel(i + ""));
		}
		slider.setLabelTable(labelTable);
		slider.setValue(savedValue);
		slider.setMinorTickSpacing(1);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		JLabel jLabel = new JLabel(this.label);
		panel.add(jLabel);
		panel.add(slider);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				notifyListeners(hasChanges());
			}
		});
	}

	private boolean hasChanges() {
		return (savedValue != slider.getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amuse.scheduler.gui.settings.EditableAmuseSettingBody#saveChanges()
	 */
	public void saveChanges() {
		if (hasChanges()) {
			savedValue = slider.getValue();
			AmusePreferences.putInt(key, savedValue);
		}
		notifyListeners(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * amuse.scheduler.gui.settings.EditableAmuseSettingInterface#discardChanges
	 * ()
	 */
	public void discardChanges() {
		if (hasChanges()) {
			savedValue = AmusePreferences.getInt(key);
			slider.setValue(savedValue);
		}
		notifyListeners(false);
	}

}
