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
 * Creation date: 19.01.2010
 */
package amuse.data.datasets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import amuse.data.ModelType.RelationshipType;
import amuse.data.InputFeatureType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.nodes.classifier.ClassificationConfiguration.InputSourceType;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetException;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;

/**
 * This class represents a list of classification tasks as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id: ClassifierConfigSet.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class ClassifierConfigSet extends AbstractArffExperimentSet {

	// Strings which describe ARFF attributes
	private static final String strInputSource = "InputSource";
	private static final String strInputSourceType = "InputSourceType";
	private static final String strAttributesToIgnore  = "AttributesToIgnore";
	private static final String strInputFeatures = "InputFeatures";
	private static final String strInputFeatureType = "InputFeatureType";
	private static final String strUnit = "Unit";
	private static final String strClassificationWindowSize = "ClassificationWindowSize";
	private static final String strClassificationWindowStepSize = "ClassificationWindowStepSize";
	private static final String strTrainingAlgorithmID = "AlgorithmId";
	private static final String strGroundTruthCategoryId = "GroundTruthCategoryId";
	private static final String strAttributesToPredict = "AttributesToPredict";
	private static final String strPathToInputModel = "PathToInputModel";
	private static final String strRelationshipType = "RelationshipType";
	private static final String strLabelType = "LabelType";
	private static final String strMethodType = "MethodType";
	private static final String strMergeTrackResults = "MergeTrackResults";
	private static final String strOutputResult = "OutputResult";
	private static final String strTrainingDescription = "TrainingDescription";
	
	private static final String strDataSetName = "ClassifierConfiguration";

	// ARFF attributes
	private final StringAttribute inputSourceAttribute;
	private final NominalAttribute inputSourceTypeAttribute;
	private final StringAttribute attributesToIgnoreAttribute;
	private final StringAttribute inputFeaturesAttribute;
	private final NominalAttribute inputFeatureTypeAttribute;
	private final NominalAttribute unitAttribute;
	private final NumericAttribute classificationWindowSizeAttribute;
	private final NumericAttribute classificationWindowStepSizeAttribute;
	private final StringAttribute classificationAlgorithmIdAttribute;
	private final NumericAttribute groundTruthCategoryIdAttribute;
	private final StringAttribute attributesToPredictAttribute;
	private final StringAttribute pathToInputModelAttribute;
	private final NominalAttribute relationshipTypeAttribute;
	private final NominalAttribute labelTypeAttribute;
	private final NominalAttribute methodTypeAttribute;
	private final NumericAttribute mergeTrackResultsAttribute;
	private final StringAttribute outputResultAttribute;
	private final StringAttribute trainingDescriptionAttribute;

	private String description = "";

	/**
	 * Creates a new ClassifierConfigSet from a given DataSet 
	 * @param dataSet Given DataSet
	 */
	public ClassifierConfigSet(DataSetAbstract dataSet) throws DataSetException {
		super(dataSet.getName());
		// Check preconditions:
		dataSet.checkStringAttribute(strInputSource);
		dataSet.checkNominalAttribute(strInputSourceType);
		dataSet.checkStringAttribute(strInputFeatures);
		dataSet.checkNominalAttribute(strInputFeatureType);
		dataSet.checkNominalAttribute(strUnit);
		dataSet.checkNumericAttribute(strClassificationWindowSize);
		dataSet.checkNumericAttribute(strClassificationWindowStepSize);
		dataSet.checkStringAttribute(strTrainingAlgorithmID);
		dataSet.checkStringAttribute(strAttributesToPredict);
		dataSet.checkStringAttribute(strAttributesToIgnore);
		dataSet.checkNominalAttribute(strRelationshipType);
		dataSet.checkNominalAttribute(strLabelType);
		dataSet.checkNominalAttribute(strMethodType);
		dataSet.checkStringAttribute(strPathToInputModel);
		dataSet.checkNumericAttribute(strGroundTruthCategoryId);
		dataSet.checkNumericAttribute(strMergeTrackResults);
		dataSet.checkStringAttribute(strOutputResult);
		dataSet.checkStringAttribute(strTrainingDescription);
		
		inputSourceAttribute = (StringAttribute) dataSet.getAttribute(strInputSource);
		inputSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(strInputSourceType);
		inputFeaturesAttribute = (StringAttribute) dataSet.getAttribute(strInputFeatures);
		inputFeatureTypeAttribute = (NominalAttribute) dataSet.getAttribute(strInputFeatureType);
		unitAttribute = (NominalAttribute) dataSet.getAttribute(strUnit);
		classificationWindowSizeAttribute = (NumericAttribute) dataSet.getAttribute(strClassificationWindowSize);
		classificationWindowStepSizeAttribute = (NumericAttribute) dataSet.getAttribute(strClassificationWindowStepSize);
		classificationAlgorithmIdAttribute = (StringAttribute) dataSet.getAttribute(strTrainingAlgorithmID);
		groundTruthCategoryIdAttribute = (NumericAttribute) dataSet.getAttribute(strGroundTruthCategoryId);
		attributesToPredictAttribute = (StringAttribute) dataSet.getAttribute(strAttributesToPredict);
		attributesToIgnoreAttribute = (StringAttribute) dataSet.getAttribute(strAttributesToIgnore);
		relationshipTypeAttribute = (NominalAttribute) dataSet.getAttribute(strRelationshipType);
		labelTypeAttribute = (NominalAttribute) dataSet.getAttribute(strLabelType);
		methodTypeAttribute = (NominalAttribute) dataSet.getAttribute(strMethodType);
		pathToInputModelAttribute = (StringAttribute) dataSet.getAttribute(strPathToInputModel);
		mergeTrackResultsAttribute = (NumericAttribute) dataSet.getAttribute(strMergeTrackResults);
		outputResultAttribute = (StringAttribute) dataSet.getAttribute(strOutputResult);
		trainingDescriptionAttribute = (StringAttribute) dataSet.getAttribute(strTrainingDescription);
		
		addAttribute(inputSourceAttribute);
		addAttribute(inputSourceTypeAttribute);
		addAttribute(attributesToIgnoreAttribute);
		addAttribute(inputFeaturesAttribute);
		addAttribute(inputFeatureTypeAttribute);
		addAttribute(classificationWindowSizeAttribute);
		addAttribute(classificationWindowStepSizeAttribute);
		addAttribute(classificationAlgorithmIdAttribute);
		addAttribute(groundTruthCategoryIdAttribute);
		addAttribute(mergeTrackResultsAttribute);
		addAttribute(attributesToPredictAttribute);
		addAttribute(relationshipTypeAttribute);
		addAttribute(labelTypeAttribute);
		addAttribute(methodTypeAttribute);
		addAttribute(outputResultAttribute);
		addAttribute(trainingDescriptionAttribute);
	}


	/**
	 * Creates a new ClassifierConfigSet from a file. Validates if the given file contains a ClassifierConfigSet.
	 * @param file The file to load form.
	 * @throws java.io.IOException Thrown whenever given file does not represent a valid ClassifierConfigSet.
	 */
	public ClassifierConfigSet(File file) throws IOException {
		super(file);
		// Check preconditions:
		checkStringAttribute(strInputSource);
		checkNominalAttribute(strInputSourceType);
		checkStringAttribute(strInputFeatures);
		checkNominalAttribute(strInputFeatureType);
		checkNominalAttribute(strUnit);
		checkNumericAttribute(strClassificationWindowSize);
		checkNumericAttribute(strClassificationWindowStepSize);
		checkStringAttribute(strTrainingAlgorithmID);
		checkNumericAttribute(strGroundTruthCategoryId);
		checkStringAttribute(strAttributesToPredict);
		checkStringAttribute(strAttributesToIgnore);
		checkNominalAttribute(strRelationshipType);
		checkNominalAttribute(strLabelType);
		checkNominalAttribute(strMethodType);
		checkStringAttribute(strPathToInputModel);
		checkNumericAttribute(strMergeTrackResults);
		checkStringAttribute(strOutputResult);
		checkStringAttribute(strTrainingDescription);
		
		inputSourceAttribute = (StringAttribute) getAttribute(strInputSource);
		inputSourceTypeAttribute = (NominalAttribute) getAttribute(strInputSourceType);
		inputFeaturesAttribute = (StringAttribute) getAttribute(strInputFeatures);
		inputFeatureTypeAttribute = (NominalAttribute) getAttribute(strInputFeatureType);
		unitAttribute = (NominalAttribute) getAttribute(strUnit);
		classificationWindowSizeAttribute = (NumericAttribute) getAttribute(strClassificationWindowSize);
		classificationWindowStepSizeAttribute = (NumericAttribute) getAttribute(strClassificationWindowStepSize);
		classificationAlgorithmIdAttribute = (StringAttribute) getAttribute(strTrainingAlgorithmID);
		groundTruthCategoryIdAttribute = (NumericAttribute) getAttribute(strGroundTruthCategoryId);
		attributesToPredictAttribute = (StringAttribute) this.getAttribute(strAttributesToPredict);
		attributesToIgnoreAttribute = (StringAttribute) this.getAttribute(strAttributesToIgnore);
		relationshipTypeAttribute = (NominalAttribute) this.getAttribute(strRelationshipType);
		labelTypeAttribute = (NominalAttribute) this.getAttribute(strLabelType);
		methodTypeAttribute = (NominalAttribute) this.getAttribute(strMethodType);
		pathToInputModelAttribute = (StringAttribute) this.getAttribute(strPathToInputModel);
		mergeTrackResultsAttribute = (NumericAttribute) getAttribute(strMergeTrackResults);
		outputResultAttribute = (StringAttribute) getAttribute(strOutputResult);
		trainingDescriptionAttribute = (StringAttribute) getAttribute(strTrainingDescription);
	}

	public ClassifierConfigSet( List<File> inputFileList,
			List<String> inputSourceTypeList,
			List<String> attributesToIgnore,
			List<String> inputFeatures,
			List<String> inputFeatureTypes,
			List<String> units,
			List<Integer> classificationWindowSizes,
			List<Integer> classificationWindowStepSizes,
			List<String> algorithmIDs,
			List<Integer> groundTruthSources,
			List<String> attributesToPredict,
			List<String> relationshipTypes,
			List<String> labelTypes,
			List<String> methodTypes,
			List<Integer> mergeTrackResults,
			List<String> outputResultPaths,
			List<String> pathToInputModel,
			List<String> trainingDescriptions) {
		super(strDataSetName);
		List<String> files = new ArrayList<String>();
		for (File f: inputFileList)
			files.add(f.getAbsolutePath());
		inputSourceAttribute = new StringAttribute(strInputSource, files);
		inputSourceTypeAttribute = new NominalAttribute(strInputSourceType, getAllowedValues(), inputSourceTypeList);
		inputFeaturesAttribute = new StringAttribute(strInputFeatures, inputFeatures);
		List<String> inputFeatureTypeValues = new ArrayList<String>();
		for(InputFeatureType type : InputFeatureType.values()) {
			inputFeatureTypeValues.add(type.toString());
		}
		inputFeatureTypeAttribute = new NominalAttribute(strInputFeatureType, inputFeatureTypeValues, inputFeatureTypes);
		List<String> unitValues = new ArrayList<String>();
		for(Unit unit : Unit.values()) {
			unitValues.add(unit.toString());
		}
		unitAttribute = new NominalAttribute(strUnit, unitValues, units);
		classificationWindowSizeAttribute = NumericAttribute.createFromIntList(strClassificationWindowSize, classificationWindowSizes);
		classificationWindowStepSizeAttribute = NumericAttribute.createFromIntList(strClassificationWindowStepSize, classificationWindowStepSizes);
		classificationAlgorithmIdAttribute = new StringAttribute(strTrainingAlgorithmID, algorithmIDs);
		groundTruthCategoryIdAttribute = NumericAttribute.createFromIntList(strGroundTruthCategoryId, groundTruthSources);
		attributesToPredictAttribute = new StringAttribute(strAttributesToPredict, attributesToPredict);
		attributesToIgnoreAttribute = new StringAttribute(strAttributesToIgnore, attributesToIgnore);
		
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
		relationshipTypeAttribute = new NominalAttribute(strRelationshipType, relationshipTypeValues, relationshipTypes);
		labelTypeAttribute = new NominalAttribute(strLabelType, labelTypeValues, labelTypes);
		methodTypeAttribute = new NominalAttribute(strMethodType, methodTypeValues, methodTypes);
		pathToInputModelAttribute = new StringAttribute(strPathToInputModel, pathToInputModel);
		mergeTrackResultsAttribute = NumericAttribute.createFromIntList(strMergeTrackResults, mergeTrackResults);
		outputResultAttribute = new StringAttribute(strOutputResult, outputResultPaths);
		trainingDescriptionAttribute = new StringAttribute(strTrainingDescription, trainingDescriptions);
		
		addAttribute(inputSourceAttribute);
		addAttribute(inputSourceTypeAttribute);
		addAttribute(attributesToIgnoreAttribute);
		addAttribute(inputFeaturesAttribute);
		addAttribute(inputFeatureTypeAttribute);
		addAttribute(unitAttribute);
		addAttribute(classificationWindowSizeAttribute);
		addAttribute(classificationWindowStepSizeAttribute);
		addAttribute(classificationAlgorithmIdAttribute);
		addAttribute(groundTruthCategoryIdAttribute);
		addAttribute(attributesToPredictAttribute);
		addAttribute(pathToInputModelAttribute);
		addAttribute(relationshipTypeAttribute);
		addAttribute(labelTypeAttribute);
		addAttribute(methodTypeAttribute);
		addAttribute(mergeTrackResultsAttribute);
		addAttribute(outputResultAttribute);
		addAttribute(trainingDescriptionAttribute);
	}

	public ClassifierConfigSet( String inputSource,
			String inputSourceType,
			String attributesToIgnore,
			String inputFeatures,
			String inputFeatureType,
			String unit,
			int classificationWindowSize,
			int classificationWindowStepSize,
			String algorithmId,
			int groundTruthSource,
			String attributesToPredict,
			String relationshipType,
			String labelType,
			String methodType,
			int mergeTrackResults,
			String outputResultPath,
			String pathToInputModel,
			String trainingDescription) {
		super(strDataSetName);
		inputSourceAttribute = StringAttribute.createFromString(strInputSource, inputSource);
		List <String> values = new ArrayList<String>();
		values.add(inputSourceType);
		inputSourceTypeAttribute = new NominalAttribute(strInputSourceType, getAllowedValues(), values);
		inputFeaturesAttribute = StringAttribute.createFromString(strInputFeatures, inputFeatures);
		List<String> inputFeatureTypeValues = new ArrayList<String>();
		for(InputFeatureType type : InputFeatureType.values()) {
			inputFeatureTypeValues.add(type.toString());
		}
		List<String> inputFeatureTypes = new ArrayList<String>();
		inputFeatureTypes.add(inputFeatureType);
		inputFeatureTypeAttribute = new NominalAttribute(strInputFeatureType, inputFeatureTypeValues, inputFeatureTypes);
		List<String> unitValues = new ArrayList<String>();
		for(Unit value : Unit.values()) {
			unitValues.add(value.toString());
		}
		List<String> units = new ArrayList<String>();
		units.add(unit);
		unitAttribute = new NominalAttribute(strUnit, unitValues, units);
		classificationWindowSizeAttribute = NumericAttribute.createFromDouble(strClassificationWindowSize, classificationWindowSize);
		classificationWindowStepSizeAttribute = NumericAttribute.createFromDouble(strClassificationWindowStepSize, classificationWindowStepSize);
		classificationAlgorithmIdAttribute = StringAttribute.createFromString(strTrainingAlgorithmID, algorithmId);
		groundTruthCategoryIdAttribute = NumericAttribute.createFromDouble(strGroundTruthCategoryId, groundTruthSource);
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
		pathToInputModelAttribute = StringAttribute.createFromString(strPathToInputModel, pathToInputModel);
		mergeTrackResultsAttribute = NumericAttribute.createFromDouble(strMergeTrackResults, mergeTrackResults);
		outputResultAttribute = StringAttribute.createFromString(strOutputResult, outputResultPath);
		trainingDescriptionAttribute = StringAttribute.createFromString(strTrainingDescription, trainingDescription);
		
		addAttribute(inputSourceAttribute);
		addAttribute(inputSourceTypeAttribute);
		addAttribute(attributesToIgnoreAttribute);
		addAttribute(inputFeaturesAttribute);
		addAttribute(inputFeatureTypeAttribute);
		addAttribute(unitAttribute);
		addAttribute(classificationWindowSizeAttribute);
		addAttribute(classificationWindowStepSizeAttribute);
		addAttribute(classificationAlgorithmIdAttribute);
		addAttribute(groundTruthCategoryIdAttribute);
		addAttribute(attributesToPredictAttribute);
		addAttribute(pathToInputModelAttribute);
		addAttribute(relationshipTypeAttribute);
		addAttribute(labelTypeAttribute);
		addAttribute(methodTypeAttribute);
		addAttribute(mergeTrackResultsAttribute);
		addAttribute(outputResultAttribute);
		addAttribute(trainingDescriptionAttribute);
	}

	public List<File> getInputFileLists() {
		List<File> musicFileLists = new ArrayList<File>();
		for (int i = 0; i < inputSourceAttribute.getValueCount(); i++)
			musicFileLists.add(new File(inputSourceAttribute.getValueAt(i)));
		return musicFileLists;
	}

	public String getType() {
		return "Classification";
	}

	public String getDescription() {
		if (description.equals("")) {
			String processedFeatureStr = inputFeaturesAttribute.getValueAt(0);
			int groundTruthSource = groundTruthCategoryIdAttribute.getValueAt(0).intValue();
			String result = outputResultAttribute.getValueAt(0);
			description = "Features: " + processedFeatureStr + " Source: " + groundTruthSource + " Output: " + result;
		}
		return description;
	}

	public StringAttribute getInputFeaturesAttribute() {
		return inputFeaturesAttribute;
	}

	public StringAttribute getClassificationAlgorithmIdAttribute() {
		return classificationAlgorithmIdAttribute;
	}

	public NumericAttribute getGroundTruthSourceAttribute() {
		return groundTruthCategoryIdAttribute;
	}

	public NumericAttribute getMergeTrackResultsAttribute() {
		return mergeTrackResultsAttribute;
	}

	public StringAttribute getOutputResultAttribute() {
		return outputResultAttribute;
	}

	public StringAttribute getInputSourceAttribute() {
		return inputSourceAttribute;
	}

	public NominalAttribute getInputSourceTypeAttribute() {
		return inputSourceTypeAttribute;
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

	public StringAttribute getPathToInputModelAttribute() {
		return pathToInputModelAttribute;
	}
	
	public StringAttribute getTrainingDescriptionAttribute() {
		return trainingDescriptionAttribute;
	}

	@Override
	public TaskConfiguration[] getTaskConfiguration() {
		return null;
	}

	private static List<String> getAllowedValues() {
		List<String> allowedValues = new ArrayList<String>();
		for(InputSourceType value : InputSourceType.values()) {
			allowedValues.add(value.toString());
		}
		return allowedValues;
	}
	
	public NominalAttribute getInputFeatureTypeAttribute()	{
		return this.inputFeatureTypeAttribute;
	}
	
	public NominalAttribute getUnitAttribute() {
		return this.unitAttribute;
	}
	
	public NumericAttribute getClassificationWindowSizeAttribute() {
		return this.classificationWindowSizeAttribute;
	}
	
	public NumericAttribute getClassificationWindowStepSizeAttribute() {
		return this.classificationWindowStepSizeAttribute;
	}
}
