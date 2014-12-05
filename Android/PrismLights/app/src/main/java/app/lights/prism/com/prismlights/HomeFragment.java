package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
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
public class HomeFragment extends Fragment {
//    private OnFragmentInteractionListener mListener;


    PHHueSDK hueSDK;

    public HomeFragment() {
        hueSDK = PHHueSDK.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_home, container, false);
        GridView gridView= (GridView) frame.findViewById(R.id.homeGridView);
        gridView.setAdapter(new HomeGridAdapter());
        return frame;
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

        private List<PHLight> currentLights;
        private String[] lightNames;

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
            TextView bulbName = (TextView) currentView.findViewById(R.id.bulbName);
            bulbName.setText(lightName);
            return currentView;
        }


    }

}
