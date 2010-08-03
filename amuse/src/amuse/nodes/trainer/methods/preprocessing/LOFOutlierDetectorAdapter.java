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
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.io.ArffExampleSetWriter;
import com.rapidminer.operator.preprocessing.filter.ExampleFilter;
import com.rapidminer.operator.preprocessing.outlier.LOFOutlierOperator;
import com.rapidminer.tools.OperatorService;

/**
 * Removes outliers with LOF method 
 * 
 * @author Igor Vatolkin
 * @version $Id: $
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
			LibraryInitializer.initializeRapidMiner(properties.getProperty("preprocessorFolder") + "/operatorsClassification.xml");
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
			process.getRootOperator().addOperator(outlierDetector);
			
			// (3) Remove outliers
			Operator outlierFilter = OperatorService.createOperator(ExampleFilter.class);
			outlierFilter.setParameter("condition_class", "attribute_value_filter");
			outlierFilter.setParameter("parameter_string", "Outlier < " + boundary);
			process.getRootOperator().addOperator(outlierFilter);
			
			// (4) Save the ExampleSet and replace the original data file for classification
			Operator exampleWriter = OperatorService.createOperator(ArffExampleSetWriter.class);
			exampleWriter.setParameter("example_set_file", new String(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + "/input.arff"));
			process.getRootOperator().addOperator(exampleWriter);
			
			// (4) Run the process
			process.run(new IOContainer(new IOObject[]{exampleSet}));
			
			// (5) Convert the results to AMUSE EditableDataSet
			((TrainingConfiguration)(this.correspondingScheduler.getConfiguration())).setGroundTruthSource(new DataSetInput(
					new DataSet(exampleSet)));
		} catch(Exception e) {
			throw new NodeException("LOF outlier detection preprocessing failed: " + e.getMessage());
		}
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...LOF outlier detection finished");
	}

}
