package app.lights.prism.com.prismlights;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LightSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LightSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LightSettingsFragment extends Fragment {

//    private OnFragmentInteractionListener mListener;

    //TODO: I need to change this for group of lights. now it is just for a single light.
    private int position; // The number for the chosen Light
    private EditText nameEditor;
    private Switch bulbOnState;
    private SeekBar brightness;
    private TextView brightnessPercentage;
    private View currentColorView;
    private int currentColor;
    private ImageView colorPickerImage;

    private Button applyRulesButton;

    private PHHueSDK hueSDK;

    private List<PHLight> currentLights;
    private String[] lightNames;


    public static String positionString = "CURRENT_BULB_ID";

    public LightSettingsFragment() {
        hueSDK = PHHueSDK.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt(positionString);

        Toast.makeText(getActivity(), "SettingFragment opened with light " + position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_light_settings, container, false);
        nameEditor = (EditText) frame.findViewById(R.id.nameEditor);
        bulbOnState = (Switch) frame.findViewById(R.id.bulbOnState);
        brightness = (SeekBar) frame.findViewById(R.id.brightness);
        brightnessPercentage = (TextView) frame.findViewById(R.id.brightnessLabel);
        currentColorView = frame.findViewById(R.id.currentColor);
        colorPickerImage = (ImageView) frame.findViewById(R.id.colorPickerImage);

        applyRulesButton = (Button)frame.findViewById(R.id.applyRulesButton);

        currentLights = hueSDK.getSelectedBridge().getResourceCache().getAllLights();
        lightNames = hueSDK.getLightNames(currentLights);
        PHLight currentLight = currentLights.get(position);
        PHLightState state = currentLight.getLastKnownLightState();
        nameEditor.setText(lightNames[position]);
        bulbOnState.setChecked(state.isOn());
        int currentBrightness = getCurrentBrightness(state.getBrightness());
        brightness.setProgress(currentBrightness);
        brightnessPercentage.setText(currentBrightness + "%");
        currentColor = PHUtilities.colorFromXY(new float[]{state.getX(), state.getY()}, "");
        currentColorView.setBackgroundColor(currentColor);
        nameEditor.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    //TODO check if name is valid
                    HueBulbChangeUtility.changeLightName(position, nameEditor.getText().toString());
                }
                return false;
            }
        });
        bulbOnState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HueBulbChangeUtility.turnBulbOnOff(position, isChecked);
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
                HueBulbChangeUtility.changeBrightness(position, seekBar.getProgress());
            }
        });
        colorPickerImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                //I though ACTION_OUTSIDE would cover this but it doesn't seem to
                if(x < 0 || y < 0 || y > colorPickerImage.getHeight() || x > colorPickerImage.getWidth()) {
                    currentColorView.setBackgroundColor(currentColor);
                    return false;
                }
                //the image view coords must be translated to the bitmap coordinates or it will select the wrong color
                Bitmap bitmap = ((BitmapDrawable) (colorPickerImage.getDrawable())).getBitmap();
                double yRatio =  (double) bitmap.getHeight() / colorPickerImage.getHeight();
                double xRatio =  (double) bitmap.getWidth() / colorPickerImage.getWidth();
                x = (int) Math.round(x * xRatio);
                y = (int) Math.round(y * yRatio);
                int color = bitmap.getPixel(x, y);
                currentColorView.setBackgroundColor(color);
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    currentColor = color;
                    HueBulbChangeUtility.changeBulbColor(position, color);
                }
                return true;
            }
        });

        applyRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("POSITION", position);

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

    private int getCurrentBrightness(int phBrightness) {
        return (int) Math.round((phBrightness * 100.0) / HueBulbChangeUtility.MAX_BRIGHTNESS);
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
