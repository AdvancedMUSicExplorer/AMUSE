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
package amuse.nodes.validator.metrics.confusionmatrix;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMetricCalculator;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;

/**
 * The classification algorithm distinguishes poorly between positive and negative examples if Discriminant Power (DP) < 1, 
 * limited if DP < 2, fair if DP < 3 and good in other cases
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class DiscriminantPower extends ClassificationQualityDoubleMetricCalculator {

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
		
		double firstLog = Math.log10(recall[0].getValue()/(1.0d-recall[0].getValue()));
		double secondLog = Math.log10(specificity[0].getValue()/(1.0d-specificity[0].getValue()));
		double dp = Math.sqrt(3.0d) / Math.PI;
		dp *= (firstLog + secondLog);
		
		// Prepare the result
		ValidationMetricDouble[] discriminantPowerMetric = new ValidationMetricDouble[1];
		discriminantPowerMetric[0] = new ValidationMetricDouble(false);
		discriminantPowerMetric[0].setId(112);
		discriminantPowerMetric[0].setName("Discriminant power on song level");
		discriminantPowerMetric[0].setValue(new Double(dp));
		return discriminantPowerMetric;
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
		
		double firstLog = Math.log10(recall[0].getValue()/(1.0d-recall[0].getValue()));
		double secondLog = Math.log10(specificity[0].getValue()/(1.0d-specificity[0].getValue()));
		double dp = Math.sqrt(3.0d) / Math.PI;
		dp *= (firstLog + secondLog);
		
		// Prepare the result
		ValidationMetricDouble[] discriminantPowerMetric = new ValidationMetricDouble[1];
		discriminantPowerMetric[0] = new ValidationMetricDouble(false);
		discriminantPowerMetric[0].setId(112);
		discriminantPowerMetric[0].setName("Discriminant power on partition level");
		discriminantPowerMetric[0].setValue(new Double(dp));
		return discriminantPowerMetric;
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

