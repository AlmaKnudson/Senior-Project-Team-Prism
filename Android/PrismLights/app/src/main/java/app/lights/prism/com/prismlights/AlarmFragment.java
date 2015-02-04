package app.lights.prism.com.prismlights;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


/**
 * A fragment representing a list of currentBulbAlarms.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 */
public class AlarmFragment extends Fragment {

    private static final String ARG_PARAM1 = "CURRENT_BULB_ID";

    private static int currentBulbId; // The chosen Light BULB ID
    private static int chosenAlarmPosition;
    private static ArrayList<Alarm> currentBulbAlarms;
    private ListView alarmListView;
    static AlarmAdapter adapter;
    static private PHHueSDK phHueSDK;
    private static PHBridge bridge;
    String delegate;
    //private OnFragmentInteractionListener mListener;



//    public static AlarmFragment newInstance(int param1) {
//        AlarmFragment fragment = new AlarmFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_PARAM1, param1);
//        fragment.setArguments(args);
//        return fragment;
//    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlarmFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phHueSDK = ((MainActivity)getActivity()).hueBridgeSdk;
        bridge = phHueSDK.getSelectedBridge();

        delegate = "hh:mm aaa";
        if (getArguments() != null) {
            currentBulbId = getArguments().getInt(ARG_PARAM1);
        }

        try{
            currentBulbAlarms = ((MainActivity)getActivity()).alarms.get(currentBulbId);
        }
        catch(IndexOutOfBoundsException e) {
            ((MainActivity) getActivity()).alarms.add(currentBulbId, new ArrayList<Alarm>());
            currentBulbAlarms = ((MainActivity)getActivity()).alarms.get(currentBulbId);
        }

        currentBulbAlarms = ((MainActivity)getActivity()).alarms.get(currentBulbId);
        chosenAlarmPosition = -1;// default is -1 when user choose an alarm, or add new alarm, it changes appropriately.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        ImageView addButton = (ImageView)view.findViewById(R.id.alarmPlusButton);

        alarmListView = (ListView)view.findViewById(R.id.alarmListView);

        // When + image is click, open a time picker to create new alarm.
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(-1);

//                Bundle bundle = new Bundle();
//                bundle.putInt("BULB_POSITION", currentBulbId);
//                bundle.putInt("ALARM_POSITION", -1); // alarm position -1 means new alarm.
//
//                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                AlarmSettingFragment alarmSettingFragment = new AlarmSettingFragment();
//                alarmSettingFragment.setArguments(bundle);
//                fragmentTransaction.replace(R.id.container, alarmSettingFragment);
//                fragmentTransaction.addToBackStack("AlarmSettingFragment");
//                fragmentTransaction.commit();
            }
        });

        adapter = new AlarmAdapter();
        alarmListView.setAdapter(adapter);

        // When individual Alarm is clicked, open a time picker to change the Alarm
        alarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                showTimePickerDialog(position);
