package amuse.scheduler.gui.training;

import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;

public interface ModelTypeListener {
	public void updateModelType(RelationshipType relationshipType, LabelType labelType, MethodType methodType);
}
