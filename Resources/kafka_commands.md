## list all topics
    /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

## delete all topic
    /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic '.*'

## test weather-topic
    /opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic weather-topic --from-beginning

## test rain-detection-topic
    /opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic rain-detection-topic --from-beginning