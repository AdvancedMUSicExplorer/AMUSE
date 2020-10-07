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
 * Creation date: 22.02.2010
 */ 
package amuse.nodes.trainer.methods.preprocessing;

import java.io.File;

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
import com.rapidminer.operator.preprocessing.outlier.LOFOutlierOperator;
import com.rapidminer.tools.OperatorService;

/**
 * Removes outliers with LOF method 
 * 
 * @author Igor Vatolkin
 * @version $Id: LOFOutlierDetectorAdapter.java 200 2017-08-11 12:23:59Z frederik-h $
 */
public class LOFOutlierDetectorAdapter extends AmuseTask implements ClassificationPreprocessingInterface {

	/** The boundary for outlier detection */
	private Double boundary;
	
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
		
		// Set the boundary for outlier detection
		this.boundary = new Double(parameterString);
	}


	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.trainer.interfaces.ClassificationPreprocessingInterface#runPreprocessing(java.lang.String)
	 */
	public void runPreprocessing()
			throws NodeException {
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the LOF outlier detection...");
		try {
			
			Process process = new Process();

			// (1) Create ExampleSet from the TrainingConfiguration 
			ExampleSet exampleSet = ((DataSetInput)((TrainingConfiguration)(this.correspondingScheduler.getConfiguration())).
					getGroundTruthSource()).getDataSet().convertToRapidMinerExampleSet();
			
			// (2) Find outliers
			Operator outlierDetector = OperatorService.createOperator(LOFOutlierOperator.class);
			process.getRootOperator().getSubprocess(0).addOperator(outlierDetector);
			
			// (3) Remove outliers
			Operator outlierFilter = OperatorService.createOperator(ExampleFilter.class);
			outlierFilter.setParameter("condition_class", "attribute_value_filter");
			outlierFilter.setParameter("parameter_string", "outlier < " + boundary);
			process.getRootOperator().getSubprocess(0).addOperator(outlierFilter);
			
			// (4) Connect the ports
			InputPort outlierDetectorInputPort = outlierDetector.getInputPorts().getPortByName("example set input");
			OutputPort outlierDetectorOutputPort = outlierDetector.getOutputPorts().getPortByName("example set output");
			
			InputPort outlierFilterInputPort = outlierFilter.getInputPorts().getPortByName("example set input");
			OutputPort outlierFilterOutputPort = outlierFilter.getOutputPorts().getPortByName("example set output");
				
			OutputPort processSourceOutputPort = process.getRootOperator().getSubprocess(0).getInnerSources().getPortByIndex(0);
			InputPort processSinkInputPort = process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0);
			
			processSourceOutputPort.connectTo(outlierDetectorInputPort);
			outlierDetectorOutputPort.connectTo(outlierFilterInputPort);
			outlierFilterOutputPort.connectTo(processSinkInputPort);
			
			
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
			throw new NodeException("LOF outlier detection preprocessing failed: " + e.getMessage());
		}
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...LOF outlier detection finished");
	}

}
