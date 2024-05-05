package org.example;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

public class RainDetectorStream {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String INPUT_TOPIC = "weather-topic";
    private static final String OUTPUT_TOPIC = "rain-detection-topic";

    public static void detectRain() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "rain-detector-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> weatherStream = builder.stream(INPUT_TOPIC);

        weatherStream.filter((key, value) -> extractHumidity(value) > 70)
                .mapValues(RainDetectorStream::createRainMessage)
                .to(OUTPUT_TOPIC);

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static int extractHumidity(String messageValue) {
        String value = WeatherMessageParser
                .extractFieldValue(messageValue, "humidity");

        assert value != null;

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
}

