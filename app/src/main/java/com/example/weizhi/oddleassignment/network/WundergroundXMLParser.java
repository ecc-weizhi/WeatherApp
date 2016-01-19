package com.example.weizhi.oddleassignment.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Xml;

import com.example.weizhi.oddleassignment.model.Weather;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * This class parses through XML returned from Wunderground server by using XmlPullParser.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class WundergroundXMLParser {
    private static final String TAG = "'WundergroundXMLParser";
    // We don't use namespaces
    private static final String ns = null;

    public static @NonNull ArrayList<String> parseCity(InputStream in){
        ArrayList<String> cities = new ArrayList();

        try {
            // Some setup
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            int eventType = parser.getEventType();
            // We will parse the entire xml.
            while (eventType != XmlPullParser.END_DOCUMENT){
                // We are interested in start tag.
                if(eventType == XmlPullParser.START_TAG){
                    // Specifically, we are looking for "name" start tag.
                    if(parser.getName().equals("name")){
                        eventType = parser.next();
                        String cityName = parser.getText();

                        // We now search for "type" starting tag.
                        // Based on the API documentation, the next tag must be "type"
                        while(eventType!=XmlPullParser.START_TAG){
                            if(eventType == XmlPullParser.END_DOCUMENT)
                                break;
                            else {
                                eventType = parser.next();
                            }
                        }

                        String type;
                        // If we reached here, we have either found "type" tag or xml ended.
                        if(eventType == XmlPullParser.START_TAG && parser.getName().equals("type")){
                            parser.next();
                            type = parser.getText();

                            // We are only interested in "city".
                            if(type.equals("city")){
                                cities.add(cityName);
                            }
                        }
                    }
                }

                if(eventType != XmlPullParser.END_DOCUMENT)
                    eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error encountered while parsing through autocomplete request");
        }

        return cities;
    }

    public static @Nullable Weather parseWeather(InputStream in, String city, String state){
        String condition = null;
        int tempCelsius = 0;
        int tempFahrenheit = 0;
        int hour = 0;
        String icon = null;

        try {
            // Some setup
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            boolean isDone = false;
            int eventType = parser.getEventType();
            // We will parse until done.
            while (!isDone && eventType != XmlPullParser.END_DOCUMENT){
                // Found a start tag.
                if(eventType == XmlPullParser.START_TAG){
                    if(parser.getName().equals("FCTTIME")){
                        boolean hasFoundHour = false;
                        while(!hasFoundHour){
                            eventType = parser.next();
                            if(eventType != XmlPullParser.START_TAG){
                                continue;
                            }

                            if(parser.getName().equals("hour")){
                                parser.next();
                                hour = Integer.valueOf(parser.getText());
                                hasFoundHour = true;
                            }
                        }
                        parser.next();
                        skipOutOfTag(parser);
                    } else if(parser.getName().equals("temp")){
                        boolean hasFoundFahrenheit = false;
                        boolean hasFoundCelsius = false;
                        while(!hasFoundFahrenheit || !hasFoundCelsius){
                            eventType = parser.next();
                            if(eventType != XmlPullParser.START_TAG){
                                continue;
                            }

                            if(parser.getName().equals("english")){
                                parser.next();
                                tempFahrenheit = Integer.valueOf(parser.getText());
                                hasFoundFahrenheit = true;
                                parser.next();
                            }

                            if(parser.getName().equals("metric")){
                                parser.next();
                                tempCelsius = Integer.valueOf(parser.getText());
                                hasFoundCelsius = true;
                                parser.next();
                            }
                        }
                        parser.next();
                        skipOutOfTag(parser);
                    } else if(parser.getName().equals("condition")){
                        parser.next();
                        condition = parser.getText();
                    } else if(parser.getName().equals("icon")){
                        parser.next();
                        icon = parser.getText();
                        isDone = true;
                    }
                }
                eventType = parser.next();
            }

            return new Weather(city, state, condition, tempCelsius, tempFahrenheit, hour, icon);

        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error encountered while parsing through weather request");
        }

        return null;
    }

    private static void skipOutOfTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        int currentDepth = parser.getDepth();

        while(parser.getDepth() >= currentDepth){
            parser.next();
        }
    }
}
