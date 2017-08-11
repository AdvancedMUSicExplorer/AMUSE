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
 * Creation date: 01.06.2010
 */ 
package amuse.data.io;

/**
 * AMUSE data input as DataSet
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class DataSetInput implements DataInputInterface {

	/** For Serializable interface */
	private static final long serialVersionUID = 8746093829906251250L;
	
	DataSet dataSet;
	
	/**
	 * @param dataSet
	 */
	public DataSetInput(DataSet dataSet) {
		super();
		this.dataSet = dataSet;
	}

	@Override
	public String toString() {
		return dataSet.getName();
	}

	/**
	 * @return the dataSet
	 */
	public DataSet getDataSet() {
		return dataSet;
	}
}
