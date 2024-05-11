import argparse

from elasticsearch import Elasticsearch
import pyarrow.parquet as pq
import glob
import os

es = Elasticsearch([{'host': 'localhost', 'port': 9200, 'scheme': 'http'}])


def avro_to_dict(avro_record, i):
    return {
        'station_id': avro_record['station_id'][i],
        's_no': avro_record['s_no'][i],
        'battery_status': avro_record['battery_status'][i],
        'status_timestamp': avro_record['status_timestamp'][i],
        'weather': {
            'humidity': avro_record['weather'][i]['humidity'],
            'temperature': avro_record['weather'][i]['temperature'],
            'wind_speed': avro_record['weather'][i]['wind_speed']
        }
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('parquet_dir', help='Path to the parquet_dir')

    args = parser.parse_args()

    loaded_files = set()

    while True:
        # Construct the directory pattern using os.path.join
        directory_pattern = os.path.join(args.parquet_dir, '*', '*', '*.parquet')

        # Use glob to find files matching the pattern
        parquet_files = glob.glob(directory_pattern)

        for parquet_file in parquet_files:

            if parquet_file in loaded_files:
                continue
            else:
                try:

                    table = pq.read_table(parquet_file)

                    records = table.to_pydict()

                    for i in range(len(records['s_no'])):
                        es.index(index='weather_data', body=avro_to_dict(records, i))

                    loaded_files.add(parquet_file)

                    print("Parquet file: " + parquet_file + " Loaded Successfully")

                except Exception as e:
                    continue


if __name__ == '__main__':
    main()
