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

import amuse.data.annotation.ClassifiedSongPartitions;
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
		
		double gm = Math.sqrt(recall[0].getValue() * specificity[0].getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] geometricMeansMeasure = new ValidationMeasureDouble[1];
		geometricMeansMeasure[0] = new ValidationMeasureDouble(false);
		geometricMeansMeasure[0].setId(113);
		geometricMeansMeasure[0].setName("Geometric means on song level");
		geometricMeansMeasure[0].setValue(new Double(gm));
		return geometricMeansMeasure;
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
		
		double gm = Math.sqrt(recall[0].getValue() * specificity[0].getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] geometricMeansMeasure = new ValidationMeasureDouble[1];
		geometricMeansMeasure[0] = new ValidationMeasureDouble(false);
		geometricMeansMeasure[0].setId(113);
		geometricMeansMeasure[0].setName("Geometric means on partition level");
		geometricMeansMeasure[0].setValue(new Double(gm));
		return geometricMeansMeasure;
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
		
		double gm = Math.sqrt(recall[0].getValue() * specificity[0].getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] geometricMeansMeasure = new ValidationMeasureDouble[1];
		geometricMeansMeasure[0] = new ValidationMeasureDouble(false);
		geometricMeansMeasure[0].setId(113);
		geometricMeansMeasure[0].setName("Geometric means on song level");
		geometricMeansMeasure[0].setValue(new Double(gm));
		return geometricMeansMeasure;
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
		
		double gm = Math.sqrt(recall[0].getValue() * specificity[0].getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] geometricMeansMeasure = new ValidationMeasureDouble[1];
		geometricMeansMeasure[0] = new ValidationMeasureDouble(false);
		geometricMeansMeasure[0].setId(113);
		geometricMeansMeasure[0].setName("Geometric means on partition level");
		geometricMeansMeasure[0].setValue(new Double(gm));
		return geometricMeansMeasure;
	}
}

