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
package amuse.nodes.validator.measures.events.base;

import amuse.data.annotation.EventsDescription;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.EventDetectionQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasure;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Number of false negatives (annotated events which are not among predicted events with regard to the tolerance boundary)
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class FalseNegatives extends EventDetectionQualityDoubleMeasureCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.EventDetectionQualityMeasureCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.EventDetectionQualityMeasureCalculatorInterface#calculateMeasure(amuse.data.annotation.EventsDescription, amuse.data.annotation.EventsDescription)
	 */
	public ValidationMeasure[] calculateMeasure(EventsDescription groundTruthEvents, EventsDescription predictedEvents) throws NodeException {
		double numberOfFalseNegatives = 0;
		for(int i=0;i<groundTruthEvents.getEventMs().length;i++) {
			boolean matchingPredictedEventFound = false;
			for(int j=0;j<predictedEvents.getEventMs().length;j++) {
				if(Math.abs(groundTruthEvents.getEventMs()[i] - predictedEvents.getEventMs()[j]) <= this.getToleranceBoundary()) {
					matchingPredictedEventFound = true; break;
				}
			}
			if(!matchingPredictedEventFound) {
				numberOfFalseNegatives++;
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] truePositivesMeasure = new ValidationMeasureDouble[1];
		truePositivesMeasure[0] = new ValidationMeasureDouble();
		truePositivesMeasure[0].setId(403);
		truePositivesMeasure[0].setName("Number of false negatives");
		truePositivesMeasure[0].setValue(new Double(numberOfFalseNegatives));
		return truePositivesMeasure;
	}
}

