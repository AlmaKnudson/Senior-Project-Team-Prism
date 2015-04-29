package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHSchedule;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

import app.lights.prism.com.prismlights.receiver.BroadCastAlarmReceiver;


public class MainActivity extends Activity implements PHSDKListener{


    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("Prism Lights", ESTIMOTE_PROXIMITY_UUID, null, null);
    private BeaconManager beaconManager;
    private int threshold = 2; //There has to be number of threshold readings in a row of a range change before anything happens.
    private HashMap<String, Integer> beaconInRangeMapCount;
    private HashMap<String, Integer> beaconOutOfRangeMapCount;

    private static final String DEBUG_TAG = "MainActivity";

    private BeaconAssociationListener beaconAssociationListener;
    private PHHueSDK hueBridgeSdk;
    private Dialog dialog;
    private Button homeButton;
    private Button musicButton;
    private Button voiceButton;
    private ImageButton settingsButton;
    private int connectionLostCount = 0;
    private Date sunrise;
    private Date sunset;
    public static int MIN_CONNECTION_LOST_COUNT=2;
    public static final String homeFragmentTag="HOME_FRAGMENT";
    public static final String musicFragmentTag="MUSIC_FRAGMENT_TAG";
    public static final String voiceFragmentTag="VOICE_FRAGMENT_TAG";
    private static final String settingsFragmentTag = "SETTINGS_FRAGMENT";
    private static final String colorCycleFileName = "colorCycle.out";
    private static final String colorCycleTaskFileName = "colorCycleTask.out";
    private static final String colorCycleTasksGroupFileName = "colorCycleTaskGroup.out";
    public  static final String REAL_HOME_FRAGMENT = "REAL_HOME_FRAGMENT";
    private String prevLatitude;
    private String prevLongitude;
    private boolean authenticating = false;

    private String attemptingToConnectIP;
    private Map<String, PHBridge> currentConnectedBridges; //since I can't seem to disconnect them
    //colorCycle utilities
    private List<ColorCycle> colorCycles;
    private boolean connecting = false;

    public MainActivity() {
        super();
        currentConnectedBridges = new HashMap<String, PHBridge>();
    }

    public List<ColorCycle> getAllColorCycles(){
        return  colorCycles;
    }
    public void setColorCycle(int i, ColorCycle colorCycle) {
        colorCycles.set(i, colorCycle);
    }
    public void deleteColorCycle(int i){
        colorCycles.remove(i);
    }
    public void addColorCycle(ColorCycle colorCycle){
        colorCycles.add(colorCycle);
    }
    public int containsCycleName(String newName) {
        List<ColorCycle> list = getAllColorCycles();
        int nameExist = -1;
        for(int i = 0; i<list.size();i++)
        {
            if(list.get(i).getName().equals(newName))
                nameExist = i;
        }
        return nameExist;
    }

    //for passing colorCycleTasks
    private Map<String,List<ScheduledFuture>> colorCycleTasks;
    private Map<String,List<ScheduledFuture>> colorCycleTasksGroup;
    public void setColorCycleTasks(String identifier, List<ScheduledFuture> tasks, boolean isGroup){
        if (!isGroup)
            colorCycleTasks.put(identifier, tasks);
        else
            colorCycleTasksGroup.put(identifier,tasks);
    }
    public List<ScheduledFuture> getColorCycleTasks(String identifier, boolean isGroup){
        if(!isGroup)
            return colorCycleTasks.get(identifier);
        else
            return colorCycleTasksGroup.get(identifier);
    }

    // for passing schedule between fragments
    private PHSchedule currentPHschedule;
    public PHSchedule getCurrentPHschedule(){
        return currentPHschedule;
    }
    public void setCurrentPHschedule(PHSchedule schedule){
        currentPHschedule = schedule;
    }

    // for passin schedule from scheduleFragment to scheduleConfigFragment
    private Schedule currentSchedule;
    public Schedule getCurrentSchedule(){
        return currentSchedule;
    }
    public void setCurrentSchedule(Schedule schedule){
        currentSchedule = schedule;
    }

