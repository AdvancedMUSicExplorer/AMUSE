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
 * Creation date: 03.06.2019
 */
package amuse.data;

import java.io.IOException;

/*
 * @author Philipp Ginsel
 */
public class ModelType {
	public enum RelationshipType {BINARY, CONTINUOUS};
	public enum LabelType {MULTICLASS, MULTILABEL, SINGLELABEL};
	public enum MethodType {SUPERVISED, UNSUPERVISED, REGRESSION};
	
	private final RelationshipType relationshipType;
	private final LabelType labelType;
	private final MethodType methodType;
	
	public ModelType(RelationshipType relationshipType, LabelType labelType, MethodType methodType) throws IOException {
		this.relationshipType = relationshipType;
		this.labelType = labelType;
		this.methodType = methodType;
		
		if(labelType == LabelType.MULTICLASS && relationshipType == RelationshipType.CONTINUOUS) {
			throw new IOException("Continuos multiclass classification is not possible.");
		}
		
		if(methodType == MethodType.REGRESSION && relationshipType == RelationshipType.BINARY) {
			throw new IOException("Binary regression is not possible.");
		}
	}
	
	public RelationshipType getRelationshipType() {
		return relationshipType;
	}
	
	public LabelType getLabelType() {
		return labelType;
	}
	
	public MethodType getMethodType() {
		return methodType;
	}
}
