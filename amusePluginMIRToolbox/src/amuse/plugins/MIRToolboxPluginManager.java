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
 *  Creation date: 12.05.2010
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
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.FileOperations;

/**
 * This class runs specific routines during MIR Toolbox installation procedure
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class MIRToolboxPluginManager implements PluginInstallerInterface {

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
		
		// Set the path to AMUSE folder in feature extraction base script
		File baseScript = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + "/tools/MIRToolbox/MIRToolboxBase.xml");
		File updatedBaseScript = new File(AmusePreferences.get(KeysStringValue.AMUSE_PATH) + "/tools/MIRToolbox/MIRToolboxBaseUpdated.xml");
		
		try {
			BufferedReader baseScriptReader = new BufferedReader(new FileReader(baseScript));
			DataOutputStream values_writer = new DataOutputStream(new FileOutputStream(updatedBaseScript)); 
			
			String line = new String();
			while ((line = baseScriptReader.readLine()) != null) {
				if(line.startsWith(new String("addpath('%AMUSEHOME%"))) {
					AmuseLogger.write(this.getClass().getName(), Level.INFO, "Setting the AMUSE path for MIR Toolbox folders in base extractor script..");
					values_writer.writeBytes(line.replace("%AMUSEHOME%", AmusePreferences.get(KeysStringValue.AMUSE_PATH)) + System.getProperty("line.separator"));
				} else {
					values_writer.writeBytes(line + System.getProperty("line.separator"));
				}
			}
			
			baseScriptReader.close();
			values_writer.close();
			
			// Replace the base script with the updated file
			FileOperations.move(updatedBaseScript, baseScript);
			
		} catch(Exception e) {
			throw new PluginException("Could not set the path to AMUSE folder in feature extraction base script: " + e.getMessage());
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