    private String currentColorCycleName;
    public String getCurrentColorCycleName(){
        return currentColorCycleName;
    }
    public void setCurrentColorCycleName(String name){
        currentColorCycleName = name;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* BEACON/BLUETOOTH START */
        beaconInRangeMapCount = new HashMap<String, Integer>();
        beaconOutOfRangeMapCount = new HashMap<String, Integer>();
        beaconManager = new BeaconManager(this.getApplicationContext());
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (rangedBeacons.size() == 0)
                            return;
                        else {
                            if (beaconAssociationListener != null) {
                                beaconAssociationListener.onBeaconAssociation(getBeaconId(rangedBeacons.get(0)));
                            }
//                            BeaconFragment myFragment = (BeaconFragment)getFragmentManager().findFragmentByTag("BEACON_FRAGMENT");
//                            if (myFragment.isVisible()) {
//                               System.out.println("MADE IT");
//                                System.exit(0);
//                            }

                            for (Beacon beacon : rangedBeacons) {
                                boolean beaconInRange = false;
                                String beaconId = getBeaconId(beacon);
                                String associations = HueSharedPreferences.getInstance(getApplicationContext()).getBeaconOrBulbAssociations(beaconId);
                                Scanner s = new Scanner(associations);
                                while (s.hasNextLine()) {

                                    String currentLine = s.nextLine();
                                    if (currentLine.trim().equals(""))
                                        return;
                                    String[] result = currentLine.split("~!~");
//                                    String beaconId = result[0];
                                    String bulbId = result[1];
                                    String range = result[2];
                                    boolean isGroup = Boolean.parseBoolean(result[3]);
                                    double distance = Utils.computeAccuracy(beacon);
                                    if (((int) (distance * 3)) <= Integer.parseInt(range)) {
                                        beaconInRange = true;
                                    }
//                                    else {
//                                        HueBulbChangeUtility.turnBulbOnOff(bulbId, false);
//                                    }
//                                    String beaconId = getBeaconId(beacon);
                                    if (beaconInRangeMapCount.containsKey(beaconId)) {
                                        if(beaconInRange){
                                            int count = beaconInRangeMapCount.get(beaconId);
                                            if((count+1) >= threshold){
                                                if(isGroup){
                                                    HueBulbChangeUtility.turnGroupOnOff(bulbId, true, false, MainActivity.this, null);
                                                } else {
                                                    HueBulbChangeUtility.turnBulbOnOff(bulbId, true,false, MainActivity.this, null);
                                                }
                                                beaconInRangeMapCount.put(beaconId, 0);
                                            } else {
                                                beaconInRangeMapCount.put(beaconId, count + 1);
                                            }
                                        } else {
                                            beaconInRangeMapCount.put(beaconId, 0);
                                        }
                                    } else {
                                        if(beaconInRange)
                                            beaconInRangeMapCount.put(beaconId, 1);
                                        else
                                            beaconInRangeMapCount.put(beaconId, 0);
                                    }

                                    if (beaconOutOfRangeMapCount.containsKey(beaconId)) {
                                        if(!beaconInRange){
                                            int count = beaconOutOfRangeMapCount.get(beaconId);
                                            if( (count+1) >= threshold){
                                                if(isGroup){
                                                    HueBulbChangeUtility.turnGroupOnOff(bulbId, false, false, MainActivity.this, null);
                                                } else {
                                                    HueBulbChangeUtility.turnBulbOnOff(bulbId, false, false, MainActivity.this, null);
                                                }
                                                beaconOutOfRangeMapCount.put(beaconId, 0);
                                            } else
                                                beaconOutOfRangeMapCount.put(beaconId, count + 1);
                                        } else {
                                            beaconOutOfRangeMapCount.put(beaconId, 0);
                                        }
                                    } else {
                                        if(!beaconInRange)
                                            beaconOutOfRangeMapCount.put(beaconId, 1);
                                        else
                                            beaconOutOfRangeMapCount.put(beaconId, 0);
                                    }
                                }



                            }
                        }
                    }
                });
            }

            private String getBeaconId(Beacon beacon){
                return beacon.getMajor() + "." + beacon.getMinor();
            }
        });



        //Start bluetooth beacon ranging:
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                try {
//                    addAssociation = true;
                    beaconManager.setBackgroundScanPeriod(100, 100);
                    if(beaconManager.isBluetoothEnabled()) {
                        beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                    } else {
                        System.out.println("NEED TO ENABLE BLUETOOTH");
                    }
                } catch (RemoteException e) {
                    System.out.println("Cannot start ranging" + e);
                }
            }
        });
        /* BEACON/BLUETOOTH END */


        // get Stored sunrise and sunset time
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if(settings.contains("sunrise")) {
            sunrise = new Date(settings.getLong("sunrise", 0));
            sunset = new Date(settings.getLong("sunset", 0));
        }
        else {
            sunrise = null;
            sunset = null;
        }
        if(settings.contains("latitude")) {
            prevLatitude = settings.getString("latitude", "0");
            prevLongitude = settings.getString("longitude", "0");
        }
        else{
            prevLatitude = null;
            prevLongitude = null;
        }

        // get Stored colorCycles
        try {
            File file = new File(this.getFilesDir(), colorCycleFileName);
            FileInputStream in = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(in);
            colorCycles = (ArrayList<ColorCycle>) (ois.readObject());
            ois.close();
            in.close();

        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Getting colorCycle info failed: "+ e);
            colorCycles = new ArrayList<>();

            //add Demo cycle
            ColorCycle demoCycle = new ColorCycle("My First Cycle");
            float[] color = {0.1658f,0.0504f};
            float[] color2 = {0.4087f,0.5158f};
            float[] color3 = {0.6091f,0.3294f};
            float[] color4 = {0.2207f,0.1386f};
            float[] color5 = {0.3452f,0.1879f};
            float[] color6 = {0.4407f,0.5200f};

            demoCycle.add(color, 100, 7, 2 );
            demoCycle.add(color2, 20, 7, 2 );
            demoCycle.add(color3, 100, 7, 2 );
            demoCycle.add(color4, 20, 7, 2 );
            demoCycle.add(color5, 100, 7, 2 );
            demoCycle.add(color6, 20, 7, 2 );

            colorCycles.add(demoCycle);
        }

        // get Stored colorCycleTasks
        try {
            File file = new File(this.getFilesDir(), colorCycleTaskFileName);
            FileInputStream in = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(in);
            colorCycleTasks = (Map<String,List<ScheduledFuture>>) (ois.readObject());
            ois.close();
            in.close();

        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Getting colorCycleTasks info failed: "+ e);
            colorCycleTasks = new HashMap<>();
        }
        // get Stored colorCycleTasks for Group
        try {
            File file = new File(this.getFilesDir(), colorCycleTasksGroupFileName);
            FileInputStream in = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(in);
            colorCycleTasksGroup = (Map<String,List<ScheduledFuture>>) (ois.readObject());
            ois.close();
            in.close();

        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Getting colorCycleTaskGroup info failed: "+ e);
            colorCycleTasksGroup = new HashMap<>();
        }

        currentSchedule = null;

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, new SettingsFragment());
        fragmentTransaction.addToBackStack(settingsFragmentTag);
        fragmentTransaction.commit();


        setContentView(R.layout.activity_main);
        homeButton = (Button) findViewById(R.id.homeButton);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        musicButton = (Button) findViewById(R.id.musicButton);
        voiceButton = (Button) findViewById(R.id.voiceButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBackStack();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, new RealHomeFragment(), homeFragmentTag);
                fragmentTransaction.addToBackStack(homeFragmentTag);
                fragmentTransaction.commit();
            }
        });
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(getCurrentFragment() instanceof MusicFragment)) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container, new MusicFragment(), musicFragmentTag);
                    fragmentTransaction.addToBackStack(musicFragmentTag);
                    fragmentTransaction.commit();
                }
            }
        });
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(getCurrentFragment() instanceof VoiceFragment)) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container, new VoiceFragment(), voiceFragmentTag);
                    fragmentTransaction.addToBackStack(voiceFragmentTag);
                    fragmentTransaction.commit();
                }
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(getCurrentFragment() instanceof SettingsFragment)) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container, new SettingsFragment());
                    fragmentTransaction.addToBackStack(settingsFragmentTag);
                    fragmentTransaction.commit();
                }
            }
        });
        hueBridgeSdk = PHHueSDK.getInstance();
        hueBridgeSdk.setAppName("Prism Lights");
        hueBridgeSdk.setDeviceName(Build.MODEL);
        hueBridgeSdk.getNotificationManager().registerSDKListener(this);
        connectToBridge(true);
    }

    private void connectToBridge(boolean create) {
        if(create || (connecting && (dialog == null || !dialog.isShowing()))) {
            CharSequence waitingText = "";
            //code from example app
            HueSharedPreferences prefs = HueSharedPreferences.getInstance(getApplicationContext());
            String lastIpAddress = prefs.getLastConnectedIPAddress();
            String lastUsername = prefs.getUsername();
            if (lastIpAddress != null && !lastIpAddress.equals("")) {
                PHAccessPoint lastAccessPoint = new PHAccessPoint();
                lastAccessPoint.setIpAddress(lastIpAddress);
                lastAccessPoint.setUsername(lastUsername);
                if (!hueBridgeSdk.isAccessPointConnected(lastAccessPoint)) {
                    connecting = true;
                    attemptingToConnectIP = lastAccessPoint.getIpAddress();
                    hueBridgeSdk.connect(lastAccessPoint);
                    waitingText = getText(R.string.connecting);
                } else {
                    openHomeScreen(); //already connected, can go directly to homescreen
                    return;
                }

            } else {
                PHBridgeSearchManager bridgeSearchManager = (PHBridgeSearchManager) hueBridgeSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
                bridgeSearchManager.search(true, true);
                waitingText = getText(R.string.searching);
            }
            DialogCreator.showLoadingDialog(waitingText.toString(), this);
        }
        else if (dialog == null || !dialog.isShowing()){
            //only search if not already connecting
            searchForBridge();
        }

    }

    public void saveColorCycles() {
        //save the color cycles
        if(sunrise!= null && sunset != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            settings.edit().putLong("sunrise", sunrise.getTime()).commit();
            settings.edit().putLong("sunset", sunset.getTime()).commit();
        }
        if(prevLatitude != null && prevLongitude!=null){
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            settings.edit().putString("latitude", prevLatitude).commit();
            settings.edit().putString("longitude", prevLongitude).commit();
        }
        if(colorCycles != null){
            try {
                File file = new File(this.getFilesDir(), colorCycleFileName);
                FileOutputStream out = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(colorCycles);
                oos.flush();
                oos.close();
                out.close();
            } catch (Exception e) {
                System.out.println("Problem Saving colorCycle: " + e);
            }
        }
        if(colorCycleTasks != null){
            try {
                File file = new File(this.getFilesDir(), colorCycleTaskFileName);
                FileOutputStream out = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(colorCycleTasks);
                oos.flush();
                oos.close();
                out.close();
            } catch (Exception e) {
                System.out.println("Problem Saving colorCycleTasks: " + e);
            }
        }
        if(colorCycleTasks != null){
            try {
                File file = new File(this.getFilesDir(), colorCycleTasksGroupFileName);
                FileOutputStream out = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(colorCycleTasksGroup);
                oos.flush();
                oos.close();
                out.close();
            } catch (Exception e) {
                System.out.println("Problem Saving colorCycleTasksGroup: " + e);
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("PAUSED");
        saveColorCycles();
        //turn of the internet connection
        PHHueSDK.getInstance().disableAllHeartbeat();
        PHHueSDK.getInstance().getNotificationManager().unregisterSDKListener(this);
        DialogCreator.cancelShowingDialogIfProgress(this);
        //consider destroying the sdk
    }

    @Override
    protected void onResume() {
        super.onResume();
        PHBridge bridge = hueBridgeSdk.getSelectedBridge();
        hueBridgeSdk.getNotificationManager().registerSDKListener(this);
        if(bridge != null) {
            hueBridgeSdk.enableHeartbeat(hueBridgeSdk.getSelectedBridge(), PHHueSDK.HB_INTERVAL);
            hueBridgeSdk.getHeartbeatManager().enableLightsHeartbeat(bridge, 2000);
            if(getCurrentFragment() instanceof SettingsFragment) {
                openHomeScreen();
            }
        } else {
            //restart searching progress
            connectToBridge(false);
        }
    }

    public void searchForBridge() {
        PHBridgeSearchManager bridgeSearchManager = (PHBridgeSearchManager) hueBridgeSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        bridgeSearchManager.search(true, true);
        CharSequence waitingText = getText(R.string.searching);
        DialogCreator.showLoadingDialog(waitingText.toString(), this);
    }


    @Override
    public void onCacheUpdated(List<Integer> integers, PHBridge phBridge) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment currentFragment = getCurrentFragment();
                if (currentFragment instanceof CacheUpdateListener) {
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
    public void onBridgeConnected(final PHBridge phBridge) {
        authenticating = false;
        hueBridgeSdk.setSelectedBridge(phBridge);
        hueBridgeSdk.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
        hueBridgeSdk.getHeartbeatManager().enableLightsHeartbeat(phBridge, 2000);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(attemptingToConnectIP != null) {
                    currentConnectedBridges.put(attemptingToConnectIP, phBridge);
                    attemptingToConnectIP = null;
                }
                connectionLostCount = 0;
                //only open the home screen if really new connection or at least not when unexpected
                Fragment currentFragment = getCurrentFragment();
                if(currentFragment instanceof SettingsFragment || currentFragment instanceof PushButtonFragment) {
                    openHomeScreen();
                }
            }
        });
    }


    private void openHomeScreen() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, new RealHomeFragment(), REAL_HOME_FRAGMENT);
        fragmentTransaction.commit();
        DialogCreator.cancelShowingDialog(this);
        //enable tab buttons so we can use them
        voiceButton.setEnabled(true);
        settingsButton.setEnabled(true);
        musicButton.setEnabled(true);
        homeButton.setEnabled(true);
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
                if(!authenticating) {
                    authenticating = true;
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container, new PushButtonFragment());
                    fragmentTransaction.addToBackStack("authenticationRequired");
                    fragmentTransaction.commit();
                    DialogCreator.cancelShowingDialog(MainActivity.this);
                }
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
    public void onAccessPointsFound(final List<PHAccessPoint> accessPoints) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast toast = Toast.makeText(MainActivity.this, "Access Points Found", Toast.LENGTH_LONG);
