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
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;

public class LightSettingsFragment extends Fragment implements CacheUpdateListener{


    private String identifier; // The id of the chosen Light
    private EditText nameEditor;
    private ToggleButton bulbOnState;
    private SeekBar brightness;
    private TextView brightnessPercentage;
    private float[] currentColor;
    private ColorPickerViewGroup colorPicker;

    private Button advancedSettingButton;

    private PHHueSDK hueSDK;



    public LightSettingsFragment() {
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
        View frame = inflater.inflate(R.layout.fragment_light_settings, container, false);
        nameEditor = (EditText) frame.findViewById(R.id.nameEditor);
        bulbOnState = (ToggleButton) frame.findViewById(R.id.bulbOnState);
        brightness = (SeekBar) frame.findViewById(R.id.brightness);
        brightnessPercentage = (TextView) frame.findViewById(R.id.brightnessLabel);
        colorPicker = (ColorPickerViewGroup) frame.findViewById(R.id.colorPickerView);

        advancedSettingButton = (Button)frame.findViewById(R.id.advancedSettingButton);

        updateState();
        nameEditor.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    //TODO check if name is valid
                    HueBulbChangeUtility.changeLightName(identifier, nameEditor.getText().toString());
                    nameEditor.clearFocus();
//                    shouldUpdateFromCache = 2;
                }
                return false;
            }
        });
        //doesn't used onCheckedChanged to avoid programmatic sending
        bulbOnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton bulbOn = (ToggleButton) v;
                HueBulbChangeUtility.turnBulbOnOff(identifier, bulbOn.isChecked());
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
                HueBulbChangeUtility.changeBrightness(identifier, seekBar.getProgress());
            }
        });
        colorPicker.setColorChangedListener(new ColorChangedListener() {
            @Override
            public void onColorChanged(float[] newColor) {
                currentColor = newColor;
                HueBulbChangeUtility.changeBulbColor(identifier, newColor);
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
        return frame;
    }

    private void updateState() {
        PHLight  currentLight = hueSDK.getSelectedBridge().getResourceCache().getLights().get(identifier);
        PHLightState state = currentLight.getLastKnownLightState();
        if(!nameEditor.hasFocus()) {
            nameEditor.setText(currentLight.getName());
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
