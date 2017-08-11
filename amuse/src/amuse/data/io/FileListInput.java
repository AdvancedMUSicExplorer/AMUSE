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
 * Creation date: 05.06.2010
 */ 
package amuse.data.io;

import java.io.File;
import java.util.List;

/**
 * AMUSE input which is saved in a list of files
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class FileListInput implements DataInputInterface {
	
	/** For Serializable interface */
	private static final long serialVersionUID = 3783241616958276246L;

	List<File> inputFiles;
	
	List<Integer> inputFileIds;
	
	/**
	 * Standard constructor
	 */
	public FileListInput(List<File> inputFiles, List<Integer> inputFileIds) {
		this.inputFiles = inputFiles;
		this.inputFileIds = inputFileIds;
	}
	

	@Override
	public String toString() {
		return "List of " + inputFiles.size() + " files";
	}


	/**
	 * @return the inputFiles
	 */
	public List<File> getInputFiles() {
		return inputFiles;
	}


	/**
	 * @return the inputFileIds
	 */
	public List<Integer> getInputFileIds() {
		return inputFileIds;
	}
	
	

}
