package org.example;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class AvroSchemas {
    public static final Schema WEATHER_STATUS_SCHEMA = SchemaBuilder.record("WeatherStatus")
            .namespace("org.example.avro")
            .fields()
            .requiredInt("humidity")
            .requiredInt("temperature")
            .requiredInt("wind_speed")
            .endRecord();

    public static final Schema WEATHER_MESSAGE_SCHEMA = SchemaBuilder.record("WeatherMessage")
            .namespace("org.example.avro")
            .fields()
            .requiredInt("station_id")
            .requiredLong("s_no")
            .requiredString("battery_status")
            .requiredLong("status_timestamp")
            .name("weather").type(WEATHER_STATUS_SCHEMA).noDefault()
            .endRecord();
}
