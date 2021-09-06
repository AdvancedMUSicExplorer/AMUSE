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

import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Absolute error sums up the differences between labeled and predicted relationships.
 *  
 * @author Igor Vatolkin
 * @version $Id: AbsoluteError.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class AbsoluteError extends ClassificationQualityDoubleMeasureCalculator {

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
			
			// Calculate error
			Double error = Math.abs(groundTruthRelationships.get(i)-currentPredictedValue);
			errorSum += error;
		}
		
		// Prepare the result
		ValidationMeasureDouble[] absMeasure = new ValidationMeasureDouble[1];
		absMeasure[0] = new ValidationMeasureDouble();
		absMeasure[0].setId(200);
		absMeasure[0].setName("Absolute error on track level");
		absMeasure[0].setValue(errorSum);
		return absMeasure;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				Double error = Math.abs(groundTruthRelationships.get(i)-predictedRelationships.get(i).getRelationships()[j][0]);
				errorSum += error;
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] absMeasure = new ValidationMeasureDouble[1];
		absMeasure[0] = new ValidationMeasureDouble();
		absMeasure[0].setId(200);
		absMeasure[0].setName("Absolute error on classification window level");
		absMeasure[0].setValue(errorSum);
		return absMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		//TODO: check if it should be calculated like that
		// Go through all tracks
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			// Calculate the error for the current track
			double trackError = 0.0d;
			for(int j=0;j<groundTruthRelationships.get(i).getRelationships().length;j++) {
				//calculate the error for the current classification window
				double error = 0;
				for(int category=0;category<groundTruthRelationships.get(i).getLabels().length;category++) {
					error += Math.abs(groundTruthRelationships.get(i).getRelationships()[j][category] - predictedRelationships.get(i).getRelationships()[j][category]);
				}
				//divide the error by two because otherwise wrong classification windows are counted twice
				trackError += error/2;
			}
			
			trackError /= groundTruthRelationships.get(i).getRelationships().length;
			
			// Calculate error
			errorSum += trackError;
		}
		
		// Prepare the result
		ValidationMeasureDouble[] absMeasure = new ValidationMeasureDouble[1];
		absMeasure[0] = new ValidationMeasureDouble();
		absMeasure[0].setId(200);
		absMeasure[0].setName("Absolute error on track level");
		absMeasure[0].setValue(errorSum);
		return absMeasure;
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		// Go through all classification windows
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<groundTruthRelationships.get(i).getRelationships().length;j++) {
				//Calculate the error of the classification window
				double error = 0;
				for(int category=0;category<groundTruthRelationships.get(i).getLabels().length;category++) {
					error += Math.abs(groundTruthRelationships.get(i).getRelationships()[j][category] - predictedRelationships.get(i).getRelationships()[j][category]);
				}
				//Divide the error by two, because otherwise wrong classification windows are counted twice
				errorSum += error/2;
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] absMeasure = new ValidationMeasureDouble[1];
		absMeasure[0] = new ValidationMeasureDouble();
		absMeasure[0].setId(200);
		absMeasure[0].setName("Absolute error on classification window level");
		absMeasure[0].setValue(errorSum);
		return absMeasure;
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		//TODO: check if it should be calculated like that
		// Go through all tracks
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			// Calculate the error for the current track
			double trackError = 0.0d;
			for(int j=0;j<groundTruthRelationships.get(i).getRelationships().length;j++) {
				//Calculate the error for the current classification window
				double error = 0;
				for(int category=0;category<groundTruthRelationships.get(i).getLabels().length;category++) {
					error += Math.pow(groundTruthRelationships.get(i).getRelationships()[j][category] - predictedRelationships.get(i).getRelationships()[j][category], 2);
				}
				trackError += Math.sqrt(error);
			}
			
			trackError /= groundTruthRelationships.get(i).getRelationships().length;
			
			// Calculate error
			errorSum += trackError;
		}
		
		// Prepare the result
		ValidationMeasureDouble[] absMeasure = new ValidationMeasureDouble[1];
		absMeasure[0] = new ValidationMeasureDouble();
		absMeasure[0].setId(200);
		absMeasure[0].setName("Absolute error on track level");
		absMeasure[0].setValue(errorSum);
		return absMeasure;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnClasificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		//TODO: check if it should be calculated like that
		// Go through all classification windows
		double errorSum = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<groundTruthRelationships.get(i).getRelationships().length;j++) {
				//Calculate the error of the classification window
				double error = 0;
				for(int category=0;category<groundTruthRelationships.get(i).getLabels().length;category++) {
					error += Math.pow(groundTruthRelationships.get(i).getRelationships()[j][category] - predictedRelationships.get(i).getRelationships()[j][category], 2);
				}
				errorSum += Math.sqrt(error);
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] absMeasure = new ValidationMeasureDouble[1];
		absMeasure[0] = new ValidationMeasureDouble();
		absMeasure[0].setId(200);
		absMeasure[0].setName("Absolute error on classification window level");
		absMeasure[0].setValue(errorSum);
		return absMeasure;
	}
}

