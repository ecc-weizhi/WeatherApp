package com.example.weizhi.oddleassignment.model;

import java.util.List;

/**
 * POJO to match to structure of Hourly API call JSON.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HourlyJsonPojo {
    public Response response;
    public List<HourlyForecast> hourly_forecast;

    public static class Response{
        public double version;
        public String termsofService;
        public Features features;

        public static class Features{
            public int hourly;
        }
    }

    public static class HourlyForecast{
        public FctTime FCTTIME;
        public EnglishMetric temp;
        public EnglishMetric dewpoint;
        public String condition;
        public String icon;
        public String icon_url;
        public int fctcode;
        public int sky;
        public EnglishMetric wspd;
        public WindDirection wdir;
        public String wx;
        public int uvi;
        public int humidity;
        public EnglishMetric windchill;
        public EnglishMetric heatindex;
        public EnglishMetric feelslike;
        public EnglishMetric qpf;
        public EnglishMetric snow;
        public int pop;
        public EnglishMetric mslp;

        public static class FctTime{
            public int hour;
            public String hour_padded;
            public String min;
            public int min_unpadded;
            public int sec;
            public int year;
            public int mon;
            public String mon_padded;
            public String mon_abbrev;
            public int mday;
            public String mday_padded;
            public String yday;
            public int isdst;
            public long epoch;
            public String pretty;
            public String civil;
            public String month_name;
            public String month_name_abbrev;
            public String weekday_name;
            public String weekday_name_night;
            public String weekday_name_abbrev;
            public String weekday_name_unlang;
            public String weekday_name_night_unlang;
            public String ampm;
            public String tz;
            public String age;
            public String UTCDATE;
        }

        public static class EnglishMetric{
            public double english;
            public int metric;
        }

        public static class WindDirection{
            public String dir;
            public int degrees;
        }
    }
}

