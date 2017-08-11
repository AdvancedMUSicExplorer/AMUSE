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
 * Creation date: 26.05.2011
 */
package amuse.nodes.optimizer.methods.es.operators.crossover.interfaces;

import org.w3c.dom.NodeList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.optimizer.methods.es.EvolutionaryStrategy;
import amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface;

/**
 * Each crossover operator must implement this interface
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public interface CrossoverInterface {

	/**
	 * Sets the parameters of this crossover
	 * @param parameters Parameters of this crossover
	 */
	public void setParameters(NodeList parameters, EvolutionaryStrategy correspondingStrategy) throws NodeException;
	
	/**
	 * Does the crossover
	 */
	public RepresentationInterface[] crossover(RepresentationInterface[] representation) throws NodeException;
	
	/**
	 * Returns the parent number
	 */
	public int getParentNumber();
	
	/**
	 * Returns the offspring number
	 */
	public int getOffspringNumber();
}
