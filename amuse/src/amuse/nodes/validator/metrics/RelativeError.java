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
package amuse.nodes.validator.metrics;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.classifier.interfaces.MulticlassClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMetricCalculator;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;

/**
 * Relative error sums up the differences between labeled and predicted relationships and divides the sum by the number
 * of instances.
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class RelativeError extends ClassificationQualityDoubleMetricCalculator {

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
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			// Calculate error
			Double error = Math.abs(groundTruthRelationships.get(i)-currentPredictedValue);
			errorSum += error;
		}
		errorSum /= groundTruthRelationships.size();
		
		// Prepare the result
		ValidationMetricDouble[] relMetric = new ValidationMetricDouble[1];
		relMetric[0] = new ValidationMetricDouble();
		relMetric[0].setId(201);
		relMetric[0].setName("Relative error on song level");
		relMetric[0].setValue(errorSum);
		return relMetric;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateOneClassMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateOneClassMetricOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Calculate the number of all partitions
		int overallPartitionNumber = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			overallPartitionNumber += predictedRelationships.get(i).getRelationships().length;
		}
		
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				Double error = Math.abs(groundTruthRelationships.get(i)-predictedRelationships.get(i).getRelationships()[j]);
				errorSum += error;
			}
		}
		
		errorSum /= overallPartitionNumber;
		
		// Prepare the result
		ValidationMetricDouble[] relMetric = new ValidationMetricDouble[1];
		relMetric[0] = new ValidationMetricDouble();
		relMetric[0].setId(201);
		relMetric[0].setName("Relative error on partition level");
		relMetric[0].setValue(errorSum);
		return relMetric;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMulticlassMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMultiClassMetricOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Go through all songs
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the error for the current song
			double songError = 0.0d;
			for(int j=0;j<((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).getLabels().length;j++) {
				String currentPartitionGTLabel = ((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).
						getLabels()[j];
				String currentPartitionPredictedLabel = ((MulticlassClassifiedSongPartitions)predictedRelationships.get(i)).
						getLabels()[j];
				if(!currentPartitionGTLabel.equals(currentPartitionPredictedLabel)) {
					songError++;
				}
			}
			songError /= ((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).getLabels().length;
			
			// Calculate error
			errorSum += songError;
		}
		
		// Prepare the result
		ValidationMetricDouble[] relMetric = new ValidationMetricDouble[1];
		relMetric[0] = new ValidationMetricDouble();
		relMetric[0].setId(201);
		relMetric[0].setName("Relative error on song level");
		relMetric[0].setValue(errorSum);
		return relMetric;
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMulticlassMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMultiClassMetricOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Go through all partitions
		double errorSum = 0.0d;
		int partitionNumber =0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			partitionNumber += ((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).getLabels().length;
			for(int j=0;j<((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).getLabels().length;j++) {
				String currentPartitionGTLabel = ((MulticlassClassifiedSongPartitions)groundTruthRelationships.get(i)).
						getLabels()[j];
				String currentPartitionPredictedLabel = ((MulticlassClassifiedSongPartitions)predictedRelationships.get(i)).
						getLabels()[j];
				if(!currentPartitionGTLabel.equals(currentPartitionPredictedLabel)) {
					errorSum++;
				}
			}
		}
		
		// Prepare the result
		ValidationMetricDouble[] arelMetric = new ValidationMetricDouble[1];
		arelMetric[0] = new ValidationMetricDouble();
		arelMetric[0].setId(201);
		arelMetric[0].setName("Relative error on partition level");
		arelMetric[0].setValue(errorSum);
		return arelMetric;
	}



}

