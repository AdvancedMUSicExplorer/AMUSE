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
 * Methods which calculate measures based on classification results and ground truth information
 * should implement this interface.
 *  
 * @author Igor Vatolkin
 * @version $Id: ClassificationQualityMeasureCalculatorInterface.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public interface ClassificationQualityMeasureCalculatorInterface extends MeasureCalculatorInterface {
	
	/** Switchs the measure calculation on track level on/off */
	public void setTrackLevel(boolean level);
	
	/** Switchs the measure calculation on classificationWindow level on/off */
	public void setWindowLevel(boolean level);
	
	/** Switchs the fuzzy measure calculation on/off */
	public void setContinuous(boolean fuzzy);
	
	/** Returns true if this measure will be calculated on track level */
	public boolean getTrackLevel();
	
	/** Returns true if this measure will be calculated on classification window level */
	public boolean getWindowLevel();
	
	//** Returns true if this measure will be be calculated in a fuzzy way */
	public boolean isContinuous();
	
	/**
	 * Calculates the measure
	 * @param groundTruthRelationships Ground truth relationships of classifier input (labeled relationships)
	 * @param classifierRelationships Relationships calculated by classifier (predicted relationships)
	 * @return Measure or null if this calculator does not support required classifiers
	 * @throws NodeException
	 */
	public ValidationMeasure[] calculateOneClassMeasure(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException;
	
	/**
	 * Calculates the measure, if this calculator supports fuzzy classifiers
	 * @param groundTruthRelationships Fuzzy ground truth relationships of classifier input (labeled relationships)
	 * @param classifierRelationships Fuzzy relationships calculated by classifier (predicted relationships)
	 * @return Measure or null if this calculator does not support fuzzy classifiers
	 * @throws NodeException
	 */
	public ValidationMeasure[] calculateOneClassMeasureOnTrackLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException;
	
	public ValidationMeasure[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException;
	
	
	public ValidationMeasure[] calculateMultiClassMeasure(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException;
	
	/**
	 * Calculates the measure, if this calculator supports multiclass classifiers
	 * @param groundTruthRelationships Multiclass ground truth relationships of classifier input (labeled relationships)
	 * @param classifierRelationships Multiclass relationships calculated by classifier (predicted relationships)
	 * @return Measure or null if this calculator does not support multiclass classifiers
	 * @throws NodeException
	 */
	public ValidationMeasure[] calculateMultiClassMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException;
	
	public ValidationMeasure[] calculateMultiClassMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException;
	
	
	public ValidationMeasure[] calculateMultiLabelMeasure(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException;
	
	/**
	 * Calculates the measure, if this calculator supports multilabel classifiers
	 * @param groundTruthRelationships Multilabel ground truth relationships of classifier input (labeled relationships)
	 * @param classifierRelationships Multilabel relationships calculated by classifier (predicted relationships)
	 * @return Measure or null if this calculator does not support multilabel classifiers
	 * @throws NodeException
	 */
	public ValidationMeasure[] calculateMultiLabelMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException;
	
	public ValidationMeasure[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException;
}
