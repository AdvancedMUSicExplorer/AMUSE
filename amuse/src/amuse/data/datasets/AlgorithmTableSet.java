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
 * Creation date: 28.04.2009
 */
package amuse.data.datasets;

import java.io.File;
import java.io.IOException;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetException;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;

/**
 * This class represents a list of algorithms as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id$
 */
public class AlgorithmTableSet extends ArffDataSet {

    private final NumericAttribute idAttribute;
    private final StringAttribute nameAttribute;
    private final NominalAttribute categoryAttribute;
    private final StringAttribute algorithmDescriptionAttribute;
    private final StringAttribute parameterNamesAttribute;
    private final StringAttribute parameterDefinitionsAttribute;
    private final StringAttribute defaultParameterValuesAttribute;
    private final StringAttribute parameterDescriptionsAttribute;

    private static final String strID = "Id";
    private static final String strName = "Name";
    private static final String strCategory = "Category";
    private static final String strAlgorithmDescription = "AlgorithmDescription";
    private static final String strParameterNames = "ParameterNames";
    private static final String strParameterDefinitions = "ParameterDefinitions";
    private static final String strDefaultParameterValues = "DefaultParameterValues";
    private static final String strParameterDescriptions = "ParameterDescriptions";

    /**
     * Creates a new FileTableSet from a file. Validates if the given file contains a FileTableSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid FileTableSet.
     */
    public AlgorithmTableSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        if (!this.getAttributeNames().contains(strID) || !(this.getAttribute(strID) instanceof NumericAttribute)) {
            throw new IOException("No "+strID+" Attribute!");
        }
        if (this.getAttributeNames().contains(strCategory) && this.getAttribute(strCategory) instanceof NominalAttribute){
        	categoryAttribute = (NominalAttribute) this.getAttribute(strCategory);
        } else {
        	categoryAttribute = null;
        }
        checkStringAttribute(strName);
        checkStringAttribute(strAlgorithmDescription);
        checkStringAttribute(strParameterNames);
        checkStringAttribute(strParameterDefinitions);
        checkStringAttribute(strDefaultParameterValues);
        checkStringAttribute(strParameterDescriptions);
        idAttribute = (NumericAttribute) this.getAttribute(strID);
        nameAttribute = (StringAttribute) this.getAttribute(strName);
        algorithmDescriptionAttribute = (StringAttribute) this.getAttribute(strAlgorithmDescription);
        parameterNamesAttribute = (StringAttribute) this.getAttribute(strParameterNames);
        parameterDefinitionsAttribute = (StringAttribute) this.getAttribute(strParameterDefinitions);
        defaultParameterValuesAttribute = (StringAttribute) this.getAttribute(strDefaultParameterValues);
        parameterDescriptionsAttribute = (StringAttribute) this.getAttribute(strParameterDescriptions);
    }

    /**
     * @return the idAttribute
     */
    public NumericAttribute getIdAttribute() {
        return idAttribute;
    }

    /**
     * @return the nameAttribute
     */
    public StringAttribute getNameAttribute() {
        return nameAttribute;
    }

    /**
     * @return the algorithmDescriptionAttribute
     */
    public StringAttribute getAlgorithmDescriptionAttribute() {
        return algorithmDescriptionAttribute;
    }

    /**
     * @return the parameterDefinitionsAttribute
     */
    public StringAttribute getParameterDefinitionsAttribute() {
        return parameterDefinitionsAttribute;
    }

    /**
     * @return the defaultParameterValuesAttribute
     */
    public StringAttribute getDefaultParameterValuesAttribute() {
        return defaultParameterValuesAttribute;
    }

    /**
     * @return the parameterDescriptionsAttribute
     */
    public StringAttribute getParameterDescriptionsAttribute() {
        return parameterDescriptionsAttribute;
    }

    /**
     * @return the parameterNamesAttribute
     */
    public StringAttribute getParameterNamesAttribute() {
        return parameterNamesAttribute;
    }

    /**
	 * @return the categoryAttribute
	 */
	public NominalAttribute getCategoryAttribute() throws DataSetException {
		if (categoryAttribute != null) {
		return categoryAttribute;
		} else {
			throw new DataSetException("This Algorithm Table does not have categories.");
		}
	}
}
