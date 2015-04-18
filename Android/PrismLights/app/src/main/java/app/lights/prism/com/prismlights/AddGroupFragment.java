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


public class AddGroupFragment extends Fragment implements CacheUpdateListener {

    private BulbSelectionFragment bulbSelectionFragment;
    private EditText nameEditor;
    private Button doneButton;
    private Dialog progressDialog;
    private boolean done;
    private boolean groupCreated;



    public AddGroupFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_add_multi, container, false);
        TextView title = (TextView) layout.findViewById(R.id.title);
        title.setText(R.string.add_group);
        bulbSelectionFragment = (BulbSelectionFragment) getFragmentManager().findFragmentById(R.id.selectBulbFragment);
        if(bulbSelectionFragment == null) {
            bulbSelectionFragment = (BulbSelectionFragment) getChildFragmentManager().findFragmentById(R.id.selectBulbFragment);
        }
        bulbSelectionFragment.allowLongClick(false);
        doneButton = (Button) layout.findViewById(R.id.doneButton);
        bulbSelectionFragment.setOnCheckedNumberChanged(new CheckedNumberChangedListener() {
            @Override
            public void onCheckedNumberChanged(int checkedNumber) {
                if(checkedNumber > 1) {
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
                    HueBulbChangeUtility.createGroup(nameEditor.getText().toString(), bulbSelectionFragment.getSelectedLightIds(), new OnCompletedListener() {
                        @Override
                        public void onCompleted() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!groupCreated) {
                                        groupCreated = true;
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
        bulbSelectionFragment.cacheUpdated();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(bulbSelectionFragment != null) {
            getFragmentManager().beginTransaction().remove(bulbSelectionFragment).commit();
        }
    }
}
