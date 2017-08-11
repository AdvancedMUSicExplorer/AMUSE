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
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.optimizer.OptimizationConfiguration;

/**
 * This class represents a list of optimization tasks as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id$
 */
public class OptimizerConfigSet extends AbstractArffExperimentSet {

	// Strings which describe ARFF attributes
	private static final String strCategoryId = "CategoryId";
    private static final String strCategoryOptimizationIdAttribute = "CategoryOptimizationId";
    private static final String strCategoryTestIdAttribute = "CategoryTestId";
    private static final String strAlgorithmIdAttribute = "AlgorithmId";
    private static final String strContinueOldExperimentFromAttribute = "ContinueOldExperimentFrom";
    private static final String strDestinationFolderAttribute = "DestinationFolder";
    
	// ARFF attributes
	private final StringAttribute categoryIdAttribute;
    private final StringAttribute categoryOptimizationIdAttribute;
    private final StringAttribute categoryTestIdAttribute;
    private final StringAttribute algorithmIdAttribute;
    private final StringAttribute continueOldExperimentFromAttribute;
    private final StringAttribute destinationFolderAttribute;
    
    /**
     * Creates a new ValidatorConfigSet from a file. Validates if the given file contains a ValidatorConfigSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid ValidatorConfigSet.
     */
    public OptimizerConfigSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        checkStringAttribute(strCategoryId);
        checkStringAttribute(strCategoryOptimizationIdAttribute);
        checkStringAttribute(strCategoryTestIdAttribute);
        checkStringAttribute(strAlgorithmIdAttribute);
        checkStringAttribute(strContinueOldExperimentFromAttribute);
        checkStringAttribute(strDestinationFolderAttribute);
        categoryIdAttribute = (StringAttribute) getAttribute(strCategoryId);
        categoryOptimizationIdAttribute = (StringAttribute) getAttribute(strCategoryOptimizationIdAttribute);
        categoryTestIdAttribute = (StringAttribute) getAttribute(strCategoryTestIdAttribute);
        algorithmIdAttribute = (StringAttribute) getAttribute(strAlgorithmIdAttribute);
        continueOldExperimentFromAttribute = (StringAttribute) getAttribute(strContinueOldExperimentFromAttribute);
        destinationFolderAttribute = (StringAttribute) getAttribute(strDestinationFolderAttribute);
    }

    public OptimizerConfigSet(DataSetAbstract dataSet) {
        super(dataSet.getName());
        // Check preconditions:
        checkNumericAttribute(strCategoryId);
        checkNumericAttribute(strCategoryOptimizationIdAttribute);
        checkNumericAttribute(strCategoryTestIdAttribute);
        checkStringAttribute(strAlgorithmIdAttribute);
        checkStringAttribute(strContinueOldExperimentFromAttribute);
        checkStringAttribute(strDestinationFolderAttribute);
        categoryIdAttribute = (StringAttribute) getAttribute(strCategoryId);
        categoryOptimizationIdAttribute = (StringAttribute) getAttribute(strCategoryOptimizationIdAttribute);
        categoryTestIdAttribute = (StringAttribute) getAttribute(strCategoryTestIdAttribute);
        algorithmIdAttribute = (StringAttribute) getAttribute(strAlgorithmIdAttribute);
        continueOldExperimentFromAttribute = (StringAttribute) getAttribute(strContinueOldExperimentFromAttribute);
        destinationFolderAttribute = (StringAttribute) getAttribute(strDestinationFolderAttribute);
        addAttribute(categoryIdAttribute);
        addAttribute(categoryOptimizationIdAttribute);
        addAttribute(categoryTestIdAttribute);
        addAttribute(algorithmIdAttribute);
        addAttribute(continueOldExperimentFromAttribute);
        addAttribute(destinationFolderAttribute);
    }

    @Override
    public TaskConfiguration[] getTaskConfiguration() {
    	try {
    		return OptimizationConfiguration.loadConfigurationsFromDataSet(this);
    	} catch (IOException ex) {
    		throw new RuntimeException(ex);
    	}
    }

	/**
	 * @return the categoryAttribute
	 */
	public StringAttribute getCategoryIdAttribute() {
		return categoryIdAttribute;
	}

	/**
	 * @return the categoryOptimizationIdAttribute
	 */
	public StringAttribute getCategoryOptimizationIdAttribute() {
		return categoryOptimizationIdAttribute;
	}

	/**
	 * @return the categoryTestIdAttribute
	 */
	public StringAttribute getCategoryTestIdAttribute() {
		return categoryTestIdAttribute;
	}

	/**
	 * @return the algorithmIdAttribute
	 */
	public StringAttribute getAlgorithmIdAttribute() {
		return algorithmIdAttribute;
	}

	/**
	 * @return the continueOldExperimentFromAttribute
	 */
	public StringAttribute getContinueOldExperimentFromAttribute() {
		return continueOldExperimentFromAttribute;
	}

	/**
	 * @return the destinationFolderAttribute
	 */
	public StringAttribute getDestinationFolderAttribute() {
		return destinationFolderAttribute;
	}

}
