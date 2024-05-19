package org.example;

import org.apache.avro.file.CodecFactory;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.parquet.avro.AvroParquetWriter;

import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.avro.reflect.ReflectData;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import static org.apache.parquet.hadoop.ParquetFileWriter.Mode.CREATE;
import static org.apache.parquet.hadoop.ParquetFileWriter.Mode.OVERWRITE;
import static org.apache.parquet.hadoop.metadata.CompressionCodecName.SNAPPY;

public class MessagesParquetWriter {
    public static void writeParquets(String path, List<WeatherMessage> weatherMessageList) {
        Path dataFile = new Path(path);

        try (ParquetWriter<GenericRecord> writer =  AvroParquetWriter.<GenericRecord>builder(dataFile)
                .withSchema(AvroSchemas.WEATHER_MESSAGE_SCHEMA)
                .withConf(new Configuration())
                .withCompressionCodec(SNAPPY)
                .withWriteMode(OVERWRITE)
                .build()) {
            for (WeatherMessage message : weatherMessageList) {

                GenericRecord weatherStatusRecord = new GenericRecordBuilder(AvroSchemas.WEATHER_STATUS_SCHEMA)
                        .set("humidity", message.getWeather().getHumidity())
                        .set("temperature", message.getWeather().getTemperature())
                        .set("wind_speed", message.getWeather().getWind_speed())
                        .build();

                GenericRecord weatherMessageRecord = new GenericRecordBuilder(AvroSchemas.WEATHER_MESSAGE_SCHEMA)
                        .set("station_id", message.getStation_id())
                        .set("s_no", message.getS_no())
                        .set("battery_status", message.getBattery_status())
                        .set("status_timestamp", message.getStatus_timestamp())
                        .set("weather", weatherStatusRecord)
                        .build();

                writer.write(weatherMessageRecord);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
