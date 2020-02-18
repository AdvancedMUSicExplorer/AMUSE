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
 * Creation date: 01.04.2010
 */

package amuse.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.log4j.Level;

/**
 * This Class bundles various methods for file operations like copy, move, delete...
 * @author waeltken
 */
public class FileOperations {

    private static Level defaultLevel = Level.DEBUG;


    /**
     * Use this method to copy single files or directories recursively.
     * @param from the file or folder to copy.
     * @param to the file to copy to or the folder to move the copy in.
     * @return true if operation was successful.
     */
    public static void copy(File from, File to) throws IOException {
	copy(from, to, defaultLevel);
    }
    
    /**
     * Use this method to copy single files or directories recursively.
     * @param from the file or folder to copy.
     * @param to the file to copy to or the folder to move the copy in.
     * @return true if operation was successful.
     */
    public static void copy(File from, File to, Level l) throws IOException {
        if (from.isDirectory()) {
            if (isParent(from, to)) {
                IOException ex = new IOException("Source is child of destination folder!");
                log(l, ex.getLocalizedMessage());
                throw ex;
            }
            to.mkdirs();
            for (File f: from.listFiles()) {
                copy(f, new File(to, f.getName()), l);
            }
        } else {
        	FileInputStream inChannelFIS = null;
        	FileOutputStream outChannelFOS = null;
            try {
            	log(l, "Copying: " + from.getAbsolutePath());
            	inChannelFIS = new FileInputStream(from);
            	FileChannel inChannel = inChannelFIS.getChannel();
            	outChannelFOS = new FileOutputStream(to);
                FileChannel outChannel = outChannelFOS.getChannel();
                try {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } catch (IOException ex) {
                    log(l, ex.getLocalizedMessage());
                    throw ex;
                } finally {
                    if (inChannel != null) {
                        inChannel.close();
                    }
                    if (outChannel != null) {
                        outChannel.close();
                    }
                }
            } catch (FileNotFoundException ex) {
                log(l, ex.getLocalizedMessage());
                throw ex;
            } catch (IOException ex) {
                log(l, ex.getLocalizedMessage());
                throw ex;
            }
            finally{
            	if(inChannelFIS != null){
            		try {
						inChannelFIS.close();
					} 
            		catch (IOException e) {}
            	}
            	if(outChannelFOS != null){
            		try {
            			outChannelFOS.close();
					} 
            		catch (IOException e) {}
            	}
            }
        }
    }

    /**
     * Use this method to move files or folders. Much more efficient then copy and delete.
     * @param from the file to move.
     * @param to the target to move this file to.
     * @return true if the move was successful.
     */
    public static void move(File from, File to) throws IOException {
	move(from, to, defaultLevel);
    }

    /**
     * Use this method to move files or folders. Much more efficient then copy and delete.
     * @param from the file to move.
     * @param to the target to move this file to.
     * @return true if the move was successful.
     */
    public static void move(File from, File to, Level l) throws IOException {
        // If move is successfull we are finished.
        if (from.renameTo(to)) {
        	log(l, "Moving: " + from.getAbsolutePath());
        	return;
        }
        // Else try copy/delete:
        try {
            log(l, "Unable to move file. Now using copy/delete.");
            copy(from, to, l);
        } catch (IOException ex) {
            log(l, ex.getLocalizedMessage());
            throw ex;
        }
        // Copy was successfull, now try to delete:
        if (!delete(from, true, l)) {
            //Unable to delete throw Exception...
            IOException ex = new IOException("Unable to delete source.");
            log(l, ex.getLocalizedMessage());
            throw ex;
        }
    }

    /**
     * Use this method to delete a single file or empty folder.
     * @param f the file to delte.
     * @return true if delete successful.
     */
    public static boolean delete(File f) {
	return delete(f, defaultLevel);
    }

    /**
     * Use this method to delete a single file or empty folder.
     * @param f the file to delte.
     * @return true if delete successful.
     */
    public static boolean delete(File f, Level l) {
    	log(l, "Deleting: " + f.getAbsolutePath());
    	return delete(f, false, l);
    }

    /**
     * Use this method to delete files or folders.
     * @param file the file or folder to delete.
     * @param recursive true to delete folders and their content recursively.
     * @return true if all delete operations were successful.
     */
    public static boolean delete(File file, boolean recursive) {
	return delete(file, recursive, defaultLevel);
    }

    /**
     * Use this method to delete files or folders.
     * @param file the file or folder to delete.
     * @param recursive true to delete folders and their content recursively.
     * @return true if all delete operations were successful.
     */
    public static boolean delete(File file, boolean recursive, Level l) {
        if (file.isFile()) {
            return file.delete();
        }
        if (recursive && file.isDirectory()) {
            for (File f: file.listFiles()) {
                if (f.isFile()) {
                	log(l, "Deleting: " + f.getAbsolutePath());
                    f.delete();
                } else if (f.isDirectory()) {
                    delete(f, true, l);
                }
            }
            return file.delete();
        }
        if (file.isDirectory()) {
            if (file.listFiles().length == 0) {
            	log(l, "Deleting: " + file.getAbsolutePath());
                return file.delete();
            } else {
                log(l, "Folder to delete is not empty! ("+
                        file.getAbsolutePath() +
                        ")\nPlease use recursive option to delete.");
                return false;
            }
        }
        return false;
    }

    private static void log(Level l, String msg) {
        AmuseLogger.write(FileOperations.class.getName(), l, msg);
    }

    private static boolean isParent(File possibleParent, File child) {
        File parent = child.getParentFile();
        while (parent != null) {
            if (possibleParent.equals(parent)) {
		return true;
	    }
            parent = parent.getParentFile();
        }
        return false;
    }
}