//                Bundle bundle = new Bundle();
//                bundle.putInt("BULB_POSITION", currentBulbId);
//                bundle.putInt("ALARM_POSITION", position);
//
//                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                AlarmSettingFragment alarmSettingFragment = new AlarmSettingFragment();
//                alarmSettingFragment.setArguments(bundle);
//                fragmentTransaction.replace(R.id.container, alarmSettingFragment);
//                fragmentTransaction.addToBackStack("AlarmSettingFragment");
//                fragmentTransaction.commit();
            }
        });

        return view;
    }


    private class AlarmAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return currentBulbAlarms.size();
        }

        @Override
        public Object getItem(int position) {
            return currentBulbAlarms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            final Alarm alarm = currentBulbAlarms.get(position);

            Calendar c = alarm.cal;

            String timeString = (String) DateFormat.format(delegate, c.getTime());
            RelativeLayout currentView;
            if(convertView == null) {
                currentView = (RelativeLayout) LayoutInflater.from(AlarmFragment.this.getActivity()).inflate(R.layout.single_alarm, parent, false);
            } else {
                currentView = (RelativeLayout) convertView;
            }

            TextView timeView = (TextView) currentView.findViewById(R.id.singleAlarmTimeText);
            timeView.setText(timeString);


            Switch switchView = (Switch) currentView.findViewById(R.id.singleAlarmSwitch);
            switchView.setChecked(alarm.isOn);

            switchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Switch)v).toggle();
                    if (alarm.isOn) {
                        turnOffAlarm(position, currentBulbId);
                    }
                    else {
                        turnOnAlarm(position, currentBulbId);
                    }

                    adapter.notifyDataSetChanged();
                }
            });

            return currentView;
        }
    }

    private static void turnOnAlarm(int alarmPosition, int bulbId) {
        currentBulbAlarms.get(alarmPosition).isOn = true;

        String scheduleId = bulbId + "_" + alarmPosition;
        PHSchedule schedule = new PHSchedule(scheduleId);


        Calendar calendar = currentBulbAlarms.get(alarmPosition).cal;
        schedule.setDate( calendar.getTime());
        List<PHLight> lights = bridge.getResourceCache().getAllLights();
        PHLight light = lights.get(currentBulbId);
        String lightIdentifier = light.getIdentifier();

        schedule.setLightIdentifier(lightIdentifier);

        PHLightState lightState = getNewLightState();

        schedule.setLightState(lightState);

        schedule.setDescription("testing");

        schedule.setRecurringDays(PHSchedule.RecurringDay.RECURRING_ALL_DAY.getValue());

        schedule.setLocalTime(true);


        bridge.createSchedule(schedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {
                int i = 0;
                //Toast.makeText(AlarmFragment.this.getActivity(), "Alarm is set up", Toast.LENGTH_LONG);
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });

    }

    private static void turnOffAlarm(int alarmPosition, int bulbId) {
        currentBulbAlarms.get(alarmPosition).isOn = false;

        String scheduleId = bulbId + "_" + alarmPosition;
        bridge.removeSchedule(scheduleId, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    //This function gets called when user want to add/change alarm, and this opens the timepicker.
    public void showTimePickerDialog(int alarmPosition) {
        chosenAlarmPosition = alarmPosition;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    // this is TimePickerFragment, showTimePickerDialog create this DialogFragment.
    // when user click "done", onTimeSet get called.
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour;
            int minute;
            if (chosenAlarmPosition == -1) {
                // Use the current time as the default values for the picker
                final Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }
            else{
                Calendar cal = currentBulbAlarms.get(chosenAlarmPosition).cal;
                hour = cal.HOUR_OF_DAY;
                minute = cal.MINUTE;
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            if (view.isShown()) {
                // user want to add new Alarm
                if (chosenAlarmPosition == -1)
                {
                    Calendar alarmTime = Calendar.getInstance();
                    alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    alarmTime.set(Calendar.MINUTE, minute);
                    currentBulbAlarms.add(new Alarm(alarmTime));
                    turnOnAlarm(currentBulbAlarms.size()-1, currentBulbId);
                }
                // user wants to change existing alarm
                else
                {
                    turnOffAlarm(chosenAlarmPosition,currentBulbId); // turn off the previous alarm
                    Calendar alarmTime = currentBulbAlarms.get(chosenAlarmPosition).cal;
                    alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    alarmTime.set(Calendar.MINUTE, minute);
                    turnOnAlarm(chosenAlarmPosition,currentBulbId); // turn on the new alarm
                }

            }

            adapter.notifyDataSetChanged();
        }
    }

    static private PHLightState getNewLightState() {
        PHLightState state = new PHLightState();

        state.setOn(true);
        state.setHue(50);
        state.setSaturation(50);
        state.setBrightness(50);
        state.setX((float)0);
        state.setY((float)0);
        state.setEffectMode(PHLight.PHLightEffectMode.EFFECT_NONE);
        state.setAlertMode(PHLight.PHLightAlertMode.ALERT_NONE);
        state.setTransitionTime(1);
        return state;
    }

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
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int currentBulbId, long id) {
//        if (null != mListener) {
//            // Notify the active callbacks interface (the activity, if the
//            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(AlarmList.currentBulbAlarms.get(currentBulbId).name);
//        }
//    }
//
//    /**
//     * The default content for this Fragment has a TextView that is shown when
//     * the list is empty. If you would like to change the text, call this method
//     * to supply the text it should use.
//     */
//    public void setEmptyText(CharSequence emptyText) {
//        View emptyView = alarmListView.getEmptyView();
//
//        if (emptyView instanceof TextView) {
//            ((TextView) emptyView).setText(emptyText);
//        }
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
//        public void onFragmentInteraction(String id);
//    }

}
