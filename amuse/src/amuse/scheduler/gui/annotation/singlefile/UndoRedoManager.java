package amuse.scheduler.gui.annotation.singlefile;

import java.util.LinkedList;

import javax.swing.JButton;

import amuse.scheduler.gui.annotation.singlefile.action.AnnotationAction;

public class UndoRedoManager {
	private LinkedList<AnnotationAction> undoList;
	private LinkedList<AnnotationAction> redoList;
	private JButton undoButton, redoButton;
	
	private static final int capacity = 10;
	
	
	public UndoRedoManager(){
		undoList = new LinkedList<AnnotationAction>();
		redoList = new LinkedList<AnnotationAction>();
		
		undoButton = new JButton();
		redoButton = new JButton();
		
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
		
		undoButton.addActionListener(e -> undo());
		redoButton.addActionListener(e -> redo());
		
	}
	
	public JButton getUndoButton(){
		return undoButton;
	}
	
	public JButton getRedoButton(){
		return redoButton;
	}
	
	public void undo(){
		if(!undoList.isEmpty()){
			AnnotationAction action = undoList.removeFirst();
			redoList.addFirst(action.getRedoAction());
			action.undo();
			
			undoButton.setEnabled(!undoList.isEmpty());
			redoButton.setEnabled(true);
		}
	}
	
	public void redo(){
		if(!redoList.isEmpty()){
			AnnotationAction action = redoList.removeFirst();
			undoList.addFirst(action.getRedoAction());
			action.undo();
			
			undoButton.setEnabled(!undoList.isEmpty());
			redoButton.setEnabled(!redoList.isEmpty());
		}
	}
	
	public void addAction(AnnotationAction action){
		redoList = new LinkedList<AnnotationAction>();
		if(undoList.size() >= capacity){
			undoList.removeLast();
		}
		undoList.addFirst(action);
		
		undoButton.setEnabled(true);
		redoButton.setEnabled(false);
	}

	public boolean isRedoable() {
		return !redoList.isEmpty();
	}

	public void clearHistory() {
		undoList = new LinkedList<AnnotationAction>();
		redoList = new LinkedList<AnnotationAction>();
		
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
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
