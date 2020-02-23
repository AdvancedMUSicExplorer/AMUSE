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

/**
 * Methods which calculate double measures based on event detection results and ground truth information should extend this class.
 *  
 * @author Igor Vatolkin
 * @version $Id: $
 */
public abstract class EventDetectionQualityDoubleMeasureCalculator implements EventDetectionQualityMeasureCalculatorInterface {
	
	/** The maximum distance between predicted and annotated event for which the predicted event is treated as correct */
	private double toleranceBoundary = 0d;

	/**
	 * @return the toleranceBoundary
	 */
	public double getToleranceBoundary() {
		return toleranceBoundary;
	}

	/**
	 * @param toleranceBoundary the toleranceBoundary to set
	 */
	public void setToleranceBoundary(double toleranceBoundary) {
		this.toleranceBoundary = toleranceBoundary;
	}

}
