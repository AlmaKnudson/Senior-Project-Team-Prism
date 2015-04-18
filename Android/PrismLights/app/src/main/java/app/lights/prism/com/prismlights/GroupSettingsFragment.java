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
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

public class GroupSettingsFragment extends Fragment implements CacheUpdateListener {
    private String identifier; // The id of the chosen Light
    private EditText nameEditor;
    private ToggleButton bulbOnState;
    private SeekBar brightness;
    private TextView brightnessPercentage;
    private float[] currentColor;
    private ColorPickerViewGroup colorPicker;

    private Button advancedSettingButton;
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
        editGroupButton = (ImageButton) frame.findViewById(R.id.editGroupButton);
        updateState();
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
                    //TODO check if name is valid
                    HueBulbChangeUtility.changeGroupName(identifier, nameEditor.getText().toString());
                    nameEditor.clearFocus();
                }
                return false;
            }
        });
        //doesn't used onCheckedChanged to avoid programmatic sending
        bulbOnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton bulbOn = (ToggleButton) v;
                HueBulbChangeUtility.turnGroupOnOff(identifier, bulbOn.isChecked());
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
                HueBulbChangeUtility.changeGroupBrightness(identifier, seekBar.getProgress());
            }
        });
        colorPicker.setColorChangedListener(new ColorChangedListener() {
            @Override
            public void onColorChanged(float[] newColor) {
                currentColor = newColor;
                HueBulbChangeUtility.changeGroupColor(identifier, newColor);
            }
        });

//        advancedSettingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Bundle bundle = new Bundle();
//                bundle.putString(RealHomeFragment.lightPositionString, identifier);
//                bundle.putBoolean(RealHomeFragment.groupOrLightString, isGroup);
//                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                AdvancedSettingFragment advancedSettingFragment = new AdvancedSettingFragment();
//                advancedSettingFragment.setArguments(bundle);
//                fragmentTransaction.replace(R.id.container, advancedSettingFragment);
//                fragmentTransaction.addToBackStack("AdvancedSettings");
//                fragmentTransaction.commit();
//            }
//        });

        return frame;
    }

    private void updateState() {
        PHLight currentLight;
        PHGroup currentGroup = null;
        currentGroup = hueSDK.getSelectedBridge().getResourceCache().getGroups().get(identifier);
        currentLight = hueSDK.getSelectedBridge().getResourceCache().getLights().get(currentGroup.getLightIdentifiers().get(0));
        PHLightState state = currentLight.getLastKnownLightState();
        if(!nameEditor.hasFocus()) {
            nameEditor.setText(currentGroup.getName());
        }
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

