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
 * Creation date: 15.01.2009
 */
package amuse.data.io;

import amuse.data.io.attributes.*;
import amuse.data.io.attributes.Attribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Clemens Waeltken
 */
public class DataSetTest {

    private static ArffDataSet dataSet;
    private static StringAttribute strAttribute;
    private static NumericAttribute numAttribute;
    private static File file = new File("test/test.arff");
    private static File speedTestFile = new File("test/hugeArffForPerformanceTest.arff");
    private static String name = "Test Set";

    public DataSetTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        dataSet = new ArffDataSet(name);
        List<String> strList = new Vector<String>();
        strList.add("String1");
        strList.add("Users/Shared/Musik/50 Cent/Curtis/01' Intro.mp3");
        strAttribute = new StringAttribute("Test String Attribute", strList);
        List<Double> numList = new Vector<Double>();
        numList.add(new Double(1f));
        numList.add(new Double(2f));
        numAttribute = new NumericAttribute("TestNumericAttribute", numList);
        dataSet.addAttribute(strAttribute);
        dataSet.addAttribute(numAttribute);
        dataSet.addAmuseAttribute("TestThis", "Tested");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        dataSet = null;
        file.delete();
    }

    @Test
    public void testGetName() {
        System.out.println("getName");
        ArffDataSet instance = dataSet;
        String expResult = name;
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void testSaveToArffFile() {
        System.out.println("saveToArffFile");
        ArffDataSet instance = dataSet;
        List<String> attribs = instance.getAttributeNames();
        // Setup the expected Result for this File:
        StringBuilder arffFileContentExpected = new StringBuilder();
        arffFileContentExpected.append("%@TestThis=Tested\n");
        arffFileContentExpected.append(ArffDataSet.relationStr + " \"" + instance.getName() + "\"\n\n");
        for (String atrName : attribs) {
            arffFileContentExpected.append(instance.getAttribute(atrName).getHeaderStr() + "\n");
        }
        arffFileContentExpected.append("\n" + ArffDataSet.dataStr + "\n");
        for (int i = 0; i < instance.getValueCount(); i++) {
            for (String atrName : attribs) {
                arffFileContentExpected.append(instance.getAttribute(atrName).getValueStrAt(i));
                if (attribs.indexOf(atrName) == attribs.size() - 1) {
                    arffFileContentExpected.append("\n");
                } else {
                    arffFileContentExpected.append(",");
                }
            }
        }
        System.out.println("Expected:\n---------------------------------\n" +
                arffFileContentExpected.toString() +
                "\n---------------------------------");
        StringBuilder arffFileActualContent = new StringBuilder();
        try {
            // Write the actual file from ArffDataSet Object:
            instance.saveToArffFile(file);
        } catch (IOException ex) {
            assertTrue("Unable to write TestFile", false);
        }
        try {
            BufferedReader bufferFromFile = new BufferedReader(new FileReader(file));
            while (bufferFromFile.ready()) {
                arffFileActualContent.append(bufferFromFile.readLine() + "\n");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Not supposed to happen here!");
        }
        System.out.println("Actual Content:\n---------------------------------\n" +
                arffFileActualContent.toString() +
                "\n---------------------------------");
        assertEquals(arffFileContentExpected.toString(), arffFileActualContent.toString());
    //file.delete();
    }

    @Test
    public void testAddAttribute() {
        System.out.println("addAttribute");
        NumericAttribute attribute = new NumericAttribute("AddAttribute", new Vector<Double>());

        // Check if wrong value count throws exception.
        boolean throwsException = false;
        ArffDataSet instance = dataSet;
        try {
            instance.addAttribute(attribute);
        } catch (DataSetException exp) {
            throwsException = true;
        }
        assertTrue(throwsException);

        // Check if Attributes with correct value count are excepted.
        attribute.addValue(new Double(0.1f));
        attribute.addValue(new Double(0.2f));
        instance.addAttribute(attribute);
        List<Attribute> resultList = new Vector<Attribute>();
        resultList.add(strAttribute);
        resultList.add(numAttribute);
        resultList.add(attribute);
        assertNotNull(instance.getAttribute(attribute.getName()));
    }

    @Test
    public void testAddAttributeList() {
        System.out.println("addAttributeList");
        List<Attribute> attributeList = new Vector<Attribute>();
        attributeList.add(strAttribute);
        attributeList.add(numAttribute);
        ArffDataSet instance = new ArffDataSet(name);
        instance.addAttributeList(attributeList);
        assertNotNull(instance.getAttribute(strAttribute.getName()));
        assertNotNull(instance.getAttribute(numAttribute.getName()));
    }

    @Test
    public void testAddAmuseAttribute() {
        ArffDataSet instance = dataSet;
        instance.addAmuseAttribute("TestKey", "TestValue");
    }

    @Test
    public void testGetAmuseAttribute() {
        ArffDataSet instance = dataSet;
        assertEquals(instance.getAmuseAttribute("TestKey"), "TestValue");
        boolean thrown = false;
        try {
            instance.getAmuseAttribute("NoAttribute");
        } catch (DataSetException ex) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testGetAttribute() {
        assertEquals(dataSet.getAttribute(numAttribute.getName()).getHeaderStr(), numAttribute.getHeaderStr());
        assertEquals(dataSet.getAttribute(strAttribute.getName()).getHeaderStr(), strAttribute.getHeaderStr());
    }

    @Test
    public void testGetValueCount() {
        System.out.println("getValueCount");
        ArffDataSet instance = new ArffDataSet("");
        assertEquals(-1, instance.getValueCount());
        instance.addAttribute(strAttribute);
        assertEquals(2, instance.getValueCount());
        instance = new ArffDataSet("");
        List<Double> values = new Vector<Double>();
        values.add(new Double(1));
        values.add(new Double(1));
        values.add(new Double(1));
        values.add(new Double(1));
        values.add(new Double(1));
        instance.addAttribute(new NumericAttribute("", values));
        assertEquals(5, instance.getValueCount());
    }

    @Test
    public void testDataSetFile() {
        setUpClass();
        ArffDataSet instance = dataSet;
        System.out.println("-------------------------------------" +
                "TestDataSetFile");
        instance.addAmuseAttribute("AmuseAttributeTestWhileLoading", "Tested");
        try {
            instance.saveToArffFile(file);
        } catch (IOException ex) {
            assertTrue("Unable to write Testfile!", false);
        }
        boolean thrown = false;
        try {
            @SuppressWarnings("unused")
			DataSet loadedSet1 = new DataSet(new File("nofile"));
        } catch (IOException ex) {
            thrown = true;
        }
        assertTrue("Exception not thrown!", thrown);
        try {
            instance.printSet();
            ArffDataSet loadedSet2 = new ArffDataSet(file, 1);
            assertEquals(instance.getName(), loadedSet2.getName());
            assertEquals(instance.getAttributeNames(), loadedSet2.getAttributeNames());
//            assertEquals("Tested", loadedSet2.getAmuseAttribute("AmuseAttributeTestWhileLoading"));
            for (String atrName : instance.getAttributeNames()) {
                for (int i = 0; i < instance.getAttribute(atrName).getValueCount(); i++) {
                    String str1 = instance.getAttribute(atrName).getValueStrAt(i);
                    String str2 = loadedSet2.getAttribute(atrName).getValueStrAt(i);
                    assertEquals(str1, str2);
                }
            }
        } catch (IOException ex) {
            assertTrue("File not Loaded: " + ex.getMessage(), false);
        }
        //             Validate all arff files in Amuse folder and subdirectories.
        try {
            File amuseFolder = new File("./");
            for (File f : getArffs(amuseFolder)) {
                System.out.println("TRYING TO READ: " + f.getCanonicalPath());
                new ArffDataSet(f);
            }
        } catch (IOException iOException) {
        }
    }

    private File[] getArffs(File folder) {
        if (!folder.isDirectory()) {
            return new File[0];
        }
        ArrayList<File> files = new ArrayList<File>();
        for (File f : folder.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".arff")) {
                files.add(f);
            } else if (f.isDirectory()) {
                for (File arff : getArffs(f)) {
                    files.add(arff);
                }
            }
        }
        return files.toArray(new File[files.size()]);
    }
    
    @Test
    public void testSpeed() throws IOException {
    	Date before = new Date();
    	System.out.println("Previously on MBPro 2.8GHz DualCore: Checking in 7.686 sec, reading 37.044 sec.");
    	System.out.println("Starting to read "+ speedTestFile.getName()+"("+ speedTestFile.length() / 1024 / 1024 +"MB)...");
    	DataSet	speedTestSet = new DataSet(speedTestFile);
    	Date after = new Date();
    	long timePassed = after.getTime() - before.getTime();
    	System.out.println("Finished initialisation and checking after " + (double)timePassed / 1000 + " seconds!");
    	before = after;
//        speedTestSet.printSet();
        System.out.println("\nVALUE COUNT:\n" + speedTestSet.getValueCount() +" * " + speedTestSet.getAttributeCount() + " = " + speedTestSet.getValueCount() * speedTestSet.getAttributeCount());
    	List<Attribute> attributes = new ArrayList<Attribute>();
    	for (String atrName:speedTestSet.getAttributeNames()) {
    		attributes.add(speedTestSet.getAttribute(atrName));
    	}
    	for (int i = 0; i < speedTestSet.getValueCount(); i++) {
    		for (Attribute a: attributes) {
    			a.getValueAt(i);
    		}
//		   System.out.println(i);
    	}
    	after = new Date();
    	timePassed = after.getTime() - before.getTime();
    	System.out.println("Finished reading values after " + (double)timePassed / 1000 + " seconds!");
    }
}