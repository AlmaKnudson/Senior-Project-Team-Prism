package app.lights.prism.com.prismlights;

import android.text.format.DateFormat;

import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HueBulbChangeUtility {


    public static int MAX_BRIGHTNESS = 254;

    public static final String COLOR_XY_MODEL_FOR_HUE = "LCT001";
    public static final String DEFAULT_GROUP_ID = "0";

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

    public static void toggleBulbState(String lightId, MainActivity activity) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromId(lightId, bridge);
        toggleBulbState(light, activity);
    }

    public static void toggleBulbState(PHLight light, MainActivity activity) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        ColorCycle.removePreviousColorCycle(bridge, light.getIdentifier(), false, activity);
        PHLightState lightState = new PHLightState();
        if(light.getLastKnownLightState().isOn()) {
            lightState.setOn(false);
        }
        else {
            lightState.setOn(true);
        }
        bridge.updateLightState(light, lightState);
    }




    public static void turnBulbOnOff(String lightId, boolean on, MainActivity activity, OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        turnBulbOnOff(getLightFromId(lightId, bridge), on, activity, onCompletedListener);

    }

    public static void turnBulbOnOff(PHLight light, final boolean on, MainActivity activity, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        ColorCycle.removePreviousColorCycle(bridge, light.getIdentifier(), false, activity);
        PHLightState lightState = new PHLightState();
        lightState.setOn(on);
        bridge.updateLightState(light, lightState, new PHLightListener() {
            @Override
            public void onReceivingLightDetails(PHLight phLight) {

            }

            @Override
            public void onReceivingLights(List<PHBridgeResource> phBridgeResources) {

            }

            @Override
            public void onSearchComplete() {

            }

            @Override
            public void onSuccess() {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    private static void callOnCompletedListener(OnCompletedListener onCompletedListener) {
        if(onCompletedListener != null) {
            onCompletedListener.onCompleted();
        }
    }

    public static boolean changeLightName(String lightId, String lightName, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromId(lightId, bridge);
        light.setName(lightName);
        bridge.updateLight(light, new PHLightListener() {
            @Override
            public void onSuccess() {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
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

    public static void changeBrightness(String lightId, int brightness, MainActivity activity, OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromId(lightId, bridge);
        changeBrightness(light, brightness, activity, onCompletedListener);
    }

    public static void changeBrightness(PHLight light, int brightness, MainActivity activity, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        ColorCycle.removePreviousColorCycle(bridge, light.getIdentifier(), false, activity);
        bridge.updateLightState(light, getBrightnessState(brightness, !light.getLastKnownLightState().isOn()), new PHLightListener() {
            @Override
            public void onReceivingLightDetails(PHLight phLight) {

            }

            @Override
            public void onReceivingLights(List<PHBridgeResource> phBridgeResources) {

            }

            @Override
            public void onSearchComplete() {

            }

            @Override
            public void onSuccess() {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }


    public static void changeBulbColor(String lightId, float[] xY, MainActivity activity, OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        PHLight light = getLightFromId(lightId, bridge);
        changeBulbColor(light, xY, activity, onCompletedListener);
    }

    private static void changeBulbColor(PHLight light, float[] xY, MainActivity activity, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        ColorCycle.removePreviousColorCycle(bridge, light.getIdentifier(), false, activity);
        bridge.updateLightState(light, getColorState(xY, !light.getLastKnownLightState().isOn()), new PHLightListener() {
            @Override
            public void onReceivingLightDetails(PHLight phLight) {

            }

            @Override
            public void onReceivingLights(List<PHBridgeResource> phBridgeResources) {

            }

            @Override
            public void onSearchComplete() {

            }

            @Override
            public void onSuccess() {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
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

    // 100 scale -> 254 scale
    public static int convertBrightness(int brightness){
        return (int) Math.round((brightness/100.0) * 254.0);
    }

    // 254 scale -> 100 scale
    public static int revertBrightness(int convertedBrightness){
        return (int) Math.round(convertedBrightness/254.0 * 100.0);
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

    private static Map<String, PHGroup> getAllGroups() {
        return PHHueSDK.getInstance().getSelectedBridge().getResourceCache().getGroups();
    }

    public static boolean isGroupReachable(String groupId) {
        if(DEFAULT_GROUP_ID.equals(groupId)) {
            return isGroupReachable(new ArrayList<String>(getAllLights().keySet()));
        } else {
            return isGroupReachable(getAllGroups().get(groupId).getLightIdentifiers());
        }
    }

    private static boolean isGroupReachable(List<String> lightIds) {
        Map<String, PHLight> lights = getAllLights();
        for(String id : lightIds) {
            if(lights.get(id).getLastKnownLightState().isReachable()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGroupOff(String groupId) {
        if(DEFAULT_GROUP_ID.equals(groupId)) {
            return isGroupOff(new ArrayList<String>(getAllLights().keySet()));
        } else {
            return isGroupOff(getAllGroups().get(groupId).getLightIdentifiers());
        }
    }

    private static boolean isGroupOff(List<String> lightIds) {
        Map<String, PHLight> lights = getAllLights();
        for(String id : lightIds) {
            if(lights.get(id).getLastKnownLightState().isReachable() && lights.get(id).getLastKnownLightState().isOn()) {
                return false;
            }
        }
        return true;
    }

    public static Integer getGroupColor(String groupId) {
        if(DEFAULT_GROUP_ID.equals(groupId)) {
            return getGroupColor(new ArrayList<String>(getAllLights().keySet()));
        } else {
            return getGroupColor(getAllGroups().get(groupId).getLightIdentifiers());
        }
    }

    private static Integer getGroupColor(List<String> lightIds) {
        Map<String, PHLight> lights = getAllLights();
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
        if(previousColor == null) {
            return null;
        }
        return PHUtilities.colorFromXY(previousColor, COLOR_XY_MODEL_FOR_HUE);
    }

    public static void toggleBulbGroupState(String groupId, MainActivity activity) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        ColorCycle.removePreviousColorCycle(bridge, groupId, true, activity);
        if(DEFAULT_GROUP_ID.equals(groupId)) {
            bridge.setLightStateForDefaultGroup(getOnState(getToggledBulbGroupState(new ArrayList<String>(getAllLights().keySet()))));
        } else {
            bridge.setLightStateForGroup(groupId, getOnState(getToggledBulbGroupState(getAllGroups().get(groupId).getLightIdentifiers())));
        }

    }

    private static boolean getToggledBulbGroupState(List<String> lightIds) {
        Map<String, PHLight> lights = getAllLights();
        boolean shouldSetOn = true;
        for(String id : lightIds) {
            if(lights.get(id).getLastKnownLightState().isReachable() && lights.get(id).getLastKnownLightState().isOn()) {
                shouldSetOn = false;
                break;
            }
        }
        return shouldSetOn;

    }

    public static void changeGroupName(String identifier, String groupName, final OnCompletedListener onCompletedListener) {
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
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }



    public static void turnGroupOnOff(String identifier, final boolean on, MainActivity activity, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        ColorCycle.removePreviousColorCycle(bridge, identifier, true, activity);
        //same for default group as group
        bridge.setLightStateForGroup(identifier, getOnState(on), new PHGroupListener() {
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
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    public static void changeGroupBrightness(String identifier, int brightness, MainActivity activity, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        ColorCycle.removePreviousColorCycle(bridge, identifier, true, activity);

        bridge.setLightStateForGroup(identifier, getBrightnessState(brightness, true), new PHGroupListener() {
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
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });

    }

    public static void changeGroupColor(String identifier, float[] newColor, MainActivity activity, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        ColorCycle.removePreviousColorCycle(bridge, identifier, true, activity);
        bridge.setLightStateForGroup(identifier, getColorState(newColor, true), new PHGroupListener() {
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
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

            }
        });
    }

    public static String getNextGroupId() {
        return "Group " + (2 + getAllGroups().size());
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
                callOnCompletedListener(onCompletedListener);
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

                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                System.out.println("Error Group");
                //TOOD handle error when
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {
                System.out.println("State update");
            }
        });
    }

    public static void deleteGroup(PHGroup phGroup, final OnCompletedListener onCompletedListener) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        if(phGroup == null) {
            callOnCompletedListener(onCompletedListener);
            return;
        }
        bridge.deleteGroup(phGroup.getIdentifier(), new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onReceivingGroupDetails(PHGroup phGroup) {

            }

            @Override
            public void onReceivingAllGroups(List<PHBridgeResource> phBridgeResources) {

            }

            @Override
            public void onSuccess() {
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
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

    public static void activateFavorite(Favorite favorite, MainActivity activity) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        if(favorite.getIsAll()) {
            bridge.setLightStateForDefaultGroup(favorite.getAllLightState());
        } else {
            for (String lightId : favorite.getLightStates().keySet()) {
                PHLightState lightState = favorite.getLightStates().get(lightId);
                //account for deleted lights or change in bridge
                PHLight light = getLightFromId(lightId, bridge);
                if(light != null) {
                    ColorCycle.removePreviousColorCycle(bridge, lightId, false, activity);
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
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
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
                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {
                callOnCompletedListener(onCompletedListener);
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
                callOnCompletedListener(onCompletedListener);
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

                callOnCompletedListener(onCompletedListener);
            }

            @Override
            public void onError(int i, String s) {
                System.out.println("Error Group");
                callOnCompletedListener(onCompletedListener);
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

    public static String getGroupName(String groupId) {
        if(DEFAULT_GROUP_ID.equals(groupId)) {
            return "All Lights";
        }
        return PHHueSDK.getInstance().getSelectedBridge().getResourceCache().getGroups().get(groupId).getName();
    }

    public static int getGroupSize(String groupId) {
        if(DEFAULT_GROUP_ID.equals(groupId)) {
            return getAllLights().size();
        } else {
            return getAllGroups().get(groupId).getLightIdentifiers().size();
        }
    }
}
