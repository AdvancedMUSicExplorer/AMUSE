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
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.OperatorService;

/**
 * Loads the binary classificatio model created by RapidMiner.
 * For more details about RapidMiner see <a href="http://rapid-i.com/">http://rapid-i.com/</a>
 * 
 * @author Igor Vatolkin
 * @version $Id: RapidMinerModelLoader.java 1108 2010-07-02 09:39:44Z vatolkin $
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
			LibraryInitializer.initializeRapidMiner(properties.getProperty("classifierFolder") + "/operatorsClassification.xml");
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
			process.getLog().setVerbosityLevel(9);
			
			// (1) Create ExampleSet from the ClassificationConfiguration 
			ExampleSet exampleSet = dataSetToClassify.convertToRapidMinerExampleSet();

			// (2) Load the model
			Operator modelLoader = OperatorService.createOperator("ModelLoader"); 
			modelLoader.getLog().setVerbosityLevel(9);
			modelLoader.setParameter("model_file", pathToModelFile); 
			process.getRootOperator().addOperator(modelLoader);
			
			// (3) Apply the model
			Operator modelApp = OperatorService.createOperator("ModelApplier");
			modelApp.getLog().setVerbosityLevel(9);
			process.getRootOperator().addOperator(modelApp);
			
			// (4) Run the process
			process.run(new IOContainer(new IOObject[]{exampleSet}));
			
			// (5) Convert the results to AMUSE EditableDataSet
			exampleSet.getAttributes().getPredictedLabel().setName("PredictedCategory");
			((ClassificationConfiguration)(this.correspondingScheduler.getConfiguration())).setInputToClassify(new DataSetInput(
					new DataSet(exampleSet)));

		} catch(Exception e) {
			throw new NodeException("Error classifying data: " + e.getMessage());
		}
	}

}
