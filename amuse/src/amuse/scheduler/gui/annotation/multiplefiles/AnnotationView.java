package amuse.scheduler.gui.annotation.multiplefiles;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import amuse.scheduler.gui.controller.MultipleFilesAnnotationController;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.HasSaveButton;
import net.miginfocom.swing.MigLayout;

/**
 * Assembles the different visual components for annotation purposes
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationView extends JSplitPane implements HasCaption, HasLoadButton, HasSaveButton{
	
	private TableView tableView;
	private MusicPlayerView musicPlayerView;
	private MultipleFilesAnnotationController annotationController;
	private FilterView filterView;
	private TableControlView tableControlView;

	public AnnotationView(MultipleFilesAnnotationController annotationController){
		super(JSplitPane.VERTICAL_SPLIT);

		this.annotationController = annotationController;
		musicPlayerView = new MusicPlayerView(annotationController);
		tableView = new TableView(annotationController);
		filterView = new FilterView(annotationController, tableView.getTableModel(), tableView.getColumnModel());
		tableControlView = new TableControlView(annotationController, tableView.getSelectionModel());
		
		JPanel panel = new JPanel(new MigLayout("wrap 2, fillx"));
		panel.add(filterView, "pushy, growy, w 50%, span 1 2");
		panel.add(musicPlayerView, "w 50%");
		panel.add(tableControlView, "w 50%");
		
		this.setTopComponent(panel);
		this.setBottomComponent(tableView);
		
		this.setResizeWeight(0.3);
		
		
		// To set the initial divider location, the JSplitPane must be visible.
		this.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				setDividerLocation(0.3);
				removeComponentListener(this); //The divider location must only be set once
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {}
			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
	}
	
	public MusicPlayerView getMusicPlayerView(){
		return musicPlayerView;
	}
	
	public TableView getTableView(){
		return tableView;
	}

	@Override
	public String getSaveButtonText() {
		return "Save Annotation";
	}

	@Override
	public void saveButtonClicked() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setFileFilter(new FileNameExtensionFilter("", "arff"));
		if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
			String path = fileChooser.getSelectedFile().toString();
			if(!path.endsWith(".arff")){
				path += ".arff";
			}
			annotationController.saveAnnotation(path);
		}
	}

	@Override
	public String getLoadButtonText() {
		return "Load Annotation";
	}

	@Override
	public void loadButtonClicked() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("", "arff"));
		if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			annotationController.clearAnnotation();
			annotationController.loadAnnotation(fileChooser.getSelectedFile().toString());
		}
	}

	@Override
	public String getCaption() {
		return "Multiple Files Annotation Editor";
	}

	public TableControlView getTableControlView() {
		return tableControlView;
	}
}