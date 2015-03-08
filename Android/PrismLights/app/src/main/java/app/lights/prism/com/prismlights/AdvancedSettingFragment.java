package app.lights.prism.com.prismlights;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class AdvancedSettingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static String lightPositionString = "CURRENT_BULB_POSITION";



    private int currentBulbId; // ID of the chosen Light
    private TextView alarmText;
    private TextView timerText;
    private TextView scheduleText;


    public AdvancedSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentBulbId = getArguments().getInt(lightPositionString);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_advanced_setting, container, false);

        alarmText = (TextView)view.findViewById(R.id.alarmTextAdvancedSetting);
        timerText = (TextView)view.findViewById(R.id.timerTextAdvancedSetting);
        scheduleText = (TextView)view.findViewById(R.id.scheduleTextAdvancedSetting);

        alarmText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(lightPositionString, currentBulbId);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                AlarmFragment alarmFragment = new AlarmFragment();
                alarmFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, alarmFragment);
                fragmentTransaction.addToBackStack("AlarmFragment");
                fragmentTransaction.commit();
            }
        });

        timerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(lightPositionString, currentBulbId);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                TimerFragment timerFragment = new TimerFragment();
                timerFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, timerFragment);
                fragmentTransaction.addToBackStack("TimerFragment");
                fragmentTransaction.commit();
            }
        });

        scheduleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(lightPositionString, currentBulbId);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ScheduleFragment scheduleFragment = new ScheduleFragment();
                scheduleFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, scheduleFragment);
                fragmentTransaction.addToBackStack("ScheduleFragment");
                fragmentTransaction.commit();
            }
        });


        return view;

    }
//
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
//
}
