package com.example.weizhi.oddleassignment.model;

/**
 * POJO for weather information.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class Weather {
    public final String cityName;
    public final String stateName;
    public String condition;
    public int tempCelsius;
    public int tempFahrenheit;
    public int hour;
    public String icon;
    public boolean shouldUpdate;

    public Weather(String cityName, String stateName){
        this.cityName = cityName;
        this.stateName = stateName;
        shouldUpdate = true;
    }

    public Weather(String cityName, String stateName, String condition, int tempCelsius,
                   int tempFahrenheit, int hour, String icon){
        this.cityName = cityName;
        this.stateName = stateName;
        this.condition = condition;
        this.tempCelsius = tempCelsius;
        this.tempFahrenheit = tempFahrenheit;
        this.hour = hour;
        this.icon = icon;
        shouldUpdate = false;
    }

    public String toKey(){
        return cityName + ", " + stateName;
    }

    @Override
    public String toString() {
        String s = "name:"+cityName+", "+stateName;
        if(condition!=null) {
            s = s + " condition:" + condition;
            s = s + " temp:" + tempCelsius + "/" + tempFahrenheit;
            s = s + " hour:" + hour;
            s = s + " icon:" + icon;
        }
        return s;
    }

    @Override
    public int hashCode() {
        int ret = 41;
        ret = hc(ret, cityName);
        ret = hc(ret, stateName);
        ret = hc(ret, condition);
        ret = hc(ret, tempCelsius);
        ret = hc(ret, tempFahrenheit);
        ret = hc(ret, hour);
        ret = hc(ret, icon);
        return ret;
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == null) return false;
        if (ob.getClass() != Weather.class) return false;
        Weather r = (Weather)ob;
        if (!eq(r.cityName, cityName)) return false;
        if (!eq(r.stateName, stateName)) return false;
        if (!eq(r.condition, condition)) return false;
        if (!eq(r.tempCelsius, tempCelsius)) return false;
        if (!eq(r.tempFahrenheit, tempFahrenheit)) return false;
        if (!eq(r.hour, hour)) return false;
        if (!eq(r.icon, icon)) return false;
        return true;
    }

    private static boolean eq(Object ob1, Object ob2) {
        return ob1 == null ? ob2 == null : ob1.equals(ob2);
    }

    private static int hc(int hc, Object field) {
        return field == null ? hc : 43 + hc * field.hashCode();
    }
}
