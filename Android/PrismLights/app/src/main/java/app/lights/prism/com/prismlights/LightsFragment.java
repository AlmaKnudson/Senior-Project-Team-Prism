package app.lights.prism.com.prismlights;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHLight;

import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class LightsFragment extends Fragment implements CacheUpdateListener, EditButtonPresentCaller {


    private GridView gridView;
    private List<String> currentLightIdOrder;
    private Map<String, PHLight> currentLights;
    private PHHueSDK hueSDK;
    private LayoutIdOrder layoutIdOrder;
    private EditButtonPresentListener editButtonPresentListener;



    public LightsFragment() {
        hueSDK = PHHueSDK.getInstance();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        layoutIdOrder = LayoutIdOrder.getInstance(activity.getFilesDir());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_lights, container, false);
        gridView= (GridView) layout.findViewById(R.id.homeGridView);
        updateFromCache();
        gridView.setAdapter(new LightViewAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                    HueBulbChangeUtility.toggleBulbState((PHLight) gridView.getAdapter().getItem(position), (MainActivity)getActivity());
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString(RealHomeFragment.lightPositionString, currentLightIdOrder.get(position));

                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                LightSettingsFragment lightSettingFragment = new LightSettingsFragment();
                lightSettingFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, lightSettingFragment);
                fragmentTransaction.addToBackStack("lightsettings");
                fragmentTransaction.commit();

                return false;
            }
        });
        return layout;
    }

    /**
     * Calls the edit button present listener after checking for null
     */
    private void callEditButtonPresentListener(boolean shown) {
        if(editButtonPresentListener != null) {
            editButtonPresentListener.editButtonPresent(shown);
        }
    }

    @Override
    public void cacheUpdated() {
        updateFromCache();
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        super.onResume();
        updateFromCache();
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();

    }

    /**
     * Updates the current lights and their id order from the cache, calling the edit button present listener
     * if the number of lights has changed
     */
    private void updateFromCache() {
        //invalid initial value
        int lastSize = -1;
        if(currentLightIdOrder != null) {
            lastSize = currentLightIdOrder.size();
        }
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getLights();
        currentLightIdOrder = layoutIdOrder.getLightsFromBridgeOrder(currentLights.keySet());
        //if there are no lights it should update when initializing because 0 != -1.
        // It should also update at any other time
        if(lastSize != currentLightIdOrder.size()) {
            if (currentLightIdOrder.size() < 2) {
                callEditButtonPresentListener(false);
            } else {
                callEditButtonPresentListener(true);
            }
        }
    }

    public void setEditButtonPresentListener(EditButtonPresentListener editButtonPresentListener) {
        this.editButtonPresentListener = editButtonPresentListener;
    }

    @Override
    public boolean shouldEditButtonBePresent() {
        if(currentLightIdOrder != null) {
            return currentLightIdOrder.size() > 1;
        } else {
            return false;
        }
    }


    private class LightViewAdapter extends BaseAdapter {


        public LightViewAdapter() {
            super();
        }
        @Override
        public int getCount() {
            return currentLights.size();
        }

        @Override
        public Object getItem(int position) {
            return currentLights.get(currentLightIdOrder.get(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout currentView;
            if(convertView == null) {
                currentView = (LinearLayout) LayoutInflater.from(LightsFragment.this.getActivity()).inflate(R.layout.bulb_view, parent, false);
            } else {
                currentView = (LinearLayout) convertView;
            }
            PHLight currentLight = (PHLight) getItem(position);

            String lightName = currentLight.getName();

            ImageView bulbTop = (ImageView) currentView.findViewById(R.id.bulbTop);
            TextView bulbName = (TextView) currentView.findViewById(R.id.bulbName);
            bulbName.setText(lightName);

            ImageView bulbBottom = (ImageView) currentView.findViewById(R.id.bulbBottom);
            if(!currentLight.getLastKnownLightState().isReachable()) {
                bulbBottom.setColorFilter(RealHomeFragment.disabledOverlay);
                bulbTop.setColorFilter(RealHomeFragment.disabledOverlay);
                currentView.findViewById(R.id.warning).setVisibility(View.VISIBLE);
                return currentView;
            } else {
                currentView.findViewById(R.id.warning).setVisibility(View.GONE);
                bulbBottom.clearColorFilter();
            }
            if(!currentLight.getLastKnownLightState().isOn()) {
                bulbTop.setColorFilter(RealHomeFragment.offOverlay);
                return currentView;
            }
            Float x = currentLight.getLastKnownLightState().getX();
            Float y = currentLight.getLastKnownLightState().getY();
            int currentColor = PHUtilities.colorFromXY(new float[]{x, y}, HueBulbChangeUtility.COLOR_XY_MODEL_FOR_HUE);
            currentColor = Color.argb(300, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
            bulbTop.setColorFilter(currentColor);
            return currentView;
        }
    }

}

