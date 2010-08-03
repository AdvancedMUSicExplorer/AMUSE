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
 * Creation date: 10.10.2007
 */
package amuse.util;

import java.io.IOException;

import amuse.interfaces.nodes.NodeException;

/**
 * Adapter for external tools
 * 
 * @author Igor Vatolkin
 * @version $Id: ExternalToolAdapter.java 1251 2010-08-03 13:56:39Z vatolkin $
 */
public class ExternalToolAdapter {

	/**
	 * Runs the batch script of an external tool
	 * @param toolStartScript Command line script for starting of the tool
	 * @param batchFile Tool batch script
	 * @throws NodeException
	 */
	public static void runBatch(String toolStartScript, String batchFile) throws NodeException {
		try {
			// Start the external tool
			Process process = Runtime.getRuntime().exec("bash " + toolStartScript + " " + batchFile);
			process.waitFor();
			
			// DEBUG Show the runtime outputs
			/*String s = null ;
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			AmuseLogger.write(ExternalToolAdapter.class.getName(), Level.INFO,"\nHere is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) { AmuseLogger.write(ExternalToolAdapter.class.getName(), Level.INFO,s); }
			AmuseLogger.write(ExternalToolAdapter.class.getName(), Level.INFO,"\n+Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {	AmuseLogger.write(ExternalToolAdapter.class.getName(), Level.INFO,s); }*/
			
		} catch (IOException e) {
			throw new NodeException("Start of Yale failed: " + e.getMessage());
		} catch (InterruptedException e) {
			throw new NodeException("Start of Yale failed: " + e.getMessage());		
		}
	}
	
}