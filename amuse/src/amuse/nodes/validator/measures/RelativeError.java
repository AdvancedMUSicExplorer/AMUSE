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
package amuse.nodes.validator.measures;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.classifier.interfaces.MulticlassClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Relative error sums up the differences between labeled and predicted relationships and divides the sum by the number
 * of instances.
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class RelativeError extends ClassificationQualityDoubleMeasureCalculator {

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
		ValidationMeasureDouble[] relMeasure = new ValidationMeasureDouble[1];
		relMeasure[0] = new ValidationMeasureDouble();
		relMeasure[0].setId(201);
		relMeasure[0].setName("Relative error on song level");
		relMeasure[0].setValue(errorSum);
		return relMeasure;
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
		
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				Double error = Math.abs(groundTruthRelationships.get(i)-predictedRelationships.get(i).getRelationships()[j]);
				errorSum += error;
			}
		}
		
		errorSum /= overallPartitionNumber;
		
		// Prepare the result
		ValidationMeasureDouble[] relMeasure = new ValidationMeasureDouble[1];
		relMeasure[0] = new ValidationMeasureDouble();
		relMeasure[0].setId(201);
		relMeasure[0].setName("Relative error on partition level");
		relMeasure[0].setValue(errorSum);
		return relMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
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
		ValidationMeasureDouble[] relMeasure = new ValidationMeasureDouble[1];
		relMeasure[0] = new ValidationMeasureDouble();
		relMeasure[0].setId(201);
		relMeasure[0].setName("Relative error on song level");
		relMeasure[0].setValue(errorSum);
		return relMeasure;
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
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
		ValidationMeasureDouble[] arelMeasure = new ValidationMeasureDouble[1];
		arelMeasure[0] = new ValidationMeasureDouble();
		arelMeasure[0].setId(201);
		arelMeasure[0].setName("Relative error on partition level");
		arelMeasure[0].setValue(errorSum);
		return arelMeasure;
	}



}

