package amuse.nodes.annotation;

/**
 * AnnotationAttribute of type EVENT
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationEventAttribute extends AnnotationAttribute<Integer>{

	public AnnotationEventAttribute(String pName, int id) {
		super(pName, id);
		type = AnnotationAttributeType.EVENT;
	}

}
