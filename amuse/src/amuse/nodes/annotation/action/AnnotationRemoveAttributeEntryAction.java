package amuse.nodes.annotation.action;

import amuse.nodes.annotation.attribute.AnnotationAttributeEntry;
import amuse.scheduler.gui.controller.AnnotationController;

public class AnnotationRemoveAttributeEntryAction extends AnnotationAction{

	AnnotationAttributeEntry<?> entry;
	public AnnotationRemoveAttributeEntryAction(AnnotationController annotationController, AnnotationAttributeEntry<?> entry) {
		super(annotationController);
		this.entry = entry;
	}
	
	@Override
	public void undo() {
		annotationController.addEntryToItsAttribute(entry);
	}
	
	@Override
	public String toString(){
		return this.getClass().getName() + ": " + entry.toString();
	}

	@Override
	public AnnotationAction getRedoAction() {
		return new AnnotationAddAttributeEntryAction(annotationController, entry);
	}

}