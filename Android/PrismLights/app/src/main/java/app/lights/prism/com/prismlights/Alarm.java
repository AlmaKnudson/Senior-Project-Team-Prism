package app.lights.prism.com.prismlights;

import android.text.format.Time;

import java.util.Calendar;

/**
 * Created by Brian_Oh on 12/11/14.
 */
public class Alarm {

    public Calendar cal;
    public boolean isOn;

    public Alarm(Calendar cal) {
        this.cal = cal;
        this.isOn = true;
    }

    @Override
    public String toString() {
        return cal.toString();
    }
}