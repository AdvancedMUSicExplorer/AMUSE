package amuse.scheduler.gui.annotation.singlefile.attribute;

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

	@Override
	public AnnotationAttribute<Double> newInstance() {
		return new AnnotationNumericAttribute(getName(), getId());
	}

}
