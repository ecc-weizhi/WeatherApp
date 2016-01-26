package com.example.weizhi.oddleassignment.model;

import java.util.List;

/**
 * POJO class to match the structure of auto complete JSON
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class AutoCompleteJsonPojo {
    public List<City> RESULTS;

    public static class City{
        public String name;
        public String type;
        public String c;
        public String zmw;
        public String tz;
        public String tzs;
        public String l;
        public String ll;
        public String lat;
        public String lon;
    }
}
