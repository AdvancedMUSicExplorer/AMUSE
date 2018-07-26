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
 *  Creation date: 23.01.2009
 */ 
package amuse.nodes.validator.measures.confusionmatrix.base;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 *  Number of false negatives
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class FalseNegatives extends ClassificationQualityDoubleMeasureCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.calculateMulticlassMeasureOnSongLevel#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.calculateMulticlassMeasureOnSongLevel#calculateOneClassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		int numberOfFalseNegatives = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			if(currentPredictedValue >= 0.5) {
				currentPredictedValue = 1.0d;
			} else {
				currentPredictedValue = 0.0d;
			}
			
			// Round the ground truth value to a binary value
			Double currentGroundTruthValue = groundTruthRelationships.get(i);
			if(currentGroundTruthValue >= 0.5) {
				currentGroundTruthValue = 1.0d;
			} else {
				currentGroundTruthValue = 0.0d;
			}
			
			if(currentGroundTruthValue.doubleValue() == 1.0 && currentPredictedValue.doubleValue() == 0.0) {
				numberOfFalseNegatives++;
			}
			
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falseNegativesMeasure = new ValidationMeasureDouble[1];
		falseNegativesMeasure[0] = new ValidationMeasureDouble();
		falseNegativesMeasure[0].setId(103);
		falseNegativesMeasure[0].setName("Number of false negatives on song level");
		falseNegativesMeasure[0].setValue(new Double(numberOfFalseNegatives));
		return falseNegativesMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.calculateMulticlassMeasureOnSongLevel#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		int numberOfFalseNegatives = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				if(groundTruthRelationships.get(i).doubleValue() == 1.0 && 
						predictedRelationships.get(i).getRelationships()[j].doubleValue() == 0.0) {
					numberOfFalseNegatives++;
				}
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falseNegativesMeasure = new ValidationMeasureDouble[1];
		falseNegativesMeasure[0] = new ValidationMeasureDouble();
		falseNegativesMeasure[0].setId(103);
		falseNegativesMeasure[0].setName("Number of false negatives on partition level");
		falseNegativesMeasure[0].setValue(new Double(numberOfFalseNegatives));
		return falseNegativesMeasure;
	}

	
	/**
	 * @see amuse.nodes.validator.interfaces.calculateMulticlassMeasureOnSongLevel#calculateMulticlassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}


	/**
	 * @see amuse.nodes.validator.interfaces.calculateMulticlassMeasureOnSongLevel#calculateMulticlassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}


}

