package app.lights.prism.com.prismlights;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Fragment allowing the user to add groups
 */
public class AddGroupFragment extends Fragment implements CacheUpdateListener {

    private BulbSelectionView bulbSelectionView;
    private EditText nameEditor;
    private Button doneButton;
    private Dialog progressDialog;
    private boolean done; //prevents the user from adding the group twice
    private boolean groupCreated; //prevents the callback from the group being created from running twice



    public AddGroupFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_add_multi, container, false);
        TextView title = (TextView) layout.findViewById(R.id.title);
        title.setText(R.string.add_group);
        bulbSelectionView = (BulbSelectionView) layout.findViewById(R.id.selectBulbView);
        if(bulbSelectionView == null) {
            bulbSelectionView = (BulbSelectionView) layout.findViewById(R.id.selectBulbView);
        }
        bulbSelectionView.allowLongClick(false, null);
        doneButton = (Button) layout.findViewById(R.id.doneButton);
        bulbSelectionView.setOnCheckedNumberChanged(new CheckedNumberChangedListener() {
            @Override
            public void onCheckedNumberChanged(int checkedNumber) {
                if (checkedNumber > 1) {
                    doneButton.setEnabled(true);
                } else {
                    doneButton.setEnabled(false);
                }
            }
        });
        nameEditor = (EditText) layout.findViewById(R.id.nameEditor);
        nameEditor.setText(HueBulbChangeUtility.getNextGroupId());
        done = false;
        groupCreated = false;

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!done) {
                    done = true;
                    progressDialog = DialogCreator.showLoadingDialog(getText(R.string.adding_group).toString(), (MainActivity) getActivity());
                    HueBulbChangeUtility.createGroup(nameEditor.getText().toString(), bulbSelectionView.getSelectedLightIds(), new OnCompletedListener() {
                        @Override
                        public void onCompleted() {
                            if(getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!groupCreated) {
                                            groupCreated = true;
                                            if(isVisible()) {
                                                getFragmentManager().popBackStack();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        return layout;
    }

    @Override
    /**
     * Update the selection view from the cache when we recieve a cache update
     */
    public void cacheUpdated() {
        bulbSelectionView.cacheUpdated();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //get rid of the uncancelable dialog if the fragment is dismissed
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
