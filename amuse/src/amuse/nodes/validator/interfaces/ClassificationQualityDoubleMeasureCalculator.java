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

import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.interfaces.nodes.NodeException;

/**
 * Methods which calculate double measures based on classification results and ground truth information should extend this class.
 *  
 * @author Igor Vatolkin
 * @version $Id: ClassificationQualityDoubleMeasureCalculator.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public abstract class ClassificationQualityDoubleMeasureCalculator implements ClassificationQualityMeasureCalculatorInterface {
	
	/** True if this measure will be calculated on track level*/
	private boolean calculateForTrackLevel = false;
	
	/** True if this measure will be calculated on classification window level */
	private boolean calculateForWindowLevel = false;
	
	/** True if this measure will be calculated in a fuzzy way */
	private boolean continuous = false;

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidationMeasureCalculatorInterface#getTrackLevel()
	 */
	public boolean getTrackLevel() {
		return calculateForTrackLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#isFuzzy()
	 */
	public boolean isContinuous() {
		return continuous;
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidationMeasureCalculatorInterface#setTrackLevel(boolean)
	 */
	public void setTrackLevel(boolean forTrackLevel) {
		this.calculateForTrackLevel = forTrackLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidationMeasureCalculatorInterface#getWindowLevel()
	 */
	public boolean getWindowLevel() {
		return calculateForWindowLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ValidationMeasureCalculatorInterface#setWindowLevel(boolean)
	 */
	public void setWindowLevel(boolean forWindowLevel) {
		this.calculateForWindowLevel = forWindowLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#setFuzzy(boolean)
	 */
	public void setContinuous(boolean continuous) {
		this.continuous = continuous;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMeasure(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasure(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		if(groundTruthRelationships.size() != predictedRelationships.size()) {
			throw new NodeException("The number of labeled instances must be equal to the number of predicted instances!");
		}
		
		ValidationMeasureDouble[] measureOnTrackLev = null;
		ValidationMeasureDouble[] measureOnPartLev = null;
		
		if(this.getTrackLevel()) {
			measureOnTrackLev = (ValidationMeasureDouble[])calculateOneClassMeasureOnTrackLevel(groundTruthRelationships, predictedRelationships);
		} 
		if(this.getWindowLevel()) {
			measureOnPartLev = (ValidationMeasureDouble[])calculateOneClassMeasureOnClassficationWindowLevel(groundTruthRelationships, predictedRelationships);
		}
		
		// Return the corresponding number of measure values
		if(this.getTrackLevel() && !this.getWindowLevel()) {
			return measureOnTrackLev;
		} else if(!this.getTrackLevel() && this.getWindowLevel()) {
			return measureOnPartLev;
		} else if(this.getTrackLevel() && this.getWindowLevel()) {
			ValidationMeasureDouble[] measures = new ValidationMeasureDouble[measureOnTrackLev.length + measureOnPartLev.length];
			for(int i = 0; i < measureOnTrackLev.length; i++) {
				measures[i] = measureOnTrackLev[i];
			}
			for(int i = 0; i < measureOnPartLev.length; i++) {
				measures[i + measureOnTrackLev.length] = measureOnPartLev[i];
			}
			return measures;
		} else {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiClassMeasure(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasure(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, 
			ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		if(groundTruthRelationships.size() != predictedRelationships.size()) {
			throw new NodeException("The number of labeled instances must be equal to the number of predicted instances!");
		}
		
		ValidationMeasureDouble[] measureOnTrackLev = null;
		ValidationMeasureDouble[] measureOnPartLev = null;
		
		if(this.getTrackLevel()) {
			measureOnTrackLev = (ValidationMeasureDouble[])calculateMultiClassMeasureOnTrackLevel(groundTruthRelationships, predictedRelationships);
		} 
		if(this.getWindowLevel()) {
			measureOnPartLev = (ValidationMeasureDouble[])calculateMultiClassMeasureOnWindowLevel(groundTruthRelationships, predictedRelationships);
		}
		
		// Return the corresponding number of measure values
		if(this.getTrackLevel() && !this.getWindowLevel()) {
			return measureOnTrackLev;
		} else if(!this.getTrackLevel() && this.getWindowLevel()) {
			return measureOnPartLev;
		} else if(this.getTrackLevel() && this.getWindowLevel()) {
			ValidationMeasureDouble[] measures = new ValidationMeasureDouble[measureOnTrackLev.length + measureOnPartLev.length];
			for(int i = 0; i < measureOnTrackLev.length; i++) {
				measures[i] = measureOnTrackLev[i];
			}
			for(int i = 0; i < measureOnPartLev.length; i++) {
				measures[i + measureOnTrackLev.length] = measureOnPartLev[i];
			}
			return measures;
		} else {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasure(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasure(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships,
			ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		ValidationMeasureDouble[] measureOnTrackLev = null;
		ValidationMeasureDouble[] measureOnPartLev = null;
		
		if(this.getTrackLevel()) {
			measureOnTrackLev = (ValidationMeasureDouble[])calculateMultiLabelMeasureOnTrackLevel(groundTruthRelationships, predictedRelationships);
		} 
		if(this.getWindowLevel()) {
			measureOnPartLev = (ValidationMeasureDouble[])calculateMultiLabelMeasureOnWindowLevel(groundTruthRelationships, predictedRelationships);
		}
		
		// Return the corresponding number of measure values
		if(this.getTrackLevel() && !this.getWindowLevel()) {
			return measureOnTrackLev;
		} else if(!this.getTrackLevel() && this.getWindowLevel()) {
			return measureOnPartLev;
		} else if(this.getTrackLevel() && this.getWindowLevel()) {
			ValidationMeasureDouble[] measures = new ValidationMeasureDouble[measureOnTrackLev.length + measureOnPartLev.length];
			for(int i = 0; i < measureOnTrackLev.length; i++) {
				measures[i] = measureOnTrackLev[i];
			}
			for(int i = 0; i < measureOnPartLev.length; i++) {
				measures[i + measureOnTrackLev.length] = measureOnPartLev[i];
			}
			return measures;
		} else {
			return null;
		}
	}

}
