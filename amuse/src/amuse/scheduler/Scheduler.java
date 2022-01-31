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
 */ 
package amuse.scheduler;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Level;

import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.scheduler.SchedulerException;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.optimizer.OptimizationConfiguration;
import amuse.nodes.processor.ProcessingConfiguration;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.validator.ValidationConfiguration;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysBooleanValue;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.pluginmanagement.PluginInstaller;
import amuse.scheduler.pluginmanagement.PluginLoader;
import amuse.scheduler.pluginmanagement.PluginRemover;
import amuse.scheduler.taskstarters.ClassificationStarter;
import amuse.scheduler.taskstarters.ClassificationTrainingStarter;
import amuse.scheduler.taskstarters.ClassificationValidationStarter;
import amuse.scheduler.taskstarters.FeatureExtractionStarter;
import amuse.scheduler.taskstarters.FeatureProcessingStarter;
import amuse.scheduler.taskstarters.OptimizationStarter;
import amuse.scheduler.taskstarters.ToolStarter;
import amuse.scheduler.tools.ToolConfiguration;
import amuse.util.AmuseLogger;

/**
 * Scheduler is a central component of the Amuse. It can be started either from the command line or from
 * GUI. Scheduler delegates the Amuse tasks (e.g. feature extraction or classification) to the corresponding nodes.
 * The configurations of tasks can be given as TaskConfiguration class or as String with path to Arff configuration
 * file.
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class Scheduler implements Runnable {

	/** The instance of a scheduler */ 
	private static Scheduler schedulerInstance;
	
	/** Counter of all jobs processed during the current run of Amuse scheduler */
	private long jobCounter;
	
	/** Scheduler properties */
	private Properties properties = null;
	
	/** Main arguments defining the tasks */
	private String[] taskList = null;
	
	/** Scheduler private constructor */
	private Scheduler() {
		this.jobCounter = 0l;
		try {
			File jobCounterFile = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + "/config/jobcounter.txt");
			if(!jobCounterFile.exists()) {
				FileOutputStream values_toTest = new FileOutputStream(jobCounterFile);
				DataOutputStream values_writerTest = new DataOutputStream(values_toTest);
				values_writerTest.writeBytes("0");
				values_toTest.close();
			}
			FileReader inputReader = new FileReader(jobCounterFile);
			BufferedReader bufferedInputReader = new BufferedReader(inputReader);
			String line =  new String();
			line = bufferedInputReader.readLine();
			this.jobCounter = new Integer(line);
			inputReader.close();
		} catch(Exception e) {
			AmuseLogger.write(this.getClass().getName(),Level.ERROR,"Could not load the number of batch jobs: " + e.getMessage());
		}
		try {
			PluginLoader.loadPlugins(new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "lib" + File.separator + "plugins"));
		} catch(SchedulerException e) {
			AmuseLogger.write(this.getClass().getName(),Level.ERROR,"Could not load properly AMUSE plugins, further " +
					"errors are possible (" + e.getMessage() + ")");
		}
	}
	
	/**
	 * This method is used to get an instance of Scheduler (as singleton)
	 * @return instance of Scheduler
	 */
	public static Scheduler getInstance() {
		if (schedulerInstance == null){
			synchronized (Scheduler.class) {
				if (schedulerInstance == null) {
					schedulerInstance = new Scheduler();
					AmuseLogger.write(schedulerInstance.getClass().getName(),Level.INFO,"Scheduler started...");
                }
			}
		}
		return schedulerInstance;
	}
	
	/**
	 * Starts the Scheduler from command line
	 * @param args List with tasks 
	 */
	public static void main(String[] args) {
		AmuseLogger.write(Scheduler.class.getName(),Level.INFO,"Scheduler started");
		
		if(args.length == 0) {
			AmuseLogger.write(Scheduler.class.getName(),Level.FATAL,"No tasks defined");
			return;
		}
		
		schedulerInstance = new Scheduler();
		Thread schedulerThread = new Thread(schedulerInstance);
		schedulerInstance.taskList = args;
		schedulerThread.start();
	}
	
	/**
	 * Starts the Scheduler
	 */
	public void run() {
		// Create input directory for logs of ready grid jobs
		File inputDir = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "taskinput");
		if(!inputDir.exists()) {
			if(!inputDir.mkdirs()) {
				AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not create an input folder for task logs");
				System.exit(1);
			}
		}
		
		// Clear input directory which may contain logs from previous jobs
		if(inputDir.listFiles().length != 0) {
			for(int j=0;j<inputDir.listFiles().length;j++) {
				if(!inputDir.listFiles()[j].delete()) {
					AmuseLogger.write(this.getClass().getName(),Level.ERROR,
						"Problem by deleting of the old jobs");
				}
			}
		} 
		
		// Create output directory for task configurations of ongoing grid jobs
		File outputDir = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "taskoutput");
		if(!outputDir.exists()) {
			if(!outputDir.mkdirs()) {
				AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not create an output folder for job configurations");
				System.exit(1);
			}
		}
		
		// Clear output directory which may contain configurations from previous jobs
		/*if(outputDir.listFiles().length != 0) {
			for(int j=0;j<outputDir.listFiles().length;j++) {
				if(!outputDir.listFiles()[j].delete()) {
					AmuseLogger.write(this.getClass().getName(),Level.ERROR,
						"Problem by deleting of the old job configurations");
				}
			}
		} */
		
		// Go through tasks defined in the command line
		try {
			for(int counterOfTaskLines = 0; counterOfTaskLines < taskList.length; counterOfTaskLines++) {
				if (taskList[counterOfTaskLines].equals("-start_loop")) {

					// Start of Amuse in loop mode requires 1 parameter: 
					// -start_loop folder4Tasks
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Path to task input folder missing!");
						System.exit(1);
					}
					AmuseLogger.write(this.getClass().getName(),Level.INFO, "Scheduler started in loop mode");
					waitForTasksInLoopMode(new File(taskList[++counterOfTaskLines]));

				} else if(taskList[counterOfTaskLines].equals("-fe")) {
					
					// Only 1 parameter is required!
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
						System.exit(1);
					} 
					
					proceedExtractionTasks(ExtractionConfiguration.loadConfigurationsFromFile(new File(taskList[++counterOfTaskLines])));
				} else if (taskList[counterOfTaskLines].equals("-fp")) {

					// Only 1 parameter is required!
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
						System.exit(1);
					} 
					
					proceedProcessingTasks(ProcessingConfiguration.loadConfigurationsFromFile(new File(taskList[++counterOfTaskLines])));
				} else if (taskList[counterOfTaskLines].equals("-ct")) {
					
					// Only 1 parameter is required!
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
						System.exit(1);
					} 
					
					proceedClassificationTrainingTasks(TrainingConfiguration.loadConfigurationsFromFile(new File(taskList[++counterOfTaskLines])));
				} else if (taskList[counterOfTaskLines].equals("-c")) {

					// Only 1 parameter is required!
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
						System.exit(1);
					} 
					
					proceedClassificationTasks(ClassificationConfiguration.loadConfigurationsFromFile(new File(taskList[++counterOfTaskLines])));
				} else if (taskList[counterOfTaskLines].equals("-v")) {

					// Only 1 parameter is required!
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
						System.exit(1);
					} 
					
					proceedValidationTasks(ValidationConfiguration.loadConfigurationsFromFile(new File(taskList[++counterOfTaskLines])));
				} else if (taskList[counterOfTaskLines].equals("-o")) {

					// Only 1 parameter is required!
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
						System.exit(1);
					} 
					
					proceedOptimizationTasks(OptimizationConfiguration.loadConfigurationsFromFile(new File(taskList[++counterOfTaskLines])));
				} else if (taskList[counterOfTaskLines].equals("-pi")) {

					// Only 1 parameter is required!
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
						System.exit(1);
					} 
					
					PluginInstaller pm = new PluginInstaller(taskList[++counterOfTaskLines]);
					pm.installPlugin();
				} else if (taskList[counterOfTaskLines].equals("-pr")) {

					// Only 1 parameter is required!
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
						System.exit(1);
					} 
					
					PluginRemover pm = new PluginRemover(new Integer(taskList[++counterOfTaskLines]));
					pm.removePlugin();
				} else if (taskList[counterOfTaskLines].equals("-t")) {

					// Only 1 parameter is required!
					if(counterOfTaskLines+1 >= taskList.length) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
						System.exit(1);
					} 
					
					proceedToolTasks(ToolConfiguration.loadConfigurationsFromFile(new File(taskList[++counterOfTaskLines])));
				} else {
					AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly");
					System.exit(1);
				}
			} 
		} catch(SchedulerException e) {
			AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Some of the tasks could not be started: " + 
					e.getMessage());
			System.exit(1);
		} catch(IOException e) {
			AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Some of the tasks could not be started: " + 
					e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Waits until all grid jobs belonging to one task are finished 
	 */
	private void waitForJobs(Long numberOfJobsToWaitFor) {
		File inputDir = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "taskinput");
		
		// Sleep and look up for finished jobs
		boolean isReady = false;
		try {
			while(!isReady) {
				// If there are no new job log files in input folder, sleep a bit
				if(inputDir.listFiles().length == 0) {
					Thread.sleep(1000);
				} 
				// If there are any job log files...
				else {
					for(int j=0;j<inputDir.listFiles().length;j++) {
						String fileName = inputDir.listFiles()[j].getPath();
						
						if(!fileName.substring(fileName.lastIndexOf(File.separator)+1,fileName.length()).startsWith(".")) {
						
							// TODO extract the exact Amuse job ID 
							AmuseLogger.write(this.getClass().getName(),Level.INFO,
								"Job " + fileName + " ready");
	
							// TODO RELEASE 0.2 The log results from nodes can be saved somewhere if required...
							
							// Delete logs
							if(!inputDir.listFiles()[j].delete()) {
								AmuseLogger.write(this.getClass().getName(),Level.FATAL,
										"Log of job " + fileName.substring(fileName.lastIndexOf(".")+1) + 
										" could not be deleted; Can't calculate properly if all jobs have been finished!");
								System.exit(1);
							}
							// Update the number of currently running jobs
							numberOfJobsToWaitFor--;
							
							// Are all extraction jobs ready? If yes, finish Amuse
							if(numberOfJobsToWaitFor == 0l) {
								isReady = true;
							}
						}
					}
				}
			}
		} catch(InterruptedException e) {
			AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Scheduler interrupted: " + e.getMessage());
			System.exit(1);
		}	
	}

	/**
	 * Waits for new tasks in a loop mode. The scheduler listens to the task folder, and if a new text
	 * file with task description appears, it is processed.
	 * @param taskFolder Folder for incoming tasks
	 */
	private void waitForTasksInLoopMode(File taskFolder) {
		// Sleep and look up for new tasks
		boolean isReady = false;
		try {
			while(!isReady) {
				if(!taskFolder.exists()) {
					AmuseLogger.write(this.getClass().getName(),Level.FATAL, "Task input folder " + taskFolder.getAbsolutePath() + " does not exist!");
					System.exit(1);
				}
				
				// If there are no new job log files in input folder, sleep a bit
				if(taskFolder.listFiles().length == 0) {
					// TODO Amuse configuration parameter
					Thread.sleep(5000);
				} 
				// If there are any task files...
				else {
					// Take the first file
					String fileName = taskFolder.listFiles()[0].getPath();
					AmuseLogger.write(this.getClass().getName(),Level.INFO,
						"New task " + fileName + " found");

					// Read the task
					FileReader taskInput = null;
					taskInput = new FileReader(fileName);
					BufferedReader featuresReader = new BufferedReader(taskInput);
					String line =  new String();
					line = featuresReader.readLine();
					if (line != null) {
						
						// Run the task
						StringTokenizer taskJobs = new StringTokenizer(line," ");
						while(taskJobs.hasMoreElements()) {
							try {
								String currentTask = taskJobs.nextToken();
								if(currentTask.equals("-end_loop")) {
									AmuseLogger.write(this.getClass().getName(),Level.INFO, "Scheduler finished loop mode");
									isReady=true;
									break;
								} else if(currentTask.equals("-fe")) {
									
									// Only 1 parameter is required!
									if(taskJobs.countTokens() < 1) {
										AmuseLogger.write(this.getClass().getName(),Level.ERROR,"Could not parse the command line properly," +
												"feature extraction task aborted");
									} else {
										proceedExtractionTasks(ExtractionConfiguration.loadConfigurationsFromFile(new File(taskJobs.nextToken())));
									}
								} else if (currentTask.equals("-fp")) {
									
									// Only 1 parameter is required!
									if(taskJobs.countTokens() < 1) {
										AmuseLogger.write(this.getClass().getName(),Level.ERROR,"Could not parse the command line properly," +
												"feature processing task aborted");
									} else {
										proceedProcessingTasks(ProcessingConfiguration.loadConfigurationsFromFile(new File(taskJobs.nextToken())));
									}
								} else if (currentTask.equals("-ct")) {
									
									// Only 1 parameter is required!
									if(taskJobs.countTokens() < 1) {
										AmuseLogger.write(this.getClass().getName(),Level.ERROR,"Could not parse the command line properly," +
												"classification training task aborted");
									} else {
										proceedClassificationTrainingTasks(TrainingConfiguration.loadConfigurationsFromFile(new File(taskJobs.nextToken())));
									}
								} else if (currentTask.equals("-c")) {
									
									// Only 1 parameter is required!
									if(taskJobs.countTokens() < 1) {
										AmuseLogger.write(this.getClass().getName(),Level.ERROR,"Could not parse the command line properly," +
												"classification task aborted");
									} else {
										proceedClassificationTasks(ClassificationConfiguration.loadConfigurationsFromFile(new File(taskJobs.nextToken())));
									}
								} else if (currentTask.equals("-v")) {
									
									// Only 1 parameter is required!
									if(taskJobs.countTokens() < 1) {
										AmuseLogger.write(this.getClass().getName(),Level.ERROR,"Could not parse the command line properly," +
												"validation task aborted");
									} else {
										proceedValidationTasks(ValidationConfiguration.loadConfigurationsFromFile(new File(taskJobs.nextToken())));
									}
								} else if (currentTask.equals("-o")) {
									
									// Only 1 parameter is required!
									if(taskJobs.countTokens() < 1) {
										AmuseLogger.write(this.getClass().getName(),Level.ERROR,"Could not parse the command line properly," +
												"optimization task aborted");
									} else {
										proceedOptimizationTasks(OptimizationConfiguration.loadConfigurationsFromFile(new File(taskJobs.nextToken())));
									}
								}  else if (currentTask.equals("-pi")) {

									// Only 1 parameter is required!
									if(taskJobs.countTokens() < 1) {
										AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly," +
												"plugin installation aborted");
										System.exit(1);
									} 
									
									PluginInstaller pm = new PluginInstaller(taskJobs.nextToken());
									pm.installPlugin();
								} else if (currentTask.equals("-pr")) {

									// Only 1 parameter is required!
									if(taskJobs.countTokens() < 1) {
										AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Could not parse the command line properly," + 
												"plugin deinstallation aborted");
										System.exit(1);
									} 
									
									PluginRemover pm = new PluginRemover(new Integer(taskJobs.nextToken()));
									pm.removePlugin();
								}  else {
									AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Unknown parameter " + currentTask);
								}
								
								
								
							} catch(SchedulerException e) { 
								AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Some of the tasks could not be started: " + 
										e.getMessage());
								System.exit(1);
							}
						}
		            }
					featuresReader.close();
					
					// Delete task file
					if(!taskFolder.listFiles()[0].delete()) {
						AmuseLogger.write(this.getClass().getName(),Level.FATAL,
								"Task " + fileName.substring(fileName.lastIndexOf(".")+1) + 
								" could not be deleted; Can't calculate properly if all jobs have been finished!");
						System.exit(1);
					}
				}
			}
		} catch(InterruptedException e) {
			AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Scheduler interrupted: " + e.getMessage());
			System.exit(1);
		} catch(FileNotFoundException e) {
			AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Scheduler error: File not found: " + e.getMessage());
			System.exit(1);
		} catch(IOException e) {
			AmuseLogger.write(this.getClass().getName(),Level.FATAL,"Scheduler error: " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Proceeds several tasks
	 * @param taskConfiguration Task configurations
	 */
	public synchronized void proceedTask(TaskConfiguration[] taskConfiguration) throws SchedulerException {
		for(TaskConfiguration c : taskConfiguration) {
			proceedTask(c);
		}
	}
	
	/**
	 * Proceeds a task with a given configuration
	 * @param taskConfiguration Task configuration
	 */
	public synchronized void proceedTask(TaskConfiguration taskConfiguration) throws SchedulerException {
		try {
			if(taskConfiguration instanceof ExtractionConfiguration) { 
				ExtractionConfiguration[] ec = {(ExtractionConfiguration)taskConfiguration};
				proceedExtractionTasks(ec);
			} else if(taskConfiguration instanceof ProcessingConfiguration) {
				ProcessingConfiguration[] pc = {(ProcessingConfiguration)taskConfiguration};
				proceedProcessingTasks(pc);
			} else if(taskConfiguration instanceof TrainingConfiguration) {
				TrainingConfiguration[] tc = {(TrainingConfiguration)taskConfiguration};
				proceedClassificationTrainingTasks(tc);
			} else if(taskConfiguration instanceof ClassificationConfiguration) {
				ClassificationConfiguration[] cc = {(ClassificationConfiguration)taskConfiguration};
				proceedClassificationTasks(cc);
			} else if(taskConfiguration instanceof ValidationConfiguration) {
				ValidationConfiguration[] vc = {(ValidationConfiguration)taskConfiguration};
				proceedValidationTasks(vc);
			} else if(taskConfiguration instanceof OptimizationConfiguration) {
				OptimizationConfiguration[] oc = {(OptimizationConfiguration)taskConfiguration};
				proceedOptimizationTasks(oc);
			}
		}			
		catch(SchedulerException e) {
			throw e;
		}
	}
	
	/**
	 * Delegates the extraction task(s) to the corresponding task starter
	 * @param extractionConfigurations Task configuration(s)
	 * @throws SchedulerException
	 */
	private void proceedExtractionTasks(ExtractionConfiguration[] extractionConfigurations) throws SchedulerException {
		FeatureExtractionStarter fes;
		// If the task will be started via grid, wait until they are ready
		if(AmusePreferences.getBoolean(KeysBooleanValue.USE_GRID_EXTRACTOR) == true) {
			fes = new FeatureExtractionStarter("extractor", schedulerInstance.jobCounter, false);
			Long currentLastJob = new Long(jobCounter);
			schedulerInstance.jobCounter = fes.startTask(extractionConfigurations, properties);
			waitForJobs(schedulerInstance.jobCounter - currentLastJob);
		} else {
			fes = new FeatureExtractionStarter("extractor", schedulerInstance.jobCounter, true);
			schedulerInstance.jobCounter = fes.startTask(extractionConfigurations, properties);
		}
		fes.logResults();
		
	}
	
	
	
	/**
	 * Delegates the processing task(s) to the corresponding task starter
	 * @param processingConfigurations Task configuration(s)
	 * @throws SchedulerException
	 */
	private void proceedProcessingTasks(ProcessingConfiguration[] processingConfigurations) throws SchedulerException {
		FeatureProcessingStarter fps;
		// If the task will be started via grid, wait until they are ready
		if(AmusePreferences.getBoolean(KeysBooleanValue.USE_GRID_PROCESSOR) == true) {
			fps = new FeatureProcessingStarter("processor", schedulerInstance.jobCounter, false);
			Long currentLastJob = new Long(jobCounter);
			schedulerInstance.jobCounter = fps.startTask(processingConfigurations, properties);
			waitForJobs(schedulerInstance.jobCounter - currentLastJob);
		} else {
			fps = new FeatureProcessingStarter("processor", schedulerInstance.jobCounter, true);
			schedulerInstance.jobCounter = fps.startTask(processingConfigurations, properties);
		}
		fps.logResults();
	}
	
	/**
	 * Delegates the classification training task(s) to the corresponding task starter
	 * @param trainingConfigurations Task configuration(S)
	 * @throws SchedulerException
	 */
	private void proceedClassificationTrainingTasks(TrainingConfiguration[] trainingConfigurations) throws SchedulerException {
		ClassificationTrainingStarter cts;
		// If the task will be started via grid, wait until they are ready
		if(AmusePreferences.getBoolean(KeysBooleanValue.USE_GRID_TRAINER) == true) {
			cts = new ClassificationTrainingStarter("trainer", schedulerInstance.jobCounter, false);
			Long currentLastJob = new Long(jobCounter);
			schedulerInstance.jobCounter = cts.startTask(trainingConfigurations, properties);
			waitForJobs(schedulerInstance.jobCounter - currentLastJob);
		} else {
			cts = new ClassificationTrainingStarter("trainer", schedulerInstance.jobCounter, true);
			schedulerInstance.jobCounter = cts.startTask(trainingConfigurations, properties);
		}
		cts.logResults();
	}
	
	/**
	 * Delegates the classification task(s) to the corresponding task starter
	 * @param classificationConfigurations Task configuration(s)
	 * @throws SchedulerException
	 */
	private void proceedClassificationTasks(ClassificationConfiguration[] classificationConfigurations) throws SchedulerException {
		ClassificationStarter cs;
		// If the task will be started via grid, wait until they are ready
		if(AmusePreferences.getBoolean(KeysBooleanValue.USE_GRID_CLASSIFIER) == true) {
			cs = new ClassificationStarter("classifier", schedulerInstance.jobCounter, false);
			Long currentLastJob = new Long(jobCounter);
			schedulerInstance.jobCounter = cs.startTask(classificationConfigurations, properties);
			waitForJobs(schedulerInstance.jobCounter - currentLastJob);
		} else {
			cs = new ClassificationStarter("classifier", schedulerInstance.jobCounter, true);
			schedulerInstance.jobCounter = cs.startTask(classificationConfigurations, properties);
		}
		cs.logResults();
	}
	
	/**
	 * Delegates the validation task(s) to the corresponding task starter
	 * @param validationConfigurations Task configuration(s)
	 * @throws SchedulerException
	 */
	private void proceedValidationTasks(ValidationConfiguration[] validationConfigurations) throws SchedulerException {
		ClassificationValidationStarter cvs;
		// If the task will be started via grid, wait until they are ready
		if(AmusePreferences.getBoolean(KeysBooleanValue.USE_GRID_VALIDATOR) == true) {
			cvs = new ClassificationValidationStarter("validator", schedulerInstance.jobCounter, false);
			Long currentLastJob = new Long(jobCounter);
			schedulerInstance.jobCounter = cvs.startTask(validationConfigurations, properties);
			waitForJobs(schedulerInstance.jobCounter - currentLastJob);
		} else {
			cvs = new ClassificationValidationStarter("validator", schedulerInstance.jobCounter, true);
			schedulerInstance.jobCounter = cvs.startTask(validationConfigurations, properties);
		}
		cvs.logResults();
	}
	
	/**
	 * Delegates the optimization task(s) to the corresponding task starter
	 * @param optimizationConfigurations Task configuration(s)
	 * @throws SchedulerException
	 */
	private void proceedOptimizationTasks(OptimizationConfiguration[] optimizationConfigurations) throws SchedulerException {
		OptimizationStarter os;
		// If the task will be started via grid, wait until they are ready
		if(AmusePreferences.getBoolean(KeysBooleanValue.USE_GRID_OPTIMIZER) == true) {
			os = new OptimizationStarter("optimizer", schedulerInstance.jobCounter, false);
			Long currentLastJob = new Long(jobCounter);
			schedulerInstance.jobCounter = os.startTask(optimizationConfigurations, properties);
			waitForJobs(schedulerInstance.jobCounter - currentLastJob);
		} else {
			os = new OptimizationStarter("optimizer", schedulerInstance.jobCounter, true);
			schedulerInstance.jobCounter = os.startTask(optimizationConfigurations, properties);
		}
		os.logResults();
	}
	
	/**
	 * Delegates the tool task(s) to the corresponding task starter
	 * @param toolConfigurations Task configuration(s)
	 * @throws SchedulerException
	 */
	private void proceedToolTasks(ToolConfiguration[] toolConfigurations) throws SchedulerException {
		ToolStarter ts;
		// If the task will be started via grid, wait until they are ready
		if(AmusePreferences.getBoolean(KeysBooleanValue.USE_GRID_TOOL) == true) {
			ts = new ToolStarter("tool", schedulerInstance.jobCounter, false);
			Long currentLastJob = new Long(jobCounter);
			schedulerInstance.jobCounter = ts.startTask(toolConfigurations, properties);
			waitForJobs(schedulerInstance.jobCounter - currentLastJob);
		} else {
			ts = new ToolStarter("tool", schedulerInstance.jobCounter, true);
			schedulerInstance.jobCounter = ts.startTask(toolConfigurations, properties);
		}
		ts.logResults();
	}
}
