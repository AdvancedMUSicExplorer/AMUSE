package amuse.nodes.processor.methods.converters;

import java.util.ArrayList;

import org.apache.log4j.Level;

import amuse.data.Feature;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.processor.ProcessorNodeScheduler;
import amuse.nodes.processor.interfaces.MatrixToVectorConverterInterface;
import amuse.util.AmuseLogger;

public class RawFeaturesConverter extends AmuseTask implements MatrixToVectorConverterInterface{

	@Override
	public void initialize() throws NodeException {
		// Do nothing
		
	}

	@Override
	public void setParameters(String parameterString) throws NodeException {
		// Do nothing
	}

	@Override
	public ArrayList<Feature> runConversion(ArrayList<Feature> features, Integer ms, Integer overlap,
			String nameOfProcessorModel) throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the raw feature conversion...");
		
		int windowSize = ((ProcessorNodeScheduler)this.correspondingScheduler).getMinimalFrameSize();
				
		// Single features used as classifier input vector
		ArrayList<Feature> endFeatures = new ArrayList<Feature>();
		
		try {
			double partitionSizeInWindows;
			double overlapSizeInWindows;
			int numberOfAllPartitions;
			
			int sampleRate = features.get(0).getSampleRate();
			
			// Aggregate the data over the complete song or build partitions?
			if(ms == -1) {
				
				// In 1st case we have only one "partition" which covers the complete song
				partitionSizeInWindows = features.get(0).getWindows().get(features.get(0).getWindows().size()-1);
				overlapSizeInWindows = partitionSizeInWindows;
				numberOfAllPartitions = 1;
			} else {
				
				// In 2nd case we can calculate the number of windows which belong to each partition
				partitionSizeInWindows = (Double)(sampleRate*(ms/1000d)/windowSize);
				overlapSizeInWindows = (Double)(sampleRate*((ms-overlap)/1000d)/windowSize);
				
				// FIXME evtl. check! Calculates the last used time window and the number of maximum available partitions from it
				double numberOfAllPartitionsD = ((features.get(0).getWindows().get(features.get(0).getWindows().size()-1)) - partitionSizeInWindows)/(partitionSizeInWindows - overlapSizeInWindows)+1;
				numberOfAllPartitions = new Double(Math.floor(numberOfAllPartitionsD)).intValue();
			}
			
			// If the partition size is greater than music song length..
			if(numberOfAllPartitions == 0) {
				throw new NodeException("Partition size too large");
			}
			
			for(int i=0; i<partitionSizeInWindows;i++) {
				for(int j=0; j<features.size(); j++) {
					int dimensions = features.get(j).getDimension();
					for(int k=0; k<dimensions;k++) {
						Feature newFeature = new Feature(-1);
						newFeature.setHistory(features.get(j).getHistory());
						newFeature.getHistory().add("window"+(i+1)+"_dim"+(k+1));
						endFeatures.add(newFeature);
					}
				}
			}
			
			int currentWindow = 0;
			// Go through all partitions
			for(int numberOfCurrentPartition=0;numberOfCurrentPartition<numberOfAllPartitions;numberOfCurrentPartition++) {
				
				// Calculate the start (inclusive) and end (exclusive) windows for the current partition
				Double partitionStart = Math.floor(new Double(partitionSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentPartition));
				Double partitionEnd = Math.floor((new Double(partitionSizeInWindows - overlapSizeInWindows)*new Double(numberOfCurrentPartition)+partitionSizeInWindows));
				
				// Increment the number of current time window if the lower partition boundary is not achieved
				for(int k=currentWindow;k<features.get(0).getWindows().size();k++) {
					if(features.get(0).getWindows().get(k) >= partitionStart) {
						currentWindow = k;
						break;
					}
				}
				
				// If no features are available for the current partition, go to the next partition
				if(features.get(0).getWindows().get(currentWindow) > partitionEnd || features.get(0).getWindows().get(currentWindow) < partitionStart) {
					continue;
				}
				
				int currentEndFeature = 0;
				for(int window= partitionStart.intValue(); window<partitionEnd;window++) {
					// Increment the number of current time window if the current window is not reached
					for(int k=currentWindow;k<features.get(0).getWindows().size();k++) {
						if(features.get(0).getWindows().get(k) >= window) {
							currentWindow = k;
							break;
						}
					}
					
					// if the window could not be reached we have to fill the remaining values with NaN values
					if(features.get(0).getWindows().get(currentWindow) < window) {
						for(Feature feature : features) {
							int dimensions = feature.getDimension();
							for(int dim=0; dim<dimensions; dim++) {
								Double[] val = {Double.NaN};
								endFeatures.get(currentEndFeature).getWindows().add(new Double(window));
								endFeatures.get(currentEndFeature).getValues().add(val);
								currentEndFeature++;
							}
						}
					} else {
						// fill the end features with the correct values
						for(Feature feature : features) {
							int dimensions = feature.getDimension();
							Double[] vals = feature.getValues().get(currentWindow);
							for(int dim=0; dim<dimensions; dim++) {
								Double[] val = {vals[dim]};
								endFeatures.get(currentEndFeature).getWindows().add(new Double(window));
								endFeatures.get(currentEndFeature).getValues().add(val);
								currentEndFeature++;
							}
						}
					}
				}
			}
			
		} catch(Exception e) {
			throw new NodeException("Problem occured during feature conversion: " + e.getMessage());
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "...conversion succeeded");
		return endFeatures;
	}

}
