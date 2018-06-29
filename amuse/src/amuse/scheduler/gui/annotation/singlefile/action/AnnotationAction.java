package amuse.scheduler.gui.annotation.singlefile.action;

import amuse.scheduler.gui.controller.SingleFileAnnotationController;

public abstract class AnnotationAction{
	
	SingleFileAnnotationController annotationController;
	
	public AnnotationAction(SingleFileAnnotationController annotationController){
		this.annotationController = annotationController;
	}
	public abstract void undo();
	
	public abstract AnnotationAction getRedoAction();
}
