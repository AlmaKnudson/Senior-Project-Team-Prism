package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.TextView;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class AdvancedSettingFragment extends Fragment implements CacheUpdateListener{
    private static final String DEBUG_TAG = "AdvancedSettingFragment";


    private PHHueSDK hueBridgeSdk;
    private PHBridge bridge;
    private String identifier; // ID of the chosen Light
    private Boolean isGroup;

    private TextView alarmText;
    private TextView timerText;
    private TextView scheduleText;
    private TextView sunsetTimeText;
    private TextView sunriseTimeText;

    private TextView beaconsText;
    private ToggleButton sunriseSwitch;
    private ToggleButton sunsetSwitch;
    ArrayList<PHSchedule> sunsetSchedules;
    ArrayList<PHSchedule> sunriseSchedules;
    private String name; //TODO: Show this name
    private Dialog progressDialog;

    public AdvancedSettingFragment() {
        name ="";
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            identifier = getArguments().getString(RealHomeFragment.lightPositionString);
            isGroup = getArguments().getBoolean(RealHomeFragment.groupOrLightString);
        }
        hueBridgeSdk = PHHueSDK.getInstance();
        bridge = hueBridgeSdk.getSelectedBridge();
        if(isGroup) {
            PHGroup group = hueBridgeSdk.getSelectedBridge().getResourceCache().getGroups().get(identifier);
            if(group == null) {
                getFragmentManager().popBackStack();
                return;
            } else {
                name = group.getName();
            }
        }
        else {
            PHLight light = hueBridgeSdk.getSelectedBridge().getResourceCache().getLights().get(identifier);
            if(light == null) {
                getFragmentManager().popBackStack();
                return;
            } else {
                name = light.getName();
            }
        }

        getSunSchedules();
    }

    private void getSunSchedules() {
        List<PHSchedule> schedules = bridge.getResourceCache().getAllSchedules(true);
        sunsetSchedules = new ArrayList<>();
        sunriseSchedules = new ArrayList<>();

        for (int i = 0; i < schedules.size(); i++) {
            PHSchedule schedule = schedules.get(i);
            if(isGroup){
                if ( schedule.getGroupIdentifier()!=null
                        && schedule.getGroupIdentifier().equals(identifier)
                        && schedule.getDescription().equals("prism,smartsun,2")) {
                    sunsetSchedules.add(schedule);
                } else if (schedule.getGroupIdentifier()!= null
                        && schedule.getGroupIdentifier().equals(identifier)
                        && schedule.getDescription().equals("prism,smartsun,1")) {
                    sunriseSchedules.add(schedule);
                }
            }else {
                if ( schedule.getLightIdentifier()!=null
                        && schedule.getLightIdentifier().equals(identifier)
                        && schedule.getDescription().equals("prism,smartsun,2") ) {
                    sunsetSchedules.add(schedule);
                } else if (schedule.getLightIdentifier()!=null
                        && schedule.getLightIdentifier().equals(identifier)
                        && schedule.getDescription().equals("prism,smartsun,1") ) {
                    sunriseSchedules.add(schedule);
                }
            }
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

        beaconsText = (TextView)view.findViewById(R.id.beaconsText);
        sunriseSwitch = (ToggleButton)view.findViewById(R.id.SunriseSwitch);
        sunsetSwitch = (ToggleButton)view.findViewById(R.id.SunsetSwitch);
        sunriseTimeText = (TextView)view.findViewById(R.id.sunriseTime);
        sunsetTimeText = (TextView)view.findViewById(R.id.sunsetTime);


        alarmText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(RealHomeFragment.lightPositionString, identifier);
                bundle.putBoolean(RealHomeFragment.groupOrLightString, isGroup);

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
                bundle.putString(RealHomeFragment.lightPositionString, identifier);
                bundle.putBoolean(RealHomeFragment.groupOrLightString, isGroup);

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
                bundle.putString(RealHomeFragment.lightPositionString, identifier);
                bundle.putBoolean(RealHomeFragment.groupOrLightString, isGroup);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ScheduleFragment scheduleFragment = new ScheduleFragment();
                scheduleFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, scheduleFragment);
                fragmentTransaction.addToBackStack("ScheduleFragment");
                fragmentTransaction.commit();
            }
        });


        beaconsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(RealHomeFragment.lightPositionString, identifier);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                BeaconFragment beaconFragment = new BeaconFragment();
                System.out.println();
                beaconFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, beaconFragment);
                fragmentTransaction.addToBackStack("BeaconFragment");
                fragmentTransaction.commit();
            }
        });

        updateSunScheduleToggles();

        sunriseSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if there is recurring schedule for sunrise if there is, enable it, or create it.
                if (((ToggleButton) v).isChecked()) {
                    if (sunriseSchedules.size() == 0) {
                        createSunSchedule(identifier, 1);
                    } else if (sunriseSchedules.size() == 1) {
                        sunriseSchedules.get(0).setStatus(PHSchedule.PHScheduleStatus.ENABLED);
                        updateSchedule(sunriseSchedules.get(0), 1);
                    }
                } else {
                    // if there is recurring schedule for sunrise, disable it.
                    if (sunriseSchedules.size() == 1) {
                        sunriseSchedules.get(0).setStatus(PHSchedule.PHScheduleStatus.DISABLED);
                        updateSchedule(sunriseSchedules.get(0), 1);
                    }
                }
                getSunSchedules();
                updateSunScheduleToggles();
            }
        });

        sunsetSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked()) {
                    if (sunsetSchedules.size() == 0) {
                        createSunSchedule(identifier, 2);
                    } else if (sunsetSchedules.size() == 1) {
                        sunsetSchedules.get(0).setStatus(PHSchedule.PHScheduleStatus.ENABLED);
                        updateSchedule(sunsetSchedules.get(0), 2);
                    }
                } else {
                    // if there is recurring schedule for sunset, disable it.
                    if (sunsetSchedules.size() == 1) {
                        sunsetSchedules.get(0).setStatus(PHSchedule.PHScheduleStatus.DISABLED);
                        updateSchedule(sunsetSchedules.get(0), 2);
                    }
                }
                getSunSchedules();
                updateSunScheduleToggles();
            }
        });

        return view;

    }

    private void updateSunScheduleToggles() {
        if (sunriseSchedules.size() == 0) {
            sunriseSwitch.setChecked(false);
        } else if (sunriseSchedules.size() == 1){
            PHSchedule.PHScheduleStatus status = sunriseSchedules.get(0).getStatus();
            if (status==null || status.equals(PHSchedule.PHScheduleStatus.ENABLED)) {
                sunriseSwitch.setChecked(true);
                sunriseTimeText.setText("On: "+getTimeString(sunriseSchedules.get(0).getDate()));
            }
            else {
                sunriseSwitch.setChecked(false);
                sunriseTimeText.setText("");
            }
        } else
            Log.e(DEBUG_TAG, "There were more than 1 sunrise schedule for bulb" + identifier);

        if (sunsetSchedules.size() == 0) {
            sunsetSwitch.setChecked(false);
        } else if (sunsetSchedules.size() == 1){
            PHSchedule.PHScheduleStatus status = sunsetSchedules.get(0).getStatus();
            if (status==null || status.equals(PHSchedule.PHScheduleStatus.ENABLED)) {
                sunsetSwitch.setChecked(true);
                sunsetTimeText.setText("Off: "+getTimeString(sunsetSchedules.get(0).getDate()));
            }
            else {
                sunsetSwitch.setChecked(false);
                sunsetTimeText.setText("");
            }
        } else
            Log.e(DEBUG_TAG, "There were more than 1 sunset schedule for bulb" + identifier);
    }

    private String getTimeString(Date date) {
        return (String) DateFormat.format("hh:mm aaa", date);
    }

    private void updateSchedule(final PHSchedule phSchedule, int name) {
        phSchedule.setAutoDelete(true);

        if(phSchedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED)) {
            if (name==1)
                phSchedule.setDate(getSunriseTime());
            else
                phSchedule.setDate(getSunsetTime());
        }

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
                    }
                });
            }

            @Override
            public void onError(int i, final String s) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (phSchedule.getDescription().equals("prism,smartsun,1")) {
                            if (phSchedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED))
                                sunriseSwitch.setChecked(false);
                            else
                                sunriseSwitch.setChecked(true);

                        } else {
                            if (phSchedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED))
                                sunsetSwitch.setChecked(false);
                            else
                                sunsetSwitch.setChecked(true);
                        }
                        closeProgressDialog();
                        DialogCreator.showWarningDialog("Error", getText(R.string.failed_update_schedule).toString(), (MainActivity)getActivity());
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    private void createSunSchedule(String identifier, int name) {

        PHSchedule currentSchedule;
        if (name==1) {
            currentSchedule = new PHSchedule("Sunrise");
            currentSchedule.setDate(getSunriseTime());

        }
        else {
            currentSchedule = new PHSchedule("Sunset");
            currentSchedule.setDate(getSunsetTime());
        }
        currentSchedule.setRecurringDays(127); //127 is for everyday
        PHLightState state = new PHLightState();
        if (name==1)
            state.setOn(false);
        else
            state.setOn(true);
        currentSchedule.setLightState(state);
        currentSchedule.setDescription("prism,smartsun,"+ name);
        if (isGroup)
            currentSchedule.setGroupIdentifier(identifier);
        else
            currentSchedule.setLightIdentifier(identifier);

        currentSchedule.setAutoDelete(true);

        progressDialog = DialogCreator.showLoadingDialog(getText(R.string.sending_progress).toString(), (MainActivity)getActivity());

        bridge.createSchedule(currentSchedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                    }
                });
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int i, final String s) {

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        closeProgressDialog();
                        DialogCreator.showWarningDialog("Error", getText(R.string.failed_create_schedule).toString(), (MainActivity)getActivity());
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {
            }
        });
    }

    private Date getSunriseTime() {
        return ((MainActivity)getActivity()).getSunrise();
    }
    private Date getSunsetTime() {
        return ((MainActivity)getActivity()).getSunset();
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

    @Override
    public void onResume() {
        super.onResume();
        if(isGroup) {
            PHGroup group = hueBridgeSdk.getSelectedBridge().getResourceCache().getGroups().get(identifier);
            if(group == null) {
                getFragmentManager().popBackStack();
                return;
            } else {
                name = group.getName();
            }
        }
        else {
            PHLight light = hueBridgeSdk.getSelectedBridge().getResourceCache().getLights().get(identifier);
            if(light == null) {
                getFragmentManager().popBackStack();
                return;
            } else {
                name = light.getName();
            }
        }
        getSunSchedules();
        updateSunScheduleToggles();
    }

    @Override
    public void cacheUpdated() {
        if(isGroup) {
            PHGroup group = hueBridgeSdk.getSelectedBridge().getResourceCache().getGroups().get(identifier);
            if(group == null) {
                getFragmentManager().popBackStack();
                return;
            } else {
                name = group.getName();
            }
        }
        else {
            PHLight light = hueBridgeSdk.getSelectedBridge().getResourceCache().getLights().get(identifier);
            if(light == null) {
                getFragmentManager().popBackStack();
                return;
            } else {
                name = light.getName();
            }
        }

        getSunSchedules();
        updateSunScheduleToggles();
    }
}
