/**
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 *
 * Copyright 2006-2020 by code authors
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
 * Creation date: 21.01.2010
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
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.processor.ProcessingConfiguration.InputSourceType;
import amuse.nodes.processor.ProcessingConfiguration.Unit;

/**
 * This class represents a list of processing tasks as used in AMUSE. Serialization to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id: ProcessorConfigSet.java 197 2017-08-11 12:15:34Z frederik-h $
 */
public class ProcessorConfigSet extends AbstractArffExperimentSet {

	/** Automatically generated serial version ID */
	private static final long serialVersionUID = -2440427291614221309L;
	
	// Strings which describe ARFF attributes
	private static final String strMusicFileList = "FileList";
	private static final String strInputSourceType = "InputSourceType";
	private static final String strInput = "Input";
	private static final String strReductionSteps = "ReductionSteps";
	private static final String strUnit = "Unit";
	private static final String strAggregationWindowSize = "AggregationWindowSize";
	private static final String strAggregationWindowStepSize = "AggregationWindowStepSize";
	private static final String strMatrixToVectorMethod = "MatrixToVectorMethod";
	private static final String strFeatureDescription = "FeatureDescription";

	// ARFF attributes
	private final StringAttribute musicFileListAttribute;
	private final NominalAttribute inputSourceTypeAttribute;
    private final StringAttribute inputAttribute;
    private final StringAttribute reductionStepsAttribute;
    private final NominalAttribute unitAttribute;
    private final NumericAttribute aggregationWindowSizeAttribute;
    private final NumericAttribute aggregationWindowStepSizeAttribute;
    private final StringAttribute matrixToVectorMethodAttribute;
    private final StringAttribute featureDescriptionAttribute;
    private String description = "";

    public ProcessorConfigSet(DataSetAbstract dataSet) throws DataSetException {
        super(dataSet.getName());
        dataSet.checkStringAttribute(strMusicFileList);
        dataSet.checkNominalAttribute(strInputSourceType);
        dataSet.checkStringAttribute(strInput);
        dataSet.checkStringAttribute(strReductionSteps);
        dataSet.checkNominalAttribute(strUnit);
        dataSet.checkNumericAttribute(strAggregationWindowSize);
        dataSet.checkNumericAttribute(strAggregationWindowStepSize);
        dataSet.checkStringAttribute(strMatrixToVectorMethod);
        dataSet.checkStringAttribute(strFeatureDescription);
        musicFileListAttribute = (StringAttribute) dataSet.getAttribute(strMusicFileList);
        inputSourceTypeAttribute = (NominalAttribute) dataSet.getAttribute(strInputSourceType);
        inputAttribute = (StringAttribute) dataSet.getAttribute(strInput);
        reductionStepsAttribute = (StringAttribute) dataSet.getAttribute(strReductionSteps);
        unitAttribute = (NominalAttribute) dataSet.getAttribute(strUnit);
        aggregationWindowSizeAttribute = (NumericAttribute) dataSet.getAttribute(strAggregationWindowSize);
        aggregationWindowStepSizeAttribute = (NumericAttribute) dataSet.getAttribute(strAggregationWindowStepSize);
        matrixToVectorMethodAttribute = (StringAttribute) dataSet.getAttribute(strMatrixToVectorMethod);
        featureDescriptionAttribute = (StringAttribute) dataSet.getAttribute(strFeatureDescription);

    }

    public List<File> getFeatureTables() {
        List<File> featureTables = new ArrayList<File>();
        for (int i = 0; i < inputAttribute.getValueCount(); i++) {
        	if(inputSourceTypeAttribute.getValueAt(i).toString().equals("RAW_FEATURE_LIST")) {        	
        		featureTables.add(new File(inputAttribute.getValueAt(i)));
        	} 
        }
        return featureTables;
    }

    public List<File> getMusicFileLists() {
        List<File> musicFileLists = new ArrayList<File>();
        for (int i = 0; i < musicFileListAttribute.getValueCount(); i++) {
	    musicFileLists.add(new File(musicFileListAttribute.getValueAt(i)));
	}
        return musicFileLists;
    }

