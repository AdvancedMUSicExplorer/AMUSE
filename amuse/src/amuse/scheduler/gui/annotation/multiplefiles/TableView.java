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
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import amuse.scheduler.gui.annotation.multiplefiles.attribute.AnnotationAttribute;
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
	DefaultTableModel tableModel;
	final JLabel labelPlay, labelPause;
	MultipleFilesAnnotationController annotationController;
	final String COLUMN_PLAYPAUSE = " ";
	final String COLUMN_PATH = "Path";
	
	public TableView(MultipleFilesAnnotationController annotationController){
		super();
		this.annotationController = annotationController;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
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
		
		
		tableModel = new DefaultTableModel(new Object[][]{}, new String[]{COLUMN_PLAYPAUSE, COLUMN_PATH});
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
								Object selectedItem = comboBox.getSelectedItem();
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
			
			public void setValueForSelectedRowsInColumn(Object value, int column){
				for(int row: table.getSelectedRows()){
					table.setValueAt(value, row, column);
				}
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
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
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
		 *  Configure the column containing the path
		 */
		table.getColumn(COLUMN_PATH).setCellRenderer(new DefaultTableCellRenderer(){
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
				      boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				String text = comp.getText();
				// Display only the name of the file, not the whole path
				if(text.contains(File.separator)){
					text = text.substring(text.lastIndexOf(File.separator) + 1);
					comp.setText(text);
				}
				return comp;
			}
		});
		
		
		
		JScrollPane scrollPane = new JScrollPane(table);
		
		
		
		this.add(scrollPane);
	}
	
	public void addSong(String path){
		if(!containsInColumn(1, path)){
			tableModel.addRow(new Object[]{null, path});
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
		if(rowData.length < tableModel.getColumnCount()){
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



	
	
	
	
}
