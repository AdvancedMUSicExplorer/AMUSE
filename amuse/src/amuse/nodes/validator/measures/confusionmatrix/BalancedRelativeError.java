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
 * Precision measure
 *  
 * @author Philipp Ginsel
 * @version $Id: Precision.java 243 2019-10-14 14:18:30Z frederik-h $
 */
public class BalancedRelativeError extends ClassificationQualityDoubleMeasureCalculator {

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
		
		// Get true positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setTrackLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tp = truePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setTrackLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fp = falsePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setTrackLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tn = trueNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setTrackLevel(true);
		falseNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fn = falseNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double bre = 0.5 * (fn.getValue()/(tp.getValue() + fn.getValue()) + fp.getValue()/(tn.getValue() + fp.getValue()));
		
		// Prepare the result
		ValidationMeasureDouble[] breMeasure = new ValidationMeasureDouble[1];
		breMeasure[0] = new ValidationMeasureDouble(false);
		breMeasure[0].setId(115);
		breMeasure[0].setName("Balanced relative error on track level");
		breMeasure[0].setValue(new Double(bre));
		return breMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		
		// Get true positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setWindowLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tp = truePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setWindowLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fp = falsePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setWindowLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tn = trueNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setWindowLevel(true);
		falseNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fn = falseNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double bre = 0.5 * (fn.getValue()/(tp.getValue() + fn.getValue()) + fp.getValue()/(tn.getValue() + fp.getValue()));
		
		// Prepare the result
		ValidationMeasureDouble[] breMeasure = new ValidationMeasureDouble[1];
		breMeasure[0] = new ValidationMeasureDouble(false);
		breMeasure[0].setId(115);
		breMeasure[0].setName("Balanced relative error on classification window level");
		breMeasure[0].setValue(new Double(bre));
		return breMeasure;
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
		// Get true positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setTrackLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tp = truePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setTrackLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fp = falsePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setTrackLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setTrackLevel(true);
		falseNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fn = falseNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		double tpSum = 0;
		double fpSum = 0;
		double tnSum = 0;
		double fnSum = 0;
		
		for(int i = 0; i < numberOfCategories; i++) {
			tpSum += tp[i].getValue();
			fpSum += fp[i].getValue();
			tnSum += tn[i].getValue();
			fnSum += fn[i].getValue();
		}
		
		double bre = 0.5 * (fnSum / (tpSum + fnSum) + fpSum / (tnSum + fpSum));
		
		// Prepare the result
		ValidationMeasureDouble[] breMeasure = new ValidationMeasureDouble[1];
		breMeasure[0] = new ValidationMeasureDouble(false);
		breMeasure[0].setId(115);
		breMeasure[0].setName("Balanced relative error on track level");
		breMeasure[0].setValue(new Double(bre));
		return breMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		// Get true positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setWindowLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tp = truePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setWindowLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fp = falsePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setWindowLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setWindowLevel(true);
		falseNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fn = falseNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		double tpSum = 0;
		double fpSum = 0;
		double tnSum = 0;
		double fnSum = 0;
		
		for(int i = 0; i < numberOfCategories; i++) {
			tpSum += tp[i].getValue();
			fpSum += fp[i].getValue();
			tnSum += tn[i].getValue();
			fnSum += fn[i].getValue();
		}
		
		double bre = 0.5 * (fnSum / (tpSum + fnSum) + fpSum / (tnSum + fpSum));
		
		// Prepare the result
		ValidationMeasureDouble[] breMeasure = new ValidationMeasureDouble[1];
		breMeasure[0] = new ValidationMeasureDouble(false);
		breMeasure[0].setId(115);
		breMeasure[0].setName("Balanced relative error on classification window level");
		breMeasure[0].setValue(new Double(bre));
		return breMeasure;
	}
}

