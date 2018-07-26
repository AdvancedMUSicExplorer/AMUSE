package amuse.scheduler.gui.controller;

import java.io.File;
import java.util.LinkedHashMap;

import javax.swing.RowFilter;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.scheduler.gui.annotation.MusicPlayerModel;
import amuse.scheduler.gui.annotation.multiplefiles.AnnotationIO;
import amuse.scheduler.gui.annotation.multiplefiles.AnnotationView;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttributeType;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.media.MediaPlayer.Status;

public class MultipleFilesAnnotationController extends AbstractController{
	
	private WizardController wizardController;
	private MusicPlayerModel musicPlayerModel;
	private AnnotationView annotationView;
	private AnnotationIO annotationIO;
	
	public MultipleFilesAnnotationController(WizardController wc){
		wizardController = wc;
		annotationIO = new AnnotationIO(this);
		annotationView = new AnnotationView(this);
		musicPlayerModel = new MusicPlayerModel(annotationView.getMusicPlayerView().getCurrentTimeListener(), 
				annotationView.getMusicPlayerView().getStatusListener());
		
	}
	
	public void loadMusic(String pPath){
		musicPlayerModel.load(pPath);
		annotationView.getMusicPlayerView().repaint();
	}
	
	public void showAddAttributeDialog(String title, AnnotationAttributeType... validTypesToShow){
		annotationView.getTableControlView().showAddAttributeDialog(title, validTypesToShow);
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
	public AnnotationView getView() {
		return annotationView;
	}
	
	public LinkedHashMap<Integer, AnnotationAttribute<?>> getAnnotationAttributeTable(){
		return annotationIO.getAnnotationAttributeTable();
	}
	
	public void repaintTable(){
		annotationView.getTableView().repaint();
	}

	public void playMusic() {
		musicPlayerModel.play();
	}

	public void pauseMusic() {
		musicPlayerModel.pause();
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
	
	public String getMusicAbsoulutePath(){
		return musicPlayerModel.getMusicFilePath();
	}
	
	public boolean isMusicPaused(){
		return musicPlayerModel.isPaused();
	}
	
	public ReadOnlyObjectProperty<Status> getMusicPlayerStatusProperty(){
		return musicPlayerModel.getStatusProperty();
	}

	public int getNextAvailableId() {
		return annotationIO.getNextAvailableId();
	}
	
	public boolean isAttributeNameAvailable(String name){
		return annotationIO.isAttributeNameAvailable(name);
	}

	public void clearAnnotation() {
		annotationView.getTableView().clearAnnotation();
	}
	
	public DefaultTableModel getTableModel(){
		return annotationView.getTableView().getTableModel();
	}
	
	public TableColumnModel getColumnModel(){
		return annotationView.getTableView().getColumnModel();
	}

	public void saveAnnotation(String path) {
		annotationIO.saveAnnotation(path);
	}

	public void loadAnnotation(String path) {
		annotationIO.loadAnnotation(path);
	}
	
	public void setRowFilter(RowFilter<DefaultTableModel, Integer> filter){
		annotationView.getTableView().setRowFilter(filter);
	}

	public void addTrack(String absolutePath) {
		annotationView.getTableView().addTrack(absolutePath);
	}
	
	public int getColumnCount(){
		return annotationView.getTableView().getColumnModel().getColumnCount();
	}

	public void addAttribute(AnnotationAttribute<?> att) {
		annotationView.getTableView().addAttribute(att);
	}
	
	public void addNewAttribute(AnnotationAttribute<?> att){
		annotationIO.addNewAttribute(att);
	}
	
	public void removeAttributeFromAnnotationAttributeTable(int id){
		annotationIO.removeAttributeFromAnnotationAttributeTable(id);
	}
	
	public AnnotationAttribute<?> getAttributeFromColumn(int column){
		return annotationView.getTableView().getAttributeFromColumn(column);
	}
	
	public void addRow(Object[] rowData){
		annotationView.getTableView().addRow(rowData);
	}

	public void seekInMusic(double millis) {
		musicPlayerModel.seek(millis);
	}

	public void removeSelectedTracks() {
		annotationView.getTableView().removeSelectedRows();
	}
	
	public void showAbsolutePath(boolean bool){
		annotationView.getTableView().showAbsolutePath(bool);
	}
	

	
}
