package app.lights.prism.com.prismlights;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Favorite {

    private Map<String, PHLightState> lightStates;
    private List<Integer> favoriteColors;
    private String name;

    public Favorite(List<String> lightIds, String name) {
        lightStates = new HashMap<String, PHLightState>();
        favoriteColors = new ArrayList<Integer>();
        Map<String, PHLight> lights = PHHueSDK.getInstance().getSelectedBridge().getResourceCache().getLights();
        for(String lightId : lightIds) {
            PHLightState lightState = new PHLightState(lights.get(lightId).getLastKnownLightState());
            lightStates.put(lightId, lightState);
            favoriteColors.add(PHUtilities.colorFromXY(new float[]{lightState.getX(), lightState.getY()}, HueBulbChangeUtility.colorXYModelForHue));
        }
        this.name = name;
    }

    public Map<String, PHLightState> getLightStates() {
        return lightStates;
    }

    public String saveLightStates() {
        //TODO save light states
        return "";
    }

    public List<Integer> getColors() {
        return favoriteColors;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
