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
public class MetricTable implements List<Metric>, Serializable {

    /**	For Serializable interface */
	private static final long serialVersionUID = -925369538370176875L;
	
	private final List<Metric> metrics = new ArrayList<Metric>();
    
    public MetricTable(File file) throws IOException {
        MetricTableSet metricTableSet = new MetricTableSet(file);
        for (int i = 0; i < metricTableSet.getValueCount(); i++) {
            int id = metricTableSet.getIdAttribute().getValueAt(i).intValue();
            String name = metricTableSet.getNameAttribute().getValueAt(i);
            String category = metricTableSet.getCategoryAttribute().getValueAt(i);
            Double optimalValue = metricTableSet.getOptimalValueAttribute().getValueAt(i);
            String metricClass = metricTableSet.getMetricClassAttribute().getValueAt(i);
            String supportsBinary = metricTableSet.getSupportsBinaryAttribute().getValueAt(i);
            String supportsMultiClass = metricTableSet.getSupportsMultiClassAttribute().getValueAt(i);
            String supportsFuzzy = metricTableSet.getSupportsFuzzyAttribute().getValueAt(i);
            if (!metricClass.equalsIgnoreCase("?")) {
            	Metric newMetric = new Metric(id, name, category, optimalValue, metricClass, supportsBinary, supportsMultiClass, supportsFuzzy);
            	if(metricTableSet.getCalculateForSongs().getValueAt(i).equalsIgnoreCase(new String("false"))) {
            		newMetric.setSongLevelSelected(false);
            	}
            	if(metricTableSet.getCalculateForPartitions().getValueAt(i).equalsIgnoreCase(new String("false"))) {
            		newMetric.setPartitionLevelSelected(false);
            	}
                metrics.add(newMetric);
            }
        }
    }

    public MetricTable() {
        
    }

    @Override
    public int size() {
        return metrics.size();
    }

    @Override
    public boolean isEmpty() {
        return metrics.isEmpty();
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean contains(Object o) {
        return metrics.contains(o);
    }

    @Override
    public Iterator<Metric> iterator() {
        return metrics.iterator();
    }

    @Override
    public Object[] toArray() {
        return metrics.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return metrics.toArray(a);
    }

    @Override
    public boolean add(Metric e) {
        return metrics.add(e);
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean remove(Object o) {
        return metrics.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return metrics.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Metric> c) {
        return metrics.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Metric> c) {
        return metrics.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return metrics.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return metrics.retainAll(c);
    }

    @Override
    public void clear() {
        metrics.clear();
    }

    @Override
    public Metric get(int index) {
        return metrics.get(index);
    }

    @Override
    public Metric set(int index, Metric element) {
        return metrics.set(index, element);
    }

    @Override
    public void add(int index, Metric element) {
        metrics.add(index, element);
    }

    @Override
    public Metric remove(int index) {
        return metrics.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return metrics.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return metrics.lastIndexOf(o);
    }

    @Override
    public ListIterator<Metric> listIterator() {
        return metrics.listIterator();
    }

    @Override
    public ListIterator<Metric> listIterator(int index) {
        return metrics.listIterator(index);
    }

    @Override
    public List<Metric> subList(int fromIndex, int toIndex) {
        return metrics.subList(fromIndex, toIndex);
    }

    public void saveToArffFile(File metricTableFile) throws IOException {
        new MetricTableSet(metrics).saveToArffFile(metricTableFile);
    }

    private class MetricTableSet extends ArffDataSet {

        private static final String strId = "Id";
        private static final String strName = "Name";
        private static final String strOptimalValue = "OptimalValue";
        private static final String strMetricClass = "MetricClass";
        private static final String strSupportsBinary = "SupportsBinary";
        private static final String strSupportsMulticlass = "SupportsMulticlass";
        private static final String strSupportsFuzzy = "SupportsFuzzy";
        private static final String strCategory = "Category";
        private static final String strDataSetName = "Category";
        private String strCalculateForSongs = "CalculateForSongs";
        private String strCalculateForPartitions = "CalculateForPartitions";

        private MetricTableSet(File metricTable) throws IOException {
            super(metricTable);
            checkNumericAttribute(strId);
            checkStringAttribute(strName);
            checkNumericAttribute(strOptimalValue);
            checkStringAttribute(strMetricClass);
            checkStringAttribute(strSupportsBinary);
            checkStringAttribute(strSupportsMulticlass);
            checkStringAttribute(strSupportsFuzzy);
            checkNominalAttribute(strCategory);
            checkNominalAttribute(strCalculateForSongs);
            checkNominalAttribute(strCalculateForPartitions);
        }

        private MetricTableSet(List<Metric> metrics) {
            super(strDataSetName);
            List<Double> ids = new ArrayList<Double>();
            List<String> names = new ArrayList<String>();
            List<Double> optimalValue = new ArrayList<Double>();
            List<String> categories = new ArrayList<String>();
            List<String> metricClasses = new ArrayList<String>();
            List<Boolean> calculateForSongs = new ArrayList<Boolean>();
            List<Boolean> calculateForPartitions = new ArrayList<Boolean>();
            List<String> supportsBinary = new ArrayList<String>();
            List<String> supportsMulticlass = new ArrayList<String>();
            List<String> supportsFuzzy = new ArrayList<String>();
            for (Metric m: metrics) {
                ids.add((double)m.getID());
                names.add(m.getName());
                optimalValue.add(m.getOptimalValue());
                categories.add(m.getCategory());
                metricClasses.add(m.getMetricClass());
                calculateForSongs.add(m.isSongLevelSelected());
                calculateForPartitions.add(m.isPartitionLevelSelected());
                supportsBinary.add(m.getSupportsBinary());
                supportsMulticlass.add(m.getSupportsMultiClass());
                supportsFuzzy.add(m.getSupportsFuzzy());
            }
            addAttribute(new NumericAttribute(strId, ids));
            addAttribute(new StringAttribute(strName, names));
            addAttribute(new NumericAttribute(strOptimalValue, optimalValue));
            addAttribute(new NominalAttribute(strCategory, categories));
            addAttribute(new StringAttribute(strMetricClass, metricClasses));
            addAttribute(NominalAttribute.createFromBooleans(strCalculateForSongs, calculateForSongs));
            addAttribute(NominalAttribute.createFromBooleans(strCalculateForPartitions, calculateForPartitions));
            addAttribute(new StringAttribute(strSupportsBinary, supportsBinary));
            addAttribute(new StringAttribute(strSupportsMulticlass, supportsMulticlass));
            addAttribute(new StringAttribute(strSupportsFuzzy, supportsFuzzy));
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

        private NominalAttribute getCalculateForSongs() {
            return (NominalAttribute) this.getAttribute(strCalculateForSongs);
        }
        
        private NominalAttribute getCalculateForPartitions() {
            return (NominalAttribute) this.getAttribute(strCalculateForPartitions);
        }
        
        private StringAttribute getMetricClassAttribute() {
            return (StringAttribute) this.getAttribute(strMetricClass);
        }

        private StringAttribute getSupportsBinaryAttribute() {
            return (StringAttribute) this.getAttribute(strSupportsBinary);
        }

        private StringAttribute getSupportsMultiClassAttribute() {
            return (StringAttribute) this.getAttribute(strSupportsMulticlass);
        }

        private StringAttribute getSupportsFuzzyAttribute() {
            return (StringAttribute) this.getAttribute(strSupportsFuzzy);
        }
    }
}