//                toast.show();
//            }
//        });
        if(accessPoints != null) {
            if(accessPoints.size() == 1) {
                PHAccessPoint accessPoint = accessPoints.get(0);
                connectToAccessPoint(accessPoint);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(dialog.isShowing()) {
                            dialog.setCancelable(true);
                            dialog.cancel();
                        }
                        Fragment fragment = getCurrentFragment();
                        if(fragment instanceof SettingsFragment) {
                            SettingsFragment settingsFragment = (SettingsFragment) fragment;
                            settingsFragment.setAccessPoints(accessPoints);
                        }
                    }
                });
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogCreator.showWarningDialog(getText(R.string.no_bridge_found).toString(), getText(R.string.no_bridge_message).toString(), MainActivity.this);
                }
            });
        }
    }

    public void connectToAccessPoint(final PHAccessPoint accessPoint) {
        HueSharedPreferences preferences = HueSharedPreferences.getInstance(this.getApplicationContext());
        accessPoint.setUsername(preferences.getUsername());
        preferences.setLastConnectedIPAddress(accessPoint.getIpAddress());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                attemptingToConnectIP= accessPoint.getIpAddress();
                if(!hueBridgeSdk.isAccessPointConnected(accessPoint)) {
                    hueBridgeSdk.connect(accessPoint);
                } else if(currentConnectedBridges.containsKey(accessPoint.getIpAddress())) {
                    hueBridgeSdk.setSelectedBridge(currentConnectedBridges.get(accessPoint.getIpAddress()));
                    connecting = false;
                    openHomeScreen();
                }else {

                    connecting = false;
                    openHomeScreen();
                }
            }
        });
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
                if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
                    //seems to only happen when connecting to bridge that's there but won't respond
                    if (!connecting) {
                        DialogCreator.showWarningDialog(getText(R.string.could_not_connect).toString(), getText(R.string.could_not_connect_explanation).toString(), MainActivity.this);
                    } else {
                        connecting = false;
                        searchForBridge();
                    }
                    return;
                }
                if (message.equals("No bridge found")) {
                    DialogCreator.showWarningDialog(getText(R.string.no_bridge_found).toString(), getText(R.string.no_bridge_message).toString(), MainActivity.this);
                    return;
                }
                if (code == PHHueError.NO_CONNECTION) {
                    return;
                }
                if(code == PHHueError.AUTHENTICATION_FAILED) {
                    authenticating = false;
                }
