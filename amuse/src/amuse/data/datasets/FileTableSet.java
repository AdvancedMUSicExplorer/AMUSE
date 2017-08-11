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
 * Creation date: 22.04.2009
 */
package amuse.data.datasets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import amuse.data.io.ArffDataSet;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;

/**
 * This class represents a list of files as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id$
 */
public class FileTableSet extends ArffDataSet {

    private final NumericAttribute idAttribute;
    private final StringAttribute pathAttribute;

    private static final String pathStr = "Path";
    private static final String idStr = "Id";

    /**
     * Creates a new FileTableSet from a file. Validates if the given file contains a FileTableSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid FileTableSet.
     */
    public FileTableSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        checkStringAttribute(pathStr);
        if (!this.getAttributeNames().contains(idStr) || !(this.getAttribute(idStr) instanceof NumericAttribute)) {
            System.out.println("This DataSet does not contain Id-Attribute!");
        }
        idAttribute = (NumericAttribute) this.getAttribute(idStr);
        pathAttribute = (StringAttribute) this.getAttribute(pathStr);
    }

    public FileTableSet(List<File> files) {
        super("FileTable");
        List<Double> ids = new ArrayList<Double>();
        List<String> paths = new ArrayList<String>();
        Double id = new Double(1.0f);
        for (File f : files) {
            paths.add(f.getAbsolutePath());
            ids.add(id);
            id++;
        }
        idAttribute = new NumericAttribute(idStr, ids);
        pathAttribute = new StringAttribute(pathStr, paths);
        this.addAttribute(idAttribute);
        this.addAttribute(pathAttribute);
    }

    public FileTableSet(List<Integer> ids, List<File> files) {
        super("FileTable");
        if (ids.size() != files.size()) {
            throw new IllegalArgumentException("IDs and files dont contain same amount of values!");
        }
        List<String> paths = new ArrayList<String>();
        List<Double> idsAsDouble = new ArrayList<Double>();
        for (int id : ids) {
            idsAsDouble.add(new Double(id));
        }
        for (File f : files) {
            paths.add(f.getAbsolutePath());
        }
        idAttribute = new NumericAttribute(idStr, idsAsDouble);
        pathAttribute = new StringAttribute(pathStr, paths);
        this.addAttribute(idAttribute);
        this.addAttribute(pathAttribute);
    }
    
    public NumericAttribute getIDAttribute() {
        return idAttribute;
    }
    
    public StringAttribute getPathAttribute() {
        return pathAttribute;
    }

    public List<File> getFiles() {
        List<File> files = new ArrayList<File>();
        for (String p : pathAttribute.getValues()) {
            files.add(new File(p));
        }
        return files;
    }
}
