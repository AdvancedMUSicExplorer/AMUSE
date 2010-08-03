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

package amuse.data.io.attributes;

import amuse.data.io.attributes.NumericAttribute;
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
public class NumericAttributeTest {

    private static NumericAttribute atrib;

    private static String name = "TestName";

    private static List<Double> values;

    public NumericAttributeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        values = new Vector<Double>();
        values.add(new Double(1f));
        values.add(new Double(2.5f));
        values.add(new Double(3f));
        values.add(new Double(4f));
        atrib = new NumericAttribute(name, values);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetValues() {
        System.out.println("getValues");
        NumericAttribute instance = atrib;
        List<Double> expResult = values;
        List<Double> result = instance.getValues();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetHeaderStr() {
        System.out.println("getHeaderStr");
        NumericAttribute instance = atrib;
        String expResult = "@ATTRIBUTE " + name + " " + atrib.getTypeStr();
        String result = instance.getHeaderStr();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTypeStr() {
        System.out.println("getTypeStr");
        NumericAttribute instance = atrib;
        String expResult = "NUMERIC";
        String result = instance.getTypeStr();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValueCount() {
        System.out.println("getValueCount");
        NumericAttribute instance = atrib;
        int expResult = 4;
        int result = instance.getValueCount();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValueStrAt() {
        System.out.println("getValueStrAt");
        int index = 0;
        NumericAttribute instance = atrib;
        String expResult = "1";
        String result = instance.getValueStrAt(index);
        assertEquals(expResult, result);
        index++;
        expResult = "2.5";
        result = instance.getValueStrAt(index);
        assertEquals(expResult, result);
    }
}