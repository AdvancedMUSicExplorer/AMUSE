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
 * Creation date: 13.03.2008
 */
package amuse.interfaces.nodes;

/**
 * All node schedulers should implement this interface
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public interface NodeSchedulerInterface {

	/**
	 * Proceeds the task which parameters are described in TaskConfiguration from
	 * %NODEHOME%/task.ser file
	 * @param args Task parameters: Optimizer home folder and job id
	 */
	public void proceedTask(String[] args);
	
	/**
	 * Proceeds the task
	 * @param homeFolder Home folder of the corresponding node
	 * @param jobId Amuse job id
	 * @param args taskConfiguration Task configuration
	 */
	public void proceedTask(String homeFolder, long jobId, TaskConfiguration taskConfiguration);
}
