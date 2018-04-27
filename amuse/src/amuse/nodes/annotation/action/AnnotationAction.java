package amuse.nodes.annotation.action;

import amuse.scheduler.gui.controller.AnnotationController;

public abstract class AnnotationAction{
	
	AnnotationController annotationController;
	
	public AnnotationAction(AnnotationController annotationController){
		this.annotationController = annotationController;
	}
	public abstract void undo();
	
	public abstract AnnotationAction getRedoAction();
}
