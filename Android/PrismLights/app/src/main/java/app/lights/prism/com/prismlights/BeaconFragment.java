package app.lights.prism.com.prismlights;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by AlmaKnudson on 4/16/15.
 */
public class BeaconFragment extends Fragment {
    public static String lightPositionString = "CURRENT_BULB_POSITION";

    private int currentBulbId; // The chosen Light BULB ID
    private PHLight currentBulb;
    private static PHBridge bridge;
    private PHHueSDK hueBridgeSdk;

    private SeekBar beaconRangeSeekBar;
    private TextView beaconLabel;
    private TextView beaconRangeLabel;
    private Button beaconStartTrackingButton;
    private Button beaconStopTrackingButton;

    //Range is in terms of feet
    private final int minRange;
    private volatile int currentRange;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BeaconFragment() {
        minRange = 3;
        currentRange = 15;
    }


    //TODO: add on/off switch for each schedule. schedule.setStatus("Enabled" or "Disabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            currentBulbId = getArguments().getInt(lightPositionString);
        }
        hueBridgeSdk = PHHueSDK.getInstance();
        bridge = hueBridgeSdk.getSelectedBridge();
        currentBulb = bridge.getResourceCache().getAllLights().get(currentBulbId);

        //get current bulb
        currentBulb = bridge.getResourceCache().getAllLights().get(currentBulbId);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beacons, container, false);
        beaconRangeSeekBar = (SeekBar) view.findViewById(R.id.beaconRangeSeekBar);
        beaconStartTrackingButton = (Button) view.findViewById(R.id.beaconStartTrackingButton);
        beaconStopTrackingButton = (Button) view.findViewById(R.id.beaconStopTrackingButton);
        beaconLabel = (TextView) view.findViewById(R.id.beaconLabel);
        beaconRangeLabel = (TextView) view.findViewById(R.id.beaconRangeLabel);



        beaconStopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Stop Tracking button has been pressed.");
            }
        });

        beaconStartTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("START Tracking button has been pressed.");
            }
        });

        beaconRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentRange = progress + minRange;
                beaconRangeLabel.setText(currentRange + " Feet");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return view;
    }





}
