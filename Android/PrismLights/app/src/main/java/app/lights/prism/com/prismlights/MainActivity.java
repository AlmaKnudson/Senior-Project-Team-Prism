package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;

import java.util.List;


public class MainActivity extends Activity implements PHSDKListener{

    PHHueSDK hueBridgeSdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hueBridgeSdk = PHHueSDK.getInstance();
        hueBridgeSdk.setAppName("Prism Lights");
        hueBridgeSdk.setDeviceName(Build.MODEL);
        hueBridgeSdk.getNotificationManager().registerSDKListener(this);
//        Dialog dialog = new ProgressDialog(this);
//        dialog.show();
//        TextView textView = new TextView(this);
//        textView.setText("Searching For Bridge...");
//        textView.setTextColor(Color.WHITE);
//        dialog.setContentView(textView);
        PHBridgeSearchManager bridgeSearchManager = (PHBridgeSearchManager) hueBridgeSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        bridgeSearchManager.search(true,true);
    }

    @Override
    public void onCacheUpdated(List<Integer> cacheNotificationsList, PHBridge bridge) {
        if (cacheNotificationsList.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
            System.out.println("Lights Cache Updated ");
        }
    }

    @Override
    /**
     * From API:
     * Here it is recommended to set your connected bridge in your sdk object (as above) and start the heartbeat.
     * At this point you are connected to a bridge so you should pass control to your main program/activity.
     * Also it is recommended you store the connected IP Address/ Username in your app here.  This will allow easy automatic connection on subsequent use.
     */
    public void onBridgeConnected(PHBridge phBridge) {
        hueBridgeSdk.setSelectedBridge(phBridge);
        hueBridgeSdk.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
    }

    @Override
    /**
     * From API:
     * Arriving here indicates that Pushlinking is required (to prove the User has physical access to the bridge).  Typically here
     * you will display a pushlink image (with a timer) indicating to to the user they need to push the button on their bridge within 30 seconds.
     */
    public void onAuthenticationRequired(PHAccessPoint accessPoint) {
        hueBridgeSdk.startPushlinkAuthentication(accessPoint);
    }

    @Override
    /**
     * From API:
     * Handle your bridge search results here.  Typically if multiple results are returned you will want to display them in a list
     * and let the user select their bridge.   If one is found you may opt to connect automatically to that bridge.
     */
    public void onAccessPointsFound(List<PHAccessPoint> accessPoints) {
        Toast toast = Toast.makeText(this, "Access Points Found", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    /**
     * From API:
     * Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found
     */
    public void onError(int i, String s) {
        Toast toast = Toast.makeText(this, "Error", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onConnectionResumed(PHBridge bridge) {

    }

    @Override
    /**
     * From API:
     * Here you would handle the loss of connection to your bridge.
     */
    public void onConnectionLost(PHAccessPoint phAccessPoint) {

    }

    @Override
    public void onParsingErrors(List<PHHueParsingError> phHueParsingErrors) {

    }
}
