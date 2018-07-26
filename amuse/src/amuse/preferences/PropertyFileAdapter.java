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
 * Creation date: 19.06.2009
 */
package amuse.preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;

/**
 * @author Clemens Waeltken
 * 
 */
public class PropertyFileAdapter {

    private Properties properties = new Properties();
    private File propertyFile;
    private static final String comment = "This file contains settings for Amuse.";

    public PropertyFileAdapter() throws FileNotFoundException, IOException {
        this(new File(KeysStringValue.AMUSE_PATH.getDefaultValue() + File.separator + "config" + File.separator + "amuse.properties"));
    }

    public PropertyFileAdapter(File f) throws FileNotFoundException,
            IOException {
        propertyFile = f;
        restoreFromFile(propertyFile);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void put(String key, String value) {
        properties.setProperty(key, value);
        saveToFile();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        boolean b = defaultValue;
        String valueStr = properties.getProperty(key);
        if (valueStr == null) {
            return b;
        } else if (valueStr.equalsIgnoreCase("true")) {
            b = true;
        } else if (valueStr.equalsIgnoreCase("false")) {
            b = false;
        }
        return b;
    }

    public void putBoolean(String key, boolean value) {
        properties.setProperty(key, Boolean.toString(value));
        saveToFile();
    }

    public int getInt(String key, int defaultValue) {
        int i = defaultValue;
        try {
            i = new Integer(properties.getProperty(key));
        } catch (NumberFormatException e) {
        }
        return i;
    }

    public void putInt(String key, int value) {
        properties.setProperty(key, Integer.toString(value));
        saveToFile();
    }

    public void saveToFile() {
        try {
            saveToFile(propertyFile);
        } catch (IOException ex) {
            AmuseLogger.write(this.getClass().toString(), Level.ERROR, "Unable to update preferences.arff!");
        }
    }

    public void saveToFile(File f) throws IOException {
        if (!f.exists()) {
            if (!f.createNewFile()) {
                throw new IOException("Unable to create new Property file!");
            }
        }
        FileOutputStream fos = new FileOutputStream(f);
        store(fos, comment);
    }

    private synchronized void store(OutputStream out, String comments)
            throws IOException {
        BufferedWriter awriter;
        awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
        if (comments != null) {
            writeln(awriter, "#" + comments);
        }
        writeln(awriter, "#" + new Date().toString());
        ArrayList<String> keys = new ArrayList<String>();
        for (Enumeration<Object> e = properties.keys(); e.hasMoreElements();) {
            keys.add((String) e.nextElement());
        }
        Collections.sort(keys);
        for (String key : keys) {
            String val = (String) properties.get(key);
            key = saveConvert(key, true);

            /*
             * No need to escape embedded and trailing spaces for value, hence
             * pass false to flag.
             */
            val = saveConvert(val, false);
            writeln(awriter, "# " + AmusePreferences.getCommentFor(key));
            writeln(awriter, key + "=" + val);
            writeln(awriter, "");
        }
        awriter.flush();
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes special characters
     * with a preceding slash
     */
    private String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;
                default:
                    if ((aChar < 0x0020) || (aChar > 0x007e)) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     *
     * @param nibble
     *            the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }
    /** A table of hex digits */
    private static final char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6',
        '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static void writeln(BufferedWriter bw, String s) throws IOException {
        bw.write(s);
        bw.newLine();
    }

    public void restoreFromFile(File f) throws IOException {
            FileInputStream fis = new FileInputStream(f);
            properties.load(fis);
            fis.close();
            saveToFile();
    }

    /**
     *
     */
    public void deleteSettings() {
        properties.clear();
        saveToFile();
    }
}
