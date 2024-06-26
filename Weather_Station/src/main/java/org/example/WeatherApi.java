package org.example;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.InputStreamReader;
import java.net.URL;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.Random;


public class WeatherApi {
    public static JSONArray time;
    public static JSONArray temperature;
    public static JSONArray windSpeed;
    public static JSONArray humidity;
    private static int currentIdx = 1;
    private static final Random random = new Random();


    public static void getWeather() throws ParseException, IOException {
        currentIdx = 1;
        String requestUrl = "https://api.open-meteo.com/v1/forecast?latitude=" +
                System.getenv("LATITUDE") +
                "&longitude=" +
                System.getenv("LONGITUDE") +
                "&minutely_15=temperature_2m,relative_humidity_2m,wind_speed_10m" +
                "&timezone=Africa%2FCairo";

        URL url = new URL(requestUrl);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(new InputStreamReader(url.openStream()));
        JSONObject weatherData = (JSONObject) json.get("minutely_15");

        time = (JSONArray) weatherData.get("time");
        temperature = (JSONArray) weatherData.get("temperature_2m");
        windSpeed = (JSONArray) weatherData.get("wind_speed_10m");
        humidity = (JSONArray) weatherData.get("relative_humidity_2m");

        setIdx();
    }

    public static void setIdx() throws ParseException, IOException {
        long currentTime = System.currentTimeMillis();

        for(int i=currentIdx; i<time.size()-1; i++){
            if(currentTime < dateToMillis((String) time.get(i+1))){
                currentIdx = i;
                return;
            }
        }

        if(currentIdx == time.size() - 1)
            getWeather();
    }

    private static long dateToMillis(String dateTimeString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
        Instant instant = localDateTime.toInstant(ZoneOffset.ofHours(3));

        return instant.toEpochMilli();
    }

    public static int getHumidity(){
        double val = random.nextDouble();
        if(val < 0.3)
            return (int) ((long) humidity.get(currentIdx - 1));
        else if(val < 0.6)
            return (int) ((long) humidity.get(currentIdx));
        else
            return (int) ((long) humidity.get(currentIdx + 1));
    }

    public static int getTemperature(){
        double val = random.nextDouble();
        if(val < 0.3)
            return (int) ((double) temperature.get(currentIdx - 1));
        else if(val < 0.6)
            return (int) ((double) temperature.get(currentIdx));
        else
            return (int) ((double) temperature.get(currentIdx + 1));
    }

    public static int getWindSpeed(){
        double val = random.nextDouble();
        if(val < 0.3)
            return (int) ((double) windSpeed.get(currentIdx - 1));
        else if(val < 0.6)
            return (int) ((double) windSpeed.get(currentIdx));
        else
            return (int) ((double) windSpeed.get(currentIdx + 1));
    }

    public static String getTime(){
        return (String) time.get(currentIdx);
    }
}
