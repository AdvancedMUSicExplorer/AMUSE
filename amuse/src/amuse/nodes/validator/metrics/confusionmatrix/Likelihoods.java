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
package amuse.nodes.validator.metrics.confusionmatrix;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMetricCalculator;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;

/**
 * Higher positive and lower negative likelihood mean better performance on positive and negative classes.
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class Likelihoods extends ClassificationQualityDoubleMetricCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateOneClassMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateOneClassMetricOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		Specificity specificityCalculator = new Specificity();
		Recall recallCalculator = new Recall();
		
		ValidationMetricDouble[] specificity = specificityCalculator.calculateOneClassMetricOnSongLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMetricDouble[] recall = recallCalculator.calculateOneClassMetricOnSongLevel(
				groundTruthRelationships, predictedRelationships);
		
		double likelihoodPositive = recall[0].getValue() / (1 - specificity[0].getValue());
		double likelihoodNegative = (1 - recall[0].getValue()) / specificity[0].getValue();
		
		// Prepare the result
		ValidationMetricDouble[] likelihoodsMetric = new ValidationMetricDouble[2];
		likelihoodsMetric[0] = new ValidationMetricDouble();
		likelihoodsMetric[0].setId(111);
		likelihoodsMetric[0].setName("Positive likelihood on song level");
		likelihoodsMetric[0].setValue(new Double(likelihoodPositive));
		likelihoodsMetric[1] = new ValidationMetricDouble();
		likelihoodsMetric[1].setId(111);
		likelihoodsMetric[1].setName("Negative likelihood on song level");
		likelihoodsMetric[1].setValue(new Double(likelihoodNegative));
		return likelihoodsMetric;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateOneClassMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateOneClassMetricOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		Specificity specificityCalculator = new Specificity();
		Recall recallCalculator = new Recall();
		
		ValidationMetricDouble[] specificity = specificityCalculator.calculateOneClassMetricOnPartitionLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMetricDouble[] recall = recallCalculator.calculateOneClassMetricOnPartitionLevel(
				groundTruthRelationships, predictedRelationships);
		
		double likelihoodPositive = recall[0].getValue() / (1 - specificity[0].getValue());
		double likelihoodNegative = (1 - recall[0].getValue()) / specificity[0].getValue();
		
		// Prepare the result
		ValidationMetricDouble[] likelihoodsMetric = new ValidationMetricDouble[2];
		likelihoodsMetric[0] = new ValidationMetricDouble();
		likelihoodsMetric[0].setId(111);
		likelihoodsMetric[0].setName("Positive likelihood on partition level");
		likelihoodsMetric[0].setValue(new Double(likelihoodPositive));
		likelihoodsMetric[1] = new ValidationMetricDouble();
		likelihoodsMetric[1].setId(111);
		likelihoodsMetric[1].setName("Negative likelihood on partition level");
		likelihoodsMetric[1].setValue(new Double(likelihoodNegative));
		return likelihoodsMetric;
	}

	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMulticlassMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMultiClassMetricOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMulticlassMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMultiClassMetricOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}


}

