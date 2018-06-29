package amuse.scheduler.gui.annotation.singlefile.attribute;

/**
 * AnnotationAttribute of type EVENT
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationEventAttribute extends AnnotationAttribute<Double>{

	public AnnotationEventAttribute(String pName, int id) {
		super(pName, id);
		type = AnnotationAttributeType.EVENT;
	}

	@Override
	public AnnotationAttribute<Double> newInstance() {
		return new AnnotationEventAttribute(getName(), getId());
	}

}
