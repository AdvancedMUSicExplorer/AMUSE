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
 * The String Attribute as used in ARFF. String Attributes store any kind of
 * String data.
 * 
 * @author Clemens Waeltken
 * @version $Id$
 */
public class StringAttribute extends Attribute {

	/**
	 * Type-String for StringAttributes.
	 */
	public static final String typeStr = "STRING";

	private static String removeEscapeChars(String str) {
		StringBuilder returnStr = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '\\' && str.charAt(i + 1) == '\'') {
				returnStr.append('\'');
				i++;
			} else if (str.charAt(i) == '\\' && str.charAt(i + 1) == '"') {
				returnStr.append('"');
				i++;
			} else if (str.charAt(i) == '\\' && str.charAt(i + 1) == '\\') {
				returnStr.append('\\');
				i++;
			} else {
				returnStr.append(str.charAt(i));
			}
		}
		return returnStr.toString();
	}

	private List<String> stringValues = new ArrayList<String>();

	/**
	 * 
	 * @param name
	 * @param values
	 */
	public StringAttribute(String name, List<String> values) {
		super(name, values);
	}

	public static StringAttribute createFromString(String name, String value) {
		List<String> list = new ArrayList<String>();
		list.add(value);
		return new StringAttribute(name, list);
	}

	/**
	 * This constructor should only be used by DataSet! It is not part of the
	 * API!
	 * 
	 * @param name
	 *            Name of this Attribute.
	 * @param dataSet
	 *            The DataSet loaded form arff file.
	 */
	public StringAttribute(String name, ArffDataSet dataSet) {
		super(name, dataSet);
	}

	public StringAttribute(StringAttribute a) {
		this(a.name, a.getValues());
	}

    public StringAttribute(String name, String[] string) {
	this(name, Arrays.asList(string));
    }

	/**
	 * 
	 * @return
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
	public String getHeaderStr() {
		return attributeStr + " '" + name + "' " + typeStr;
	}

	@Override
	public String getTypeStr() {
		return typeStr;
	}

	@Override
	public String getValueStrAt(int index) {
		StringBuilder valueStr = new StringBuilder(getValueAt(index));
		StringBuilder returnStr = new StringBuilder("'");
		for (int i = 0; i < valueStr.length(); i++) {
			if (valueStr.charAt(i) == '\'') {
				returnStr.append("\\'");
			} else if (valueStr.charAt(i) == '\\') {
				returnStr.append("\\\\");
			} else {
				returnStr.append(valueStr.charAt(i));
			}
		}
		return returnStr.toString() + "'";
	}

	@Override
	public String getValueAt(int index) {
		if (getDataSet() == null) {
			return (String) valueList.get(index);
		} else {
			return stringValues
					.get((int) getDataSet().getValueFor(index, this));
		}
	}
	
	@Override
    public void setValueAt(int index, Object value) {
    	if (getDataSet() != null) {
    		stringValues.set((int) getDataSet().getValueFor(index, this), (String) value);
        } else {
            valueList.set(index, value);
        }
    }

	static String parse(String str) {
		if (str.startsWith("\"")) {
			if (!str.endsWith("\"")) {
				throw new IllegalArgumentException(str);
			}
			return removeEscapeChars(str.substring(1, str.length() - 1));
		}
		if (str.startsWith("'")) {
			if (!str.endsWith("'")) {
				throw new IllegalArgumentException(str);
			}
			return removeEscapeChars(str.substring(1, str.length() - 1));
		}
		return removeEscapeChars(str);
	}

	@Override
	public boolean isValid(String value) {
		try {
			parse(value);
		} catch (IllegalArgumentException ex) {
			return false;
		}
		return true;
	}

	public double addStringValue(String value) {
		if (!stringValues.contains(value))
			stringValues.add(value);
		return stringValues.indexOf(value);
	}
}
