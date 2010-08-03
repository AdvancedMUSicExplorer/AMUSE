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
 *  Creation date: 10.03.2008
 */ 
package amuse.nodes.validator.metrics;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitionsDescription;
import amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculator;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;

/**
 * The root mean square error calculates the root of the sum of squared differences between 
 * labeled and predicted relationships.
 *  
 * @author Igor Vatolkin
 * @version $Id: MeanSquaredError.java 1086 2010-07-01 14:08:46Z vatolkin $
 */
public class MeanSquaredError extends ClassificationQualityMetricCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMetric(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMetric(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		if(groundTruthRelationships.size() != predictedRelationships.size()) {
			throw new NodeException("The number of labeled instances must be equal to the number of predicted instances!");
		}
		
		ValidationMetricDouble[] metricOnSongLev = null;
		ValidationMetricDouble[] metricOnPartLev = null;
		
		if(groundTruthRelationships.get(0) instanceof Double) {
			if(this.getSongLevel()) {
				metricOnSongLev = calculateFuzzyMetricOnSongLevel(groundTruthRelationships, predictedRelationships);
			} 
			if(this.getPartitionLevel()) {
				metricOnPartLev = calculateFuzzyMetricOnPartitionLevel(groundTruthRelationships, predictedRelationships);
			}
		} else {
			return null;
		}
		
		// Return the corresponding number of metric values
		if(this.getSongLevel() && !this.getPartitionLevel()) {
			return metricOnSongLev;
		} else if(!this.getSongLevel() && this.getPartitionLevel()) {
			return metricOnPartLev;
		} else if(this.getSongLevel() && this.getPartitionLevel()) {
			ValidationMetricDouble[] metrics = new ValidationMetricDouble[2];
			metrics[0] = metricOnSongLev[0];
			metrics[1] = metricOnPartLev[0];
			return metrics;
		} else {
			return null;
		}
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateBinaryMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateBinaryMetricOnSongLevel(ArrayList<Boolean> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		return null;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateBinaryMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateBinaryMetricOnPartitionLevel(ArrayList<Boolean> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		return null;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateFuzzyMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateFuzzyMetricOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		// Maximum possible error on the given ground truth
		// E.g. if an instance has relationship 0.4, maximum classification error is 0.6 and
		// maximum squared error is 0.36
		double maxError = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			double firstBoundary = groundTruthRelationships.get(i);
			double secondBoundary = 1 - groundTruthRelationships.get(i);
			if(firstBoundary >= secondBoundary) {
				maxError += firstBoundary*firstBoundary;
			} else {
				maxError += secondBoundary*secondBoundary;
			}
		}
		maxError /= groundTruthRelationships.size(); // Expected maximum error per song
		
		// Calculate MSE error
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			// Calculate error
			Double error = (groundTruthRelationships.get(i)-currentPredictedValue) * 
				(groundTruthRelationships.get(i)-currentPredictedValue);
			errorSum += error;
		}
		errorSum = errorSum / groundTruthRelationships.size();
		errorSum = errorSum / maxError;
		
		// Prepare the result
		ValidationMetricDouble[] rmsMetric = new ValidationMetricDouble[1];
		rmsMetric[0] = new ValidationMetricDouble();
		rmsMetric[0].setId(202);
		rmsMetric[0].setName("Mean squared error on song level");
		rmsMetric[0].setValue(errorSum);
		return rmsMetric;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateFuzzyMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateFuzzyMetricOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		// Maximum possible error on the given ground truth
		// E.g. if an instance has relationship 0.4, maximum classification error is 0.6 and
		// maximum squared error is 0.36
		double maxError = 0.0d;
		int overallPartitionNumber = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			int currentPartitionNumber = predictedRelationships.get(i).getRelationships().length;
			overallPartitionNumber += currentPartitionNumber;
			double firstBoundary = groundTruthRelationships.get(i);
			double secondBoundary = 1 - groundTruthRelationships.get(i);
			double maxErrorForOnePartition;
			if(firstBoundary >= secondBoundary) {
				maxErrorForOnePartition = firstBoundary*firstBoundary;
			} else {
				maxErrorForOnePartition = secondBoundary*secondBoundary;
			}
			maxError += (maxErrorForOnePartition * currentPartitionNumber);
		}
		maxError /= overallPartitionNumber;
		
		// Calculate MSE error
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				Double error = (groundTruthRelationships.get(i)-predictedRelationships.get(i).getRelationships()[j]) * 
					(groundTruthRelationships.get(i)-predictedRelationships.get(i).getRelationships()[j]);
				errorSum += error;
			}
		}
		errorSum = errorSum / overallPartitionNumber;
		errorSum = errorSum / maxError;
		
		// Prepare the result
		ValidationMetricDouble[] rmsMetric = new ValidationMetricDouble[1];
		rmsMetric[0] = new ValidationMetricDouble();
		rmsMetric[0].setId(202);
		rmsMetric[0].setName("Mean squared error on partition level");
		rmsMetric[0].setValue(errorSum);
		return rmsMetric;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMulticlassMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMulticlassMetricOnSongLevel(ArrayList<ArrayList<Double>> groundTruthRelationships, ArrayList<ArrayList<ClassifiedSongPartitionsDescription>> predictedRelationships) throws NodeException {
		return null;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMulticlassMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMulticlassMetricOnPartitionLevel(ArrayList<ArrayList<Double>> groundTruthRelationships, ArrayList<ArrayList<ClassifiedSongPartitionsDescription>> predictedRelationships) throws NodeException {
		return null;
	}


}

