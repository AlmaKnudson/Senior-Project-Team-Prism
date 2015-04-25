package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.FragmentTransaction;
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
import android.widget.ToggleButton;
import android.widget.TextView;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
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
    private ListView scheduleListView;
    static ScheduleAdapter adapter;
    static private PHHueSDK phHueSDK;
    private static PHBridge bridge;
    String delegate;
    ScheduleList scheduleList;
    private Dialog progressDialog;

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
    }

    // this function get list of schedule from the bridge, and return schedules for this bulb
    private void getPhSchedules() {
        List<PHSchedule> phSchedules = new ArrayList<>();

        List<PHSchedule> recurringSchedules = bridge.getResourceCache().getAllSchedules(true);

        // get recurring schedules from Bridge that are for this bulb

        for (int i=0; i<recurringSchedules.size();i++)
        {
            PHSchedule schedule = recurringSchedules.get(i);
            if(isGroup){
                if (schedule.getGroupIdentifier() != null && schedule.getGroupIdentifier().equals(identifier)
                        && !schedule.getDescription().startsWith("prism,smartsun")
                        && schedule.getDescription().startsWith("prism")) {
                    phSchedules.add(recurringSchedules.get(i));
                }
            }else {
                if (schedule.getLightIdentifier() != null && schedule.getLightIdentifier().equals(identifier)
                        && !schedule.getDescription().startsWith("prism,smartsun")
                        && schedule.getDescription().startsWith("prism")) {
                    phSchedules.add(recurringSchedules.get(i));
                }
            }
        }

        scheduleList = new ScheduleList(phSchedules);
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
                ((MainActivity) getActivity()).setCurrentSchedule(scheduleList.getSchedule(position));

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
            return scheduleList.getSize();
        }

        @Override
        public Object getItem(int position) {
            return scheduleList.getSchedule(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {

            View currentView;
            if(convertView == null) {
                currentView = LayoutInflater.from(ScheduleFragment.this.getActivity()).inflate(R.layout.single_schedule_new, parent, false);
            } else {
                currentView = convertView;
            }

            final Schedule schedule = scheduleList.getSchedule(position);

            TextView nameText = (TextView) currentView.findViewById(R.id.nameText);
            nameText.setText("Name: " + schedule.getName());


            TextView OnTimeText = (TextView) currentView.findViewById(R.id.onTimeText);

            int timeChoiceOn = schedule.getTimeChoiceOn();
            if(timeChoiceOn==0){
                Date onTime = schedule.getOnTime();
                String onTimeString = (String) DateFormat.format(delegate, onTime.getTime());
                OnTimeText.setText("On: "+ onTimeString);
            }
            else if (timeChoiceOn==1)
                OnTimeText.setText("On: Sunrise");
            else if (timeChoiceOn==2)
                OnTimeText.setText("On: Sunset");
            else
                OnTimeText.setText("On: None");


            TextView OffTimeText = (TextView) currentView.findViewById(R.id.offTimeText);

            int timeChoiceOff = schedule.getTimeChoiceOff();
            if(timeChoiceOff==0) {
                Date offTime = schedule.getOffTime();
                String offTimeString = (String) DateFormat.format(delegate, offTime.getTime());
                OffTimeText.setText("Off: " + offTimeString);
            }
            else if (timeChoiceOff==1)
                OffTimeText.setText("Off: Sunrise");
            else if (timeChoiceOff==2)
                OffTimeText.setText("Off: Sunset");
            else
                OffTimeText.setText("Off: None");

            PHLightState state = schedule.getLightState();

            String brightness;
            if (state.getBrightness() == null)
                brightness = "Brightness: None";
            else
                brightness = "Brightness: " + HueBulbChangeUtility.revertBrightness(state.getBrightness()) + "%";

            TextView brightnessText = (TextView) currentView.findViewById(R.id.brightnessText);
            brightnessText.setText(brightness);

            int color;
            if (state.getX() == null && state.getY() == null)
                color = 0xFFFFFF;
            else
                color = PHUtilities.colorFromXY(new float[]{state.getX(), state.getY()}, "");

            View colorImage =  currentView.findViewById(R.id.colorView);
            colorImage.setBackgroundColor(color);


            final ToggleButton scheduleSwitch = (ToggleButton)currentView.findViewById(R.id.toggleButton);
            if(schedule.getStatus()==null || schedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED)) // when a new schedule is created, bridge returns null for status
                scheduleSwitch.setChecked(true);
            else
                scheduleSwitch.setChecked(false);

            scheduleSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (schedule.getScheduleOn()!=null){
                        if (((ToggleButton)v).isChecked()) {
                            schedule.getScheduleOn().setStatus(PHSchedule.PHScheduleStatus.ENABLED);
                        } else {
                            schedule.getScheduleOn().setStatus(PHSchedule.PHScheduleStatus.DISABLED);
                        }
                        updateSchedule(schedule.getScheduleOn());
                    }

                    if (schedule.getScheduleOn()!=null){
                        if (((ToggleButton)v).isChecked()) {
                            schedule.getScheduleOff().setStatus(PHSchedule.PHScheduleStatus.ENABLED);
                        } else {
                            schedule.getScheduleOff().setStatus(PHSchedule.PHScheduleStatus.DISABLED);
                        }
                        updateSchedule(schedule.getScheduleOff());
                    }
                }
            });




            TextView recurringDayText = (TextView) currentView.findViewById(R.id.daysText);

            String recurringDaysBitStr = String.format("%07d", new BigInteger(
                    Integer.toBinaryString(schedule.getRecurringDays())));

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

            recurringDayText.setText("Days: " + recurringDays);

            ImageView deleteTextView = (ImageView) currentView.findViewById(R.id.deleteScheduleImageButton);


            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(schedule.getScheduleOn()!=null)
                        deleteSchedule(schedule.getScheduleOn());
                    if(schedule.getScheduleOff()!=null)
                        deleteSchedule(schedule.getScheduleOff());
                }
            });

            return currentView;
        }
    }

    private void updateSchedule(final PHSchedule phSchedule) {
        phSchedule.setAutoDelete(true);
        progressDialog = DialogCreator.showLoadingDialog(getText(R.string.sending_progress).toString(), (MainActivity)getActivity());


        bridge.updateSchedule(phSchedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {

            }

            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        closeProgressDialog();
                        getPhSchedules();
                        adapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onError(int i, final String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        DialogCreator.showWarningDialog("Error", s, (MainActivity) getActivity());
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    private void deleteSchedule(PHSchedule schedule) {
        String scheduleID;
        scheduleID = schedule.getIdentifier();

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
                        getPhSchedules();
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(int i, final String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        DialogCreator.showWarningDialog("Error", s, (MainActivity)getActivity());
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPhSchedules();
        adapter.notifyDataSetChanged();
    }

    private void closeProgressDialog() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    @Override
    public void onDetach() {
        closeProgressDialog();
        super.onDetach();
    }
}
