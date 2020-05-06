/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2020 by code authors
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
 *  Creation date: 17.04.2020
 */
package amuse.nodes.validator.measures.events;

import java.lang.Math;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import amuse.data.annotation.EventsDescription;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.EventDetectionQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasure;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;
/**
 * Deviation (median deviation between 
 * reference and estimated boundary time)
 *  
 * @author Felix Wolff
 * @version $Id: $
 */
public class Deviation extends EventDetectionQualityDoubleMeasureCalculator{

    /*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.EventDetectionQualityMeasureCalculatorInterface#calculateMeasure(amuse.data.annotation.EventsDescription, amuse.data.annotation.EventsDescription)
	 */
     public ValidationMeasure[] calculateMeasure(EventsDescription groundTruthEvents, EventsDescription predictedEvents)
            throws NodeException {
        //calculate distance
        double[][] distance = new double[groundTruthEvents.getEventMs().length][predictedEvents.getEventMs().length];
        int count=0;
        for (double boundary : groundTruthEvents.getEventMs()){
            distance[count] = arrayAbsDistance(predictedEvents.getEventMs(), boundary);
            count++;
        }
        //find minimum
        Arrays.sort(distance,Comparator.comparingDouble(a -> DoubleStream.of(a).sum()));


        double estimatedToReference = median(distance[0]);
        // Prepare the result
        ValidationMeasureDouble[] deviationMeasure = new ValidationMeasureDouble[1];
        deviationMeasure[0] = new ValidationMeasureDouble(true);
        deviationMeasure[0].setId(416);
		deviationMeasure[0].setName("Deviation-measure");
		deviationMeasure[0].setValue(new Double(estimatedToReference));
		return deviationMeasure;
    }
    /**
     * calculates median for array
     * @return median
     */
    private static double median(double[] input) {
        if (input.length==0) {
            throw new IllegalArgumentException("to calculate median we need at least 1 element");
        }
        Arrays.sort(input);
        if (input.length%2==0) {
            return (input[input.length/2-1] + input[input.length/2])/2;
        } 
        return input[input.length/2];
    }

    /**
     * calculates absolute distance from value to each element of array
     * 
     * @return array with distance values
     */
    private static double[] arrayAbsDistance(Double[] array, Double value) {
        double[] outArray = new double[array.length];
        for (int i=0; i<array.length;i++) {
            outArray[i] = Math.abs(array[i] - value);
        }
        return outArray;
    }

    /**
	 * @see amuse.nodes.validator.interfaces.EventDetectionQualityMeasureCalculatorInterface#setParameters(java.lang.String)
	 */
    public void setParameters(String parameterString) throws NodeException {
        // no parameters

    }




}