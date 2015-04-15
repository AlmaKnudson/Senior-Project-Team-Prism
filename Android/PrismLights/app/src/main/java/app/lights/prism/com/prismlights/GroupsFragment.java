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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupsFragment extends Fragment implements CacheUpdateListener {

    private GridView gridView;
    private List<String> currentGroupIdOrder;
    private Map<String, PHGroup> currentGroups;
    private PHHueSDK hueSDK;

    public GroupsFragment() {
        hueSDK = PHHueSDK.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_lights, container, false);
        gridView= (GridView) layout.findViewById(R.id.homeGridView);
        gridView.setAdapter(new GroupViewAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                HueBulbChangeUtility.toggleBulbGroupState((PHGroup) gridView.getAdapter().getItem(position));
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getActivity(), "" + position+" is clicked", Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString(RealHomeFragment.lightPositionString, currentGroupIdOrder.get(position));
                bundle.putBoolean(RealHomeFragment.groupOrLightString, true);

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

    @Override
    public void cacheUpdated() {
        updateFromCache();
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();

    }

    private void updateFromCache() {
        currentGroups = hueSDK.getSelectedBridge().getResourceCache().getGroups();
        currentGroupIdOrder = new ArrayList<String>(currentGroups.keySet());
        HueBulbChangeUtility.sortIds(currentGroupIdOrder);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFromCache();
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
    }

    private class GroupViewAdapter extends BaseAdapter {


        public GroupViewAdapter() {
            super();
            updateFromCache();
        }
        @Override
        public int getCount() {
            return currentGroups.size();
        }

        @Override
        public Object getItem(int position) {
            return currentGroups.get(currentGroupIdOrder.get(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View currentView;
            if(convertView == null) {
                currentView = LayoutInflater.from(GroupsFragment.this.getActivity()).inflate(R.layout.group_view, parent, false);
            } else {
                currentView =convertView;
            }
            PHGroup currentGroup = (PHGroup) getItem(position);

            String lightName = currentGroup.getName();

            ImageView group2Top = (ImageView) currentView.findViewById(R.id.group2Top);
            ImageView group2Bottom = (ImageView) currentView.findViewById(R.id.group2Bottom);
            ImageView group3Top = (ImageView) currentView.findViewById(R.id.group3Top);
            ImageView group3Bottom = (ImageView) currentView.findViewById(R.id.group3Bottom);
            TextView groupName = (TextView) currentView.findViewById(R.id.groupName);
            groupName.setText(lightName);
            ImageView groupTop;
            ImageView groupBottom;
            if(currentGroup.getLightIdentifiers().size() > 2) {
                group2Top.setVisibility(View.INVISIBLE);
                group2Bottom.setVisibility(View.INVISIBLE);
                group3Top.setVisibility(View.VISIBLE);
                group3Bottom.setVisibility(View.VISIBLE);
                groupTop = group3Top;
                groupBottom = group3Bottom;
            } else {
                group3Bottom.setVisibility(View.INVISIBLE);
                group3Top.setVisibility(View.INVISIBLE);
                group2Bottom.setVisibility(View.VISIBLE);
                group2Top.setVisibility(View.VISIBLE);
                groupTop = group2Top;
                groupBottom = group2Bottom;
            }
            if(!HueBulbChangeUtility.isGroupReachable(currentGroup)) {
                groupBottom.setColorFilter(RealHomeFragment.disabledOverlay);
                groupTop.setColorFilter(RealHomeFragment.disabledOverlay);
                return currentView;
            } else {
                groupBottom.clearColorFilter();
            }
            if(HueBulbChangeUtility.isGroupOff(currentGroup)) {
                groupTop.setColorFilter(RealHomeFragment.offOverlay);
                return currentView;
            }
            Integer currentColor = HueBulbChangeUtility.getGroupColor(currentGroup);
            if(currentColor != null) {
                currentColor = Color.argb(300, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
                System.out.println(currentColor);
                groupTop.setColorFilter(currentColor);
            } else {
                groupTop.clearColorFilter();
            }
            return currentView;
        }
    }


}
