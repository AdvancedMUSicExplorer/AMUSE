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

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
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
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Get true positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setSongLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tp = truePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setSongLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fp = falsePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setSongLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tn = trueNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setSongLevel(true);
		falseNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fn = falseNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double bre = 0.5 * (fn.getValue()/(tp.getValue() + fn.getValue()) + fp.getValue()/(tn.getValue() + fp.getValue()));
		
		// Prepare the result
		ValidationMeasureDouble[] breMeasure = new ValidationMeasureDouble[1];
		breMeasure[0] = new ValidationMeasureDouble(false);
		breMeasure[0].setId(115);
		breMeasure[0].setName("Balanced relative error on song level");
		breMeasure[0].setValue(new Double(bre));
		return breMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Get true positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setPartitionLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tp = truePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setPartitionLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fp = falsePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setPartitionLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tn = trueNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setPartitionLevel(true);
		falseNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fn = falseNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double bre = 0.5 * (fn.getValue()/(tp.getValue() + fn.getValue()) + fp.getValue()/(tn.getValue() + fp.getValue()));
		
		System.out.println("Partition Level, One Class");
		System.out.println("TP = " + tp.getValue());
		System.out.println("TN = " + tn.getValue());
		System.out.println("FP = " + fp.getValue());
		System.out.println("FN = " + fn.getValue());
		System.out.println("BRE = " + bre);
		
		// Prepare the result
		ValidationMeasureDouble[] breMeasure = new ValidationMeasureDouble[1];
		breMeasure[0] = new ValidationMeasureDouble(false);
		breMeasure[0].setId(115);
		breMeasure[0].setName("Balanced relative error on partition level");
		breMeasure[0].setValue(new Double(bre));
		return breMeasure;
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
		// Get true positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setSongLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tp = truePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setSongLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fp = falsePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setSongLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setSongLevel(true);
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
		breMeasure[0].setName("Balanced relative error on song level");
		breMeasure[0].setValue(new Double(bre));
		return breMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		// Get true positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setPartitionLevel(true);
		truePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tp = truePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setPartitionLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] fp = falsePositivesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setPartitionLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setPartitionLevel(true);
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
		breMeasure[0].setName("Balanced relative error on partition level");
		breMeasure[0].setValue(new Double(bre));
		return breMeasure;
	}
}

