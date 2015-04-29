package app.lights.prism.com.prismlights;

import android.app.FragmentTransaction;
import android.graphics.Color;
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
import com.philips.lighting.model.PHSchedule;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;


public class ColorCycleFragment extends Fragment {

    private static  final String DEBUG_TAG = "ColorCycleFragment";
    public static final String chosenColorCycleString = "CHOSEN_COLOR_CYCLE";
    public static final String isNewString = "IS_NEW";

    private boolean isGroup; //True if group false otherwise
    private List<ColorCycle> colorCycles;
    private PHHueSDK hueSDK;
    private PHBridge bridge;
    private PHLight currentLight;
    private PHGroup currentGroup;
    private String currentIdentifier;
    private ColorCycleListAdapter colorCycleListAdapter;
    private boolean popping;

    public ColorCycleFragment() {
        // Required empty public constructor
        popping = false;
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
        if(popping || HueBulbChangeUtility.popBackStackIfItemNotExist(currentIdentifier, isGroup, getFragmentManager())) {
            return;
        }
        if(!isGroup) {
            currentLight = bridge.getResourceCache().getLights().get(currentIdentifier);
            currentIdentifier = currentLight.getIdentifier();
            currentGroup = null;
        }
        else {
            currentGroup = bridge.getResourceCache().getGroups().get(currentIdentifier);
            currentIdentifier = currentGroup.getIdentifier();
            currentLight = null;
        }
        checkColorCycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_color_cycle, container, false);
        if(popping || HueBulbChangeUtility.popBackStackIfItemNotExist(currentIdentifier, isGroup, getFragmentManager())) {
            return view;
        }
//        TextView nameTextView = (TextView)view.findViewById(R.id.colorCycleBulbNameText);
        ImageView colorCycleAddImageView = (ImageView)view.findViewById(R.id.colorCycleAddButton);
        ListView colorCycleListView = (ListView)view.findViewById(R.id.colorCycleListView);
//        if(isGroup)
//            nameTextView.setText(currentGroup.getName());
//        else
//            nameTextView.setText(currentLight.getName());

        colorCycleAddImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(chosenColorCycleString, -1); // indicating this is for a new colorcycle
                bundle.putBoolean(isNewString, true);
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

        colorCycleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putInt(chosenColorCycleString, position);//pass chosen colorCycle
                bundle.putBoolean(isNewString, false);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ColorCycleDetailFragment colorCycleDetailFragment = new ColorCycleDetailFragment();
                colorCycleDetailFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, colorCycleDetailFragment);
                fragmentTransaction.addToBackStack("ColorCycleDetailFragment");
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    private void checkColorCycle(){
        /****************Checking Color Cycle**************/
        //check if there is current colorCycle going on.
        List<PHSchedule> colorCycles = bridge.getResourceCache().getAllTimers(true);
        List<PHSchedule> colorCyclesForThisBulb = new ArrayList<>();
        if(isGroup){
            for (int i = 0; i < colorCycles.size(); i++) {
                if (colorCycles.get(i).getGroupIdentifier() != null
                        && colorCycles.get(i).getGroupIdentifier().equals(currentIdentifier)
                        && colorCycles.get(i).getDescription().startsWith("prism"))
                    colorCyclesForThisBulb.add(colorCycles.get(i));
            }
        } else {
            for (int i = 0; i < colorCycles.size(); i++) {
                if (colorCycles.get(i).getLightIdentifier() != null
                        && colorCycles.get(i).getLightIdentifier().equals(currentIdentifier)
                        && colorCycles.get(i).getDescription().startsWith("prism"))
                    colorCyclesForThisBulb.add(colorCycles.get(i));
            }
        }

        // if there is a color cycle running, set it as current color cycle. if this is new color cycle from other device, add it to the list.
        if (colorCyclesForThisBulb.size()!=0) {
            ColorCycle currentColorCycle = new ColorCycle(colorCyclesForThisBulb); // this generate ColorCycle class out of List of recurring timer schedule
            String currentColorCycleName = currentColorCycle.getName();
            ((MainActivity) getActivity()).setCurrentColorCycleName(currentColorCycleName);
            int nameExist = ((MainActivity) getActivity()).containsCycleName(currentColorCycleName);
            if (nameExist < 0) { // if nameExist is -1, this means there is no such name in current color cycles, so add new one.
                ((MainActivity) getActivity()).addColorCycle(currentColorCycle);
            }
        }
        /****************Checking Color Cycle END**************/
    }

    @Override
    public void onResume() {
        super.onResume();
        if(popping || HueBulbChangeUtility.popBackStackIfItemNotExist(currentIdentifier, isGroup, getFragmentManager())) {
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    //    private void refresh(){
//        checkColorCycle();
//        colorCycles = ((MainActivity)getActivity()).getAllColorCycles();
//        colorCycleListAdapter.notifyDataSetChanged();
//    }

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
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View currentView;
            if(convertView == null) {
                currentView = LayoutInflater.from(ColorCycleFragment.this.getActivity()).inflate(R.layout.color_cycle_list_item, parent, false);
            } else {
                currentView = convertView;
            }
            TextView colorCycleName = (TextView) currentView.findViewById(R.id.colorCycleName);
            colorCycleName.setText(colorCycles.get(position).getName());
            SingleColorCycleView cycleView = (SingleColorCycleView) currentView.findViewById(R.id.colorCycleSmallDetailView);
            cycleView.setColors(colorCycles.get(position).getColors());
            final Button runColorCycleButton = (Button) currentView.findViewById(R.id.colorCycleRun);
            String currentColorCycleName = ((MainActivity)getActivity()).getCurrentColorCycleName();
            if(currentColorCycleName!=null && currentColorCycleName.equals(colorCycles.get(position).getName()))
                runColorCycleButton.setText("Stop");
            else
                runColorCycleButton.setText("Run");

            runColorCycleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(runColorCycleButton.getText().equals("Run")) {
                        List<ScheduledFuture> tasks = colorCycles.get(position).startColorCycle(10, bridge, currentIdentifier, isGroup, (MainActivity) getActivity());
                        ((MainActivity) getActivity()).setColorCycleTasks(currentIdentifier, tasks, isGroup);
                        ((MainActivity)getActivity()).setCurrentColorCycleName(colorCycles.get(position).getName());
                        //runColorCycleButton.setText("Stop");
                        colorCycleListAdapter.notifyDataSetChanged();

                    } else if(runColorCycleButton.getText().equals("Stop")){
                        ColorCycle.removePreviousColorCycle(bridge,currentIdentifier, isGroup, (MainActivity)getActivity());
                        //runColorCycleButton.setText("Run");
                        colorCycleListAdapter.notifyDataSetChanged();
                    }
                }
            });

            ImageView deleteImage = (ImageView)currentView.findViewById(R.id.colorCyleListDeleteImage);
            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(runColorCycleButton.getText().equals("Stop")) {
                        ColorCycle.removePreviousColorCycle(bridge, currentIdentifier, isGroup, (MainActivity) getActivity());
                    }

                    ((MainActivity)getActivity()).deleteColorCycle(position);
                    colorCycles = ((MainActivity)getActivity()).getAllColorCycles();
                    notifyDataSetChanged();
                }
            });
            return currentView;
        }
    }

}
