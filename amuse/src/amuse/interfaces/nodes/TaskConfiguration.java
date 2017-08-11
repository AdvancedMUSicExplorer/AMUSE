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
 * Creation date: 11.04.2008
 */
package amuse.interfaces.nodes;

import java.io.Serializable;


/**
 * All node configurations (defining exactly a Amuse task which should be proceeded) should 
 * implement this interface
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public abstract class TaskConfiguration implements Serializable {

    /** For Serializable interface */
    private static final long serialVersionUID = -3440600170290868055L;

    /** Returns the type of this configuration */
    abstract public String getType();

    /** Returns the description of this configuration */
    abstract public String getDescription();
}
