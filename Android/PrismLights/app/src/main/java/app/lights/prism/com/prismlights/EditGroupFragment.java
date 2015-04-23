package app.lights.prism.com.prismlights;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHGroup;

public class EditGroupFragment extends Fragment implements CacheUpdateListener {
    private BulbSelectionView bulbSelectionView;
    private TextView name;
    private Button doneButton;
    private boolean done;
    private String groupId;
    private PHHueSDK hueSDK;
    private PHGroup group;
    private Dialog progressDialog;
    private boolean groupEdited;

    public EditGroupFragment() {
        done = false;
        hueSDK = PHHueSDK.getInstance();
        groupEdited = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupId = getArguments().getString(RealHomeFragment.lightPositionString);
        group = hueSDK.getSelectedBridge().getResourceCache().getGroups().get(groupId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.edit_group, container, false);
        TextView title = (TextView) layout.findViewById(R.id.title);
        title.setText(R.string.edit_group);
        bulbSelectionView = (BulbSelectionView) layout.findViewById(R.id.selectBulbView);
        if(bulbSelectionView == null) {
            bulbSelectionView = (BulbSelectionView) layout.findViewById(R.id.selectBulbView);
        }
        bulbSelectionView.allowLongClick(false, null);
        name = (TextView) layout.findViewById(R.id.groupName);
        name.setText(group.getName());
        doneButton = (Button) layout.findViewById(R.id.doneButton);
        bulbSelectionView.setOnCheckedNumberChanged(new CheckedNumberChangedListener() {
            @Override
            public void onCheckedNumberChanged(int checkedNumber) {
                if(checkedNumber > 1) {
                    doneButton.setEnabled(true);
                } else {
                    doneButton.setEnabled(false);
                }
            }
        });
        bulbSelectionView.setSelectedIds(group.getLightIdentifiers());
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!done) {
                    done = true;
                    progressDialog = DialogCreator.showLoadingDialog(getText(R.string.edit_group).toString(), (MainActivity)getActivity());
                    HueBulbChangeUtility.editGroup(groupId, bulbSelectionView.getSelectedLightIds(), new OnCompletedListener() {
                        @Override
                        public void onCompleted() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!groupEdited) {
                                        groupEdited = true;
                                        getFragmentManager().popBackStack();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
        return layout;
    }

    @Override
    public void cacheUpdated() {
        bulbSelectionView.cacheUpdated();
        group = hueSDK.getSelectedBridge().getResourceCache().getGroups().get(groupId);
        name.setText(group.getName());

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}