    /**
     * Creates a new ProcessorConfigSet from a file. Validates if the given file contains a ProcessorConfigSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid ProcessorConfigSet.
     */
    public ProcessorConfigSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        checkStringAttribute(strMusicFileList);
        checkNominalAttribute(strInputSourceType);
        checkStringAttribute(strInput);
        checkStringAttribute(strReductionSteps);
        checkNominalAttribute(strUnit);
        checkNumericAttribute(strAggregationWindowSize);
        checkNumericAttribute(strAggregationWindowStepSize);
        checkStringAttribute(strMatrixToVectorMethod);
        checkStringAttribute(strFeatureDescription);
        musicFileListAttribute = (StringAttribute) this.getAttribute(strMusicFileList);
        inputSourceTypeAttribute = (NominalAttribute) this.getAttribute(strInputSourceType);
        inputAttribute = (StringAttribute) this.getAttribute(strInput);
        reductionStepsAttribute = (StringAttribute) this.getAttribute(strReductionSteps);
        unitAttribute = (NominalAttribute) this.getAttribute(strUnit);
        aggregationWindowSizeAttribute = (NumericAttribute) this.getAttribute(strAggregationWindowSize);
        aggregationWindowStepSizeAttribute = (NumericAttribute) this.getAttribute(strAggregationWindowStepSize);
        matrixToVectorMethodAttribute = (StringAttribute) this.getAttribute(strMatrixToVectorMethod);
        featureDescriptionAttribute = (StringAttribute) this.getAttribute(strFeatureDescription);
    }

    /**
     * Create a new ProcessorConfigSet from given input.
     * @param fileList The file list of previously extracted music files.
     * @param featureTable The feature table containing previously extracted features.
     * @param reductionSteps The String representing the reduction steps and their configuration.
     * @param unit The unit for classification window size and step size.
     * @param classificatinWindowSize The classification window size.
     * @param classificationWindowStepSize The classification window step size.
     * @param matrixToVectorMethod The matrix to vector method and configuration as String.
     */
    public ProcessorConfigSet(File fileList, String inputSourceType, String input,
            String reductionSteps, String unit,
            int classificationWindowSize, int classificationWindowStepSize,
            String matrixToVectorMethod, String featureDescription) {
        super("ProcessorConfig");
        List<String> fileL = new ArrayList<String>();
        fileL.add(fileList.getAbsolutePath());
        musicFileListAttribute = new StringAttribute(strMusicFileList, fileL);

        List<String> inputSourceTypeL = new ArrayList<String>();
        inputSourceTypeL.add(inputSourceType);
        List<String> inputSourceValues = new ArrayList<String>();
        for(InputSourceType value : InputSourceType.values()) {
        	inputSourceValues.add(value.toString());
        }
        inputSourceTypeAttribute = new NominalAttribute(strInputSourceType, inputSourceValues, inputSourceTypeL);
        
        List<String> inputsL = new ArrayList<String>();
        inputsL.add(input);
        inputAttribute = new StringAttribute(strInput, inputsL);

        List<String> reductionStepList = new ArrayList<String>();
        reductionStepList.add(reductionSteps);
        reductionStepsAttribute = new StringAttribute(strReductionSteps, reductionStepList);

        List<String> unitList = new ArrayList<String>();
        unitList.add(unit);
        List<String> unitValues = new ArrayList<String>();
        for(Unit value : Unit.values()) {
        	unitValues.add(value.toString());
        }
        unitAttribute = new NominalAttribute(strUnit, unitValues, unitList);

        ArrayList<Double> classificationWindowSizes = new ArrayList<Double>();
        classificationWindowSizes.add(((Integer)classificationWindowSize).doubleValue());
        aggregationWindowSizeAttribute = new NumericAttribute(strAggregationWindowSize, new ArrayList<Double>(classificationWindowSizes));

        ArrayList<Double> stepSizeList = new ArrayList<Double>();
        stepSizeList.add(((Integer)classificationWindowStepSize).doubleValue());
        aggregationWindowStepSizeAttribute = new NumericAttribute(strAggregationWindowStepSize, stepSizeList);

        List<String> matrixToVectorMethodsList = new ArrayList<String>();
        matrixToVectorMethodsList.add(matrixToVectorMethod);
        matrixToVectorMethodAttribute = new StringAttribute(strMatrixToVectorMethod, matrixToVectorMethodsList);
        
        List<String> featureDescriptionList = new ArrayList<String>();
        featureDescriptionList.add(featureDescription);
        featureDescriptionAttribute = new StringAttribute(strFeatureDescription, featureDescriptionList);

        this.addAttribute(musicFileListAttribute);
        this.addAttribute(inputSourceTypeAttribute);
        this.addAttribute(inputAttribute);
        this.addAttribute(reductionStepsAttribute);
        this.addAttribute(unitAttribute);
        this.addAttribute(aggregationWindowSizeAttribute);
        this.addAttribute(aggregationWindowStepSizeAttribute);
        this.addAttribute(matrixToVectorMethodAttribute);
        this.addAttribute(featureDescriptionAttribute);
    }

    public String getType() {
        return "Feature Processing";
    }

    public String getDescription() {
        int musicFiles = 0;
        int featureCount = 0;
        if (description.equals("")) {
            try {
                musicFiles = new FileTableSet(new File(musicFileListAttribute.getValueAt(0))).getValueCount();
            } catch (IOException ex) {
                description = "WARNING: Music file table seems to be broken.";
                return description;
            }
            try {
                featureCount = new FeatureTableSet(new File(inputAttribute.getValueAt(0))).getValueCount();
            } catch (IOException ex) {
                description = "WARINING: Feature table seems to be broken.";
                return description;
            }
            description = musicFiles + " file(s) and " + featureCount + " feature(s) seleced, processing with " + reductionStepsAttribute.getValueAt(0);
        }
        return description;
    }

    public NominalAttribute getUnitAttribute() {
        return unitAttribute;
    }

    public NumericAttribute getAggregationWindowSizeAttribute() {
        return aggregationWindowSizeAttribute;
    }

    public NumericAttribute getAggregationWindowStepSizeAttribute() {
        return aggregationWindowStepSizeAttribute;
    }

    public StringAttribute getMatrixToVectorAttribute() {
        return matrixToVectorMethodAttribute;
    }

    public StringAttribute getReductionStepsAttribute() {
        return reductionStepsAttribute;
    }
    
    public StringAttribute getFeatureDescriptionAttribute() {
        return featureDescriptionAttribute;
    }

    @Override
    public TaskConfiguration[] getTaskConfiguration() {
    	try {
    		return ProcessingConfiguration.loadConfigurationsFromDataSet(this);
    	} catch (IOException ex) {
    		throw new RuntimeException(ex);
    	}
    }

	/**
	 * @return the inputSourceTypeAttribute
	 */
	public NominalAttribute getInputSourceTypeAttribute() {
		return inputSourceTypeAttribute;
	}
	
	/**
	 * @return the musicFileListAttribute
	 */
	public StringAttribute getMusicFileListAttribute() {
		return musicFileListAttribute;
	}

	/**
	 * @return the inputAttribute
	 */
	public StringAttribute getInputAttribute() {
		return inputAttribute;
	}

	/**
	 * @return the matrixToVectorMethodAttribute
	 */
	public StringAttribute getMatrixToVectorMethodAttribute() {
		return matrixToVectorMethodAttribute;
	}
   
}

