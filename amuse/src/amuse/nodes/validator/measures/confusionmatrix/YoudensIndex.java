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

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Youden's index measures the algorithm's ability to correctly label both positive and negative data samples 
 * (i.e. generally avoid failures).
 *  
 * @author Igor Vatolkin
 * @version $Id: YoudensIndex.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class YoudensIndex extends ClassificationQualityDoubleMeasureCalculator {

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
		
		Specificity specificityCalculator = new Specificity();
		specificityCalculator.setContinuous(isContinuous());
		Recall recallCalculator = new Recall();
		recallCalculator.setContinuous(isContinuous());
		
		ValidationMeasureDouble[] specificity = specificityCalculator.calculateOneClassMeasureOnSongLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMeasureDouble[] recall = recallCalculator.calculateOneClassMeasureOnSongLevel(
				groundTruthRelationships, predictedRelationships);
		
		double index = specificity[0].getValue() + recall[0].getValue() - 1;
		
		// Prepare the result
		ValidationMeasureDouble[] youdenxIndexMeasure = new ValidationMeasureDouble[1];
		youdenxIndexMeasure[0] = new ValidationMeasureDouble(false);
		youdenxIndexMeasure[0].setId(110);
		youdenxIndexMeasure[0].setName("Youden's index on song level");
		youdenxIndexMeasure[0].setValue(new Double(index));
		return youdenxIndexMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		Specificity specificityCalculator = new Specificity();
		specificityCalculator.setContinuous(isContinuous());
		Recall recallCalculator = new Recall();
		recallCalculator.setContinuous(isContinuous());
		
		ValidationMeasureDouble[] specificity = specificityCalculator.calculateOneClassMeasureOnPartitionLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMeasureDouble[] recall = recallCalculator.calculateOneClassMeasureOnPartitionLevel(
				groundTruthRelationships, predictedRelationships);
		
		double index = specificity[0].getValue() + recall[0].getValue() - 1;
		
		// Prepare the result
		ValidationMeasureDouble[] youdenxIndexMeasure = new ValidationMeasureDouble[1];
		youdenxIndexMeasure[0] = new ValidationMeasureDouble(false);
		youdenxIndexMeasure[0].setId(110);
		youdenxIndexMeasure[0].setName("Youden's index on partition level");
		youdenxIndexMeasure[0].setValue(new Double(index));
		return youdenxIndexMeasure;
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
		Specificity specificityCalculator = new Specificity();
		specificityCalculator.setContinuous(isContinuous());
		Recall recallCalculator = new Recall();
		recallCalculator.setContinuous(isContinuous());
		
		ValidationMeasureDouble[] specificity = specificityCalculator.calculateMultiLabelMeasureOnSongLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMeasureDouble[] recall = recallCalculator.calculateMultiLabelMeasureOnSongLevel(
				groundTruthRelationships, predictedRelationships);
		
		double index = specificity[0].getValue() + recall[0].getValue() - 1;
		
		// Prepare the result
		ValidationMeasureDouble[] youdenxIndexMeasure = new ValidationMeasureDouble[1];
		youdenxIndexMeasure[0] = new ValidationMeasureDouble(false);
		youdenxIndexMeasure[0].setId(110);
		youdenxIndexMeasure[0].setName("Youden's index on song level");
		youdenxIndexMeasure[0].setValue(new Double(index));
		return youdenxIndexMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		Specificity specificityCalculator = new Specificity();
		specificityCalculator.setContinuous(isContinuous());
		Recall recallCalculator = new Recall();
		recallCalculator.setContinuous(isContinuous());
		
		ValidationMeasureDouble[] specificity = specificityCalculator.calculateMultiLabelMeasureOnPartitionLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMeasureDouble[] recall = recallCalculator.calculateMultiLabelMeasureOnPartitionLevel(
				groundTruthRelationships, predictedRelationships);
		
		double index = specificity[0].getValue() + recall[0].getValue() - 1;
		
		// Prepare the result
		ValidationMeasureDouble[] youdenxIndexMeasure = new ValidationMeasureDouble[1];
		youdenxIndexMeasure[0] = new ValidationMeasureDouble(false);
		youdenxIndexMeasure[0].setId(110);
		youdenxIndexMeasure[0].setName("Youden's index on partition level");
		youdenxIndexMeasure[0].setValue(new Double(index));
		return youdenxIndexMeasure;
	}

}

