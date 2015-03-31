package app.lights.prism.com.prismlights.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import app.lights.prism.com.prismlights.service.SmartSunService;

/**
 * Created by Brian_Oh on 3/24/15.
 * This Alarm Receiver will start SmartSunService when Alarm Broadcasting happen.
 * Alarm Broadcasting function, "setAlarmBroadcasting(Context context)" is located in Mainactivity
 */
public class BroadCastAlarmReceiver extends BroadcastReceiver{

    private static final String DEBUG_TAG = "BroadCastAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DEBUG_TAG, "Recurring broadcast alarm; requesting download service.");
        Intent downloader = new Intent(context, SmartSunService.class);

        /*
        TODO: need to change to real url
        http://www.earthtools.org/sun/<latitude>/<longitude>/<day>/<month>/<timezone>/<dst>
        where:
        latitude - decimal latitude of the point to query (from -90 to 90).
        longitude - decimal longitude of the point to query (from -180 to 180).
        day - day to query (from 1 through 31).
        month - month to query (from 1 through 12).
        timezone - hours offset from UTC (from -12 to 14). Alternatively, use '99' as the timezone in order to automatically work out the timezone based on the given latitude/longitude.
        dst - whether daylight saving time should be taken into account (either 0 for no or 1 for yes).
        Note that the query should be entered without the <s or >s (as in the example below).
         */
        downloader.setData(Uri.parse("http://www.earthtools.org/sun/40.71417/-74.00639/4/12/-5/1"));
        context.startService(downloader);
    }
}
