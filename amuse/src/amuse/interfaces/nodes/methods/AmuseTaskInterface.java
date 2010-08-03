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
 * Creation date: 18.03.2008
 */ 
package amuse.interfaces.nodes.methods;

import amuse.interfaces.nodes.NodeException;

/**
 * All algorithms implement this interface. The algorithm runs one of Amuse tasks and is "method" 
 * (completely implemented in Amuse) or "adapter" (runs an external tool). 
 * 
 * @author Igor Vatolkin
 * @version $Id: AmuseTaskInterface.java 14 2008-04-11 10:13:58Z vatolkin $
 */
public interface AmuseTaskInterface {
	
	/**
	 * Sets the required parameters for this algorithm
	 * @param parameterString
	 * @throws NodeException
	 */
	public void setParameters(String parameterString) throws NodeException;
	
	/**
	 * If required, any initialization routines should be done here
	 * @throws NodeException
	 */
	public void initialize() throws NodeException;
	
}
