package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ScheduleConfigFragment extends Fragment {

    private String identifier;
    private boolean isGroup;
    static private PHHueSDK phHueSDK;
    private static PHBridge bridge;

    private PHSchedule currentSchedule;
    private EditText nameEditor;
    private ToggleButton bulbOnState;
    private SeekBar brightness;
    private TextView brightnessPercentage;
    private int currentColor;
    private ColorPickerViewGroup colorPicker;
    private Button saveButton;

    private ToggleButton btnSun;
    private ToggleButton btnMon;
    private ToggleButton btnTue;
    private ToggleButton btnWed;
    private ToggleButton btnThur;
    private ToggleButton btnFri;
    private ToggleButton btnSat;

    private Button btnScheduleTime;
    private int mHour;
    private int mMinute;
    private static Date timeToSend;

    private static int recurringDays;
    private static String recurringDaysBitStr;



    //TODO: validate name length, show error message when update or create schedule fails.

    private Dialog progressDialog;


//    private OnFragmentInteractionListener mListener;

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment ScheduleConfigFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static ScheduleConfigFragment newInstance(String param1, String param2) {
//        ScheduleConfigFragment fragment = new ScheduleConfigFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    public ScheduleConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phHueSDK = PHHueSDK.getInstance();
        bridge = phHueSDK.getSelectedBridge();

        currentSchedule = ((MainActivity)getActivity()).getCurrentSchedule();
        ((MainActivity)getActivity()).setCurrentSchedule(null);

        if (getArguments() != null) {
            identifier = getArguments().getString(RealHomeFragment.lightPositionString);
            isGroup = getArguments().getBoolean(RealHomeFragment.groupOrLightString);
        }

        recurringDays = 0; //default value
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_schedule_config, container, false);
        nameEditor = (EditText) frame.findViewById(R.id.nameEditor);
        bulbOnState = (ToggleButton) frame.findViewById(R.id.bulbOnState);
        brightness = (SeekBar) frame.findViewById(R.id.brightness);
        brightnessPercentage = (TextView) frame.findViewById(R.id.brightnessLabel);
        colorPicker = (ColorPickerViewGroup) frame.findViewById(R.id.scheduleColorPicker);

        saveButton = (Button)frame.findViewById(R.id.buttonSave);

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessPercentage.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        colorPicker.setColorChangedListener(new ColorChangedListener() {
            @Override
            public void onColorChanged(float[] newColor) {
                currentColor = PHUtilities.colorFromXY(newColor, HueBulbChangeUtility.COLOR_XY_MODEL_FOR_HUE);
            }
        });

        btnSun = (ToggleButton) frame.findViewById(R.id.btnSun);
        btnMon = (ToggleButton) frame.findViewById(R.id.btnMon);
        btnTue = (ToggleButton) frame.findViewById(R.id.btnTue);
        btnWed = (ToggleButton) frame.findViewById(R.id.btnWed);
        btnThur = (ToggleButton) frame.findViewById(R.id.btnThur);
        btnFri = (ToggleButton) frame.findViewById(R.id.btnFri);
        btnSat = (ToggleButton) frame.findViewById(R.id.btnSat);

        btnSun.setOnClickListener(recurringHandler);
        btnMon.setOnClickListener(recurringHandler);
        btnTue.setOnClickListener(recurringHandler);
        btnWed.setOnClickListener(recurringHandler);
        btnThur.setOnClickListener(recurringHandler);
        btnFri.setOnClickListener(recurringHandler);
        btnSat.setOnClickListener(recurringHandler);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSchedule != null)
                    updateSchedule();
                else
                    addNewSchedule();

            }
        });

        btnScheduleTime = (Button) frame.findViewById(R.id.btnTimerTime);

        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        btnScheduleTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TimePickerDialog timePicker = new TimePickerDialog(
                        getActivity(),
                        mTimeSetListener, mHour, mMinute, false);

                timePicker.show();
            }
        });

        btnSun.setChecked(false);
        btnSat.setChecked(false);
        btnFri.setChecked(false);
        btnThur.setChecked(false);
        btnThur.setChecked(false);
        btnWed.setChecked(false);
        btnTue.setChecked(false);
        btnMon.setChecked(false);

        if(currentSchedule != null) {
            PHLightState state = currentSchedule.getLightState();

            nameEditor.setText(currentSchedule.getName());

            bulbOnState.setChecked(state.isOn());

            if(state.getBrightness() != null) {
                int currentBrightness = HueBulbChangeUtility.revertBrightness(state.getBrightness());
                brightness.setProgress(currentBrightness);
                brightnessPercentage.setText(currentBrightness + "%");
            }

            if(state.getX() != null && state.getY() != null) {
                float[] currentXYColor = new float[]{state.getX(), state.getY()};
                currentColor = PHUtilities.colorFromXY(currentXYColor, HueBulbChangeUtility.COLOR_XY_MODEL_FOR_HUE);
                colorPicker.setColor(currentXYColor);
            }

            recurringDays = currentSchedule.getRecurringDays();
            recurringDaysBitStr = String.format("%07d", new BigInteger(
                    Integer.toBinaryString(recurringDays)));

            for (int i = 0; i < recurringDaysBitStr.length(); i++) {
                switch (i) {
                    case 0:
                        if (recurringDaysBitStr.charAt(0) == '1') {

                            recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_MONDAY
                                    .getValue());
                            btnMon.setChecked(true);
                        }

                        break;
                    case 1:
                        if (recurringDaysBitStr.charAt(1) == '1') {
                            recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_TUESDAY
                                    .getValue());
                            btnTue.setChecked(true);
                        }
                        break;
                    case 2:
                        if (recurringDaysBitStr.charAt(2) == '1') {
                            recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_WEDNESDAY
                                    .getValue());
                            btnWed.setChecked(true);
                        }
                        break;
                    case 3:
                        if (recurringDaysBitStr.charAt(3) == '1') {
                            recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_THURSDAY
                                    .getValue());
                            btnThur.setChecked(true);
                        }
                        break;
                    case 4:
                        if (recurringDaysBitStr.charAt(4) == '1') {
                            recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_FRIDAY
                                    .getValue());
                            btnFri.setChecked(true);
                        }
                        break;
                    case 5:
                        if (recurringDaysBitStr.charAt(5) == '1') {
                            recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_SATURDAY
                                    .getValue());
                            btnSat.setChecked(true);
                        }
                        break;
                    case 6:
                        if (recurringDaysBitStr.charAt(6) == '1') {
                            recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_SUNDAY
                                    .getValue());
                            btnSun.setChecked(true);
                        }
                        break;
                    default:
                        break;
                }
            }

            Date lastScheduleTime = currentSchedule.getDate();
            if (lastScheduleTime != null) {
                mHour = lastScheduleTime.getHours();
                mMinute = lastScheduleTime.getMinutes();
                timeToSend = Calendar.getInstance().getTime();
                timeToSend.setHours(mHour);
                timeToSend.setMinutes(mMinute);
                updateDisplay();
            }
        }
        else {
            brightness.setProgress(100);
        }

        return frame;
    }

    private void updateSchedule() {

        currentSchedule.setName(nameEditor.getText().toString().trim());
        currentSchedule.setDate(timeToSend);
        currentSchedule.setRecurringDays(recurringDays);
        currentSchedule.setLightState(getLightState());
        currentSchedule.setDescription("prism");
        if(isGroup)
            currentSchedule.setGroupIdentifier(identifier);
        else
            currentSchedule.setLightIdentifier(identifier);
        currentSchedule.setAutoDelete(true);

        progressDialog = DialogCreator.showLoadingDialog(getText(R.string.sending_progress).toString(), (MainActivity)getActivity());


        bridge.updateSchedule(currentSchedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {
                ;
            }

            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        closeProgressDialog();
                    }
                });
                goToScheduleFragment();
            }

            @Override
            public void onError(int i, final String s) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        closeProgressDialog();
                        DialogCreator.showWarningDialog("Error", s, (MainActivity)getActivity());
                    }
                });
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {
            ;
            }
        });

    }

    private void addNewSchedule() {
        currentSchedule = new PHSchedule(nameEditor.getText().toString().trim());
        //currentSchedule.setName(nameEditor.getText().toString());
        currentSchedule.setDate(timeToSend);
        currentSchedule.setRecurringDays(recurringDays);
        currentSchedule.setLightState(getLightState());
        currentSchedule.setDescription("prism");
        if(isGroup)
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
                        //TODO add result dialog?
                    }
                });
                goToScheduleFragment();
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int i, final String s) {
                getActivity().runOnUiThread(new Runnable() {
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


    private PHLightState getLightState() {
        PHLightState state = new PHLightState();
        state.setOn(bulbOnState.isChecked());
//        state.setHue(50);
//        state.setSaturation(50);
        state.setBrightness(HueBulbChangeUtility.convertBrightness(brightness.getProgress()));
        float xy[] = PHUtilities.calculateXY(currentColor, "");
        state.setX(xy[0]);
        state.setY(xy[1]);
//        state.setX((float)0);
//        state.setY((float)0);
//        state.setEffectMode(PHLight.PHLightEffectMode.EFFECT_NONE);
//        state.setAlertMode(PHLight.PHLightAlertMode.ALERT_NONE);
//        state.setTransitionTime(1);
        return state;
    }


    private void goToScheduleFragment() {
        android.app.FragmentManager fm = getActivity().getFragmentManager();
        fm.popBackStack();
    }

    private boolean isCurrentActivity() {
        ActivityManager mActivityManager = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
        String currentClass = "." + this.getClass().getSimpleName();
        String topActivity =  ar.topActivity.getShortClassName().toString();
        return topActivity.contains(currentClass);
    }

    /**
     * Listener for ToggleButton for recurring days.
     */
    private View.OnClickListener recurringHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            recurringDaysBitStr = String.format("%07d", new BigInteger(
                    Integer.toBinaryString(recurringDays)));
            StringBuffer sb = new StringBuffer(recurringDaysBitStr);
            switch (v.getId()) {
                case R.id.btnSun:
                    if (btnSun.isChecked()) {

                        recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_SUNDAY
                                .getValue());
                    } else {
                        sb.setCharAt(6, '0');
                        recurringDaysBitStr = sb.toString();
                        recurringDays = Integer.parseInt(recurringDaysBitStr, 2);
                    }
                    break;
                case R.id.btnMon:
                    if (btnMon.isChecked()) {
                        recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_MONDAY
                                .getValue());
                    } else {
                        sb.setCharAt(0, '0');
                        recurringDaysBitStr = sb.toString();
                        recurringDays = Integer.parseInt(recurringDaysBitStr, 2);
                    }
                    break;
                case R.id.btnTue:
                    if (btnTue.isChecked()) {
                        recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_TUESDAY
                                .getValue());
                    } else {
                        sb.setCharAt(1, '0');
                        recurringDaysBitStr = sb.toString();
                        recurringDays = Integer.parseInt(recurringDaysBitStr, 2);
                    }
                    break;
                case R.id.btnWed:
                    if (btnWed.isChecked()) {
                        recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_WEDNESDAY
                                .getValue());
                    } else {
                        sb.setCharAt(2, '0');
                        recurringDaysBitStr = sb.toString();
                        recurringDays = Integer.parseInt(recurringDaysBitStr, 2);
                    }
                    break;
                case R.id.btnThur:
                    if (btnThur.isChecked()) {
                        recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_THURSDAY
                                .getValue());
                    } else {
                        sb.setCharAt(3, '0');
                        recurringDaysBitStr = sb.toString();
                        recurringDays = Integer.parseInt(recurringDaysBitStr, 2);
                    }
                    break;
                case R.id.btnFri:
                    if (btnFri.isChecked()) {
                        recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_FRIDAY
                                .getValue());
                    } else {
                        sb.setCharAt(4, '0');
                        recurringDaysBitStr = sb.toString();
                        recurringDays = Integer.parseInt(recurringDaysBitStr, 2);
                    }
                    break;
                case R.id.btnSat:
                    if (btnSat.isChecked()) {
                        recurringDays = (recurringDays | PHSchedule.RecurringDay.RECURRING_SATURDAY
                                .getValue());
                    } else {
                        sb.setCharAt(5, '0');
                        recurringDaysBitStr = sb.toString();
                        recurringDays = Integer.parseInt(recurringDaysBitStr, 2);
                    }
                    break;
            }
        }
    };


    /*
     * Listener for TimerPicker dialog to indicate the user is done filling in
     * the time.
     */
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            updateDisplay();

            timeToSend = Calendar.getInstance().getTime();
            timeToSend.setHours(mHour);
            timeToSend.setMinutes(mMinute);
        }
    };

    /*
     * update the displayed time on button.
     */
    private void updateDisplay() {
        btnScheduleTime.setText(new StringBuilder().append(PHHelper.pad(mHour))
                .append(":").append(PHHelper.pad(mMinute)));
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
