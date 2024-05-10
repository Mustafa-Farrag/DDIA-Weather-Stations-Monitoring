package org.example;



public class Main {
    public static void main(String[] args) {
        Thread rainDetectorThread = new Thread(RainDetectorProcessor::detectRain);
        rainDetectorThread.start();

        CentralStation centralStation = new CentralStation(args[0]);

        Thread centralStationThread = new Thread(centralStation::consume);
        centralStationThread.start();

    }
}