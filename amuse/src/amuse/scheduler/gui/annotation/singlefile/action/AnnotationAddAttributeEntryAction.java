package amuse.scheduler.gui.annotation.singlefile.action;

import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttributeEntry;
import amuse.scheduler.gui.controller.SingleFileAnnotationController;

public class AnnotationAddAttributeEntryAction  extends AnnotationAction{

	private AnnotationAttributeEntry<?> entry;
	
	public AnnotationAddAttributeEntryAction(SingleFileAnnotationController annotationController, AnnotationAttributeEntry<?> entry) {
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
