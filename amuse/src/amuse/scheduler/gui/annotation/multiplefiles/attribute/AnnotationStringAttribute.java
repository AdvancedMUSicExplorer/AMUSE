package amuse.scheduler.gui.annotation.multiplefiles.attribute;

/**
 * Annotation Attribute of type STRING
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationStringAttribute extends AnnotationAttribute<String>{

	public AnnotationStringAttribute(String pName) {
		super(pName);
		type = AnnotationAttributeType.STRING;
	}

	@Override
	public AnnotationAttribute<String> newInstance() {
		return new AnnotationStringAttribute(getName());
	}

}
