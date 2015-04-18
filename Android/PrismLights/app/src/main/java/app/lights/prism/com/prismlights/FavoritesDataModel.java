package app.lights.prism.com.prismlights;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A data model for favorites because scenes aren't guaranteed to stay on the bridge,
 * and I don't want to have to create a group every time to use them anyway
 * NOT THREAD SAFE...SHOULD ONLY BE CALLED ON UI THREAD
 * TODO think about if you should save the bridge (if you can) for limited lights
 */
public class FavoritesDataModel {

    private List<Favorite> favorites;
    private static FavoritesDataModel favoritesDataModel = null;
    private File directory;
    private FavoritesDataModel() {
        favorites = new LinkedList<Favorite>();
    }
    public static FavoritesDataModel getInstance(File directory) {
        if(favoritesDataModel == null) {
            loadFromFile(directory);
        }
        return favoritesDataModel;
    }


    private static void loadFromFile(File directory) {
        File file = new File(directory, "favorites.txt");
        if(file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedFileReader = new BufferedReader(fileReader);
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(FavoritesDataModel.class, new FavoritesDataModelDeserializer());
                Gson gson = gsonBuilder.create();
                favoritesDataModel =  gson.fromJson(bufferedFileReader, FavoritesDataModel.class);
                //happens when the file is empty
                if(favoritesDataModel == null) {
                    favoritesDataModel = new FavoritesDataModel();
                    favoritesDataModel.directory = directory;
                    favoritesDataModel.loadDefaultFavorites();
                } else {
                    favoritesDataModel.directory = directory;
                }
                bufferedFileReader.close();
            } catch (Exception e) {
                favoritesDataModel = new FavoritesDataModel();
                favoritesDataModel.directory = directory;
                favoritesDataModel.loadDefaultFavorites();
            }
        } else {
            favoritesDataModel = new FavoritesDataModel();
            favoritesDataModel.directory = directory;
            favoritesDataModel.loadDefaultFavorites();
        }
    }

    private void loadDefaultFavorites() {
        PHLightState normalState = new PHLightState();
        normalState.setOn(true);
        normalState.setX(0.4596f);
        normalState.setY(0.4105f);
        normalState.setBrightness(254);
        favoritesDataModel.favorites.add(new Favorite("Normal All On", normalState));
        PHLightState offState = new PHLightState();
        offState.setOn(false);
        favoritesDataModel.favorites.add(new Favorite("All Off", offState));
        saveToFile();
    }

    public void saveToFile() {
        File file = new File(directory, "favorites.txt");
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(FavoritesDataModel.class, new FavoritesModelSerializer());
            Gson gson = gsonBuilder.create();
            bufferedWriter.write(gson.toJson(this, FavoritesDataModel.class));
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addStateAsFavorite(List<String> lightIds, String name) {
        favorites.add(new Favorite(name, lightIds));
        saveToFile();
    }

    public int getFavoritesCount() {
        return favorites.size();
    }

    public Favorite getFavoriteAtIndex(int index) {
        return favorites.get(index);
    }

    public String getNextFavoriteName() {
        return "Favorite " + favorites.size();
    }

    public void addStateAsFavorite(String name, PHLightState lightState) {
        favorites.add(new Favorite(name, lightState));
        saveToFile();
    }

    public void modifyFavorite(int favoritePosition, List<String> selectedLightIds, String name) {
        favorites.set(favoritePosition, new Favorite(name, selectedLightIds));
        saveToFile();
    }

    public void modifyFavorite(int favoritePosition, String name, PHLightState lightState) {
        favorites.set(favoritePosition, new Favorite(name, lightState));
        saveToFile();
    }

    public void reorderFavorites(int shiftedFrom, int shiftedTo) {
        favorites.add(shiftedTo, favorites.remove(shiftedFrom));
    }

    public void doneReordering() {
        saveToFile();
    }

    public void removeFavorites(Set<Integer> toRemove) {
        for(int toRemovePos: toRemove) {
            favorites.remove(toRemovePos);
        }
        saveToFile();
    }

    static class FavoritesModelSerializer implements JsonSerializer<FavoritesDataModel> {

        @Override
        public JsonElement serialize(FavoritesDataModel src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray allFavorites = new JsonArray();
            for(Favorite favorite: src.favorites) {
                allFavorites.add(serialize(favorite));
            }
            return allFavorites;
        }

        public JsonElement serialize(Favorite favorite) {
            JsonObject jsonFavorite = new JsonObject();
            if(favorite.getName() != null) {
                jsonFavorite.addProperty("name", favorite.getName());
            }
            if(favorite.getLightStates() != null) {
                JsonArray lightStateArray = new JsonArray();
                for(String id: favorite.getLightStates().keySet()) {
                    lightStateArray.add(serialize(id, favorite.getLightStates().get(id)));
                }
                jsonFavorite.add("lightStates", lightStateArray);
            }
            jsonFavorite.addProperty("all", favorite.getIsAll());
            if(favorite.getAllLightState() != null) {
                jsonFavorite.add("allLightState", serialize("all", favorite.getAllLightState()));
            }
            return jsonFavorite;
        }

        public JsonElement serialize(String lightId, PHLightState lightState) {
            JsonObject jsonObject = new JsonObject();
            if(lightState.getBrightness() != null) {
                jsonObject.addProperty("brightness", lightState.getBrightness());
            }
            if(lightState.isOn() != null) {
                jsonObject.addProperty("on", lightState.isOn());
            }
            if(lightState.getX() != null) {
                jsonObject.addProperty("x", lightState.getX());
            }
            if(lightState.getY() != null) {
                jsonObject.addProperty("y", lightState.getY());
            }
            jsonObject.addProperty("lightId", lightId);
            return jsonObject;
        }
    }

    static class FavoritesDataModelDeserializer implements JsonDeserializer<FavoritesDataModel> {

        @Override
        public FavoritesDataModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            FavoritesDataModel favoritesDataModel1 = new FavoritesDataModel();
            JsonArray allFavorites = json.getAsJsonArray();
            for(JsonElement favoriteJson : allFavorites) {
                favoritesDataModel1.favorites.add(deserializeFavorite(favoriteJson.getAsJsonObject()));
            }
            return favoritesDataModel1;
        }


        public Favorite deserializeFavorite(JsonObject json) throws JsonParseException {
            String name = null;
            if(json.has("name")) {
                name = json.get("name").getAsString();
            }
            if(json.has("all")) {
                if(json.get("all").getAsBoolean() && json.has("allLightState")) {
                    return new Favorite(name, deserializeLightState(json.get("allLightState").getAsJsonObject(), null));
                }
            }
            Map<String, PHLightState> phLightStates = new HashMap<String, PHLightState>();
            if(json.has("lightStates")) {
                JsonArray lightStates = json.getAsJsonArray("lightStates");
                for(JsonElement lightState : lightStates) {
                    deserializeLightState(lightState.getAsJsonObject(), phLightStates);
                }
            }
            return new Favorite(name, phLightStates);
        }
        public PHLightState deserializeLightState(JsonObject jsonObject, Map<String, PHLightState> phLightStates) throws JsonParseException {
            PHLightState phLightState = new PHLightState();
            if(jsonObject.has("brightness")) {
                phLightState.setBrightness(jsonObject.get("brightness").getAsInt());
            }
            if(jsonObject.has("on")) {
                phLightState.setOn(jsonObject.get("on").getAsBoolean());
            }
            if(jsonObject.has("x")) {
                phLightState.setX(jsonObject.get("x").getAsFloat());
            }
            if(jsonObject.has("y")) {
                phLightState.setY(jsonObject.get("y").getAsFloat());
            }
            if(phLightState.getX() != null && phLightState.getY() != null) {
                phLightState.setColorMode(PHLight.PHLightColorMode.COLORMODE_XY);
            }
            String id = null;
            if(jsonObject.has("lightId")) {
                id = jsonObject.get("lightId").getAsString();
            }
            if(phLightStates != null) {
                phLightStates.put(id, phLightState);
            }
            return phLightState;
        }
    }
}
