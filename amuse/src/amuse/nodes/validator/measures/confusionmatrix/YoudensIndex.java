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
 * @version $Id$
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
		Recall recallCalculator = new Recall();
		
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
		Recall recallCalculator = new Recall();
		
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
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}



}

