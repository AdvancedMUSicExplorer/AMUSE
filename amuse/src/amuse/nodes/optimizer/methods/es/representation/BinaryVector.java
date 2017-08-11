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
 * This representation consists of a vector of boolean values
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class BinaryVector extends AbstractRepresentation {

	/** Vector with boolean values */
	Boolean[] vector;
	
	/**
	 * Empty constructor, used for createValueFromString method in ESIndividual.initializeFromLog()
	 */
	public BinaryVector() {
		vector = null;
	}
	
	/**
	 * Standard constructor
	 */
	public BinaryVector(ESConfiguration esConfiguration, Boolean[] vector) {
		super(esConfiguration);
		this.vector = vector;
	}
	
	/**
	 * Returns the vector
	 * @return Vector with boolean values
	 */
	public Boolean[] getValue() {
		return vector;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.representation.AbstractRepresentation#clone()
	 */
	public BinaryVector clone() {
		Boolean[] valueCopy = new Boolean[vector.length];
		for(int i=0;i<valueCopy.length;i++) {
			valueCopy[i] = new Boolean(vector[i]);
		}
		return new BinaryVector(esConfiguration, valueCopy);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<vector.length;i++) {
			sb.append(vector[i] ? "1" : "0");
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface#createValueFromString(java.lang.String)
	 */
	public Object createValueFromString(String s) {
		Boolean[] vector = new Boolean[s.length()];
		for(int k=0;k<s.length();k++) {
    		if(s.charAt(k) == '1') {
    			vector[k] = true;
    		} else {
    			vector[k] = false;
    		}
    	}
		return vector;
	}
	
}
