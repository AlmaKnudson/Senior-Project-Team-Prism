package app.lights.prism.com.prismlights;

import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brian_Oh on 4/22/15.
 * this class take list of PHSchedules and then make List of Schedule
 */
public class ScheduleList {
    ArrayList<Schedule> list;

    public ScheduleList(List<PHSchedule> allSchedule){
        list = new ArrayList<>();
        ArrayList<PHSchedule> onList = new ArrayList<>();
        ArrayList<PHSchedule> offList = new ArrayList<>();

        for (int i=0; i< allSchedule.size();i++){
            if (allSchedule.get(i).getLightState().isOn())
                onList.add(allSchedule.get(i));
            else
                offList.add(allSchedule.get(i));
        }

        for (int i=0; i< onList.size();i++){
            // get ket from description. Description format is "prism,key,timeMode".
            // timeMode 0:specific time, 1:sunrise, 2:sunset, (3:none schedule doesn't exit)
            String keyOn = onList.get(i).getDescription().split("[,]")[1];

            boolean foundPair = false;
            for (int j = 0; j<offList.size();j++){
                String keyOff = offList.get(j).getDescription().split("[,]")[1];
                if(keyOn.equals(keyOff)){
                    list.add(new Schedule(onList.get(i), offList.get(j)));
                    offList.remove(j);
                    foundPair = true;
                    break;
                }
            }
            if (!foundPair)
                list.add(new Schedule(onList.get(i), null));
        }

        for (int i = 0; i<offList.size();i++){
            list.add(new Schedule(null, offList.get(i)));
        }
    }

    public Schedule getSchedule(int i){
        return list.get(i);
    }
    public int getSize(){
        return list.size();
    }
}
