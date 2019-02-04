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
 * Creation date: 28.05.2010
 */
package amuse.data.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import amuse.data.io.attributes.Attribute;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Ontology;

/**
 * DataSet contains different attributes with values
 * 
 * @author Igor Vatolkin
 * @version $Id: DataSet.java 241 2018-07-26 12:35:24Z frederik-h $
 */
public class DataSet extends DataSetAbstract {

    public DataSet(String name) {
	this.name = name;
    }

    /**
     * Constructor which initializes DataSet from an ARFF file loading the data
     * into memory
     *
     * @param arffFile
     * @param name
     * @throws IOException
     */
    public DataSet(File arffFile) throws IOException {
	super();
	ArffDataSet fileSet = new ArffDataSet(arffFile);

	// Create containers:
	Object[] values = new Object[fileSet.getAttributeCount()];
	int aCount = 0;
	for (Attribute a : fileSet.attributes) {
	    Attribute newAttr;
	    if (a instanceof NumericAttribute) {
		values[aCount] = new Double[fileSet.getValueCount()];
	    } else if (a instanceof StringAttribute) {
		values[aCount] = new String[fileSet.getValueCount()];
	    } else {
		values[aCount] = new String[fileSet.getValueCount()];
	    }
            aCount++;
	}
	// Fill containers:
	for (int i = 0; i < fileSet.getValueCount(); i++) {
	    aCount = 0;
	    for (Attribute a : fileSet.attributes) {
		if (a instanceof NumericAttribute) {
		    NumericAttribute atr = (NumericAttribute) a;
		    Double[] val = (Double[]) values[aCount];
		    val[i]= (atr.getValueAt(i));
		} else if (a instanceof StringAttribute) {
		    StringAttribute atr = (StringAttribute) a;
		    String[] val = (String[]) values[aCount];
		    val[i]= (atr.getValueAt(i));
		} else {
		    NominalAttribute atr = (NominalAttribute) a;
		    String[] val = (String[]) values[aCount];
		    val[i]= (atr.getValueAt(i));
		}
                aCount++;
	    }
	}
	
	// Create the new Attributes
	aCount = 0;
	for (Attribute a : fileSet.attributes) {
	    Attribute newAttr;
	    if (a instanceof NumericAttribute) {
		newAttr = new NumericAttribute(a.getName(), (Double[])values[aCount]);
	    } else if (a instanceof StringAttribute) {
		newAttr = new StringAttribute(a.getName(), (String[])values[aCount]);
	    } else {
		newAttr = new NominalAttribute(a.getName(), (String[])values[aCount]);
	    }
	    attributes.add(newAttr);
            aCount++;
	}
    }

    public DataSet(File arffFile, String name) throws IOException {
	this(arffFile);
	this.name = name;
    }

    /**
     * Constructor which creates DataSet from RapidMiner object ExampleSet
     *
     * @param exampleSet
     */
    public DataSet(ExampleSet exampleSet) {
	this.name = exampleSet.getName();

	// Create the attributes
	Iterator<com.rapidminer.example.Attribute> it = exampleSet.getAttributes().allAttributes();
	while (it.hasNext()) {
	    com.rapidminer.example.Attribute a = it.next();
	    if (a.isNumerical()) {
		attributes.add(new NumericAttribute(a.getName(), new ArrayList<Double>()));
	    } else {
		attributes.add(new StringAttribute(a.getName(), new ArrayList<String>()));
	    }
	}

	// Load the values
	it = exampleSet.getAttributes().allAttributes();
	int i = 0;
	while (it.hasNext()) {
	    com.rapidminer.example.Attribute a = it.next();
	    for (int j = 0; j < exampleSet.size(); j++) {
		if (a.isNumerical()) {
		    attributes.get(i).addValue(exampleSet.getExample(j).getValue(a));
		} else {
		    String s = a.getMapping().mapIndex(new Double(exampleSet.getExample(j).getValue(a)).intValue());
		    attributes.get(i).addValue(s);
		}
	    }
	    i++;
	}
    }

    /**
     * Returns the number of values if it is equal for all attributes; otherwise
     * returns -1
     */
    public final int getValueCount() {
	int valueCountForFirstAttr = attributes.get(0).getValueCount();
	for (Attribute a : attributes) {
	    if (a.getValueCount() != valueCountForFirstAttr) {
		return -1;
	    }
	}
	return valueCountForFirstAttr;
    }

