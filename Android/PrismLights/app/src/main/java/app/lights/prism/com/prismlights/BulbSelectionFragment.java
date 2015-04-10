package app.lights.prism.com.prismlights;


import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHLight;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class BulbSelectionFragment extends Fragment {

    private PHHueSDK hueSDK;

    private List<PHLight> currentLights;
    private String[] lightNames;
    private Set<Integer> checked;
//    private List<PHGroup> currentGroups;
//    private String[] groupNames;
//    private int shouldUpdateFromCache = 0;

    private GridView gridView;

    private static int disabledOverlay = Color.argb(125, 0, 0, 0);
    private static int offOverlay = Color.argb(50, 0, 0, 0);

    public BulbSelectionFragment() {
        // Required empty public constructor
        hueSDK = PHHueSDK.getInstance();
        checked = new HashSet<Integer>();
        updateFromCache();
    }

    private void updateFromCache() {
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getAllLights();
        lightNames = hueSDK.getLightNames(currentLights);
//        currentGroups = hueSDK.getSelectedBridge().getResourceCache().getAllGroups();
//        groupNames = hueSDK.getGroupNames(currentGroups);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout =  inflater.inflate(R.layout.fragment_bulb_selection, container, false);
        gridView = (GridView) layout.findViewById(R.id.selectionGridView);
        gridView.setAdapter(new SelectGridAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HueBulbChangeUtility.setBulbAlertState(currentLights.get(position).getIdentifier(), true);
                CheckBox checkBox = (CheckBox)view.findViewById(R.id.selectBulbCheck);
                changeChecked(checkBox, position);
            }
        });
        return layout;
    }

    private void changeChecked(CheckBox checkBox, Integer position) {
        if(checked.contains(position)) {
            checked.remove(position);
            checkBox.setChecked(false);
        } else {
            checked.add(position);
            checkBox.setChecked(true);
        }
    }


    private class SelectGridAdapter extends BaseAdapter {


        public SelectGridAdapter() {
            super();
            updateFromCache();
        }

        @Override
        public int getCount() {
            return currentLights.size();
        }

        @Override
        public Object getItem(int position) {
            return currentLights.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View currentView;
            if (convertView == null) {
                currentView = LayoutInflater.from(BulbSelectionFragment.this.getActivity()).inflate(R.layout.bulb_select_view   , parent, false);
            } else {
                currentView = convertView;
            }
            return getLightView(position, currentView, (PHLight) getItem(position));
//            } else {
//                return getGroupView(position - currentLights.size(), currentView, (PHGroup) currentLight);
//            }

        }



        private View getLightView(final int position, View currentView, PHLight currentLight) {
            String lightName = lightNames[position];

            ImageView bulbTop = (ImageView) currentView.findViewById(R.id.bulbTop);
            TextView bulbName = (TextView) currentView.findViewById(R.id.bulbName);
            bulbName.setText(lightName);

            ImageView bulbBottom = (ImageView) currentView.findViewById(R.id.bulbBottom);
            bulbTop.setImageResource(R.drawable.bulb_top);
            bulbBottom.setImageResource(R.drawable.bulb_bottom);
            CheckBox checkBox = (CheckBox) currentView.findViewById(R.id.selectBulbCheck);
            if(checked.contains(position)) {
                checkBox.setChecked(true);
            } else{
                checkBox.setChecked(false);
            }
            if (!currentLight.getLastKnownLightState().isReachable()) {
                bulbBottom.setColorFilter(disabledOverlay);
                bulbTop.setColorFilter(disabledOverlay);
                return currentView;
            } else {
                bulbBottom.clearColorFilter();
            }
            if (!currentLight.getLastKnownLightState().isOn()) {
                bulbTop.setColorFilter(offOverlay);
                return currentView;
            }
            //TODO make work with alternate color formats
            Float x = currentLight.getLastKnownLightState().getX();
            Float y = currentLight.getLastKnownLightState().getY();
            int currentColor = PHUtilities.colorFromXY(new float[]{x, y}, HueBulbChangeUtility.colorXYModelForHue);
            currentColor = Color.argb(300, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
            bulbTop.setColorFilter(currentColor);

            return currentView;
        }

//        private View getGroupView(int position, View currentView, PHGroup currentGroup) {
//            String groupName = groupNames[position];
//            ImageView bulbTop = (ImageView) currentView.findViewById(R.id.bulbTop);
//            TextView bulbName = (TextView) currentView.findViewById(R.id.bulbName);
//            bulbName.setText(groupName);
//
//            ImageView bulbBottom = (ImageView) currentView.findViewById(R.id.bulbBottom);
//            bulbTop.setImageResource(R.drawable.group_top);
//            bulbBottom.setImageResource(R.drawable.group_bottom);
//            if(!HueBulbChangeUtility.isGroupReachable(currentGroup)) {
//                bulbBottom.setColorFilter(disabledOverlay);
//                bulbTop.setColorFilter(disabledOverlay);
//                return currentView;
//            } else {
//                bulbBottom.clearColorFilter();
//            }
//            if(HueBulbChangeUtility.isGroupOff(currentGroup)) {
//                bulbTop.setColorFilter(offOverlay);
//                return currentView;
//            }
//            Integer currentColor = HueBulbChangeUtility.getGroupColor(currentGroup);
//            if(currentColor != null) {
//                currentColor = Color.argb(300, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
//                System.out.println(currentColor);
//                bulbTop.setColorFilter(currentColor);
//            } else {
//                bulbTop.clearColorFilter();
//            }
//            return currentView;
//        }
//    }
    }

}
