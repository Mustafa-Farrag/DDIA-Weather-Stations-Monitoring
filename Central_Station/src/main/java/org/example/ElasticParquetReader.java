package org.example;

import org.apache.avro.generic.GenericRecord;

import org.apache.http.HttpHost;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import org.elasticsearch.client.RequestOptions;


import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ElasticParquetReader {

    private String parquetDir;

    ElasticParquetReader(String parquetDir){
        this.parquetDir = parquetDir;
    }
    public void sendParquets() {

        Set<String> loadedFiles = new HashSet<>();

        while (true) {
            Path directory = Paths.get(this.parquetDir);
            try {
                Files.walk(directory)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".parquet"))
                        .forEach(parquetFile -> {
                            if (loadedFiles.contains(parquetFile.toString())) {
                                return;
                            }

                            try {
                                List<String> records = readRecords(parquetFile.toString());
                                sendToElasticsearch(records);
                                loadedFiles.add(parquetFile.toString());
                                System.out.println("Parquet file: " + parquetFile + " Loaded Successfully");
                            } catch (Exception e) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static List<String> readRecords(String parquetFile) throws IOException {

        List<String> records = new ArrayList<>();
        ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(new org.apache.hadoop.fs.Path(parquetFile))
                .build();

        GenericRecord nextRecord;
        while ((nextRecord = reader.read()) != null) {
            records.add(nextRecord.toString());
        }
        return records;
    }

    private static void sendToElasticsearch(List<String> records) {
        try (RestClient restClient = RestClient.builder(
                        new HttpHost(
                                System.getenv("ELASTIC_SERVICE"),
                                Integer.parseInt(System.getenv("ELASTIC_PORT")),
                        "http")
                        ).build()) {

            String index = "weather_data";
            String type = "_doc";

            for (String record : records) {
                String endpoint = "/" + index + "/" + type;
                String jsonString = record; // Assuming each record is a JSON string

                Request request = new Request("POST", endpoint);
                request.setJsonEntity(jsonString);

                Response response = restClient.performRequest(request);
            }
        } catch (IOException e) {
            // Handle IOException
            e.printStackTrace();
        }
    }
}