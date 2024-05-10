package org.example;

public class RainMessage {
    private final long station_id;
    private final long s_no;
    private final long status_timestamp;
    private final int humidity;

    public RainMessage(long station_id, long s_no, long status_timestamp, int humidity) {
        this.station_id = station_id;
        this.s_no = s_no;
        this.status_timestamp = status_timestamp;
        this.humidity = humidity;
    }

    @Override
    public String toString() {
        return "RainMessage{" +
                "station_id=" + station_id +
                ", s_no=" + s_no +
                ", status_timestamp=" + status_timestamp +
                ", humidity=" + humidity +
                '}';
    }

    public long getStation_id() {
        return station_id;
    }

    public long getS_no() {
        return s_no;
    }

    public long getStatus_timestamp() {
        return status_timestamp;
    }

    public int getHumidity() {
        return humidity;
    }
}
