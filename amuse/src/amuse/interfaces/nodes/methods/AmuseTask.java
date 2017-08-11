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
 * Creation date: 19.01.2007
 */ 
package amuse.interfaces.nodes.methods;

import java.util.Properties;

import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.NodeScheduler;

/**
 * All algorithms should extend this class. The algorithm runs one of Amuse tasks and is "method" 
 * (completely implemented in Amuse) or "adapter" (runs an external tool). 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public abstract class AmuseTask implements AmuseTaskInterface {
	
	/** Configuration properties */
	protected Properties properties;
	
	/** Corresponding node scheduler */
	protected NodeScheduler correspondingScheduler;
	
	/**
	 * Configures the Amuse task
	 * 
	 * @param properties Properties
	 * @param correspondingScheduler Node, which started this task
	 * @param parameterString Algorithm parameters if required
	 */
	public void configure(Properties properties, NodeScheduler correspondingScheduler, String parameterString) throws NodeException {
		this.properties = properties;
		this.correspondingScheduler = correspondingScheduler;
		this.setParameters(parameterString);
		this.initialize(); 
	}
	
	/**
	 * Get the properties
	 * 
	 * @return Properties
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Returns the corresponding node scheduler
	 * 
	 * @return Corresponding node scheduler
	 */
	public NodeScheduler getCorrespondingScheduler() {
		return correspondingScheduler;
	}
}
