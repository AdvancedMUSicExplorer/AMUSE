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
 * Creation date: 21.07.2009
 */
package amuse.scheduler.gui.training;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;

/**
 * @author Clemens Waeltken
 *
 */
public class CategorySelectionPanel extends JPanel {

	private JComboBox comboBox = new JComboBox();
	private CategoryComboBoxModel model;

	public CategorySelectionPanel() {
		super(new MigLayout("fillx"));
		this.setBorder(new TitledBorder("Select Annotation"));
		this.add(new JLabel("Category:"));
		this.add(comboBox, "pushx, gap rel, wrap");
		try {
			model = new CategoryComboBoxModel();
			comboBox.setModel(model);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Unable to load Categories: \""+ ex.getLocalizedMessage() + "\"", "Unable To Load Categories!", JOptionPane.ERROR_MESSAGE);
		}
	}

	public int getSelectedCategoryID() {
		return model.getSelectedID();
	}

	void setSelectedCategory(int value) {
		model.setSelectedCategory(value + "");
	}

	public void setOptional(boolean b) {
		if (b) {
			model.addDontUseEntry();
		} else {
			try {
				model = new CategoryComboBoxModel();
			} catch (IOException ex) {
				Logger.getLogger(CategorySelectionPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
			comboBox.setModel(model);
		}
	}

	public void setCategory(int id) {
		setSelectedCategory(id);
	}

	private class CategoryComboBoxModel extends DefaultComboBoxModel {

		private static final long serialVersionUID = -680154994516168686L;
		private File file;
		private String idStr = "Id";
		private String fileNameStr = "Path";
		private String categoryNameStr = "CategoryName";
		private Category selected;
		private List<Category> categories;

		private CategoryComboBoxModel() throws IOException {
			file = new File(AmusePreferences.getMultipleTracksAnnotationTablePath());
			DataSetAbstract categorySet = new ArffDataSet(file);
			List<String> names = categorySet.getAttributeNames();
			if (!names.contains(idStr) || !names.contains(fileNameStr) || !names.contains(categoryNameStr)) {
				throw new IOException("Missing Attribute!");
			}
			if (!(categorySet.getAttribute(idStr) instanceof NumericAttribute)) {
				throw new IOException(idStr + "-Attribute not Numeric!");
			}
			if (!(categorySet.getAttribute(fileNameStr) instanceof StringAttribute)) {
				throw new IOException(fileNameStr + "-Attribute not String!");
			}
			if (!(categorySet.getAttribute(categoryNameStr) instanceof StringAttribute)) {
				throw new IOException(categoryNameStr + "-Attribute not String!");
			}
			NumericAttribute idAttr = (NumericAttribute) categorySet.getAttribute(idStr);
			StringAttribute fileNameAttr = (StringAttribute) categorySet.getAttribute(fileNameStr);
			StringAttribute categoryAttr = (StringAttribute) categorySet.getAttribute(categoryNameStr);
			// Create Model:
			categories = new ArrayList<Category>();
			for (int i = 0; i < categorySet.getValueCount(); i++) {
				// Create ProcessingAlgorithm Object:
				Category category = new Category(idAttr.getValueAt(i).intValue(), fileNameAttr.getValueAt(i), categoryAttr.getValueAt(i));
				categories.add(category);
			}
			Collections.sort(categories);
			for (Category cat : categories) {
				super.addElement(cat);
			}
		}

		@Override
		public void setSelectedItem(Object item) {
			if(item instanceof Category) {
				selected = (Category) item;
			    fireContentsChanged(this, -1, -1);
			} else {
				throw new RuntimeException("Only Categories Supported!");
			}
		}

		@Override
		public Object getSelectedItem() {
			return selected;
		}

		private int getSelectedID() {
			return selected.getID();
		}

		private void setSelectedCategory(String value) {
			int id = new Integer(value);
			setSelectedCategory(id);
		}

		private void setSelectedCategory(int id) {
			for (Category cat:categories) {
				if (cat.getID() == id) {
					setSelectedItem(cat);
					return;
				}
			}
		}

		private void addDontUseEntry() {
			Category dontUseCategory = new Category(-1, "-1", "Don't use");
			super.addElement(dontUseCategory);
			this.setSelectedItem(dontUseCategory);
		}

		protected class Category implements Comparable<Category> {

			final int id;
			final String fileName;
			final String categoryName;

			public Category(int id, String fileName, String categoryName) {
				this.id = id;
				this.fileName = fileName;
				this.categoryName = categoryName;
			}

			@Override
			public String toString() {
				return this.categoryName;
			}

			/* (non-Javadoc)
			 * @see java.lang.Comparable#compareTo(java.lang.Object)
			 */
			@Override
			public int compareTo(Category o) {
				return toString().toLowerCase().compareTo(o.toString().toLowerCase());
			}

			public int getID() {
				return this.id;
			}
		}
	}
}
