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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import amuse.data.io.ArffDataSet;


/**
 * This is a stub implementation for Attributes. Extend this Class for each Attribute Type.
 * @author Clemens Waeltken
 * @version $Id$
 */
public abstract class Attribute implements AttributeInterface, Serializable {
    private static double missingValue = Double.NaN;

    public static double missingValue() {
	return missingValue;
    }

    /**
     * The name of this Attribute.
     */
    protected final String name;
    private ArffDataSet arffDataSet;
    /**
     * The list that stores any kind of values.
     */
    protected final List valueList = new ArrayList<Object>();
    /**
     * The Attribute String as used in ARFF.
     */
    public static final String attributeStr = "@ATTRIBUTE";

    /**
     * Only to be used by DataSet or Subclasses instanciated by DataSet. Not part of the API!
     * @param name The Name of this Attribute
     * @param dataSet The DataSet loaded from arff file.
     */
    Attribute(String name, ArffDataSet dataSet) {
        this.name = name;
        this.arffDataSet = dataSet;
    }

    /**
     * Creates a new Attribute with initial values.
     * @param name Name of the new Attributes.
     * @param list List of initial values.
     */
    protected Attribute(String name, List list) {
        this.name = name;
        this.valueList.addAll(list);
    }

    @SuppressWarnings("unused")
	private Attribute() {
        this.name = null;
    }

    @Override
    public Object getValueAt(int index) {
        if (getDataSet() == null) {
            return valueList.get(index).toString();
        } else {
            return getDataSet().getValueFor(index, this);
        }
    }

    @Override
    public final String getName() {
        return name;
    }

    /**
     * Returns the DataSet of this Attribute. Not part of the API!
     * @return The DataSet of this Attribute or <b>null</b> if not loaded from file.
     */
    final ArffDataSet getDataSet() {
        return arffDataSet;
    }

    public String getValueStrAt(int index) {
	return this.getValueAt(index).toString();
    }
    @Override
    public final int getValueCount() {
        if (arffDataSet != null) {
            return arffDataSet.getValueCount();
        } else {
            return valueList.size();
        }
    }
    
    @Override
    public void setValueAt(int index, Object value) {
    	if (arffDataSet != null) {
            arffDataSet.setValueAt(index, this,value); 
        } else {
            valueList.set(index, value);
        }
    }
    
    @Override
    public void addValue(Object value) {
    	if (arffDataSet != null) {
            arffDataSet.addValue(this,value);
        } else {
            valueList.add(value);
        }
    }
}

