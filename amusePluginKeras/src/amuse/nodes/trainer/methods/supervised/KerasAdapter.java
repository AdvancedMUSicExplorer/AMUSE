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
package amuse.nodes.trainer.methods.supervised;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.trainer.interfaces.TrainerInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.algorithm.Algorithm;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;
import amuse.util.FileOperations;

/**
 * Adapter for training of neural networks with Keras
 * @author Philipp Ginsel
 *
 */
public class KerasAdapter extends AmuseTask implements TrainerInterface {

	private String netPath;
	private int epochs;
	private int batchSize;
	private String optimizer;
	private String loss;

	@Override
	public void setParameters(String parameterString) throws NodeException {
		// default parameters?
		if(parameterString == "" || parameterString == null) {
			netPath = System.getenv("AMUSEHOME") + File.separator + "tools" + File.separator + "Keras" + File.separator + "nets" + File.separator + "nn.py";
			epochs = 100;
			batchSize = 100;
		} else {
			String[] parameters = Algorithm.scanParameters("[" + parameterString + "]");
			netPath = parameters[0];
			epochs = new Integer(parameters[1]);
			batchSize = new Integer(parameters[2]);
			optimizer = parameters[3];
			loss = parameters[4];
		}
	}
	
	@Override
	public void initialize() throws NodeException {
		// TODO Auto-generated method stub
	}

	@Override
	public void trainModel(String outputModel) throws NodeException {
		// set up working directory
		File workingDirectory = new File(System.getenv("AMUSEHOME") + File.separator + "tools" + File.separator + "Keras" + File.separator + "workingDirectory");
		
		// clean working directory
		cleanFolder(workingDirectory);
		
		// save trainer input in working directory
		DataSet trainingDataSet = ((DataSetInput)((TrainingConfiguration)this.correspondingScheduler.getConfiguration()).getGroundTruthSource()).getDataSet();
		String inputPath = workingDirectory + File.separator + "trainerInput.arff";
		String outputPath = workingDirectory + File.separator + "output.mod";
		try {
			trainingDataSet.saveToArffFile(new File(inputPath));
		} catch(IOException e) {
			cleanFolder(workingDirectory);
			throw new NodeException("Saving of trainer input for Keras failed: " + e.getMessage());
		}
		
		// copy the python script that describes the desired neural network
		String workingNetPath = System.getenv("AMUSEHOME") + File.separator + "tools" + File.separator + "Keras" + File.separator + "net.py";
		File workingNetFile = new File(workingNetPath);
		try {
			FileOperations.copy(new File(netPath), workingNetFile);
		} catch (IOException e) {
			throw new NodeException("Could not copy the python script with the net architecutre: " + e.getMessage());
		}
		
		// prepare the trainer script and start it
		try {
			
			List<String> commands = new ArrayList<String>();
			
			// add the commands
			commands.add(AmusePreferences.get(KeysStringValue.PYTHON_PATH));
			commands.add(System.getenv("AMUSEHOME") + File.separator + "tools" + File.separator + "Keras" + File.separator + "keras_train.py");
			commands.add(inputPath);
			commands.add(outputPath);
			commands.add(Integer.toString(epochs));
			commands.add(Integer.toString(batchSize));
			int windowSize = ((TrainingConfiguration)this.correspondingScheduler.getConfiguration()).getNumberOfValuesPerWindow();
			commands.add(Integer.toString(windowSize));
			commands.add(optimizer);
			commands.add(loss);
			
			ExternalProcessBuilder trainer = new ExternalProcessBuilder(commands);
			trainer.setWorkingDirectory(workingDirectory);
			//segmenter.setEnv("LD_LIBRARY_PATH", AmusePreferences.get(KeysStringValue.LD_LIBRARY_PATH));
			trainer.redirectOutputToAMUSE();
			Process trainingProcess = trainer.start();
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "...training process started");
			trainingProcess.waitFor();
			
		} catch(IOException e) {
			cleanFolder(workingDirectory);
			throw new NodeException("Start of Keras training failed: " + e.getMessage());
		} catch(InterruptedException e) {
			cleanFolder(workingDirectory);
			throw new NodeException("Keras training failed: " + e.getMessage());
		}
		// load the output and save it to the model database
		try {
			FileOperations.copy(new File(outputPath), new File(outputModel));
		} catch (IOException e) {
			cleanFolder(workingDirectory);
			throw new NodeException("Reading the results of Keras training failed: " + e.getMessage());
		}
		
		// clean working directory
		cleanFolder(workingDirectory);
		// remove the working net file
		if(!workingNetFile.delete()) {
			AmuseLogger.write(this.getClass().getName(),Level.DEBUG, "Could not remove the file: " + workingNetFile.getAbsolutePath());
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
