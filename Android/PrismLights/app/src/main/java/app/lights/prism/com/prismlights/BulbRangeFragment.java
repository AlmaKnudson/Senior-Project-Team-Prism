package app.lights.prism.com.prismlights;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHSchedule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by AlmaKnudson on 4/14/15.
 */
public class BulbRangeFragment extends Fragment {

    private ListView bulbsListView;
    private List<String> lightIdentifiers;
    private Map<String, PHLight> map;

    private HashMap<String, String> lightRangeMap;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_bulb_range, container, false);
        lightRangeMap = LightRangeMap.getLightRangeMap().getMap();





        map = HueBulbChangeUtility.getLightMap();

        int count = 0;
//        lightIdentifiers = new String[map.keySet().size()];
        lightIdentifiers = new ArrayList<String>();
        for(String s : map.keySet()){
           if( (map.get(s)).getLastKnownLightState().isReachable()){
               lightIdentifiers.add(s);
           }
        }


        bulbsListView = (ListView) layout.findViewById(R.id.bulbsListView);

        bulbsListView.setAdapter(new SerialCodeListAdapter());

//        lightIdentifiers = bridge.getResourceCache().getLights()
//        lightIdentifiers = (String[]) HueBulbChangeUtility.getLightIdentifiers().toArray();

        return layout;
    }



    private class SerialCodeListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return lightIdentifiers.size();
        }

        @Override
        public Object getItem(int position) {
            String key = lightIdentifiers.get(position);
            Object value = map.get(key).getName();
            return value;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            final View currentView;
            if(convertView == null) {
                currentView =  LayoutInflater.from(BulbRangeFragment.this.getActivity()).inflate(R.layout.bulb_range_selection_view, parent, false);
            } else {
                currentView = convertView;
            }

            ToggleButton none;
            ToggleButton low;
            ToggleButton mid;
            ToggleButton high;

            View.OnClickListener rangeListener = new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    //http://stackoverflow.com/questions/5004013/get-other-child-that-shares-parent-with-the-current-view
                    ViewGroup row = (ViewGroup) v.getParent();
                    TextView textView = (TextView) row.findViewById(R.id.textView);
                    switch (v.getId()) {
                        case R.id.noneButton:
//                            System.out.println("NONE SELECTED");

                            if(lightRangeMap.containsKey(textView.getText().toString())){
                                lightRangeMap.remove(textView.getContentDescription().toString());
                            }
                            ((ToggleButton) row.findViewById(R.id.lowsButton)).setChecked(false);
                            ((ToggleButton) row.findViewById(R.id.midsButton)).setChecked(false);
                            ((ToggleButton) row.findViewById(R.id.highsButton)).setChecked(false);
                            break;
                        case R.id.lowsButton:
                            lightRangeMap.put(textView.getContentDescription().toString(), "LOW");
                            ((ToggleButton) row.findViewById(R.id.noneButton)).setChecked(false);
                            ((ToggleButton) row.findViewById(R.id.midsButton)).setChecked(false);
                            ((ToggleButton) row.findViewById(R.id.highsButton)).setChecked(false);
                            break;
                        case R.id.midsButton:
                            lightRangeMap.put(textView.getContentDescription().toString(), "MID");
                            ((ToggleButton) row.findViewById(R.id.lowsButton)).setChecked(false);
                            ((ToggleButton) row.findViewById(R.id.noneButton)).setChecked(false);
                            ((ToggleButton) row.findViewById(R.id.highsButton)).setChecked(false);
                            break;
                        case R.id.highsButton:
                            lightRangeMap.put(textView.getContentDescription().toString(), "HIGH");
                            ((ToggleButton) row.findViewById(R.id.lowsButton)).setChecked(false);
                            ((ToggleButton) row.findViewById(R.id.midsButton)).setChecked(false);
                            ((ToggleButton) row.findViewById(R.id.noneButton)).setChecked(false);
                            break;
                    }
                }
            };
            none = (ToggleButton) currentView.findViewById(R.id.noneButton);
            low = (ToggleButton) currentView.findViewById(R.id.lowsButton);
            mid = (ToggleButton) currentView.findViewById(R.id.midsButton);
            high = (ToggleButton) currentView.findViewById(R.id.highsButton);

            none.setOnClickListener(rangeListener);
            mid.setOnClickListener(rangeListener);
            low.setOnClickListener(rangeListener);
            high.setOnClickListener(rangeListener);


            TextView textView = (TextView) currentView.findViewById(R.id.textView);
            String bulbIdentifier = (String)getItem(position);
            String key = lightIdentifiers.get(position);
            textView.setContentDescription(key);
            if(lightRangeMap.containsKey(key)){
                if(lightRangeMap.get(key).equals("LOW")){
                    low.setChecked(true);
                } else if(lightRangeMap.get(key).equals("MID")){
                    mid.setChecked(true);
                } else if(lightRangeMap.get(key).equals("HIGH")){
                    high.setChecked(true);
                } else {
                    none.setChecked(true);
                }
            } else {
                none.setChecked(true);
            }

            textView.setText(bulbIdentifier);
            return currentView;
        }
    }

}
