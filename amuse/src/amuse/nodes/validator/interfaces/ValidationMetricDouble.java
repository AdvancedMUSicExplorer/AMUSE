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
 *  Creation date: 10.03.2008
 */ 
package amuse.nodes.validator.interfaces;

/**
 * For validation metrics which consist of one double value.
 *  
 * @author Igor Vatolkin
 * @version $Id: ValidationMetricDouble.java 1122 2010-07-02 11:33:30Z vatolkin $
 */
public class ValidationMetricDouble extends ValidationMetric {
	
	/**
	 * Gets metric value
	 * @return Metric value
	 */
	public Double getValue() {
		return (Double)value;
	}

	/**
	 * Sets metric value
	 * @param value Metric value
	 */
	public void setValue(Double value) {
		this.value = value;
	}
	
}
