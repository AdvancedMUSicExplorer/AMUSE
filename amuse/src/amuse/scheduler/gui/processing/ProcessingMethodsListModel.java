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
import java.util.Scanner;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import amuse.data.datasets.AlgorithmTableSet;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.scheduler.gui.algorithm.Algorithm;
import amuse.scheduler.gui.algorithm.AlgorithmChangeListener;

/**
 * 
 * @author Clemens Waeltken
 */
public class ProcessingMethodsListModel implements ListModel {

	private File algorithmTableFile = new File(
			"config" + File.separator + "processorAlgorithmTable.arff");
	private AlgorithmTableSet algorithmDataSet;
	private List<ProcessingAlgorithm> availableAlgorithms = new ArrayList<ProcessingAlgorithm>();
	private List<ProcessingAlgorithm> selectedAlgorithms = new ArrayList<ProcessingAlgorithm>();
	private List<ListDataListener> listeners = new ArrayList<ListDataListener>();

	public ProcessingMethodsListModel() throws IOException {
		// Load DataSet:
		this.algorithmDataSet = new AlgorithmTableSet(algorithmTableFile);
		// Get Attributes of AlgorithmDataSet:
		NumericAttribute idAttr = algorithmDataSet.getIdAttribute();
		StringAttribute nameAttr = algorithmDataSet.getNameAttribute();
		NominalAttribute categoryAttr = algorithmDataSet.getCategoryAttribute();
		StringAttribute descAttr = algorithmDataSet
				.getAlgorithmDescriptionAttribute();
		StringAttribute exParamNamesAttr = algorithmDataSet
				.getParameterNamesAttribute();
		StringAttribute exParamAttr = algorithmDataSet
				.getParameterDefinitionsAttribute();
		StringAttribute defaultValsAttr = algorithmDataSet
				.getDefaultParameterValuesAttribute();
		StringAttribute paramDescAttr = algorithmDataSet
				.getParameterDescriptionsAttribute();
		// Create Model:
		for (int i = 0; i < algorithmDataSet.getValueCount(); i++) {
			// Create ProcessingAlgorithm Object:
			ProcessingAlgorithm al = new ProcessingAlgorithm(idAttr.getValueAt(
					i).intValue(), nameAttr.getValueAt(i), descAttr
					.getValueAt(i), categoryAttr.getValueAt(i),
					exParamNamesAttr.getValueAt(i), exParamAttr.getValueAt(i),
					defaultValsAttr.getValueAt(i), paramDescAttr.getValueAt(i));
			this.availableAlgorithms.add(al);
		}
	}

	@Override
	public int getSize() {
		return selectedAlgorithms.size();
	}

