package amuse.nodes.annotation.action;

import amuse.nodes.annotation.attribute.AnnotationAttributeEntry;
import amuse.scheduler.gui.controller.AnnotationController;

public class AnnotationAddAttributeEntryAction  extends AnnotationAction{

	private AnnotationAttributeEntry<?> entry;
	
	public AnnotationAddAttributeEntryAction(AnnotationController annotationController, AnnotationAttributeEntry<?> entry) {
		super(annotationController);
		this.entry = entry;
	}
	
	@Override
	public void undo() {
		annotationController.removeEntry(entry);
	}
	
	@Override
	public String toString(){
		return this.getClass().getName() + ": " + entry.toString();
	}

	@Override
	public AnnotationAction getRedoAction() {
		return new AnnotationRemoveAttributeEntryAction(annotationController, entry);
	}

}
