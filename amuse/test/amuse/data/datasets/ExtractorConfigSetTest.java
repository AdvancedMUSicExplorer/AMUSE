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
 * Creation date: 21.01.2010
 */

package amuse.data.datasets;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author waeltken
 */
public class ExtractorConfigSetTest {

    private static File exampleExtracorConfigFile = new File("experiments/extractorConfig.arff");
    public ExtractorConfigSetTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of getFeatureTables method, of class ExtractorConfigSet.
     */
    @Test
    public void testGetFeatureTables() {
        System.out.println("getFeatureTables");
        ExtractorConfigSet instance = null;
        try {
            instance = new ExtractorConfigSet(exampleExtracorConfigFile);
        } catch (IOException ex) {
            fail("Unable to open " + exampleExtracorConfigFile.getName());
        }
        List<File> result = instance.getFeatureTables();
        assertTrue(!result.isEmpty());
    }

    /**
     * Test of getMusicFileLists method, of class ExtractorConfigSet.
     */
    @Test
    public void testGetMusicFileLists() {
        System.out.println("getMusicFileLists");
        ExtractorConfigSet instance = null;
        try {
            instance = new ExtractorConfigSet(exampleExtracorConfigFile);
        } catch (IOException ex) {
            fail("Unable to open " + exampleExtracorConfigFile.getName());
        }
        List<File> result = instance.getMusicFileLists();
        assertTrue(!result.isEmpty());
    }

}