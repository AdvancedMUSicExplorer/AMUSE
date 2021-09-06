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
import amuse.nodes.validator.measures.confusionmatrix.base.FalsePositives;
import amuse.nodes.validator.measures.confusionmatrix.base.TrueNegatives;

/**
 * Specificity measure
 *  
 * @author Igor Vatolkin
 * @version $Id: Specificity.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class Specificity extends ClassificationQualityDoubleMeasureCalculator {

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
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setTrackLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tn = trueNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setTrackLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fp = falsePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double specificity = tn.getValue() / (fp.getValue() + tn.getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] specificityMeasure = new ValidationMeasureDouble[1];
		specificityMeasure[0] = new ValidationMeasureDouble(false);
		specificityMeasure[0].setId(107);
		specificityMeasure[0].setName("Specificity on track level");
		specificityMeasure[0].setValue(new Double(specificity));
		return specificityMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setWindowLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tn = trueNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setWindowLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fp = falsePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double specificity = tn.getValue() / (fp.getValue() + tn.getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] specificityMeasure = new ValidationMeasureDouble[1];
		specificityMeasure[0] = new ValidationMeasureDouble(false);
		specificityMeasure[0].setId(107);
		specificityMeasure[0].setName("Specificity on classification window level");
		specificityMeasure[0].setValue(new Double(specificity));
		return specificityMeasure;
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
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setTrackLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setTrackLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fp = falsePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		double enumerator = 0;
		double denominator = 0;
		for(int i = 0; i < numberOfCategories; i++) {
			enumerator += tn[i].getValue();
			denominator += fp[i].getValue() + tn[i].getValue();
		}
		
		double specificity = enumerator / denominator;
		
		// Prepare the result
		ValidationMeasureDouble[] specificityMeasure = new ValidationMeasureDouble[1];
		specificityMeasure[0] = new ValidationMeasureDouble(false);
		specificityMeasure[0].setId(107);
		specificityMeasure[0].setName("Specificity on track level");
		specificityMeasure[0].setValue(new Double(specificity));
		return specificityMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setWindowLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setWindowLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fp = falsePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		double enumerator = 0;
		double denominator = 0;
		for(int i = 0; i < numberOfCategories; i++) {
			enumerator += tn[i].getValue();
			denominator += fp[i].getValue() + tn[i].getValue();
		}
		
		double specificity = enumerator / denominator;
		
		// Prepare the result
		ValidationMeasureDouble[] specificityMeasure = new ValidationMeasureDouble[1];
		specificityMeasure[0] = new ValidationMeasureDouble(false);
		specificityMeasure[0].setId(107);
		specificityMeasure[0].setName("Specificity on classification window level");
		specificityMeasure[0].setValue(new Double(specificity));
		return specificityMeasure;
	}
}

