kubectl apply -f Central_Station\central-station.yaml
kubectl apply -f Kafka\zookeeper.yaml
kubectl apply -f Kafka\kafka.yaml
for /l %%i in (1, 1, 10) do (
    kubectl apply -f Weather_Station\weather-station-%%i.yaml
)
pause