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
 * Creation date: 04.09.2009
 */
package amuse.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import amuse.data.io.ArffDataSet;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;

/**
 * @author Clemens Waeltken
 */
public class MeasureTable implements List<Measure>, Serializable {

    /**	For Serializable interface */
	private static final long serialVersionUID = -925369538370176875L;
	
	private final List<Measure> measures = new ArrayList<Measure>();
    
    public MeasureTable(File file) throws IOException {
        MeasureTableSet measureTableSet = new MeasureTableSet(file);
        for (int i = 0; i < measureTableSet.getValueCount(); i++) {
            int id = measureTableSet.getIdAttribute().getValueAt(i).intValue();
            String name = measureTableSet.getNameAttribute().getValueAt(i);
            String category = measureTableSet.getCategoryAttribute().getValueAt(i);
            Double optimalValue = measureTableSet.getOptimalValueAttribute().getValueAt(i);
            String measureClass = measureTableSet.getMeasureClassAttribute().getValueAt(i);
            if (!measureClass.equalsIgnoreCase("?")) {
            	Measure newMeasure = new Measure(id, name, category, optimalValue, measureClass);
            	if(measureTableSet.getCalculateForTracks().getValueAt(i).equalsIgnoreCase(new String("false"))) {
            		newMeasure.setTrackLevelSelected(false);
            	}
            	if(measureTableSet.getCalculateForWindows().getValueAt(i).equalsIgnoreCase(new String("false"))) {
            		newMeasure.setWindowLevelSelected(false);
            	}
                measures.add(newMeasure);
            }
        }
    }

    public MeasureTable() {
        
    }

    @Override
    public int size() {
        return measures.size();
    }

    @Override
    public boolean isEmpty() {
        return measures.isEmpty();
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean contains(Object o) {
        return measures.contains(o);
    }

    @Override
    public Iterator<Measure> iterator() {
        return measures.iterator();
    }

    @Override
    public Object[] toArray() {
        return measures.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return measures.toArray(a);
    }

    @Override
    public boolean add(Measure e) {
        return measures.add(e);
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean remove(Object o) {
        return measures.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return measures.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Measure> c) {
        return measures.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Measure> c) {
        return measures.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return measures.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return measures.retainAll(c);
    }

    @Override
    public void clear() {
        measures.clear();
    }

    @Override
    public Measure get(int index) {
        return measures.get(index);
    }

    @Override
    public Measure set(int index, Measure element) {
        return measures.set(index, element);
    }

    @Override
    public void add(int index, Measure element) {
        measures.add(index, element);
    }

    @Override
    public Measure remove(int index) {
        return measures.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return measures.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return measures.lastIndexOf(o);
    }

    @Override
    public ListIterator<Measure> listIterator() {
        return measures.listIterator();
    }

    @Override
    public ListIterator<Measure> listIterator(int index) {
        return measures.listIterator(index);
    }

    @Override
    public List<Measure> subList(int fromIndex, int toIndex) {
        return measures.subList(fromIndex, toIndex);
    }

    public void saveToArffFile(File measureTableFile) throws IOException {
        new MeasureTableSet(measures).saveToArffFile(measureTableFile);
    }

    private class MeasureTableSet extends ArffDataSet {

        private static final String strId = "Id";
        private static final String strName = "Name";
        private static final String strOptimalValue = "OptimalValue";
        private static final String strMeasureClass = "MeasureClass";
        private static final String strCategory = "Category";
        private static final String strDataSetName = "Category";
        private String strCalculateForTracks = "CalculateForTracks";
        private String strCalculateForWindows = "CalculateForWindows";

        private MeasureTableSet(File measureTable) throws IOException {
            super(measureTable);
            checkNumericAttribute(strId);
            checkStringAttribute(strName);
            checkNumericAttribute(strOptimalValue);
            checkStringAttribute(strMeasureClass);
            checkNominalAttribute(strCategory);
            checkNominalAttribute(strCalculateForTracks);
            checkNominalAttribute(strCalculateForWindows);
        }

        private MeasureTableSet(List<Measure> measures) {
            super(strDataSetName);
            List<Double> ids = new ArrayList<Double>();
            List<String> names = new ArrayList<String>();
            List<Double> optimalValue = new ArrayList<Double>();
            List<String> categories = new ArrayList<String>();
            List<String> measureClasses = new ArrayList<String>();
            List<Boolean> calculateForTracks = new ArrayList<Boolean>();
            List<Boolean> calculateForWindows = new ArrayList<Boolean>();
            for (Measure m: measures) {
                ids.add((double)m.getID());
                names.add(m.getName());
                optimalValue.add(m.getOptimalValue());
                categories.add(m.getCategory());
                measureClasses.add(m.getMeasureClass());
                calculateForTracks.add(m.isTrackLevelSelected());
                calculateForWindows.add(m.isWindowLevelSelected());
            }
            addAttribute(new NumericAttribute(strId, ids));
            addAttribute(new StringAttribute(strName, names));
            addAttribute(new NumericAttribute(strOptimalValue, optimalValue));
            addAttribute(new NominalAttribute(strCategory, categories));
            addAttribute(new StringAttribute(strMeasureClass, measureClasses));
            addAttribute(NominalAttribute.createFromBooleans(strCalculateForTracks, calculateForTracks));
            addAttribute(NominalAttribute.createFromBooleans(strCalculateForWindows, calculateForWindows));
        }

        private NumericAttribute getIdAttribute() {
            return (NumericAttribute) this.getAttribute(strId);
        }

        private StringAttribute getNameAttribute() {
            return (StringAttribute) this.getAttribute(strName);
        }
        
        private NumericAttribute getOptimalValueAttribute() {
            return (NumericAttribute) this.getAttribute(strOptimalValue);
        }

        private NominalAttribute getCategoryAttribute() {
            return (NominalAttribute) this.getAttribute(strCategory);
        }

        private NominalAttribute getCalculateForTracks() {
            return (NominalAttribute) this.getAttribute(strCalculateForTracks);
        }
        
        private NominalAttribute getCalculateForWindows() {
            return (NominalAttribute) this.getAttribute(strCalculateForWindows);
        }
        
        private StringAttribute getMeasureClassAttribute() {
            return (StringAttribute) this.getAttribute(strMeasureClass);
        }
    }
}

