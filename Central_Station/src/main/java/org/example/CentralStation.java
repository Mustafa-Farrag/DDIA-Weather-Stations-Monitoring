package org.example;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;


public class CentralStation {

    private static final String TOPIC = "weather-topic";
    private static final String BOOTSTRAP_SERVERS = System.getenv("KAFKA_BOOTSTRAP");

    private final WeatherMessageHandler weatherMessageHandler;

    public void consume() {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "central-station-group");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TOPIC));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    String messageValue = record.value();
                    System.out.println("msg: "+messageValue);
                    weatherMessageHandler.addMessage(messageValue);
                }
            }
        } catch (Exception e) {
            System.out.println("Error :: " + e);
        }
    }

    public CentralStation(String storageBaseDir){
        this.weatherMessageHandler = new WeatherMessageHandler(storageBaseDir);
    }
}
