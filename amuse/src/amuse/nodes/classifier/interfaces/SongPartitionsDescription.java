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
 * This class describes the partitions from the given song
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class SongPartitionsDescription {
	
	/** Path to the song */
	final String pathToMusicSong;
	
	/** Id of the song */
	final int songId;
	
	/** Starts of the partitions in ms */
	final Double[] startMs;
	
	/** Ends of the partitions in ms */
	final Double[] endMs;
	
    /**
	 * Constructor
	 * @param pathToMusicSong Path to the song
	 * @param startMs Starts of the partitions in ms
	 * @param endMs Ends of the partitions in ms
	 */
	public SongPartitionsDescription(String pathToMusicSong, int songId, Double[] startMs, Double[] endMs) {
		if(startMs.length != endMs.length) {
			throw new RuntimeException("Could not instantiate SongPartitionsDescription: the number of " +
					"partition starts (" + startMs.length + ") is not equal to the number of partition ends (" + 
					endMs.length + ")");
		}
		this.pathToMusicSong = pathToMusicSong;
		this.songId = songId;
		this.startMs = startMs;
		this.endMs = endMs;
	}

	/**
	 * @return the pathToMusicSong
	 */
	public String getPathToMusicSong() {
		return pathToMusicSong;
	}

	/**
	 * @return the startMs
	 */
	public Double[] getStartMs() {
		return startMs;
	}

	/**
	 * @return the endMs
	 */
	public Double[] getEndMs() {
		return endMs;
	}

	/**
	 * @return the songId
	 */
	public int getSongId() {
		return songId;
	}

}
