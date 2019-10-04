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
 * Creation date: 25.01.2010
 */ 
package amuse.nodes.trainer.methods.preprocessing;

import org.apache.log4j.Level;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.trainer.interfaces.ClassificationPreprocessingInterface;
import amuse.util.AmuseLogger;
import amuse.util.LibraryInitializer;

import com.rapidminer.Process;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.preprocessing.filter.ExampleFilter;
import com.rapidminer.operator.preprocessing.outlier.DBOutlierOperator;
import com.rapidminer.tools.OperatorService;

/**
 * Removes outliers with density-based method 
 * 
 * @author Igor Vatolkin
 * @version $Id: DensityBasedOutlierDetectorAdapter.java 200 2017-08-11 12:23:59Z frederik-h $
 */
public class DensityBasedOutlierDetectorAdapter extends AmuseTask implements ClassificationPreprocessingInterface {

	/** The distance for objects */
	private Double distance;
	
	/** The proportion of objects related to distance */
	private Double proportion;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.methods.AmuseTaskInterface#initialize()
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
	 * @see amuse.interfaces.nodes.methods.AmuseTaskInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		
		// Set the distance for objects
		this.distance = new Double(parameterString.substring(0,parameterString.indexOf("|")));
		
		// Set the proportion of objects related to distance
		this.proportion = new Double(parameterString.substring(parameterString.indexOf("|")+1));
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.trainer.interfaces.ClassificationPreprocessingInterface#runPreprocessing(java.lang.String)
	 */
	public void runPreprocessing() throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the density based outlier detection...");
		try {
			
			Process process = new Process();

			// (1) Create ExampleSet from the TrainingConfiguration 
			ExampleSet exampleSet = ((DataSetInput)((TrainingConfiguration)(this.correspondingScheduler.getConfiguration())).
					getGroundTruthSource()).getDataSet().convertToRapidMinerExampleSet();
			
			// (2) Find outliers
			Operator outlierDetector = OperatorService.createOperator(DBOutlierOperator.class);
			outlierDetector.setParameter("distance", distance.toString());
			outlierDetector.setParameter("proportion", proportion.toString());
			outlierDetector.setParameter("distance_function", "euclidian distance");
			process.getRootOperator().getSubprocess(0).addOperator(outlierDetector);
			
			// (3) Remove outliers
			Operator exampleFilter = OperatorService.createOperator(ExampleFilter.class);
			exampleFilter.setParameter("condition_class", "attribute_value_filter");
			exampleFilter.setParameter("parameter_string", "outlier=false");
			exampleFilter.setParameter("invert_filter", "false");
			process.getRootOperator().getSubprocess(0).addOperator(exampleFilter);
			
			// (4) Connect the ports
			InputPort outlierDetectorInputPort = outlierDetector.getInputPorts().getPortByName("example set input");
			OutputPort outlierDetectorOutputPort = outlierDetector.getOutputPorts().getPortByName("example set output");
			InputPort exampleFilterInputPort = exampleFilter.getInputPorts().getPortByName("example set input");
			OutputPort exampleFilterOutputPort = exampleFilter.getOutputPorts().getPortByName("example set output");
			OutputPort processSourceOutputPort = process.getRootOperator().getSubprocess(0).getInnerSources().getPortByIndex(0);
			InputPort processSinkInputPort = process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0);
			
			outlierDetectorOutputPort.connectTo(exampleFilterInputPort);
			processSourceOutputPort.connectTo(outlierDetectorInputPort);
			exampleFilterOutputPort.connectTo(processSinkInputPort);
			
			// (5) Run the process and update the example set (removing the outliers)
			int oldSize = exampleSet.size();
			IOContainer container = process.run(new IOContainer(exampleSet));

			exampleSet = container.get(ExampleSet.class);
			
			int newSize = exampleSet.size();
			
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "Outlier detection reduced the size of the dataset from " + oldSize + " to " + newSize + ".");
			
			if(newSize == 0){
				throw new Exception("Every example was marked as outlier.");
			}
			// (6) Convert the results to AMUSE EditableDataSet
			((TrainingConfiguration)(this.correspondingScheduler.getConfiguration())).setGroundTruthSource(new DataSetInput(
					new DataSet(exampleSet)));
			
		} catch(Exception e) {
			throw new NodeException("Density based outlier detection preprocessing failed: " + e.getMessage());
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...density based outlier detection finished");
		
	}

}
