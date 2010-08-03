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

import amuse.data.io.attributes.*;
import java.util.Vector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Clemens Waeltken
 */
public class AttributeTest {
    
    private static String name = "TestName";

    private static Attribute atrib;

    public AttributeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        atrib = new NumericAttribute(name, new Vector<Double>());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetName() {
        System.out.println("getName");
        Attribute instance = atrib;
        String expResult = name;
        String result = instance.getName();
        assertEquals(expResult, result);
    }

}