package amuse.scheduler.gui.annotation.singlefile.attribute;

/**
 * Annotation Attribute of type STRING
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationStringAttribute extends AnnotationAttribute<String>{

	public AnnotationStringAttribute(String pName, int id) {
		super(pName, id);
		type = AnnotationAttributeType.STRING;
	}

	@Override
	public AnnotationAttribute<String> newInstance() {
		return new AnnotationStringAttribute(getName(), getId());
	}

}
