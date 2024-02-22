package amuse.util.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.attributes.Attribute;
import amuse.data.modality.Format;
import amuse.interfaces.nodes.NodeException;
import amuse.preferences.AmusePreferences;
import amuse.util.AmuseLogger;
import amuse.util.audio.AudioFileConversion;

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
	
	public static void deleteFile(File wavFile) {
        boolean success = wavFile.delete();
        if (!success) {
        	wavFile.deleteOnExit();
        }
    }
	
	public static void fileCopy(File srcFile, File destFile) {
    	FileInputStream srcChannelFIS = null;
    	FileOutputStream dstChannelFOS = null;
        try {
            // Create channel on the source
        	srcChannelFIS = new FileInputStream(srcFile);
            FileChannel srcChannel = srcChannelFIS.getChannel();

            // Create channel on the destination
            dstChannelFOS = new FileOutputStream(destFile);
            FileChannel dstChannel = dstChannelFOS.getChannel();

            // Copy file contents from source to destination
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

            // Close the channels
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            AmuseLogger.write(AudioFileConversion.class.getName(), Level.ERROR, "Unable to copy " + srcFile.getAbsolutePath() + " to " + destFile.getAbsolutePath() + ".");
        }
        finally{
        	if(srcChannelFIS != null){
        		try {
					srcChannelFIS.close();
				} catch (IOException e) {}
        	}
        	if(dstChannelFOS != null){
        		try {
        			dstChannelFOS.close();
				} catch (IOException e) {}
        	}
        }
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
	
	public static String cutExtension(String filename) {
		 int pos = filename.lastIndexOf('.');
         return filename.substring(0, pos);
	}
	
	public void convert(File file, File outputFolder) throws IOException, NodeException;
	
	public String getEnding();
}
