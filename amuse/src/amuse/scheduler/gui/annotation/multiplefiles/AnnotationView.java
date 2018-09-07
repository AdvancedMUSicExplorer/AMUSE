package amuse.scheduler.gui.annotation.multiplefiles;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.apache.log4j.Level;

import amuse.data.io.ArffDataSet;
import amuse.preferences.AmusePreferences;
import amuse.scheduler.gui.controller.MultipleFilesAnnotationController;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.util.AmuseLogger;
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
		String dataSetName = null;
		while((dataSetName = JOptionPane.showInputDialog("Enter the data set name.")) != null){
			if(dataSetName.contains(" ")){
				JOptionPane.showConfirmDialog(null, "Please specify a name without whitespaces.", "Naming Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			}
			else{
				annotationController.saveAnnotation(dataSetName);
				break;
			}
		}
	}

	@Override
	public String getLoadButtonText() {
		return "Load Annotation";
	}

	@Override
	public void loadButtonClicked() {
		ArffDataSet categoryList = null;
		try {
			categoryList = new ArffDataSet(new File(AmusePreferences.getMultipleTracksAnnotationTablePath()));
		} catch (IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Could not load the MultipleTracksAnnotationTable. In file '"
					+ AmusePreferences.getMultipleTracksAnnotationTablePath()
					+ "' following error occured"
					+ e.getMessage());
			return; 
		}
		String[] categories = new String[categoryList.getValueCount()];
		for(int i = 0; i < categoryList.getValueCount(); i++) {
			categories[i] = categoryList.getAttribute("CategoryName").getValueAt(i).toString();
		}
		JComboBox<String> categoryComboBox = new JComboBox<String>(categories);
		if(JOptionPane.showConfirmDialog(
				null,
				new JComponent[] {categoryComboBox},
				"Choose the annotation name to load",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION
				&& categoryComboBox.getSelectedIndex() != -1){
			annotationController.clearAnnotation();
			annotationController.loadAnnotation(categoryList.getAttribute("Path").getValueAt(categoryComboBox.getSelectedIndex()).toString());
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