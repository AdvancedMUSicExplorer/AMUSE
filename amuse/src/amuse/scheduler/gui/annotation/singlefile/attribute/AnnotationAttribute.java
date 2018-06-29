package amuse.scheduler.gui.annotation.singlefile.attribute;

import javax.swing.DefaultListModel;
/**
 * Holds the annotated entries for one attribute
 * @author Frederik Heerde
 * @version $Id$
 */
public abstract class AnnotationAttribute<T>{

	private String name;
	private int id;
	protected AnnotationAttributeType type;
	protected DefaultListModel<AnnotationAttributeEntry<T>> entryList;
	
	public AnnotationAttribute(String pName, int pId){
		name = pName;
		id = pId;
		entryList = new DefaultListModel<AnnotationAttributeEntry<T>>();
	}
	
	/**
	 * Inserts a new AnnotationAttributeEntry to this attribute's entryList at the proper place to keep them sorted by start time.
	 * @param start - The point in time where the new AnnotationAttributeEntry starts
	 * @param end - The point in time where the new AnnotationAttributeEntry ends
	 * @param value . The actual value of the new AnnotationAttributeEntry
	 * @return The added AnnotationAttributeEntry
	 */
	protected AnnotationAttributeEntry<T> addEntry(double start, double end, T value){
		AnnotationAttributeEntry<T> annotationAttributeEntry = new AnnotationAttributeEntry<T>(this, start, end, value);
		
		// Insert element at the correct position
		int index = entryList.size();
		for(int i = 0; i < entryList.size(); i++){
			if(entryList.getElementAt(i).getStart() > start){
				index = i;
				break;
			}
		}
		entryList.add(index, annotationAttributeEntry);
		return annotationAttributeEntry;
	}
	
	protected AnnotationAttributeEntry<T> addEntry(AnnotationAttributeEntry<T> entry){
		entry.setAnnotationAttribute(this);
		int index = entryList.size();
		for(int i = 0; i < entryList.size(); i++){
			if(entryList.getElementAt(i).getStart() > entry.getStart()){
				index = i;
				break;
			}
		}
		entryList.add(index, entry);
		return entry;
	}
	
	/**
	 * This method can be used after the start of attEntry was changed to ensure, that the entryList is sorted by start time.
	 * @param attEntry The AnnotationAttributeEntry whose start time was changed
	 */
	public void sortEntryAfterChange(AnnotationAttributeEntry<T> attEntry){
		int index = entryList.indexOf(attEntry);
		while(entryList.getElementAt(Math.min(index + 1, entryList.size() - 1)).getStart() < attEntry.getStart()){
			AnnotationAttributeEntry<T> smallerEntry = entryList.getElementAt(index + 1);
			entryList.set(index, smallerEntry);
			entryList.set(index + 1, attEntry);
			index++;
		}
		while(entryList.getElementAt(Math.max(index - 1, 0)).getStart() > attEntry.getStart()){
			AnnotationAttributeEntry<T> biggerValue = entryList.getElementAt(index - 1);
			entryList.set(index, biggerValue);
			entryList.set(index - 1, attEntry);
			index--;
		}
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
	
	public DefaultListModel<AnnotationAttributeEntry<T>> getEntryList(){
		return entryList;
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
