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
 * Creation date: 16.01.2008
 */
package amuse.nodes.validator.interfaces;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;


/**
 * Validation methods should implement this interface.
 *  
 * @author Igor Vatolkin
 * @version $Id: ValidatorInterface.java 241 2018-07-26 12:35:24Z frederik-h $
 */
public interface ValidatorInterface {
	
	public void validate() throws NodeException;
	
	/** Calculates list of all files with processed features which are used for training and validation.
	 * This list is required for data reduction measures */
	public ArrayList<String> calculateListOfUsedProcessedFeatureFiles() throws NodeException;

}
