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

import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetException;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;

/**
 * This class represents a list of classification tasks as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id: $
 */
public class ClassifierConfigSet extends AbstractArffExperimentSet {

	// Strings which describe ARFF attributes
	private static final String strInputFileList = "InputFileList";
    private static final String strInputSourceType = "InputSourceType";
    private static final String strProcessedFeatureDescription = "ProcessedFeaturesDescription";
    private static final String strTrainingAlgorithmID = "AlgorithmId";
    private static final String strCategoryID = "CategoryId";
    private static final String strMergeSongResults = "MergeSongResults";
    private static final String strOutputResult = "OutputResult";
    private static final String strDataSetName = "ClassifierConfiguration";
    
    // ARFF attributes
	private final StringAttribute inputFileListAttribute;
    private final NominalAttribute inputSourceTypeAttribute;
    private final StringAttribute processedFeatureDescriptionAttribute;
    private final StringAttribute trainingAlgorithmIdAttribute;
    private final NumericAttribute categoryIdAttribute;
    private final NumericAttribute mergeSongResultsAttribute;
    private final StringAttribute outputResultAttribute;
    
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
        dataSet.checkNumericAttribute(strCategoryID);
        dataSet.checkNumericAttribute(strMergeSongResults);
        dataSet.checkStringAttribute(strOutputResult);
        inputFileListAttribute = (StringAttribute) dataSet.getAttribute(strInputFileList);
        inputSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(strInputSourceType);
        processedFeatureDescriptionAttribute = (StringAttribute) dataSet.getAttribute(strProcessedFeatureDescription);
        trainingAlgorithmIdAttribute = (StringAttribute) dataSet.getAttribute(strTrainingAlgorithmID);
        categoryIdAttribute = (NumericAttribute) dataSet.getAttribute(strCategoryID);
        mergeSongResultsAttribute = (NumericAttribute) dataSet.getAttribute(strMergeSongResults);
        outputResultAttribute = (StringAttribute) dataSet.getAttribute(strOutputResult);
        addAttribute(inputFileListAttribute);
        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(trainingAlgorithmIdAttribute);
        addAttribute(categoryIdAttribute);
        addAttribute(mergeSongResultsAttribute);
        addAttribute(outputResultAttribute);
        addAttribute(inputSourceTypeAttribute);
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
        checkNumericAttribute(strCategoryID);
        checkNumericAttribute(strMergeSongResults);
        checkStringAttribute(strOutputResult);
        inputFileListAttribute = (StringAttribute) getAttribute(strInputFileList);
        inputSourceTypeAttribute = (NominalAttribute) getAttribute(strInputSourceType);
        processedFeatureDescriptionAttribute = (StringAttribute) getAttribute(strProcessedFeatureDescription);
        trainingAlgorithmIdAttribute = (StringAttribute) getAttribute(strTrainingAlgorithmID);
        categoryIdAttribute = (NumericAttribute) getAttribute(strCategoryID);
        mergeSongResultsAttribute = (NumericAttribute) getAttribute(strMergeSongResults);
        outputResultAttribute = (StringAttribute) getAttribute(strOutputResult);
    }

    public ClassifierConfigSet( List<File> inputFileList,
    							List<String> inputSourceTypeList,
                                List<String> processedFeatureDescription,
                                List<String> algorithmIDs,
                                List<Integer> categoryIDs,
                                List<Integer> mergeSongResults,
                                List<String> outputResultPaths) {
        super(strDataSetName);
        List<String> files = new ArrayList<String>();
        for (File f: inputFileList)
            files.add(f.getAbsolutePath());
        inputFileListAttribute = new StringAttribute(strInputFileList, files);
        inputSourceTypeAttribute = new NominalAttribute(strInputSourceType, getAllowedValues(), inputSourceTypeList);
        processedFeatureDescriptionAttribute = new StringAttribute(strProcessedFeatureDescription, processedFeatureDescription);
        trainingAlgorithmIdAttribute = new StringAttribute(strTrainingAlgorithmID, algorithmIDs);
        categoryIdAttribute = NumericAttribute.createFromIntList(strCategoryID, categoryIDs);
        mergeSongResultsAttribute = NumericAttribute.createFromIntList(strMergeSongResults, mergeSongResults);
        outputResultAttribute = new StringAttribute(strOutputResult, outputResultPaths);
        addAttribute(inputFileListAttribute);
        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(trainingAlgorithmIdAttribute);
        addAttribute(categoryIdAttribute);
        addAttribute(mergeSongResultsAttribute);
        addAttribute(outputResultAttribute);
        addAttribute(inputSourceTypeAttribute);
    }

    public ClassifierConfigSet( File inputFile,
				String inputSourceType,
                                String processedFeatureDescription,
                                String algorithmId,
                                int categoryId,
                                int mergeSongResults,
                                String outputResultPath) {
        super(strDataSetName);
        inputFileListAttribute = StringAttribute.createFromString(strInputFileList, inputFile.getAbsolutePath());
        List <String> values = new ArrayList<String>();
        values.add(inputSourceType);
        inputSourceTypeAttribute = new NominalAttribute(strInputSourceType, getAllowedValues(), values);
        processedFeatureDescriptionAttribute = StringAttribute.createFromString(strProcessedFeatureDescription, processedFeatureDescription);
        trainingAlgorithmIdAttribute = StringAttribute.createFromString(strTrainingAlgorithmID, algorithmId);
        categoryIdAttribute = NumericAttribute.createFromDouble(strCategoryID, categoryId);
        mergeSongResultsAttribute = NumericAttribute.createFromDouble(strMergeSongResults, mergeSongResults);
        outputResultAttribute = StringAttribute.createFromString(strOutputResult, outputResultPath);
        addAttribute(inputFileListAttribute);
        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(trainingAlgorithmIdAttribute);
        addAttribute(categoryIdAttribute);
        addAttribute(mergeSongResultsAttribute);
        addAttribute(outputResultAttribute);
        addAttribute(inputSourceTypeAttribute);
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
            int categoryId = categoryIdAttribute.getValueAt(0).intValue();
            String result = outputResultAttribute.getValueAt(0);
            description = "Features: " + processedFeatureStr + " Category: " + categoryId + " Output: " + result;
        }
        return description;
    }

    public StringAttribute getProcessedFeatureDescriptionAttribute() {
        return processedFeatureDescriptionAttribute;
    }

    public StringAttribute getTrainingAlgorithmIdAttribute() {
        return trainingAlgorithmIdAttribute;
    }

    public NumericAttribute getCategoryIdAttribute() {
        return categoryIdAttribute;
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
