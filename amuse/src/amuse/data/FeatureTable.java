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
 * Creation date: 22.04.2008
 */
package amuse.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Level;

import amuse.data.datasets.FeatureTableSet;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Describes the list of features for Amuse tasks 
 * 
 * @author Clemens Waeltken
 * @version $Id$
 */
public class FeatureTable implements Serializable {

    private static final long serialVersionUID = 7702565526106025072L;
    
    private final List<Feature> features = new ArrayList<Feature>();
    
    /**
     * Creates FeatureTable from existing featureTable.arff using only those with existing extractor ID.
     * @param featureTableFile - java.io.File object which contains featureTable stored as ".arff".
     */
    public FeatureTable(File featureTableFile) {
        try {
            FeatureTableSet featureTableSet = new FeatureTableSet(featureTableFile);
            StringAttribute id = featureTableSet.getIDAttribute();
            StringAttribute description = featureTableSet.getDescriptionAttribute();
            NumericAttribute extractorID = featureTableSet.getExtractorIDAttribute();
            NumericAttribute dimension = featureTableSet.getDimensionsAttribute();
            NumericAttribute windowSize = featureTableSet.getWindowSizeAttribute();
            NumericAttribute stepSize = featureTableSet.getStepSizeAttribute();
            NominalAttribute featureType = featureTableSet.getFeatureTypeAttribute();
            for (int i = 0; i < featureTableSet.getValueCount(); i++) {
            	if (extractorID.getValueAt(i).isNaN()) {
            		// Skip this
            	} else {
            		String idString = id.getValueAt(i);
            		int idInt;
            		Integer configurationId = null;
            		if(idString.contains("_")) {
            			idInt = new Double(idString.substring(0, idString.indexOf("_"))).intValue();
            			configurationId = new Double(idString.substring(idString.indexOf("_") + 1)).intValue();
            		} else {
            			idInt = new Double(idString).intValue();
            		}
             		Feature f = new Feature(idInt, description.getValueAt(i), dimension.getValueAt(i).intValue(), extractorID.getValueAt(i).intValue(), featureType.getValueAt(i));
            		f.setSourceFrameSize(windowSize.getValueAt(i).intValue());
            		f.setSourceStepSize(stepSize.getValueAt(i).intValue());
            		if(configurationId != null) {
            			f.setConfigurationId(configurationId);
            		}
            		features.add(f);
            	}
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            AmuseLogger.write(this.getClass().getName(), Level.FATAL, "Can't load Feature Table!");
        }
    }
    
    /**
     * Creates FeatureTable from a list of features
     * @param features
     */
    public FeatureTable(List<Feature> features) {
    	for(Feature feature : features) {
    		this.features.add(feature);
    	}
    }
    
    /**
     * Constructor that creates empty FeatureTable
     */
    public FeatureTable() {
		// Do nothing
	}

	/**
     * This methods removes every feature, that has the attribute is not suitable for feature matrix processing.
     */
    public void removeUnsuitableForFeatureMatrixProcessing(){
    	for(int i = 0; i < features.size();){
    		if(!features.get(i).isSuitableForProcessing()){
    			features.remove(i);
    		}
    		else{
    			i++;
    		}
    	}
    }

    /**
     * This method creates and returns a DataSet representation of this FeatureTable, containing all features currently selected for extraction.
     * @return DataSet containign all currently selected features.
     */
    public FeatureTableSet getAccordingDataSet() {
        List<String> featureIDList = new ArrayList<String>();
        List<String> featureDescriptionList = new ArrayList<String>();
        List<Integer> extractorIDList = new ArrayList<Integer>();
        List<Integer> windowSizeList = new ArrayList<Integer>();
        List<Integer> dimensionList = new ArrayList<Integer>();
        List<String> featureTypeList = new ArrayList<String>();
        for (Feature f : features) {
            if (f.isSelectedForExtraction()) {
            	Integer configurationId = f.getConfigurationId();
            	if(configurationId != null) {
            		featureIDList.add(f.getId() + "_" + configurationId);
                } else {
                	featureIDList.add("" + f.getId());
                }
                featureDescriptionList.add(f.getDescription());
                extractorIDList.add(f.getExtractorId());
                windowSizeList.add(f.getSourceFrameSize());
                dimensionList.add(f.getDimension());
                featureTypeList.add(f.getFeatureType());
            }
        }
    	FeatureTableSet featureSet = new FeatureTableSet(featureDescriptionList, featureIDList, extractorIDList, windowSizeList, dimensionList, featureTypeList);
        return featureSet;
    }

    /**
     * @param index - int index of element to return.
     * @return amuse.data.Feature at int index.
     */
    public Feature getFeatureAt(int index) {
        return this.features.get(index);
    }

    public Feature getFeatureByID(int featureID) {
        for (Feature feature : features) {
            if (feature.getId() == featureID) {
                return feature;
            }
        }
        return null;
    }
    
    public List<Feature> getFeatures() {
        return this.features;
    }

    public List<Integer> getSelectedIds() {
        Vector<Integer> indices = new Vector<Integer>();
        for (int i = 0; i < features.size(); i++) {
            if (features.get(i).isSelectedForExtraction()) {
                indices.add(features.get(i).getId());
            }
        }
        return indices;
    }
    
    public List<Feature> getSelectedFeatures() {
    	Vector<Feature> selectedFeatures = new Vector<Feature>();
    	for(int i = 0; i < features.size(); i++) {
    		if(features.get(i).isSelectedForExtraction()) {
    			selectedFeatures.add(features.get(i));
    		}
    	}
    	return selectedFeatures;
    }

    public void printTable() {
        for (int i = 0; i < features.size(); i++) {
            System.out.println(features.get(i).isSelectedForExtraction() + "ID:" + features.get(i).getId() + " " + features.get(i).getDescription() + " " + features.get(i).getDimension() + " " + features.get(i).getExtractorId());
        }
    }

    public int size() {
        return this.features.size();
    }

    public void setAllIds(boolean bool) {
        for (int i = 0; i < features.size(); i++) {
            features.get(i).setSelectedForExtraction(bool);
        }
    }

    /**
     * @return Number of all dimensions from all features
     */
    public int getDimensionsCount() {
    	int dimNumber = 0;
    	for(int i=0;i<this.features.size();i++) {
    		dimNumber += this.features.get(i).getDimension();
    	}
    	return dimNumber;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FeatureTable))
			return false;
		FeatureTable other = (FeatureTable) obj;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		return true;
	}

	public List<Integer> getSelectedConfigurationIds() {
		Vector<Integer> indices = new Vector<Integer>();
        for (int i = 0; i < features.size(); i++) {
            if (features.get(i).isSelectedForExtraction()) {
                indices.add(features.get(i).getConfigurationId());
            }
        }
        return indices;
	}
	
	/*
	 * Adds the corresponding custom features of this
	 */
	public void addCustomFeatures() {
		for(int i = 0; i < features.size(); i++) {
			Feature feature = features.get(i);
			List<Feature> customFeatures = loadCustomFeatures(features.get(i).getId());
			for(Feature customFeature : customFeatures) {
				if(!features.contains(customFeature)) {
					i++;
					features.add(i, customFeature);
				}
			}
		}
	}
	
	/*
	 * Removes all custom features from this feature table
	 */
	public void removeCustomFeatures() {
		for(int i = 0; i < features.size(); i++) {
			if(features.get(i).getConfigurationId() != null) {
				features.remove(i);
				i--;
			}
		}
	}
	
	/*
	 * Loads the corresponding custom features for a feature id
	 */
	private List<Feature> loadCustomFeatures(int id){
		List<Feature> features = new ArrayList<Feature>();
		File featureTableFile = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "features" + File.separator + id + ".arff");
		if(featureTableFile.exists()) {
			try {
	            FeatureTableSet featureTableSet = new FeatureTableSet(featureTableFile);
	            StringAttribute idAttribute = featureTableSet.getIDAttribute();
	            StringAttribute description = featureTableSet.getDescriptionAttribute();
	            NumericAttribute extractorID = featureTableSet.getExtractorIDAttribute();
	            NumericAttribute dimension = featureTableSet.getDimensionsAttribute();
	            NumericAttribute windowSize = featureTableSet.getWindowSizeAttribute();
	            NumericAttribute stepSize = featureTableSet.getStepSizeAttribute();
	            NominalAttribute featureType = featureTableSet.getFeatureTypeAttribute();
	            for (int i = 0; i < featureTableSet.getValueCount(); i++) {
	            	if (extractorID.getValueAt(i).isNaN()) {
	            		// Skip this
	            	} else {
	            		String configIdString = idAttribute.getValueAt(i);
	            		int configIdInt = new Double(configIdString).intValue();
	             		Feature f = new Feature(id, description.getValueAt(i), dimension.getValueAt(i).intValue(), extractorID.getValueAt(i).intValue(), featureType.getValueAt(i));
	            		f.setSourceFrameSize(windowSize.getValueAt(i).intValue());
	            		f.setSourceStepSize(stepSize.getValueAt(i).intValue());
	            		f.setConfigurationId(configIdInt);
	            		features.add(f);
	            	}
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	            AmuseLogger.write(this.getClass().getName(), Level.FATAL, "Can't load Feature Table!");
	        }
		}
		return features;
	}
}
