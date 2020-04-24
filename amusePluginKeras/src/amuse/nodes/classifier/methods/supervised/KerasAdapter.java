/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2020 by code authors
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
 * Creation date: 03.04.2020
 */
package amuse.nodes.classifier.methods.supervised;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.data.io.attributes.NumericAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.ClassifierNodeScheduler;
import amuse.nodes.classifier.interfaces.ClassifierInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;

/**
 * Adapter for classification with neural networks using Keras
 * @author ginsel
 *
 */
public class KerasAdapter extends AmuseTask implements ClassifierInterface {

	@Override
	public void setParameters(String parameterString) throws NodeException {
		// do nothing
	}

	@Override
	public void initialize() throws NodeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void classify(String pathToModelFile) throws NodeException {
		// set up working directory
		File workingDirectory = new File(System.getenv("AMUSEHOME") + File.separator + "tools" + File.separator + "Keras" + File.separator + "workingDirectory");
		
		// clean working directory
		cleanFolder(workingDirectory);
		
		// save classifier input in working directory
		DataSet dataSetToClassify = ((DataSetInput)((ClassificationConfiguration)this.correspondingScheduler.
				getConfiguration()).getInputToClassify()).getDataSet();
		String inputPath = workingDirectory + File.separator + "classifierInput.arff";
		String outputPath = workingDirectory + File.separator + "classifierOutput.arff";
		try {
			dataSetToClassify.saveToArffFile(new File(inputPath));
		} catch(IOException e) {
			cleanFolder(workingDirectory);
			throw new NodeException("Saving of classifier input for Keras failed: " + e.getMessage());
		}
		
		// prepare the classifier script and start it
		try {
			List<String> commands = new ArrayList<String>();
			commands.add(AmusePreferences.get(KeysStringValue.PYTHON_PATH));
			commands.add(System.getenv("AMUSEHOME") + File.separator + "tools" + File.separator + "Keras" + File.separator + "keras_classify.py");
			commands.add(inputPath);
			commands.add(pathToModelFile);
			commands.add(outputPath);
			int windowSize = ((ClassificationConfiguration)this.correspondingScheduler.getConfiguration()).getNumberOfValuesPerWindow();
			commands.add(Integer.toString(windowSize));
			ExternalProcessBuilder trainer = new ExternalProcessBuilder(commands);
			trainer.setWorkingDirectory(workingDirectory);
			//segmenter.setEnv("LD_LIBRARY_PATH", AmusePreferences.get(KeysStringValue.LD_LIBRARY_PATH));
			trainer.redirectOutputToAMUSE();
			Process classificationProcess = trainer.start();
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "...classification process started");
			classificationProcess.waitFor();
		} catch(IOException e) {
			cleanFolder(workingDirectory);
			throw new NodeException("Start of Keras classification failed: " + e.getMessage());
		} catch(InterruptedException e) {
			cleanFolder(workingDirectory);
			throw new NodeException("Keras classification failed: " + e.getMessage());
		}
		
		// read the results
		DataSet results = null;
		try {
			results = new DataSet(new File(outputPath));
		} catch (IOException e) {
			cleanFolder(workingDirectory);
			throw new NodeException("Reading the results of Keras classification failed: " + e.getMessage());
		}
		// clean the working directory
		cleanFolder(workingDirectory);
		
		// convert the result to Amuse format and save it in dataSetToClassify
		int numberOfAttributes = dataSetToClassify.getAttributeCount();
		int numberOfCategories =  results.getAttributeCount();
		((ClassifierNodeScheduler)this.correspondingScheduler).setNumberOfCategories(numberOfCategories);
		String[] categoryNames = new String[numberOfCategories];
		if(dataSetToClassify.getAttributeNames().contains("NumberOfCategories")) {
			for(int i = 0; i < numberOfCategories; i++) {
				categoryNames[i] = dataSetToClassify.getAttribute(numberOfAttributes - numberOfCategories + i).getName();
			}
		} else {
			for(int i = 0; i < numberOfCategories; i++) {
				categoryNames[i] = "Category_" + i;
			}
		}
		for(String categoryName : categoryNames) {
			dataSetToClassify.addAttribute(new NumericAttribute("Predicted_" + categoryName, new ArrayList<Double>()));
		}
		for(int i = 0; i < results.getAttributeCount(); i++) {
			for(int j = 0; j < results.getValueCount(); j++) {
				dataSetToClassify.getAttribute(numberOfAttributes + i).addValue(results.getAttribute(i).getValueAt(j));
			}
		}
	}

	private void cleanFolder(File folder) {
		File[] filesToDelete = folder.listFiles();
		if(filesToDelete.length != 0) {
			for(int j=0;j<filesToDelete.length;j++) {
				if(!filesToDelete[j].delete()) {
					AmuseLogger.write(this.getClass().getName(),Level.DEBUG, "Could not remove the file: " + filesToDelete[j].getAbsolutePath());
				}
			}
		} 
	}
}
