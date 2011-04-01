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
 * Creation date: 04.08.2008
 */
package amuse.scheduler.gui.logger;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Level;

/**
 * This class is used to create an write colored log events into a given JPanelAmuseLogger.
 * @author Clemens Waeltken
 */
final class ColoredLoggerEntry {

	private final String category;
	private final Level level;
	private final String message;
	private final Color color;

	// These static fields specify the color of each log level.
	private static final Color DEFAULT_COLOR = new Color(0, 0, 0);
	private static final Color DEBUG_COLOR = new Color(50, 100, 165);
	private static final Color INFO_COLOR = new Color(0, 130, 10);
	private static final Color WARN_COLOR = new Color(220, 145, 15);
	private static final Color ERROR_COLOR = new Color(230, 30, 50);
	private static final Color FATAL_COLOR = new Color(150, 70, 140);

	/**
	 * Creates a colored entry into the Amuse Logger.
	 * @param category Category of this logging entry.
	 * @param level Log level of this logging entry.
	 * @param message
	 */

	public ColoredLoggerEntry(String category, Level level, String message) {
		this.level = level;
		this.category = category;
		this.message = message;
		this.color = levelToColor(level);
	}

	/**
	 * Creates a colored entry into the Amuse Logger.
	 * @param level - log level
	 * @return according color.
	 */
	private Color levelToColor(Level level) {
		switch(level.toInt()) {
		case(Level.DEBUG_INT): return DEBUG_COLOR;	
		case(Level.INFO_INT): return INFO_COLOR;
		case(Level.WARN_INT): return WARN_COLOR;
		case(Level.ERROR_INT): return ERROR_COLOR;
		case(Level.FATAL_INT): return FATAL_COLOR;
		default: return DEFAULT_COLOR;
		}
	}

	/**
	 * @return - returns color according to this events LogLevel.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * writes colored string in given JPanelAmuseLogger.
	 */
	public void printEvent(JPanelAmuseLogger panelAmuseLogger) {
		panelAmuseLogger.append(getTimestamp() + " ", DEFAULT_COLOR);
		panelAmuseLogger.append(level.toString() + "\t", color);
		int l = level.toInt();
		if (l == Level.DEBUG_INT || l == Level.ERROR_INT || l == Level.FATAL_INT)
				panelAmuseLogger.append(category + ": ", color);
		panelAmuseLogger.append(message + "\n", color);
	}

	/**
	 * @return - returns formated timestamp.
	 */
	private String getTimestamp() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Date currentTime = new Date();
		return formatter.format(currentTime);
	}
}
