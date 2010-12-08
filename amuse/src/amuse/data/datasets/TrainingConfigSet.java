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

import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.nodes.trainer.TrainingConfiguration;

/**
 * This class represents a Training Configuration as used in AMUSE. Serialization to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id: $
 */
public class TrainingConfigSet extends AbstractArffExperimentSet {

	// Strings which describe ARFF attributes
	private static final String processedFeatureDescriptionStr = "ProcessedFeaturesDescription";
    private static final String algorithmIdStr = "AlgorithmId";
    private static final String preprocessingAlgorithmIdStr = "PreprocessingAlgorithmId";
    private static final String groundTruthSourceStr = "GroundTruthSource";
    private static final String groundTruthSourceTypeStr = "GroundTruthSourceType";
    private static final String pathToOutputModelStr = "PathToOutputModel";
    
    // ARFF attributes
    private final StringAttribute processedFeatureDescriptionAttribute;
    private final StringAttribute algorithmIdAttribute;
    private final StringAttribute preprocessingAlgorithmIdAttribute;
    private final StringAttribute groundTruthSourceAttribute;
    private final NominalAttribute groundTruthSourceTypeAttribute;
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
        checkStringAttribute(processedFeatureDescriptionStr);
        checkStringAttribute(algorithmIdStr);
        checkStringAttribute(preprocessingAlgorithmIdStr);
        checkStringAttribute(groundTruthSourceStr);
        checkNominalAttribute(groundTruthSourceTypeStr);
        checkStringAttribute(pathToOutputModelStr);
        processedFeatureDescriptionAttribute =
                (StringAttribute) this.getAttribute(processedFeatureDescriptionStr);
        algorithmIdAttribute = (StringAttribute) this.getAttribute(algorithmIdStr);
        preprocessingAlgorithmIdAttribute = (StringAttribute) this.getAttribute(preprocessingAlgorithmIdStr);
        groundTruthSourceAttribute = (StringAttribute) this.getAttribute(groundTruthSourceStr);
        groundTruthSourceTypeAttribute = (NominalAttribute) this.getAttribute(groundTruthSourceTypeStr);
        pathToOutputModelAttribute = (StringAttribute) this.getAttribute(pathToOutputModelStr);
    }


    public TrainingConfigSet(TrainingConfiguration trainingConfiguration) {
        super(dataSetNameStr);
        List<String> processingDescriptions = new ArrayList<String>();
        processingDescriptions.add(trainingConfiguration.getProcessedFeaturesModelName());
        List<String> algorithmStr = new ArrayList<String>();
        algorithmStr.add(trainingConfiguration.getAlgorithmDescription());
        List<String> preprocessingStr = new ArrayList<String>();
        preprocessingStr.add(trainingConfiguration.getPreprocessingAlgorithmDescription());
        List<String> groundTruthList = new ArrayList<String>();
        groundTruthList.add(trainingConfiguration.getGroundTruthSource().toString());
        List<String> groundTruthSourceList = new ArrayList<String>();
        groundTruthSourceList.add(trainingConfiguration.getGroundTruthSourceType().toString());
        List<String> pathToOutputModel = new ArrayList<String>();
        pathToOutputModel.add(trainingConfiguration.getPathToOutputModel());

        processedFeatureDescriptionAttribute = new StringAttribute(processedFeatureDescriptionStr, processingDescriptions);
        algorithmIdAttribute = new StringAttribute(algorithmIdStr, algorithmStr);
        preprocessingAlgorithmIdAttribute = new StringAttribute(preprocessingAlgorithmIdStr, preprocessingStr);
        groundTruthSourceAttribute = new StringAttribute(groundTruthSourceStr, groundTruthList);
        List<String> allowedValues = new ArrayList<String>();
        for (TrainingConfiguration.GroundTruthSourceType type : TrainingConfiguration.GroundTruthSourceType.values()) {
            allowedValues.add(type.toString());
        }
        groundTruthSourceTypeAttribute = new NominalAttribute(groundTruthSourceTypeStr,allowedValues, groundTruthSourceList);
        pathToOutputModelAttribute = new StringAttribute(pathToOutputModelStr, pathToOutputModel);
        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(algorithmIdAttribute);
        addAttribute(preprocessingAlgorithmIdAttribute);
        addAttribute(groundTruthSourceAttribute);
        addAttribute(groundTruthSourceTypeAttribute);
        addAttribute(pathToOutputModelAttribute);
    }

    public TrainingConfigSet(DataSetAbstract dataSet) {
        super(dataSet.getName());
        // Check preconditions:
        dataSet.checkStringAttribute(processedFeatureDescriptionStr);
        dataSet.checkStringAttribute(algorithmIdStr);
        dataSet.checkStringAttribute(preprocessingAlgorithmIdStr);
        dataSet.checkStringAttribute(groundTruthSourceStr);
        dataSet.checkNominalAttribute(groundTruthSourceTypeStr);
        dataSet.checkStringAttribute(pathToOutputModelStr);
        processedFeatureDescriptionAttribute =
                (StringAttribute) dataSet.getAttribute(processedFeatureDescriptionStr);
        algorithmIdAttribute = (StringAttribute) dataSet.getAttribute(algorithmIdStr);
        preprocessingAlgorithmIdAttribute = (StringAttribute) dataSet.getAttribute(preprocessingAlgorithmIdStr);
        groundTruthSourceAttribute = (StringAttribute) dataSet.getAttribute(groundTruthSourceStr);
        groundTruthSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(groundTruthSourceTypeStr);
        pathToOutputModelAttribute = (StringAttribute) dataSet.getAttribute(pathToOutputModelStr);

        addAttribute(processedFeatureDescriptionAttribute);
        addAttribute(algorithmIdAttribute);
        addAttribute(preprocessingAlgorithmIdAttribute);
        addAttribute(groundTruthSourceAttribute);
        addAttribute(groundTruthSourceTypeAttribute);
        addAttribute(pathToOutputModelAttribute);
    }
    
    public StringAttribute getAlgorithmIdAttribute() {
        return algorithmIdAttribute;
    }

    public StringAttribute getGroundTruthSourceAttribute() {
        return groundTruthSourceAttribute;
    }
    
    public StringAttribute getProcessedFeatureDescriptionAttribute() {
        return processedFeatureDescriptionAttribute;
    }

    public StringAttribute getPathToOutputModelAttribute() {
        return pathToOutputModelAttribute;
    }

    public NominalAttribute getGroundTruthSourceTypeAttribute() {
        return groundTruthSourceTypeAttribute;
    }

    public StringAttribute getPreprocessingAlgorithmIdAttribute() {
        return preprocessingAlgorithmIdAttribute;
    }

    public String getType() {
        return "Classification Training";
    }

    public String getDescription() {
        if (description.equals("")) {
            String preprocessingStr = processedFeatureDescriptionAttribute.getValueAt(0);
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
