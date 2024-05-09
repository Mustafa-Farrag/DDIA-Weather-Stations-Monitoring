package org.example;


public class Main {
    public static void main(String[] args) {

        CentralStation centralStation = new CentralStation("D:\\Parquets\\");

        Thread centralStationThread = new Thread(() -> centralStation.consume());
        centralStationThread.start();

    }
}