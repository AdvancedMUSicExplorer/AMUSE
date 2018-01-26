package amuse.nodes.annotation;

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

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
	ChangeListener<? super Duration> durationListener;
	ChangeListener<? super Status> statusListener;
	
	public MusicPlayerModel(ChangeListener<? super Duration> durationListener, ChangeListener<? super Status> statusListener){
		this.durationListener = durationListener;
		this.statusListener = statusListener;
		new JFXPanel(); // Needed to initialize the JavaFX toolkit
		mediaPlayer = null;
	}
	
	public MusicPlayerModel(String path, ChangeListener<? super Duration> durationListener, ChangeListener<? super Status> statusListener){
		this(durationListener, statusListener);
		this.load(path);
	}
	
	public void load(String path){
		if(mediaPlayer != null){
			mediaPlayer.stop();
		}
		Media media = new Media(new File(path).toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		mediaPlayer.currentTimeProperty().addListener(durationListener);
		mediaPlayer.statusProperty().addListener(statusListener);
		
		/*  The durationListener only works when the mediaPlayer was started once, but it is needed before the user starts the mediaPlayer manually.
		 *  Therefore, the mediaPlayer needs to be started and paused once.
		 */
		mediaPlayer.statusProperty().addListener(new ChangeListener<Status>(){

			@Override
			public void changed(ObservableValue<? extends Status> observable, Status oldValue, Status newValue) {
				
				// The mediaPlayer can only be started when ready.
				if(newValue == Status.READY){
					play();
				}
				else if(newValue == Status.PLAYING){
					pause();
					mediaPlayer.seek(new Duration(0));
					
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
