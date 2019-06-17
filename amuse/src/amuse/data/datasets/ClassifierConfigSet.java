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
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
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
	private static final String strInputFileList = "InputFileList";
	private static final String strInputSourceType = "InputSourceType";
	private static final String strAttributesToIgnore  = "AttributesToIgnore";
	private static final String strProcessedFeatureDescription = "ProcessedFeaturesDescription";
	private static final String strTrainingAlgorithmID = "AlgorithmId";
	private static final String strGroundTruthCategoryId = "GroundTruthCategoryId";
	private static final String strAttributesToClassify = "AttributesToClassify";
	private static final String strPathToInputModel = "PathToInputModel";
	private static final String strRelationshipType = "RelationshipType";
	private static final String strLabelType = "LabelType";
	private static final String strMethodType = "MethodType";
	private static final String strMergeSongResults = "MergeSongResults";
	private static final String strOutputResult = "OutputResult";
	private static final String strTrainingDescription = "TrainingDescription";
	
	private static final String strDataSetName = "ClassifierConfiguration";

	// ARFF attributes
	private final StringAttribute inputFileListAttribute;
	private final NominalAttribute inputSourceTypeAttribute;
	private final StringAttribute attributesToIgnoreAttribute;
	private final StringAttribute processedFeatureDescriptionAttribute;
	private final StringAttribute classificationAlgorithmIdAttribute;
	private final NumericAttribute groundTruthCategoryIdAttribute;
	private final StringAttribute attributesToClassifyAttribute;
	private final NominalAttribute relationshipTypeAttribute;
	private final NominalAttribute labelTypeAttribute;
	private final NominalAttribute methodTypeAttribute;
	private final StringAttribute pathToInputModelAttribute;
	private final NumericAttribute mergeSongResultsAttribute;
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
		dataSet.checkStringAttribute(strInputFileList);
		dataSet.checkNominalAttribute(strInputSourceType);
		dataSet.checkStringAttribute(strProcessedFeatureDescription);
		dataSet.checkStringAttribute(strTrainingAlgorithmID);

		dataSet.checkStringAttribute(strAttributesToClassify);
		dataSet.checkStringAttribute(strAttributesToIgnore);
		dataSet.checkNominalAttribute(strRelationshipType);
		dataSet.checkNominalAttribute(strLabelType);
		dataSet.checkNominalAttribute(strMethodType);
		dataSet.checkStringAttribute(strPathToInputModel);
		dataSet.checkNumericAttribute(strGroundTruthCategoryId);
		dataSet.checkNumericAttribute(strMergeSongResults);
		dataSet.checkStringAttribute(strOutputResult);
		dataSet.checkStringAttribute(strTrainingDescription);
		
		inputFileListAttribute = (StringAttribute) dataSet.getAttribute(strInputFileList);
		inputSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(strInputSourceType);
		processedFeatureDescriptionAttribute = (StringAttribute) dataSet.getAttribute(strProcessedFeatureDescription);
		classificationAlgorithmIdAttribute = (StringAttribute) dataSet.getAttribute(strTrainingAlgorithmID);
		groundTruthCategoryIdAttribute = (NumericAttribute) dataSet.getAttribute(strGroundTruthCategoryId);
		attributesToClassifyAttribute = (StringAttribute) dataSet.getAttribute(strAttributesToClassify);
		attributesToIgnoreAttribute = (StringAttribute) dataSet.getAttribute(strAttributesToIgnore);
		relationshipTypeAttribute = (NominalAttribute) dataSet.getAttribute(strRelationshipType);
		labelTypeAttribute = (NominalAttribute) dataSet.getAttribute(strLabelType);
		methodTypeAttribute = (NominalAttribute) dataSet.getAttribute(strMethodType);
		pathToInputModelAttribute = (StringAttribute) dataSet.getAttribute(strPathToInputModel);
		mergeSongResultsAttribute = (NumericAttribute) dataSet.getAttribute(strMergeSongResults);
		outputResultAttribute = (StringAttribute) dataSet.getAttribute(strOutputResult);
		trainingDescriptionAttribute = (StringAttribute) dataSet.getAttribute(strTrainingDescription);
		
		addAttribute(inputFileListAttribute);
		addAttribute(inputSourceTypeAttribute);
		addAttribute(attributesToIgnoreAttribute);
		addAttribute(processedFeatureDescriptionAttribute);
		addAttribute(classificationAlgorithmIdAttribute);
		addAttribute(groundTruthCategoryIdAttribute);
		addAttribute(mergeSongResultsAttribute);
		addAttribute(attributesToClassifyAttribute);
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
		checkStringAttribute(strInputFileList);
		checkNominalAttribute(strInputSourceType);
		checkStringAttribute(strProcessedFeatureDescription);
		checkStringAttribute(strTrainingAlgorithmID);
		checkNumericAttribute(strGroundTruthCategoryId);
		checkStringAttribute(strAttributesToClassify);
		checkStringAttribute(strAttributesToIgnore);
		checkNominalAttribute(strRelationshipType);
		checkNominalAttribute(strLabelType);
		checkNominalAttribute(strMethodType);
		checkStringAttribute(strPathToInputModel);
		checkNumericAttribute(strMergeSongResults);
		checkStringAttribute(strOutputResult);
		checkStringAttribute(strTrainingDescription);
		
		inputFileListAttribute = (StringAttribute) getAttribute(strInputFileList);
		inputSourceTypeAttribute = (NominalAttribute) getAttribute(strInputSourceType);
		processedFeatureDescriptionAttribute = (StringAttribute) getAttribute(strProcessedFeatureDescription);
		classificationAlgorithmIdAttribute = (StringAttribute) getAttribute(strTrainingAlgorithmID);
		groundTruthCategoryIdAttribute = (NumericAttribute) getAttribute(strGroundTruthCategoryId);
		attributesToClassifyAttribute = (StringAttribute) this.getAttribute(strAttributesToClassify);
		attributesToIgnoreAttribute = (StringAttribute) this.getAttribute(strAttributesToIgnore);
		relationshipTypeAttribute = (NominalAttribute) this.getAttribute(strRelationshipType);
		labelTypeAttribute = (NominalAttribute) this.getAttribute(strLabelType);
		methodTypeAttribute = (NominalAttribute) this.getAttribute(strMethodType);
		pathToInputModelAttribute = (StringAttribute) this.getAttribute(strPathToInputModel);
		mergeSongResultsAttribute = (NumericAttribute) getAttribute(strMergeSongResults);
		outputResultAttribute = (StringAttribute) getAttribute(strOutputResult);
		trainingDescriptionAttribute = (StringAttribute) getAttribute(strTrainingDescription);
	}

	public ClassifierConfigSet( List<File> inputFileList,
			List<String> inputSourceTypeList,
			List<String> attributesToIgnore,
			List<String> processedFeatureDescription,
			List<String> algorithmIDs,
			List<Integer> groundTruthSources,
			List<String> attributesToClassify,
			List<String> relationshipTypes,
			List<String> labelTypes,
			List<String> methodTypes,
			List<Integer> mergeSongResults,
			List<String> outputResultPaths,
			List<String> pathToInputModel,
			List<String> trainingDescriptions) {
		super(strDataSetName);
		List<String> files = new ArrayList<String>();
		for (File f: inputFileList)
			files.add(f.getAbsolutePath());
		inputFileListAttribute = new StringAttribute(strInputFileList, files);
		inputSourceTypeAttribute = new NominalAttribute(strInputSourceType, getAllowedValues(), inputSourceTypeList);
		processedFeatureDescriptionAttribute = new StringAttribute(strProcessedFeatureDescription, processedFeatureDescription);
		classificationAlgorithmIdAttribute = new StringAttribute(strTrainingAlgorithmID, algorithmIDs);
		groundTruthCategoryIdAttribute = NumericAttribute.createFromIntList(strGroundTruthCategoryId, groundTruthSources);
		attributesToClassifyAttribute = new StringAttribute(strAttributesToClassify, attributesToClassify);
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
		mergeSongResultsAttribute = NumericAttribute.createFromIntList(strMergeSongResults, mergeSongResults);
		outputResultAttribute = new StringAttribute(strOutputResult, outputResultPaths);
		trainingDescriptionAttribute = new StringAttribute(strTrainingDescription, trainingDescriptions);
		
		addAttribute(inputFileListAttribute);
		addAttribute(inputSourceTypeAttribute);
		addAttribute(attributesToIgnoreAttribute);
		addAttribute(processedFeatureDescriptionAttribute);
		addAttribute(classificationAlgorithmIdAttribute);
		addAttribute(groundTruthCategoryIdAttribute);
		addAttribute(attributesToClassifyAttribute);
		addAttribute(relationshipTypeAttribute);
		addAttribute(labelTypeAttribute);
		addAttribute(methodTypeAttribute);
		addAttribute(mergeSongResultsAttribute);
		addAttribute(outputResultAttribute);
		addAttribute(pathToInputModelAttribute);
		addAttribute(trainingDescriptionAttribute);
	}

	public ClassifierConfigSet( File inputFile,
			String inputSourceType,
			String attributesToIgnore,
			String processedFeatureDescription,
			String algorithmId,
			int groundTruthSource,
			String attributesToClassify,
			String relationshipType,
			String labelType,
			String methodType,
			int mergeSongResults,
			String outputResultPath,
			String pathToInputModel,
			String trainingDescription) {
		super(strDataSetName);
		inputFileListAttribute = StringAttribute.createFromString(strInputFileList, inputFile.getAbsolutePath());
		List <String> values = new ArrayList<String>();
		values.add(inputSourceType);
		inputSourceTypeAttribute = new NominalAttribute(strInputSourceType, getAllowedValues(), values);
		processedFeatureDescriptionAttribute = StringAttribute.createFromString(strProcessedFeatureDescription, processedFeatureDescription);
		classificationAlgorithmIdAttribute = StringAttribute.createFromString(strTrainingAlgorithmID, algorithmId);
		groundTruthCategoryIdAttribute = NumericAttribute.createFromDouble(strGroundTruthCategoryId, groundTruthSource);
		attributesToClassifyAttribute = StringAttribute.createFromString(strAttributesToClassify, attributesToClassify);
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
		mergeSongResultsAttribute = NumericAttribute.createFromDouble(strMergeSongResults, mergeSongResults);
		outputResultAttribute = StringAttribute.createFromString(strOutputResult, outputResultPath);
		trainingDescriptionAttribute = StringAttribute.createFromString(strTrainingDescription, trainingDescription);
		
		addAttribute(inputFileListAttribute);
		addAttribute(inputSourceTypeAttribute);
		addAttribute(attributesToIgnoreAttribute);
		addAttribute(processedFeatureDescriptionAttribute);
		addAttribute(classificationAlgorithmIdAttribute);
		addAttribute(groundTruthCategoryIdAttribute);
		addAttribute(attributesToClassifyAttribute);
		addAttribute(relationshipTypeAttribute);
		addAttribute(labelTypeAttribute);
		addAttribute(methodTypeAttribute);
		addAttribute(mergeSongResultsAttribute);
		addAttribute(outputResultAttribute);
		addAttribute(pathToInputModelAttribute);
		addAttribute(trainingDescriptionAttribute);
	}

	public List<File> getInputFileLists() {
		List<File> musicFileLists = new ArrayList<File>();
		for (int i = 0; i < inputFileListAttribute.getValueCount(); i++)
			musicFileLists.add(new File(inputFileListAttribute.getValueAt(i)));
		return musicFileLists;
	}

	public String getType() {
		return "Classification";
	}

	public String getDescription() {
		if (description.equals("")) {
			String processedFeatureStr = processedFeatureDescriptionAttribute.getValueAt(0);
			int groundTruthSource = groundTruthCategoryIdAttribute.getValueAt(0).intValue();
			String result = outputResultAttribute.getValueAt(0);
			description = "Features: " + processedFeatureStr + " Source: " + groundTruthSource + " Output: " + result;
		}
		return description;
	}

	public StringAttribute getProcessedFeatureDescriptionAttribute() {
		return processedFeatureDescriptionAttribute;
	}

	public StringAttribute getClassificationAlgorithmIdAttribute() {
		return classificationAlgorithmIdAttribute;
	}

	public NumericAttribute getGroundTruthSourceAttribute() {
		return groundTruthCategoryIdAttribute;
	}

	public NumericAttribute getMergeSongResultsAttribute() {
		return mergeSongResultsAttribute;
	}

	public StringAttribute getOutputResultAttribute() {
		return outputResultAttribute;
	}

	public StringAttribute getInputFileListAttribute() {
		return inputFileListAttribute;
	}

	public NominalAttribute getInputSourceTypeAttribute() {
		return inputSourceTypeAttribute;
	}


	public StringAttribute getAttributesToClassifyAttribute() {
		return attributesToClassifyAttribute;
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
		allowedValues.add("FILE_LIST");
		allowedValues.add("READY_INPUT");
		return allowedValues;
	}
}
