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
 * Creation date: 15.01.2009
 */
package amuse.data.io.attributes;

/**
 * This Interface describes basic funktionality offered by Attributes.
 * 
 * @author Clemens Waeltken
 * @version $Id$
 */
public interface AttributeInterface {

	/**
	 * Returns this Attributes Name.
	 * 
	 * @return Name of this Attribute.
	 */
	String getName();

	/**
	 * Returns the header String as used in ARFF.
	 * 
	 * @return Header for this Attribute as used in ARFF,
	 */
	String getHeaderStr();

	/**
	 * Returns the type String as used in ARFF.
	 * 
	 * @return Type String as used in ARFF.
	 */
	String getTypeStr();

	/**
	 * Use this method to get the count of values stored in this Attribute.
	 * 
	 * @return Count of values in this Attribute.
	 */
	int getValueCount();

	/**
	 * Return a value of this Attribute. This method is usually overwritten by
	 * Subclasses of <class>Attribute</class>.
	 * 
	 * @param index
	 *            The index of this value.
	 *            <em>IndexOutOfBoundsException will be thrown if not in range!</em>
	 * @return Returns a value of this Attribute. The exact type of a value is
	 *         determined by each implementing Class.
	 */
	Object getValueAt(int index);

	void setValueAt(int index, Object value);

	void addValue(Object value);

	/**
	 * Check if this String would be a valid value for this
	 * <class>Attribute</class>. This method is only used while validating arff
	 * files loaded by <class>DataSet</class>.
	 * 
	 * @param value
	 *            The value to check.
	 * @return true if the given value is valid in the context of this
	 *         <class>Attribute</class>.
	 */
	boolean isValid(String value);
}
