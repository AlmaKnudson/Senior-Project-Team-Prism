package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

//TODO: I need to add repeat option showing Mon~Sun
//TODO: need to add option on, off, alert option
/**
 * A fragment representing a list of currentBulbAlarms.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 */
public class AlarmFragment extends Fragment {

    public static String lightPositionString = "CURRENT_BULB_POSITION";

    private int currentBulbId; // The chosen Light BULB ID
    private int chosenAlarmPosition;
    private PHLight currentBulb;
    private ListView alarmListView;
    static AlarmAdapter adapter;
    static private PHHueSDK phHueSDK;
    private static PHBridge bridge;
    private Date timeToSend;
    int alarmMode;
    AlertDialog modeDialog;
    TimePickerFragment timePickerDialog;
    String delegate;
    List<PHSchedule> alarmSchedules; // this List of schedules in bridge whose description is Alarm.

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlarmFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phHueSDK = PHHueSDK.getInstance();
        bridge = phHueSDK.getSelectedBridge();

        delegate = "hh:mm aaa";
        timeToSend = null;
        alarmMode = 0;
        modeDialog = null;
        timePickerDialog = null;

        if (getArguments() != null) {
            currentBulbId = getArguments().getInt(lightPositionString);
        }

        //get current bulb
        currentBulb = bridge.getResourceCache().getAllLights().get(currentBulbId);
        getAlarmSchedules();
    }

    // this function get list of schedule from the bridge, and return schedules that are alarm schedule and for this bulb
    private void getAlarmSchedules() {
        alarmSchedules = new ArrayList<PHSchedule>();
        // TODO: currently getting just non recurring schedules. to get recurring schedules, param need to be true.
        List<PHSchedule> nonRecurringSchedules = bridge.getResourceCache().getAllSchedules(false);

        // get schedules from Bridge that are for this bulb and has description "Alarm"

        String currentBulbIdentity = currentBulb.getIdentifier();

        for (int i=0; i<nonRecurringSchedules.size();i++)
        {
            PHSchedule schedule = nonRecurringSchedules.get(i);
            if(schedule.getLightIdentifier()!=null && schedule.getLightIdentifier().equals(currentBulbIdentity) && schedule.getDescription().equals("Alarm"))
            {
                alarmSchedules.add(nonRecurringSchedules.get(i));
            }
        }
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
            }
        });

        adapter = new AlarmAdapter();
        alarmListView.setAdapter(adapter);

        ImageView refreshButton = (ImageView)view.findViewById(R.id.refreshButton);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlarmSchedules();
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getAlarmSchedules();
        adapter.notifyDataSetChanged();
    }

    private class AlarmAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return alarmSchedules.size();
        }

        @Override
        public Object getItem(int position) {
            return alarmSchedules.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            final PHSchedule alarm = alarmSchedules.get(position);

            RelativeLayout currentView;
            if(convertView == null) {
                currentView = (RelativeLayout) LayoutInflater.from(AlarmFragment.this.getActivity()).inflate(R.layout.single_alarm, parent, false);
            } else {
                currentView = (RelativeLayout) convertView;
            }

            TextView timeView = (TextView) currentView.findViewById(R.id.singleAlarmTimeText);
            Date d = alarm.getDate();
            String timeString = (String) DateFormat.format(delegate, d.getTime());
            timeView.setText(timeString);

            TextView modeView = (TextView) currentView.findViewById(R.id.textMode);
            String modeString = null;
            PHLightState state = alarm.getLightState();
            if (!state.isOn())
                modeString = "Mode:Off";
            else if (state.getAlertMode() == PHLight.PHLightAlertMode.ALERT_SELECT)
                modeString = "Mode:Alert";
            else
                modeString = "Mode:On";

            modeView.setText(modeString);

            // When individual Alarm is clicked, open a time picker to change the Alarm
            timeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePickerDialog(position);
                }
            });

            TextView deleteTextView = (TextView) currentView.findViewById(R.id.alarmDeleteText);

            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteAlarm(position);
                }
            });

            return currentView;
        }
    }

    // this is TimePickerFragment, showTimePickerDialog create this DialogFragment.
    // when user click "done", onTimeSet get called.
    // TODO: is this ok non static? public class in a class.... private-> fragment have to be public,...?
    private class TimePickerFragment extends DialogFragment
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
                Date date = alarmSchedules.get(chosenAlarmPosition).getDate();
                hour = date.getHours();
                minute = date.getMinutes();
            }

            // Create a new instance of TimePickerDialog and return it

            return new TimePickerDialog(getActivity(), this, hour, minute, false);

        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            if (view.isShown()) {

                Date alarmTime = new Date();
                alarmTime.setHours(hourOfDay);
                alarmTime.setMinutes(minute);

                Date currentTime = new Date();
                if(alarmTime.getTime() < currentTime.getTime()) {
                    alarmTime = new Date(alarmTime.getTime()+86400000); //adding 24 hours in milliseconds
                }

                timeToSend = alarmTime;

                // Strings to Show In Dialog with Radio Buttons
                final CharSequence[] items = {" On "," Off "," Alert "};

                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Select The Alarm Mode");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {


                        switch (item) {
                            case 0:
                                alarmMode = 0; //on
                                break;
                            case 1:
                                alarmMode = 1; //off
                                break;
                            case 2:
                                alarmMode = 2; //alert
                                break;

                        }

                        // user want to add new Alarm
                        if (chosenAlarmPosition == -1) {
                            addNewAlarm(timeToSend);
                        }
                        // user wants to change existing alarm
                        else {
                            updateAlarm(chosenAlarmPosition, timeToSend);
                        }

                        modeDialog.dismiss();
                        getAlarmSchedules();
                        adapter.notifyDataSetChanged();
                    }
                });
                modeDialog = builder.create();
                timePickerDialog.dismiss();
                timePickerDialog = null;
                modeDialog.show();
            }
        }
    }

    //This function gets called when user want to add/change alarm, and this opens the timepicker.
    public void showTimePickerDialog(int alarmPosition) {
        chosenAlarmPosition = alarmPosition;
        timePickerDialog = new TimePickerFragment();
        timePickerDialog.show(getFragmentManager(), "timePicker");
    }

    private void updateAlarm(int alarmPosition, Date alarmTime) {

       PHSchedule schedule = alarmSchedules.get(alarmPosition);
       schedule.setDate(alarmTime);
       schedule.setLightState(getLightState());

        final PHWizardAlertDialog dialogManager = PHWizardAlertDialog
                .getInstance();
        dialogManager.showProgressDialog(R.string.sending_progress, getActivity());

        bridge.updateSchedule(schedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {

            }

            @Override
            public void onSuccess() {
                dialogManager.closeProgressDialog();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (isCurrentActivity()) {
                            PHWizardAlertDialog.showResultDialog(getActivity(), getString(R.string.txt_alarm_updated), R.string.btn_ok, R.string.txt_result);
                        }
                        getAlarmSchedules();
                        adapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onError(int i, final String s) {
                dialogManager.closeProgressDialog();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (isCurrentActivity()) {
                            PHWizardAlertDialog.showErrorDialog(getActivity(), s,R.string.btn_ok);
                        }
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });

    }

    private void addNewAlarm(Date alarmTime) {
        String scheduleName = (String) DateFormat.format(delegate, alarmTime.getTime());
        PHSchedule schedule = new PHSchedule(scheduleName);
        schedule.setDate(alarmTime);
        schedule.setLightIdentifier(currentBulb.getIdentifier());
        schedule.setLightState(getLightState());
        schedule.setDescription("Alarm");

        final PHWizardAlertDialog dialogManager = PHWizardAlertDialog.getInstance();
        dialogManager.showProgressDialog(R.string.sending_progress, getActivity());

        bridge.createSchedule(schedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {
                dialogManager.closeProgressDialog();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isCurrentActivity()) {
                            PHWizardAlertDialog.showResultDialog(getActivity(), getString(R.string.txt_alarm_created), R.string.btn_ok, R.string.txt_result);
                        }
                        getAlarmSchedules();
                        adapter.notifyDataSetChanged();
                    }
                });

                return;
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int i, final String s) {
                dialogManager.closeProgressDialog();

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (isCurrentActivity()) {
                            PHWizardAlertDialog.showErrorDialog(getActivity(), s, R.string.btn_ok);
                        }
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {
            }
        });
    }

    private void deleteAlarm(int position) {
        String scheduleID = alarmSchedules.get(position).getIdentifier();

        final PHWizardAlertDialog dialogManager = PHWizardAlertDialog.getInstance();
        dialogManager.showProgressDialog(R.string.sending_progress, getActivity());

        bridge.removeSchedule(scheduleID, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {

            }

            @Override
            public void onSuccess() {
                dialogManager.closeProgressDialog();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isCurrentActivity()) {
                            PHWizardAlertDialog.showResultDialog(getActivity(), getString(R.string.txt_alarm_deleted), R.string.btn_ok, R.string.txt_result);
                        }
                        getAlarmSchedules();
                        adapter.notifyDataSetChanged();
                    }
                });

                return;
            }

            @Override
            public void onError(int i, final String s) {
                dialogManager.closeProgressDialog();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isCurrentActivity()) { PHWizardAlertDialog.showErrorDialog(getActivity(), s, R.string.btn_ok); }
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    private boolean isCurrentActivity() {
        ActivityManager mActivityManager = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
        String currentClass = "." + this.getClass().getSimpleName();
        String topActivity =  ar.topActivity.getShortClassName().toString();
        return topActivity.contains(currentClass);
    }

    private PHLightState getLightState() {
        //PHLightState state = phHueSDK.getCurrentLightState();
        //currentBulb.getLastKnownLightState();
        PHLightState state = new PHLightState();

        switch(alarmMode)
        {
            case 0:
                state.setOn(true); //on
                break;
            case 1:
                state.setOn(false); //off
                break;
            case 2:
                state.setOn(true);
                state.setAlertMode(PHLight.PHLightAlertMode.ALERT_SELECT); //alert
                break;

        }
//        state.setHue(50);
//        state.setSaturation(50);
//        state.setBrightness(50);
//        state.setX((float)0);
//        state.setY((float)0);
//        state.setEffectMode(PHLight.PHLightEffectMode.EFFECT_NONE);
//        state.setAlertMode(PHLight.PHLightAlertMode.ALERT_NONE);
//        state.setTransitionTime(1);
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
