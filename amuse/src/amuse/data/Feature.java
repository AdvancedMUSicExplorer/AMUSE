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
 * Creation date: 18.04.2008
 */
package amuse.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;

/**
 * Feature Object that contains information about a single music feature.
 * This class is used for extracted features as well as for feature extraction itself.
 * Processed features are also stored in this object.
 * 
 * @author Clemens Waeltken
 * @version $Id$
 */
public class Feature implements Serializable {

	private static final long serialVersionUID = 9197405714295877274L;

	/** For a raw feature only one id exists; if a processed feature is calculated based on different 
	 * features (e.g. after PCA), all ids of required raw features are saved here. This information can
	 * be used e.g. for calculation of pruning ratio */
	private ArrayList<Integer> ids;
	
	private int extractorId;
	
	/*
	 * Id of feature configuration.
	 * Only set if custom configuration is used.
	 */
	private Integer configurationId;
	
	/*
	 * Custom extraction script for this feature.
	 * Only set if custom configuration is used.
	 */
	private String customScript;
	
	/** Dimension of this feature (e.g. linear prediction coefficients may have a dimension equal to 10 */
	private int dimension;
	
	/** Time window size in samples, from which the feature was extracted, e.g. 512 samples */
	private int sourceFrameSize;
	
	/** Step size in samples between the extraction windows, from which the feature was extracted, e.g. 512 samples */
	private int sourceStepSize;

	/** The number of samples per second */
	private int sampleRate;

	/** Type of feature: WindowedNumeric, WindowedString, Event, SegmentedNumeric, SegmentedString.*/
	public enum FeatureType{WindowedNumeric, WindowedString, Event, SegmentedNumeric, SegmentedString};
	
	private FeatureType featureType;
	
	/** For processed features, here the performed steps are saved.
	 *  First step = original name of the feature */
	private ArrayList<String> history;
	
	private ArrayList<Double[]> values;
	
	private ArrayList<Double> windows;
	
	/** Description of this feature */
	private String description;
	
	/** This value is used to determine if this feature is selected for extraction */
	private boolean isSelectedForExtraction = true;
	
	public Feature(ArrayList<Integer> ids, String description, ArrayList<Double[]> values,ArrayList<Double> windows) {
		this.ids = ids;
		this.values = values;
		this.windows = windows;
		this.dimension = values.get(0).length;
		this.history = new ArrayList<String>();
		this.description = description;
		this.history.add(new String(description));
		this.featureType = FeatureType.WindowedNumeric;
	}
	
	public Feature(ArrayList<Integer> ids, String description, int dimension) {
		this.ids = ids;
		this.description = description;
		this.dimension = dimension;
		this.history = new ArrayList<String>();
		this.history.add(new String(description));
		this.values = new ArrayList<Double[]>();
		this.windows = new ArrayList<Double>();
		this.featureType = FeatureType.WindowedNumeric;
	}
	
	public Feature(int id, String description, int dimension, int extractorID, String featureType) {
		this.ids = new ArrayList<Integer>(1);
		ids.add(id);
		this.description = description;
		this.dimension = dimension;
		this.extractorId = extractorID;
		this.featureType = FeatureType.WindowedNumeric;
		for(FeatureType f: FeatureType.values()){
			if(f.toString().equals(featureType)){
				this.featureType = f;
				break;
			}
		}
	}
	
	public Feature(int id) {
		this.ids = new ArrayList<Integer>(1);
		ids.add(id);
		this.values = new ArrayList<Double[]>();
		this.windows = new ArrayList<Double>();
		this.history = new ArrayList<String>();
		this.featureType = FeatureType.WindowedNumeric;
	}
	
	public String getDescription() {
		return this.description;
	}

	/**
	 * Returns the first feature id (and the single id for raw features)
	 * @return First feature id
	 */
	public int getId() {
		return ids.get(0);
	}

	/**
	 * Returns all feature ids (for processed features: all ids of raw features required for calculation of this feature)
	 * @return All feature ids
	 */
	public ArrayList<Integer> getIds() {
		return ids;
	}
	
	/**
	 * Sets feature ids
	 * @param ids Feature ids
	 */
	public void setIds(ArrayList<Integer> ids) {
		this.ids = ids;
	}
	
	public int getDimension(){
		return this.dimension;
	}
	
	public int getExtractorId() {
		return this.extractorId;
	}
	public ArrayList<Double[]> getValues() {
		return values;
	}
	
	/**
	 * Returns the values of the given time window; returns null if this time window is not found
	 * @param window The given time window
	 */
	public Double[] getValuesFromWindow(double window) {
		for(int i=0;i<windows.size();i++) {
			if(windows.get(i) == window) {
				return values.get(i);
			}
		}
		return null;
	}

	public ArrayList<Double> getWindows() {
		return windows;
	}
	
	public ArrayList<String> getHistory() {
		return history;
	}
	
	/**
	 * @return History of transforms as single string
	 */
	public String getHistoryAsString() {
		StringBuffer historyBuffer = new StringBuffer();
		for(int j=history.size()-1;j>=0;j--) {
			historyBuffer.append(history.get(j));
			if(j>0) historyBuffer.append("(");
		}
		for(int j=history.size()-1;j>0;j--) {
			historyBuffer.append(")");
		}
		return historyBuffer.toString();
	}
	
	public int getSourceFrameSize() {
		return sourceFrameSize;
	}
	
	public void setSourceFrameSize(int sourceFrameSize) {
		this.sourceFrameSize = sourceFrameSize;
	}
	
	public int getSourceStepSize() {
		return sourceStepSize;
	}
	
	public void setSourceStepSize(int sourceStepSize) {
		this.sourceStepSize = sourceStepSize;
	}
	
	public void setHistory(ArrayList<String> history) {
		this.history = new ArrayList<String>();
		for(int i=0;i<history.size();i++) {
			this.history.add(new String(history.get(i)));
		}
	}
	
	public boolean isSelectedForExtraction() {
		return this.isSelectedForExtraction;
	}
	
	public void setSelectedForExtraction(boolean bool) {
		this.isSelectedForExtraction = bool;
	}
	
	@Override
	public String toString()  {
		if(ids.size() == 1) {
			return "ID: " + this.ids.get(0) + " Name: " + this.description + " Dimension: " + this.dimension;
		} else {
			return "Name: " + this.description + " Dimension: " + this.dimension;
		}
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public boolean isSuitableForProcessing() {
		return featureType == FeatureType.WindowedNumeric;
	}
	
	public String getFeatureType(){
		return featureType.toString();
	}
	
	public void setConfigurationId(int configurationId) throws IOException {
		this.configurationId = configurationId;
		// there might be an alternative extractor id specified in the configuration set
		DataSet configurationDataSet = new DataSet(new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "features" + File.separator + this.ids.get(0) + ".arff"));
		for(int i = 0; i < configurationDataSet.getValueCount(); i++) {
			if(configurationDataSet.getAttribute("Id").getValueAt(i).equals(configurationId)) {
				extractorId = ((Double)configurationDataSet.getAttribute("ExtractorId").getValueAt(i)).intValue();
				customScript = configurationDataSet.getAttribute("InputBatch").getValueAt(i).toString();
				break;
			}
		}
	}
	
	public Integer getConfigurationId() {
		return this.configurationId;
	}
	
	public String getCustomScript()	{
		return this.customScript;
	}
	
	public boolean equals(Object o) {
		if(o instanceof Feature) {
			return ((Feature)o).getId() == this.getId() && ((Feature)o).getConfigurationId() == this.getConfigurationId();
		} else {
			return false;
		}
	}
}
