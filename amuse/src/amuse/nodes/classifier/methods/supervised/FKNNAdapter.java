/* This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2019 by code authors
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
 * Creation date: 01.06.2018
 */
package amuse.nodes.classifier.methods.supervised;

import java.io.File;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Level;

import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetException;
import amuse.data.io.DataSetInput;
import amuse.data.io.attributes.NumericAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.ClassifierNodeScheduler;
import amuse.nodes.classifier.interfaces.ClassifierInterface;
import amuse.util.AmuseLogger;

/**
 * classifies data using the FKNN algorithm
 * 
 * @author Philipp Ginsel
 */
public class FKNNAdapter extends AmuseTask implements ClassifierInterface {

	private int neighborNumber;
	private int m;

	public void setParameters(String parameterString) {
		// Default parameters?
		if(parameterString == "" || parameterString == null) {
			neighborNumber = 1;
		} else { 
			neighborNumber = new Integer(parameterString);
		}
		
		//m is always 2. Maybe later it will be possible to change that
		m = 2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		//Does nothing
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.classifier.interfaces.ClassifierInterface#classify(java.lang.String, java.util.ArrayList, java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see amuse.nodes.classifier.interfaces.ClassifierInterface#classify(java.lang.String)
	 */
	public void classify(String pathToModelFile) throws NodeException {
		DataSet dataSetToClassify = ((DataSetInput)((ClassificationConfiguration)this.correspondingScheduler.
				getConfiguration()).getInputToClassify()).getDataSet();
		
		boolean fuzzy = ((ClassificationConfiguration)this.correspondingScheduler.getConfiguration()).getRelationshipType() == RelationshipType.CONTINUOUS;
		boolean multiclass = ((ClassificationConfiguration)this.correspondingScheduler.getConfiguration()).getLabelType() == LabelType.MULTICLASS;
		
		try {
			
			DataSet trainingDataSet = new DataSet(new File(pathToModelFile));
			
			int numberOfCategories = ((Double)trainingDataSet.getAttribute("NumberOfCategories").getValueAt(0)).intValue();
			((ClassifierNodeScheduler)this.correspondingScheduler).setNumberOfCategories(numberOfCategories);
			int positionOfFirstCategory = trainingDataSet.getAttributeCount() - numberOfCategories;
			
			for(int i = positionOfFirstCategory; i < trainingDataSet.getAttributeCount(); i++) {
				dataSetToClassify.addAttribute(new NumericAttribute("Predicted_" + trainingDataSet.getAttribute(i).getName(), new ArrayList<Double>()));
			}
			
			//iterate through every classification window that has to be classified
			for(int classificationWindowToClassify = 0; classificationWindowToClassify < dataSetToClassify.getAttribute(0).getValueCount(); classificationWindowToClassify++) {
				
				//SortedSet of the k nearestNeighbors
				SortedSet<Example> nearestNeighbors = new TreeSet<Example>();
				
				//iterate through all training tracks/clasification windows
				for(int trainingWindow = 0; trainingWindow < trainingDataSet.getValueCount(); trainingWindow++) {
					double distance = 0;
					double classifyValue = 0;
					double trainValue = 0;
					boolean nanClassify = false;
					boolean nanTrain = false;
					//calculate the distance between test window and training window
					for(int n = 0; n < trainingDataSet.getAttributeCount() - numberOfCategories - 2; n++) {
						classifyValue = (Double)dataSetToClassify.getAttribute(n).getValueAt(classificationWindowToClassify);
						trainValue = (Double)trainingDataSet.getAttribute(n).getValueAt(trainingWindow);
						if(Double.isNaN(classifyValue)){
							try {
								AmuseLogger.write(FKNNAdapter.class.getClass().getName(), Level.WARN,"Not a Number in track " + dataSetToClassify.getAttribute("Id").getValueAt(classificationWindowToClassify));
							} catch(DataSetException e) {
								AmuseLogger.write(FKNNAdapter.class.getClass().getName(), Level.WARN,"Not a Number in an input track");
							}
							nanClassify = true;
							break;
						}
						if(Double.isNaN(trainValue)){
							nanTrain = true;
							break;
						}
						
						distance += Math.pow(classifyValue - trainValue, 2);
					}
					if(nanClassify) {//if there is a NaN in the window, we want to classify, we cannot properly classify it
						break;
					}
					if(nanTrain) {//if there is a NaN in the training window, we ignore that window
						continue;
					}
					
					distance = Math.sqrt(distance);
					
					//what category has the training window?
					double[] currentRelationships = new double[numberOfCategories];
					
					for(int category = 0; category < numberOfCategories; category++) {
						currentRelationships[category] = (double)trainingDataSet.getAttribute(positionOfFirstCategory + category).getValueAt(trainingWindow);
					}
					
					Example currentExample = new Example(distance, currentRelationships);
					
					//add the Example to the k nearest neighbors. Remove the Example with the largest distance, if we have to many neighbors.
					nearestNeighbors.add(currentExample);
					if(nearestNeighbors.size() > neighborNumber) {
						nearestNeighbors.remove(nearestNeighbors.last());
					}
				}
				
				if(nearestNeighbors.size() == 0) {//If no neighbors were found (probably because of NaN in the classification window that has to be classified), the window cannot be properly classified
					throw new NodeException("Classification window cannot be classified, because no neighbours were found.");
				}
				
				//make sure that the distances are not 0
				boolean allZero = nearestNeighbors.last().distance == 0;
				
				//if all distances are 0, they are weighed equally
				if(allZero) {
					AmuseLogger.write(FKNNAdapter.class.getClass().getName(), Level.WARN,"Distances are zero!");
					for(Example example : nearestNeighbors) {
						example.distance = 1;
					}
				} else { //otherwise all distances that are 0 are set to a value that is not 0, but is still the smallest distance
					double minimumDistance = 0; //the smallest distance that is not 0
					for(Example example : nearestNeighbors) {
						if(example.distance != 0) {
							minimumDistance = example.distance;
							break;
						}
					}
					for(Example example : nearestNeighbors) {
						if(example.distance == 0) {
							example.distance = minimumDistance/2;
						}
						else {
							break;
						}
					}
				}
				
				double[] relationships = new double[numberOfCategories];
				
				for(int category = 0; category < numberOfCategories; category++) {
				
					double relationship;
					double enumerator = 0;
					double denominator = 0;
					double weight;
				
					for(Example example : nearestNeighbors) {
						weight = 1.0/(Math.pow(example.distance, 2/(m - 1)));
						enumerator += example.relationships[category] * weight;
						denominator += weight;
					}
					relationship = enumerator/denominator;
					
					//make sure that no errors happened with too small distances (or something similar)
					if(Double.isNaN(relationship)) {
						throw new NodeException("Relationship is NaN");
					}
					
					//add the correctValues of Relationship and PredictedCategory to the DataSet
					relationships[category] = relationship;	
				}
				if(multiclass) {
					double maxRelationship = 0.0;
					int positionOfMaxRelationship = 0;
					for(int category = 0; category < numberOfCategories; category++) {
						if(relationships[category] > maxRelationship) {
							maxRelationship = relationships[category];
							positionOfMaxRelationship = category;
						}
					}
					
					for(int category = 0; category < numberOfCategories; category++) {
						dataSetToClassify.getAttribute(dataSetToClassify.getAttributeCount() - numberOfCategories + category).addValue(category == positionOfMaxRelationship ? 1.0 : 0.0);
					}
				} else {
					for(int category = 0; category < numberOfCategories; category++) {
						if(!fuzzy) {
							relationships[category] = relationships[category] >= 0.5 ? 1.0 : 0.0;
						}
						dataSetToClassify.getAttribute(dataSetToClassify.getAttributeCount() - numberOfCategories + category).addValue(relationships[category]);
					}
				}
			}
		} catch(Exception e) {
			throw new NodeException("Error classifying data: " + e.getMessage());
		}
	}
	
	private class Example implements Comparable<Example>{
		
		private double distance;
		private double[] relationships;
		
		private Example(double distance, double[] relationships) {
			this.distance = distance;
			this.relationships = relationships.clone();
		}
		
		public int compareTo(Example e) {
			if(this.distance - e.distance < 0.0) {
				return -1;
			} else if(this.distance - e.distance == 0.0) {
				return 0;
			}
			else {
				return 1;
			}
		}

	}
}