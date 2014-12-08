package app.lights.prism.com.prismlights;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.Hashtable;
import java.util.List;

public class HueBulbChangeUtility {


    public static int MAX_BRIGHTNESS = 254;

    public static PHLight getLightFromPosition(int position, PHBridge bridge) {
        List<PHLight> currentLights = bridge.getResourceCache().getAllLights();
        return currentLights.get(position);
    }

    public static void toggleBulbState(int lightPosition) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromPosition(lightPosition, bridge);
        PHLightState lightState = light.getLastKnownLightState();
        if(light.getLastKnownLightState().isOn()) {
            lightState.setOn(false);
        }
        else {
            lightState.setOn(true);
        }
        bridge.updateLightState(light, lightState);
    }




    public static void turnBulbOnOff (int lightPosition, boolean on) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromPosition(lightPosition, bridge);
        PHLightState lightState = light.getLastKnownLightState();
        lightState.setOn(on);
        bridge.updateLightState(light, lightState);
    }

    public static boolean changeLightName(int lightPosition, String lightName) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromPosition(lightPosition, bridge);
        light.setName(lightName);
        bridge.updateLight(light, new PHLightListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onStateUpdate(Hashtable<String, String> stringStringHashtable, List<PHHueError> phHueErrors) {

            }
        });
        return true;
    }

    public static void changeBrightness(int lightPosition, int brightness) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromPosition(lightPosition, bridge);
        PHLightState lightState = light.getLastKnownLightState();
        int convertedBrightness = (int) Math.round((brightness * 254.0) / 100);
        lightState.setBrightness(convertedBrightness);
        bridge.updateLightState(light, lightState);
    }

    public static void changeBulbColor(int lightPosition, int rgbColor) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromPosition(lightPosition, bridge);
        PHLightState lightState = light.getLastKnownLightState();
        float[] convertedColor = PHUtilities.calculateXY(rgbColor, "rgb");
        lightState.setX(convertedColor[0]);
        lightState.setY(convertedColor[1]);
        bridge.updateLightState(light, lightState);
    }
}
