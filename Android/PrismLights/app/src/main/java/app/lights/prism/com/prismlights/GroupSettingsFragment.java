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

import java.util.List;

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



    public GroupSettingsFragment() {
        hueSDK = PHHueSDK.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        identifier = getArguments().getString(RealHomeFragment.lightPositionString);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View frame = inflater.inflate(R.layout.fragment_group_settings, container, false);
        nameEditor = (EditText) frame.findViewById(R.id.nameEditor);
        bulbOnState = (ToggleButton) frame.findViewById(R.id.bulbOnState);
        brightness = (SeekBar) frame.findViewById(R.id.brightness);
        brightnessPercentage = (TextView) frame.findViewById(R.id.brightnessLabel);
        colorPicker = (ColorPickerViewGroup) frame.findViewById(R.id.colorPickerView);

        advancedSettingButton = (Button)frame.findViewById(R.id.advancedSettingButton);
        colorCycleButton = (Button)frame.findViewById(R.id.colorCycleButton);

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
                        HueBulbChangeUtility.changeGroupName(identifier, nameEditor.getText().toString());
                        nameEditor.clearFocus();
                    }
                    return false;
                }
            });
        }
        //doesn't used onCheckedChanged to avoid programmatic sending
        bulbOnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton bulbOn = (ToggleButton) v;
                HueBulbChangeUtility.turnGroupOnOff(identifier, bulbOn.isChecked(), (MainActivity)getActivity());
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
                HueBulbChangeUtility.changeGroupBrightness(identifier, seekBar.getProgress(), (MainActivity)getActivity());
            }
        });
        colorPicker.setColorChangedListener(new ColorChangedListener() {
            @Override
            public void onColorChanged(float[] newColor) {
                currentColor = newColor;
                HueBulbChangeUtility.changeGroupColor(identifier, newColor, (MainActivity)getActivity());
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
        if(!nameEditor.hasFocus()) {
            nameEditor.setText(HueBulbChangeUtility.getGroupName(identifier));
        }
        PHBridgeResourcesCache cache = hueSDK.getSelectedBridge().getResourceCache();
        PHLight currentLight;
        if(HueBulbChangeUtility.DEFAULT_GROUP_ID.equals("0")) {
            List<PHLight> lights = cache.getAllLights();
            if(!lights.isEmpty()) {
               currentLight = lights.get(0);
            } else {
                return;
            }
        } else {
            currentLight = cache.getLights().get(cache.getGroups().get(identifier).getLightIdentifiers().get(0));
        }
        PHLightState state = currentLight.getLastKnownLightState();

        bulbOnState.setChecked(state.isOn());
        int currentBrightness = getCurrentBrightness(state.getBrightness());
        brightness.setProgress(currentBrightness);
        brightnessPercentage.setText(currentBrightness + "%");
        currentColor = new float[]{state.getX(), state.getY()};
        colorPicker.setColor(currentColor);
    }

    private int getCurrentBrightness(int phBrightness) {
        return (int) Math.round((phBrightness * 100.0) / HueBulbChangeUtility.MAX_BRIGHTNESS);
    }

    @Override
    public void cacheUpdated() {
        updateState();
    }

}

