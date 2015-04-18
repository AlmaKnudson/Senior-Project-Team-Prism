package app.lights.prism.com.prismlights;

import android.os.AsyncTask;
import android.text.format.DateFormat;

import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHScene;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class HueBulbChangeUtility {


    public static int MAX_BRIGHTNESS = 254;

    public static String colorXYModelForHue = "LCT001";

    public static PHLight getLightFromId(String identifier, PHBridge bridge) {
        return bridge.getResourceCache().getLights().get(identifier);
    }

    private static PHGroup getGroupFromId(String identifier, PHBridge bridge) {
        return bridge.getResourceCache().getGroups().get(identifier);
    }

    public static void setBulbAlertState(String lightId, boolean on) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = bridge.getResourceCache().getLights().get(lightId);
        PHLightState lightState = new PHLightState();
        if(on) {
            lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_SELECT);
        } else {
            lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_NONE);
        }
        bridge.updateLightState(light, lightState);
    }
    public static void toggleBulbState(String lightId) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromId(lightId, bridge);
        toggleBulbState(light);
    }

    public static void toggleBulbState(PHLight light) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLightState lightState = new PHLightState();
        if(light.getLastKnownLightState().isOn()) {
            lightState.setOn(false);
        }
        else {
            lightState.setOn(true);
        }
        bridge.updateLightState(light, lightState);
    }




    public static void turnBulbOnOff (String lightId, boolean on) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        turnBulbOnOff(getLightFromId(lightId, bridge), on);

    }

    public static void turnBulbOnOff(PHLight light, boolean on) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLightState lightState = new PHLightState();
        lightState.setOn(on);
        bridge.updateLightState(light, lightState);
    }

    public static boolean changeLightName(String lightId, String lightName) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromId(lightId, bridge);
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

    public static void changeBrightness(String lightId, int brightness) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromId(lightId, bridge);
        changeBrightness(light, brightness);
    }

    public static void changeBrightness(PHLight light, int brightness) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();

        bridge.updateLightState(light, getBrightnessState(brightness, !light.getLastKnownLightState().isOn()));
    }


    public static void changeBulbColor(String lightId, float[] xY) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromId(lightId, bridge);
        changeBulbColor(light, xY);
    }

    private static void changeBulbColor(PHLight light, float[]xY) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        bridge.updateLightState(light, getColorState(xY, !light.getLastKnownLightState().isOn()));
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

    private static PHLightState getBrightnessState(int brightness, boolean shouldSetOn) {
        PHLightState lightState = new PHLightState();
        int convertedBrightness = (int) Math.round((brightness * 254.0) / 100);
        lightState.setBrightness(convertedBrightness);
        if(shouldSetOn) {
            lightState.setOn(true);
        }
        return lightState;
    }

    private static PHLightState getColorState(float[]  xY, boolean shouldSetOn) {
        PHLightState lightState = new PHLightState();
//        float[] convertedColor = PHUtilities.calculateXY(rgbColor, "rgb");
        lightState.setX(xY[0]);
        lightState.setY(xY[1]);
        lightState.setColorMode(PHLight.PHLightColorMode.COLORMODE_XY);
        if(shouldSetOn) {
            lightState.setOn(true);
        }
        return lightState;
    }

    private static PHLightState getOnState(boolean shouldSetOn) {
        PHLightState lightState = new PHLightState();
        lightState.setOn(shouldSetOn);
        return lightState;
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

    private static Map<String, PHLight> getAllLights() {
        return PHHueSDK.getInstance().getSelectedBridge().getResourceCache().getLights();
    }

    public static boolean isGroupReachable(PHGroup currentGroup) {
        Map<String, PHLight> lights = getAllLights();
        List<String> lightIds = currentGroup.getLightIdentifiers();

        for(String id : lightIds) {
            if(lights.get(id).getLastKnownLightState().isReachable()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGroupOff(PHGroup currentGroup) {
        Map<String, PHLight> lights = getAllLights();
        List<String> lightIds = currentGroup.getLightIdentifiers();
        for(String id : lightIds) {
            if(lights.get(id).getLastKnownLightState().isReachable() && lights.get(id).getLastKnownLightState().isOn()) {
                return false;
            }
        }
        return true;
    }

    public static Integer getGroupColor(PHGroup currentGroup) {
        Map<String, PHLight> lights = getAllLights();
        List<String> lightIds = currentGroup.getLightIdentifiers();
        float[] previousColor = null;

        for(String id: lightIds) {
            PHLightState lightState =  lights.get(id).getLastKnownLightState();
            float[] currentColor = new float[]{lightState.getX(), lightState.getY()};
            if(lights.get(id).getLastKnownLightState().isReachable()) {
                if (previousColor == null) {
                    previousColor = currentColor;
                } else {
                    if (!colorsEqual(previousColor, currentColor)) {
                        return null;
                    }
                }
            }
        }
        return PHUtilities.colorFromXY(previousColor, colorXYModelForHue);
    }

    public static void toggleBulbGroupState(PHGroup group) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        Map<String, PHLight> lights = getAllLights();
        List<String> lightIds = group.getLightIdentifiers();
        boolean shouldSetOn = true;
        for(String id : lightIds) {
            if(lights.get(id).getLastKnownLightState().isReachable() && lights.get(id).getLastKnownLightState().isOn()) {
                shouldSetOn = false;
                break;
            }
        }
        bridge.setLightStateForGroup(group.getIdentifier(), getOnState(shouldSetOn));

    }

    public static void changeGroupName(String identifier, String groupName) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHGroup group = getGroupFromId(identifier, bridge);
        group.setName(groupName);
        bridge.updateGroup(group, new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {

            }

            @Override
            public void onReceivingGroupDetails(PHGroup phGroup) {

            }

            @Override
            public void onReceivingAllGroups(List<PHBridgeResource> phBridgeResources) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }



    public static void turnGroupOnOff(String identifier, boolean on) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHGroup group = getGroupFromId(identifier, bridge);
        bridge.setLightStateForGroup(group.getIdentifier(), getOnState(on));
    }

    public static void changeGroupBrightness(String identifier, int brightness) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHGroup group = getGroupFromId(identifier, bridge);
        Map<String, PHLight> lights = getAllLights();
        bridge.setLightStateForGroup(group.getIdentifier(), getBrightnessState(brightness, true));

    }

    public static void changeGroupColor(String identifier, float[] newColor) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHGroup group = getGroupFromId(identifier, bridge);
        bridge.setLightStateForGroup(group.getIdentifier(), getColorState(newColor, true));
    }

    public static String getNextGroupId() {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        return "Group " + (1 + bridge.getResourceCache().getGroups().size());
    }

    /**
     * @param name The name of the group
     * @param lightIdentifiers the ids of the lights to be included in the group
     * @param onCompletedListener a listener which is called when the group is created (may be called multiple times)
     * @param onCompletedListener a listener which is called when the group is created (may be called multiple times)
     */
    public static void createGroup(String name, List<String> lightIdentifiers, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        bridge.createGroup(name, lightIdentifiers, new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {
                System.out.println("Created Group");
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onReceivingGroupDetails(PHGroup phGroup) {
                System.out.println("recieving group details");
            }

            @Override
            public void onReceivingAllGroups(List<PHBridgeResource> phBridgeResources) {
                System.out.println("recieving all groups");
            }

            @Override
            public void onSuccess() {
                System.out.println("Success Group");

                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onError(int i, String s) {
                System.out.println("Error Group");
                //TOOD handle error when
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {
                System.out.println("State update");
            }
        });
    }

    //Doesn't work with api ANNOYING
    public static void deleteLight(PHLight phLight) {
//        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
//        bridge.deleteLightWithId(phLight.getIdentifier(), new PHLightListener() {
//            @Override
//            public void onReceivingLightDetails(PHLight phLight) {
//
//            }
//
//            @Override
//            public void onReceivingLights(List<PHBridgeResource> phBridgeResources) {
//
//            }
//
//            @Override
//            public void onSearchComplete() {
//
//            }
//
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onError(int i, String s) {
//
//            }
//
//            @Override
//            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {
//
//            }
//        });
    }

    public static void deleteGroup(PHGroup phGroup, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        if(phGroup == null) {
            if(onCompletedListener != null) {
                onCompletedListener.onCompleted();
            }
            return;
        }
        bridge.deleteGroup(phGroup.getIdentifier(), new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onReceivingGroupDetails(PHGroup phGroup) {

            }

            @Override
            public void onReceivingAllGroups(List<PHBridgeResource> phBridgeResources) {

            }

            @Override
            public void onSuccess() {
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onError(int i, String s) {
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    public static void sortIds(List<String> ids) {
        Collections.sort(ids, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                try{
                    int l = Integer.parseInt(lhs);
                    int r = Integer.parseInt(rhs);
                    return l - r;

                } catch (Exception e) {//TODO make this more specific
                    return lhs.compareTo(rhs);
                }
            }
        });
    }

    public static void activateFavorite(Favorite favorite) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        if(favorite.getIsAll()) {
            bridge.setLightStateForDefaultGroup(favorite.getAllLightState());
        } else {
            for (String lightId : favorite.getLightStates().keySet()) {
                PHLightState lightState = favorite.getLightStates().get(lightId);
                //account for deleted lights or change in bridge
                PHLight light = getLightFromId(lightId, bridge);
                if(light != null) {
                    bridge.updateLightState(getLightFromId(lightId, bridge), lightState);
                }
            }
        }
    }

    public static void searchForLights(final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        bridge.findNewLights(new PHLightListener() {
            @Override
            public void onReceivingLightDetails(PHLight phLight) {

            }

            @Override
            public void onReceivingLights(List<PHBridgeResource> phBridgeResources) {

            }

            @Override
            public void onSearchComplete() {
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    public static void searchForLights(List<String> serialCodes, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        bridge.findNewLightsWithSerials(serialCodes, new PHLightListener() {
            @Override
            public void onReceivingLightDetails(PHLight phLight) {

            }

            @Override
            public void onReceivingLights(List<PHBridgeResource> phBridgeResources) {

            }

            @Override
            public void onSearchComplete() {
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    public static void editGroup(String groupId, List<String> lightIdentifiers, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHGroup group = bridge.getResourceCache().getGroups().get(groupId);
        group.setLightIdentifiers(lightIdentifiers);
        bridge.updateGroup(group, new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {
                System.out.println("Created Group");
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onReceivingGroupDetails(PHGroup phGroup) {
                System.out.println("recieving group details");
            }

            @Override
            public void onReceivingAllGroups(List<PHBridgeResource> phBridgeResources) {
                System.out.println("recieving all groups");
            }

            @Override
            public void onSuccess() {
                System.out.println("Success Group");

                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onError(int i, String s) {
                System.out.println("Error Group");
                //TOOD handle error when
                if(onCompletedListener != null) {
                    onCompletedListener.onCompleted();
                }
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {
                System.out.println("State update");
            }
        });
    }

    public static void deleteGroups(Set<String> checked, OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        final CompletedMap completedMap = new CompletedMap(checked, onCompletedListener);
        for(final String groupId: checked) {
            PHGroup group = bridge.getResourceCache().getGroups().get(groupId);
            deleteGroup(group, new OnCompletedListener() {
                @Override
                public void onCompleted() {
                    completedMap.complete(groupId);
                }
            });
        }

    }
}
