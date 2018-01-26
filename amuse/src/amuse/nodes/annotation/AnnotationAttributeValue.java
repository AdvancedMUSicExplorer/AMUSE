package amuse.nodes.annotation;

import javax.swing.DefaultListModel;

/**
 * Holds the start, end and actual value of one annotated instance
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationAttributeValue<T>{
	private int start;
	private int end;
	private T value;
	private AnnotationAttribute<T> annotationAttribute;
	
	public AnnotationAttributeValue(AnnotationAttribute<T> pAnnotationAttribute, int pStart, int pEnd, T pValue){
		annotationAttribute = pAnnotationAttribute;
		start = pStart;
		end = pEnd;
		value = pValue;
	}
	
	public AnnotationAttributeValue<T> getNextValue(){
		DefaultListModel<AnnotationAttributeValue<T>> values = annotationAttribute.getValueList();
		int nextIndex = values.indexOf(this) + 1;
		if(nextIndex >= values.size()){
			return null;
		}
		else{
			return values.get(nextIndex);
		}
	}
	
	public AnnotationAttributeValue<T> getPreviousValue(){
		DefaultListModel<AnnotationAttributeValue<T>> values = annotationAttribute.getValueList();
		int nextIndex = values.indexOf(this) - 1;
		if(nextIndex < 0){
			return null;
		}
		else{
			return values.get(nextIndex);
		}
	}
	
	public AnnotationAttribute<T> getAnnotationAttribute(){
		return annotationAttribute;
	}
	
	public int getStart(){
		return start;
	}
	
	public int getEnd(){
		return end;
	}
	
	public int getDuration(){
		return end - start;
	}
	
	public T getValue(){
		return value;
	}
	
	public void setStart(int newStart){
		start = newStart;
		annotationAttribute.sortValueAfterChange(this);
	}
	
	public void setEnd(int newEnd){
		end = newEnd;
	}
	
	public void setValue(T newValue){
		value = newValue;
	}
	
	@Override
	public String toString(){
		// Event attributes are represented by the time stamp only
		if(annotationAttribute.getType() == AnnotationAttributeType.EVENT){ 
			return String.format("%02d:%02d,%03d", start / 60000, (start % 60000) / 1000, (start % 1000));
		}
		
		// Other attributes than event attributes are represented by the start time, end time and value.
		return String.format("%02d:%02d,%03d - %02d:%02d,%03d: %s", start / 60000, (start % 60000) / 1000, (start % 1000),
															 end / 60000, (end % 60000) / 1000, (end % 1000), value + "");
	}
}