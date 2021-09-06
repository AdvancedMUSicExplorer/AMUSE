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
 *  Creation date: 01.12.2009
 */ 
package amuse.nodes.validator.measures.correlation;

import java.util.ArrayList;

import amuse.data.annotation.ClassifiedClassificationWindow;
import amuse.interfaces.nodes.NodeException;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Kendall's tau rank correlation coefficient is based on the comparison of all possible ordered pairs of instances between ground truth
 * and labeled data.
 *  
 * @author Igor Vatolkin
 * @version $Id: KendallsTauRankCorrelation.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class KendallsTauRankCorrelation extends ClassificationQualityDoubleMeasureCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnTrackLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		
		// Rank calculation is not required, since the numeric values can be compared directly!
		// Calculate the predicted track relationships (averaged over all classification windows)
		ArrayList<Double> predictedTrackRelationships = new ArrayList<Double>(groundTruthRelationships.size());
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this track (averaging among all classificatoin windows)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j][0];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			predictedTrackRelationships.add(currentPredictedValue);
		}
		
		// Calculate all ordered pairs of instances for ground truth and the number of tied (similar) values for ground truth
		int tied_gt = 0;
		ArrayList<Integer[]> orderedPairsGroundTruth = new ArrayList<Integer[]>();
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=i+1;j<groundTruthRelationships.size();j++) {
				if(groundTruthRelationships.get(i) > groundTruthRelationships.get(j)) {
					orderedPairsGroundTruth.add(new Integer[]{i,j}); 
				} else if(groundTruthRelationships.get(i) < groundTruthRelationships.get(j)){
					orderedPairsGroundTruth.add(new Integer[]{j,i});
				} else {
					tied_gt++;
				}
			}
		}
		
		// Calculate all ordered pairs of instances for predicted values and the number of tied (similar) values for predicted values
		int tied_predicted = 0;
		ArrayList<Integer[]> orderedPairsPredicted = new ArrayList<Integer[]>();
		for(int i=0;i<predictedTrackRelationships.size();i++) {
			for(int j=i+1;j<predictedTrackRelationships.size();j++) {
				if(predictedTrackRelationships.get(i) > predictedTrackRelationships.get(j)) {
					orderedPairsPredicted.add(new Integer[]{i,j}); 
				} else if(predictedTrackRelationships.get(i) < predictedTrackRelationships.get(j)) {
					orderedPairsPredicted.add(new Integer[]{j,i});
				} else {
					tied_predicted++;
				}
			}
		}
		
		// Calculate the number of equal untied pairs for ground truth and predicted values
		int equal_pairs = 0;
		for(int i=0;i<orderedPairsGroundTruth.size();i++) {
			for(int j=0;j<orderedPairsPredicted.size();j++) {

				// Comparison
				if(orderedPairsGroundTruth.get(i)[0].equals(orderedPairsPredicted.get(j)[0]) &&
				   orderedPairsGroundTruth.get(i)[1].equals(orderedPairsPredicted.get(j)[1])) {
					equal_pairs++;
					break;
				}
			}
		}
		
		// Number of unequal pairs for ground truth and predicted values
		int unequal_pairs = orderedPairsGroundTruth.size() + orderedPairsPredicted.size() - 2*equal_pairs;
		
		// Calculate the Kendall's tau rank correlation coefficient
		double corrCoef = (equal_pairs - unequal_pairs) /  
			(Math.sqrt(equal_pairs + unequal_pairs + tied_predicted) * Math.sqrt(equal_pairs + unequal_pairs + tied_gt));
		
		// Prepare the result
		ValidationMeasureDouble[] correlationMeasure = new ValidationMeasureDouble[1];
		correlationMeasure[0] = new ValidationMeasureDouble(false);
		correlationMeasure[0].setId(302);
		correlationMeasure[0].setName("Kendall's tau rank correlation coefficient on track level");
		correlationMeasure[0].setValue(corrCoef);
		return correlationMeasure;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnClassficationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnClassficationWindowLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		
		// Save the ground truth values for each classification window
		ArrayList<Double> groundTruthClassificationWindowRelationships = new ArrayList<Double>();
		for(int i=0;i<predictedRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				groundTruthClassificationWindowRelationships.add(new Double(groundTruthRelationships.get(i)));
			}
		}
		
		// Save the predicted values for each classification window
		ArrayList<Double> predictedClassificationWindowRelationships = new ArrayList<Double>();
		for(int i=0;i<predictedRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				predictedClassificationWindowRelationships.add(new Double(predictedRelationships.get(i).getRelationships()[j][0]));
			}
		}
		
		// Calculate all ordered pairs of instances for ground truth and the number of tied (similar) values for ground truth
		int tied_gt = 0;
		ArrayList<Integer[]> orderedPairsGroundTruth = new ArrayList<Integer[]>();
		for(int i=0;i<groundTruthClassificationWindowRelationships.size();i++) {
			for(int j=i+1;j<groundTruthClassificationWindowRelationships.size();j++) {
				if(groundTruthClassificationWindowRelationships.get(i) > groundTruthClassificationWindowRelationships.get(j)) {
					orderedPairsGroundTruth.add(new Integer[]{i,j}); 
				} else if(groundTruthClassificationWindowRelationships.get(i) < groundTruthClassificationWindowRelationships.get(j)){
					orderedPairsGroundTruth.add(new Integer[]{j,i});
				} else {
					tied_gt++;
				}
			}
		}
		
		// Calculate all ordered pairs of instances for predicted values and the number of tied (similar) values for predicted values
		int tied_predicted = 0;
		ArrayList<Integer[]> orderedPairsPredicted = new ArrayList<Integer[]>();
		for(int i=0;i<predictedClassificationWindowRelationships.size();i++) {
			for(int j=i+1;j<predictedClassificationWindowRelationships.size();j++) {
				if(predictedClassificationWindowRelationships.get(i) > predictedClassificationWindowRelationships.get(j)) {
					orderedPairsPredicted.add(new Integer[]{i,j}); 
				} else if(predictedClassificationWindowRelationships.get(i) < predictedClassificationWindowRelationships.get(j)) {
					orderedPairsPredicted.add(new Integer[]{j,i});
				} else {
					tied_predicted++;
				}
			}
		}
		
		// Calculate the number of equal untied pairs for ground truth and predicted values
		int equal_pairs = 0;
		for(int i=0;i<orderedPairsGroundTruth.size();i++) {
			for(int j=0;j<orderedPairsPredicted.size();j++) {

				// Comparison
				if(orderedPairsGroundTruth.get(i)[0].equals(orderedPairsPredicted.get(j)[0]) &&
				   orderedPairsGroundTruth.get(i)[1].equals(orderedPairsPredicted.get(j)[1])) {
					equal_pairs++;
					break;
				}
			}
		}
		
		// Number of unequal pairs for ground truth and predicted values
		int unequal_pairs = orderedPairsGroundTruth.size() + orderedPairsPredicted.size() - 2*equal_pairs;
		
		// Calculate the Kendall's tau rank correlation coefficient
		double corrCoef = (equal_pairs - unequal_pairs) /  
			(Math.sqrt(equal_pairs + unequal_pairs + tied_predicted) * Math.sqrt(equal_pairs + unequal_pairs + tied_gt));
		
		// Prepare the result
		ValidationMeasureDouble[] correlationMeasure = new ValidationMeasureDouble[1];
		correlationMeasure[0] = new ValidationMeasureDouble(false);
		correlationMeasure[0].setId(302);
		correlationMeasure[0].setName("Kendall's tau rank correlation coefficient on classification window level");
		correlationMeasure[0].setValue(corrCoef);
		return correlationMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		return calculateMultiLabelMeasureOnTrackLevel(groundTruthRelationships, predictedRelationships);
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		return calculateMultiLabelMeasureOnWindowLevel(groundTruthRelationships, predictedRelationships);
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnTrackLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnTrackLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		double[] corrCoef = new double[numberOfCategories];
		for(int category = 0; category < numberOfCategories; category++) {
			// Rank calculation is not required, since the numeric values can be compared directly!
			// Calculate the predicted track relationships (averaged over all classification windows)
			ArrayList<Double> predictedTrackRelationships = new ArrayList<Double>(groundTruthRelationships.size());
			for(int i=0;i<groundTruthRelationships.size();i++) {
				
				// Calculate the predicted value for this track (averaging among all classification windows)
				Double currentPredictedValue = 0.0d;
				for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
					currentPredictedValue += predictedRelationships.get(i).getRelationships()[j][category];
				}
				currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
				predictedTrackRelationships.add(currentPredictedValue);
			}
			
			// Calculate all ordered pairs of instances for ground truth and the number of tied (similar) values for ground truth
			int tied_gt = 0;
			ArrayList<Integer[]> orderedPairsGroundTruth = new ArrayList<Integer[]>();
			for(int i=0;i<groundTruthRelationships.size();i++) {
				for(int j=i+1;j<groundTruthRelationships.size();j++) {
					if(groundTruthRelationships.get(i).getRelationships()[0][category] > groundTruthRelationships.get(j).getRelationships()[0][category]) {
						orderedPairsGroundTruth.add(new Integer[]{i,j}); 
					} else if(groundTruthRelationships.get(i).getRelationships()[0][category] < groundTruthRelationships.get(j).getRelationships()[0][category]){
						orderedPairsGroundTruth.add(new Integer[]{j,i});
					} else {
						tied_gt++;
					}
				}
			}
			
			// Calculate all ordered pairs of instances for predicted values and the number of tied (similar) values for predicted values
			int tied_predicted = 0;
			ArrayList<Integer[]> orderedPairsPredicted = new ArrayList<Integer[]>();
			for(int i=0;i<predictedTrackRelationships.size();i++) {
				for(int j=i+1;j<predictedTrackRelationships.size();j++) {
					if(predictedTrackRelationships.get(i) > predictedTrackRelationships.get(j)) {
						orderedPairsPredicted.add(new Integer[]{i,j}); 
					} else if(predictedTrackRelationships.get(i) < predictedTrackRelationships.get(j)) {
						orderedPairsPredicted.add(new Integer[]{j,i});
					} else {
						tied_predicted++;
					}
				}
			}
			
			// Calculate the number of equal untied pairs for ground truth and predicted values
			int equal_pairs = 0;
			for(int i=0;i<orderedPairsGroundTruth.size();i++) {
				for(int j=0;j<orderedPairsPredicted.size();j++) {
	
					// Comparison
					if(orderedPairsGroundTruth.get(i)[0].equals(orderedPairsPredicted.get(j)[0]) &&
					   orderedPairsGroundTruth.get(i)[1].equals(orderedPairsPredicted.get(j)[1])) {
						equal_pairs++;
						break;
					}
				}
			}
			
			// Number of unequal pairs for ground truth and predicted values
			int unequal_pairs = orderedPairsGroundTruth.size() + orderedPairsPredicted.size() - 2*equal_pairs;
			
			// Calculate the Kendall's tau rank correlation coefficient
			corrCoef[category] = (equal_pairs - unequal_pairs) /  
				(Math.sqrt(equal_pairs + unequal_pairs + tied_predicted) * Math.sqrt(equal_pairs + unequal_pairs + tied_gt));
		}
		
		// Prepare the result
		ValidationMeasureDouble[] correlationMeasure = new ValidationMeasureDouble[numberOfCategories];
		for(int category = 0; category < numberOfCategories; category++) {
			correlationMeasure[category] = new ValidationMeasureDouble(false);
			correlationMeasure[category].setId(302);
			correlationMeasure[category].setName("Kendall's tau rank correlation coefficient on track level for category " + groundTruthRelationships.get(0).getLabels()[category]);
			correlationMeasure[category].setValue(corrCoef[category]);
		}
		return correlationMeasure;
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMultiLabelMeasureOnClassificationWindowLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiLabelMeasureOnWindowLevel(ArrayList<ClassifiedClassificationWindow> groundTruthRelationships, ArrayList<ClassifiedClassificationWindow> predictedRelationships) throws NodeException {
		int numberOfCategories = groundTruthRelationships.get(0).getLabels().length;
		
		double[] corrCoef = new double[numberOfCategories];
		for(int category = 0; category < numberOfCategories; category++) {
			// Save the ground truth values for each classification window
			ArrayList<Double> groundTruthClassificationWindowRelationships = new ArrayList<Double>();
			for(int i=0;i<predictedRelationships.size();i++) {
				for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
					groundTruthClassificationWindowRelationships.add(new Double(groundTruthRelationships.get(i).getRelationships()[0][category]));
				}
			}
			
			// Save the predicted values for each classification window
			ArrayList<Double> predictedClassificationWindowRelationships = new ArrayList<Double>();
			for(int i=0;i<predictedRelationships.size();i++) {
				for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
					predictedClassificationWindowRelationships.add(new Double(predictedRelationships.get(i).getRelationships()[j][category]));
				}
			}
			
			// Calculate all ordered pairs of instances for ground truth and the number of tied (similar) values for ground truth
			int tied_gt = 0;
			ArrayList<Integer[]> orderedPairsGroundTruth = new ArrayList<Integer[]>();
			for(int i=0;i<groundTruthClassificationWindowRelationships.size();i++) {
				for(int j=i+1;j<groundTruthClassificationWindowRelationships.size();j++) {
					if(groundTruthClassificationWindowRelationships.get(i) > groundTruthClassificationWindowRelationships.get(j)) {
						orderedPairsGroundTruth.add(new Integer[]{i,j}); 
					} else if(groundTruthClassificationWindowRelationships.get(i) < groundTruthClassificationWindowRelationships.get(j)){
						orderedPairsGroundTruth.add(new Integer[]{j,i});
					} else {
						tied_gt++;
					}
				}
			}
			
			// Calculate all ordered pairs of instances for predicted values and the number of tied (similar) values for predicted values
			int tied_predicted = 0;
			ArrayList<Integer[]> orderedPairsPredicted = new ArrayList<Integer[]>();
			for(int i=0;i<predictedClassificationWindowRelationships.size();i++) {
				for(int j=i+1;j<predictedClassificationWindowRelationships.size();j++) {
					if(predictedClassificationWindowRelationships.get(i) > predictedClassificationWindowRelationships.get(j)) {
						orderedPairsPredicted.add(new Integer[]{i,j}); 
					} else if(predictedClassificationWindowRelationships.get(i) < predictedClassificationWindowRelationships.get(j)) {
						orderedPairsPredicted.add(new Integer[]{j,i});
					} else {
						tied_predicted++;
					}
				}
			}
			
			// Calculate the number of equal untied pairs for ground truth and predicted values
			int equal_pairs = 0;
			for(int i=0;i<orderedPairsGroundTruth.size();i++) {
				for(int j=0;j<orderedPairsPredicted.size();j++) {
	
					// Comparison
					if(orderedPairsGroundTruth.get(i)[0].equals(orderedPairsPredicted.get(j)[0]) &&
					   orderedPairsGroundTruth.get(i)[1].equals(orderedPairsPredicted.get(j)[1])) {
						equal_pairs++;
						break;
					}
				}
			}
			
			// Number of unequal pairs for ground truth and predicted values
			int unequal_pairs = orderedPairsGroundTruth.size() + orderedPairsPredicted.size() - 2*equal_pairs;
			
			// Calculate the Kendall's tau rank correlation coefficient
			corrCoef[category] = (equal_pairs - unequal_pairs) /  
				(Math.sqrt(equal_pairs + unequal_pairs + tied_predicted) * Math.sqrt(equal_pairs + unequal_pairs + tied_gt));
		}
		
		// Prepare the result
		ValidationMeasureDouble[] correlationMeasure = new ValidationMeasureDouble[numberOfCategories];
		for(int category = 0; category < numberOfCategories; category++) {
			correlationMeasure[category] = new ValidationMeasureDouble(false);
			correlationMeasure[category].setId(302);
			correlationMeasure[category].setName("Kendall's tau rank correlation coefficient on classification window level for category " + groundTruthRelationships.get(0).getLabels()[category]);
			correlationMeasure[category].setValue(corrCoef[category]);
		}
		return correlationMeasure;
	}
}