    @Override
    public void addAttribute(Attribute a) {
	attributes.add(a);
    }

    /**
     * Generates the RapidMiner object ExampleSet from this DataSet
     *
     * @return RapidMiner ExampleSet
     */
    public ExampleSet convertToRapidMinerExampleSet() throws IOException{

	NominalMapping mapping = null;
	com.rapidminer.example.Attribute idAttribute = null;
	com.rapidminer.example.Attribute labelAttribute = null;

	// Create attribute list
	List<com.rapidminer.example.Attribute> attributes = new LinkedList<com.rapidminer.example.Attribute>();
	int numberOfCategories = 1;
	for (int a = 0; a < getAttributeCount(); a++) {
	    if (getAttribute(a).getName().equals("Id")) {
		idAttribute = AttributeFactory.createAttribute(getAttribute(a).getName(), Ontology.INTEGER);
		attributes.add(idAttribute);
	    } 
	    
	    else if (getAttribute(a).getName().equals("NumberOfCategories")) {
	    	numberOfCategories = (int)((double)getAttribute(a).getValueAt(0));
	    	labelAttribute = AttributeFactory.createAttribute("Category", Ontology.NOMINAL);
	    	attributes.add(labelAttribute);
	    	mapping = attributes.get(attributes.size() - 1).getMapping();
	    	a += numberOfCategories;
	    }
	    
	    else if (getAttribute(a) instanceof StringAttribute) {
		attributes.add(AttributeFactory.createAttribute(getAttribute(a).getName(), Ontology.NOMINAL));
	    } else {
		attributes.add(AttributeFactory.createAttribute(getAttribute(a).getName(), Ontology.REAL));
	    }
	}

	// Create table
	MemoryExampleTable table = new MemoryExampleTable(attributes);

	// Fill table with all time windows
	for (int d = 0; d < getValueCount(); d++) {
	    double[] data = new double[attributes.size()];
	    // int currData = 0;
	    int offSet = 0;//offSet between the RapidMiner attributes and the DataSet attributes (NumberOfAttributes + Category in the DataSet correspond to just  Category in the RapidMiner set)
	    for (int a = 0; a < attributes.size(); a++) {
	    	// System.out.println(attributes.get(a).getName());
	    	if (attributes.get(a).getName().equals("Id")) {
	    		/*
	    		* System.out.println("id found");
	    		*
		     	*System.out.println(getAttribute(a).getValueAt(d));
		     	*System.
		     	* out.println(getAttribute(a).getValueAt(d).toString());
		     	*/
	    		data[a] = new Double(getAttribute(a+offSet).getValueAt(d).toString());
	    		// data[a] =
	    		// attributes.get(a).getMapping().mapString(getAttribute(a).getValueAt(d).toString());
	    		// currData++;offSet++;
	    		// idAttribute.
	    		
	    	} else if (attributes.get(a).getName().equals("Category")) {
	    		if(offSet != 0) {
	    			throw new IOException("There is something wrong with the data.");
	    		}
	    		if(numberOfCategories == 1) {
	    			data[a] = mapping.mapString(((double)getAttribute(a+1).getValueAt(d) >= 0.5 ? "" : "NOT_") + getAttribute(a+1).getName());
			    }
	    		
	    		for(int i = 0; i < numberOfCategories; i++) {
	    			if((double)getAttribute(a+1+i).getValueAt(d) == 1) {
	    				data[a] = mapping.mapString(i + "-" + getAttribute(a+1+i).getName());
	    			}
	    		}
	    		
			    offSet += numberOfCategories;
			    // currData++;
			} else if (getAttribute(a+offSet) instanceof StringAttribute) {
	    		data[a] = attributes.get(a).getMapping().mapString(
			    getAttribute(a+offSet).getValueAt(d).toString());
	    		// currData++;
	    	} else {
	    		data[a] = new Double(getAttribute(a+offSet).getValueAt(d).toString());
	    		// currData++;
	    	}
	    }

	    // Add data row
	    table.addDataRow(new DoubleArrayDataRow(data));
	}

	// Create example set
	return table.createExampleSet(labelAttribute, null, idAttribute);
    }

    public final void saveToArffFile(File file) throws IOException {
	ArffDataSet arffDataSet = new ArffDataSet(this.name);
	arffDataSet.addAttributeList(this.attributes);
	arffDataSet.saveToArffFile(file);
    }
}
