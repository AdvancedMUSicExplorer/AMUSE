package amuse.nodes.annotation;

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

}
