package app.lights.prism.com.prismlights;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FavoritesFragment extends Fragment implements CacheUpdateListener {

    public FavoritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_lights, container, false);
        return layout;
    }

    @Override
    public void cacheUpdated() {

    }

    private void updateFromCache() {

    }

    @Override
    public void onResume() {
        super.onResume();
        updateFromCache();
    }
}
