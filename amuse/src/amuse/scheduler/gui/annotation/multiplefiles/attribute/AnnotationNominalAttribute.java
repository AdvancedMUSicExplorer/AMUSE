package amuse.scheduler.gui.annotation.multiplefiles.attribute;

import javax.swing.DefaultListModel;

/**
 * AnnotationAttribute of type NOMINAL
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationNominalAttribute extends AnnotationAttribute<String>{

	private DefaultListModel<String> allowedValues; // The List of the values, that are allowed to use in an attribute value
	
	public AnnotationNominalAttribute(String name, DefaultListModel<String> allowedValues) {
		this(name);
		this.allowedValues = allowedValues;
	}
	
	public AnnotationNominalAttribute(String name) {
		super(name);
		type = AnnotationAttributeType.NOMINAL;
		allowedValues = new DefaultListModel<String>();
	}
	
	public void addAllowedValue(String allowedValue){
		if(!allowedValues.contains(allowedValue)){
			allowedValues.addElement(allowedValue);
		}
	}
	
	public DefaultListModel<String> getAllowedValues(){
		return allowedValues;
	}

	@Override
	public AnnotationAttribute<String> newInstance() {
		return new AnnotationNominalAttribute(getName(), allowedValues);
	}
}