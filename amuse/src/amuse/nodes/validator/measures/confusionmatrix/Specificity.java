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

import amuse.data.annotation.ClassifiedSongPartitions;
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
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setSongLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tn = trueNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setSongLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fp = falsePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double specificity = tn.getValue() / (fp.getValue() + tn.getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] specificityMeasure = new ValidationMeasureDouble[1];
		specificityMeasure[0] = new ValidationMeasureDouble(false);
		specificityMeasure[0].setId(107);
		specificityMeasure[0].setName("Specificity on song level");
		specificityMeasure[0].setValue(new Double(specificity));
		return specificityMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setPartitionLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble tn = trueNegativesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setPartitionLevel(true);
		falsePositivesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble fp = falsePositivesCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double specificity = tn.getValue() / (fp.getValue() + tn.getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] specificityMeasure = new ValidationMeasureDouble[1];
		specificityMeasure[0] = new ValidationMeasureDouble(false);
		specificityMeasure[0].setId(107);
		specificityMeasure[0].setName("Specificity on partition level");
		specificityMeasure[0].setValue(new Double(specificity));
		return specificityMeasure;
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
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setSongLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setSongLevel(true);
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
		specificityMeasure[0].setName("Specificity on song level");
		specificityMeasure[0].setValue(new Double(specificity));
		return specificityMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		// Get true negatives
		TrueNegatives trueNegativesCalculator = new TrueNegatives();
		trueNegativesCalculator.setPartitionLevel(true);
		trueNegativesCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble[] tn = trueNegativesCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships);
		
		// Get false positives
		FalsePositives falsePositivesCalculator = new FalsePositives();
		falsePositivesCalculator.setPartitionLevel(true);
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
		specificityMeasure[0].setName("Specificity on partition level");
		specificityMeasure[0].setValue(new Double(specificity));
		return specificityMeasure;
	}
}

