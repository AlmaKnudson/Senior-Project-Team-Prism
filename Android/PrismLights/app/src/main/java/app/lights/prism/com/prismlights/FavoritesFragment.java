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
//                HueBulbChangeUtility.activateScene(selectedScene);
            }
        });
        return layout;
    }



    private class FavoriteViewAdapter extends BaseAdapter {

        private FavoritesDataModel favoritesDataModel;

        public FavoriteViewAdapter() {
            super();
            favoritesDataModel = FavoritesDataModel.getInstance();
        }
        @Override
        public int getCount() {
            return favoritesDataModel.getFavoritesCount() + 2;
        }

        @Override
        public Object getItem(int position) {
            if(getItemViewType(position) == RealHomeFragment.NORMAL_VIEW) {
                return favoritesDataModel.getFavoriteAtIndex(position);
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == favoritesDataModel.getFavoritesCount() + 1) {
                return RealHomeFragment.EDIT_BUTTON;
            } else if (position == favoritesDataModel.getFavoritesCount()) {
                return RealHomeFragment.PLUS_BUTTON;
            } else {
                return RealHomeFragment.NORMAL_VIEW;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(getItemViewType(position) == RealHomeFragment.EDIT_BUTTON) {
                View editView =  LayoutInflater.from(FavoritesFragment.this.getActivity()).inflate(R.layout.edit_view, parent, false);
                TextView editText = (TextView) editView.findViewById(R.id.editLabel);
                editText.setText(getText(R.string.edit_favorites));
                return editView;
            }
            else if(getItemViewType(position) == RealHomeFragment.PLUS_BUTTON) {
                View addView =  LayoutInflater.from(FavoritesFragment.this.getActivity()).inflate(R.layout.add_view, parent, false);
                TextView addText = (TextView) addView.findViewById(R.id.addLabel);
                addText.setText(getText(R.string.add_favorite));
                return addView;
            }
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
            return currentView;
        }
    }

}
