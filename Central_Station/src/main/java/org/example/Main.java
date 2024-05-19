package org.example;


import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Thread rainDetectorThread = new Thread(RainDetectorProcessor::detectRain);
        rainDetectorThread.start();

        CentralStation centralStation = new CentralStation(System.getenv("PARQUET_PATH"));
        Thread centralStationThread = new Thread(centralStation::consume);
        centralStationThread.start();

        ElasticParquetReader elasticParquetReader = new ElasticParquetReader(System.getenv("PARQUET_PATH"));
        Thread elasticParquetReaderThread = new Thread(elasticParquetReader::sendParquets);
        elasticParquetReaderThread.start();
    }
}