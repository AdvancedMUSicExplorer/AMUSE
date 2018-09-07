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
 *  Creation date: 25.11.2009
 */ 
package amuse.nodes.validator.measures.correlation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Spearman's rank correlation coefficient is a special case of Pearson product-moment correlation coefficient.
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class SpearmansRhoRankCorrelation extends ClassificationQualityDoubleMeasureCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Sort labeled and predicted values
		ArrayList<Double> sortedPredictedRelationships = new ArrayList<Double>(groundTruthRelationships.size());
		ArrayList<Double> sortedLabeledRelationships = new ArrayList<Double>(groundTruthRelationships.size());
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			sortedPredictedRelationships.add(currentPredictedValue);
			sortedLabeledRelationships.add(new Double(groundTruthRelationships.get(i)));
		}
		Collections.sort(sortedPredictedRelationships);
		Collections.sort(sortedLabeledRelationships);
		
		// Calculate ranks for predicted values
		HashMap<Double, Double> predictedValueToRang = new HashMap<Double, Double>();
		for(int i=sortedPredictedRelationships.size()-1;i>=0;i--) {
			
			// Calculate the number of the equal values
			int equalValuesNumber = 1;
			double rank = sortedPredictedRelationships.size() - i; // First position in descending order
			for(int j=i-1;j>=0;j--) {
				if(sortedPredictedRelationships.get(j).equals(sortedPredictedRelationships.get(i))) {
					equalValuesNumber++;
					rank += sortedPredictedRelationships.size() - j; // Add the next position in descending order
				} else {
					break;
				}
			}
			rank /= equalValuesNumber;
			predictedValueToRang.put(sortedPredictedRelationships.get(i), rank);
			
			// Go to the next position with different value
			i -= (equalValuesNumber-1);
		}
		
		// Calculate ranks for labeled values
		HashMap<Double, Double> labeledValueToRang = new HashMap<Double, Double>();
		for(int i=sortedLabeledRelationships.size()-1;i>=0;i--) {
			
			// Calculate the number of the equal values
			int equalValuesNumber = 1;
			double rank = sortedLabeledRelationships.size() - i; // First position in descending order
			for(int j=i-1;j>=0;j--) {
				if(sortedLabeledRelationships.get(j).equals(sortedLabeledRelationships.get(i))) {
					equalValuesNumber++;
					rank += sortedLabeledRelationships.size() - j; // Add the next position in descending order
				} else {
					break;
				}
			}
			rank /= equalValuesNumber;
			labeledValueToRang.put(sortedLabeledRelationships.get(i), rank);
			
			// Go to the next position with different value
			i -= (equalValuesNumber-1);
		}
		
		// Calculate the Spearman's rank correlation
		double sumOfRankMultiplications = 0.0d;
		double sumOfSquaredPredictedRanks = 0.0d;
		double sumOfSquaredLabeledRanks = 0.0d;
		double p = groundTruthRelationships.size() * Math.pow((groundTruthRelationships.size()+1d)/2d, 2);
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			sumOfRankMultiplications += predictedValueToRang.get(currentPredictedValue) * labeledValueToRang.get(groundTruthRelationships.get(i));
			sumOfSquaredPredictedRanks += Math.pow(predictedValueToRang.get(currentPredictedValue), 2);
			sumOfSquaredLabeledRanks += Math.pow(labeledValueToRang.get(groundTruthRelationships.get(i)), 2);
			
		}	
		
		// Calculate the correlation coefficient
		double corrCoef = (sumOfRankMultiplications - p) / 
			(Math.sqrt(sumOfSquaredPredictedRanks - p) * Math.sqrt(sumOfSquaredLabeledRanks - p));
		
		// Prepare the result
		ValidationMeasureDouble[] correlationMeasure = new ValidationMeasureDouble[1];
		correlationMeasure[0] = new ValidationMeasureDouble(false);
		correlationMeasure[0].setId(301);
		correlationMeasure[0].setName("Speraman's rank correlation coefficient on song level");
		correlationMeasure[0].setValue(corrCoef);
		return correlationMeasure;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Calculate the number of all partitions
		int overallPartitionNumber = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			overallPartitionNumber += predictedRelationships.get(i).getRelationships().length;
		}
		
		// Sort labeled and predicted values
		ArrayList<Double> sortedPredictedRelationships = new ArrayList<Double>(groundTruthRelationships.size());
		ArrayList<Double> sortedLabeledRelationships = new ArrayList<Double>(groundTruthRelationships.size());
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				sortedPredictedRelationships.add(predictedRelationships.get(i).getRelationships()[j]);
				sortedLabeledRelationships.add(new Double(groundTruthRelationships.get(i)));
			}
		}
		Collections.sort(sortedPredictedRelationships);
		Collections.sort(sortedLabeledRelationships);
		
		
		// Calculate ranks for predicted values
		HashMap<Double, Double> predictedValueToRang = new HashMap<Double, Double>();
		for(int i=sortedPredictedRelationships.size()-1;i>=0;i--) {
			
			// Calculate the number of the equal values
			int equalValuesNumber = 1;
			double rank = sortedPredictedRelationships.size() - i; // First position in descending order
			for(int j=i-1;j>=0;j--) {
				if(sortedPredictedRelationships.get(j).equals(sortedPredictedRelationships.get(i))) {
					equalValuesNumber++;
					rank += sortedPredictedRelationships.size() - j; // Add the next position in descending order
				} else {
					break;
				}
			}
			rank /= equalValuesNumber;
			predictedValueToRang.put(sortedPredictedRelationships.get(i), rank);
			
			// Go to the next position with different value
			i -= (equalValuesNumber-1);
		}
		
		// Calculate ranks for labeled values
		HashMap<Double, Double> labeledValueToRang = new HashMap<Double, Double>();
		for(int i=sortedLabeledRelationships.size()-1;i>=0;i--) {
			
			// Calculate the number of the equal values
			int equalValuesNumber = 1;
			double rank = sortedLabeledRelationships.size() - i; // First position in descending order
			for(int j=i-1;j>=0;j--) {
				if(sortedLabeledRelationships.get(j).equals(sortedLabeledRelationships.get(i))) {
					equalValuesNumber++;
					rank += sortedLabeledRelationships.size() - j; // Add the next position in descending order
				} else {
					break;
				}
			}
			rank /= equalValuesNumber;
			labeledValueToRang.put(sortedLabeledRelationships.get(i), rank);
			
			// Go to the next position with different value
			i -= (equalValuesNumber-1);
		}
		
		// Calculate the Spearman's rank correlation
		double sumOfRankMultiplications = 0.0d;
		double sumOfSquaredPredictedRanks = 0.0d;
		double sumOfSquaredLabeledRanks = 0.0d;
		double p = overallPartitionNumber * Math.pow((overallPartitionNumber+1d)/2d, 2);
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				sumOfRankMultiplications += predictedValueToRang.get(predictedRelationships.get(i).getRelationships()[j]) * labeledValueToRang.get(groundTruthRelationships.get(i));
				sumOfSquaredPredictedRanks += Math.pow(predictedValueToRang.get(predictedRelationships.get(i).getRelationships()[j]), 2);
				sumOfSquaredLabeledRanks += Math.pow(labeledValueToRang.get(groundTruthRelationships.get(i)), 2);
			}
		}
		
		// Calculate the correlation coefficient
		double corrCoef = (sumOfRankMultiplications - p) / 
			(Math.sqrt(sumOfSquaredPredictedRanks - p) * Math.sqrt(sumOfSquaredLabeledRanks - p));
		
		// Prepare the result
		ValidationMeasureDouble[] correlationMeasure = new ValidationMeasureDouble[1];
		correlationMeasure[0] = new ValidationMeasureDouble(false);
		correlationMeasure[0].setId(301);
		correlationMeasure[0].setName("Speraman's rank correlation coefficient on partition level");
		correlationMeasure[0].setValue(corrCoef);
		return correlationMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}



}

