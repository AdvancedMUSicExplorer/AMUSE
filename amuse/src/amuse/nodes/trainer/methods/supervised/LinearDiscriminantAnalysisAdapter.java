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
 * Creation date: 14.06.2010
 */
package amuse.nodes.trainer.methods.supervised;

import java.io.File;
import java.io.IOException;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.trainer.interfaces.TrainerInterface;

/**
 * Adapter for Linear Discriminant Analysis. For further details of RapidMiner see <a href="http://rapid-i.com/">http://rapid-i.com/</a>
 * 
 * @author Igor Vatolkin
 * @version $Id: LinearDiscriminantAnalysisAdapter.java 197 2017-08-11 12:15:34Z frederik-h $
 */
public class LinearDiscriminantAnalysisAdapter extends AmuseTask implements TrainerInterface {

	/**
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#setParameters(String)
	 */
	public void setParameters(String parameterString) {
		// Does nothing
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#trainModel(java.lang.String, java.lang.String, long)
	 */
	public void trainModel(String outputModel) throws NodeException {
		DataSet dataSet = ((DataSetInput)((TrainingConfiguration)this.correspondingScheduler.getConfiguration()).getGroundTruthSource()).getDataSet();
		
		// (1) Save the dataSet as ARFF since Matlab LDA can not save models
		try {
			dataSet.saveToArffFile(new File(outputModel));
		} catch(IOException e) {
			throw new NodeException("Could not save the data: " + e.getMessage());
		}
	}

}
