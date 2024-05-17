package org.example;

import java.util.Random;

public class WeatherStatus {
    private final int humidity;
    private final int temperature;
    private final int wind_speed;

    public WeatherStatus(int humidity, int temperature, int wind_speed) {
        this.humidity = humidity;
        this.temperature = temperature;
        this.wind_speed = wind_speed;
    }

    public WeatherStatus() {
        Random random = new Random();
        this.humidity = random.nextInt(0, 101);
        this.temperature = random.nextInt(-40, 123);
        this.wind_speed = random.nextInt(0, 118);
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
