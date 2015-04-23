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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ScheduleFragment extends Fragment implements CacheUpdateListener {

    private String identifier;
    private boolean isGroup;
    private int chosenSchedulePosition;
    private ListView scheduleListView;
    static ScheduleAdapter adapter;
    static private PHHueSDK phHueSDK;
    private static PHBridge bridge;
    String delegate;
    List<PHSchedule> phSchedules; // this List of schedules in bridge whose description is Schedule.
    ToggleButton currentSwitch;

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
            identifier = getArguments().getString(RealHomeFragment.lightPositionString);
            isGroup = getArguments().getBoolean(RealHomeFragment.groupOrLightString);
        }

        getPhSchedules();

        currentSwitch = null;
    }

    // this function get list of schedule from the bridge, and return schedules for this bulb
    private void getPhSchedules() {
        phSchedules = new ArrayList<PHSchedule>();

        List<PHSchedule> recurringSchedules = bridge.getResourceCache().getAllSchedules(true);

        // get recurring schedules from Bridge that are for this bulb

        for (int i=0; i<recurringSchedules.size();i++)
        {
            PHSchedule schedule = recurringSchedules.get(i);
            if(isGroup){
                if (schedule.getGroupIdentifier() != null && schedule.getGroupIdentifier().equals(identifier)
                        && schedule.getDescription().startsWith("prism")) {
                    phSchedules.add(recurringSchedules.get(i));
                }
            }else {
                if (schedule.getLightIdentifier() != null && schedule.getLightIdentifier().equals(identifier)
                        && schedule.getDescription().startsWith("prism")) {
                    phSchedules.add(recurringSchedules.get(i));
                }
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
                bundle.putString(RealHomeFragment.lightPositionString, identifier);
                bundle.putBoolean(RealHomeFragment.groupOrLightString, isGroup);
                ((MainActivity) getActivity()).setCurrentSchedule(null);

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
                bundle.putString(RealHomeFragment.lightPositionString, identifier);
                bundle.putBoolean(RealHomeFragment.groupOrLightString, isGroup);
                ((MainActivity) getActivity()).setCurrentSchedule(phSchedules.get(position));

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

    @Override
    public void cacheUpdated() {
        getPhSchedules();
        adapter.notifyDataSetChanged();
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
            View currentView;
            if(convertView == null) {
                currentView = LayoutInflater.from(ScheduleFragment.this.getActivity()).inflate(R.layout.single_schedule, parent, false);
            } else {
                currentView = convertView;
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
                brightness = HueBulbChangeUtility.revertBrightness(state.getBrightness()) + "%";

            TextView brightnessText = (TextView) currentView.findViewById(R.id.scheduleBrightness);
            brightnessText.setText(brightness);

            int color;
            if (state.getX() == null && state.getY() == null)
                color = 0xFFFFFF;
            else
                color = PHUtilities.colorFromXY(new float[]{state.getX(), state.getY()}, "");

            View colorImage =  currentView.findViewById(R.id.scheduleColor);
            colorImage.setBackgroundColor(color);


            final ToggleButton scheduleSwitch = (ToggleButton)currentView.findViewById(R.id.singleScheduleSwitch);
            if(phSchedule.getStatus()==null || phSchedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED)) // when a new schedule is created, bridge returns null for status
                scheduleSwitch.setChecked(true);
            else
                scheduleSwitch.setChecked(false);

            scheduleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        phSchedule.setStatus(PHSchedule.PHScheduleStatus.ENABLED);
                    } else {
                        phSchedule.setStatus(PHSchedule.PHScheduleStatus.DISABLED);
                    }
                    currentSwitch = scheduleSwitch;
                    updateSchedule(phSchedule);
                }
            });

            TextView nameText = (TextView) currentView.findViewById(R.id.scheduleName);
            nameText.setText(phSchedule.getName());

            TextView recurringDayText = (TextView) currentView.findViewById(R.id.scheduleRecurringDay);

            String recurringDaysBitStr = String.format("%07d", new BigInteger(
                    Integer.toBinaryString(phSchedule.getRecurringDays())));

            String recurringDays = "";

            for (int i = 0; i < recurringDaysBitStr.length(); i++) {
                switch (i) {
                    case 0:
                        if (recurringDaysBitStr.charAt(0) == '1') {

                            recurringDays = recurringDays + "Mon";
                        }

                        break;
                    case 1:
                        if (recurringDaysBitStr.charAt(1) == '1') {
                            if (recurringDays != "")
                                recurringDays = recurringDays+ ", ";

                            recurringDays = recurringDays + "Tue";
                        }
                        break;
                    case 2:
                        if (recurringDaysBitStr.charAt(2) == '1') {
                            if (recurringDays != "")
                                recurringDays = recurringDays+ ", ";

                            recurringDays = recurringDays + "Wed";
                        }
                        break;
                    case 3:
                        if (recurringDaysBitStr.charAt(3) == '1') {
                            if (recurringDays != "")
                                recurringDays = recurringDays+ ", ";

                            recurringDays = recurringDays + "Thur";
                        }
                        break;
                    case 4:
                        if (recurringDaysBitStr.charAt(4) == '1') {
                            if (recurringDays != "")
                                recurringDays = recurringDays+ ", ";

                            recurringDays = recurringDays + "Fri";
                        }
                        break;
                    case 5:
                        if (recurringDaysBitStr.charAt(5) == '1') {
                            if (recurringDays != "")
                                recurringDays = recurringDays+ ", ";

                            recurringDays = recurringDays + "Sat";
                        }
                        break;
                    case 6:
                        if (recurringDaysBitStr.charAt(6) == '1') {
                            if (recurringDays != "")
                                recurringDays = recurringDays+ ", ";

                            recurringDays = recurringDays + "Sun";
                        }
                        break;
                    default:
                        break;
                }
            }


            recurringDayText.setText(recurringDays);

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

    private void updateSchedule(final PHSchedule phSchedule) {
        phSchedule.setAutoDelete(true);
        final PHWizardAlertDialog dialogManager = PHWizardAlertDialog.getInstance();
        dialogManager.showProgressDialog(R.string.sending_progress, getActivity());

        bridge.updateSchedule(phSchedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {

            }

            @Override
            public void onSuccess() {
                currentSwitch = null;
                dialogManager.closeProgressDialog();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (isCurrentActivity()) {
                            PHWizardAlertDialog.showResultDialog(getActivity(), getString(R.string.txt_schedule_updated), R.string.btn_ok, R.string.txt_result);
                        }
                    }
                });

            }

            @Override
            public void onError(int i, final String s) {

                if (phSchedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED))
                    currentSwitch.setChecked(false);
                else
                    currentSwitch.setChecked(true);

                currentSwitch = null;

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
                ;
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();
        getPhSchedules();
        adapter.notifyDataSetChanged();
    }
}
