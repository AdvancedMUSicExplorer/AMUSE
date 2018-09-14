package amuse.scheduler.gui.controller;

import java.io.File;

import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.scheduler.gui.annotation.MusicPlayerModel;
import amuse.scheduler.gui.annotation.multiplefiles.AnnotationIO;
import amuse.scheduler.gui.annotation.multiplefiles.AnnotationView;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttribute;
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

	public void clearAnnotation() {
		annotationView.getTableView().clearAnnotation();
		annotationIO.startNewAnnotation();
		wizardController.getTitleUpdater().setTitleSuffix(annotationView.getCaption());
	}
	
	public DefaultTableModel getTableModel(){
		return annotationView.getTableView().getTableModel();
	}
	
	public TableColumnModel getColumnModel(){
		return annotationView.getTableView().getColumnModel();
	}

	public void saveAnnotation(String path, String dataSetName) {
		annotationIO.saveAnnotation(path, dataSetName);
		wizardController.getTitleUpdater().setTitleSuffix(annotationView.getCaption());
	}

	public void loadAnnotation(String path, String dataSetName) {
		annotationIO.loadAnnotation(path, dataSetName);
		wizardController.getTitleUpdater().setTitleSuffix(annotationView.getCaption());
	}
	
	public String getLoadedDataSetName(){
		return annotationIO.getLoadedDataSetName();
	}
	
	public String getLoadedPath(){
		return annotationIO.getLoadedPath();
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
