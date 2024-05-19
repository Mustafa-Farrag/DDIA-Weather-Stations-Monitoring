kubectl apply -f Central_Station\central-station.yaml
kubectl apply -f Kafka\zookeeper.yaml
kubectl apply -f Kafka\kafka.yaml
kubectl apply -f Elastic_Kibana\elastic.yaml
kubectl apply -f Elastic_Kibana\kibana.yaml
for /l %%i in (1, 1, 10) do (
    kubectl apply -f Weather_Station\weather-station-%%i.yaml
)
pause