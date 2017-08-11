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
 * Creation date: 28.12.2009
 */
package amuse.nodes.optimizer.methods.es;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.interfaces.nodes.NodeException;

/**
 * All parameters of ES are stored here
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ESConfiguration {

	/** XML document with ES configuration */
	private Document document;
	
	/**
	 * Standard constructor
	 * @param pathToConfiguration Path to XML file with ES configuration
	 */
	public ESConfiguration(String pathToConfiguration) throws NodeException {
		try {
			this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(pathToConfiguration));
		} catch (java.io.IOException e) {
			throw new NodeException("Cannot open configuration XML file: " + e.getMessage());
		} catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: "	+ e.getMessage());
		} catch (org.xml.sax.SAXException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: " + e.getMessage());
		}
	}
	
	/**
	 * @return XML node with all parameters which should be optimized by ES
	 */
	public Node getParametersToOptimize() {
		NodeList children = document.getDocumentElement().getChildNodes();
		Node headNode = null;
		for(int i=0;i<children.getLength();i++) {
			if(children.item(i).getNodeName().equals(new String("problemParametersToOptimize"))) {
				headNode = children.item(i);
				break;
			}
		}
		return headNode;
	}
	
	/**
	 * @return XML node with all constant problem parameters
	 */
	public Node getParametersConstant() {
		NodeList children = document.getDocumentElement().getChildNodes();
		Node headNode = null;
		for(int i=0;i<children.getLength();i++) {
			if(children.item(i).getNodeName().equals(new String("problemParametersConstant"))) {
				headNode = children.item(i);
				break;
			}
		}
		return headNode;
	}
	
	/**
	 * @return XML node with all ES parameters 
	 */
	public Node getESParameters() {
		NodeList children = document.getDocumentElement().getChildNodes();
		Node headNode = null;
		for(int i=0;i<children.getLength();i++) {
			if(children.item(i).getNodeName().equals(new String("esParameters"))) {
				headNode = children.item(i);
				break;
			}
		}
		return headNode;
	}
	
	/**
	 * @return XML node with parameters for logging during optimization process
	 */
	public Node getOutputParameters() {
		NodeList children = document.getDocumentElement().getChildNodes();
		Node headNode = null;
		for(int i=0;i<children.getLength();i++) {
			if(children.item(i).getNodeName().equals(new String("output"))) {
				headNode = children.item(i);
				break;
			}
		}
		return headNode;
	}
	
	/**
	 * Returns the node which describes a constant parameter
	 * @param name The name of a constant parameter
	 * @return The node which describes a constant parameter
	 */
	public Node getConstantParameterByName(String name) {
		return getParameterByName(name, getParametersConstant());
	}
	
	/**
	 * Returns the node which describes an optimization parameter
	 * @param name The name of an optimization parameter
	 * @return The node which describes an optimization parameter
	 */
	public Node getOptimizationParameterByName(String name) {
		return getParameterByName(name, getParametersToOptimize());
	}
	
	/**
	 * Returns the node which describes an ES parameter
	 * @param name The name of an ES parameter
	 * @return The node which describes an ES parameter
	 */
	public Node getESParameterByName(String name) {
		return getParameterByName(name, getESParameters());
	}
	
	/**
	 * Returns the node which describes an output parameter
	 * @param name The name of an output parameter
	 * @return The node which describes an output parameter
	 */
	public Node getOutputParameterByName(String name) {
		return getParameterByName(name, getOutputParameters());
	}
	
	/**
	 * Parses recursively for the first node with given name starting at parametersHeadNode
	 * @param name Node name to search for
	 * @param parametersHeadNode Start node for parsing
	 * @return The first node with given name starting at parametersHeadNode
	 */
	public Node getParameterByName(String name, Node parametersHeadNode) {
		NodeList headChildren = parametersHeadNode.getChildNodes();
		for(int i=0;i<headChildren.getLength();i++) {
			
			// If we have an element node..
			if(headChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
				
				// Check if the "name" attribute contains the searched parameter. If yes, return the node
				if(headChildren.item(i).getAttributes().getNamedItem("name") != null) {
					if(headChildren.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(name)) {
						return headChildren.item(i);
					} else {
						if(headChildren.item(i).hasChildNodes()) {
							if(getParameterByName(name,headChildren.item(i)) != null) {
								return getParameterByName(name,headChildren.item(i));
							}
						}
					}
				} else {
					if(headChildren.item(i).hasChildNodes()) {
						if(getParameterByName(name,headChildren.item(i)) != null) {
							return getParameterByName(name,headChildren.item(i));
						}
					}
				}
			}
		}
		
		// Nothing found
		return null;
	}
	
}
