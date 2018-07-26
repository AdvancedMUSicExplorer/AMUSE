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
 *  Creation date: 05.03.2011
 */ 
package amuse.nodes.validator.interfaces;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;

/**
 * Methods which calculate string measure based on classification results and ground truth information should extend this class.
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public abstract class ClassificationQualityStringMeasureCalculator implements calculateMulticlassMeasureOnSongLevel {
	
	/** True if this measure will be calculated on song level*/
	private boolean calculateForSongLevel = false;
	
	/** True if this measure will be calculated on partition level */
	private boolean calculateForPartitionLevel = false;

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidationMeasureCalculatorInterface#getSongLevel()
	 */
	public boolean getSongLevel() {
		return calculateForSongLevel;
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidationMeasureCalculatorInterface#setSongLevel(boolean)
	 */
	public void setSongLevel(boolean forSongLevel) {
		this.calculateForSongLevel = forSongLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidationMeasureCalculatorInterface#getPartitionLevel()
	 */
	public boolean getPartitionLevel() {
		return calculateForPartitionLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidationMeasureCalculatorInterface#setPartitionLevel(boolean)
	 */
	public void setPartitionLevel(boolean forPartitionLevel) {
		this.calculateForPartitionLevel = forPartitionLevel;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.calculateMulticlassMeasureOnSongLevel#calculateMeasure(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureString[] calculateOneClassMeasure(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		if(groundTruthRelationships.size() != predictedRelationships.size()) {
			throw new NodeException("The number of labeled instances must be equal to the number of predicted instances!");
		}
		
		ValidationMeasureString[] measureOnSongLev = null;
		ValidationMeasureString[] measureOnPartLev = null;
		
		if(this.getSongLevel()) {
			measureOnSongLev = (ValidationMeasureString[])calculateOneClassMeasureOnSongLevel(groundTruthRelationships, predictedRelationships);
		} 
		if(this.getPartitionLevel()) {
			measureOnPartLev = (ValidationMeasureString[])calculateOneClassMeasureOnPartitionLevel(groundTruthRelationships, predictedRelationships);
		}
		
		// Return the corresponding number of measure values
		if(this.getSongLevel() && !this.getPartitionLevel()) {
			return measureOnSongLev;
		} else if(!this.getSongLevel() && this.getPartitionLevel()) {
			return measureOnPartLev;
		} else if(this.getSongLevel() && this.getPartitionLevel()) {
			ValidationMeasureString[] measures = new ValidationMeasureString[2];
			measures[0] = measureOnSongLev[0];
			measures[1] = measureOnPartLev[0];
			return measures;
		} else {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiClassMeasure(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureString[] calculateMultiClassMeasure(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, 
			ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		if(groundTruthRelationships.size() != predictedRelationships.size()) {
			throw new NodeException("The number of labeled instances must be equal to the number of predicted instances!");
		}
		
		ValidationMeasureString[] measureOnSongLev = null;
		ValidationMeasureString[] measureOnPartLev = null;
		
		if(this.getSongLevel()) {
			measureOnSongLev = (ValidationMeasureString[])calculateMultiClassMeasureOnSongLevel(groundTruthRelationships, predictedRelationships);
		} 
		if(this.getPartitionLevel()) {
			measureOnPartLev = (ValidationMeasureString[])calculateMultiClassMeasureOnPartitionLevel(groundTruthRelationships, predictedRelationships);
		}
		
		// Return the corresponding number of measure values
		if(this.getSongLevel() && !this.getPartitionLevel()) {
			return measureOnSongLev;
		} else if(!this.getSongLevel() && this.getPartitionLevel()) {
			return measureOnPartLev;
		} else if(this.getSongLevel() && this.getPartitionLevel()) {
			ValidationMeasureString[] measures = new ValidationMeasureString[2];
			measures[0] = measureOnSongLev[0];
			measures[1] = measureOnPartLev[0];
			return measures;
		} else {
			return null;
		}
	}


}
