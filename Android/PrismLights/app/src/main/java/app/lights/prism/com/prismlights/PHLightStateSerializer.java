package app.lights.prism.com.prismlights;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.philips.lighting.model.PHLightState;


import java.lang.reflect.Type;

public class PHLightStateSerializer implements JsonSerializer<PHLightState>{

    @Override
    public JsonElement serialize(PHLightState lightState, Type typeOfSrc, JsonSerializationContext context) {
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
        return jsonObject;
    }
}
