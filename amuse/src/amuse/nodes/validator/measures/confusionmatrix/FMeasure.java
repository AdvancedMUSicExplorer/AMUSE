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
package amuse.nodes.validator.measures.confusionmatrix;

import java.util.ArrayList;

import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * F-Measure measure 
 *  
 * @author Igor Vatolkin
 * @version $Id: FMeasure.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class FMeasure extends ClassificationQualityDoubleMeasureCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// TODO Currently only F1 is calculated!
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnTrackLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		
		// Get precision
		Precision precisionCalculator = new Precision();
		precisionCalculator.setTrackLevel(true);
		precisionCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble p = precisionCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get recall
		Recall recallCalculator = new Recall();
		recallCalculator.setTrackLevel(true);
		recallCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble r = recallCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double fMeasure = 2 * p.getValue() * r.getValue() / (p.getValue() + r.getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] fMeasureMeasure = new ValidationMeasureDouble[1];
		fMeasureMeasure[0] = new ValidationMeasureDouble(false);
		fMeasureMeasure[0].setId(108);
		fMeasureMeasure[0].setName("F-measure on track level");
		fMeasureMeasure[0].setValue(new Double(fMeasure));
		return fMeasureMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		
		// Get precision
		Precision precisionCalculator = new Precision();
		precisionCalculator.setWindowLevel(true);
		precisionCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble p = precisionCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get recall
		Recall recallCalculator = new Recall();
		recallCalculator.setWindowLevel(true);
		recallCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble r = recallCalculator.calculateOneClassMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double fMeasure = 2 * p.getValue() * r.getValue() / (p.getValue() + r.getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] fMeasureMeasure = new ValidationMeasureDouble[1];
		fMeasureMeasure[0] = new ValidationMeasureDouble(false);
		fMeasureMeasure[0].setId(108);
		fMeasureMeasure[0].setName("F-measure on classification window level");
		fMeasureMeasure[0].setValue(new Double(fMeasure));
		return fMeasureMeasure;
	}

	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		return calculateMultiLabelMeasureOnTrackLevel(groundTruthRelationships, predictedRelationships);
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		return calculateMultiLabelMeasureOnWindowLevel(groundTruthRelationships, predictedRelationships);
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		
		// Get precision
		Precision precisionCalculator = new Precision();
		precisionCalculator.setTrackLevel(true);
		precisionCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble p = precisionCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get recall
		Recall recallCalculator = new Recall();
		recallCalculator.setTrackLevel(true);
		recallCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble r = recallCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double fMeasure = 2 * p.getValue() * r.getValue() / (p.getValue() + r.getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] fMeasureMeasure = new ValidationMeasureDouble[1];
		fMeasureMeasure[0] = new ValidationMeasureDouble(false);
		fMeasureMeasure[0].setId(108);
		fMeasureMeasure[0].setName("F-measure on track level");
		fMeasureMeasure[0].setValue(new Double(fMeasure));
		return fMeasureMeasure;
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		// Get precision
		Precision precisionCalculator = new Precision();
		precisionCalculator.setWindowLevel(true);
		precisionCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble p = precisionCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		// Get recall
		Recall recallCalculator = new Recall();
		recallCalculator.setWindowLevel(true);
		recallCalculator.setContinuous(isContinuous());
		ValidationMeasureDouble r = recallCalculator.calculateMultiLabelMeasure(groundTruthRelationships, predictedRelationships)[0];
		
		double fMeasure = 2 * p.getValue() * r.getValue() / (p.getValue() + r.getValue());
		
		// Prepare the result
		ValidationMeasureDouble[] fMeasureMeasure = new ValidationMeasureDouble[1];
		fMeasureMeasure[0] = new ValidationMeasureDouble(false);
		fMeasureMeasure[0].setId(108);
		fMeasureMeasure[0].setName("F-measure on classification window level");
		fMeasureMeasure[0].setValue(new Double(fMeasure));
		return fMeasureMeasure;
	}
}

