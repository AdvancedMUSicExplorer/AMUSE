package amuse.scheduler.gui.annotation.singlefile;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysBooleanValue;
import amuse.scheduler.gui.controller.SingleFileAnnotationController;

/**
 * Displays the amplitude spectrum of the audio file
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationAudioSpectrumPanel extends AnnotationScrollPane {

	BufferedImage audiospectrumImage, scaledAudiospectrumImageExtract;
	double pixelHeight, pixelWidth;
	double repeatWindowRelativeStart, repeatWindowRelativeEnd;
	Point posMouseOrigin;
	Rectangle rectViewOrigin;
	JLayeredPane layeredPane;
	JPanel currentTimePanel, repeatWindowPanel;

	public AnnotationAudioSpectrumPanel(SingleFileAnnotationController pAnnotationController) {
		super(pAnnotationController);
		scaledAudiospectrumImageExtract = null;
		pixelWidth = 2.;
		pixelHeight = 1.;
		audiospectrumImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		rectViewOrigin = getViewport().getViewRect();
		posMouseOrigin = new Point(0, 0);
		repeatWindowRelativeStart = 0.1;
		repeatWindowRelativeEnd = 0.9;
		
		repeatWindowPanel = new JPanel(){
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Dimension size = getSize();
				g.setColor(Color.WHITE);
				int startPixel = (int) (repeatWindowRelativeStart * size.getWidth());
				int endPixel = (int) (repeatWindowRelativeEnd * size.getWidth());
				Rectangle viewRect = getViewport().getViewRect();
				g.setColor(new Color(0.f, 0.f, 0.f, 0.5f));

				g.fillRect(viewRect.x, viewRect.y, startPixel - viewRect.x, size.height);
				g.fillRect(endPixel, viewRect.y, viewRect.width + viewRect.x - endPixel, viewRect.height);
			}
			
			@Override
			public Dimension getPreferredSize() {
				return getContentSize();
			}
		};
		repeatWindowPanel.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				final double THRESHOLD = 10.;
				double width = getContentSize().getWidth();
				if (Math.abs(repeatWindowRelativeStart * width - e.getX()) <= THRESHOLD) {
					setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				}
				else if (Math.abs(repeatWindowRelativeEnd * width - e.getX()) <= THRESHOLD) {
					setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
				}
				else if(e.getX() > repeatWindowRelativeStart * width && e.getX() < repeatWindowRelativeEnd * width){
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
				}
				else {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				int cursorType = getCursor().getType();
				double width = getContentSize().getWidth();
				double relativeX = e.getX() / width;
				if (cursorType == Cursor.E_RESIZE_CURSOR) {
					repeatWindowRelativeStart = Math.max(0., Math.min(repeatWindowRelativeEnd - 10. / width, relativeX));
				}
				else if (cursorType == Cursor.W_RESIZE_CURSOR) {
					repeatWindowRelativeEnd = Math.min(Math.max(repeatWindowRelativeStart + 10. / width, relativeX), 1.);
				}
				else if (cursorType == Cursor.MOVE_CURSOR){
					double repeatWindowRelativeWidth = repeatWindowRelativeEnd - repeatWindowRelativeStart;
					repeatWindowRelativeStart = relativeX - repeatWindowRelativeWidth / 2.;
					repeatWindowRelativeEnd = relativeX + repeatWindowRelativeWidth / 2.;
					if(repeatWindowRelativeStart < 0.){
						repeatWindowRelativeStart = 0.;
						repeatWindowRelativeEnd = repeatWindowRelativeWidth;
					}
					else if(repeatWindowRelativeEnd > 1.){
						repeatWindowRelativeEnd = 1.;
						repeatWindowRelativeStart = 1. - repeatWindowRelativeWidth;
					}
				}

				repeatWindowPanel.repaint();
				// Refresh the entries written in the lower part of the panel
				((AnnotationView) annotationController.getView()).getAnnotationSelectionPanel().repaint();
			}
		});
		repeatWindowPanel.setOpaque(false);
		repeatWindowPanel.setVisible(false);
		
		
		
		
		
		
		
		currentTimePanel = new JPanel(){
			
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.BLACK);
				g.drawLine(0, 0, 0, getHeight());
			}
			
			@Override
			public Dimension getPreferredSize() {
				return getContentSize();
			}
		};
		currentTimePanel.setOpaque(false);
		
		layeredPane = new JLayeredPane(){
			@Override
			public Dimension getPreferredSize() {
				return getContentSize();
			}
		};
		layeredPane.add(contentPanel, new Integer(1));
		layeredPane.add(currentTimePanel, new Integer(2));
		layeredPane.add(repeatWindowPanel, new Integer(3));
		
		this.setViewportView(layeredPane);
		
		contentPanel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mousePressed(MouseEvent e) {
				rectViewOrigin = getViewport().getViewRect();
				posMouseOrigin = new Point(e.getX() - (int) rectViewOrigin.getX(), e.getY() - (int) rectViewOrigin.getY());
			}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseClicked(MouseEvent e) {
				int totalWidth = getContentSize().width;
				double totalMillis = annotationController.getDurationInMs();
				double millisPerPixel = totalMillis / totalWidth;
				annotationController.seekInMusic(e.getX() * millisPerPixel);
			}
		});

		contentPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				double totalHeight = contentPanel.getHeight();
				double relativePos = (double) (totalHeight - 1.0 - e.getY()) / totalHeight;
				((AnnotationView) annotationController.getView()).setMouseFreq(relativePos * annotationController.getSampleRate() / 2);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				setCursor(new Cursor(Cursor.MOVE_CURSOR));
				contentPanel.scrollRectToVisible(new Rectangle(	(int) (rectViewOrigin.x 
						+ posMouseOrigin.getX()
						- e.getX()
						+ getViewport().getViewRect().x),
						(int) (rectViewOrigin.y
								+ posMouseOrigin.getY()
								- e.getY()
								+ getViewport().getViewRect().y),
						rectViewOrigin.width, 
						rectViewOrigin.height));
			}
		});

		this.addMouseWheelListener(e -> repaint());

		this.getVerticalScrollBar().addAdjustmentListener(e -> {
			scaledAudiospectrumImageExtract = null;
			repaint();
		});
		
		this.getHorizontalScrollBar().addAdjustmentListener(e -> {
			scaledAudiospectrumImageExtract = null;
			repaint();
		});
	}

	@Override
	public Dimension getPreferredSize(){
		return new Dimension(getContentSize().width + getVerticalScrollBar().getWidth() + 3, 3 + 256); // Height = 3 for the border + height of an audiospectrum image extracted with windowsize 512
	}

	public void enableRepeating(Boolean enabled){
		double width = getContentSize().getWidth();
		repeatWindowRelativeStart = Math.max(0., (currentTimePanel.getX() - 100.) / width);
		repeatWindowRelativeEnd = Math.min(1., (currentTimePanel.getX() + 100.) / width);
		repeatWindowPanel.setVisible(enabled);
	}
	
	public void drawContent(Graphics g) {
		if(audiospectrumImage.getHeight() > 1){
			Point offset = this.getViewport().getViewPosition();
			if(scaledAudiospectrumImageExtract == null){
				Dimension size  = this.getViewport().getSize();
				// To reduce computation time and memory consumption, only the current window gets scaled and drawn.
				AffineTransform affineTransform = AffineTransform.getScaleInstance(pixelWidth, pixelHeight);
				
				//Add two scaled up pixels to compensate for rounding errors
				scaledAudiospectrumImageExtract = new BufferedImage(size.width + (int) (2 * pixelWidth), size.height +  (int) (2 * pixelHeight), BufferedImage.TYPE_INT_RGB);
				
				int y = (int) Math.max((offset.y / pixelHeight), 0.);
				int x = (int) Math.max((offset.x / pixelWidth), 0.);
				scaledAudiospectrumImageExtract.createGraphics().drawRenderedImage(audiospectrumImage.getSubimage(
						x,
						y,
						(int) Math.min((size.width / pixelWidth) + 2, audiospectrumImage.getWidth() - x),
						(int) Math.min((size.height / pixelHeight) + 2, audiospectrumImage.getHeight() - y)),
						affineTransform);
				
			}
			g.drawImage(scaledAudiospectrumImageExtract, offset.x - (int) (offset.x % pixelWidth), offset.y - (int) (offset.y % pixelHeight), null);
		}
	}
	
	public void repaintCurrentTimeBeam(){
		Dimension preferredSize = getContentSize();
		double currentMs = annotationController.getCurrentMs();
		double duration = annotationController.getDurationInMs();
		double relativeX = currentMs / duration;
		currentTimePanel.setBounds((int) (relativeX * preferredSize.getWidth()),0,1, (int) (preferredSize.height * pixelHeight));
		//currentTimePanel.repaint();
		
		if(repeatWindowPanel.isVisible()){
			if(relativeX < repeatWindowRelativeStart || relativeX >= repeatWindowRelativeEnd){
				double repeatWindowStartTime = repeatWindowRelativeStart * duration;
				annotationController.seekInMusic(repeatWindowStartTime);
			}
		}
	}

	public void setPixelHeight(double pPixelHeight){
		scaledAudiospectrumImageExtract = null;
		
		Rectangle viewRectOrigin = getViewport().getViewRect();
		Point2D relativeViewPointOrigin = new Point2D.Double((viewRectOrigin.x + 0.5 * viewRectOrigin.width) / getContentSize().width,
				(viewRectOrigin.y + 1.0 * viewRectOrigin.height) / getContentSize().height);
		
		pixelHeight = pPixelHeight;
		repaint();
		
		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
				Rectangle viewRect = getViewport().getViewRect();
				Point2D relativeMiddleViewPoint = new Point2D.Double(relativeViewPointOrigin.getX() * getContentSize().width,
						relativeViewPointOrigin.getY() * getContentSize().height);
				contentPanel.scrollRectToVisible(new Rectangle( (int) (relativeMiddleViewPoint.getX() - 0.5 * viewRect.width),
						(int) Math.ceil(relativeMiddleViewPoint.getY() - 1.0 * viewRect.height),
						viewRect.width,
						viewRect.height));
				repaintCurrentTimeBeam(); // The height of the time beam needs to be adjusted
	        }
		});
	}

	@Override
	public Dimension getContentSize() {
		int width = (int) (audiospectrumImage.getWidth() * pixelWidth);
		int height = (int) (audiospectrumImage.getHeight() * pixelHeight);
		contentPanel.setBounds(0,0,width, height);
		repeatWindowPanel.setBounds(0,0,width, height);
		return new Dimension(width, height);
	}

	public void setPixelWidth(double pPixelWidth){
		scaledAudiospectrumImageExtract = null;
		
		Rectangle oldViewRect = getViewport().getViewRect();
		Dimension oldSize = getContentSize();
		Point2D relativeMiddleViewPoint = new Point2D.Double((oldViewRect.x + 0.5 * oldViewRect.width) / oldSize.width,
				(oldViewRect.y + 0.5 * oldViewRect.height) / oldSize.height);

		pixelWidth = pPixelWidth;
		repaint();

		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				Rectangle newViewRect = getViewport().getViewRect();
				Dimension newSize = getContentSize();
				Point2D absoluteMiddleViewPoint = new Point2D.Double(relativeMiddleViewPoint.getX() * newSize.width,
						relativeMiddleViewPoint.getY() * newSize.height);
				contentPanel.scrollRectToVisible(new Rectangle( (int) (absoluteMiddleViewPoint.getX() - 0.5 * newViewRect.width),
						(int) (absoluteMiddleViewPoint.getY() - 0.5 * newViewRect.height),
						newViewRect.width,
						newViewRect.height));
			}
		});

	}

	public void setAudioSpectrumImage(BufferedImage audiospectrumImage) {
		currentTimePanel.setVisible(AmusePreferences.getBoolean(KeysBooleanValue.MARK_CURRENT_TIME_IN_ANNOTATION_AUDIOSPECTRUM));
		this.audiospectrumImage = audiospectrumImage;
	}

	public void normalizeCurrentView() {
		if(audiospectrumImage.getHeight() > 1){
			Point offset = this.getViewport().getViewPosition();
			Dimension size  = this.getViewport().getSize();
			// To reduce computation time and memory consumption, only the current window gets scaled and drawn.
			
			int x = (int) Math.max((offset.x / pixelWidth), 0.);
			int y = (int) Math.max((offset.y / pixelHeight), 0.);
			
			//Add two scaled up pixels to compensate for rounding errors
			BufferedImage subImage = new BufferedImage(
					(int) Math.min((size.width / pixelWidth) + 2, audiospectrumImage.getWidth() - x),
					(int) Math.min((size.height / pixelHeight) + 2, audiospectrumImage.getHeight() - y),
					BufferedImage.TYPE_INT_RGB);
			subImage.getGraphics().drawImage(audiospectrumImage.getSubimage(
					x,
					y,
					(int) Math.min((size.width / pixelWidth) + 2, audiospectrumImage.getWidth() - x),
					(int) Math.min((size.height / pixelHeight) + 2, audiospectrumImage.getHeight() - y)), 0, 0, null);
			float[][] value = new float[subImage.getWidth()][subImage.getHeight()];
			float minValue = Float.POSITIVE_INFINITY;
			float maxValue = Float.NEGATIVE_INFINITY;
			for(int i = 0; i < subImage.getWidth(); i++){
				for(int j = 0; j < subImage.getHeight(); j++){
					Color color = new Color(subImage.getRGB(i, j));
					float[] hsbvals = new float[3];
					Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbvals);
					// hue = 0.666f - 0.666f * (float) value
					value[i][j] = (hsbvals[0] -0.666f)/ (-0.666f);
					minValue = Math.min(minValue, value[i][j]);
					maxValue = Math.max(maxValue, value[i][j]);
				}
			}
			
			Graphics g = subImage.getGraphics();
			for(int i = 0; i < subImage.getWidth(); i++){
				for(int j = 0; j < subImage.getHeight(); j++){
					float hue = (value[i][j] - minValue) / (maxValue - minValue);
					hue = 0.666f - 0.666f * hue;
					g.setColor(new Color(Color.HSBtoRGB(hue, 1.f, 1.f)));
					g.fillRect(i, j, 1, 1);
				}
			}
			
			
			

			scaledAudiospectrumImageExtract = new BufferedImage(size.width + (int) (2 * pixelWidth), size.height +  (int) (2 * pixelHeight), BufferedImage.TYPE_INT_RGB);
			AffineTransform affineTransform = AffineTransform.getScaleInstance(pixelWidth, pixelHeight);
			scaledAudiospectrumImageExtract.createGraphics().drawRenderedImage(subImage, affineTransform);
			contentPanel.repaint();
		}
	}

	public void scrollToTime(int millis) {
		double totalMillis = annotationController.getDurationInMs();
		int totalWidth = contentPanel.getWidth();
		Rectangle visibleRect = contentPanel.getVisibleRect();
		contentPanel.scrollRectToVisible(new Rectangle((int) (totalWidth * (millis / totalMillis)), visibleRect.y, visibleRect.width, visibleRect.height));
	}


}
