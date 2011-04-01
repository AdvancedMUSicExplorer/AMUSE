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
 * Creation date: 10.08.2008
 */
package amuse.scheduler.gui.navigation;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A Navigation Panel with Title.
 * @author  Clemens Waeltken
 */
public class JButtonPanelWithHeading extends javax.swing.JPanel implements ButtonPanelInterface {
	
	private static final long serialVersionUID = -5066126034135574202L;
	
	private final JPanel buttonsPanel;

	public JButtonPanelWithHeading(String heading) {
		super();
		LayoutManager layout = new BorderLayout();
		this.setLayout(layout);
		JLabel label = new JLabel(heading);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(label, BorderLayout.NORTH);
		buttonsPanel = new JPanel();
		this.add(buttonsPanel, BorderLayout.CENTER);
		BoxLayout layoutButtons = new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS);
		buttonsPanel.setLayout(layoutButtons);
		buttonsPanel.add(Box.createHorizontalGlue());
	}
	
    @Override
	public void addButton(JButton button) {
		buttonsPanel.add(button);
		buttonsPanel.add(Box.createHorizontalGlue());
	}

    @Override
    public JPanel getPanel() {
        return this;
    }
}