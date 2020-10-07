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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Level;

import amuse.interfaces.scheduler.SchedulerException;
import amuse.util.AmuseLogger;
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
