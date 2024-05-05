package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherMessageParser {

    public static String extractFieldValue(String message, String field) {
        // Construct the regex pattern to match the field and its value
        String patternString = field + "=([^,}]+)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extract the matched group (field value) from the regex match
            return matcher.group(1).trim();
        }

        // Return null if the field is not found in the message
        return null;
    }

    public static void main(String[] args) {
        String message = "WeatherMessage{" +
                "station_id=123" +
                ", s_no=1" +
                ", battery_status=OK" +
                ", status_timestamp=123456789" +
                ", weather=" + "WeatherStatus{" +
                "humidity=75" +
                ", temperature=25" +
                ", wind_speed=10" +
                '}' +
                '}';

        String extractedValue = extractFieldValue(message, "wind_speed");
        System.out.println("Extracted humidity value: " + extractedValue);
        System.out.println(message);
    }
}