//                Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
//                toast.show();

            }
        });
    }

    @Override
    public void onConnectionResumed(PHBridge bridge) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(connectionLostCount >= MIN_CONNECTION_LOST_COUNT) {
                    connectionLostCount = 0;
                    if (dialog != null && dialog.isShowing()) {
                        dialog.cancel();
                    } else {
                        DialogCreator.showWarningDialog(getText(R.string.connection_found).toString(), getText(R.string.connection_found_message).toString(), MainActivity.this);
                    }
                }
            }
        });
    }

    public void showAuthenticationFailedDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.cancel();
        } else {
            DialogCreator.showWarningDialog(getText(R.string.authenticationFailed).toString(), getText(R.string.authenticationFailedExplanation).toString(), this);
        }
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
                connectionLostCount++;
                if(connectionLostCount == MIN_CONNECTION_LOST_COUNT) {
                    DialogCreator.showWarningDialog(getText(R.string.connection_lost).toString(), getText(R.string.connection_lost_message).toString(), MainActivity.this);
                }
            }
        });
    }

    public Dialog getDialog() {
        return dialog;
    }


    @Override
    public void onParsingErrors(List<PHHueParsingError> phHueParsingErrors) {

    }

    public void clearBackStack() {
        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /* this function starts recurring service.
    *  Once this is called, SmartSunService will be triggered every day.
    */
    public void setAlarmBroadcasting(Context context) {
        Calendar updateTime = Calendar.getInstance();
        updateTime.setTimeZone(TimeZone.getDefault());
        updateTime.set(Calendar.HOUR_OF_DAY, 01);
        updateTime.set(Calendar.MINUTE, 00);

        Intent downloader = new Intent(context, BroadCastAlarmReceiver.class);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(
                Context.ALARM_SERVICE);
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                recurringDownload);
//        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
//                updateTime.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES,
//                recurringDownload);
//        alarms.setRepeating(AlarmManager.RTC_WAKEUP,
//                updateTime.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES,
//                recurringDownload);
    }

    //TODO: add enable or disable SmartSun option in general option.
    /*
     this function cancel recurring service.
     */
    private void cancelAlarmBroadcasting(Context context) {
        Intent downloader = new Intent(context, BroadCastAlarmReceiver.class);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(
                Context.ALARM_SERVICE);
        alarms.cancel(recurringDownload);
    }

    public Date getSunrise(){
        // if sunrise is yesterday, get new one other wise just return sunrise
        if(sunrise == null || !DateUtils.isToday(sunrise.getTime())) {
            updateSunTime();
        }
        return sunrise;
    }

    public Date getSunset(){
        // if sunset is yesterday, get new one other wise just return sunrise
        if(sunrise == null || !DateUtils.isToday(sunset.getTime())) {
            updateSunTime();
        }
        return sunset;
    }

    private void updateSunTime() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

        //if there is no access to location make
        if (location!=null) {
            setLatitude(location.getLatitude());
            setLongitude(location.getLongitude());
            callWebservice(prevLatitude, prevLongitude);

        }else if (prevLatitude!=null && prevLongitude!=null){
            callWebservice(prevLatitude, prevLongitude);

        }else {
            // if no location is available, user can use previous sunrise sunset time...

            // if no location, and no previous location, no previous sunrise or sunset time, make one. this shouldn't happen though.
            if(sunrise==null) {
                sunrise = new Date();
                sunrise.setDate(sunrise.getDate() - 1);
                sunrise.setHours(6);
                sunrise.setMinutes(31);
                sunrise.setSeconds(00);
                sunset = new Date();
                sunset.setDate(sunset.getDate() - 1);
                sunset.setHours(20);
                sunset.setMinutes(21);
                sunset.setSeconds(00);
            }
            new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                    .setTitle("Warning")
                    .setMessage("There is no access to the current location. The sunrise and sunset time may not be accurate. Please try again later for more accurate result.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void callWebservice(String latitude, String longitude) {
        Date date = new Date();

        URL url;

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                url = new URL("http://www.earthtools.org/sun/" + latitude + "/"
                        + longitude + "/" + date.getMonth() + "/" + date.getDate() + "/99/1");
                DownloaderTask downloaderTask = new DownloaderTask();
                downloaderTask.execute(url);
            } catch (MalformedURLException e) {
                Log.e(DEBUG_TAG, "Bad URL getting sunset and sunrise", e);
            }
        } else {
            Log.e(DEBUG_TAG, "No network connection available.");
        }
    }


    public void setBeaconAssociationListener(BeaconAssociationListener beaconAssociationListener) {
        this.beaconAssociationListener = beaconAssociationListener;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public void setLatitude(double latitude) {
        prevLatitude = latitude+"";
    }

    public void setLongitude(double longitude) {
        prevLongitude = longitude+"";
    }

    public Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.container);
    }

    private class DownloaderTask extends AsyncTask<URL, Void, Boolean> {

        private static final String DEBUG_TAG = "MainActivity$DownloaderTask";

        @Override
        protected Boolean doInBackground(URL... params) {
            boolean succeeded = false;
            URL downloadPath = params[0];

            if (downloadPath != null) {
                succeeded = xmlParse(downloadPath);
            }
            return succeeded;
        }

        private boolean xmlParse(URL downloadPath) {

            boolean succeeded = false;

            XmlPullParser parser;

            try {
                parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(downloadPath.openStream(), null);
                int eventType = -1;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String tagName = parser.getName();
                        if (tagName.equals("sunrise")) {
                            parser.next();
                            String time = parser.getText();
                            Log.d(DEBUG_TAG, "sunrise: " + time);
                            sunrise = new Date();

                            int hour, min, sec;
                            try {
                                hour = Integer.parseInt(time.substring(0, 2));
                                min = Integer.parseInt(time.substring(3, 5));
                                sec = Integer.parseInt(time.substring(6, 8));

                                sunrise.setHours(hour);
                                sunrise.setMinutes(min);
                                sunrise.setSeconds(sec);
                            } catch (NumberFormatException e) {
                                Log.d(DEBUG_TAG, "Parsing Error: sunrise");
                            }

                        } else if (tagName.equals("sunset")) {
                            parser.next();
                            String time = parser.getText();
                            Log.d(DEBUG_TAG,
                                    "sunset: " + time);
                            sunset = new Date();

                            int hour, min, sec;
                            try {
                                hour = Integer.parseInt(time.substring(0, 2));
                                min = Integer.parseInt(time.substring(3, 5));
                                sec = Integer.parseInt(time.substring(6, 8));

                                sunset.setHours(hour);
                                sunset.setMinutes(min);
                                sunset.setSeconds(sec);
                            } catch (NumberFormatException e) {
                                Log.d(DEBUG_TAG, "Parsing Error: sunset");
                            }
                        }
                    }
                    eventType = parser.next();
                }
                // no exceptions during parsing
                succeeded = true;
            } catch (XmlPullParserException e) {
                Log.e(DEBUG_TAG, "Error during parsing", e);
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "IO Error during parsing", e);
            }

            return succeeded;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Log.w(DEBUG_TAG, "XML download and parse had errors");
            }
        }
    }

}
