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
 *  Creation date: 12.07.2019
 */ 
package amuse.plugins;

import java.util.Properties;

import amuse.interfaces.plugins.PluginException;
import amuse.interfaces.plugins.PluginInstallerInterface;

/**
 * This class runs specific routines during Librosa installation procedure
 * For further details of Librosa see <a href="https://github.com/librosa/librosa/blob/master/README.md</a>
 * 
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class LibrosaPluginManager implements PluginInstallerInterface {

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
		// Currently nothing is required
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.plugins.PluginInstallerInterface#runDeinstallationRoutines(java.lang.String)
	 */
	public void runDeinstallationRoutines(String arg0) throws PluginException {
		// No specific deinstallation routines are required
	}

}
