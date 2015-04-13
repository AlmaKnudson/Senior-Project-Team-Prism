package app.lights.prism.com.prismlights;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddFavoriteFragment extends Fragment implements CacheUpdateListener{
    private BulbSelectionFragment bulbSelectionFragment;
    private EditText nameEditor;
    private Button doneButton;
    private FavoritesDataModel favoritesDataModel;



    public AddFavoriteFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        favoritesDataModel = FavoritesDataModel.getInstance(getActivity().getFilesDir());
        View layout = inflater.inflate(R.layout.fragment_add_multi, container, false);
        TextView title = (TextView) layout.findViewById(R.id.title);
        title.setText(R.string.add_favorite);
        bulbSelectionFragment = (BulbSelectionFragment) getFragmentManager().findFragmentById(R.id.selectBulbFragment);
        bulbSelectionFragment.allowLongClick(true);
        nameEditor = (EditText) layout.findViewById(R.id.nameEditor);
        nameEditor.setText(favoritesDataModel.getNextFavoriteName());
        doneButton = (Button) layout.findViewById(R.id.doneButton);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoritesDataModel.addStateAsFavorite(bulbSelectionFragment.getSelectedLightIds(), nameEditor.getText().toString());
                getFragmentManager().popBackStack();
            }
        });
        return layout;
    }

    @Override
    public void cacheUpdated() {
        bulbSelectionFragment.cacheUpdated();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(bulbSelectionFragment != null) {
            getFragmentManager().beginTransaction().remove(bulbSelectionFragment).commit();
        }
    }
}
