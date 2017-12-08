package amuse.scheduler.gui.annotation;

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
				((AnnotationView) annotationController.getView()).setMouseFreq(relativePos * annotationController.getSampleRate());
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
			AffineTransform affineTransform = AffineTransform.getScaleInstance(pixelWidth, pixelHeight);
			BufferedImage scaledAudiospectrumImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
			scaledAudiospectrumImage.createGraphics().drawRenderedImage(audiospectrumImage.getSubimage((int) (offset.x / pixelWidth),
					(int) (offset.y / pixelHeight),
					(int) Math.ceil(size.width / pixelWidth),
					(int) Math.ceil(size.height / pixelHeight))
					, affineTransform);
			g.drawImage(scaledAudiospectrumImage, offset.x, offset.y, null);
		}
	}

	public void setPixelHeight(double pPixelHeight){
		Rectangle viewRectOrigin = getViewport().getViewRect();
		Point2D relativeViewPointOrigin = new Point2D.Double((viewRectOrigin.x + 0.5 * viewRectOrigin.width) / getContentSize().width,
				(viewRectOrigin.y + 1.0 * viewRectOrigin.height) / getContentSize().height);
		pixelHeight = pPixelHeight;
		repaint();


		Rectangle viewRect = getViewport().getViewRect();
		Point2D relativeMiddleViewPoint = new Point2D.Double(relativeViewPointOrigin.getX() * getContentSize().width,
				relativeViewPointOrigin.getY() * getContentSize().height);
		contentPanel.scrollRectToVisible(new Rectangle( (int) (relativeMiddleViewPoint.getX() - 0.5 * viewRect.width),
				(int) Math.ceil(relativeMiddleViewPoint.getY() - 1.0 * viewRect.height),
				viewRect.width,
				viewRect.height));
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


		Rectangle viewRect = getViewport().getViewRect();
		Point2D relativeMiddleViewPoint = new Point2D.Double(relativeMiddleViewPointOrigin.getX() * getContentSize().width,
				relativeMiddleViewPointOrigin.getY() * getContentSize().height);
		contentPanel.scrollRectToVisible(new Rectangle( (int) (relativeMiddleViewPoint.getX() - 0.5 * viewRect.width),
				(int) (relativeMiddleViewPoint.getY() - 0.5 * viewRect.height),
				viewRect.width,
				viewRect.height));

	}

	public void setAudioSpectrumImage(BufferedImage audiospectrumImage) {
		this.audiospectrumImage = audiospectrumImage;
	}


}
