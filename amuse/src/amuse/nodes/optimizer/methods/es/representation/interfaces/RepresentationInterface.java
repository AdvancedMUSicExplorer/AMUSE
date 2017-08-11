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

/**
 * All ES representations must implement this interface
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public interface RepresentationInterface {
	
	/**
	 * @return Representation as string
	 */
	public String toString();
	
	/**
	 * @return Java object of this representation
	 */
	public Object getValue();
	
	/**
	 * Creates corresponding Java object from string. It is used e.g. if a representation is
	 * created from data stored in log file 
	 * @param s String with description of this representation
	 * @return Java object generated from given string
	 */
	public Object createValueFromString(String s);
}
