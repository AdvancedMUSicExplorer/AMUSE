package amuse.scheduler.gui.controller;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;

import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.scheduler.gui.annotation.MusicPlayerModel;
import amuse.scheduler.gui.annotation.singlefile.AnnotationView;
import amuse.scheduler.gui.annotation.singlefile.AudioSpectrumModel;
import amuse.scheduler.gui.annotation.singlefile.UndoRedoManager;
import amuse.scheduler.gui.annotation.singlefile.action.AnnotationAction;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttribute;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttributeEntry;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationModel;

/**
 * Controller to connect the different model and view components for annotation purposes
 * @author Frederik Heerde
 * @version $Id$
 */
public class SingleFileAnnotationController extends AbstractController{
	
	private WizardController wizardController;
	private AnnotationView annotationView;
	private AudioSpectrumModel audioSpectrumModel;
	private AnnotationModel annotationModel;
	private MusicPlayerModel musicPlayerModel;
	private UndoRedoManager undoRedoManager;
	
	public SingleFileAnnotationController(WizardController wc){
		wizardController = wc;
		annotationModel = new AnnotationModel(this);
		musicPlayerModel = new MusicPlayerModel(
				(value, oldDuration, newDuration) -> {
					annotationView.setCurrentTime(newDuration.toMillis());
					annotationView.getAnnotationAudioSpectrumPanel().repaintCurrentTimeBeam();
				},
				(observable, oldValue, newValue) -> annotationView.getAnnotationUserInterfacePanel().refreshButtonPlayPauseIcon());
		undoRedoManager = new UndoRedoManager();
		annotationView = new AnnotationView(this);
		
	}
	
	public void loadMusic(String pPath){
		try {
			audioSpectrumModel = new AudioSpectrumModel(pPath);
			annotationView.getAnnotationAudioSpectrumPanel().setAudioSpectrumImage(audioSpectrumModel.getAudiospectrumImage());
			musicPlayerModel.load(pPath);
			annotationModel.loadAnnotation();
			annotationView.resizePanels();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addUndoableAction(AnnotationAction action){
		undoRedoManager.addAction(action);
	}
	
	public boolean isAttributeNameAvailable(String name){
		return annotationModel.isAttributeNameAvailable(name);
	}
	
	public boolean isAttributeIdAvailable(String id){
		return annotationModel.isAttributeIdAvailable(id);
	}
	
	public void removeAttribute(AnnotationAttribute<?> att){
		for(int i = 0; i < att.getEntryList().size(); i++){
			annotationView.getAnnotationVisualizationPanel().removeEntryPanel(att.getEntryList().getElementAt(i));
		}
		annotationModel.removeAttribute(att);
		annotationView.resizePanels();
	}
	
	public void deleteAttributeFile(AnnotationAttribute<?> att){
		this.removeAttribute(att);
		annotationModel.deleteAttributeFile(att);
	}
	
	public AnnotationAttribute<?> addAttribute(int id) {
		AnnotationAttribute<?> att = annotationModel.addAttribute(id);
		for(int i = 0; i < att.getEntryList().size(); i++){
			annotationView.getAnnotationVisualizationPanel().addEntryPanel(att.getEntryList().getElementAt(i));
		}
		return att;
	}
	
	public AnnotationAttribute<?> insertAttribute(int index, AnnotationAttribute<?> att){
		annotationModel.getListModel().add(index, att);
		for(int i = 0; i < att.getEntryList().size(); i++){
			annotationView.getAnnotationVisualizationPanel().addEntryPanel(att.getEntryList().getElementAt(i));
		}
		return att;
	}
	
	public void addEntryToItsAttribute(AnnotationAttributeEntry<?> entry){
		annotationModel.addEntryToItsAttribute(entry);
		annotationView.getAnnotationVisualizationPanel().addEntryPanel(entry);
	}
	
	public AnnotationAttributeEntry<?> addNewEntryToAttribute(AnnotationAttribute<?> annotationAttribute){
		AnnotationAttributeEntry<?> entry = annotationModel.addNewValueToAttribute(annotationAttribute);
		annotationView.getAnnotationVisualizationPanel().addEntryPanel(entry);
		return entry;
	}
	
	public void removeEntry(AnnotationAttributeEntry<?> entry){
		annotationView.getAnnotationVisualizationPanel().removeEntryPanel(entry);
		entry.getAnnotationAttribute().getEntryList().removeElement(entry);
	}
	
	public UndoRedoManager getUndoRedoManager(){
		return undoRedoManager;
	}

	@Override
	public void saveTask(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadTask(DataSetAbstract dataSet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadTask(TaskConfiguration conf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TaskConfiguration getExperimentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void addNewAttribute(AnnotationAttribute<?> att) {
		annotationModel.addNewAttribute(att);
	}

	@Override
	public JComponent getView() {
		return annotationView;
	}

	public void playMusic() {
		musicPlayerModel.play();
	}

	public void pauseMusic() {
		musicPlayerModel.pause();
		annotationView.getAnnotationCurrentTimePanel().setCurrentTime(getCurrentMs());
	}
	
	public double getCurrentMs(){
		return musicPlayerModel.getCurrentMs();
	}
	
	public double getDurationInMs(){
		return musicPlayerModel.getDurationInMs();
	}
	
	public double getDurationInSecs(){
		return musicPlayerModel.getDurationInMs() / 1000.;
	}
	
	public LinkedHashMap<Integer, AnnotationAttribute<?>> getAnnotationAttributeTable(){
		return annotationModel.getAnnotationAttributeTable();
	}
	
	public String getMusicFilePath(){
		return musicPlayerModel.getMusicFilePath();
	}

	public DefaultListModel<AnnotationAttribute<?>> getAttributeListModel() {
		return annotationModel.getListModel();
	}
	
	public float getSampleRate(){
		return audioSpectrumModel == null? 44100.f: audioSpectrumModel.getSampleRate();
	}

	public boolean isMusicPaused() {
		return musicPlayerModel.isPaused();
	}

	public void seekInMusic(double timeInMs) {
		musicPlayerModel.seek(timeInMs);
	}
	
	public void selectAttributeEntry(AnnotationAttributeEntry<?> entry){
		annotationView.getAnnotationSelectionPanel().selectAttributeEntry(entry);
	}
	
	public void scrollToTime(int millis){
		annotationView.getAnnotationAudioSpectrumPanel().scrollToTime(millis);
	}
	
	public void selectAttributeWithEntry(int attributeNumber, double secs) {
		annotationView.getAnnotationSelectionPanel().selectAttributeWithEntry(attributeNumber, secs);
	}
	
	public void clearAnnotation(){
		annotationModel.clearAnnotation();
		annotationView.getAnnotationVisualizationPanel().clearAnnotation();
		annotationView.getAnnotationSelectionPanel().clearAnnotation();
	}
	
	public void saveAnnotation(){ 
		annotationModel.saveAnnotation();
	}

	public int getNextAvailableId() {
		return annotationModel.getNextAvailableId();
	}

	public JList<AnnotationAttributeEntry<?>> getAttributeEntryList(){
		return annotationView.getAnnotationSelectionPanel().getAttributeEntryList();
	}
	
}
