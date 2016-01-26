# WeatherApp
A demo android app

Note: In order for the API calls to work, you will need to supply your
own API key in  to be used for Weather Underground API. Fill in the variable API_KEY at 
[HourlyApiRequest.java]( app/src/main/java/com/example/weizhi/oddleassignment/network/HourlyApiRequest.java). 

See http://www.wunderground.com/weather/api/d/docs?d=index 

The following abilities were demonstrated:
- Making RESTFUL API calls to server.
- Uses [RoboSpice](https://github.com/stephanenicolas/robospice) to perform network request.
- Parsing JSON using [Gson](https://github.com/google/gson).
- Using RecyclerView to display a list of items
- Insert/remove items in RecyclerView
- Using AlarmManager to perform operations repeatedly
- Using IntentService to perform asynchronous operations
- Using BroadcastReceiver to respond to system broadcast.
- Sending notifications to notification drawer
