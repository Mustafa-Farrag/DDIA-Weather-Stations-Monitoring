package org.example;

public class WeatherStatus {
    private final int humidity;
    private final int temperature;
    private final int wind_speed;

    public WeatherStatus(String humidity, String temperature, String wind_speed) {
        this.humidity = Integer.parseInt(humidity);
        this.temperature = Integer.parseInt(temperature);
        this.wind_speed = Integer.parseInt(wind_speed);
    }

    @Override
    public String toString() {
        return "WeatherStatus{" +
                "humidity=" + humidity +
                ", temperature=" + temperature +
                ", wind_speed=" + wind_speed +
                '}';
    }

    public int getHumidity() {
        return humidity;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getWind_speed() {
        return wind_speed;
    }
}