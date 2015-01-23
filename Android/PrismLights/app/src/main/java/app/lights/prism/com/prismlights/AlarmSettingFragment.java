package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.ArrayList;

import app.lights.prism.com.prismlights.R;

/**
TODO: this fragment need to be deleted.
 */
public class AlarmSettingFragment extends Fragment {
    private static final String ARG_PARAM1 = "BULB_POSITION";
    private static final String ARG_PARAM2 = "ALARM_POSITION";

    private int bulbPosition; // The id number of the chosen Light
    private int alarmPosition; // The position of the chosen/new alarm in the ArrayList alarms.

    //private ArrayList<Alarm> alarms = ((MainActivity)getActivity()).alarms.;

    //private OnFragmentInteractionListener mListener;

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment AlarmSettingFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static AlarmSettingFragment newInstance(String param1, String param2) {
//        AlarmSettingFragment fragment = new AlarmSettingFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    public AlarmSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bulbPosition = getArguments().getInt(ARG_PARAM1);
            alarmPosition = getArguments().getInt(ARG_PARAM2);
        }
    }

    //TODO: put default time, get time from picker put it into bundle and reopen the alarm fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm_setting, container, false);


        final TimePicker timePicker = (TimePicker)view.findViewById(R.id.alarmTimePicker);

        Button saveButton = (Button)view.findViewById(R.id.alarmSettingSaveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
      //         alarms.get(alarmPosition).time.hour = timePicker.getCurrentHour();
            }
        });
        return view;
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

}
