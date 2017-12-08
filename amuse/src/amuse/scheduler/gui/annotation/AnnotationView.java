package amuse.scheduler.gui.annotation;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.controller.AnnotationController;
import amuse.scheduler.gui.navigation.HasCaption;
import amuse.scheduler.gui.navigation.HasLoadButton;
import amuse.scheduler.gui.navigation.HasSaveButton;
import net.miginfocom.swing.MigLayout;

/**
 * Assembles the different visual components for annotation purposes
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationView extends JPanel implements HasCaption, HasLoadButton, HasSaveButton{

	AnnotationUserInterfacePanel userInterfacePanel;
	JScrollBar horizontalScrollBar;
	AnnotationAudioSpectrumPanel audioSpectrumPanel;
	AnnotationCurrentTimePanel currentTimePanel;
	AnnotationVisualizationPanel visualizationPanel;
	AnnotationSelectionPanel selectionPanel;

	AnnotationController annotationController;

	public AnnotationView(AnnotationController pAnnotationController) {
		super();
		this.setLayout(new MigLayout("wrap 1", "[align center]"));
		
		annotationController = pAnnotationController;
		audioSpectrumPanel = new AnnotationAudioSpectrumPanel(annotationController);
		visualizationPanel = new AnnotationVisualizationPanel(annotationController);
		currentTimePanel = new AnnotationCurrentTimePanel(annotationController);
		selectionPanel = new AnnotationSelectionPanel(annotationController);
		userInterfacePanel = new AnnotationUserInterfacePanel();
		
		
		horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL){
			@Override
			public Dimension getPreferredSize(){
				return new Dimension(audioSpectrumPanel.getPreferredSize().width, super.getPreferredSize().height);
			}
		};
		horizontalScrollBar.setModel(audioSpectrumPanel.getHorizontalScrollBar().getModel());
		horizontalScrollBar.addAdjustmentListener(e -> {
			audioSpectrumPanel.getHorizontalScrollBar().setValue(horizontalScrollBar.getValue());
			visualizationPanel.getHorizontalScrollBar().setValue(horizontalScrollBar.getValue());
			currentTimePanel.getHorizontalScrollBar().setValue(horizontalScrollBar.getValue());
			audioSpectrumPanel.repaint();
		});
		horizontalScrollBar.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				//audioSpectrumPanel.repaint();
				visualizationPanel.repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseClicked(MouseEvent e) {}
		});
		
		// The visualizationPanel and selectionPanel are in competition regarding the height.
		// Therefore, encapsulate both in one JPanel
		JPanel panel = new JPanel(new MigLayout("wrap 1, insets 0", "[align center]"));
		panel.add(visualizationPanel, "hmax pref, growy");
		panel.add(selectionPanel, "hmin 50%, push, grow");
		
		this.add(userInterfacePanel, "hmin pref");
		this.add(horizontalScrollBar, "hmin pref");
		this.add(audioSpectrumPanel, "hmin pref");
		this.add(currentTimePanel, "hmin pref");
		this.add(panel, "push, grow");
	}
	
	public void resizePanels(){

		this.revalidate();
		this.getLayout().layoutContainer(this);
		audioSpectrumPanel.revalidate();
		currentTimePanel.revalidate();
		visualizationPanel.revalidate();
	}
	
	public AnnotationCurrentTimePanel getAnnotationCurrentTimePanel() {
		return currentTimePanel;
	}

	public AnnotationAudioSpectrumPanel getAnnotationAudioSpectrumPanel() {
		return audioSpectrumPanel;
	}
	
	public AnnotationVisualizationPanel getAnnotationVisualizationPanel(){
		return visualizationPanel;
	}

	public AnnotationSelectionPanel getAnnotationSelectionPanel() {
		return selectionPanel;
	}

	public void setCurrentTime(double millis) {
		currentTimePanel.setCurrentTime(millis);
		userInterfacePanel.setCurrentTime(millis);
	}
	
	public void setMouseTime(double millis){
		userInterfacePanel.setMouseTime(millis);
	}

	public void setMouseFreq(double freq) {
		userInterfacePanel.setMouseFreq(freq);
	}
	
	@Override
	public String getLoadButtonText() {
		return "Load";
	}

	@Override
	public String getCaption() {
		return "Annotation Editor";
	}

	@Override
	public void loadButtonClicked() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE)));
		fileChooser.setFileFilter(new FileNameExtensionFilter("", "mp3", "wav"));
		if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			annotationController.clearAnnotation();
			annotationController.loadMusic(fileChooser.getSelectedFile().getAbsolutePath());
		}
		
	}

	@Override
	public String getSaveButtonText() {
		return "Save";
	}

	@Override
	public void saveButtonClicked() {
		annotationController.saveAnnotation();
	}
	
	private class AnnotationUserInterfacePanel extends JPanel{

		private JLabel labelMouseTime, labelCurrentTime, labelMouseFreq;
		public AnnotationUserInterfacePanel(){
			super();
			this.setLayout(new MigLayout("", "[fill]"));
			
			
			/*
			 * Init Buttons
			 */
			JButton buttonPlay = new JButton("play");
			buttonPlay.addActionListener(e -> annotationController.playMusic());

			JButton buttonPause = new JButton("pause");
			buttonPause.addActionListener(e -> annotationController.pauseMusic());

			labelCurrentTime = new JLabel(""); 
			setCurrentTime(0.);
			
			labelMouseTime = new JLabel(""); 
			setMouseTime(0.);
			
			labelMouseFreq = new JLabel(""); 
			setMouseFreq(0.);
			
			JSlider sliderTimeZoom = new JSlider(JSlider.HORIZONTAL, 0, 80, 0);
			sliderTimeZoom.addChangeListener((ChangeEvent e) -> {
				audioSpectrumPanel.setPixelWidth(sliderTimeZoom.getValue() / 10. + 2);
				resizePanels();
			});
			
			JSlider sliderFreqZoom = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
			sliderFreqZoom.addChangeListener((ChangeEvent e) -> {
				audioSpectrumPanel.setPixelHeight((sliderFreqZoom.getValue() / 20.) + 1);
				resizePanels();
			});
			
			
			this.add(buttonPlay, "");
			this.add(buttonPause, "");
			this.add(labelCurrentTime, "");
			this.add(new JSeparator(SwingConstants.VERTICAL), "growy");
			this.add(labelMouseTime, "");
			this.add(labelMouseFreq, "");
			this.add(new JSeparator(SwingConstants.VERTICAL), "growy");
			this.add(new JLabel("Zoom Time:"), "split 2");
			this.add(sliderTimeZoom, "");
			this.add(new JSeparator(SwingConstants.VERTICAL), "growy");
			this.add(new JLabel("Zoom Frequency:"), "split 2");
			this.add(sliderFreqZoom, "");
		}
		
		public void setMouseFreq(double freq) {
			labelMouseFreq.setText(String.format("%.2f Hz)", freq));
		}

		public void setCurrentTime(double millis){
			labelCurrentTime.setText(String.format("Current Time: %02dm:%02ds,%03dms", (int) (millis / 60000), (int) ((millis % 60000) / 1000), (int) ((millis % 1000))));
		}
		
		public void setMouseTime(double millis){
			labelMouseTime.setText(String.format("Mouse Position: (%02dm:%02ds,%03dms, ", (int) (millis / 60000), (int) ((millis % 60000) / 1000), (int) ((millis % 1000))));
		}
	}
}