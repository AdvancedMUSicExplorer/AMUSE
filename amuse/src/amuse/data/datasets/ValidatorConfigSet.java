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

import amuse.data.InputFeatureType;
import amuse.data.MeasureTable;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.validator.ValidationConfiguration;

/**
 * This class represents a list of ValidationTasks as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id: ValidatorConfigSet.java 241 2018-07-26 12:35:24Z frederik-h $
 */
public class ValidatorConfigSet extends AbstractArffExperimentSet {

	// Strings which describe ARFF attributes
	private static final String strValidationMethodId = "ValidationMethodId";
	private static final String strMeasureList = "MeasureList";
	private static final String strInputFeatures = "InputFeatures";
	private static final String strInputFeatureType = "InputFeatureType";
	private static final String strUnit = "Unit";
	private static final String strClassificationWindowSize = "ClassificationWindowSize";
	private static final String strClassificationWindowStepSize = "ClassificationWindowStepSize";
	private static final String strInputToValidate = "InputToValidate";
	private static final String strGroundTruthSourceType = "GroundTruthSourceType";
	private static final String strClassificationAlgorithmId = "ClassificationAlgorithmId";
	private static final String strAttributesToPredict = "AttributesToPredict";
	private static final String strAttributesToIgnore  = "AttributesToIgnore";
	private static final String strRelationshipType = "RelationshipType";
	private static final String strLabelType = "LabelType";
	private static final String strMethodType = "MethodType";
	private static final String strOutputPath = "OutputPath";
	

	// ARFF attributes
	private final StringAttribute validationMethodIdAttribute;
	private final StringAttribute measureListAttribute;
	private final StringAttribute inputFeaturesAttribute;
	private final NominalAttribute inputFeatureTypeAttribute;
	private final NominalAttribute unitAttribute;
	private final NumericAttribute classificationWindowStepSizeAttribute;
	private final NumericAttribute classificationWindowSizeAttribute;
	private final StringAttribute inputToValidateAttribute;
	private final NominalAttribute groundTruthSourceTypeAttribute;
	private final StringAttribute classificationAlgorithmIdAttribute;
	private final StringAttribute attributesToPredictAttribute;
	private final StringAttribute attributesToIgnoreAttribute;
	private final NominalAttribute relationshipTypeAttribute;
	private final NominalAttribute labelTypeAttribute;
	private final NominalAttribute methodTypeAttribute;
	private final StringAttribute outputPathAttribute;
	

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
		checkStringAttribute(strMeasureList);
		checkStringAttribute(strInputFeatures);
		checkNominalAttribute(strInputFeatureType);
		checkNominalAttribute(strUnit);
		checkNumericAttribute(strClassificationWindowStepSize);
		checkNumericAttribute(strClassificationWindowSize);
		checkStringAttribute(strInputToValidate);
		checkNominalAttribute(strGroundTruthSourceType);
		checkStringAttribute(strClassificationAlgorithmId);
		checkStringAttribute(strAttributesToPredict);
		checkStringAttribute(strAttributesToIgnore);
		checkNominalAttribute(strRelationshipType);
		checkNominalAttribute(strLabelType);
		checkNominalAttribute(strMethodType);
		checkStringAttribute(strOutputPath);
		

