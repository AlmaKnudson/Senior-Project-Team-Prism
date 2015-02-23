package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.ListFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class SettingsFragment extends Fragment {

//    private OnFragmentInteractionListener interactionListener;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.settings_fragment, container);
        Button bridgeSearch = (Button) layout.findViewById(R.id.searchBridgeButton);
        bridgeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)(SettingsFragment.this.getActivity())).searchForBridge();
            }
        });
        return layout;
    }

}
