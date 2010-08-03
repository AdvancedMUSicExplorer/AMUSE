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

/**
 * This interface is used in combination with <code>JPanelAmuseNavigator</code> to allow certain JPanels in the Wizard to have different "Next" button behavior.
 * @author Clemens Waeltken
 *
 */
public interface NextButtonUsable {
	
	/**
     * This method should delegate the event to the current component displayed by the <code>AmuseNavigator</code> and trigger the correct behavior.
     * @return Return true if you want the navigator to trigger it's default behavior.
     */
	public boolean nextButtonClicked();
	/**
	 * This method should return the text to be displayed on the "Next" button.
	 * @return - <b>String</b> containing the button text to be displayed in the <code>AmuseNavigator</code>.
	 */
	public String getNextButtonText();
}
