package org.example;


public class Main {
    public static void main(String[] args) {
        int station_id = Integer.parseInt(args[0]);
        WeatherProducer.produce(station_id);
    }
}