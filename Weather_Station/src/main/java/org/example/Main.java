package org.example;


public class Main {
    public static void main(String[] args) {
        int station_id = Integer.parseInt(System.getenv("STATION_ID"));
        WeatherProducer.produce(station_id);
    }
}