		validationMethodIdAttribute = (StringAttribute) getAttribute(strValidationMethodId);
		measureListAttribute = (StringAttribute) getAttribute(strMeasureList);
		inputFeaturesAttribute = (StringAttribute) getAttribute(strInputFeatures);
		inputFeatureTypeAttribute = (NominalAttribute) getAttribute(strInputFeatureType);
		unitAttribute = (NominalAttribute) getAttribute(strUnit);
		classificationWindowSizeAttribute = (NumericAttribute) getAttribute(strClassificationWindowSize);
		classificationWindowStepSizeAttribute = (NumericAttribute) getAttribute(strClassificationWindowStepSize);
		inputToValidateAttribute = (StringAttribute) getAttribute(strInputToValidate);
		groundTruthSourceTypeAttribute = (NominalAttribute) getAttribute(strGroundTruthSourceType);
		classificationAlgorithmIdAttribute = (StringAttribute) getAttribute(strClassificationAlgorithmId);
		attributesToPredictAttribute = (StringAttribute) this.getAttribute(strAttributesToPredict);
		attributesToIgnoreAttribute = (StringAttribute) this.getAttribute(strAttributesToIgnore);
		relationshipTypeAttribute = (NominalAttribute) this.getAttribute(strRelationshipType);
		labelTypeAttribute = (NominalAttribute) this.getAttribute(strLabelType);
		methodTypeAttribute = (NominalAttribute) this.getAttribute(strMethodType);
		outputPathAttribute = (StringAttribute) this.getAttribute(strOutputPath);
		
	}

	public ValidatorConfigSet(String validationMethodId,
			File measureListFile,
			String inputFeatures,
			String inputFeatureType,
			String unit,
			Integer classificationWindowSize,
			Integer classificationWindowStepSize,
			String inputToValidate,
			String groundTruthSourceType,
			String attributesToPredict,
			String attributesToIgnore,
			String relationshipType,
			String labelType,
			String methodType,
			String classificationAlgorithmId,
			String outputPath) {
		super("ValidatorConfig");
		validationMethodIdAttribute = StringAttribute.createFromString(strValidationMethodId, validationMethodId);
		measureListAttribute = StringAttribute.createFromString(strMeasureList, measureListFile.getAbsolutePath());
		inputFeaturesAttribute = StringAttribute.createFromString(strInputFeatures, inputFeatures);
		List<String> inputFeatureTypeValues = new ArrayList<String>();
		for(InputFeatureType type : InputFeatureType.values()) {
			inputFeatureTypeValues.add(type.toString());
		}
		List<String> inputFeatureTypes = new ArrayList<String>();
		inputFeatureTypes.add(inputFeatureType);
		inputFeatureTypeAttribute = new NominalAttribute(strInputFeatureType, inputFeatureTypeValues, inputFeatureTypes);
		inputToValidateAttribute = StringAttribute.createFromString(strInputToValidate, inputToValidate);
		List<String> unitValues = new ArrayList<String>();
		for(Unit value : Unit.values()) {
			unitValues.add(value.toString());
		}
		List<String> units = new ArrayList<String>();
		units.add(unit);
		unitAttribute = new NominalAttribute(strUnit, unitValues, units);
		classificationWindowSizeAttribute = NumericAttribute.createFromDouble(strClassificationWindowSize, classificationWindowSize);
		classificationWindowStepSizeAttribute = NumericAttribute.createFromDouble(strClassificationWindowStepSize, classificationWindowStepSize);
		List <String> values = new ArrayList<String>();
		values.add(groundTruthSourceType);
		groundTruthSourceTypeAttribute = new NominalAttribute(strGroundTruthSourceType, getAllowedValues(), values);
		attributesToPredictAttribute = StringAttribute.createFromString(strAttributesToPredict, attributesToPredict);
		attributesToIgnoreAttribute = StringAttribute.createFromString(strAttributesToIgnore, attributesToIgnore);
		List<String> relationshipTypeValues = new ArrayList<String>();
		List<String> labelTypeValues = new ArrayList<String>();
		List<String> methodTypeValues = new ArrayList<String>();
		for(RelationshipType type : RelationshipType.values()) {
			relationshipTypeValues.add(type.toString());
		}
		for(LabelType type : LabelType.values()) {
			labelTypeValues.add(type.toString());
		}
		for(MethodType type : MethodType.values()) {
			methodTypeValues.add(type.toString());
		}
		List <String> relationshipTypes = new ArrayList<String>();
		relationshipTypes.add(relationshipType);
		List<String> labelTypes = new ArrayList<String>();
		labelTypes.add(labelType);
		List<String> methodTypes = new ArrayList<String>();
		methodTypes.add(methodType);
		relationshipTypeAttribute = new NominalAttribute(strRelationshipType, relationshipTypeValues, relationshipTypes);
		labelTypeAttribute = new NominalAttribute(strLabelType, labelTypeValues, labelTypes);
		methodTypeAttribute = new NominalAttribute(strMethodType, methodTypeValues, methodTypes);
		classificationAlgorithmIdAttribute = StringAttribute.createFromString(strClassificationAlgorithmId, classificationAlgorithmId);
		outputPathAttribute = StringAttribute.createFromString(strOutputPath, outputPath);
		
		addAttribute(validationMethodIdAttribute);
		addAttribute(measureListAttribute);
		addAttribute(inputFeaturesAttribute);
		addAttribute(inputFeatureTypeAttribute);
		addAttribute(unitAttribute);
		addAttribute(classificationWindowSizeAttribute);
		addAttribute(classificationWindowStepSizeAttribute);
		addAttribute(inputToValidateAttribute); 
		addAttribute(groundTruthSourceTypeAttribute);
		addAttribute(attributesToPredictAttribute);
		addAttribute(attributesToIgnoreAttribute);
		addAttribute(relationshipTypeAttribute);
		addAttribute(labelTypeAttribute);
		addAttribute(methodTypeAttribute);
		addAttribute(classificationAlgorithmIdAttribute);
		addAttribute(outputPathAttribute);
	}

	public ValidatorConfigSet(DataSetAbstract dataSet) {
		super(dataSet.getName());
		// Check preconditions:
		dataSet.checkStringAttribute(strValidationMethodId);
		dataSet.checkStringAttribute(strMeasureList);
		dataSet.checkStringAttribute(strInputFeatures);
		dataSet.checkNominalAttribute(strInputFeatureType);
		dataSet.checkNominalAttribute(strUnit);
		dataSet.checkNumericAttribute(strClassificationWindowSize);
		dataSet.checkNumericAttribute(strClassificationWindowStepSize);
		dataSet.checkStringAttribute(strInputToValidate);
		dataSet.checkNominalAttribute(strGroundTruthSourceType);
		dataSet.checkStringAttribute(strAttributesToPredict);
		dataSet.checkStringAttribute(strAttributesToIgnore);
		dataSet.checkNominalAttribute(strRelationshipType);
		dataSet.checkNominalAttribute(strLabelType);
		dataSet.checkNominalAttribute(strMethodType);
		dataSet.checkStringAttribute(strClassificationAlgorithmId);
		dataSet.checkStringAttribute(strOutputPath);
		
		validationMethodIdAttribute = (StringAttribute) dataSet.getAttribute(strValidationMethodId);
		measureListAttribute = (StringAttribute) dataSet.getAttribute(strMeasureList);
		inputFeaturesAttribute = (StringAttribute) dataSet.getAttribute(strInputFeatures);
		inputFeatureTypeAttribute = (NominalAttribute) dataSet.getAttribute(strInputFeatureType);
		unitAttribute = (NominalAttribute) dataSet.getAttribute(strUnit);
		classificationWindowSizeAttribute = (NumericAttribute) dataSet.getAttribute(strClassificationWindowSize);
		classificationWindowStepSizeAttribute = (NumericAttribute) dataSet.getAttribute(strClassificationWindowStepSize);
		inputToValidateAttribute = (StringAttribute) dataSet.getAttribute(strInputToValidate);
		groundTruthSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(strGroundTruthSourceType);
		attributesToPredictAttribute = (StringAttribute) dataSet.getAttribute(strAttributesToPredict);
		attributesToIgnoreAttribute = (StringAttribute) dataSet.getAttribute(strAttributesToIgnore);
		relationshipTypeAttribute = (NominalAttribute) dataSet.getAttribute(strRelationshipType);
		labelTypeAttribute = (NominalAttribute) dataSet.getAttribute(strLabelType);
		methodTypeAttribute = (NominalAttribute) dataSet.getAttribute(strMethodType);
		outputPathAttribute = (StringAttribute) dataSet.getAttribute(strOutputPath);

		classificationAlgorithmIdAttribute = (StringAttribute) dataSet.getAttribute(strClassificationAlgorithmId);
		addAttribute(validationMethodIdAttribute);
		addAttribute(measureListAttribute);
		addAttribute(inputFeaturesAttribute);
		addAttribute(inputFeatureTypeAttribute);
		addAttribute(classificationWindowStepSizeAttribute);
		addAttribute(classificationWindowSizeAttribute);
		addAttribute(inputToValidateAttribute);
		addAttribute(groundTruthSourceTypeAttribute);
		addAttribute(attributesToPredictAttribute);
		addAttribute(attributesToIgnoreAttribute);
		addAttribute(relationshipTypeAttribute);
		addAttribute(labelTypeAttribute);
		addAttribute(methodTypeAttribute);
		addAttribute(classificationAlgorithmIdAttribute);
		addAttribute(outputPathAttribute);
	}

	public String getType() {
		return "Validation";
	}

	public String getDescription() {
		if (description.equals("")) {
			int measures;
			try {
				measures = new MeasureTable(new File(measureListAttribute.getValueAt(0))).size();
			} catch (IOException ex) {
				description = "WARNING: Measure selection seems to be broken";
				return description;
			}
			String input = groundTruthSourceTypeAttribute.getValueAt(0).toString();
			description = "Input: " + input + " Generating " + measures + " measure(s)";
		}
		return description;
	}

	public StringAttribute getValidationMethodIdAttribute() {
		return validationMethodIdAttribute;
	}

	public NominalAttribute getGroundTruthSourceAttribute() {
		return groundTruthSourceTypeAttribute;
	}

	public StringAttribute getAttributesToPredictAttribute() {
		return attributesToPredictAttribute;
	}

	public StringAttribute getAttributesToIgnoreAttribute() {
		return attributesToIgnoreAttribute;
	}

	public NominalAttribute getRelationshipTypeAttribute() {
		return relationshipTypeAttribute;
	}
	
	public NominalAttribute getLabelTypeAttribute() {
		return labelTypeAttribute;
	}
	
	public NominalAttribute getMethodTypeAttribute() {
		return methodTypeAttribute;
	}

	public StringAttribute getClassificationAlgorithmIdAttribute() {
		return classificationAlgorithmIdAttribute;
	}

	public StringAttribute getMeasureListAttribute() {
		return measureListAttribute;
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
	 * @return the inputFeatures
	 */
	public StringAttribute getInputFeaturesAttribute() {
		return inputFeaturesAttribute;
	}
	
	public NominalAttribute getInputFeatureTypeAttribute() {
		return inputFeatureTypeAttribute;
	}
	
	public NominalAttribute getUnitAttribute() {
		return unitAttribute;
	}
	
	public NumericAttribute getClassificationWindowSizeAttribute() {
		return classificationWindowSizeAttribute;
	}
	
	public NumericAttribute getClassificationWindowStepSizeAttribute() {
		return classificationWindowStepSizeAttribute;
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
	
	/**
	 * @return the outputPathAttribute
	 */
	public StringAttribute getOutputPathAttribute() {
		return outputPathAttribute;
	}
}
