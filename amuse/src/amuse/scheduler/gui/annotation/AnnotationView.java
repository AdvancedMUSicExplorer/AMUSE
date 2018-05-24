package amuse.scheduler.gui.annotation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
	JLabel loadPanelLabel;
	final static String CONTENTPANEL = "ContentPanel";
	final static String LOADPANEL = "LoadPanel";

	AnnotationController annotationController;

	public AnnotationView(AnnotationController pAnnotationController) {
		super();
		this.setLayout(new CardLayout());
		JPanel contentPanel = new JPanel(new MigLayout("wrap 1", "[align center]"));
		annotationController = pAnnotationController;
		audioSpectrumPanel = new AnnotationAudioSpectrumPanel(annotationController);
		selectionPanel = new AnnotationSelectionPanel(annotationController);
		currentTimePanel = new AnnotationCurrentTimePanel(annotationController);
		userInterfacePanel = new AnnotationUserInterfacePanel();
		visualizationPanel = new AnnotationVisualizationPanel(annotationController, selectionPanel.getAttributeEntryList());
		
		
		horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL){
			@Override
			public Dimension getPreferredSize(){
				return new Dimension(audioSpectrumPanel.getPreferredSize().width, super.getPreferredSize().height);
			}
		};
		horizontalScrollBar.setModel(audioSpectrumPanel.getHorizontalScrollBar().getModel());
		horizontalScrollBar.addAdjustmentListener(e -> {
			visualizationPanel.getHorizontalScrollBar().setValue(horizontalScrollBar.getValue());
			currentTimePanel.getHorizontalScrollBar().setValue(horizontalScrollBar.getValue());
			visualizationPanel.repaint();
		});
		
		// The visualizationPanel and selectionPanel are in competition regarding the height.
		// Therefore, encapsulate both in one JPanel
		JPanel panel = new JPanel(new MigLayout("wrap 1, insets 0, fill", "[align center]"));
		panel.add(visualizationPanel, "hmax pref, growy");
		panel.add(selectionPanel, "push, grow");
		
		contentPanel.add(userInterfacePanel, "hmin pref");
		contentPanel.add(horizontalScrollBar, "hmin pref");
		contentPanel.add(audioSpectrumPanel, "hmin pref");
		contentPanel.add(currentTimePanel, "hmin pref");
		contentPanel.add(panel, "push, grow");
		
		JPanel loadPanel = new JPanel(new BorderLayout());
		loadPanelLabel = new JLabel("Load a track to start your annotation!");
		loadPanelLabel.setHorizontalAlignment(JLabel.CENTER);
		loadPanel.add(loadPanelLabel, BorderLayout.CENTER);
		
		this.add(contentPanel, CONTENTPANEL);
		this.add(loadPanel, LOADPANEL);
		((CardLayout) this.getLayout()).show(this, LOADPANEL);
	}
	
	public void resizePanels(){

		this.revalidate();
		this.getLayout().layoutContainer(this);
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
	
	public AnnotationUserInterfacePanel getAnnotationUserInterfacePanel(){
		return userInterfacePanel;
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
		return "Load Track";
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
			loadPanelLabel.setText("Loading...");
			((CardLayout) getLayout()).show(AnnotationView.this, LOADPANEL);
			SwingUtilities.invokeLater(new Runnable() {
		        public void run() {
					annotationController.clearAnnotation();
					annotationController.loadMusic(fileChooser.getSelectedFile().getAbsolutePath());
					((CardLayout) getLayout()).show(AnnotationView.this, CONTENTPANEL);
				}
			});
		}
		
	}

	@Override
	public String getSaveButtonText() {
		return "Save Annotation";
	}

	@Override
	public void saveButtonClicked() {
		annotationController.saveAnnotation();
	}
	
	public class AnnotationUserInterfacePanel extends JPanel{

		private JLabel labelMouseTime, labelCurrentTime, labelMouseFreq;
		private ImageIcon iconPlay, iconPause;
		private JButton buttonPlayPause;
		public AnnotationUserInterfacePanel(){
			super();
			this.setLayout(new MigLayout("", "[fill]"));
			
			
			/*
			 * Init Buttons
			 */
			
			// Init Icons for the play/ pause button
			try {
				String path = "jar:file:lib/jlfgr-1_0.jar!/toolbarButtonGraphics/media/Play16.gif";
				InputStream is = ((JarURLConnection)new URL(path).openConnection()).getInputStream();
				iconPlay = new ImageIcon(ImageIO.read(is));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				String path = "jar:file:lib/jlfgr-1_0.jar!/toolbarButtonGraphics/media/Pause16.gif";
				InputStream is = ((JarURLConnection)new URL(path).openConnection()).getInputStream();
				iconPause = new ImageIcon(ImageIO.read(is));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			buttonPlayPause = new JButton(){
				@Override
				public void setIcon(Icon icon){
					super.setIcon(icon);
					
					// When the icon is changed, the tool tip text needs to change, too.
					if(icon.equals(iconPlay)){
						buttonPlayPause.setToolTipText("Play");
					}
					else{
						buttonPlayPause.setToolTipText("Pause");
					}
				}
			};
			buttonPlayPause.setIcon(iconPlay);
			buttonPlayPause.addActionListener(e -> {
				if(annotationController.isMusicPaused()){
					annotationController.playMusic();
				}
				else{
					annotationController.pauseMusic();
				}
				this.refreshButtonPlayPauseIcon();
			});

			labelCurrentTime = new JLabel(""); // The text of the label is set in the method setCurrentTime
			setCurrentTime(0.);
			
			labelMouseTime = new JLabel(""); // The text of the label is set in the method setMouseTime
			setMouseTime(0.);
			
			labelMouseFreq = new JLabel(""); // The text of the label is set in the method setMouseFreq
			setMouseFreq(0.);
			
			JCheckBox checkBoxAutoScroll = new JCheckBox("");
			checkBoxAutoScroll.setToolTipText("Scroll the audio spectrum automatically while the music plays");
			checkBoxAutoScroll.addChangeListener(e -> {
				currentTimePanel.enableScrolling(checkBoxAutoScroll.isSelected());
			});
			
			JSlider sliderTimeZoom = new JSlider(JSlider.HORIZONTAL, 0, 99, 19);
			sliderTimeZoom.setToolTipText("Horizontal extent of the audio spectrum");
			sliderTimeZoom.addChangeListener((ChangeEvent e) -> {
				audioSpectrumPanel.setPixelWidth(sliderTimeZoom.getValue() / 10. + 0.1);
				resizePanels();
			});
			
			JSlider sliderFreqZoom = new JSlider(JSlider.HORIZONTAL, 5, 100, 5);
			sliderFreqZoom.setToolTipText("Vertical extent of the audio spectrum");
			sliderFreqZoom.addChangeListener((ChangeEvent e) -> {
				audioSpectrumPanel.setPixelHeight((sliderFreqZoom.getValue() / 5.));
				resizePanels();
			});
			
			JButton normalizeCurrentViewButton = new JButton("Normalize Window");
			normalizeCurrentViewButton.setToolTipText("Normalize the audio spectrum for the currently visible part");
			normalizeCurrentViewButton.addActionListener(e -> audioSpectrumPanel.normalizeCurrentView());
			
			
			ImageIcon iconRedo = null;
			try {
				String path = "jar:file:lib/jlfgr-1_0.jar!/toolbarButtonGraphics/general/Redo16.gif";
				InputStream is = ((JarURLConnection)new URL(path).openConnection()).getInputStream();
				iconRedo = new ImageIcon(ImageIO.read(is));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			JButton redoButton = annotationController.getUndoRedoManager().getRedoButton();
			redoButton.setIcon(iconRedo);
			
			ImageIcon iconUndo = null;
			try {
				String path = "jar:file:lib/jlfgr-1_0.jar!/toolbarButtonGraphics/general/Undo16.gif";
				InputStream is = ((JarURLConnection)new URL(path).openConnection()).getInputStream();
				iconUndo = new ImageIcon(ImageIO.read(is));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			JButton undoButton = annotationController.getUndoRedoManager().getUndoButton();
			undoButton.setIcon(iconUndo);
			
			JCheckBox checkBoxRepeatWindow = new JCheckBox("");
			checkBoxRepeatWindow.setToolTipText("Enable the repitition of a time window");
			checkBoxRepeatWindow.addActionListener(e -> audioSpectrumPanel.enableRepeating(checkBoxRepeatWindow.isSelected()));
			
			
			this.add(buttonPlayPause, "");
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
			this.add(normalizeCurrentViewButton, "");
			this.add(new JLabel("Keep Scrolling:"), "split 2");
			this.add(checkBoxAutoScroll, "");
			this.add(new JLabel("Repeat Window:"), "split 2");
			this.add(checkBoxRepeatWindow);
			this.add(undoButton, "");
			this.add(redoButton, "");
		}
		
		public void refreshButtonPlayPauseIcon(){
			if(annotationController.isMusicPaused()){
				buttonPlayPause.setIcon(iconPlay);
			}
			else{
				buttonPlayPause.setIcon(iconPause);
			}
		}
		
		public void setMouseFreq(double freq) {
			labelMouseFreq.setText(String.format(Locale.ROOT, "%.2fHz)", freq));
		}

		public void setCurrentTime(double millis){
			labelCurrentTime.setText(String.format(Locale.ROOT, "Current Time: %02dm:%02ds.%03dms", (int) (millis / 60000), (int) ((millis % 60000) / 1000), (int) ((millis % 1000))));
		}
		
		public void setMouseTime(double millis){
			labelMouseTime.setText(String.format(Locale.ROOT, "Mouse Position: (%02dm:%02ds.%03dms, ", (int) (millis / 60000), (int) ((millis % 60000) / 1000), (int) ((millis % 1000))));
		}
	}
}