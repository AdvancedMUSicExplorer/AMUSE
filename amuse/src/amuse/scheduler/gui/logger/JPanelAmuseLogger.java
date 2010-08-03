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
 * Creation date: 29.07.2008
 */
package amuse.scheduler.gui.logger;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentEvent;

import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Level;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysIntValue;
import amuse.util.AmuseLogger;
import amuse.util.LoggerListener;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentListener;
import javax.swing.JScrollPane;

/**
 * @author Clemens Waeltken
 *
 */
public class JPanelAmuseLogger extends JScrollPane implements LoggerListener {

    private static final long serialVersionUID = 1900018463030143312L;
    private static JTextPane txtArea = new JTextPane();

    public JPanelAmuseLogger() {
	super(txtArea);
	// Setup text style
	txtArea.setFont(new Font("Arial", Font.BOLD, 12));
	txtArea.setEditable(false);
    }

    public void startListening() {
	// Start listening on AmuseLogger
	AmuseLogger.addListener(this);
	txtArea.addComponentListener(new ComponentListenerImpl(this.getViewport()));
    }

    public synchronized void append(String appendString, Color textColor) {
	Document doc = txtArea.getDocument();
	SimpleAttributeSet textAttributes = new SimpleAttributeSet();
	StyleConstants.setForeground(textAttributes, textColor);
	try {
	    doc.insertString(doc.getLength(), appendString, textAttributes);
	    // append the formatted string to the document
	} catch (BadLocationException ex) {
	    System.out.println("Error writing to the text pane");
	}
    }

    @Override
    public void receiveLoggerEvent(String category, Level level, String message) {
	ColoredLoggerEntry entry = new ColoredLoggerEntry(category, level,
		message);
	if (isToBeDisplayed(level)) {
	    entry.printEvent(this);
	}
    }

    /**
     * @param entry
     * @return
     */
    private boolean isToBeDisplayed(Level level) {
	int logLevel = AmusePreferences.getInt(KeysIntValue.GUI_LOG_LEVEL);
	boolean display = true;
	if (logLevel == 1) {
	    if (level.toInt() == Level.DEBUG_INT) {
		display = false;
	    }
	} else if (logLevel == 2) {
	    if (level.toInt() == Level.DEBUG_INT) {
		display = false;
	    } else if (level.toInt() == Level.ERROR_INT) {
		display = false;
	    }
	}
	return display;
    }

    public enum logLevels {

	Debug,
	Warn,
	Quiet;
    }

    private static class ComponentListenerImpl implements ComponentListener {
	private final JViewport vp;

	private ComponentListenerImpl(JViewport viewport) {
	    this.vp = viewport;
	}

	@Override
	public void componentResized(ComponentEvent e) {
	    Rectangle visRect = vp.getViewRect();
	    Dimension viewDim = vp.getViewSize();
	    Rectangle rect = new Rectangle(0, (int) (viewDim.getHeight() - visRect.getHeight()),
		    (int) visRect.getWidth(), (int) visRect.getHeight());
	    vp.scrollRectToVisible(rect);
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
    }
}
