package app.lights.prism.com.prismlights;

import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.Date;

/**
 * Created by Brian_Oh on 4/22/15.
 * This is class to contain paired PHSchedules.
 * This is made for ScheduleFragment.
 */
public class Schedule {
    private PHSchedule scheduleOn;
    private PHSchedule scheduleOff;

    public Schedule(PHSchedule scheduleOn,PHSchedule scheduleOff){
        this.scheduleOn = scheduleOn;
        this.scheduleOff = scheduleOff;
    }

    public Date getOnTime(){
        if(scheduleOn!=null)
            return scheduleOn.getDate();
        else
            return null;
    }

    public Date getOffTime(){
        if(scheduleOff!=null)
            return scheduleOff.getDate();
        else
            return null;
    }

    public int getTimeChoiceOn(){
        if(scheduleOn!=null) {
            try {
                // all of our schedule must have "prism,[key],[choice]" format. if it is not, it is from other app.
                // key is usually time of the creation in milliseconds.
                // choice 0:fix time, 1:sunrise, 2:sunset, 3:nothing(user wants only on or off, not both of them and one of them has to be none)
                return Integer.parseInt(getScheduleOn().getDescription().split("[,]")[2]);
            }catch (Exception e){
                return 0;
            }
        }
        else
            return 3; // 3 means user did not want any turn on scheduled.
    }

    public int getTimeChoiceOff(){
        if(scheduleOff!=null) {
            try {
                return Integer.parseInt(getScheduleOff().getDescription().split("[,]")[2]);
            }
            catch (Exception e){
                return 3;
            }
        }
        else
            return 3; // 3 means user did not want any turn off scheduled.
    }

    public PHSchedule getScheduleOn(){
        return scheduleOn;
    }

    public PHSchedule getScheduleOff(){
        return scheduleOff;
    }

    public int getRecurringDays(){
        if (scheduleOn!= null)
            return scheduleOn.getRecurringDays();
        else
            return scheduleOff.getRecurringDays();
    }

    public PHLightState getLightState(){
        if (scheduleOn!= null)
            return scheduleOn.getLightState();
        else
            return scheduleOff.getLightState();
    }

    public String getName(){
        if (scheduleOn!= null)
            return scheduleOn.getName();
        else
            return scheduleOff.getName();
    }

    public PHSchedule.PHScheduleStatus getStatus(){
        if(scheduleOn!= null)
            return scheduleOn.getStatus();
        else
            return scheduleOff.getStatus();
    }

    public String getIdentifier(boolean isGroup){
        if(scheduleOn!= null)
            if(isGroup)
                return scheduleOn.getGroupIdentifier();
            else
                return scheduleOn.getLightIdentifier();
        else
            if(isGroup)
                return scheduleOff.getGroupIdentifier();
            else
                return scheduleOff.getLightIdentifier();
    }

}
