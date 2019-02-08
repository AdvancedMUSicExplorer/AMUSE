package amuse.scheduler.gui.training;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public class TrainingDescriptionPanel extends JPanel {
	private JLabel textFieldLabel = new JLabel("Training Descrption:");
	private JTextField textField = new JTextField(10);
	private TitledBorder title = new TitledBorder("Optional Training Descritption");
	
	public TrainingDescriptionPanel() {
		super(new MigLayout("fillx"));
		this.setBorder(title);
		this.add(textFieldLabel, "pushx,wrap");
		this.add(textField, "growx, wrap");
	}
	
	public String getTrainingDescription() {
		return textField.getText();
	}
}
