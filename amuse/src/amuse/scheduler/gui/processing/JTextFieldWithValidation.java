/*
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
 * Creation date: 11.03.2009
 */

package amuse.scheduler.gui.processing;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

import amuse.scheduler.gui.views.WizardView;

/**
 * A Text Field indicating valid content by its color.
 * @author Clemens Waeltken
 */
public class JTextFieldWithValidation extends JTextField {

	private static final long serialVersionUID = -2010039159064687L;
	private static final Color validColor = WizardView.VALID_COLOR;
    private static final Color invalidColor = WizardView.INVALID_COLOR;
    private final String pattern;

    public JTextFieldWithValidation(String currentValue, String validationExpression) {
    	super(10);
        pattern = validationExpression;
        super.setText(currentValue);
        setColor();
        this.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                setColor();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                setColor();
            }
        });
    }
    
    @Override
	public void setText(String text) {
		super.setText(text);
		setColor();
	}

    public void setColor() {
        if (this.getText().matches(pattern)) {
            this.setForeground(validColor);
        } else {
            this.setForeground(invalidColor);
        }
    }

}
