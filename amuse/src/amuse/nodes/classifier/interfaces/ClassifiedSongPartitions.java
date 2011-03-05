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
 * Creation date: 22.07.2009
 */
package amuse.nodes.classifier.interfaces;

/**
 * This class saves the results of a classification task
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class ClassifiedSongPartitions extends SongPartitionsDescription {
	
	/** Corresponding relationships for the partitions */
	final Double[] relationships; 
    
	/**
	 * Constructor
	 * @param pathToMusicSong Music song which was classified
	 * @param startMs Starts of the partitions in ms
	 * @param endMs Ends of the partitions in ms
	 * @param labels Assigned label/category
	 * @param relationships Corresponding relationships for the partitions
	 */
	public ClassifiedSongPartitions(String pathToMusicSong, int songId, Double[] startMs, Double[] endMs,
			Double[] relationships) {
		super(pathToMusicSong, songId, startMs, endMs);
		if(relationships.length != startMs.length) {
			throw new RuntimeException("Could not instantiate ClassifiedSongPartitionsDescription: " +
					"the number of partition starts (" + startMs.length + ") is not equal to the number " +
					"of relationships (" + relationships.length + ")");
		}
		this.relationships = relationships;
	}

	/**
	 * @return the relationships
	 */
	public Double[] getRelationships() {
		return relationships;
	}
	
	public double getMeanRelationship() {
		double sum = 0d;
		for(Double d : relationships) {
			sum += d;
		}
		sum /= relationships.length;
		return sum;
	}
}
