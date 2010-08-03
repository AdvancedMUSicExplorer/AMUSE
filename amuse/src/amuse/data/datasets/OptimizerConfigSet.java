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
 * Creation date: 22.03.2010
 */
package amuse.data.datasets;

import java.io.File;
import java.io.IOException;

import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.optimizer.OptimizationConfiguration;

/**
 * This class represents a list of ValidationTasks as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id: $
 */
public class OptimizerConfigSet extends AbstractArffExperimentSet {

    private final StringAttribute idAttribute;
    private final StringAttribute nameAttribute;
    private final NominalAttribute categoryAttribute;
    private final StringAttribute homeFolderAttribute;
    private final StringAttribute startScriptAttribute;
    private final StringAttribute inputBaseTrainingBatchAttribute;
    private final StringAttribute inputTrainingBatchAttribute;
    private final StringAttribute algorithmDescriptionAttribute;
    private final StringAttribute parameterNamesAttribute;
    private final StringAttribute parameterDefinitionsAttribute;
    private final StringAttribute defaultParameterValuesAttribute;
    private final StringAttribute parameterDescriptionsAttribute;
    
    private static final String strId = "Id";
    private static final String strName = "Name";
    private static final String strCategory = "Category";
    private static final String strHomeFolder = "HomeFolder";
    private static final String strStartScript = "StartScript";
    private static final String strInputBaseTrainingBatch = "InputBaseTrainingBatch";
    private static final String strInputTrainingBatch = "InputTrainingBatch";
    private static final String strAlgorithmDescription = "AlgorithmDescription";
    private static final String strParameterNames = "ParameterNames";
    private static final String strParameterDefinitions = "ParameterDefinitions";
    private static final String strDefaultParameterValues = "DefaultParameterValues";
    private static final String strParameterDescriptions = "ParameterDescriptions";

    /**
     * Creates a new ValidatorConfigSet from a file. Validates if the given file contains a ValidatorConfigSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid ValidatorConfigSet.
     */
    public OptimizerConfigSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        checkStringAttribute(strId);
        checkStringAttribute(strName);
        checkNominalAttribute(strCategory);
        checkStringAttribute(strHomeFolder);
        checkStringAttribute(strStartScript);
        checkStringAttribute(strInputBaseTrainingBatch);
        checkStringAttribute(strInputTrainingBatch);
        checkStringAttribute(strAlgorithmDescription);
        checkStringAttribute(strParameterNames);
        checkStringAttribute(strParameterDefinitions);
        checkStringAttribute(strDefaultParameterValues);
        checkStringAttribute(strParameterDescriptions);
        idAttribute = (StringAttribute) getAttribute(strId);
        nameAttribute = (StringAttribute) getAttribute(strName);
        categoryAttribute = (NominalAttribute) getAttribute(strCategory);
        homeFolderAttribute = (StringAttribute) getAttribute(strHomeFolder);
        startScriptAttribute = (StringAttribute) getAttribute(strStartScript);
        inputBaseTrainingBatchAttribute = (StringAttribute) getAttribute(strInputBaseTrainingBatch);
        inputTrainingBatchAttribute = (StringAttribute) getAttribute(strInputTrainingBatch);
        algorithmDescriptionAttribute = (StringAttribute) getAttribute(strAlgorithmDescription);
        parameterNamesAttribute = (StringAttribute) getAttribute(strParameterNames);
        parameterDefinitionsAttribute = (StringAttribute) getAttribute(strParameterDefinitions);
        defaultParameterValuesAttribute = (StringAttribute) getAttribute(strDefaultParameterValues);
        parameterDescriptionsAttribute = (StringAttribute) getAttribute(strParameterDescriptions);

    }

    public OptimizerConfigSet(DataSetAbstract dataSet) {
        super(dataSet.getName());
        // Check preconditions:
        checkStringAttribute(strId);
        checkStringAttribute(strName);
        checkNominalAttribute(strCategory);
        checkStringAttribute(strHomeFolder);
        checkStringAttribute(strStartScript);
        checkStringAttribute(strInputBaseTrainingBatch);
        checkStringAttribute(strInputTrainingBatch);
        checkStringAttribute(strAlgorithmDescription);
        checkStringAttribute(strParameterNames);
        checkStringAttribute(strParameterDefinitions);
        checkStringAttribute(strDefaultParameterValues);
        checkStringAttribute(strParameterDescriptions);
        idAttribute = (StringAttribute) getAttribute(strId);
        nameAttribute = (StringAttribute) getAttribute(strName);
        categoryAttribute = (NominalAttribute) getAttribute(strCategory);
        homeFolderAttribute = (StringAttribute) getAttribute(strHomeFolder);
        startScriptAttribute = (StringAttribute) getAttribute(strStartScript);
        inputBaseTrainingBatchAttribute = (StringAttribute) getAttribute(strInputBaseTrainingBatch);
        inputTrainingBatchAttribute = (StringAttribute) getAttribute(strInputTrainingBatch);
        algorithmDescriptionAttribute = (StringAttribute) getAttribute(strAlgorithmDescription);
        parameterNamesAttribute = (StringAttribute) getAttribute(strParameterNames);
        parameterDefinitionsAttribute = (StringAttribute) getAttribute(strParameterDefinitions);
        defaultParameterValuesAttribute = (StringAttribute) getAttribute(strDefaultParameterValues);
        parameterDescriptionsAttribute = (StringAttribute) getAttribute(strParameterDescriptions);
        addAttribute(idAttribute);
        addAttribute(nameAttribute);
        addAttribute(categoryAttribute);
        addAttribute(homeFolderAttribute);
        addAttribute(startScriptAttribute);
        addAttribute(inputBaseTrainingBatchAttribute);
        addAttribute(inputTrainingBatchAttribute);
        addAttribute(algorithmDescriptionAttribute);
        addAttribute(parameterNamesAttribute);
        addAttribute(parameterDefinitionsAttribute);
        addAttribute(defaultParameterValuesAttribute);
        addAttribute(parameterDescriptionsAttribute);
    }

    @Override
    public TaskConfiguration[] getTaskConfiguration() {
	try {
	    return OptimizationConfiguration.loadConfigurationsFromDataSet(this);
	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	}
    }
}
