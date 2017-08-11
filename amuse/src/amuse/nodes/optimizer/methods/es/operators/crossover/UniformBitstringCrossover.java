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
package amuse.nodes.optimizer.methods.es.operators.crossover;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.apache.log4j.Level;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.optimizer.methods.es.ESConfiguration;
import amuse.nodes.optimizer.methods.es.EvolutionaryStrategy;
import amuse.nodes.optimizer.methods.es.operators.crossover.interfaces.AbstractCrossover;
import amuse.nodes.optimizer.methods.es.representation.BinaryVector;
import amuse.nodes.optimizer.methods.es.representation.interfaces.AbstractRepresentation;
import amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface;
import amuse.util.AmuseLogger;

/**
 * Uniform bit string crossover
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class UniformBitstringCrossover extends AbstractCrossover {

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#mutate(amuse.nodes.optimizer.methods.es.representation.AbstractRepresentation)
	 */
	public RepresentationInterface[] crossover(RepresentationInterface[] representation) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Uniform bit string crossover started");
		
		// Check the preliminary conditions:
		// (a) all parents must be bit strings!
		for(RepresentationInterface r : representation) {
			if(! (r instanceof BinaryVector)) {
				throw new NodeException("Representation class (" + representation.getClass().toString() + ") must be BinaryVector!");
			}
		}
		int length = ((BinaryVector)representation[0]).getValue().length;
		// (b) number of parents must match the settings
		if(representation.length != parentNumber) {
			throw new NodeException("Parent number (here: " + representation.length + ") must be equal to " + parentNumber);
		}
		
		Boolean[][] newString = new Boolean[offspringNumber][length];
		Random rand = new Random();
		for(int i=0;i<parentNumber;i++) {
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Parent " + i + ": " + ((BinaryVector)representation[i]).toString());
		}
			
		// Run crossover going through all bits
		for(int currentBit=0;currentBit<length;currentBit++) {
				
			// Select the parent which bit value will be inherited for the current offspring
			for(int currentOffspring=0;currentOffspring<offspringNumber;currentOffspring++) {
				double r = rand.nextDouble();
				int parent = new Double(r / (1d / new Double(parentNumber))).intValue();
				newString[currentOffspring][currentBit] = ((BinaryVector)representation[parent]).getValue()[currentBit];
			}
		}
			
		// Check if the new representations have at least one feature; otherwise add a random feature!
		for(int currentOffspring=0;currentOffspring<offspringNumber;currentOffspring++) {
			boolean added = false;
			for(int i=0;i<newString.length;i++) {
				if(newString[currentOffspring][i] == true) {
					added = true;
					break;
				}
			}
			if(!added) {
				newString[currentOffspring][rand.nextInt(newString.length)] = true;
			}
		}
		
		// Create the new children 
		RepresentationInterface[] children = new RepresentationInterface[offspringNumber];
		for(int currentOffspring=0;currentOffspring<offspringNumber;currentOffspring++) {
			
			// Create the string representation of the corresponding offspring
			StringBuffer b = new StringBuffer();
			for(int i=0;i<newString[currentOffspring].length;i++) {
				if(newString[currentOffspring][i]) {
					b.append("1"); 
				} else {
					b.append("0");
				}
			}
			String currentSolution = b.toString();
			
			// Create the representation class similar to input representation
			try {
				Class<?> representationClass = Class.forName(representation[0].getClass().getName());
				
				// Set parameters for constructor to search for
				Class<?> partypes[] = new Class[2];
	            partypes[0] = ESConfiguration.class;
	            Class<?>[] classList = null;
	            
	            // The second parameter is dependent on concrete representation, e.g. BinaryVector uses Boolean[]
	            partypes[1] = representationClass.getMethod("getValue", classList).getReturnType();
	            Constructor<?> ct = representationClass.getConstructor(partypes);
	            Object correspondingValue = null;
	           	RepresentationInterface ri = (RepresentationInterface)representationClass.newInstance();
	           	correspondingValue = ri.createValueFromString(currentSolution);
				Object[] initArgs = {((BinaryVector)representation[0]).esConfiguration, correspondingValue};
				children[currentOffspring] = (AbstractRepresentation)ct.newInstance(initArgs);
			} catch (Exception e) {
				throw new NodeException("Could not create crossover output: " + e.getMessage());
			} 
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Child " + currentOffspring + ": " + children[currentOffspring].toString());
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Uniform bit string crossover finished");
		return children;
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
				if(parameterName.equals(new String("parentNumber"))) {
					parentNumber = new Integer(parameters.item(i).getAttributes().getNamedItem("intValue").getNodeValue());
				} else if(parameterName.equals(new String("offspringNumber"))) {
					offspringNumber = new Integer(parameters.item(i).getAttributes().getNamedItem("intValue").getNodeValue());
				} 
			}
		}
	}

}
