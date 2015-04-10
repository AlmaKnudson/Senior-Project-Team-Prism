package app.lights.prism.com.prismlights;

import java.util.LinkedList;
import java.util.List;

/**
 * A data model for favorites because scenes aren't guaranteed to stay on the bridge,
 * and I don't want to have to create a group every time to use them anyway
 * NOT THREAD SAFE...SHOULD ONLY BE CALLED ON UI THREAD
 * TODO think about if you should save the bridge (if you can) for limited lights
 */
public class FavoritesDataModel {

    private List<Favorite> favorites;
    private static FavoritesDataModel favoritesDataModel = null;
    private FavoritesDataModel() {
        favorites = new LinkedList<Favorite>();
        loadFavorites();
    }

    private void loadFavorites() {

    }

    public static FavoritesDataModel getInstance() {
        if(favoritesDataModel == null) {
            favoritesDataModel = new FavoritesDataModel();
        }
        return favoritesDataModel;
    }

    public void addStateAsFavorite(List<String> lightIds, String name) {
        favorites.add(new Favorite(lightIds, name));
    }

    public int getFavoritesCount() {
        return favorites.size();
    }

    public Favorite getFavoriteAtIndex(int index) {
        return favorites.get(index);
    }
}
