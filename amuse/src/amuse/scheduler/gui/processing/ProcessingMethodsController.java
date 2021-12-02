/*
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
 * Creation date: 11.03.2009
 */
package amuse.scheduler.gui.processing;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import amuse.nodes.processor.ProcessingConfiguration.Unit;
import amuse.scheduler.gui.algorithm.AlgorithmInterface;
import amuse.scheduler.gui.algorithm.AlgorithmView;
import amuse.scheduler.gui.controller.ProcessingController;

/**
 * Controller for the Processing Methods Panel.
 * @author Clemens Waeltken
 */
public class ProcessingMethodsController {

    private ProcessingMethodsListModel pcmListModel;
    private ProcessingMethodsPanel view;
    private MatrixToVectorMethodModel mtvModel;
    private ProcessingController processingController;

    public ProcessingMethodsController(ProcessingController processingController, ProcessingMethodsListModel pcmListModel, MatrixToVectorMethodModel mtvModel) {
        this.pcmListModel = pcmListModel;
        this.mtvModel = mtvModel;
        this.view = new ProcessingMethodsPanel(pcmListModel, mtvModel, this);
        this.processingController = processingController;
    }

    public JPanel getView() {
        return view;
    }

    void addMethod(Object selectedItem, int selectedIndex) {
        if (selectedIndex == -1) {
            pcmListModel.addAlgorithm(pcmListModel.getSize(), selectedItem);
        } else {
            pcmListModel.addAlgorithm(selectedIndex + 1, selectedItem);
        }
    }

    ListSelectionListener getSelectionListener() {
        return new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int index = view.getListSelectedIndex();
                if (index < 0 || index >= pcmListModel.getSize()) {
                    return;
                }
                AlgorithmInterface algorithm = (AlgorithmInterface) pcmListModel.getElementAt(index);
                JComponent methodView = createAlgorithmView(algorithm);
                view.setRightSide(methodView);
//                System.out.println(index);
            }
        };
    }

    void setupMatrixToVector() {
        JComponent methodView = createAlgorithmView((AlgorithmInterface) mtvModel.getSelectedItem());
        view.setRightSide(methodView);
    }

    private JComponent createAlgorithmView(AlgorithmInterface al) {
        return new AlgorithmView(al).getPanel();
    }

    void moveDown(int selectedIndex) {
        pcmListModel.moveDown(selectedIndex);
        if (selectedIndex < pcmListModel.getSize()) {
            view.setListSelectedIndex(selectedIndex + 1);
        }
    }

    void moveUp(int selectedIndex) {
        pcmListModel.moveUp(selectedIndex);
        if (selectedIndex > 0) {
            view.setListSelectedIndex(selectedIndex - 1);
        }
    }

    void removeMethods(Object[] selectedValues) {
        pcmListModel.removeAlgorithms(selectedValues);
        view.setRightSide(new JPanel());
    }

    void addProcessing() {
        processingController.addProcessing();
    }

    /**
     * @return the selected classification window size.
     */
    public int getClassificationWindowSize() {
        int ret = new Integer(view.getClassificationWindowSizeStr());
        return ret;
    }

    /**
     * @return the selected classificaiton window step size.
     */
    public int getClassificationWindowStepSize() {
        int ret = new Integer(view.getStepSizeStr());
        return ret;
    }

    /**
     * @return the configured processing reduction steps as String.
     */
    public String getReductionSteps() {
        return pcmListModel.getReductionStepsString();
    }

    /**
     * @return
     */
    public String getFeatureDescription() {
        return view.getModelDescription();
    }

    /**
     * @return the configured Matrix to Vector conversion step as String.
     */
    public String getConversionStep() {
        return mtvModel.getConversionStepStr();
    }

    void saveButtonClicked() {
        processingController.saveButtonClicked();
    }

    /**
     *
     * @return the selected unit for classification window size and classification window step size.
     */
    public Unit getUnit() {
        return view.getUnit();
    }

    public void setUnit(String value) {
        view.setUnit(value);
    }

    public void setClassificationWindowSize(int value) {
        view.setClassificationWindowSizeStr(value);
    }

    public void setStepSize(int value) {
        view.setClassificationWindowStepSizeStr(value);
    }

    public void setMatrixToVector(String value) {
        mtvModel.setMethod(value);
    }

    public void setReductionSteps(String reductionSteps) {
        pcmListModel.setReductionSteps(reductionSteps);
    }

    public String getOptionalModelStr() {
	return view.getOptionalModelStr();
    }

    public void setOptionalModelStr(String str) {
	view.setOptionalModelStr(str);
    }
}
