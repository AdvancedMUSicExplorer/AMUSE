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
package amuse.nodes.validator.measures.confusionmatrix;

import java.util.ArrayList;

import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;
import amuse.nodes.validator.measures.confusionmatrix.base.FalseNegatives;
import amuse.nodes.validator.measures.confusionmatrix.base.FalsePositives;
import amuse.nodes.validator.measures.confusionmatrix.base.TrueNegatives;
import amuse.nodes.validator.measures.confusionmatrix.base.TruePositives;

/**
 * The root mean square error calculates the root of the sum of squared differences between 
 * labeled and predicted relationships.
 *  
 * @author Igor Vatolkin
 * @version $Id: Accuracy.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class Accuracy extends ClassificationQualityDoubleMeasureCalculator {

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
		double errorSum = 0.0d;
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
			
			// Calculate error
			Double error = Math.abs(currentPredictedValue - currentGroundTruthValue);
			errorSum += error;
		}
		Double numberOfCorrectClassifications = groundTruthRelationships.size() - errorSum;
		Double accuracy = numberOfCorrectClassifications * 1.0d / groundTruthRelationships.size();
		
		// Prepare the result
		ValidationMeasureDouble[] accuracyMeasure = new ValidationMeasureDouble[1];
		accuracyMeasure[0] = new ValidationMeasureDouble(false);
		accuracyMeasure[0].setId(104);
		accuracyMeasure[0].setName("Accuracy on track level");
		accuracyMeasure[0].setValue(accuracy);
		return accuracyMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		double errorSum = 0;
		int classificationWindowNumber = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			classificationWindowNumber += predictedRelationships.get(i).getRelationships().length;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				errorSum += Math.abs(predictedRelationships.get(i).getRelationships()[j][0].doubleValue() - groundTruthRelationships.get(i).doubleValue());
			}
		}
		double numberOfCorrectClassifications = classificationWindowNumber - errorSum;
		Double accuracy = new Double(numberOfCorrectClassifications) * 1.0d / classificationWindowNumber;
		
		// Prepare the result
		ValidationMeasureDouble[] accuracyMeasure = new ValidationMeasureDouble[1];
		accuracyMeasure[0] = new ValidationMeasureDouble(false);
		accuracyMeasure[0].setId(104);
		accuracyMeasure[0].setName("Accuracy on classification window level");
		accuracyMeasure[0].setValue(accuracy);
		return accuracyMeasure;
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
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		//Get True Positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setTrackLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tp = truePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		//Get True Negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setTrackLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		//Get False Positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setTrackLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fp = falsePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		//Get False Negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setTrackLevel(true);
		falseNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fn = falseNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		double accuracy = 0;
		for(int i = 0; i < numberOfCategories; i++) {
			accuracy += (tp[i].getValue() + tn[i].getValue()) / (tp[i].getValue() + fn[i].getValue() + fp[i].getValue() + tn[i].getValue());
		}
		accuracy /= numberOfCategories;
		
		// Prepare the result
		ValidationMeasureDouble[] accuracyMeasure = new ValidationMeasureDouble[1];
		accuracyMeasure[0] = new ValidationMeasureDouble(false);
		accuracyMeasure[0].setId(104);
		accuracyMeasure[0].setName("Accuracy on track level");
		accuracyMeasure[0].setValue(accuracy);
		return accuracyMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		//Get True Positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setWindowLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tp = truePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		//Get True Negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setWindowLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		//Get False Positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setWindowLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fp = falsePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		//Get False Negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setWindowLevel(true);
		falseNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fn = falseNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		double accuracy = 0;
		for(int i = 0; i < numberOfCategories; i++) {
			accuracy += (tp[i].getValue() + tn[i].getValue()) / (tp[i].getValue() + fn[i].getValue() + fp[i].getValue() + tn[i].getValue());
		}
		accuracy /= numberOfCategories;
		
		// Prepare the result
		ValidationMeasureDouble[] accuracyMeasure = new ValidationMeasureDouble[1];
		accuracyMeasure[0] = new ValidationMeasureDouble(false);
		accuracyMeasure[0].setId(104);
		accuracyMeasure[0].setName("Accuracy on classification window level");
		accuracyMeasure[0].setValue(accuracy);
		return accuracyMeasure;
	}

}

