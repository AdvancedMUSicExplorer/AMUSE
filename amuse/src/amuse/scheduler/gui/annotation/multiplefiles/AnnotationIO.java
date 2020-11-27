package amuse.scheduler.gui.annotation.multiplefiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Level;

import amuse.data.io.ArffDataSet;
import amuse.data.io.attributes.Attribute;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.preferences.AmusePreferences;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttributeType;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationNominalAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationNumericAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationStringAttribute;
import amuse.scheduler.gui.controller.MultipleFilesAnnotationController;
import amuse.util.AmuseLogger;

/**
 * Manages the annotation process related to the annotation attributes
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationIO {
	
	private MultipleFilesAnnotationController annotationController;
	private static final String ARFF_VALUE_UNDEFINED = "?"; 
	private String loadedPath;
	private String loadedDataSetName;

	public AnnotationIO(MultipleFilesAnnotationController annotationController){
		this.annotationController = annotationController;
		loadedPath = null;
		loadedDataSetName = null;
	}
	
	public String getLoadedPath(){
		return loadedPath;
	}
	
	public String getLoadedDataSetName(){
		return loadedDataSetName;
	}
	
	public void startNewAnnotation(){
		loadedPath = null;
		loadedDataSetName = null;
	}
	
	public void loadAnnotation(String path, String dataSetName){
		// If the path does not exist, no annotation has been saved yet. Therefore, return.
		if(!new File(path).exists()){
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Unable to load annotation. Path '"
					+ path
					+ "' does not exist.");
			return;
		}
		annotationController.clearAnnotation();
		ArffDataSet dataSet = null;
		try {
			dataSet = new ArffDataSet(new File(path));
		} catch (IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Unable to load annotation. While reading the file '"
					+ path
					+ "' the following error occured: "
					+ e.getMessage());
			loadedPath = null;
			loadedDataSetName = null;
			return; 
		}
		LinkedList<String> attributesToAvoid = new LinkedList<String>(Arrays.asList(new String[]{"Id", "Path", "Unit", "Start", "End"}));
		LinkedList<Integer> indicesToOmitList = new LinkedList<Integer>();
		for(int i = 0; i < dataSet.getAttributeCount(); i++){
			if(attributesToAvoid.contains(dataSet.getAttribute(i).getName())){
				indicesToOmitList.add(i);
			}
			else{
				Attribute att = dataSet.getAttribute(i);
				AnnotationAttribute<?> annAtt;
				if (att instanceof NominalAttribute){
					annAtt = new AnnotationNominalAttribute(att.getName());
					for(String allowedValue: ((NominalAttribute) att).getNominalValues()){
						((AnnotationNominalAttribute) annAtt).addAllowedValue(allowedValue);
					}
				}
				else if(att instanceof StringAttribute){
					annAtt = new AnnotationStringAttribute(att.getName());
				}
				else{
					annAtt = new AnnotationNumericAttribute(att.getName());
				}
				annotationController.addAttribute(annAtt);
			}
		}
		
		for(int row = 0; row < dataSet.getValueCount(); row++){
			DefaultTableModel tableModel = annotationController.getTableModel();
			TableColumnModel columnModel = annotationController.getColumnModel();
			tableModel.addRow(new Object[]{null});
			for(int column = 1; column < columnModel.getColumnCount(); column ++){
				Object header = columnModel.getColumn(column).getHeaderValue();
				String name = header instanceof AnnotationAttribute? ((AnnotationAttribute<?>) header).getName(): header.toString();
				String value = dataSet.getAttribute(name).getValueAt(row).toString();
				if(value.equals(ARFF_VALUE_UNDEFINED)
						|| value.equals("NaN")){
					value = "";
				}
				tableModel.setValueAt(value, row, column);
			}
		}
		loadedPath = path;
		loadedDataSetName = dataSetName;
	}

	public void saveAnnotation(String path, String dataSetName){
		DefaultTableModel tableModel = annotationController.getTableModel();
		TableColumnModel columnModel = annotationController.getColumnModel();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path);
			writer.write("@RELATION 'Annotation " + dataSetName + "'\n");
			writer.write("%rows=" + tableModel.getRowCount() + "\n");
			writer.write("%columns=" + (columnModel.getColumnCount() + 3) + "\n\n");
			
			// Write the header according to the type of the attribute
			writer.write("@ATTRIBUTE Id NUMERIC\n");
			writer.write("@ATTRIBUTE Path STRING\n");
			writer.write("@ATTRIBUTE Unit {milliseconds,samples}\n");
			writer.write("@ATTRIBUTE Start NUMERIC\n");
			writer.write("@ATTRIBUTE End NUMERIC\n");
			// List that contains indicates the need of quotation marks for every attribute.
			ArrayList<String> quotMarks = new ArrayList<String>(columnModel.getColumnCount());
			quotMarks.add(""); // For the first column
			quotMarks.add("'"); // The path needs quotation marks
			for(int col = 2; col < columnModel.getColumnCount(); col++){
				AnnotationAttribute<?> att = (AnnotationAttribute<?>) columnModel.getColumn(col).getHeaderValue();
				String typeString = "";
				if(att.getType() == AnnotationAttributeType.NOMINAL){
					DefaultListModel<String> listModel = ((AnnotationNominalAttribute) att).getAllowedValues();
					for(int i = 0; i < listModel.size(); i++){
						typeString += "'" + listModel.getElementAt(i) + "'" + ",";
					}
					typeString = "{" + typeString.substring(0, typeString.length() - 1) + "}";
				}
				else{
					typeString = att.getType().toString();
				}
				if(att.getType() == AnnotationAttributeType.NUMERIC){
					quotMarks.add("");
				}
				else{
					quotMarks.add("'");
				}
				writer.write("@ATTRIBUTE '" + att.getName() + "' " + typeString + "\n");
			}
			writer.write("\n\n");
			
			
			
			// Write the data of the attribute
			writer.write("@DATA\n");
			for(int row = 0; row < tableModel.getRowCount(); row++){
				String rowData = row + ", '" + tableModel.getValueAt(row, 1) + "', milliseconds, 0, -1, ";
				for(int col = 2; col < columnModel.getColumnCount(); col++){
					String value = tableModel.getValueAt(row, col) + "";
					if(value.equals(null + "") || value.equals("")){
						value = ARFF_VALUE_UNDEFINED;
					}
					rowData += quotMarks.get(col) + value + quotMarks.get(col) + ", ";
				}
				writer.write(rowData.substring(0, rowData.length() - 2) + "\n"); //Delete the last comma and add a line break
			}
		}
		catch (FileNotFoundException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Unable to save the annotation: "
					+ e.getMessage());
			loadedPath = null;
			loadedDataSetName = null;
			return;
		}
		finally {
			if(writer != null){
				writer.close();
			}
		}
		loadedPath = path;
		loadedDataSetName = dataSetName;
		ArffDataSet categoryList = null;
		try {
			categoryList = new ArffDataSet(new File(AmusePreferences.getMultipleTracksAnnotationTablePath()));
		} catch (IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Unable to synchronize the annotation with the multipleTracksAnnotationTable. While reading '"
					+ AmusePreferences.getMultipleTracksAnnotationTablePath()
					+ "' the following error occured: "
					+ e.getMessage());
			loadedDataSetName = null;
			return;
		}
		int maxId = 0;
		for(int i = 0; i < categoryList.getValueCount(); i++) {
			int id = new Double(categoryList.getAttribute("Id").getValueAt(i).toString()).intValue();
			maxId = Math.max(id, maxId);
			if(categoryList.getAttribute("CategoryName").getValueAt(i).toString().equals(dataSetName)){
				categoryList.getAttribute("Path").setValueAt(i,path);
				try {
					categoryList.saveToArffFile(new File(AmusePreferences.getMultipleTracksAnnotationTablePath()));
				} catch (IOException e) {
					AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Unable to synchronize the annotation with the multipleTracksAnnotationTable. While saving '"
							+ AmusePreferences.getMultipleTracksAnnotationTablePath()
							+ "' the following error occured: "
							+ e.getMessage());
					loadedDataSetName = null;
				}
				return;
			}
		}
		// add new line in categoryTable
		try {
		    Files.write(Paths.get(AmusePreferences.getMultipleTracksAnnotationTablePath()),
		    		("\n" + (maxId + 1) + ",'" + path + "','" + dataSetName + "'").getBytes() ,
		    		StandardOpenOption.APPEND);
		}catch (IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Unable to synchronize the annotation with the multipleTracksAnnotationTable. While appending a line to '"
					+ AmusePreferences.getMultipleTracksAnnotationTablePath()
					+ "' the following error occured: "
					+ e.getMessage());
			loadedDataSetName = null;
			return;
		}
		
	}

	
}

