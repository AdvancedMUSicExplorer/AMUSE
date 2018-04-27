package amuse.nodes.annotation;

import java.util.LinkedList;

import amuse.nodes.annotation.action.AnnotationAction;

public class UndoRedoManager {
	private LinkedList<AnnotationAction> undoList;
	private LinkedList<AnnotationAction> redoList;
	
	private static final int capacity = 10;
	
	
	public UndoRedoManager(){
		undoList = new LinkedList<AnnotationAction>();
		redoList = new LinkedList<AnnotationAction>();
	}
	
	public void undo(){
		if(!undoList.isEmpty()){
			AnnotationAction action = undoList.removeFirst();
			redoList.addFirst(action.getRedoAction());
			//System.out.println("undo " + action);
			action.undo();
		}
	}
	
	public void redo(){
		if(!redoList.isEmpty()){
			AnnotationAction action = redoList.removeFirst();
			undoList.addFirst(action.getRedoAction());
			//System.out.println("redo " + action);
			action.undo();
		}
	}
	
	public void addAction(AnnotationAction action){
		redoList = new LinkedList<AnnotationAction>();
		if(undoList.size() >= capacity){
			undoList.removeLast();
		}
		undoList.addFirst(action);
		//System.out.println(action);
	}

	public boolean isRedoable() {
		return !redoList.isEmpty();
	}

	public void clearHistory() {
		undoList = new LinkedList<AnnotationAction>();
		redoList = new LinkedList<AnnotationAction>();
	}
	
	@Override
	public String toString(){
		String output = this.getClass().getName() + ":\nUndoList:\n";
		for(AnnotationAction action: undoList){
			output += action + "\n";
		}
		output += "\nRedoList:";
		for(AnnotationAction action: redoList){
			output += "\n" + action;
		}
		
		return output;
	}
}
