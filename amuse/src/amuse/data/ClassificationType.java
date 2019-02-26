/* This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2019 by code authors
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
 * Creation date: 01.11.2019
 */
package amuse.data;

/**
 * Describes the type of the classification
 * 
 * @author Philipp Ginsel
 */

public enum ClassificationType {
	UNSUPERVISED,
	BINARY,
	MULTILABEL,
	MULTICLASS;
	
	public static String[] stringValues(){
		ClassificationType[] values = ClassificationType.values();
		String[] output = new String[values.length];
		for(int i = 0; i < values.length; i++){
			output[i] = values[i].toString();
		}
		return output;
	}
}
