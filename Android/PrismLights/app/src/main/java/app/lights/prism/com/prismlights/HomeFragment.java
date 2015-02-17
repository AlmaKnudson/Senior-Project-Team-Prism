package app.lights.prism.com.prismlights;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHLight;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements CacheUpdateListener{
//    private OnFragmentInteractionListener mListener;


    private PHHueSDK hueSDK;

    private List<PHLight> currentLights;
    private String[] lightNames;

    private GridView gridView;

    private static int disabledOverlay = Color.argb(125, 0, 0, 0);
    private static int offOverlay = Color.argb(50, 0, 0, 0);

    public HomeFragment() {
        hueSDK = PHHueSDK.getInstance();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getAllLights();
        lightNames = hueSDK.getLightNames(currentLights);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_home, container, false);
        gridView= (GridView) frame.findViewById(R.id.homeGridView);
        gridView.setAdapter(new HomeGridAdapter());

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                HueBulbChangeUtility.toggleBulbState(position);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                //TODO: I need to change this for group of lights. now it is just for a single light.
                Bundle bundle = new Bundle();
                bundle.putInt(LightSettingsFragment.positionString, position);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                LightSettingsFragment lightSettingFragment = new LightSettingsFragment();
                lightSettingFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, lightSettingFragment);
                fragmentTransaction.addToBackStack("lightsettings");
                fragmentTransaction.commit();

                return false;
            }
        });

        return frame;
    }

    @Override
    public void cacheUpdated() {
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getAllLights();
        lightNames = hueSDK.getLightNames(currentLights);
        gridView.invalidateViews();
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
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


    private class HomeGridAdapter extends BaseAdapter {


        public HomeGridAdapter() {
            super();
            currentLights = hueSDK.getSelectedBridge().getResourceCache().getAllLights();
            lightNames = hueSDK.getLightNames(currentLights);
        }
        @Override
        public int getCount() {
            return currentLights.size();
        }

        @Override
        public Object getItem(int position) {
            return currentLights.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PHLight currentLight = (PHLight) getItem(position);
            String lightName = lightNames[position];
            LinearLayout currentView;
            if(convertView == null) {
                currentView = (LinearLayout) LayoutInflater.from(HomeFragment.this.getActivity()).inflate(R.layout.bulb_view, parent, false);
            } else {
                currentView = (LinearLayout) convertView;
            }
            ImageView bulbTop = (ImageView) currentView.findViewById(R.id.bulbTop);
            TextView bulbName = (TextView) currentView.findViewById(R.id.bulbName);
            bulbName.setText(lightName);

            if(!currentLight.getLastKnownLightState().isReachable()) {
                ImageView bulbBottom = (ImageView) currentView.findViewById(R.id.bulbBottom);
                bulbBottom.setColorFilter(disabledOverlay);
                bulbTop.setColorFilter(disabledOverlay);
                return currentView;
            }
            if(!currentLight.getLastKnownLightState().isOn()) {
                bulbTop.setColorFilter(offOverlay);
                return currentView;
            }
            //TODO make work with alternate color formats
            Float x = currentLight.getLastKnownLightState().getX();
            Float y = currentLight.getLastKnownLightState().getY();
            int currentColor = PHUtilities.colorFromXY(new float[]{x, y}, HueBulbChangeUtility.colorXYModelForHue);
            currentColor = Color.argb(300, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
            bulbTop.setColorFilter(currentColor);
            return currentView;
        }


    }

}
