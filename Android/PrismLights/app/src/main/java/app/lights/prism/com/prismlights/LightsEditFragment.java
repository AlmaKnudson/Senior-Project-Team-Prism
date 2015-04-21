package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHLight;

import java.util.List;
import java.util.Map;

public class LightsEditFragment extends Fragment implements OnItemShiftedListener {
    private ReorderGridView gridView;
    private List<String> currentLightIdOrder;
    private Map<String, PHLight> currentLights;
    private PHHueSDK hueSDK;
    private LayoutIdOrder layoutIdOrder;



    public LightsEditFragment() {
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
        View layout = inflater.inflate(R.layout.fragment_edit_list, container, false);
        gridView = (ReorderGridView) layout.findViewById(R.id.reorderGridView);
        gridView.setAdapter(new LightViewAdapter());
        gridView.setOnItemShiftedListener(this);
        layout.findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        layout.findViewById(R.id.trashButton).setVisibility(View.GONE);
        return layout;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        layoutIdOrder.saveToFile();
    }

    @Override
    public void onItemShifted(int shiftedFrom, int shiftedTo) {
        currentLightIdOrder = layoutIdOrder.updateLightIdOrder(shiftedFrom, shiftedTo);
        ((BaseAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }


    private void updateFromCache() {
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getLights();
        currentLightIdOrder = layoutIdOrder.getLightsFromBridgeOrder(currentLights.keySet());
    }

    private class LightViewAdapter extends BaseAdapter {


        public LightViewAdapter() {
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
            LinearLayout currentView;
            if(convertView == null) {
                currentView = (LinearLayout) LayoutInflater.from(LightsEditFragment.this.getActivity()).inflate(R.layout.bulb_view, parent, false);
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
            int currentColor = PHUtilities.colorFromXY(new float[]{x, y}, HueBulbChangeUtility.COLOR_XY_MODEL_FOR_HUE);
            currentColor = Color.argb(300, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
            bulbTop.setColorFilter(currentColor);
//            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.moving_light);
//            animation.setRepeatCount(Animation.INFINITE);
//            animation.setRepeatMode(Animation.REVERSE);
//            currentView.findViewById(R.id.bulbImage).startAnimation(animation);
            return currentView;
        }
    }
}
