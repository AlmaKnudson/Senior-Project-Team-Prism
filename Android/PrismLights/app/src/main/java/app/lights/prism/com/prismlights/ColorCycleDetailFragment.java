package app.lights.prism.com.prismlights;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

public class ColorCycleDetailFragment extends Fragment {

    private int chosenIndex;
    private boolean isNew;

    private ColorCycle newColorCycle;

    private ColorCycleAdapter adapter;
//    private int newPosition;
//    private float[] newColor;
//    private int newBrightness;
//    private int newDuration;
//    private int newTransition;
    float[] newColor;// color holder for color selection at each color cycle row.

    public ColorCycleDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chosenIndex = -1;
        if (getArguments() != null) {
            chosenIndex = getArguments().getInt(ColorCycleFragment.chosenColorCycleString);
            isNew = getArguments().getBoolean(ColorCycleFragment.isNewString);
        }
        if(chosenIndex != -1) {
            ColorCycle chosenColorCycle = ((MainActivity) getActivity()).getAllColorCycles().get(chosenIndex);
            newColorCycle = new ColorCycle(chosenColorCycle);
        }
        else
            newColorCycle = new ColorCycle("");

        newColor = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_cycle_detail, container, false);
        final EditText nameEditor = (EditText)view.findViewById(R.id.nameEditor);
        ImageView addImageView = (ImageView)view.findViewById(R.id.addColorButton);
        ListView colorCycleListView = (ListView)view.findViewById(R.id.colorCycleDetailListView);
        Button saveButton = (Button)view.findViewById(R.id.colorCycleSaveButton);

        nameEditor.setText(newColorCycle.getName());

        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float[] newColor = {0.39f,0.46f};
                newColorCycle.add(newColor, 100, 10, 2);
                adapter.notifyDataSetChanged();
            }
        });

        adapter = new ColorCycleAdapter();
        colorCycleListView.setAdapter(adapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nameEditor.getText().toString().trim().equals(""))
                    new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
                            .setTitle("Error")
                            .setMessage("Please Enter Name for this Color Cycle.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                else if (newColorCycle.getSize() < 2)
                    new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
                            .setTitle("Error")
                            .setMessage("Please add at least two colors.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                else {
                    String newName = nameEditor.getText().toString().trim();
                    // if this is new cycle, check if the name already exist or not.
                    if(isNew && ((MainActivity) getActivity()).containsCycleName(newName) != -1) {
                        new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
                                .setTitle("Error")
                                .setMessage("Same Name already exits, please enter other name.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }else {
                        newColorCycle.setName(newName);
                        if (chosenIndex == -1) {
                            ((MainActivity) getActivity()).addColorCycle(newColorCycle);
                        } else {
                            ((MainActivity) getActivity()).setColorCycle(chosenIndex, newColorCycle);
                        }

                        android.app.FragmentManager fm = getActivity().getFragmentManager();
                        fm.popBackStack();
                    }
                }
            }
        });

        return view;
    }

    private class ColorCycleAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return newColorCycle.getSize();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View currentView;
            if(convertView == null) {
                currentView = LayoutInflater.from(ColorCycleDetailFragment.this.getActivity()).inflate(R.layout.single_color_cycle_edit_row, parent, false);
            } else {
                currentView = convertView;
            }

            View color = currentView.findViewById(R.id.colorEditText);
            final TextView brightness = (TextView)currentView.findViewById(R.id.brightnessEditText);
            TextView duration = (TextView)currentView.findViewById(R.id.durationEditText);
            TextView transition = (TextView)currentView.findViewById(R.id.transitionEditText);
            ImageView deleteImageView = (ImageView)currentView.findViewById(R.id.deleteColorButton);

            color.setBackgroundColor(newColorCycle.getColor(position));
            brightness.setText(newColorCycle.getBrightness(position) + "%");
            duration.setText(newColorCycle.getDuration(position)+"s");
            transition.setText(newColorCycle.getTransition(position)+"s");

            color.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
                    builder.setTitle("Choose Color");
                    final ColorPickerViewGroup colorPickerViewGroup = new ColorPickerViewGroup(getActivity());
                    if (newColorCycle.getColorFloat(position) != null) {
                        newColor = newColorCycle.getColorFloat(position);
                        colorPickerViewGroup.setColor(newColor);
                    }
                    colorPickerViewGroup.setColorChangedListener(new ColorChangedListener() {
                        @Override
                        public void onColorChanged(float[] _newColor) {
                            newColor = _newColor;
                        }
                    });
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            newColorCycle.setColor(position, newColor);
                            newColor = null;
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            newColor = null;
                        }
                    });
                    builder.setView(colorPickerViewGroup);
                    builder.show();
                }
            });
            brightness.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
                    builder.setTitle("Enter Brightness");

                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View brightnessView = inflater.inflate(R.layout.brightness_dialog, null);
                    final TextView percentText = (TextView) (brightnessView.findViewById(R.id.percentText));
                    final SeekBar seekBar = (SeekBar) (brightnessView.findViewById(R.id.seekBar));
                    int brightnessInt = newColorCycle.getBrightness(position);
                    percentText.setText(brightnessInt+"%");
                    seekBar.setProgress(brightnessInt);

                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            percentText.setText(progress + "%");
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            newColorCycle.setBrightness(position, seekBar.getProgress());
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setView(brightnessView);
                    builder.show();
                }
            });
            duration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
                    builder.setTitle("Enter Duration");

                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View brightnessView = inflater.inflate(R.layout.duration_dialog, null);
                    TimePicker timePicker = (TimePicker)brightnessView.findViewById(R.id.timePicker);
                    TimePicker timePickerSec = (TimePicker)brightnessView.findViewById(R.id.timePicker_Sec);
                    final TextView hourText = (TextView)brightnessView.findViewById(R.id.hourText);
                    final TextView minText = (TextView)brightnessView.findViewById(R.id.minText);
                    final TextView secText = (TextView)brightnessView.findViewById(R.id.secText);

                    timePicker.setIs24HourView(true);
                    timePickerSec.setIs24HourView(true);
                    int timeInSeconds = newColorCycle.getDuration(position);
                    int hour = timeInSeconds/3600;
                    int min = (timeInSeconds%3600)/60;
                    int sec = timeInSeconds%60;
                    timePicker.setCurrentHour(hour);
                    timePicker.setCurrentMinute(min);
                    timePickerSec.setCurrentMinute(sec);
                    hourText.setText(hour + " h");
                    minText.setText(min + " m");
                    secText.setText(sec + " s");

                    timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                            hourText.setText(hourOfDay + " h");
                            minText.setText(minute + " m");
                        }
                    });
                    timePickerSec.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                            secText.setText(minute+" s");
                        }
                    });

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int time = Integer.parseInt(hourText.getText().toString().split("\\s")[0]) * 3600
                            + Integer.parseInt(minText.getText().toString().split("\\s")[0]) * 60
                            + Integer.parseInt(secText.getText().toString().split("\\s")[0]);

                            if (time == 0){
                                time = 1;
                                DialogCreator.showWarningDialog("Error", "Duration cannot be 0.", (MainActivity) getActivity());
                            }

                            newColorCycle.setDuration(position, time);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setView(brightnessView);
                    builder.show();

                }
            });
            transition.setOnClickListener(new View.OnClickListener() {

                TextView hourText = null;
                TextView minText = null;
                TextView secText = null;

                class CustomListener implements View.OnClickListener {
                    private final Dialog dialog;
                    public CustomListener(Dialog dialog) {
                        this.dialog = dialog;
                    }
                    @Override
                    public void onClick(View v) {
                        int time = Integer.parseInt(hourText.getText().toString().split("\\s")[0]) * 3600
                                + Integer.parseInt(minText.getText().toString().split("\\s")[0]) * 60
                                + Integer.parseInt(secText.getText().toString().split("\\s")[0]);
                        if(time > newColorCycle.getDuration(position))
                        {
                            new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
                                    .setTitle("Error")
                                    .setMessage("Transition time cannot be longer than duration of the color.")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        } else {
                            newColorCycle.setTransition(position, time);
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }


                    }
                }

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
                    builder.setTitle("Enter Duration");

                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View transitionView = inflater.inflate(R.layout.duration_dialog, null);
                    TimePicker timePicker = (TimePicker)transitionView.findViewById(R.id.timePicker);
                    TimePicker timePickerSec = (TimePicker)transitionView.findViewById(R.id.timePicker_Sec);
                    hourText = (TextView)transitionView.findViewById(R.id.hourText);
                    minText = (TextView)transitionView.findViewById(R.id.minText);
                    secText = (TextView)transitionView.findViewById(R.id.secText);

                    timePicker.setIs24HourView(true);
                    timePickerSec.setIs24HourView(true);
                    int timeInSeconds = newColorCycle.getTransition(position);
                    int hour = timeInSeconds/3600;
                    int min = (timeInSeconds%3600)/60;
                    int sec = timeInSeconds% 60;
                    timePicker.setCurrentHour(hour);
                    timePicker.setCurrentMinute(min);
                    timePickerSec.setCurrentMinute(sec);
                    hourText.setText(hour + " h");
                    minText.setText(min + " m");
                    secText.setText(sec + " s");

                    timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                            hourText.setText(hourOfDay + " h");
                            minText.setText(minute + " m");
                        }
                    });
                    timePickerSec.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                            secText.setText(minute + " s");
                        }
                    });

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int time = Integer.parseInt(hourText.getText().toString().split("\\s")[0]) * 3600
                                    + Integer.parseInt(minText.getText().toString().split("\\s")[0]) * 60
                                    + Integer.parseInt(secText.getText().toString().split("\\s")[0]);
                            newColorCycle.setTransition(position, time);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setView(transitionView);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    theButton.setOnClickListener(new CustomListener(alertDialog));


                }
            });

            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newColorCycle.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });

            return currentView;

        }
    }

    @Override
    public void onDestroyView() {
        //add more often color cycle saving
        ((MainActivity)getActivity()).saveColorCycles();
        super.onDetach();
    }
}
