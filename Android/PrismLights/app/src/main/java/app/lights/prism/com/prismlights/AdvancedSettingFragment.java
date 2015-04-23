package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
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
import com.philips.lighting.model.PHHueError;
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

    private TextView beaconsText;
    private ToggleButton sunriseSwitch;
    private ToggleButton sunsetSwitch;
    ArrayList<PHSchedule> sunsetSchedules;
    ArrayList<PHSchedule> sunriseSchedules;
    private String name; //TODO: Show this name


    public AdvancedSettingFragment() {
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
        if(isGroup)
            name = hueBridgeSdk.getSelectedBridge().getResourceCache().getGroups().get(identifier).getName();
        else
            name = hueBridgeSdk.getSelectedBridge().getResourceCache().getLights().get(identifier).getName();

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
                        && schedule.getDescription().equals("prism,sunset")) {
                    sunsetSchedules.add(schedule);
                } else if (schedule.getGroupIdentifier()!= null
                        && schedule.getGroupIdentifier().equals(identifier)
                        && schedule.getDescription().equals("prism,sunrise")) {
                    sunriseSchedules.add(schedule);
                }
            }else {
                if ( schedule.getLightIdentifier()!=null
                        && schedule.getLightIdentifier().equals(identifier)
                        && schedule.getDescription().equals("prism,sunset") ) {
                    sunsetSchedules.add(schedule);
                } else if (schedule.getLightIdentifier()!=null
                        && schedule.getLightIdentifier().equals(identifier)
                        && schedule.getDescription().equals("prism,sunrise") ) {
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
                        createSunSchedule(identifier, "sunrise");
                    } else if (sunriseSchedules.size() == 1) {
                        sunriseSchedules.get(0).setStatus(PHSchedule.PHScheduleStatus.ENABLED);
                        updateSchedule(sunriseSchedules.get(0), "sunrise");
                    }
                } else {
                    // if there is recurring schedule for sunrise, disable it.
                    if (sunriseSchedules.size() == 1) {
                        sunriseSchedules.get(0).setStatus(PHSchedule.PHScheduleStatus.DISABLED);
                        updateSchedule(sunriseSchedules.get(0), "sunrise");
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
                        createSunSchedule(identifier, "sunset");
                    } else if (sunsetSchedules.size() == 1) {
                        sunsetSchedules.get(0).setStatus(PHSchedule.PHScheduleStatus.ENABLED);
                        updateSchedule(sunsetSchedules.get(0), "sunset");
                    }
                } else {
                    // if there is recurring schedule for sunset, disable it.
                    if (sunsetSchedules.size() == 1) {
                        sunsetSchedules.get(0).setStatus(PHSchedule.PHScheduleStatus.DISABLED);
                        updateSchedule(sunsetSchedules.get(0), "sunset");
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
            if (status==null || status.equals(PHSchedule.PHScheduleStatus.ENABLED)) //TODO right after creating new schedule, it returns null value for the status...
                sunriseSwitch.setChecked(true);
            else
                sunriseSwitch.setChecked(false);
        } else
            Log.e(DEBUG_TAG, "There were more than 1 sunrise schedule for bulb" + identifier);

        if (sunsetSchedules.size() == 0) {
            sunsetSwitch.setChecked(false);
        } else if (sunsetSchedules.size() == 1){
            PHSchedule.PHScheduleStatus status = sunsetSchedules.get(0).getStatus();
            if (status==null || status.equals(PHSchedule.PHScheduleStatus.ENABLED))
                sunsetSwitch.setChecked(true);
            else
                sunsetSwitch.setChecked(false);
        } else
            Log.e(DEBUG_TAG, "There were more than 1 sunset schedule for bulb" + identifier);
    }

    private void updateSchedule(final PHSchedule phSchedule, String name) {
        phSchedule.setAutoDelete(true);

        if(phSchedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED)) {
            if (name.equals("sunrise"))
                phSchedule.setDate(getSunriseTime());
            else
                phSchedule.setDate(getSunsetTime());
        }

        final PHWizardAlertDialog dialogManager = PHWizardAlertDialog.getInstance();
        dialogManager.showProgressDialog(R.string.sending_progress, getActivity());

        bridge.updateSchedule(phSchedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {
                ;
            }

            @Override
            public void onSuccess() {
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
                if (phSchedule.getDescription().equals("prism,sunrise")) {
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

    private void createSunSchedule(String identifier, String name) {
        PHSchedule currentSchedule = new PHSchedule(name);
        if (name.equals("sunrise"))
            currentSchedule.setDate(getSunriseTime());
        else
            currentSchedule.setDate(getSunsetTime());
        currentSchedule.setRecurringDays(127); //127 is for everyday
        PHLightState state = new PHLightState();
        if (name.equals("sunrise"))
            state.setOn(false);
        else
            state.setOn(true);
        currentSchedule.setLightState(state);
        currentSchedule.setDescription("prism," + name);
        if (isGroup)
            currentSchedule.setGroupIdentifier(identifier);
        else
            currentSchedule.setLightIdentifier(identifier);

        currentSchedule.setAutoDelete(true);

        final PHWizardAlertDialog dialogManager = PHWizardAlertDialog.getInstance();
        dialogManager.showProgressDialog(R.string.sending_progress, getActivity());

        bridge.createSchedule(currentSchedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {
                dialogManager.closeProgressDialog();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isCurrentActivity()) {
                            PHWizardAlertDialog.showResultDialog(getActivity(), getString(R.string.txt_schedule_created), R.string.btn_ok, R.string.txt_result);
                        }
                    }
                });
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

    private Date getSunriseTime() {
        return ((MainActivity)getActivity()).getSunrise();
    }
    private Date getSunsetTime() {
        return ((MainActivity)getActivity()).getSunset();
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
        getSunSchedules();
        updateSunScheduleToggles();
    }

    @Override
    public void cacheUpdated() {
        getSunSchedules();
        updateSunScheduleToggles();
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
