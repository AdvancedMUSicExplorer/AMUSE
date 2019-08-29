package amuse.scheduler.gui.training;

import amuse.data.ModelType;
import amuse.data.ModelType.RelationshipType;
import amuse.data.ModelType.LabelType;
import amuse.data.ModelType.MethodType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public class ModelTypePanel extends JPanel {
	private JLabel relationshipTypeLabel = new JLabel("Relationship Type:");
	private JLabel labelTypeLabel = new JLabel("Label Type:");
	private JLabel methodTypeLabel = new JLabel("Method Type:");
	private JComboBox relationshipComboBox = new JComboBox();
	private JComboBox labelComboBox = new JComboBox();
	private JComboBox methodComboBox = new JComboBox();
	private TitledBorder title = new TitledBorder("Select Model Type");
	private List<ModelTypeListener> listeners = new ArrayList<ModelTypeListener>();
	
	public ModelTypePanel() {
		super(new MigLayout("fillx"));
		this.setBorder(title);
		this.add(relationshipTypeLabel, "pushx, wrap");
		this.add(relationshipComboBox, "pushx, wrap");
		this.add(labelTypeLabel, "pushx, wrap");
		this.add(labelComboBox, "pushx, wrap");
		this.add(methodTypeLabel, "pushx, wrap");
		this.add(methodComboBox, "pushx, wrap");
		
		DefaultComboBoxModel relationshipModel = new DefaultComboBoxModel(RelationshipType.values());
		relationshipComboBox.setModel(relationshipModel);
		relationshipComboBox.setSelectedItem(RelationshipType.BINARY);
		relationshipComboBox.addActionListener(l -> {
			notifyListeners();
		});
		
		DefaultComboBoxModel labelModel = new DefaultComboBoxModel(LabelType.values());
		labelComboBox.setModel(labelModel);
		labelComboBox.setSelectedItem(LabelType.SINGLELABEL);
		labelComboBox.addActionListener(l -> {
			notifyListeners();
		});
		
		
		DefaultComboBoxModel methodModel = new DefaultComboBoxModel(new MethodType[] {MethodType.SUPERVISED});
		methodComboBox.setModel(methodModel);
		methodComboBox.setSelectedItem(MethodType.SUPERVISED);
		methodComboBox.addActionListener(l -> {
			notifyListeners();
		});
	}

	public ModelType getModelType() {
		RelationshipType relationshipType = (RelationshipType)relationshipComboBox.getSelectedItem();
		LabelType labelType = (LabelType)labelComboBox.getSelectedItem();
		MethodType methodType = (MethodType)methodComboBox.getSelectedItem();
		if(labelType.equals(LabelType.MULTICLASS) && relationshipType.equals(RelationshipType.CONTINUOUS)) {
			JOptionPane.showMessageDialog(this, "Continuos multiclass classification is not possible.", "Error", JOptionPane.ERROR_MESSAGE);
			relationshipComboBox.setSelectedItem(RelationshipType.BINARY);
			relationshipType = RelationshipType.BINARY;
		}
		ModelType modelType = null;
		try {
			modelType = new ModelType(relationshipType, labelType, methodType);
		} catch(IOException e) {}
		return modelType;
	}

	public void setModelType(ModelType modelType) {
		relationshipComboBox.setSelectedItem(modelType.getRelationshipType());
		labelComboBox.setSelectedItem(modelType.getLabelType());
		methodComboBox.setSelectedItem(modelType.getMethodType());
	}
	
	public void addModelTypeListener(ModelTypeListener modelTypeListener) {
		listeners.add(modelTypeListener);
	}
	
	private void notifyListeners() {
		for(ModelTypeListener listener : listeners) {
			listener.updateModelType((RelationshipType)relationshipComboBox.getSelectedItem(), (LabelType)labelComboBox.getSelectedItem(), (MethodType)methodComboBox.getSelectedItem());
		}
	}
}
