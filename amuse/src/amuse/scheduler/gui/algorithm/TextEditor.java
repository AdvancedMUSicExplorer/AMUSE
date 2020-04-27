package amuse.scheduler.gui.algorithm;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

public class TextEditor implements ActionListener{
	
    private JTextArea textArea;
    private JDialog dialog;
    private File currentFile;
    private boolean changed;
  
    public TextEditor(File file) {
    	
        dialog = new JDialog((Frame)null, "Editor", true) {
        	@Override
        	public void dispose() {
        		// check if the changes have been saved before closing the window
    			if(!changed || JOptionPane.showConfirmDialog(dialog, "Do you want to close the editor without saving your changes?", "Unsaved Changes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
    				changed = false;
    				super.dispose();
    			}
        	}
        };
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        textArea = new JTextArea();
        textArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				changed = true;
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changed = true;
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				changed = true;
			}
        	
        });
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        JMenuBar menuBar = new JMenuBar();
  
        // Create menu items 
        JMenuItem newFile = new JMenuItem("New"); 
        JMenuItem openFile = new JMenuItem("Open"); 
        JMenuItem saveFile = new JMenuItem("Save");
  
        // Add action listener 
        newFile.addActionListener(this); 
        openFile.addActionListener(this); 
        saveFile.addActionListener(this);
  
        menuBar.add(newFile); 
        menuBar.add(openFile); 
        menuBar.add(saveFile);
        
        if(file != null) {
	        try {
				open(file);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(dialog, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
        }
        currentFile = file;
  
        changed = false;
        
        dialog.setJMenuBar(menuBar); 
        dialog.add(scrollPane); 
        dialog.setSize(500, 500);
        dialog.setVisible(true);
    }
  
    // If a button is pressed 
    public void actionPerformed(ActionEvent e) 
    { 
        String s = e.getActionCommand(); 
  
        if (s.equals("Save")) { 
            // Create an object of JFileChooser class 
        	JFileChooser fileChooser;
            if(currentFile == null) {
            	fileChooser = new JFileChooser();
            } else {
            	fileChooser = new JFileChooser(currentFile.getParent());
            }
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            	
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                
                // check if the file already exists and if it should be overwritten
                if(!file.exists() || JOptionPane.showConfirmDialog(dialog, file.getName() + " already exists!\\nDo you want to overwrite this file?", "File already exists!", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
	                try { 
	                    save(file);
	                } catch (Exception ex) { 
	                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
	                }
                }
            }
        } else if (s.equals("Open")) { 
            // Create an object of JFileChooser class
            JFileChooser fileChooser;
            if(currentFile == null) {
            	fileChooser = new JFileChooser();
            } else {
            	fileChooser = new JFileChooser(currentFile.getParent());
            }
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  
            // If the user selects a file 
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
                // Set the label to the path of the selected directory 
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath()); 
  
                try { 
                	open(file);
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(dialog, ex.getMessage()); 
                } 
            }
        } 
        else if (s.equals("New")) { 
            textArea.setText("");
            currentFile = null;
            changed = true;
        }
    }
    
    private void open(File file) throws IOException {
    	String nextLine = "";
        String text = ""; 

        FileReader fileReader = new FileReader(file); 
        BufferedReader bufferedReader = new BufferedReader(fileReader); 

        text = bufferedReader.readLine(); 

        // Take the input from the file 
        while ((nextLine = bufferedReader.readLine()) != null) { 
            text = text + "\n" + nextLine; 
        } 

        // Set the current file
        currentFile = file;
        
        // Set the text 
        textArea.setText(text);
        changed = false;
    }
    
    private void save(File file) throws IOException {
    	FileWriter fileWriter = new FileWriter(file, false); 
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter); 

        bufferedWriter.write(textArea.getText()); 

        bufferedWriter.flush();
        bufferedWriter.close();
        
        currentFile = file;
        changed = false;
    }
    
    public File getFile() {
    	return currentFile;
    }
}
