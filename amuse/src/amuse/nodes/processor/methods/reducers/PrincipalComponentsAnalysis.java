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
 * Creation date: 23.03.2008
 */
package amuse.nodes.processor.methods.reducers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;

import com.rapidminer.Process;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.transformation.PCA;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

import amuse.data.Feature;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.processor.interfaces.DimensionProcessorInterface;
import amuse.util.AmuseLogger;
import amuse.util.LibraryInitializer;

/**
 * Performs principal components analysis of the given features
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class PrincipalComponentsAnalysis extends AmuseTask implements DimensionProcessorInterface {

	/** Percent of principal components to be saved for further processing */
	private double percentOfComponentsToRemain = 0.0d;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		this.percentOfComponentsToRemain = new Double(parameterString);
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		try {
			LibraryInitializer.initializeRapidMiner();
		} catch (Exception e) {
			throw new NodeException("Could not initialize RapidMiner: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.processor.interfaces.DimensionProcessorInterface#runReduction(java.util.ArrayList)
	 */
	public void runDimensionProcessing(ArrayList<Feature> features) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting principal components analysis...");
		int sampleRate = features.get(0).getSampleRate();
		
		// -------------------------------
		// (I) Prepare data for RapidMiner
		// -------------------------------
		
		// Create attribute list
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (int a = 0; a < features.size(); a++) {
			for(int dim=0;dim<features.get(a).getDimension();dim++) {
				
				// If a feature is multidimensional, the attributes must have different names (for RapidMiner)
				if(features.get(a).getDimension() > 1) {
					attributes.add(AttributeFactory.createAttribute(features.get(a).getHistory().toString() + " " + (dim+1),
							Ontology.REAL));
				} else {
					attributes.add(AttributeFactory.createAttribute(features.get(a).getHistory().toString(),
							Ontology.REAL));
				}
			}
		}
		
		// Create table
		MemoryExampleTable table = new MemoryExampleTable(attributes);
		
		// Fill table with all time windows
		for(int d = 0; d < features.get(0).getValues().size(); d++) {
			double[] data = new double[attributes.size()];
			int currentFeature = 0;
			while(currentFeature < attributes.size()-1) {
				for(int a = 0; a < features.size(); a++) {
					for(int dim=0;dim<features.get(a).getDimension();dim++) {
						data[currentFeature] = features.get(a).getValues().get(d)[dim];
						currentFeature++;
					}
				}
			}

			// Add data row
			table.addDataRow(new DoubleArrayDataRow(data));
		}
				
		// Create example set
		ExampleSet exampleSet = table.createExampleSet();
		Integer numberOfComponents = new Integer(new Double(attributes.size()*(percentOfComponentsToRemain/100d)).intValue());
		
		// --------------
		// (II) Apply PCA
		// --------------
		try {
			Process process = new Process();
			
			// Train the model
			Operator pca = OperatorService.createOperator(PCA.class);
			pca.setParameter("number_of_components", numberOfComponents.toString());
			pca.setParameter("dimensionality_reduction", "fixed_number");
			process.getRootOperator().getSubprocess(0).addOperator(pca);
			
			// Connect the Ports
			InputPort pcaInputPort = pca.getInputPorts().getPortByName("example set input");
			OutputPort pcaOutputPort = pca.getOutputPorts().getPortByName("example set output");
			OutputPort processOutputPort = process.getRootOperator().getSubprocess(0).getInnerSources().getPortByIndex(0);
			
			InputPort processInputPort = process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0);

			processOutputPort.connectTo(pcaInputPort);
			pcaOutputPort.connectTo(processInputPort);
			
			// Run the process
			IOContainer result = process.run(new IOContainer(exampleSet));
			
			exampleSet = result.get(ExampleSet.class);
		} catch(OperatorException e) {
			throw new NodeException("Problem occured during PCA: " + e.getMessage());
		} catch (OperatorCreationException e) {
			throw new NodeException("Problem occured during PCA operator creation: " + e.getMessage());
		} 
		
		// --------------------------
		// (III) Get the new features
		// --------------------------
		
		// Raw features required to calculate the new PCA components
		ArrayList<Integer> requiredFeatures = new ArrayList<Integer>(features.size());
		for(int i=0;i<features.size();i++) {
			for(int j=0;j<features.get(i).getIds().size();j++) {
				if(!requiredFeatures.contains(features.get(i).getIds().get(j))) {
					requiredFeatures.add(features.get(i).getIds().get(j));					
				}
			}
		}
		
		// Save the windows information
		ArrayList<Double> windows = features.get(0).getWindows();
		features.clear();
		for(int currentComponent=0;currentComponent<numberOfComponents;currentComponent++) {
			ArrayList<Double[]> values = new ArrayList<Double[]>();
			for(int i=0;i<exampleSet.size();i++) {
				Double[] cVal = {exampleSet.getExample(i).getValue(exampleSet.getAttributes().get("pc_" + (currentComponent+1)))}; 
				values.add(cVal);
			}
			Feature newFeature = new Feature(requiredFeatures, "PCA_component_" + (currentComponent+1) + "_of_" + attributes.size(),values,windows);
			newFeature.setSampleRate(sampleRate);
			features.add(newFeature);
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...reduction succeeded");
	}

}

