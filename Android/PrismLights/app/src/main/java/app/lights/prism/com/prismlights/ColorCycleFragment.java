package app.lights.prism.com.prismlights;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;

import com.philips.lighting.model.PHLight;



import java.util.List;
import java.util.concurrent.ScheduledFuture;


public class ColorCycleFragment extends Fragment {

    private static  final String DEBUG_TAG = "ColorCycleFragment";
    public static final String chosenColorCycleString = "CHOSEN_COLOR_CYCLE";
    public static final String identifierString = "CHOSEN_IDENTIFIER";

    private int position; // The number for the chosen Light
    private boolean isGroup; //True if group false otherwise
    private List<ColorCycle> colorCycles;
    private PHHueSDK hueSDK;
    private PHBridge bridge;
    private PHLight currentLight;
    private PHGroup currentGroup;
    private String currentIdentifier;
    private int chosenColorCycle;
    private ColorCycleListAdapter colorCycleListAdapter;
    private ColorCycleDetailListAdapter colorCycleDetailListAdapter;

    public ColorCycleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentIdentifier = getArguments().getString(RealHomeFragment.lightPositionString);
            isGroup = getArguments().getBoolean(RealHomeFragment.groupOrLightString);
        }
        colorCycles = ((MainActivity)getActivity()).getAllColorCycles();
        hueSDK = PHHueSDK.getInstance();
        bridge = hueSDK.getSelectedBridge();
        if(!isGroup) {
            currentLight = hueSDK.getSelectedBridge().getResourceCache().getLights().get(currentIdentifier);
            currentIdentifier = currentLight.getIdentifier();
            currentGroup = null;
        }
        else {
            currentGroup = hueSDK.getSelectedBridge().getResourceCache().getGroups().get(currentIdentifier);
            currentIdentifier = currentGroup.getIdentifier();
            currentLight = null;
        }

        if (colorCycles!=null && colorCycles.size()>0)
            chosenColorCycle = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_color_cycle, container, false);
        TextView nameTextView = (TextView)view.findViewById(R.id.colorCycleBulbNameText);
        ImageView colorCycleAddImageView = (ImageView)view.findViewById(R.id.colorCycleAddButton);
        ListView colorCycleListView = (ListView)view.findViewById(R.id.colorCycleListView);
        Button colorCycleEditButton = (Button)view.findViewById(R.id.colorCycleEditButton);
        ListView colorCycleDetailListView = (ListView)view.findViewById(R.id.colorCycleDetailListView);
        Button okButton = (Button)view.findViewById(R.id.colorCycleOkButton);
        Button cancelButton = (Button)view.findViewById(R.id.colorCycleCancelButton);

        if(isGroup)
            nameTextView.setText(currentGroup.getName());
        else
            nameTextView.setText(currentLight.getName());

        colorCycleAddImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(chosenColorCycleString, -1); // indicating this is for a new colorcycle
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ColorCycleDetailFragment colorCycleDetailFragment = new ColorCycleDetailFragment();
                colorCycleDetailFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, colorCycleDetailFragment);
                fragmentTransaction.addToBackStack("ColorCycleDetailFragment");
                fragmentTransaction.commit();
            }
        });
        colorCycleListAdapter = new ColorCycleListAdapter();
        colorCycleListView.setAdapter(colorCycleListAdapter);

        if (colorCycles!=null && colorCycles.size()>0)
            colorCycleListView.setSelection(0);//TODO: check if selection working. I need to highLight selected one.

        colorCycleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenColorCycle = position;
                colorCycleDetailListAdapter.notifyDataSetChanged();
            }
        });

        colorCycleEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(chosenColorCycleString, chosenColorCycle);//pass chosen colorCycle
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ColorCycleDetailFragment colorCycleDetailFragment = new ColorCycleDetailFragment();
                colorCycleDetailFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, colorCycleDetailFragment);
                fragmentTransaction.addToBackStack("ColorCycleDetailFragment");
                fragmentTransaction.commit();
            }
        });

        colorCycleDetailListAdapter = new ColorCycleDetailListAdapter();
        colorCycleDetailListView.setAdapter(colorCycleDetailListAdapter);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: need to get duration from user
                List<ScheduledFuture> tasks = colorCycles.get(chosenColorCycle).startColorCycle(10, bridge, currentIdentifier, isGroup);
                ((MainActivity)getActivity()).setColorCycleTasks(currentIdentifier,tasks);
                android.app.FragmentManager fm = getActivity().getFragmentManager();
                fm.popBackStack();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.FragmentManager fm = getActivity().getFragmentManager();
                fm.popBackStack();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        colorCycles = ((MainActivity)getActivity()).getAllColorCycles();
        colorCycleListAdapter.notifyDataSetChanged();
        colorCycleDetailListAdapter.notifyDataSetChanged();
    }

    private class ColorCycleListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return colorCycles.size();
        }

        @Override
        public Object getItem(int position) {
            return colorCycles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView currentView;
            if(convertView == null) {
                currentView = new TextView(getActivity());
            } else {
                currentView = (TextView)convertView;
            }
            currentView.setText(colorCycles.get(position).getName());
            return currentView;
        }
    }

    private  class ColorCycleDetailListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            if (colorCycles==null || colorCycles.size()==0)
                return 0;
            else
                return colorCycles.get(chosenColorCycle).getSize();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View currentView;
            if(convertView == null) {
                currentView = LayoutInflater.from(ColorCycleFragment.this.getActivity()).inflate(R.layout.single_color_cycle_detail_row, parent, false);
            } else {
                currentView = convertView;
            }

            ColorCycle currentColorCycle = colorCycles.get(chosenColorCycle);

            TextView color = (TextView)currentView.findViewById(R.id.colorCycleColorText);
            TextView brightness = (TextView)currentView.findViewById(R.id.colorCycleBrightnessText);
            TextView duration = (TextView)currentView.findViewById(R.id.colorCycleDurationText);
            TextView transition = (TextView)currentView.findViewById(R.id.colorCycleTransitionText);

            color.setBackgroundColor(currentColorCycle.getColor(position));
            brightness.setText(currentColorCycle.getBrightness(position));
            duration.setText(currentColorCycle.getDuration(position));
            transition.setText(currentColorCycle.getTransition(position));

            return currentView;
        }
    }

}
