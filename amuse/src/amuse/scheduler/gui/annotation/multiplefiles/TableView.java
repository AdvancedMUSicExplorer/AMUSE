package amuse.scheduler.gui.annotation.multiplefiles;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.EventObject;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttributeType;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationNominalAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationNumericAttribute;
import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationStringAttribute;
import amuse.scheduler.gui.controller.MultipleFilesAnnotationController;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.MediaPlayer.Status;


public class TableView extends JPanel{
	
	JTable table;
	TableModel tableModel;
	final JLabel labelPlay, labelPause;
	MultipleFilesAnnotationController annotationController;
	final String COLUMN_PLAYPAUSE = " ";
	final String COLUMN_PATH = "Path";
	private String commonPathPrefix;
	
	public TableView(MultipleFilesAnnotationController annotationController){
		super();
		this.annotationController = annotationController;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		commonPathPrefix = "";
		
		/*
		 *  Setting up the icons and JLabels used for playing/ pausing the music
		 */
		ImageIcon iconPlay = null;
		try {
			String path = "jar:file:lib/jlfgr-1_0.jar!/toolbarButtonGraphics/media/Play16.gif";
			InputStream is = ((JarURLConnection)new URL(path).openConnection()).getInputStream();
			iconPlay = new ImageIcon(ImageIO.read(is));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ImageIcon iconPause = null;
		try {
			String path = "jar:file:lib/jlfgr-1_0.jar!/toolbarButtonGraphics/media/Pause16.gif";
			InputStream is = ((JarURLConnection)new URL(path).openConnection()).getInputStream();
			iconPause = new ImageIcon(ImageIO.read(is));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		labelPlay = new JLabel(iconPlay);
		labelPause = new JLabel(iconPause);
		
		
		tableModel = new TableModel(new Object[][]{}, new String[]{COLUMN_PLAYPAUSE, COLUMN_PATH});
		table = new JTable(tableModel){
			@Override
			public TableCellEditor getCellEditor(int row, int column){
				if(column < 2){
					return super.getCellEditor();
				}
				else{
					AnnotationAttribute<?> att = getAttributeFromColumn(column);
					if(att instanceof AnnotationNominalAttribute){
						JComboBox<Object> comboBox = new JComboBox<Object>(((AnnotationNominalAttribute) att).getAllowedValues().toArray());
						JTextField textField = new JTextField();
						comboBox.addKeyListener(new KeyListener() {
							
							@Override
							public void keyTyped(KeyEvent e) { }
							
							@Override
							public void keyReleased(KeyEvent e) { }
							
							@Override
							public void keyPressed(KeyEvent e) {
								if(e.getKeyCode() == KeyEvent.VK_ENTER){
									table.getCellEditor().stopCellEditing();
									if(table.getSelectedRowCount() == 1){
										int newRow = 0;
										if(row < table.getRowCount() - 1){
											newRow = row + 1;
										}
										table.setRowSelectionInterval(newRow, newRow);
									}
								}
							}
						});
						DefaultCellEditor cellEditor =  new DefaultCellEditor(textField){
							@Override
							public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
								JTextField comp = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
								comboBox.setSelectedItem(comp.getText());
								comboBox.grabFocus();
								return comboBox;
							}
							
							@Override
							public boolean stopCellEditing(){
								String selectedItem = comboBox.getSelectedItem().toString();
								if(selectedItem.equals("?")){
									selectedItem = "";
								}
								textField.setText(selectedItem + "");
								setValueForSelectedRowsInColumn(selectedItem, column);
								return super.stopCellEditing();
							}
						};
						return cellEditor;
					}
					else if(att instanceof AnnotationNumericAttribute){
						JTextField textField = new JTextField();

						return new DefaultCellEditor(textField){
							@Override
							public boolean stopCellEditing(){
								String text = textField.getText();
								text = text.replaceAll("[^0-9.]", "");
								text = text.replaceAll("\\.", " ");
								if(text.contains(" ")){
									text = text.substring(0, text.lastIndexOf(' ')) + "." + text.substring(text.lastIndexOf(' ') + 1);
									text = text.replaceAll(" ", "");
								}
								textField.setText(text);
								setValueForSelectedRowsInColumn(text, column);
								return super.stopCellEditing();
							}
						};
					}
					else if(att instanceof AnnotationStringAttribute){
						JTextField textField = new JTextField();

						return new DefaultCellEditor(textField){
							@Override
							public boolean stopCellEditing(){
								String text = textField.getText().replaceAll("'", "");
								textField.setText(text);
								setValueForSelectedRowsInColumn(text, column);
								return super.stopCellEditing();
							}
						};
					}
					return null;
				}
			}
			
			@Override
			public void repaint(){
				findCommonPathPrefix();
				super.repaint();
			}
		};
		table.setAutoCreateColumnsFromModel(false);
		
		/*
		 *  Configure the column containing the play/ pause icons 
		 */
		table.getColumn(COLUMN_PLAYPAUSE).setCellRenderer(new DefaultTableCellRenderer(){
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
				      boolean isSelected, boolean hasFocus, int row, int column) {
				return (!annotationController.isMusicPaused() && isMusicFromRowLoaded(row))? labelPause: labelPlay;
			}
		});
		table.getColumn(COLUMN_PLAYPAUSE).setCellEditor(new DefaultCellEditor(new JTextField()){

			@Override
			public boolean isCellEditable(EventObject anEvent) {
				return false;
			}
			
		});
		table.getColumn(COLUMN_PLAYPAUSE).setMaxWidth(iconPause.getIconWidth());
		
		
		table.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()){
					showPopupMenu(e);
				}}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()){
					showPopupMenu(e);
				}
			}
			
			public void showPopupMenu(MouseEvent e){
				JPopupMenu popupMenu = new JPopupMenu();
				
				int row = table.rowAtPoint(e.getPoint());
				int column = table.columnAtPoint(e.getPoint());
				String columnName = "value";
				if(column >= 2){
					columnName = table.getColumnName(column);
				}
				JLabel title = new JLabel("Add " + columnName + " for all selected tracks");
				popupMenu.add(title);
				
				JTextField editorTextField = new JTextField("");
				JMenuItem applyItem = new JMenuItem("Apply");
				applyItem.addActionListener(evt -> {
					TableCellEditor editor = table.getCellEditor(row, column);
					editor.getTableCellEditorComponent(table, editorTextField.getText(), true, row, column);
					editor.stopCellEditing();
				});	
				
				AnnotationAttribute<?> att = getAttributeFromColumn(column);
				if(att != null && att.getType() == AnnotationAttributeType.NOMINAL){
					DefaultListModel<String> allowedValues = ((AnnotationNominalAttribute) att).getAllowedValues();
					for(int i = 0; i < allowedValues.size(); i++){
						String allowedValue = allowedValues.getElementAt(i);
						JMenuItem allowedValueItem = new JMenuItem(allowedValue);
						allowedValueItem.addActionListener(evt -> {
							TableCellEditor editor = table.getCellEditor(row, column);
							editor.getTableCellEditorComponent(table, allowedValue, true, row, column);
							editor.stopCellEditing();
						});
						allowedValueItem.setEnabled(table.getSelectedRowCount() > 0);
						popupMenu.add(allowedValueItem);
					}
				}
				else{
					popupMenu.add(editorTextField);
					popupMenu.add(applyItem);
				}
				
				
				JMenuItem cancelItem = new JMenuItem("Cancel");
				popupMenu.add(cancelItem);
				
				if(table.getSelectedRowCount() == 0 || column < 2){
					title.setEnabled(false);
					applyItem.setEnabled(false);
					cancelItem.setEnabled(false);
					editorTextField.setEnabled(false);
				}
				
				
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int column = table.columnAtPoint(e.getPoint());
				if(row >= 0 && column == 0){
					boolean musicLoaded = isMusicFromRowLoaded(row);
					if(!musicLoaded){
						annotationController.loadMusic((String) table.getValueAt(row, 1));
						ReadOnlyObjectProperty<Status> statusProperty = annotationController.getMusicPlayerStatusProperty();
						statusProperty.addListener(new ChangeListener<Status>() {

							@Override
							public void changed(ObservableValue<? extends Status> observable, Status oldValue, Status newValue) {
								if(newValue == Status.PAUSED){
									annotationController.playMusic();
									statusProperty.removeListener(this);
									statusProperty.addListener(new ChangeListener<Status>() {

										@Override
										public void changed(ObservableValue<? extends Status> observable, Status oldValue, Status newValue) {
											if(newValue == Status.PLAYING){
												repaint();
												statusProperty.removeListener(this);
											}
										}
									});
								
									
								}
							}
						});
					}
					else{
						if(!annotationController.isMusicPaused()){
							annotationController.pauseMusic();
						}
						else{
							annotationController.playMusic();
						}
					}
					repaint();
				}
			}
		});
		
		/*
		 * Listener for removing a column
		 */
		table.getTableHeader().addMouseListener(new MouseListener() {
					
					@Override
					public void mouseReleased(MouseEvent e) {
						if(e.isPopupTrigger()){
							showPopupMenu(e);
						}}
					
					@Override
					public void mousePressed(MouseEvent e) {
						if(e.isPopupTrigger()){
							showPopupMenu(e);
						}
					}
					
					public void showPopupMenu(MouseEvent e){
						int column = table.columnAtPoint(e.getPoint());
						if(column >= 2){
							JPopupMenu popupMenu = new JPopupMenu();
							
							JMenuItem deleteItem = new JMenuItem("Delete");
							deleteItem.addActionListener(evt -> {
								if(JOptionPane.YES_OPTION == 
										JOptionPane.showConfirmDialog(null,
										"Do you really want to delete the column '" + table.getColumnName(column) + "' and its content?",
										"Remove Column", 
										JOptionPane.YES_NO_OPTION, 
										JOptionPane.QUESTION_MESSAGE)){
									removeColumn(column);
									
								}
							});
							JMenuItem cancelItem = new JMenuItem("Cancel");
							popupMenu.add(deleteItem);
							popupMenu.add(cancelItem);
							
							popupMenu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
					
					

					@Override
					public void mouseExited(MouseEvent e) {}
					
					@Override
					public void mouseEntered(MouseEvent e) {}
					
					@Override
					public void mouseClicked(MouseEvent e) {}
		});
					
		
		
		/*
		 *  Configure the column containing the path
		 */
		showAbsolutePath(false);
		
		table.getTableHeader().setReorderingAllowed(false);
		JScrollPane scrollPane = new JScrollPane(table);
		
		
		
		this.add(scrollPane);
	}
	
	
	
	public void showAbsolutePath(boolean bool){
		if(bool){
			table.getColumn(COLUMN_PATH).setCellRenderer(new DefaultTableCellRenderer());
		}
		else{
			table.getColumn(COLUMN_PATH).setCellRenderer(new DefaultTableCellRenderer(){
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value,
					      boolean isSelected, boolean hasFocus, int row, int column) {
					JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					String text = comp.getText();
					// Display only the name of the file, not the whole path
					if(text.length() > commonPathPrefix.length()) {
						text = text.substring(commonPathPrefix.length());
					}
					comp.setText(text);
					return comp;
				}
			});
		}
		table.repaint();
	}
	
	public void setValueForSelectedRowsInColumn(Object value, int column){
		for(int row: table.getSelectedRows()){
			table.setValueAt(value, row, column);
		}
	}
	
	public ListSelectionModel getSelectionModel(){
		return table.getSelectionModel();
	}
	
	public void findCommonPathPrefix(){
		if(table == null || table.getRowCount() == 0){
			commonPathPrefix = "";
		}
		else{
			commonPathPrefix = (String) table.getValueAt(0, 1);
			if(commonPathPrefix.contains(File.separator)){
				commonPathPrefix = commonPathPrefix.substring(0, commonPathPrefix.lastIndexOf(File.separator) + 1);
			}
			for(int i = 1; i < table.getRowCount() && commonPathPrefix.length() > 0; i++){
				String currentRow = ((String) table.getValueAt(i, 1));
				while(!currentRow.startsWith(commonPathPrefix)){
					commonPathPrefix = commonPathPrefix.substring(0, commonPathPrefix.length() - 1);
				}
			}
			if(commonPathPrefix.lastIndexOf(File.separator) > 0){
				commonPathPrefix = commonPathPrefix.substring(0, commonPathPrefix.lastIndexOf(File.separator) + 1);
			}
		}
	}
	
	public void addTrack(String path){
		if(!containsInColumn(1, path)){
			tableModel.addRow(new Object[]{null, path});
		}
	}
	
	public void removeSelectedRows(){
		int[] selectedRows = table.getSelectedRows();
		for(int i = selectedRows.length - 1; i >= 0; i--){
			tableModel.removeRow(table.convertRowIndexToModel(selectedRows[i]));
		}
	}
	
	private boolean containsInColumn(int column, Object obj){
		for(int row = 0; row < table.getRowCount(); row ++){
			if(table.getValueAt(row, column).equals(obj)){
				return true;
			}
		}
		return false;
	}
	
	public AnnotationAttribute<?> getAttributeFromColumn(int column){
		return column < 2? null: (AnnotationAttribute<?>) table.getColumnModel().getColumn(column).getHeaderValue();
	}
	
	
	private boolean isMusicFromRowLoaded(int row){
		return annotationController.getMusicAbsoulutePath().equals(table.getValueAt(row, 1));
	}
	
	public void addAttribute(AnnotationAttribute<?> att){
		tableModel.addColumn(att.toString());
		TableColumn column = new TableColumn(table.getColumnCount());
		column.setHeaderValue(att);
		table.addColumn(column);
	}
	
	public DefaultTableModel getTableModel(){
		return tableModel;
	}
	
	public void addRow(Object[] rowData){
		if(rowData[0] != null && rowData.length < table.getColumnModel().getColumnCount()){
			Object[] newRowData = new Object[rowData.length + 1];
			newRowData[0] = null;
			for(int i = 0; i < rowData.length; i++){
				newRowData[i + 1] = rowData[i];
			}
			tableModel.addRow(newRowData);
		}
		else{
			tableModel.addRow(rowData);
		}
	}
	
	public TableColumnModel getColumnModel(){
		return table.getColumnModel();
	}
	
	
	public void setRowFilter(RowFilter<DefaultTableModel, Integer> filter) {
		TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<DefaultTableModel>(tableModel);
		rowSorter.setRowFilter(filter);
		table.setRowSorter(rowSorter);
	}
	
	public void clearAnnotation(){
		for(int column = table.getColumnCount() - 1; column >= 2; column--){
			table.removeColumn(table.getColumnModel().getColumn(column));
		}
		for(int row = table.getRowCount() - 1; row >= 0; row--){
			tableModel.removeRow(row);
		}
	}
	
	private void removeColumn(int viewColumnIndex) {
		for(int i = viewColumnIndex + 1; i < table.getColumnCount(); i++){
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setModelIndex(column.getModelIndex() - 1);
		}
		table.removeColumn(table.getColumnModel().getColumn(viewColumnIndex));
		tableModel.removeColumn(viewColumnIndex);
	}
	
	private class TableModel extends DefaultTableModel{
		
		public TableModel (Object[][] data, Object[] columnNames){
			super(data, columnNames);
		}
		
		public TableModel (Vector data, Vector columnNames) {
			super(data, columnNames);
		}
		
		public void removeColumn(int i){
			int columnCount = this.getColumnCount();
			for(Object obj: dataVector){
				((Vector<?>) obj).removeElementAt(i);
			}
			columnIdentifiers.removeElementAt(i);
			
			this.fireTableStructureChanged();
		}
	}



	
	
	
	
}
