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
 * Creation date: 26.05.2010
 */
package amuse.scheduler.pluginmanagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Level;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.interfaces.scheduler.SchedulerException;
import amuse.util.AmuseLogger;
import amuse.util.FileOperations;
import ca.cgjennings.jvm.JarLoader;

/**
 * PluginLoader loads plugin classes to CLASSPATH
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class PluginLoader {
	
	/**
	 * Adds the classes from all JARs in AMUSE plugin folder to CLASSPATH
	 * @throws SchedulerException
	 */
	public static void loadPlugins(File pluginDir) throws SchedulerException {
		if(pluginDir.exists()) {
			// file that stores path to the jar files that have to be deleted
			File pluginRemover = new File (AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "config" + File.separator + "pluginRemover.arff");
			// if file exists, proceed to deleting jar files
			if (pluginRemover.exists()) {
				boolean isRemoved = false;
			try (BufferedReader reader = new BufferedReader(new FileReader(pluginRemover))) {
	            String line;
	            // Skip the first line
	            reader.readLine();
	            // Read the rest of the lines
	            while ((line = reader.readLine()) != null) {
	            	File jarToDelete = new File(line);
	            	isRemoved= FileOperations.delete(jarToDelete, Level.INFO);
	            	if (isRemoved)
	            		AmuseLogger.write(PluginLoader.class.getName(), Level.INFO, "Plugin jar "  + jarToDelete.getName() +
									" deleted");
	            	else
	            		AmuseLogger.write(PluginRemover.class.getName(),Level.ERROR,"Could not remove the plugin jar!");
	            }
	            
	           
	        } catch (Exception e) {
	        	throw new SchedulerException(e.getMessage());
	        }
			// deleted plugin remover file, since it is not needed anymore
			pluginRemover.delete();
			}
			
			if(pluginDir.listFiles().length != 0) {
				File[] plugins = pluginDir.listFiles();
				for(File currentPlugin : plugins) {
					
					// Load the classes of all JARs found in plugin folder
					if(currentPlugin.getAbsoluteFile().toString().toLowerCase().endsWith(".jar")) {
						try {
							File jar = new File(currentPlugin.getAbsolutePath());
							
							JarLoader.addToClassPath(jar);
							
							AmuseLogger.write(PluginLoader.class.getName(), Level.INFO, "Plugin classes from " + currentPlugin.getName() + 
									" loaded");
						} catch (Exception e) {
							throw new SchedulerException(e.getMessage());
						} 
					}
				}
			}
		}
	}
}
