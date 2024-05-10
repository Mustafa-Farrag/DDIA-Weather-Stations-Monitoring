package org.example;

import org.apache.parquet.avro.AvroParquetWriter;

import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.reflect.ReflectData;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.ParquetWriter;

import static org.apache.parquet.hadoop.ParquetFileWriter.Mode.CREATE;
import static org.apache.parquet.hadoop.ParquetFileWriter.Mode.OVERWRITE;
import static org.apache.parquet.hadoop.metadata.CompressionCodecName.SNAPPY;

public class MessagesParquetWriter {

    public static void writeParquets(String path, List<WeatherMessage> weatherMessageList) throws RuntimeException {
        Path dataFile = new Path(path);

        try (ParquetWriter<WeatherMessage> writer = AvroParquetWriter.<WeatherMessage>builder(dataFile)
                .withSchema(ReflectData.AllowNull.get().getSchema(WeatherMessage.class))
                .withDataModel(ReflectData.get())
                .withConf(new Configuration())
                .withCompressionCodec(SNAPPY)
                .withWriteMode(OVERWRITE)
                .build()) {
            for (WeatherMessage message : weatherMessageList) {
                writer.write(message);
            }
            System.out.println("done, " + path);
        } catch (IOException e) {
            System.out.println("Hi, Error happened\n" + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }
}
