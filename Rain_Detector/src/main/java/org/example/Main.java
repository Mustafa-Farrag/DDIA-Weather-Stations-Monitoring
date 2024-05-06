package org.example;


public class Main {
    public static void main(String[] args) {
        Thread rainDetectorThread = new Thread(RainDetectorProcessor::detectRain);
        rainDetectorThread.start();
    }
}