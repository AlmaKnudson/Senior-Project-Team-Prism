package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupsEditFragment extends Fragment implements OnItemShiftedListener {
    private ReorderGridView gridView;
    private List<String> currentGroupIdOrder;
    private Map<String, PHGroup> currentGroups;
    private PHHueSDK hueSDK;
    private Set<String> checked;
    private Dialog progressDialog;
    private LayoutIdOrder layoutIdOrder;


    public GroupsEditFragment() {
        hueSDK = PHHueSDK.getInstance();
        checked = new HashSet<String>();
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
        gridView.setAdapter(new GroupViewAdapter());
        gridView.setOnItemShiftedListener(this);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateChecked((CheckBox) view.findViewById(R.id.selectCheck), ((PHGroup)gridView.getAdapter().getItem(position)).getIdentifier());
            }
        });
        layout.findViewById(R.id.trashButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress);
                    TextView progressText = (TextView) progressDialog.findViewById(R.id.progressText);
                    progressText.setText(getText(R.string.adding_group));
                } else {
                    progressDialog.show();
                }
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                HueBulbChangeUtility.deleteGroups(checked, new OnCompletedListener() {
                    @Override
                    public void onCompleted() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog.isShowing()) {
                                    progressDialog.hide();
                                    updateFromCache();
                                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                                }
                            }
                        });
                    }
                });
            }
        });
        layout.findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        return layout;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        layoutIdOrder.saveToFile();
    }

    private void updateChecked(CheckBox checkBox, String id) {
        if(checked.contains(id)) {
            checked.remove(id);
            checkBox.setChecked(false);
        } else {
            checked.add(id);
            checkBox.setChecked(true);
        }
    }

    @Override
    public void onItemShifted(int shiftedFrom, int shiftedTo) {
        currentGroupIdOrder = layoutIdOrder.updateGroupIdOrder(shiftedFrom, shiftedTo);
        ((BaseAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }


    private void updateFromCache() {
        currentGroups = hueSDK.getSelectedBridge().getResourceCache().getGroups();
        currentGroupIdOrder = layoutIdOrder.getGroupsFromBridgeOrder(currentGroups.keySet());
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
                currentView = LayoutInflater.from(GroupsEditFragment.this.getActivity()).inflate(R.layout.group_view_select, parent, false);
            } else {
                currentView =convertView;
            }
            PHGroup currentGroup = (PHGroup) getItem(position);

            String lightName = currentGroup.getName();
            ((CheckBox) currentView.findViewById(R.id.selectCheck)).setChecked(false);

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
                currentView.findViewById(R.id.warning).setVisibility(View.VISIBLE);
                return currentView;
            } else {
                currentView.findViewById(R.id.warning).setVisibility(View.GONE);
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


