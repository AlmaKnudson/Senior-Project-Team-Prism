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
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MusicFragment extends Fragment {

    private  AudioProcessor p;
    private AudioDispatcher dispatcher;
    private WaveformView mWaveformView;
    private ToggleButton toggleButton;
    private SeekBar bPM;
    private SeekBar maxBrightnessSlider;
    private TextView bpmLabel;
    private TextView brightnessLabel;



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
        bpmLabel = (TextView) layout.findViewById(R.id.bpmLabel);
        brightnessLabel = (TextView) layout.findViewById(R.id.muzeBrightnessLabel);
        maxBrightnessSlider = (SeekBar) layout.findViewById(R.id.maxBrightnessSlider);

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 2048, 0);

       pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result,AudioEvent e) {
                short[] data = new short[e.getBufferSize()/2];
                ByteBuffer.wrap(e.getByteBuffer()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(data);
                mWaveformView.updateAudioData(data);

                final float pitchInHz = result.getPitch();
                MusicFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView text = (TextView) findViewById(R.id.textView1);
//                        System.out.println("" + pitchInHz);
                    }
                });
            }
        };

        p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_PITCH, 44100, 2048, pdh);
//        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();
//        slave = new Thread(dispatcher,"Audio Dispatcher").start();

//        task = new MusicTask();
//        dispatcher.stop();


        bPM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    bpmLabel.setText("" + (progress + 60) );
//                brightnessPercentage.setText(progress + "%");
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

//            sensitivity = 60;
            @Override
            public void onClick(View v) {
                ToggleButton startRecording = (ToggleButton) v;

                if(startRecording.isChecked()) {
                    dispatcher.addAudioProcessor(p);
                    /**
                     * Processing mic audio
                     */
                    lowTimer = new CountDownTimer(30000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            System.out.println("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            System.out.println("DONE");
                        }
                    }.start();

                    midTimer = new CountDownTimer(30000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            System.out.println("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            System.out.println("DONE");
                        }
                    }.start();

                    highTimer = new CountDownTimer(30000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            System.out.println("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            System.out.println("DONE");
                        }
                    }.start();


                    decrementTimer = new CountDownTimer(30000, 1000) {

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







}
