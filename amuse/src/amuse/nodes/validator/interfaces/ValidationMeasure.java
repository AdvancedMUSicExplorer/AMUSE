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
 *  Creation date: 24.06.2010
 */ 
package amuse.nodes.validator.interfaces;

/**
 * For all validation measures.
 *  
 * @author Igor Vatolkin
 * @version $Id: ValidationMeasure.java 241 2018-07-26 12:35:24Z frederik-h $
 */
public abstract class ValidationMeasure {
	
	/** Measure ID */
	private int id;
	
	/** Measure name */
	private String name;
	
	/** Measure value */
	protected Object value;

	/**
	 * Gets measure id
	 * @return Measure id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets measure id
	 * @param id Measure id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets measure name
	 * @return Measure name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets measure name
	 * @param name Measure name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets measure value
	 * @return Measure value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets measure value
	 * @param value Measure value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
}
