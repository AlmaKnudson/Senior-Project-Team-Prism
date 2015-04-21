package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class FavoritesEditFragment extends Fragment implements OnItemShiftedListener {
    private ReorderGridView gridView;
    private FavoritesDataModel favoritesDataModel;
    private TreeSet<Integer> checked;

    public FavoritesEditFragment() {
        checked = new TreeSet<Integer>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_edit_list, container, false);
        gridView = (ReorderGridView) layout.findViewById(R.id.reorderGridView);
        gridView.setAdapter(new FavoriteViewAdapter());
        gridView.setOnItemShiftedListener(this);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateChecked((CheckBox) view.findViewById(R.id.selectCheck), position);
            }
        });
        layout.findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        layout.findViewById(R.id.trashButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoritesDataModel.removeFavorites(checked);
                ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                //close fragment when there are no more favorites to edit
                if(favoritesDataModel.getFavoritesCount() == 0) {
                    getFragmentManager().popBackStack();
                }
                checked.clear();
            }
        });
        return layout;
    }

    private void updateChecked(CheckBox checkBox, int position) {
        if(checked.contains(position)) {
            checked.remove(position);
            checkBox.setChecked(false);
        } else {
            checked.add(position);
            checkBox.setChecked(true);
        }
    }

    @Override
    public void onItemShifted(int shiftedFrom, int shiftedTo) {
        favoritesDataModel.reorderFavorites(shiftedFrom, shiftedTo);
        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        favoritesDataModel = FavoritesDataModel.getInstance(getActivity().getFilesDir());
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
                currentView = (LinearLayout) LayoutInflater.from(FavoritesEditFragment.this.getActivity()).inflate(R.layout.favorite_view_select, parent, false);
            } else {
                currentView = (LinearLayout) convertView;
            }
            Favorite favorite = (Favorite) getItem(position);
            String favoriteName = favorite.getName();

            TextView favoriteNameView = (TextView) currentView.findViewById(R.id.favoriteName);
            favoriteNameView.setText(favoriteName);
            SingleFavoriteView favoriteView = (SingleFavoriteView) currentView.findViewById(R.id.singleFavoriteView);
            favoriteView.setColors(favorite.getColors());

            CheckBox checkBox = (CheckBox) currentView.findViewById(R.id.selectCheck);
            if(checked.contains(position)) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
            return currentView;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        favoritesDataModel.doneReordering();
    }
}

