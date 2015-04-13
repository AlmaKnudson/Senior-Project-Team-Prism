package app.lights.prism.com.prismlights;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.lang.reflect.Type;

public class PHLightStateDeserializer implements JsonDeserializer<PHLightState> {
    @Override
    public PHLightState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        PHLightState phLightState = new PHLightState();
        JsonObject jsonObject = json.getAsJsonObject();
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
        return phLightState;
    }
}
