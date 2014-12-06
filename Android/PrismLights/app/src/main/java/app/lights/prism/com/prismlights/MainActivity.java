package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
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
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;


public class MainActivity extends Activity implements PHSDKListener{

    PHHueSDK hueBridgeSdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hueBridgeSdk = PHHueSDK.getInstance();
//        hueBridgeSdk.setAppName("Prism Lights");
        hueBridgeSdk.setDeviceName(Build.MODEL);
        hueBridgeSdk.getNotificationManager().registerSDKListener(this);
        Dialog dialog = new ProgressDialog(this);
        dialog.show();
        TextView textView = new TextView(this);
        textView.setText("Searching For Bridge...");
        textView.setTextColor(Color.WHITE);
        dialog.setContentView(textView);
        //code from example app
        HueSharedPreferences prefs = HueSharedPreferences.getInstance(getApplicationContext());
        String lastIpAddress = prefs.getLastConnectedIPAddress();
        String lastUsername = prefs.getUsername();
        if (lastIpAddress !=null && !lastIpAddress.equals("")) {
            PHAccessPoint lastAccessPoint = new PHAccessPoint();
            lastAccessPoint.setIpAddress(lastIpAddress);
            lastAccessPoint.setUsername(lastUsername);
            if (!hueBridgeSdk.isAccessPointConnected(lastAccessPoint)) {
                hueBridgeSdk.connect(lastAccessPoint);
            }
        } else {
            PHBridgeSearchManager bridgeSearchManager = (PHBridgeSearchManager) hueBridgeSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
            bridgeSearchManager.search(true, true);
        }
        //end code from example app
    }

    @Override
    public void onCacheUpdated(int i, com.philips.lighting.model.PHBridge phBridge) {

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, new HomeFragment());
                fragmentTransaction.commit();
                Toast toast = Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    /**
     * From API:
     * Arriving here indicates that Pushlinking is required (to prove the User has physical access to the bridge).  Typically here
     * you will display a pushlink image (with a timer) indicating to to the user they need to push the button on their bridge within 30 seconds.
     */
    public void onAuthenticationRequired(PHAccessPoint accessPoint) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, new PushButtonFragment());
                fragmentTransaction.commit();
            }
        });
        hueBridgeSdk.startPushlinkAuthentication(accessPoint);
    }

    @Override
    /**
     * From API:
     * Handle your bridge search results here.  Typically if multiple results are returned you will want to display them in a list
     * and let the user select their bridge.   If one is found you may opt to connect automatically to that bridge.
     */
    public void onAccessPointsFound(List<PHAccessPoint> accessPoints) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(MainActivity.this, "Access Points Found", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        if(accessPoints != null && accessPoints.size() == 1) {
            HueSharedPreferences preferences = HueSharedPreferences.getInstance(this.getApplicationContext());
            PHAccessPoint accessPoint = accessPoints.get(0);
            accessPoint.setUsername(preferences.getUsername());
            preferences.setLastConnectedIPAddress(accessPoint.getIpAddress());
            hueBridgeSdk.connect(accessPoints.get(0));
        }
    }

    @Override
    /**
     * From API:
     * Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found
     */
    public void onError(int code, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(MainActivity.this, "connection lost", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
