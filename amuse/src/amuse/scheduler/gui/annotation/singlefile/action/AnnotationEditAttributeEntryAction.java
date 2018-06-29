package amuse.scheduler.gui.annotation.singlefile.action;

import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttributeEntry;
import amuse.scheduler.gui.controller.SingleFileAnnotationController;

public class AnnotationEditAttributeEntryAction extends AnnotationAction{

	private AnnotationAttributeEntry<?> entry;
	private double oldStart, oldEnd;
	private Object oldValue;
	
	public AnnotationEditAttributeEntryAction(SingleFileAnnotationController annotationController, AnnotationAttributeEntry<?> entry, double oldStart, double oldEnd, Object oldValue) {
		super(annotationController);
		this.entry = entry;
		this.oldStart = oldStart;
		this.oldEnd = oldEnd;
		this.oldValue = oldValue;
	}
	
	@Override
	public void undo() {
		entry.setEnd(oldEnd);
		((AnnotationAttributeEntry<Object>) entry).setValue(oldValue);
		entry.setStart(oldStart);
		annotationController.getView().repaint();
	}
	
	@Override
	public String toString(){
		return this.getClass().getName() + ": " + entry.toString();
	}

	@Override
	public AnnotationAction getRedoAction() {
		return new AnnotationEditAttributeEntryAction(annotationController, entry, entry.getStart(), entry.getEnd(), entry.getValue());
	}
}