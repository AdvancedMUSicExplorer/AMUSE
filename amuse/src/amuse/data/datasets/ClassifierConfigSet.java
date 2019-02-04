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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import amuse.data.ClassificationType;
import amuse.data.GroundTruthSourceType;
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
    private static final String strProcessedFeatureDescription = "ProcessedFeaturesDescription";
    private static final String strTrainingAlgorithmID = "AlgorithmId";
    private static final String strGroundTruthSource = "GroundTruthSource";
    private static final String strGroundTruthSourceType = "GroundTruthSourceType";
    
    private static final String strCategoriesToClassify = "CategoriesToClassify";
    private static final String strFeaturesToIgnore  = "FeaturesToIgnore";
    private static final String strClassificationType = "ClassificationType";
    private static final String strFuzzy = "Fuzzy";
    private static final String strPathToInputModel = "PathToInputModel";
    
    private static final String strMergeSongResults = "MergeSongResults";
    private static final String strOutputResult = "OutputResult";
    private static final String strDataSetName = "ClassifierConfiguration";
    
    // ARFF attributes
	private final StringAttribute inputFileListAttribute;
    private final NominalAttribute inputSourceTypeAttribute;
    private final StringAttribute processedFeatureDescriptionAttribute;
    private final StringAttribute classificationAlgorithmIdAttribute;
    private final StringAttribute groundTruthSourceAttribute;
    private final NominalAttribute groundTruthSourceTypeAttribute;
    
    private final StringAttribute categoriesToClassifyAttribute;
    private final StringAttribute featuresToIgnoreAttribute;
    private final NominalAttribute classificationTypeAttribute;
    private final NumericAttribute fuzzyAttribute;
    private final StringAttribute pathToInputModelAttribute;
    
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
        
        dataSet.checkStringAttribute(strCategoriesToClassify);
        dataSet.checkStringAttribute(strFeaturesToIgnore);
        dataSet.checkNominalAttribute(strClassificationType);
        dataSet.checkNumericAttribute(strFuzzy);
        dataSet.checkStringAttribute(strPathToInputModel);
        
        NumericAttribute categoryIdAttribute = null;
        try{
        	dataSet.checkStringAttribute(strGroundTruthSource);
        	dataSet.checkNominalAttribute(strGroundTruthSourceType);
        }
        catch(DataSetException e){ // In case of older configurations, there is only a numeric attribute for "CategoryId"
        	try{
        		dataSet.checkNumericAttribute("CategoryId");
        		categoryIdAttribute = (NumericAttribute) dataSet.getAttribute("CategoryId");
        	}
        	catch (DataSetException e1) { // If no CategoryId Attribute is defined, throw the actual exception
				throw e;
			}
        }
        if(categoryIdAttribute == null){
        	groundTruthSourceAttribute = (StringAttribute) dataSet.getAttribute(strGroundTruthSource);
            groundTruthSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(strGroundTruthSourceType);
        }
        else{
        	List<String> categoryIdList = new ArrayList<String>(categoryIdAttribute.getValueCount());
    		for(Double d: categoryIdAttribute.getValues()){
    			categoryIdList.add(d.intValue() + "");
    		}
        	groundTruthSourceAttribute = new StringAttribute(strGroundTruthSource, categoryIdList);
        	groundTruthSourceTypeAttribute = new NominalAttribute(strGroundTruthSourceType, Collections.nCopies(categoryIdAttribute.getValueCount(), GroundTruthSourceType.CATEGORY_ID.toString()));
        }
        
        dataSet.checkNumericAttribute(strMergeSongResults);
        dataSet.checkStringAttribute(strOutputResult);
        inputFileListAttribute = (StringAttribute) dataSet.getAttribute(strInputFileList);
        inputSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(strInputSourceType);
        processedFeatureDescriptionAttribute = (StringAttribute) dataSet.getAttribute(strProcessedFeatureDescription);
        classificationAlgorithmIdAttribute = (StringAttribute) dataSet.getAttribute(strTrainingAlgorithmID);
        
        categoriesToClassifyAttribute = (StringAttribute) dataSet.getAttribute(strCategoriesToClassify);
        featuresToIgnoreAttribute = (StringAttribute) dataSet.getAttribute(strFeaturesToIgnore);
        classificationTypeAttribute = (NominalAttribute) dataSet.getAttribute(strClassificationType);
        fuzzyAttribute = (NumericAttribute) dataSet.getAttribute(strFuzzy);
        pathToInputModelAttribute = (StringAttribute) dataSet.getAttribute(strPathToInputModel);
        
        mergeSongResultsAttribute = (NumericAttribute) dataSet.getAttribute(strMergeSongResults);
        outputResultAttribute = (StringAttribute) dataSet.getAttribute(strOutputResult);
        addAttribute(inputFileListAttribute);
        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(classificationAlgorithmIdAttribute);
        addAttribute(groundTruthSourceAttribute);
        addAttribute(groundTruthSourceTypeAttribute);
        addAttribute(mergeSongResultsAttribute);
        
        addAttribute(categoriesToClassifyAttribute);
        addAttribute(featuresToIgnoreAttribute);
        addAttribute(classificationTypeAttribute);
        addAttribute(fuzzyAttribute);
        
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
        
		NumericAttribute categoryIdAttribute = null;
        try{
        	checkStringAttribute(strGroundTruthSource);
        	checkNominalAttribute(strGroundTruthSourceType);
        }
        catch(DataSetException e){ // In case of older configurations, there is only a numeric attribute for "CategoryId"
        	try{
        		checkNumericAttribute("CategoryId");
        		categoryIdAttribute = (NumericAttribute) getAttribute("CategoryId");
        	}
        	catch (DataSetException e1) { // If no CategoryId Attribute is defined, throw the actual exception
				throw e;
			}
        }
        if(categoryIdAttribute == null){
        	groundTruthSourceAttribute = (StringAttribute) getAttribute(strGroundTruthSource);
            groundTruthSourceTypeAttribute = (NominalAttribute) getAttribute(strGroundTruthSourceType);
        }
        else{
        	List<String> categoryIdList = new ArrayList<String>(categoryIdAttribute.getValueCount());
    		for(Double d: categoryIdAttribute.getValues()){
    			categoryIdList.add(d.intValue() + "");
    		}
        	groundTruthSourceAttribute = new StringAttribute(strGroundTruthSource, categoryIdList);
        	groundTruthSourceTypeAttribute = new NominalAttribute(strGroundTruthSourceType, Collections.nCopies(categoryIdAttribute.getValueCount(), GroundTruthSourceType.CATEGORY_ID.toString()));
        }

        checkStringAttribute(strCategoriesToClassify);
        checkStringAttribute(strFeaturesToIgnore);
        checkNominalAttribute(strClassificationType);
        checkNumericAttribute(strFuzzy);
        checkStringAttribute(strPathToInputModel);
        
        
        checkNumericAttribute(strMergeSongResults);
        checkStringAttribute(strOutputResult);
        inputFileListAttribute = (StringAttribute) getAttribute(strInputFileList);
        inputSourceTypeAttribute = (NominalAttribute) getAttribute(strInputSourceType);
        processedFeatureDescriptionAttribute = (StringAttribute) getAttribute(strProcessedFeatureDescription);
        classificationAlgorithmIdAttribute = (StringAttribute) getAttribute(strTrainingAlgorithmID);
        
        categoriesToClassifyAttribute = (StringAttribute) this.getAttribute(strCategoriesToClassify);
        featuresToIgnoreAttribute = (StringAttribute) this.getAttribute(strFeaturesToIgnore);
        classificationTypeAttribute = (NominalAttribute) this.getAttribute(strClassificationType);
        fuzzyAttribute = (NumericAttribute) this.getAttribute(strFuzzy);
        pathToInputModelAttribute = (StringAttribute) this.getAttribute(strPathToInputModel);
        
        mergeSongResultsAttribute = (NumericAttribute) getAttribute(strMergeSongResults);
        outputResultAttribute = (StringAttribute) getAttribute(strOutputResult);
    }

    public ClassifierConfigSet( List<File> inputFileList,
    							List<String> inputSourceTypeList,
                                List<String> processedFeatureDescription,
                                List<String> algorithmIDs,
                                List<String> groundTruthSources,
                                List<String> groundTruthSourceTypes,
                                List<String> categoriesToClassify,
                                List<String> featuresToIgnore,
                                List<String> classificationTypes,
                                List<Integer> fuzzy,
                                List<Integer> mergeSongResults,
                                List<String> outputResultPaths,
                                List<String> pathToInputModel) {
        super(strDataSetName);
        List<String> files = new ArrayList<String>();
        for (File f: inputFileList)
            files.add(f.getAbsolutePath());
        inputFileListAttribute = new StringAttribute(strInputFileList, files);
        inputSourceTypeAttribute = new NominalAttribute(strInputSourceType, getAllowedValues(), inputSourceTypeList);
        processedFeatureDescriptionAttribute = new StringAttribute(strProcessedFeatureDescription, processedFeatureDescription);
        classificationAlgorithmIdAttribute = new StringAttribute(strTrainingAlgorithmID, algorithmIDs);
        groundTruthSourceAttribute = new StringAttribute(strGroundTruthSource, groundTruthSources);
        groundTruthSourceTypeAttribute = new NominalAttribute(strGroundTruthSourceType, Arrays.asList(GroundTruthSourceType.stringValues()), groundTruthSourceTypes);
        
        categoriesToClassifyAttribute = new StringAttribute(strCategoriesToClassify, categoriesToClassify);
        featuresToIgnoreAttribute = new StringAttribute(strFeaturesToIgnore, featuresToIgnore);
        classificationTypeAttribute = new NominalAttribute(strClassificationType, Arrays.asList(ClassificationType.stringValues()), classificationTypes);
        fuzzyAttribute = NumericAttribute.createFromIntList(strFuzzy, fuzzy);
        pathToInputModelAttribute = new StringAttribute(strPathToInputModel, pathToInputModel);
        
        mergeSongResultsAttribute = NumericAttribute.createFromIntList(strMergeSongResults, mergeSongResults);
        outputResultAttribute = new StringAttribute(strOutputResult, outputResultPaths);
        addAttribute(inputFileListAttribute);
        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(classificationAlgorithmIdAttribute);
        addAttribute(groundTruthSourceAttribute);
        addAttribute(groundTruthSourceTypeAttribute);
        
        addAttribute(categoriesToClassifyAttribute);
        addAttribute(featuresToIgnoreAttribute);
        addAttribute(classificationTypeAttribute);
        addAttribute(fuzzyAttribute);
        addAttribute(pathToInputModelAttribute);
        
        addAttribute(mergeSongResultsAttribute);
        addAttribute(outputResultAttribute);
        addAttribute(inputSourceTypeAttribute);
    }

    public ClassifierConfigSet( File inputFile,
								String inputSourceType,
                                String processedFeatureDescription,
                                String algorithmId,
                                String groundTruthSource,
                                String groundTruthType,
                                String categoriesToClassify,
    							String featuresToIgnore,
    							String classificationType,
    							int fuzzy,
                                int mergeSongResults,
                                String outputResultPath,
                                String pathToInputModel) {
        super(strDataSetName);
        inputFileListAttribute = StringAttribute.createFromString(strInputFileList, inputFile.getAbsolutePath());
        List <String> values = new ArrayList<String>();
        values.add(inputSourceType);
        inputSourceTypeAttribute = new NominalAttribute(strInputSourceType, getAllowedValues(), values);
        processedFeatureDescriptionAttribute = StringAttribute.createFromString(strProcessedFeatureDescription, processedFeatureDescription);
        classificationAlgorithmIdAttribute = StringAttribute.createFromString(strTrainingAlgorithmID, algorithmId);
        groundTruthSourceAttribute = StringAttribute.createFromString(strGroundTruthSource, groundTruthSource);
        List <String> groundTruthSourceTypeValues = new ArrayList<String>();
        groundTruthSourceTypeValues.add(groundTruthType);
        groundTruthSourceTypeAttribute = new NominalAttribute(strGroundTruthSourceType, Arrays.asList(GroundTruthSourceType.stringValues()), groundTruthSourceTypeValues);
        
        //****
        categoriesToClassifyAttribute = StringAttribute.createFromString(strClassificationType, classificationType);
        featuresToIgnoreAttribute = StringAttribute.createFromString(strFeaturesToIgnore, featuresToIgnore);
        List <String> classificationTypeValues = new ArrayList<String>();
        classificationTypeValues.add(classificationType);
        classificationTypeAttribute = new NominalAttribute(strClassificationType, Arrays.asList(ClassificationType.stringValues()), classificationTypeValues);
        fuzzyAttribute = NumericAttribute.createFromDouble(strFuzzy, fuzzy);
        pathToInputModelAttribute = StringAttribute.createFromString(strPathToInputModel, pathToInputModel);
        //****
        
        mergeSongResultsAttribute = NumericAttribute.createFromDouble(strMergeSongResults, mergeSongResults);
        outputResultAttribute = StringAttribute.createFromString(strOutputResult, outputResultPath);
        addAttribute(inputFileListAttribute);
        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(classificationAlgorithmIdAttribute);
        addAttribute(groundTruthSourceAttribute);
        addAttribute(groundTruthSourceTypeAttribute);
        
        addAttribute(categoriesToClassifyAttribute);
        addAttribute(featuresToIgnoreAttribute);
        addAttribute(classificationTypeAttribute);
        addAttribute(fuzzyAttribute);
        addAttribute(pathToInputModelAttribute);
        
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
            String groundTruthSource = groundTruthSourceAttribute.getValueAt(0);
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

    public StringAttribute getGroundTruthSourceAttribute() {
        return groundTruthSourceAttribute;
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
    
    
    public StringAttribute getCategoriesToClassifyAttribute() {
    	return categoriesToClassifyAttribute;
    }
    
    public StringAttribute getFeaturesToIgnoreAttribute() {
    	return featuresToIgnoreAttribute;
    }
    
    public NominalAttribute getClassificationTypeAttribute() {
    	return classificationTypeAttribute;
    }
    
    public NumericAttribute getFuzzyAttribute() {
    	return fuzzyAttribute;
    }
    
    public StringAttribute getPathToInputModelAttribute() {
    	return pathToInputModelAttribute;
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


	public NominalAttribute getGroundTruthSourceTypeAttribute() {
		return groundTruthSourceTypeAttribute;
	}
}
