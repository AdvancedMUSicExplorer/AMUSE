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

import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 *  Number of false negatives
 *  
 * @author Igor Vatolkin
 * @version $Id: FalseNegatives.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class FalseNegatives extends ClassificationQualityDoubleMeasureCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnTrackLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		double numberOfFalseNegatives = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this track (averaging among all classification windows)
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
			
			numberOfFalseNegatives += currentGroundTruthValue.doubleValue() * (1-currentPredictedValue.doubleValue());
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falseNegativesMeasure = new ValidationMeasureDouble[1];
		falseNegativesMeasure[0] = new ValidationMeasureDouble();
		falseNegativesMeasure[0].setId(103);
		falseNegativesMeasure[0].setName("Number of false negatives on track level");
		falseNegativesMeasure[0].setValue(new Double(numberOfFalseNegatives));
		return falseNegativesMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		double numberOfFalseNegatives = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				numberOfFalseNegatives += groundTruthRelationships.get(i).doubleValue() * (1-predictedRelationships.get(i).getRelationships()[j][0].doubleValue());
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falseNegativesMeasure = new ValidationMeasureDouble[1];
		falseNegativesMeasure[0] = new ValidationMeasureDouble();
		falseNegativesMeasure[0].setId(103);
		falseNegativesMeasure[0].setName("Number of false negatives on classification window level");
		falseNegativesMeasure[0].setValue(new Double(numberOfFalseNegatives));
		return falseNegativesMeasure;
	}

	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		return calculateMultiLabelMeasureOnTrackLevel(groundTruthRelationships, predictedRelationships);
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		return calculateMultiLabelMeasureOnWindowLevel(groundTruthRelationships, predictedRelationships);
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		double[] numberOfFalseNegatives = new double[groundTruthRelationships.get(0).getLabels().length];
		
		for(int i = 0; i < groundTruthRelationships.size(); i++	) {
			for(int category = 0; category < groundTruthRelationships.get(i).getLabels().length; category ++) {
				// Calculate the predicted value for this track (averaging among all classification windows)
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
				
				numberOfFalseNegatives[category] += currentGroundTruthValue.doubleValue() * (1-currentPredictedValue.doubleValue());
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falseNegativesMeasure = new ValidationMeasureDouble[numberOfFalseNegatives.length];
		for(int i = 0; i < falseNegativesMeasure.length; i++) {
			falseNegativesMeasure[i] = new ValidationMeasureDouble();
			falseNegativesMeasure[i].setId(103);
			falseNegativesMeasure[i].setName("Number of false negatives on track level for category " + groundTruthRelationships.get(0).getLabels()[i]);
			falseNegativesMeasure[i].setValue(new Double(numberOfFalseNegatives[i]));
		}
		return falseNegativesMeasure;
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		double[] numberOfFalseNegatives = new double[groundTruthRelationships.get(0).getLabels().length];
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				for(int category = 0; category < numberOfFalseNegatives.length; category ++) {
					numberOfFalseNegatives[category] += groundTruthRelationships.get(i).getRelationships()[j][category] * (1-predictedRelationships.get(i).getRelationships()[j][category]);
				}
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falseNegativesMeasure = new ValidationMeasureDouble[numberOfFalseNegatives.length];
		for(int i = 0; i < numberOfFalseNegatives.length; i++) {
			falseNegativesMeasure[i] = new ValidationMeasureDouble();
			falseNegativesMeasure[i].setId(103);
			falseNegativesMeasure[i].setName("Number of false negatives on classification window level for cateogry " + groundTruthRelationships.get(0).getLabels()[i]);
			falseNegativesMeasure[i].setValue(new Double(numberOfFalseNegatives[i]));
		}
		return falseNegativesMeasure;
	}

}

