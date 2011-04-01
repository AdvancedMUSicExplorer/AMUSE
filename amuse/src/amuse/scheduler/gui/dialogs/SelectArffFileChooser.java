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
 * Creation date: 08.03.2009
 */
package amuse.scheduler.gui.dialogs;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This File chooser only accepts .arff files.
 * @author Clemens Waeltken
 */
public class SelectArffFileChooser extends JFileChooser {

    /**
     * Creates an arff File chooser.
     * @param type short descriptor for type of arff file.
     * @param the folder to be preselected in this file chooser.
     */
    public SelectArffFileChooser(String type, File destinationFolder) {
        super(destinationFolder);
        this.setAcceptAllFileFilterUsed(false);
        this.setFileSelectionMode(JFileChooser.FILES_ONLY);
        this.setDialogTitle("Select ARFF " + type);
        this.setMultiSelectionEnabled(false);
        this.setFileFilter(new FileNameExtensionFilter("ARFF " +type, "arff"));
    }
}
