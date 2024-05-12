package org.example;



public class Main {
    public static void main(String[] args) {
        Thread rainDetectorThread = new Thread(RainDetectorProcessor::detectRain);
        rainDetectorThread.start();

        CentralStation centralStation = new CentralStation(System.getenv("PARQUET_PATH"));

        Thread centralStationThread = new Thread(centralStation::consume);
        centralStationThread.start();
    }
}