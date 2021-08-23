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
 *  Number of false positives
 *  
 * @author Igor Vatolkin
 * @version $Id: FalsePositives.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class FalsePositives extends ClassificationQualityDoubleMeasureCalculator {

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
		double numberOfFalsePositives = 0;
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
			
			numberOfFalsePositives += (1 - currentGroundTruthValue.doubleValue()) * currentPredictedValue.doubleValue();
			
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falsePositivesMeasure = new ValidationMeasureDouble[1];
		falsePositivesMeasure[0] = new ValidationMeasureDouble();
		falsePositivesMeasure[0].setId(102);
		falsePositivesMeasure[0].setName("Number of false positives on track level");
		falsePositivesMeasure[0].setValue(new Double(numberOfFalsePositives));
		return falsePositivesMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		double numberOfFalsePositives = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				numberOfFalsePositives += (1-groundTruthRelationships.get(i).doubleValue()) * predictedRelationships.get(i).getRelationships()[j][0].doubleValue();
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falsePositivesMeasure = new ValidationMeasureDouble[1];
		falsePositivesMeasure[0] = new ValidationMeasureDouble();
		falsePositivesMeasure[0].setId(102);
		falsePositivesMeasure[0].setName("Number of false positives on classification window level");
		falsePositivesMeasure[0].setValue(new Double(numberOfFalsePositives));
		return falsePositivesMeasure;
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
		double[] numberOfFalsePositives = new double[groundTruthRelationships.get(0).getLabels().length];
		
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
				
				numberOfFalsePositives[category] += (1 - currentGroundTruthValue.doubleValue()) * currentPredictedValue.doubleValue();
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falsePositivesMeasure = new ValidationMeasureDouble[numberOfFalsePositives.length];
		for(int i = 0; i < falsePositivesMeasure.length; i++) {
			falsePositivesMeasure[i] = new ValidationMeasureDouble();
			falsePositivesMeasure[i].setId(102);
			falsePositivesMeasure[i].setName("Number of false positives on track level for category " + groundTruthRelationships.get(0).getLabels()[i]);
			falsePositivesMeasure[i].setValue(new Double(numberOfFalsePositives[i]));
		}
		return falsePositivesMeasure;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		double[] numberOfFlasePositives = new double[groundTruthRelationships.get(0).getLabels().length];
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				for(int category = 0; category < numberOfFlasePositives.length; category ++) {
					numberOfFlasePositives[category] += (1 - groundTruthRelationships.get(i).getRelationships()[j][category]) * predictedRelationships.get(i).getRelationships()[j][category];
				}
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] falsePositivesMeasure = new ValidationMeasureDouble[numberOfFlasePositives.length];
		for(int i = 0; i < numberOfFlasePositives.length; i++) {
			falsePositivesMeasure[i] = new ValidationMeasureDouble();
			falsePositivesMeasure[i].setId(102);
			falsePositivesMeasure[i].setName("Number of false positives on classification window level for cateogry " + groundTruthRelationships.get(0).getLabels()[i]);
			falsePositivesMeasure[i].setValue(new Double(numberOfFlasePositives[i]));
		}
		return falsePositivesMeasure;
	}

}

