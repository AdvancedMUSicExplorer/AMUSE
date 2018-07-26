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
 * Creation date: 29.06.2010
 */
package amuse.nodes.optimizer.methods.es.operators.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import amuse.nodes.optimizer.methods.es.ESIndividual;
import amuse.nodes.optimizer.methods.es.EvolutionaryStrategy;
import amuse.nodes.optimizer.methods.es.operators.selection.interfaces.AbstractSelection;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Hypervolume-strategy: updates the population due to the individual hypervolume contribution (delta S-measure). 
 * 
 * For details of SMS-EMOA see: Michael Emmerich, Nicola Beume, and Boris Naujoks. An EMO algorithm using the hypervolume 
 * measure as selection criterion. In: C. A. Coello Coello et al., Eds., Proc. Evolutionary Multi-Criterion Optimization, 
 * 3rd Int'l Conf. (EMO 2005), LNCS 3410, pp. 62-76. Springer, Berlin, 2005.
 *
 * @author Igor Vatolkin
 * @version $Id$
 */
public class HypervolumeSelection extends AbstractSelection {

	/** List with all individuals */
	private ArrayList<Integer>individuals = null;
	
	ArrayList<ArrayList<Integer>> fronts = null;
	
	public HypervolumeSelection(EvolutionaryStrategy correspondingES) {
		super(correspondingES);
	}
	
	// TODO Provide better implementation
	private class OneFitnessValue implements Comparable<OneFitnessValue> {
		
		public int individualNumber;
		public double value;
		
		public OneFitnessValue(int i, double val) {
			individualNumber = i;
			value = val;
		}
		
