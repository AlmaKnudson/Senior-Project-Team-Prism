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

public class BulbSelectionView extends FrameLayout implements CacheUpdateListener{
    private GridView gridView;
    private List<String> currentLightIdOrder;
    private Map<String, PHLight> currentLights;
    private PHHueSDK hueSDK;
    private Set<String> checked;
    private boolean allChecked;
    private CheckBox selectAllCheckBox;
    private CheckedNumberChangedListener checkedNumberChangedListener;

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

    private void construct() {
        LayoutInflater.from(getContext()).inflate(R.layout.fragment_bulb_selection, this);
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
    public void cacheUpdated() {
        updateFromCache();
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();

    }

    private void updateFromCache() {
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getLights();
        currentLightIdOrder = new ArrayList<String>(currentLights.keySet());
        HueBulbChangeUtility.sortIds(currentLightIdOrder);
    }

    public void allowLongClick(boolean shouldAllowLongClick, FragmentManager fragmentManager) {
        if(gridView != null) {
            if(shouldAllowLongClick) {
                setGridViewLongClickListener(fragmentManager);
            } else {
                gridView.setOnItemLongClickListener(null);
            }
        }
    }

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

    public List<String> getSelectedLightIds() {
        return new ArrayList<String>(checked);
    }

    public boolean getAllChecked() {
        return allChecked;
    }

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

    public void setOnCheckedNumberChanged(CheckedNumberChangedListener checkedNumberChangedListener) {
        this.checkedNumberChangedListener = checkedNumberChangedListener;
    }

    private void callCheckedNumberChangedListener() {
        if(checkedNumberChangedListener != null) {
            checkedNumberChangedListener.onCheckedNumberChanged(checked.size());
        }
    }

    public void selectAll() {
        selectAllCheckBox.setChecked(true);
        allChecked = true;
        checked.addAll(currentLightIdOrder);
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
        callCheckedNumberChangedListener();
    }

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


