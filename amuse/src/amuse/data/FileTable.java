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
 * Creation date: 04.03.2010
 */
package amuse.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import amuse.data.datasets.FileTableSet;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.util.AmuseLogger;

/**
 * Describes the list of files for Amuse tasks 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class FileTable implements Serializable {

	/** For Serializable interface */
	private static final long serialVersionUID = 8527398039294796708L;
	
	/** File id list */
	private List<Integer> ids = null;
	
	/** File list */
	private List<String> files = null;

    /**
     * Creates FeatureTable from existing fileTable.arff.
     * @param fileTableFile - java.io.File object witch contains fileTable stored as ".arff".
     */
    public FileTable(File fileTableFile) {
        try {
            FileTableSet fileTableSet = new FileTableSet(fileTableFile);
            ids = new ArrayList<Integer>(fileTableSet.getValueCount());
            files = new ArrayList<String>(fileTableSet.getValueCount());
            NumericAttribute id = fileTableSet.getIDAttribute();
            StringAttribute path = fileTableSet.getPathAttribute();
            for (int i = 0; i < fileTableSet.getValueCount(); i++) {
            	ids.add(id.getValueAt(i).intValue());
            	files.add(path.getValueAt(i));
            }
        } catch (FileNotFoundException ex) {
        	AmuseLogger.write(this.getClass().getName(), Level.FATAL, "Can't initialize FileTable: File not Found: " + ex.getMessage());
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            AmuseLogger.write(this.getClass().getName(), Level.FATAL, "Can't initialize FileTable: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Another constructor
     * @param ids List with file ids
     * @param files List with file names
     */
    public FileTable(List<Integer> ids, List<String> files) {
    	this.ids = ids;
    	this.files = files;
    }

    public FileTable(List<File> files) {
        List<Integer> generatedIds = new ArrayList<Integer>();
        List<String> fileStrings = new ArrayList<String>();
        for (int i = 0; i < files.size(); i++) {
            generatedIds.add(i);
            fileStrings.add(files.get(i).getAbsolutePath());
        }
        this.ids = generatedIds;
        this.files = fileStrings;
    }

    /**
     * @return DataSet representation of this FileTable
     */
    public FileTableSet getAccordingDataSet() {
        List<Integer> fileIdList = new ArrayList<Integer>();
        List<File> filePathList = new ArrayList<File>();
        for(int i=0;i<files.size();i++) {
           fileIdList.add(ids.get(i));
           filePathList.add(new File(files.get(i)));
        }
    	FileTableSet fileSet = new FileTableSet(fileIdList,filePathList);
        return fileSet;
    }

    /**
     * @return File path for given index
     */
    public String getFileAt(int index) {
        return this.files.get(index);
    }

    /**
     * @return File path for given file id
     */
    public String getFileByID(int fileId) {
        for(int i=0;i<ids.size();i++) {
            if(ids.get(i) == fileId) {
                return files.get(i);
            }
        }
        return null;
    }

    /**
     * @return File list
     */
    public List<String> getFiles() {
        return this.files;
    }
    
    /**
     * @return File id list
     */
    public List<Integer> getIds() {
    	return this.ids;
    }

    /*public void printTable() {
        for (int i = 0; i < features.size(); i++) {
            System.out.println(features.get(i).isSelectedForExtraction() + "ID:" + features.get(i).getId() + " " + features.get(i).getDescription() + " " + features.get(i).getDimension() + " " + features.get(i).getExtractorId());
        }
    }

    public int size() {
        return this.features.size();
    }*/

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	/*public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
		return result;
	}*/

    public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FileTable))
			return false;
		FileTable other = (FileTable) obj;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		return true;
	}
}
