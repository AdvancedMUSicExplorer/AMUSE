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

import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;
import amuse.data.GroundTruthSourceType;
import amuse.data.InputFeatureType;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.trainer.TrainingConfiguration;

/**
 * This class represents a Training Configuration as used in AMUSE. Serialization to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id: TrainingConfigSet.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class TrainingConfigSet extends AbstractArffExperimentSet {

	// Strings which describe ARFF attributes
	private static final String inputFeaturesStr = "InputFeatures";
	private static final String inputFeatureTypeStr = "InputFeatureType";
	private static final String unitStr = "Unit";
	private static final String classificationWindowSizeStr = "ClassificationWindowSize";
	private static final String classificationWindowStepSizeStr = "ClassificationWindowStepSize";
    private static final String algorithmIdStr = "AlgorithmId";
    private static final String preprocessingAlgorithmIdStr = "PreprocessingAlgorithmId";
    private static final String groundTruthSourceStr = "GroundTruthSource";
    private static final String groundTruthSourceTypeStr = "GroundTruthSourceType";
    private static final String attributesToPredictStr = "AttributesToPredict";
    private static final String attributesToIgnoreStr  = "AttributesToIgnore";
    private static final String relationshipTypeStr = "RelationshipType";
    private static final String labelTypeStr = "LabelType";
    private static final String methodTypeStr = "MethodType";
    private static final String trainingDescriptionStr = "TrainingDescription";
    private static final String pathToOutputModelStr = "PathToOutputModel";
    
    // ARFF attributes
    private final StringAttribute inputFeaturesAttribute;
    private final NominalAttribute inputFeatureTypeAttribute;
    private final NominalAttribute unitAttribute;
    private final NumericAttribute classificationWindowSizeAttribute;
    private final NumericAttribute classificationWindowStepSizeAttribute;
    private final StringAttribute algorithmIdAttribute;
    private final StringAttribute preprocessingAlgorithmIdAttribute;
    private final StringAttribute groundTruthSourceAttribute;
    private final NominalAttribute groundTruthSourceTypeAttribute;
    private final StringAttribute attributesToPredictAttribute;
    private final StringAttribute attributesToIgnoreAttribute;
    private final NominalAttribute relationshipTypeAttribute;
    private final NominalAttribute labelTypeAttribute;
    private final NominalAttribute methodTypeAttribute;
    private final StringAttribute trainingDescriptionAttribute;
    private final StringAttribute pathToOutputModelAttribute;

    private static final String dataSetNameStr = "TrainerConfig";
    private String description = "";

    /**
     * Creates a new TrainingTableSet from a file. Validates if the given file contains a TrainingTableSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid TrainingTableSet.
     */
    public TrainingConfigSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        checkStringAttribute(inputFeaturesStr);
        checkNominalAttribute(inputFeatureTypeStr);
        checkNominalAttribute(unitStr);
        checkNumericAttribute(classificationWindowSizeStr);
        checkNumericAttribute(classificationWindowStepSizeStr);
        checkStringAttribute(algorithmIdStr);
        checkStringAttribute(preprocessingAlgorithmIdStr);
        checkStringAttribute(groundTruthSourceStr);
        checkNominalAttribute(groundTruthSourceTypeStr);
        checkStringAttribute(attributesToPredictStr);
        checkStringAttribute(attributesToIgnoreStr);
        checkNominalAttribute(relationshipTypeStr);
        checkNominalAttribute(labelTypeStr);
        checkNominalAttribute(methodTypeStr);
        checkStringAttribute(trainingDescriptionStr);
        checkStringAttribute(pathToOutputModelStr);
        
        inputFeaturesAttribute = (StringAttribute) this.getAttribute(inputFeaturesStr);
        inputFeatureTypeAttribute = (NominalAttribute) this.getAttribute(inputFeatureTypeStr);
        unitAttribute = (NominalAttribute) this.getAttribute(unitStr);
        classificationWindowSizeAttribute = (NumericAttribute) this.getAttribute(classificationWindowSizeStr);
        classificationWindowStepSizeAttribute = (NumericAttribute) this.getAttribute(classificationWindowStepSizeStr);
        algorithmIdAttribute = (StringAttribute) this.getAttribute(algorithmIdStr);
        preprocessingAlgorithmIdAttribute = (StringAttribute) this.getAttribute(preprocessingAlgorithmIdStr);
        groundTruthSourceAttribute = (StringAttribute) this.getAttribute(groundTruthSourceStr);
        groundTruthSourceTypeAttribute = (NominalAttribute) this.getAttribute(groundTruthSourceTypeStr);
        attributesToPredictAttribute = (StringAttribute) this.getAttribute(attributesToPredictStr);
        attributesToIgnoreAttribute = (StringAttribute) this.getAttribute(attributesToIgnoreStr);
        relationshipTypeAttribute = (NominalAttribute) this.getAttribute(relationshipTypeStr);
        labelTypeAttribute = (NominalAttribute) this.getAttribute(labelTypeStr);
        methodTypeAttribute = (NominalAttribute ) this.getAttribute(methodTypeStr);
        trainingDescriptionAttribute = (StringAttribute) this.getAttribute(trainingDescriptionStr);
        pathToOutputModelAttribute = (StringAttribute) this.getAttribute(pathToOutputModelStr);
    }


    public TrainingConfigSet(String inputFeatures,
    		String inputFeatureType,
    		String unit,
    		int classificationWindowSize,
    		int classificationWindowStepSize,
    		String algorithmId,
    		String preprocessingAlgorithmId,
    		String groundTruthSource,
    		String groundTruthSourceType,
    		String attributesToPredict,
    		String attributesToIgnore,
    		String relationshipType,
    		String labelType,
    		String methodType,
    		String trainingDescription,
    		String pathToOutputModel){
    	super(dataSetNameStr);
    	inputFeaturesAttribute = StringAttribute.createFromString(inputFeaturesStr, inputFeatures);
    	List<String> inputFeatureTypeValues = new ArrayList<String>();
		for(InputFeatureType type : InputFeatureType.values()) {
			inputFeatureTypeValues.add(type.toString());
		}
		List<String> inputFeatureTypes = new ArrayList<String>();
		inputFeatureTypes.add(inputFeatureType);
		inputFeatureTypeAttribute = new NominalAttribute(inputFeatureTypeStr, inputFeatureTypeValues, inputFeatureTypes);
		List<String> unitValues = new ArrayList<String>();
		for(Unit unitValue : Unit.values()) {
			unitValues.add(unitValue.toString());
		}
		List<String> units = new ArrayList<String>();
		units.add(unit);
		unitAttribute = new NominalAttribute(unitStr, unitValues, units);
		classificationWindowSizeAttribute = NumericAttribute.createFromDouble(classificationWindowSizeStr, classificationWindowSize);
		classificationWindowStepSizeAttribute = NumericAttribute.createFromDouble(classificationWindowStepSizeStr, classificationWindowStepSize);
		algorithmIdAttribute = StringAttribute.createFromString(algorithmIdStr, algorithmId);
		preprocessingAlgorithmIdAttribute = StringAttribute.createFromString(preprocessingAlgorithmIdStr, preprocessingAlgorithmId);
		groundTruthSourceAttribute = StringAttribute.createFromString(groundTruthSourceStr, groundTruthSource);
		List<String> groundTruthSourceTypeValues = new ArrayList<String>();
		for(GroundTruthSourceType type : GroundTruthSourceType.values()) {
			groundTruthSourceTypeValues.add(type.toString());
		}
		List<String> groundTruthSourceTypes = new ArrayList<String>();
		groundTruthSourceTypes.add(groundTruthSourceType);
		groundTruthSourceTypeAttribute = new NominalAttribute(groundTruthSourceTypeStr, groundTruthSourceTypeValues, groundTruthSourceTypes);
		attributesToPredictAttribute = StringAttribute.createFromString(attributesToPredictStr, attributesToPredict);
		attributesToIgnoreAttribute = StringAttribute.createFromString(attributesToIgnoreStr, attributesToIgnore);
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
		relationshipTypeAttribute = new NominalAttribute(relationshipTypeStr, relationshipTypeValues, relationshipTypes);
		labelTypeAttribute = new NominalAttribute(labelTypeStr, labelTypeValues, labelTypes);
		methodTypeAttribute = new NominalAttribute(methodTypeStr, methodTypeValues, methodTypes);
		trainingDescriptionAttribute = StringAttribute.createFromString(trainingDescriptionStr, trainingDescription);
		pathToOutputModelAttribute = StringAttribute.createFromString(pathToOutputModelStr, pathToOutputModel);
		
		addAttribute(inputFeaturesAttribute);
		addAttribute(inputFeatureTypeAttribute);
		addAttribute(unitAttribute);
        addAttribute(classificationWindowSizeAttribute);
        addAttribute(classificationWindowStepSizeAttribute);
        addAttribute(algorithmIdAttribute);
        addAttribute(preprocessingAlgorithmIdAttribute);
        addAttribute(groundTruthSourceAttribute);
        addAttribute(groundTruthSourceTypeAttribute);
        addAttribute(attributesToPredictAttribute);
        addAttribute(attributesToIgnoreAttribute);
        addAttribute(relationshipTypeAttribute);
        addAttribute(labelTypeAttribute);
        addAttribute(methodTypeAttribute);
        addAttribute(trainingDescriptionAttribute);
        addAttribute(pathToOutputModelAttribute);
    }

    public TrainingConfigSet(DataSetAbstract dataSet) {
        super(dataSet.getName());
        // Check preconditions:
        dataSet.checkStringAttribute(algorithmIdStr);
        dataSet.checkNominalAttribute(inputFeatureTypeStr);
        dataSet.checkNominalAttribute(unitStr);
        dataSet.checkNumericAttribute(classificationWindowSizeStr);
        dataSet.checkNumericAttribute(classificationWindowStepSizeStr);
        dataSet.checkStringAttribute(inputFeaturesStr);
        dataSet.checkStringAttribute(preprocessingAlgorithmIdStr);
        dataSet.checkStringAttribute(groundTruthSourceStr);
        dataSet.checkNominalAttribute(groundTruthSourceTypeStr);
        dataSet.checkStringAttribute(attributesToPredictStr);
        dataSet.checkStringAttribute(attributesToIgnoreStr);
        dataSet.checkNominalAttribute(relationshipTypeStr);
        dataSet.checkNominalAttribute(labelTypeStr);
        dataSet.checkNominalAttribute(methodTypeStr);
        dataSet.checkStringAttribute(trainingDescriptionStr);
        dataSet.checkStringAttribute(pathToOutputModelStr);
        
        inputFeaturesAttribute =
                (StringAttribute) dataSet.getAttribute(inputFeaturesStr);
        inputFeatureTypeAttribute = (NominalAttribute) dataSet.getAttribute(inputFeatureTypeStr);
        unitAttribute = (NominalAttribute) dataSet.getAttribute(unitStr);
        classificationWindowSizeAttribute = (NumericAttribute) dataSet.getAttribute(classificationWindowSizeStr);
        classificationWindowStepSizeAttribute = (NumericAttribute) dataSet.getAttribute(classificationWindowStepSizeStr);
        algorithmIdAttribute = (StringAttribute) dataSet.getAttribute(algorithmIdStr);
        preprocessingAlgorithmIdAttribute = (StringAttribute) dataSet.getAttribute(preprocessingAlgorithmIdStr);
        groundTruthSourceAttribute = (StringAttribute) dataSet.getAttribute(groundTruthSourceStr);
        groundTruthSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(groundTruthSourceTypeStr);
        attributesToPredictAttribute = (StringAttribute) dataSet.getAttribute(attributesToPredictStr);
        attributesToIgnoreAttribute = (StringAttribute) dataSet.getAttribute(attributesToIgnoreStr);
        relationshipTypeAttribute = (NominalAttribute) dataSet.getAttribute(relationshipTypeStr);
        labelTypeAttribute = (NominalAttribute) dataSet.getAttribute(labelTypeStr);
        methodTypeAttribute = (NominalAttribute) dataSet.getAttribute(methodTypeStr);
        trainingDescriptionAttribute = (StringAttribute) dataSet.getAttribute(trainingDescriptionStr);
        pathToOutputModelAttribute = (StringAttribute) dataSet.getAttribute(pathToOutputModelStr);
        
        addAttribute(inputFeaturesAttribute);
        addAttribute(inputFeatureTypeAttribute);
        addAttribute(unitAttribute);
        addAttribute(classificationWindowSizeAttribute);
        addAttribute(classificationWindowStepSizeAttribute);
        addAttribute(algorithmIdAttribute);
        addAttribute(preprocessingAlgorithmIdAttribute);
        addAttribute(groundTruthSourceAttribute);
        addAttribute(groundTruthSourceTypeAttribute);
        addAttribute(attributesToPredictAttribute);
        addAttribute(attributesToIgnoreAttribute);
        addAttribute(relationshipTypeAttribute);
        addAttribute(labelTypeAttribute);
        addAttribute(methodTypeAttribute);
        addAttribute(trainingDescriptionAttribute);
        addAttribute(pathToOutputModelAttribute);
    }
    
    public StringAttribute getAlgorithmIdAttribute() {
        return algorithmIdAttribute;
    }

    public StringAttribute getGroundTruthSourceAttribute() {
        return groundTruthSourceAttribute;
    }
    
    public StringAttribute getInputFeatureAttribute() {
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

    public StringAttribute getPathToOutputModelAttribute() {
        return pathToOutputModelAttribute;
    }

    public NominalAttribute getGroundTruthSourceTypeAttribute() {
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
    
    public StringAttribute getTrainingDescriptionAttribute() {
    	return trainingDescriptionAttribute;
    }

    public StringAttribute getPreprocessingAlgorithmIdAttribute() {
        return preprocessingAlgorithmIdAttribute;
    }

    public String getType() {
        return "Classification Training";
    }

    public String getDescription() {
        if (description.equals("")) {
            String preprocessingStr = inputFeaturesAttribute.getValueAt(0);
            String groundTruthSource = groundTruthSourceAttribute.getValueAt(0);
            String groundTruthType = groundTruthSourceTypeAttribute.getValueAt(0);
            description = "Training with " + preprocessingStr + " and " + groundTruthType + ":" + groundTruthSource + "";
        }
        return description;
    }

    @Override
    public TaskConfiguration[] getTaskConfiguration() {
		try {
		    return TrainingConfiguration.loadConfigurationsFromDataSet(this);
		} catch (IOException ex) {
		    throw new RuntimeException(ex);
		}
    }
}
