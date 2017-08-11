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
 * Creation date: 21.01.2010
 */
package amuse.nodes.optimizer.methods.es;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import amuse.interfaces.nodes.NodeException;

/**
 * Logs ES output messages
 *  
 * @author Igor Vatolkin
 * @version $Id$
 */
public class ESLogger {
	
	FileOutputStream values_to = null;
	DataOutputStream values_writer = null;
	String sep = null;
	
	/**
	 * Standard constructor
	 * @param logFile Path to log file
	 */
	public ESLogger(File logFile) throws NodeException {
		try {
			values_to = new FileOutputStream(logFile,true);
		} catch (FileNotFoundException e) {
			throw new NodeException("Could not log the results: " + e.getMessage());
		}
		values_writer = new DataOutputStream(values_to);
		sep = System.getProperty("line.separator");
	}
	
	/**
	 * Logs a string
	 * @param s String to log
	 */
	public void logString(String s) throws NodeException {
		try {
			values_writer.writeBytes(s + sep);
		} catch (IOException e) {
			throw new NodeException("Could not write to the logging file: " + e.getMessage());
		}
	}
	
	/**
	 * Closes the logging streams
	 */
	public void close() throws NodeException {
		try {
			values_to.close();
			values_writer.close();
		} catch (IOException e) {
			throw new NodeException("Could not close the logging file: " + e.getMessage());
		}
	}
	
}
