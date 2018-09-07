package amuse.scheduler.gui.annotation.singlefile;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

import amuse.scheduler.gui.controller.SingleFileAnnotationController;

/**
 * Displays a slider that is used to indicate the currently playing moment of the audio file and to change it.
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationCurrentTimePanel extends JScrollPane{

	JSlider slider;
	SingleFileAnnotationController annotationController;
	ChangeListener scrollListener;
	
	public AnnotationCurrentTimePanel(SingleFileAnnotationController pAnnotationController) {
		super(VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
		annotationController = pAnnotationController;
		slider = new JSlider(JSlider.HORIZONTAL, 0, 10000, 0){
			
			@Override
			public Dimension getPreferredSize() {
				int width = ((AnnotationView) annotationController.getView()).getAnnotationAudioSpectrumPanel().getContentSize().width; 
				double relativeValue = slider.getValue() / (double) slider.getMaximum();
				slider.setMaximum(width);
				slider.setValue((int) (relativeValue * slider.getMaximum()));
				return new Dimension(width + 14, 18);//Add 14 to ensure that the actual slider will span over the whole width
			}
			
			@Override 
			public Dimension getMinimumSize(){
				return getPreferredSize();
			}
		};
		slider.addMouseListener(new MouseListener() {
					
				@Override
				public void mouseReleased(MouseEvent e) {
					int newValue = ((BasicSliderUI) slider.getUI()).valueForXPosition(e.getX());
					double totalTime = annotationController.getDurationInMs();
					double selectedTime = (totalTime * newValue) / slider.getMaximum();
					annotationController.seekInMusic(selectedTime);
				}
				
				@Override
				public void mousePressed(MouseEvent e) { }
				
				@Override
				public void mouseExited(MouseEvent e) { }
				
				@Override
				public void mouseEntered(MouseEvent e) { }
				
				@Override
				public void mouseClicked(MouseEvent e) { }
		});

		JPanel sliderPanel = new JPanel();
		sliderPanel.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				int totalWidth = ((AnnotationView) annotationController.getView()).getAnnotationAudioSpectrumPanel().getContentSize().width;
				double totalMillis = annotationController.getDurationInMs();
				double millisPerPixel = totalMillis / totalWidth;
				((AnnotationView) annotationController.getView()).setMouseTime((getX() + e.getX()) * millisPerPixel);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {}
		});
		sliderPanel.add(slider);
		this.setViewportView(sliderPanel);
		
		// Disable Scrollbars
		//this.getVerticalScrollBar().setPreferredSize(new Dimension(0,0));
		this.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));
		this.setWheelScrollingEnabled(false);

		this.getHorizontalScrollBar().setModel(new DefaultBoundedRangeModel(){
			@Override
			public void setValue(int n){
				// This should align the Slider exactly with the other Panels.
				super.setValue((int)(n + 12));
			}
		});
		scrollListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Rectangle visibleRect = slider.getVisibleRect();
				int totalWidth = ((AnnotationView) annotationController.getView()).getAnnotationAudioSpectrumPanel().getContentSize().width;
				double totalMillis = annotationController.getDurationInMs();
				double millisPerPixel = totalMillis / totalWidth;
				double sliderPos = slider.getValue() * totalMillis / slider.getMaximum() / millisPerPixel;
				if(sliderPos > visibleRect.getX() + visibleRect.getWidth()){
					annotationController.scrollToTime((int) annotationController.getCurrentMs()); 
				}
				
			}
		};
		
	}
	
	@Override
	public void revalidate(){
		if(slider != null){
			slider.revalidate();
		}
		super.revalidate();
	}
	
	public void enableScrolling(boolean b){
		if(b){
			for(ChangeListener listener:slider.getChangeListeners()){
				if(listener.equals(scrollListener)){
					return;
				}
			}
			slider.addChangeListener(scrollListener);
		}
		else{
			slider.removeChangeListener(scrollListener);
		}
	}
	
	public void setCurrentTime(double millis){
		slider.setValue((int)(slider.getMaximum() * millis / annotationController.getDurationInMs()));
	}
}
