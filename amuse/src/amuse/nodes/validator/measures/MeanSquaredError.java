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
package amuse.nodes.validator.measures;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * The root mean square error calculates the root of the sum of squared differences between 
 * labeled and predicted relationships.
 *  
 * @author Igor Vatolkin
 * @version $Id: MeanSquaredError.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class MeanSquaredError extends ClassificationQualityDoubleMeasureCalculator {

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
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j][0];
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
		ValidationMeasureDouble[] rmsMeasure = new ValidationMeasureDouble[1];
		rmsMeasure[0] = new ValidationMeasureDouble();
		rmsMeasure[0].setId(202);
		rmsMeasure[0].setName("Mean squared error on song level");
		rmsMeasure[0].setValue(errorSum);
		return rmsMeasure;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
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
				Double error = (groundTruthRelationships.get(i)-predictedRelationships.get(i).getRelationships()[j][0]) * 
					(groundTruthRelationships.get(i)-predictedRelationships.get(i).getRelationships()[j][0]);
				errorSum += error;
			}
		}
		errorSum = errorSum / overallPartitionNumber;
		errorSum = errorSum / maxError;
		
		// Prepare the result
		ValidationMeasureDouble[] rmsMeasure = new ValidationMeasureDouble[1];
		rmsMeasure[0] = new ValidationMeasureDouble();
		rmsMeasure[0].setId(202);
		rmsMeasure[0].setName("Mean squared error on partition level");
		rmsMeasure[0].setValue(errorSum);
		return rmsMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " is not yet implemented for multiclass classification in the current version");
		//TODO update this method
//		// Go through all songs
//		double errorSum = 0.0d;
//		for(int i=0;i<groundTruthRelationships.size();i++) {
//			
//			// Calculate the error for the current song
//			double songError = 0.0d;
//			for(int j=0;j<((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).getLabels().length;j++) {
//				String currentPartitionGTLabel = ((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).
//						getLabels()[j];
//				String currentPartitionPredictedLabel = ((MulticlassClassifiedSongPartitions)predictedRelationships.get(i)).
//						getLabels()[j];
//				if(!currentPartitionGTLabel.equals(currentPartitionPredictedLabel)) {
//					songError++;
//				}
//			}
//			songError /= ((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).getLabels().length;
//			
//			errorSum += (songError*songError);
//		}
//		errorSum /= groundTruthRelationships.size();
//		
//		// Prepare the result
//		ValidationMeasureDouble[] rmsMeasure = new ValidationMeasureDouble[1];
//		rmsMeasure[0] = new ValidationMeasureDouble();
//		rmsMeasure[0].setId(202);
//		rmsMeasure[0].setName("Mean squared error on song level");
//		rmsMeasure[0].setValue(errorSum);
//		return rmsMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " is not yet implemented for multiclass classification in the current version");
		//TODO update this method
//		// Go through all partitions
//		double errorSum = 0.0d;
//		int partitionNumber =0;
//		for(int i=0;i<groundTruthRelationships.size();i++) {
//			partitionNumber += ((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).getLabels().length;
//			for(int j=0;j<((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).getLabels().length;j++) {
//				String currentPartitionGTLabel = ((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).
//						getLabels()[j];
//				String currentPartitionPredictedLabel = ((MulticlassClassifiedSongPartitions)predictedRelationships.get(i)).
//						getLabels()[j];
//				if(!currentPartitionGTLabel.equals(currentPartitionPredictedLabel)) {
//					errorSum++;
//				}
//			}
//		}
//		errorSum /= partitionNumber;
//		
//		// Prepare the result
//		ValidationMeasureDouble[] rmsMeasure = new ValidationMeasureDouble[1];
//		rmsMeasure[0] = new ValidationMeasureDouble();
//		rmsMeasure[0].setId(202);
//		rmsMeasure[0].setName("Mean squared error on partition level");
//		rmsMeasure[0].setValue(errorSum);
//		return rmsMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " cannot be calculated for multilabel classification tasks");
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " cannot be calculated for multilabel classification tasks");
	}
}

