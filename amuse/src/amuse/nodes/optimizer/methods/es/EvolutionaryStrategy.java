/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2020 by code authors
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Level;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.data.MeasureTable;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.nodes.optimizer.interfaces.OptimizerInterface;
import amuse.nodes.optimizer.methods.es.evaluation.interfaces.EvaluationInterface;
import amuse.nodes.optimizer.methods.es.operators.crossover.interfaces.CrossoverInterface;
import amuse.nodes.optimizer.methods.es.operators.mutation.interfaces.MutationInterface;
import amuse.nodes.optimizer.methods.es.operators.selection.CommaSelection;
import amuse.nodes.optimizer.methods.es.operators.selection.HypervolumeSelection;
import amuse.nodes.optimizer.methods.es.operators.selection.PlusSelection;
import amuse.nodes.optimizer.methods.es.operators.selection.interfaces.SelectionInterface;
import amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.FileOperations;

/**
 * Evolutionary Strategy (ES) algorithm
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class EvolutionaryStrategy extends AmuseTask implements OptimizerInterface {

	/** Configuration of this ES */
	ESConfiguration esConfiguration;
	
	/** Parameters from ESConfiguration which are saved here for faster processing */
	public int popSize = 0;
	public int offspringPopSize = 0;
	public int generationLimit = -1;
	public long startTime; // The experiment start
	public long runTime; // Maximal runtime
	int evaluationLimit = -1;
	public boolean isIndependentTestSetUsed = false;
	int loggingInterval = 1;
	long loggingDelay = -1;
	StringBuffer delayedLog = null; // If logging should be done only each loggingDelay ms
	long lastLogTime = -1;
	boolean logLocally = false; 
	File targetLog = null;
	String logFile = null;
	boolean logLastGeneration = false;
	boolean logGeneration = true;
	boolean logEvaluation = true;
	boolean logPopulationRepresentations = false;
	boolean logPopulationFitness = true;
	boolean logPopulationFitnessOnTestSet = false;
	boolean logOffspringPopulationRepresentations = false;
	boolean logOffspringPopulationFitness = true;
	boolean logOffspringPopulationFitnessOnTestSet = false;
	// FIXME Incompatible for MOO! Must be removed since measure optimization direction is now saved in measure!
	public boolean isMinimizingFitness = true; // As a default, measure values (fitness) are minimized by optimization
	public String optimizationCategoryId = null;
	public boolean runTimeLimitAchieved = false; // From the old log
	
	/** ES populations */
	public ESIndividual[] population;
	public ValidationMeasureDouble[][] populationFitnessValues;
	public ValidationMeasureDouble[][] populationFitnessValuesOnTestSet;
	public ESIndividual[] offspringPopulation;
	public ValidationMeasureDouble[][] offspringPopulationFitnessValues;
	public ValidationMeasureDouble[][] offspringPopulationFitnessValuesOnTestSet;
	SelectionInterface selectionOperator;
	public int numberOfFitnessValues;
	
	/** Maps the name of representation class to the list with used crossover operators */
	HashMap<String,List<CrossoverInterface>> crossoverMap;
	
	/** Maps the name of representation class to the list with used mutation operators */
	HashMap<String,List<MutationInterface>> mutationMap;
	
	/** Maps the name of representation class to the list with used VNS operators */
	HashMap<String,List<MutationInterface>> vnsMap;
	
	/** ES run parameters */
	public int currentGeneration;
	public int currentEvaluation;
	public int currentSuccessCounter; // TODO replace by successHistory: currently IntegerMutation may set this
	// counter to zero after x (e.g. 5) iterations; if other mutations do this after y (e.g. 10) iterations, it is not counted properly! Also check the update for SMS-EMOA!!
	ESLogger esLogger;
	private EvaluationInterface fitnessEvaluator;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.interfaces.OptimizerInterface#optimize()
	 */
	public void optimize() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting ES: " + 
				esConfiguration.getESParameterByName("Population strategy").getAttributes().getNamedItem("stringValue").getNodeValue());
		
		// Create directory and file for logging..
		if(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getContinueOldExperimentFrom().
				equals("-1")) {
			
			File folderForResults = new File(AmusePreferences.get(KeysStringValue.OPTIMIZATION_DATABASE) + "/" + 
					optimizationCategoryId + File.separator + 
					((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getDestinationFolder());
			if(!folderForResults.exists()) {
				folderForResults.mkdirs();
			}
			
			boolean logCreated = false;
			File newLog = null;
			// FIXME Provide some stopping criterion! If the log exists, the loop continues without stopping..
			while(!logCreated) {
				newLog = new File(folderForResults + File.separator + "optimization_" + 
					folderForResults.listFiles().length + "_" + esConfiguration.getESParameterByName("Random seed").getAttributes().getNamedItem("longValue").getNodeValue() + ".arff");
				try {
					if(newLog.createNewFile()) {
						logCreated = true;
					}
					// Local logging is useful for experiments in batch systems using scratch folders
					if(logLocally) {
						logCreated = false;
						targetLog = newLog;
						
						// Create a folder for local optimization log
						File folder = new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + 
								this.correspondingScheduler.getTaskId());
						if(!folder.exists() && !folder.mkdirs()) {
							throw new NodeException("Could not create temp folder for local optimization log: " + 
									folder.toString());
						}
						
						newLog = new File(folder.getAbsolutePath() + "/optimization" + Calendar.getInstance().getTimeInMillis() + ".log");
						if(newLog.createNewFile()) {
							logCreated = true;
						}
					}
				} catch(IOException e) {
					throw new NodeException("Could not create log file '" + newLog.getAbsolutePath() + 
							"': " + e.getMessage());
				}
			}
			esLogger = new ESLogger(newLog);
			logFile = newLog.getAbsolutePath();
		} else { // ..or continue writing to older log from previous experiment 
			if(logLocally) {
				throw new NodeException("Does not support local logging AND loading of an older log at the same time!");
			}
			// FIXME old esLogger = new ESLogger(new File(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getContinueOldExperimentFrom()));
			File pathToLogFile = new File(AmusePreferences.get(KeysStringValue.OPTIMIZATION_DATABASE) + "/" + optimizationCategoryId + 
					"/" + ((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getDestinationFolder() + 
					"/optimization_" + ((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getContinueOldExperimentFrom() + 
					"_-1.arff");
			esLogger = new ESLogger(pathToLogFile);
		}
			
		// Calculate the population fitness values for the first time
		// TODO getContinueOldExperimentFrom is currently not supported
		for(int i=0;i<popSize;i++) {
			populationFitnessValues[i] = population[i].getFitness();
			if(isIndependentTestSetUsed) {
				populationFitnessValuesOnTestSet[i] = population[i].getFitnessOnIndependentTestSet();
			}
		}
		
		// Output the log header
		if(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getContinueOldExperimentFrom().
				equals("-1")) {
			outputHeaderLog();
		}
		
		// ES generation loop
		for(;currentGeneration<generationLimit && currentEvaluation<evaluationLimit;currentGeneration++) {
		
			// -------------------------------------
			// (I) Select parents for new population
			// -------------------------------------
			// TODO Currently this is done randomly; no different strategies are allowed; 
			ArrayList<Integer> candidateIndices = new ArrayList<Integer>(popSize);
			for(int i=0;i<popSize;i++) {
				candidateIndices.add(i);
			}
			offspringPopulation = new ESIndividual[offspringPopSize];
			
			// -------------------
			// (II) Make crossover
			// -------------------
			if(!crossoverMap.isEmpty()) { 
				
				// Estimate the number of required crossover applications required to create the offsprings
				// (e.g. for (10+5)-ES and crossover with 2 children as output three crossover operations are required).
				CrossoverInterface ci = crossoverMap.values().iterator().next().get(0);
				int breedingNumber = new Double(Math.ceil((double)offspringPopSize / ci.getOffspringNumber())).intValue();
				
				// Create the offspring population cloning the 1st individiual for representation info
				for(int j=0;j<offspringPopSize;j++) {
					offspringPopulation[j] = population[0].clone();
				}
				
				// Run the breedings
				for(int i=0;i<breedingNumber;i++) {
					Collections.shuffle(candidateIndices);
					ESIndividual[] parentPopulation = new ESIndividual[ci.getParentNumber()];
					
					// Select the required parent number randomly
					for(int j=0;j<ci.getParentNumber();j++) {
						parentPopulation[j] = population[candidateIndices.get(j)].clone();
					}
					
					// Go through each representation
					for(int j=0;j<parentPopulation[i].getRepresentationList().size();j++) {
						
						// Get the crossover which should be proceeded for current representation
						List<CrossoverInterface> crossoverToProceed = crossoverMap.get(parentPopulation[i].getRepresentationList().get(j).getClass().getName());
						if(crossoverToProceed != null) {
							RepresentationInterface[] ri = new RepresentationInterface[ci.getParentNumber()];
							for(int currentParent=0;currentParent<ci.getParentNumber();currentParent++) {
								ri[currentParent] = parentPopulation[currentParent].getRepresentationList().get(j);
							}
							
							RepresentationInterface[] offspringRepresentations = crossoverToProceed.get(0).crossover(ri);
							
							// Save the new representations for offsprings
							for(int currentCrossoverOutput=0;currentCrossoverOutput<offspringRepresentations.length;currentCrossoverOutput++) {
								
								// Some of the crossover output solutions may be omitted if the strategy offspring number is achieved
								int currentOverallCrossoverOutput = i * ci.getOffspringNumber() + currentCrossoverOutput;
								if(currentOverallCrossoverOutput < offspringPopSize) { 
									offspringPopulation[currentOverallCrossoverOutput].getRepresentationList().
											set(j, offspringRepresentations[currentCrossoverOutput]);
								} else break; // Further crossover output is not required!
							}
						} else {
							
							// Transfer the corresponding parent representation to offsprings if no crossover will be applied for it
							for(int currentCrossoverOutput=0;currentCrossoverOutput<ci.getOffspringNumber();currentCrossoverOutput++) {
								int currentOverallCrossoverOutput = breedingNumber*ci.getOffspringNumber() + currentCrossoverOutput;
								if(currentOverallCrossoverOutput < offspringPopSize) {
									offspringPopulation[currentOverallCrossoverOutput].getRepresentationList().
											set(j, parentPopulation[i].getRepresentationList().get(j));
								} else break;
							}
						}
					}
				}
			}
			
			// -------------------
			// (III) Make mutation
			// -------------------
			// Select the candidates for mutation randomly (otherwise they have been generated by crossover)
			if(crossoverMap.isEmpty()) {
				Collections.shuffle(candidateIndices);
				for(int i=0;i<offspringPopSize;i++) {
					offspringPopulation[i] = population[candidateIndices.get(i)].clone();
				}
			}

			// Go through offspring population
			for(int i=0;i<offspringPopSize;i++) {
				
				// Go through each representation
				for(int j=0;j<offspringPopulation[i].getRepresentationList().size();j++) {
					
					// Get the list of mutations which should be proceeded for current representation
					List<MutationInterface> mutationsToProceed = mutationMap.get(offspringPopulation[i].getRepresentationList().get(j).getClass().getName());
					for(MutationInterface m : mutationsToProceed) {
						m.mutate(offspringPopulation[i].getRepresentationList().get(j));
					}
				}
				
				// Fitness after mutation(s) of the current offspring
				offspringPopulationFitnessValues[i] = offspringPopulation[i].getFitness();
				if(isIndependentTestSetUsed) {
					offspringPopulationFitnessValuesOnTestSet[i] = offspringPopulation[i].getFitnessOnIndependentTestSet();
				}
			}
			
			// Log after the mutation
			outputLog();
			
			// --------------------------------------------
			// (IV) Make local search if VNS scheme is used
			// --------------------------------------------
			if(!vnsMap.isEmpty()) { 
				
				// Go through offspring population
				for(int i=0;i<offspringPopSize;i++) {
						
					// Run VNS after each mutation or only after successful mutations?
					if(esConfiguration.getESParameterByName("Apply VNS only after successful mutations").getAttributes().
						getNamedItem("booleanValue").getNodeValue().equals("false") || 
						(esConfiguration.getESParameterByName("Apply VNS only after successful mutations").getAttributes().
						getNamedItem("booleanValue").getNodeValue().equals("true")) && 
						
						// Are we minimizing or maximizing fitness?
						(isMinimizingFitness && offspringPopulationFitnessValues[i][0].getValue() < populationFitnessValues[i][0].getValue()) ||
						(!isMinimizingFitness && offspringPopulationFitnessValues[i][0].getValue() > populationFitnessValues[i][0].getValue())) {
						
						// Go through each representation
						for(int j=0;j<offspringPopulation[i].getRepresentationList().size();j++) {
							
							// Get the list of mutations which should be proceeded for current representation
							List<MutationInterface> mutationsToProceed = vnsMap.get(offspringPopulation[i].getRepresentationList().get(j).getClass().getName());
							if(mutationsToProceed != null) {
								 runVNS(i, j, mutationsToProceed);
							}
						}
					}
				}
			}
			
			// --------------------------------------------------------------------------------------------------
			// (V) Update the parent population and the success number (if the children were better than parents)
			// --------------------------------------------------------------------------------------------------
			currentSuccessCounter += selectionOperator.replaceParentPopulation();
			
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Generation: " + currentGeneration + 
					" Evaluation: " + currentEvaluation);
			
			// Check if the runtime exit condition is fulfilled
			if(Calendar.getInstance().getTimeInMillis() - startTime > runTime) {
				runTimeLimitAchieved = true;
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Runtime limit achieved. Current runtime: " + 
						(Calendar.getInstance().getTimeInMillis() - startTime) + " Limit: " + runTime); 
				break;
			}
		}
		fitnessEvaluator.close();
		if(loggingDelay != -1) {
			esLogger.logString(delayedLog.toString());
		}
		esLogger.close();
		
		if(logLocally) {
			try {
				FileOperations.copy(new File(logFile), targetLog);
			} catch (IOException e) {
				throw new NodeException("Could not copy the local log: " + e.getMessage());
			}
		}
		
		if(logLastGeneration) {
			outputLastGenerationLog();
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "ES optimization finished");
	}

	private void runVNS(int offspringNumber, int representationToProceed,
			List<MutationInterface> mutationsToProceed) throws NodeException {
		
		// Load the current fitness
		ValidationMeasureDouble[] currentFitness = new ValidationMeasureDouble[numberOfFitnessValues];
		for(int i=0;i<numberOfFitnessValues;i++) {
			currentFitness[i] = new ValidationMeasureDouble();
			currentFitness[i].setValue(new Double(offspringPopulationFitnessValues[offspringNumber][i].getValue()));
			currentFitness[i].setName(offspringPopulationFitnessValues[offspringNumber][i].getName());
		}
		
		// Load the current fitness for the test set
		ValidationMeasureDouble[] currentFitnessOnTestSet = new ValidationMeasureDouble[numberOfFitnessValues];
		for(int i=0;i<numberOfFitnessValues;i++) {
			currentFitnessOnTestSet[i] = new ValidationMeasureDouble();
			if(isIndependentTestSetUsed) {
				currentFitnessOnTestSet[i].setValue(new Double(offspringPopulationFitnessValuesOnTestSet[offspringNumber][i].getValue()));
				currentFitnessOnTestSet[i].setName(offspringPopulationFitnessValuesOnTestSet[offspringNumber][i].getName());
			} else {
				currentFitnessOnTestSet[i].setValue(0d);
				currentFitnessOnTestSet[i].setName("");
			}
		}
		
		// TODO If more than two operators are used for VNS local search...
		// What operator is currently active during VNS?
		//boolean isLocalSearchForAddingActive = new Random().nextBoolean();
		boolean isFirstLocalSearchOperatorActive = new Random().nextBoolean();
		// Was a previous operator successful?
		boolean hadPreviousOperatorSuccess = true;
		
		ESIndividual individual = offspringPopulation[offspringNumber];
		ESIndividual candidate = individual.clone();
		
		// Proceed VNS so long as the maximum number of evaluations is not achieved
		while(currentEvaluation < evaluationLimit) {
		
			// TODO Currently available for only and exactly two LS operators
			if(isFirstLocalSearchOperatorActive) {
				mutationsToProceed.get(0).mutate(candidate.getRepresentationList().get(representationToProceed));
			} else {
				mutationsToProceed.get(1).mutate(candidate.getRepresentationList().get(representationToProceed));
			}
					
			ValidationMeasureDouble[] newFitness = candidate.getFitness();
			ValidationMeasureDouble[] newFitnessOnTestSet = new ValidationMeasureDouble[newFitness.length]; 
			if(isIndependentTestSetUsed) {
				newFitnessOnTestSet = candidate.getFitnessOnIndependentTestSet();
			}
			
			// Set the currently tried solution for logging
			currentFitness = offspringPopulationFitnessValues[offspringNumber];
			offspringPopulationFitnessValues[offspringNumber] = newFitness;
			if(isIndependentTestSetUsed) {
				offspringPopulationFitnessValuesOnTestSet[offspringNumber] = newFitnessOnTestSet;
			}
			outputLog();
			offspringPopulationFitnessValues[offspringNumber] = currentFitness;
			if(isIndependentTestSetUsed) {
				offspringPopulationFitnessValuesOnTestSet[offspringNumber] = currentFitnessOnTestSet;
			}
				
			// Is the individual after local search better?
			if((isMinimizingFitness && newFitness[0].getValue() < currentFitness[0].getValue()) || 
					(!isMinimizingFitness && newFitness[0].getValue() > currentFitness[0].getValue())) {
				currentFitness = newFitness;
				offspringPopulationFitnessValues[offspringNumber] = newFitness;
				if(isIndependentTestSetUsed) {
					currentFitnessOnTestSet = newFitnessOnTestSet;
					offspringPopulationFitnessValuesOnTestSet[offspringNumber] = newFitnessOnTestSet;
				}
				individual = candidate.clone();
				candidate = individual.clone();
				hadPreviousOperatorSuccess = true;
			} else {
				candidate = individual.clone();
				
				// Break if previous operator also wasn't successful
				if(hadPreviousOperatorSuccess == false) break;
				hadPreviousOperatorSuccess = false;
				
				// Change operator - TODO only for two operators!
				if(isFirstLocalSearchOperatorActive) {
					isFirstLocalSearchOperatorActive = false;
				}
				else isFirstLocalSearchOperatorActive = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.methods.AmuseTaskInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException { 
		esConfiguration = new ESConfiguration(parameterString);
		isIndependentTestSetUsed = (!((OptimizationConfiguration)this.correspondingScheduler.getConfiguration()).
				getTestInput().equals("-1")) ? true : false;
		
		// Should the fitness value be minimized or maximized?
		try {
			MeasureTable measureTable = new MeasureTable(new File(esConfiguration.getConstantParameterByName("Measure table").
				getAttributes().getNamedItem("fileValue").getNodeValue()));
			if(measureTable.get(0).getOptimalValue() == 1d || measureTable.get(0).getOptimalValue() == Double.MAX_VALUE ||
					measureTable.get(0).getOptimalValue() == Double.POSITIVE_INFINITY) {
				isMinimizingFitness = false;
			}
			numberOfFitnessValues = measureTable.size(); 
		} catch(IOException e) {
			throw new NodeException("Could not load the measure table: " + e.getMessage());
		}
		
		// Set the population sizes
		Node strategy = esConfiguration.getESParameterByName("Population strategy");
		String strategyString = strategy.getAttributes().getNamedItem("stringValue").getNodeValue();
		
		// TODO for sms-emoa
		if(strategyString.startsWith("sms-emoa")) {
			popSize = new Integer(strategyString.substring(9,strategyString.indexOf("+")));
			offspringPopSize = new Integer(1);
			selectionOperator = new HypervolumeSelection(this);
		} else if(strategyString.indexOf("+") != -1) {
			popSize = new Integer(strategyString.substring(0,strategyString.indexOf("+")));
			offspringPopSize = new Integer(strategyString.substring(strategyString.indexOf("+")+1));
			selectionOperator = new PlusSelection(this);
		} else if(strategyString.indexOf(",") != -1) {
			popSize = new Integer(strategyString.substring(0,strategyString.indexOf(",")));
			offspringPopSize = new Integer(strategyString.substring(strategyString.indexOf(",")+1));
			selectionOperator = new CommaSelection(this);
		} else {
			throw new NodeException("Could not parse the population strategy description");
		}
		population = new ESIndividual[popSize];
		populationFitnessValues = new ValidationMeasureDouble[popSize][numberOfFitnessValues];
		offspringPopulation = new ESIndividual[offspringPopSize];
		offspringPopulationFitnessValues = new ValidationMeasureDouble[offspringPopSize][numberOfFitnessValues];
		if(isIndependentTestSetUsed) {
			populationFitnessValuesOnTestSet = new ValidationMeasureDouble[popSize][numberOfFitnessValues];
			offspringPopulationFitnessValuesOnTestSet = new ValidationMeasureDouble[offspringPopSize][numberOfFitnessValues];
		}
		
		// Set the crossover operators
		// TODO the number of parents and children must be the same for all crossover operators across different representation!
		crossoverMap = new HashMap<String,List<CrossoverInterface>>();
		NodeList crossoverNodes = esConfiguration.getESParameterByName("List with crossover operators").getChildNodes();
		for(int i=0;i<crossoverNodes.getLength();i++) {
			if(crossoverNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				
				// Get the class for representation
				String parameterToOptimizeName = crossoverNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
				String parameterToOptimizeClass = esConfiguration.getOptimizationParameterByName(parameterToOptimizeName).
					getAttributes().getNamedItem("classValue").getNodeValue();
				
				// Go through crossover operators for this representation and initialize them
				try {
					NodeList crossoverSpecification = crossoverNodes.item(i).getChildNodes();
					for(int j=0;j<crossoverSpecification.getLength();j++) {
						if(crossoverSpecification.item(j).getNodeType() == Node.ELEMENT_NODE) {
							Class<?> crossoverClass = Class.forName(crossoverSpecification.item(j).getAttributes().
									getNamedItem("classValue").getNodeValue());
							CrossoverInterface ci = (CrossoverInterface)crossoverClass.newInstance();
							ci.setParameters(crossoverSpecification.item(j).getChildNodes(),this);
							if(ci.getParentNumber() > popSize) {
								throw new NodeException("Crossover requires " + ci.getParentNumber() + " parents; popSize = " + popSize);
							}
							
							// Has any crossover been already configured for this representation?
							if(crossoverMap.containsKey(parameterToOptimizeClass)) {
								crossoverMap.get(parameterToOptimizeClass).add(ci);
							} else {
								ArrayList<CrossoverInterface> list = new ArrayList<CrossoverInterface>(1);
								list.add(ci);
								crossoverMap.put(parameterToOptimizeClass,list);
							}
						}
					}
				} catch(Exception e) {
					throw new NodeException("Could not set up crossover for " + parameterToOptimizeClass + " : " + e.getMessage());
				}
			}
		}
		
		// Set the mutation operators
		mutationMap = new HashMap<String,List<MutationInterface>>();
		NodeList mutationNodes = esConfiguration.getESParameterByName("List with mutation operators").getChildNodes();
		for(int i=0;i<mutationNodes.getLength();i++) {
			if(mutationNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				
				// Get the class for representation
				String parameterToOptimizeName = mutationNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
				String parameterToOptimizeClass = esConfiguration.getOptimizationParameterByName(parameterToOptimizeName).
					getAttributes().getNamedItem("classValue").getNodeValue();
				
				// Go through mutation operators for this representation and initialize them
				try {
					NodeList mutationSpecification = mutationNodes.item(i).getChildNodes();
					for(int j=0;j<mutationSpecification.getLength();j++) {
						if(mutationSpecification.item(j).getNodeType() == Node.ELEMENT_NODE) {
							Class<?> mutationClass = Class.forName(mutationSpecification.item(j).getAttributes().
									getNamedItem("classValue").getNodeValue());
							MutationInterface mi = (MutationInterface)mutationClass.newInstance();
							mi.setParameters(mutationSpecification.item(j).getChildNodes(),this);
							
							// Has any mutation been already configured for this representation?
							if(mutationMap.containsKey(parameterToOptimizeClass)) {
							 	mutationMap.get(parameterToOptimizeClass).add(mi);
							} else {
								ArrayList<MutationInterface> list = new ArrayList<MutationInterface>(1);
								list.add(mi);
								mutationMap.put(parameterToOptimizeClass,list);
							}
						}
					}
				} catch(Exception e) {
					throw new NodeException("Could not set up mutation for " + parameterToOptimizeClass + " : " + e.getMessage());
				}
			}
		}
		
		// Set the evaluation
		Node eval = esConfiguration.getConstantParameterByName("Fitness estimation class");
		String evalString = eval.getAttributes().getNamedItem("classValue").getNodeValue();
		try {
			Class<?> evaluationClass = Class.forName(evalString);
			fitnessEvaluator = (EvaluationInterface)evaluationClass.newInstance();
		} catch (Exception e) {
			throw new NodeException("Could not set up evaluation: " + e.getMessage());
		}
		
		// Set the VNS operators (variable neighborhood search)
		vnsMap = new HashMap<String,List<MutationInterface>>();
		NodeList vnsNodes = esConfiguration.getESParameterByName("List with VNS operators").getChildNodes();
		for(int i=0;i<vnsNodes.getLength();i++) {
			if(vnsNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				
				// Get the class for representation
				String parameterToOptimizeName = vnsNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
				String parameterToOptimizeClass = esConfiguration.getOptimizationParameterByName(parameterToOptimizeName).
					getAttributes().getNamedItem("classValue").getNodeValue();
				
				// Go through mutation operators for this representation and initialize them
				try {
					NodeList mutationSpecification = vnsNodes.item(i).getChildNodes();
					for(int j=0;j<mutationSpecification.getLength();j++) {
						if(mutationSpecification.item(j).getNodeType() == Node.ELEMENT_NODE) {
							Class<?> mutationClass = Class.forName(mutationSpecification.item(j).getAttributes().
									getNamedItem("classValue").getNodeValue());
							MutationInterface mi = (MutationInterface)mutationClass.newInstance();
							mi.setParameters(mutationSpecification.item(j).getChildNodes(),this);
							
							// Has any mutation been already configured for this representation?
							if(vnsMap.containsKey(parameterToOptimizeClass)) {
								vnsMap.get(parameterToOptimizeClass).add(mi);
							} else {
								ArrayList<MutationInterface> list = new ArrayList<MutationInterface>(1);
								list.add(mi);
								vnsMap.put(parameterToOptimizeClass,list);
							}
						}
					}
				} catch(Exception e) {
					throw new NodeException("Could not set up VNS mutation for " + parameterToOptimizeClass + " : " + e.getMessage());
				}
			}
		}
		
		// Set the exit conditions
		generationLimit = new Integer(esConfiguration.getESParameterByName("Number of generations").
				getAttributes().getNamedItem("intValue").getNodeValue());
		evaluationLimit = new Integer(esConfiguration.getESParameterByName("Number of evaluations").
				getAttributes().getNamedItem("intValue").getNodeValue());
		startTime = Calendar.getInstance().getTimeInMillis();
		runTime = new Long(esConfiguration.getESParameterByName("Runtime in milliseconds").
				getAttributes().getNamedItem("intValue").getNodeValue());
		
		// Set the logging parameters
		loggingInterval = new Integer(esConfiguration.getOutputParameterByName("Logging interval").
				getAttributes().getNamedItem("intValue").getNodeValue());
		if(esConfiguration.getOutputParameterByName("Logging delay") != null) {
			loggingDelay = new Long(esConfiguration.getOutputParameterByName("Logging delay").
					getAttributes().getNamedItem("intValue").getNodeValue());
		}
		if(esConfiguration.getOutputParameterByName("Local logging") != null) {
			logLocally = new Boolean(esConfiguration.getOutputParameterByName("Local logging").
					getAttributes().getNamedItem("booleanValue").getNodeValue());
		}
		if(esConfiguration.getOutputParameterByName("Log the last generation") != null) {
			logLastGeneration = new Boolean(esConfiguration.getOutputParameterByName("Log the last generation").
					getAttributes().getNamedItem("booleanValue").getNodeValue());
		}
		logGeneration = new Boolean(esConfiguration.getOutputParameterByName("Generation number").
				getAttributes().getNamedItem("booleanValue").getNodeValue());
		logEvaluation = new Boolean(esConfiguration.getOutputParameterByName("Evaluation number").
				getAttributes().getNamedItem("booleanValue").getNodeValue());
		logPopulationRepresentations = new Boolean(esConfiguration.getOutputParameterByName("Complete population representations").
				getAttributes().getNamedItem("booleanValue").getNodeValue());
		logPopulationFitness = new Boolean(esConfiguration.getOutputParameterByName("Complete population fitness values").
				getAttributes().getNamedItem("booleanValue").getNodeValue());
		logPopulationFitnessOnTestSet = new Boolean(esConfiguration.getOutputParameterByName("Complete population fitness values on test set").
				getAttributes().getNamedItem("booleanValue").getNodeValue());
		logOffspringPopulationRepresentations = new Boolean(esConfiguration.getOutputParameterByName("Complete offspring population representations").
				getAttributes().getNamedItem("booleanValue").getNodeValue());
		logOffspringPopulationFitness = new Boolean(esConfiguration.getOutputParameterByName("Complete offspring population fitness values").
				getAttributes().getNamedItem("booleanValue").getNodeValue());
		logOffspringPopulationFitnessOnTestSet = new Boolean(esConfiguration.getOutputParameterByName("Complete offspring population fitness values on test set").
				getAttributes().getNamedItem("booleanValue").getNodeValue());
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.methods.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		
		// Set the optimization category id (if n-fold optimization is used, it is equal to learning category)
		String trainingInput = ((OptimizationConfiguration)this.getCorrespondingScheduler().
				getConfiguration()).getTrainingInput();
		Integer categoryTrainingId = (trainingInput.contains("[") ? new Double(trainingInput.substring(0,trainingInput.indexOf("["))).intValue() :
			new Double(trainingInput).intValue());
		String optimizationInput = ((OptimizationConfiguration)this.getCorrespondingScheduler().
				getConfiguration()).getOptimizationInput();
		//String optimizationCategoryId = null;
		if(optimizationInput.contains("[")) {
			optimizationCategoryId = optimizationInput.substring(0,optimizationInput.indexOf("["));
		} else {
			if(new Integer(optimizationInput) >= 0) {
				optimizationCategoryId = optimizationInput;
			} else {
				optimizationCategoryId = categoryTrainingId.toString();
			}
		}
		
		// Create population
		for(int i=0;i<popSize;i++) {
			ESIndividual ind = new ESIndividual(this);
			if(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getContinueOldExperimentFrom().
					equals("-1")) {
				ind.initialize();
			} else {
				ind.initializeFromLog(new File(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).
						getContinueOldExperimentFrom()), i);
			}
			population[i] = ind;
		}
		fitnessEvaluator.initialize(this, isIndependentTestSetUsed);
		
		if(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getContinueOldExperimentFrom().
				equals("-1")) {
			
			// The first evaluation of parent population is not counted
			currentEvaluation = -popSize;
			currentGeneration = 0;
		} else {
			File pathToLogFile = new File(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).
					getContinueOldExperimentFrom());
			
			DataSetAbstract loggingSet;
			try {
				loggingSet = new ArffDataSet(pathToLogFile);
			} catch (IOException e) {
				throw new NodeException("Could not load the given log file '" + pathToLogFile.getAbsolutePath() + "': " + e.getMessage());
			}
			
			String generationAttribute = new String("Generation number");
			if(loggingSet.getAttributeNames().contains(generationAttribute)) {
		     	currentGeneration = new Double(loggingSet.getAttribute(generationAttribute).getValueAt(loggingSet.getValueCount()-1).toString()).intValue()+1;
			} else {
				throw new NodeException("Could not load the generation number from log file '" + pathToLogFile.getAbsolutePath() + "'");
			}
			
			String evaluationAttribute = new String("Evaluation number");
			if(loggingSet.getAttributeNames().contains(evaluationAttribute)) {
				
				// popSize is subtracted here since the complete population must be evaluated once at the beginning of the loop
		     	currentEvaluation = new Double(loggingSet.getAttribute(evaluationAttribute).getValueAt(loggingSet.getValueCount()-1).toString()).intValue() - popSize;
			} else {
				throw new NodeException("Could not load the evaluation number from log file '" + pathToLogFile.getAbsolutePath() + "'");
			}
		}
		
		currentSuccessCounter = 0; // TODO Evtl.load also from log file
	}
	
	/**
	 * Output the header for arff logging file
	 * @throws NodeException
	 */
	public void outputHeaderLog() throws NodeException {
		esLogger.logString("@RELATION 'Optimization results'" + esLogger.sep);
		
		// Load the names of fitness values
		MeasureTable measureTable;
		try {
			measureTable = new MeasureTable(new File(esConfiguration.getConstantParameterByName("Measure table").
				getAttributes().getNamedItem("fileValue").getNodeValue()));
		} catch(IOException e) {
			throw new NodeException("Could not load the measure table: " + e.getMessage());
		}
		String[] fitnessValueNames = new String[numberOfFitnessValues];
		for(int i=0;i<numberOfFitnessValues;i++) {
			fitnessValueNames[i] = new String(measureTable.get(i).getName());
		}
		
		// Output the current generation number
		if(logGeneration) {
			esLogger.logString("@ATTRIBUTE 'Generation number' NUMERIC");
		}
		
		// Output the population data
		for(int i=0;i<popSize;i++) {
			
			// Output the current population representations
			if(logPopulationRepresentations) {
				for(int j=0;j<population[i].getRepresentationList().size();j++) {
					esLogger.logString("@ATTRIBUTE 'Representation " + population[i].getRepresentationList().get(j).getClass().getSimpleName().toString() + 
							" of individual " + i + "' STRING");
				}
			}
			
			// Output the current population fitness values
			if(logPopulationFitness) {
				for(int k=0;k<numberOfFitnessValues;k++) {
					if(k==0) {
						esLogger.logString("@ATTRIBUTE 'Fitness value used for optimization (" + fitnessValueNames[k] + ") of individual " + i + "' NUMERIC");
					} else {
						esLogger.logString("@ATTRIBUTE 'Further fitness value (" + fitnessValueNames[k] + ") of individual " + i + "' NUMERIC");
					}
				}
			}
			
			// Output the current population fitness values on test set
			if(isIndependentTestSetUsed && logPopulationFitnessOnTestSet) {
				for(int k=0;k<numberOfFitnessValues;k++) {
					if(k==0) {
						esLogger.logString("@ATTRIBUTE 'Fitness value used for optimization (" + fitnessValueNames[k] + ") of individual " + i + " on the independent test set' NUMERIC");
					} else {
						esLogger.logString("@ATTRIBUTE 'Further fitness value (" + fitnessValueNames[k] + ") of individual " + i + " on the independent test set' NUMERIC");
					}
				}
			}
		}
		
		// Output the offspring population data
		for(int i=0;i<offspringPopSize;i++) {
				
			// Go through offspring population and output the offspring representations
			if(logOffspringPopulationRepresentations) {
				
				// Since offspring population cannot exist at the beginning (where the header is written,
				// just get the representation lost from the 1st population individual
				for(int j=0;j<population[0].getRepresentationList().size();j++) {
					esLogger.logString("@ATTRIBUTE 'Representation " + population[0].getRepresentationList().get(j).getClass().getSimpleName().toString() + 
							" of offspring individual " + i + "' STRING");
				}
			}
					
			// Output the current population fitness values
			if(logOffspringPopulationFitness) {
				for(int k=0;k<numberOfFitnessValues;k++) {
					if(k==0) {
						esLogger.logString("@ATTRIBUTE 'Fitness value used for optimization (" + fitnessValueNames[k] + ") of offspring individual " + i + "' NUMERIC");
					} else {
						esLogger.logString("@ATTRIBUTE 'Further fitness value (" + fitnessValueNames[k] + ") of offspring individual " + i + "' NUMERIC");
					}
				}
			}
			
			// Output the current population fitness values on test set
			if(isIndependentTestSetUsed && logOffspringPopulationFitnessOnTestSet) {
				for(int k=0;k<numberOfFitnessValues;k++) {
					if(k==0) {
						esLogger.logString("@ATTRIBUTE 'Fitness value used for optimization (" + fitnessValueNames[k] + ") of offspring individual " + i + " on the independent test set' NUMERIC");
					} else {
						esLogger.logString("@ATTRIBUTE 'Further fitness value (" + fitnessValueNames[k] + ") of offspring individual " + i + " on the independent test set' NUMERIC");
					}
				}
			}
		}
			
		// TODO E.g. expected step size can be also output.
		// However the for-loop is required to search if any IntegerMutation is there..
		// FIXME
		//esLogger.logString("@ATTRIBUTE 'Self-adaptation factor' NUMERIC");
		esLogger.logString("@ATTRIBUTE 'Time' NUMERIC");
			
		// Output the current population fitness values
		if(logEvaluation) {
			esLogger.logString("@ATTRIBUTE 'Evaluation number' NUMERIC");
		}
		
		esLogger.logString(esLogger.sep + "@DATA");
		
		// For delayed logs
		if(loggingDelay != -1) {
			delayedLog = new StringBuffer();
			lastLogTime = Calendar.getInstance().getTimeInMillis();
		}
	}
	
	/**
	 * Output the logging string
	 * @throws NodeException
	 */
	public void outputLog() throws NodeException {
		StringBuffer outputBuffer = new StringBuffer();
		
		// Is logging enabled for this generation?
		if(currentGeneration % loggingInterval == 0) {
			
			// Output the current generation number
			if(logGeneration) {
				outputBuffer.append(currentGeneration + ",");
			}
		
			// Output the population data
			for(int i=0;i<popSize;i++) {
			
				// Output the current population representations
				if(logPopulationRepresentations) {
					for(int j=0;j<population[i].getRepresentationList().size();j++) {
						outputBuffer.append(population[i].getRepresentationList().get(j).toString() + ",");
					}
				}
			
				// Output the current population fitness values
				if(logPopulationFitness) {
					for(int k=0;k<numberOfFitnessValues;k++) {
						outputBuffer.append(populationFitnessValues[i][k].getValue() + ",");
					}
				}
				
				// Output the current population fitness values on test set
				if(isIndependentTestSetUsed && logPopulationFitnessOnTestSet) {
					for(int k=0;k<numberOfFitnessValues;k++) {
						outputBuffer.append(populationFitnessValuesOnTestSet[i][k].getValue() + ",");
					}
				}

			}
		
			// Output the offspring population data
			for(int i=0;i<offspringPopSize;i++) {
				
				// Go through offspring population and output the offspring representations
				if(logOffspringPopulationRepresentations) {
					for(int j=0;j<offspringPopulation[i].getRepresentationList().size();j++) {
						outputBuffer.append(offspringPopulation[i].getRepresentationList().get(j).toString() + ",");
					}
				}
					
				// Output the current population fitness values
				if(logOffspringPopulationFitness) {
					for(int k=0;k<numberOfFitnessValues;k++) {
						outputBuffer.append(offspringPopulationFitnessValues[i][k].getValue() + ",");
					}
				}
				
				// Output the current population fitness values on test set
				if(isIndependentTestSetUsed && logOffspringPopulationFitnessOnTestSet) {
					for(int k=0;k<numberOfFitnessValues;k++) {
						outputBuffer.append(offspringPopulationFitnessValuesOnTestSet[i][k].getValue() + ",");
					}
				}
			}
			
			// TODO E.g. expected step size can be also output.
			// However the for-loop is required to search if any IntegerMutation is there..
			// FIXME
			// Go through each representation
			/*List<MutationInterface> mutationsToProceed = mutationMap.get(offspringPopulation[0].getRepresentationList().get(0).getClass().getName());
			MutationInterface m = mutationsToProceed.get(0);
			outputBuffer.append(((amuse.nodes.optimizer.methods.es.operators.mutation.RandomBitFlip)m).selfAdaptationFactor + ",");*/
			
			// FIXME
			outputBuffer.append(Calendar.getInstance().getTimeInMillis() + ",");
			
			// Output the current population fitness values
			if(logEvaluation) {
				outputBuffer.append(currentEvaluation + ",");
			}
			
			// Write to the log file
			if(loggingDelay == -1) {
				esLogger.logString(outputBuffer.deleteCharAt(outputBuffer.length()-1).toString());
			} else {
				if((Calendar.getInstance().getTimeInMillis() - lastLogTime) > loggingDelay) {
					esLogger.logString(delayedLog.toString() + outputBuffer.deleteCharAt(outputBuffer.length()-1).toString());
					lastLogTime = Calendar.getInstance().getTimeInMillis();
					delayedLog = new StringBuffer();
				} else {
					delayedLog.append(outputBuffer.deleteCharAt(outputBuffer.length()-1).toString() + System.getProperty("line.separator"));
				}
			}
		}
	}
	
	public void outputLastGenerationLog() throws NodeException {
		FileOutputStream values_to;

		String lastLog = null;
		if(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getContinueOldExperimentFrom().
				equals("-1")) {
			if(!logLocally) {
				lastLog = logFile.substring(0,logFile.length()-5) + "_lastLog.arff";
			} else {
				lastLog = targetLog.getAbsolutePath().substring(0,targetLog.getAbsolutePath().length()-5) + "_lastLog.arff";
			}
			try {
				values_to = new FileOutputStream(new File(lastLog),true);
				DataOutputStream values_writer = new DataOutputStream(values_to);
				
				// Output the population data
				for(int i=0;i<popSize;i++) {
					
					// Output the current population representations
					for(int j=0;j<population[i].getRepresentationList().size();j++) {
						values_writer.writeBytes(population[i].getRepresentationList().get(j).toString() + System.getProperty("line.separator"));
					}
				}
				// FIXME log also candidate solution if the run is to be continued!
				if(runTimeLimitAchieved) {
					// Output the current population representations
					for(int j=0;j<offspringPopulation[0].getRepresentationList().size();j++) {
						values_writer.writeBytes(offspringPopulation[0].getRepresentationList().get(j).toString() + System.getProperty("line.separator"));
					}
				}
				values_writer.close();
			} catch (Exception e) {
				throw new NodeException("Could not log the results: " + e.getMessage());
			}
		} else {
			File pathToLogFile = new File(AmusePreferences.get(KeysStringValue.OPTIMIZATION_DATABASE) + "/" + optimizationCategoryId + 
					"/" + ((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getDestinationFolder() + 
					"/optimization_" + ((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getContinueOldExperimentFrom() + 
					"_-1.arff");
			File pathToRepFile = new File(pathToLogFile.getAbsoluteFile().toString().substring(0,
					pathToLogFile.getAbsoluteFile().toString().length()-5) + "_lastLog.arff");
			try {
				values_to = new FileOutputStream(pathToRepFile,false);
				DataOutputStream values_writer = new DataOutputStream(values_to);
				
				// Output the population data
				for(int i=0;i<popSize;i++) {
				
					// Output the current population representations
					for(int j=0;j<population[i].getRepresentationList().size();j++) {
						values_writer.writeBytes(population[i].getRepresentationList().get(j).toString() + System.getProperty("line.separator"));
					}
				}
				// FIXME log also candidate solution if the run is to be continued!!! 
				if(runTimeLimitAchieved) {
					
					// Output the current population representations
					for(int j=0;j<offspringPopulation[0].getRepresentationList().size();j++) {
						values_writer.writeBytes(offspringPopulation[0].getRepresentationList().get(j).toString() + System.getProperty("line.separator"));
					}
				}
				values_writer.close();
			} catch (Exception e) {
				throw new NodeException("Could not log the results: " + e.getMessage());
			}
		}
	}
	
	public ESConfiguration getConfiguration() {
		return esConfiguration;
	}
	
	public ValidationMeasureDouble[] fitnessOf(int individualNumber) {
		return individualNumber < popSize ? populationFitnessValues[individualNumber] : 
			offspringPopulationFitnessValues[individualNumber - popSize];
	}
	
	public ValidationMeasureDouble[] fitnessOfTestSet(int individualNumber) {
		return individualNumber < popSize ? populationFitnessValuesOnTestSet[individualNumber] : 
			offspringPopulationFitnessValuesOnTestSet[individualNumber - popSize];
	}

	/**
	 * @return the fitnessEvalualor
	 */
	public EvaluationInterface getFitnessEvalualor() {
		return fitnessEvaluator;
	}

}

