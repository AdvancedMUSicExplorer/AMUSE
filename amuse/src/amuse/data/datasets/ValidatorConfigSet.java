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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import amuse.data.MetricTable;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.validator.ValidationConfiguration;

/**
 * This class represents a list of ValidationTasks as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id$
 */
public class ValidatorConfigSet extends AbstractArffExperimentSet {

	// Strings which describe ARFF attributes
    private static final String strValidationMethodId = "ValidationMethodId";
    private static final String strMetricList = "MetricList";
    private static final String strProcessedFeatureDescription = "ProcessedFeaturesDescription";
    private static final String strInputToValidate = "InputToValidate";
    private static final String strGroundTruthSourceType = "GroundTruthSourceType";
    private static final String strClassificationAlgorithmId = "ClassificationAlgorithmId";
    
	// ARFF attributes
	private final StringAttribute validationMethodIdAttribute;
    private final StringAttribute metricListAttribute;
    private final StringAttribute processedFeatureDescriptionAttribute;
    private final StringAttribute inputToValidateAttribute;
    private final NominalAttribute groundTruthSourceTypeAttribute;
    private final StringAttribute classificationAlgorithmIdAttribute;
    
    private String description = "";

    /**
     * Creates a new ValidatorConfigSet from a file. Validates if the given file contains a ValidatorConfigSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid ValidatorConfigSet.
     */
    public ValidatorConfigSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        checkStringAttribute(strValidationMethodId);
        checkStringAttribute(strMetricList);
        checkStringAttribute(strProcessedFeatureDescription);
        checkStringAttribute(strInputToValidate);
        checkNominalAttribute(strGroundTruthSourceType);
        checkStringAttribute(strClassificationAlgorithmId);
        validationMethodIdAttribute = (StringAttribute) getAttribute(strValidationMethodId);
        metricListAttribute = (StringAttribute) getAttribute(strMetricList);
        processedFeatureDescriptionAttribute = (StringAttribute) getAttribute(strProcessedFeatureDescription);
        inputToValidateAttribute = (StringAttribute) getAttribute(strInputToValidate);
        groundTruthSourceTypeAttribute = (NominalAttribute) getAttribute(strGroundTruthSourceType);
        classificationAlgorithmIdAttribute = (StringAttribute) getAttribute(strClassificationAlgorithmId);
    }

    public ValidatorConfigSet(  String validationMethodId,
                                File metricListFile,
                                String processedFeatureDescription,
                                String inputToValidate,
                                String groundTruthSourceType,
                                String classificationAlgorithmId) {
        super("ValidatorConfig");
        validationMethodIdAttribute = StringAttribute.createFromString(strValidationMethodId, validationMethodId);
        metricListAttribute = StringAttribute.createFromString(strMetricList, metricListFile.getAbsolutePath());
        processedFeatureDescriptionAttribute = StringAttribute.createFromString(strProcessedFeatureDescription, processedFeatureDescription);
        inputToValidateAttribute = StringAttribute.createFromString(strInputToValidate, inputToValidate);
        List <String> values = new ArrayList<String>();
        values.add(groundTruthSourceType);
        groundTruthSourceTypeAttribute = new NominalAttribute(strGroundTruthSourceType, getAllowedValues(), values);
        classificationAlgorithmIdAttribute = StringAttribute.createFromString(strClassificationAlgorithmId, classificationAlgorithmId);
        addAttribute(validationMethodIdAttribute);
        addAttribute(metricListAttribute);
        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(inputToValidateAttribute); 
        addAttribute(groundTruthSourceTypeAttribute);
        addAttribute(classificationAlgorithmIdAttribute);
    }

    public ValidatorConfigSet(DataSetAbstract dataSet) {
        super(dataSet.getName());
        // Check preconditions:
        dataSet.checkStringAttribute(strValidationMethodId);
        dataSet.checkStringAttribute(strMetricList);
        dataSet.checkStringAttribute(strProcessedFeatureDescription);
        dataSet.checkStringAttribute(strInputToValidate);
        dataSet.checkNominalAttribute(strGroundTruthSourceType);
        dataSet.checkStringAttribute(strClassificationAlgorithmId);
        validationMethodIdAttribute = (StringAttribute) dataSet.getAttribute(strValidationMethodId);
        metricListAttribute = (StringAttribute) dataSet.getAttribute(strMetricList);
        processedFeatureDescriptionAttribute = (StringAttribute) dataSet.getAttribute(strProcessedFeatureDescription);
        inputToValidateAttribute = (StringAttribute) dataSet.getAttribute(strInputToValidate);
        groundTruthSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(strGroundTruthSourceType);
        classificationAlgorithmIdAttribute = (StringAttribute) dataSet.getAttribute(strClassificationAlgorithmId);
        addAttribute(validationMethodIdAttribute);
        addAttribute(metricListAttribute);
        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(inputToValidateAttribute);
        addAttribute(groundTruthSourceTypeAttribute);
        addAttribute(classificationAlgorithmIdAttribute);
    }

    public String getType() {
        return "Validation";
    }

    public String getDescription() {
        if (description.equals("")) {
            int metrics;
            try {
                metrics = new MetricTable(new File(metricListAttribute.getValueAt(0))).size();
            } catch (IOException ex) {
                description = "WARNING: Metric selection seems to be broken";
                return description;
            }
            String input = groundTruthSourceTypeAttribute.getValueAt(0).toString();
            description = "Input: " + input + " Generating " + metrics + " metric(s)";
        }
        return description;
    }

    public StringAttribute getValidationMethodIdAttribute() {
        return validationMethodIdAttribute;
    }

    public NominalAttribute getGroundTruthSourceAttribute() {
        return groundTruthSourceTypeAttribute;
    }

    public StringAttribute getClassificationAlgorithmIdAttribute() {
        return classificationAlgorithmIdAttribute;
    }

    public StringAttribute getMetricListAttribute() {
        return metricListAttribute;
    }

    @Override
    public TaskConfiguration[] getTaskConfiguration() {
    	try {
    		return ValidationConfiguration.loadConfigurationsFromDataSet(this);
    	} catch (IOException ex) {
    		throw new RuntimeException(ex);
    	}
    }

	/**
	 * @return the processedFeatureDescriptionAttribute
	 */
	public StringAttribute getProcessedFeatureDescriptionAttribute() {
		return processedFeatureDescriptionAttribute;
	}
	
	private static List<String> getAllowedValues() {
		 List<String> allowedValues = new ArrayList<String>();
		 allowedValues.add("CATEGORY_ID");
		 allowedValues.add("FILE_LIST");
	     allowedValues.add("READY_INPUT");
	     return allowedValues;
	}

	/**
	 * @return the inputToValidateAttribute
	 */
	public StringAttribute getInputToValidateAttribute() {
		return inputToValidateAttribute;
	}
}
