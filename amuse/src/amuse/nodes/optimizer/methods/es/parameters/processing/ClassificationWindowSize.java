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
 * Creation date: 05.01.2010
 */
package amuse.nodes.optimizer.methods.es.parameters.processing;

import java.util.Random;

import org.apache.log4j.Level;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.nodes.optimizer.methods.es.ESConfiguration;
import amuse.nodes.optimizer.methods.es.representation.IntegerValue;
import amuse.util.AmuseLogger;

/**
 * Corresponding AMUSE task: feature processing
 * 
 * Classification window size for feature processing is set here. 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ClassificationWindowSize extends IntegerValue {

	/**
	 * Empty constructor, used for createValueFromString method in ESIndividual.initializeFromLog()
	 */
	public ClassificationWindowSize() {
	}
	
	/**
	 * Standard constructor
	 * @param esConfiguration Corresponding ES configuration
	 * @param value Classification window size
	 */
	public ClassificationWindowSize(ESConfiguration esConfiguration, Integer value, Integer max, Integer min) {
		super(esConfiguration, value, max, min);
	}
	
	/**
	 * Constructor which generates random classification window size
	 * @param esConfiguration Corresponding ES configuration
	 */
	public ClassificationWindowSize(ESConfiguration esConfiguration) {
		super(esConfiguration, generateRandomClassificationWindowSize(esConfiguration),loadMax(esConfiguration),loadMin(esConfiguration));
	}
	
	
	/**
	 * Creates a random classification window size within given boundaries
	 * @param esConfiguration Corresponding ES configuration
	 * @return
	 */
	private static int generateRandomClassificationWindowSize(ESConfiguration esConfiguration) {
		Random rand = new Random();
		int maxClassificationWindowSize = loadMax(esConfiguration);
		int minClassificationWindowSize = loadMin(esConfiguration);
		return minClassificationWindowSize + rand.nextInt(maxClassificationWindowSize + 1 - minClassificationWindowSize);
	}
	
	/**
	 * Loads the maximum available classification window size
	 * @param esConfiguration Corresponding ES configuration
	 * @return
	 */
	private static int loadMax(ESConfiguration esConfiguration) {
		int maxClassificationWindowSize = -1;
		
		// Load the minimal and maximum bounds for classification window size
		Node classificationWindowSizeNode = esConfiguration.getOptimizationParameterByName("Classification window size");
		NodeList parameters = classificationWindowSizeNode.getChildNodes();
		for(int i=0;i<parameters.getLength();i++) {
			if(parameters.item(i).getNodeType() == Node.ELEMENT_NODE && parameters.item(i).getNodeName().equals("optimizationParameter")) {
				if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Maximum classification window size")) {
					maxClassificationWindowSize = new Integer(parameters.item(i).getAttributes().getNamedItem("intValue").getNodeValue());
					break;
				} 
			}
		}
		
		if(maxClassificationWindowSize == -1) {
			AmuseLogger.write(ClassificationWindowSize.class.getName(), Level.WARN, "Maximum classification window size parameter was not found" +
				" and will be set to 30000 (default value)");
			maxClassificationWindowSize = 30000;
		}
		return maxClassificationWindowSize;
	}
	
	/**
	 * Loads the minimal available classification window size
	 * @param esConfiguration Corresponding ES configuration
	 * @return
	 */
	private static int loadMin(ESConfiguration esConfiguration) {
		int minClassificationWindowSize = -1;
		
		// Load the minimal and maximum bounds for classification window size
		Node classificationWindowSizeNode = esConfiguration.getOptimizationParameterByName("Classification window size");
		NodeList parameters = classificationWindowSizeNode.getChildNodes();
		for(int i=0;i<parameters.getLength();i++) {
			if(parameters.item(i).getNodeType() == Node.ELEMENT_NODE && parameters.item(i).getNodeName().equals("optimizationParameter")) {
				if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Minimal classification window size")) {
					minClassificationWindowSize = new Integer(parameters.item(i).getAttributes().getNamedItem("intValue").getNodeValue());
					break;
				} 
			}
		}
		
		if(minClassificationWindowSize == -1) {
			AmuseLogger.write(ClassificationWindowSize.class.getName(), Level.WARN, "Minimal classification window size parameter was not found" +
				" and will be set to 500 (default value)");
			minClassificationWindowSize = 500;
		}
		return minClassificationWindowSize;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public ClassificationWindowSize clone() {
		IntegerValue valueCopy = super.clone();
		return new ClassificationWindowSize(esConfiguration,valueCopy.getValue(),valueCopy.getMax(),valueCopy.getMin());
	}
	
}
