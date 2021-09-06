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
import amuse.nodes.optimizer.methods.es.representation.BinaryVector;
import amuse.util.AmuseLogger;

/**
 * Corresponding AMUSE task: feature processing
 * 
 * Features which are selected for classification training are set here. 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class SelectedFeatures extends BinaryVector {

	/**
	 * Empty constructor, used for createValueFromString method in ESIndividual.initializeFromLog()
	 */
	public SelectedFeatures() {
	}
	
	/**
	 * Standard constructor
	 * @param esConfiguration Corresponding ES configuration
	 * @param value Classification window size
	 */
	public SelectedFeatures(ESConfiguration esConfiguration, Boolean[] vector) {
		super(esConfiguration, vector);
	}
	
	/**
	 * Constructor which generates random vector with selected features
	 * @param esConfiguration Corresponding ES configuration
	 */
	public SelectedFeatures(ESConfiguration esConfiguration) {
		super(esConfiguration, generateFeatureVector(esConfiguration));
	}
	
	/**
	 * Creates a random vector with boolean values for feature selection
	 * @param esConfiguration Corresponding ES configuration
	 * @return
	 */
	private static Boolean[] generateFeatureVector(ESConfiguration esConfiguration) {
		Boolean[] vector;
		//Random rand = new Random();
		Long seed = new Long(esConfiguration.getESParameterByName("Random seed").getAttributes().getNamedItem("longValue").getNodeValue());
		Random rand; // TODO seed = -1 means generate each time new!!
		if(seed != -1) {
			rand = new Random(seed);
		} else {
			rand = new Random();
		}
			
		// Load the 
		// (1) feature number
		// (2) initial feature rate
		int featureNumber = 0;
		double initFeatureRate = 0.5;
		Node selectedFeaturesNode = esConfiguration.getOptimizationParameterByName("Selected features");
		NodeList parameters = selectedFeaturesNode.getChildNodes();
		for(int i=0;i<parameters.getLength();i++) {
			if(parameters.item(i).getNodeType() == Node.ELEMENT_NODE && parameters.item(i).getNodeName().equals("optimizationParameter")) {
				if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Feature number")) {
					featureNumber = new Integer(parameters.item(i).getAttributes().getNamedItem("intValue").getNodeValue());
				} else if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Initial rate of selected features")) {
					initFeatureRate = new Double(parameters.item(i).getAttributes().getNamedItem("doubleValue").getNodeValue());
				}
			}
		}
		
		// Set initial feature rate randomly
		if(initFeatureRate == 0) {
			initFeatureRate = rand.nextDouble();
			AmuseLogger.write(SelectedFeatures.class.getName(), Level.DEBUG, "Initial rate of selected features is randomly set to " + initFeatureRate);
		}
			
		// Create an array with appropriate number of features dimensions which will be later switched on/off by ES
		vector = new Boolean[featureNumber];
		int numberOfSelectedFeatures = 0;
		for(int i=0;i<vector.length;i++) {
			// DEBUG Switch all features on!
			//vector[i] = true;
			double toss = rand.nextDouble();
			if(toss < initFeatureRate) {
				vector[i] = true;
				numberOfSelectedFeatures++;
			} else {
				vector[i] = false;
			}
		}
		
		// Check that at least one feature is selected
		if(numberOfSelectedFeatures == 0) {
			vector[rand.nextInt(vector.length)] = true;
		}
		
		return vector;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.representation.BinaryVector#clone()
	 */
	public SelectedFeatures clone() {
		BinaryVector vectorCopy = super.clone();
		return new SelectedFeatures(esConfiguration, vectorCopy.getValue());
	}

}
