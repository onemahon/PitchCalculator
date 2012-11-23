package com.example.pitchcalculator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.pitchcalculator.AudioAnalyzer.DataProcessedListener;

public class PitchActivity extends Activity {

	private Button toggleRecording;
//	private TextView[] pitches;
	private boolean isRecording;
	private boolean stopThread = false; // TODO decide when to actually break out of the listener thread
//	private AudioReader audioReader;
//	private FFTTransformer spectrumAnalyser;
//	
//	private float[] spectrumData;
//	private float[][] spectrumHist;
//	private int spectrumIndex;
//	private float[] biasRange;
//	private long audioProcessed = 0;
//	private long audioSequence = 0;
//	
//	
//	
	
	private AudioAnalyzer analyzer;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pitch_activity);
        toggleRecording = (Button) findViewById(R.id.toggleRecordingButton);
        pitchesTexts = new TextView[5];
        pitchesTexts[0] = (TextView) findViewById(R.id.pitch0);
        pitchesTexts[1] = (TextView) findViewById(R.id.pitch1);
        pitchesTexts[2] = (TextView) findViewById(R.id.pitch2);
        pitchesTexts[3] = (TextView) findViewById(R.id.pitch3);
        pitchesTexts[4] = (TextView) findViewById(R.id.pitch4);
        
        toggleRecording.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (isRecording) {
        			stopRecording();
        		} else {
        			startRecording();
        		}
        	}
        });
        analyzer = new AudioAnalyzer();
        analyzer.setDataProcessedListener(new DataProcessedListener() {
        	@Override
        	public void onEvent(float[] mainPitches) {
        		setPitchOutput(mainPitches);
        	}
        });
        
        startAnalyzingThread();
        
        stopRecording();

//        
//        pitches = new TextView[] { (TextView) findViewById(R.id.pitch0),
//        		(TextView) findViewById(R.id.pitch1), 
//        		(TextView) findViewById(R.id.pitch2), 
//        		(TextView) findViewById(R.id.pitch3), 
//        		(TextView) findViewById(R.id.pitch4) };
//        
//        
//        audioReader = new AudioReader();
//        
//        clearData();
//        
//        
    }
    
    private Thread analysisThread;
    private void startAnalyzingThread() {
    	stopThread = false;
    	
    	analysisThread = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		while (true) {
        			if (isRecording)
        				analyzer.doUpdate();
        			if (stopThread)
        				break;
        		}
        	}
        });
    	analysisThread.start();
    }
    
    private TextView[] pitchesTexts;
    private void setPitchOutput(final float[] pitches) {
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pitches != null && pitches.length > 5) {
					for (int i = 0; i < 5; i++) {
						pitchesTexts[i].setText(pitches[i]+" hz");
					}
				}				
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pitch_activity, menu);
        return true;
    }
    
//    private void clearData() {
//    	for (TextView v : pitches) {
//			v.setText("0"+" hz");
//		}
//    	
//        spectrumAnalyser = new FFTTransformer(256, Window.Function.BLACKMAN_HARRIS);
//        spectrumData = new float[256 / 2];
//        spectrumHist = new float[256 / 2][4];
//        spectrumIndex = 0;
//        
//        biasRange = new float[2];
//    }
    
    private void startRecording() {
    	startAnalyzingThread();
    	
    	toggleRecording.setText("Stop recording");
    	isRecording = true;
    	
    	analyzer.measureStart();
    	
    	
//    	clearData();
//    	
//    	audioProcessed = audioSequence = 0;
//    	audioReader.startReader(8000, 256 * 1, new AudioReader.Listener() {
//			@Override
//			public void onReadError(int error) {
//				Log.d("PitchActivity", "Read error number "+error);
//			}
//			
//			@Override
//			public void onReadComplete(short[] buffer) {
//				
//			}
//		});
//    	
//    	
    }
    
    private void stopRecording() {
    	toggleRecording.setText("Start recording");
    	isRecording = false;
    	analyzer.measureStop();
    	stopThread = true;
    }
    
}
