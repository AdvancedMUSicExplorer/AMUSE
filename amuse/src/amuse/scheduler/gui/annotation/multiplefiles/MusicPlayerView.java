package amuse.scheduler.gui.annotation.multiplefiles;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import javafx.scene.media.MediaPlayer.Status;


import amuse.scheduler.gui.controller.MultipleFilesAnnotationController;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

public class MusicPlayerView extends JPanel{
	JSlider slider;
	JLabel title;
	JButton buttonPlayPause;
	ImageIcon iconPlay, iconPause;
	
	MultipleFilesAnnotationController annotationController;
	
	public MusicPlayerView(MultipleFilesAnnotationController annotationController){
		super();
		this.annotationController = annotationController;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		/*
		 *  Setting up the icons used for playing/ pausing the music
		 */
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
		
		slider = new JSlider(JSlider.HORIZONTAL, 0, 1, 0);
		slider.setEnabled(false);
		slider.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				int newValue = ((BasicSliderUI) slider.getUI()).valueForXPosition(e.getX());
				annotationController.seekInMusic(newValue);
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
		title = new JLabel(" ");
		title.setAlignmentX(0.5f);
		
		buttonPlayPause = new JButton();
		buttonPlayPause.setIcon(iconPlay);
		buttonPlayPause.setEnabled(false);
		buttonPlayPause.addActionListener(e -> {
			if(this.annotationController.isMusicPaused()){
				this.annotationController.playMusic();
			}
			else{
				this.annotationController.pauseMusic();
			}
			this.annotationController.repaintTable();
		});
		
		
		
		JPanel buttonPanel = new JPanel();
		
		buttonPanel.add(buttonPlayPause);
		

		
		this.add(title);
		this.add(slider);
		this.add(buttonPanel);
	}
	
	public JSlider getSlider(){
		return slider;
	}

	public ChangeListener<Duration> getCurrentTimeListener() {
		return (value, oldTime, newTime) -> {
			slider.setValue((int) newTime.toMillis());
		};
	}
	
	public ChangeListener<Status> getStatusListener() {
		return (value, oldStatus, newStatus) -> {
			String path = annotationController.getMusicAbsoulutePath();
			title.setText(path.substring(path.lastIndexOf(File.separator) + 1));
			buttonPlayPause.setIcon(newStatus == Status.PLAYING? iconPause: iconPlay);
			if(path == ""){
				buttonPlayPause.setEnabled(false);
				slider.setEnabled(false);
			}
			else{
				buttonPlayPause.setEnabled(true);
				slider.setEnabled(true);
				slider.setMaximum((int) annotationController.getDurationInMs());
			}
			this.revalidate();
			this.repaint();
		};
	}
}
