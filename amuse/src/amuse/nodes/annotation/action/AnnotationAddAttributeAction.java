package amuse.nodes.annotation.action;

import amuse.nodes.annotation.attribute.AnnotationAttribute;
import amuse.scheduler.gui.controller.AnnotationController;

public class AnnotationAddAttributeAction extends AnnotationAction{

	AnnotationAttribute<?> att;
	public AnnotationAddAttributeAction(AnnotationController annotationController, AnnotationAttribute<?> att) {
		super(annotationController);
		this.att = att;
	}
	
	@Override
	public void undo() {
		annotationController.removeAttribute(att);
	}
	
	@Override
	public String toString(){
		return this.getClass().getName() + ": " + att.toString();
	}

	@Override
	public AnnotationAction getRedoAction() {
		return new AnnotationRemoveAttributeAction(annotationController, att, annotationController.getAttributeListModel().indexOf(att));
	}

}
