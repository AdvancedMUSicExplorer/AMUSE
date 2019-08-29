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
 * Creation date: 21.07.2009
 */
package amuse.scheduler.gui.training;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import amuse.data.datasets.AlgorithmTableSet;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.scheduler.gui.algorithm.Algorithm;

/**
 * @author Clemens Waeltken
 *
 */
public class TrainingAlgorithmComboBoxModel extends DefaultComboBoxModel {

	private static final long serialVersionUID = 9019195556557764573L;
	private File file = new File("config" + File.separator + "classifierAlgorithmTable.arff");
	private AlgorithmTableSet algorithmTable;
	private final List<Algorithm> availableAlgorithms;
	
	public TrainingAlgorithmComboBoxModel() throws IOException {
		// Load AlgorithmTableSet from file.
		this.algorithmTable = new AlgorithmTableSet(file);
		// Get Attributes of AlgorithmDataSet:
		NumericAttribute idAttr = algorithmTable.getIdAttribute();
		StringAttribute nameAttr = algorithmTable.getNameAttribute();
		NominalAttribute categoryAttr = algorithmTable.getCategoryAttribute();
		StringAttribute descAttr = algorithmTable
				.getAlgorithmDescriptionAttribute();
		StringAttribute exParamNamesAttr = algorithmTable
				.getParameterNamesAttribute();
		StringAttribute exParamAttr = algorithmTable
				.getParameterDefinitionsAttribute();
		StringAttribute defaultValsAttr = algorithmTable
				.getDefaultParameterValuesAttribute();
		StringAttribute paramDescAttr = algorithmTable
				.getParameterDescriptionsAttribute();
		
		NumericAttribute supportsBinaryAttr = algorithmTable.getSupportsBinaryAttribute();
		NumericAttribute supportsContinuousAttr = algorithmTable.getSupportsContinuousAttribute();
		NumericAttribute supportsMulticlassAttr = algorithmTable.getSupportsMulticlassAttribute();
		NumericAttribute supportsMultilabelAttr = algorithmTable.getSupportsMultilabelAttribute();
		NumericAttribute supportsSinglelabelAttr = algorithmTable.getSupportsSinglelabelAttribute();
		NumericAttribute supportsSupervisedAttr = algorithmTable.getSupportsSupervisedAttribute();
		NumericAttribute supportsUnsupervisedAttr = algorithmTable.getSupportsUnsupervisedAttribute();
		NumericAttribute supportsRegressionAttr = algorithmTable.getSupportsRegressionAttribute();
		
		// Create Model:
		availableAlgorithms = new ArrayList<Algorithm>();
		for (int i = 0; i < algorithmTable.getValueCount(); i++) {
			boolean supportsBinary = supportsBinaryAttr.getValueAt(i) != 0;
			boolean supportsContinuous = supportsContinuousAttr.getValueAt(i) != 0;
			boolean supportsMulticlass = supportsMulticlassAttr.getValueAt(i) != 0;
			boolean supportsMultilabel = supportsMultilabelAttr.getValueAt(i) != 0;
			boolean supportsSinglelabel = supportsSinglelabelAttr.getValueAt(i) != 0;
			boolean supportsSupervised = supportsSupervisedAttr.getValueAt(i) != 0;
			boolean supportsUnsupervised = supportsUnsupervisedAttr.getValueAt(i) != 0;
			boolean supportsRegression = supportsRegressionAttr.getValueAt(i) != 0;
			
			// Create ProcessingAlgorithm Object:
			Algorithm al = new Algorithm(idAttr.getValueAt(
					i).intValue(), nameAttr.getValueAt(i), descAttr
					.getValueAt(i), categoryAttr.getValueAt(i),
					exParamNamesAttr.getValueAt(i), exParamAttr.getValueAt(i),
					defaultValsAttr.getValueAt(i), paramDescAttr.getValueAt(i),
					supportsBinary,
					supportsContinuous,
					supportsMulticlass,
					supportsMultilabel,
					supportsSinglelabel,
					supportsSupervised,
					supportsUnsupervised,
					supportsRegression);
			availableAlgorithms.add(al);
		}
		for (Algorithm al : availableAlgorithms) {
			super.addElement(al);
		}
	}

    public void setSelectedAlgorithm(String str) {
	String idStr = str;
        int paramBegin = str.indexOf("[");
        int paramEnd = str.indexOf("]");
        String parameters = "";
        if (paramBegin != -1 && paramEnd != -1) {
            idStr = str.substring(0, paramBegin);
            parameters = str.substring(paramBegin, paramEnd + 1);
        }
        int id = new Integer(idStr);
	for (Algorithm a: availableAlgorithms) {
	    if (a.getID() == id) {
		super.setSelectedItem(a);
                if ( !parameters.equals("")) {
                    a.setCurrentParameters(Algorithm.scanParameters(parameters));
                }
            }
	}
    }
}
