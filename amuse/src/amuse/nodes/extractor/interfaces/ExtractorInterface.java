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
 * Creation date: 11.12.2006
 */
package amuse.nodes.extractor.interfaces;

import java.util.HashMap;

import amuse.data.FeatureTable;
import amuse.interfaces.nodes.NodeException;

/**
 * This interface defines the operations which should be supported by all feature extractors.
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public interface ExtractorInterface {
	
	/**
	 * Sets the file names needed for extraction
	 * @param musicFile Input music file, from which the features should be extracted
	 * @param outputFeatureFile Output file with extracted features
	 * @param currentPart If the input music file was splitted, here is the number of current part
	 * @throws NodeException
	 */
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException;
	
	/**
	 * Converts the base input script (which extracts all feature available from this extractor) to
	 * the input script, which extracts features defined in the feature table 
	 * @param feature2Tool Maps feature IDs to extractor IDs
	 * @param featureTable Complete feature table 
	 */
	public void convertBaseScript(HashMap<Integer,Integer> feature2Tool, FeatureTable featureTable) throws NodeException;
	
	/**
	 * Extracts the features from music file
	 * @throws NodeException
	 */
	public void extractFeatures() throws NodeException;
	
	/**
	 * Converts the extracted features to Amuse ARFF
	 * @throws NodeException
	 */
	public void convertOutput() throws NodeException;

}
