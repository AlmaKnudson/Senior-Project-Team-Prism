package app.lights.prism.com.prismlights;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LayoutIdOrder {
    private LinkedList<String> groupIdOrder;
    private LinkedList<String> lightIdOrder;
    private File directory;

    private static LayoutIdOrder layoutIdOrderModel;

    public static LayoutIdOrder getInstance(File directory) {
        if(layoutIdOrderModel == null) {
            loadFromFile(directory);
            layoutIdOrderModel.directory = directory;
        }
        return layoutIdOrderModel;
    }

    private LayoutIdOrder() {
        groupIdOrder = new LinkedList<String>();
        lightIdOrder = new  LinkedList<String>();
    }

    public List<String> updateLightIdOrder(int fromPosition, int toPosition) {
        lightIdOrder.add(toPosition, lightIdOrder.remove(fromPosition));
        return lightIdOrder;
    }

    public List<String> updateGroupIdOrder(int fromPosition, int toPosition) {
        groupIdOrder.add(toPosition, groupIdOrder.remove(fromPosition));
        return groupIdOrder;
    }

    public LinkedList<String> getGroupsFromBridgeOrder(Set<String> ids) {
        return updateIdOrderFromBridge(new HashSet<String>(ids), groupIdOrder);
    }

    public LinkedList<String> getLightsFromBridgeOrder(Set<String> ids) {
        return updateIdOrderFromBridge(new HashSet<String>(ids), lightIdOrder);
    }

    private LinkedList<String> updateIdOrderFromBridge(Set<String> ids, LinkedList<String> currentIdOrder) {
        if(!currentIdOrder.isEmpty()) {
            List<String> currentIdOrderCopy = new LinkedList<String>(currentIdOrder);
            for (String id : currentIdOrderCopy) {
                if (ids.contains(id)) {
                    ids.remove(id);
                } else {
                    currentIdOrder.remove(id);
                }
            }
            ArrayList<String> arrayIds = new ArrayList<String>(ids);
            HueBulbChangeUtility.sortIds(arrayIds);
            for (String id : arrayIds) {
                currentIdOrder.addLast(id);
            }
        } else {
            ArrayList<String> arrayIds = new ArrayList<String>(ids);
            HueBulbChangeUtility.sortIds(arrayIds);
            currentIdOrder.addAll(arrayIds);
        }
        return currentIdOrder;
    }

    private static void loadFromFile(File directory) {
        File file = new File(directory, "order.txt");
        if(file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedFileReader = new BufferedReader(fileReader);
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(LayoutIdOrder.class, new LayoutIdOrderDeserializer());
                Gson gson = gsonBuilder.create();
                layoutIdOrderModel =  gson.fromJson(bufferedFileReader, LayoutIdOrder.class);
                //happens when the file is empty
                if(layoutIdOrderModel == null) {
                    layoutIdOrderModel = new LayoutIdOrder();
                }
                bufferedFileReader.close();
            } catch (IOException e) {
                layoutIdOrderModel = new LayoutIdOrder();
            }
        } else {
            layoutIdOrderModel = new LayoutIdOrder();
        }
    }
    public void saveToFile() {
        File file = new File(directory, "order.txt");
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LayoutIdOrder.class, new LayoutIdOrderSerializer());
            Gson gson = gsonBuilder.create();
            bufferedWriter.write(gson.toJson(this, LayoutIdOrder.class));
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class LayoutIdOrderSerializer implements JsonSerializer<LayoutIdOrder> {

        @Override
        public JsonElement serialize(LayoutIdOrder src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject orders = new JsonObject();
            JsonArray lights = new JsonArray();
            JsonArray groups = new JsonArray();
            for(String light: src.lightIdOrder) {
                lights.add(new JsonPrimitive(light));
            }
            for(String group: src.groupIdOrder) {
                groups.add(new JsonPrimitive(group));
            }
            orders.add("lights", lights);
            orders.add("groups", groups);
            return orders;
        }
    }

    static class LayoutIdOrderDeserializer implements JsonDeserializer<LayoutIdOrder> {

        @Override
        public LayoutIdOrder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            LayoutIdOrder layoutIdOrder = new LayoutIdOrder();
            JsonObject orders = json.getAsJsonObject();
            if(orders.has("lights")) {
                JsonArray lights = orders.get("lights").getAsJsonArray();
                for(JsonElement light: lights) {
                    layoutIdOrder.lightIdOrder.addLast(light.getAsString());
                }

            }
            if(orders.has("groups")) {
                JsonArray groups = orders.get("groups").getAsJsonArray();
                for(JsonElement group: groups) {
                    layoutIdOrder.groupIdOrder.addLast(group.getAsString());
                }

            }
            return layoutIdOrder;
        }
    }
}
