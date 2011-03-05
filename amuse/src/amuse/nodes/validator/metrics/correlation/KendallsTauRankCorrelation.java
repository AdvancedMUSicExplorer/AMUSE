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
 *  Creation date: 01.12.2009
 */ 
package amuse.nodes.validator.metrics.correlation;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMetricCalculator;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;

/**
 * Kendall's tau rank correlation coefficient is based on the comparison of all possible ordered pairs of instances between ground truth
 * and labeled data.
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class KendallsTauRankCorrelation extends ClassificationQualityDoubleMetricCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateOneClassMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateOneClassMetricOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Rank calculation is not required, since the numeric values can be compared directly!
		// Calculate the predicted song relationships (averaged over all partitions)
		ArrayList<Double> predictedSongRelationships = new ArrayList<Double>(groundTruthRelationships.size());
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			predictedSongRelationships.add(currentPredictedValue);
		}
		
		// Calculate all ordered pairs of instances for ground truth and the number of tied (similar) values for ground truth
		int tied_gt = 0;
		ArrayList<Integer[]> orderedPairsGroundTruth = new ArrayList<Integer[]>();
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=i+1;j<groundTruthRelationships.size();j++) {
				if(groundTruthRelationships.get(i) > groundTruthRelationships.get(j)) {
					orderedPairsGroundTruth.add(new Integer[]{i,j}); 
				} else if(groundTruthRelationships.get(i) < groundTruthRelationships.get(j)){
					orderedPairsGroundTruth.add(new Integer[]{j,i});
				} else {
					tied_gt++;
				}
			}
		}
		
		// Calculate all ordered pairs of instances for predicted values and the number of tied (similar) values for predicted values
		int tied_predicted = 0;
		ArrayList<Integer[]> orderedPairsPredicted = new ArrayList<Integer[]>();
		for(int i=0;i<predictedSongRelationships.size();i++) {
			for(int j=i+1;j<predictedSongRelationships.size();j++) {
				if(predictedSongRelationships.get(i) > predictedSongRelationships.get(j)) {
					orderedPairsPredicted.add(new Integer[]{i,j}); 
				} else if(predictedSongRelationships.get(i) < predictedSongRelationships.get(j)) {
					orderedPairsPredicted.add(new Integer[]{j,i});
				} else {
					tied_predicted++;
				}
			}
		}
		
		// Calculate the number of equal untied pairs for ground truth and predicted values
		int equal_pairs = 0;
		for(int i=0;i<orderedPairsGroundTruth.size();i++) {
			for(int j=0;j<orderedPairsPredicted.size();j++) {

				// Comparison
				if(orderedPairsGroundTruth.get(i)[0].equals(orderedPairsPredicted.get(j)[0]) &&
				   orderedPairsGroundTruth.get(i)[1].equals(orderedPairsPredicted.get(j)[1])) {
					equal_pairs++;
					break;
				}
			}
		}
		
		// Number of unequal pairs for ground truth and predicted values
		int unequal_pairs = orderedPairsGroundTruth.size() + orderedPairsPredicted.size() - 2*equal_pairs;
		
		// Calculate the Kendall's tau rank correlation coefficient
		double corrCoef = (equal_pairs - unequal_pairs) /  
			(Math.sqrt(equal_pairs + unequal_pairs + tied_predicted) * Math.sqrt(equal_pairs + unequal_pairs + tied_gt));
		
		// Prepare the result
		ValidationMetricDouble[] correlationMetric = new ValidationMetricDouble[1];
		correlationMetric[0] = new ValidationMetricDouble(false);
		correlationMetric[0].setId(302);
		correlationMetric[0].setName("Kendall's tau rank correlation coefficient on song level");
		correlationMetric[0].setValue(corrCoef);
		return correlationMetric;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateOneClassMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateOneClassMetricOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// The the current implementation is very slow if very large partition number is used!
		// It can be done faster if it is assumed that only binary relationships are allowed
		
		// Save the ground truth values for each partition
		ArrayList<Double> groundTruthPartitionRelationships = new ArrayList<Double>();
		for(int i=0;i<predictedRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				groundTruthPartitionRelationships.add(new Double(groundTruthRelationships.get(i)));
			}
		}
		
		// Save the predicted values for each partition
		ArrayList<Double> predictedPartitionRelationships = new ArrayList<Double>();
		for(int i=0;i<predictedRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				predictedPartitionRelationships.add(new Double(predictedRelationships.get(i).getRelationships()[j]));
			}
		}
		
		// Calculate all ordered pairs of instances for ground truth and the number of tied (similar) values for ground truth
		int tied_gt = 0;
		ArrayList<Integer[]> orderedPairsGroundTruth = new ArrayList<Integer[]>();
		for(int i=0;i<groundTruthPartitionRelationships.size();i++) {
			for(int j=i+1;j<groundTruthPartitionRelationships.size();j++) {
				if(groundTruthPartitionRelationships.get(i) > groundTruthPartitionRelationships.get(j)) {
					orderedPairsGroundTruth.add(new Integer[]{i,j}); 
				} else if(groundTruthPartitionRelationships.get(i) < groundTruthPartitionRelationships.get(j)){
					orderedPairsGroundTruth.add(new Integer[]{j,i});
				} else {
					tied_gt++;
				}
			}
		}
		
		// Calculate all ordered pairs of instances for predicted values and the number of tied (similar) values for predicted values
		int tied_predicted = 0;
		ArrayList<Integer[]> orderedPairsPredicted = new ArrayList<Integer[]>();
		for(int i=0;i<predictedPartitionRelationships.size();i++) {
			for(int j=i+1;j<predictedPartitionRelationships.size();j++) {
				if(predictedPartitionRelationships.get(i) > predictedPartitionRelationships.get(j)) {
					orderedPairsPredicted.add(new Integer[]{i,j}); 
				} else if(predictedPartitionRelationships.get(i) < predictedPartitionRelationships.get(j)) {
					orderedPairsPredicted.add(new Integer[]{j,i});
				} else {
					tied_predicted++;
				}
			}
		}
		
		// Calculate the number of equal untied pairs for ground truth and predicted values
		int equal_pairs = 0;
		for(int i=0;i<orderedPairsGroundTruth.size();i++) {
			for(int j=0;j<orderedPairsPredicted.size();j++) {

				// Comparison
				if(orderedPairsGroundTruth.get(i)[0].equals(orderedPairsPredicted.get(j)[0]) &&
				   orderedPairsGroundTruth.get(i)[1].equals(orderedPairsPredicted.get(j)[1])) {
					equal_pairs++;
					break;
				}
			}
		}
		
		// Number of unequal pairs for ground truth and predicted values
		int unequal_pairs = orderedPairsGroundTruth.size() + orderedPairsPredicted.size() - 2*equal_pairs;
		
		// Calculate the Kendall's tau rank correlation coefficient
		double corrCoef = (equal_pairs - unequal_pairs) /  
			(Math.sqrt(equal_pairs + unequal_pairs + tied_predicted) * Math.sqrt(equal_pairs + unequal_pairs + tied_gt));
		
		// Prepare the result
		ValidationMetricDouble[] correlationMetric = new ValidationMetricDouble[1];
		correlationMetric[0] = new ValidationMetricDouble(false);
		correlationMetric[0].setId(302);
		correlationMetric[0].setName("Kendall's tau rank correlation coefficient on partition level");
		correlationMetric[0].setValue(corrCoef);
		return correlationMetric;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMulticlassMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMultiClassMetricOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMulticlassMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMultiClassMetricOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}



}

