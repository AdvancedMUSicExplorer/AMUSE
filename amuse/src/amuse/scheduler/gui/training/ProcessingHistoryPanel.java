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
 * Creation date: 23.08.2009
 */

package amuse.scheduler.gui.training;

import amuse.data.ProcessingHistory;
import amuse.util.AmuseLogger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Level;

import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

/**
 * This JPanel is used to display Processing History and returns the selected or typed in values as String.
 * It also notifies the user if any problems with the Processing History occur.
 * @author Clemens Wältken
 */
public class ProcessingHistoryPanel extends JPanel {

    private JLabel comboBoxLabel = new JLabel("Previous Processings:");
    private JComboBox comboBox = new JComboBox();
    private JLabel textFieldLabel = new JLabel("Processing Steps:");
    private JTextField textField = new JTextField(10);
    private JLabel attributesToIgnoreLabel = new JLabel("Attributes to ignore:");
    private JTextField attributesToIgnoreTextField = new JTextField(10);
    private TitledBorder title = new TitledBorder("Select Processing Model");

    public ProcessingHistoryPanel () {
        super(new MigLayout("fillx"));
        this.setBorder(title);
        this.add(comboBoxLabel, "pushx, wrap");
        this.add(comboBox, "pushx, wrap");
        this.add(textFieldLabel, "pushx, wrap");
        this.add(textField, "growx, wrap");
        this.add(attributesToIgnoreLabel, "pushx, wrap");
        this.add(attributesToIgnoreTextField, "growx, wrap");
        try {
			DefaultComboBoxModel model = new DefaultComboBoxModel(ProcessingHistory.getHistoryList());
            comboBox.setModel(model);
            Object item = model.getSelectedItem();
            if (item != null)
                textField.setText(item.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Unable to load Processing History: \"" + ex.getLocalizedMessage() + "\"", "Unable to load Processing History!", JOptionPane.ERROR_MESSAGE);
        }
        comboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                textField.setText(comboBox.getSelectedItem().toString());
            }
        });
    }

    public String getProcessingHistoryString() {
        return textField.getText();
    }
    
    public List<Integer> getAttributesToIgnore(){
    	String attributesToIgnoreString = attributesToIgnoreTextField.getText();
		attributesToIgnoreString = attributesToIgnoreString.replaceAll("\\[", "").replaceAll("\\]", "");
		String[] attributesToIgnoreStringArray = attributesToIgnoreString.split("\\s*,\\s*");
		List<Integer> attributesToIgnore = new ArrayList<Integer>();
		try {
			for(String str : attributesToIgnoreStringArray) {
				if(!str.equals("")) {
					attributesToIgnore.add(Integer.parseInt(str));
				}
			}
		} catch(NumberFormatException e) {
			AmuseLogger.write(this.getClass().getName(), Level.WARN,
					"The attributes to ignore were not properly specified. All features will be used for training.");
			attributesToIgnore = new ArrayList<Integer>();
		}
		return attributesToIgnore;
    }

    public void setProcessingModelString(String value) {
		textField.setText(value);
		comboBox.setSelectedItem(value);
    }
    
    public void setAttributesToIgnore(List<Integer> attributesToIgnore) {
    	attributesToIgnoreTextField.setText(attributesToIgnore.toString());
    }
    
    public JTextField getAttributesToIgnoreTextField() {
    	return attributesToIgnoreTextField;
    }
}
