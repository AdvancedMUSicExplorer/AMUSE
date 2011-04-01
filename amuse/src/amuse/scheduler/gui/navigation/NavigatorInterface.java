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
 * Creation date: 18.02.2009
 */

package amuse.scheduler.gui.navigation;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Interface for the navigation.
 * @author Clemens Waeltken
 */
public interface NavigatorInterface {

    /**
     * @return returns a JPanel diplaying the navigation bar.
     */
    public JPanel getNavBar();

    /**
     * Navigate back.
     */
    void goBack();

    /**
     * Navigate forward.
     */
    void goForward();

    /**
     * Set new content and move the old contetn onto the undo stack.
     * Discard all forward steps.
     * @param content the new Content.
     */
    void setNewContent(JComponent content);
    
}
