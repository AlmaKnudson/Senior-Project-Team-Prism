package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.List;


public class MainActivity extends Activity implements PHSDKListener{

    private PHHueSDK hueBridgeSdk;
    private Dialog dialog;
    private Button homeButton;
    private Button voiceButton;
    private ImageButton settingsButton;
    private int connectionLostCount = 0;
    public static int MIN_CONNECTION_LOST_COUNT=1;

    //TODO: I might need to find better way...
    protected PHSchedule currentSchedule; // this is for passing schedule from fragment to fragment.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentSchedule = null;

        setContentView(R.layout.activity_main);
        homeButton = (Button) findViewById(R.id.homeButton);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        settingsButton.setEnabled(false);//TODO do this in the xml if possible
        voiceButton = (Button) findViewById(R.id.voiceButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(getFragmentManager().findFragmentById(R.id.container) instanceof HomeFragment)) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container, new HomeFragment());
                    fragmentTransaction.addToBackStack("home");
                    fragmentTransaction.commit();
                }
            }
        });
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(getFragmentManager().findFragmentById(R.id.container) instanceof VoiceFragment)) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container, new VoiceFragment());
                    fragmentTransaction.addToBackStack("settings");
                    fragmentTransaction.commit();
                }
            }
        });
        hueBridgeSdk = PHHueSDK.getInstance();
        hueBridgeSdk.setAppName("Prism Lights");
        hueBridgeSdk.setDeviceName(Build.MODEL);
        hueBridgeSdk.getNotificationManager().registerSDKListener(this);
        CharSequence waitingText = "";
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
                waitingText = getText(R.string.connecting);
            }
        } else {
            PHBridgeSearchManager bridgeSearchManager = (PHBridgeSearchManager) hueBridgeSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
            bridgeSearchManager.search(true, true);
            waitingText = getText(R.string.searching);
        }

        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        TextView textView = new TextView(this);
        textView.setText(waitingText);
        textView.setTextColor(Color.WHITE);
        dialog.setContentView(textView);
        //end code from example app
    }

    public void searchForBridge() {
        PHBridgeSearchManager bridgeSearchManager = (PHBridgeSearchManager) hueBridgeSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        bridgeSearchManager.search(true, true);
        CharSequence waitingText = getText(R.string.searching);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        TextView textView = new TextView(this);
        textView.setText(waitingText);
        textView.setTextColor(Color.WHITE);
        dialog.setContentView(textView);
    }


    @Override
    public void onCacheUpdated(List<Integer> integers, PHBridge phBridge) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.container);
                if(currentFragment instanceof CacheUpdateListener) {
                    CacheUpdateListener fragment = (CacheUpdateListener) currentFragment;
                    fragment.cacheUpdated();
                }
            }
        });
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
        hueBridgeSdk.getHeartbeatManager().enableLightsHeartbeat(phBridge, 1000);
        connectionLostCount = 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, new HomeFragment());
                fragmentTransaction.commit();
                dialog.setCancelable(true);
                dialog.cancel();
                //enable tab buttons so we can use them
                settingsButton.setEnabled(true);
                voiceButton.setEnabled(true);
                homeButton.setEnabled(true);
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
                dialog.setCancelable(true);
                dialog.cancel();
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
            dialog.setCancelable(true);
            dialog.cancel();
        }
    }

    @Override
    /**
     * From API:
     * Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found
     */
    public void onError(final int code, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(code == PHHueError.BRIDGE_NOT_RESPONDING) {
                    searchForBridge();
                    return;
                }
                //TODO use code rather than message
                if(message.equals("No bridge found")) {
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setContentView(R.layout.dialog_warning);
                    return;
                }
                if(code == PHHueError.NO_CONNECTION) {
                    return;
                }
                Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
                toast.show();

            }
        });
    }

    @Override
    public void onConnectionResumed(PHBridge bridge) {
        if(connectionLostCount > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog.isShowing()) {
                        dialog.cancel();
                    } else {
                        dialog.setCancelable(true);
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.setContentView(R.layout.dialog_warning);
                        TextView dialogTitle = (TextView) (dialog.findViewById(R.id.dialogTitle));
                        dialogTitle.setText(getText(R.string.connection_found));
                        TextView dialogText = (TextView) (dialog.findViewById(R.id.textExplanation));
                        dialogText.setText(getText(R.string.connection_found_message));
                        dialog.show();
                    }
                }
            });
        }
        connectionLostCount = 0;
    }

    @Override
    /**
     * From API:
     * Here you would handle the loss of connection to your bridge.
     */
    public void onConnectionLost(PHAccessPoint phAccessPoint) {
        connectionLostCount++;
        if(connectionLostCount == MIN_CONNECTION_LOST_COUNT) {
            //TODO make it stop searching for the bridge
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setContentView(R.layout.dialog_warning);
                    TextView dialogTitle = (TextView) (dialog.findViewById(R.id.dialogTitle));
                    dialogTitle.setText(getText(R.string.connection_lost));
                    TextView dialogText = (TextView) (dialog.findViewById(R.id.textExplanation));
                    dialogText.setText(getText(R.string.connection_lost_message));
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void onParsingErrors(List<PHHueParsingError> phHueParsingErrors) {

    }
}
