package amuse.scheduler.gui.annotation.multiplefiles.attribute;
/**
 * Holds the annotated entries for one attribute
 * @author Frederik Heerde
 * @version $Id$
 */
public abstract class AnnotationAttribute<T>{

	private String name;
	private int id;
	protected AnnotationAttributeType type;
	
	public AnnotationAttribute(String pName, int pId){
		name = pName;
		id = pId;
	}
	
	public AnnotationAttributeType getType(){
		return type;
	}
	
	protected void setName(String pName){
		name = pName;
	}
	
	public String getName(){
		return name;
	}
	
	public int getId(){
		return id;
	}

	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof AnnotationAttribute){
			return name.equals(((AnnotationAttribute<?>)obj).getName());
		}
		return false;
	}
	
	public abstract AnnotationAttribute<T> newInstance();
}

