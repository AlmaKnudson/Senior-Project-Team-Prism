package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.philips.lighting.model.PHLightState;

public class EditFavoriteFragment extends Fragment implements CacheUpdateListener {
    private BulbSelectionView bulbSelectionFragment;
    private EditText nameEditor;
    private Button doneButton;
    private FavoritesDataModel favoritesDataModel;
    private int favoritePosition;
    private Favorite favorite;
    private boolean done;

    public EditFavoriteFragment() {
        done = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoritePosition = getArguments().getInt(RealHomeFragment.favoritePosition);
        favorite = favoritesDataModel.getFavoriteAtIndex(favoritePosition);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        favoritesDataModel = FavoritesDataModel.getInstance(getActivity().getFilesDir());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_add_multi, container, false);
        TextView title = (TextView) layout.findViewById(R.id.title);
        title.setText(R.string.edit_favorite);
        bulbSelectionFragment = (BulbSelectionView) layout.findViewById(R.id.selectBulbView);
        if(bulbSelectionFragment == null) {
            bulbSelectionFragment = (BulbSelectionView) layout.findViewById(R.id.selectBulbView);
        }
        bulbSelectionFragment.allowLongClick(true, getFragmentManager());
        nameEditor = (EditText) layout.findViewById(R.id.nameEditor);
        //also figure out why after 3 back and forths it stops working correctly
        nameEditor.setText(favorite.getName());
        doneButton = (Button) layout.findViewById(R.id.doneButton);
        bulbSelectionFragment.setOnCheckedNumberChanged(new CheckedNumberChangedListener() {
            @Override
            public void onCheckedNumberChanged(int checkedNumber) {
                if (checkedNumber > 0) {
                    doneButton.setEnabled(true);
                } else {
                    doneButton.setEnabled(false);
                }
            }
        });
        if(favorite.getIsAll()) {
            bulbSelectionFragment.selectAll();
        } else {
            bulbSelectionFragment.setSelectedIds(favorite.getLightStates().keySet());
        }
//        bulbSelectionFragment.setSelectedIds(favorite.);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //avoid popping the backstack twice
                if (!done)
                    done = true;
                    if (bulbSelectionFragment.getAllChecked()) {
                        PHLightState lightState = bulbSelectionFragment.allBulbsSameState();
                        if (lightState == null) {
                            favoritesDataModel.modifyFavorite(favoritePosition, bulbSelectionFragment.getSelectedLightIds(), nameEditor.getText().toString());
                        } else {
                            favoritesDataModel.modifyFavorite(favoritePosition, nameEditor.getText().toString(), lightState);
                        }
                    } else {
                        favoritesDataModel.modifyFavorite(favoritePosition, bulbSelectionFragment.getSelectedLightIds(), nameEditor.getText().toString());
                    }
                getFragmentManager().popBackStack();
            }
        });
        return layout;
    }

    @Override
    public void cacheUpdated() {
        bulbSelectionFragment.cacheUpdated();
    }
}

