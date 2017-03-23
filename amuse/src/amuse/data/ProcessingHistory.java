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
 * Creation date: 01.07.2009
 */
package amuse.data;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Level;

import amuse.data.io.ArffDataSet;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * @author Clemens Waeltken
 *
 */
public class ProcessingHistory {

    private static File historyFile = new File(AmusePreferences.get(KeysStringValue.PROCESSED_FEATURE_DATABASE) + File.separator + "history.arff");
    private static ArffDataSet historySet;
    private static String historySetStr = "ProcessingHistory";
    private static String idStr = "Id";
    private static String dateStr = "Date";
    private static String parameterStr = "ProcessingParameterUsed";
    private static final int maxLineCount = 20;
    private static List<Double> idValues;
    private static List<String> parameterValues;
    private static List<String> dateValues;

    public ProcessingHistory() throws IOException {
        if (!historyFile.exists()) {
            createEmptyHistory();
        }
        loadHistoryFile();
    }

    /**
     * @throws IOException
     */
    private synchronized void loadHistoryFile() throws IOException {
    	try{
    		historySet = new ArffDataSet(historyFile);
    	}
    	catch(IOException e){
    		int result = JOptionPane.showConfirmDialog((Component) null, "The history file is either empty or corrupted. Create a new one?", "Create new history file", JOptionPane.YES_NO_OPTION);
    		if(result == JOptionPane.YES_OPTION){
    			createEmptyHistory();
    		}
    	}
    	List<String> attributeNames = historySet.getAttributeNames();
        if (attributeNames.contains(idStr) && historySet.getAttribute(idStr) instanceof NumericAttribute) {
            NumericAttribute idAttribute = (NumericAttribute) historySet.getAttribute(idStr);
            idValues = idAttribute.getValues();
        } else {
            AmuseLogger.write(ProcessingHistory.class.toString(), Level.FATAL, "Corrupted history file detected! Please delete file at: \"" + historyFile.getAbsolutePath() + "\"");
            System.exit(1);
        }
        if (attributeNames.contains(dateStr) && historySet.getAttribute(dateStr) instanceof StringAttribute) {
            StringAttribute dateAttribute = (StringAttribute) historySet.getAttribute(dateStr);
            dateValues = dateAttribute.getValues();
        } else {
            AmuseLogger.write(ProcessingHistory.class.toString(), Level.FATAL, "Corrupted history file detected! Please delete file at: \"" + historyFile.getAbsolutePath() + "\"");
            System.exit(1);
        }
        if (attributeNames.contains(parameterStr) && historySet.getAttribute(parameterStr) instanceof StringAttribute) {
            StringAttribute parameterAttribute = (StringAttribute) historySet.getAttribute(parameterStr);
            parameterValues = parameterAttribute.getValues();
        } else {
            AmuseLogger.write(ProcessingHistory.class.toString(), Level.FATAL, "Corrupted history file detected! Please delete file at: \"" + historyFile.getAbsolutePath() + "\"");
            System.exit(1);
        }
    }

    /**
     * @throws IOException
     */
    private synchronized void createEmptyHistory() throws IOException {
        AmuseLogger.write(ProcessingHistory.class.toString(), Level.INFO, "No Processing history file found! Creating empty history file...");
        historySet = new ArffDataSet(historySetStr);
        historySet.addAttribute(new NumericAttribute(idStr, new ArrayList<Double>()));
        historySet.addAttribute(new StringAttribute(dateStr, new ArrayList<String>()));
        historySet.addAttribute(new StringAttribute(parameterStr, new ArrayList<String>()));
        historySet.saveToArffFile(historyFile);
    }

    /**
     * This methods appends a new Line to the processing history.
     * @param parameters The Parameter used in the last processing task.
     * @throws IOException Maybe thrown whenever IOErrors occur.
     */
    public synchronized void appendLine(String parameters) throws IOException {
        removeDoubleEntries(parameters);
        if (parameterValues.size() < maxLineCount) {
            parameterValues.add(0, parameters);
            dateValues.add(0, getCurrentDateStr());
            createIdValues();
        } else {
            parameterValues.remove(parameterValues.size() - 1);
            parameterValues.add(0, parameters);
            dateValues.remove(dateValues.size() - 1);
            dateValues.add(0, getCurrentDateStr());
            createIdValues();
        }
        saveToFile();
    }

    private void createIdValues() {
        idValues = new ArrayList<Double>();
        double d = 1;
        for (int i = 0; i < parameterValues.size(); i++) {
            idValues.add(d);
            d++;
        }
    }

    private void removeDoubleEntries(String str) {
        for (int i = 0; i < parameterValues.size(); i++) {
            if (parameterValues.get(i).equals(str)) {
                dateValues.remove(i);
                parameterValues.remove(i);
            }
        }
    }

    private String getCurrentDateStr() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    private synchronized void saveToFile() throws IOException {
        historySet = new ArffDataSet(historySetStr);
        historySet.addAttribute(new NumericAttribute(idStr, idValues));
        historySet.addAttribute(new StringAttribute(dateStr, dateValues));
        historySet.addAttribute(new StringAttribute(parameterStr, parameterValues));
        historySet.saveToArffFile(historyFile);
    }

    /**
     * @return
     */
    public static String[] getHistoryList() throws IOException {
        new ProcessingHistory();
        StringAttribute parameters = (StringAttribute) ProcessingHistory.historySet.getAttribute(parameterStr);
        return parameters.getValues().toArray(new String[parameters.getValueCount()]);
    }
}
