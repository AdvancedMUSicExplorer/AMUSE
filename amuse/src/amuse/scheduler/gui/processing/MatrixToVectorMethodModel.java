/*
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
 * Creation date: 06.03.2009
 */

package amuse.scheduler.gui.processing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import amuse.data.datasets.AlgorithmTableSet;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.scheduler.gui.algorithm.Algorithm;

/**
 * Model used for MatrixToVector Methods.
 * @author Clemens Waeltken
 */
public class MatrixToVectorMethodModel implements ComboBoxModel {
    private File file = new File("config" + File.separator + "processorConversionAlgorithmTable.arff");
    private AlgorithmTableSet matrixToVectorSet;
    private List<Algorithm> methods = new Vector<Algorithm>();
    private Algorithm selectedMethod;
    private List<ListDataListener> listeners = new ArrayList<ListDataListener>();

    public MatrixToVectorMethodModel() throws IOException {
        matrixToVectorSet = new AlgorithmTableSet(file);
        NumericAttribute idAttr = matrixToVectorSet.getIdAttribute();
        StringAttribute nameAttr = matrixToVectorSet.getNameAttribute();
        StringAttribute descAttr = matrixToVectorSet.getAlgorithmDescriptionAttribute();
        StringAttribute exParamNamesAttr = matrixToVectorSet.getParameterNamesAttribute();
        StringAttribute exParamAttr = matrixToVectorSet.getParameterDefinitionsAttribute();
        StringAttribute defaultValsAttr = matrixToVectorSet.getDefaultParameterValuesAttribute();
        StringAttribute paramDescAttr = matrixToVectorSet.getParameterDescriptionsAttribute();
        for (int i = 0; i < matrixToVectorSet.getValueCount(); i++) {
            methods.add(new Algorithm(idAttr.getValueAt(i).intValue(), nameAttr.getValueAt(i), descAttr.getValueAt(i), "",exParamNamesAttr.getValueAt(i), exParamAttr.getValueAt(i), defaultValsAttr.getValueAt(i), paramDescAttr.getValueAt(i), true, true, true, true, true, true, true, true));
        }
        if (methods.isEmpty()) {
            throw new IOException("Empty Processor Conversion Algorithm Table!");
        }
        Collections.sort(methods);
        selectedMethod = methods.get(0);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedMethod = (Algorithm) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedMethod;
    }

    @Override
    public int getSize() {
        return methods.size();
    }

    @Override
    public Object getElementAt(int index) {
        return methods.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

	/**
	 * @return
	 */
	public String getConversionStepStr() {
		Algorithm al = (Algorithm) getSelectedItem();
		return al.getID()+al.getParameterStr();
	}

    void setMethod(String value) {
        String paramStr = "";
        value = value.trim();
        if (value.contains("[")) {
            if (!value.endsWith("]"))
                throw new IllegalArgumentException("Not a valid matrix to vector description: "+value);
            paramStr = value.substring(value.indexOf("["), value.indexOf("]")+1);
            value = value.substring(0, value.indexOf("["));
        }
        int id = new Integer(value);
        for(Algorithm al : methods) {
            if (al.getID() == id) {
                setSelectedItem(al);
                if (!paramStr.isEmpty()) {
                    al.setCurrentParameters(paramStr);
                }
            }
        }
    }
}
