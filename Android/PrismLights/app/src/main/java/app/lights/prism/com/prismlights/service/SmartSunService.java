package app.lights.prism.com.prismlights.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHSchedule;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import app.lights.prism.com.prismlights.HueSharedPreferences;

/**
 * Created by Brian_Oh on 3/23/15.
 * This service will be fired by BroadCastAlarmReceiver every day at certain time.
 */
public class SmartSunService extends Service {

    private static final String DEBUG_TAG = "SmartSunService";
    private DownloaderTask downloaderTask;
    private Date sunrise;
    private Date sunset;
    private PHHueSDK hueBridgeSdk;
    private PHBridge bridge;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        URL url;

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                url = new URL(intent.getDataString());
                downloaderTask = new DownloaderTask();
                downloaderTask.execute(url);
            } catch (MalformedURLException e) {
                Log.e(DEBUG_TAG, "Bad URL", e);
            }
        } else {
            Log.e(DEBUG_TAG, "No network connection available.");
        }

        return Service.START_FLAG_REDELIVERY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class DownloaderTask extends AsyncTask<URL, Void, Boolean> implements PHSDKListener{

        private static final String DEBUG_TAG = "SmartSunService$DownloaderTask";
        Boolean isDone;
        Boolean sunriseDone;
        Boolean sunsetDone;

        @Override
        protected Boolean doInBackground(URL... params) {
            boolean succeeded = false;
            URL downloadPath = params[0];

            if (downloadPath != null) {
                succeeded = xmlParse(downloadPath);
                if(succeeded)
                    succeeded = connectAndUpdate();
            }
            return succeeded;
        }

        private boolean connectAndUpdate() {
            boolean succeeded = false;
            isDone = false;
            hueBridgeSdk = PHHueSDK.getInstance();
            hueBridgeSdk.setAppName("Prism Lights");
            hueBridgeSdk.setDeviceName(Build.MODEL);
            hueBridgeSdk.getNotificationManager().registerSDKListener(this);

            HueSharedPreferences prefs = HueSharedPreferences.getInstance(getApplicationContext());
            String lastIpAddress = prefs.getLastConnectedIPAddress();
            String lastUsername = prefs.getUsername();
            if (lastIpAddress !=null && !lastIpAddress.equals("")) {
                PHAccessPoint lastAccessPoint = new PHAccessPoint();
                lastAccessPoint.setIpAddress(lastIpAddress);
                lastAccessPoint.setUsername(lastUsername);
                if (!hueBridgeSdk.isAccessPointConnected(lastAccessPoint)) {
                    hueBridgeSdk.connect(lastAccessPoint);
                    while(isDone == false)
                        ;
                }
                updateSchedules();
                succeeded = true;
            }
            else{
                Log.e(DEBUG_TAG,"There is no previous connection");
            }

            return succeeded;
        }

        private void updateSchedules() {

            bridge = hueBridgeSdk.getSelectedBridge();

            List<PHSchedule> schedules = bridge.getResourceCache().getAllSchedules(true);
            ArrayList<PHSchedule> sunriseSchedules = new ArrayList<>();
            ArrayList<PHSchedule> sunsetSchedules = new ArrayList<>();

            for (int i = 0; i < schedules.size(); i++) {
                PHSchedule schedule = schedules.get(i);
                if (schedule.getDescription().equals("prism,smartsun,1")) {
                    sunriseSchedules.add(schedule);
                } else if (schedule.getDescription().equals("prism,smartsun,2")) {
                    sunsetSchedules.add(schedule);
                }
            }

            if (sunriseSchedules.size() > 0)
                sunriseDone = false;
            else
                sunriseDone = true;

            if (sunsetSchedules.size() > 0)
                sunsetDone = false;
            else
                sunsetDone = true;

            for (int i = 0; i < sunriseSchedules.size(); i++) {
                final PHSchedule schedule = sunriseSchedules.get(i);
                schedule.setDate(sunrise);
                schedule.setAutoDelete(true);
                bridge.updateSchedule(schedule, new PHScheduleListener() {
                    @Override
                    public void onCreated(PHSchedule phSchedule) {

                    }

                    @Override
                    public void onSuccess() {

                        sunriseDone = true;
                    }

                    @Override
                    public void onError(int i, String s) {

                        sunriseDone = true;
                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                    }
                });
            }

            for (int i = 0; i < sunsetSchedules.size(); i++) {
                final PHSchedule schedule = sunsetSchedules.get(i);
                schedule.setDate(sunset);
                schedule.setAutoDelete(true);
                bridge.updateSchedule(schedule, new PHScheduleListener() {
                    @Override
                    public void onCreated(PHSchedule phSchedule) {

                    }

                    @Override
                    public void onSuccess() {

                        sunsetDone = true;
                    }

                    @Override
                    public void onError(int i, String s) {

                        sunsetDone = true;
                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                    }
                });
            }

            while (!sunriseDone || !sunsetDone)
                ;
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
                                hour = Integer.parseInt(time.substring(0,2));
                                min = Integer.parseInt(time.substring(3,5));
                                sec = Integer.parseInt(time.substring(6,8));

                                sunrise.setHours(hour);
                                sunrise.setMinutes(min);
                                sunrise.setSeconds(sec);
                            }catch(NumberFormatException e){
                                Log.d(DEBUG_TAG, "Parsing Error: sunrise");
                            }

                        }else if (tagName.equals("sunset")){
                            parser.next();
                            String time = parser.getText();
                            Log.d(DEBUG_TAG,
                                    "sunset: " + time);
                            sunset = new Date();

                            int hour, min, sec;
                            try {
                                hour = Integer.parseInt(time.substring(0,2));
                                min = Integer.parseInt(time.substring(3,5));
                                sec = Integer.parseInt(time.substring(6,8));

                                sunset.setHours(hour);
                                sunset.setMinutes(min);
                                sunset.setSeconds(sec);
                            }catch(NumberFormatException e){
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

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
            ;
        }

        @Override
        public void onBridgeConnected(PHBridge phBridge) {
            hueBridgeSdk.setSelectedBridge(phBridge);
            isDone = true;
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
            ;
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {
            ;
        }

        @Override
        public void onError(int i, String s) {
            ;
        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {
            ;
        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {
            ;
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {
            ;
        }
    }
}
