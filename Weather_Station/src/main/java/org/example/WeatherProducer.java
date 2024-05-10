package org.example;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.lang.*;
import java.util.concurrent.TimeUnit;

public class WeatherProducer {
    private static final String TOPIC = "weather-topic";
    private static final String BOOTSTRAP_SERVERS = "kafka:9092";

    public static void produce(int station_id) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        Random random = new Random();

        int s_no = 0;

        try (Producer<String, String> producer = new KafkaProducer<>(props)){

            while (true){
                String key = Integer.toString(s_no);
                String value = new WeatherMessage(station_id, s_no).toString();

                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, value);

                if(random.nextDouble() < 0.1) {
                    System.out.println("msg #"+ s_no +" dropped");
                }else{
                    producer.send(record);
                    producer.flush();
                }

                s_no++;
                TimeUnit.SECONDS.sleep(1);
            }

        }catch (Exception e){
            System.out.println("error from station " + station_id
                    + " while producing weather_msg: " + e);
        }
    }
}
