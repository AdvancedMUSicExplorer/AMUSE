package amuse.scheduler.gui.annotation.multiplefiles;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import org.apache.log4j.Level;

import amuse.data.io.ArffDataSet;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.controller.MultipleFilesAnnotationController;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
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
		panel.add(filterView, "pushy, grow, w 50%, span 1 2");
		panel.add(musicPlayerView, "w 50%, growx");
		panel.add(tableControlView, "w 50%, growx");
		
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
		String dataSetName = annotationController.getLoadedDataSetName();
		String path = annotationController.getLoadedPath();
		if(dataSetName != null && path != null){
			JPopupMenu popupMenu = new JPopupMenu();
			
			JMenuItem saveAsItem = new JMenuItem("Save as...");
			saveAsItem.addActionListener(e -> {
				String newDataSetName = this.showDataSetNameDialog();
				if(newDataSetName == null){
					return;
				}
				String newPath = this.showPathDialog();
				if(newPath == null){
					return;
				}
				annotationController.saveAnnotation(newPath, newDataSetName);
			});

			JMenuItem saveItem = new JMenuItem("Save");
			saveItem.addActionListener(e -> annotationController.saveAnnotation(annotationController.getLoadedPath(), 
					annotationController.getLoadedDataSetName()));
			
			popupMenu.add(saveItem);
			popupMenu.add(saveAsItem);
			
			Point mousePos = this.getParent().getParent().getMousePosition();
			JButton saveButton = (JButton) ((Container) this.getParent().getParent().getComponentAt(mousePos)).getComponentAt(mousePos);
			popupMenu.show(this, saveButton.getX(), saveButton.getY());
		}
		else{
			if(dataSetName == null){
				dataSetName = this.showDataSetNameDialog();
				if(dataSetName == null){
					return;
				}
			}
			if(path == null){
				path = this.showPathDialog();
				if(path == null){
					return;
				}
			}
			annotationController.saveAnnotation(path, dataSetName);
		}
	}
	
	private String showDataSetNameDialog(){
		String dataSetName;
		while((dataSetName = JOptionPane.showInputDialog("Enter the data set name.")) != null){
			if(dataSetName.contains(" ")){
				JOptionPane.showConfirmDialog(null, "Please specify a name without whitespaces.", "Naming Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			}
			else{
				break;
			}
		}
		return dataSetName;
	}
	
	private String showPathDialog(){
		JFileChooser fc = new SelectArffFileChooser("Classification Task", new File(AmusePreferences.get(KeysStringValue.MULTIPLE_TRACKS_ANNOTATION_DATABASE)));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        String path = fc.getSelectedFile().getAbsolutePath();
        if(!path.endsWith(".arff")){
        	path += ".arff";
        }
        return path;
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
			annotationController.loadAnnotation(categoryList.getAttribute("Path").getValueAt(categoryComboBox.getSelectedIndex()).toString(), (String) categoryComboBox.getSelectedItem());
		}
	}

	@Override
	public String getCaption() {
		String dataSetName = annotationController.getLoadedDataSetName();
		return "Multiple Files Annotation Editor" + (dataSetName == null? "": " - " + dataSetName);
	}

	public TableControlView getTableControlView() {
		return tableControlView;
	}
}