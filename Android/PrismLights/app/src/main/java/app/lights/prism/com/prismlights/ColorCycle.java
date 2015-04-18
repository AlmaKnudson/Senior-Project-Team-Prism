package app.lights.prism.com.prismlights;

import android.content.Context;
import android.util.Log;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Brian_Oh on 4/13/15.
 */
public class ColorCycle implements Serializable{

    private static final String DEBUG_TAG = "ColorCycle";

    private String name;
    ArrayList<SingleColor> colors;

    ColorCycle(String name){
        this.name = name;
        colors = new ArrayList<>();
    }

    ColorCycle(ColorCycle another){
        name = another.getName();
        colors = new ArrayList<>();
        for (int i = 0; i<another.getSize(); i++){
            colors.add(new SingleColor(another.colors.get(i).color, another.getBrightness(i), another.getDuration(i), another.getTransition(i)));
        }
    }

    // recurring timer's description should have the following format: "prism,[index in the cycle],[duration of the color]"
    // Before construct a color cycle using this, you must check following:
    //      *if there is only one name in the list*
    //      *all description starts with "prism,"*
    ColorCycle(ArrayList<PHSchedule> schedules){
        colors = new ArrayList<>();

        name = schedules.get(0).getName();

        for (int i = 0; i<schedules.size(); i++){
            PHSchedule schedule = schedules.get(i);

            String description = schedule.getDescription();

            String[] tokens = description.split("[,]");

            int order = Integer.parseInt(tokens[1]);
            int duration = Integer.parseInt(tokens[2]);

            PHLightState state = schedule.getLightState();
            float[] color = {state.getX(), state.getY()};
            int brightness = state.getBrightness();
            int transition = state.getTransitionTime();
            colors.add(order, new SingleColor(color, brightness, duration, transition));
        }
    }

    private class SingleColor implements Serializable{
        float[] color;
        int brightness;
        int duration;
        int transition;

        SingleColor(float[] color, int brightness, int duration, int transition) {
            this.color = color;
            this.brightness = brightness; // out of 100
            this.duration = duration; //in seconds
            this.transition = transition; // not sure what this is
        }
    }

    void add(float[] color, int brightness, int duration, int transition){
        colors.add(new SingleColor(color, brightness, duration, transition));
    }

    void setName(String name){
        this.name = name;
    }

    void setColor (int i, float[] color){
        colors.get(i).color = color;
    }

    void setBrightness (int i , int brightness){
        colors.get(i).brightness = brightness;
    }

    void setDuration (int i, int duration){
        colors.get(i).duration = duration;
    }

    void setTransition (int i, int transition){
        colors.get(i).transition = transition;
    }

    String getName(){
        return name;
    }

    int getSize(){
        return colors.size();
    }

    int getColor(int i){
        float[] color = colors.get(i).color;
        return PHUtilities.colorFromXY(new float[]{color[0], color[1]}, "");
    }

    float[] getColorFloat(int i){
        return colors.get(i).color;
    }

    int getBrightness(int i){
        return colors.get(i).brightness;
    }

    int getDuration(int i){
        return colors.get(i).duration;
    }

    int getTransition(int i){
        return colors.get(i).transition;
    }

    public boolean equals(Object obj){
        if (obj instanceof ColorCycle){
            ColorCycle colorCycle = (ColorCycle)obj;

            if(colorCycle.colors.size() != this.colors.size())
                return false;

            for (int i = 0; i < colorCycle.colors.size(); i++){
                SingleColor otherSingleColor = colorCycle.colors.get(i);
                SingleColor thisSingleColor = this.colors.get(i);
                if(!otherSingleColor.color.equals(thisSingleColor.color))
                    return false;
                if(otherSingleColor.brightness != thisSingleColor.brightness)
                    return false;
                if(otherSingleColor.duration != thisSingleColor.duration)
                    return false;
                if(otherSingleColor.transition != thisSingleColor.transition)
                    return false;
            }
            return true;

        }else{
            return false;
        }
    }

    // return null if something goes wrong. otherwise return List<ScheduledFuture>. List list will contain each task.
    // task.get() will return null once the task is completed.
    List<ScheduledFuture> startColorCycle(int totalDurationInMin, final PHBridge bridge, final String identifier, final Boolean isGroup){

        if(totalDurationInMin < 0){
            Log.e(DEBUG_TAG,"Total duration was less than 0");
            return null;
        }
        if(bridge == null ){
            Log.e(DEBUG_TAG,"bridge is null");
            return null;
        }
        if(identifier == null ){
            Log.e(DEBUG_TAG,"identifier is null");
            return null;
        }


        //remove current colorCycle
        List<PHSchedule> timers = bridge.getResourceCache().getAllTimers(true);
        ArrayList<PHSchedule> timersForThisBulb = new ArrayList<>();
        for (int i = 0; i < timers.size(); i++){
            PHSchedule timer = timers.get(i);
            if(isGroup) {
                if (timer.getLightIdentifier().equals(identifier)
                        && timer.getDescription().startsWith("prism"))
                    timersForThisBulb.add(timer);
            }
            else{
                if (timer.getGroupIdentifier().equals(identifier)
                        && timer.getDescription().startsWith("prism"))
                    timersForThisBulb.add(timer);
            }
        }
        for (int i = 0; i<timersForThisBulb.size();i++){
            bridge.removeSchedule(timersForThisBulb.get(i).getIdentifier(), new PHScheduleListener() {
                @Override
                public void onCreated(PHSchedule phSchedule) {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(int i, String s) {
                    Log.e(DEBUG_TAG, "Removing existing timer failed: " + s);
                }

                @Override
                public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                }
            });
        }



        int durationSum = 0;
        for (int i = 0; i< colors.size(); i++){
            durationSum = durationSum + colors.get(i).duration;
        }

        final int numRecurrences = totalDurationInMin/durationSum;

        List<ScheduledFuture> cycleServiceList = new ArrayList<>();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        int delay = 0;

        for(int i = 0; i< colors.size();i++) {
            final SingleColor singleColor = colors.get(i);
            final int j = i;
            cycleServiceList.add(scheduler.schedule(new Runnable() {

                @Override
                public void run() {
                    //need to schedule recurring timer with identification, color, brightness, transition, durationSum, numRecurrences
                    PHSchedule schedule = new PHSchedule(name);
                    schedule.setTimer(singleColor.duration);
                    schedule.setRecurringTimerInterval(numRecurrences);
                    if(isGroup){
                        schedule.setGroupIdentifier(identifier);
                    }
                    else{
                        schedule.setLightIdentifier(identifier);
                    }
                    PHLightState lightState = new PHLightState();
                    lightState.setOn(true);
                    lightState.setX(singleColor.color[0]);
                    lightState.setY(singleColor.color[1]);
                    lightState.setBrightness(singleColor.brightness);
                    lightState.setTransitionTime(singleColor.transition);

                    schedule.setLightState(lightState);
                    schedule.setDescription("prism,"+j+","+singleColor.duration); //Description must have "prism,[index in the cycle],[duration of the color]" format

                    bridge.createSchedule(schedule, new PHScheduleListener() {
                        @Override
                        public void onCreated(PHSchedule phSchedule) {

                        }

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(int i, String s) {
                            Log.e(DEBUG_TAG, "Creating timer failed: "+ s);
                        }

                        @Override
                        public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                        }
                    });
                }
            }, delay, TimeUnit.SECONDS));
            delay = delay + colors.get(i).duration;
        }

        return cycleServiceList;
    }
}
