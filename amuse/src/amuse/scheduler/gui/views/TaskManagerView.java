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
 * Creation date: 12.10.2010
 */

package amuse.scheduler.gui.views;

import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.controller.WizardController;
import amuse.scheduler.gui.controller.WizardControllerInterface;
import amuse.scheduler.gui.dialogs.SelectArffFileChooser;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.HasSaveButton;
import amuse.scheduler.gui.navigation.NextButtonUsable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author waeltken
 */
public class TaskManagerView extends JPanel implements HasCaption, NextButtonUsable, HasSaveButton, HasLoadButton{
    private JTable tblTasks;
    private JScrollPane scpTaks;
    private JButton btnAddTask = new JButton("+");
    private JButton btnRemoveTask = new JButton("-");
    private JButton btnUp = new JButton("Up");
    private JButton btnDown = new JButton("Down");
    private JButton btnCheckTasks = new JButton("Check Tasks");
    private JButton btnSaveTasks = new JButton("Save Tasks");
    private JPopupMenu menSelectTask = new JPopupMenu();
    private WizardControllerInterface wizard = WizardController.getInstance();
    private ExperimentSetTable experimentTable = new ExperimentSetTable();
    private static TaskManagerView instance;
    private boolean isEditing = false;

    public static TaskManagerView getInstance() {
        if (instance == null) {
            instance = new TaskManagerView();
        }
        return instance;
    }

    private TaskManagerView() {
        super(new MigLayout("fill", "", ""));
        tblTasks = new JTable(experimentTable);
        tblTasks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scpTaks = new JScrollPane(tblTasks);
        createPopupMenu();
        tblTasks.getColumnModel().getColumn(0).setMinWidth(30);
        tblTasks.getColumnModel().getColumn(0).setMaxWidth(30);
        tblTasks.getColumnModel().getColumn(1).setMinWidth(200);
        tblTasks.getColumnModel().getColumn(1).setMaxWidth(200);
        
        btnAddTask.addActionListener(e -> showPopupMenu());
        btnRemoveTask.addActionListener(e -> removeSelected());
        btnUp.addActionListener(e -> moveUp());
        btnDown.addActionListener(e -> moveDown());
        tblTasks.addMouseListener(new TableClickListener());
        
        add(scpTaks, "spany 4, spanx 2, push, grow");
        add(btnAddTask, "wrap, bottom, sg buttons");
        add(btnRemoveTask, "wrap, sg buttons");
        add(btnUp, "wrap, sg buttons");
        add(btnDown, "wrap, sg buttons");
        btnCheckTasks.setEnabled(false);
        btnSaveTasks.setEnabled(false);
        add(btnCheckTasks, "right");
        add(btnSaveTasks);
	btnCheckTasks.setVisible(false);
	btnSaveTasks.setVisible(false);
    }

    private void showPopupMenu() {
        menSelectTask.show(this, 
                btnAddTask.getX(),
                btnAddTask.getY() + btnAddTask.getHeight());
    }

    private void removeSelected() {
        experimentTable.remove(tblTasks.getSelectedRow());
    }

    private void moveUp() {
        int selected = tblTasks.getSelectedRow();
        if (selected > -1) {
            selected = experimentTable.moveUp(selected);
            tblTasks.addRowSelectionInterval(selected, selected);
        }
    }

    private void moveDown() {
        int selected = tblTasks.getSelectedRow();
        if (selected > -1) {
            selected = experimentTable.moveDown(selected);
            tblTasks.addRowSelectionInterval(selected, selected);
        }
    }


