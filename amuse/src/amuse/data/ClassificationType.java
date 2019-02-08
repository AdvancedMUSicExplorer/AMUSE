package amuse.data;

public enum ClassificationType {
	//UNSUPERVISED,
	BINARY,
	MULTILABEL,
	MULTICLASS;
	
	public static String[] stringValues(){
		ClassificationType[] values = ClassificationType.values();
		String[] output = new String[values.length];
		for(int i = 0; i < values.length; i++){
			output[i] = values[i].toString();
		}
		return output;
	}
}
