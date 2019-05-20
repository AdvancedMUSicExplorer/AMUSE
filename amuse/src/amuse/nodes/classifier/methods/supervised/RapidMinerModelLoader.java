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

import amuse.data.ClassificationType;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.data.io.attributes.NumericAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.ClassifierNodeScheduler;
import amuse.nodes.classifier.interfaces.ClassifierInterface;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.util.LibraryInitializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.Process;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.io.ModelLoader;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.OperatorService;

/**
 * Loads the binary classificatio model created by RapidMiner.
 * For more details about RapidMiner see <a href="http://rapid-i.com/">http://rapid-i.com/</a>
 * 
 * @author Igor Vatolkin
 * @version $Id: RapidMinerModelLoader.java 208 2017-09-29 12:21:50Z frederik-h $
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
		//test if the settings are supported
		if(((ClassificationConfiguration)this.correspondingScheduler.getConfiguration()).isFuzzy() || ((ClassificationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationType() == ClassificationType.MULTILABEL || ((TrainingConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationType() == ClassificationType.UNSUPERVISED) {
			throw new NodeException("Only crisp binary or multiclass classification is supported by this method.");
		}
		
		DataSet dataSetToClassify = ((DataSetInput)((ClassificationConfiguration)this.correspondingScheduler.
				getConfiguration()).getInputToClassify()).getDataSet();
		
		
		try {
			
			Process process = new Process();
			
			// (1) Create ExampleSet from the ClassificationConfiguration 
			ExampleSet exampleSet = dataSetToClassify.convertToRapidMinerExampleSet();
			int numberOfAttributes = new DataSet(exampleSet).getAttributeCount();//Number of attributes before classification
			
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
			
			DataSet exampleDataSet = new DataSet(exampleSet);
			DataSet resultDataSet = new DataSet("ClassificationSet");
			
			List<String> labels = new ArrayList<String>();
			for(int i=0;i<exampleDataSet.getAttributeCount();i++) {
				//the original attributes are copied
				if(i < numberOfAttributes) {
					resultDataSet.addAttribute(exampleDataSet.getAttribute(i));
				}
				//the names of the predicted labels are saved in labels
				else if(i < exampleDataSet.getAttributeCount() - 1) {
					String name = exampleDataSet.getAttribute(i).getName();
					labels.add(name.substring(11, name.length() - 1));
				}
				//the last attribute is the predicted category
				else {
					//find out the number of categories and sort the names
					int numberOfCategories = labels.size();
					String[] categoryNames;
					//for binary classification it is differentiated between Category and NOT_Category
					if(((ClassificationConfiguration)this.correspondingScheduler.getConfiguration()).getClassificationType().equals(ClassificationType.BINARY)) {
						numberOfCategories = 1;
						categoryNames = new String[numberOfCategories];
						if(labels.size() != 2 || 
								(!labels.get(0).startsWith("NOT") && !labels.get(1).startsWith("NOT"))) {
							throw new NodeException("The model is not suited for binary classification!");
						}
						categoryNames[0] = labels.get(0);
						if(categoryNames[0].startsWith("NOT")) {
							categoryNames[0] = categoryNames[0].substring(4);
						}
					}
					//for multiclass classification the names are sorted like they were during training
					else {
						categoryNames = new String[numberOfCategories];
						for(String label : labels) {
							int position = 0;
							try {
								position = Integer.parseInt(label.substring(0, label.indexOf("-")));
							} catch(Exception e) {
								throw new NodeException("The model is not suited for multiclass classification!");
							}
							categoryNames[position] = label.substring(label.indexOf("-") + 1);
						}
					}
					
					((ClassifierNodeScheduler)this.correspondingScheduler).setNumberOfCategories(numberOfCategories);
					
					//If only one  category was classified, it is distinguished between Category and NOT_Category
					if(numberOfCategories == 1) {
						resultDataSet.addAttribute(new NumericAttribute("Predicted_" + categoryNames[0], new ArrayList<Double>()));
						for(int j=0;j<exampleDataSet.getValueCount();j++) {
							String predictedLabel = exampleDataSet.getAttribute(i).getValueAt(j).toString();
							resultDataSet.getAttribute(i - labels.size()).addValue(predictedLabel.startsWith("NOT") ? 0.0 : 1.0);
						}
					}
					//If there are more categories, it is distinguished between all the different names
					else {
						//add attributes for all categories
						for(int k = 0; k < numberOfCategories; k++) {
							resultDataSet.addAttribute(new NumericAttribute("Predicted_" + categoryNames[k], new ArrayList<Double>()));
						}
						//add the predicted relationships of the categories to their attributes
						for(int j=0; j<exampleDataSet.getValueCount(); j++) {
							String predictedLabel = exampleDataSet.getAttribute(i).getValueAt(j).toString();
							int position = Integer.parseInt(predictedLabel.substring(0, predictedLabel.indexOf("-")));
							for(int k = 0; k < numberOfCategories; k++) {
								resultDataSet.getAttribute(resultDataSet.getAttributeCount() - numberOfCategories + k).addValue(k == position ? 1.0 : 0.0);
							}
						}
						
					}
				}
			}
			
			((ClassificationConfiguration)(this.correspondingScheduler.getConfiguration())).setInputToClassify(new DataSetInput(
					resultDataSet));
			
		} catch(Exception e) {
			throw new NodeException("Error classifying data: " + e.getMessage());
		}
	}

}
