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
 * Creation date: 15.04.2009
 */
package amuse.data.io.attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetException;
import java.util.Arrays;

/**
 * The Nominal Attribute as used in ARFF. Nominal Attributes store any kind of
 * nominal data, e.g.: "@Attribute Sex {Male, Female}". The nominal values will
 * be handled case insensitve. (Male == mAlE)
 * 
 * @author Clemens Waeltken
 * @version $Id$
 */
public class NominalAttribute extends Attribute {

	static String typeStrPattern = "\\{.*,.*\\}";

	static List<String> parseNominalList(String str) throws IOException {
		List<String> list = new ArrayList<String>();
		if (!(str.startsWith("{") || str.endsWith("}"))) {
			throw new IOException("Illegal input: " + str);
		}
		Scanner scanner = new Scanner(str.substring(1, str.length() - 1));
		scanner.useDelimiter("\\p{Blank}*,\\p{Blank}*");
		while (scanner.hasNext()) {
			list.add(removeQuotes(scanner.next()));
		}
		return list;
	}

	/**
	 * @param str
	 * @return
	 */
	private static String removeQuotes(String str) {
		if (str.startsWith("\"") && str.endsWith("\"")) {
			return str.substring(1, str.length() - 1);
		} else {
			return str;
		}
	}

	private List<String> nominalValues = new ArrayList<String>();

	/**
	 * Creates a new NominalAttribute, with given values.
	 * 
	 * @param name
	 *            Name of this Attribute.
	 * @param values
	 *            List of nominal values.
	 */
	public NominalAttribute(String name, List<String> values) {
		super(name, values);
		generateNominalValues(values);
	}

	/**
	 * Creates a new NominalAttribute with list of allowed nominal values. The
	 * value list will be checked accordingly.
	 * 
	 * @param name
	 *            Name of this Attribute
	 * @param allowedValues
	 *            List of allowed nominal values.
	 * @param values
	 *            List of nominal values.
	 */
	public NominalAttribute(String name, List<String> allowedValues,
			List<String> values) {
		super(name, values);
		nominalValues.addAll(allowedValues);
		validateValues(values);
	}

	/**
	 * This constructor is not part of the API! Only to be used by DataSet while
	 * loading from file!
	 * 
	 * @param name
	 *            The name of this Attribute.
	 * @param allowedValues
	 *            List of Strings with expected Nominal Values. <b>getValue</b>
	 *            will only return these.
	 * @param dataSet
	 */
	public NominalAttribute(String name, List<String> allowedValues,
			ArffDataSet dataSet) {
		super(name, dataSet);
		nominalValues.addAll(allowedValues);
	}

	public NominalAttribute(NominalAttribute a) {
		this(a.name, a.getValues());
	}

	public static NominalAttribute createFromBooleans(String name,
			List<Boolean> values) {
		ArrayList<String> allowedValues = new ArrayList<String>();
		allowedValues.add(Boolean.toString(true));
		allowedValues.add(Boolean.toString(false));
		ArrayList<String> valuesAsStrings = new ArrayList<String>();
		for (Boolean b : values) {
			valuesAsStrings.add(Boolean.toString(b));
		}
		return new NominalAttribute(name, allowedValues, valuesAsStrings);
	}

    public NominalAttribute(String name, String[] string) {
	this(name, Arrays.asList(string));
    }

    @Override
	public String getHeaderStr() {
		return attributeStr + " '" + name + "' " + getTypeStr();
	}

	@Override
	public String getTypeStr() {
		return getNomialValueList();
	}

	@Override
	public String getValueAt(int index) {
		if (super.getDataSet() == null) {
			return (valueList.get(index).toString());
		} else {
			int val = (int) getDataSet().getValueFor(index, this);
			return nominalValues.get(val);
		}
	}

	/**
	 * This method is used to return a List of all values stored in this
	 * Attribute.
	 * 
	 * @return <code>List</code> with all nominal values as <code>String</code>
	 *         stored in this Attribute.
	 */
	public List<String> getValues() {
		if (getDataSet() == null) {
			return new ArrayList<String>(valueList);
		} else {
			ArrayList<String> values = new ArrayList<String>();
			for (int i = 0; i < getValueCount(); i++) {
				values.add(getValueAt(i));
			}
			return values;
		}
	}

	@Override
	public String getValueStrAt(int index) {
		return "'" + getValueAt(index) + "'";
	}

	private void checkValue(String val) {
		if (val.equalsIgnoreCase("?")) {
			return;
		}
		for (String nominal : nominalValues) {
			if (nominal.equalsIgnoreCase(removeQuotes(val))) {
				return;
			}
		}
		String msg = "One or more given values do not match accepted nominal values!\n"
				+ val + " not in: " + getNomialValueList() + "!";
		throw new DataSetException(msg);
	}

	private void generateNominalValues(List<String> values) {
		for (String val : values) {
			if (!nominalValues.contains(val.toLowerCase())) {
				nominalValues.add(val);
			}
		}
	}

	private String getNomialValueList() {
		StringBuilder str = new StringBuilder("{");
		for (int i = 0; i < nominalValues.size(); i++) {
			str.append("'" + nominalValues.get(i) + "'");
			if (i < nominalValues.size() - 1) {
				str.append(",");
			}
		}
		str.append("}");
		return str.toString();
	}

	private void validateValues(List<String> values) {
		for (String val : values) {
			checkValue(val);
		}
	}

	@Override
	public boolean isValid(String value) {
		value = removeQuotes(value);
		try {
			checkValue(value);
		} catch (DataSetException ex) {
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	public String[] getNominalValues() {
		return nominalValues.toArray(new String[nominalValues.size()]);
	}

	public int indexOfValue(String value) {
		return nominalValues.indexOf(value);
	}
}

