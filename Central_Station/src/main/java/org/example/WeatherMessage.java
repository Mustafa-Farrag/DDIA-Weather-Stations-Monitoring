package org.example;

public class WeatherMessage {
    private final int station_id;
    private final long s_no;

    private final String battery_status;
    private final long status_timestamp;
    private final WeatherStatus weather;

    public WeatherMessage(String station_id, String s_no, String battery_status, String timeStamp, WeatherStatus weather) {
        this.station_id = Integer.parseInt(station_id);
        this.s_no = Long.parseLong(s_no);
        this.battery_status = battery_status;
        this.status_timestamp = Long.parseLong(timeStamp);
        this.weather = weather;
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

    public int getStation_id() {
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
