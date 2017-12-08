package amuse.nodes.annotation;

import javax.swing.DefaultListModel;

/**
 * AnnotationAttribute of type NOMINAL
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationNominalAttribute extends AnnotationAttribute<String>{

	DefaultListModel<String> allowedValues; // The List of the values, that are allowed to use in an attribute value
	
	public AnnotationNominalAttribute(String pName, int id) {
		super(pName, id);
		type = AnnotationAttributeType.NOMINAL;
		allowedValues = new DefaultListModel<String>();
	}
	
	public void addAllowedValue(String allowedValue){
		if(!allowedValues.contains(allowedValue)){
			allowedValues.addElement(allowedValue);
		}
	}
	
	/**
	 * Removes a string from the allowedValues list. 
	 * This method also deletes every attribute value that contained this string
	 * @param allowedValueToRemove - The String that should be removed from the allowedValues list
	 */
	public void removeAllowedValue(String allowedValueToRemove){
		allowedValues.removeElement(allowedValueToRemove);
		for(int i = 0; i < valueList.size(); i++){
			AnnotationAttributeValue<String> value = valueList.getElementAt(i);
			if(value.getValue().equals(allowedValueToRemove)){
				valueList.removeElement(value);
			}
		}
	}
	
	/**
	 * Inserts a new AnnotationAttributeValue to this attribute's valueList at the proper place to keep them sorted by start time.
	 * The AnnotationAttributeValue is only added, when its value is in the allowedValues list.
	 * @param start - The point in time where the new AnnotationAttributeValue starts
	 * @param end - The point in time where the new AnnotationAttributeValue ends
	 * @param value . The actual value of the new AnnotationAttributeValue
	 * @return The added AnnotationAttributeValue
	 */
	@Override
	public AnnotationAttributeValue<String> addValue(int start, int end, String value){
		if(allowedValues.contains(value)){
			return super.addValue(start, end, value);
		}
		return null;
	}
	
	/**
	 * Alters one entry in the allowedValues list and the corresponding value in the AnnotationAttributeValues that are held by this object
	 * @param oldAllowedValue
	 * @param newAllowedValue
	 */
	public void editAllowedValue(String oldAllowedValue, String newAllowedValue) {
		allowedValues.set(allowedValues.lastIndexOf(oldAllowedValue), newAllowedValue);
		for(int i = 0; i < valueList.size(); i++){
			AnnotationAttributeValue<String> value = valueList.getElementAt(i);
			if(value.getValue().equals(oldAllowedValue)){
				value.setValue(newAllowedValue);
			}
		}
	}
	
	public DefaultListModel<String> getAllowedValues(){
		return allowedValues;
	}
}