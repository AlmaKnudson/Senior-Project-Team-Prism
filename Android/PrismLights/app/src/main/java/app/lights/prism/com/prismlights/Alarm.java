package app.lights.prism.com.prismlights;

import android.text.format.Time;

import com.philips.lighting.model.PHSchedule;

import java.util.Calendar;

/**
 * Created by Brian_Oh on 12/11/14.
 */
public class Alarm {

    public PHSchedule schedule;
    public boolean isOn;

    public Alarm(PHSchedule schedule) {
        this.schedule = schedule;
        this.isOn = true;
    }

    @Override
    public String toString() {
        return schedule.getDate().toString();
    }
}