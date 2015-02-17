package app.lights.prism.com.prismlights;

import android.text.format.DateFormat;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class HueBulbChangeUtility {


    public static int MAX_BRIGHTNESS = 254;

    public static String colorXYModelForHue = "LCT001";

    public static PHLight getLightFromPosition(int position, PHBridge bridge) {
        List<PHLight> currentLights = bridge.getResourceCache().getAllLights();
        return currentLights.get(position);
    }

    public static void toggleBulbState(int lightPosition) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromPosition(lightPosition, bridge);
        PHLightState lightState = new PHLightState();
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
        PHLightState lightState = new PHLightState();
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
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }

            @Override
            public void onReceivingLightDetails(PHLight phLight) {

            }

            @Override
            public void onReceivingLights(List<PHBridgeResource> phBridgeResources) {

            }

            @Override
            public void onSearchComplete() {

            }
        });
        return true;
    }

    public static void changeBrightness(int lightPosition, int brightness) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromPosition(lightPosition, bridge);
        PHLightState lightState = new PHLightState();
        int convertedBrightness = (int) Math.round((brightness * 254.0) / 100);
        lightState.setBrightness(convertedBrightness);
        lightState.setOn(true);
        bridge.updateLightState(light, lightState);
    }

    public static void changeBulbColor(int lightPosition, float[] xY) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromPosition(lightPosition, bridge);
        PHLightState lightState = new PHLightState();
//        float[] convertedColor = PHUtilities.calculateXY(rgbColor, "rgb");
        lightState.setX(xY[0]);
        lightState.setY(xY[1]);
        lightState.setColorMode(PHLight.PHLightColorMode.COLORMODE_XY);
        if(!light.getLastKnownLightState().isOn()) {
            lightState.setOn(true);
        }
        bridge.updateLightState(light, lightState);
    }


    public static void setAlarm(String lightIdentifier, Date date){
        String scheduleName = (String) DateFormat.format("hh:mm aaa", date.getTime());
        PHSchedule schedule = new PHSchedule(scheduleName);
        schedule.setDate(date);
        schedule.setLightIdentifier(lightIdentifier);
        schedule.setLightState(getLightState());
        schedule.setDescription("Alarm");
    }

    private static PHLightState getLightState() {
        //PHLightState state = phHueSDK.getCurrentLightState();
        //currentBulb.getLastKnownLightState();
        PHLightState state = new PHLightState();
        state.setOn(true);
//        state.setHue(50);
//        state.setSaturation(50);
//        state.setBrightness(50);
//        state.setX((float)0);
//        state.setY((float)0);
//        state.setEffectMode(PHLight.PHLightEffectMode.EFFECT_NONE);
//        state.setAlertMode(PHLight.PHLightAlertMode.ALERT_NONE);
//        state.setTransitionTime(1);
        return state;
    }
    
    public static boolean colorsEqual(float[] color1, float[] color2) {
        System.out.println("x: " + Math.abs(color1[0] - color2[0]));
        System.out.println("y: " + Math.abs(color1[1] - color2[1]));
        if(Math.abs(color1[0] - color2[0])  > 0.001) {
            return false;
        }
        if(Math.abs(color1[1] - color2[1]) > 0.001) {
            return false;
        }
        return true;
    }
}
