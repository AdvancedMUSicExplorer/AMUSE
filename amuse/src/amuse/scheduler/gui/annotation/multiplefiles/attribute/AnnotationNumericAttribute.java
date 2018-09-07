package amuse.scheduler.gui.annotation.multiplefiles.attribute;

/**
 * AnnotationAttribute of type NUMERIC
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationNumericAttribute extends AnnotationAttribute<Double>{

	public AnnotationNumericAttribute(String pName) {
		super(pName);
		type = AnnotationAttributeType.NUMERIC;
	}

	@Override
	public AnnotationAttribute<Double> newInstance() {
		return new AnnotationNumericAttribute(getName());
	}

}
