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
 * Methods which calculate double measures based on classification results and ground truth information should extend this class.
 *  
 * @author Igor Vatolkin
 * @version $Id: ClassificationQualityDoubleMeasureCalculator.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public abstract class ClassificationQualityDoubleMeasureCalculator implements ClassificationQualityMeasureCalculatorInterface {
	
	/** True if this measure will be calculated on song level*/
	private boolean calculateForSongLevel = false;
	
	/** True if this measure will be calculated on partition level */
	private boolean calculateForPartitionLevel = false;
	
	/** True if this measure will be calculated fuzzily */
	private boolean fuzzy = false;

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidationMeasureCalculatorInterface#getSongLevel()
	 */
	public boolean getSongLevel() {
		return calculateForSongLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#isFuzzy()
	 */
	public boolean isFuzzy() {
		return fuzzy;
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
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#setFuzzy(boolean)
	 */
	public void setFuzzy(boolean fuzzy) {
		this.fuzzy = fuzzy;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMeasure(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasure(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		if(groundTruthRelationships.size() != predictedRelationships.size()) {
			throw new NodeException("The number of labeled instances must be equal to the number of predicted instances!");
		}
		
		ValidationMeasureDouble[] measureOnSongLev = null;
		ValidationMeasureDouble[] measureOnPartLev = null;
		
		if(this.getSongLevel()) {
			measureOnSongLev = (ValidationMeasureDouble[])calculateOneClassMeasureOnSongLevel(groundTruthRelationships, predictedRelationships);
		} 
		if(this.getPartitionLevel()) {
			measureOnPartLev = (ValidationMeasureDouble[])calculateOneClassMeasureOnPartitionLevel(groundTruthRelationships, predictedRelationships);
		}
		
		// Return the corresponding number of measure values
		if(this.getSongLevel() && !this.getPartitionLevel()) {
			return measureOnSongLev;
		} else if(!this.getSongLevel() && this.getPartitionLevel()) {
			return measureOnPartLev;
		} else if(this.getSongLevel() && this.getPartitionLevel()) {
			ValidationMeasureDouble[] measures = new ValidationMeasureDouble[2];
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
	public ValidationMeasureDouble[] calculateMultiClassMeasure(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, 
			ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		if(groundTruthRelationships.size() != predictedRelationships.size()) {
			throw new NodeException("The number of labeled instances must be equal to the number of predicted instances!");
		}
		
		ValidationMeasureDouble[] measureOnSongLev = null;
		ValidationMeasureDouble[] measureOnPartLev = null;
		
		if(this.getSongLevel()) {
			measureOnSongLev = (ValidationMeasureDouble[])calculateMultiClassMeasureOnSongLevel(groundTruthRelationships, predictedRelationships);
		} 
		if(this.getPartitionLevel()) {
			measureOnPartLev = (ValidationMeasureDouble[])calculateMultiClassMeasureOnPartitionLevel(groundTruthRelationships, predictedRelationships);
		}
		
		// Return the corresponding number of measure values
		if(this.getSongLevel() && !this.getPartitionLevel()) {
			return measureOnSongLev;
		} else if(!this.getSongLevel() && this.getPartitionLevel()) {
			return measureOnPartLev;
		} else if(this.getSongLevel() && this.getPartitionLevel()) {
			ValidationMeasureDouble[] measures = new ValidationMeasureDouble[2];
			measures[0] = measureOnSongLev[0];
			measures[1] = measureOnPartLev[0];
			return measures;
		} else {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasure(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasure(ArrayList<ClassifiedSongPartitions> groundTruthRelationships,
			ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		ValidationMeasureDouble[] measureOnSongLev = null;
		ValidationMeasureDouble[] measureOnPartLev = null;
		
		if(this.getSongLevel()) {
			measureOnSongLev = (ValidationMeasureDouble[])calculateMultiLabelMeasureOnSongLevel(groundTruthRelationships, predictedRelationships);
		} 
		if(this.getPartitionLevel()) {
			measureOnPartLev = (ValidationMeasureDouble[])calculateMultiLabelMeasureOnPartitionLevel(groundTruthRelationships, predictedRelationships);
		}
		
		// Return the corresponding number of measure values
		if(this.getSongLevel() && !this.getPartitionLevel()) {
			return measureOnSongLev;
		} else if(!this.getSongLevel() && this.getPartitionLevel()) {
			return measureOnPartLev;
		} else if(this.getSongLevel() && this.getPartitionLevel()) {
			ValidationMeasureDouble[] measures = new ValidationMeasureDouble[2];
			measures[0] = measureOnSongLev[0];
			measures[1] = measureOnPartLev[0];
			return measures;
		} else {
			return null;
		}
	}

}
