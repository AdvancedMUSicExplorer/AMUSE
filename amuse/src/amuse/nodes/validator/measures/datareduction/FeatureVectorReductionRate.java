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
 * Creation date: 11.11.2009
 */
package amuse.nodes.validator.measures.datareduction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.DataReductionMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * This data reduction measure calculates the rate of feature vector entries corresponding to the initial number of feature
 * matrix entries.
 * 
 * @author Igor Vatolkin
 * @version $Id: FeatureVectorReductionRate.java 241 2018-07-26 12:35:24Z frederik-h $
 */
public class FeatureVectorReductionRate extends DataReductionMeasureCalculator {

	// TODO Metrikenklassen sollen mehrere unterschiedliche Metriken berechnen können!
	// -> hier wird dieselbe Arbeit gemacht wie bei MatrixReduktionsrate-Metrik..
	
	public ValidationMeasureDouble[] calculateMeasure(ArrayList<String> usedProcessedFeatureFiles) throws NodeException {
		
		long initalNumberOfFeatureMatrixEntries = 0;
		long finalNumberOfFeatureVectorEntries = 0;
		
		if(usedProcessedFeatureFiles != null) {
			try {
				for(String f : usedProcessedFeatureFiles) {
					BufferedReader reader = new BufferedReader(new FileReader(new File(f)));
					String line = reader.readLine();
					boolean initFound = false, finalFound = false;
					while(line != null) {
						if(line.startsWith("%initalNumberOfFeatureMatrixEntries")) {
							initalNumberOfFeatureMatrixEntries += new Long(line.substring(line.indexOf("=")+1, line.length()));
							initFound = true;
						} else if(line.startsWith("%finalNumberOfFeatureVectorEntries")) {
							finalNumberOfFeatureVectorEntries += new Long(line.substring(line.indexOf("=")+1, line.length()));
							finalFound = true;
						}
						if(initFound && finalFound) break;
						line = reader.readLine();
					}
					reader.close();
				}
			} catch(IOException e) {
				throw new NodeException("Could not load data from processed feature files: " + e.getMessage());
			}
		}
		
		Double rate = new Double(finalNumberOfFeatureVectorEntries) / new Double(initalNumberOfFeatureMatrixEntries);
		ValidationMeasureDouble[] ratioOfUsedRawTimeWindows = new ValidationMeasureDouble[1];
		ratioOfUsedRawTimeWindows[0] = new ValidationMeasureDouble();
		ratioOfUsedRawTimeWindows[0].setId(2);
		ratioOfUsedRawTimeWindows[0].setName("Feature vector reduction rate");
		ratioOfUsedRawTimeWindows[0].setValue(rate);
		return ratioOfUsedRawTimeWindows;
	}

	@Override
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
    
}
