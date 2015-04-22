package app.lights.prism.com.prismlights;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FavoritesFragment extends Fragment implements EditButtonPresentCaller {


    private GridView gridView;
    private EditButtonPresentListener editButtonPresentListener;
    private FavoritesDataModel favoritesDataModel;

    public FavoritesFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_lights, container, false);
        gridView= (GridView) layout.findViewById(R.id.homeGridView);
        favoritesDataModel = FavoritesDataModel.getInstance(getActivity().getFilesDir());
        gridView.setAdapter(new FavoriteViewAdapter());
        //only time it needs to be called because they can't be deleted remotely
        callEditButtonPresentListener();
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

    @Override
    public void setEditButtonPresentListener(EditButtonPresentListener listener) {
        this.editButtonPresentListener = listener;
    }

    @Override
    public boolean shouldEditButtonBePresent() {
        if(favoritesDataModel != null) {
            return favoritesDataModel.getFavoritesCount() > 0;
        } else {
            return false;
        }
    }

    /**
     * Calls the edit button present listener after checking for null
     */
    private void callEditButtonPresentListener() {
        if(editButtonPresentListener != null) {
            if(favoritesDataModel.getFavoritesCount() > 0) {
                editButtonPresentListener.editButtonPresent(true);
            } else {
                editButtonPresentListener.editButtonPresent(false);
            }
        }
    }

    private class FavoriteViewAdapter extends BaseAdapter {

        public FavoriteViewAdapter() {
            super();
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
