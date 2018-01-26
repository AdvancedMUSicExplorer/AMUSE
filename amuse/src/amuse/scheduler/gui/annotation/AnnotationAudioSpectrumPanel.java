package amuse.scheduler.gui.annotation;

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

import javax.swing.SwingUtilities;

import amuse.scheduler.gui.controller.AnnotationController;

/**
 * Displays the amplitude spectrum of the audio file
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationAudioSpectrumPanel extends AnnotationScrollPane {

	BufferedImage audiospectrumImage;
	double pixelHeight, pixelWidth;
	Point posMouseOrigin;
	Rectangle rectViewOrigin;

	public AnnotationAudioSpectrumPanel(AnnotationController pAnnotationController) {
		super(pAnnotationController);
		pixelWidth = 2;
		pixelHeight = 1;
		audiospectrumImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		rectViewOrigin = getViewport().getViewRect();
		posMouseOrigin = new Point(0, 0);

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

		this.getVerticalScrollBar().addAdjustmentListener(e -> repaint());
	}

	@Override
	public Dimension getPreferredSize(){
		return new Dimension(getContentSize().width, audiospectrumImage.getHeight() + 3);
	}

	public void drawContent(Graphics g) {
		if(audiospectrumImage.getHeight() > 1){
			Point offset = this.getViewport().getViewPosition();
			Dimension size  = this.getViewport().getSize();
			// To reduce computation time and memory consumption, only the current window gets scaled and drawn.
			AffineTransform affineTransform = AffineTransform.getScaleInstance(pixelWidth, pixelHeight);
			
			//Add two scaled up pixels to compensate for rounding errors
			BufferedImage scaledAudiospectrumImage = new BufferedImage(size.width + (int) (2 * pixelWidth), size.height +  (int) (2 * pixelHeight), BufferedImage.TYPE_INT_RGB);
			
			int y = (int) Math.max((offset.y / pixelHeight), 0.);
			int x = (int) Math.max((offset.x / pixelWidth), 0.);
			scaledAudiospectrumImage.createGraphics().drawRenderedImage(audiospectrumImage.getSubimage(
					x,
					y,
					(int) Math.min((size.width / pixelWidth) + 2, audiospectrumImage.getWidth() - x),
					(int) Math.min((size.height / pixelHeight) + 2, audiospectrumImage.getHeight() - y)),
					affineTransform);
			
			g.drawImage(scaledAudiospectrumImage, offset.x - (int) (offset.x % pixelWidth), offset.y - (int) (offset.y % pixelHeight), null);
		}
	}

	public void setPixelHeight(double pPixelHeight){
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
	        }
		});
	}

	@Override
	public Dimension getContentSize() {
		return new Dimension((int) (audiospectrumImage.getWidth() * pixelWidth), (int) (audiospectrumImage.getHeight() * pixelHeight));
	}

	public void setPixelWidth(double pPixelWidth){
		Rectangle viewRectOrigin = getViewport().getViewRect();
		Point2D relativeMiddleViewPointOrigin = new Point2D.Double((viewRectOrigin.x + 0.5 * viewRectOrigin.width) / getContentSize().width,
				(viewRectOrigin.y + 0.5 * viewRectOrigin.height) / getContentSize().height);

		pixelWidth = pPixelWidth;
		repaint();

		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
				Rectangle viewRect = getViewport().getViewRect();
				Point2D relativeMiddleViewPoint = new Point2D.Double(relativeMiddleViewPointOrigin.getX() * getContentSize().width,
						relativeMiddleViewPointOrigin.getY() * getContentSize().height);
				contentPanel.scrollRectToVisible(new Rectangle( (int) (relativeMiddleViewPoint.getX() - 0.5 * viewRect.width),
						(int) (relativeMiddleViewPoint.getY() - 0.5 * viewRect.height),
						viewRect.width,
						viewRect.height));
	        }
		});

	}

	public void setAudioSpectrumImage(BufferedImage audiospectrumImage) {
		this.audiospectrumImage = audiospectrumImage;
	}

	public void normalizeCurrentView() {
		if(audiospectrumImage.getHeight() > 1){
			Point offset = this.getViewport().getViewPosition();
			Dimension size  = this.getViewport().getSize();
			// To reduce computation time and memory consumption, only the current window gets scaled and drawn.
			
			//Add two scaled up pixels to compensate for rounding errors
			int x = (int) Math.max((offset.x / pixelWidth), 0.);
			int y = (int) Math.max((offset.y / pixelHeight), 0.);
			/*BufferedImage subImage = audiospectrumImage.getSubimage(
					x,
					y,
					(int) Math.min((size.width / pixelWidth) + 2, audiospectrumImage.getWidth() - x),
					(int) Math.min((size.height / pixelHeight) + 2, audiospectrumImage.getHeight() - y));
			subImage = new BufferedImage(subImage.getColorModel(), subImage.copyData(null), subImage.isAlphaPremultiplied(), null);
			*/
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
			
			
			
			BufferedImage scaledAudiospectrumImage = new BufferedImage(size.width + (int) (2 * pixelWidth), size.height +  (int) (2 * pixelHeight), BufferedImage.TYPE_INT_RGB);
			AffineTransform affineTransform = AffineTransform.getScaleInstance(pixelWidth, pixelHeight);
			scaledAudiospectrumImage.createGraphics().drawRenderedImage(subImage, affineTransform);

			contentPanel.getGraphics().drawImage(scaledAudiospectrumImage, offset.x - (int) (offset.x % pixelWidth), offset.y - (int) (offset.y % pixelHeight), null);
		}
	}

	public void scrollToTime(int millis) {
		double totalMillis = annotationController.getDurationInMs();
		int totalWidth = contentPanel.getWidth();
		Rectangle visibleRect = contentPanel.getVisibleRect();
		contentPanel.scrollRectToVisible(new Rectangle((int) (totalWidth * (millis / totalMillis)), visibleRect.y, visibleRect.width, visibleRect.height));
	}


}
