/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2020 by code authors
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
 * Creation date: 15.02.2020
 */
package amuse.data.annotation;

/**
 * This class describes the partitions from the given song
 * 
 * @author Igor Vatolkin
 * @version $Id $
 */
public class EventsDescription {
	
	/** Path to the song */
	final String pathToMusicSong;
	
	/** Id of the song */
	final int songId;
	
	/** Event positions in ms */
	final Double[] eventMs;
	
    /**
	 * Constructor
	 * @param pathToMusicSong Path to the song
	 * @param eventMs Positions of the events in ms
	 */
	public EventsDescription(String pathToMusicSong, int songId, Double[] eventMs) {
		this.pathToMusicSong = pathToMusicSong;
		this.songId = songId;
		this.eventMs = eventMs;
	}

	/**
	 * @return the pathToMusicSong
	 */
	public String getPathToMusicSong() {
		return pathToMusicSong;
	}

	/**
	 * @return the eventMs
	 */
	public Double[] getEventMs() {
		return eventMs;
	}

	/**
	 * @return the songId
	 */
	public int getSongId() {
		return songId;
	}

}
