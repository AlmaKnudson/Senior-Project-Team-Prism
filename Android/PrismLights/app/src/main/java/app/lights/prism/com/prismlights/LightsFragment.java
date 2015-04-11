package app.lights.prism.com.prismlights;


import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class LightsFragment extends Fragment implements CacheUpdateListener {


    private GridView gridView;
    private List<String> currentLightIdOrder;
    private Map<String, PHLight> currentLights;
    private PHHueSDK hueSDK;



    public LightsFragment() {
        hueSDK = PHHueSDK.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_lights, container, false);
        gridView= (GridView) layout.findViewById(R.id.homeGridView);
        gridView.setAdapter(new LightViewAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                int itemViewType = gridView.getAdapter().getItemViewType(position);
                if (itemViewType == RealHomeFragment.NORMAL_VIEW) {
                    HueBulbChangeUtility.toggleBulbState((PHLight) gridView.getAdapter().getItem(position));
                }
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                if (gridView.getAdapter().getItemViewType(position) == RealHomeFragment.NORMAL_VIEW) {
                    Bundle bundle = new Bundle();
                    bundle.putString(RealHomeFragment.lightPositionString, currentLightIdOrder.get(position));
                    bundle.putBoolean(RealHomeFragment.groupOrLightString, false);

                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                    LightSettingsFragment lightSettingFragment = new LightSettingsFragment();
                    lightSettingFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.container, lightSettingFragment);
                    fragmentTransaction.addToBackStack("lightsettings");
                    fragmentTransaction.commit();
                }

                return false;
            }
        });
        return layout;
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

    private void updateFromCache() {
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getLights();
        currentLightIdOrder = new ArrayList<String>(currentLights.keySet());
        HueBulbChangeUtility.sortIds(currentLightIdOrder);
    }


    private class LightViewAdapter extends BaseAdapter {


        public LightViewAdapter() {
            super();
            updateFromCache();
        }
        @Override
        public int getCount() {
            return currentLights.size() + 2;
        }

        @Override
        public Object getItem(int position) {
            if(getItemViewType(position) == RealHomeFragment.NORMAL_VIEW) {
                return currentLights.get(currentLightIdOrder.get(position));
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == currentLights.size() + 1) {
                return RealHomeFragment.EDIT_BUTTON;
            } else if (position == currentLights.size()) {
                return RealHomeFragment.PLUS_BUTTON;
            } else {
                return RealHomeFragment.NORMAL_VIEW;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(getItemViewType(position) == RealHomeFragment.EDIT_BUTTON) {
                View editView =  LayoutInflater.from(LightsFragment.this.getActivity()).inflate(R.layout.edit_view, parent, false);
                TextView editText = (TextView) editView.findViewById(R.id.editLabel);
                editText.setText(getText(R.string.edit_lights));
                return editView;
            }
            else if(getItemViewType(position) == RealHomeFragment.PLUS_BUTTON) {
                View addView =  LayoutInflater.from(LightsFragment.this.getActivity()).inflate(R.layout.add_view, parent, false);
                TextView addText = (TextView) addView.findViewById(R.id.addLabel);
                addText.setText(getText(R.string.add_light));
                return addView;
            }
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
                return currentView;
            } else {
                bulbBottom.clearColorFilter();
            }
            if(!currentLight.getLastKnownLightState().isOn()) {
                bulbTop.setColorFilter(RealHomeFragment.offOverlay);
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
    }

}

