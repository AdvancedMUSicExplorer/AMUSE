package amuse.nodes.extractor.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.data.FileTable;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.Attribute;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.extractor.ExtractionConfiguration;
import amuse.nodes.extractor.ExtractorNodeScheduler;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

public class WindowedAttackSlopesAdapter extends AmuseTask implements ExtractorInterface {
	
	/** Input music file */
	private String musicFile;
	
	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;
	
	/** Feature table with the base features that are extracted and then converted */
	private FeatureTable featureTableBase;
	
	/** Feature extractors for the base features */
	private List<ExtractorInterface> extractors;

	@Override
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
		// musicFile need not to be saved to input script since Matlab reads the music files from a given directory and 
		// does not use music file names
		this.musicFile = musicFile;
		this.currentPart = currentPart;
	}

	@Override
	public void convertBaseScript(HashMap<Integer, Integer> feature2Tool, FeatureTable featureTable)
			throws NodeException {
		// convert basescripts for features 400 (matlab, 2), 419 (MIRToolbox, 4) and 426 (MIRToolbox, 4)
		HashMap<Integer, Integer> feature2ToolBase = new HashMap<Integer, Integer>();
		feature2ToolBase.put(400, 2);
		feature2ToolBase.put(423, 4);
		feature2ToolBase.put(426, 4);
		
		for(ExtractorInterface ead : extractors) {
			ead.convertBaseScript(feature2ToolBase, featureTableBase);
		}
		
	}

	@Override
	public void extractFeatures() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature extraction...");
		// extract the base features
		for(ExtractorInterface ead : extractors) {
			((AmuseTask)ead).configure(((AmuseTask)ead).getProperties(), correspondingScheduler, "");
			ead.setFilenames(musicFile, null, currentPart);
			ead.extractFeatures();
		}
		System.out.println("Base features extracted!");
		convertOutput();
	}

	@Override
	public void convertOutput() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature conversion...");
		// convert the base features
		String musicFileName = this.musicFile.substring(musicFile.lastIndexOf(File.separator) + 1, musicFile.lastIndexOf("."));
		
		String durationOfMusicPiecePath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + 
				File.separator + this.currentPart + File.separator + "MatlabFeatures" + File.separator + musicFileName + "_400.arff";
		String attackTimesPath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() + 
				File.separator + this.currentPart + File.separator + "MIRToolbox" + File.separator + musicFileName + "_423.arff";
		String attackSlopesPath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() +
				File.separator + this.currentPart + File.separator + "MIRToolbox" + File.separator + musicFileName + "_426.arff";
		
		DataSet durationOfMusicPieceDataSet = null;
		DataSet attackTimesDataSet = null;
		DataSet attackSlopesDataSet = null;
		try {
			durationOfMusicPieceDataSet = new DataSet(new File(durationOfMusicPiecePath));
			attackTimesDataSet = new DataSet(new File(attackTimesPath));
			attackSlopesDataSet = new DataSet(new File(attackSlopesPath));
		} catch (IOException e) {
			throw new NodeException(e.getLocalizedMessage());
		}
		
		double durationOfMusicPiece = (double)durationOfMusicPieceDataSet.getAttribute(0).getValueAt(0);
		int windowSize = 512;
		int stepSize = 512;
		int sampleRate = 22050;
		int numberOfWindows = (int)Math.ceil((durationOfMusicPiece*sampleRate - windowSize) / stepSize + 1);
		System.out.println(numberOfWindows);
		
		double[] attackSlopeWindows = new double[numberOfWindows];
		int currentAttack = -1;
		double currentSlope = Double.NaN;
		for(int i = 0; i < numberOfWindows; i++) {
			double middleOfWindow = i * ((double)stepSize/sampleRate) + ((double)windowSize/sampleRate)/2;
			while(currentAttack + 1 < attackTimesDataSet.getValueCount() && middleOfWindow >= (double)attackTimesDataSet.getAttribute(0).getValueAt(currentAttack + 1)) {
				currentAttack++;
				currentSlope = (double)attackSlopesDataSet.getAttribute(0).getValueAt(currentAttack);
			}
			attackSlopeWindows[i] = currentSlope;
		}
		
		String featureFolderPath = this.correspondingScheduler.getHomeFolder() + File.separator + "input" + File.separator + "task_" + this.correspondingScheduler.getTaskId() +
				File.separator + this.currentPart + File.separator + "AttackSlopes";
		File featureFolder = new File(featureFolderPath);
		if(featureFolder.exists()) {
			File[] files = featureFolder.listFiles();
			for(File f : files) {
				f.delete();
			}
		} else {
			featureFolder.mkdirs();
		}
		String featureFilePath = featureFolderPath + File.separator + musicFileName + "_429.arff";
		File featureFile = new File(featureFilePath);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(featureFile);
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
			
			bufferedWriter.write("@RELATION 'Music feature'\n");
			bufferedWriter.write("%rows=1\n");
			bufferedWriter.write("%columns=" + numberOfWindows + "\n");
			bufferedWriter.write("%sample_rate=" + sampleRate +"\n");
			bufferedWriter.write("%window_size=" + windowSize + "\n");
			bufferedWriter.write("%step_size=" + stepSize + "\n");
			bufferedWriter.write("\n");
			bufferedWriter.write("@ATTRIBUTE 'Attack slopes as windowed numeric' NUMERIC\n");
			bufferedWriter.write("@ATTRIBUTE WindowNumber NUMERIC\n");
			bufferedWriter.write("\n");
			bufferedWriter.write("@DATA\n");
			
			for(int i = 0; i < attackSlopeWindows.length; i++) {
				bufferedWriter.write(attackSlopeWindows[i] + "," + (i+1));
				if(i < attackSlopeWindows.length - 1) {
					bufferedWriter.write("\n");
				}
			}
			bufferedWriter.close();
			
		} catch (IOException e) {
			throw new NodeException(e.getLocalizedMessage());
		}
		
	}

	@Override
	public void setParameters(String parameterString) throws NodeException {
		// Do nothing, since initialization is not required	
	}

	@Override
	public void initialize() throws NodeException {
		extractors = new ArrayList<ExtractorInterface>();
		String featureTablePath = AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + "AttackSlopes" + File.separator + "featureTable.arff";
		featureTableBase = new FeatureTable(new File(featureTablePath));
		try {	
			DataSetAbstract toolTableSet = new ArffDataSet(new File(AmusePreferences.getFeatureExtractorToolTablePath()));
			Attribute idAttribute = toolTableSet.getAttribute("Id");
			Attribute extractorNameAttribute = toolTableSet.getAttribute("Name");
			Attribute adapterClassAttribute = toolTableSet.getAttribute("AdapterClass");
			Attribute homeFolderAttribute = toolTableSet.getAttribute("HomeFolder");
			Attribute inputExtractorBaseBatchAttribute = toolTableSet.getAttribute("InputBaseBatch");
		    Attribute inputExtractorBatchAttribute = toolTableSet.getAttribute("InputBatch");
			
			for(int i = 0; i < toolTableSet.getValueCount(); i++) {
				int id = ((Double)idAttribute.getValueAt(i)).intValue();
				if(id == 2 || id == 4) {
					// Create extractor adapter for the modification of the
	    			// base script
	    			Class<?> adapter = Class.forName(adapterClassAttribute.getValueAt(i).toString());
	    			ExtractorInterface ead = (ExtractorInterface) adapter.newInstance();
	    			
	    			// Set the extractor properties
					Properties extractorProperties = new Properties();
					extractorProperties.setProperty("id", new Integer(new Double(idAttribute.getValueAt(i).toString()).intValue()).toString());
					extractorProperties.setProperty("extractorName", extractorNameAttribute.getValueAt(i).toString());
					extractorProperties.setProperty("extractorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator +"tools" + File.separator + homeFolderAttribute.getValueAt(i));
					extractorProperties.setProperty("inputExtractorBaseBatch", inputExtractorBaseBatchAttribute.getValueAt(i).toString());
					String script = inputExtractorBatchAttribute.getValueAt(i).toString();
					script = script.substring(0, script.lastIndexOf(".")) + "_attack_slopes" + script.substring(script.lastIndexOf("."));
					extractorProperties.setProperty("inputExtractorBatch", script);
					extractorProperties.setProperty("extractorFolderName", homeFolderAttribute.getValueAt(i).toString());
					((AmuseTask) ead).configure(extractorProperties, null, null);
	
					// Save the extractor
					extractors.add(ead);
				}
			}
			
		} catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			throw new NodeException(e.getLocalizedMessage());
		}
	}

}
