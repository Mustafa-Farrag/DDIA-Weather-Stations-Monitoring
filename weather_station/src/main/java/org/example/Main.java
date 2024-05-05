package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        Thread producerThread = new Thread(() -> {
            try {
                MyProducer.produce(Integer.parseInt(args[0]));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        producerThread.start();
    }
}