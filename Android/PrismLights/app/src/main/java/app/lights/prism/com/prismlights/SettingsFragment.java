package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.ListFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHAccessPoint;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

//    private OnFragmentInteractionListener interactionListener;

    private List<PHAccessPoint> accessPoints;
    private ListView bridgeList;
    public SettingsFragment() {
        accessPoints = new ArrayList<PHAccessPoint>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.settings_fragment, container, false);
        final Button bridgeSearch = (Button) layout.findViewById(R.id.searchBridgeButton);
        bridgeList = (ListView) layout.findViewById(R.id.bridgeList);
        bridgeList.setAdapter(new BridgeListAdapter());
        bridgeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)(SettingsFragment.this.getActivity())).searchForBridge();
            }
        });
        bridgeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity)getActivity()).connectToAccessPoint((PHAccessPoint)bridgeList.getAdapter().getItem(position));
            }
        });
        return layout;
    }

    public void setAccessPoints(List<PHAccessPoint> accessPoints) {
        this.accessPoints.clear();
        this.accessPoints.addAll(accessPoints);
        ((BaseAdapter) bridgeList.getAdapter()).notifyDataSetChanged();
    }

    private class BridgeListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return accessPoints.size();
        }

        @Override
        public Object getItem(int position) {
            return accessPoints.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PHAccessPoint accessPoint = (PHAccessPoint) getItem(position);
            View currentView;
            if(convertView == null) {
                currentView = LayoutInflater.from(SettingsFragment.this.getActivity()).inflate(R.layout.access_point_view, parent, false);
            } else {
                currentView = convertView;
            }
            TextView macAddressView = (TextView) currentView.findViewById(R.id.macAddressText);
            macAddressView.setText("MAC Address: " + accessPoint.getMacAddress());
            TextView ipAddressView = (TextView) currentView.findViewById(R.id.ipAddressText);
            ipAddressView.setText("IP Address: " + accessPoint.getIpAddress());
            return currentView;
        }
    }
}
