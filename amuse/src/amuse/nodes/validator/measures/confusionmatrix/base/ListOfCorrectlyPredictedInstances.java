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
 *  Creation date: 29.01.2009
 */ 
package amuse.nodes.validator.measures.confusionmatrix.base;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityStringMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;
import amuse.nodes.validator.interfaces.ValidationMeasureString;

/**
 * Returns the list with correctly predicted instances
 *  
 * @author Igor Vatolkin
 * @version $Id: ListOfCorrectlyPredictedInstances.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class ListOfCorrectlyPredictedInstances extends ClassificationQualityStringMeasureCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureString[] calculateOneClassMeasureOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		if(isContinuous()) {
			throw new NodeException(this.getClass().getName() + " can be calculated only for crisp classification tasks");
		}
		ArrayList<Integer> listOfCorrectlyPredictedSongs = new ArrayList<Integer>();
		
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j][0];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			if(currentPredictedValue >= 0.5) {
				currentPredictedValue = 1.0d;
			} else {
				currentPredictedValue = 0.0d;
			}
			
			// Round the ground truth value to a binary value
			Double currentGroundTruthValue = groundTruthRelationships.get(i);
			if(currentGroundTruthValue >= 0.5) {
				currentGroundTruthValue = 1.0d;
			} else {
				currentGroundTruthValue = 0.0d;
			}
			
			if(currentGroundTruthValue.doubleValue() == 1.0 && currentPredictedValue.doubleValue() == 1.0 ||
					currentGroundTruthValue.doubleValue() == 0.0 && currentPredictedValue.doubleValue() == 0.0) {
				listOfCorrectlyPredictedSongs.add(i);
			}
			
		}
		
		ValidationMeasureString[] list = new ValidationMeasureString[1];
		list[0] = new ValidationMeasureString();
		StringBuffer buff = new StringBuffer();
		buff.append("\"");
		for(int i=0;i<listOfCorrectlyPredictedSongs.size();i++) {
			buff.append(listOfCorrectlyPredictedSongs.get(i) + " ");
		}
		buff.append("\"");
		list[0].setValue(buff.toString());
		list[0].setId(114);
		list[0].setName("List of correctly predicted instances on song level");
		
		return list;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureString[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		if(isContinuous()) {
			throw new NodeException(this.getClass().getName() + " can be calculated only for crisp classification tasks");
		}
		ArrayList<Integer> listOfCorrectlyPredictedPartitions = new ArrayList<Integer>();
		int currentPartitionNumber = 0;
		
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				
				if(groundTruthRelationships.get(i).doubleValue() == 1.0 && predictedRelationships.get(i).getRelationships()[j][0].doubleValue() == 1.0 ||
						groundTruthRelationships.get(i).doubleValue() == 0.0 && predictedRelationships.get(i).getRelationships()[j][0].doubleValue() == 0.0) {
					listOfCorrectlyPredictedPartitions.add(currentPartitionNumber);
				}
				currentPartitionNumber++;
			}
		}
			
		ValidationMeasureString[] list = new ValidationMeasureString[1];
		list[0] = new ValidationMeasureString();
		StringBuffer buff = new StringBuffer();
		buff.append("\"");
		for(int i=0;i<listOfCorrectlyPredictedPartitions.size();i++) {
			buff.append(listOfCorrectlyPredictedPartitions.get(i) + " ");
		}
		buff.append("\"");
		list[0].setValue(buff.toString());
		list[0].setId(114);
		list[0].setName("List of correctly predicted instances on partition level");
		
		return list;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureString[] calculateMultiClassMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
//		throw new NodeException(this.getClass().getName() + " is not yet implemented for multiclass classification in the current version");
		ArrayList<Integer> listOfCorrectlyPredictedSongs = new ArrayList<Integer>();
		
		for(int i=0;i<groundTruthRelationships.size();i++) {
			boolean currentSongPredictedCorrectly = true;
			double[] currentPredictedRelationships = new double[predictedRelationships.get(i).getLabels().length];
			double[] currentGroundTruthRelationships = new double[groundTruthRelationships.get(i).getLabels().length];
			for(int j=0;j<groundTruthRelationships.get(i).getRelationships().length;j++) {
				for(int category=0;category<groundTruthRelationships.get(i).getLabels().length;category++) {
					currentPredictedRelationships[category] += predictedRelationships.get(i).getRelationships()[j][category];
					currentGroundTruthRelationships[category] += groundTruthRelationships.get(i).getRelationships()[j][category];
				}
			}
			for(int category=0;category<groundTruthRelationships.get(i).getLabels().length; category++) {
				currentPredictedRelationships[category] = (currentPredictedRelationships[category] / predictedRelationships.get(i).getRelationships().length) >= 0.5 ? 1.0 : 0.0;
				currentGroundTruthRelationships[category] = (currentGroundTruthRelationships[category] / groundTruthRelationships.get(i).getRelationships().length) >= 0.5 ? 1.0 : 0.0;
				if(currentPredictedRelationships[category] != currentGroundTruthRelationships[category]) {
					currentSongPredictedCorrectly = false; break;
				}
			}
			if(currentSongPredictedCorrectly) {
				listOfCorrectlyPredictedSongs.add(i);
			}
		}
			
		ValidationMeasureString[] list = new ValidationMeasureString[1];
		list[0] = new ValidationMeasureString();
		StringBuffer buff = new StringBuffer();
		buff.append("\"");
		for(int i=0;i<listOfCorrectlyPredictedSongs.size();i++) {
			buff.append(listOfCorrectlyPredictedSongs.get(i) + " ");
		}
		buff.append("\"");
		list[0].setValue(buff.toString());
		list[0].setId(114);
		list[0].setName("List of correctly predicted instances on song level");
		
		return list;
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureString[] calculateMultiClassMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		ArrayList<Integer> listOfCorrectlyPredictedPartitions = new ArrayList<Integer>();
		int currentPartitionNumber = 0;
		
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<groundTruthRelationships.get(i).getRelationships().length;j++) {
				boolean currentPartitionPredictedCorrectly = true;
				for(int category=0;category<groundTruthRelationships.get(i).getLabels().length;category++) {
					if(groundTruthRelationships.get(i).getRelationships()[j][category] != predictedRelationships.get(i).getRelationships()[j][category]) {
						currentPartitionPredictedCorrectly = false; break;
					}
				}
				if(currentPartitionPredictedCorrectly) {
					listOfCorrectlyPredictedPartitions.add(currentPartitionNumber);
				}
				currentPartitionNumber++;
			}
		}
			
		ValidationMeasureString[] list = new ValidationMeasureString[1];
		list[0] = new ValidationMeasureString();
		StringBuffer buff = new StringBuffer();
		buff.append("\"");
		for(int i=0;i<listOfCorrectlyPredictedPartitions.size();i++) {
			buff.append(listOfCorrectlyPredictedPartitions.get(i) + " ");
		}
		buff.append("\"");
		list[0].setValue(buff.toString());
		list[0].setId(114);
		list[0].setName("List of correctly predicted instances on partition level");
		
		return list;
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " cannot be calculated for multilabel classification tasks");
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " cannot be calculated for multilabel classification tasks");
	}
}

