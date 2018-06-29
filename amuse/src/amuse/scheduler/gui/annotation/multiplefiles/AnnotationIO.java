package amuse.scheduler.gui.annotation.multiplefiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Level;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttributeType;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationNominalAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationNumericAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationStringAttribute;
import amuse.scheduler.gui.controller.MultipleFilesAnnotationController;
import amuse.util.AmuseLogger;
import sun.reflect.annotation.AnnotationType;

/**
 * Manages the annotation process related to the annotation attributes
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationIO {
	
	private MultipleFilesAnnotationController annotationController;
	private LinkedHashMap<Integer, AnnotationAttribute<?>> idToAttributeMap;
	private int maxAssignedId;
	private static final String ARFF_VALUE_UNDEFINED = "?"; 
	private static final String MULTIPLE_ANNOTATION_ATTRIBUTE_TABLE_FILE_NAME = "multipleSongsAnnotationAttributeTable.arff";

	public AnnotationIO(MultipleFilesAnnotationController annotationController){
		this.annotationController = annotationController;
		maxAssignedId = -1;
		idToAttributeMap = new LinkedHashMap<Integer, AnnotationAttribute<?>>(){
			
			@Override
			public AnnotationAttribute<?> get(Object key) {
				return super.get(key).newInstance();
			}
		};
		loadAnnotationAttributeTable();
	}
	
	/**
	 * Saves the table called "annotationAttributeTable.arff" that should be placed in the annotation database.
	 */
	private void saveAnnotationAttributeTable(){
		String tablePath = AmusePreferences.get(KeysStringValue.AMUSE_PATH)
				+ File.separator
				+ "config"
				+ File.separator
				+ MULTIPLE_ANNOTATION_ATTRIBUTE_TABLE_FILE_NAME;
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(tablePath);
			
			writer.write("@RELATION 'Annotation Attribute Table'\n"
						+ "%rows=" + idToAttributeMap.size() + "\n"
						+ "%columns=3\n"
						+ "%max id=" + maxAssignedId + "\n\n"
						+ "@ATTRIBUTE 'ID' NUMERIC\n"
						+ "@ATTRIBUTE 'Attribute Type' STRING\n"
						+ "@ATTRIBUTE 'Name' STRING\n\n"
						+ "@DATA\n");
			for(AnnotationAttribute<?> att: idToAttributeMap.values()){
				String typeString = att.getType().toString();
				if(att.getType() == AnnotationAttributeType.NOMINAL){
					typeString = "{";
					for(int i = 0; i < ((AnnotationNominalAttribute) att).getAllowedValues().size(); i++){
						typeString += "'" + ((AnnotationNominalAttribute) att).getAllowedValues().get(i) + "',";
					}
					typeString = typeString.substring(0, typeString.length() - 1) + "}";
				}
				
				writer.write(att.getId() + ", " + typeString + ", '" + att.getName() + "'\n");
			}
		}
		catch(FileNotFoundException e){
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Could not find the annotationAttributeTable '" + tablePath + "'");
		}
		finally{
			if(writer != null){
				writer.close();
			}
		}
	}
	
	
	/**
	 * Loads the table called "annotationAttributeTable.arff" that should be placed in the annotation database.
	 */
	private void loadAnnotationAttributeTable(){
		String tablePath = AmusePreferences.get(KeysStringValue.AMUSE_PATH)
				+ File.separator
				+ "config"
				+ File.separator
				+ MULTIPLE_ANNOTATION_ATTRIBUTE_TABLE_FILE_NAME;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(tablePath));
			String line = "";
			LinkedList<String> attributeOrder = new LinkedList<String>();
			/*
			 * Read the attributes @ATTRIBUTE
			 */
			while((line = reader.readLine()) != null){
				// If the line is empty or a comment, do nothing
				String lowerCaseLine = line.toLowerCase();
				if(line.isEmpty() || line.equals("\n") || line.startsWith("@relation")){
					continue;
				}
				else if(lowerCaseLine.startsWith("%max id")){
					maxAssignedId = Integer.parseInt(lowerCaseLine.substring(lowerCaseLine.indexOf('=') + 1).replace(" ", ""));
				}
				else if(lowerCaseLine.startsWith("@attribute")){
					if(lowerCaseLine.contains("attribute type")){
						attributeOrder.add("type");
					}
					else if(lowerCaseLine.contains("name")){
						attributeOrder.add("name");
					}
					else if(lowerCaseLine.contains("id")){
						attributeOrder.add("id");
					}
				}
				else if(lowerCaseLine.startsWith("@data")){
					break;
				}
			}
			// If the appropriate number of attributes were not read, something went wrong.
			if(attributeOrder.size() != 3){
				AmuseLogger.write(this.getClass().getName(), Level.ERROR,
						"Could not load the annotationAttributeTable '" + tablePath 
						+ "' because of the wrong number of attributes. Read: " + attributeOrder.size() +  " Expected: 3");
				return;
			}
			/*
			 * Extract the data after @DATA
			 */
			while((line = reader.readLine()) != null){
				// If the line is empty or a comment, do nothing
				if(line.isEmpty() || line.equals("\n") || line.startsWith("%") || line.length() <= 2){
					continue;
				}
				else{
					line = line + ","; // Add a comma to the end of the line to ensure that line.indexOf(',') always returns something positive
					String name = null;
					int id = -1;
					AnnotationAttributeType type = null;
					String[] allowedValuesForNominalAttributes = null;
					for(String currentAttribute: attributeOrder){
						int indexOfComma = line.indexOf(',');
						
						if(currentAttribute.equals("name")){
							name = line.substring(0, indexOfComma);
							if (name.startsWith("'")){
								name = name.substring(1);
							}
							if (name.endsWith("'")){
								name = name.substring(0, name.length() - 1);
							}
						}
						else if(currentAttribute.equals("type")){
							String lowerCaseType = line.substring(0, indexOfComma).toLowerCase();
							if(line.startsWith("{")){
								type = AnnotationAttributeType.NOMINAL;
								indexOfComma = line.indexOf('}') + 1;
								String nominalValues = line.substring(0, indexOfComma);
								
								if(nominalValues.indexOf('{') + 2 <= nominalValues.length() - 2){
									String allowedValues = nominalValues.substring(nominalValues.indexOf('{') + 2, nominalValues.length() - 2);
									if(allowedValues.length() > 0){
										allowedValuesForNominalAttributes = allowedValues.split("' *, *'");
									}
								}
							}
							else if(lowerCaseType.contains("string")){
								type = AnnotationAttributeType.STRING;
							}
							else if(lowerCaseType.contains("numeric")){
								type = AnnotationAttributeType.NUMERIC;
							}
							
						}
						else if(currentAttribute.equals("id")){
							id = Integer.parseInt(line.substring(0, indexOfComma));
						}
						
						// Delete the previously read part of the line
						line = line.substring(indexOfComma + 1);
						
						// Delete leading spaces
						while(line.startsWith(" ")){
							line = line.substring(1);
						}
					}
					if(name == null || id == -1 || type == null || (type == AnnotationAttributeType.NOMINAL && allowedValuesForNominalAttributes == null)){
						AmuseLogger.write(this.getClass().getName(), Level.ERROR,
								"Could not load the annotationAttributeTable '" + tablePath + "'");
						return;
					}
					AnnotationAttribute<?> att = null;
					switch(type){
					case NUMERIC: att = new AnnotationNumericAttribute(name, id); break;
					case STRING: att = new AnnotationStringAttribute(name, id); break;
					case NOMINAL: 
						att = new AnnotationNominalAttribute(name, id);
						for(String allowedValue: allowedValuesForNominalAttributes){
							((AnnotationNominalAttribute) att).addAllowedValue(allowedValue);
						}
						break;
					}
					idToAttributeMap.put(att.getId(), att);
						
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// If the comment for the max id was not recognized, set maxAssignedId to the maximum id of all attributes.
		if(maxAssignedId < 0){
			for(int id: idToAttributeMap.keySet()){
				maxAssignedId = Math.max(maxAssignedId, id);
			}
		}
		
	}
	
	
	public void addNewAttribute(AnnotationAttribute<?> att){
		idToAttributeMap.put(att.getId(), att);
		maxAssignedId = Math.max(att.getId(), maxAssignedId);
		this.saveAnnotationAttributeTable();
	}

	public boolean isAttributeNameAvailable(String name){
		for (AnnotationAttribute<?> att: idToAttributeMap.values()){
			if(att.getName().equalsIgnoreCase(name)){
				return false;
			}
		}
		return true;
	}
	
	public void removeAttributeFromAnnotationAttributeTable(int id){
		idToAttributeMap.remove(id);
		saveAnnotationAttributeTable();
	}
	
	public LinkedHashMap<Integer, AnnotationAttribute<?>> getAnnotationAttributeTable() {
		return idToAttributeMap;
	}

	public int getNextAvailableId() {
		return maxAssignedId + 1;
	}
	
	
	public void loadAnnotation(String path){
	
		// If the path does not exist, no annotation has been saved yet. Therefore, return.
		if(!new File(path).exists()){
			return;
		}
		annotationController.clearAnnotation();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(path)));
			String line = "";
			/*
			 * Read the attributes @ATTRIBUTE
			 */
			LinkedList<Integer> indicesToOmitList = new LinkedList<Integer>();
			int indexOfPath = -1;
			int currentIndex = 0;
			while((line = reader.readLine()) != null){
				// If the line is empty, a comment or starts with @RELATION, do nothing
				String lowerCaseLine = line.toLowerCase();
				if(line.isEmpty() || line.matches(" *\n") || line.startsWith("%") || lowerCaseLine.startsWith("@relation")){
					continue;
				}
				else if(lowerCaseLine.startsWith("@attribute")){
					if(lowerCaseLine.contains("path")){
						indexOfPath = currentIndex;
						indicesToOmitList.add(currentIndex);
					}
					else{
						lowerCaseLine = lowerCaseLine.replaceAll("@attribute *'*", "");
						Matcher matcher = Pattern.compile("[0-9]+").matcher(lowerCaseLine);
						if(matcher.find()){
							int id = Integer.parseInt(matcher.group(0));
							if(idToAttributeMap.containsKey(id)){
								AnnotationAttribute<?> att = idToAttributeMap.get(id);
								annotationController.addAttribute(att);
							}
							else{
								indicesToOmitList.add(currentIndex);
								AmuseLogger.write(this.getClass().getName(),
										Level.WARN,
										"The attribute with id '" + id + "' was not found and its data will be ignored.");
							}
						}
						else{
							
							if(JOptionPane.showConfirmDialog(null,
									"The Id is missing for the attribute from line '"
									+ line
									+ "'. Do you want to load its data into another attribute?",
									"Loading Data from an Attribute Without Id",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
								int columnCount = annotationController.getColumnCount();
								if(line.contains("}")){
									annotationController.showAddAttributeDialog("Specifying an Attribute for " + line, AnnotationAttributeType.NOMINAL);
								}
								else{
									for(AnnotationAttributeType type: AnnotationAttributeType.values()){
										if(lowerCaseLine.endsWith(type.toString().toLowerCase())){
											annotationController.showAddAttributeDialog("Specifying an Attribute for " + line, type);
											break;
										}
									}
								}
								// Check if no column was added
								if(columnCount == annotationController.getColumnCount()){
									indicesToOmitList.add(currentIndex);
									AmuseLogger.write(this.getClass().getName(),
											Level.WARN,
											"The attribute from the line '" + line + "' does not have an id and therefore, its data will be ignored.");
								}
							}
							else{
								
								indicesToOmitList.add(currentIndex);
								AmuseLogger.write(this.getClass().getName(),
										Level.WARN,
										"The attribute from the line '" + line + "' does not have an id and therefore, its data will be ignored.");
							}
						}
					}
					currentIndex++;
				}
				else if(lowerCaseLine.startsWith("@data")){
					break;
				}
			}
			/*
			 * Extract the data after @DATA
			 */
			while((line = reader.readLine()) != null){
				if(line.isEmpty() || line.equals("\n") || line.startsWith("%")){
					continue;
				}
				else{
					line = line.replaceAll("\\?", "");
					String[] splitLine = line.split("'? *, *'?");
					splitLine[0] = splitLine[0].replaceAll("'", "");
					splitLine[splitLine.length - 1] = splitLine[splitLine.length - 1].replaceAll("'", "");
					String[] rowData = new String[splitLine.length - indicesToOmitList.size() + 1]; // + 1 for the path, whose index is also in indicesToOmitList
					rowData[0] = splitLine[indexOfPath]; // The path must be in first place
					int j = 1;
					for(int i = 0; i < splitLine.length; i++){
						if(!indicesToOmitList.contains(i)){
							rowData[j] = splitLine[i];
							j++;
						}
					}
					annotationController.addRow(rowData);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void saveAnnotation(String path){
		DefaultTableModel tableModel = annotationController.getTableModel();
		TableColumnModel columnModel = annotationController.getColumnModel();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path);
			writer.write("@RELATION 'Annotation'" + "'\n");
			writer.write("%rows=" + tableModel.getRowCount() + "\n");
			writer.write("%columns=" + (tableModel.getColumnCount() - 1) + "\n\n");
			
			// Write the header according to the type of the attribute
			writer.write("@ATTRIBUTE Path String\n");
			ArrayList<String> quotMarks = new ArrayList<String>(columnModel.getColumnCount());
			quotMarks.add(""); // For the first column
			quotMarks.add("'");
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
				writer.write("@ATTRIBUTE '" + att.getId() + " - " + att.getName() + "' " + typeString + "\n");
			}
			writer.write("\n\n");
			
			
			
			// Write the data of the attribute
			writer.write("@DATA\n");
			for(int row = 0; row < tableModel.getRowCount(); row++){
				String rowData = "";
				for(int col = 1; col < tableModel.getColumnCount(); col++){
					String value = tableModel.getValueAt(row, col) + "";
					if(value.equals(null + "")){
						value = ARFF_VALUE_UNDEFINED;
					}
					rowData += quotMarks.get(col) + value + quotMarks.get(col) + ", ";
				}
				writer.write(rowData.substring(0, rowData.length() - 2) + "\n"); //Delete the last comma and add a line break
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			if(writer != null){
				writer.close();
			}
		}
		
	}

	
}

