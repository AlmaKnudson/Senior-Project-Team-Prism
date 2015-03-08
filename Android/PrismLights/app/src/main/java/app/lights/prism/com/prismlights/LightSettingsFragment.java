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
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;

public class LightSettingsFragment extends Fragment implements CacheUpdateListener{

//    private OnFragmentInteractionListener mListener;

    //TODO: I need to change this for group of lights. now it is just for a single light.
    private int position; // The number for the chosen Light
    private boolean isGroup; //True if group false otherwise
    private EditText nameEditor;
    private Switch bulbOnState;
    private SeekBar brightness;
    private TextView brightnessPercentage;
    private float[] currentColor;
    private ColorPickerViewGroup colorPicker;

    private Button advancedSettingButton;

    private PHHueSDK hueSDK;

    private List<PHLight> currentLights;
    private String[] lightNames;
    private List<PHGroup> currentGroups;
    private String[] groupNames;



    public LightSettingsFragment() {
        hueSDK = PHHueSDK.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt(HomeFragment.lightPositionString);
        isGroup = getArguments().getBoolean(HomeFragment.groupOrLightString);
        Toast.makeText(getActivity(), "SettingFragment opened with light " + position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_light_settings, container, false);
        nameEditor = (EditText) frame.findViewById(R.id.nameEditor);
        bulbOnState = (Switch) frame.findViewById(R.id.bulbOnState);
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
                    if(isGroup) {
                        HueBulbChangeUtility.changeGroupName(position, nameEditor.getText().toString());
                    } else {
                        HueBulbChangeUtility.changeLightName(position, nameEditor.getText().toString());
                    }
                    nameEditor.clearFocus();
                }
                return false;
            }
        });
        //doesn't used onCheckedChanged to avoid programmatic sending
        bulbOnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch bulbOn = (Switch) v;
                if(isGroup) {
                    HueBulbChangeUtility.turnGroupOnOff(position, bulbOn.isChecked());
                } else {
                    HueBulbChangeUtility.turnBulbOnOff(position, bulbOn.isChecked());
                }
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
                if(isGroup) {
                    HueBulbChangeUtility.changeGroupBrightness(position, seekBar.getProgress());
                } else {
                    HueBulbChangeUtility.changeBrightness(position, seekBar.getProgress());
                }
            }
        });
        colorPicker.setColorChangedListener(new ColorChangedListener() {
            @Override
            public void onColorChanged(float[] newColor) {
                currentColor = newColor;
                if(isGroup) {
                    HueBulbChangeUtility.changeGroupColor(position, newColor);
                } else {
                    HueBulbChangeUtility.changeBulbColor(position, newColor);
                }
            }
        });

        if(!isGroup) {
            advancedSettingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(HomeFragment.lightPositionString, position);
                    bundle.putBoolean(HomeFragment.groupOrLightString, isGroup);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    AdvancedSettingFragment advancedSettingFragment = new AdvancedSettingFragment();
                    advancedSettingFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.container, advancedSettingFragment);
                    fragmentTransaction.addToBackStack("AdvancedSettings");
                    fragmentTransaction.commit();
                }
            });
        }

        return frame;
    }

    private void updateState() {
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getAllLights();
        currentGroups = hueSDK.getSelectedBridge().getResourceCache().getAllGroups();
        groupNames = hueSDK.getGroupNames(currentGroups);
        lightNames = hueSDK.getLightNames(currentLights);
        PHLight currentLight;
        if(isGroup) {
            //TODO find out if group can have no lights in it
            currentLight = hueSDK.getSelectedBridge().getResourceCache().getLights().get(currentGroups.get(position).getLightIdentifiers().get(0));
        } else {
            currentLight = currentLights.get(position);
        }
        PHLightState state = currentLight.getLastKnownLightState();
        if(!nameEditor.hasFocus()) {
            if(isGroup) {
                nameEditor.setText(groupNames[position]);
            } else {
                nameEditor.setText(lightNames[position]);
            }
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

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p/>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

}
