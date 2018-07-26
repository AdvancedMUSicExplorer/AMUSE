package amuse.scheduler.gui.settings;

import amuse.preferences.KeysBooleanValue;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import amuse.preferences.KeysIntValue;
import amuse.scheduler.gui.settings.panels.BooleanSelectionPanel;
import amuse.scheduler.gui.settings.panels.ListSelectionPanel;

/**
 * 
 * @author Frederik Heerde
 *
 */
public class AnnotationSettings extends AmuseSettingsPageBody {

	public AnnotationSettings() {
		panel.setLayout(new MigLayout("fillx"));
		panel.setBorder(new TitledBorder(this.toString()));
		
		// Audiospectrum Section:
		JPanel internalPanel = new JPanel(new MigLayout("fillx"));
		internalPanel.setBorder(new TitledBorder("Audiospectrum Settings"));
		Vector<EditableAmuseSettingInterface> settings = new Vector<EditableAmuseSettingInterface>();
		
		String [] valuesWindowSize = {"256", "512", "1024"};
		settings.add(new ListSelectionPanel("Select Window Size for the Extraction of the Audio Spectrum", valuesWindowSize, KeysIntValue.AUDIOSPECTRUM_WINDOWSIZE));
		settings.add(new ListSelectionPanel("Select Hop Size for the Extraction of the Audio Spectrum", valuesWindowSize, KeysIntValue.AUDIOSPECTRUM_HOPSIZE));
		settings.add(new BooleanSelectionPanel("Mark Current Time in Audio Spectrum", KeysBooleanValue.MARK_CURRENT_TIME_IN_ANNOTATION_AUDIOSPECTRUM));
        
		for (EditableAmuseSettingInterface singlePref : settings) {
			internalPanel.add(singlePref.getPanel(), "wmax pref, wrap");
			watchForChanges(singlePref);
		}
		panel.add(internalPanel, "grow x, wrap");
	}

	public String toString() {
		return "Annotation Settings";
	}
}
