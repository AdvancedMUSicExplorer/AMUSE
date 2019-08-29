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
 * Creation date: 27.04.2009
 */

package amuse.scheduler.gui.algorithm;

/**
 *
 * @author Clemens Waeltken
 */
public interface AlgorithmInterface{

    String getName();

    String getDescription();
    
    String getCategory();

    String[] getAllowedParamerterStrings();

    String[] getCurrentParameterValues();

    String[] getParameterNames();

    String[] getParameterDescriptions();

    String[] getDefaultParameters();

    void setCurrentParameters(String[] parameters);
    
    void setCurrentParameterAt(int i, String parameter);
    
    void addAlgorithmChangeListener(AlgorithmChangeListener listener);
    
    void removeAlgoritmChangeListener(AlgorithmChangeListener listener);

	/**
	 * 
	 */
	void resetDefaults();
	
	public boolean supportsBinary();
	
	public boolean supportsContinuous();
	
	public boolean supportsMulticlass();
	
	public boolean supportsMultilabel();
	
	public boolean supportsSinglelabel();
	
	public boolean supportsSupervised();
	
	public boolean supportsUnsupervised();
	
	public boolean supportsRegression();
}
