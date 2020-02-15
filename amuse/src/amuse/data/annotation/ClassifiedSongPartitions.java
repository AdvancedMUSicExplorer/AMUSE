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
package amuse.data.annotation;

/**
 * This class saves the results of a classification task
 * 
 * @author Igor Vatolkin
 * @version $Id: ClassifiedSongPartitions.java 197 2017-08-11 12:15:34Z frederik-h $
 */
public class ClassifiedSongPartitions extends SongPartitionsDescription {
	
	/** labels that were classified **/
	final String[] labels;
	
	/** Corresponding relationships for the partitions
	 * 	First dimension represents the partitions, second dimension the categories.
	 * */
	final Double[][] relationships;
    
	/**
	 * Constructor
	 * @param pathToMusicSong Music song which was classified
	 * @param startMs Starts of the partitions in ms
	 * @param endMs Ends of the partitions in ms
	 * @param labels names of the categories
	 * @param relationships Corresponding relationships for the partitions
	 */
	public ClassifiedSongPartitions(String pathToMusicSong, int songId, Double[] startMs, Double[] endMs, String[] labels,
			Double[][] relationships) {
		super(pathToMusicSong, songId, startMs, endMs);
		if(relationships.length != startMs.length) {
			throw new RuntimeException("Could not instantiate ClassifiedSongPartitionsDescription: " +
					"the number of partition starts (" + startMs.length + ") is not equal to the number " +
					"of relationships (" + relationships.length + ")");
		}
		this.relationships = relationships;
		this.labels = labels;
	}

	/**
	 * @return the relationships
	 */
	public Double[][] getRelationships() {
		return relationships;
	}
	
	public double getMeanRelationship(int category) {
		double sum = 0d;
		for(int i=0;i<relationships.length;i++) {
			double d = relationships[i][category];
			sum += d;
		}
		sum /= relationships.length;
		return sum;
	}
	
	/**
	 * @return the label
	 */
	public String[] getLabels() {
		return labels;
	}
}
