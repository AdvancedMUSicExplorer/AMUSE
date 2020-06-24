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
package amuse.nodes.validator.interfaces;

import amuse.data.annotation.EventsDescription;
import amuse.interfaces.nodes.NodeException;

/**
 * Methods which calculate measures based on event detection results (e.g., segment boundaries or onsets) 
 * and ground truth information should implement this interface.
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public interface EventDetectionQualityMeasureCalculatorInterface extends MeasureCalculatorInterface {
	
	/** Sets the tolerance boundary (the maximum distance between predicted and annotated event for
	 * which the predicted event is treated as correct) */
	public void setToleranceBoundary(double boundary);
	
	/** Returns the tolerance boundary */
	public double getToleranceBoundary();
	
	/**
	 * Calculates the measure
	 * @param groundTruthEvents Annotated events
	 * @param predictedEvents Predicted events
	 * @return Measure or null if this calculator does not support required classifiers
	 * @throws NodeException
	 */
	public ValidationMeasure[] calculateMeasure(EventsDescription groundTruthEvents, EventsDescription predictedEvents) throws NodeException;

}
