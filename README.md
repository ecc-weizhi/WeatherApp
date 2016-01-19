# WeatherApp
A demo android app

Note: In order for the API calls to work, you will need to supply your
own API key in  to be used for Weather Underground API. Fill in the variable API_KEY at 
[WundergroundAPICallManager.java]( app/src/main/java/com/example/weizhi/oddleassignment/network/WundergroundAPICallManager.java). 

See http://www.wunderground.com/weather/api/d/docs?d=index 

The following abilities were demonstrated:
- Making RESTFUL API calls to server.
- Parsing XML using XmlPullParser class
- Using RecyclerView to display a list of items
- Insert/remove items in RecyclerView
- Using AlarmManager to perform operations repeatedly
- Using IntentService to perform asynchronous operations
- Using BroadcastReceiver to respond to system broadcast.
- Sending notifications to notification drawer