	@Override
	public Object getElementAt(int index) {
		return selectedAlgorithms.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	public void moveDown(int selected) {
		ProcessingAlgorithm al = selectedAlgorithms.get(selected);
		if (selected < selectedAlgorithms.size() - 1) {
			selectedAlgorithms.remove(al);
			selectedAlgorithms.add(selected + 1, al);
			notifyListenersRemove(selected);
			notifyListenersAdd(selected + 1);
		}
	}

	public void moveUp(int selected) {
		ProcessingAlgorithm al = selectedAlgorithms.get(selected);
		if (selected > 0) {
			selectedAlgorithms.remove(al);
			selectedAlgorithms.add(selected - 1, al);
			notifyListenersRemove(selected);
			notifyListenersAdd(selected - 1);
		}
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}

	public void addAlgorithm(int insertIndex, Object object) {
		if (!(object instanceof ProcessingAlgorithm)) {
			throw new IllegalArgumentException("Unknown Object!");
		}
		ProcessingAlgorithm al = new ProcessingAlgorithm((ProcessingAlgorithm) object);
		selectedAlgorithms.add(al);
		notifyListenersAdd(selectedAlgorithms.indexOf(al));
		al.addAlgorithmChangeListener(new UpdateOnChangeListener());
	}

	public void removeAlgorithms(Object[] algorithms) {
		for (Object o : algorithms) {
			selectedAlgorithms.remove(o);
		}
		notifyListeners();
	}

	private void notifyListeners() {
		for (ListDataListener l : listeners) {
			l.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, 0, this.selectedAlgorithms
							.size()));
		}
	}

	private void notifyListenersRemove(int index) {
		for (ListDataListener l : listeners) {
			l.intervalRemoved(new ListDataEvent(this,
					ListDataEvent.INTERVAL_REMOVED, index, index));
		}
	}

	private void notifyListenersAdd(int index) {
		for (ListDataListener l : listeners) {
			l.intervalAdded(new ListDataEvent(this,
					ListDataEvent.INTERVAL_ADDED, index, index));
		}
	}

	public String[] getCategories() {
		return algorithmDataSet.getCategoryAttribute().getNominalValues();
	}

	protected List<Object> getAvailableAlgorithms() {
		List<ProcessingAlgorithm> list = new ArrayList<ProcessingAlgorithm>(
				availableAlgorithms);
		Collections.sort(list);
		return new ArrayList<Object>(list);
	}

    void setReductionSteps(String reductionSteps) {
        Scanner scan = new Scanner(reductionSteps);
        scan.useDelimiter("-");
        while(scan.hasNext()) {
            addAlgorithm(scan.next());
        }
    }

    private void addAlgorithm(String str) {
        String idStr = str;
        int paramBegin = str.indexOf("[");
        int paramEnd = str.indexOf("]");
        String parameters = "";
        if (paramBegin != -1 && paramEnd != -1) {
            idStr = str.substring(0, paramBegin);
            parameters = str.substring(paramBegin, paramEnd + 1);
        }
        int id = new Integer(idStr);
        for (ProcessingAlgorithm a:availableAlgorithms) {
            if (a.getID() == id) {
                ProcessingAlgorithm algo = new ProcessingAlgorithm(a);
                selectedAlgorithms.add(algo);
                if ( !parameters.equals("")) {
                    algo.setCurrentParameters(Algorithm.scanParameters(parameters));
                }
            }
        }
    }

	/**
	 * @author Clemens Waeltken
	 * 
	 */
	private final class UpdateOnChangeListener implements
			AlgorithmChangeListener {

		@Override
		public void parametersChanged() {
			notifyListeners();
		}

		/* (non-Javadoc)
		 * @see amuse.scheduler.gui.processing.AlgorithmChangeListener#parametersReset()
		 */
		@Override
		public void parametersReset() {
			notifyListeners();
		}
	}

	class ProcessingAlgorithm extends Algorithm {

		/**
		 * @param algorithm
		 */
		public ProcessingAlgorithm(ProcessingAlgorithm algorithm) {
			super (algorithm);
		}

		/**
		 * @param id
		 * @param name
		 * @param daescription
		 * @param category
		 * @param expectedParameterNames
		 * @param expectedParameters
		 * @param defaultParameterValues
		 * @param expectedParameterDescription
		 */
		public ProcessingAlgorithm(int id, String name,
				String description, String category, String expectedParameterNames,
				String expectedParameters, String defaultParameterValues, String expectedParameterDescription) {
			super(id, name, description, category, expectedParameterNames, expectedParameters, defaultParameterValues, expectedParameterDescription, true, true, true, true, true, true, true, true);
		}

		@Override
		public String toString() {
			if (selectedAlgorithms.contains(this)) {
				return selectedAlgorithms.indexOf(this) + 1 + ".\t"
						+ super.toString() + "\t"
						+ createParameterStr(this.getCurrentParameterValues());
			} else {
				return super.toString();
			}
		}
	}

	public String getReductionStepsString() {
		StringBuilder returnStr = new StringBuilder();
		for (ProcessingAlgorithm al: selectedAlgorithms) {
			returnStr.append(al.getID() + al.getParameterStr() + "-");
		}
		String reductionSteps = returnStr.toString();
		if (reductionSteps.endsWith("-")) {
			reductionSteps = reductionSteps.substring(0, reductionSteps.length() - 1);
		}
		return reductionSteps;
	}

	/**
	 * @param currentParameterValues
	 * @return
	 */
	private String createParameterStr(String[] currentParameterValues) {
	    if (currentParameterValues.length == 0) {
		return "";
	    }
		StringBuilder returnStr = new StringBuilder("[");
		for (int i = 0; i < currentParameterValues.length; i++) {
			returnStr.append(currentParameterValues[i]);
			if (i + 1 < currentParameterValues.length) {
				returnStr.append("_");
			}
		}
		returnStr.append("]");
		return returnStr.toString();
	}

}
