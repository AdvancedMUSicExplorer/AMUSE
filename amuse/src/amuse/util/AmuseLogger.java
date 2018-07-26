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
 * Creation date: 11.01.2007
 */
package amuse.util;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;

/**
 * AmuseLogger takes care of log messages
 * TODO add support for multiple listeners.
 * @author Igor Vatolkin
 * @version $Id$
 */
public class AmuseLogger {
	
	private static ArrayList<LoggerListener> listeners = new ArrayList<LoggerListener>();
	
	/** The logger instance */
	private static AmuseLogger logger = new AmuseLogger();
	
	/**
	 * Private constructor 
	 */
	private AmuseLogger() {
		boolean usingEnv = true;
		if(System.getenv("AMUSEHOME") == null){
			usingEnv = false;
		}
		PropertyConfigurator.configure(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "log4j.properties");
		if(usingEnv){
           	info(AmusePreferences.class.toString(), "Using the environment variable 'AMUSEHOME'");
		}
		else{
			info(AmusePreferences.class.toString(), "Environment variable 'AMUSEHOME' was not found, setting the home to " + AmusePreferences.get(KeysStringValue.AMUSE_PATH));
		}

	}
	
	/**
	 * Logs a message
	 * @param category Category of a message, should be equal to the class name
	 * @param level Priority level of a message, should be equal to one of the following priorities: 
	 * 	<ul>
	 *  	<li>DEBUG: Important only during the debugging and testing of Amuse
	 *  	<li>INFO: Standard information output, e.g. about the initialization of components
	 *  	<li>WARN: Warning indicates the occurrence of some error, which does not have any influence on the results of Amuse work
	 *  	<li>ERROR: Error indicates the occurence of some error, which has influence on the results if Amuse work
	 *  	<li>FATAL: Fatal error indicates the occurence of critical error which influences the exit of Amuse
	 *  </ul>
	 * @param message Message string
	 */
	public static void write(String category, Level level, String message) {
		notifyListeners(category, level, message);
		switch(level.toInt()) {
			case(Level.DEBUG_INT): logger.debug(category, message); break;	
			case(Level.INFO_INT): logger.info(category, message); break;
			case(Level.WARN_INT): logger.warn(category, message); break;
			case(Level.ERROR_INT): logger.error(category, message); break;
			case(Level.FATAL_INT): logger.fatal(category, message); break;
			default: Logger.getRootLogger().error("No Logger for this level available!"); break;
		}
	}
	
	/**
	 * Logs a debug message
	 * @param category Category of a message, should be equal to the class name
	 * @param message Message string
	 */
	private void debug(String category, String message) {
		Logger.getLogger(category).debug(message); 
	}
	
	/**
	 * Logs an info message
	 * @param category Category of a message, should be equal to the class name
	 * @param message Message string
	 */
	private void info(String category, String message) {
		Logger.getLogger(category).info(message); 
	}

	/**
	 * Logs a warn message
	 * @param category Category of a message, should be equal to the class name
	 * @param message Message string
	 */
	private void warn(String category, String message) {
		Logger.getLogger(category).warn(message); 
	}
	
	/**
	 * Logs an error message
	 * @param category Category of a message, should be equal to the class name
	 * @param message Message string
	 */
	private void error(String category, String message) {
		Logger.getLogger(category).error(message); 
	}
	
	/**
	 * Logs a fatal message
	 * @param category Category of a message, should be equal to the class name
	 * @param message Message string
	 */
	private void fatal(String category, String message) {
		Logger.getLogger(category).fatal(message); 
	}
	
	public static void addListener(LoggerListener listener){
		listeners.add(listener);
	}
	
	public static void removeListener(LoggerListener listener){
		listeners.remove(listener);
	}
	
	private static void notifyListeners(String category, Level level, String message) {
		for (LoggerListener li : listeners) {
			li.receiveLoggerEvent(category, level, message);
		}
	}
}
