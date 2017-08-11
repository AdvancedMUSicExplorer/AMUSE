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
 * Creation date: 05.01.2010
 */
package amuse.nodes.optimizer.methods.es.representation.interfaces;

import amuse.nodes.optimizer.methods.es.ESConfiguration;

/**
 * All ES representations must extend this abstract class
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public abstract class AbstractRepresentation implements RepresentationInterface, Cloneable {
	
	/** Configuration of a corresponding ES */
	public ESConfiguration esConfiguration;
	
	/**
	 * Empty constructor, used for createValueFromString method in ESIndividual.initializeFromLog()
	 */
	public AbstractRepresentation() {
	}
	
	/**
     * Standard constructor, generates a random representation value
	 * @param esConfiguration Configuration of a corresponding ES
	 */
	public AbstractRepresentation(ESConfiguration esConfiguration) {
		this.esConfiguration = esConfiguration;
	}
	
	/**
	 * Constructor which uses a given representation value
	 * @param esConfiguration Configuration of a corresponding ES
	 * @param value Given representation value
	 */
	public AbstractRepresentation(ESConfiguration esConfiguration, Object value) {
		this.esConfiguration = esConfiguration;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface#createValueFromString(java.lang.String)
	 */
	public Object createValueFromString(String s) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public AbstractRepresentation clone() {
		return null;
	}

}
