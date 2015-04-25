package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
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

    private Schedule currentSchedule;
    private EditText nameEditor;
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

    private PHSchedule currentOnSchedule;
    private Button timeOnPickerButton;
    private Date timeOn;
    private int timeChoiceOn;

    private PHSchedule currentOffSchedule;
    private Button timeOffPickerButton;
    private Date timeOff;
    private int timeChoiceOff;

    private static int recurringDays;
    private static String recurringDaysBitStr;

    private int updateCounter;





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

        //TODO: get both currentOnSchedule, and currentOffSchedule Here
        currentSchedule = ((MainActivity)getActivity()).getCurrentSchedule();
        ((MainActivity)getActivity()).setCurrentPHschedule(null);

        if (getArguments() != null) {
            identifier = getArguments().getString(RealHomeFragment.lightPositionString);
            isGroup = getArguments().getBoolean(RealHomeFragment.groupOrLightString);
        }

        recurringDays = 0; //default value
        if(currentSchedule!=null) {
            currentOnSchedule = currentSchedule.getScheduleOn();
            timeChoiceOn = currentSchedule.getTimeChoiceOn();
            timeOn = currentSchedule.getOnTime();

            currentOffSchedule = currentSchedule.getScheduleOff();
            timeChoiceOff = currentSchedule.getTimeChoiceOff();
            timeOff = currentSchedule.getOffTime();
        }
        else{
            currentOnSchedule = null;
            timeChoiceOn = -1;
            timeOn = null;

            currentOffSchedule = null;
            timeChoiceOff = -1;
            timeOff = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_schedule_config, container, false);
        nameEditor = (EditText) frame.findViewById(R.id.nameEditor);
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
                String name = nameEditor.getText().toString().trim();
                if (name.equals("")) {
                    DialogCreator.showWarningDialog("Error", "Please enter a name.", (MainActivity) getActivity());
                } else if (name.length()>50){
                    DialogCreator.showWarningDialog("Error", "Name is too long. Please enter shorter name.", (MainActivity) getActivity());
                } else if (timeChoiceOn==-1 && timeChoiceOff==-1){
                    DialogCreator.showWarningDialog("Error", "Please enter On time or Off time", (MainActivity) getActivity());
                } else if (recurringDays == 0){
                    DialogCreator.showWarningDialog("Error", "Please choose at least one day.", (MainActivity) getActivity());
                }
                else {
                    String key = new Date().getTime() + "";

                    if (currentSchedule != null && currentOnSchedule != null) {
                        if (timeChoiceOn == 0) {
                            updateSchedule(currentOnSchedule, timeOn, timeChoiceOn, true);
                            updateCounter++;
                        } else if (timeChoiceOn == 1) {
                            updateSchedule(currentOnSchedule, ((MainActivity) getActivity()).getSunrise(), timeChoiceOn, true);
                            updateCounter++;
                        } else if (timeChoiceOn == 2) {
                            updateSchedule(currentOnSchedule, ((MainActivity) getActivity()).getSunset(), timeChoiceOn, true);
                            updateCounter++;
                        } else {//timeChoiceOn is -1 or 3
                            if (currentOnSchedule != null) {
                                deleteSchedule(currentOnSchedule);
                                updateCounter++;
                            }
                        }
                    } else {

                        if (timeChoiceOn == 0) {
                            addNewSchedule(timeOn, timeChoiceOn, true, key);
                            updateCounter++;
                        } else if (timeChoiceOn == 1) {
                            addNewSchedule(((MainActivity) getActivity()).getSunrise(), timeChoiceOn, true, key);
                            updateCounter++;
                        } else if (timeChoiceOn == 2) {
                            addNewSchedule(((MainActivity) getActivity()).getSunset(), timeChoiceOn, true, key);
                            updateCounter++;
                        }
                    }

                    if (currentSchedule != null && currentOffSchedule != null) {
                        if (timeChoiceOff == 0) {
                            updateSchedule(currentOffSchedule, timeOff, timeChoiceOff, false);
                            updateCounter++;
                        } else if (timeChoiceOff == 1) {
                            updateSchedule(currentOffSchedule, ((MainActivity) getActivity()).getSunrise(), timeChoiceOff, false);
                            updateCounter++;
                        } else if (timeChoiceOff == 2) {
                            updateSchedule(currentOffSchedule, ((MainActivity) getActivity()).getSunset(), timeChoiceOff, false);
                            updateCounter++;
                        } else {//timeChoiceOff is -1 or 3
                            if (currentOffSchedule != null) {
                                deleteSchedule(currentOffSchedule);
                                updateCounter++;
                            }
                        }
                    } else {
                        if (timeChoiceOff == 0) {
                            addNewSchedule(timeOff, timeChoiceOff, false, key);
                            updateCounter++;
                        } else if (timeChoiceOff == 1) {
                            addNewSchedule(((MainActivity) getActivity()).getSunrise(), timeChoiceOff, false, key);
                            updateCounter++;
                        } else if (timeChoiceOff == 2) {
                            addNewSchedule(((MainActivity) getActivity()).getSunset(), timeChoiceOff, false, key);
                            updateCounter++;
                        }
                    }
                }
            }
        });

        timeOnPickerButton = (Button) frame.findViewById(R.id.btnOnTime);
        timeOffPickerButton = (Button) frame.findViewById(R.id.btnOffTime);

        timeOnPickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose Turn On Time");

                //timeChoiceOn 0:timePick, 1:sunrise, 2:sunset, none
                timeChoiceOn = 0;
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.schedule_time_picker, null);
                final TimePicker timePicker = (TimePicker) (view.findViewById(R.id.ScheduleTimePicker));
                final RadioGroup radioGroup = (RadioGroup) (view.findViewById(R.id.ScheduleRadioGroup));
                final RadioButton radioButtonSunrise = (RadioButton) (view.findViewById(R.id.radioButtonSunrise));
                final RadioButton radioButtonSunset = (RadioButton) (view.findViewById(R.id.radioButtonSunset));
                final RadioButton radioButtonNone = (RadioButton) (view.findViewById(R.id.radioButtonNone));

                timePicker.setIs24HourView(false);

                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        radioGroup.clearCheck();
                        timeChoiceOn = 0;
                        // TODO: blur timePicker
                    }
                });


                radioButtonSunrise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timeChoiceOn = 1;
                        // TODO: blur timePicker
                    }
                });

                radioButtonSunset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timeChoiceOn = 2;
                        // TODO: blur timePicker
                    }
                });

                radioButtonNone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timeChoiceOn = 3;
                        // TODO: blur timePicker
                    }
                });


                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (timeChoiceOn == 0) {
                            timeOn = Calendar.getInstance().getTime();
                            timeOn.setHours(timePicker.getCurrentHour());
                            timeOn.setMinutes(timePicker.getCurrentMinute());
                        }
                        updateOnTimeDisplay();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setView(view);
                builder.show();
            }
        });

        timeOffPickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose Turn Off Time");

                //timeChoiceOn 0:timePick, 1:sunrise, 2:sunset, none
                timeChoiceOff = 0;
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.schedule_time_picker, null);
                final TimePicker timePicker = (TimePicker) (view.findViewById(R.id.ScheduleTimePicker));
                final RadioGroup radioGroup = (RadioGroup) (view.findViewById(R.id.ScheduleRadioGroup));
                final RadioButton radioButtonSunrise = (RadioButton) (view.findViewById(R.id.radioButtonSunrise));
                final RadioButton radioButtonSunset = (RadioButton) (view.findViewById(R.id.radioButtonSunset));
                final RadioButton radioButtonNone = (RadioButton) (view.findViewById(R.id.radioButtonNone));

                timePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioGroup.clearCheck();
                        timeChoiceOff = 0;
                        // TODO: blur timePicker
                    }
                });

                radioButtonSunrise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timeChoiceOff = 1;
                        // TODO: blur timePicker
                    }
                });

                radioButtonSunset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timeChoiceOff = 2;
                        // TODO: blur timePicker
                    }
                });

                radioButtonNone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timeChoiceOff = 3;
                        // TODO: blur timePicker
                    }
                });


                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (timeChoiceOff == 0) {
                            timeOff = Calendar.getInstance().getTime();
                            timeOff.setHours(timePicker.getCurrentHour());
                            timeOff.setMinutes(timePicker.getCurrentMinute());
                        }
                        updateOffTimeDisplay();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setView(view);
                builder.show();
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

            if(state.getBrightness() != null) {
                int currentBrightness = HueBulbChangeUtility.revertBrightness(state.getBrightness());
                brightness.setProgress(currentBrightness);
                brightnessPercentage.setText(currentBrightness + "%");
            }
            else
                brightness.setProgress(100);

            if(state.getX() != null && state.getY() != null) {
                float[] currentXYColor = new float[]{state.getX(), state.getY()};
                currentColor = PHUtilities.colorFromXY(currentXYColor, HueBulbChangeUtility.COLOR_XY_MODEL_FOR_HUE);
                colorPicker.setColor(currentXYColor);
            }

            recurringDays = currentSchedule.getRecurringDays();
            updateDaysToggles(recurringDays);



            updateOnTimeDisplay();
            updateOffTimeDisplay();
        }
        else {
            brightness.setProgress(100);
            recurringDays = 127; //default: every day
            updateDaysToggles(recurringDays);
        }

        return frame;
    }

    private void updateDaysToggles(int recurringDays) {
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
    }

    private void updateSchedule(PHSchedule schedule, Date time, int choiceTime, boolean isOn) {

        schedule.setName(nameEditor.getText().toString().trim());
        schedule.setDate(time);
        schedule.setRecurringDays(recurringDays);
        schedule.setLightState(getLightState(isOn));

        String[] tokens = schedule.getDescription().split("[,]");

        schedule.setDescription("prism,"+ tokens[1] + "," + choiceTime);
        if(isGroup)
            schedule.setGroupIdentifier(identifier);
        else
            schedule.setLightIdentifier(identifier);
        schedule.setAutoDelete(true);

        progressDialog = DialogCreator.showLoadingDialog(getText(R.string.sending_progress).toString(), (MainActivity)getActivity());

        bridge.updateSchedule(schedule, new PHScheduleListener() {
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
                updateCounter--;
                goToScheduleFragment();
            }

            @Override
            public void onError(int i, final String s) {
                updateCounter--;
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

    private void addNewSchedule(Date time, int choiceTime, boolean isOn, String key) {
        PHSchedule schedule = new PHSchedule(nameEditor.getText().toString().trim());
        schedule.setDate(time);
        schedule.setRecurringDays(recurringDays);
        schedule.setLightState(getLightState(isOn));
        schedule.setDescription("prism,"+ key + "," + choiceTime);
        if(isGroup)
            schedule.setGroupIdentifier(identifier);
        else
            schedule.setLightIdentifier(identifier);
        schedule.setAutoDelete(true);

        progressDialog = DialogCreator.showLoadingDialog(getText(R.string.sending_progress).toString(), (MainActivity)getActivity());

        bridge.createSchedule(schedule, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        //TODO add result dialog?
                    }
                });
                updateCounter--;
                goToScheduleFragment();
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int i, final String s) {
                updateCounter--;
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

    private void deleteSchedule(PHSchedule schedule) {
        String scheduleID = schedule.getIdentifier();

        bridge.removeSchedule(scheduleID, new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {

            }

            @Override
            public void onSuccess() {
                updateCounter--;
                return;
            }

            @Override
            public void onError(int i, final String s) {
                updateCounter--;
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }


    private PHLightState getLightState(boolean isOn) {
        PHLightState state = new PHLightState();
        state.setOn(isOn);
        state.setBrightness(HueBulbChangeUtility.convertBrightness(brightness.getProgress()));
        float xy[] = PHUtilities.calculateXY(currentColor, "");
        state.setX(xy[0]);
        state.setY(xy[1]);
        return state;
    }


    private void goToScheduleFragment() {
        if(updateCounter == 0) {
            android.app.FragmentManager fm = getActivity().getFragmentManager();
            fm.popBackStack();
        }
    }

//    private boolean isCurrentActivity() {
//
//        ActivityManager mActivityManager = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
//        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
//        String currentClass = "." + this.getClass().getSimpleName();
//        String topActivity =  ar.topActivity.getShortClassName().toString();
//        return topActivity.contains(currentClass);
//    }

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
     * update the displayed time on button.
     */
    private void updateOnTimeDisplay() {
        if(timeChoiceOn==0){
            String delegate = "hh:mm aaa";
            String onTimeString = (String) DateFormat.format(delegate, timeOn.getTime());
            timeOnPickerButton.setText(onTimeString);
        }

        else if (timeChoiceOn==1)
            timeOnPickerButton.setText("Sunrise");
        else if (timeChoiceOn==2)
            timeOnPickerButton.setText("Sunset");
        else
            timeOnPickerButton.setText("None");
    }

    private void updateOffTimeDisplay() {
        if(timeChoiceOff==0){
            String delegate = "hh:mm aaa";
            String offTimeString = (String) DateFormat.format(delegate, timeOff.getTime());
            timeOffPickerButton.setText(offTimeString);
        }
        else if (timeChoiceOff==1)
            timeOffPickerButton.setText("Sunrise");
        else if (timeChoiceOff==2)
            timeOffPickerButton.setText("Sunset");
        else
            timeOffPickerButton.setText("None");
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
