package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Context;
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
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment {
    private static final String ARG_PARAM1 = "CURRENT_BULB_ID";
    private static final String ARG_PARAM2 = "CURRENT_SCHEDULE";

    private int currentBulbId; // The chosen Light BULB ID
    private int chosenSchedulePosition;
    private PHLight currentBulb;
    private ListView scheduleListView;
    static ScheduleAdapter adapter;
    static private PHHueSDK phHueSDK;
    private static PHBridge bridge;
    String delegate;
    List<PHSchedule> phSchedules; // this List of schedules in bridge whose description is Schedule.

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScheduleFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phHueSDK = PHHueSDK.getInstance();
        bridge = phHueSDK.getSelectedBridge();

        delegate = "hh:mm aaa";

        if (getArguments() != null) {
            currentBulbId = getArguments().getInt(ARG_PARAM1);
        }

        //get current bulb
        currentBulb = bridge.getResourceCache().getAllLights().get(currentBulbId);
        getPhSchedules();
    }

    // this function get list of schedule from the bridge, and return schedules for this bulb
    private void getPhSchedules() {
        phSchedules = new ArrayList<PHSchedule>();

        List<PHSchedule> recurringSchedules = bridge.getResourceCache().getAllSchedules(true);

        // get recurring schedules from Bridge that are for this bulb

        String currentBulbIdentity = currentBulb.getIdentifier();

        for (int i=0; i<recurringSchedules.size();i++)
        {
            PHSchedule schedule = recurringSchedules.get(i);
            if(schedule.getLightIdentifier().equals(currentBulbIdentity))
            {
                phSchedules.add(recurringSchedules.get(i));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        ImageView addButton = (ImageView)view.findViewById(R.id.schedulePlusButton);

        // When + image is click, open a time picker to create new schedule.
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //need to open new fragment with BulbID argument.
                Bundle bundle = new Bundle();
                bundle.putInt(ARG_PARAM1, currentBulbId);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ScheduleConfigFragment scheduleConfigFragment = new ScheduleConfigFragment();
                scheduleConfigFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, scheduleConfigFragment);
                fragmentTransaction.addToBackStack("ScheduleConfigFragment");
                fragmentTransaction.commit();
            }
        });

        scheduleListView = (ListView)view.findViewById(R.id.scheduleListView);

        adapter = new ScheduleAdapter();
        scheduleListView.setAdapter(adapter);
        scheduleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //need to open new fragment with BulbID and scheduleID arguments.
                Bundle bundle = new Bundle();
                bundle.putInt(ARG_PARAM1, currentBulbId);
                bundle.putSerializable(ARG_PARAM2, (Serializable)phSchedules.get(position));

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ScheduleConfigFragment scheduleConfigFragment = new ScheduleConfigFragment();
                scheduleConfigFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, scheduleConfigFragment);
                fragmentTransaction.addToBackStack("ScheduleConfigFragment");
                fragmentTransaction.commit();
            }
        });

        return view;
    }


    private class ScheduleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return phSchedules.size();
        }

        @Override
        public Object getItem(int position) {
            return phSchedules.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            final PHSchedule phSchedule = phSchedules.get(position);

            Date d = phSchedule.getDate();

            String timeString = (String) DateFormat.format(delegate, d.getTime());
            RelativeLayout currentView;
            if(convertView == null) {
                currentView = (RelativeLayout) LayoutInflater.from(ScheduleFragment.this.getActivity()).inflate(R.layout.single_schedule, parent, false);
            } else {
                currentView = (RelativeLayout) convertView;
            }

            TextView timeText = (TextView) currentView.findViewById(R.id.scheduleTime);
            timeText.setText(timeString);

            PHLightState state = phSchedule.getLightState();

            String isOnText;
            if (state.isOn())
                isOnText = "On";
            else
                isOnText = "Off";

            TextView onOffText = (TextView) currentView.findViewById(R.id.scheduleOnOff);
            onOffText.setText(isOnText);

            String brightness;
            if (state.getBrightness() == null)
                brightness = "";
            else
                brightness = state.getBrightness() + "";

            TextView brightnessText = (TextView) currentView.findViewById(R.id.scheduleBrightness);
            brightnessText.setText(brightness);

            int color;
            if (state.getX() == null && state.getY() == null)
                color = 0xFFFFFF;
            else
                color = PHUtilities.colorFromXY(new float[]{state.getX(), state.getY()}, "");

            ImageView colorImage = (ImageView) currentView.findViewById(R.id.scheduleColor);
            colorImage.setBackgroundColor(color);

            TextView nameText = (TextView) currentView.findViewById(R.id.scheduleName);
            nameText.setText(phSchedule.getName());

            //TODO: I need to get correct recurring day string.
            TextView recurringDayText = (TextView) currentView.findViewById(R.id.scheduleRecurringDay);
            recurringDayText.setText(phSchedule.getRecurringDays()+"");

            TextView deleteTextView = (TextView) currentView.findViewById(R.id.scheduleDelete);


            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteSchedule(position);
                }
            });

            return currentView;
        }
    }

    private void deleteSchedule(int position) {
        String scheduleID = phSchedules.get(position).getIdentifier();

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
                            PHWizardAlertDialog.showResultDialog(getActivity(), getString(R.string.txt_schedule_deleted), R.string.btn_ok, R.string.txt_result);
                        }
                        getPhSchedules();
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

//    // this is TimePickerFragment, showTimePickerDialog create this DialogFragment.
//    // when user click "done", onTimeSet get called.
//    // TODO: is this ok non static? public class in a class.... private-> fragment have to be public,...?
//    private class TimePickerFragment extends DialogFragment
//            implements TimePickerDialog.OnTimeSetListener {
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            int hour;
//            int minute;
//            if (chosenSchedulePosition == -1) {
//                // Use the current time as the default values for the picker
//                final Calendar c = Calendar.getInstance();
//                hour = c.get(Calendar.HOUR_OF_DAY);
//                minute = c.get(Calendar.MINUTE);
//            }
//            else{
//                Date date = phSchedules.get(chosenSchedulePosition).getDate();
//                hour = date.getHours();
//                minute = date.getMinutes();
//            }
//
//            // Create a new instance of TimePickerDialog and return it
//            return new TimePickerDialog(getActivity(), this, hour, minute,
//                    DateFormat.is24HourFormat(getActivity()));
//        }
//
//        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//
//            if (view.isShown()) {
//
//                Date scheduleTime = new Date();
//                scheduleTime.setHours(hourOfDay);
//                scheduleTime.setMinutes(minute);
//
//                Date currentTime = new Date();
//                if(scheduleTime.getTime() < currentTime.getTime()) {
//                    scheduleTime = new Date(scheduleTime.getTime()+86400000); //adding 24 hours in milliseconds
//                }
//
//
//                // user want to add new Schedule
//                if (chosenSchedulePosition == -1)
//                {
//                    addNewSchedule(scheduleTime);
//                }
//                // user wants to change existing schedule
//                else
//                {
//                    updateSchedule(chosenSchedulePosition, scheduleTime);
//                }
//            }
//
//            adapter.notifyDataSetChanged();
//        }
//    }

//    //This function gets called when user want to add/change schedule, and this opens the timepicker.
//    public void showTimePickerDialog(int schedulePosition) {
//        chosenSchedulePosition = schedulePosition;
//        DialogFragment newFragment = new TimePickerFragment();
//        newFragment.show(getFragmentManager(), "timePicker");
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
//        View emptyView = scheduleListView.getEmptyView();
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
