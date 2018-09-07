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
 * Creation date: 07.08.2009
 */
package amuse.scheduler.gui.validation;

import javax.swing.JComponent;

import amuse.data.GroundTruthSourceType;
import amuse.scheduler.gui.algorithm.AlgorithmConfigurationFacade;
import amuse.scheduler.gui.training.TrainingView;

/**
 * @author Clemens Waeltken
 *
 */
public class ValidationView {
	
	private TrainingView trainingView;
        private static final String ToolTipValidationAlgorithms = "Select Validation Method.";

	public ValidationView(AlgorithmConfigurationFacade validationAlgorithms) {
		this.trainingView = new TrainingView( "Setup Validation" );
                validationAlgorithms.setToolTip(ToolTipValidationAlgorithms);
		trainingView.addRightSide(validationAlgorithms.getPrameterPanel());
		trainingView.addLineInView(validationAlgorithms.getAlgorithmSelectionComboBox());
	}
	
	public JComponent getView() {
		return trainingView.getView();
	}
	
	/**
	 * @return
	 */
	public String getProcessingModelString() {
		return trainingView.getProcessingModelString();
	}
	
	/**
	 * Sets the text for the processing steps description
	 * @param processingSteps - The String that should be put in the textbox
	 */
	public void setProcessingModelString(String processingSteps){
		trainingView.setProcessingModelString(processingSteps);
	}
	
	/**
	 * @return
	 */
    public String getClassifierAlgorithmStr() {
        return trainingView.getSelectedTrainingAlgorithmStr();
    }

    public void setSelecstedCategoryID(int value) {
        //trainingView.setSelectedCategoryID(value);
    }

    public void setClassifierAlgorithm(String string) {
        trainingView.setSelectedTrainingAlgorithm(string);
    }
    
    public void setGroundTruthSourceType(GroundTruthSourceType type){
    	trainingView.setGroundTruthSourceType(type);
	}
	
	public GroundTruthSourceType getGroundTruthSourceType(){
		return trainingView.getGroundTruthSourceType();
	}
	
	/*public String getSelectedReadyInputFile(){
		return trainingView.getReadyInputSelectionPanel().getSelectedReadyInputFile();
	}

	public void setSelectedReadyInputFile(String groundTruthSource) {
		trainingView.getReadyInputSelectionPanel().setSelectedReadyInputFile(groundTruthSource);
	}*/

	public String getPathToOutputModel() {
		// TODO Auto-generated method stub
		return "/home/heerde/workspace/Music/Models/randomforest/model.mod";
	}
	
	public String getGroundTruthSource(){
		return trainingView.getGroundTruthSource();
	}

	public void setGroundTruthSource(String groundTruthSource) {
		trainingView.setGroundTruthSource(groundTruthSource);
	}

}
