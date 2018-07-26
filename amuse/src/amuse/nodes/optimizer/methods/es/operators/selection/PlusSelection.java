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
package amuse.nodes.optimizer.methods.es.operators.selection;

import java.util.ArrayList;

import amuse.nodes.optimizer.methods.es.ESIndividual;
import amuse.nodes.optimizer.methods.es.EvolutionaryStrategy;
import amuse.nodes.optimizer.methods.es.operators.selection.interfaces.AbstractSelection;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * (+)-strategy: select candidates for new population from both parent and offspring population
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class PlusSelection extends AbstractSelection {

	public PlusSelection(EvolutionaryStrategy correspondingES) {
		super(correspondingES);
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.selection.interfaces.SelectionInterface#replaceParentPopulation()
	 */
	public int replaceParentPopulation() {
		
		// Individual indices are sorted in the way so that at first comes the parent population
		// (individual 0 to individual popSize-1) and then offspring population (popSize to popSize+offspringSize-1)
		ArrayList<Integer>individualsToSort = null;
		
		individualsToSort = new ArrayList<Integer>(correspondingES.popSize + correspondingES.offspringPopSize);
		for(int i=0;i<correspondingES.popSize + correspondingES.offspringPopSize;i++) {
			individualsToSort.add(i);
		}
		
		// This individuals will built the new population
		ArrayList<Integer>sortedIndividuals = new ArrayList<Integer>(correspondingES.popSize);
		
		// Search for the i-th best individual
		for(int i=0;i<correspondingES.popSize;i++) {
			int indexOfBestIndividual = individualsToSort.get(0);
			int positionOfBestIndividual = 0;
			
			for(int j=1;j<individualsToSort.size();j++) {
				if(correspondingES.isMinimizingFitness) {
					if(correspondingES.fitnessOf(individualsToSort.get(j))[0].getValue() < correspondingES.fitnessOf(indexOfBestIndividual)[0].getValue()) {
						indexOfBestIndividual = individualsToSort.get(j);
						positionOfBestIndividual = j;
					}
				} else {
					if(correspondingES.fitnessOf(individualsToSort.get(j))[0].getValue() > correspondingES.fitnessOf(indexOfBestIndividual)[0].getValue()) {
						indexOfBestIndividual = individualsToSort.get(j);
						positionOfBestIndividual = j;
					}
				}
			}
			sortedIndividuals.add(individualsToSort.get(positionOfBestIndividual));
			individualsToSort.remove(positionOfBestIndividual);
		}
		
		// Estimate the success number (number of new solutions which will replace the parents)
		int successCounter = 0;
		for(Integer ind : sortedIndividuals) {
			if(ind >= correspondingES.popSize) {
				successCounter++;
			}
		}
		
		// Replace parent population
		ESIndividual[] newPopulation = new ESIndividual[correspondingES.popSize];
		ValidationMeasureDouble[][] newPopulationFitnessValues = new ValidationMeasureDouble[correspondingES.popSize][correspondingES.numberOfFitnessValues];
		ValidationMeasureDouble[][] newPopulationFitnessValuesOnTestSet = new ValidationMeasureDouble[correspondingES.popSize][correspondingES.numberOfFitnessValues];
		for(int i=0;i<correspondingES.popSize;i++) {
			ESIndividual newParent = (sortedIndividuals.get(i) < correspondingES.popSize) ? 
					correspondingES.population[sortedIndividuals.get(i)].clone() : 
					correspondingES.offspringPopulation[sortedIndividuals.get(i) - correspondingES.popSize].clone();
			ValidationMeasureDouble[] newFitness = correspondingES.fitnessOf(sortedIndividuals.get(i));
			newPopulation[i] =  newParent;
			newPopulationFitnessValues[i] = newFitness;
			if(correspondingES.isIndependentTestSetUsed) {
				ValidationMeasureDouble[] newFitnessOnTestSet = correspondingES.fitnessOfTestSet(sortedIndividuals.get(i));
				newPopulationFitnessValuesOnTestSet[i] = newFitnessOnTestSet;
			}
		}
		correspondingES.population = newPopulation;
		correspondingES.populationFitnessValues = newPopulationFitnessValues;
		if(correspondingES.isIndependentTestSetUsed) {
			correspondingES.populationFitnessValuesOnTestSet = newPopulationFitnessValuesOnTestSet;
		}
		
		return successCounter;
	}

}
