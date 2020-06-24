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
 * Creation date: 26.04.2009
 */
package amuse.data.datasets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import amuse.data.io.ArffDataSet;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;

/**
 * This class represents a list of features as used in AMUSE. Serialisation to ARFF is supported.
 * @author Clemens Waeltken
 * @version $Id$
 */
public class FeatureTableSet extends ArffDataSet {

    private final StringAttribute idAttribute;
    private final StringAttribute descriptionAttribute;
    private final NumericAttribute extractorIDAttribute;
    private final NumericAttribute windowSizeAttribute;
    private final NumericAttribute stepSizeAttribute;
    private final NumericAttribute dimensionsAttribute;
    private final NominalAttribute featureTypeAttribute;

    private static final String strID = "Id";
    private static final String strDescription = "Description";
    private static final String strExtractiorID = "ExtractorId";
    private static final String strWindowSize = "WindowSize";
    private static final String strStepSize = "StepSize";
    private static final String strDimensions = "Dimensions";
    private static final String strFeatureType = "FeatureType";

    /**
     * Creates a new FeatureTableSet from a file. Validates if the given file contains a FeatureTableSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid FeatureTableSet.
     */
    public FeatureTableSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        if (!this.getAttributeNames().contains(strDescription) || !(this.getAttribute(strDescription) instanceof StringAttribute)) {
            throw new IOException("No " + strDescription +" Attribute!");
        }
        if (!this.getAttributeNames().contains(strID) || (!(this.getAttribute(strID) instanceof NumericAttribute) && !(this.getAttribute(strID) instanceof StringAttribute))) {
            throw new IOException("No " + strID + " Attribute!");
        }
        if (!this.getAttributeNames().contains(strExtractiorID) || !(this.getAttribute(strExtractiorID) instanceof NumericAttribute)) {
            throw new IOException("No " + strExtractiorID + " Attribute!");
        }
        if (!this.getAttributeNames().contains(strWindowSize) || !(this.getAttribute(strWindowSize) instanceof NumericAttribute)) {
            throw new IOException("No " + strWindowSize + " Attribute!");
        }
        if (!this.getAttributeNames().contains(strDimensions) || !(this.getAttribute(strDimensions) instanceof NumericAttribute)) {
            throw new IOException("No " + strDimensions + " Attribute!");
        }
        if (!this.getAttributeNames().contains(strFeatureType) || !(this.getAttribute(strFeatureType) instanceof NominalAttribute)) {
            throw new IOException("No " + strFeatureType + " Attribute!");
        }
        if(this.getAttribute(strID) instanceof NumericAttribute) {
        	NumericAttribute idAttributeNumeric = (NumericAttribute) this.getAttribute(strID);
        	List<String> values = new ArrayList<String>();
        	for(Double value : idAttributeNumeric.getValues()) {
        		values.add(value.toString());
        	}
        	idAttribute = new StringAttribute(strID, values);
        } else {
        	idAttribute = (StringAttribute) this.getAttribute(strID);
        }
        descriptionAttribute = (StringAttribute) this.getAttribute(strDescription);
        extractorIDAttribute = (NumericAttribute) this.getAttribute(strExtractiorID);
        windowSizeAttribute = (NumericAttribute) this.getAttribute(strWindowSize);
        // if there is no step size attribute the step sizes are equal to the window sizes
        if (!this.getAttributeNames().contains(strStepSize) || !(this.getAttribute(strStepSize) instanceof NumericAttribute)) {
            stepSizeAttribute = new NumericAttribute(strStepSize, new ArrayList<Double>());
            for(double value : windowSizeAttribute.getValues()) {
            	stepSizeAttribute.addValue(value);
            }
        } else {
        	stepSizeAttribute = (NumericAttribute) this.getAttribute(strStepSize);
        }
        dimensionsAttribute = (NumericAttribute) this.getAttribute(strDimensions);
        featureTypeAttribute = (NominalAttribute) this.getAttribute(strFeatureType);
    }
    
    public FeatureTableSet(List<String> description, List<String> featureIds, List<Integer> extractorId, List<Integer> windowsize, List<Integer> stepsize, List<Integer> dimensions, List<String> featureTypes) {
    	super("FeatureTable");
    	idAttribute = new StringAttribute(strID, featureIds);
    	descriptionAttribute = new StringAttribute(strDescription, description);
    	extractorIDAttribute = NumericAttribute.createFromIntList(strExtractiorID, extractorId);
    	windowSizeAttribute = NumericAttribute.createFromIntList(strWindowSize, windowsize);
    	stepSizeAttribute = NumericAttribute.createFromIntList(strStepSize, stepsize);
    	dimensionsAttribute = NumericAttribute.createFromIntList(strDimensions, dimensions);
    	featureTypeAttribute = new NominalAttribute(strFeatureType, featureTypes);
    	this.addAttribute(idAttribute);
    	this.addAttribute(descriptionAttribute);
    	this.addAttribute(extractorIDAttribute);
    	this.addAttribute(windowSizeAttribute);
    	this.addAttribute(stepSizeAttribute);
    	this.addAttribute(dimensionsAttribute);
    	this.addAttribute(featureTypeAttribute);
    }
    
    public FeatureTableSet(List<String> description, List<String> featureIds, List<Integer> extractorId, List<Integer> windowsize, List<Integer> dimensions, List<String> featureTypes) {
    	super("FeatureTable");
    	idAttribute = new StringAttribute(strID, featureIds);
    	descriptionAttribute = new StringAttribute(strDescription, description);
    	extractorIDAttribute = NumericAttribute.createFromIntList(strExtractiorID, extractorId);
    	windowSizeAttribute = NumericAttribute.createFromIntList(strWindowSize, windowsize);
    	stepSizeAttribute = NumericAttribute.createFromIntList(strStepSize, windowsize);
    	dimensionsAttribute = NumericAttribute.createFromIntList(strDimensions, dimensions);
    	featureTypeAttribute = new NominalAttribute(strFeatureType, featureTypes);
    	this.addAttribute(idAttribute);
    	this.addAttribute(descriptionAttribute);
    	this.addAttribute(extractorIDAttribute);
    	this.addAttribute(windowSizeAttribute);
    	this.addAttribute(stepSizeAttribute);
    	this.addAttribute(dimensionsAttribute);
    	this.addAttribute(featureTypeAttribute);
    }

	public StringAttribute getIDAttribute() {
        return idAttribute;
    }

    public StringAttribute getDescriptionAttribute() {
        return descriptionAttribute;
    }

    public NumericAttribute getExtractorIDAttribute() {
        return extractorIDAttribute;
    }

    public NumericAttribute getWindowSizeAttribute() {
        return windowSizeAttribute;
    }
    
    public NumericAttribute getStepSizeAttribute() {
    	return stepSizeAttribute;
    }

    public NumericAttribute getDimensionsAttribute() {
        return dimensionsAttribute;
    }
    
    public NominalAttribute getFeatureTypeAttribute() {
        return featureTypeAttribute;
    }
}
