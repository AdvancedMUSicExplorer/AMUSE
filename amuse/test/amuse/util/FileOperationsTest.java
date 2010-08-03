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
 * Creation date: 01.04.2010
 */

package amuse.util;

import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Clemens Waeltken
 */
public class FileOperationsTest {

    public FileOperationsTest() {
    }

    private static final File testFolder = new File("test/fileoperations/");
    private static final File folderMoveTarget = new File("test/movedFolder");
    private static final File testFile = new File("test/test.mp3");
    private static final File copyTarget = new File("test/copytarget.mp3");
    private static final File moveTarget = new File(testFolder.getAbsolutePath() + "/moveTarget.mp3");
    @BeforeClass
    public static void setUpClass() throws Exception {
	testFolder.mkdirs();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
	FileOperations.delete(testFolder, true);
    }

    @Test
    public void testCopy() {
	System.out.println("copy");
	File from = testFile;
	File to = copyTarget;
	boolean result = true;
        try {
            FileOperations.copy(from, to);
        } catch (IOException ex) {
            result = false;
        }
	assertTrue(result);
	assertEquals(from.length(), to.length());
        try {
            FileOperations.copy(new File("src/"), new File(testFolder, "test/"));
        } catch (IOException ex) {
            result = false;
        }
        assertTrue(result);
    }

    @Test
    public void testMove() {
	System.out.println("move");
	File from = copyTarget;
	long length = from.length();
	File to = moveTarget;
	boolean result = true;
        try {
            FileOperations.move(from, to);
        } catch (IOException ex) {
            result = false;
        }
	assertTrue(result);
	assertEquals(length, to.length());
        try {
            FileOperations.move(testFolder, folderMoveTarget);
        } catch (IOException ex) {
            result = false;
        }
	assertTrue(result);
    }

    @Test
    public void testDelete_File() throws IOException {
	System.out.println("delete");
	File f = testFile;
	boolean expResult = copyTarget.exists();
	boolean result = FileOperations.delete(copyTarget);
	assertEquals(expResult, result);
        FileOperations.copy(f, copyTarget);
	expResult = copyTarget.exists();
	result = FileOperations.delete(copyTarget);
	assertEquals(expResult, result);
    }

    @Test
    public void testDelete_File_boolean() {
	System.out.println("delete_recursive");
	File file = folderMoveTarget;
	boolean recursive = false;
	boolean expResult = false;
	boolean result = FileOperations.delete(file, recursive);
	assertEquals(expResult, result);
	recursive = true;
	expResult = true;
	result = FileOperations.delete(file, recursive);
	assertEquals(expResult, result);
    }

}