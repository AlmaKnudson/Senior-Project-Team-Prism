package app.lights.prism.com.prismlights;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHScene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritesFragment extends Fragment {


    private GridView gridView;

    public FavoritesFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_lights, container, false);
        gridView= (GridView) layout.findViewById(R.id.homeGridView);
        gridView.setAdapter(new FavoriteViewAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HueBulbChangeUtility.activateFavorite((Favorite) gridView.getAdapter().getItem(position));
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HueBulbChangeUtility.activateFavorite((Favorite) gridView.getAdapter().getItem(position));
                Bundle bundle = new Bundle();
                bundle.putInt(RealHomeFragment.favoritePosition, position);

                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                EditFavoriteFragment editFavoriteFragment = new EditFavoriteFragment();
                editFavoriteFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.container, editFavoriteFragment);
                fragmentTransaction.addToBackStack("editfavorite");
                fragmentTransaction.commit();

                return false;
            }
        });
        return layout;
    }



    private class FavoriteViewAdapter extends BaseAdapter {

        private FavoritesDataModel favoritesDataModel;

        public FavoriteViewAdapter() {
            super();
            favoritesDataModel = FavoritesDataModel.getInstance(getActivity().getFilesDir());
        }
        @Override
        public int getCount() {
            return favoritesDataModel.getFavoritesCount();
        }

        @Override
        public Object getItem(int position) {
            return favoritesDataModel.getFavoriteAtIndex(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout currentView;
            if(convertView == null) {
                currentView = (LinearLayout) LayoutInflater.from(FavoritesFragment.this.getActivity()).inflate(R.layout.favorites_view, parent, false);
            } else {
                currentView = (LinearLayout) convertView;
            }
            Favorite favorite = (Favorite) getItem(position);
            String favoriteName = favorite.getName();

            TextView favoriteNameView = (TextView) currentView.findViewById(R.id.favoriteName);
            favoriteNameView.setText(favoriteName);
            SingleFavoriteView favoriteView = (SingleFavoriteView) currentView.findViewById(R.id.singleFavoriteView);
            favoriteView.setColors(favorite.getColors());
            return currentView;
        }
    }

}
