package org.example;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class RainDetector {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String WEATHER_TOPIC = "weather-topic";
    private static final String RAIN_DETECTION_TOPIC = "rain-detection-topic";

    public static void detectRain() {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "rain-detector-group");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(WEATHER_TOPIC));
            int msg_no = 0;

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {

                    String messageValue = record.value();
                    int humidity = extractHumidity(messageValue);

                    if (humidity > 70) {
                        //  temp initialization
                        RainMessage rainMessage = createRainMessage(messageValue);
                        produceRainEvent(rainMessage);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("error from rain_detector while consuming: " + e);
        }
    }

    private static int extractHumidity(String messageValue) {
        String value = WeatherMessageParser
                .extractFieldValue(messageValue, "humidity");

        if(value == null) return 0;

        return Integer.parseInt(value);
    }

    private static RainMessage createRainMessage(String messageValue) {
        String station_id = WeatherMessageParser
                .extractFieldValue(messageValue, "station_id");
        String s_no = WeatherMessageParser
                .extractFieldValue(messageValue, "s_no");
        String status_timestamp = WeatherMessageParser
                .extractFieldValue(messageValue, "status_timestamp");
        String humidity = WeatherMessageParser
                .extractFieldValue(messageValue, "humidity");

        assert station_id != null;
        assert s_no != null;
        assert status_timestamp != null;
        assert humidity != null;

        return new RainMessage(
                Long.parseLong(station_id),
                Long.parseLong(s_no),
                Long.parseLong(status_timestamp),
                Integer.parseInt(humidity)
        );
    }

    private static void produceRainEvent(RainMessage rainMessage) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {

            String key = String.valueOf(rainMessage.getStation_id());
            String value = rainMessage.toString();
            ProducerRecord<String, String> record = new ProducerRecord<>(RAIN_DETECTION_TOPIC, key, value);
            producer.send(record);

        } catch (Exception e) {
            System.out.println("error from rain_detector while producing rain_event: " + e);
        }
    }
}