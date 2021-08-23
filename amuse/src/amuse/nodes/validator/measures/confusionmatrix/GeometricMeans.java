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
 *  Creation date: 08.12.2009
 */ 
package amuse.nodes.validator.measures.confusionmatrix;

import java.util.ArrayList;

import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * This measure is high when sensivity and specificity are high and the difference between them is low. 
 * By optimizing geometric means, it is ensured that the accuracy on the both classes is maximized and 
 * the distribution stays balanced.
 *  
 * @author Igor Vatolkin
 * @version $Id: GeometricMeans.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class GeometricMeans extends ClassificationQualityDoubleMeasureCalculator {

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
		
		double gm = Math.sqrt(recall[0].getValue() * specificity[0].getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] geometricMeansMeasure = new ValidationMeasureDouble[1];
		geometricMeansMeasure[0] = new ValidationMeasureDouble(false);
		geometricMeansMeasure[0].setId(113);
		geometricMeansMeasure[0].setName("Geometric means on track level");
		geometricMeansMeasure[0].setValue(new Double(gm));
		return geometricMeansMeasure;
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
		
		double gm = Math.sqrt(recall[0].getValue() * specificity[0].getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] geometricMeansMeasure = new ValidationMeasureDouble[1];
		geometricMeansMeasure[0] = new ValidationMeasureDouble(false);
		geometricMeansMeasure[0].setId(113);
		geometricMeansMeasure[0].setName("Geometric means on classification window level");
		geometricMeansMeasure[0].setValue(new Double(gm));
		return geometricMeansMeasure;
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
		
		double gm = Math.sqrt(recall[0].getValue() * specificity[0].getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] geometricMeansMeasure = new ValidationMeasureDouble[1];
		geometricMeansMeasure[0] = new ValidationMeasureDouble(false);
		geometricMeansMeasure[0].setId(113);
		geometricMeansMeasure[0].setName("Geometric means on track level");
		geometricMeansMeasure[0].setValue(new Double(gm));
		return geometricMeansMeasure;
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
		
		double gm = Math.sqrt(recall[0].getValue() * specificity[0].getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] geometricMeansMeasure = new ValidationMeasureDouble[1];
		geometricMeansMeasure[0] = new ValidationMeasureDouble(false);
		geometricMeansMeasure[0].setId(113);
		geometricMeansMeasure[0].setName("Geometric means on classification window level");
		geometricMeansMeasure[0].setValue(new Double(gm));
		return geometricMeansMeasure;
	}
}

