package amuse.scheduler.gui.annotation;

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

/**
 * Used to play the audio file.
 * @author Frederik Heerde
 * @version $Id$
 */
public class MusicPlayerModel{
	MediaPlayer mediaPlayer;
	ChangeListener<? super Duration> currentTimeListener;
	ChangeListener<? super Status> statusListener;
	
	Duration pauseTime;
	
	public MusicPlayerModel(ChangeListener<Duration> durationListener, ChangeListener<Status> statusListener){
		this.currentTimeListener = durationListener;
		this.statusListener = statusListener;
		new JFXPanel(); // Needed to initialize the JavaFX toolkit
		mediaPlayer = null;

	}
	
	public MusicPlayerModel(String path, ChangeListener<Duration> durationListener, ChangeListener<Status> statusListener){
		this(durationListener, statusListener);
		this.load(path);
	}
	
	public void load(String path){
		if(mediaPlayer != null){
			mediaPlayer.stop();
		}
		Media media = new Media(new File(path).toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		mediaPlayer.currentTimeProperty().addListener(currentTimeListener);
		mediaPlayer.statusProperty().addListener(statusListener);
		
		// Pauses the mediaPlayer at the end
		mediaPlayer.setOnEndOfMedia(() -> {
			this.seek(getDurationInMs() - 1);
			this.pause();
		});
				
		pauseTime = new Duration(0);
		/*
		 *  When the mediaPlayer is paused, it takes some time until it actually stops. 
		 *  As this is unwanted behavior, this listener forces the mediaPlayer to seek the time where it was paused. 
		 */
		mediaPlayer.setOnPaused(() -> mediaPlayer.seek(pauseTime));
		
		/*  The durationListener only works when the mediaPlayer was started once, but it is needed before the user starts the mediaPlayer manually.
		 *  Therefore, the mediaPlayer needs to be started and paused once.
		 */
		mediaPlayer.statusProperty().addListener(new ChangeListener<Status>(){

			@Override
			public void changed(ObservableValue<? extends Status> observable, Status oldValue, Status newValue) {
				
				// The mediaPlayer can only be started when ready.
				if(newValue == Status.READY){
					mediaPlayer.play();
				}
				else if(newValue == Status.PLAYING){
					mediaPlayer.pause();
					// As this Listener only needs to start and pause the mediaPlayer once, it is now removed.
					mediaPlayer.statusProperty().removeListener(this);
				}
			}
			
		});
	}
	
	public void play(){
		if(mediaPlayer != null){
			mediaPlayer.play();
		}
	}
	
	public void pause(){
		if(mediaPlayer != null){
			pauseTime = mediaPlayer.getCurrentTime();
			mediaPlayer.pause();
		}
	}
	
	public double getCurrentMs(){
		if(mediaPlayer != null){
			Duration time = mediaPlayer.getCurrentTime();
			return time.toMillis();
		}
		return -1.;
	}

	public double getDurationInMs() {
		if(mediaPlayer != null){
			return mediaPlayer.getCycleDuration().toMillis();
		}
		return -1.;
	}

	public boolean isPaused() {
		return mediaPlayer == null || mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING;
	}

	public void seek(double millis) {
		if(mediaPlayer != null){
			mediaPlayer.seek(new Duration(millis));
		}
	}
	
	public ReadOnlyObjectProperty<Status> getStatusProperty(){
		return mediaPlayer == null? null: mediaPlayer.statusProperty();
	}
	

	public String getMusicFilePath() {
		if(mediaPlayer != null){
			try {
				File file = new File(new URI(mediaPlayer.getMedia().getSource()));
				return file.getAbsolutePath();
			} catch (URISyntaxException e) {
				return "";
			}
		}
		return "";
	}
}
