package app.lights.prism.com.prismlights;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.CountDownTimer;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;


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

    private  AudioProcessor p;
    private PercussionOnsetDetector pOC;
    private ComplexOnsetDetector cOP;
    private AudioDispatcher dispatcher;
    private WaveformView mWaveformView;
    private ToggleButton toggleButton;
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

    private double mostRecentPitch;

    PitchDetectionHandler pdh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop(){
        super.onStop();
//        if (dispatcher != null) {
//            dispatcher.removeAudioProcessor(p);
//            dispatcher.stop();
//        }
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
        View layout = inflater.inflate(R.layout.fragment_music, container, false);
        mWaveformView = (WaveformView) layout.findViewById(R.id.waveformView);
        toggleButton = (ToggleButton) layout.findViewById(R.id.toggleButton);
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
                    System.out.println("Most Recent Pitch: " + pitchInHz);
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

        cOP  = new ComplexOnsetDetector(2048, 0.1, 0.002, -70);
        cOP.setHandler(this);
        p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 2048, pdh);
        // add a processor, handle percussion event.
//        dispatcher.addAudioProcessor(cOP);

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
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
            CountDownTimer lowTimer;
            CountDownTimer midTimer;
            CountDownTimer highTimer;
            CountDownTimer decrementTimer;
            ToggleButton startRecording;

//            sensitivity = 60;
            @Override
            public void onClick(View v) {
                startRecording = (ToggleButton) v;

                if(startRecording.isChecked()) {
                    dispatcher.addAudioProcessor(p);
                    /**
                     * Processing mic audio
                     */
                    lowTimer = new CountDownTimer(10000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            System.out.println("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {

                            dispatcher.removeAudioProcessor(p);
                            lowTimer.cancel();
                            midTimer.cancel();
                            highTimer.cancel();
                            decrementTimer.cancel();
                            startRecording.setChecked(false);
                            System.out.println("DONE");
                        }
                    }.start();

                    midTimer = new CountDownTimer(10000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            System.out.println("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            dispatcher.removeAudioProcessor(p);
                            lowTimer.cancel();
                            midTimer.cancel();
                            highTimer.cancel();
                            decrementTimer.cancel();
                            startRecording.setChecked(false);
                            System.out.println("DONE");
                        }
                    }.start();

                    highTimer = new CountDownTimer(10000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            System.out.println("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {


                            dispatcher.removeAudioProcessor(p);
                            lowTimer.cancel();
                            midTimer.cancel();
                            highTimer.cancel();
                            decrementTimer.cancel();
                            startRecording.setChecked(false);
                            System.out.println("DONE");
                        }
                    }.start();


                    decrementTimer = new CountDownTimer(10000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            System.out.println("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            System.out.println("DONE");
                        }
                    }.start();

                } else {
                    dispatcher.removeAudioProcessor(p);
                    lowTimer.cancel();
                    midTimer.cancel();
                    highTimer.cancel();
                    decrementTimer.cancel();
                }
            }
        };

        toggleButton.setOnClickListener(listener);
        return layout;
    }


    @Override
    public void handleOnset(double time, double salience) {
        double frequency = (double) salience * (44100.0 / 2048 / 2.0);
        System.out.println(String.format("%.4f;%.4f", time, salience));

        //TODO-- Send bulb requests based on FREQUENCY ranges and the frequency detected by this ONSET.
        //Compare mostRecentPitch to the pitch selections on sliders.


    }




}
