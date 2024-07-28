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
import java.util.HashMap;   /* added for parameter adjustment */
import java.util.Map;  /* added for parameter adjustment */

/**
 * @author Clemens Waeltken
 */
public class Measure implements Serializable {
	
	/**	For Serializable interface */
	private Map<String, Parameter> parameters = new HashMap<>(); 
	private static final long serialVersionUID = -6860419828779025751L;
	private final int id;
    private final String name;
    private final Double optimalValue;
    private boolean extractTrackLevel;
    private boolean extractWindowLevel;
    private boolean extractChangeParameter;    /* added for parameter adjustment */
    private final String category;
    private final String measureClass;   
    private Double newParameter;   /* added for parameter adjustment */



    public Measure(int id, String name, String category, Double optimalValue, String measureClass) {
        this.id = id;
        this.name = name;
        this.optimalValue = optimalValue;
        this.extractTrackLevel = true;
        this.extractWindowLevel = true;
        this.extractChangeParameter = true;    /* added for parameter adjustment */
        this.category = category;
        this.measureClass = measureClass;
        this.newParameter = null;   /* added for parameter adjustment */
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

    public boolean isTrackLevelSelected() {
        return this.extractTrackLevel;
    }

    public boolean isWindowLevelSelected() {
        return this.extractWindowLevel;
    }
    /* added for parameter adjustment */
    public boolean isChangeParameterSelected() {    
        return this.extractChangeParameter;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getMeasureClass() {
        return this.measureClass;
    }
    /* added for parameter adjustment */
    public Double getNewParameter() {
        return newParameter;
    }

    public void setWindowLevelSelected(boolean windowLevel) {
        extractWindowLevel = windowLevel;
    }
    
     public void setTrackLevelSelected(boolean trackLevel) {
        extractTrackLevel = trackLevel;
    }
     /* added for parameter adjustment */
     public void setChangeParameterSelected(boolean changeParameter) {
    	extractChangeParameter = changeParameter;
     }
     public void setNewParameter(Double newParameter) {
         this.newParameter = newParameter;
     }
     /* for the popup table */
     public static class Parameter {
    	    private String name;
    	    private double value;
    	    private double defaultValue;
    	    private Double range;
    	    private String definition;

    	    public Parameter(String name, double value, double defaultValue, Double paramRange, String definition) {
    	        this.name = name;
    	        this.value = value;
    	        this.defaultValue = defaultValue;
    	        this.range = paramRange;
    	        this.definition = definition;
    	    }

    	    public String getName() { return name; }
    	    public double getValue() { return value; }
    	    public double getDefaultValue() { return defaultValue; }
    	    public Double getRange() { return range; }
    	    public String getDefinition() { return definition; }

    	    public void setValue(double value) { this.value = value; }
    	}

    	// Added methods for the parameters
    	public Map<String, Parameter> getParameters() {
    	    return parameters;
    	}

    	public void addParameter(Parameter parameter) {
    	    parameters.put(parameter.getName(), parameter);
    	}
}