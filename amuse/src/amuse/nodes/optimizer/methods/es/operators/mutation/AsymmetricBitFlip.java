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
 * Creation date: 17.02.2010
 */
package amuse.nodes.optimizer.methods.es.operators.mutation;

import java.util.Random;

import org.apache.log4j.Level;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.optimizer.methods.es.EvolutionaryStrategy;
import amuse.nodes.optimizer.methods.es.representation.BinaryVector;
import amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface;
import amuse.util.AmuseLogger;

/**
 * Asymmetric bit flip mutation
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class AsymmetricBitFlip extends RandomBitFlip {

	/** Parameters from ESConfiguration which are saved here for faster processing */
	double p_01;
	double p_10;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#mutate(amuse.nodes.optimizer.methods.es.representation.AbstractRepresentation)
	 */
	public void mutate(RepresentationInterface representation) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Asymmetric bit flip mutation started");
		if(representation instanceof BinaryVector) {
			BinaryVector valueToMutate = (BinaryVector)representation;
			Random rand = new Random();
			selfAdaptation();
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Current value: " + valueToMutate.toString());
			for(int i=0;i<valueToMutate.getValue().length;i++) {
				
				// Perform 0 -> 1 mutation?
				if(!valueToMutate.getValue()[i]) {
					double mutationProbability = (p_01*gamma) / valueToMutate.getValue().length;
					if(rand.nextDouble() < mutationProbability) {
						valueToMutate.getValue()[i] = true;
					}
				}
				
				// Perform 1 -> 0 mutation?
				else {
					double mutationProbability = (p_10*gamma) / valueToMutate.getValue().length;
					if(rand.nextDouble() < mutationProbability) {
						valueToMutate.getValue()[i] = false;
					}
				}
			}
			
			// Check if the new individual has at least one feature; otherwise add a random feature!
			boolean added = false;
			for(int i=0;i<valueToMutate.getValue().length;i++) {
				if(valueToMutate.getValue()[i] == true) {
					added = true;
					break;
				}
			}
			if(!added) {
				valueToMutate.getValue()[rand.nextInt(valueToMutate.getValue().length)] = true;
			}
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Mutated value: " + valueToMutate.toString());
		} else {
			throw new NodeException("Representation class (" + representation.getClass().toString() + ") must be BinaryVector!");
		}
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Asymmetric bit flip mutation finished");
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#setParameters(org.w3c.dom.NodeList)
	 */
	public void setParameters(NodeList parameters, EvolutionaryStrategy correspondingStrategy) throws NodeException {
		super.setParameters(parameters, correspondingStrategy);
		for(int i=0;i<parameters.getLength();i++) {
			if(parameters.item(i).getNodeType() == Node.ELEMENT_NODE) {
				String parameterName = parameters.item(i).getAttributes().getNamedItem("name").getNodeValue();
				if(parameterName.equals(new String("p_01"))) {
					p_01 = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				} else if(parameterName.equals(new String("p_10"))) {
					p_10 = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				}
			}
		}
	}

}
