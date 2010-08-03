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
 *  Creation date: 25.11.2009
 */ 
package amuse.nodes.validator.metrics.correlation;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitionsDescription;
import amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculator;
import amuse.nodes.validator.interfaces.ValidationMetricDouble;

/**
 * Correlation coefficient calculates the correlation between labeled and predicted relationships.
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class StandardCorrelationCoefficient extends ClassificationQualityMetricCalculator {

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
			ValidationMetricDouble[] metrics = new ValidationMetricDouble[2];
			metrics[0] = metricOnSongLev[0];
			metrics[1] = metricOnPartLev[0];
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
		
		// Calculate the mean values for predicted and labeled instances
		double meanPredictedValue = 0.0d;
		double meanLabeledValue = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			meanPredictedValue += currentPredictedValue;
			meanLabeledValue += groundTruthRelationships.get(i);
		}
		meanPredictedValue /= groundTruthRelationships.size();
		meanLabeledValue /= groundTruthRelationships.size();
		
		// Calculate the covariance and variance for predicted and labeled instances
		double covariance = 0.0d;
		double variancePredicted = 0.0d;
		double varianceLabeled = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			covariance += (currentPredictedValue - meanPredictedValue)*(groundTruthRelationships.get(i) - meanLabeledValue);
			variancePredicted += (currentPredictedValue - meanPredictedValue)*(currentPredictedValue - meanPredictedValue);
			varianceLabeled += (groundTruthRelationships.get(i) - meanLabeledValue)*(groundTruthRelationships.get(i) - meanLabeledValue);
		}	
		covariance /= (groundTruthRelationships.size() - 1);
		variancePredicted /= (groundTruthRelationships.size() - 1);
		varianceLabeled /= (groundTruthRelationships.size() - 1);
		
		// Calculate the correlation coefficient
		double corrCoef = covariance / Math.sqrt(variancePredicted * varianceLabeled);
		
		// Prepare the result
		ValidationMetricDouble[] correlationMetric = new ValidationMetricDouble[1];
		correlationMetric[0] = new ValidationMetricDouble();
		correlationMetric[0].setId(300);
		correlationMetric[0].setName("Standard correlation coefficient on song level");
		correlationMetric[0].setValue(corrCoef);
		return correlationMetric;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMetricCalculatorInterface#calculateFuzzyMetricOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMetricDouble[] calculateFuzzyMetricOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitionsDescription> predictedRelationships) throws NodeException {
		
		// Calculate the number of all partitions
		int overallPartitionNumber = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			overallPartitionNumber += predictedRelationships.get(i).getRelationships().length;
		}
		
		// Calculate the mean values for predicted and labeled instances
		double meanPredictedValue = 0.0d;
		double meanLabeledValue = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				meanPredictedValue += predictedRelationships.get(i).getRelationships()[j];
				meanLabeledValue += groundTruthRelationships.get(i);
			}
		}
		meanPredictedValue /= overallPartitionNumber;
		meanLabeledValue /= overallPartitionNumber;
		
		// Calculate the covariance and variance for predicted and labeled instances
		double covariance = 0.0d;
		double variancePredicted = 0.0d;
		double varianceLabeled = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				covariance += (predictedRelationships.get(i).getRelationships()[j] - meanPredictedValue)*(groundTruthRelationships.get(i) - meanLabeledValue);
				variancePredicted += (predictedRelationships.get(i).getRelationships()[j] - meanPredictedValue)*(predictedRelationships.get(i).getRelationships()[j] - meanPredictedValue);
				varianceLabeled += (groundTruthRelationships.get(i) - meanLabeledValue)*(groundTruthRelationships.get(i) - meanLabeledValue);
			}
		}	
		covariance /= (overallPartitionNumber - 1);
		variancePredicted /= (overallPartitionNumber - 1);
		varianceLabeled /= (overallPartitionNumber - 1);
		
		// Calculate the correlation coefficient
		double corrCoef = covariance / Math.sqrt(variancePredicted * varianceLabeled);
		
		// Prepare the result
		ValidationMetricDouble[] correlationMetric = new ValidationMetricDouble[1];
		correlationMetric[0] = new ValidationMetricDouble();
		correlationMetric[0].setId(300);
		correlationMetric[0].setName("Standard correlation coefficient on partition level");
		correlationMetric[0].setValue(corrCoef);
		return correlationMetric;
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

