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
 * Creation date: 28.04.2009
 */
package amuse.scheduler.gui.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * 
 * @author Clemens Waeltken
 */
public class Algorithm implements Comparable<Algorithm>, AlgorithmInterface {

	private final int id;
	private final String name;
	private final String description;
	private final String[] expectedParameterNames;
	private final String[] expectedParameters;
	private final String[] defaultParameterValues;
	private final String[] expectedParameterDescription;
	private final String[] currentParameterValues;
	private final String category;
	private final boolean supportsBinary;
	private final boolean supportsContinuous;
	private final boolean supportsMulticlass;
	private final boolean supportsMultilabel;
	private final boolean supportsSinglelabel;
	private final boolean supportsSupervised;
	private final boolean supportsUnsupervised;
	private final boolean supportsRegression;
	private final List<AlgorithmChangeListener> listeners = new ArrayList<AlgorithmChangeListener>();

	public Algorithm(int id, String name, String description, String category, String expectedParameterNames,
			String expectedParameters,
			String defaultParameterValues, String expectedParameterDescription,
			boolean supportsBinary,
			boolean supportsContinuous,
			boolean supportsMulticlass,
			boolean supportsMultilabel,
			boolean supportsSinglelabel,
			boolean supportsSupervised,
			boolean supportsUnsupervised,
			boolean supportsRegression
			) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.expectedParameterNames = scanNamesOrDescriptions(expectedParameterNames);
		this.expectedParameters = scanParameterDefinition(expectedParameters);
		this.defaultParameterValues = scanParameters(defaultParameterValues);
		this.expectedParameterDescription = scanNamesOrDescriptions(expectedParameterDescription);
		this.currentParameterValues = scanParameters(defaultParameterValues);
		this.supportsBinary = supportsBinary;
		this.supportsContinuous = supportsContinuous;
		this.supportsMulticlass = supportsMulticlass;
		this.supportsMultilabel = supportsMultilabel;
		this.supportsSinglelabel = supportsSinglelabel;
		this.supportsSupervised = supportsSupervised;
		this.supportsUnsupervised = supportsUnsupervised;
		this.supportsRegression = supportsRegression;
		
	}

	public Algorithm(Algorithm copy) {
		this.id = copy.id;
		this.name = copy.name;
		this.description = copy.description;
		this.category = copy.category;
		this.expectedParameterNames = copy.expectedParameterNames;
		this.expectedParameters = copy.expectedParameters;
		this.defaultParameterValues = copy.defaultParameterValues;
		this.expectedParameterDescription = copy.expectedParameterDescription;
		this.currentParameterValues = copy.currentParameterValues.clone();
		this.supportsBinary = copy.supportsBinary;
		this.supportsContinuous = copy.supportsContinuous;
		this.supportsMulticlass = copy.supportsMulticlass;
		this.supportsMultilabel = copy.supportsMultilabel;
		this.supportsSinglelabel = copy.supportsSinglelabel;
		this.supportsSupervised = copy.supportsSupervised;
		this.supportsUnsupervised = copy.supportsUnsupervised;
		this.supportsRegression = copy.supportsRegression;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public String[] getCurrentParameterValues() {
		return currentParameterValues;
	}

	/**
	 * @param currentParameterValues
	 * @return
	 */
	public String getParameterStr() {
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

	@Override
	public void setCurrentParameters(String[] parameters) {
		if (parameters.length != currentParameterValues.length) {
			throw new IllegalArgumentException();
		}
		int i = 0;
		for (String s : parameters) {
			currentParameterValues[i] = s;
			i++;
		}
		notifyListeners();
	}

	public void setCurrentParameters(String parameterStr) {
		String[] parameters = scanParameters(parameterStr);
		if (parameters.length != currentParameterValues.length) {
			throw new IllegalArgumentException();
		}
		int i = 0;
		for (String s : parameters) {
			currentParameterValues[i] = s;
			i++;
		}
		notifyListeners();
	}

	private void notifyListeners() {
		for (AlgorithmChangeListener l : listeners) {
			l.parametersChanged();
		}
	}

	@Override
	public String[] getAllowedParamerterStrings() {
		return expectedParameters;
	}

	@Override
	public String[] getParameterDescriptions() {
		return expectedParameterDescription;
	}

	private String[] scanNamesOrDescriptions(String expectedParameterDescription) {
		//        System.out.println(expectedParameterDescription);
		Scanner scanner = new Scanner(expectedParameterDescription);
		scanner.useDelimiter("\\||$");
		List<String> params = new ArrayList<String>();
		while (scanner.hasNext()) {
			params.add(scanner.next());
		}
		return params.toArray(new String[params.size()]);
	}

	private String[] scanParameterDefinition(String expectedParameters) {
		if (expectedParameters.isEmpty()) {
			return new String[0];
		}
		if (!expectedParameters.startsWith("[") || !expectedParameters.endsWith("]")) {
			throw new IllegalArgumentException("Unable to parse parameter definition: " + expectedParameters);
		}
		Scanner scanner = new Scanner(expectedParameters.substring(1, expectedParameters.length() - 1));
		scanner.useDelimiter("%");
		List<String> params = new ArrayList<String>();
		while (scanner.hasNext()) {
			params.add(scanner.next());
		}
		return params.toArray(new String[params.size()]);
	}
	
	public static String[] scanParameters(String parameterString) {
		if (parameterString.isEmpty()) {
			return new String[0];
		}
		if (!parameterString.startsWith("[") || !parameterString.endsWith("]")) {
			throw new IllegalArgumentException("Unable to parse parameters: " + parameterString);
		}
		Scanner scanner = new Scanner(parameterString.substring(1, parameterString.length() - 1));
		scanner.useDelimiter("\\_"); //FIXME
		List<String> params = new ArrayList<String>();
		while (scanner.hasNext()) {
			String next = scanner.next();
			if(next.startsWith("|")){ //path values are surrounded by "|". In path values, "_" is allowed.
				if(!next.endsWith("|")){
					while (scanner.hasNext()) {
						next += "_" + scanner.next();
						if(next.endsWith("|")){
							break;
						}
					}
				}
				if(!next.endsWith("|")){
					throw new IllegalArgumentException("Unable to parse default parameters: " + parameterString);
				}
				next = next.substring(1, next.length() - 1);
			}
			params.add(next);
		}
		scanner.close();
		if (params.isEmpty()) {
			params.add("");
		}
		return params.toArray(new String[params.size()]);
	}

	@Override
	public String[] getParameterNames() {
		return this.expectedParameterNames;
	}

	@Override
	public String[] getDefaultParameters() {
		return this.defaultParameterValues;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.processing.AlgorithmInterface#addAlgorithmChangeListener(amuse.scheduler.gui.processing.AlgorithmChangeListener)
	 */
	@Override
	public void addAlgorithmChangeListener(AlgorithmChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.processing.AlgorithmInterface#removeAlgoritmChangeListener()
	 */
	@Override
	public void removeAlgoritmChangeListener(AlgorithmChangeListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Algorithm o) {
		return this.toString().compareTo(o.toString());
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.processing.AlgorithmInterface#setCurrentParameterAt(int, java.lang.String)
	 */
	@Override
	public void setCurrentParameterAt(int i, String parameter) {
		currentParameterValues[i] = parameter;
		notifyListeners();
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.processing.AlgorithmInterface#resetDefaults()
	 */
	@Override
	public void resetDefaults() {
		setCurrentParameters(defaultParameterValues);
		notifyListenersReset();
	}

	private void notifyListenersReset() {
		for (AlgorithmChangeListener l : listeners) {
			l.parametersReset();
		}
	}

	public int getID() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see amuse.scheduler.gui.algorithm.AlgorithmInterface#getCategory()
	 */
	@Override
	public String getCategory() {
		return this.category;
	}

	public String getIdAndParameterStr() {
		String algorithmStr = getID() + "";
		String[] allowedParameterStrings = getAllowedParamerterStrings();
    	String[] currentParameterValues =  getCurrentParameterValues();
    	String[] modifiedParameterValues = new String[currentParameterValues.length];
    	if(currentParameterValues.length > 0) {
    		algorithmStr += "[";
	    	for(int i = 0; i < currentParameterValues.length; i++){
	    		if(allowedParameterStrings[i].equals("fof") || allowedParameterStrings[i].equals("f") || allowedParameterStrings[i].equals("s") || allowedParameterStrings[i].equals("c")){
	    			modifiedParameterValues[i] = "|" + currentParameterValues[i] + "|";
	    		} else {
	    			modifiedParameterValues[i] = currentParameterValues[i];
	    		}
	    	}
	    	for(int i = 0; i < modifiedParameterValues.length; i++) {
	    		algorithmStr += modifiedParameterValues[i];
	    		if(i < modifiedParameterValues.length -1) {
	    			algorithmStr += "_";
	    		}
	    	}
	    	algorithmStr += "]";
    	}
		return algorithmStr;
	}
	
	public boolean supportsBinary() {
		return supportsBinary;
	}
	
	public boolean supportsContinuous() {
		return supportsContinuous;
	}
	
	public boolean supportsMulticlass() {
		return supportsMulticlass;
	}
	
	public boolean supportsMultilabel() {
		return supportsMultilabel;
	}
	
	public boolean supportsSinglelabel() {
		return supportsSinglelabel;
	}
	
	public boolean supportsSupervised() {
		return supportsSupervised;
	}
	
	public boolean supportsUnsupervised() {
		return supportsUnsupervised;
	}
	
	public boolean supportsRegression() {
		return supportsRegression;
	}
}
