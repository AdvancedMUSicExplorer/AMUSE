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
 * Creation date: 13.03.2008
 */
package amuse.interfaces.nodes;




/**
 * Node event describes the status of corresponding task.
 * 
 * @author Igor Vatolkin
 * @version $Id: NodeEvent.java 1054 2010-07-01 14:02:37Z vatolkin $
 */
public class NodeEvent {
	
	/** Indicates that the extraction of features has been successfully finished */
	public static int EXTRACTION_COMPLETED = 0;
	
	/** Indicates that the extraction of features has been failed */
	public static int EXTRACTION_FAILED = 1;

	/** Indicates that the processing of features has been successfully finished */
	public static int PROCESSING_COMPLETED = 2;
	
	/** Indicates that the processing of features has been failed */
	public static int PROCESSING_FAILED = 3;
	
	/** Indicates that the training of classification model has been successfully finished */
	public static int TRAINING_COMPLETED = 4;
	
	/** Indicates that the training of classification model  has been failed */
	public static int TRAINING_FAILED = 5;

	/** Indicates that the classification has been successfully finished */
	public static int CLASSIFICATION_COMPLETED = 6;
	
	/** Indicates that the classification has been failed */
	public static int CLASSIFICATION_FAILED = 7;

	/** Indicates that the classifier validation has been successfully finished */
	public static int VALIDATION_COMPLETED = 8;
	
	/** Indicates that the classifier validation has been failed */
	public static int VALIDATION_FAILED = 9;
	
	/** Indicates that the optimization has been successfully finished */
	public static int OPTIMIZATION_COMPLETED = 10;
	
	/** Indicates that the optimization has been failed */
	public static int OPTIMIZATION_FAILED = 11;

	/** The type of this event */
	private int eventType;
	
	/** The source of this event */
	private NodeEventSource source;
	
	/**
	 * Constructor sets the event type and source
	 * @param eventType The event type
	 * @param source The event source
	 */
	public NodeEvent(int eventType, NodeEventSource source) {
		this.eventType = eventType;
		this.source = source;
	}
	
	/**
	 * Returns the event source
	 * @return The event source
	 */
	public NodeEventSource getEventSource() {
		return this.source;
	}
	
	/**
	 * Returns the event type
	 * @return The event type
	 */
	public int getEventType() {
		return this.eventType;
	}
}

