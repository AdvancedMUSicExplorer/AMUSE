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
 * For validation measures which consist of one double value.
 *  
 * @author Igor Vatolkin
 * @version $Id: ValidationMeasureDouble.java 241 2018-07-26 12:35:24Z frederik-h $
 */
public class ValidationMeasureDouble extends ValidationMeasure {
	
	/** Should this measure be minimized (for better solutions)? */
	protected boolean isForMinimizing = true;
	
	/**
	 * Standard constructor
	 */
	public ValidationMeasureDouble() {
		super();
	}
	
	/**
	 * Constructor with setting of isForMinimizing
	 */
	public ValidationMeasureDouble(boolean isForMinimizing) {
		super();
		this.isForMinimizing = isForMinimizing;
	}
	
	/**
	 * Gets measure value
	 * @return Measure value
	 */
	public Double getValue() {
		return (Double)value;
	}

	/**
	 * Sets measure value
	 * @param value Measure value
	 */
	public void setValue(Double value) {
		this.value = value;
	}

	/**
	 * @return the isForMinimizing
	 */
	public boolean isForMinimizing() {
		return isForMinimizing;
	}

	/**
	 * @param isForMinimizing True if this measure should be minimized (for better solutions)
	 */
	public void setForMinimizing(boolean isForMinimizing) {
		this.isForMinimizing = isForMinimizing;
	}
	
}
