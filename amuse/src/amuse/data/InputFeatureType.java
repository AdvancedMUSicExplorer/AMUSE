package amuse.data;

public enum InputFeatureType {
	RAW_FEATURES,
	PROCESSED_FEATURES;
	
	public static String[] stringValues(){
		InputFeatureType[] values = InputFeatureType.values();
		String[] output = new String[values.length];
		for(int i = 0; i < values.length; i++){
			output[i] = values[i].toString();
		}
		return output;
	}
}
