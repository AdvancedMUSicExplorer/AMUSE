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
 *  Creation date: 07.12.2009
 */ 
package amuse.nodes.validator.measures.confusionmatrix;

import java.util.ArrayList;

import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Higher positive and lower negative likelihood mean better performance on positive and negative classes.
 *  
 * @author Igor Vatolkin
 * @version $Id: Likelihoods.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class Likelihoods extends ClassificationQualityDoubleMeasureCalculator {

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
		
		Specificity specificityCalculator = new Specificity();
		specificityCalculator.setContinuous(isContinuous());
		Recall recallCalculator = new Recall();
		recallCalculator.setContinuous(isContinuous());
		
		ValidationMeasureDouble[] specificity = specificityCalculator.calculateOneClassMeasureOnTrackLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMeasureDouble[] recall = recallCalculator.calculateOneClassMeasureOnTrackLevel(
				groundTruthRelationships, predictedRelationships);
		
		double likelihoodPositive = recall[0].getValue() / (1 - specificity[0].getValue());
		double likelihoodNegative = (1 - recall[0].getValue()) / specificity[0].getValue();
		
		// Prepare the result
		ValidationMeasureDouble[] likelihoodsMeasure = new ValidationMeasureDouble[2];
		likelihoodsMeasure[0] = new ValidationMeasureDouble();
		likelihoodsMeasure[0].setId(111);
		likelihoodsMeasure[0].setName("Positive likelihood on track level");
		likelihoodsMeasure[0].setValue(new Double(likelihoodPositive));
		likelihoodsMeasure[1] = new ValidationMeasureDouble();
		likelihoodsMeasure[1].setId(111);
		likelihoodsMeasure[1].setName("Negative likelihood on track level");
		likelihoodsMeasure[1].setValue(new Double(likelihoodNegative));
		return likelihoodsMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		
		Specificity specificityCalculator = new Specificity();
		specificityCalculator.setContinuous(isContinuous());
		Recall recallCalculator = new Recall();
		recallCalculator.setContinuous(isContinuous());
		
		ValidationMeasureDouble[] specificity = specificityCalculator.calculateOneClassMeasureOnClassficationWindowLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMeasureDouble[] recall = recallCalculator.calculateOneClassMeasureOnClassficationWindowLevel(
				groundTruthRelationships, predictedRelationships);
		
		double likelihoodPositive = recall[0].getValue() / (1 - specificity[0].getValue());
		double likelihoodNegative = (1 - recall[0].getValue()) / specificity[0].getValue();
		
		// Prepare the result
		ValidationMeasureDouble[] likelihoodsMeasure = new ValidationMeasureDouble[2];
		likelihoodsMeasure[0] = new ValidationMeasureDouble();
		likelihoodsMeasure[0].setId(111);
		likelihoodsMeasure[0].setName("Positive likelihood on classification window level");
		likelihoodsMeasure[0].setValue(new Double(likelihoodPositive));
		likelihoodsMeasure[1] = new ValidationMeasureDouble();
		likelihoodsMeasure[1].setId(111);
		likelihoodsMeasure[1].setName("Negative likelihood on classification window level");
		likelihoodsMeasure[1].setValue(new Double(likelihoodNegative));
		return likelihoodsMeasure;
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
		Specificity specificityCalculator = new Specificity();
		specificityCalculator.setContinuous(isContinuous());
		Recall recallCalculator = new Recall();
		recallCalculator.setContinuous(isContinuous());
		
		ValidationMeasureDouble[] specificity = specificityCalculator.calculateMultiLabelMeasureOnTrackLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMeasureDouble[] recall = recallCalculator.calculateMultiLabelMeasureOnTrackLevel(
				groundTruthRelationships, predictedRelationships);
		
		double likelihoodPositive = recall[0].getValue() / (1 - specificity[0].getValue());
		double likelihoodNegative = (1 - recall[0].getValue()) / specificity[0].getValue();
		
		// Prepare the result
		ValidationMeasureDouble[] likelihoodsMeasure = new ValidationMeasureDouble[2];
		likelihoodsMeasure[0] = new ValidationMeasureDouble();
		likelihoodsMeasure[0].setId(111);
		likelihoodsMeasure[0].setName("Positive likelihood on track level");
		likelihoodsMeasure[0].setValue(new Double(likelihoodPositive));
		likelihoodsMeasure[1] = new ValidationMeasureDouble();
		likelihoodsMeasure[1].setId(111);
		likelihoodsMeasure[1].setName("Negative likelihood on track level");
		likelihoodsMeasure[1].setValue(new Double(likelihoodNegative));
		return likelihoodsMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		Specificity specificityCalculator = new Specificity();
		specificityCalculator.setContinuous(isContinuous());
		Recall recallCalculator = new Recall();
		recallCalculator.setContinuous(isContinuous());
		
		ValidationMeasureDouble[] specificity = specificityCalculator.calculateMultiLabelMeasureOnWindowLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMeasureDouble[] recall = recallCalculator.calculateMultiLabelMeasureOnWindowLevel(
				groundTruthRelationships, predictedRelationships);
		
		double likelihoodPositive = recall[0].getValue() / (1 - specificity[0].getValue());
		double likelihoodNegative = (1 - recall[0].getValue()) / specificity[0].getValue();
		
		// Prepare the result
		ValidationMeasureDouble[] likelihoodsMeasure = new ValidationMeasureDouble[2];
		likelihoodsMeasure[0] = new ValidationMeasureDouble();
		likelihoodsMeasure[0].setId(111);
		likelihoodsMeasure[0].setName("Positive likelihood on classification window level");
		likelihoodsMeasure[0].setValue(new Double(likelihoodPositive));
		likelihoodsMeasure[1] = new ValidationMeasureDouble();
		likelihoodsMeasure[1].setId(111);
		likelihoodsMeasure[1].setName("Negative likelihood on classification window level");
		likelihoodsMeasure[1].setValue(new Double(likelihoodNegative));
		return likelihoodsMeasure;
	}
}

