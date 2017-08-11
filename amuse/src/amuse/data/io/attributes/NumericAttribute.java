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

import java.util.ArrayList;
import java.util.List;

import amuse.data.io.ArffDataSet;
import java.util.Arrays;

/**
 * The Numeric Attribute as used in ARFF. Numeric Attributes store any kind of
 * numeric data.
 * 
 * @author Clemens Waeltken
 * @version $Id$
 */
public class NumericAttribute extends Attribute {

	/**
	 * The type String for NumericAttributes as used in ARFF.
	 */
	public static final String typeStr = "NUMERIC";

	/**
	 * Creates a new NumericAttribute with given name and initial values.
	 * 
	 * @param name
	 *            Name of this new Attribute.
	 * @param values
	 *            Initial values of this Attribute.
	 */
	public NumericAttribute(String name, List<Double> values) {
		super(name, values);
	}

	public static NumericAttribute createFromIntList(String name,
			List<Integer> values) {
		List<Double> list = new ArrayList<Double>();
		for (int i : values) {
			list.add(new Double(i));
		}
		return new NumericAttribute(name, list);
	}

	public static NumericAttribute createFromDouble(String name, double value) {
		List<Double> list = new ArrayList<Double>();
		list.add(value);
		return new NumericAttribute(name, list);
	}

	public NumericAttribute(String name, ArffDataSet accordingSet) {
		super(name, accordingSet);
	}

	/* Only copies containing data. */
	public NumericAttribute(NumericAttribute a) {
		this(a.name, a.getValues());
	}

    public NumericAttribute(String name, Double[] aDouble) {
	this(name, Arrays.asList(aDouble));
    }

	/**
	 * This method is used to return a List of all values stored in this
	 * Attribute.
	 * 
	 * @return <code>List</code> with all <code>Double</code> values stored in
	 *         this Attribute.
	 */
	public List<Double> getValues() {
		if (getDataSet() == null) {
			return new ArrayList<Double>(valueList);
		} else {
			ArrayList<Double> values = new ArrayList<Double>();
			for (int i = 0; i < getValueCount(); i++) {
				values.add(getValueAt(i));
			}
			return values;
		}
	}

	@Override
	public String getHeaderStr() {
		return attributeStr + " '" + name + "' " + typeStr;
	}

	@Override
	public String getTypeStr() {
		return typeStr;
	}

	public String getValueStrAt(int index) {
		if (getValueAt(index) - Math.floor(getValueAt(index)) == 0) {
			return getValueAt(index).intValue() + "";
		}
		return getValues().get(index).toString();
	}

	@Override
	public Double getValueAt(int index) {
		if (getDataSet() == null) {
			return (Double) valueList.get(index);
		} else {
			return parse(super.getValueAt(index).toString());
		}
	}

	static Double parse(String str) throws NumberFormatException {
		if (str.equals("?")) {
			return new Double(Double.NaN);
		}
		return new Double(str);
	}

	@Override
	public boolean isValid(String value) {
		try {
			parse(value);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}
}
