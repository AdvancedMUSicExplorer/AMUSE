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
import java.io.IOException;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExecutionMode;
import com.rapidminer.repository.Repository;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.local.LocalRepository;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;

/**
 * This class loads on demand libraries which are integrated in AMUSE 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class LibraryInitializer {
	
	public static final String RAPIDMINER_REPO_NAME = "RapidMinerRepoAmuse";
	public static final String REPOSITORY_PATH = AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + "RapidMiner9" + File.separator + "repository";
	
	private static boolean rapidMinerInitialized = false;
	private static Repository rapidMinerRepo;

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
				String pathToRapidMinerHome = AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + "RapidMiner9";
				pathToRapidMinerHome = pathToRapidMinerHome.replaceAll(File.separator + "+", File.separator); // Make sure there are no consecutive separators
				System.setProperty("rapidminer.home", pathToRapidMinerHome);
				System.setProperty("rapidminer.init.weka", "true");
				System.setProperty("rapidminer.init.plugins", "true");
				System.setProperty("rapidminer.init.jdbc.lib", "false");
				System.setProperty("rapidminer.init.jdbc.classpath", "false");
				RapidMiner.setExecutionMode(ExecutionMode.COMMAND_LINE);
				
				// DEV_INFO: To load all operators, comment the next line
//				System.setProperty(RapidMiner.PROPERTY_RAPIDMINER_INIT_OPERATORS,"/OperatorsCoreReduced.xml");
				System.setProperty(RapidMiner.PROPERTY_RAPIDMINER_INIT_OPERATORS,"/OperatorsCore.xml");
				

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
				
				// Load the RapidMiner Repository
				loadRapidMinerRepo();
			} catch(Exception e) {
				throw e;
			}
			rapidMinerInitialized = true;
		}
		clearRepositoryFolder();
	}
	
	/**
	 * Loads the RapidMiner repository.
	 * Creates it if it does not exist yet.
	 * @throws RepositoryException
	 */
	private static void loadRapidMinerRepo() throws RepositoryException {
		// if the repository already exists remove it
		try {
			rapidMinerRepo = RepositoryManager.getInstance(null).getRepository(RAPIDMINER_REPO_NAME);
			RepositoryManager.getInstance(null).removeRepository(rapidMinerRepo);
		} catch (RepositoryException e) {}
		// create a new repository
		rapidMinerRepo = new LocalRepository(RAPIDMINER_REPO_NAME, new File(REPOSITORY_PATH));
		RepositoryManager.getInstance(null).addRepository(rapidMinerRepo);
	}
	
	/**
	 * Clears the repository folder
	 * @throws IOException 
	 */
	private static void clearRepositoryFolder() throws IOException {
		File[] files = new File(REPOSITORY_PATH).listFiles();
		if(files == null) {
			return;
		}
		boolean successful = true;
		for(File file : files) {
			successful = successful & FileOperations.delete(file, true);
		}
		if(!successful) {
			throw new IOException("Could not clear RapidMiner repository folder!");
		}
	}
}
