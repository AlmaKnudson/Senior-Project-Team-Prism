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

/**
 * Fragment allowing the user to add  favorites
 */
public class AddFavoriteFragment extends Fragment implements CacheUpdateListener{
    private BulbSelectionView bulbSelectionView;
    private EditText nameEditor;
    private Button doneButton;
    private FavoritesDataModel favoritesDataModel;
    private boolean done; //used to prevent the user from adding the same favorite twice by pressing the done button quickly



    public AddFavoriteFragment() {
        done = false;
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
        title.setText(R.string.add_favorite);
        bulbSelectionView = (BulbSelectionView) layout.findViewById(R.id.selectBulbView);
        bulbSelectionView.allowLongClick(true, getFragmentManager());
        nameEditor = (EditText) layout.findViewById(R.id.nameEditor);
        nameEditor.setText(favoritesDataModel.getNextFavoriteName());
        doneButton = (Button) layout.findViewById(R.id.doneButton);
        bulbSelectionView.setOnCheckedNumberChanged(new CheckedNumberChangedListener() {
            @Override
            public void onCheckedNumberChanged(int checkedNumber) {
                if(checkedNumber > 0) {
                    doneButton.setEnabled(true);
                } else {
                    doneButton.setEnabled(false);
                }
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!done) {
                    done = true;
                    if (bulbSelectionView.getAllChecked()) {
                        PHLightState lightState = bulbSelectionView.allBulbsSameState();
                        if (lightState == null) {
                            favoritesDataModel.addStateAsFavorite(bulbSelectionView.getSelectedLightIds(), nameEditor.getText().toString());
                        } else {
                            favoritesDataModel.addStateAsFavorite(nameEditor.getText().toString(), lightState);
                        }
                    } else {
                        favoritesDataModel.addStateAsFavorite(bulbSelectionView.getSelectedLightIds(), nameEditor.getText().toString());
                    }
                    getFragmentManager().popBackStack();
                }
            }
        });
        return layout;
    }

    @Override
    /**
     * Update the appearance of the bulb states in the selection view
     */
    public void cacheUpdated() {
        bulbSelectionView.cacheUpdated();
    }

}
