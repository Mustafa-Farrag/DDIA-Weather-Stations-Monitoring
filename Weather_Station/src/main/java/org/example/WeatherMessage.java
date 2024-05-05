package org.example;

import java.util.Random;

public class WeatherMessage {
    private final long station_id;
    private final long s_no;

    private final String battery_status;
    private final long status_timestamp;
    private final WeatherStatus weather;

    public WeatherMessage(long station_id, long s_no) {
        this.station_id = station_id;
        this.s_no = s_no;

        double randomNumber = new Random().nextDouble();
        if (randomNumber < 0.3) {
            this.battery_status = "low";
        } else if (randomNumber < 0.3 + 0.4) {
            this.battery_status = "medium";
        } else {
            this.battery_status = "high";
        }

        this.status_timestamp = System.currentTimeMillis();
        this.weather = new WeatherStatus();
    }

    @Override
    public String toString() {
        return "WeatherMessage{" +
                "station_id=" + station_id +
                ", s_no=" + s_no +
                ", battery_status=" + battery_status +
                ", status_timestamp=" + status_timestamp +
                ", weather=" + weather +
                '}';
    }

    public long getStation_id() {
        return station_id;
    }

    public long getS_no() {
        return s_no;
    }

    public String getBattery_status() {
        return battery_status;
    }

    public long getStatus_timestamp() {
        return status_timestamp;
    }

    public WeatherStatus getWeather() {
        return weather;
    }
}
