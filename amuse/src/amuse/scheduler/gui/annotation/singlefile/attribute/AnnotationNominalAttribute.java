package amuse.scheduler.gui.annotation.singlefile.attribute;

import javax.swing.DefaultListModel;

/**
 * AnnotationAttribute of type NOMINAL
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationNominalAttribute extends AnnotationAttribute<String>{

	private DefaultListModel<String> allowedValues; // The List of the values, that are allowed to use in an attribute value
	
	public AnnotationNominalAttribute(String name, int id, DefaultListModel<String> allowedValues) {
		this(name, id);
		this.allowedValues = allowedValues;
	}
	
	public AnnotationNominalAttribute(String name, int id) {
		super(name, id);
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
		for(int i = 0; i < entryList.size(); i++){
			AnnotationAttributeEntry<String> entry = entryList.getElementAt(i);
			if(entry.getValue().equals(allowedValueToRemove)){
				entryList.removeElement(entry);
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
	public AnnotationAttributeEntry<String> addEntry(double start, double end, String value){
		if(allowedValues.contains(value)){
			return super.addEntry(start, end, value);
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
		for(int i = 0; i < entryList.size(); i++){
			AnnotationAttributeEntry<String> value = entryList.getElementAt(i);
			if(value.getValue().equals(oldAllowedValue)){
				value.setValue(newAllowedValue);
			}
		}
	}
	
	public DefaultListModel<String> getAllowedValues(){
		return allowedValues;
	}

	@Override
	public AnnotationAttribute<String> newInstance() {
		return new AnnotationNominalAttribute(getName(), getId(), allowedValues);
	}
}