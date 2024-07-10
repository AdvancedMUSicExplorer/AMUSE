package amuse.util.converters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.Attribute;
import amuse.data.modality.Format;
import amuse.interfaces.nodes.NodeException;
import amuse.preferences.AmusePreferences;

/**
 * This interface defines the operations which should be supported by all converter classes
 * and provides methods to search for possible conversions in conversionTable.arff.
 * 
 * @author Clara Pingel
 */
public interface ConverterInterface {
	
	/**
	 * Searchs conversionTable.arff for possible target formats for given source format.
	 * 
	 * @return list of possible target formats
	 */
	public static List<Format> conversionsAvailable(Format source) {
		DataSetAbstract configTable;
		List<Format> targetFormats = new ArrayList<Format>();
		try {
			configTable = new ArffDataSet(new File(AmusePreferences.getConversionTablePath()));
			Attribute sourceAttribute = configTable.getAttribute("Source");
			Attribute targetAttribute = configTable.getAttribute("Target");
			
			for(int i = 0; i<configTable.getValueCount(); i++) {
				if(sourceAttribute.getValueAt(i).toString().equals(source.toString())) {
					targetFormats.add(Format.getFormatByString(targetAttribute.getValueAt(i).toString()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return targetFormats;
	}
	
	/**
	 * Returns conversion class for given source and target format.
	 */
	public static ConverterInterface getConversionClass(Format source, Format target) {
		DataSetAbstract configTable;
		try {
			configTable = new ArffDataSet(new File(AmusePreferences.getConversionTablePath()));
			Attribute sourceAttribute = configTable.getAttribute("Source");
			Attribute targetAttribute = configTable.getAttribute("Target");
			Attribute conversionClassAttribute = configTable.getAttribute("ConversionClass");
			
			for(int i = 0; i<configTable.getValueCount(); i++) {
				if(sourceAttribute.getValueAt(i).equals(source.toString()) && targetAttribute.getValueAt(i).equals(target.toString())) {
					Class<?> converter = Class.forName(conversionClassAttribute.getValueAt(i).toString());
					ConverterInterface ca = (ConverterInterface)converter.newInstance();
					return ca;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public File convert(File file, File outputFolder) throws IOException, NodeException;
	
	public String getEnding();
}
