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
package amuse.nodes.validator.metrics.confusionmatrix;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMetricCalculator;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;

/**
 * F-Measure metric 
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class FMeasure extends ClassificationQualityDoubleMetricCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// TODO Currently only F1 is calculated!
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateOneClassMetricOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateOneClassMetricOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Get precision
		Precision precisionCalculator = new Precision();
		precisionCalculator.setSongLevel(true);
		ValidationMetricDouble p = precisionCalculator.calculateOneClassMetric(groundTruthRelationships, predictedRelationships)[0];
		
		// Get recall
		Recall recallCalculator = new Recall();
		recallCalculator.setSongLevel(true);
		ValidationMetricDouble r = recallCalculator.calculateOneClassMetric(groundTruthRelationships, predictedRelationships)[0];
		
		double fMeasure = 2 * p.getValue() * r.getValue() / (p.getValue() + r.getValue());
		
		// Prepare the result
		ValidationMetricDouble[] fMeasureMetric = new ValidationMetricDouble[1];
		fMeasureMetric[0] = new ValidationMetricDouble(false);
		fMeasureMetric[0].setId(108);
		fMeasureMetric[0].setName("F-measure on song level");
		fMeasureMetric[0].setValue(new Double(fMeasure));
		return fMeasureMetric;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateOneClassMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateOneClassMetricOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Get precision
		Precision precisionCalculator = new Precision();
		precisionCalculator.setPartitionLevel(true);
		ValidationMetricDouble p = precisionCalculator.calculateOneClassMetric(groundTruthRelationships, predictedRelationships)[0];
		
		// Get recall
		Recall recallCalculator = new Recall();
		recallCalculator.setPartitionLevel(true);
		ValidationMetricDouble r = recallCalculator.calculateOneClassMetric(groundTruthRelationships, predictedRelationships)[0];
		
		double fMeasure = 2 * p.getValue() * r.getValue() / (p.getValue() + r.getValue());
		
		// Prepare the result
		ValidationMetricDouble[] fMeasureMetric = new ValidationMetricDouble[1];
		fMeasureMetric[0] = new ValidationMetricDouble(false);
		fMeasureMetric[0].setId(108);
		fMeasureMetric[0].setName("F-measure on partition level");
		fMeasureMetric[0].setValue(new Double(fMeasure));
		return fMeasureMetric;
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

