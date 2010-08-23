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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Level;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.data.MetricTable;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.nodes.optimizer.interfaces.OptimizerInterface;
import amuse.nodes.optimizer.methods.es.operators.mutation.interfaces.MutationInterface;
import amuse.nodes.optimizer.methods.es.operators.selection.CommaSelection;
import amuse.nodes.optimizer.methods.es.operators.selection.PlusSelection;
import amuse.nodes.optimizer.methods.es.operators.selection.interfaces.SelectionInterface;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Evolutionary Strategy (ES) algorithm
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class EvolutionaryStrategy extends AmuseTask implements OptimizerInterface {

	/** Configuration of this ES */
	ESConfiguration esConfiguration;
	
	/** Parameters from ESConfiguration which are saved here for faster processing */
	public int popSize = 0;
	public int offspringPopSize = 0;
	//boolean isPlus = true;
	public int generationLimit = -1;
	int evaluationLimit = -1;
	public boolean isIndependentTestSetUsed = false;
	int loggingInterval = 1;
	boolean logGeneration = true;
	boolean logEvaluation = true;
	boolean logPopulationRepresentations = false;
	boolean logPopulationFitness = true;
	boolean logPopulationFitnessOnTestSet = false;
	boolean logOffspringPopulationRepresentations = false;
	boolean logOffspringPopulationFitness = true;
	boolean logOffspringPopulationFitnessOnTestSet = false;
	public boolean isMinimizingFitness = true; // As a default, metric values (fitness) are minimized by optimization
	
	/** ES populations */
	public ESIndividual[] population;
	public ValidationMetricDouble[][] populationFitnessValues;
	public ValidationMetricDouble[][] populationFitnessValuesOnTestSet;
	public ESIndividual[] offspringPopulation;
	public ValidationMetricDouble[][] offspringPopulationFitnessValues;
	public ValidationMetricDouble[][] offspringPopulationFitnessValuesOnTestSet;
	SelectionInterface selectionOperator;
	public int numberOfFitnessValues;
	
	/** Used for the update of success counter */
	ValidationMetricDouble[][] originalOffspringFitnessValues;
	
	/** Maps the name of representation class to the list with used mutation operators */
	HashMap<String,List<MutationInterface>> mutationMap;
	
	/** Maps the name of representation class to the list with used VNS operators */
	HashMap<String,List<MutationInterface>> vnsMap;
	
	/** ES run parameters */
	public int currentGeneration;
	public int currentEvaluation;
	public int currentSuccessCounter;
	ESLogger esLogger;
	private FitnessEvaluator fitnessEvalualor;
	
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
			
			// Set the optimization category id (if n-fold optimization is used, it is equal to learning category)
			String optimizationCategoryId = new String(
					(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getCategoryOptimizationId() >= 0) ? 
					((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getCategoryOptimizationId().toString() 
					: 
					((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getCategoryLearningId().toString());
			File folderForResults = new File(AmusePreferences.get(KeysStringValue.OPTIMIZATION_DATABASE) + "/" + 
					optimizationCategoryId + "/" + 
					((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getDestinationFolder());
			if(!folderForResults.exists()) {
				folderForResults.mkdirs();
			}
			esLogger = new ESLogger(new File(folderForResults + "/optimization_" + 
					folderForResults.listFiles().length + ".arff"));
		} else { // ..or continue writing to older log from previous experiment 
			esLogger = new ESLogger(new File(((OptimizationConfiguration)this.getCorrespondingScheduler().getConfiguration()).getContinueOldExperimentFrom()));
		}
			
		// Calculate the population fitness values for the first time
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
			Collections.shuffle(candidateIndices);
			offspringPopulation = new ESIndividual[offspringPopSize];
			for(int i=0;i<offspringPopSize;i++) {
				offspringPopulation[i] = population[candidateIndices.get(i)].clone();
				originalOffspringFitnessValues[i] = populationFitnessValues[candidateIndices.get(i)];
			}
			
			// -------------------
			// (II) Make crossover
			// -------------------
			
			// -------------------
			// (III) Make mutation
			// -------------------

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
				
				// Make logging after mutation of the current individual
				outputLog();
			}
			
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
			
			// Update the success counter going through offspring population
			for(int i=0;i<offspringPopSize;i++) {
				if((isMinimizingFitness && (offspringPopulationFitnessValues[i][0].getValue() < originalOffspringFitnessValues[i][0].getValue())) ||
						(!isMinimizingFitness && (offspringPopulationFitnessValues[i][0].getValue() > originalOffspringFitnessValues[i][0].getValue()))) {
					currentSuccessCounter++;
				}
			}
			
			// --------------------------------
			// (V) Update the parent population
			// --------------------------------
			selectionOperator.replaceParentPopulation();
			
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Generation: " + currentGeneration + 
					" Evaluation: " + currentEvaluation);
		}
		esLogger.close();
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "ES optimization finished");
	}

	private void runVNS(int offspringNumber, int representationToProceed,
			List<MutationInterface> mutationsToProceed) throws NodeException {
		
		// Load the current fitness
		ValidationMetricDouble[] currentFitness = new ValidationMetricDouble[numberOfFitnessValues];
		for(int i=0;i<numberOfFitnessValues;i++) {
			currentFitness[i] = new ValidationMetricDouble();
			currentFitness[i].setValue(new Double(offspringPopulationFitnessValues[offspringNumber][i].getValue()));
			currentFitness[i].setName(offspringPopulationFitnessValues[offspringNumber][i].getName());
		}
		
		// Load the current fitness for the test set
		ValidationMetricDouble[] currentFitnessOnTestSet = new ValidationMetricDouble[numberOfFitnessValues];
		for(int i=0;i<numberOfFitnessValues;i++) {
			currentFitnessOnTestSet[i] = new ValidationMetricDouble();
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
					
			ValidationMetricDouble[] newFitness = candidate.getFitness();
			ValidationMetricDouble[] newFitnessOnTestSet = new ValidationMetricDouble[newFitness.length]; 
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
		isIndependentTestSetUsed = (((OptimizationConfiguration)this.correspondingScheduler.getConfiguration()).
				getCategoryTestId() != -1) ? true : false;
		
		// Should the fitness value be minimized or maximized?
		try {
			MetricTable metricTable = new MetricTable(new File(esConfiguration.getConstantParameterByName("Metric table").
				getAttributes().getNamedItem("fileValue").getNodeValue()));
			if(metricTable.get(0).getOptimalValue() == 1d || metricTable.get(0).getOptimalValue() == Double.MAX_VALUE ||
					metricTable.get(0).getOptimalValue() == Double.POSITIVE_INFINITY) {
				isMinimizingFitness = false;
			}
			numberOfFitnessValues = metricTable.size(); 
		} catch(IOException e) {
			throw new NodeException("Could not load the metric table: " + e.getMessage());
		}
		
		// Set the population sizes
		Node strategy = esConfiguration.getESParameterByName("Population strategy");
		String strategyString = strategy.getAttributes().getNamedItem("stringValue").getNodeValue();
		
		// TODO for sms-emoa
		/*popSize = new Integer(strategyString.substring(0,strategyString.indexOf("+")));
		offspringPopSize = new Integer(strategyString.substring(strategyString.indexOf("+")+1));
		selectionOperator = new HypervolumeSelection(this);*/
		if(strategyString.indexOf("+") != -1) {
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
		populationFitnessValues = new ValidationMetricDouble[popSize][numberOfFitnessValues];
		offspringPopulation = new ESIndividual[offspringPopSize];
		offspringPopulationFitnessValues = new ValidationMetricDouble[offspringPopSize][numberOfFitnessValues];
		originalOffspringFitnessValues = new ValidationMetricDouble[offspringPopSize][numberOfFitnessValues];
		if(isIndependentTestSetUsed) {
			populationFitnessValuesOnTestSet = new ValidationMetricDouble[popSize][numberOfFitnessValues];
			offspringPopulationFitnessValuesOnTestSet = new ValidationMetricDouble[offspringPopSize][numberOfFitnessValues];
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
		// TODO set runtime limit; handle the situations if not all limits are set (e.g. only runtime)
		generationLimit = new Integer(esConfiguration.getESParameterByName("Number of generations").
				getAttributes().getNamedItem("intValue").getNodeValue());
		evaluationLimit = new Integer(esConfiguration.getESParameterByName("Number of evaluations").
				getAttributes().getNamedItem("intValue").getNodeValue());
		
		// Set the logging parameters
		loggingInterval = new Integer(esConfiguration.getOutputParameterByName("Logging interval").
				getAttributes().getNamedItem("intValue").getNodeValue());
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
		fitnessEvalualor = new FitnessEvaluator(this, isIndependentTestSetUsed);
		
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
		MetricTable metricTable;
		try {
			metricTable = new MetricTable(new File(esConfiguration.getConstantParameterByName("Metric table").
				getAttributes().getNamedItem("fileValue").getNodeValue()));
		} catch(IOException e) {
			throw new NodeException("Could not load the metric table: " + e.getMessage());
		}
		String[] fitnessValueNames = new String[numberOfFitnessValues];
		for(int i=0;i<numberOfFitnessValues;i++) {
			fitnessValueNames[i] = new String(metricTable.get(i).getName());
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
			
		// Output the current population fitness values
		if(logEvaluation) {
			esLogger.logString("@ATTRIBUTE 'Evaluation number' NUMERIC");
		}
		
		esLogger.logString(esLogger.sep + "@DATA");
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
			
			// Output the current population fitness values
			if(logEvaluation) {
				outputBuffer.append(currentEvaluation + ",");
			}
			
			// Write to the log file
			esLogger.logString(outputBuffer.deleteCharAt(outputBuffer.length()-1).toString());
		}
	}
	
	public ESConfiguration getConfiguration() {
		return esConfiguration;
	}
	
	public ValidationMetricDouble[] fitnessOf(int individualNumber) {
		return individualNumber < popSize ? populationFitnessValues[individualNumber] : 
			offspringPopulationFitnessValues[individualNumber - popSize];
	}
	
	public ValidationMetricDouble[] fitnessOfTestSet(int individualNumber) {
		return individualNumber < popSize ? populationFitnessValuesOnTestSet[individualNumber] : 
			offspringPopulationFitnessValuesOnTestSet[individualNumber - popSize];
	}

	/**
	 * @return the fitnessEvalualor
	 */
	public FitnessEvaluator getFitnessEvalualor() {
		return fitnessEvalualor;
	}

}

