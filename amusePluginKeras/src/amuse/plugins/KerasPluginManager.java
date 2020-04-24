/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2019 by code authors
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
 *  Creation date: 23.04.2020
 */ 
package amuse.plugins;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

import org.apache.log4j.Level;

import amuse.interfaces.plugins.PluginException;
import amuse.interfaces.plugins.PluginInstallerInterface;
import amuse.util.AmuseLogger;
import amuse.util.FileOperations;

/**
 * This class runs specific routines during Librosa installation procedure
 * For further details of Librosa see <a href="https://github.com/librosa/librosa/blob/master/README.md</a>
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class KerasPluginManager implements PluginInstallerInterface {

	/**
	 * Does nothing and is required to find this class as a main class in pluginManager.jar
	 */
	public static void main(String[] args) {

	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.plugins.PluginInstallerInterface#runInstallationRoutines(java.util.Properties)
	 */
	public void runInstallationRoutines(Properties properties) throws PluginException {
		// Set the path to AMUSE folder in classifierAlgorithmTable
		File classifierTable = new File(System.getenv("AMUSEHOME") + File.separator + "config" + File.separator + "classifierAlgorithmTable.arff");
		File updatedClassifierTable = new File(System.getenv("AMUSEHOME") + File.separator + "config" + File.separator + "updatedClassifierAlgorithmTable.arff");
		
		try {
			BufferedReader classifierTableReader = new BufferedReader(new FileReader(classifierTable));
			DataOutputStream values_writer = new DataOutputStream(new FileOutputStream(updatedClassifierTable)); 
			
			String line = new String();
			while ((line = classifierTableReader.readLine()) != null) {
				if(line.startsWith(new String("7"))) {
					AmuseLogger.write(this.getClass().getName(), Level.INFO, "Setting the AMUSE path for Keras in classifier algorithm table..");
					values_writer.writeBytes(line.replace("%AMUSEHOME%", System.getenv("AMUSEHOME")) + System.getProperty("line.separator"));
				} else {
					values_writer.writeBytes(line + System.getProperty("line.separator"));
				}
			}
			
			classifierTableReader.close();
			values_writer.close();
			
			// Replace the base script with the updated file
			FileOperations.move(updatedClassifierTable, classifierTable);
			
		} catch(Exception e) {
			throw new PluginException("Could not set the path to AMUSE folder in classifier algorithm table: " + e.getMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.plugins.PluginInstallerInterface#runDeinstallationRoutines(java.lang.String)
	 */
	public void runDeinstallationRoutines(String arg0) throws PluginException {
		// No specific deinstallation routines are required
	}

}
