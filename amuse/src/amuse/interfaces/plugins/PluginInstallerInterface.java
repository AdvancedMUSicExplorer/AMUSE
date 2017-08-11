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
 *  Creation date: 01.12.2009
 */ 
package amuse.interfaces.plugins;

import java.util.Properties;

/**
 * If any plugin-specific installation/deinstallation routines are required, the pluginManager.jar main class 
 * must implement this interface
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public interface PluginInstallerInterface {

	/**
	 * Runs specific installation routines, e.g. setting the paths etc.
	 * @param properties Properties of this plugin 
	 * @throws PluginException
	 */
	public void runInstallationRoutines(Properties properties) throws PluginException;
	
	/**
	 * Runs specific deinstallation routines
	 * @param pathToPluginFolder Path to plugin information folder ($AMUSEHOME$/config/plugininfos/$PLUGIN_ID$
	 * @throws PluginException
	 */
	public void runDeinstallationRoutines(String pathToPluginFolder) throws PluginException;
	
}
