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

import java.io.Serializable;


/**
 * @author Clemens Waeltken
 */
public class Metric implements Serializable {
	
	/**	For Serializable interface */
	private static final long serialVersionUID = -6860419828779025751L;
	private final int id;
    private final String name;
    private final Double optimalValue;
    private boolean extractSongLevel;
    private boolean extractPartitionLevel;
    private final String category;
    private final String metricClass;
    private final String supportsBinary;
    private final String supportsMultiClass;
    private final String supportsFuzzy;


    public Metric(int id, String name, String category, Double optimalValue, String metricClass, String supportsBinary, String supportsMultiClass, String supportsFuzzy) {
        this.id = id;
        this.name = name;
        this.optimalValue = optimalValue;
        this.extractSongLevel = true;
        this.extractPartitionLevel = true;
        this.category = category;
        this.metricClass = metricClass;
        this.supportsBinary = supportsBinary;
        this.supportsFuzzy = supportsFuzzy;
        this.supportsMultiClass = supportsMultiClass;
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
    
    public Double getOptimalValue() {
        return this.optimalValue;
    }

    public boolean isSongLevelSelected() {
        return this.extractSongLevel;
    }

    public boolean isPartitionLevelSelected() {
        return this.extractPartitionLevel;
    }

    public String getCategory() {
        return category;
    }
    
    public String getMetricClass() {
        return this.metricClass;
    }

    public void setPartitionLevelSelected(boolean partitionLevel) {
        extractPartitionLevel = partitionLevel;
    }
    
     public void setSongLevelSelected(boolean songLevel) {
        extractSongLevel = songLevel;
    }

    public String getSupportsBinary() {
        return supportsBinary;
    }

    public String getSupportsFuzzy() {
        return supportsFuzzy;
    }

    public String getSupportsMultiClass() {
        return supportsMultiClass;
    }
}