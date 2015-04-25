package app.lights.prism.com.prismlights;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.List;

public class LightSettingsFragment extends Fragment implements CacheUpdateListener{

    private EditText nameEditor;
    private ToggleButton bulbOnState;
    private SeekBar brightness;
    private TextView brightnessPercentage;
    private float[] currentColor;
    private ColorPickerViewGroup colorPicker;

    private Button advancedSettingButton;
    private Button colorCycleButton;

    private PHHueSDK hueSDK;
    private PHBridge bridge;

    private String identifier;

    private int shouldUpdateOnOffState;
    private int shouldUpdateBrightness;
    private int shouldUpdateName;
    private int shouldUpdateColor;
    private boolean popping;


    public LightSettingsFragment() {
        hueSDK = PHHueSDK.getInstance();
        popping = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        identifier = getArguments().getString(RealHomeFragment.lightPositionString);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        shouldUpdateColor = 0;
        shouldUpdateBrightness = 0;
        shouldUpdateOnOffState = 0;
        shouldUpdateName = 0;
        // Inflate the layout for this fragment
        View frame = inflater.inflate(R.layout.fragment_light_settings, container, false);
        nameEditor = (EditText) frame.findViewById(R.id.nameEditor);
        bulbOnState = (ToggleButton) frame.findViewById(R.id.bulbOnState);
        brightness = (SeekBar) frame.findViewById(R.id.brightness);
        brightnessPercentage = (TextView) frame.findViewById(R.id.brightnessLabel);
        colorPicker = (ColorPickerViewGroup) frame.findViewById(R.id.colorPickerView);

        advancedSettingButton = (Button)frame.findViewById(R.id.advancedSettingButton);
        colorCycleButton = (Button)frame.findViewById(R.id.colorCycleButton);

        updateState();
        nameEditor.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    //TODO check if name is valid
                    shouldUpdateName++;
                    HueBulbChangeUtility.changeLightName(identifier, nameEditor.getText().toString(), new OnCompletedListener() {
                        boolean hasCompleted = false;

                        @Override
                        public void onCompleted() {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!hasCompleted && shouldUpdateName > 0) {
                                        hasCompleted = true;
                                        shouldUpdateName--;
                                        nameEditor.clearFocus();
                                    }
                                }
                            });
                        }
                    });
                }
                return false;
            }
        });
        //doesn't used onCheckedChanged to avoid programmatic sending
        bulbOnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldUpdateOnOffState++;
                ToggleButton bulbOn = (ToggleButton) v;
                HueBulbChangeUtility.turnBulbOnOff(identifier, bulbOn.isChecked(), (MainActivity) getActivity(), new OnCompletedListener() {
                    boolean hasCompleted = false;

                    @Override
                    public void onCompleted() {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               if (!hasCompleted && shouldUpdateOnOffState > 0) {
                                   hasCompleted = true;
                                   shouldUpdateOnOffState--;
                               }
                            }
                        });
                    }
                });
            }
        });
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessPercentage.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                shouldUpdateBrightness++;
                HueBulbChangeUtility.changeBrightness(identifier, seekBar.getProgress(), (MainActivity) getActivity(), new OnCompletedListener() {
                    boolean hasCompleted = false;

                    @Override
                    public void onCompleted() {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!hasCompleted && shouldUpdateBrightness > 0) {
                                    hasCompleted = true;
                                    shouldUpdateBrightness--;
                                }
                            }
                        });
                    }
                });
            }
        });
        colorPicker.setColorChangedListener(new ColorChangedListener() {
            @Override
            public void onColorChanged(float[] newColor) {
                shouldUpdateColor++;
                currentColor = newColor;
                HueBulbChangeUtility.changeBulbColor(identifier, newColor, (MainActivity)getActivity(), new OnCompletedListener() {
                    boolean hasCompleted = false;

                    @Override
                    public void onCompleted() {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!hasCompleted && shouldUpdateColor > 0) {
                                    hasCompleted = true;
                                    shouldUpdateColor--;
                                }
                            }
                        });
                    }
                });
            }
        });

        advancedSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(RealHomeFragment.lightPositionString, identifier);
                bundle.putBoolean(RealHomeFragment.groupOrLightString, false);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                AdvancedSettingFragment advancedSettingFragment = new AdvancedSettingFragment();
                advancedSettingFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, advancedSettingFragment);
                fragmentTransaction.addToBackStack("AdvancedSettings");
                fragmentTransaction.commit();
            }
        });

        colorCycleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(RealHomeFragment.lightPositionString, identifier);
                bundle.putBoolean(RealHomeFragment.groupOrLightString, false);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ColorCycleFragment colorCycleFragment = new ColorCycleFragment();
                colorCycleFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, colorCycleFragment);
                fragmentTransaction.addToBackStack("colorCycle");
                fragmentTransaction.commit();
            }
        });
        
        return frame;
    }

    private void updateState() {

        /****************Checking Color Cycle**************/
        //check if there is current colorCycle going on.
        bridge = hueSDK.getSelectedBridge();
        List<PHSchedule> colorCycles = bridge.getResourceCache().getAllTimers(true);
        List<PHSchedule> colorCyclesForThisBulb = new ArrayList<>();
        for (int i = 0; i< colorCycles.size();i++){
            if (colorCycles.get(i).getLightIdentifier()!=null
                    &&colorCycles.get(i).getLightIdentifier().equals(identifier)
                    && colorCycles.get(i).getDescription().startsWith("prism"))
                colorCyclesForThisBulb.add(colorCycles.get(i));
        }

        // if there is a color cycle running, set it as current color cycle. if this is new color cycle from other device, add it to the list.
        if (colorCyclesForThisBulb.size()!=0){
            ColorCycle currentColorCycle = new ColorCycle(colorCyclesForThisBulb); // this generate ColorCycle class out of List of recurring timer schedule
            String currentName = currentColorCycle.getName();
            int nameExist = ((MainActivity)getActivity()).containsCycleName(currentName);
            if(nameExist < 0){ // if nameExist is -1, this means there is no such name in current color cycles, so add new one.
                ((MainActivity)getActivity()).addColorCycle(currentColorCycle);
            } else{ // if the same name exist, just replace with recent one. Other user might have changed this cycle.
                //((MainActivity)getActivity()).setColorCycle(nameExist, currentColorCycle);     //<-- this doesn't work. no replacement....
            }
        }

        PHLight  currentLight = hueSDK.getSelectedBridge().getResourceCache().getLights().get(identifier);
        //means light has been deleted
        if(currentLight == null) {
            if(popping) {
                return;
            }
            getFragmentManager().popBackStack();
            popping = true;
            return;
        }
        PHLightState state = currentLight.getLastKnownLightState();
        if(!nameEditor.hasFocus() && shouldUpdateName == 0) {
            nameEditor.setText(currentLight.getName());
        }
        if(shouldUpdateOnOffState == 0) {
            bulbOnState.setChecked(state.isOn());
        }
        if(shouldUpdateBrightness == 0) {
            int currentBrightness = getCurrentBrightness(state.getBrightness());
            brightness.setProgress(currentBrightness);
            brightnessPercentage.setText(currentBrightness + "%");
        }
        if(shouldUpdateColor == 0) {
            currentColor = new float[]{state.getX(), state.getY()};
            colorPicker.setColor(currentColor);
        }
    }

    private int getCurrentBrightness(int phBrightness) {
        return (int) Math.round((phBrightness * 100.0) / HueBulbChangeUtility.MAX_BRIGHTNESS);
    }

    @Override
    public void cacheUpdated() {
        updateState();
    }
}
