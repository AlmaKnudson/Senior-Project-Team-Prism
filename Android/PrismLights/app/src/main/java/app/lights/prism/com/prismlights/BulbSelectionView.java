package app.lights.prism.com.prismlights;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A view to be used to select from all the bulbs on the bridge
 */
public class BulbSelectionView extends FrameLayout implements CacheUpdateListener{
    private GridView gridView;
    private List<String> currentLightIdOrder; //the current ordering of the lights
    private Map<String, PHLight> currentLights; //the current lights on the bridge
    private PHHueSDK hueSDK;
    private Set<String> checked;//the ids of the bulbs who are checked
    private boolean allChecked;//true if select all is checked
    private CheckBox selectAllCheckBox;
    private CheckedNumberChangedListener checkedNumberChangedListener;
    private LayoutIdOrder layoutIdOrder; //data model representing the order of the bulbs/groups

    public BulbSelectionView(Context context) {
        super(context);
        construct();
    }

    public BulbSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public BulbSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }

    /**
     * Creates the view from a layout and setups the checked collection
     */
    private void construct() {
        LayoutInflater.from(getContext()).inflate(R.layout.fragment_bulb_selection, this);
        layoutIdOrder = LayoutIdOrder.getInstance(getContext().getFilesDir());
        hueSDK = PHHueSDK.getInstance();
        checked = new HashSet<String>();
        allChecked = false;
        gridView = (GridView) findViewById(R.id.selectionGridView);
        gridView.setAdapter(new SelectGridAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HueBulbChangeUtility.setBulbAlertState(currentLightIdOrder.get(position), true);
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.selectBulbCheck);
                changeChecked(checkBox, currentLightIdOrder.get(position));
            }
        });
        selectAllCheckBox = (CheckBox) findViewById(R.id.selectAllCheckBox);
        selectAllCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allChecked) {
                    allChecked = false;
                    selectAllCheckBox.setChecked(false);
                } else {
                    selectAllCheckBox.setChecked(true);
                    allChecked = true;
                    checked.addAll(currentLightIdOrder);
                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                }
                callCheckedNumberChangedListener();
            }
        });
    }

    /**
     * If the checkbox is checked, unchecks it, otherwise checks it
     * Also updates the model
     * @param checkBox the checkbox in question
     * @param id the id of the element associated with the checkbox
     */
    private void changeChecked(CheckBox checkBox, String id) {
        if(checked.contains(id)) {
            checked.remove(id);
            checkBox.setChecked(false);
            selectAllCheckBox.setChecked(false);
            allChecked = false;
        } else {
            checked.add(id);
            checkBox.setChecked(true);
        }
        callCheckedNumberChangedListener();
    }

    @Override
    /**
     * Updates the bulbs so they show the correct colors/state
     */
    public void cacheUpdated() {
        updateFromCache();
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();

    }

    /**
     * Updates the bulbs' state from the cache and their order from the model
     */
    private void updateFromCache() {
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getLights();
        currentLightIdOrder = layoutIdOrder.getLightsFromBridgeOrder(currentLights.keySet());
    }

    /**
     *
     * @param shouldAllowLongClick if true, sets up the view to allow the user to edit the bulb's state
     * @param fragmentManager can be null if shouldAllowLongClick is false
     */
    public void allowLongClick(boolean shouldAllowLongClick, FragmentManager fragmentManager) {
        if(gridView != null) {
            if(shouldAllowLongClick) {
                setGridViewLongClickListener(fragmentManager);
            } else {
                gridView.setOnItemLongClickListener(null);
            }
        }
    }

    /**
     * Sets up the grid view to allow the user to edit the bulb's state
     * @param fragmentManager the fragment manager to use to open the settings view
     */
    private void setGridViewLongClickListener(final FragmentManager fragmentManager) {
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString(RealHomeFragment.lightPositionString, currentLightIdOrder.get(position));
                bundle.putBoolean(RealHomeFragment.groupOrLightString, false);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                LightSettingsFragment lightSettingFragment = new LightSettingsFragment();
                lightSettingFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, lightSettingFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                return false;
            }
        });
    }

    /**
     * @return the light ids of the bulbs that are selected
     */
    public List<String> getSelectedLightIds() {
        return new ArrayList<String>(checked);
    }

    /**
     * @return true if select all is checked, false otherwise
     */
    public boolean getAllChecked() {
        return allChecked;
    }

    /**
     *
     * @return true if all the bulbs have the same color, on, and brightness state, false otherwise
     */
    public PHLightState allBulbsSameState() {
        PHLightState returnState = null;
        float[] currentColor = new float[2];
        for(String id: checked) {
            PHLightState lightState = currentLights.get(id).getLastKnownLightState();
            if(returnState == null) {
                returnState = new PHLightState();
                returnState.setX(lightState.getX());
                returnState.setY(lightState.getY());
                returnState.setOn(lightState.isOn());
                returnState.setBrightness(lightState.getBrightness());
                currentColor[0] = returnState.getX();
                currentColor[1] = returnState.getY();
            } else {
                if(!HueBulbChangeUtility.colorsEqual(currentColor, new float[]{lightState.getX(), lightState.getY()}) ||
                        ((returnState.getBrightness() != null && returnState.getBrightness().equals(lightState.getBrightness())) ||
                                returnState.getBrightness() == null && lightState.getBrightness() != null) ||
                        (returnState.isOn() != null && returnState.isOn().equals(lightState.isOn())) ||
                        returnState.isOn() == null && lightState.isOn() != null) {
                    return null;
                }
            }
        }
        return returnState;
    }

    /**
     * Allows the setting of a listener for when the bulbs that are selected change
     * @param checkedNumberChangedListener
     */
    public void setOnCheckedNumberChanged(CheckedNumberChangedListener checkedNumberChangedListener) {
        this.checkedNumberChangedListener = checkedNumberChangedListener;
    }

    /**
     * Safely calls the checked number changed listener, checking if it's null
     */
    private void callCheckedNumberChangedListener() {
        if(checkedNumberChangedListener != null) {
            checkedNumberChangedListener.onCheckedNumberChanged(checked.size());
        }
    }

    /**
     * Slects all the bulbs and sets the select all checkbox to true
     */
    public void selectAll() {
        selectAllCheckBox.setChecked(true);
        allChecked = true;
        checked.addAll(currentLightIdOrder);
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
        callCheckedNumberChangedListener();
    }

    /**
     * Sets the ids given to checked
     * @param ids
     */
    public void setSelectedIds(Collection<String> ids) {
        this.checked.addAll(ids);
        if(checked.size() == gridView.getAdapter().getCount()) {
            allChecked = true;
            selectAllCheckBox.setChecked(true);
        }
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
        callCheckedNumberChangedListener();
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
            return currentLights.get(currentLightIdOrder.get(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        /**
         * Returns a view a stateful bulb with a checkbox
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            View currentView;
            if(convertView == null) {
                currentView = LayoutInflater.from(BulbSelectionView.this.getContext()).inflate(R.layout.bulb_select_view, parent, false);
            } else {
                currentView = convertView;
            }
            PHLight currentLight = (PHLight) getItem(position);

            String lightName = currentLight.getName();

            ImageView bulbTop = (ImageView) currentView.findViewById(R.id.bulbTop);
            TextView bulbName = (TextView) currentView.findViewById(R.id.bulbName);
            bulbName.setText(lightName);

            ImageView bulbBottom = (ImageView) currentView.findViewById(R.id.bulbBottom);
            CheckBox checkBox = (CheckBox) currentView.findViewById(R.id.selectBulbCheck);
            if(checked.contains(currentLightIdOrder.get(position))) {
                checkBox.setChecked(true);
            } else{
                checkBox.setChecked(false);
            }
            if(!currentLight.getLastKnownLightState().isReachable()) {
                bulbBottom.setColorFilter(RealHomeFragment.disabledOverlay);
                bulbTop.setColorFilter(RealHomeFragment.disabledOverlay);
                currentView.findViewById(R.id.warning).setVisibility(View.VISIBLE);
                return currentView;
            } else {
                bulbBottom.clearColorFilter();
                currentView.findViewById(R.id.warning).setVisibility(View.GONE);
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