		public int compareTo(OneFitnessValue object) {
			OneFitnessValue valueToCompare = object;
	        if (value <= valueToCompare.value) {
	            return -1;
	        } else {
	            return 1;
	        }
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.selection.interfaces.SelectionInterface#replaceParentPopulation()
	 */
	public int replaceParentPopulation() {
		
		// Individual indices are sorted in the way so that at first comes the parent population
		// (individual 0 to individual popSize-1) and then offspring population (popSize to popSize+offspringSize-1)
		individuals = new ArrayList<Integer>(correspondingES.popSize + correspondingES.offspringPopSize);
		for(int i=0;i<correspondingES.popSize + correspondingES.offspringPopSize;i++) {
			individuals.add(i);
		}
		
		// Calculate the fronts
		fronts = fastNondominatedSorting();
		
		// Calculate delta S values for the worst front
		HashMap<Integer,Double> individualOfWorstFront2DeltaS = calculateDeltaS();
		
		// Search for the worst individual (with the smallest delta S) from the worst front
		int worstIndividualIndex = -1;
		double worstIndividualValue = Double.POSITIVE_INFINITY;
		Iterator<Integer> keyIt = individualOfWorstFront2DeltaS.keySet().iterator();		
		while(keyIt.hasNext()) {
			Integer nextIndividualIndex = keyIt.next();
			if(individualOfWorstFront2DeltaS.get(nextIndividualIndex) <= worstIndividualValue) {
				worstIndividualIndex = nextIndividualIndex;
				worstIndividualValue = individualOfWorstFront2DeltaS.get(nextIndividualIndex);
			}
		}
		
		// Replace parent population only if the worst individual was not the child individual
		if(worstIndividualIndex < correspondingES.popSize) {
			ESIndividual newParent = correspondingES.offspringPopulation[0].clone();
			ValidationMeasureDouble[] newFitness = correspondingES.offspringPopulationFitnessValues[0];
			ValidationMeasureDouble[] newFitnessOnTestSet = null;
			if(correspondingES.isIndependentTestSetUsed) {
				newFitnessOnTestSet = correspondingES.offspringPopulationFitnessValuesOnTestSet[0];
			}
			
			correspondingES.population[worstIndividualIndex] = newParent;
			correspondingES.populationFitnessValues[worstIndividualIndex] = newFitness;
			if(correspondingES.isIndependentTestSetUsed) {
				correspondingES.populationFitnessValuesOnTestSet[worstIndividualIndex] = newFitnessOnTestSet;
			}
			return 1;
		}
		return 0;
	}

	private ArrayList<ArrayList<Integer>> fastNondominatedSorting() {
		
		// fronts.get(0) contains the individual indices of the best front; fronts.get(1) from the second-best front etc.
		ArrayList<ArrayList<Integer>> fronts = new ArrayList<ArrayList<Integer>>();
		fronts.add(new ArrayList<Integer>());
		
		int individualNumber = correspondingES.popSize + correspondingES.offspringPopSize;
		
		// The number of the individuals, which dominate the current individual, are stored here
    	int[] numberOfBetterIndividuals = new int[individualNumber];
    	for(int i=0;i<individualNumber;i++) {
    		numberOfBetterIndividuals[i] = 0;
    	}
    	
    	// The indices of the individuals, which are dominated by the current individual, are stored here
    	ArrayList<ArrayList<Integer>> indexesOfDominatedIndividuals = new ArrayList<ArrayList<Integer>>();
    	for(int i=0;i<individualNumber;i++) {
    		indexesOfDominatedIndividuals.add(new ArrayList<Integer>());
    	}
    	
    	// (1) Calculate the dominance information
    	for(int i=0;i<individualNumber;i++) {
    		for(int j=0;j<individualNumber;j++) {
    			
    			// If i dominates j then include j in indexesOfDominatedIndividuals
    			if(dominates(i,j)) {
    				(indexesOfDominatedIndividuals.get(i)).add(new Integer(j));
    			}
    			
    			// Else increment numberOfBetterIndividuals
    			else if(dominates(j,i)) {
    				numberOfBetterIndividuals[i]++;
    			}
    		}
    		
    		// If no solution dominates p then p is a member of the first front
    		if(numberOfBetterIndividuals[i] == 0) {
    			fronts.get(0).add(new Integer(i));
    		}
    	}
    	
    	// (2) Calculate the fronts
    	int indexOfCurrentFront=0;
    	while(true) {
    		ArrayList<Integer> newFront = new ArrayList<Integer>();
        	for(int i=0;i<fronts.get(indexOfCurrentFront).size();i++) {
        		int currentIndividual = fronts.get(indexOfCurrentFront).get(i);
       			
        		// Go through all individuals dominated by the currentIndividual
       			for(int j=0;j<(indexesOfDominatedIndividuals.get(currentIndividual)).size();j++) {
       			
       				// Decrement numberOfBetterIndividuals by one
       				int currentDominatedIndividual = indexesOfDominatedIndividuals.get(currentIndividual).get(j);
       				numberOfBetterIndividuals[currentDominatedIndividual]--;
       				if(numberOfBetterIndividuals[currentDominatedIndividual] == 0) {
       					newFront.add(new Integer(currentDominatedIndividual));
       				}
        		}
        	}
        	indexOfCurrentFront++;
        	if(newFront.size() == 0) {
        		break;
        	} else {
        		fronts.add(newFront);
        	}
    	}
		
		return fronts;
	}
	
	private ValidationMeasureDouble[] getIndividualFitness(int i) {
		return (i<correspondingES.popSize) ? correspondingES.populationFitnessValues[i] : 
			correspondingES.offspringPopulationFitnessValues[i-correspondingES.popSize];
	}
	
	private boolean dominates(int i, int j) {
    	
		// Two conditions must be hold if i dominates j:
    	// (1) i is not worse as j in all dimensions 
    	boolean condition1 = true;
    	// (2) i is better in at least one dimension
    	boolean condition2 = false;
    	
    	ValidationMeasureDouble[] firstFitnessValues = getIndividualFitness(i); 
    	ValidationMeasureDouble[] secondFitnessValues = getIndividualFitness(j);
    	
    	for(int k=0;k<firstFitnessValues.length;k++) {
    		if(firstFitnessValues[k].isForMinimizing()) {
    			if(firstFitnessValues[k].getValue() > secondFitnessValues[k].getValue())
    				condition1 = false;
    			if(firstFitnessValues[k].getValue() < secondFitnessValues[k].getValue())
    				condition2 = true;
    		} else {
    			if(firstFitnessValues[k].getValue() < secondFitnessValues[k].getValue())
    				condition1 = false;
    			if(firstFitnessValues[k].getValue() > secondFitnessValues[k].getValue())
    				condition2 = true;
    		}
    	}
    	if(condition1 && condition2)
    		return true;
    	else
    		return false;
    }
	
	private HashMap<Integer, Double> calculateDeltaS() {
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		
		ArrayList<Integer> worstFront = fronts.get(fronts.size()-1);
		OneFitnessValue[] fitnessValuesForCurrentDimension = new OneFitnessValue[worstFront.size()];
    	
    	// TODO Fitness values for the first dimension of the worst front; currently only two dimensions are supported!!
    	for(int i=0;i<worstFront.size();i++) {
    		fitnessValuesForCurrentDimension[i] = new OneFitnessValue(worstFront.get(i),getIndividualFitness(worstFront.get(i))[0].getValue());
    	}
    	Arrays.sort(fitnessValuesForCurrentDimension);
    	
    	// Boundary individuals have the largest delta S
    	map.put(fitnessValuesForCurrentDimension[0].individualNumber, Double.POSITIVE_INFINITY);
    	map.put(fitnessValuesForCurrentDimension[fitnessValuesForCurrentDimension.length-1].individualNumber, Double.POSITIVE_INFINITY);
    	
    	for(int i=1;i<fitnessValuesForCurrentDimension.length-1;i++) {
    		int currentIndividualIndex = fitnessValuesForCurrentDimension[i].individualNumber;
    		double value1OfIndividual = fitnessValuesForCurrentDimension[i].value;
    		double value1OfIndividualPlus1 = fitnessValuesForCurrentDimension[i+1].value;
    		double value2OfIndividual = getIndividualFitness(currentIndividualIndex)[1].getValue();
    		double value2OfIndividualMinus1 = getIndividualFitness(fitnessValuesForCurrentDimension[i-1].
    				individualNumber)[1].getValue();
    		double deltaS = Math.abs((value1OfIndividualPlus1 - value1OfIndividual)) * 
    				Math.abs((value2OfIndividualMinus1 - value2OfIndividual));
    		map.put(currentIndividualIndex, deltaS);
    	}
		
		return map;
	}

}

