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
package amuse.nodes.classifier.methods.supervised;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.interfaces.ClassifierInterface;
import amuse.util.ExternalToolAdapter;

/**
 * Adapter for Matlab Linear Discriminant Analysis. 
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class LinearDiscriminantAnalysisAdapter extends AmuseTask implements ClassifierInterface {

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
		// Does nothing
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.classifier.interfaces.ClassifierInterface#classify(java.lang.String, java.util.ArrayList, java.lang.String)
	 */
	public void classify(String pathToModelFile) throws NodeException {
		DataSet dataSetToClassify = ((DataSetInput)((ClassificationConfiguration)this.correspondingScheduler.
				getConfiguration()).getInputToClassify()).getDataSet();
		
		// (1) Save the dataSet as ARFF
		try {
			dataSetToClassify.saveToArffFile(new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + "/input.arff"));
		} catch(IOException e) {
			throw new NodeException("Could not save the data: " + e.getMessage());
		}
		
		// (2) Run Matlab LDA
		try {
			ExternalToolAdapter.runBatch(properties.getProperty("classifierFolder") + "/" + properties.getProperty("startScript"), 
				// ARFF input for LDA classification
				this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + "/input.arff" + " " + 
				// Model (here the training set)
				pathToModelFile + " " + 
			    // Temporal output
				this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + "/output.arff");
		} catch (NodeException e) {
			throw new NodeException("Classification with Matlab failed: " + e.getMessage());
		}
		
		// (3) Convert the results to AMUSE format
		try {
			dataSetToClassify.addAttribute(new StringAttribute("PredictedCategory", new ArrayList<String>()));
			FileReader inputReader = new FileReader(new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + 
					this.correspondingScheduler.getTaskId() + "/output.arff"));
			BufferedReader bufferedInputReader = new BufferedReader(inputReader);
			String line =  new String();
			line = bufferedInputReader.readLine();
			while(line != null) {
				dataSetToClassify.getAttribute("PredictedCategory").addValue(line);
				line = bufferedInputReader.readLine();
			}
			inputReader.close();
		} catch (Exception e) {
			throw new NodeException("Could not parse the classification results: " + e.getMessage());
		}
	}

}
