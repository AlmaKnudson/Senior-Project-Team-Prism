package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
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


// TODO: need to change timer picker. 00 hour 00 min

//TODO: need to change the format of time showing.

//TODO: need to fix the left time to show correctly.


/**
 * Created by Brian_Oh on 2/10/15.
 */
public class TimerFragment extends Fragment{

    private static final String ARG_PARAM1 = "CURRENT_BULB_ID";

    private int currentBulbId; // The chosen Light BULB ID
    private int chosenTimerPosition;
    private PHLight currentBulb;
    private ListView timerListView;
    static TimerAdapter adapter;
    static private PHHueSDK phHueSDK;
    private static PHBridge bridge;
    String delegate;
    List<PHSchedule> currentTimers; // this List of Timer schedules in bridge.
    private List<CountDownTimer> countDownTimers; // List of the Countdown for timers.

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TimerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phHueSDK = PHHueSDK.getInstance();
        bridge = phHueSDK.getSelectedBridge();

        // TODO: need seconds too.   DateFormat.format(delegate, d.getTime());
        delegate = "hh:mm aaa";

        if (getArguments() != null) {
            currentBulbId = getArguments().getInt(ARG_PARAM1);
        }

        //get current bulb
        currentBulb = bridge.getResourceCache().getAllLights().get(currentBulbId);
        getCurrentTimers();
        countDownTimers = new ArrayList<>();
    }

    // this function get list of schedule from the bridge, and return schedules that are timer schedule and for this bulb
    private void getCurrentTimers() {
        currentTimers = new ArrayList<PHSchedule>();

        List<PHSchedule> allTimers = bridge.getResourceCache().getAllTimers(false);

        // get schedules from Bridge those are for this bulb

        String currentBulbIdentity = currentBulb.getIdentifier();

        for (int i=0; i< allTimers.size();i++)
        {
            PHSchedule schedule = allTimers.get(i);
            if(schedule.getIdentifier() != null && schedule.getLightIdentifier().equals(currentBulbIdentity))
            {
                currentTimers.add(allTimers.get(i));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

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

        return view;
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

            // When individual timer is clicked, open a time picker to change the timer
            timeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePickerDialog(position);
                }
            });

            final TextView deleteTextView = (TextView) currentView.findViewById(R.id.TimerDeleteText);

            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteTimer(position);
                }
            });

            Date startTimer = timer.getCreated();
            startTimer.setTime(startTimer.getTime()+(long)(timer.getTimer()*1000)); //add duration of the timer to current time.
            Date currentTime = new Date();
            long timeLeft = startTimer.getTime() - currentTime.getTime(); // calculate the difference between current time and timer time



            CountDownTimer countDownTimer = new CountDownTimer(timeLeft,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //changer time left text
                    String timerString = getTimerString((int)millisUntilFinished/1000);
                    timeView.setText(timerString);
                }

                @Override
                public void onFinish() {
                    // clean currentTimers and countDownTimers and refresh the adapter
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            getCurrentTimers();
                            refreshCountDown();
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };

            countDownTimer.start();

            countDownTimers.add(position,countDownTimer);

            return currentView;
        }
    }

    private void refreshCountDown() {
        for (int i = 0; i < countDownTimers.size();i++)
        {
            countDownTimers.get(i).cancel();
        }
        countDownTimers.clear();
        countDownTimers = new ArrayList<CountDownTimer>(); //TODO: do I need this?
    }

    private String getTimerString(int timerTime) {
        int hours = timerTime / 3600;
        int minutes = (timerTime % 3600) / 60;
        int seconds = timerTime % 60;

        String timerString = hours+":"+minutes+":"+seconds;

        return timerString;
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

//            if (chosenTimerPosition == -1) {
//                // Use the current time as the default values for the picker
//
                hour = 0;
                minute = 0;

//            }
//            else{
//                Date date = currentTimers.get(chosenTimerPosition).getDate();
//                hour = date.getHours();
//                minute = date.getMinutes();
//            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    true);
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {

            if (view.isShown()) {

                int time = hour * 3600 + minute * 60;

                // user want to add new Timer
                if (chosenTimerPosition == -1)
                {
                    addNewTimer(time);
                }
                // user wants to change existing Timer
                else
                {
                    updateTimer(chosenTimerPosition, time);
                }

            }

            adapter.notifyDataSetChanged();
        }
    }

    //This function gets called when user want to add/change timer, and this opens the timepicker.
    public void showTimePickerDialog(int timerPosition) {
        chosenTimerPosition = timerPosition;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    private void updateTimer(int timerPosition, int time) {

        PHSchedule schedule = currentTimers.get(timerPosition);
        schedule.setTimer(time);

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
                            PHWizardAlertDialog.showResultDialog(getActivity(), getString(R.string.txt_timer_updated), R.string.btn_ok, R.string.txt_result);
                        }
                        getCurrentTimers();
                        refreshCountDown();
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

    private void addNewTimer(int time) {
        String scheduleName = ""+ time;
        PHSchedule schedule = new PHSchedule(scheduleName);
        schedule.setTimer(time);
        schedule.setLightIdentifier(currentBulb.getIdentifier());
        schedule.setLightState(getLightState());
        schedule.setDescription("Timer");
        Date startTime = new Date();
//        schedule.setStartTime(startTime);
        schedule.setCreated(startTime);

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
                            PHWizardAlertDialog.showResultDialog(getActivity(), getString(R.string.txt_timer_created), R.string.btn_ok, R.string.txt_result);
                        }
                        getCurrentTimers();
                        refreshCountDown();
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

    private void deleteTimer(int position) {
        String scheduleID = currentTimers.get(position).getIdentifier();

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
                            PHWizardAlertDialog.showResultDialog(getActivity(), getString(R.string.txt_timer_deleted), R.string.btn_ok, R.string.txt_result);
                        }
                        getCurrentTimers();
                        refreshCountDown();
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
        state.setOn(true);
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