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
 * Creation date: 15.06.2010
 */
package amuse.util;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExecutionMode;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;

/**
 * This class loads on demand libraries which are integrated in AMUSE 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class LibraryInitializer {
	
	private static boolean rapidMinerInitialized = false;
	

	/**
	 * Initializes RapidMiner as library
	 * @deprecated use {@link #initializeRapidMiner()} instead.
	 */
	public static void initializeRapidMiner(String pathToXml) throws Exception{
		throw new Exception("Method is outdated.");
	}
	/**
	 * Initializes RapidMiner as library
	 */
	public static void initializeRapidMiner() throws Exception {
		if(!rapidMinerInitialized) {
			try {
				String pathToRapidMinerHome = AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + "RapidMiner5";
				pathToRapidMinerHome = pathToRapidMinerHome.replaceAll(File.separator + "+", File.separator); // Make sure there are no consecutive separators 
				System.setProperty("rapidminer.home", pathToRapidMinerHome);
				System.setProperty("rapidminer.init.weka", "true");
				System.setProperty("rapidminer.init.plugins", "false");
				System.setProperty("rapidminer.init.jdbc.lib", "false");
				System.setProperty("rapidminer.init.jdbc.classpath", "false");
				RapidMiner.setExecutionMode(ExecutionMode.COMMAND_LINE);
				
				// DEV_INFO: To load all operators, comment the next line
				System.setProperty(RapidMiner.PROPERTY_RAPIDMINER_INIT_OPERATORS,"/OperatorsCoreReduced.xml");
				

				// This log handler redirects messages to the AmuseLogger
				Handler logHandler = new Handler() {
					
					@Override
					public void publish(LogRecord record) {
						AmuseLogger.write("RapidMiner", org.apache.log4j.Level.DEBUG, record.getMessage());
					}
					
					@Override
					public void flush() {
					}
					
					@Override
					public void close() throws SecurityException {
					}
				};
				
				// Add the handler to Rapidminers logging system
				Logger rootLogger = LogManager.getLogManager().getLogger("");
				rootLogger.removeHandler(rootLogger.getHandlers()[0]);
				rootLogger.addHandler(logHandler);
				RapidMiner.init();
			} catch(Exception e) {
				throw e;
			}
			rapidMinerInitialized = true;
		}
	}
	
}
