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
 * Creation date: 21.01.2008
 */
package amuse.nodes.classifier.methods.supervised;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.interfaces.ClassifierInterface;
import amuse.util.LibraryInitializer;

import com.rapidminer.Process;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.io.ModelLoader;
import com.rapidminer.operator.io.ModelWriter;
import com.rapidminer.operator.learner.tree.RandomForestLearner;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.OperatorService;

/**
 * Loads the binary classificatio model created by RapidMiner.
 * For more details about RapidMiner see <a href="http://rapid-i.com/">http://rapid-i.com/</a>
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class RapidMinerModelLoader extends AmuseTask implements ClassifierInterface {

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) {
		// Does nothing
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
	 * @see amuse.nodes.classifier.interfaces.ClassifierInterface#classify(java.lang.String, java.util.ArrayList, java.lang.String)
	 */
	public void classify(String pathToModelFile) throws NodeException {
		DataSet dataSetToClassify = ((DataSetInput)((ClassificationConfiguration)this.correspondingScheduler.
				getConfiguration()).getInputToClassify()).getDataSet();
		
		try {
			Process process = new Process();
			
			// (1) Create ExampleSet from the ClassificationConfiguration 
			ExampleSet exampleSet = dataSetToClassify.convertToRapidMinerExampleSet();
			
			// (2) Load the model
			Operator modelLoader = OperatorService.createOperator(ModelLoader.class); 
			modelLoader.setParameter(ModelLoader.PARAMETER_MODEL_FILE, pathToModelFile);
			process.getRootOperator().getSubprocess(0).addOperator(modelLoader);
			
			// (3) Apply the model
			Operator modelApplier = OperatorService.createOperator(ModelApplier.class);
			process.getRootOperator().getSubprocess(0).addOperator(modelApplier);

			// (4) Connect the ports
			InputPort modelApplierModelInputPort = modelApplier.getInputPorts().getPortByName("model");
			InputPort modelApplierUnlabelledDataInputPort = modelApplier.getInputPorts().getPortByName("unlabelled data");
			OutputPort modelLoaderOutputPort = modelLoader.getOutputPorts().getPortByName("output");
			OutputPort processOutputPort = process.getRootOperator().getSubprocess(0).getInnerSources().getPortByIndex(0);

			modelLoaderOutputPort.connectTo(modelApplierModelInputPort);
			processOutputPort.connectTo(modelApplierUnlabelledDataInputPort);
			
			// (5) Run the process
			process.run(new IOContainer(exampleSet));
			
			
			// (6) Convert the results to AMUSE EditableDataSet
			exampleSet.getAttributes().getPredictedLabel().setName("PredictedCategory");
			((ClassificationConfiguration)(this.correspondingScheduler.getConfiguration())).setInputToClassify(new DataSetInput(
					new DataSet(exampleSet)));
			
		} catch(Exception e) {
			throw new NodeException("Error classifying data: " + e.getMessage());
		}
	}

}
