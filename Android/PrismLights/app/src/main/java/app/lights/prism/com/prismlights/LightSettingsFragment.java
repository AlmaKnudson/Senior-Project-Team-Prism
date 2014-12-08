package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import app.lights.prism.com.prismlights.R;

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
    private View currentColor;
    private ImageView colorPickerImage;

    private PHHueSDK hueSDK;

    private List<PHLight> currentLights;
    private String[] lightNames;


    public LightSettingsFragment() {
        hueSDK = PHHueSDK.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt("POSITION");

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
        currentColor = frame.findViewById(R.id.currentColor);
        colorPickerImage = (ImageView) frame.findViewById(R.id.colorPickerImage);
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getAllLights();
        lightNames = hueSDK.getLightNames(currentLights);
        PHLight currentLight = currentLights.get(position);
        PHLightState state = currentLight.getLastKnownLightState();
        nameEditor.setText(lightNames[position]);
        bulbOnState.setChecked(state.isOn());
        brightness.setProgress(state.getBrightness());
        brightnessPercentage.setText(state.getBrightness() + "%");
        currentColor.setBackgroundColor(PHUtilities.colorFromXY(new float[]{state.getX(), state.getY()}, ""));

        return frame;
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
