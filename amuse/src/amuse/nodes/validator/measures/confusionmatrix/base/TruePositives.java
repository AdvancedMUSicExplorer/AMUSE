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
import java.util.Arrays;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Number of true positives
 *  
 * @author Igor Vatolkin
 * @version $Id: TruePositives.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class TruePositives extends ClassificationQualityDoubleMeasureCalculator {

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
		double numberOfTruePositives = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j][0];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			System.out.println("PredictedValue = " + currentPredictedValue);
			
			//If the classification was not continuous, round the predicted values
			if(!isContinuous()) {
				if(currentPredictedValue >= 0.5) {
						currentPredictedValue = 1.0d;
				} else {
						currentPredictedValue = 0.0d;
				}
			}
			
			System.out.println("Rounded Predicted Value = " + currentPredictedValue);
			
			Double currentGroundTruthValue = groundTruthRelationships.get(i);
			
			System.out.println("GroundTruth Value = " + currentGroundTruthValue);
			
			System.out.println("True Positive = " + currentGroundTruthValue.doubleValue() * currentPredictedValue.doubleValue());
			
			System.out.println();
			
			numberOfTruePositives += currentGroundTruthValue.doubleValue() * currentPredictedValue.doubleValue();
			
		}
		
		// Prepare the result
		ValidationMeasureDouble[] truePositivesMeasure = new ValidationMeasureDouble[1];
		truePositivesMeasure[0] = new ValidationMeasureDouble();
		truePositivesMeasure[0].setId(100);
		truePositivesMeasure[0].setName("Number of true positives on song level");
		truePositivesMeasure[0].setValue(new Double(numberOfTruePositives));
		return truePositivesMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		double numberOfTruePositives = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				numberOfTruePositives += groundTruthRelationships.get(i).doubleValue() * predictedRelationships.get(i).getRelationships()[j][0].doubleValue();
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] truePositivesMeasure = new ValidationMeasureDouble[1];
		truePositivesMeasure[0] = new ValidationMeasureDouble();
		truePositivesMeasure[0].setId(100);
		truePositivesMeasure[0].setName("Number of true positives on partition level");
		truePositivesMeasure[0].setValue(new Double(numberOfTruePositives));
		return truePositivesMeasure;
	}

	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		return calculateMultiLabelMeasureOnSongLevel(groundTruthRelationships, predictedRelationships);
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		return calculateMultiLabelMeasureOnPartitionLevel(groundTruthRelationships, predictedRelationships);
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		double[] numberOfTruePositives = new double[groundTruthRelationships.get(0).getLabels().length];
		
		for(int i = 0; i < groundTruthRelationships.size(); i++	) {
			for(int category = 0; category < groundTruthRelationships.get(i).getLabels().length; category ++) {
				// Calculate the predicted value for this song (averaging among all partitions)
				Double currentPredictedValue = 0.0d;
				for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
					currentPredictedValue += predictedRelationships.get(i).getRelationships()[j][category];
				}
				currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
				
				//If the classification was not continuous, round the predicted values
				if(!isContinuous()) {
					if(currentPredictedValue >= 0.5) {
						currentPredictedValue = 1.0d;
					} else {
						currentPredictedValue = 0.0d;
					}
				}
				
				Double currentGroundTruthValue = groundTruthRelationships.get(i).getRelationships()[0][category];
				
				numberOfTruePositives[category] += currentGroundTruthValue.doubleValue() * currentPredictedValue.doubleValue();
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] truePositivesMeasure = new ValidationMeasureDouble[numberOfTruePositives.length];
		for(int i = 0; i < truePositivesMeasure.length; i++) {
			truePositivesMeasure[i] = new ValidationMeasureDouble();
			truePositivesMeasure[i].setId(100);
			truePositivesMeasure[i].setName("Number of true positives on song level for category " + groundTruthRelationships.get(0).getLabels()[i]);
			truePositivesMeasure[i].setValue(new Double(numberOfTruePositives[i]));
		}
		return truePositivesMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		double[] numberOfTruePositives = new double[groundTruthRelationships.get(0).getLabels().length];
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				for(int category = 0; category < numberOfTruePositives.length; category ++) {
					numberOfTruePositives[category] += groundTruthRelationships.get(i).getRelationships()[j][category] * predictedRelationships.get(i).getRelationships()[j][category];
				}
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] truePositivesMeasure = new ValidationMeasureDouble[numberOfTruePositives.length];
		for(int i = 0; i < numberOfTruePositives.length; i++) {
			truePositivesMeasure[i] = new ValidationMeasureDouble();
			truePositivesMeasure[i].setId(100);
			truePositivesMeasure[i].setName("Number of true positives on partition level for cateogry " + groundTruthRelationships.get(0).getLabels()[i]);
			truePositivesMeasure[i].setValue(new Double(numberOfTruePositives[i]));
		}
		return truePositivesMeasure;
	}
}

