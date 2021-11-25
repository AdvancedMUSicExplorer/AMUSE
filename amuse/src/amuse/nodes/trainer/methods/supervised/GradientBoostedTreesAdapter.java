package amuse.nodes.trainer.methods.supervised;

import java.io.File;
import java.util.Set;

import com.rapidminer.Process;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.io.RepositoryStorer;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.OperatorService;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.trainer.interfaces.TrainerInterface;
import amuse.util.FileOperations;
import amuse.util.LibraryInitializer;

public class GradientBoostedTreesAdapter extends AmuseTask implements TrainerInterface {

	/** The number of decision trees */
	private int treeNumber;
	
	/**
	 * amuse.nodes.trainer.interfaces.TrainerInterface#setParameters(String)
	 */
	@Override
	public void setParameters(String parameterString) throws NodeException {
		// Default parameters?
		if(parameterString == "" || parameterString == null) {
			treeNumber = 20;
		} else {
			treeNumber = new Integer(parameterString);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#trainModel(java.lang.String, java.lang.String, long)
	 */
	@Override
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
	@Override
	public void trainModel(String outputModel) throws NodeException {
		DataSet dataSet = ((DataSetInput)((TrainingConfiguration)this.correspondingScheduler.getConfiguration()).getGroundTruthSource()).getDataSet();
		
		// Train the model and save it
		try {
			
			Process process = new Process();
			
			// Train the model
			Operator modelLearner = OperatorService.createOperator("h2o:gradient_boosted_trees");
			modelLearner.setParameter("number_of_trees", "" + this.treeNumber);
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
			e.printStackTrace();
			throw new NodeException("Classification training failed: " + e.getMessage());
		}
	}

}
