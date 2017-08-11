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
 * Creation date: 20.01.2010
 */
package amuse.nodes.optimizer.methods.es.operators.mutation;

import java.util.Random;

import org.apache.log4j.Level;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.optimizer.methods.es.EvolutionaryStrategy;
import amuse.nodes.optimizer.methods.es.operators.mutation.interfaces.AbstractMutation;
import amuse.nodes.optimizer.methods.es.representation.BinaryVector;
import amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface;
import amuse.util.AmuseLogger;

/**
 * Random bit flip mutation
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class RandomBitFlip extends AbstractMutation {

	/** Parameters from ESConfiguration which are saved here for faster processing */
	double gamma;
	double alpha = -1;
	double boundaryForSelfAdaptation;
	boolean increaseForHigherSuccessRate;
	int intervalForSuccessRateCalculation;
	
	/** Depending on the previous successes, the bit flip probability is multiplied by this factor */
	public double selfAdaptationFactor;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#mutate(amuse.nodes.optimizer.methods.es.representation.AbstractRepresentation)
	 */
	public void mutate(RepresentationInterface representation) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Random bit flip mutation started");
		if(representation instanceof BinaryVector) {
			BinaryVector valueToMutate = (BinaryVector)representation;
			Random rand = new Random();
			double mutationProbability = this.selfAdaptationFactor * gamma / valueToMutate.getValue().length;
			selfAdaptation();
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Current value: " + valueToMutate.toString());
			for(int i=0;i<valueToMutate.getValue().length;i++) {
				
				// Perform mutation with probability gamma/n for each feature (n = number of all features)
				if(rand.nextDouble() < mutationProbability) {
					
					if(valueToMutate.getValue()[i]) {
						valueToMutate.getValue()[i] = false;
					} else  {
						valueToMutate.getValue()[i] = true;
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
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Random bit flip mutation finished");
	}
	
	/**
	 * Adjust the mutation probability due to self-adaptation concept
	 */
	protected void selfAdaptation() {
		if(alpha != -1 && (correspondingES.currentGeneration + 1) % intervalForSuccessRateCalculation == 0) {
			double successRate = ((Integer)correspondingES.currentSuccessCounter).doubleValue() / 
				(((Integer)intervalForSuccessRateCalculation).doubleValue()*correspondingES.offspringPopSize);
			
			// Increase or decrease the mutation probability?
			if(increaseForHigherSuccessRate) {
				if(successRate > boundaryForSelfAdaptation) {
					selfAdaptationFactor *= alpha;
				} else if(successRate < boundaryForSelfAdaptation) {
					selfAdaptationFactor /= alpha;
				}
			} else {
				if(successRate > boundaryForSelfAdaptation) {
					selfAdaptationFactor /= alpha;
				} else if(successRate < boundaryForSelfAdaptation) {
					selfAdaptationFactor *= alpha;
				}
			}
			correspondingES.currentSuccessCounter = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#setParameters(org.w3c.dom.NodeList)
	 */
	public void setParameters(NodeList parameters, EvolutionaryStrategy correspondingStrategy) throws NodeException {
		this.correspondingES = correspondingStrategy;
		
		for(int i=0;i<parameters.getLength();i++) {
			if(parameters.item(i).getNodeType() == Node.ELEMENT_NODE) {
				String parameterName = parameters.item(i).getAttributes().getNamedItem("name").getNodeValue();
				if(parameterName.equals(new String("gamma"))) {
					gamma = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				} else if(parameterName.equals(new String("alpha"))) {
					alpha = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				} else if(parameterName.equals(new String("boundaryForSelfAdaptation"))) {
					boundaryForSelfAdaptation = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				} else if(parameterName.equals(new String("increaseForHigherSuccessRate"))) {
					increaseForHigherSuccessRate = new Boolean(parameters.item(i).getAttributes().getNamedItem("booleanValue").getNodeValue());
				} else if(parameterName.equals(new String("intervalForSuccessRateCalculation"))) {
					intervalForSuccessRateCalculation = new Integer(parameters.item(i).getAttributes().getNamedItem("intValue").getNodeValue());
				}
			}
		}
		this.selfAdaptationFactor = 1d;
	}

}
