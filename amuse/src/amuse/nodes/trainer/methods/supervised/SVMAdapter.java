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
 * Creation date: 16.06.2010
 */
package amuse.nodes.trainer.methods.supervised;

import java.io.File;
import java.util.StringTokenizer;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.trainer.interfaces.TrainerInterface;
import amuse.util.FileOperations;
import amuse.util.LibraryInitializer;

import com.rapidminer.Process;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.io.RepositoryStorer;
import com.rapidminer.operator.learner.functions.kernel.JMySVMLearner;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.repository.Repository;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.local.LocalRepository;
import com.rapidminer.tools.OperatorService;

/**
 * Adapter for JMySVMLearner. For further details of RapidMiner see <a href="http://rapid-i.com/">http://rapid-i.com/</a>
 * 
 * @author Igor Vatolkin
 * @version $Id: SVMAdapter.java 241 2018-07-26 12:35:24Z frederik-h $
 */
public class SVMAdapter extends AmuseTask implements TrainerInterface {

	/** The SVM kernel type */
	private String kernel;
	
	/** The SVM kernel parameter gamma (for radial kernel only) */
	private Double kernelGamma;
	
	/** The SVM kernel parameter degree (for polynomial kernel only) */
	private Double kernelDegree;
	
	/** The SVM kernel parameter a (for neural kernel only) */
	private Double kernelA;
	
	/** The SVM kernel parameter b (for neural kernel only) */
	private Double kernelB;
	
	/** The SVM complexity constant */
	private Double c;
	
	/** Insensitivity constant */
	private Double epsilon;
	
	/**
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) {

		// Default parameters?
		if(parameterString == "" || parameterString == null) {
			kernel = new String("dot");
			kernelGamma = 1.0;
			kernelDegree = 2.0;
			kernelA = 1.0;
			kernelB = 0.0;
			c = 0.0;
			epsilon = 0.0;
		} else {
			StringTokenizer tok = new StringTokenizer(parameterString, "_");
			kernel = tok.nextToken();
			kernelGamma = new Double(tok.nextToken());
			kernelDegree = new Double(tok.nextToken());
			kernelA = new Double(tok.nextToken());
			kernelB = new Double(tok.nextToken());
			c = new Double(tok.nextToken());
			epsilon = new Double(tok.nextToken());
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
			Operator modelLearner = OperatorService.createOperator(JMySVMLearner.class);
			
			// Set the parameters
			modelLearner.setParameter("kernel_type", this.kernel);
			modelLearner.setParameter("kernel_gamma", this.kernelGamma.toString());
			modelLearner.setParameter("kernel_degree", this.kernelDegree.toString());
			modelLearner.setParameter("kernel_a", this.kernelA.toString());
			modelLearner.setParameter("kernel_b", this.kernelB.toString());
			modelLearner.setParameter("C", this.c.toString());
			modelLearner.setParameter("epsilon", this.epsilon.toString());
			process.getRootOperator().getSubprocess(0).addOperator(modelLearner);
			
			// Write the model
			RepositoryStorer modelWriter = OperatorService.createOperator(RepositoryStorer.class);
			modelWriter.setParameter(RepositoryStorer.PARAMETER_REPOSITORY_ENTRY, "//" + LibraryInitializer.RAPIDMINER_REPO_NAME + "/model");
			process.getRootOperator().getSubprocess(0).addOperator(modelWriter);
			
			// Connect the ports
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
	}

}
