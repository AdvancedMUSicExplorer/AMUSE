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
 * Creation date: 26.01.2010
 */

package amuse.scheduler.gui.controller;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.scheduler.gui.views.TaskManagerView;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 *
 * @author waeltken
 */
public abstract class AbstractController {

    protected JComponent view;
    protected TaskManagerView taskManager = TaskManagerView.getInstance();
    public AbstractController() {
    }

    public abstract void saveTask(File file);

    public abstract void loadTask(DataSetAbstract dataSet);

    public abstract void loadTask(TaskConfiguration conf);

    public abstract TaskConfiguration getExperimentConfiguration();

    public void loadTask(File file) throws IOException {
        loadTask(new ArffDataSet(file));
    }

    protected File addArff(File file) {
        if (file.getName().endsWith(".arff"))
            return file;
        else
            return new File(file.getAbsolutePath() + ".arff");
    }
    protected void showErr(String msg) {
        JOptionPane.showMessageDialog(view, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected void showMsg(String msg) {
        JOptionPane.showMessageDialog(view, msg);
    }

    protected boolean showYesNoOption(String title, String msg) {
        return JOptionPane.showConfirmDialog(view, msg, title, JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
    }

    protected boolean askOverwrite(File file) {
        if (file.exists())
            return showYesNoOption("File already exists!", file.getName() + " already exists!\nDo you want to overwrite this file?");
        return true;
    }

    public abstract JComponent getView();
}
