package amuse.scheduler.gui.annotation.singlefile.attribute;

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
import amuse.scheduler.gui.controller.SingleFileAnnotationController;
import amuse.util.AmuseLogger;

/**
 * Manages the annotation process related to the annotation attributes
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationModel {
	
	private DefaultListModel<AnnotationAttribute<?>> attributeListModel;
	private SingleFileAnnotationController annotationController;
	private LinkedHashMap<Integer, AnnotationAttribute<?>> idToAttributeMap;
	private int maxAssignedId;
	private static final String ARFF_VALUE_UNDEFINED = "?"; 

	public AnnotationModel(SingleFileAnnotationController annotationController){
		this.annotationController = annotationController;
		attributeListModel = new DefaultListModel<AnnotationAttribute<?>>();
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
	 * Saves the table called "singleTrackAnnotationAttributeTable.arff" that should be placed in the annotation database.
	 */
	private void saveAnnotationAttributeTable(){
		String tablePath = AmusePreferences.getSingleTrackAnnotationAttributeTablePath();
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(tablePath);
			
			writer.write("@RELATION 'Annotation Attribute Table'\n"
						+ "%rows=" + idToAttributeMap.size() + "\n"
						+ "%columns=3\n"
						+ "%max id=" + maxAssignedId + "\n\n"
						+ "@ATTRIBUTE 'ID' NUMERIC\n"
						+ "@ATTRIBUTE 'Attribute Type' STRING\n"
						+ "@ATTRIBUTE 'Name' STRING\n"
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
	 * Loads the table called "singleTrackAnnotationAttributeTable.arff" that should be placed in the annotation database.
	 */
	private void loadAnnotationAttributeTable(){
		String tablePath = AmusePreferences.getSingleTrackAnnotationAttributeTablePath();
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
		
		// If the comment for the max id was not recognized, set maxAssignedId to the maximum id of all attributes.
		if(maxAssignedId < 0){
			for(int id: idToAttributeMap.keySet()){
				maxAssignedId = Math.max(maxAssignedId, id);
			}
		}
		
	}
	
	public void addEntryToItsAttribute(AnnotationAttributeEntry<?> entry){
		((AnnotationAttribute<Object>) entry.getAnnotationAttribute()).addEntry((AnnotationAttributeEntry<Object>) entry);
	}
	
	public AnnotationAttributeEntry<?> addNewValueToAttribute(AnnotationAttribute<?> att){
		double start = annotationController.getCurrentMs() / 1000.;
		switch(att.getType()){
		case NOMINAL:
			if(((AnnotationNominalAttribute) att).getAllowedValues().isEmpty()){
				((AnnotationNominalAttribute) att).addAllowedValue("new value");
				return ((AnnotationNominalAttribute) att).addEntry(start, start + 0.001, "new value");
			}
			else{
				return ((AnnotationNominalAttribute) att).addEntry(start, start + 0.001, ((AnnotationNominalAttribute) att).getAllowedValues().get(0));
			}
		case EVENT: 
			return ((AnnotationEventAttribute) att).addEntry(start, -1, null);
		case NUMERIC:
			return ((AnnotationNumericAttribute) att).addEntry(start, start + 0.001, 0.);
		case STRING: 
			return ((AnnotationStringAttribute) att).addEntry(start, start + 0.001, "new value");
		default: 
			return att.addEntry(start, start + 1, null);
		}
	}
	
	public AnnotationAttribute<?> addAttribute(int id){
		AnnotationAttribute<?> att = idToAttributeMap.get(id);
		attributeListModel.addElement(att);
		return att;
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
	
	public boolean isAttributeIdAvailable(String id){
		return !idToAttributeMap.keySet().contains(id);
	}
	
	public void removeAttribute(AnnotationAttribute<?> att){
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
		for(File file: new File(pathToDir).listFiles()){
			if(!file.isDirectory() && file.getName().endsWith(".arff")){
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(file));
					String line = "";
					String entryName = file.getName();
					String id;
					if(entryName.contains("-")){
						id = entryName.substring(0, entryName.indexOf('-'));
					}
					else{
						id = entryName.substring(0,entryName.indexOf('.'));
					}
					AnnotationAttribute<?> att = null;
					try{
						att = idToAttributeMap.get(Integer.parseInt(id));
					}
					catch(NumberFormatException e){
						continue;
					}
					if(att == null){
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
					/*
					 * Extract the data after @DATA
					 */
					if(attributeOrder.size() == 1 && att.getType() == AnnotationAttributeType.EVENT // Old format
							|| attributeOrder.size() == 3){
						while((line = reader.readLine()) != null){
							// If the line is empty or a comment, do nothing
							if(line.isEmpty() || line.equals("\n") || line.startsWith("%")){
								continue;
							}
							else{
								AnnotationAttributeEntry<?> entry = annotationController.addNewEntryToAttribute(att);
								line = line + ","; // Add a comma to the end of the line to ensure that line.indexOf(',') always returns something positive
								for(String currentAttribute: attributeOrder){
									int indexOfComma = line.indexOf(',');
									
									if(currentAttribute.equals("start")){
										String start = line.substring(0,indexOfComma);
										entry.setStart(Double.parseDouble(start));
										if (!start.contains(".")){ // start is in ms and must be converted to seconds 
											entry.setStart(entry.getStart() / 1000);
										}
									}
									else if(currentAttribute.equals("end")){
										String end = line.substring(0,indexOfComma);
										entry.setEnd(Double.parseDouble(end));
										if (!end.contains(".")){ // end is in ms and must be converted to seconds 
											entry.setEnd(entry.getEnd() / 1000);
										}
									}
									else if(currentAttribute.equals("value")){
										switch(att.getType()){
										case STRING: 
										case NOMINAL: 
											((AnnotationAttributeEntry<String>) entry).setValue(line.substring(1, line.lastIndexOf('\'')));
											break;
										case NUMERIC:
											((AnnotationAttributeEntry<Double>) entry).setValue(Double.parseDouble(line.substring(0,indexOfComma)));
											break;
										case EVENT: // This case is impossible because EventAttributes do not have a value
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
						
					}
					/*
					 * New Format: Only the start time is saved per time window. Also, the time is saved as second
					 */
					else if(att.getType() != AnnotationAttributeType.EVENT
							&& attributeOrder.size() == 2){
						LinkedList<AnnotationAttributeEntry<?>> emptyEntryList = new LinkedList<AnnotationAttributeEntry<?>>(); //Saves the entries that define empty spaces and need to be deleted afterwards
						while((line = reader.readLine()) != null){
							// If the line is empty or a comment, do nothing
							if(line.isEmpty() || line.equals("\n") || line.startsWith("%")){
								continue;
							}
							else{
								AnnotationAttributeEntry<?> entry = annotationController.addNewEntryToAttribute(att);
								line = line + ","; // Add a comma to the end of the line to ensure that line.indexOf(',') always returns something positive
								for(String currentAttribute: attributeOrder){
									int indexOfComma = line.indexOf(',');
									
									if(currentAttribute.equals("start")){
										String start = line.substring(0,indexOfComma);
										entry.setStart(Double.parseDouble(start));
										AnnotationAttributeEntry<?> previousEntry = entry.getPreviousEntry();
										if(previousEntry != null){
											previousEntry.setEnd(entry.getStart());
										}
									}
									else if(currentAttribute.equals("value")){
										if(line.contains(ARFF_VALUE_UNDEFINED) && line.indexOf('\'') == -1){ // Line defines an empty space
											emptyEntryList.add(entry);
										}
										else{
											switch(att.getType()){
											case STRING: 
											case NOMINAL: 
												((AnnotationAttributeEntry<String>) entry).setValue(line.substring(1, line.lastIndexOf('\'')));
												break;
											case NUMERIC:
												((AnnotationAttributeEntry<Double>) entry).setValue(Double.parseDouble(line.substring(0,indexOfComma)));
												break;
											case EVENT: // This case is impossible because EventAttributes do not have a value
											}
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
						// Delete the empty spaces
						for(AnnotationAttributeEntry<?> entry: emptyEntryList){
							annotationController.removeEntry(entry);
						}
					}
					else{// If the appropriate number of attributes were not read, something went wrong.
						AmuseLogger.write(this.getClass().getName(), Level.ERROR,
								"Could not load the annotation file '" + file.getAbsolutePath() 
								+ "' because of the wrong number of attributes. Read: " + attributeOrder.size() +  " Expected: " + (att.getType() == AnnotationAttributeType.EVENT? "1": "2"));
						continue;
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
		pathToDir = AmusePreferences.get(KeysStringValue.SINGLE_TRACK_ANNOTATION_DATABASE)
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
			saveAnnotation(pathToDir + getAttributeFileName(att), att);
		}
	}
	
	private String getAttributeFileName(AnnotationAttribute<?> att){
		return att.getId() + "-" + att.getName().replace(" ", "_") + ".arff";
	}
	
	private void saveAnnotation(String pathToArff, AnnotationAttribute<?> att) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(pathToArff);
			writer.write("@RELATION 'Annotation " + att.getType() + "'\n");
			writer.write("%rows=" + att.getEntryList().size() + "\n");
			writer.write("%columns=" + (att.getType() == AnnotationAttributeType.EVENT? "1": "3") + "\n");
			writer.write("%file path=" + annotationController.getMusicFilePath() + "\n\n");
			
			// Write the header according to the type of the attribute
			String quotMarks = att.getType() == AnnotationAttributeType.NUMERIC ? "" : "'";
			if(att.getType() == AnnotationAttributeType.EVENT){
				writer.write("@ATTRIBUTE '" + att.getName() + ": time in s' NUMERIC\n\n");
			}
			else{
				writer.write("@ATTRIBUTE '" + att.getName() + ": start time in s' NUMERIC\n");
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
				for(int i = 0; i < att.getEntryList().size(); i++) {
					writer.write(att.getEntryList().getElementAt(i).getStart() + "\n");
				}
			}
			else if(att.getEntryList().size() > 0){
				if(att.getEntryList().getElementAt(0).getStart() > 0.){
					writer.write("0.0"
							+ ", " 
							+ ARFF_VALUE_UNDEFINED
							+ "\n");
				}
				double lastEnd = Double.POSITIVE_INFINITY;
				for(int i = 0; i < att.getEntryList().size(); i++) {
					AnnotationAttributeEntry<?> entry = att.getEntryList().getElementAt(i); 
					double currentEnd = entry.getEnd();
					double currentStart = entry.getStart();
					if(currentStart > lastEnd){
						writer.write(lastEnd
								+ ", " 
								+ ARFF_VALUE_UNDEFINED
								+ "\n");
					}
					writer.write(currentStart
								+ ", " 
								+ quotMarks
								+ entry.getValue()
								+ quotMarks
								+ "\n");
					lastEnd = currentEnd;
				}
				writer.write(lastEnd
						+ ", " 
						+ ARFF_VALUE_UNDEFINED
						+ "\n");
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

	public int getNextAvailableId() {
		return maxAssignedId + 1;
	}

	public void deleteAttributeFile(AnnotationAttribute<?> att) {
		new File(getAnnotationFolderForMusic() + getAttributeFileName(att)).delete();
	}

	
}
