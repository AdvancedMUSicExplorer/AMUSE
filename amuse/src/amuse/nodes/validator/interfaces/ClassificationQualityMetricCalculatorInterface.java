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
 *  Creation date: 16.01.2008
 */ 
package amuse.nodes.validator.interfaces;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;

/**
 * Methods which calculate metrics based on classification results and ground truth information
 * should implement this interface.
 *  
 * @author Igor Vatolkin
 * @version $Id: ClassificationQualityMetricCalculatorInterface.java 1084 2010-07-01 14:08:28Z vatolkin $
 */
public interface ClassificationQualityMetricCalculatorInterface extends MetricCalculatorInterface {
	
	/** Switchs the metric calculation on song level on/off */
	public void setSongLevel(boolean level);
	
	/** Switchs the metric calculation on partition level on/off */
	public void setPartitionLevel(boolean level);
	
	/** Returns true if this metric will be calculated on song level */
	public boolean getSongLevel();
	
	/** Returns true if this metric will be calculated on partition level */
	public boolean getPartitionLevel();
	
	/**
	 * Calculates the metric
	 * @param groundTruthRelationships Ground truth relationships of classifier input (labeled relationships)
	 * @param classifierRelationships Relationships calculated by classifier (predicted relationships)
	 * @return Metric or null if this calculator does not support required classifiers
	 * @throws NodeException
	 */
	public ValidationMetric[] calculateOneClassMetric(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException;
	
	/**
	 * Calculates the metric, if this calculator supports fuzzy classifiers
	 * @param groundTruthRelationships Fuzzy ground truth relationships of classifier input (labeled relationships)
	 * @param classifierRelationships Fuzzy relationships calculated by classifier (predicted relationships)
	 * @return Metric or null if this calculator does not support fuzzy classifiers
	 * @throws NodeException
	 */
	public ValidationMetric[] calculateOneClassMetricOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException;
	
	public ValidationMetric[] calculateOneClassMetricOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException;
	
	

	public ValidationMetric[] calculateMultiClassMetric(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException;
	
	/**
	 * Calculates the metric, if this calculator supports multiclass classifiers
	 * @param groundTruthRelationships Multiclass ground truth relationships of classifier input (labeled relationships)
	 * @param classifierRelationships Multiclass relationships calculated by classifier (predicted relationships)
	 * @return Metric or null if this calculator does not support multiclass classifiers
	 * @throws NodeException
	 */
	// TODO v0.x Multiclass fuzzy relationships are currently not supported!
	public ValidationMetric[] calculateMultiClassMetricOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException;
	
	// TODO v0.x Multiclass fuzzy relationships are currently not supported!
	public ValidationMetric[] calculateMultiClassMetricOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException;
}
