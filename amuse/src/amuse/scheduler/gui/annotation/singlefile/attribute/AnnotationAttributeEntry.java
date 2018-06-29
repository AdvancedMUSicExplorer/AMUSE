package amuse.scheduler.gui.annotation.singlefile.attribute;

import javax.swing.DefaultListModel;

import amuse.scheduler.gui.annotation.singlefile.AnnotationVisualizationPanel.EntryPanel;

/**
 * Holds the start, end and actual value of one annotated instance
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationAttributeEntry<T>{
	private double start;
	private double end;
	private T value;
	private AnnotationAttribute<T> annotationAttribute;
	
	private EntryPanel<T> entryPanel;
	
	public AnnotationAttributeEntry(AnnotationAttribute<T> pAnnotationAttribute, double pStart, double pEnd, T pValue){
		annotationAttribute = pAnnotationAttribute;
		start = pStart;
		end = pEnd;
		value = pValue;
		
		entryPanel = null;
	}
	
	public AnnotationAttributeEntry<T> getNextEntry(){
		DefaultListModel<AnnotationAttributeEntry<T>> entries = annotationAttribute.getEntryList();
		int nextIndex = entries.indexOf(this) + 1;
		if(nextIndex >= entries.size()){
			return null;
		}
		else{
			return entries.get(nextIndex);
		}
	}
	
	public AnnotationAttributeEntry<T> getPreviousEntry(){
		DefaultListModel<AnnotationAttributeEntry<T>> entries = annotationAttribute.getEntryList();
		int nextIndex = entries.indexOf(this) - 1;
		if(nextIndex < 0){
			return null;
		}
		else{
			return entries.get(nextIndex);
		}
	}
	
	public AnnotationAttribute<T> getAnnotationAttribute(){
		return annotationAttribute;
	}
	
	protected void setAnnotationAttribute(AnnotationAttribute<T> att){
		this.annotationAttribute = att;
	}
	
	public double getStart(){
		return start;
	}
	
	public double getEnd(){
		return end;
	}
	
	public double getDuration(){
		return end - start;
	}
	
	public T getValue(){
		return value;
	}
	
	public EntryPanel<T> getEntryPanel(){
		return entryPanel;
	}
	
	public void setEntryPanel(EntryPanel<T> entryPanel){
		this.entryPanel = entryPanel; 
	}
	
	public void setStart(double newStart){
		start = newStart;
		annotationAttribute.sortEntryAfterChange(this);
	}
	
	public void setEnd(double newEnd){
		end = newEnd;
	}
	
	public void setValue(T newValue){
		value = newValue;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof AnnotationAttributeEntry<?>){
			AnnotationAttributeEntry<?> entry = (AnnotationAttributeEntry <?>) o;
			return entry.getAnnotationAttribute().getType() == getAnnotationAttribute().getType()
					&& entry.getStart() == getStart()
					&& entry.getEnd() == getEnd()
					&& entry.getValue() == getValue();
		}
		return false;
	}
	
	@Override
	public String toString(){
		int startInt = (int) start;
		int endInd = (int) end;
		// Event attributes are represented by the time stamp only
		if(annotationAttribute.getType() == AnnotationAttributeType.EVENT){ 
			return String.format("%02d:%02d,%03d", startInt / 60, (startInt % 60), (int)((start - startInt) * 1000));
		}
		
		// Other attributes than event attributes are represented by the start time, end time and value.
		return String.format("%02d:%02d,%03d - %02d:%02d,%03d: %s", startInt / 60, (startInt % 60), (int)((start - startInt) * 1000),
				endInd / 60, (endInd % 60), (int)((end - endInd) * 1000), value + "");
	}
}