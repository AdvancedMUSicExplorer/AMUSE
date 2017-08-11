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
import amuse.nodes.optimizer.methods.es.representation.IntegerValue;
import amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface;
import amuse.util.AmuseLogger;

/**
 * Integer mutation as introduced in:
 * Rudolph, G.: An Evolutionary Algorithm for Integer Programming. Parallel Problem Solving from Nature - PPSN III, 139-148 (1994)
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class IntegerMutation extends AbstractMutation {

	/** Parameters from ESConfiguration which are saved here for faster processing */
	double probabilityOfThisMutation;
	double p;
	double stepSize;
	double alpha;
	double adaptationFunctionParameter;
	
	/** Expected step size for the current generation */
	int currentExpectedIntegerMutationStepSize = -1;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#mutate(amuse.nodes.optimizer.methods.es.representation.AbstractRepresentation)
	 */
	public void mutate(RepresentationInterface representation) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Integer mutation started");
		if(representation instanceof IntegerValue) {
			
			IntegerValue valueToMutate = (IntegerValue)representation;
			
			// If the mutation is run for the first time, set the expected step size to the half of the maximal step size at the beginning 
			if(currentExpectedIntegerMutationStepSize == -1) {
				if(this.correspondingES.currentGeneration>0) {
					currentExpectedIntegerMutationStepSize = 
						( valueToMutate.getMax() - valueToMutate.getMin() ) / 2;
				} else {
					
					// TODO If a run is continued from older log file, the expected step size must be read from it!
					throw new NodeException("IntegerMutation with self-adaptation for runs continued from previous experiments " +
							"is not supported currently!");
				}
			}
			
			Random rand = new Random();
			if(rand.nextDouble() < probabilityOfThisMutation) {
				
				// If a function is used for adaptation of expected step size, calculate it
				if(alpha == -1) { 	
					currentExpectedIntegerMutationStepSize = new Double(15000*Math.pow(adaptationFunctionParameter, correspondingES.currentGeneration) +
						correspondingES.currentGeneration*(1-15000*Math.pow(adaptationFunctionParameter,correspondingES.currentGeneration))/(correspondingES.generationLimit-1)).intValue();
				} else {
					
					// 1/5-rule self-adaptation with alpha parameter
					// TODO
					// Currently self-adaptation is done every 5th generation.
					// However other possibilities (parameter!) can be considered
					if((correspondingES.currentGeneration + 1) % 5 == 0) {
						
						double successRate = ((Integer)correspondingES.currentSuccessCounter).doubleValue() / (5d*correspondingES.offspringPopSize);
						
						if(successRate > 1d/5d) {
							currentExpectedIntegerMutationStepSize *= alpha;
						} else if(successRate == 1d/5d) {
							currentExpectedIntegerMutationStepSize /= alpha;
						} 
						correspondingES.currentSuccessCounter = 0;
					}
				}

				double p = 1 - (currentExpectedIntegerMutationStepSize / (Math.sqrt(1 + 
						currentExpectedIntegerMutationStepSize*currentExpectedIntegerMutationStepSize) + 1));
				double u1 = rand.nextDouble();
				double u2 = rand.nextDouble();
				int g1 = new Double(Math.floor(Math.log10(1.0d-u1) / Math.log10(1.0d-p))).intValue();
				int g2 = new Double(Math.floor(Math.log10(1.0d-u2) / Math.log10(1.0d-p))).intValue();
				
				// Step size for mutation (e.g. g == -4 means take four steps to the left)
				int g = g1 - g2;
				if(g == 0) {
					if(rand.nextDouble() > 0.5) {
						g = 1;
					} else {
						g = -1;
					}
				} else {
					g = g + new Float(Math.signum(new Integer(g).floatValue())).intValue();
				}
					
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Current expected step size: " + currentExpectedIntegerMutationStepSize);
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "p: " + p);
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Steps to do: " + g);
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Current value: " + valueToMutate.toString());
				if((valueToMutate.getValue() + g*stepSize) > valueToMutate.getMax()) {
					valueToMutate.setValue(valueToMutate.getMax());
				} else if((valueToMutate.getValue() + g*stepSize) < valueToMutate.getMin()) {
					valueToMutate.setValue(valueToMutate.getMin());
				} else {
					valueToMutate.setValue(new Double(valueToMutate.getValue() + g*stepSize).intValue());
				}
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Mutated value: " + valueToMutate.toString());
			} 
		} else {
			throw new NodeException("Representation class (" + representation.getClass().toString() + ") must be IntValue!");
		}
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Integer mutation finished");
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
				if(parameterName.equals(new String("Probability"))) {
					probabilityOfThisMutation = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				} else if(parameterName.equals(new String("p"))) {
					p = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				} else if(parameterName.equals(new String("Step size"))) {
					stepSize = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				} else if(parameterName.equals(new String("alpha"))) {
					alpha = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				} else if(parameterName.equals(new String("Adaptation function parameter"))) {
					adaptationFunctionParameter = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				}
			}
		}
	}

}
