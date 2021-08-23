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
 * This class describes the classification windows from the given track
 * 
 * @author Igor Vatolkin
 * @version $Id: ClassificationWindowsDescription.java 197 2017-08-11 12:15:34Z frederik-h $
 */
public class ClassificationWindowsDescription {
	
	/** Path to the track */
	final String pathToMusicTrack;
	
	/** Id of the track */
	final int trackId;
	
	/** Starts of the classification windows in ms */
	final Double[] startMs;
	
	/** Ends of the classification windows in ms */
	final Double[] endMs;
	
    /**
	 * Constructor
	 * @param pathToMusicTrack Path to the track
	 * @param startMs Starts of the classification windows in ms
	 * @param endMs Ends of the classification windows in ms
	 */
	public ClassificationWindowsDescription(String pathToMusicTrack, int trackId, Double[] startMs, Double[] endMs) {
		if(startMs.length != endMs.length) {
			throw new RuntimeException("Could not instantiate ClassificationWindowsDescription: the number of " +
					"classfication window starts (" + startMs.length + ") is not equal to the number of classification window ends (" + 
					endMs.length + ")");
		}
		this.pathToMusicTrack = pathToMusicTrack;
		this.trackId = trackId;
		this.startMs = startMs;
		this.endMs = endMs;
	}

	/**
	 * @return the pathToMusicTrack
	 */
	public String getPathToMusicTrack() {
		return pathToMusicTrack;
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
	 * @return the trackId
	 */
	public int getTrackId() {
		return trackId;
	}

}
