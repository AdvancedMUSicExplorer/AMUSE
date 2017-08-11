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

import java.util.StringTokenizer;

import amuse.nodes.optimizer.methods.es.ESConfiguration;
import amuse.nodes.optimizer.methods.es.representation.interfaces.AbstractRepresentation;

/**
 * This representation consists of a vector of integer values
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class IntegerVector extends AbstractRepresentation {

	/** Vector with integer values */
	Integer[] vector;
	
	/**
	 * Empty constructor, used for createValueFromString method in ESIndividual.initializeFromLog()
	 */
	public IntegerVector() {
		vector = null;
	}
	
	/**
	 * Standard constructor
	 */
	public IntegerVector(ESConfiguration esConfiguration, Integer[] vector) {
		super(esConfiguration);
		this.vector = vector;
	}
	
	/**
	 * Returns the vector
	 * @return Vector with integer values
	 */
	public Integer[] getValue() {
		return vector;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.representation.AbstractRepresentation#clone()
	 */
	public IntegerVector clone() {
		Integer[] vectorCopy = new Integer[vector.length];
		for(int i=0;i<vector.length;i++) {
			vectorCopy[i] = new Integer(vector[i]);
		}
		return new IntegerVector(esConfiguration, vectorCopy);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<vector.length;i++) {
			sb.append(vector[i] + " ");
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface#createValueFromString(java.lang.String)
	 */
	public Object createValueFromString(String s) {
		StringTokenizer tok = new StringTokenizer(s, " ");
		Integer[] vector = new Integer[tok.countTokens()];
		for(int k=0;k<s.length();k++) {
    		vector[k] = new Integer(tok.nextToken());
    	}
		return vector;
	}
	
}
