/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2020 by code authors
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
 *  Creation date: 23.02.2020
 */ 
package amuse.nodes.validator.measures.events;

import amuse.data.annotation.EventsDescription;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.EventDetectionQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasure;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * F-Measure (the harmonic mean of precision and recall)
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class FMeasure extends EventDetectionQualityDoubleMeasureCalculator {

	/** Beta parameter, the default value is 1.0 */
	private double beta = 1.0d;
	
	/**
	 * @see amuse.nodes.validator.interfaces.EventDetectionQualityMeasureCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		try {
			beta = new Double(parameterString);
		} catch(ClassCastException e) {
			throw new NodeException("Could not properly parse parameter string for beta value: " + parameterString + " (" + 
					e.getMessage() + ")");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.EventDetectionQualityMeasureCalculatorInterface#calculateMeasure(amuse.data.annotation.EventsDescription, amuse.data.annotation.EventsDescription)
	 */
	public ValidationMeasure[] calculateMeasure(EventsDescription groundTruthEvents, EventsDescription predictedEvents) throws NodeException {
		
		Precision precisionCalculator = new Precision();
		precisionCalculator.setToleranceBoundary(this.getToleranceBoundary());
		ValidationMeasureDouble p = (ValidationMeasureDouble)precisionCalculator.calculateMeasure(groundTruthEvents, predictedEvents)[0];

		// Get recall
		Recall recallCalculator = new Recall();
		recallCalculator.setToleranceBoundary(this.getToleranceBoundary());
		ValidationMeasureDouble r = (ValidationMeasureDouble)recallCalculator.calculateMeasure(groundTruthEvents, predictedEvents)[0];

		double fMeasure = (1d + beta*beta) * ((p.getValue() * r.getValue()) / (beta*beta*(p.getValue() + r.getValue())));

		// Prepare the result
		ValidationMeasureDouble[] fMeasureMeasure = new ValidationMeasureDouble[1];
		fMeasureMeasure[0] = new ValidationMeasureDouble(false);
		fMeasureMeasure[0].setId(408);
		fMeasureMeasure[0].setName("F-measure");
		fMeasureMeasure[0].setValue(new Double(fMeasure));
		return fMeasureMeasure;
	}
}

