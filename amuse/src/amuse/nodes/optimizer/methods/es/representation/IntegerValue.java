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
package amuse.nodes.optimizer.methods.es.representation;

import amuse.nodes.optimizer.methods.es.ESConfiguration;
import amuse.nodes.optimizer.methods.es.representation.interfaces.AbstractRepresentation;

/**
 * This representation consists of an integer value with given max/min boundaries
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class IntegerValue extends AbstractRepresentation {

	/** Integer value */
	Integer value;
	
	/** Allowed upper bound */
	Integer max;
	
	/** Allowed lower bound */
	Integer min;
	
	/**
	 * Empty constructor, used for createValueFromString method in ESIndividual.initializeFromLog()
	 */
	public IntegerValue() {
		value = null;
	}
	
	/**
	 * Standard constructor
	 */
	public IntegerValue(ESConfiguration esConfiguration, Integer value, Integer max, Integer min) {
		super(esConfiguration);
		this.value = value;
		this.max = max;
		this.min = min;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface#getValue()
	 */
	public Integer getValue() {
		return value;
	}
	
	/**
	 * Sets the integer value
	 * @param value Integer value
	 */
	public void setValue(Integer value) {
		this.value = value;
	}
	
	/**
	 * @return Allowed upper bound
	 */
	public Integer getMax() {
		return max;
	}
	
	/**
	 * @return Allowed lower bound
	 */
	public Integer getMin() {
		return min;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.representation.AbstractRepresentation#clone()
	 */
	public IntegerValue clone() {
		Integer valueCopy = new Integer(this.value);
		Integer maxCopy = new Integer(this.max);
		Integer minCopy = new Integer(this.min);
		return new IntegerValue(esConfiguration, valueCopy, maxCopy, minCopy);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return value.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface#createValueFromString(java.lang.String)
	 */
	public Object createValueFromString(String s) {
		return new Integer(s);
	}
	
}
