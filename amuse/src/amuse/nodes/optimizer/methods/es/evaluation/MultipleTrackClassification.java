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
 * Creation date: 28.01.2010
 */
package amuse.nodes.optimizer.methods.es.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;
import amuse.data.FeatureTable;
import amuse.data.FileTable;
import amuse.data.GroundTruthSourceType;
import amuse.data.InputFeatureType;
import amuse.data.MeasureTable;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetInput;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.nodes.optimizer.methods.es.ESIndividual;
import amuse.nodes.optimizer.methods.es.EvolutionaryStrategy;
import amuse.nodes.optimizer.methods.es.evaluation.interfaces.EvaluationInterface;
import amuse.nodes.optimizer.methods.es.parameters.processing.SelectedFeatures;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.trainer.TrainerNodeScheduler;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.nodes.validator.ValidatorNodeScheduler;
import amuse.nodes.validator.interfaces.ValidationMeasure;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.FileOperations;

/**
 * Evaluates the fitness of ES individual (optimization solution) starting required AMUSE tasks
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class MultipleTrackClassification implements EvaluationInterface {

	//  Parameters from ESConfiguration which are used for decision which AMUSE tasks must be started 
	boolean isFeatureExtractionRequired = false;
	boolean isFeatureProcessingRequired = false;
	boolean isFeatureSelectionRequired = false;
	
	DataSet trainingData = null;
	DataSet optimizationData = null;
	DataSet testData = null;
	
	String categoryForLearningDescriptionFile;
	String categoryForOptimizationDescriptionFile;
	String categoryForTestDescriptionFile;
	String categoryForLearningDescription;
	String categoryForOptimizationDescription;
	String categoryForTestDescription;
	
	String processedModel;
	String pathToFeatureDatabase;
	String pathToProcessingDatabase;
	String pathToModelDatabase;
		
	/**
	 * Initializes the Fitness Evaluator with the settings derived from the given individual
	 * @param individual ES individual
	 * @param isEvaluatedOnIndependentTestSet Will the optimization process be evaluated additionally on the independent test set?
	 */
	public void initialize(EvolutionaryStrategy strategy, boolean isEvaluatedOnIndependentTestSet) {
		
		// Nodes: <FE> for feature extraction, <FP> for feature processing, <C> for classification
		NodeList amuseTasksToOptimize = strategy.getConfiguration().getParametersToOptimize().getChildNodes();
		
		// Go through all nodes which belong to <problemParametersToOptimize>
		for(int i=0;i<amuseTasksToOptimize.getLength();i++) {
			if(amuseTasksToOptimize.item(i).getNodeType() == Node.ELEMENT_NODE){
				if(amuseTasksToOptimize.item(i).getNodeName().equals("FE")) {
					isFeatureExtractionRequired = true;
				} else if (amuseTasksToOptimize.item(i).getNodeName().equals("FP")) {
					isFeatureProcessingRequired = true;
				} else if (amuseTasksToOptimize.item(i).getNodeName().equals("FS")) {
					isFeatureSelectionRequired = true;
				}
				
			}
		}
		
		// Load the information about used music categories (which correspond to training, optimization and test sets)
		String data = ((OptimizationConfiguration)strategy.getCorrespondingScheduler().
				getConfiguration()).getTrainingInput();
		int categoryLearningId = (data.contains("[") ? new Double(data.substring(0,data.indexOf("["))).intValue() :
			new Double(data).intValue());
		data = ((OptimizationConfiguration)strategy.getCorrespondingScheduler().
				getConfiguration()).getOptimizationInput();
		int categoryOptimizationId = (data.contains("[") ? new Double(data.substring(0,data.indexOf("["))).intValue() :
			new Double(data).intValue());;
		data = ((OptimizationConfiguration)strategy.getCorrespondingScheduler().
				getConfiguration()).getTestInput();
		int categoryTestId = (data.contains("[") ? new Double(data.substring(0,data.indexOf("["))).intValue() :
			new Double(data).intValue());;
		categoryForLearningDescription = new Integer(categoryLearningId).toString();
		categoryForOptimizationDescription = new Integer(categoryOptimizationId).toString();
		categoryForTestDescription = new Integer(categoryTestId).toString();
		boolean learningCatFound = false;
		boolean optimizationCatFound = false;
		if(categoryOptimizationId < 0) {
			optimizationCatFound = true; // If optimization set is built from training data using n-fold cross-validation
		}
		boolean testCatFound = false;
		if(categoryTestId == -1) {
			testCatFound = true; // In that case it is not required to search for test category
		}
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
				if(idOfCurrentCategory == categoryLearningId) {
					categoryForLearningDescriptionFile = categoryDescriptionInstance.stringValue(fileNameAttribute);
					categoryForLearningDescription += ("-" + categoryDescriptionInstance.stringValue(categoryNameAttribute));
					learningCatFound = true;
				} else if(idOfCurrentCategory == categoryOptimizationId) {
					categoryForOptimizationDescriptionFile = categoryDescriptionInstance.stringValue(fileNameAttribute);
					categoryForOptimizationDescription += ("-" + categoryDescriptionInstance.stringValue(categoryNameAttribute));
					optimizationCatFound = true;
				} else if(idOfCurrentCategory == categoryTestId) {
					categoryForTestDescriptionFile = categoryDescriptionInstance.stringValue(fileNameAttribute);
					categoryForTestDescription += ("-" + categoryDescriptionInstance.stringValue(categoryNameAttribute));
					testCatFound = true;
				} 
				if(learningCatFound && optimizationCatFound && testCatFound) break;
				categoryDescriptionInstance = categoryDescriptionLoader.getNextInstance(categoryDescriptionLoader.getStructure());
			}
			categoryDescriptionLoader.reset();
		} catch (IOException e) {
			throw new RuntimeException("Could not load the music category information: " + e.getMessage());
		}
		
		
		// String for processed model
		String processingDesc = strategy.getConfiguration().getConstantParameterByName("Processing description").
			getAttributes().getNamedItem("stringValue").getNodeValue();
		
		processedModel = new String(
			strategy.getConfiguration().getConstantParameterByName("Processing steps").
				getAttributes().getNamedItem("stringValue").getNodeValue() + "__" + 
			strategy.getConfiguration().getConstantParameterByName("Conversion steps").
				getAttributes().getNamedItem("stringValue").getNodeValue() + "__" + 
			strategy.getConfiguration().getConstantParameterByName("Classification window size").
				getAttributes().getNamedItem("intValue").getNodeValue() + "ms_" + 
			strategy.getConfiguration().getConstantParameterByName("Classification window step size").
				getAttributes().getNamedItem("intValue").getNodeValue() + "ms");
		if(processingDesc != "" && processingDesc != null) {
			processedModel += "_" + processingDesc;
		}
		
		// These paths will be overwritten if extraction resp. processing are done here
		pathToFeatureDatabase = new String(AmusePreferences.get(KeysStringValue.FEATURE_DATABASE));
		pathToProcessingDatabase = new String(AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE));
		pathToModelDatabase = new String(strategy.getCorrespondingScheduler().getHomeFolder() +File.separator+ "input" +File.separator +"task_" +
				strategy.getCorrespondingScheduler().getTaskId() + File.separator+ "Models");

		// Load the processed features directly if no extraction / processing is optimized (only feature selection OR / AND
		// classification are optimized). It means that the optimization data can be loaded only once for all individuals.
		// Therefore loadData function for strategy.population[0] is applied (we have at least one individual).
		try {
			if(!isFeatureExtractionRequired && !isFeatureProcessingRequired) {
				trainingData = loadData(strategy.population[0],"training"); 
				if(categoryOptimizationId >= 0) {
					optimizationData = loadData(strategy.population[0],"optimization");
				}
				if(isEvaluatedOnIndependentTestSet) {
					testData = loadData(strategy.population[0],"test");
				}
			}
		} catch(NodeException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not initialize FitnessEvaluator: " + e.getMessage());
		}
		
		
		// TODO v0.2: provide saving of data sets
		/*try {
			trainingData.saveToArffFile(new File("/home/vatol/trainingSet.arff"));
			//optimizationData.saveToArffFile(new File("/home/vatol/optimizationSet.arff"));
			//testData.saveToArffFile(new File("/home/vatol/testSet.arff"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(1);*/
		
	}
	
	/**
	 * Performs the calculation of fitness value of the given ES individual
	 * @param individual Given ES individual
	 * @param isEvaluatedOnIndependentTestSet True if the evaluation is done on the independent test set; false (default) for
	 * evaluation on the optimization set
	 * @return Array with fitness value(s): the 1st one is used for single-objective optimization
	 */
	public ValidationMeasureDouble[] getFitness(ESIndividual individual, boolean isEvaluatedOnIndependentTestSet) throws NodeException {
		
		// Load the feature table
		Node featureTableNode = individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Feature table");
		FeatureTable featureTable = new FeatureTable(new File(featureTableNode.getAttributes().getNamedItem("fileValue").getNodeValue()));
		
		// Load the measure table
		Node measureTableNode = individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Measure table");
		MeasureTable measureTable;
		try {
			measureTable = new MeasureTable(new File(measureTableNode.getAttributes().getNamedItem("fileValue").getNodeValue()));
		} catch (Exception e) {
			throw new NodeException(e.getMessage());
		}

		// String with description of classification algorithm
		String classifierConfig = individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classifier configuration").
			getAttributes().getNamedItem("stringValue").getNodeValue();
		String classifierDescription = new String();
		ArffLoader classifierDescriptionLoader = new ArffLoader();
		Instance classifierDescriptionInstance;
		try {	
			int idOfSearchedAlgorithm;
			if(classifierConfig.indexOf("[") != -1) {
				idOfSearchedAlgorithm = new Integer(classifierConfig.substring(0, classifierConfig.indexOf("[")));
			} else {
				idOfSearchedAlgorithm = new Integer(classifierConfig);
			}
			classifierDescriptionLoader.setFile(new File(AmusePreferences.getClassifierAlgorithmTablePath()));
			classifierDescriptionInstance = classifierDescriptionLoader.getNextInstance(classifierDescriptionLoader.getStructure());
			Attribute idAttribute = classifierDescriptionLoader.getStructure().attribute("Id");
			Attribute nameAttribute = classifierDescriptionLoader.getStructure().attribute("Name");
			while(classifierDescriptionInstance != null) {
				int idOfCurrentAlgorithm = new Double(classifierDescriptionInstance.value(idAttribute)).intValue();
				if(idOfCurrentAlgorithm == idOfSearchedAlgorithm) {
					classifierDescription = classifierConfig + "-" + classifierDescriptionInstance.stringValue(nameAttribute);
					break;
				} 
				classifierDescriptionInstance = classifierDescriptionLoader.getNextInstance(classifierDescriptionLoader.getStructure());
			}
			classifierDescriptionLoader.reset();
		} catch (IOException e) {
			throw new NodeException("Could not load the classifier information: " + e.getMessage());
		}
		
		
		// -------------------------------------
		// (II) If the extraction must be done..
		// -------------------------------------
		if(isFeatureExtractionRequired) {
			pathToFeatureDatabase = new String(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder() + 
					File.separator + "input" +File.separator + "task_" + individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() +
					File.separator +"Features");
			
			// TODO Run extraction
			throw new NodeException("Extraction is currently not supported! " + pathToFeatureDatabase);
		}
		
		// --------------------------------------
		// (III) If the processing must be done..
		// --------------------------------------
		if(isFeatureProcessingRequired) {
			
			pathToProcessingDatabase = new String(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder() + 
					File.separator + "input" +File.separator + "task_" + individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() +
					File.separator + "Processed_Features");
			
			// ------------------------------------------------------------------------------------------------
			// (IIIa) Make the complete processing only if the classification window size is used as optimization parameter
			// ------------------------------------------------------------------------------------------------
			ArrayList<ProcessingConfiguration> processingTasks = new ArrayList<ProcessingConfiguration>();
				
			ArffLoader musicFileLoader = new ArffLoader();
			Instance musicFileInstance;
			try {
					
				// Prepare the processing configurations for learning set
				musicFileLoader.setFile(new File(categoryForLearningDescriptionFile));
				musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
				Attribute idAttribute = musicFileLoader.getStructure().attribute("Id");
				Attribute fileNameAttribute = musicFileLoader.getStructure().attribute("Path");
				while(musicFileInstance != null) {
							
					// Create the processing configuration for the current music file from the learning set
					ArrayList<Integer> fileId = new ArrayList<Integer>(1);
					fileId.add(new Double(musicFileInstance.value(idAttribute)).intValue());
					ArrayList<String> filePath = new ArrayList<String>(1);
					filePath.add(musicFileInstance.stringValue(fileNameAttribute));
					ProcessingConfiguration pConf = new ProcessingConfiguration(
						new FileTable(fileId, filePath),
						featureTable,
						individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Processing steps").
							getAttributes().getNamedItem("stringValue").getNodeValue(),
						Unit.valueOf(individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Unit").
								getAttributes().getNamedItem("stringValue").getNodeValue().toString()),
						new Integer(individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classification window size").
							getAttributes().getNamedItem("intValue").getNodeValue()),
						new Integer(individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classification window step size").
							getAttributes().getNamedItem("intValue").getNodeValue()),
						individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Conversion steps").
							getAttributes().getNamedItem("stringValue").getNodeValue(),"");
					pConf.setProcessedFeatureDatabase(pathToProcessingDatabase);
					processingTasks.add(pConf);
					
					// Go to the next music file
					musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
				}
					
				// Prepare the processing configurations for optimization set
				if(!isEvaluatedOnIndependentTestSet) {
					musicFileLoader.setFile(new File(categoryForOptimizationDescriptionFile));
					musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
					fileNameAttribute = musicFileLoader.getStructure().attribute("Path");
					while(musicFileInstance != null) {
								
						// Create the processing configuration for the current music file from the optimization set
						ArrayList<Integer> fileId = new ArrayList<Integer>(1);
						fileId.add(new Double(musicFileInstance.value(idAttribute)).intValue());
						ArrayList<String> filePath = new ArrayList<String>(1);
						filePath.add(musicFileInstance.stringValue(fileNameAttribute));
						ProcessingConfiguration pConf = new ProcessingConfiguration(
							new FileTable(fileId, filePath),
							featureTable,
							individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Processing steps").
								getAttributes().getNamedItem("stringValue").getNodeValue(),
							Unit.valueOf(individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Unit").
									getAttributes().getNamedItem("stringValue").getNodeValue().toString()),
							new Integer(individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classification window size").
								getAttributes().getNamedItem("intValue").getNodeValue()),
							new Integer(individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classificaiton window step size").
								getAttributes().getNamedItem("intValue").getNodeValue()),
							individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Conversion steps").
								getAttributes().getNamedItem("stringValue").getNodeValue(),"");
						pConf.setProcessedFeatureDatabase(pathToProcessingDatabase);
						processingTasks.add(pConf);
							
						// Go to the next music file
						musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
					}
				} else {
					
					// Prepare the processing configurations for independent test set
					musicFileLoader.setFile(new File(categoryForTestDescriptionFile));
					musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
					fileNameAttribute = musicFileLoader.getStructure().attribute("Path");
					while(musicFileInstance != null) {
								
						// Create the processing configuration for the current music file from the test set 
						ArrayList<Integer> fileId = new ArrayList<Integer>(1);
						fileId.add(new Double(musicFileInstance.value(idAttribute)).intValue());
						ArrayList<String> filePath = new ArrayList<String>(1);
						filePath.add(musicFileInstance.stringValue(fileNameAttribute));
						ProcessingConfiguration pConf = new ProcessingConfiguration(
							new FileTable(fileId, filePath),
							featureTable,
							individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Processing steps").
								getAttributes().getNamedItem("stringValue").getNodeValue(),
							Unit.valueOf(individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Unit").
									getAttributes().getNamedItem("stringValue").getNodeValue().toString()),
							new Integer(individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classification window size").
								getAttributes().getNamedItem("intValue").getNodeValue()),
							new Integer(individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classification window step size").
								getAttributes().getNamedItem("intValue").getNodeValue()),
							individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Conversion steps").
								getAttributes().getNamedItem("stringValue").getNodeValue(),"");
						pConf.setProcessedFeatureDatabase(pathToProcessingDatabase);
						processingTasks.add(pConf);
						
						// Go to the next music file
						musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
					}
				}
					
				AmuseLogger.write(MultipleTrackClassification.class.getName(), Level.DEBUG, "Starting processing...");
					
				ProcessorNodeScheduler ps = new ProcessorNodeScheduler(individual.getCorrespondingES().
					getCorrespondingScheduler().getHomeFolder() + File.separator + "input" +File.separator + "task_" +
					individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() + File.separator +"processor");
				ps.setCleanInputFolder(false);
				for(ProcessingConfiguration currentConf : processingTasks) {
					ps.proceedTask(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder() + File.separator + "input" +File.separator + "task_" +
							individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() + File.separator +"processor",
					new Long(individual.getCorrespondingES().getCorrespondingScheduler().getTaskId()), currentConf);
				}
				
				musicFileLoader.reset();
			} catch (IOException e) {
				throw new NodeException("Error during feature processing: " + e.getMessage()); 
			}
		}
		
		// ---------------------------------------------------
		// (IV) Train and validate the classification models..
		// ---------------------------------------------------
		ValidationConfiguration vConf = null;
		// (a) ..building n models using n-fold cross-validation process on training set
		if(categoryForOptimizationDescription.startsWith("-")) {
			
			// Create n models from the training set using a n-fold cross-validation procedure
			// and deliver mean measures as fitness value(s)
			if(!isEvaluatedOnIndependentTestSet) {

				// Validate the model only with the features selected by EA
				DataSet optimizationDataWithOnlySelectedFeatures = new DataSet("OptimizationSet");
				int indexOfSelectedFeaturesRepresentation = 0;
				for(int j=0;j<individual.getRepresentationList().size();j++) {
					if(individual.getRepresentationList().get(j) instanceof SelectedFeatures) {
						indexOfSelectedFeaturesRepresentation = j;
						break;
					}
				}
				for(int i=0;i<((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
						getValue().length;i++) {
					if(((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
						getValue()[i]) {
						optimizationDataWithOnlySelectedFeatures.addAttribute(trainingData.getAttribute(i));
					}
				}
					
				// Add id and category attributes
				optimizationDataWithOnlySelectedFeatures.addAttribute(trainingData.getAttribute(trainingData.getAttributeCount()-2));
				optimizationDataWithOnlySelectedFeatures.addAttribute(trainingData.getAttribute(trainingData.getAttributeCount()-1));
				
				// Start n-fold validation (the optimization category must be set to "-n")
				vConf = new ValidationConfiguration("1[" + categoryForOptimizationDescription.substring(1,categoryForOptimizationDescription.length()) + "_0]",measureTable, 
						processedModel, 
						InputFeatureType.PROCESSED_FEATURES,
						null,
						-1,
						-1,
						individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classifier configuration").
							getAttributes().getNamedItem("stringValue").getNodeValue(), 
						new DataSetInput(optimizationDataWithOnlySelectedFeatures), GroundTruthSourceType.READY_INPUT);
				vConf.setProcessedFeatureDatabase(pathToProcessingDatabase);
				vConf.setModelDatabase(pathToModelDatabase);
				ValidatorNodeScheduler vs = new ValidatorNodeScheduler(individual.getCorrespondingES().
					getCorrespondingScheduler().getHomeFolder() + File.separator + "input" + File.separator + "task_" + 
					individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() + File.separator + "validator");
				vs.setCleanInputFolder(false);
				vs.setCategoryDescription(categoryForLearningDescription);
				vs.proceedTask(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder(), 
						new Long(individual.getCorrespondingES().getCorrespondingScheduler().getTaskId()), vConf, false);
			}
			
			// If an independent test set must be used..
			else {
				
				String pathToModels = new String(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder() + File.separator + "input" +File.separator + "task_" +
						individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() + File.separator +"Models"+ File.separator +
						categoryForLearningDescription + File.separator + classifierDescription + File.separator + processedModel);
					
				// Train the model only with the features selected by EA
				DataSet trainingDataWithOnlySelectedFeatures = new DataSet("TrainingSet");
				int indexOfSelectedFeaturesRepresentation = 0;
				for(int j=0;j<individual.getRepresentationList().size();j++) {
					if(individual.getRepresentationList().get(j) instanceof SelectedFeatures) {
						indexOfSelectedFeaturesRepresentation = j;
						break;
					}
				}
				for(int i=0;i<((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
						getValue().length;i++) {
					if(((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
						getValue()[i]) {
						trainingDataWithOnlySelectedFeatures.addAttribute(trainingData.getAttribute(i));
					}
				}
					
				// Add id and category attributes
				trainingDataWithOnlySelectedFeatures.addAttribute(trainingData.getAttribute(trainingData.getAttributeCount()-2));
				trainingDataWithOnlySelectedFeatures.addAttribute(trainingData.getAttribute(trainingData.getAttributeCount()-1));
			
				TrainingConfiguration tConf = new TrainingConfiguration(
						processedModel, InputFeatureType.PROCESSED_FEATURES, null, -1, -1,
						individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classifier configuration").
							getAttributes().getNamedItem("stringValue").getNodeValue(), 
						individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classifier preprocessing").
							getAttributes().getNamedItem("stringValue").getNodeValue(),
						new DataSetInput(trainingDataWithOnlySelectedFeatures), 
						GroundTruthSourceType.READY_INPUT,
						pathToModels + File.separator + "model.mod");
				tConf.setProcessedFeatureDatabase(pathToProcessingDatabase);
				tConf.setModelDatabase(pathToModelDatabase);
				TrainerNodeScheduler ts = new TrainerNodeScheduler(individual.getCorrespondingES().
					getCorrespondingScheduler().getHomeFolder() + File.separator + "input" +File.separator + "task_" +
					individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() + File.separator + "trainer");
				ts.setCleanInputFolder(false);
				ts.proceedTask(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder(), 
					individual.getCorrespondingES().getCorrespondingScheduler().getTaskId(), tConf);
					
				// Validate the model only with the features selected by EA
				DataSet testDataWithOnlySelectedFeatures = new DataSet("TestSet");
				indexOfSelectedFeaturesRepresentation = 0;
				for(int j=0;j<individual.getRepresentationList().size();j++) {
					if(individual.getRepresentationList().get(j) instanceof SelectedFeatures) {
						indexOfSelectedFeaturesRepresentation = j;
						break;
					}
				}
				for(int i=0;i<((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
						getValue().length;i++) {
					if(((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
						getValue()[i]) {
						testDataWithOnlySelectedFeatures.addAttribute(testData.getAttribute(i));
					}
				}
					
				// Add id and category attributes
				testDataWithOnlySelectedFeatures.addAttribute(testData.getAttribute(testData.getAttributeCount()-2));
				testDataWithOnlySelectedFeatures.addAttribute(testData.getAttribute(testData.getAttributeCount()-1));
				
				vConf = new ValidationConfiguration("0[" + pathToModels+ File.separator +"model.mod" + "]",measureTable,
						processedModel, 
						InputFeatureType.PROCESSED_FEATURES,
						null,
						-1,
						-1,
						individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classifier configuration").
							getAttributes().getNamedItem("stringValue").getNodeValue(), 
						new DataSetInput(testDataWithOnlySelectedFeatures), GroundTruthSourceType.READY_INPUT);
				vConf.setProcessedFeatureDatabase(pathToProcessingDatabase);
				vConf.setModelDatabase(pathToModelDatabase);
				ValidatorNodeScheduler vs = new ValidatorNodeScheduler(individual.getCorrespondingES().
					getCorrespondingScheduler().getHomeFolder() + File.separator + "input" +File.separator + "task_" +
					individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() + File.separator + "validator");
				vs.setCleanInputFolder(false);
				vs.setCategoryDescription(categoryForTestDescription);
				vs.proceedTask(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder(), 
						new Long(individual.getCorrespondingES().getCorrespondingScheduler().getTaskId()), vConf, false);
			}
		} 
		
		// (b) ..or using a separate optimization set
		else {
			String pathToModels = new String(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder() + File.separator + "input" +File.separator + "task_" +
				individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() + File.separator +"Models"+ File.separator +
				categoryForLearningDescription + File.separator + classifierDescription + File.separator + processedModel);
			
			// Train the model only with the features selected by EA
			DataSet trainingDataWithOnlySelectedFeatures = new DataSet("TrainingSet");
			int indexOfSelectedFeaturesRepresentation = 0;
			for(int j=0;j<individual.getRepresentationList().size();j++) {
				if(individual.getRepresentationList().get(j) instanceof SelectedFeatures) {
					indexOfSelectedFeaturesRepresentation = j;
					break;
				}
			}
			for(int i=0;i<((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
					getValue().length;i++) {
				if(((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
					getValue()[i]) {
					trainingDataWithOnlySelectedFeatures.addAttribute(trainingData.getAttribute(i));
				}
			}
			
			// Add id and category attributes
			trainingDataWithOnlySelectedFeatures.addAttribute(trainingData.getAttribute(trainingData.getAttributeCount()-2));
			trainingDataWithOnlySelectedFeatures.addAttribute(trainingData.getAttribute(trainingData.getAttributeCount()-1));
	
			
			TrainingConfiguration tConf = new TrainingConfiguration(processedModel, InputFeatureType.PROCESSED_FEATURES, null, -1, -1,
					individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classifier configuration").
						getAttributes().getNamedItem("stringValue").getNodeValue(), 
					individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classifier preprocessing").
						getAttributes().getNamedItem("stringValue").getNodeValue(),
					new DataSetInput(trainingDataWithOnlySelectedFeatures), 
					GroundTruthSourceType.READY_INPUT,
					pathToModels + File.separator + "model.mod");
			tConf.setProcessedFeatureDatabase(pathToProcessingDatabase);
			tConf.setModelDatabase(pathToModelDatabase);
			TrainerNodeScheduler ts = new TrainerNodeScheduler(individual.getCorrespondingES().
				getCorrespondingScheduler().getHomeFolder() + File.separator + "input" +File.separator + "task_" +
				individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() + File.separator + "trainer");
			ts.setCleanInputFolder(false);
			ts.proceedTask(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder(), 
				individual.getCorrespondingES().getCorrespondingScheduler().getTaskId(), tConf);
			
			// ----------------------------------------
			// (V) Validate the classification model(s)
			// ----------------------------------------
			
			// Validate the model only with the features selected by EA
			DataSet optimizationDataWithOnlySelectedFeatures = new DataSet("OptimizationSet");
			DataSet testDataWithOnlySelectedFeatures = new DataSet("TestSet");
			indexOfSelectedFeaturesRepresentation = 0;
			for(int j=0;j<individual.getRepresentationList().size();j++) {
				if(individual.getRepresentationList().get(j) instanceof SelectedFeatures) {
					indexOfSelectedFeaturesRepresentation = j;
					break;
				}
			}
			for(int i=0;i<((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
					getValue().length;i++) {
				if(((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
					getValue()[i]) {
					optimizationDataWithOnlySelectedFeatures.addAttribute(optimizationData.getAttribute(i));
					if(isEvaluatedOnIndependentTestSet) {
						testDataWithOnlySelectedFeatures.addAttribute(testData.getAttribute(i));
					}
				}
			}
			
			// Add id and category attributes
			optimizationDataWithOnlySelectedFeatures.addAttribute(optimizationData.getAttribute(optimizationData.getAttributeCount()-2));
			optimizationDataWithOnlySelectedFeatures.addAttribute(optimizationData.getAttribute(optimizationData.getAttributeCount()-1));
			if(isEvaluatedOnIndependentTestSet) {
				testDataWithOnlySelectedFeatures.addAttribute(testData.getAttribute(testData.getAttributeCount()-2));
				testDataWithOnlySelectedFeatures.addAttribute(testData.getAttribute(testData.getAttributeCount()-1));
			}
			
			if(!isEvaluatedOnIndependentTestSet) {
				vConf = new ValidationConfiguration("0[" + pathToModels+ File.separator +"model.mod" + "]",measureTable,
						processedModel, 
						InputFeatureType.PROCESSED_FEATURES,
						null,
						-1,
						-1,
						individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classifier configuration").
							getAttributes().getNamedItem("stringValue").getNodeValue(), 
						new DataSetInput(optimizationDataWithOnlySelectedFeatures), GroundTruthSourceType.READY_INPUT);
			} else {
				vConf = new ValidationConfiguration("0[" + pathToModels+ File.separator +"model.mod" + "]",measureTable,
						processedModel, 
						InputFeatureType.PROCESSED_FEATURES,
						null,
						-1,
						-1,
						individual.getCorrespondingES().getConfiguration().getConstantParameterByName("Classifier configuration").
							getAttributes().getNamedItem("stringValue").getNodeValue(), 
						new DataSetInput(testDataWithOnlySelectedFeatures), GroundTruthSourceType.READY_INPUT);
			}
			vConf.setProcessedFeatureDatabase(pathToProcessingDatabase);
			vConf.setModelDatabase(pathToModelDatabase);
			ValidatorNodeScheduler vs = new ValidatorNodeScheduler(individual.getCorrespondingES().
				getCorrespondingScheduler().getHomeFolder() + File.separator + "input" +File.separator + "task_" +
				individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() + File.separator + "validator");
			vs.setCleanInputFolder(false);
			vs.setCategoryDescription(isEvaluatedOnIndependentTestSet ? categoryForTestDescription : categoryForOptimizationDescription);
			vs.proceedTask(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder(), 
					new Long(individual.getCorrespondingES().getCorrespondingScheduler().getTaskId()), vConf, false);
		}
			
		// ---------------------------------------
		// (VI) Load the measure (ES fitness value)
		// ---------------------------------------
		ArrayList<ValidationMeasure> measuresAll = vConf.getCalculatedMeasures();
		ArrayList<ValidationMeasureDouble> measures = new ArrayList<ValidationMeasureDouble>();
				
		// (a) ..measures using n-fold cross-validation process on training set
		if(categoryForOptimizationDescription.startsWith("-")) {
			for(int i=0;i<measuresAll.size();i++) {
				if(!isEvaluatedOnIndependentTestSet) {
				
					// Only double value measures are proceeded; only mean measures over n models are saved
					if(measuresAll.get(i) instanceof ValidationMeasureDouble && 
							measuresAll.get(i).getName().startsWith("mean(")) {
						ValidationMeasureDouble currentMeasure = new ValidationMeasureDouble();
						currentMeasure.setName(measuresAll.get(i).getName());
						currentMeasure.setId(measuresAll.get(i).getId());
						currentMeasure.setValue(measuresAll.get(i).getValue());
						currentMeasure.setForMinimizing(((ValidationMeasureDouble)measuresAll.get(i)).isForMinimizing());
														
						// TODO Calculate the feature selection rate
						if(currentMeasure.getId() == 203) {
							int indexOfSelectedFeaturesRepresentation = 0;
							int number = 0;
							for(int j=0;j<individual.getRepresentationList().size();j++) {
								if(individual.getRepresentationList().get(j) instanceof SelectedFeatures) {
									indexOfSelectedFeaturesRepresentation = j;
									break;
								}
							}
							for(int k=0;k<((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
									getValue().length;k++) {
								if(((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
									getValue()[k]) {
									number++;
								}
							}
							currentMeasure.setValue((double)number / (double)((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
									getValue().length);
						}
						measures.add(currentMeasure);
					}
				} else {
					if(measuresAll.get(i) instanceof ValidationMeasureDouble) {
						ValidationMeasureDouble currentMeasure = new ValidationMeasureDouble();
						currentMeasure.setName(measuresAll.get(i).getName());
						currentMeasure.setId(measuresAll.get(i).getId());
						currentMeasure.setValue(measuresAll.get(i).getValue());
						currentMeasure.setForMinimizing(((ValidationMeasureDouble)measuresAll.get(i)).isForMinimizing());
						
						// TODO Calculate the feature selection rate
						if(currentMeasure.getId() == 203) {
							int indexOfSelectedFeaturesRepresentation = 0;
							int number = 0;
							for(int j=0;j<individual.getRepresentationList().size();j++) {
								if(individual.getRepresentationList().get(j) instanceof SelectedFeatures) {
									indexOfSelectedFeaturesRepresentation = j;
									break;
								}
							}
							for(int k=0;k<((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
									getValue().length;k++) {
								if(((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
									getValue()[k]) {
									number++;
								}
							}
							currentMeasure.setValue((double)number / (double)((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
									getValue().length);
						}
						
						measures.add(currentMeasure);
					}
				}
			}
		} 
			
		// (b) ..measures from 1 model
		else {
			for(int i=0;i<measuresAll.size();i++) {
				
				// Only double value measures are proceeded
				if(measuresAll.get(i) instanceof ValidationMeasureDouble) {
					ValidationMeasureDouble currentMeasure = new ValidationMeasureDouble();
					currentMeasure.setName(measuresAll.get(i).getName());
					currentMeasure.setId(measuresAll.get(i).getId());
					currentMeasure.setValue(measuresAll.get(i).getValue());
					currentMeasure.setForMinimizing(((ValidationMeasureDouble)measuresAll.get(i)).isForMinimizing());
						
					// TODO Calculate the feature selection rate
					if(currentMeasure.getId() == 203) {
						int indexOfSelectedFeaturesRepresentation = 0;
						int number = 0;
						for(int j=0;j<individual.getRepresentationList().size();j++) {
							if(individual.getRepresentationList().get(j) instanceof SelectedFeatures) {
								indexOfSelectedFeaturesRepresentation = j;
								break;
							}
						}
						for(int k=0;k<((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
								getValue().length;k++) {
							if(((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
								getValue()[k]) {
								number++;
							}
						}
						currentMeasure.setValue((double)number / (double)((SelectedFeatures)individual.getRepresentationList().get(indexOfSelectedFeaturesRepresentation)).
								getValue().length);
					}
					
					measures.add(currentMeasure);
				}
			}
		}
		
		// Clean the generated results
		FileOperations.delete(new File(pathToModelDatabase),true);
		FileOperations.delete(new File(individual.getCorrespondingES().getCorrespondingScheduler().getHomeFolder() + 
			File.separator + "input" + File.separator + "task_" + individual.getCorrespondingES().getCorrespondingScheduler().getTaskId() +  File.separator +"Processed_Features"),true);
			
		ValidationMeasureDouble[] measuresAsArray = new ValidationMeasureDouble[measures.size()];
		for(int i=0;i<measuresAsArray.length;i++) {
			if(measures.get(i) instanceof ValidationMeasureDouble) {
				measuresAsArray[i] = (ValidationMeasureDouble)measures.get(i);
			} else {
				throw new NodeException("Only double measures can be used for optimization; please remove the measure '" + 
					measures.get(i).getName() + "'");
			}
		}
		
		return measuresAsArray;
	}
	
	private DataSet loadData(ESIndividual individual, String string) throws NodeException {
		DataSet data = null;
		
		// Input files with processed features
		ArrayList<String> inputProcessedFeatureFiles = new ArrayList<String>();
		ArrayList<Double> ids = new ArrayList<Double>();
		ArrayList<String> labels = new ArrayList<String>();
		ArffLoader musicFileLoader = new ArffLoader();
		Instance musicFileInstance;
		
		// What set must be loaded?
		try {
			if(string.equals("training")) {
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading training set...");
				
				// Check if the path to the previously saved ARFF with training set is given
				String inputToLearn = ((OptimizationConfiguration)individual.getCorrespondingES().getCorrespondingScheduler().
						getConfiguration()).getTrainingInput();
				if(inputToLearn.contains("[")) {
					data = new DataSet(new File(inputToLearn.substring(inputToLearn.indexOf("[")+1,inputToLearn.indexOf("]"))));
					return data;
				}
				data = new DataSet("TrainingSet");
				musicFileLoader.setFile(new File(categoryForLearningDescriptionFile));
			} else if(string.equals("optimization")) {
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading optimization set...");
				
				// Check if the path to the previously saved ARFF with optimization set is given
				String inputToOptimize = ((OptimizationConfiguration)individual.getCorrespondingES().getCorrespondingScheduler().
						getConfiguration()).getOptimizationInput();
				if(inputToOptimize.contains("[")) {
					data = new DataSet(new File(inputToOptimize.substring(inputToOptimize.indexOf("[")+1,inputToOptimize.indexOf("]"))));
					return data;
				}
				data = new DataSet("OptimizationSet");
				musicFileLoader.setFile(new File(categoryForOptimizationDescriptionFile));
			} else if(string.equals("test")) {
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading test set...");
				
				// Check if the path to the previously saved ARFF with test set is given
				String inputToTest = ((OptimizationConfiguration)individual.getCorrespondingES().getCorrespondingScheduler().
						getConfiguration()).getTestInput();
				if(inputToTest.contains("[")) {
					data = new DataSet(new File(inputToTest.substring(inputToTest.indexOf("[")+1,inputToTest.indexOf("]"))));
					return data;
				}
				data = new DataSet("TestSet");
				musicFileLoader.setFile(new File(categoryForTestDescriptionFile));
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new NodeException("Error during loading of processed features: " + e.getMessage());
		}
			
		// Load the data
		try {
			musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
			Attribute fileNameAttribute = musicFileLoader.getStructure().attribute("Path");
			Attribute idAttribute = musicFileLoader.getStructure().attribute("Id");
			Attribute labelAttribute = musicFileLoader.getStructure().attribute("Category");
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
				String label = musicFileInstance.stringValue(labelAttribute);
				Double confidence = musicFileInstance.value(relationshipAttribute);
				// TODO Parameter?
				if(confidence >= 0.5) {
					labels.add(label);
				} else {
					labels.add("NOT_" + label);
				}
				

				// Go to the next music file
				musicFileInstance = musicFileLoader.getNextInstance(musicFileLoader.getStructure());
			}
					
			// Load the processed features of the current file
			for(int currFile = 0;currFile<inputProcessedFeatureFiles.size();currFile++) {
				AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading processed features of: " + inputProcessedFeatureFiles.get(currFile));
				DataSetAbstract featuresOfCurrentFile = new ArffDataSet(new File(inputProcessedFeatureFiles.get(currFile)));
					
				// For the first time, attributes must be created!
				if(data.getAttributeCount() == 0) {
					
					// Omit unit, start and end attributes at the end
					for(int i=0;i<featuresOfCurrentFile.getAttributeCount()-3;i++) {
						data.addAttribute(new NumericAttribute(featuresOfCurrentFile.getAttribute(i).getName(), new ArrayList<Double>()));
					}
					
					// Add id and category attributes
					data.addAttribute(new NumericAttribute("Id", new ArrayList<Double>()));
					data.addAttribute(new StringAttribute("Category", new ArrayList<String>()));
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
			
		return data;
	}

	@Override
	public void close() throws NodeException {
		// Do nothing
	}

}



