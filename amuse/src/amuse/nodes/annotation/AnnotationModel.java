package amuse.nodes.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import javax.swing.DefaultListModel;

import org.apache.log4j.Level;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.controller.AnnotationController;
import amuse.util.AmuseLogger;

/**
 * Manages the annotation process related to the annotation attributes
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationModel {
	
	private DefaultListModel<AnnotationAttribute<?>> attributeListModel;
	private AnnotationController annotationController;
	private LinkedHashMap<Integer, AnnotationAttribute<?>> idToAttributeMap;

	public AnnotationModel(AnnotationController annotationController){
		this.annotationController = annotationController;
		attributeListModel = new DefaultListModel<AnnotationAttribute<?>>();
		
		idToAttributeMap = new LinkedHashMap<Integer, AnnotationAttribute<?>>();
		loadAnnotationAttributeTable();
	}
	
	/**
	 * Loads the table called "annotationAttributeTable.arff" that should be placed in the annotation database.
	 */
	private void loadAnnotationAttributeTable(){
		String tablePath = AmusePreferences.get(KeysStringValue.AMUSE_PATH)
				+ File.separator
				+ "config"
				+ File.separator
				+ "annotationAttributeTable.arff";
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
				if(line.isEmpty() || line.equals("\n") || line.startsWith("%") || line.startsWith("@relation")){
					continue;
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
										allowedValuesForNominalAttributes = allowedValues.split("','");
									}
								}
							}
							else if(lowerCaseType.contains("string")){
								type = AnnotationAttributeType.STRING;
							}
							else if(lowerCaseType.contains("numeric")){
								type = AnnotationAttributeType.NUMERIC;
							}
							else if(lowerCaseType.contains("event")){
								type = AnnotationAttributeType.EVENT;
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
					case EVENT: att = new AnnotationEventAttribute(name, id); break;
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
	}
	
	
	
	public AnnotationAttributeValue<?> addNewValueToAttribute(AnnotationAttribute<?> annotationAttribute){
		int start = (int) annotationController.getCurrentMs();
		switch(annotationAttribute.getType()){
		case NOMINAL:
			if(((AnnotationNominalAttribute) annotationAttribute).getAllowedValues().isEmpty()){
				((AnnotationNominalAttribute) annotationAttribute).addAllowedValue("new value");
				return ((AnnotationNominalAttribute) annotationAttribute).addValue(start, start + 1, "new value");
			}
			else{
				return ((AnnotationNominalAttribute) annotationAttribute).addValue(start, start + 1, ((AnnotationNominalAttribute) annotationAttribute).getAllowedValues().get(0));
			}
		case EVENT: 
			return ((AnnotationEventAttribute) annotationAttribute).addValue(start, -1, null);
		case NUMERIC:
			return ((AnnotationNumericAttribute) annotationAttribute).addValue(start, start + 1, 0.);
		case STRING: 
			return ((AnnotationStringAttribute) annotationAttribute).addValue(start, start + 1, "new value");
		default: 
			return annotationAttribute.addValue(start, start + 1, null);
		}
	}
	
	public void addAttribute(int id){
		AnnotationAttribute<?> att = idToAttributeMap.get(id);
		attributeListModel.addElement(att);
	}
	
	public boolean isAttributeNameValid(String name){
		for (int i = 0; i < attributeListModel.size(); i++){
			if(((AnnotationAttribute<?>) attributeListModel.getElementAt(i)).getName().equalsIgnoreCase(name)){
				return false;
			}
		}
		return true;
	}
	
	public void deleteAttribute(AnnotationAttribute<?> att){
		attributeListModel.removeElement(att);
	}
	
	public DefaultListModel<AnnotationAttribute<?>> getListModel(){
		return attributeListModel;
	}
	
	public void clearAnnotation(){
		attributeListModel.clear();
	}
	
	public void loadAnnotation(){
		String pathToDir = this.getAnnotationFolderForMusic();
		
		// If the path does not exist, no annotation has been saved yet. Therefore, return.
		if(!new File(pathToDir).exists()){
			return;
		}
		annotationController.clearAnnotation();
		for(File entry: new File(pathToDir).listFiles()){
			if(!entry.isDirectory() && entry.getName().endsWith(".arff")){
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(entry));
					String line = "";
					String id = entry.getName();
					id = id.substring(0,id.indexOf('.'));
					AnnotationAttribute<?> att = null;
					try{
						att = idToAttributeMap.get(Integer.parseInt(id));
					}
					catch(NumberFormatException e){
						continue;
					}
					LinkedList<String> attributeOrder = new LinkedList<String>();
					/*
					 * Read the attributes @ATTRIBUTE
					 */
					while((line = reader.readLine()) != null){
						// If the line is empty, a comment or starts with @RELATION, do nothing
						String lowerCaseLine = line.toLowerCase();
						if(line.isEmpty() || line.equals("\n") || line.startsWith("%") || lowerCaseLine.startsWith("@relation")){
							continue;
						}
						else if(lowerCaseLine.startsWith("@attribute")){
							if(lowerCaseLine.contains("start") || att.getType() == AnnotationAttributeType.EVENT){
								attributeOrder.add("start");
							}
							else if(lowerCaseLine.contains("end")){
								attributeOrder.add("end");
							}
							else if(lowerCaseLine.contains("value")){
								attributeOrder.add("value");
							}
						}
						else if(lowerCaseLine.startsWith("@data")){
							break;
						}
					}
					// If the appropriate number of attributes were not read, something went wrong.
					int expectedAttributeNumber = att.getType() == AnnotationAttributeType.EVENT? 1:3;
					if(attributeOrder.size() != expectedAttributeNumber){
						AmuseLogger.write(this.getClass().getName(), Level.ERROR,
								"Could not load the annotation file '" + entry.getAbsolutePath() 
								+ "' because of the wrong number of attributes. Read: " + attributeOrder.size() +  " Expected: " + expectedAttributeNumber);
						continue;
					}
					/*
					 * Extract the data after @DATA
					 */
					while((line = reader.readLine()) != null){
						// If the line is empty or a comment, do nothing
						if(line.isEmpty() || line.equals("\n") || line.startsWith("%")){
							continue;
						}
						else{
							AnnotationAttributeValue<?> value = annotationController.addNewValueToAttribute(att);
							line = line + ","; // Add a comma to the end of the line to ensure that line.indexOf(',') always returns something positive
							for(String currentAttribute: attributeOrder){
								int indexOfComma = line.indexOf(',');
								
								if(currentAttribute.equals("start")){
									value.setStart(Integer.parseInt(line.substring(0,indexOfComma)));
								}
								else if(currentAttribute.equals("end")){
									value.setEnd(Integer.parseInt(line.substring(0,indexOfComma)));
								}
								else if(currentAttribute.equals("value")){
									switch(att.getType()){
									case STRING: 
									case NOMINAL: 
										((AnnotationAttributeValue<String>) value).setValue(line.substring(1, line.lastIndexOf('\'')));
										break;
									case NUMERIC:
										((AnnotationAttributeValue<Double>) value).setValue(Double.parseDouble(line.substring(0,indexOfComma)));
										break;
									case EVENT: // This case is impossible because EventAttributes does not have a value
									}
								}
								
								// Delete the previously read part of the line
								line = line.substring(indexOfComma + 1);
								
								// Delete leading spaces
								while(line.startsWith(" ")){
									line = line.substring(1);
								}
							}
						}
					}
					attributeListModel.addElement(att);
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
		}
	}
	
	/**
	 * Derives the path for the annotation folder corresponding to the music piece.
	 * @return the path to the folder as String
	 */
	private String getAnnotationFolderForMusic(){
		String pathToDir = annotationController.getMusicFilePath();
		int startIndex = 0;
		if(pathToDir.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
			startIndex = AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length() + 1;
		}
		pathToDir = AmusePreferences.get(KeysStringValue.ANNOTATION_DATABASE)
					+ File.separator 
					+ pathToDir.substring(startIndex, pathToDir.lastIndexOf("."))
					+ File.separator;
		pathToDir = pathToDir.replaceAll(File.separator + "+", File.separator);
		return pathToDir;
	}
	
	public void saveAnnotation(){
		String pathToDir = this.getAnnotationFolderForMusic();
		
		new File(pathToDir).mkdirs();
		for(int i = 0; i < attributeListModel.size(); i++){
			AnnotationAttribute<?> att = attributeListModel.getElementAt(i);
			saveAnnotation(pathToDir + att.getId() + ".arff", att);
		}
	}
	
	private void saveAnnotation(String pathToArff, AnnotationAttribute<?> att) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(pathToArff);
			writer.write("@RELATION 'Annotation " + att.getType() + "'\n");
			writer.write("%rows=" + att.getValueList().size() + "\n");
			writer.write("%columns=" + (att.getType() == AnnotationAttributeType.EVENT? "1": "3") + "\n\n");
			
			// Write the header according to the type of the attribute
			String quotMarks = att.getType() == AnnotationAttributeType.NUMERIC ? "" : "'";
			if(att.getType() == AnnotationAttributeType.EVENT){
				writer.write("@ATTRIBUTE '" + att.getName() + ": time in ms' NUMERIC\n\n");
			}
			else{
				writer.write("@ATTRIBUTE '" + att.getName() + ": start time in ms' NUMERIC\n");
				writer.write("@ATTRIBUTE '" + att.getName() + ": end time in ms' NUMERIC\n");
				
				writer.write("@ATTRIBUTE '" + att.getName() + ": value' ");
				switch(att.getType()){
				case STRING: 
				case NUMERIC: 
					writer.write(att.getType().toString().toUpperCase()); 
					break;
				case NOMINAL:
					if(((AnnotationNominalAttribute) att).getAllowedValues().size() == 0){
						writer.write("{}");
					}
					else{
						String allowedValues = "";
						for(int i = 0; i < ((AnnotationNominalAttribute) att).getAllowedValues().size(); i++){
							allowedValues += "'" + ((AnnotationNominalAttribute) att).getAllowedValues().get(i) + "',";
						}
						allowedValues = " {" + allowedValues.substring(0, allowedValues.length() - 1) + "}";
						writer.write(allowedValues);
					}
					break;
				case EVENT: // This case is not reachable because EventAttributes were taken care of priorly.
				}
				writer.write("\n\n");
			}
			
			
			// Write the data of the attribute
			writer.write("@DATA\n");
			
			if(att.getType() == AnnotationAttributeType.EVENT){
				for(int i = 0; i < att.getValueList().size(); i++) {
					writer.write(att.getValueList().getElementAt(i).getStart() + "\n");
				}
			}
			else{
				for(int i = 0; i < att.getValueList().size(); i++) {
					writer.write(att.getValueList().getElementAt(i).getStart() + ", " 
								+ att.getValueList().getElementAt(i).getEnd() + ", "
								+ quotMarks + att.getValueList().getElementAt(i).getValue() + quotMarks + "\n");
				}
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

	public LinkedHashMap<Integer, AnnotationAttribute<?>> getAnnotationAttributeTable() {
		return idToAttributeMap;
	}
	
}
