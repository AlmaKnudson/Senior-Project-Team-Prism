package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by Brian_Oh on 2/10/15.
 */
public class TimerFragment extends Fragment implements CacheUpdateListener{

    public static String lightPositionString = "CURRENT_BULB_POSITION";
    private boolean popping;

    private String identifier;
    private boolean isGroup;
    private int chosenTimerPosition;
    private ListView timerListView;
    static TimerAdapter adapter;
    static private PHHueSDK phHueSDK;
    private static PHBridge bridge;
    List<PHSchedule> currentTimers; // this List of Timer schedules in bridge.
    private List<CountDownTimer> countDownTimers; // List of the Countdown for timers.

    private int timeToSend;
    int alarmMode;
    AlertDialog modeDialog;
    TimePickerFragment timePickerDialog;
    private Dialog progressDialog;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TimerFragment() {
        popping = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phHueSDK = PHHueSDK.getInstance();
        bridge = phHueSDK.getSelectedBridge();

        timeToSend = 0;
        alarmMode = 0;
        modeDialog = null;
        timePickerDialog = null;

        if (getArguments() != null) {
            identifier = getArguments().getString(RealHomeFragment.lightPositionString);
            isGroup = getArguments().getBoolean(RealHomeFragment.groupOrLightString);
        }
            if(popping || HueBulbChangeUtility.popBackStackIfItemNotExist(identifier, isGroup, getFragmentManager())) {
                popping = true;
                return;
            }
        //get current bulb
        getCurrentTimers();
        countDownTimers = new ArrayList<>();
    }

    // this function get list of schedule from the bridge, and return schedules that are timer schedule and for this bulb
    private void getCurrentTimers() {
        currentTimers = new ArrayList<PHSchedule>();

        List<PHSchedule> allTimers = bridge.getResourceCache().getAllTimers(false);

        // get schedules from Bridge those are for this bulb

        for (int i=0; i< allTimers.size();i++)
        {
            PHSchedule schedule = allTimers.get(i);

            if(isGroup){
                if (schedule.getGroupIdentifier() != null && schedule.getGroupIdentifier().equals(identifier)) {
                    currentTimers.add(allTimers.get(i));
                }
            } else {
                if (schedule.getLightIdentifier() != null && schedule.getLightIdentifier().equals(identifier)) {
                    currentTimers.add(allTimers.get(i));
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        if(popping || HueBulbChangeUtility.popBackStackIfItemNotExist(identifier, isGroup, getFragmentManager())) {
            popping = true;
            return view;
        }
        ImageView addButton = (ImageView)view.findViewById(R.id.timerPlusButton);

        timerListView = (ListView)view.findViewById(R.id.timerListView);

        // When + image is click, open a time picker to create new timer.
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(-1);
            }
        });

        adapter = new TimerAdapter();

        timerListView.setAdapter(adapter);

//        timerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                showTimePickerDialog(position);
//            }
//        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(popping || HueBulbChangeUtility.popBackStackIfItemNotExist(identifier, isGroup, getFragmentManager())) {
            popping = true;
            return;
        }
        getCurrentTimers();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        clearCountDown();
    }

    @Override
    public void cacheUpdated() {
        if(popping || HueBulbChangeUtility.popBackStackIfItemNotExist(identifier, isGroup, getFragmentManager())) {
            popping = true;
            return;
        }
        clearCountDown();
        getCurrentTimers();
        adapter.notifyDataSetChanged();
    }

    private class TimerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return currentTimers.size();
        }

        @Override
        public Object getItem(int position) {
            return currentTimers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            final PHSchedule timer = currentTimers.get(position);

            RelativeLayout currentView;
            if(convertView == null) {
                currentView = (RelativeLayout) LayoutInflater.from(TimerFragment.this.getActivity()).inflate(R.layout.single_timer, parent, false);
            } else {
                currentView = (RelativeLayout) convertView;
            }

            final TextView timeView = (TextView) currentView.findViewById(R.id.singleTimerTimeText);

            final TextView deleteTextView = (TextView) currentView.findViewById(R.id.TimerDeleteText);

            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (timeView.getText() == "Done") {
                        clearCountDown();
                        getCurrentTimers();
                        adapter.notifyDataSetChanged();
                    } else
                        deleteTimer(position);
                }
            });


            TextView modeView = (TextView) currentView.findViewById(R.id.textMode);
            String modeString = null;
            PHLightState state = timer.getLightState();
            if (!state.isOn())
                modeString = "Mode:Off";
            else if (state.getAlertMode() == PHLight.PHLightAlertMode.ALERT_SELECT)
                modeString = "Mode:Alert";
            else
                modeString = "Mode:On";

            modeView.setText(modeString);


            long startTime = Long.parseLong(timer.getDescription().split("[,]")[1]);

            //Date createdTime = timer.getCreated();

            int duration = timer.getTimer();


            long endTime = startTime + (long) (duration * 1000); //add duration of the timer to current time.

            long timeLeft = endTime - new Date().getTime(); // calculate the difference between current time and timer time

