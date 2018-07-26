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
 * Creation date: 30.12.2009
 */
package amuse.nodes.optimizer.methods.es;

import java.io.File;
import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.optimizer.methods.es.representation.interfaces.AbstractRepresentation;
import amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * ES individual
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ESIndividual {
	
	/** Corresponding ES algorithm */
	private EvolutionaryStrategy correspondingES;
	
	/** Representations of parameters to optimize */
	private ArrayList<RepresentationInterface> representationList;
	
	/**
	 * Standard constructor
	 * @param es Corresponding ES algorithm
	 */
	public ESIndividual(EvolutionaryStrategy es) {
		correspondingES = es;
		representationList = new ArrayList<RepresentationInterface>(correspondingES.getConfiguration().getParametersToOptimize().getChildNodes().getLength());
	}
	
	/**
	 * Initializes this individual as a new individual (setting all representations to e.g. random values)
	 */
	public void initialize() throws NodeException {
		
		// Nodes: <FE> for feature extraction, <FP> for feature processing, <C> for classification
		NodeList amuseTasksToOptimize = correspondingES.getConfiguration().getParametersToOptimize().getChildNodes();
		
		// Go through all nodes which belong to <problemParametersToOptimize>
		for(int i=0;i<amuseTasksToOptimize.getLength();i++) {
			if(amuseTasksToOptimize.item(i).getNodeType() == Node.ELEMENT_NODE){
				try {
					Node currentTaskToOptimize = amuseTasksToOptimize.item(i);
					NodeList parametersToOptimize = currentTaskToOptimize.getChildNodes();
					
					// Go through all parameters from the current AMUSE task which should be optimized
					for(int j=0;j<parametersToOptimize.getLength();j++) {
						if(parametersToOptimize.item(j).getNodeType() == Node.ELEMENT_NODE){
							NamedNodeMap currentParameterAttributes = parametersToOptimize.item(j).getAttributes();
							Class<?> representationClass = Class.forName(currentParameterAttributes.getNamedItem("classValue").getNodeValue());
							Class<?> partypes[] = new Class[1];
				            partypes[0] = ESConfiguration.class;
				            Constructor<?> ct = representationClass.getConstructor(partypes);
							Object[] initArgs = {correspondingES.esConfiguration};
							representationList.add((AbstractRepresentation)ct.newInstance(initArgs));
						}
					}
				} catch (Exception e) {
					throw new NodeException("Could not initialize new individual: " + e.getMessage());
				} 
			}
		}
	}
	
	/**
	 * Initializes this individual from a log file of a previous experiment 
	 * @param logFile Log file of a previous experiment
	 * @param individualNumber Number of the individual in the log file
	 */
	public void initializeFromLog(File logFile, int individualNumber) throws NodeException {
		
		// Nodes: <FE> for feature extraction, <FP> for feature processing, <C> for classification
		NodeList amuseTasksToOptimize = correspondingES.getConfiguration().getParametersToOptimize().getChildNodes();
		
		// Go through all nodes which belong to <problemParametersToOptimize>
		for(int i=0;i<amuseTasksToOptimize.getLength();i++) {
			if(amuseTasksToOptimize.item(i).getNodeType() == Node.ELEMENT_NODE){
				try {
					Node currentTaskToOptimize = amuseTasksToOptimize.item(i);
					NodeList parametersToOptimize = currentTaskToOptimize.getChildNodes();
					
					// Go through all parameters from the current AMUSE task which should be optimized
					for(int j=0;j<parametersToOptimize.getLength();j++) {
						if(parametersToOptimize.item(j).getNodeType() == Node.ELEMENT_NODE){
							NamedNodeMap currentParameterAttributes = parametersToOptimize.item(j).getAttributes();
							Class<?> representationClass = Class.forName(currentParameterAttributes.getNamedItem("classValue").getNodeValue());
							
							// Set parameters for constructor to search for
							Class<?> partypes[] = new Class[2];
				            partypes[0] = ESConfiguration.class;
				            Class<?>[] classList = null;
				            
				            // The second parameter is dependent on concrete representation, e.g. BinaryVector uses Boolean[]
				            partypes[1] = representationClass.getMethod("getValue", classList).getReturnType();
				            Constructor<?> ct = representationClass.getConstructor(partypes);
				            
				            // Load the corresponding representation value from the log file
				            DataSetAbstract loggingSet = new ArffDataSet(logFile);
				            String attributeToSearchFor = new String("Representation " + representationClass.getSimpleName() + 
				            	" of individual " + individualNumber);
				            Object correspondingValue = null;
				            if(loggingSet.getAttributeNames().contains(attributeToSearchFor)) {
				        	   
				            	// Load the last appropriate representation value from log file and create the corresponding object from string
				            	String val = loggingSet.getAttribute(attributeToSearchFor).getValueAt(loggingSet.getValueCount()-1).toString();
				            	RepresentationInterface ri = (RepresentationInterface)representationClass.newInstance();
				            	correspondingValue = ri.createValueFromString(val);
				            	
				            } else {
				        	    throw new NodeException("Could not load the individual representation from log file " + 
				        		 	   logFile.getAbsolutePath() + " ; attribute '" + attributeToSearchFor + 
				        			   "' is missing!");
				            }
 				            
							Object[] initArgs = {correspondingES.esConfiguration, correspondingValue};
							representationList.add((AbstractRepresentation)ct.newInstance(initArgs));
						}
					}
				} catch (Exception e) {
					throw new NodeException("Could not initialize individual " + individualNumber + " from log file " + 
							logFile.getAbsolutePath() + ": " + e.getMessage());
				} 
			}
		}
	}
	
	/**
	 * @return List of used representations for optimization parameters
	 */
	public List<RepresentationInterface> getRepresentationList() {
		return representationList;
	}
	
	/**
	 * @return Fitness value of this individual increasing the overall evaluation number of the corresponding ES
	 */
	public ValidationMeasureDouble[] getFitness() throws NodeException {
		correspondingES.currentEvaluation++;
		
		/*ValidationMeasureDouble[] test = new ValidationMeasureDouble[2];
		 TODO for sms-emoa test
		Random r = new Random();
		test[0] = new ValidationMeasureDouble();
		test[0].setValue(r.nextDouble());
		test[1] = new ValidationMeasureDouble();
		test[1].setValue(r.nextDouble());
		return test;*/ 
		return correspondingES.getFitnessEvalualor().getFitness(this,false);
	}
	
	/**
	 * @return Fitness value of this individual on the independent test set. Here the evaluation number of the corresponding
	 * ES does not change since this function is not a part of optimization process
	 */
	public ValidationMeasureDouble[] getFitnessOnIndependentTestSet() throws NodeException {
		return correspondingES.getFitnessEvalualor().getFitness(this,true);
	}
	
	/**
	 * @return Corresponding ES
	 */
	public EvolutionaryStrategy getCorrespondingES() {
		return correspondingES;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public ESIndividual clone() {
		ESIndividual ind = new ESIndividual(this.correspondingES);
		for(RepresentationInterface re : representationList) {
			ind.representationList.add(((AbstractRepresentation)re).clone());
		}
		return ind;
	}
}
