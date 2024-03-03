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

public interface ConverterInterface {
	
	public static List<Format> conversionsAvailable(Format format) {
		DataSetAbstract configTable;
		List<Format> targetFormats = new ArrayList<Format>();
		try {
			configTable = new ArffDataSet(new File(AmusePreferences.getConversionTablePath()));
			Attribute sourceAttribute = configTable.getAttribute("Source");
			Attribute targetAttribute = configTable.getAttribute("Target");
			
			for(int i = 0; i<configTable.getValueCount(); i++) {
				if(sourceAttribute.getValueAt(i).toString().equals(format.toString())) {
					targetFormats.add(Format.getFormatByString(targetAttribute.getValueAt(i).toString()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return targetFormats;
	}
	
	public static ConverterInterface getConversionClass(Format sourceFormat, Format targetFormat) {
		DataSetAbstract configTable;
		try {
			configTable = new ArffDataSet(new File(AmusePreferences.getConversionTablePath()));
			Attribute sourceAttribute = configTable.getAttribute("Source");
			Attribute targetAttribute = configTable.getAttribute("Target");
			Attribute conversionClassAttribute = configTable.getAttribute("ConversionClass");
			
			for(int i = 0; i<configTable.getValueCount(); i++) {
				if(sourceAttribute.getValueAt(i).equals(sourceFormat.toString()) && targetAttribute.getValueAt(i).equals(targetFormat.toString())) {
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
	
	public void convert(File file, File outputFolder) throws IOException, NodeException;
	
	public String getEnding();
}
