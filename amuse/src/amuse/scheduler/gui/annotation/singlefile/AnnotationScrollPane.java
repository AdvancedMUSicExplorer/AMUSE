package amuse.scheduler.gui.annotation.singlefile;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import amuse.scheduler.gui.controller.SingleFileAnnotationController;

/**
 * Displays a panel that is possible bigger than the frame itself inside a JScrollPane.
 * Used to align the {@link AnnotationAudioSpectrumPanel} and the {@link AnnotationVisualizationPanel}
 * @author Frederik Heerde
 * @version $Id$
 */
public abstract class AnnotationScrollPane extends JScrollPane{
	SingleFileAnnotationController annotationController;
	JPanel contentPanel;
	
	public AnnotationScrollPane(SingleFileAnnotationController pAnnotationController) {
		super(VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
		contentPanel = new JPanel() {

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawContent(g);
			}

			@Override
			public Dimension getPreferredSize() {
				return getContentSize();
			}

		};
		contentPanel.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				int totalWidth = getContentSize().width;
				double totalMillis = annotationController.getDurationInMs();
				double millisPerPixel = totalMillis / totalWidth;
				((AnnotationView) annotationController.getView()).setMouseTime(e.getX() * millisPerPixel);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {}
		});
		
		
		this.setViewportView(contentPanel);
		annotationController = pAnnotationController;
		
		// Disable horizontal scrollbar
		this.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));
		
		this.setBackground(new Color(200, 200, 200));
	}
	
	@Override
	public void revalidate(){
		if(contentPanel != null){
			contentPanel.revalidate();
		}
		super.revalidate();
	}
	
	public abstract Dimension getContentSize();
	
	public abstract void drawContent(Graphics g);
}
