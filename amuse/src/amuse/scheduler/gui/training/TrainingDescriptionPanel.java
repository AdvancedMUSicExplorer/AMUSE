package amuse.scheduler.gui.training;

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
 * Creation date: 06.02.2019
 */
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

/*
 * JPanel that lets you select an optional training description
 * 
 * @author Philipp Ginsel
 */

public class TrainingDescriptionPanel extends JPanel {
	private JLabel textFieldLabel = new JLabel("Training Description:");
	private JTextField textField = new JTextField(10);
	private TitledBorder title = new TitledBorder("Optional Training Description");
	
	public TrainingDescriptionPanel() {
		super(new MigLayout("fillx"));
		this.setBorder(title);
		this.add(textFieldLabel, "pushx,wrap");
		this.add(textField, "growx, wrap");
	}
	
	public String getTrainingDescription() {
		return textField.getText();
	}

	public void setTrainingDescription(String trainingDescription) {
		textField.setText(trainingDescription);
	}
}
