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
 * Creation date: 24.08.2008
 */
package amuse.scheduler.gui.settings.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.preferences.PreferenceChangeListener;
import amuse.scheduler.gui.views.WizardView;

/**
 * @author Clemens Waeltken
 *
 */
public class PathSelectionPanel extends EditableAmuseSettingBody {

    private final JTextField textField;
    private JButton browseButton;
    private String savedPath;
    private final JFileChooser jFileChooserSelect;
    private final KeysStringValue key;
    private final Color invalidColor = WizardView.INVALID_COLOR;
    private final Color validColor = WizardView.VALID_COLOR;
	private String toolTip = "";
	
	private String basePath;
	private String relativePath;
	private boolean choosePathRelativeToBasePath;
	
	JLabel jLabel;

    /**
     * @param label
     * @param stringKey
     */
    public PathSelectionPanel(String label, KeysStringValue stringKey) {
    	this(label, stringKey, (String)null);
    }
    
    public PathSelectionPanel(String label, KeysStringValue stringKey, String relativePath) {
    	this.relativePath = relativePath;
    	this.toolTip = KeysStringValue.getCommentFor(stringKey.toString());
        // Copy values.
        this.key = stringKey;
        // Set Layout.
        panel.setLayout(new BorderLayout());
        panel.setToolTipText(toolTip);
        // Load previous path value.
        savedPath = AmusePreferences.get(this.key);
        textField = new JTextField(savedPath);
        textField.setToolTipText(toolTip);
        textField.addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                notifyListenersAndUpdateColor(hasChanges());
            }
        });
        // Setup the FileChooser.
        jFileChooserSelect = new JFileChooser();
        jFileChooserSelect.setDialogTitle("Select " + label);
        jFileChooserSelect.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        // Add Components.
        jLabel = new JLabel(label + ": ");
        panel.add(jLabel, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(getBrowseButton(), BorderLayout.EAST);
        // Listen to changes.
        AmusePreferences.addPreferenceChangeListener(new PreferenceChangeListener() {

            public void preferenceChange() {
                if (!AmusePreferences.get(key).equals(savedPath)) {
                    savedPath = AmusePreferences.get(key);
                    textField.setText(savedPath);
                }
            }
        });
        setColor();
    }

    public PathSelectionPanel(String label, KeysStringValue stringKey, Boolean filesOnly) {
        this(label, stringKey);
        if (filesOnly) {
            jFileChooserSelect.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
    }

    private Component getBrowseButton() {
        if (browseButton == null) {
            browseButton = new JButton("Browse");
            browseButton.setToolTipText(toolTip);
            browseButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    browseButtonKlicked();
                }
            });
        }
        return browseButton;
    }

    public boolean hasChanges() {
        return (savedPath.compareTo(textField.getText()) != 0);
    }

    private void browseButtonKlicked() {
        int returnValue = jFileChooserSelect.showDialog(panel,
                "Select");
        if (returnValue == javax.swing.JFileChooser.APPROVE_OPTION) {
            textField.setText(jFileChooserSelect.getSelectedFile().getAbsolutePath());
            notifyListenersAndUpdateColor(hasChanges());
        }
    }

    private void notifyListenersAndUpdateColor(Boolean bool) {
        setColor();
        notifyListeners(bool);
    }

    @Override
    public void saveChanges() {
    	if(choosePathRelativeToBasePath) {
    		this.textField.setText(basePath + File.separator + relativePath);
        	File baseFolder = new File(basePath);
        	File folderToCreate = new File(this.textField.getText());
        	if(baseFolder.exists() && !folderToCreate.exists()) {
        		folderToCreate.mkdirs();
        	}
        }
 
        if (hasChanges()) {
            savedPath = textField.getText();
            AmusePreferences.put(key, savedPath);
        }
        
        notifyListenersAndUpdateColor(false);
    }

    /* (non-Javadoc)
     * @see amuse.scheduler.gui.settings.EditableAmuseSettingInterface#discardChanges()
     */
    @Override
    public void discardChanges() {
        if (hasChanges()) {
            textField.setText(savedPath);
        }
        notifyListenersAndUpdateColor(false);
    }

    /**
     * This method is used to color the JTextField of this Path selection Panel according to validity of the current value.
     */
    private void setColor() {
        if(key.isValid(textField.getText())) {
            textField.setForeground(validColor);
        } else {
            textField.setForeground(invalidColor);
        }
    }
    
    public String getText()	{
    	return this.textField.getText();
    }
    
    public String getSavedPath() {
    	return savedPath;
    }
    
    public String getRelativePath() {
    	return relativePath;
    }
    
    public void setBasePath(String basePath) {
    	this.basePath = basePath;
    }

	public void setEnabled(boolean enabled) {
		this.textField.setEnabled(enabled);
		this.browseButton.setEnabled(enabled);
	}
	
	public void setVisible(boolean visible) {
		this.textField.setVisible(visible);
		this.browseButton.setVisible(visible);
		this.jLabel.setVisible(visible);
	}
	
	public void setChoosePathRelativeToBasePath(boolean createFolder) {
		this.choosePathRelativeToBasePath = createFolder;
	}
}
