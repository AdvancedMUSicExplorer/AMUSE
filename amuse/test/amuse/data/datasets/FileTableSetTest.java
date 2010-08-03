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

import amuse.data.io.attributes.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Clemens Waeltken
 * @version $Id: $
 */
public class FileTableSetTest {

    static ArrayList<File> fileList;

    static File testArff = new File("test/test.arff");

    @BeforeClass
    public static void setUpClass() throws Exception {
        fileList = new ArrayList<File>();
        for (File f: new File("/").listFiles()) {
            if (f.exists()) {
                fileList.add(f);
            }
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        testArff.delete();
    }

    @Test
    public void testSaveToArffFile() throws Exception {
        System.out.println("saveToArffFile");
        FileTableSet instance = new FileTableSet(fileList);
        instance.saveToArffFile(testArff);
        assertTrue(testArff.exists());
    }

    @Test
    public void testFileTableFile() throws Exception {
        assertTrue(testArff.exists());
		FileTableSet instance = new FileTableSet(testArff);
    }

    @Test
    public void testGetFiles() throws IOException {
        System.out.println("getFiles");
        FileTableSet instance = new FileTableSet(fileList);
        List<File> expResult = fileList;
        List<File> result = instance.getFiles();
        for (int i = 0; i < expResult.size(); i++) {
            assertEquals(expResult.get(i).getAbsolutePath(), result.get(i).getAbsolutePath());
        }
    }

    @Test
    public void testGetIDAttribute() {
        System.out.println("getIDAttribute");
        FileTableSet instance = new FileTableSet(fileList);
        NumericAttribute result = instance.getIDAttribute();
        for (int i = 0; i < result.getValueCount(); i++) {
            assertTrue(result.getValueAt(i).intValue() == result.getValueAt(i));
        }
    }

    @Test
    public void testGetPathAttribute() {
        System.out.println("getPathAttribute");
        FileTableSet instance = new FileTableSet(fileList);
        StringAttribute result = instance.getPathAttribute();
        for (int i = 0; i < fileList.size(); i++) {
            assertEquals(result.getValueAt(i), fileList.get(i).getAbsolutePath());
        }
    }
}