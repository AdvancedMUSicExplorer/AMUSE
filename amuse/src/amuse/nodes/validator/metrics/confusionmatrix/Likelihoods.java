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
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitionsDescription;
import amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculator;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;

/**
 * Higher positive and lower negative likelihood mean better performance on positive and negative classes.
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class Likelihoods extends ClassificationQualityMetricCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMetric(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMetric(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		if(groundTruthRelationships.size() != predictedRelationships.size()) {
			throw new NodeException("The number of labeled instances must be equal to the number of predicted instances!");
		}
		
		ValidationMetricDouble[] metricOnSongLev = null;
		ValidationMetricDouble[] metricOnPartLev = null;
		
		if(groundTruthRelationships.get(0) instanceof Double) {
			if(this.getSongLevel()) {
				metricOnSongLev = calculateFuzzyMetricOnSongLevel(groundTruthRelationships, predictedRelationships);
			} 
			if(this.getPartitionLevel()) {
				metricOnPartLev = calculateFuzzyMetricOnPartitionLevel(groundTruthRelationships, predictedRelationships);
			}
		} else {
			return null;
		}
		
		// Return the corresponding number of metric values
		if(this.getSongLevel() && !this.getPartitionLevel()) {
			return metricOnSongLev;
		} else if(!this.getSongLevel() && this.getPartitionLevel()) {
			return metricOnPartLev;
		} else if(this.getSongLevel() && this.getPartitionLevel()) {
			ValidationMetricDouble[] metrics = new ValidationMetricDouble[4];
			metrics[0] = metricOnSongLev[0];
			metrics[1] = metricOnSongLev[1];
			metrics[2] = metricOnPartLev[0];
			metrics[3] = metricOnPartLev[1];
			return metrics;
		} else {
			return null;
		}
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateBinaryMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateBinaryMetricOnSongLevel(ArrayList<Boolean> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		return null;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateBinaryMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateBinaryMetricOnPartitionLevel(ArrayList<Boolean> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		return null;
	}

	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateFuzzyMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateFuzzyMetricOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		
		Specificity specificityCalculator = new Specificity();
		Recall recallCalculator = new Recall();
		
		ValidationMetricDouble[] specificity = specificityCalculator.calculateFuzzyMetricOnSongLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMetricDouble[] recall = recallCalculator.calculateFuzzyMetricOnSongLevel(
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
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateFuzzyMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateFuzzyMetricOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		
		Specificity specificityCalculator = new Specificity();
		Recall recallCalculator = new Recall();
		
		ValidationMetricDouble[] specificity = specificityCalculator.calculateFuzzyMetricOnPartitionLevel(
				groundTruthRelationships, predictedRelationships);
		ValidationMetricDouble[] recall = recallCalculator.calculateFuzzyMetricOnPartitionLevel(
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
	public ValidationMetricDouble[] calculateMulticlassMetricOnSongLevel(ArrayList<ArrayList<Double>> groundTruthRelationships, ArrayList<ArrayList<ClassifiedSongPartitionsDescription>> predictedRelationships) throws NodeException {
		return null;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateMulticlassMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateMulticlassMetricOnPartitionLevel(ArrayList<ArrayList<Double>> groundTruthRelationships, ArrayList<ArrayList<ClassifiedSongPartitionsDescription>> predictedRelationships) throws NodeException {
		return null;
	}


}

