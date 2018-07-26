package amuse.scheduler.gui.annotation.singlefile.action;

import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttribute;
import amuse.scheduler.gui.controller.SingleFileAnnotationController;

public class AnnotationRemoveAttributeAction extends AnnotationAction{
	
	AnnotationAttribute<?> att;
	int index;
	
	public AnnotationRemoveAttributeAction(SingleFileAnnotationController annotationController, AnnotationAttribute<?> att, int index) {
		super(annotationController);
		this.att = att;
		this.index = index;
	}
	@Override
	public void undo() {
		annotationController.insertAttribute(index, att);
		
	}
	
	@Override
	public String toString(){
		return this.getClass().getName() + ": " + att.toString();
	}
	@Override
	public AnnotationAction getRedoAction() {
		return new AnnotationAddAttributeAction(annotationController, att);
	}
}
