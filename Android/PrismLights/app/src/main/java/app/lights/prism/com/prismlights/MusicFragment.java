package app.lights.prism.com.prismlights;


import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.CountDownTimer;

import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.philips.lighting.hue.sdk.utilities.PHUtilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.tarsos.dsp.onsets.PrintOnsetHandler;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MusicFragment extends Fragment implements OnsetHandler {

    public static final String musicSelectBulbsTag = "BULB_RANGE_FRAGMENT_TAG";
    private  AudioProcessor p;
    private PercussionOnsetDetector pOC;
    private ComplexOnsetDetector cOP;
    private volatile AudioDispatcher dispatcher;
    private WaveformView mWaveformView;
    private ToggleButton toggleButton;
    private Button selectLightsButton;
    private SeekBar bPM;
    private SeekBar maxBrightnessSlider;
    private SeekBar lowRangeSlider;
    private SeekBar midRangeSlider;
    private SeekBar highRangeSlider;
    private TextView bpmLabel;
    private TextView brightnessLabel;
    private TextView lowRangeMaxLabel;
    private TextView midRangeMinLabel;
    private TextView midRangeMaxLabel;
    private TextView highRangeMinLabel;

    //These lists will be SMALL
    private ArrayList<String> lows;
    private ArrayList<String> mids;
    private ArrayList<String> highs;

    private volatile double fX;

    private Random rng;
    private double mostRecentPitch;

    PitchDetectionHandler pdh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onPause(){
        super.onPause();
        try {
        if (dispatcher != null) {
            dispatcher.removeAudioProcessor(p);
            dispatcher.stop();
        } }
        catch (Exception e){
            System.out.println(e.toString());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fX = 1.75;
        HashMap<String, String> map = LightRangeMap.getLightRangeMap().getMap();
        lows = new ArrayList<String>();
        mids = new ArrayList<String>();
        highs = new ArrayList<String>();

        for(String s : map.keySet()){
            if(map.get(s).equals("LOW")){
                lows.add(s);
            } else if(map.get(s).equals("MID")){
                mids.add(s);
            } else if(map.get(s).equals("HIGH")){
                highs.add(s);
            }
        }

        View layout = inflater.inflate(R.layout.fragment_music, container, false);
        mWaveformView = (WaveformView) layout.findViewById(R.id.waveformView);
        toggleButton = (ToggleButton) layout.findViewById(R.id.toggleButton);
        selectLightsButton = (Button) layout.findViewById(R.id.selectLightsButton);
        bPM = (SeekBar) layout.findViewById(R.id.sensitivitySlider);
        lowRangeSlider = (SeekBar) layout.findViewById(R.id.lowRangeSlider);
        midRangeSlider = (SeekBar) layout.findViewById(R.id.midRangeSlider);
        highRangeSlider = (SeekBar) layout.findViewById(R.id.highRangeSlider);
        highRangeSlider.setEnabled(false);
        bpmLabel = (TextView) layout.findViewById(R.id.bpmLabel);

        lowRangeMaxLabel = (TextView) layout.findViewById(R.id.lowRangeMaxLabel);
        midRangeMinLabel = (TextView) layout.findViewById(R.id.midRangeMinLabel);
        midRangeMaxLabel = (TextView) layout.findViewById(R.id.midRangeMaxLabel);
        highRangeMinLabel = (TextView) layout.findViewById(R.id.highRangeMinLabel);
        //Initialize rng with the max hue value: 65280.... Hue Color range [0-65280]
        rng = new Random();


        brightnessLabel = (TextView) layout.findViewById(R.id.muzeBrightnessLabel);
        maxBrightnessSlider = (SeekBar) layout.findViewById(R.id.maxBrightnessSlider);

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 2048, 0);

       pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result,AudioEvent e) {
                short[] data = new short[e.getBufferSize()/2];
                ByteBuffer.wrap(e.getByteBuffer()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(data);
                mWaveformView.updateAudioData(data);
                if(result.isPitched()){
                    float pitchInHz = result.getPitch();
//                    System.out.println("Most Recent Pitch: " + pitchInHz);
                    mostRecentPitch = pitchInHz;
                }
//                System.out.println(result.isPitched());
//                float pitchInHz = result.getPitch();
//                if(pitchInHz != -1.0)
//                    System.out.println("Pitch: " + pitchInHz);

                MusicFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView text = (TextView) findViewById(R.id.textView1);
//                        System.out.println("" + pitchInHz);
                    }
                });
            }
        };

