/**
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 *
 * Copyright 2006-2010 by code authors
 *
 * Created at TU Dortmund, Chair of Algorithm Engineering
 * (Contact: <http://ls11-www.cs.tu-dortmund.de>)
 *
 * AMUSE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMUSE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
 *
 * Creation date: 09.03.2009
 */

package amuse.scheduler.gui.filesandfeatures;

import amuse.data.FeatureTable;
import amuse.data.FileTable;
import amuse.data.datasets.FileTableSet;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

/**
 * This Class combines a MusicFileTreeView with FeatureTableView.
 * They are displayed in a JSplitPane. FileTree will be on the left FeatureTable
 * on the right hand side.
 * @author Clemens Waeltken
 */
public class FilesAndFeaturesFacade {

    private final JSplitPane view;

    private final String[] endings = {"mp3" , "wav"};

    private FeatureTableModel featureTableModel;

    private final FeatureTableView featureTableView = new FeatureTableView();

    private FileTreeModel fileTreeModel;

    private final FileTreeView fileTreeView = new FileTreeView();

    private final File featureTableFile = new File(AmusePreferences.getFeatureTablePath());

    private final File musicDatabaseFolder = new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE));

    private final String musicDatabaseLabel = "Music Database";

    private final FeatureTableController featureTableController;

    private final FileTreeController fileTreeController;

    public FilesAndFeaturesFacade(FeatureTable pFeatureTable){
    	featureTableModel = new FeatureTableModel(pFeatureTable);
    	view = new JSplitPane();
        view.add(featureTableView.getView(), JSplitPane.RIGHT);
        view.add(fileTreeView.getView(), JSplitPane.LEFT);
        featureTableController = new FeatureTableController(getFeatureTableModel(), featureTableView);
        fileTreeController = new FileTreeController(getFileTreeModel(), fileTreeView);
    }
    public FilesAndFeaturesFacade() {
        view = new JSplitPane();
        view.add(featureTableView.getView(), JSplitPane.RIGHT);
        view.add(fileTreeView.getView(), JSplitPane.LEFT);
        featureTableController = new FeatureTableController(getFeatureTableModel(), featureTableView);
        fileTreeController = new FileTreeController(getFileTreeModel(), fileTreeView);
    }

    public FeatureTable getFeatureTable() {
        return featureTableModel.getCurrentFeatureTable();
    }

    public List<File> getFiles() {
        return fileTreeModel.getFiles();
    }

    /**
     * Returns this combined view as a single JComponent.
     * @return this View as JComponent.
     */
    public JComponent getView() {
        return view;
    }
    
    public FeatureTableView getFeatureTableView(){
    	return featureTableView;
    }

    private FeatureTableModel getFeatureTableModel() {
        if (featureTableModel == null) {
            featureTableModel = new FeatureTableModel(new FeatureTable(featureTableFile));
        }
        return featureTableModel;
    }
    
    public void setFeatureTableModel(FeatureTableModel pFeatureTableModel){
    	featureTableModel = pFeatureTableModel;
    }

    private FileTreeModel getFileTreeModel() {
        if(fileTreeModel == null) {
    		fileTreeModel = new FileTreeModel(musicDatabaseFolder, musicDatabaseLabel, endings);
        }
        return fileTreeModel;
    }
    
    public boolean filesAndFeaturesSelected() {
    	if (this.getFiles().size() == 0) {
    		return false;
    	}
    	if (this.getFeatureTable().getSelectedIds().size() == 0) {
    		return false;
    	}
    	return true;
    }

    public void loadFileList(File file) {
        fileTreeController.loadFileList(file);
    }

    public void loadFeatureTable(File file) {
        featureTableController.loadFeatureTableSelection(file);
    }

    public void loadFilesAndFeatures(File fileTableFile, File featureTableFile) {
        loadFileList(fileTableFile);
        loadFeatureTable(featureTableFile);
    }

    public void saveFilesAndFeatures(File fileTableFile, File featureTableFile) throws IOException {
        FileTableSet fileTableSet = new FileTableSet(getFiles());
        FeatureTable featureTable = getFeatureTable();
        fileTableSet.saveToArffFile(fileTableFile);
        featureTable.getAccordingDataSet().saveToArffFile(featureTableFile);
    }

    public void loadFileList(FileTable musicFileList) {
        List<File> files = new ArrayList<File>();
        for (String path : musicFileList.getFiles()) {
            files.add(new File(path));
        }
        fileTreeController.loadFiles(files);
    }

    public void loadFeatureTable(FeatureTable featureTable) {
        featureTableController.loadFeatureTableSelection(featureTable);
    }

    public FileTable getFileTable() {
        return new FileTable(getFiles());
    }
}
