package amuse.scheduler.gui.annotation.singlefile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Level;

import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysIntValue;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;
import amuse.util.FileOperations;
import amuse.util.audio.AudioFileConversion;
import jAudioFeatureExtractor.AudioFeatures.MagnitudeSpectrum;

/**
 * Calculates the amplitude spectrum for an audio file and saves it in a BufferedImage.
 * @author Frederik Heerde
 * @version $Id$
 */
public class AudioSpectrumModel {
	
	float sampleRate;
	BufferedImage audiospectrumImage;

	public AudioSpectrumModel(String pMusicFilePath) throws IOException {
		this(new File(pMusicFilePath));
	}

	public AudioSpectrumModel(File pMusicFile) throws IOException{
		sampleRate = 0;
		audiospectrumImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		
		File musicFile = pMusicFile;
		
		// Convert the file to WAV if neccessary
		if(!pMusicFile.getAbsolutePath().toLowerCase().endsWith("wav")){
			musicFile = convertFileToWAV(pMusicFile);
		}
		calculateAudioSpectrumImage(musicFile);
		
		// Delete any generated files
		if(musicFile != pMusicFile){
			FileOperations.delete(musicFile);
		}
	}
	
	private File convertFileToWAV(File musicFile) throws IOException{
		try {
			String musicFilePath = AmusePreferences.get(KeysStringValue.AMUSE_PATH)
					+ File.separator 
					+ "config" 
					+ File.separator 
					+ "annotation" 
					+ File.separator 
					+ "temp.wav";
			File f = new File(musicFilePath);
			if(!f.getParentFile().exists()) {
				f.getParentFile().mkdir();
			}
			AudioFileConversion.convertMp3ToWave(musicFile, f);
			return new File(musicFilePath);
		} catch(IOException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,
				"Audio decoding error: " + e.getMessage());
			throw e;
		}
	}
	
	private void calculateAudioSpectrumImage(File musicFile){
		AudioInputStream decodedStream = null;
		final int WINDOWSIZE = (int) Math.pow(2, AmusePreferences.getInt(KeysIntValue.AUDIOSPECTRUM_WINDOWSIZE) + 8);
		final int HOPSIZE = (int) Math.pow(2, AmusePreferences.getInt(KeysIntValue.AUDIOSPECTRUM_HOPSIZE) + 8);
		try {
			
			AudioInputStream stream = AudioSystem.getAudioInputStream(musicFile);
			AudioFormat format = stream.getFormat();
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					format.getSampleRate(),
					8,
					1,
					1 * 2,
					format.getFrameRate(),
					true);
			decodedStream = AudioSystem.getAudioInputStream(decodedFormat, stream);
			byte[] byte_samples = new byte[(int)stream.getFrameLength() * decodedFormat.getSampleSizeInBits() / 8];
			sampleRate = decodedStream.getFormat().getSampleRate();
			decodedStream.read(byte_samples);
			
			MagnitudeSpectrum magSpec = new MagnitudeSpectrum();
			double[][] magnitudeSpectrum = new double[(byte_samples.length - WINDOWSIZE) / HOPSIZE + 1][WINDOWSIZE / 2];
			audiospectrumImage = new BufferedImage((byte_samples.length - WINDOWSIZE) / HOPSIZE + 1, WINDOWSIZE / 2, BufferedImage.TYPE_INT_RGB);
			Graphics2D imageGraphics = audiospectrumImage.createGraphics();
			double min = 1;
			double max = 0;
			for(int windowStart = 0; windowStart < byte_samples.length - WINDOWSIZE; windowStart += HOPSIZE){

				double[] window = new double[WINDOWSIZE];
				for(int i = 0; i < WINDOWSIZE; i++){
					window[i] = byte_samples[i + windowStart];
				}
				double[] windowFreqs = magSpec.extractFeature(window, -1., new double[][]{});
				for(int i = 0; i < windowFreqs.length; i++){
					min = Math.min(windowFreqs[i], min);
					max = Math.max(windowFreqs[i], max);
					magnitudeSpectrum[windowStart / HOPSIZE][i] = windowFreqs[i];
					
				}
			}
			for(int i = 0; i < audiospectrumImage.getWidth(); i++){
				for(int j = 0; j < audiospectrumImage.getHeight(); j++){
					double value = (magnitudeSpectrum[i][j] - min) / (max - min);
					imageGraphics.setColor(new Color(Color.HSBtoRGB(0.666f - 0.666f * (float) value , 1.0f, 1.0f)));
					imageGraphics.fillRect(i, audiospectrumImage.getHeight() - j - 1, 1, 1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public BufferedImage getAudiospectrumImage(){
		return audiospectrumImage;
	}

	public float getSampleRate() {
		return sampleRate;
	}	
		
}
