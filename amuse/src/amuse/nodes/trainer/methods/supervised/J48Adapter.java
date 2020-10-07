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
 * Creation date: 10.10.2007
 */
package amuse.nodes.trainer.methods.supervised;

import amuse.data.io.DataSet;

import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.trainer.interfaces.TrainerInterface;
import amuse.util.FileOperations;
import amuse.util.LibraryInitializer;

import java.io.File;

import com.rapidminer.Process;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.io.RepositoryStorer;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.repository.Repository;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.local.LocalRepository;
import com.rapidminer.tools.OperatorService;


/**
 * Adapter for J48. For further details of Yale see <a href="http://rapid-i.com/">http://rapid-i.com/</a>
 * 
 * @author Igor Vatolkin
 * @version $Id: J48Adapter.java 197 2017-08-11 12:15:34Z frederik-h $
 */
public class J48Adapter extends AmuseTask implements TrainerInterface {

	/** Confidence threshold for pruning */
	private Double c;
	
	/**
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) {
		
		// Default parameters?
		if(parameterString == "" || parameterString == null) {
			c = 0.25;
		} else {
			c = new Double(parameterString);
		}
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
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#trainModel(java.lang.String, java.lang.String, long)
	 */
	public void trainModel(String outputModel) throws NodeException {		
		DataSet dataSet = ((DataSetInput)((TrainingConfiguration)this.correspondingScheduler.getConfiguration()).getGroundTruthSource()).getDataSet();

		// Train the model and save it
		try {
			Process process = new Process();
			
			// Train the model
			Operator modelLearner = OperatorService.createOperator("weka:W-J48");
			process.getRootOperator().getSubprocess(0).addOperator(modelLearner);
			
			// Write the model
			RepositoryStorer modelWriter = OperatorService.createOperator(RepositoryStorer.class);
			modelWriter.setParameter(RepositoryStorer.PARAMETER_REPOSITORY_ENTRY, "//" + LibraryInitializer.RAPIDMINER_REPO_NAME + "/model");
			process.getRootOperator().getSubprocess(0).addOperator(modelWriter);
			
			// Connect the Ports
			InputPort modelLearnerInputPort = modelLearner.getInputPorts().getPortByName("training set");
			OutputPort modelLearnerOutputPort = modelLearner.getOutputPorts().getPortByName("model");
			InputPort modelWriterInputPort = modelWriter.getInputPorts().getPortByName("input");
			OutputPort processOutputPort = process.getRootOperator().getSubprocess(0).getInnerSources().getPortByIndex(0);
			
			modelLearnerOutputPort.connectTo(modelWriterInputPort);
			processOutputPort.connectTo(modelLearnerInputPort);
			
			// Run the process
			process.run(new IOContainer(dataSet.convertToRapidMinerExampleSet()));
			
			// Copy the model into the model database
			FileOperations.copy(new File(LibraryInitializer.REPOSITORY_PATH + File.separator + "model.ioo"), new File(outputModel));
			
		} catch (Exception e) {
			throw new NodeException("Classification training failed: " + e.getMessage());
		}
		
		// TODO v0.2 Later log to Amuse log and not to another file!
		/*File logFile = new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + 
				this.correspondingScheduler.getTaskId() + "/Yale.log");
		try {
			FileOutputStream values_to = new FileOutputStream(logFile);
			DataOutputStream values_writer = new DataOutputStream(values_to);
			LogService.init(values_writer, LogService.IO, false);
		} catch(Exception e) {
			throw new NodeException("Classification training with Yale failed: " + e.getMessage());
		}*/
	}

}
