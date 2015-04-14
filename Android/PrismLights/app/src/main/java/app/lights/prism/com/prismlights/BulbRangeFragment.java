package app.lights.prism.com.prismlights;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by AlmaKnudson on 4/14/15.
 */
public class BulbRangeFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_bulb_range, container, false);
//        String[] list = {"Gobbly", "Goop"};
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          //      android.R.layout.bulbsListView, list);






        return layout;
    }

}
