package amuse.nodes.annotation;

import javax.swing.DefaultListModel;
/**
 * Holds the annotated values for one attribute
 * @author Frederik Heerde
 * @version $Id$
 */
public abstract class AnnotationAttribute<T>{

	private String name;
	private int id;
	protected AnnotationAttributeType type;
	protected DefaultListModel<AnnotationAttributeValue<T>> valueList;
	
	public AnnotationAttribute(String pName, int pId){
		name = pName;
		id = pId;
		valueList = new DefaultListModel<AnnotationAttributeValue<T>>();
	}
	
	/**
	 * Inserts a new AnnotationAttributeValue to this attribute's valueList at the proper place to keep them sorted by start time.
	 * @param start - The point in time where the new AnnotationAttributeValue starts
	 * @param end - The point in time where the new AnnotationAttributeValue ends
	 * @param value . The actual value of the new AnnotationAttributeValue
	 * @return The added AnnotationAttributeValue
	 */
	public AnnotationAttributeValue<T> addValue(int start, int end, T value){
		AnnotationAttributeValue<T> annotationAttributeValue = new AnnotationAttributeValue<T>(this, start, end, value);
		
		// Insert element at the correct position
		int index = valueList.size();
		for(int i = 0; i < valueList.size(); i++){
			if(valueList.getElementAt(i).getStart() > start){
				index = i;
				break;
			}
		}
		valueList.add(index, annotationAttributeValue);
		return annotationAttributeValue;
	}
	
	/**
	 * This method can be used after the start of attValue was changed to ensure, that the valueList is sorted by start time.
	 * @param attValue The AnnotationAttributeValue whose start time was changed
	 */
	public void sortValueAfterChange(AnnotationAttributeValue<T> attValue){
		int index = valueList.indexOf(attValue);
		while(valueList.getElementAt(Math.min(index + 1, valueList.size() - 1)).getStart() < attValue.getStart()){
			AnnotationAttributeValue<T> smallerValue = valueList.getElementAt(index + 1);
			valueList.set(index, smallerValue);
			valueList.set(index + 1, attValue);
			index++;
		}
		while(valueList.getElementAt(Math.max(index - 1, 0)).getStart() > attValue.getStart()){
			AnnotationAttributeValue<T> biggerValue = valueList.getElementAt(index - 1);
			valueList.set(index, biggerValue);
			valueList.set(index - 1, attValue);
			index--;
		}
	}
	
	public AnnotationAttributeType getType(){
		return type;
	}
	
	public void setName(String pName){
		name = pName;
	}
	
	public String getName(){
		return name;
	}
	
	public int getId(){
		return id;
	}
	
	public DefaultListModel<AnnotationAttributeValue<T>> getValueList(){
		return valueList;
	}

	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof AnnotationAttribute){
			return name.equals(((AnnotationAttribute<?>)obj).getName());
		}
		return false;
	}
	
}
