/**
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 *
 * Copyright 2006-2021 by code authors
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
 * Creation date: 15.02.2019
 */
package amuse.data.datasets;

import java.io.File;
import java.io.IOException;

import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.scheduler.tools.ToolConfiguration;

/**
 * This class represents a list of tool tasks as used in AMUSE. Serialisation to ARFF is supported.
 * @author Igor Vatolkin
 * @version $Id: $
 */
public class ToolConfigSet extends AbstractArffExperimentSet {

	/**	For Serializable interface */
	private static final long serialVersionUID = -2920038480627315614L;
	
	// Strings which describe ARFF attributes
	private static final String strToolClassAttribute = "ToolClass";
	private static final String strToolFolderAttribute = "ToolFolder";
	private static final String strToolObjectAttribute = "ToolObject";
    private static final String strToolConfigurationAttribute = "ToolConfiguration";
    private static final String strDestinationFolderAttribute = "DestinationFolder";
    
	// ARFF attributes
	private final StringAttribute toolClassAttribute;
	private final StringAttribute toolFolderAttribute;
	private final StringAttribute toolObjectAttribute;
    private final StringAttribute toolConfigurationAttribute;
    private final StringAttribute destinationFolderAttribute;
    
    /**
     * Creates a new ToolConfigSet from a file. Validates if the given file contains a ToolConfigSet.
     * @param file The file to load form.
     * @throws java.io.IOException Thrown whenever given file does not represent a valid ToolConfigSet.
     */
    public ToolConfigSet(File file) throws IOException {
        super(file);
        // Check preconditions:
        checkStringAttribute(strToolClassAttribute);
        checkStringAttribute(strToolFolderAttribute);
        checkStringAttribute(strToolObjectAttribute);
        checkStringAttribute(strToolConfigurationAttribute);
        checkStringAttribute(strDestinationFolderAttribute);
        toolClassAttribute = (StringAttribute) getAttribute(strToolClassAttribute);
        toolFolderAttribute = (StringAttribute) getAttribute(strToolFolderAttribute);
        toolObjectAttribute = (StringAttribute) getAttribute(strToolObjectAttribute);
        toolConfigurationAttribute = (StringAttribute) getAttribute(strToolConfigurationAttribute);
        destinationFolderAttribute = (StringAttribute) getAttribute(strDestinationFolderAttribute);
    }

    public ToolConfigSet(DataSetAbstract dataSet) {
        super(dataSet.getName());
        // Check preconditions:
        checkStringAttribute(strToolClassAttribute);
        checkStringAttribute(strToolFolderAttribute);
        checkStringAttribute(strToolObjectAttribute);
        checkStringAttribute(strToolConfigurationAttribute);
        checkStringAttribute(strDestinationFolderAttribute);
        toolClassAttribute = (StringAttribute) getAttribute(strToolClassAttribute);
        toolFolderAttribute = (StringAttribute) getAttribute(strToolFolderAttribute);
        toolObjectAttribute = (StringAttribute) getAttribute(strToolObjectAttribute);
        toolConfigurationAttribute = (StringAttribute) getAttribute(strToolConfigurationAttribute);
        destinationFolderAttribute = (StringAttribute) getAttribute(strDestinationFolderAttribute);
        addAttribute(toolClassAttribute);
        addAttribute(toolFolderAttribute);
        addAttribute(toolConfigurationAttribute);
        addAttribute(destinationFolderAttribute);
    }

    @Override
    public TaskConfiguration[] getTaskConfiguration() {
    	try {
    		return ToolConfiguration.loadConfigurationsFromDataSet(this);
    	} catch (IOException ex) {
    		throw new RuntimeException(ex);
    	}
    }

	/**
	 * @return the toolClassAttribute
	 */
	public StringAttribute getToolClassAttribute() {
		return toolClassAttribute;
	}
	
	/**
	 * @return the toolFolderAttribute
	 */
	public StringAttribute getToolFolderAttribute() {
		return toolFolderAttribute;
	}
	
	/**
	 * @return the toolObjectAttribute
	 */
	public StringAttribute getToolObjectAttribute() {
		return toolObjectAttribute;
	}

	/**
	 * @return the toolConfigurationAttribute
	 */
	public StringAttribute getToolConfigurationAttribute() {
		return toolConfigurationAttribute;
	}

	/**
	 * @return the destinationFolderAttribute
	 */
	public StringAttribute getDestinationFolderAttribute() {
		return destinationFolderAttribute;
	}

}
