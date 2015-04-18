package app.lights.prism.com.prismlights;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.estimote.sdk.Utils;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;


/**
 * Created by AlmaKnudson on 4/16/15.
 */
public class BeaconFragment extends Fragment {
    public static String lightPositionString = "CURRENT_BULB_POSITION";

    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("Prism Lights", ESTIMOTE_PROXIMITY_UUID, null, null);
    private BeaconManager beaconManager; //= new BeaconManager(this.getActivity().getApplicationContext());

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
    private double[] distanceInFeet = new double[8];
    private int index = 0;
    private double currentAverageDistance = 0;
    private boolean addAssociation = false;

    //Beacon gobblygoop
//    private BeaconManager beaconManager;
    private Beacon beacon;
//    private Region region;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BeaconFragment() {
        minRange = 3;
        currentRange = 15;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        beaconManager = new BeaconManager(this.getActivity());
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(rangedBeacons.size() == 0)
                            return;
                        beacon = rangedBeacons.get(0);
                        if(addAssociation){
                            String association = getBeaconId(beacon) + "~!~" + currentBulbId + "~!~" + currentRange;
                            HueSharedPreferences.getInstance(getActivity().getApplicationContext()).addBeaconAssociation(association);
                            addAssociation = false;
                            beaconManager.disconnect();
                        }
                        double distance = Utils.computeAccuracy(beacon);
                        if(index == distanceInFeet.length -1){
                            index = 0;
                        }
                        distanceInFeet[index++] = distance*3;
                        int totalSamples = 0;
                        double sum = 0;
                        for(int i = 0; i < distanceInFeet.length; i ++){
                            if(distanceInFeet[i] != 0){
                                totalSamples ++;
                                sum += distanceInFeet[i];
                            }
                        }
                        currentAverageDistance = sum/totalSamples;
                        //beaconLabel.setText("Distance: " + currentAverageDistance);

                    }
                });
            }
        });

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
                String association = "~!~" + currentBulbId + "~!~";
                HueSharedPreferences.getInstance(getActivity().getApplicationContext()).removeBulbAssociation(association);
                beaconManager.disconnect();
                System.out.println("Stop Tracking button has been pressed. Removed association: " + association);
            }
        });

        beaconStartTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("START Tracking button has been pressed.");
                // Should be invoked in #onStart.
                beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                    @Override public void onServiceReady() {
                        try {
                            addAssociation = true;
                            beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                        } catch (RemoteException e) {
                            System.out.println("Cannot start ranging" + e);
                        }
                    }
                });
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



    private String getBeaconId(Beacon beacon){
        return beacon.getMajor() + "." + beacon.getMinor();
    }



}
