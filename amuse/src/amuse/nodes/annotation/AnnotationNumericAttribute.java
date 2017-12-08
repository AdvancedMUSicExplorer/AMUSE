package amuse.nodes.annotation;

/**
 * AnnotationAttribute of type NUMERIC
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationNumericAttribute extends AnnotationAttribute<Double>{

	public AnnotationNumericAttribute(String pName, int id) {
		super(pName, id);
		type = AnnotationAttributeType.NUMERIC;
	}

}