//            if (timeLeft < 0) {
//                timeView.setText("Error");
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        DialogCreator.showWarningDialog(getText(R.string.time_sync_error).toString(), getText(R.string.time_sync_error_message).toString(), (MainActivity)getActivity());
//                    }
//                });
//            }
//            else {
            CountDownTimer countDownTimer = new CountDownTimer(timeLeft, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //changer time left text
                    String timerString = getTimerString((int) millisUntilFinished / 1000);
                    timeView.setText(timerString);
                }

                @Override
                public void onFinish() {
                    // clean currentTimers and countDownTimers and refresh the adapter
                    timeView.setText("Done");
                }
            };

            countDownTimer.start();

            countDownTimers.add(position, countDownTimer);
            //}

            return currentView;
        }
    }

    private void clearCountDown() {
        if(countDownTimers != null) {
            for (int i = 0; i < countDownTimers.size(); i++) {
                countDownTimers.get(i).cancel();
            }
            countDownTimers.clear();
            countDownTimers = new ArrayList<CountDownTimer>();
        }
    }

    private String getTimerString(int timerTime) {
        int hours = timerTime / 3600;
        int minutes = (timerTime % 3600) / 60;
        String min;
        if (minutes < 10)
            min = "0"+minutes;
        else
            min = ""+minutes;

        int seconds = timerTime % 60;
        String sec;
        if (seconds<10)
            sec = "0"+seconds;
        else
            sec = ""+seconds;

        String timerString = hours+":"+min+":"+sec;

        return timerString;
    }

    // this is TimePickerFragment, showTimePickerDialog create this DialogFragment.
    // when user click "done", onTimeSet get called.
    private class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour;
            int minute;

                hour = 0;
                minute = 0;
            TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), this, hour, minute, true);
            TextView textView = new TextView(getActivity());
            textView.setText(R.string.setTimer);
            textView.setTextAppearance(getActivity(), R.style.TitleTextView);
            timeDialog.setCustomTitle(textView);
            return timeDialog;
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {

            if (view.isShown()) {

                timeToSend = hour * 3600 + minute * 60;


                // Strings to Show In Dialog with Radio Buttons
                final CharSequence[] items = {" On "," Off "," Alert "};

                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);

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

                        // user want to add new Timer
                        if (chosenTimerPosition == -1)
                        {
                            addNewTimer(timeToSend);
                        }

                        modeDialog.dismiss();
//                        clearCountDown();
//                        getCurrentTimers();
//                        adapter.notifyDataSetChanged();
                    }
                });
                modeDialog = builder.create();
                timePickerDialog.dismiss();
                timePickerDialog = null;
                modeDialog.show();
            }
        }
    }

    //This function gets called when user want to add/change timer, and this opens the timepicker.
    public void showTimePickerDialog(int timerPosition) {
        chosenTimerPosition = timerPosition;
        timePickerDialog = new TimePickerFragment();
        timePickerDialog.show(getFragmentManager(), "timePicker");
    }


    private void addNewTimer(int time) {
        if(time == 0) {
            DialogCreator.showWarningDialog("Timer Length Cannot be Zero", "Please choose a longer time.", (MainActivity)getActivity());
            return;
        }
        String scheduleName = ""+ time;
        PHSchedule schedule = new PHSchedule(scheduleName);
        schedule.setTimer(time);
        if(isGroup)
            schedule.setGroupIdentifier(identifier);
        else
            schedule.setLightIdentifier(identifier);
        schedule.setLightState(getLightState());
        long startTime = new Date().getTime();
        schedule.setDescription("prism,"+startTime);

        progressDialog = DialogCreator.showLoadingDialog(getText(R.string.sending_progress).toString(), (MainActivity)getActivity());

        bridge.createSchedule(schedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        clearCountDown();
                        getCurrentTimers();
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
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        closeProgressDialog();
                        DialogCreator.showWarningDialog("Error", getText(R.string.failed_timer_create).toString(), (MainActivity)getActivity());
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {
            }
        });
    }

    private void deleteTimer(int position) {
        String scheduleID = currentTimers.get(position).getIdentifier();

        progressDialog = DialogCreator.showLoadingDialog(getText(R.string.sending_progress).toString(), (MainActivity)getActivity());

        bridge.removeSchedule(scheduleID, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {

            }

            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        clearCountDown();
                        getCurrentTimers();
                        adapter.notifyDataSetChanged();
                    }
                });

                return;
            }

            @Override
            public void onError(int i, final String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        DialogCreator.showWarningDialog("Error", getText(R.string.failed_timer_delete).toString(), (MainActivity)getActivity());
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
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

    private void closeProgressDialog() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    private void closeAllDialogs() {
        closeProgressDialog();
        if(modeDialog!= null && modeDialog.isShowing()) {
            modeDialog.dismiss();
        }
        if(timePickerDialog != null) {
            timePickerDialog.dismiss();
        }
    }
    @Override
    public void onDetach() {
        closeAllDialogs();
        super.onDetach();
    }
}
