package app.lights.prism.com.prismlights;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.philips.lighting.hue.sdk.connection.impl.PHBridgeInternal;

import java.util.Scanner;

//this class can be found in the example app
public class HueSharedPreferences {
    private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";
    private static final String LAST_CONNECTED_USERNAME      = "LastConnectedUsername";
    private static final String LAST_CONNECTED_IP            = "LastConnectedIP";
    private static final String BEACON_ASSOCIATIONS = "BeaconAssociations";
    private static HueSharedPreferences instance = null;
    private SharedPreferences mSharedPreferences = null;

    private Editor mSharedPreferencesEditor = null;


    public void create() {

    }

    public static HueSharedPreferences getInstance(Context ctx) {
        if (instance == null) {
            instance = new HueSharedPreferences(ctx);
        }
        return instance;
    }

    private HueSharedPreferences(Context appContext) {
        mSharedPreferences = appContext.getSharedPreferences(HUE_SHARED_PREFERENCES_STORE, 0); // 0 - for private mode
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }


    public String getUsername() {
        String username = mSharedPreferences.getString(LAST_CONNECTED_USERNAME, "");
        if (username==null || username.equals("")) {
            username = PHBridgeInternal.generateUniqueKey();
            setUsername(username);  // Persist the username in the shared prefs
        }
        return username;
    }

    public boolean setUsername(String username) {
        mSharedPreferencesEditor.putString(LAST_CONNECTED_USERNAME, username);
        return (mSharedPreferencesEditor.commit());
    }

    public String getLastConnectedIPAddress() {
        return mSharedPreferences.getString(LAST_CONNECTED_IP, "");
    }

    public boolean setLastConnectedIPAddress(String ipAddress) {
        mSharedPreferencesEditor.putString(LAST_CONNECTED_IP, ipAddress);
        return (mSharedPreferencesEditor.commit());
    }

    /**
     * Takes string: <BeaconID>~!~<LightId>~!~<Range>
     * @param association
     * @return
     */
    public boolean addBeaconAssociation(String association){
        String[] parsedAssocation = association.split("~!~");
        String beaconId = parsedAssocation[0];
        String lightId = parsedAssocation[1];
        String range = parsedAssocation[2];
        String currentAssociations = getBeaconOrBulbAssociations(beaconId);
        String associations = mSharedPreferences.getString(BEACON_ASSOCIATIONS, "");
        if(currentAssociations.trim().equals("")) {
            mSharedPreferencesEditor.putString(BEACON_ASSOCIATIONS, associations + "\n" + association);
            return mSharedPreferencesEditor.commit();
        } else  /* There are already associations for this bulb */{
            Scanner s = new Scanner(currentAssociations);
            while(s.hasNextLine()){
                String currentLine = s.nextLine();
                String[] tempParsedAssocation = currentLine.split("~!~");
                String tempbeaconId= parsedAssocation[0];
                String tempLightId = parsedAssocation[1];
                String tempRange = parsedAssocation[2];
                if (lightId.equals(tempLightId)){
                    //There is currently an association between this beacon & bulb. Lets replace this.
                    associations.replace(currentLine, association);
                    return mSharedPreferencesEditor.commit();
                }
            }
        }
        return mSharedPreferencesEditor.commit();
    }

    public boolean removeBulbAssociation(String association){
        String associations = mSharedPreferences.getString(BEACON_ASSOCIATIONS, "");
        Scanner s = new Scanner(associations);
        while(s.hasNextLine()){
            String currentLine = s.nextLine();
            if (currentLine.contains(association)){
                associations.replace(currentLine, "");
            }
        }
        mSharedPreferencesEditor.putString(BEACON_ASSOCIATIONS, associations);
        return mSharedPreferencesEditor.commit();
    }

    public String getBeaconOrBulbAssociations(String beaconOrBulbIdentifier){
        String associations = mSharedPreferences.getString(BEACON_ASSOCIATIONS, "");
        String beaconAssociations = "";
        Scanner s = new Scanner(associations);
        while(s.hasNextLine()){
            String currentLine = s.nextLine();
            if (currentLine.contains(beaconOrBulbIdentifier)){
                beaconAssociations += currentLine + "\n";
            }
        }
        return beaconAssociations;
    }


}
