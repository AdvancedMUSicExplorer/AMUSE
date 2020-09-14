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
 * Number of false positives (predicted events which are not among annotated events with regard to the tolerance boundary)
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class FalsePositives extends EventDetectionQualityDoubleMeasureCalculator {

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
		double numberOfFalsePositives = 0;
		for(int i=0;i<predictedEvents.getEventMs().length;i++) {
			boolean matchingAnnotatedEventFound = false;
			for(int j=0;j<groundTruthEvents.getEventMs().length;j++) {
				if(Math.abs(predictedEvents.getEventMs()[i] - groundTruthEvents.getEventMs()[j]) <= this.getToleranceBoundary()) {
					matchingAnnotatedEventFound = true; break;
				}
			}
			if(!matchingAnnotatedEventFound) {
				numberOfFalsePositives++;
			}
		}
		
		// Prepare the result
		ValidationMeasureDouble[] truePositivesMeasure = new ValidationMeasureDouble[1];
		truePositivesMeasure[0] = new ValidationMeasureDouble();
		truePositivesMeasure[0].setId(402);
		truePositivesMeasure[0].setName("Number of false positives");
		truePositivesMeasure[0].setValue(new Double(numberOfFalsePositives));
		return truePositivesMeasure;
	}
}

