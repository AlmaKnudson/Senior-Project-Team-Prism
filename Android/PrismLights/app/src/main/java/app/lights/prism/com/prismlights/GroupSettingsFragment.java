package app.lights.prism.com.prismlights;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GroupSettingsFragment extends Fragment implements CacheUpdateListener {
    private String identifier; // The id of the chosen Light
    private EditText nameEditor;
    private ToggleButton bulbOnState;
    private SeekBar brightness;
    private TextView brightnessPercentage;
    private float[] currentColor;
    private ColorPickerViewGroup colorPicker;

    private Button advancedSettingButton;
    private Button colorCycleButton;
    private ImageButton editGroupButton;

    private PHHueSDK hueSDK;
    private int shouldUpdateOnOffState;
    private int shouldUpdateBrightness;
    private int shouldUpdateName;
    private int shouldUpdateColor;
    private boolean popping;


    public GroupSettingsFragment() {
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
        View frame = inflater.inflate(R.layout.fragment_group_settings, container, false);
        nameEditor = (EditText) frame.findViewById(R.id.nameEditor);
        bulbOnState = (ToggleButton) frame.findViewById(R.id.bulbOnState);
        brightness = (SeekBar) frame.findViewById(R.id.brightness);
        brightnessPercentage = (TextView) frame.findViewById(R.id.brightnessLabel);
        colorPicker = (ColorPickerViewGroup) frame.findViewById(R.id.colorPickerView);

        advancedSettingButton = (Button)frame.findViewById(R.id.advancedSettingButton);
        colorCycleButton = (Button)frame.findViewById(R.id.colorCycleButton);
        if(HueBulbChangeUtility.DEFAULT_GROUP_ID.equals(identifier)) {
            advancedSettingButton.setVisibility(View.GONE);
            advancedSettingButton.setEnabled(false);
            colorCycleButton.setVisibility(View.GONE);
            colorCycleButton.setEnabled(false);
        }

        editGroupButton = (ImageButton) frame.findViewById(R.id.editGroupButton);
        updateState();
        if(HueBulbChangeUtility.DEFAULT_GROUP_ID.equals(identifier)) {
            editGroupButton.setVisibility(View.GONE);
            nameEditor.setEnabled(false);
        } else {
            editGroupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(RealHomeFragment.lightPositionString, identifier);

                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                    EditGroupFragment editGroupFragment = new EditGroupFragment();
                    editGroupFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.container, editGroupFragment);
                    fragmentTransaction.addToBackStack("editGroup");
                    fragmentTransaction.commit();
                }
            });
            nameEditor.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                        shouldUpdateName++;
                        HueBulbChangeUtility.changeGroupName(identifier, nameEditor.getText().toString(), new OnCompletedListener() {
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
        }
        //doesn't used onCheckedChanged to avoid programmatic sending
        bulbOnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldUpdateOnOffState++;
                ToggleButton bulbOn = (ToggleButton) v;
                HueBulbChangeUtility.turnGroupOnOff(identifier, bulbOn.isChecked(), (MainActivity)getActivity(), new OnCompletedListener() {
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
                HueBulbChangeUtility.changeGroupBrightness(identifier, seekBar.getProgress(), (MainActivity)getActivity(), new OnCompletedListener() {
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
                HueBulbChangeUtility.changeGroupColor(identifier, newColor, (MainActivity)getActivity(), new OnCompletedListener() {
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

        //for advanced settings, the default group must also work (id == "0")
        advancedSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(RealHomeFragment.lightPositionString, identifier);
                bundle.putBoolean(RealHomeFragment.groupOrLightString, true);
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
                bundle.putBoolean(RealHomeFragment.groupOrLightString, true);
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
        PHBridgeResourcesCache cache = hueSDK.getSelectedBridge().getResourceCache();
        if(!HueBulbChangeUtility.DEFAULT_GROUP_ID.equals(identifier) && cache.getGroups().get(identifier) == null) {
            //each only needs to pop their back stack

            if(popping) {
                return;
            }
            getFragmentManager().popBackStack();
            popping = true;
            return;
        }
        if(!nameEditor.hasFocus() && shouldUpdateName == 0) {
            nameEditor.setText(HueBulbChangeUtility.getGroupName(identifier));
        }
        PHLight currentLight;
        if(HueBulbChangeUtility.DEFAULT_GROUP_ID.equals(identifier)) {
            Collection<String> lights = cache.getLights().keySet();
            currentLight = getFirstReachableBulb(lights);
            if(currentLight == null) {
               return;
            }
        } else {
            currentLight = getFirstReachableBulb(cache.getGroups().get(identifier).getLightIdentifiers());
            if(currentLight == null) {
                return;
            }
        }
        PHLightState state = currentLight.getLastKnownLightState();
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

    /**
     * Returns the first reachable bulb identified by the ids in the collection
     * If there isn't a reachable bulb it returns the first bulb in the colleciton
     * If there are no bulbs in the collection it returns null
     * @param lights the colleciton of bulb ids
     * @return
     */
    private PHLight getFirstReachableBulb(Collection<String> lights) {
        if(lights.isEmpty()) {
            return null;
        }
        Map<String, PHLight> lightCache = hueSDK.getSelectedBridge().getResourceCache().getLights();
        PHLight currentLight = null;
        for(String lightId: lights) {
            currentLight = lightCache.get(lightId);
            if(currentLight != null && currentLight.getLastKnownLightState().isReachable()) {
                return currentLight;
            }
        }
        return currentLight;
    }

    private int getCurrentBrightness(int phBrightness) {
        return (int) Math.round((phBrightness * 100.0) / HueBulbChangeUtility.MAX_BRIGHTNESS);
    }

    @Override
    public void cacheUpdated() {
        updateState();
    }

}

