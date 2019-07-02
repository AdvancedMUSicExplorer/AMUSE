package amuse.data;

/** Defines the ground truth type. Can be either
 * - Id of the music category from $AMUSEHOME$/config/categoryTable.arff or
 * - Path to the labeled file list or
 * - Path to the ready training input (prepared e.g. by a validator method) */
public enum GroundTruthSourceType {
	CATEGORY_ID,
	FILE_LIST,
	READY_INPUT;
	
	public static String[] stringValues(){
		GroundTruthSourceType[] values = GroundTruthSourceType.values();
		String[] output = new String[values.length];
		for(int i = 0; i < values.length; i++){
			output[i] = values[i].toString();
		}
		return output;
	}
}
