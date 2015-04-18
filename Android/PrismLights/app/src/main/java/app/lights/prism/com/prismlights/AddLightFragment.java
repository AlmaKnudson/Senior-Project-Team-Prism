package app.lights.prism.com.prismlights;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class AddLightFragment extends Fragment {

    private List<String> serialCodes;
    private ListView serialCodeListView;
    private Button searchButton;
    private ImageButton plusSerialCodeButton;
    private Dialog addSerialCodeDialog;
    private Button cancelButton;
    private Button doneButton;
    private EditText serialCode;
    private boolean searchCompleted;
    private boolean searching;
    private ProgressDialog progressDialog;

    public AddLightFragment() {
        serialCodes = new ArrayList<String>();
        searchCompleted = false;
        searching = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_add_light, container, false);
        serialCodeListView = (ListView) layout.findViewById(R.id.serialCodeListView);
        serialCodeListView.setAdapter(new SerialCodeListAdapter());
        searchButton = (Button) layout.findViewById(R.id.searchLightsButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!searching) {
                    if (progressDialog == null) {
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.show();
                        progressDialog.setContentView(R.layout.progress);
                        TextView progressText = (TextView) progressDialog.findViewById(R.id.progressText);
                        progressText.setText(getText(R.string.searching_for_new_lights));
                    } else {
                        progressDialog.show();
                    }
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    OnCompletedListener onCompletedListener = new OnCompletedListener() {
                        @Override
                        public void onCompleted() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                if (!searchCompleted) {
                                    searchCompleted = true;
                                    getFragmentManager().popBackStack();
                                }
                                }
                            });
                        }
                    };
                    if (serialCodes.isEmpty()) {
                        //automatic search
                        HueBulbChangeUtility.searchForLights(onCompletedListener);
                    } else {
                        HueBulbChangeUtility.searchForLights(serialCodes, onCompletedListener);
                    }
                }
            }
        });
        plusSerialCodeButton = (ImageButton) layout.findViewById(R.id.addSerialCodeButton);
        plusSerialCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addSerialCodeDialog == null) {
                    addSerialCodeDialog = new ProgressDialog(getActivity());
                    addSerialCodeDialog.show();
                    addSerialCodeDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    addSerialCodeDialog.setContentView(R.layout.enter_text_dialog);
                    TextView dialogTitle = (TextView) addSerialCodeDialog.findViewById(R.id.dialog_title);
                    dialogTitle.setText(getText(R.string.add_serial_code));
                    serialCode = (EditText) addSerialCodeDialog.findViewById(R.id.editText);
                    addSerialCodeDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    doneButton = (Button) addSerialCodeDialog.findViewById(R.id.doneButton);
                    serialCode.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if(serialCode.getText().length() == 6) {
                                doneButton.setEnabled(true);
                            } else {
                                doneButton.setEnabled(false);
                            }
                        }
                    });
                    doneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(addSerialCodeDialog.isShowing()) {
                                serialCodes.add(serialCode.getText().toString());
                                serialCode.setText("");
                                doneButton.setEnabled(false);
                                ((BaseAdapter) serialCodeListView.getAdapter()).notifyDataSetChanged();
                                addSerialCodeDialog.hide();
                            }

                        }
                    });
                    cancelButton = (Button) addSerialCodeDialog.findViewById(R.id.cancelButton);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(addSerialCodeDialog.isShowing()) {
                                serialCode.setText("");
                                addSerialCodeDialog.hide();
                            }
                        }
                    });
                } else {
                    addSerialCodeDialog.show();
                }
            }
        });
        return layout;
    }

    private class SerialCodeListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return serialCodes.size();
        }

        @Override
        public Object getItem(int position) {
            return serialCodes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View currentView;
            if(convertView == null) {
                currentView =  LayoutInflater.from(AddLightFragment.this.getActivity()).inflate(R.layout.normal_text_view, parent, false);
            } else {
                currentView = convertView;
            }
            TextView textView = (TextView) currentView.findViewById(R.id.textView);
            textView.setText((String)getItem(position));
            return currentView;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
