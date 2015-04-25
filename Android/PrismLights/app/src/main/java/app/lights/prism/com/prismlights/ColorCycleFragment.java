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
        bridge = PHHueSDK.getInstance().getSelectedBridge();
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

//        colorCycleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                chosenColorCycle = position;
//                colorCycleDetailListAdapter.notifyDataSetChanged();
//            }
//        });

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
//        okButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //TODO: need to get duration from user
//                List<ScheduledFuture> tasks = colorCycles.get(chosenColorCycle).startColorCycle(10, bridge, currentIdentifier, isGroup, (MainActivity)getActivity());
//                ((MainActivity)getActivity()).setColorCycleTasks(currentIdentifier,tasks, isGroup);
//                android.app.FragmentManager fm = getActivity().getFragmentManager();
//                fm.popBackStack();
//            }
//        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(popping || HueBulbChangeUtility.popBackStackIfItemNotExist(currentIdentifier, isGroup, getFragmentManager())) {
            return;
        }
        colorCycles = ((MainActivity)getActivity()).getAllColorCycles();
        colorCycleListAdapter.notifyDataSetChanged();
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
            Button runColorCycleButton = (Button) currentView.findViewById(R.id.colorCycleRun);
            runColorCycleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<ScheduledFuture> tasks = colorCycles.get(position).startColorCycle(10, bridge, currentIdentifier, isGroup, (MainActivity) getActivity());
                    ((MainActivity) getActivity()).setColorCycleTasks(currentIdentifier, tasks, isGroup);
//                    android.app.FragmentManager fm = getActivity().getFragmentManager();
//                    fm.popBackStack();
                }
            });

            ImageView deleteImage = (ImageView)currentView.findViewById(R.id.colorCyleListDeleteImage);
            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)getActivity()).deleteColorCycle(position);
                    colorCycles = ((MainActivity)getActivity()).getAllColorCycles();
                    colorCycleListAdapter.notifyDataSetChanged();
                }
            });
            return currentView;
        }
    }

}
