package app.lights.prism.com.prismlights;

import java.util.HashMap;

/**
 * Created by AlmaKnudson on 4/14/15.
 * This is a singleton of the lights range map.
 */
public class LightRangeMap {

    //rangeMap has <K,V> where K--> bulb name and V--> range (e.g. "LOW", "MID", "HIGH")
    static HashMap<String, String> rangeMap;
    static LightRangeMap lightRangeMap;

    /**
     * Construct the rangeMap with a given map.  Meant to be used in the BulbRangeFragment ONLY.
     * @param map
     */
    public LightRangeMap(HashMap<String, String> map){
        rangeMap = map;
        lightRangeMap = this;
    }


    /**
     * Meant to be used to access rangeMap, created in the BulbRangeFragment, in the MusicFragment to determine which bulb needs to be updated.
     * @return rangeMap
     */
    public static LightRangeMap getLightRangeMap(){
        if(rangeMap == null){
            HashMap<String, String> m = new HashMap<String, String>();
            lightRangeMap = new LightRangeMap(m);
            return lightRangeMap;
        } else {
            return lightRangeMap;
        }
    }

    public void setMap(HashMap<String, String> map){
        rangeMap = map;
    }

    public HashMap<String, String> getMap(){
        return rangeMap;
    }

}
