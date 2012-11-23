package com.example.pitchcalculator;

import java.util.EventListener;

import org.hermit.android.io.AudioReader;
import org.hermit.dsp.FFTTransformer;
import org.hermit.dsp.SignalPower;
import org.hermit.dsp.Window;

public class AudioAnalyzer {

	private AudioReader audioReader;
	private FFTTransformer spectrumAnalyser;
	private short[] audioData;
	private float[] spectrumData;
	private float[][] spectrumHist;
	private int spectrumIndex;

	private int inputBlockSize = 1024;
	private Window.Function windowFunction = Window.Function.BLACKMAN_HARRIS;
	private int sampleRate = 8000;
	private long audioSequence = 0;
	private long audioProcessed = 0;
	private int sampleDecimate = 1;
	private int historyLen = 4;
	private float[] biasRange = null;


	public AudioAnalyzer() {
		audioReader = new AudioReader();

		spectrumAnalyser = new FFTTransformer(inputBlockSize, windowFunction);

		// Allocate the spectrum data.
		spectrumData = new float[inputBlockSize / 2];
		spectrumHist = new float[inputBlockSize / 2][historyLen];
		spectrumIndex = 0;

		biasRange = new float[2];
	}


	/**
	 * Handle audio input.  This is called on the thread of the
	 * parent surface.
	 * 
	 * @param   buffer      Audio data that was just read.
	 */
	private final void processAudio(short[] buffer) {
		// Process the buffer.  While reading it, it needs to be locked.
		synchronized (buffer) {
			// Calculate the power now, while we have the input
			// buffer; this is pretty cheap.
			final int len = buffer.length;

			SignalPower.biasAndRange(buffer, len - inputBlockSize, inputBlockSize, biasRange);
			//			final float bias = biasRange[0];
			float range = biasRange[1];
			if (range < 1f)
				range = 1f;

			//			double currentPower = SignalPower.calculatePowerDb(buffer, 0, len);

			spectrumAnalyser.setInput(buffer, len - inputBlockSize, inputBlockSize);

			buffer.notify();
		}

		//		long specStart = System.currentTimeMillis();
		spectrumAnalyser.transform();
		//		long specEnd = System.currentTimeMillis();

		// Get the FFT output.
		if (historyLen <= 1)
			spectrumAnalyser.getResults(spectrumData);
		else
			spectrumIndex = spectrumAnalyser.getResults(spectrumData,
					spectrumHist,
					spectrumIndex);

		
		spectrumAnalyser.findKeyFrequencies(spectrumData, keyPitches);
		
		audioDataProcessedListener.onEvent(keyPitches);

	}
	private float[] keyPitches = new float[10];

	private DataProcessedListener audioDataProcessedListener;
	public void setDataProcessedListener(DataProcessedListener listener) {
		audioDataProcessedListener = listener;
	}


	public interface DataProcessedListener extends EventListener {
		public void onEvent(float[] mainPitches);
	}


	public void measureStart() {
		audioProcessed = audioSequence = 0;

		audioReader.startReader(sampleRate, inputBlockSize * sampleDecimate, new AudioReader.Listener() {
			@Override
			public final void onReadComplete(short[] buffer) {
				receiveAudio(buffer);
			}
			@Override
			public void onReadError(int error) {
				handleError(error);
			}
		});
	}


	public void measureStop() {
		audioReader.stopReader();
	}




	private final void receiveAudio(short[] buffer) {
		// Lock to protect updates to these local variables.  See run().
		synchronized (this) {
			audioData = buffer;
			++audioSequence;
		}
	}

	private int readError = AudioReader.Listener.ERR_OK;
	private void handleError(int error) {
		synchronized (this) {
			readError = error;
		}
	}


	public final void doUpdate() {
		short[] buffer = null;
		synchronized (this) {
			if (audioData != null && audioSequence > audioProcessed) {
				audioProcessed = audioSequence;
				buffer = audioData;
			}
		}

		// If we got data, process it without the lock.
		if (buffer != null)
			processAudio(buffer);

		if (readError != AudioReader.Listener.ERR_OK)
			processError(readError);
	}



	private final void processError(int error) {
		// Pass the error to all the gauges we have.
		//        if (waveformGauge != null)
		//            waveformGauge.error(error);
		//        if (spectrumGauge != null)
		//            spectrumGauge.error(error);
		//        if (sonagramGauge != null)
		//            sonagramGauge.error(error);
		//        if (powerGauge != null)
		//            powerGauge.error(error);
	}





}
