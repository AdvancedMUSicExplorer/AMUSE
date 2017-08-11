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
 * Partition size for feature processing is set here. 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class PartitionSize extends IntegerValue {

	/**
	 * Empty constructor, used for createValueFromString method in ESIndividual.initializeFromLog()
	 */
	public PartitionSize() {
	}
	
	/**
	 * Standard constructor
	 * @param esConfiguration Corresponding ES configuration
	 * @param value Partition size
	 */
	public PartitionSize(ESConfiguration esConfiguration, Integer value, Integer max, Integer min) {
		super(esConfiguration, value, max, min);
	}
	
	/**
	 * Constructor which generates random partition size
	 * @param esConfiguration Corresponding ES configuration
	 */
	public PartitionSize(ESConfiguration esConfiguration) {
		super(esConfiguration, generateRandomPartitionSize(esConfiguration),loadMax(esConfiguration),loadMin(esConfiguration));
	}
	
	
	/**
	 * Creates a random partition size within given boundaries
	 * @param esConfiguration Corresponding ES configuration
	 * @return
	 */
	private static int generateRandomPartitionSize(ESConfiguration esConfiguration) {
		Random rand = new Random();
		int maxPartitionSize = loadMax(esConfiguration);
		int minPartitionSize = loadMin(esConfiguration);
		return minPartitionSize + rand.nextInt(maxPartitionSize + 1 - minPartitionSize);
	}
	
	/**
	 * Loads the maximum available partition size
	 * @param esConfiguration Corresponding ES configuration
	 * @return
	 */
	private static int loadMax(ESConfiguration esConfiguration) {
		int maxPartitionSize = -1;
		
		// Load the minimal and maximum bounds for partition size
		Node partitionSizeNode = esConfiguration.getOptimizationParameterByName("Partition size");
		NodeList parameters = partitionSizeNode.getChildNodes();
		for(int i=0;i<parameters.getLength();i++) {
			if(parameters.item(i).getNodeType() == Node.ELEMENT_NODE && parameters.item(i).getNodeName().equals("optimizationParameter")) {
				if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Maximum partition size")) {
					maxPartitionSize = new Integer(parameters.item(i).getAttributes().getNamedItem("intValue").getNodeValue());
					break;
				} 
			}
		}
		
		if(maxPartitionSize == -1) {
			AmuseLogger.write(PartitionSize.class.getName(), Level.WARN, "Maximum partition size parameter was not found" +
				" and will be set to 30000 (default value)");
			maxPartitionSize = 30000;
		}
		return maxPartitionSize;
	}
	
	/**
	 * Loads the minimal available partition size
	 * @param esConfiguration Corresponding ES configuration
	 * @return
	 */
	private static int loadMin(ESConfiguration esConfiguration) {
		int minPartitionSize = -1;
		
		// Load the minimal and maximum bounds for partition size
		Node partitionSizeNode = esConfiguration.getOptimizationParameterByName("Partition size");
		NodeList parameters = partitionSizeNode.getChildNodes();
		for(int i=0;i<parameters.getLength();i++) {
			if(parameters.item(i).getNodeType() == Node.ELEMENT_NODE && parameters.item(i).getNodeName().equals("optimizationParameter")) {
				if(parameters.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Minimal partition size")) {
					minPartitionSize = new Integer(parameters.item(i).getAttributes().getNamedItem("intValue").getNodeValue());
					break;
				} 
			}
		}
		
		if(minPartitionSize == -1) {
			AmuseLogger.write(PartitionSize.class.getName(), Level.WARN, "Minimal partition size parameter was not found" +
				" and will be set to 500 (default value)");
			minPartitionSize = 500;
		}
		return minPartitionSize;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public PartitionSize clone() {
		IntegerValue valueCopy = super.clone();
		return new PartitionSize(esConfiguration,valueCopy.getValue(),valueCopy.getMax(),valueCopy.getMin());
	}
	
}
