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
 * Creation date: 21.10.2010
 */
package amuse.nodes.optimizer.methods.es.operators.mutation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Level;
import org.w3c.dom.NodeList;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.NumericAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.nodes.optimizer.methods.es.EvolutionaryStrategy;
import amuse.nodes.optimizer.methods.es.representation.BinaryVector;
import amuse.nodes.optimizer.methods.es.representation.interfaces.RepresentationInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Asymmetric bit flip mutation which adapts the mutation probability using the correlation of
 * singular features to target (if the feature correlates strongly with the label, it becomes harder
 * to remove it). 
 * 
 * This strategy is refered as ES-GH in the publication:
 * B. Bischl, I. Vatolkin and M. Preuss: Selecting Small Audio Feature Sets in Music Classification 
 * by Means of Asymmetric Mutation, Proc. of the 11th Internat. Conf. on Parallel Problem Solving 
 * From Nature (PPSN), Krakow, 2010
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class AsymmetricBitFlipWithCorrelationToTarget extends AsymmetricBitFlip {

	/** List with correlation coefficients of singular features to the label */
	double[] correlationList;
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#mutate(amuse.nodes.optimizer.methods.es.representation.AbstractRepresentation)
	 */
	public void mutate(RepresentationInterface representation) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Asymmetric bit flip mutation started");
		if(representation instanceof BinaryVector) {
			BinaryVector valueToMutate = (BinaryVector)representation;
			Random rand = new Random();
			selfAdaptation();
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Current value: " + valueToMutate.toString());
			for(int i=0;i<valueToMutate.getValue().length;i++) {
				
				// Perform 0 -> 1 mutation?
				if(!valueToMutate.getValue()[i]) {
					double mutationProbability = (p_01*gamma) / valueToMutate.getValue().length * 
						Math.abs(correlationList[i]);
					if(rand.nextDouble() < mutationProbability) {
						valueToMutate.getValue()[i] = true;
					}
				}
				
				// Perform 1 -> 0 mutation?
				else {
					double mutationProbability = (p_10*gamma) / valueToMutate.getValue().length * 
					(1 - Math.abs(correlationList[i]));
					if(rand.nextDouble() < mutationProbability) {
						valueToMutate.getValue()[i] = false;
					}
				}
			}
			
			// Check if the new individual has at least one feature; otherwise add a random feature!
			boolean added = false;
			for(int i=0;i<valueToMutate.getValue().length;i++) {
				if(valueToMutate.getValue()[i] == true) {
					added = true;
					break;
				}
			}
			if(!added) {
				valueToMutate.getValue()[rand.nextInt(valueToMutate.getValue().length)] = true;
			}
			AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Mutated value: " + valueToMutate.toString());
		} else {
			throw new NodeException("Representation class (" + representation.getClass().toString() + ") must be BinaryVector!");
		}
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Asymmetric bit flip mutation finished");
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.optimizer.methods.es.operators.mutation.MutationInterface#setParameters(org.w3c.dom.NodeList)
	 */
	public void setParameters(NodeList parameters, EvolutionaryStrategy correspondingStrategy) throws NodeException {
		super.setParameters(parameters, correspondingStrategy);
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Calculation of the correlation between features and label started...");
		
		// Create the data set with labeled features for the calculation of the correlation list
		DataSet data = null;
		String categoryForTrainingDescriptionFile = null;
		
		// Input files with processed features
		ArrayList<String> inputProcessedFeatureFiles = new ArrayList<String>();
		ArrayList<Double> ids = new ArrayList<Double>();
		ArrayList<Double> labels = new ArrayList<Double>();
		ArffLoader musicFileLoader = new ArffLoader();
		Instance musicFileInstance;
		
		// Search for music pieces of the training category for the correlation estimation
		String trainingInput = ((OptimizationConfiguration)correspondingStrategy.getCorrespondingScheduler().
				getConfiguration()).getTrainingInput();
		int categoryTrainingId = (trainingInput.contains("[") ? new Double(trainingInput.substring(0,trainingInput.indexOf("["))).intValue() :
			new Double(trainingInput).intValue());
		String categoryForTrainingDescription = new Integer(categoryTrainingId).toString();
		boolean trainingCatFound = false;
		ArffLoader categoryDescriptionLoader = new ArffLoader();
		Instance categoryDescriptionInstance;
		try {	
			categoryDescriptionLoader.setFile(new File(AmusePreferences.getMultipleTracksAnnotationTablePath()));
			categoryDescriptionInstance = categoryDescriptionLoader.getNextInstance(categoryDescriptionLoader.getStructure());
			Attribute idAttribute = categoryDescriptionLoader.getStructure().attribute("Id");
			Attribute fileNameAttribute = categoryDescriptionLoader.getStructure().attribute("Path");
			Attribute categoryNameAttribute = categoryDescriptionLoader.getStructure().attribute("CategoryName");
			while(categoryDescriptionInstance != null) {
				int idOfCurrentCategory = new Double(categoryDescriptionInstance.value(idAttribute)).intValue();
				if(idOfCurrentCategory == categoryTrainingId) {
					categoryForTrainingDescriptionFile = categoryDescriptionInstance.stringValue(fileNameAttribute);
					categoryForTrainingDescription += ("-" + categoryDescriptionInstance.stringValue(categoryNameAttribute));
					trainingCatFound = true;
				}  
				if(trainingCatFound) break;
				categoryDescriptionInstance = categoryDescriptionLoader.getNextInstance(categoryDescriptionLoader.getStructure());
			}
			categoryDescriptionLoader.reset();
		} catch (IOException e) {
			throw new RuntimeException("Could not load the music category information: " + e.getMessage());
		}
		
		// Get the processed model
		String processingDesc = correspondingStrategy.getConfiguration().getConstantParameterByName("Processing description").
			getAttributes().getNamedItem("stringValue").getNodeValue();
		String processedModel = new String(
				correspondingStrategy.getConfiguration().getConstantParameterByName("Processing steps").
					getAttributes().getNamedItem("stringValue").getNodeValue() + "__" + 
				correspondingStrategy.getConfiguration().getConstantParameterByName("Conversion steps").
					getAttributes().getNamedItem("stringValue").getNodeValue() + "__" + 
				correspondingStrategy.getConfiguration().getConstantParameterByName("Classification window size").
					getAttributes().getNamedItem("intValue").getNodeValue() + "ms_" + 
				correspondingStrategy.getConfiguration().getConstantParameterByName("Classification window step size").
					getAttributes().getNamedItem("intValue").getNodeValue() + "ms");
		if(processingDesc != "" && processingDesc != null) {
			processedModel += "_" + processingDesc;
		}
		
		// Load the data
		try {
			data = new DataSet("TrainingSet");
			musicFileLoader.setFile(new File(categoryForTrainingDescriptionFile));
			musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
			Attribute fileNameAttribute = musicFileLoader.getStructure().attribute("Path");
			Attribute idAttribute = musicFileLoader.getStructure().attribute("Id");
			Attribute relationshipAttribute = musicFileLoader.getStructure().attribute("Relationship");
			while(musicFileInstance != null) {
							
				// Current music file
				String currentMusicFile = musicFileInstance.stringValue(fileNameAttribute);
						
				// Calculate the paths
				String relativeName = new String();
				if(currentMusicFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
					relativeName = currentMusicFile.substring(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)).getPath().length());
				} else {
					relativeName = currentMusicFile;
				}
				if(relativeName.charAt(0) == File.separatorChar) {
					relativeName = relativeName.substring(1);
				}
				relativeName = relativeName.substring(0,relativeName.lastIndexOf("."));
				if(relativeName.lastIndexOf(File.separator) != -1) {
					relativeName = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE) + File.separator + relativeName +
						relativeName.substring(relativeName.lastIndexOf(File.separator)) + "_" + processedModel + ".arff";
				} else {
					relativeName = AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE) + File.separator + relativeName +
							File.separator + relativeName + "_" + processedModel + ".arff";
				}
				inputProcessedFeatureFiles.add(relativeName);
				
				// Add id and label of the current file
				ids.add(musicFileInstance.value(idAttribute));
				labels.add(musicFileInstance.value(relationshipAttribute));
				
				// Go to the next music file
				musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
			}
					
			// Load the processed features of the current file
			for(int currFile = 0;currFile<inputProcessedFeatureFiles.size();currFile++) {
				DataSetAbstract featuresOfCurrentFile = new ArffDataSet(new File(inputProcessedFeatureFiles.get(currFile)));
					
				// For the first time, attributes must be created!
				if(data.getAttributeCount() == 0) {
					
					// Omit unit, start and end attributes at the end
					for(int i=0;i<featuresOfCurrentFile.getAttributeCount()-3;i++) {
						data.addAttribute(new NumericAttribute(featuresOfCurrentFile.getAttribute(i).getName(), new ArrayList<Double>()));
					}
					
					// Add id and category attributes
					data.addAttribute(new NumericAttribute("Id", new ArrayList<Double>()));
					data.addAttribute(new NumericAttribute("Category", new ArrayList<Double>()));
				}
					
				// Go through all attributes (omitting unit, start and end)
				for(int i=0;i<featuresOfCurrentFile.getAttributeCount()-3;i++) {
						
					// Go through all values and add them to the current attribute
					for(int j=0;j<featuresOfCurrentFile.getValueCount();j++) {
						data.getAttribute(i).addValue(featuresOfCurrentFile.getAttribute(i).getValueAt(j));
					}
				}
					
				// Set the id and category attributes
				for(int j=0;j<featuresOfCurrentFile.getValueCount();j++) {
					data.getAttribute(data.getAttributeCount()-2).addValue(ids.get(currFile));
					data.getAttribute(data.getAttributeCount()-1).addValue(labels.get(currFile));
				}
				
			}
			
		} catch (IOException e) {
			throw new NodeException("Error during loading of processed features: " + e.getMessage());
		}
		
		// Calculate the correlation coefficients for each features
		correlationList = new double[data.getAttributeCount()-2];
		for(int i=0;i<data.getAttributeCount()-2;i++) {
			correlationList[i] = correlation(data.getAttribute(i),data.getAttribute(data.getAttributeCount()-1));
		}
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...calculation of the correlation is finished!");
	}
	
	/**
	 * Calculates the standard correlation coefficient between the given feature and label
	 * @param feature Feature values for all classification windows
	 * @param label classification window labels
	 * @return Correlation coefficient
	 */
	double correlation(amuse.data.io.attributes.Attribute feature, amuse.data.io.attributes.Attribute label) {
		double meanFeature = 0d;
		double meanLabel = 0d;
		for(int i=0;i<feature.getValueCount();i++) {
			meanFeature += new Double(feature.getValueAt(i).toString());
			meanLabel += new Double(label.getValueAt(i).toString());
		}
		meanFeature /= feature.getValueCount();
		meanLabel /= feature.getValueCount();
		
		double cov = 0d;
		double stddevFeature = 0d;
		double stddevLabel = 0d;
		for(int i=0;i<feature.getValueCount();i++) {
			cov += (new Double(feature.getValueAt(i).toString()) - meanFeature) * 
				   (new Double(label.getValueAt(i).toString()) - meanLabel);
			stddevFeature += (new Double(feature.getValueAt(i).toString()) - meanFeature) * 
				             (new Double(feature.getValueAt(i).toString()) - meanFeature);
			stddevLabel += (new Double(label.getValueAt(i).toString()) - meanLabel) * 
				           (new Double(label.getValueAt(i).toString()) - meanLabel);
		}
		cov /= feature.getValueCount();
		stddevFeature /= feature.getValueCount();
		stddevFeature = Math.sqrt(stddevFeature);
		stddevLabel /= feature.getValueCount();
		stddevLabel = Math.sqrt(stddevLabel);
		
		return (stddevFeature * stddevLabel != 0) ? 
				(cov / (stddevFeature * stddevLabel)) : 0d;
	}

}
