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
 * Creation date: 02.06.2010
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
 *
 * @author waeltken
 */
public class PluginTableSet extends ArffDataSet {

    private final String strID = "Id";
    private final String strName = "Name";
    private final String strVersionDescription = "VersionDescription";

    private final NumericAttribute idAttribute;
    private final StringAttribute nameAttribute;
    private final StringAttribute versionAttribute;

    public PluginTableSet(File file) throws IOException {
        super(file);
        checkNumericAttribute(strID);
        checkStringAttribute(strName);
        checkStringAttribute(strVersionDescription);
        idAttribute = (NumericAttribute) getAttribute(strID);
        nameAttribute = (StringAttribute) getAttribute(strName);
        versionAttribute = (StringAttribute) getAttribute(strVersionDescription);
    }

    public List<Integer> getIDs() {
        List<Integer> ids = new ArrayList<Integer>();
        for (Double val : idAttribute.getValues()) {
            ids.add(val.intValue());
        }
        return ids;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<String>();
        for (String val : nameAttribute.getValues()) {
            names.add(val);
        }
        return names;
    }

    public List<String> getVersionDescription() {
        List<String> versionDescriptions = new ArrayList<String>();
        for (String val : versionAttribute.getValues()) {
            versionDescriptions.add(val);
        }
        return versionDescriptions;
    }
}
