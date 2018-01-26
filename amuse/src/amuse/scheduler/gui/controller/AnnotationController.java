package amuse.scheduler.gui.controller;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.annotation.AnnotationAttribute;
import amuse.nodes.annotation.AnnotationAttributeValue;
import amuse.nodes.annotation.AnnotationModel;
import amuse.nodes.annotation.AudioSpectrumModel;
import amuse.nodes.annotation.MusicPlayerModel;
import amuse.scheduler.gui.annotation.AnnotationView;

/**
 * Controller to connect the different model and view components for annotation purposes
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationController extends AbstractController{
	
	private WizardController wizardController;
	private AnnotationView annotationView;
	private AudioSpectrumModel audioSpectrumModel;
	private AnnotationModel annotationModel;
	private MusicPlayerModel musicPlayerModel;
	
	public AnnotationController(WizardController wc){
		wizardController = wc;
		annotationModel = new AnnotationModel(this);
		musicPlayerModel = new MusicPlayerModel((value, oldDuration, newDuration) -> annotationView.setCurrentTime(newDuration.toMillis()),
				(observable, oldValue, newValue) -> annotationView.getAnnotationUserInterfacePanel().refreshButtonPlayPauseIcon());
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
	
	public boolean isAttributeNameAvailable(String name){
		return annotationModel.isAttributeNameAvailable(name);
	}
	
	public boolean isAttributeIdAvailable(String id){
		return annotationModel.isAttributeIdAvailable(id);
	}
	
	public void removeAttribute(AnnotationAttribute<?> att){
		annotationModel.deleteAttribute(att);
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

	public void addAttribute(int id) {
		annotationModel.addAttribute(id);
	}
	
	public LinkedHashMap<Integer, AnnotationAttribute<?>> getAnnotationAttributeTable(){
		return annotationModel.getAnnotationAttributeTable();
	}
	
	public AnnotationAttributeValue<?> addNewValueToAttribute(AnnotationAttribute<?> annotationAttribute){
		AnnotationAttributeValue<?> value = annotationModel.addNewValueToAttribute(annotationAttribute);
		annotationView.getAnnotationVisualizationPanel().addValuePanel(value);
		return value;
	}
	
	public void removeValue(AnnotationAttributeValue<?> value){
		annotationView.getAnnotationVisualizationPanel().removeValuePanel(value);
		value.getAnnotationAttribute().getValueList().removeElement(value);
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
	
	public void selectAttributeValue(AnnotationAttributeValue<?> value){
		annotationView.getAnnotationSelectionPanel().selectAttributeValue(value);
	}
	
	public void scrollToTime(int millis){
		annotationView.getAnnotationAudioSpectrumPanel().scrollToTime(millis);
	}
	
	public void selectAttributeWithValue(int attributeNumber, double millis) {
		annotationView.getAnnotationSelectionPanel().selectAttributeWithValue(attributeNumber, millis);
	}
	
	public void clearAnnotation(){
		annotationModel.clearAnnotation();
		annotationView.getAnnotationVisualizationPanel().clearAnnotation();
		annotationView.getAnnotationSelectionPanel().clearAnnotation();
	}
	
	public void saveAnnotation(){ 
		annotationModel.saveAnnotation();
	}

	public void addNewAttribute(AnnotationAttribute<?> att) {
		annotationModel.addNewAttribute(att);
	}

	public int getNextAvailableId() {
		return annotationModel.getNextAvailableId();
	}
	
}
