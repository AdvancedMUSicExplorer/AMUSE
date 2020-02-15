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

import amuse.data.annotation.ClassifiedSongPartitions;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 *  Number of true negatives
 *  
 * @author Igor Vatolkin
 * @version $Id: TrueNegatives.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class TrueNegatives extends ClassificationQualityDoubleMeasureCalculator {

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
		double numberOfTrueNegatives = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j][0];
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
			
			Double currentGroundTruthValue = groundTruthRelationships.get(i);
			
			numberOfTrueNegatives += (1-currentGroundTruthValue.doubleValue()) * (1-currentPredictedValue.doubleValue());
			
		}
		
		// Prepare the result
		ValidationMeasureDouble[] trueNegativesMeasure = new ValidationMeasureDouble[1];
		trueNegativesMeasure[0] = new ValidationMeasureDouble();
		trueNegativesMeasure[0].setId(101);
		trueNegativesMeasure[0].setName("Number of true negatives on song level");
		trueNegativesMeasure[0].setValue(new Double(numberOfTrueNegatives));
		return trueNegativesMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		double numberOfTrueNegatives = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				numberOfTrueNegatives += (1-groundTruthRelationships.get(i).doubleValue()) * (1-predictedRelationships.get(i).getRelationships()[j][0].doubleValue());
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] trueNegativesMeasure = new ValidationMeasureDouble[1];
		trueNegativesMeasure[0] = new ValidationMeasureDouble();
		trueNegativesMeasure[0].setId(101);
		trueNegativesMeasure[0].setName("Number of true negatives on partition level");
		trueNegativesMeasure[0].setValue(new Double(numberOfTrueNegatives));
		return trueNegativesMeasure;
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
		double[] numberOfTrueNegatives = new double[groundTruthRelationships.get(0).getLabels().length];
		
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
				
				numberOfTrueNegatives[category] += (1 - currentGroundTruthValue.doubleValue()) * (1 - currentPredictedValue.doubleValue());
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] trueNegativesMeasure = new ValidationMeasureDouble[numberOfTrueNegatives.length];
		for(int i = 0; i < trueNegativesMeasure.length; i++) {
			trueNegativesMeasure[i] = new ValidationMeasureDouble();
			trueNegativesMeasure[i].setId(101);
			trueNegativesMeasure[i].setName("Number of true negatives on song level for category " + groundTruthRelationships.get(0).getLabels()[i]);
			trueNegativesMeasure[i].setValue(new Double(numberOfTrueNegatives[i]));
		}
		return trueNegativesMeasure;
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		double[] numberOfTrueNegatives = new double[groundTruthRelationships.get(0).getLabels().length];
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				for(int category = 0; category < numberOfTrueNegatives.length; category ++) {
					numberOfTrueNegatives[category] += (1 - groundTruthRelationships.get(i).getRelationships()[j][category]) * (1 - predictedRelationships.get(i).getRelationships()[j][category]);
				}
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] trueNegativesMeasure = new ValidationMeasureDouble[numberOfTrueNegatives.length];
		for(int i = 0; i < numberOfTrueNegatives.length; i++) {
			trueNegativesMeasure[i] = new ValidationMeasureDouble();
			trueNegativesMeasure[i].setId(101);
			trueNegativesMeasure[i].setName("Number of true negatives on partition level for cateogry " + groundTruthRelationships.get(0).getLabels()[i]);
			trueNegativesMeasure[i].setValue(new Double(numberOfTrueNegatives[i]));
		}
		return trueNegativesMeasure;
	}
}

