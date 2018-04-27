package amuse.nodes.annotation.action;

import amuse.nodes.annotation.attribute.AnnotationAttribute;
import amuse.scheduler.gui.controller.AnnotationController;

public class AnnotationRemoveAttributeAction extends AnnotationAction{
	
	AnnotationAttribute<?> att;
	int index;
	
	public AnnotationRemoveAttributeAction(AnnotationController annotationController, AnnotationAttribute<?> att, int index) {
		super(annotationController);
		this.att = att;
		this.index = index;
	}
	@Override
	public void undo() {
		annotationController.getAttributeListModel().add(index, att);
		
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
