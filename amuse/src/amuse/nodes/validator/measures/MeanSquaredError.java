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

import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.interfaces.nodes.NodeException;
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
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnTrackLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		// Calculate MSE error
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			// Calculate the predicted value for this track (averaging among all classification windows)
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
		
		// Prepare the result
		ValidationMeasureDouble[] rmsMeasure = new ValidationMeasureDouble[1];
		rmsMeasure[0] = new ValidationMeasureDouble();
		rmsMeasure[0].setId(202);
		rmsMeasure[0].setName("Mean squared error on track level");
		rmsMeasure[0].setValue(errorSum);
		return rmsMeasure;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		int overallClassificationWindowNumber = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			int currentClassificationWindowNumber = predictedRelationships.get(i).getRelationships().length;
			overallClassificationWindowNumber += currentClassificationWindowNumber;
		}
		
		// Calculate MSE error
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				Double error = (groundTruthRelationships.get(i)-predictedRelationships.get(i).getRelationships()[j][0]) * 
					(groundTruthRelationships.get(i)-predictedRelationships.get(i).getRelationships()[j][0]);
				errorSum += error;
			}
		}
		errorSum = errorSum / overallClassificationWindowNumber;
		
		// Prepare the result
		ValidationMeasureDouble[] rmsMeasure = new ValidationMeasureDouble[1];
		rmsMeasure[0] = new ValidationMeasureDouble();
		rmsMeasure[0].setId(202);
		rmsMeasure[0].setName("Mean squared error on classification window level");
		rmsMeasure[0].setValue(errorSum);
		return rmsMeasure;
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
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		// Calculate MSE error
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			// Calculate the predicted value for this track (averaging among all classification windows)
			double[] currentPredictedValue = new double[numberOfCategories];
			for(int j=0;j<groundTruthRelationships.get(i).getRelationships().length;j++) {
				for(int category = 0; category < numberOfCategories; category++){
					currentPredictedValue[category] += predictedRelationships.get(i).getRelationships()[j][category];
				}
				
			}
			// Calculate the error for the current track
			Double error = 0.0;
			for(int category = 0; category < numberOfCategories; category++) {
				currentPredictedValue[category] /= predictedRelationships.get(i).getRelationships().length;
				error += (groundTruthRelationships.get(i).getRelationships()[0][category] - currentPredictedValue[category]) * (groundTruthRelationships.get(i).getRelationships()[0][category] - currentPredictedValue[category]);
			}
			errorSum += error;
		}
		errorSum = errorSum / groundTruthRelationships.size();
		
		// Prepare the result
		ValidationMeasureDouble[] rmsMeasure = new ValidationMeasureDouble[1];
		rmsMeasure[0] = new ValidationMeasureDouble();
		rmsMeasure[0].setId(202);
		rmsMeasure[0].setName("Mean squared error on track level");
		rmsMeasure[0].setValue(errorSum);
		return rmsMeasure;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		int overallClassificationWindowNumber = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			int currentClassificationWindowNumber = predictedRelationships.get(i).getRelationships().length;
			overallClassificationWindowNumber += currentClassificationWindowNumber;
		}
		
		// Calculate MSE error
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				for(int category = 0; category < numberOfCategories; category++) {
					Double error = (groundTruthRelationships.get(i).getRelationships()[j][category]-predictedRelationships.get(i).getRelationships()[j][category]) * 
						(groundTruthRelationships.get(i).getRelationships()[j][category]-predictedRelationships.get(i).getRelationships()[j][category]);
					errorSum += error;
				}
			}
		}
		errorSum = errorSum / overallClassificationWindowNumber;
		
		// Prepare the result
		ValidationMeasureDouble[] rmsMeasure = new ValidationMeasureDouble[1];
		rmsMeasure[0] = new ValidationMeasureDouble();
		rmsMeasure[0].setId(202);
		rmsMeasure[0].setName("Mean squared error on classification window level");
		rmsMeasure[0].setValue(errorSum);
		return rmsMeasure;
	}
}

