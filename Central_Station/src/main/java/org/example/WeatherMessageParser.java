package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class WeatherMessageParser {
    public static String extractFieldValue(String message, String field) {
        String patternString = field + "=([^,}]+)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    public static WeatherMessage parse(String message){
        WeatherStatus weatherStatus = new WeatherStatus(
                extractFieldValue(message, "humidity"),
                extractFieldValue(message, "temperature"),
                extractFieldValue(message, "wind_speed"));

        return new WeatherMessage(
                extractFieldValue(message, "station_id"),
                extractFieldValue(message, "s_no"),
                extractFieldValue(message, "battery_status"),
                extractFieldValue(message, "status_timestamp"),
                weatherStatus);
    }


}
