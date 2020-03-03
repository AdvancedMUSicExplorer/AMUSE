/**
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 *
 * Copyright 2006-2020 by code authors
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
 * Creation date: 18.07.2010
 */
package amuse.util;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class starts other processes
 *
 * @author waeltken
 * @version $Id$
 */
public class ExternalProcessBuilder {

    private ProcessBuilder pb;
    private OSType os;

    private ExternalProcessBuilder() {
        os = determineOS();
    }
    /**
     * Sets this process builder's operating system program and arguments.
     * @param command
     */
    public ExternalProcessBuilder(List<String> command) {
        this();
        pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
    }

    /**
     * Sets this process builder's operating system program and arguments.
     * @param command
     */
    public ExternalProcessBuilder(String... command) {
        this(Arrays.asList(command));
    }
    
    /**
     * Shows logs of the started process in AMUSE console
     */
    public void redirectOutputToAMUSE() {
    	pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    }

    public static ExternalProcessBuilder buildJavaProcess(List<String> javaProperties, List<String> classPath, List<String> command) {
		OSType os = determineOS();
		String delim = ":";
		if (os == OSType.Windows) {
		    delim = ";";
		}
		String classpaths = "";
		for (String jar:classPath) {
		    classpaths += jar + delim;
		}
		
		// Delete last delimiter
		classpaths = classpaths.substring(0, classpaths.length()-1);
		String java = AmusePreferences.get(KeysStringValue.JAVA_PATH);
		List<String> commandList = new ArrayList<String>();
		commandList.add(java);
		commandList.addAll(javaProperties);
		commandList.add("-classpath");
		commandList.add(classpaths);
		commandList.addAll(command);
		return new ExternalProcessBuilder(commandList);
    }

    private enum OSType {
        Windows,
        Mac,
        Unix
    };

    private static OSType determineOS() {
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows"))
            return OSType.Windows;
        if (osName.contains("Mac"))
            return OSType.Mac;
        return OSType.Unix;
    }

    /**
     * Returns this process builder's operating system program and arguments.
     * @return
     */
    public List<String> command() {
        return pb.command();
    }

    /**
     * Add environment variable to this process.
     * @param key
     * @param value
     */
    public void setEnv(String key, String value) {
        pb.environment().put(key, value);
    }

    /**
     * Sets this process builder's working directory.
     * @param dir
     */
    public void setWorkingDirectory(File dir) {
        pb.directory(dir);
    }

    /**
     * Starts a new process using the attributes of this process builder.
     * @return
     * @throws IOException
     */
    public Process start() throws IOException {
        return pb.start();
    }
}
