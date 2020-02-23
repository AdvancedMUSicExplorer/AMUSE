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
import amuse.nodes.validator.measures.events.base.FalseNegatives;
import amuse.nodes.validator.measures.events.base.TruePositives;

/**
 * Recall measure (share of true positives in relation to all annotated events)
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class Recall extends EventDetectionQualityDoubleMeasureCalculator {

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
		
		// Get true positives
		TruePositives truePositivesCalculator = new TruePositives();
		truePositivesCalculator.setToleranceBoundary(this.getToleranceBoundary());
		ValidationMeasureDouble tp = (ValidationMeasureDouble)truePositivesCalculator.calculateMeasure(groundTruthEvents, predictedEvents)[0];

		// Get false negatives
		FalseNegatives falseNegativesCalculator = new FalseNegatives();
		falseNegativesCalculator.setToleranceBoundary(this.getToleranceBoundary());
		ValidationMeasureDouble fn = (ValidationMeasureDouble)falseNegativesCalculator.calculateMeasure(groundTruthEvents, predictedEvents)[0];

		double recall = tp.getValue() / (tp.getValue() + fn.getValue());

		// Prepare the result
		ValidationMeasureDouble[] recallMeasure = new ValidationMeasureDouble[1];
		recallMeasure[0] = new ValidationMeasureDouble(false);
		recallMeasure[0].setId(406);
		recallMeasure[0].setName("Recall");
		recallMeasure[0].setValue(new Double(recall));
		return recallMeasure;
	}
}

