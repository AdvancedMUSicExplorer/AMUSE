package amuse.scheduler.gui.training;

import amuse.data.ClassificationType;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public class ClassificationTypePanel extends JPanel {
	private JLabel comboBoxLabel = new JLabel("Classification Type:");
	private JComboBox comboBox = new JComboBox();
	private JCheckBox checkBox = new JCheckBox("Fuzzy");
	private TitledBorder title = new TitledBorder("Select Classification Type");
	
	public ClassificationTypePanel() {
		super(new MigLayout("fillx"));
		this.setBorder(title);
		this.add(comboBoxLabel, "pushx, wrap");
		this.add(comboBox, "pushx, wrap");
		DefaultComboBoxModel model = new DefaultComboBoxModel(ClassificationType.stringValues());
		comboBox.setModel(model);
		this.add(checkBox, "pushx, gap rel, wrap");
	}
	
	public ClassificationType getClassificationType() {
		return ClassificationType.valueOf(comboBox.getSelectedItem().toString());
	}
	
	public boolean isFuzzy() {
		return checkBox.isSelected();
	}
}