//        cOP  = new ComplexOnsetDetector(2048, fX/10, 0.002, -70);
        cOP  = new ComplexOnsetDetector(2048, .1, 0.002, -70);
        cOP.setHandler(this);
        p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 2048, pdh);
        // add a processor, handle percussion event.


        new Thread(dispatcher,"Audio Dispatcher").start();
//        slave = new Thread(dispatcher,"Audio Dispatcher").start();

//        task = new MusicTask();
//        dispatcher.stop();



        lowRangeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0)
                    progress = 1;
                lowRangeMaxLabel.setText((progress + 100) + "hz");
                midRangeMinLabel.setText((progress + 101) + "hz");
                int range = 901 - ( Integer.parseInt(midRangeMinLabel.getText().toString().replace("hz", "")) );
                midRangeSlider.setMax(range);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        midRangeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= 1)
                    progress = 2;
                midRangeMaxLabel.setText((progress + ( Integer.parseInt(lowRangeMaxLabel.getText().toString().replace("hz", "")) )) + "hz");
                highRangeMinLabel.setText((progress + ( Integer.parseInt(lowRangeMaxLabel.getText().toString().replace("hz", "")) ) + 1) + "hz");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        bPM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    bpmLabel.setText("" + (progress + 60) );
                    int currentBPM = progress + 60;

//                f(x) = 1/60 X + 3
                double slope = -1.0/60.0;
                double m =  (progress + 60.0); //BPM 60-180
                fX = slope * m + 3.5;
//                bpmLabel.setText("" + fX);


            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

//                dispatcher.removeAudioProcessor(p);
                dispatcher.removeAudioProcessor(cOP);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                cOP.setThreshold(fX/10);
//                cOP.setThreshold();
//                cOP  = new ComplexOnsetDetector(2048, fX/10, 0.002, -70);
//                cOP.setHandler(this);
//                p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 2048, pdh);
//                dispatcher.addAudioProcessor(p);
                dispatcher.addAudioProcessor(cOP);
            }
        });

        maxBrightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    brightnessLabel.setText("1%" );
                } else {
                    brightnessLabel.setText(progress + "%");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        //doesn't used onCheckedChanged to avoid programmatic sending
        View.OnClickListener listener = new View.OnClickListener() {
            ToggleButton startRecording;
            @Override
            public void onClick(View v) {
                startRecording = (ToggleButton) v;

                if(startRecording.isChecked()) {
                    dispatcher.addAudioProcessor(p);
                    dispatcher.addAudioProcessor(cOP);

                } else {
                    dispatcher.removeAudioProcessor(p);
                    dispatcher.removeAudioProcessor(cOP);
                }
            }
        };

        View.OnClickListener selectLightsListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, new BulbRangeFragment(), musicSelectBulbsTag);
                fragmentTransaction.addToBackStack(musicSelectBulbsTag);
                fragmentTransaction.commit();
            }
        };

        selectLightsButton.setOnClickListener(selectLightsListener);
        toggleButton.setOnClickListener(listener);
        return layout;
    }


    @Override
    public void handleOnset(double time, double salience) {

//        System.out.println(String.format("%.4f;%.4f, %.4f", time, salience, (float)mostRecentPitch));

        //TODO-- Send bulb requests based on FREQUENCY ranges and the frequency detected by this ONSET.
        //Compare mostRecentPitch to the pitch selections on sliders.

        int lowThreshold = ( Integer.parseInt(lowRangeMaxLabel.getText().toString().replace("hz", "")) );
        int midThreshold = ( Integer.parseInt(midRangeMaxLabel.getText().toString().replace("hz", "")) );
//        currentFrequencyLabel.setText(mostRecentPitch + "hz");
//        System.out.println(mostRecentPitch);
        ArrayList<String> lightsToChange = null;
        if(mostRecentPitch < lowThreshold){
            //Send request to low bulbs
            lightsToChange = lows;
        } else if (mostRecentPitch < midThreshold){
            //Send request to mid bulbs
            lightsToChange = mids;
        } else {
            //Send request to high bulbs
            lightsToChange = highs;
        }
        if(lightsToChange != null && lightsToChange.size() != 0) {
            float[] xY = {rng.nextFloat(), rng.nextFloat()};
            HueBulbChangeUtility.musicChangeBulbsColor(lightsToChange, xY, 1, 254 * (Integer.parseInt(brightnessLabel.getText().toString().replace("%", "")) / 100));
            //Fade out slowly
            HueBulbChangeUtility.musicChangeBulbsColor(lightsToChange, xY, 20, 0);
        }
    }
}
