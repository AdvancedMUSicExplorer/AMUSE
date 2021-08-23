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
 * Creation date: 24.01.2010
 */
package amuse.nodes.optimizer.methods.es.operators.mutation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.data.FeatureTable;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.optimizer.methods.es.EvolutionaryStrategy;
import amuse.nodes.optimizer.methods.es.operators.mutation.interfaces.AbstractMutation;
import amuse.nodes.optimizer.methods.es.representation.BinaryVector;
import amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface;
import amuse.util.AmuseLogger;

/**
 * Increases the number of selected features (ones) by one adding the feature (switching zero to one) which is least
 * correlated with already selected features
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class AddLeastCorrelatedFeature extends AbstractMutation {

	/** Parameters from ESConfiguration which are saved here for faster processing */
	double[][] correlationTable;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#mutate(amuse.nodes.optimizer.methods.es.representation.AbstractRepresentation)
	 */
	public void mutate(RepresentationInterface representation) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Add least correlated feature mutation started");
		if(representation instanceof BinaryVector) {
			BinaryVector valueToMutate = (BinaryVector)representation;
			
			// At least one feature which is not selected must exist for this mutation
			boolean notSelectedFeatureFound = false;
			for(int i=0;i<valueToMutate.getValue().length;i++) {
				if(!valueToMutate.getValue()[i]) {
					notSelectedFeatureFound = true;
					break;
				}
			}
			if(!notSelectedFeatureFound) {
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Mutation not possible");
				return;
			}
			
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Current value: " + valueToMutate.toString());

			// Add the feature which is least correlated with selected features
			ArrayList<Integer> indicesOfUsedFeatures = new ArrayList<Integer>(valueToMutate.getValue().length);
			ArrayList<Integer> indicesOfNotUsedFeatures = new ArrayList<Integer>(valueToMutate.getValue().length);
			for(int i=0;i<valueToMutate.getValue().length;i++) {
				if(valueToMutate.getValue()[i]) {
					indicesOfUsedFeatures.add(i);
				} else {
					indicesOfNotUsedFeatures.add(i);
				}
			}
			
			double[] ranksOfNotUsedFeatures = new double[indicesOfNotUsedFeatures.size()];
			for(int i=0;i<indicesOfNotUsedFeatures.size();i++) {
				double averageRank = 0d;
				
				// Index of the current (not used) feature for calculation of correlation rank 
				int f1 = indicesOfNotUsedFeatures.get(i);
				for(int j=0;j<indicesOfUsedFeatures.size();j++) {
					
					// Index of the current (used) feature
					int f2 = indicesOfUsedFeatures.get(j);
					
					// Since only a half of a correlation matrix is saved...
					if(f1<f2) {
						averageRank += correlationTable[f1][f2];
					} else {
						averageRank += correlationTable[f2][f1];
					}
				}
				ranksOfNotUsedFeatures[i] = averageRank / indicesOfUsedFeatures.size();
			}
			
			// Add a feature with the smallest average correlation to the used features
			double smallestCorrCoef = Double.MAX_VALUE;
			int bestPosition = 0;
			for(int i=0;i<ranksOfNotUsedFeatures.length;i++) {
				if(ranksOfNotUsedFeatures[i] < smallestCorrCoef) {
					smallestCorrCoef = ranksOfNotUsedFeatures[i];
					bestPosition = i;
				}
			}
			
			int featureToAdd = indicesOfNotUsedFeatures.get(bestPosition);
			valueToMutate.getValue()[featureToAdd] = true;
			
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Feature " + featureToAdd + " is added");
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Mutated value: " + valueToMutate.toString());
		} else {
			throw new NodeException("Representation class (" + representation.getClass().toString() + ") must be BinaryVector!");
		}
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Add least correlated feature mutation finished");
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#setParameters(org.w3c.dom.NodeList)
	 */
	public void setParameters(NodeList parameters, EvolutionaryStrategy correspondingStrategy) throws NodeException {
		
		this.correspondingES = correspondingStrategy;
		String pathToCorrelationTable = new String();
		
		for(int i=0;i<parameters.getLength();i++) {
			if(parameters.item(i).getNodeType() == Node.ELEMENT_NODE) {
				String parameterName = parameters.item(i).getAttributes().getNamedItem("name").getNodeValue();
				if(parameterName.equals(new String("Path to correlation table"))) {
					pathToCorrelationTable = this.correspondingES.getCorrespondingScheduler().getHomeFolder() + File.separator +  
							parameters.item(i).getAttributes().getNamedItem("fileValue").getNodeValue();
					break;
				} 
			}
		}
		
		// TODO other correlation methods are possible: e.g. correlation between already processed features!!
		
		// Initialize the table with correlation coefficients between all possible pairs of feature dimensions
		try {
			DataSetAbstract correlationTableSet = new ArffDataSet(new File(pathToCorrelationTable));
			
			// How much is the overall number of single feature dimensions?
			Node featureTableNode = correspondingStrategy.getConfiguration().getConstantParameterByName("Feature table");
			FeatureTable ft = new FeatureTable(new File(featureTableNode.getAttributes().getNamedItem("fileValue").getNodeValue()));
			int factor = 0;
			Node selectedFeaturesNode = correspondingStrategy.getConfiguration().getOptimizationParameterByName("Selected features");
			NodeList parameterList = selectedFeaturesNode.getChildNodes();
			for(int i=0;i<parameterList.getLength();i++) {
				if(parameterList.item(i).getNodeType() == Node.ELEMENT_NODE && parameterList.item(i).getNodeName().equals("optimizationParameter")) {
					if(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Maximum factor of generated features " +
							"related to initial raw feature set")) {
						factor = new Integer(parameterList.item(i).getAttributes().getNamedItem("intValue").getNodeValue());
						break;
					}
				}
			}
			if(factor == 0) {
				factor = 1;
			}
			int numberOfFeatureDimensions = ft.getDimensionsCount() * factor;
			correlationTable = new double[numberOfFeatureDimensions][numberOfFeatureDimensions];
			
			// Get the correlation coefficients
			for(int i=0;i<correlationTableSet.getValueCount();i++) {
				int firstFeatureId = new Double(correlationTableSet.getAttribute("First feature id").getValueAt(i).toString()).intValue();
				int firstFeatureDimension = new Double(correlationTableSet.getAttribute("First feature dimension").getValueAt(i).toString()).intValue();
				int secondFeatureId = new Double(correlationTableSet.getAttribute("Second feature id").getValueAt(i).toString()).intValue();
				int secondFeatureDimension = new Double(correlationTableSet.getAttribute("Second feature dimension").getValueAt(i).toString()).intValue();
				double corrCoef = new Double(correlationTableSet.getAttribute("Correlation coefficient").getValueAt(i).toString());
				
				// Now the ids and dimensions must be mapped to coordinates in the correlation matrix of single dimensions
				int dimCounterFirstFeature = 0;
				boolean firstFeatureIsInTable = false;
				for(int f=0;f<ft.size();f++) {
					if(firstFeatureId != ft.getFeatureAt(f).getId()) {
						dimCounterFirstFeature += ft.getFeatureAt(f).getDimension();
					} else {
						dimCounterFirstFeature += firstFeatureDimension;
						firstFeatureIsInTable = true;
						break;
					}
				}
				int dimCounterSecondFeature = 0;
				boolean secondFeatureIsInTable = false;
				for(int f=0;f<ft.size();f++) {
					if(secondFeatureId != ft.getFeatureAt(f).getId()) {
						dimCounterSecondFeature += ft.getFeatureAt(f).getDimension();
					} else {
						dimCounterSecondFeature += secondFeatureDimension;
						secondFeatureIsInTable = true;
						break;
					}
				}
				
				if(firstFeatureIsInTable && secondFeatureIsInTable) {
				
					// Set the current correlation coefficient for all dimensions which are generated by the corresponding raw features.
					// If e.g. a GMM1 model is used, and correlation coefficient c1 is given between zerocrossings and spectral centroid,
					// here it is the same for pairs (mean classification window value of zc; mean classification window value of sc),
					// (mean classification window value of zc; stddev classification window value of sc), (stddev classification window value of zc; mean classification window value of sc)
					// and (stddev classification window value of zc; stddev classification window value of sc)
					for(int m=0;m<factor;m++) {
						for(int n=0;n<factor;n++) {
							correlationTable[(dimCounterFirstFeature-1)*factor + m][(dimCounterSecondFeature-1)*factor + n] = corrCoef;
						}
					}
				}
			}
		} catch(IOException e) {
			throw new NodeException("Could not read the table with correlation coefficients between features: " + e.getMessage());
		}
	}

}
