package app.lights.prism.com.prismlights;

import android.graphics.Color;

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
    private boolean all;
    private PHLightState allLightState;
    private List<Integer> favoriteColors;
    private String name;

    public Favorite(String name, List<String> lightIds) {
        all = false;
        lightStates = new HashMap<String, PHLightState>();
        favoriteColors = new ArrayList<Integer>();
        this.name = name;
        Map<String, PHLight> lights = PHHueSDK.getInstance().getSelectedBridge().getResourceCache().getLights();
        for(String lightId : lightIds) {
            PHLightState oldLightState = lights.get(lightId).getLastKnownLightState();
            PHLightState lightState = new PHLightState();
            lightState.setColorMode(PHLight.PHLightColorMode.COLORMODE_XY);
            lightState.setBrightness(oldLightState.getBrightness());
            lightState.setOn(oldLightState.isOn());
            lightState.setX(oldLightState.getX());
            lightState.setY(oldLightState.getY());
            lightStates.put(lightId, lightState);
            favoriteColors.add(getDisplayColor(lightState));
        }
    }

    public Favorite(String name, PHLightState oldLightState) {
        all = true;
        this.name = name;
        favoriteColors = new ArrayList<Integer>();
        allLightState = new PHLightState();
        allLightState.setColorMode(PHLight.PHLightColorMode.COLORMODE_XY);
        allLightState.setBrightness(oldLightState.getBrightness());
        allLightState.setOn(oldLightState.isOn());
        allLightState.setX(oldLightState.getX());
        allLightState.setY(oldLightState.getY());
        favoriteColors.add(getDisplayColor(allLightState));
    }

    private int getDisplayColor(PHLightState lightState) {
        if(lightState.getX() != null && lightState.getY() != null) {
            int onLightColor = PHUtilities.colorFromXY(new float[]{lightState.getX(), lightState.getY()}, HueBulbChangeUtility.colorXYModelForHue);
            if(lightState.isOn() != null && lightState.isOn()) {
                return onLightColor;
            } else {
//                float[] hsv =  new float[3];
//                Color.colorToHSV(onLightColor, hsv);
//                hsv[1] = hsv[1] - (hsv[1] *.5f);
//                return Color.HSVToColor(hsv);
                return Color.BLACK;
            }
        } else {
            return Color.BLACK;
        }
    }

    /**
     * This constructor only for use in loading from save
     * @param name
     * @param lightStates
     */
    public Favorite(String name, Map<String, PHLightState> lightStates) {
        this.name = name;
        this.lightStates = lightStates;
        favoriteColors = new ArrayList<Integer>();
        for(String lightId: lightStates.keySet()) {
            PHLightState lightState = lightStates.get(lightId);
            favoriteColors.add(PHUtilities.colorFromXY(new float[]{lightState.getX(), lightState.getY()}, HueBulbChangeUtility.colorXYModelForHue));
        }
    }

    public Map<String, PHLightState> getLightStates() {
        return lightStates;
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

    public boolean getIsAll() {
        return all;
    }

    public PHLightState getAllLightState() {
        return allLightState;
    }
}
