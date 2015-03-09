package app.lights.prism.com.prismlights;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;

import java.util.List;

public class HomeFragment extends Fragment implements CacheUpdateListener{
//    private OnFragmentInteractionListener mListener;


    private PHHueSDK hueSDK;

    private List<PHLight> currentLights;
    private String[] lightNames;
    private List<PHGroup> currentGroups;
    private String[] groupNames;
    private Integer dragPosition;
    private boolean dragging = false;

    private GridView gridView;

    private static int disabledOverlay = Color.argb(125, 0, 0, 0);
    private static int offOverlay = Color.argb(50, 0, 0, 0);

    public static final String lightPositionString = "CURRENT_BULB_POSITION";
    public static final String groupOrLightString = "GROUP_OR_LIGHT";

    public HomeFragment() {
        hueSDK = PHHueSDK.getInstance();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateFromCache();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_home, container, false);
        gridView= (GridView) frame.findViewById(R.id.homeGridView);
        gridView.setAdapter(new HomeGridAdapter());

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                if(position < currentLights.size()) {
                    HueBulbChangeUtility.toggleBulbState(position);
                } else {
                    HueBulbChangeUtility.toggleBulbGroupState((PHGroup) gridView.getAdapter().getItem(position));
                }
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                if(position < currentLights.size()) {
                    bundle.putInt(lightPositionString, position);
                    bundle.putBoolean(groupOrLightString, false);
                } else {
                    bundle.putInt(lightPositionString, position - currentLights.size());
                    bundle.putBoolean(groupOrLightString, true);
                }
                dragPosition = null;

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                LightSettingsFragment lightSettingFragment = new LightSettingsFragment();
                lightSettingFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, lightSettingFragment);
                fragmentTransaction.addToBackStack("lightsettings");
                fragmentTransaction.commit();

                return false;
            }
        });

        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int newPosition = gridView.pointToPosition((int) event.getX(), (int) event.getY());
                if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                    if(newPosition < currentLights.size() + currentGroups.size()) {
                        dragPosition = newPosition;
                    } else {
                        dragPosition = null;
                    }
                }
                else if(dragPosition != null && newPosition != dragPosition && !dragging) {
                    if(gridView.getChildAt(dragPosition) == null) {
                        dragPosition = null;
                        return false;
                    }
                    View bulbImage = gridView.getChildAt(dragPosition).findViewById(R.id.bulbImage);
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(bulbImage);
                    bulbImage.startDrag(null, shadowBuilder, null, 0);
                    dragging = true;
                }
                return false;
            }
        });
        gridView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if(event.getAction() == DragEvent.ACTION_DROP) {
                    dragging = false;
                    int position = gridView.pointToPosition((int) event.getX(), (int) event.getY());
                    if(position != dragPosition && position >= 0 && position < currentLights.size() + currentGroups.size()) {
                        if(position < currentLights.size() && dragPosition < currentLights.size()) {
                            HueBulbChangeUtility.createGroup(currentLights.get(position), currentLights.get(dragPosition));
                        } else if(position >= currentLights.size() && dragPosition >= currentLights.size()) {
                            HueBulbChangeUtility.createGroup(currentGroups.get(position - currentLights.size()), currentGroups.get(dragPosition - currentLights.size()));
                        } else {
                            if(position >= currentLights.size()) {
                                HueBulbChangeUtility.createGroup(currentLights.get(dragPosition), currentGroups.get(position - currentLights.size()));
                            } else {
                                HueBulbChangeUtility.createGroup(currentLights.get(position), currentGroups.get(dragPosition - currentLights.size()));
                            }
                        }
                    }
                }
                return true;
            }
        });

        return frame;
    }

    @Override
    public void cacheUpdated() {
        updateFromCache();
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
    }

    private void updateFromCache() {
        currentLights = hueSDK.getSelectedBridge().getResourceCache().getAllLights();
        lightNames = hueSDK.getLightNames(currentLights);
        currentGroups = hueSDK.getSelectedBridge().getResourceCache().getAllGroups();
        groupNames = hueSDK.getGroupNames(currentGroups);
    }


    private class HomeGridAdapter extends BaseAdapter {


        public HomeGridAdapter() {
            super();
            updateFromCache();
        }
        @Override
        public int getCount() {
            return currentLights.size() + currentGroups.size();
        }

        @Override
        public Object getItem(int position) {
            if(position < currentLights.size()) {
                return currentLights.get(position);
            } else {
                return currentGroups.get(position - currentLights.size());
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout currentView;
            if(convertView == null) {
                currentView = (LinearLayout) LayoutInflater.from(HomeFragment.this.getActivity()).inflate(R.layout.bulb_view, parent, false);
            } else {
                currentView = (LinearLayout) convertView;
            }
            Object currentLight = getItem(position);
            if(currentLight instanceof PHLight) {
                return getLightView(position, currentView, (PHLight) currentLight);
            } else {
                return getGroupView(position - currentLights.size(), currentView, (PHGroup) currentLight);
            }

        }


        private View getLightView(int position, View currentView, PHLight currentLight) {
            String lightName = lightNames[position];

            ImageView bulbTop = (ImageView) currentView.findViewById(R.id.bulbTop);
            TextView bulbName = (TextView) currentView.findViewById(R.id.bulbName);
            bulbName.setText(lightName);

            ImageView bulbBottom = (ImageView) currentView.findViewById(R.id.bulbBottom);
            bulbTop.setImageResource(R.drawable.bulb_top);
            bulbBottom.setImageResource(R.drawable.bulb_bottom);
            if(!currentLight.getLastKnownLightState().isReachable()) {
                bulbBottom.setColorFilter(disabledOverlay);
                bulbTop.setColorFilter(disabledOverlay);
                return currentView;
            } else {
                bulbBottom.clearColorFilter();
            }
            if(!currentLight.getLastKnownLightState().isOn()) {
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

        private View getGroupView(int position, View currentView, PHGroup currentGroup) {
            String groupName = groupNames[position];
            ImageView bulbTop = (ImageView) currentView.findViewById(R.id.bulbTop);
            TextView bulbName = (TextView) currentView.findViewById(R.id.bulbName);
            bulbName.setText(groupName);

            ImageView bulbBottom = (ImageView) currentView.findViewById(R.id.bulbBottom);
            bulbTop.setImageResource(R.drawable.group_top);
            bulbBottom.setImageResource(R.drawable.group_bottom);
            if(!HueBulbChangeUtility.isGroupReachable(currentGroup)) {
                bulbBottom.setColorFilter(disabledOverlay);
                bulbTop.setColorFilter(disabledOverlay);
                return currentView;
            } else {
                bulbBottom.clearColorFilter();
            }
            if(HueBulbChangeUtility.isGroupOff(currentGroup)) {
                bulbTop.setColorFilter(offOverlay);
                return currentView;
            }
            Integer currentColor = HueBulbChangeUtility.getGroupColor(currentGroup);
            if(currentColor != null) {
                currentColor = Color.argb(300, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
                System.out.println(currentColor);
                bulbTop.setColorFilter(currentColor);
            } else {
                bulbTop.clearColorFilter();
            }
            return currentView;
        }
    }

}