    private void createPopupMenu() {
        JMenuItem feTaskItem = new JMenuItem("Feature Extraction");
        feTaskItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                wizard.goToFeatureExtraction();
                isEditing = false;
            }
        });
        JMenuItem fpTaskItem = new JMenuItem("Feature Processing");
        fpTaskItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               wizard.goToFeatureProcessing();
                isEditing = false;
            }
        });
        JMenuItem ctTaskItem = new JMenuItem("Classification Training");
        ctTaskItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                wizard.goToTrainingExperiment();
                isEditing = false;
            }
        });
        JMenuItem cTaskItem = new JMenuItem("Classification");
        cTaskItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                wizard.goToClassification();
                isEditing = false;
            }
        });
        JMenuItem vTaskItem = new JMenuItem("Classification Validation");
        vTaskItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                wizard.goToValidation();
                isEditing = false;
            }
        });
        JMenuItem oTaskItem = new JMenuItem("Optimization");
        oTaskItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                wizard.goToOptimization();
                isEditing = false;
            }
        });
        menSelectTask.add(feTaskItem);
        menSelectTask.add(fpTaskItem);
        menSelectTask.add(ctTaskItem);
        menSelectTask.add(cTaskItem);
        menSelectTask.add(vTaskItem);
	menSelectTask.add(oTaskItem);
    }

    @Override
    public String getCaption() {
        return "Experiment Configurator";
    }

    @Override
    public boolean nextButtonClicked() {
        startTasks();
        return false;
    }

    @Override
    public String getNextButtonText() {
        return "Start Experiment";
    }

    private void startTasks() {
		List<TaskConfiguration> tasks = new ArrayList<TaskConfiguration>(experimentTable.experiments);
		wizard.startTasks(tasks);
    }

    public void addExperiment(TaskConfiguration ex) {
        if (isEditing) {
            int row = tblTasks.getSelectedRow();
            experimentTable.remove(row);
            experimentTable.addExperiment(row, ex);
            isEditing = false;
        } else {
            experimentTable.addExperiment(ex);
        }
            wizard.goToExperimentManager();
    }

    private void doubleClickOnTable() {
        editExperiment(experimentTable.getExperiment(tblTasks.getSelectedRow()));
    }

    private void editExperiment(TaskConfiguration experiment) {
        if (experiment instanceof ExtractionConfiguration) {
            wizard.goToFeatureExtraction((ExtractionConfiguration)experiment);
        } else if (experiment instanceof ProcessingConfiguration) {
            wizard.goToFeatureProcessing((ProcessingConfiguration) experiment);
        } else if (experiment instanceof TrainingConfiguration) {
            wizard.goToTrainingExperiment((TrainingConfiguration) experiment);
        } else if (experiment instanceof ClassificationConfiguration) {
            wizard.goToClassification((ClassificationConfiguration) experiment);
        } else if (experiment instanceof ValidationConfiguration) {
            wizard.goToValidation((ValidationConfiguration) experiment);
        } else if (experiment instanceof OptimizationConfiguration) {
            wizard.goToOptimization((OptimizationConfiguration) experiment);
        } else {
            return;
        }
        isEditing = true;
    }

    public class ExperimentSetTable implements TableModel{

        private List<TaskConfiguration> experiments = new Vector<TaskConfiguration>();
        private List<TableModelListener> listeners = new Vector<TableModelListener>();

        public ExperimentSetTable() {
        }


        public void addExperiment(TaskConfiguration ex) {
            experiments.add(ex);
            notifyListenersOfAdd(experiments.indexOf(ex));
        }

        private void addExperiment(int row, TaskConfiguration ex) {
            experiments.add(row, ex);
            notifyListenersOfAdd(experiments.indexOf(ex));
        }

        @Override
        public int getRowCount() {
            return experiments.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch(columnIndex) {
                case 0:
                    return "ID";
                case 1:
                    return "Type";
                case 2:
                    return "Additional Information";
                default:
                    return "Default";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TaskConfiguration x = experiments.get(rowIndex);
            switch(columnIndex) {
                case 0:
                    return experiments.indexOf(x) + 1 + "";
                case 1:
                    return x.getType();
                case 2:
                    return x.getDescription();
                default:
                    return "Default";
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }

        private void notifyListeners() {
            for (TableModelListener l: listeners) {
		l.tableChanged(new TableModelEvent(this));
	    }
        }

        private void remove(int index) {
            if (index >= 0 && index < experiments.size()) {
                experiments.remove(index);
                notifyListeners();
            }
        }

        private int moveDown(int index) {
            if (index >= 0 && index < experiments.size() - 1) {
                TaskConfiguration ex = experiments.remove(index);
                experiments.add(++index, ex);
                notifyListeners();
                notifyListenersOfAdd(index);
            }
            return index;
        }

        private int moveUp(int index) {
            if (index > 0 && index < experiments.size()) {
                TaskConfiguration ex = experiments.remove(index);
                experiments.add(--index, ex);
                notifyListeners();
                notifyListenersOfAdd(index);
            }
            return index;
        }

        private void notifyListenersOfAdd(int index) {
            for (TableModelListener l: listeners) {
		l.tableChanged(new TableModelEvent(this, index));
	    }
        }

        private TaskConfiguration getExperiment(int index) {
            return experiments.get(index);
        }
    }

    private class TableClickListener implements MouseListener {

        public TableClickListener() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                doubleClickOnTable();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

	@Override
	public String getSaveButtonText() {
		return "Save Experiment";
	}

	@Override
	public void saveButtonClicked() {

        JFileChooser fc = new SelectArffFileChooser("Classification Task", new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH)));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fc.getSelectedFile();
        if(!selectedFile.getAbsolutePath().endsWith(".arff")){
        	selectedFile = new File(selectedFile.getAbsoluteFile() + ".arff");
        }
		List<TaskConfiguration> tasks = new ArrayList<TaskConfiguration>(experimentTable.experiments);
		wizard.saveTasks(tasks, selectedFile);
		
	}

	@Override
	public String getLoadButtonText() {
		return "Load Experiment";
	}

	@Override
	public void loadButtonClicked() {
		JFileChooser fc = new SelectArffFileChooser("Classification Task", new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH)));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fc.getSelectedFile();
        wizard.loadTasks(selectedFile);
	}
}
