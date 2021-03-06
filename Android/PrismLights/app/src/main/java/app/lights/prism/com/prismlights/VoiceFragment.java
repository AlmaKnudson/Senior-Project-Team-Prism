package app.lights.prism.com.prismlights;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.Date;
import com.google.gson.JsonElement;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.util.PHHueCountTimerListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ai.wit.sdk.IWitListener;
import ai.wit.sdk.Wit;
import ai.wit.sdk.model.WitOutcome;
import ai.wit.sdk.WitMic;


/**
* This is the fragment that handles all of the voice command functionality.
*  
* Example voice commands: "Lights on", "Lights off", "Set 5 minute alarm", "Wake me up at 6:00AM", "It's too bright",...
*  
* @author Alma Knudson--Senior Project (Team Prism) 
*/
public class VoiceFragment extends Fragment implements IWitListener {

    /**
     * Private voice fragment global variables.
     **/
    private Wit wit;
    private TextView micStatus;
    private TextView witResponse;
    private WitMic mic;
    private boolean ignoreCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String accessToken = "SAIGZN56HURQBH5PSTPPQQ545ZNVBSSF";
        wit = new Wit(accessToken, this);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ignoreCallback = false;
        // Inflate the layout for this fragment
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_voice_control, container, false);
        ImageButton mic = (ImageButton) layout.findViewById(R.id.micButton);
        micStatus = (TextView) layout.findViewById(R.id.micText);
        witResponse = (TextView) layout.findViewById(R.id.micResultText);
        // wit.captureTextIntent("Turn lights on");
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    wit.toggleListening();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return layout;
    }

    @Override
    public void witDidGraspIntent(ArrayList<WitOutcome> witOutcomes, String s, Error error) {
        //Check to see if we need to ignore this callback.
        if(ignoreCallback)
            return;
        //TODO: handle recieving this after the view has disappeared
        // System.out.println(witOutcomes);
        
        //Parse what Wit passes us back.
        if(witOutcomes != null && !witOutcomes.isEmpty() && error == null) {
            WitOutcome outcome = witOutcomes.get(0);
            //Notify User what their intent was interpretted to be.
            witResponse.setText("\nYOU SAID:\n" + outcome.get_text() + "\n\nYOUR INTENT:\n" + outcome.get_intent() + "\n");
            String intent = outcome.get_intent().trim();
            HashMap<String, JsonElement> entities = outcome.get_entities();

            //Hue Bridge:
            PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
            DecimalFormat formatter = new DecimalFormat("#0.00");
            /*
             * These are the 9 possible intents for voice control.
             * Handling what to do if wit interpretted voice to be any of these 9 intents. 
             */
            if(intent.equals("alarm")){

                if (entities.containsKey("datetime") ){
                    JsonElement datetime =  entities.get("datetime");
                    String dateString = datetime.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsJsonObject().get("from").getAsString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    try {
                        Date myDate = sdf.parse(dateString);
                        witResponse.setText("Setting alarm for:\n" + myDate.toString());

                    } catch (Exception e){
                        witResponse.setText("Error parsing the date: " + dateString);
                    }
                }
//                witResponse.setText();
            } else if (intent.equals("brighter")){
                //Adjust brightness of hue lights.
                StringBuilder message = new StringBuilder("INCREASING BRIGHTNESS\n");
                if(bridge != null) {
                    List<PHLight> currentLights = bridge.getResourceCache().getAllLights();
                    for (PHLight light : currentLights) {
                        int currentBrightness = light.getLastKnownLightState().getBrightness();
                        int newBrightness = Math.min(currentBrightness + 75, 254);
                        PHLightState lightState = new PHLightState();
                        lightState.setOn(true);
                        lightState.setBrightness(newBrightness);
//                    lightState.setBrightness(currentBrightness + (currentBrightness/5)); //Decrease brightness 20%
                        bridge.updateLightState(light, lightState);
                        message.append(light.getName() + ", is set to " + formatter.format((newBrightness * 1.0) / 254.0) + " brightness.\n");
                    }
                }
                witResponse.setText(message.toString());
            } else if (intent.equals("bulbname_off") || intent.equals("bulbname_on")){ //TODO--SET COLOR OF INDIVIDUAL BULBS
                //Example, "Kitchen off" or "Bathroom off"
                try {
                    if (entities.containsKey("bulbname")) {
                        String bulbname = entities.get("bulbname").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
                        boolean onOff = false;
                        if(entities.containsKey("on_off")){
                            if(entities.get("on_off").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString().equals("on")){
                                onOff = true;
                            }
                        }
                        int hV = -1;
                        if(entities.containsKey("color")){
                            String[] result = entities.get("color").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString().split("~!~");
//                        String color = entities.get("color").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
                            String color = result[0];
                            try {
                                hV = Integer.parseInt(result[1]);
                                onOff = true;
                            } catch(Exception e){

                            }
                        }
                        HueBulbChangeUtility.setLightOrGroupFromName(bulbname, onOff, hV, (MainActivity)getActivity());
                        witResponse.setText(witResponse.getText() + "Turning bulb/group '" + bulbname + "'" + onOff + "'.");
                    }

                } catch(Exception e) {
                    witResponse.setText("Did not understand this command: " + outcome.get_text());
                }
            }

//            else if (intent.equals("bulbname_on")) {
//                //Example, "Kitchen on" or "Bathroom on"
//                try {
//                    if (entities.containsKey("bulbname")) {
//                        String bulbname = entities.get("bulbname").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
//                        witResponse.setText("Turning bulb/group '" + bulbname + "' off.");
//
//                        int hV = -1;
//                        if(entities.containsKey("color")){
//                            String[] result = entities.get("color").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString().split("~!~");
////                        String color = entities.get("color").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
//                            String color = result[0];
//                            try {
//                                hV = Integer.parseInt(result[1]);
//                            } catch(Exception e){
//
//                            }
//                        }
//                        boolean onOff = false;
//                        if(entities.containsKey("on_off")){
//                            if(entities.get("on_off").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString().equals("on")){
//                                onOff = true;
//                            }
//                        }
//
//                        HueBulbChangeUtility.setLightOrGroupFromName(bulbname, onOff, hV);
//                    }
//                } catch(Exception e) {
//                    witResponse.setText("Did not understand this command: " + outcome.get_text());
//                }
//            }

            else if (intent.equals("color")) {
                if(entities.containsKey("color")){
                    try {
//                        JSONObject arr = (JSONObject)entities.get("color").getAsJsonArray().get(0).getAsJsonObject();
                        String[] result = entities.get("color").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString().split("~!~");
//                        String color = entities.get("color").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
                        String color = result[0];
                        String hueValue = result[1];
                        witResponse.setText("Set hue value to be: " + hueValue + "\nFor this color: " + color);
                    } catch(Exception e){
                        witResponse.setText("Did not understand this command: " + outcome.get_text());
                    }
                }
            } else if (intent.equals("dim")) {
                StringBuilder message = new StringBuilder("DIMMING LIGHTS\n");
                if(bridge != null) {
                    //DIM BULBS
                    List<PHLight> currentLights = bridge.getResourceCache().getAllLights();
                    for (PHLight light : currentLights) {
                        int currentBrightness = light.getLastKnownLightState().getBrightness();
                        PHLightState lightState = new PHLightState();
                        lightState.setOn(true);
                        int newBrightness = Math.max(currentBrightness - 75, 0);
                        if (newBrightness == 0)
                            lightState.setOn(false);
                        lightState.setBrightness(newBrightness); //Decrease brightness 20%
                        bridge.updateLightState(light, lightState);
                        message.append(light.getName() + ", is set to " + formatter.format((newBrightness * 1.0) / 254.0) + " brightness.\n");
                    }
                }
                witResponse.setText(message.toString());
//                witResponse.setText(intent + "\n" + entities.toString() + "dimm");
            } else if (intent.equals("timer")) {
                try {
                    String durationInSeconds = entities.get("duration").getAsJsonArray().get(0).getAsJsonObject().get("value").toString();
                    witResponse.setText("Setting timer for " + durationInSeconds + " seconds.");
//                    PHHueCountTimerListener timerLister = new PHHueCountTimerListener() {
//                        @Override
//                        public void onTick() {
//
//                        }
//
//                        @Override
//                        public void onFinish() {
//
//                        }
//                    };
//                    PHHueCountTimer timer = new PHHueCountTimer(Long.parseLong(durationInSeconds), timerLister);
//                    PHSchedule schedule = new PHSchedule();

//                        bridge.createSchedule();
                } catch(Exception e){
                    if (entities.containsKey("datetime") ){
                        JsonElement datetime =  entities.get("datetime");
                        String dateString = datetime.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsJsonObject().get("from").getAsString();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        try {
                            Date myDate = sdf.parse(dateString);
                            witResponse.setText("Setting alarm for:\n" + myDate.toString());
                        } catch (Exception e1){
                            witResponse.setText("Error parsing the date: " + dateString);
                        }
                    }
//                    witResponse.setText("Did not understand this command: " + outcome.get_text() + "\n" + entities.toString());
                }
            } else if (intent.equals("turn_lights_off")) {
                witResponse.setText("TURNING ALL LIGHTS OFF");
                if(bridge != null) {
                    List<PHLight> currentLights = bridge.getResourceCache().getAllLights();
                    for (PHLight light : currentLights) {
                        PHLightState lightState = new PHLightState();
//                    HueBulbChangeUtility.turnBulbOnOff(light);
//                    lightState.setOn(on);
                        lightState.setOn(false);
                        bridge.updateLightState(light, lightState);
                    }
                }
            }  else if (intent.equals("turn_lights_on")) {
                if(bridge != null) {
                    List<PHLight> currentLights = bridge.getResourceCache().getAllLights();
                    for (PHLight light : currentLights) {
                        PHLightState lightState = new PHLightState();
                        lightState.setOn(true);
                        bridge.updateLightState(light, lightState);
                    }
                }
                witResponse.setText("TURNING ALL LIGHTS ON");
            } else {
                System.err.println("Something was not understood. Your intent is UNCLEAR >:");
            }

        }
        micStatus.setText(this.getActivity().getText(R.string.waiting));
    }

    @Override
    public void witDidStartListening() {
        micStatus.setText(this.getActivity().getText(R.string.listening));
    }

    @Override
    public void witDidStopListening() {
        micStatus.setText(this.getActivity().getText(R.string.processing));
    }

    @Override
    public void witActivityDetectorStarted() {
        micStatus.setText(this.getActivity().getText(R.string.listening));
    }

    @Override
    public String witGenerateMessageId() {
        return null;
    }


    @Override
    public void onDestroyView() {
       try {
           wit.stopListening();
       } catch(Exception e) {

       }
        ignoreCallback = true;
        super.onDestroyView();
    }
}
